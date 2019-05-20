package cn.rongcloud.service.Impl;

import cn.rongcloud.common.*;
import cn.rongcloud.config.IMProperties;
import cn.rongcloud.config.RoomProperties;
import cn.rongcloud.dao.*;
import cn.rongcloud.im.IMHelper;
import cn.rongcloud.im.message.*;
import cn.rongcloud.job.ScheduleManager;
import cn.rongcloud.permission.DeclarePermissions;
import cn.rongcloud.pojo.*;
import cn.rongcloud.service.RoomService;
import cn.rongcloud.utils.CheckUtils;
import cn.rongcloud.utils.CodeUtil;
import cn.rongcloud.utils.DateTimeUtils;
import cn.rongcloud.utils.IdentifierUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by weiqinxiao on 2019/2/28.
 */
@Slf4j
@Service
public class RoomServiceImpl implements RoomService {
    @Autowired
    private IMHelper imHelper;

    @Autowired
    private RoomDao roomDao;

    @Autowired
    private RoomMemberDao roomMemberDao;


    @Autowired
    private RoomMicDao roomMicDao;

    @Autowired
    private ScheduleManager scheduleManager;

    @Autowired
    RoomProperties roomProperties;

    @Autowired
    private IMProperties imProperties;

    @Transactional
    @Override
    public RoomResult createRoom(int roomType, String subject, JwtUser jwtUser) throws ApiException, Exception {
        CheckUtils.checkArgument(subject != null, "subject must't be null");

        log.info("createRoom: {}, subject={}", jwtUser, subject);

        long count = roomDao.count();
        if (count >= roomProperties.getMaxCount()) {
            throw new ApiException(ErrorEnum.ERR_ROOM_OVER_MAX_COUNT);
        }

        String chatroomId = IdentifierUtils.uuid();
        IMApiResultInfo resultInfo = imHelper.createChatRoom(chatroomId, subject);
        Date date = DateTimeUtils.currentUTC();

        RoomResult roomResult = new RoomResult();
        if (resultInfo.isSuccess()) {
            Room room = new Room();
            room.setRid(chatroomId);
            room.setCreateDt(date);
            room.setType(roomType);
            room.setCreatorUid(jwtUser.getUserId());
            room.setSubject(subject);
            roomDao.save(room);

            saveRoomMember(chatroomId, jwtUser, RoleEnum.RoleAssistant.getValue());
            buildRoomMicPositions(chatroomId);

            roomResult.setRoomId(chatroomId);
            roomResult.setCreatorUserId(jwtUser.getUserId());
            roomResult.setCreateDt(date);
            roomResult.setSubject(subject);
            roomResult.setRoomType(roomType);
            roomResult.setMemCount(1);

            scheduleManager.addExpiredTask(chatroomId, this);
        } else {
            log.error("create chatroom error: {}, {}", jwtUser, resultInfo.getErrorMessage());
            throw new ApiException(ErrorEnum.ERR_CREATE_ROOM_ERROR, resultInfo.getErrorMessage());
        }
        log.info("createRoom: {}", roomResult);
        return roomResult;
    }

    private void saveRoomMember(String roomId, JwtUser jwtUser, int role) {
        RoomMember roomMember = new RoomMember();
        Date date = DateTimeUtils.currentUTC();
        roomMember.setRid(roomId);
        roomMember.setUid(jwtUser.getUserId());
        roomMember.setJoinDt(date);
        roomMember.setRole(role);
        roomMemberDao.save(roomMember);
    }

    private void buildRoomMicPositions(String roomId) {
        List<RoomMicPositionInfo> infoList = new ArrayList<>();
        for (int i = 0; i < roomProperties.getPosCount(); i++) {
            RoomMicPositionInfo posInfo = new RoomMicPositionInfo();
            posInfo.setRid(roomId);
            posInfo.setPosition(i);
            infoList.add(posInfo);
        }
         roomMicDao.saveAll(infoList);
    }

    @Transactional
    @Override
    public RoomResult joinRoom(String roomId, JwtUser jwtUser) throws Exception {
        CheckUtils.checkArgument(roomId != null, "roomId must't be null");
        CheckUtils.checkArgument(roomDao.existsByRid(roomId), "room not exist");

        int count = roomMemberDao.countByRid(roomId);
        if (count >= roomProperties.getMaxMember()) {
            throw new ApiException(ErrorEnum.ERR_ROOM_MEMBER_OVER_MAX_COUNT);
        }

        boolean exist = roomMemberDao.existsByRidAndUid(roomId, jwtUser.getUserId());
        log.info("joinRoom: roomId={}, {}, exist={}", roomId, jwtUser, exist);
        if (!exist) {
            saveRoomMember(roomId, jwtUser, RoleEnum.RoleAudience.getValue());
        }
        RoomMemberChangedMessage msg = new RoomMemberChangedMessage();
        msg.setCmd(1);
        msg.setTargetUserId(jwtUser.getUserId());
        imHelper.publishMessage(jwtUser.getUserId(), roomId, msg, 1);
        return getRoomDetail(roomId, jwtUser);
    }

    @Override
    @Transactional
    public Boolean leaveRoom(String roomId, JwtUser jwtUser) throws Exception {
        CheckUtils.checkArgument(roomId != null, "roomId must't be null");
        CheckUtils.checkArgument(roomDao.existsByRid(roomId), "room not exist");
        log.info("leaveRoom: roomId={}, {}", roomId, jwtUser);

        return leaveRoom(roomId, jwtUser.getUserId());
    }

    private Boolean leaveRoom(String roomId, String userId) throws Exception{
        roomMemberDao.deleteByRidAndUid(roomId, userId);

        RoomMicPositionInfo info = roomMicDao.findByRidAndUid(roomId, userId);
        if (info != null) {
            int state = info.getState() & (MicPositionState.Forbidden.getValue());
            roomMicDao.updateStateAndUidByRidAndPosition(roomId, info.getPosition(), state, null);
            notifyMicControlMsg(roomId, info.getPosition(), MicPositionCmd.Down.ordinal(), userId, userId);
        }

        RoomMemberChangedMessage msg = new RoomMemberChangedMessage();
        msg.setCmd(2);
        msg.setTargetUserId(userId);
        imHelper.publishMessage(userId, roomId, msg, 1);
        return true;
    }

    @Override
    public List<RoomBaseResult> getRoomList(JwtUser jwtUser) throws Exception {
        List<Room> roomList = roomDao.findAll();
        List<RoomBaseResult> roomBaseResultList = new ArrayList<>();
        for (Room room : roomList) {
            RoomBaseResult result = new RoomBaseResult();
            result.setRoomId(room.getRid());
            result.setSubject(room.getSubject());
            result.setCreateDt(room.getCreateDt());
            result.setRoomType(room.getType());
            result.setCreatorUserId(room.getCreatorUid());
            result.setMemCount(roomMemberDao.countByRid(room.getRid()));
            roomBaseResultList.add(result);
        }
        return roomBaseResultList;
    }

    @Override
    public RoomResult getRoomDetail(String roomId, JwtUser jwtUser) throws ApiException, Exception {
        CheckUtils.checkArgument(roomId != null, "roomId must't be null");
        CheckUtils.checkArgument(roomDao.existsByRid(roomId), "room not exist");
        RoomResult roomResult = new RoomResult();
        List<Room> roomList = roomDao.findByRid(roomId);
        if (roomList.isEmpty()) {
            throw new ApiException(ErrorEnum.ERR_ROOM_NOT_EXIST);
        }

        log.info("getRoomDetail: roomId={}, {}", roomId, jwtUser);
        roomResult.setRoomId(roomId);
        roomResult.setSubject(roomList.get(0).getSubject());
        roomResult.setCreatorUserId(roomList.get(0).getCreatorUid());
        roomResult.setCreateDt(roomList.get(0).getCreateDt());
        roomResult.setBgId(roomList.get(0).getBgId());

        List<RoomMicPositionInfo> micList = roomMicDao.findByRidOrderAsc(roomId);
        List<RoomResult.MicPositionResult> micPositionResultList = new ArrayList<>();
        for (RoomMicPositionInfo roomMicPositionInfo : micList) {
            RoomResult.MicPositionResult micPositionResult = new RoomResult.MicPositionResult();
            micPositionResult.setState(roomMicPositionInfo.getState());
            micPositionResult.setUserId(roomMicPositionInfo.getUid());
            micPositionResult.setPosition(roomMicPositionInfo.getPosition());
            micPositionResultList.add(micPositionResult);
        }
        roomResult.setMicPositions(micPositionResultList);

        List<RoomMember> memberList = roomMemberDao.findByRid(roomId);
        roomResult.setMemCount(memberList.size());
        List<RoomResult.AudienceResult> audienceResults = new ArrayList<>();
        for (RoomMember member : memberList) {
            RoomResult.AudienceResult audienceResult = new RoomResult.AudienceResult();
            audienceResult.setUserId(member.getUid());
            audienceResult.setJoinDt(member.getJoinDt());
            audienceResult.setRole(member.getRole());
            audienceResults.add(audienceResult);
        }
        roomResult.setAudiences(audienceResults);
        return roomResult;
    }

    public Boolean destroyRoom(String roomId) throws Exception {
        log.info("destroyRoom: {}", roomId);
        List<Room> roomList = roomDao.findByRid(roomId);
        if (roomList.isEmpty()) return false;

        roomDao.deleteByRid(roomId);
        roomMicDao.deleteByRid(roomId);
        roomMemberDao.deleteByRid(roomId);
        RoomDestroyedNotifyMessage msg = new RoomDestroyedNotifyMessage();
        imHelper.publishMessage(roomList.get(0).getCreatorUid(), roomId, msg, 1);
//        IMApiResultInfo resultInfo = imHelper.destroy(roomId);
//        if (resultInfo.isSuccess()) {
//            return true;
//        } else {
//            throw new ApiException(ErrorEnum.ERR_DESTROY_ROOM_ERROR);
//        }
        return true;
    }

    @Transactional
    @Override
    public Boolean destroyRoom(String roomId, JwtUser jwtUser) throws Exception {
        CheckUtils.checkArgument(roomId != null, "roomId must't be null");
        CheckUtils.checkArgument(roomDao.existsByRid(roomId), "room not exist");
        CheckUtils.checkArgument(roomDao.existsByRidAndCreatorUid(roomId, jwtUser.getUserId()), "No permission");
        log.info("destroyRoom: roomId={}, {}", roomId, jwtUser);

        return destroyRoom(roomId);
    }
    
    private boolean micPositionNoEmpty(String roomId, int position) {
        RoomMicPositionInfo info = roomMicDao.findByRidAndPosition(roomId, position);
        return info != null && info.getUid() != null;
    }

    @DeclarePermissions(value = RoleEnum.RoleAssistant)
    @Transactional
    @Override
    public Boolean controlMic(String roomId, int cmd, String targetUserId, int targetPosition, JwtUser jwtUser) throws Exception {
        CheckUtils.checkArgument(roomId != null, "roomId must't be null");
        CheckUtils.checkArgument(cmd >= 0 && cmd < MicPositionCmd.values().length, "not support the cmd");
        CheckUtils.checkArgument(roomDao.existsByRid(roomId), "room not exist");
        CheckUtils.checkArgument(existByRidAndUidInRoom(roomId, jwtUser.getUserId()), "Member not exist in room");

        MicPositionCmd micCmd = MicPositionCmd.values()[cmd];
        log.info("controlMic: roomId={}, targetUserId={}, targetPosition={}, cmd={}, {}", roomId, targetUserId, targetPosition, micCmd, jwtUser);
        RoomMicPositionInfo info = roomMicDao.findByRidAndPosition(roomId, targetPosition);
        switch (micCmd) {
            case Carry:
                if (info.getUid() != null) {
                    throw new ApiException(ErrorEnum.ERR_MIC_POSITION_ERROR, "Position has been hold");
                }
                RoomMicPositionInfo positionInfo = roomMicDao.findByRidAndUid(roomId, targetUserId);
                log.info("controlMic: {}, {}", roomId, positionInfo);
                if (positionInfo != null) {
                    throw new ApiException(ErrorEnum.ERR_MIC_POSITION_DUPLICATE_JOIN, "Exist in mic position");
                }
                int state = info.getState() | MicPositionState.Hold.getValue();
                roomMicDao.updateStateAndUidByRidAndPosition(roomId, targetPosition, state, targetUserId);
                notifyMicControlMsg(roomId, targetPosition, MicPositionCmd.Carry.ordinal(), jwtUser.getUserId(), targetUserId);
                break;
            case Kick:
                if (info.getUid() == null) {
                    throw new ApiException(ErrorEnum.ERR_MIC_POSITION_ERROR, "Position hasn't been hold");
                }
                state = info.getState() & (MicPositionState.Locked.getValue() | MicPositionState.Forbidden.getValue());
                roomMicDao.updateStateAndUidByRidAndPosition(roomId, targetPosition, state,null);
                roomMemberDao.deleteByRidAndUid(roomId, targetUserId);
                notifyMicControlMsg(roomId, targetPosition, MicPositionCmd.Kick.ordinal(), jwtUser.getUserId(), targetUserId);
                break;
            case Lock:
                state = info.getState() | MicPositionState.Locked.getValue();
                roomMicDao.updateStateAndUidByRidAndPosition(roomId, targetPosition, state, null);
                notifyMicControlMsg(roomId, targetPosition, MicPositionCmd.Forbidden.ordinal(), jwtUser.getUserId(), targetUserId);
                break;
            case Unlock:
                if (info == null || (info.getState() & MicPositionState.Locked.getValue()) == 0) {
                    throw new ApiException(ErrorEnum.ERR_MIC_POSITION_ERROR, "Position doesn't lock");
                } else {
                    state = info.getState() & MicPositionState.Forbidden.getValue();
                    roomMicDao.updateStateByRidAndPosition(roomId, targetPosition, state);
                    notifyMicControlMsg(roomId, targetPosition, MicPositionCmd.Unlock.ordinal(), jwtUser.getUserId(), targetUserId);
                }
                break;
            case Forbidden:
                state = info.getState() | MicPositionState.Forbidden.getValue();
                roomMicDao.updateStateByRidAndPosition(roomId, targetPosition, state);
                notifyMicControlMsg(roomId, targetPosition, MicPositionCmd.Forbidden.ordinal(), jwtUser.getUserId(), targetUserId);
                break;
            case Unforbidden:
                if (info == null || (info.getState() & MicPositionState.Forbidden.getValue()) == 0) {
                    throw new ApiException(ErrorEnum.ERR_MIC_POSITION_ERROR, "Position doesn't forbidden");
                } else {
                    state = info.getState() & (MicPositionState.Locked.getValue()|MicPositionState.Hold.getValue());
                    roomMicDao.updateStateByRidAndPosition(roomId, targetPosition, state);
                    notifyMicControlMsg(roomId, targetPosition, MicPositionCmd.Unforbidden.ordinal(), jwtUser.getUserId(), targetUserId);
                }
                break;
            case Down:
                if (info == null || (info.getState() & MicPositionState.Hold.getValue()) == 0) {
                    throw new ApiException(ErrorEnum.ERR_MIC_POSITION_ERROR, "Position doesn't hold");
                }
                state = info.getState() & (MicPositionState.Forbidden.getValue());
                roomMicDao.updateStateAndUidByRidAndPosition(roomId, targetPosition, state, null);
                notifyMicControlMsg(roomId, targetPosition, MicPositionCmd.Down.ordinal(), jwtUser.getUserId(), targetUserId);
                break;
            default:
                log.error("controlMic: unsupport the cmd: roomId={}, targetUserId={}, targetPosition={}, cmd={}, {}", roomId, targetUserId, targetPosition, micCmd, jwtUser);
                throw new ApiException(ErrorEnum.ERR_BAD_REQUEST);
        }
        return true;
    }

    @DeclarePermissions(value = RoleEnum.RoleConnector)
    @Override
    @Transactional
    public Boolean changeMicPosition(String roomId, int fromPosition, int toPosition, JwtUser jwtUser) throws Exception {
        CheckUtils.checkArgument(roomId != null, "roomId must't be null");
        CheckUtils.checkArgument(roomDao.existsByRid(roomId), "room not exist");
        CheckUtils.checkArgument(fromPosition >= 0 && fromPosition <= roomProperties.getPosCount(), "mic position error");
        CheckUtils.checkArgument(toPosition >= 0 && toPosition <= roomProperties.getPosCount(), "mic position error");
        CheckUtils.checkArgument(existByRidAndUidInRoom(roomId, jwtUser.getUserId()), "Member not exist in room");

        RoomMicPositionInfo fromInfo = roomMicDao.findByRidAndPosition(roomId, fromPosition);
        if (!jwtUser.getUserId().equals(fromInfo.getUid())) {
            throw new ApiException(ErrorEnum.ERR_MIC_POSITION_ERROR, "User doesn't hold position");
        }
        RoomMicPositionInfo toInfo = checkMicPositionLocked(roomId, toPosition);
        if (toInfo.getUid() != null) {
            throw new ApiException(ErrorEnum.ERR_MIC_POSITION_ERROR, "Position has been hold");
        }

        log.info("changeMicPosition: roomId={}, from={}, to={}, {}", roomId, fromPosition, toPosition, jwtUser);

        int state = fromInfo.getState() & (MicPositionState.Locked.getValue() | MicPositionState.Forbidden.getValue());
        roomMicDao.updateStateAndUidByRidAndPosition(roomId, fromPosition, state,null);
        roomMicDao.updateStateAndUidByRidAndPosition(roomId, toPosition, toInfo.getState()|MicPositionState.Hold.getValue(), jwtUser.getUserId());

        MicPositionChangeMessage msg = new MicPositionChangeMessage();
        msg.setTargetUserId(jwtUser.getUserId());
        msg.setCmd(MicPositionCmd.Change.ordinal());
        msg.setFromPosition(fromPosition);
        msg.setToPosition(toPosition);
        List<RoomMicPositionInfo> micPositionInfoList = roomMicDao.findByRidOrderAsc(roomId);
        msg.setMicPositions(micPositionInfoList);
        imHelper.publishMessage(jwtUser.getUserId(), roomId, msg, 1);
        return true;
    }

    private boolean existByRidAndUidInRoom(String roomId, String userId) {
        List<RoomMember> list = roomMemberDao.findByRidAndUid(roomId, userId);
        return !list.isEmpty();
    }

    @DeclarePermissions(value = RoleEnum.RoleAudience)
    @Override
    @Transactional
    public Boolean joinMic(String roomId, int targetPosition, JwtUser jwtUser) throws Exception {
        CheckUtils.checkArgument(roomId != null, "roomId must't be null");
        CheckUtils.checkArgument(targetPosition >= 0 && targetPosition < roomProperties.getPosCount(), "mic position error");
        CheckUtils.checkArgument(roomDao.existsByRid(roomId), "room not exist");
        CheckUtils.checkArgument(existByRidAndUidInRoom(roomId, jwtUser.getUserId()), "Member not exist in room");
        RoomMicPositionInfo info = checkMicPositionLocked(roomId, targetPosition);
        log.info("joinMic: {}, {}", jwtUser, info);
        if (info.getUid() != null) {
            throw new ApiException(ErrorEnum.ERR_MIC_POSITION_ERROR, "Position has been hold");
        }

        roomMicDao.updateStateAndUidByRidAndPosition(roomId, targetPosition, info.getState() | MicPositionState.Hold.getValue(), jwtUser.getUserId());
        roomMemberDao.updateRoleByRidAndUid(roomId, jwtUser.getUserId(), RoleEnum.RoleConnector.getValue());
        notifyMicControlMsg(roomId, targetPosition, MicPositionCmd.Up.ordinal(), jwtUser.getUserId(), jwtUser.getUserId());
        return true;
    }

    private RoomMicPositionInfo checkMicPositionLocked(String roomId, int micPos) {
        RoomMicPositionInfo info = roomMicDao.findByRidAndPosition(roomId, micPos);
        if ((info.getState() & MicPositionState.Locked.getValue()) != 0) {
            throw new ApiException(ErrorEnum.ERR_MIC_POSITION_LOCKED);
        }
        return info;
    }

    @DeclarePermissions(value = RoleEnum.RoleConnector)
    @Override
    @Transactional
    public Boolean leaveMic(String roomId, int targetPosition, JwtUser jwtUser) throws Exception {
        CheckUtils.checkArgument(roomId != null, "roomId must't be null");
        CheckUtils.checkArgument(roomDao.existsByRid(roomId), "room not exist");
        CheckUtils.checkArgument(existByRidAndUidInRoom(roomId, jwtUser.getUserId()), "Member not exist in room");
        CheckUtils.checkArgument(micPositionNoEmpty(roomId, targetPosition), "Not in mic position");

        log.info("leaveMic: roomId={}, position={}, {}", roomId, targetPosition, jwtUser);
        roomMicDao.updateUidByRidAndPosition(roomId, targetPosition, null);
        roomMemberDao.updateRoleByRidAndUid(roomId, jwtUser.getUserId(), RoleEnum.RoleAudience.getValue());

        notifyMicControlMsg(roomId, targetPosition, MicPositionCmd.Down.ordinal(), jwtUser.getUserId(), jwtUser.getUserId());
        return true;
    }

    private void notifyMicControlMsg(String roomId, int targetPosition, int cmd, String fromUserId, String targetUserId) throws Exception {
        List<RoomMicPositionInfo> micPositionInfoList = roomMicDao.findByRidOrderAsc(roomId);
        MicPositionControlMessage msg = new MicPositionControlMessage();
        msg.setTargetUserId(targetUserId);
        msg.setCmd(cmd);
        msg.setTargetPosition(targetPosition);
        msg.setMicPositions(micPositionInfoList);
        imHelper.publishMessage(fromUserId, roomId, msg, 1);
    }

    @Override
    @Transactional
    public List<RoomResult.AudienceResult> getMembers(String roomId, JwtUser jwtUser) throws Exception {
        CheckUtils.checkArgument(roomId != null, "roomId must't be null");
        CheckUtils.checkArgument(roomDao.existsByRid(roomId), "room not exist");
        CheckUtils.checkArgument(existByRidAndUidInRoom(roomId, jwtUser.getUserId()), "Member not exist in room");

        log.info("getMembers: roomId={}, {}", roomId, jwtUser);
        List<RoomMember> memberList = roomMemberDao.findByRid(roomId);
        List<RoomResult.AudienceResult> audienceResultList = new ArrayList<>();
        for (RoomMember member: memberList) {
            RoomResult.AudienceResult r = new RoomResult.AudienceResult();
            r.setRole(member.getRole());
            r.setJoinDt(member.getJoinDt());
            r.setUserId(member.getUid());
            audienceResultList.add(r);
        }
        return audienceResultList;
    }

    @Override
    @Transactional
    public Boolean setBackground(String roomId, int bgId, JwtUser jwtUser) throws Exception {
        CheckUtils.checkArgument(roomId != null, "roomId must't be null");
        CheckUtils.checkArgument(roomDao.existsByRid(roomId), "room not exist");
        CheckUtils.checkArgument(existByRidAndUidInRoom(roomId, jwtUser.getUserId()), "Member not exist in room");

        log.info("setBackground: roomId={}, bg={}, {}", roomId, bgId, jwtUser);
        roomDao.updateBgByRid(roomId, bgId);
        RoomBgNotifyMessage msg = new RoomBgNotifyMessage(bgId);
        imHelper.publishMessage(jwtUser.getUserId(), roomId, msg, 1);
        return true;
    }

    @Override
    public Boolean memberOnlineStatus(List<ReqMemberOnlineStatus> statusList, String nonce, String timestamp, String signature) throws ApiException, Exception {
        String sign = imProperties.getSecret() + nonce + timestamp;
        String signSHA1 = CodeUtil.hexSHA1(sign);
        if (!signSHA1.equals(signature)) {
            log.info("memberOnlineStatus signature error");
            return true;
        }

        for (ReqMemberOnlineStatus status : statusList) {
            int s = Integer.parseInt(status.getStatus());
            String userId = status.getUserId();

            log.info("memberOnlineStatus, userId={}, status={}", userId, status);
            //1：offline 离线； 0: online 在线
            if (s == 1) {
                List<RoomMember> members = roomMemberDao.findByUid(userId);
                if (!members.isEmpty()) {
                    scheduleManager.userIMOffline(userId);
                }
            } else if (s == 0) {
                scheduleManager.userIMOnline(userId);
            }
        }

        return true;
    }

    @Override
    public void userIMOfflineKick(String userId) {
        List<RoomMember> members = roomMemberDao.findByUid(userId);
        for (RoomMember member : members) {
            int userRole = member.getRole();
            log.info("userIMOfflineKick: roomId={}, {}, role={}", member.getRid(), userId, RoleEnum.getEnumByValue(userRole));
            try {
                leaveRoom(member.getRid(), userId);
                if (member.getRole() == RoleEnum.RoleAssistant.getValue()) {
                    destroyRoom(member.getRid());
                }
            } catch (Exception e) {
                log.error("userIMOfflineKick error: userId={}", userId);
            }
        }
    }
}
