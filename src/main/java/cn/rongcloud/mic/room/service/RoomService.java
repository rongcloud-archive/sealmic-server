package cn.rongcloud.mic.room.service;

import cn.rongcloud.common.jwt.JwtUser;
import cn.rongcloud.mic.common.rest.RestResult;
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
import java.util.List;

/**
 * Created by sunyinglong on 2020/6/3
 */
public interface RoomService {

    RestResult createRoom(ReqRoomCreate data, JwtUser jwtUser) throws Exception;

    RestResult getRoomDetail(String roomId);
    RestResult getRoomList(String fromRoomId, Integer size);
    RestResult roomSetting(ReqRoomSetting data, JwtUser jwtUser);
    RestResult roomUserGag(ReqRoomUserGag data, JwtUser jwtUser) throws Exception;
    RestResult getRoomMembers(String roomId) throws Exception;
    RestResult queryGagRoomUsers(String roomId, JwtUser jwtUser);
    void chrmStatusSync(List<ReqRoomStatusSync> data, Long signTimestamp, String nonce, String signature);
    RestResult roomUserKick(ReqRoomUserKick data, JwtUser jwtUser) throws Exception;
    RestResult roomMicApply(ReqRoomId data, JwtUser jwtUser) throws Exception;
    RestResult roomMicReject(ReqRoomMicReject data, JwtUser jwtUser) throws Exception;
    RestResult roomMicAccept(ReqRoomMicAccept data, JwtUser jwtUser) throws Exception;
    RestResult getMicApplyMembers(String roomId, JwtUser jwtUser);
    RestResult setMicState(ReqMicStateSet data, JwtUser jwtUser) throws Exception;
    RestResult micQuit(ReqRoomId data, JwtUser jwtUser) throws Exception;
    RestResult micKick(ReqRoomMicKick data, JwtUser jwtUser) throws Exception;
    RestResult micInvite(ReqRoomMicInvite data, JwtUser jwtUser) throws Exception;
    RestResult transferHost(ReqTransferHost data, JwtUser jwtUser) throws Exception;
    RestResult acceptTransferHost(ReqRoomId data, JwtUser jwtUser) throws Exception;
    RestResult rejectTransferHost(ReqRoomId data, JwtUser jwtUser) throws Exception;
    RestResult takeOverHost(ReqRoomId data, JwtUser jwtUser) throws Exception;
    RestResult acceptTakeOverHost(ReqTakeOverHost data, JwtUser jwtUser) throws Exception;
    RestResult rejectTakeOverHost(ReqTakeOverHost data, JwtUser jwtUser) throws Exception;
    RestResult messageBroadcast(ReqBroadcastMessage data, JwtUser jwtUser) throws Exception;
}
