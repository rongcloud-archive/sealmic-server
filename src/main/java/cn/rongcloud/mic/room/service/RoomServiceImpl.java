package cn.rongcloud.mic.room.service;

import static java.util.stream.Collectors.toList;

import cn.rongcloud.common.im.ChrmEntrySetInfo;
import cn.rongcloud.common.im.IMHelper;
import cn.rongcloud.common.im.pojos.IMApiResultInfo;
import cn.rongcloud.common.im.pojos.IMChatRoomUserResult;
import cn.rongcloud.common.im.pojos.IMChrmWhiteListResult;
import cn.rongcloud.common.im.pojos.IMIsInChrmResult;
import cn.rongcloud.common.im.pojos.IMRoomUserInfo;
import cn.rongcloud.common.jwt.JwtUser;
import cn.rongcloud.common.utils.IMSignUtil;
import cn.rongcloud.mic.common.redis.RedisLockService;
import cn.rongcloud.mic.common.rest.RestResult;
import cn.rongcloud.mic.common.rest.RestResultCode;
import cn.rongcloud.common.utils.DateTimeUtils;
import cn.rongcloud.common.utils.GsonUtil;
import cn.rongcloud.common.utils.IdentifierUtils;
import cn.rongcloud.mic.constant.CustomerConstant;
import cn.rongcloud.mic.room.dao.RoomDao;
import cn.rongcloud.mic.room.enums.ChartRoomSyncType;
import cn.rongcloud.mic.room.enums.ChrmKvChangeType;
import cn.rongcloud.mic.room.enums.MicPositionState;
import cn.rongcloud.mic.room.enums.RoomType;
import cn.rongcloud.mic.room.enums.RoomUserGagOperation;
import cn.rongcloud.mic.room.enums.TakeOverHostCmd;
import cn.rongcloud.mic.room.enums.TransferHostCmd;
import cn.rongcloud.mic.user.enums.UserType;
import cn.rongcloud.mic.room.message.ChrmKVNoticeMessage;
import cn.rongcloud.mic.room.message.ChrmKVNoticeMessage.ChrmKVChange;
import cn.rongcloud.mic.room.message.ChrmSysMessage;
import cn.rongcloud.mic.room.message.TakeOverHostMessage;
import cn.rongcloud.mic.room.message.TransferHostMessage;
import cn.rongcloud.mic.room.model.TRoom;
import cn.rongcloud.mic.user.model.TUser;
import cn.rongcloud.mic.room.pojos.MicPositionInfo;
import cn.rongcloud.mic.room.pojos.ReqBroadcastMessage;
import cn.rongcloud.mic.room.pojos.ReqMicStateSet;
import cn.rongcloud.mic.room.pojos.ReqRoomCreate;
import cn.rongcloud.mic.room.pojos.ReqRoomId;
import cn.rongcloud.mic.room.pojos.ReqRoomMicAccept;
import cn.rongcloud.mic.room.pojos.ReqRoomMicInvite;
import cn.rongcloud.mic.room.pojos.ReqRoomMicKick;
import cn.rongcloud.mic.room.pojos.ReqRoomMicReject;
import cn.rongcloud.mic.room.pojos.ReqRoomSetting;
import cn.rongcloud.mic.room.pojos.ReqRoomStatusSync;
import cn.rongcloud.mic.room.pojos.ReqRoomUserGag;
import cn.rongcloud.mic.room.pojos.ReqRoomUserKick;
import cn.rongcloud.mic.room.pojos.ReqTakeOverHost;
import cn.rongcloud.mic.room.pojos.ReqTransferHost;
import cn.rongcloud.mic.room.pojos.ResRoomCreate;
import cn.rongcloud.mic.room.pojos.ResRoomInfo;
import cn.rongcloud.mic.room.pojos.ResRoomList;
import cn.rongcloud.mic.room.pojos.RespRoomUser;
import cn.rongcloud.mic.user.service.UserService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Created by sunyinglong on 2020/6/3
 */
@Slf4j
@Service
public class RoomServiceImpl implements RoomService {

    @Autowired
    RoomDao roomDao;

    @Autowired
    UserService userService;

    @Autowired
    IMHelper imHelper;

    @Autowired
    IMSignUtil imSignUtil;

    @Resource
    RedisTemplate<String, Object> redisTemplate;

    @Resource(name = "roomRedisTemplate")
    private HashOperations<String, String, TRoom> roomHashOperations;

    @Resource(name = "micPositionRedisTemplate")
    private HashOperations<String, String, MicPositionInfo> micPositionHashOperations;

    @Resource(name = "redisTemplate")
    private ZSetOperations<String, String> zSetOperations;

    @Resource(name = "redisTemplate")
    private SetOperations<String, String> setOperations;

    @Autowired
    private RedisLockService redisLockService;

    @Value("${sealmic.domain}")
    private String domain;

    @Value("${sealmic.mic.transfer_host_expire}")
    private Long transferHostExpire;

    @Value("${sealmic.mic.takeover_host_expire}")
    private Long takeoverHostExpire;

    private static final String MIC_POSSION_KEY_PREFIX = "sealmic_position_";
    private static final String APPLIED_MIC_LIST_EMPTY_KEY = "applied_mic_list_empty";

    @Override
    public RestResult createRoom(ReqRoomCreate data, JwtUser jwtUser) throws Exception {
        //只有注册用户可操作
        if (!jwtUser.getType().equals(UserType.USER.getValue())) {
            return RestResult.generic(RestResultCode.ERR_ACCESS_DENIED);
        }
        //创建 IM 聊天室
        String chatRoomId = IdentifierUtils.uuid();
        IMApiResultInfo resultInfo = imHelper.createChatRoom(chatRoomId, data.getName());
        if (!resultInfo.isSuccess()) {
            log.error("create chatRoom error: {}, {}", jwtUser, resultInfo.getErrorMessage());
            return RestResult.generic(RestResultCode.ERR_ROOM_CREATE_ROOM_ERROR, resultInfo.getErrorMessage());
        }

        //初始化麦位信息
        initMicPositions(chatRoomId, jwtUser.getUserId());

        //将房间信息保存至数据库
        Date date = DateTimeUtils.currentUTC();
        TRoom room = new TRoom();
        room.setUid(chatRoomId);
        room.setName(data.getName());
        room.setThemePictureUrl(data.getThemePictureUrl());
        room.setType(RoomType.CUSTOM.getValue());
        room.setAllowedJoinRoom(true);
        room.setAllowedFreeJoinMic(true);
        room.setCreateDt(date);
        room.setUpdateDt(date);
        roomDao.save(room);

        //将房间信息缓存至 Redis Hash
        updateRoomCache(room);
        //将房间 Id 缓存至 Redis Zset，房间分页查询数据源
        zSetOperations.add(getRedisRoomIdsKey(), chatRoomId, date.getTime());

        //构建返回信息
        ResRoomCreate result = new ResRoomCreate();
        result.setRoomId(chatRoomId);
        result.setRoomName(data.getName());
        result.setThemePictureUrl(data.getThemePictureUrl());
        result.setType(room.getType());
        result.setCreateDt(date);

        return RestResult.success(result);
    }

    @Override
    public RestResult getRoomDetail(String roomId) {
        TRoom room = getRoomInfo(roomId);
        if (room == null) {
            return RestResult.generic(RestResultCode.ERR_ROOM_NOT_EXIST);
        }
        return RestResult.success(build(room));
    }

    @Override
    public RestResult getRoomList(String fromRoomId, Integer size) {

        Long from = 0L;
        if (!StringUtils.isEmpty(fromRoomId)) {
            from = zSetOperations.rank(getRedisRoomIdsKey(), fromRoomId);
        }
        if (from == null) {
            from = 0L;
        } else {
            from = from + 1;
        }

        ResRoomList resRoomList = new ResRoomList();
        //从 redis 分页查询
        Set<String> roomIds = zSetOperations.range(getRedisRoomIdsKey(), from, (from + size -1));
        if (roomIds == null || roomIds.isEmpty()) {
            resRoomList.setTotalCount(0L);
            return RestResult.success(resRoomList);
        }

        //从 redis 查询房间总数
        Long totalCount = zSetOperations.size(getRedisRoomIdsKey());
        resRoomList.setTotalCount(totalCount);

        //批量从 Redis 查询房间数据
        List<ResRoomInfo> roomInfos = new ArrayList<>();
        Map<String, TRoom> roomMap = hmget(getRedisRoomInfosKey(), new ArrayList<>(roomIds));
        for (String roomId: roomIds) {
            if (roomMap.containsKey(roomId)) {
                roomInfos.add(build(roomMap.get(roomId)));
            }
        }
        resRoomList.setRooms(roomInfos);

        return RestResult.success(resRoomList);
    }

    @Override
    public RestResult roomSetting(ReqRoomSetting data, JwtUser jwtUser) {
        //检查房间是否存在
        TRoom room = getRoomInfo(data.getRoomId());
        if (room == null) {
            return RestResult.generic(RestResultCode.ERR_ROOM_NOT_EXIST);
        }
        //检查当前用户是否为主持人, 0 为主持人麦位
        if (!isPresenter(data.getRoomId(), jwtUser.getUserId())) {
            return RestResult.generic(RestResultCode.ERR_ACCESS_DENIED);
        }

        boolean needUpdate =  false;

        //设置房间是否允许观众加入
        if (data.getAllowedJoinRoom() != null && room.isAllowedJoinRoom() != data.getAllowedJoinRoom()) {
            room.setAllowedJoinRoom(data.getAllowedJoinRoom());
            needUpdate = true;
        }
        //设置是否允许观众自由上麦
        if (data.getAllowedFreeJoinMic() != null && room.isAllowedFreeJoinMic() != data.getAllowedFreeJoinMic()) {
            room.setAllowedFreeJoinMic(data.getAllowedFreeJoinMic());
            needUpdate = true;
        }

        if (needUpdate) {
            //更新数据库
            roomDao.updateRoomSetting(room.getUid(), room.isAllowedJoinRoom(), room.isAllowedFreeJoinMic());
            //更新 Redis
            updateRoomCache(room);
        }

        return RestResult.success();
    }

    /**
     * 判断用户在某个聊天室是否是主持人
     * @param roomId
     * @param userId
     * @return
     */
    private boolean isPresenter(String roomId, String userId) {
        MicPositionInfo micPositionInfo = getMicPosition(roomId, 0);
        return micPositionInfo != null && micPositionInfo.getUserId() != null && micPositionInfo
            .getUserId().equals(userId);
    }

    @Override
    public RestResult roomUserGag(ReqRoomUserGag data, JwtUser jwtUser) throws Exception {

        if (data.getUserIds().isEmpty()) {
            return RestResult.generic(RestResultCode.ERR_REQUEST_PARA_ERR);
        }
        //每次最多只能设置 20 个用户
        if (data.getUserIds().size() > 20) {
            return RestResult.generic(RestResultCode.ERR_ROOM_USER_IDS_SIZE_EXCEED);
        }
        //检查房间是否存在
        TRoom room = getRoomInfo(data.getRoomId());
        if (room == null) {
            return RestResult.generic(RestResultCode.ERR_ROOM_NOT_EXIST);
        }
        //检查当前用户是否为主持人
        if (!isPresenter(data.getRoomId(), jwtUser.getUserId())) {
            return RestResult.generic(RestResultCode.ERR_ACCESS_DENIED);
        }
        //IM 设置用户禁言
        IMApiResultInfo resultInfo;
        if (data.getOperation().equals(RoomUserGagOperation.ADD.getValue())) {
            resultInfo = imHelper.addGagChatroomUser(data.getRoomId(), data.getUserIds(), null);
        } else if (data.getOperation().equals(RoomUserGagOperation.REMOVE.getValue())) {
            resultInfo = imHelper.removeGagChatroomUser(data.getRoomId(), data.getUserIds());
        } else {
            return RestResult.generic(RestResultCode.ERR_REQUEST_PARA_ERR);
        }
        if (!resultInfo.isSuccess()) {
            return RestResult.generic(RestResultCode.ERR_ROOM_ADD_GAG_USER_ERROR);
        }

        //将禁言用户信息保存至 Redis
        if (data.getOperation().equals(RoomUserGagOperation.ADD.getValue())) {
            setOperations.add(getRedisRoomGagUserKey(data.getRoomId()), data.getUserIds().toArray(new String[0]));
        } else if (data.getOperation().equals(RoomUserGagOperation.REMOVE.getValue())) {
            setOperations.remove(getRedisRoomGagUserKey(data.getRoomId()), data.getUserIds().toArray(new String[0]));
        }

        return RestResult.success();
    }

    @Override
    public RestResult getRoomMembers(String roomId) throws Exception {
        List<RespRoomUser> outs = new ArrayList<>();
        //检查房间是否存在
        TRoom room = getRoomInfo(roomId);
        if (room == null) {
            return RestResult.generic(RestResultCode.ERR_ROOM_NOT_EXIST);
        }
        //从 IM 聊天室获取成员
        IMChatRoomUserResult result = imHelper.queryChatroomUser(roomId, 50, 1);
        if (result == null || !result.isSuccess() || result.getUsers() == null || result.getUsers().isEmpty()) {
            return RestResult.success(outs);
        }
        List<IMRoomUserInfo> userInfos = result.getUsers();
        for (IMRoomUserInfo roomUserInfo: userInfos) {
            TUser user = userService.getUserInfo(roomUserInfo.getId());
            if (user != null) {
                RespRoomUser out = new RespRoomUser();
                out.setUserId(user.getUid());
                out.setUserName(user.getName());
                out.setPortrait(user.getPortrait());
                outs.add(out);
            }
        }

        return RestResult.success(outs);
    }

    @Override
    public RestResult queryGagRoomUsers(String roomId, JwtUser jwtUser) {

        List<RespRoomUser> outs = new ArrayList<>();

        //检查房间是否存在
        TRoom room = getRoomInfo(roomId);
        if (room == null) {
            return RestResult.generic(RestResultCode.ERR_ROOM_NOT_EXIST);
        }

        //查询禁言用户
        Set<String> gagUserIds = setOperations.members(getRedisRoomGagUserKey(roomId));
        if (gagUserIds == null || gagUserIds.isEmpty()) {
            return RestResult.success(outs);
        }

        for (String gagUserId: gagUserIds) {
            RespRoomUser out =  new RespRoomUser();
            TUser user = userService.getUserInfo(gagUserId);
            if (user != null) {
                out.setUserId(user.getUid());
                out.setUserName(user.getName());
                out.setPortrait(user.getPortrait());
                outs.add(out);
            }
        }

        return RestResult.success(outs);
    }

    @Async
    @Override
    public void chrmStatusSync(List<ReqRoomStatusSync> data, Long signTimestamp, String nonce, String signature) {

        //校验签名是否正确
        if (!imSignUtil.validSign(nonce, signTimestamp.toString(), signature)) {
            log.info("Access denied: signature error, signTimestamp:{}, nonce:{}, signature:{}", signTimestamp, nonce, signature);
            return;
        }

        if (data == null || data.isEmpty()) {
            return;
        }

        //同步聊天室状态
        for (ReqRoomStatusSync status: data) {
            TRoom room = getRoomInfo(status.getChatRoomId());
            if (room == null) {
                continue;
            }
            dealChrm(room, status);
        }
    }

    private void dealChrm(TRoom room, ReqRoomStatusSync status){
        if (status.getType() == ChartRoomSyncType.DESTORY.getValue()) {
            if (room.getType() == RoomType.OFFICIAL.getValue()) {
                //重置房间属性
                room.setAllowedJoinRoom(true);
                room.setAllowedFreeJoinMic(true);
                roomDao.updateRoomSetting(status.getChatRoomId(), true, false);
                updateRoomCache(room);

            } else if (room.getType() == RoomType.CUSTOM.getValue()) {
                //从缓存中删除房间对应的 Key
                redisTemplate.opsForHash().delete(getRedisRoomInfosKey(), status.getChatRoomId());
                redisTemplate.opsForZSet().remove(getRedisRoomIdsKey(), status.getChatRoomId());
                //从数据库中删除房间
                roomDao.deleteTRoomByUidEquals(room.getUid());
            }

            //删除缓存麦位信息
            redisTemplate.delete(getRedisRoomMicPositionKey(status.getChatRoomId()));
            //删除排麦列表
            redisTemplate.delete(getRedisApplyMicMembersKey(status.getChatRoomId()));
            //删除禁言用户列表
            redisTemplate.delete(getRedisRoomGagUserKey(status.getChatRoomId()));
        }

        if (status.getType() == ChartRoomSyncType.CREATE.getValue() && room.getType() == RoomType.OFFICIAL.getValue()) {

            //官方 IM 聊天室创建，是由客户端触发；接收到回调后需设置 KV 信息
            try {
                //初始化麦位信息
                initMicPositions(status.getChatRoomId(), "");
            } catch (Exception e) {
                log.info("set mic position KV failed, data:{}", GsonUtil.toJson(status));
            }
        }
    }

    @Override
    public RestResult roomUserKick(ReqRoomUserKick data, JwtUser jwtUser) throws Exception {
        //每次最多只能操作 20 个用户
        if (data.getUserIds().size() > 20) {
            return RestResult.generic(RestResultCode.ERR_ROOM_USER_IDS_SIZE_EXCEED);
        }
        //检查房间是否存在
        TRoom room = getRoomInfo(data.getRoomId());
        if (room == null) {
            return RestResult.generic(RestResultCode.ERR_ROOM_NOT_EXIST);
        }
        //检查当前用户是否为主持人, 0 为主持人麦位
        if (!isPresenter(data.getRoomId(), jwtUser.getUserId())) {
            return RestResult.generic(RestResultCode.ERR_ACCESS_DENIED);
        }
        //检查用户是否在麦位，如果在麦位需进行退麦操作
        for (String userId: data.getUserIds()) {
            quitMic(data.getRoomId(), userId);
        }

        //发送通知
        ChrmSysMessage changeMessage = new ChrmSysMessage();
        changeMessage.setOperatorId(jwtUser.getUserId());
        changeMessage.setOperatorName(jwtUser.getUserName());
        changeMessage.setRoomId(data.getRoomId());
        changeMessage.setType(0);
        imHelper.publishSysMessage(jwtUser.getUserId(), data.getUserIds(), changeMessage);

        return RestResult.generic(RestResultCode.ERR_SUCCESS);
    }

    @Override
    public RestResult roomMicApply(ReqRoomId data, JwtUser jwtUser) throws Exception {
        //检查房间是否存在
        TRoom room = getRoomInfo(data.getRoomId());
        if (room == null) {
            return RestResult.generic(RestResultCode.ERR_ROOM_NOT_EXIST);
        }
        //检查用户是否在聊天室
        boolean isInChrm = isInChrm(data.getRoomId(), jwtUser.getUserId());
        if (!isInChrm) {
            return RestResult.generic(RestResultCode.ERR_ROOM_USER_IS_NOT_IN);
        }
        //检查用户是否在麦位，在麦位也不允许申请
        MicPositionInfo micPosition = getMicPosition(data.getRoomId(), jwtUser.getUserId());
        if (micPosition != null) {
            return RestResult.generic(RestResultCode.ERR_ROOM_USER_IS_ALREADY_IN_MIC);
        }
        //检查聊天室是否允许观众自由上麦
        if (room.isAllowedFreeJoinMic()) {
            return synchronizedJoinMic(data.getRoomId(), jwtUser.getUserId());
        }
        //将用户加入排麦列表，检查用户是否在排麦列表
        Set<String> userIds = getApplyMicList(data.getRoomId());
        if (userIds != null && userIds.contains(jwtUser.getUserId())) {
            return RestResult.generic(RestResultCode.ERR_ROOM_USER_IS_APPLIED_FOR_MIC);
        }
        //将用户加入排麦列表
        addApplyMicList(data.getRoomId(), jwtUser.getUserId());

        //设置KV，是否有人在排麦，告诉主持人，红点提醒; 0 无人排麦，1 有人排麦
        if (userIds == null || userIds.isEmpty()) {
            updateAppliedMicListEmpty(data.getRoomId(), 1);
        }

        return RestResult.success();
    }

    private void updateAppliedMicListEmpty(String roomId, Integer value) throws Exception {
        ChrmKVNoticeMessage msg = new ChrmKVNoticeMessage();
        msg.setKey(APPLIED_MIC_LIST_EMPTY_KEY);
        msg.setValue(value.toString());
        msg.setType(1);

        ChrmEntrySetInfo entrySetInfo = new ChrmEntrySetInfo();
        entrySetInfo.setChrmId(roomId);
        entrySetInfo.setUserId(CustomerConstant.SYSTEM_USER_ID);
        entrySetInfo.setKey(APPLIED_MIC_LIST_EMPTY_KEY);
        entrySetInfo.setValue(value.toString());
        entrySetInfo.setObjectName(msg.getObjectName());
        entrySetInfo.setContent(GsonUtil.toJson(msg));
        entrySetInfo.setAutoDelete(false);
        imHelper.entrySet(entrySetInfo, 5);
    }

    @Override
    public RestResult roomMicReject(ReqRoomMicReject data, JwtUser jwtUser) throws Exception {
        //检查房间是否存在
        TRoom room = getRoomInfo(data.getRoomId());
        if (room == null) {
            return RestResult.generic(RestResultCode.ERR_ROOM_NOT_EXIST);
        }
        //检查当前用户是否为主持人, 0 为主持人麦位
        if (!isPresenter(data.getRoomId(), jwtUser.getUserId())) {
            return RestResult.generic(RestResultCode.ERR_ACCESS_DENIED);
        }
        //将用户从排麦列表中移除
        removeApplyMicList(data.getRoomId(), data.getUserId());

        //是否还有人在排麦，更新KV，告诉主持人，红点提醒; 0 无人排麦，1 有人排麦
        Set<String> userIds = getApplyMicList(data.getRoomId());
        if (userIds == null || userIds.isEmpty()) {
            updateAppliedMicListEmpty(data.getRoomId(), 0);
        }
        return RestResult.success();
    }

    @Override
    public RestResult roomMicAccept(ReqRoomMicAccept data, JwtUser jwtUser) throws Exception {
        //检查房间是否存在
        TRoom room = getRoomInfo(data.getRoomId());
        if (room == null) {
            return RestResult.generic(RestResultCode.ERR_ROOM_NOT_EXIST);
        }
        //检查当前用户是否为主持人, 0 为主持人麦位
        if (!isPresenter(data.getRoomId(), jwtUser.getUserId())) {
            return RestResult.generic(RestResultCode.ERR_ACCESS_DENIED);
        }
        //检查用户是否在排麦列表
        boolean isInApplyMicList = isInApplyMicList(data.getRoomId(), data.getUserId());
        if (!isInApplyMicList) {
            return RestResult.generic(RestResultCode.ERR_ROOM_USER_IS_NOT_APPLIED_FOR_MIC);
        }
        //进行上麦操作
        RestResult result = joinMic(data.getRoomId(), data.getUserId());
        if (!result.isSuccess()) {
            return result;
        }

        //将用户从排麦列表中移除
        removeApplyMicList(data.getRoomId(), data.getUserId());

        //是否还有人在排麦，更新KV，告诉主持人，红点提醒; 0 无人排麦，1 有人排麦
        Set<String> userIds = getApplyMicList(data.getRoomId());
        if (userIds == null || userIds.isEmpty()) {
            updateAppliedMicListEmpty(data.getRoomId(), 0);
        }
        return RestResult.success();
    }

    @Override
    public RestResult getMicApplyMembers(String roomId, JwtUser jwtUser) {

        List<RespRoomUser> outs = new ArrayList<>();
        //检查房间是否存在
        TRoom room = getRoomInfo(roomId);
        if (room == null) {
            return RestResult.generic(RestResultCode.ERR_ROOM_NOT_EXIST);
        }

        //获取排麦用户
        Set<String> memberIds = getApplyMicList(roomId);
        if (memberIds == null || memberIds.isEmpty()) {
            return RestResult.success(outs);
        }
        for (String memberId: memberIds) {
            TUser user = userService.getUserInfo(memberId);
            if (user != null) {
                RespRoomUser out =  new RespRoomUser();
                out.setUserId(user.getUid());
                out.setUserName(user.getName());
                out.setPortrait(user.getPortrait());
                outs.add(out);
            }
        }
        return RestResult.success(outs);
    }

    @Override
    public RestResult setMicState(ReqMicStateSet data, JwtUser jwtUser) throws Exception {
        String roomId = data.getRoomId();
        //检查房间是否存在
        TRoom room = getRoomInfo(roomId);
        if (room == null) {
            return RestResult.generic(RestResultCode.ERR_ROOM_NOT_EXIST);
        }
        //检查当前用户是否在麦上
        MicPositionInfo micPositionInfo = getMicPosition(roomId, jwtUser.getUserId());
        if (micPositionInfo == null) {
            return RestResult.generic(RestResultCode.ERR_ACCESS_DENIED);
        }
        //如果用户为主持人，则可以设置所有麦位，否则只能设置自己麦位
        if (micPositionInfo.getPosition() != 0 && data.getPosition() != micPositionInfo.getPosition().intValue()) {
            return RestResult.generic(RestResultCode.ERR_ACCESS_DENIED);
        }

        //获取麦位信息
        MicPositionInfo positionInfo = getMicPosition(roomId, data.getPosition());
        if (positionInfo == null) {
            return RestResult.generic(RestResultCode.ERR_REQUEST_PARA_ERR);
        }
        if (positionInfo.getState().intValue() == data.getState()) {
            return RestResult.success();
        }
        positionInfo.setState(data.getState());
        //更新麦位信息
        saveMicPosition(roomId, ChrmKvChangeType.STATE_SET.getValue(), positionInfo);
        return RestResult.success();
    }

    @Override
    public RestResult micQuit(ReqRoomId data, JwtUser jwtUser) throws Exception {
        //检查房间是否存在
        TRoom room = getRoomInfo(data.getRoomId());
        if (room == null) {
            return RestResult.generic(RestResultCode.ERR_ROOM_NOT_EXIST);
        }
        //进行退麦操作
        return quitMic(data.getRoomId(), jwtUser.getUserId());
    }

    @Override
    public RestResult micKick(ReqRoomMicKick data, JwtUser jwtUser) throws Exception {
        //检查房间是否存在
        TRoom room = getRoomInfo(data.getRoomId());
        if (room == null) {
            return RestResult.generic(RestResultCode.ERR_ROOM_NOT_EXIST);
        }
        //检查当前用户是否为主持人, 0 为主持人麦位
        if (!isPresenter(data.getRoomId(), jwtUser.getUserId())) {
            return RestResult.generic(RestResultCode.ERR_ACCESS_DENIED);
        }
        //进行退麦操作
        return quitMic(data.getRoomId(), data.getUserId());
    }

    private RestResult quitMic(String roomId, String userId) throws Exception {
        //检查被踢用户是否在麦上
        MicPositionInfo micPosition = getMicPosition(roomId, userId);
        if (micPosition == null) {
            return RestResult.generic(RestResultCode.ERR_ROOM_USER_IS_NOT_IN_MIC);
        }
        //进行退麦操作
        micPosition.setUserId("");
        micPosition.setState(MicPositionState.NORMAL.getValue());
        saveMicPosition(roomId, ChrmKvChangeType.QUIT.getValue(), micPosition);
        return RestResult.success();
    }

    @Override
    public RestResult micInvite(ReqRoomMicInvite data, JwtUser jwtUser) throws Exception {
        //检查房间是否存在
        TRoom room = getRoomInfo(data.getRoomId());
        if (room == null) {
            return RestResult.generic(RestResultCode.ERR_ROOM_NOT_EXIST);
        }
        //检查当前用户是否为主持人, 0 为主持人麦位
        if (!isPresenter(data.getRoomId(), jwtUser.getUserId())) {
            return RestResult.generic(RestResultCode.ERR_ACCESS_DENIED);
        }
        //检查用户是否在房间
        if (!isInChrm(data.getRoomId(), data.getUserId())) {
            return RestResult.generic(RestResultCode.ERR_ROOM_USER_IS_NOT_IN);
        }
        //检查用户是否在麦上
        MicPositionInfo micPosition = getMicPosition(data.getRoomId(), data.getUserId());
        if (micPosition != null) {
            return RestResult.generic(RestResultCode.ERR_ROOM_USER_IS_ALREADY_IN_MIC);
        }
        //进行上麦操作
        return joinMic(data.getRoomId(), data.getUserId());
    }

    @Override
    public RestResult transferHost(ReqTransferHost data, JwtUser jwtUser) throws Exception {
        //检查房间是否存在
        TRoom room = getRoomInfo(data.getRoomId());
        if (room == null) {
            return RestResult.generic(RestResultCode.ERR_ROOM_NOT_EXIST);
        }
        //检查用户是否是主持人
        if (!isPresenter(data.getRoomId(), jwtUser.getUserId())) {
            return RestResult.generic(RestResultCode.ERR_ACCESS_DENIED);
        }
        //检查被转让人是否为主播
        MicPositionInfo micPosition = getMicPosition(data.getRoomId(), data.getUserId());
        if (micPosition == null) {
            return RestResult.generic(RestResultCode.ERR_ROOM_USER_IS_NOT_IN_MIC);
        }
        //转让信息保存至Redis，有效期10分钟
        redisTemplate.opsForValue().set(getRedisTransferHostKey(data.getRoomId(), data.getUserId()), jwtUser.getUserId(), transferHostExpire, TimeUnit.SECONDS);

        //给主播发送转让通知
        TransferHostMessage msg = new TransferHostMessage();
        msg.setCmd(TransferHostCmd.APPLY.getValue());
        msg.setOperatorId(jwtUser.getUserId());
        msg.setOperatorName(jwtUser.getUserName());
        msg.setTargetUserId(data.getUserId());
        imHelper.publishMessage(jwtUser.getUserId(), data.getRoomId(), msg);

        return RestResult.success();
    }

    @Override
    public RestResult acceptTransferHost(ReqRoomId data, JwtUser jwtUser) throws Exception {
        //检查房间是否存在
        TRoom room = getRoomInfo(data.getRoomId());
        if (room == null) {
            return RestResult.generic(RestResultCode.ERR_ROOM_NOT_EXIST);
        }
        //检查当前用户是否为主播
        MicPositionInfo micPosition = getMicPosition(data.getRoomId(), jwtUser.getUserId());
        if (micPosition == null) {
            return RestResult.generic(RestResultCode.ERR_ROOM_USER_IS_NOT_IN_MIC);
        }
        //检查主持人转让信息是否有效
        String hostId = (String) redisTemplate.opsForValue().get(getRedisTransferHostKey(data.getRoomId(), jwtUser.getUserId()));
        if (hostId == null) {
            return RestResult.generic(RestResultCode.ERR_ROOM_TRANSFER_INFO_INVALID);
        }
        MicPositionInfo hostPositionInfo = getMicPosition(data.getRoomId(), 0);
        if (hostPositionInfo == null) {
            hostPositionInfo = new MicPositionInfo();
            hostPositionInfo.setUserId("");
            hostPositionInfo.setPosition(0);
            hostPositionInfo.setState(MicPositionState.NORMAL.getValue());
        }
        if (hostPositionInfo.getUserId() != null && !hostPositionInfo.getUserId().equals(hostId)) {
            return RestResult.generic(RestResultCode.ERR_ROOM_TRANSFER_INFO_INVALID);
        }
        //麦位互换, 给主持人发送同意通知
        TransferHostMessage msg = new TransferHostMessage();
        msg.setCmd(TransferHostCmd.ACCEPT.getValue());
        msg.setOperatorId(jwtUser.getUserId());
        msg.setOperatorName(jwtUser.getUserName());
        msg.setTargetUserId(hostId);
        imHelper.publishMessage(jwtUser.getUserId(), data.getRoomId(), msg);

        Integer hostPositionState = hostPositionInfo.getState();
        Integer positionState = micPosition.getState();
        //主持人麦位换成主播
        hostPositionInfo.setUserId(jwtUser.getUserId());
        hostPositionInfo.setState(positionState);
        saveMicPosition(data.getRoomId(), ChrmKvChangeType.TRANSFER_HOST.getValue(), hostPositionInfo);

        //主播麦位换位主持人
        micPosition.setUserId(hostId);
        micPosition.setState(hostPositionState);
        saveMicPosition(data.getRoomId(), ChrmKvChangeType.TRANSFER_HOST.getValue(), micPosition);

        //删除转让缓存
        redisTemplate.delete(getRedisTransferHostKey(data.getRoomId(), jwtUser.getUserId()));

        return RestResult.success();
    }

    @Override
    public RestResult rejectTransferHost(ReqRoomId data, JwtUser jwtUser) throws Exception {
        //检查房间是否存在
        TRoom room = getRoomInfo(data.getRoomId());
        if (room == null) {
            return RestResult.generic(RestResultCode.ERR_ROOM_NOT_EXIST);
        }

        //检查主持人转让信息是否有效
        String hostId = (String) redisTemplate.opsForValue().get(getRedisTransferHostKey(data.getRoomId(), jwtUser.getUserId()));
        if (hostId == null) {
            return RestResult.generic(RestResultCode.ERR_ROOM_TRANSFER_INFO_INVALID);
        }

        //给主持人发送拒绝通知
        TransferHostMessage msg = new TransferHostMessage();
        msg.setCmd(TransferHostCmd.REJECT.getValue());
        msg.setOperatorId(jwtUser.getUserId());
        msg.setOperatorName(jwtUser.getUserName());
        msg.setTargetUserId(hostId);
        imHelper.publishMessage(jwtUser.getUserId(), data.getRoomId(), msg);

        //删除转让缓存
        redisTemplate.delete(getRedisTransferHostKey(data.getRoomId(), jwtUser.getUserId()));

        return RestResult.success();
    }

    @Override
    public RestResult takeOverHost(ReqRoomId data, JwtUser jwtUser) throws Exception {
        //检查房间是否存在
        TRoom room = getRoomInfo(data.getRoomId());
        if (room == null) {
            return RestResult.generic(RestResultCode.ERR_ROOM_NOT_EXIST);
        }

        //检查主持人麦位是否为空，为空则直接接管，不为空则需要给主持人发送通知，主持人同意后方可接管
        MicPositionInfo hostPosition = getMicPosition(data.getRoomId(), 0);
        if (hostPosition == null) {
            hostPosition = new MicPositionInfo();
            hostPosition.setUserId("");
            hostPosition.setPosition(0);
            hostPosition.setState(MicPositionState.NORMAL.getValue());
        }
        if (StringUtils.isEmpty(hostPosition.getUserId())) {
            int changeType = ChrmKvChangeType.AUDIENCE_TAKEOVER_HOST.getValue();
            //主播下麦
            MicPositionInfo micPosition = getMicPosition(data.getRoomId(), jwtUser.getUserId());
            if (micPosition != null) { //micPosition 不为空时，则是主播接管主持
                changeType = ChrmKvChangeType.ANCHOR_TAKEOVER_HOST.getValue();
                micPosition.setUserId("");
                micPosition.setState(MicPositionState.NORMAL.getValue());
                saveMicPosition(data.getRoomId(), changeType, micPosition);
            }
            //主持上麦
            hostPosition.setUserId(jwtUser.getUserId());
            saveMicPosition(data.getRoomId(), changeType, hostPosition);
            return RestResult.success();
        }

        // 接管信息保存至Redis，有效期15秒
        redisTemplate.opsForValue().set(getRedisTakeoverHostKey(data.getRoomId(), jwtUser.getUserId()), jwtUser.getUserId(), takeoverHostExpire, TimeUnit.SECONDS);

        // 发送通知给主持人
        TakeOverHostMessage msg = new TakeOverHostMessage();
        msg.setCmd(TakeOverHostCmd.APPLY.getValue());
        msg.setOperatorId(jwtUser.getUserId());
        msg.setOperatorName(jwtUser.getUserName());
        msg.setTargetUserId(hostPosition.getUserId());
        imHelper.publishMessage(jwtUser.getUserId(), data.getRoomId(), msg);

        return RestResult.success();
    }

    @Override
    public RestResult acceptTakeOverHost(ReqTakeOverHost data, JwtUser jwtUser) throws Exception {
        //检查房间是否存在
        TRoom room = getRoomInfo(data.getRoomId());
        if (room == null) {
            return RestResult.generic(RestResultCode.ERR_ROOM_NOT_EXIST);
        }
        //检查当前用户是否是主持人
        if (!isPresenter(data.getRoomId(), jwtUser.getUserId())) {
            return RestResult.generic(RestResultCode.ERR_ACCESS_DENIED);
        }

        //检查主持人接管信息是否有效
        String takeoverUserId = (String) redisTemplate.opsForValue().get(getRedisTakeoverHostKey(data.getRoomId(), data.getUserId()));
        if (takeoverUserId == null) {
            return RestResult.generic(RestResultCode.ERR_ROOM_TAKEOVER_INFO_INVALID);
        }

        //同意接管，互换麦位, 发送主播发送接受通知
        TakeOverHostMessage msg = new TakeOverHostMessage();
        msg.setCmd(TakeOverHostCmd.ACCEPT.getValue());
        msg.setOperatorId(jwtUser.getUserId());
        msg.setOperatorName(jwtUser.getUserName());
        msg.setTargetUserId(data.getUserId());
        imHelper.publishMessage(jwtUser.getUserId(), data.getRoomId(), msg);

        //主持人麦位换成主播，主播麦位换位主持人
        MicPositionInfo hostPosition = getMicPosition(data.getRoomId(), 0);
        MicPositionInfo micPosition = getMicPosition(data.getRoomId(), data.getUserId());

        int changeType = ChrmKvChangeType.AUDIENCE_TAKEOVER_HOST.getValue();

        Integer actorPositionState = null;

        if (micPosition != null) { //如果micPosition不为空，则为主播接管主持人
            changeType = ChrmKvChangeType.ANCHOR_TAKEOVER_HOST.getValue();
            actorPositionState = micPosition.getState();
            micPosition.setUserId(jwtUser.getUserId());
            micPosition.setState(hostPosition.getState());
            saveMicPosition(data.getRoomId(), changeType, micPosition);
        }

        hostPosition.setUserId(data.getUserId());
        if (actorPositionState != null) {
            hostPosition.setState(actorPositionState);
        }
        saveMicPosition(data.getRoomId(), changeType, hostPosition);

        //删除接管信息缓存
        redisTemplate.delete(getRedisTakeoverHostKey(data.getRoomId(), data.getUserId()));

        return RestResult.success();
    }

    @Override
    public RestResult rejectTakeOverHost(ReqTakeOverHost data, JwtUser jwtUser) throws Exception {
        //检查房间是否存在
        TRoom room = getRoomInfo(data.getRoomId());
        if (room == null) {
            return RestResult.generic(RestResultCode.ERR_ROOM_NOT_EXIST);
        }
        //检查当前用户是否是主持人
        if (!isPresenter(data.getRoomId(), jwtUser.getUserId())) {
            return RestResult.generic(RestResultCode.ERR_ACCESS_DENIED);
        }
        //检查主持人接管信息是否有效
        String takeoverUserId = (String) redisTemplate.opsForValue().get(getRedisTakeoverHostKey(data.getRoomId(), data.getUserId()));
        if (takeoverUserId == null) {
            return RestResult.generic(RestResultCode.ERR_ROOM_TAKEOVER_INFO_INVALID);
        }
        // 发送主播发送拒绝通知
        TakeOverHostMessage msg = new TakeOverHostMessage();
        msg.setCmd(TakeOverHostCmd.REJECT.getValue());
        msg.setOperatorId(jwtUser.getUserId());
        msg.setOperatorName(jwtUser.getUserName());
        msg.setTargetUserId(data.getUserId());
        imHelper.publishMessage(jwtUser.getUserId(), data.getRoomId(), msg);
        return RestResult.success();
    }

    @Override
    public RestResult messageBroadcast(ReqBroadcastMessage data, JwtUser jwtUser)
        throws Exception {
        if (StringUtils.isEmpty(data.getFromUserId())) {
            data.setFromUserId(CustomerConstant.SYSTEM_USER_ID);
        }

        imHelper.publishBroadcastMessage(data.getFromUserId(), data.getObjectName(), data.getContent());

        return RestResult.success();
    }

    /**
     * 聊天室创建时，向 IM 聊天室初始化 9 个麦位 KV 信息
     * @param roomId 聊天室 ID
     * @param userId 聊天室创建者，设置到主持麦位
     * @throws Exception
     */
    private void initMicPositions(String roomId, String userId) throws Exception {
        Map<String, MicPositionInfo> micPositionCacheMap = new HashMap<>();

        for (int i = 0; i < 9; i++) {
            MicPositionInfo micPositionInfo = new MicPositionInfo();
            micPositionInfo.setPosition(i);
            micPositionInfo.setState(MicPositionState.NORMAL.getValue());
            if (i == 0) {
                micPositionInfo.setUserId(userId);
            } else {
                micPositionInfo.setUserId("");
            }
            String key = MIC_POSSION_KEY_PREFIX + i;

            ChrmEntrySetInfo entrySetInfo = new ChrmEntrySetInfo();
            entrySetInfo.setChrmId(roomId);
            entrySetInfo.setUserId(CustomerConstant.SYSTEM_USER_ID);
            entrySetInfo.setKey(key);
            entrySetInfo.setValue(GsonUtil.toJson(micPositionInfo));
            entrySetInfo.setAutoDelete(false);

            imHelper.entrySet(entrySetInfo, 5);
            micPositionCacheMap.put(i + "", micPositionInfo);
        }
        micPositionHashOperations.putAll(getRedisRoomMicPositionKey(roomId), micPositionCacheMap);
    }

    /**
     * 上麦操作，优先加入第一个空麦位，麦位不存在时返回错误码
     * @param roomId
     * @param userId
     */
    private RestResult joinMic(String roomId, String userId) throws Exception {

        Map<String, MicPositionInfo> micPositionInfoMap = getMicPositions(roomId);
        for (int i = 1; i < 9; i ++) {
            String position = i + "";
            MicPositionInfo positionInfo = micPositionInfoMap.get(position);
            if (positionInfo == null) {
                positionInfo = new MicPositionInfo();
                positionInfo.setPosition(i);
                positionInfo.setUserId("");
                positionInfo.setState(MicPositionState.NORMAL.getValue());
            }
            if (positionInfo.getState() != null && positionInfo.getState() == MicPositionState.NORMAL.getValue() && StringUtils.isEmpty(positionInfo.getUserId())) {
                positionInfo.setUserId(userId);
                // 更新麦位信息
                saveMicPosition(roomId, ChrmKvChangeType.JOIN.getValue(), positionInfo);
                return RestResult.success();
            }
        }
        return RestResult.generic(RestResultCode.ERR_ROOM_NO_MIC_AVAILABLE);
    }

    /**
     * 上麦操作，优先加入第一个空麦位，麦位不存在时返回错误码
     * @param roomId
     * @param userId
     */
    private RestResult synchronizedJoinMic(String roomId, String userId) throws Exception {
        String lockKey = getRedisRoomMicApplyLockKey(roomId);
        redisLockService.lock(lockKey);
        RestResult result = joinMic(roomId, userId);
        redisLockService.unlock(lockKey);
        return result;
    }

    private Map<String, MicPositionInfo> getMicPositions(String roomId){
        return micPositionHashOperations.entries(getRedisRoomMicPositionKey(roomId));
    }
    private MicPositionInfo getMicPosition(String roomId, int position){
        return micPositionHashOperations.get(getRedisRoomMicPositionKey(roomId), position + "");
    }

    private void saveMicPosition(String roomId, Integer changeType, MicPositionInfo positionInfo)
        throws Exception {
        //更新 IM KV
        String key = MIC_POSSION_KEY_PREFIX + positionInfo.getPosition();
        ChrmKVNoticeMessage msg = new ChrmKVNoticeMessage();
        msg.setKey(key);
        msg.setValue(GsonUtil.toJson(positionInfo));
        msg.setType(1);
        ChrmKVChange chrmKVChange = new ChrmKVChange();
        chrmKVChange.setChangeType(changeType);
        msg.setExtra(GsonUtil.toJson(chrmKVChange));

        ChrmEntrySetInfo entrySetInfo = new ChrmEntrySetInfo();
        entrySetInfo.setChrmId(roomId);
        entrySetInfo.setUserId(CustomerConstant.SYSTEM_USER_ID);
        entrySetInfo.setKey(key);
        entrySetInfo.setValue(GsonUtil.toJson(positionInfo));
        entrySetInfo.setObjectName(msg.getObjectName());
        entrySetInfo.setContent(GsonUtil.toJson(msg));
        entrySetInfo.setAutoDelete(false);
        imHelper.entrySet(entrySetInfo, 5);

        //更新本地缓存
        micPositionHashOperations.put(getRedisRoomMicPositionKey(roomId), positionInfo.getPosition() + "", positionInfo);
    }

    private MicPositionInfo getMicPosition(String roomId, String userId) {
        //从 Redis 缓存获取麦位信息
        Map<String, MicPositionInfo> micPositionInfoMap = getMicPositions(roomId);
        for(MicPositionInfo value : micPositionInfoMap.values()){
            if (value != null && value.getUserId() != null && value.getUserId().equals(userId)) {
                return value;
            }
        }
        return null;
    }

    //============== 麦位申请 =====================
    private void addApplyMicList(String roomId, String userId) {
        zSetOperations.add(getRedisApplyMicMembersKey(roomId), userId, DateTimeUtils.currentUTC().getTime());
    }
    private void removeApplyMicList(String roomId, String userId) {
        zSetOperations.remove(getRedisApplyMicMembersKey(roomId), userId);
    }
    private boolean isInApplyMicList(String roomId, String userId){
        Set<String> userIds = zSetOperations.range(getRedisApplyMicMembersKey(roomId), 0, 1);
        return userIds != null && userIds.contains(userId);
    }
    private Set<String> getApplyMicList(String roomId) {
        return zSetOperations.range(getRedisApplyMicMembersKey(roomId), 0, 1);
    }

    //============= Room 相关 ==================
    private boolean isInChrm(String roomId, String userId) throws Exception {
        // 从 IM 聊天室查询用户是否存在
        IMIsInChrmResult result = imHelper.isInChartRoom(roomId, userId);
        return result.isSuccess() && result.getIsInChrm();
    }
    private void updateRoomCache(TRoom room){
        roomHashOperations.put(getRedisRoomInfosKey(), room.getUid(), room);
    }
    private TRoom getRoomInfo(String roomId){
        //先从缓存查询，不存在则从数据库查询
        TRoom room = roomHashOperations.get(getRedisRoomInfosKey(), roomId);
        if (room != null) {
            return room;
        }
        log.info("get room info from db, roomId:{}", roomId);
        room = roomDao.findTRoomByUidEquals(roomId);
        if (room != null) {
            updateRoomCache(room);
        }
        return room;
    }

    public Map<String, TRoom> hmget(String key, List<String> fields) {
        List<TRoom> result = roomHashOperations.multiGet(key, fields);
        Map<String, TRoom> ans = new HashMap<>(fields.size());
        int index = 0;
        for (String field : fields) {
            if (result.get(index) == null) {
                continue;
            }
            ans.put(field, result.get(index));
            index ++;
        }
        return ans;
    }

    private ResRoomInfo build(TRoom room){
        ResRoomInfo roomInfo = new ResRoomInfo();
        roomInfo.setRoomId(room.getUid());
        roomInfo.setRoomName(room.getName());
        roomInfo.setThemePictureUrl(room.getThemePictureUrl());
        roomInfo.setAllowedJoinRoom(room.isAllowedJoinRoom());
        roomInfo.setAllowedFreeJoinMic(room.isAllowedFreeJoinMic());
        roomInfo.setUpdateDt(room.getUpdateDt());
        return roomInfo;
    }


    private String getRedisRoomIdsKey() {
        return CustomerConstant.SERVICENAME + "|room_ids";
    }
    private String getRedisRoomInfosKey() {
        return CustomerConstant.SERVICENAME + "|rooms_info";
    }
    private String getRedisRoomMicPositionKey(String roomId) {
        return CustomerConstant.SERVICENAME + "|room_mic_positions|" + roomId;
    }
    private String getRedisApplyMicMembersKey(String roomId) {
        return CustomerConstant.SERVICENAME + "|apply_mic_members|" + roomId;
    }

    private String getRedisTransferHostKey(String roomId, String transferToUserId) {
        return CustomerConstant.SERVICENAME + "|transfer_host|" + "|" + roomId + "|" + transferToUserId;
    }
    private String getRedisTakeoverHostKey(String roomId, String takeoverUserId) {
        return CustomerConstant.SERVICENAME + "|takeover_host|" + "|" + roomId + "|" + takeoverUserId;
    }
    private String getRedisRoomGagUserKey(String roomId) {
        return CustomerConstant.SERVICENAME + "|gag_user|" + roomId;
    }

    private String getRedisRoomMicApplyLockKey(String roomId) {
        return CustomerConstant.SERVICENAME + "|mic_apply_lock|" + roomId;
    }

    @PostConstruct
    private void init() throws Exception {
       initRoom();
       initChrmWhiteList();
    }

    /**
     * 初始化 5 个官方房间，IM 聊天室销毁时，官方聊天室在 DemoServer 不删除，用户加入房间时，检测到 IM 聊天室不存在，则自动创建
     */
    private void initRoom() {
        // 检查房间是否初始化
        long count = roomDao.count();
        if (count > 0) {
            return;
        }
        //房间名称
        List<String> roomNames = new ArrayList<>();
        roomNames.add("这是什么神仙初恋");
        roomNames.add("未闻花名招人");
        roomNames.add("初见综合娱乐");
        roomNames.add("天天点歌厅");
        roomNames.add("百度点唱 最美奇遇");

        for (int i = 0; i < 5; i++) {
            //将房间信息保存至数据库
            Date date = DateTimeUtils.currentUTC();
            TRoom room = new TRoom();
            room.setUid(IdentifierUtils.uuid());
            room.setName(roomNames.get(i));
            room.setThemePictureUrl(domain + "/static/room/" + (i+1) + ".png");
            room.setType(RoomType.OFFICIAL.getValue());
            room.setAllowedJoinRoom(true);
            room.setAllowedFreeJoinMic(true);
            room.setCreateDt(date);
            room.setUpdateDt(date);
            roomDao.save(room);

            //将房间信息缓存至 Redis Hash
            updateRoomCache(room);
            //将房间 Id 缓存至 Redis Zset，房间分页查询数据源
            zSetOperations.add(getRedisRoomIdsKey(), room.getUid(), date.getTime());
        }
    }

    /**
     * 添加聊天室消息白名单
     */
    private void initChrmWhiteList() throws Exception {

        List<String> chrmWhiteList = new ArrayList<>(
            Arrays.asList("RCMic:transferHostMsg", "RCMic:takeOverHostMsg", "RC:chrmKVNotiMsg", "RCMic:chrmSysMsg", "RCMic:gift", "RCMic:broadcastGift", "RCMic:chrmMsg"));

        //查询 IM 添加的消息白名单

        IMChrmWhiteListResult result = imHelper.chrmWhitelistQuery();
        if (!result.isSuccess()) {
            return;
        }
        List<String> imChrmWhiteList = result.getWhitlistMsgType();
        if (imChrmWhiteList == null) {
            imChrmWhiteList = new ArrayList<>();
        }
        imChrmWhiteList.remove("");
        List<String> finalImChrmWhiteList = imChrmWhiteList;

        List<String> needAdds = chrmWhiteList.stream().filter(item -> !finalImChrmWhiteList.contains(item)).collect(toList());
        List<String> needDeletes = finalImChrmWhiteList.stream().filter(item -> !chrmWhiteList.contains(item)).collect(toList());

        if (!needDeletes.isEmpty()) {
            imHelper.chrmWhitelistDelete(needDeletes);
        }

        if (!needAdds.isEmpty()) {
            imHelper.chrmWhitelistAdd(needAdds);
        }
    }

}
