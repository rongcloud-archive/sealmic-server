package cn.rongcloud.service;

import cn.rongcloud.common.ApiException;
import cn.rongcloud.common.JwtUser;
import cn.rongcloud.pojo.RoomResult;
import cn.rongcloud.pojo.RoomBaseResult;

import java.util.List;


/**
 * Created by weiqinxiao on 2019/2/28.
 */
public interface RoomService {
    //everyone
    public RoomBaseResult createRoom(int roomType, String subject, JwtUser jwtUser) throws ApiException, Exception;
    public List<RoomBaseResult> getRoomList(JwtUser jwtUser) throws ApiException, Exception;
    public RoomResult getRoomDetail(String roomId, JwtUser jwtUser) throws ApiException, Exception;
    public Boolean destroyRoom(String roomId, JwtUser jwtUser) throws ApiException, Exception;

    public Boolean controlMic(String roomId, int cmd, String targetUserId, int targetPosition, JwtUser jwtUser) throws ApiException, Exception;

    public RoomResult joinRoom(String roomId, JwtUser jwtUser) throws Exception;

    public Boolean changeMicPosition(String roomId, int fromPosition, int toPosition, JwtUser jwtUser) throws Exception;

    public Boolean joinMic(String roomId, int targetPosition, JwtUser jwtUser) throws Exception;

    public Boolean leaveMic(String roomId, int targetPosition, JwtUser jwtUser) throws Exception;

    public Boolean leaveRoom(String roomId, JwtUser jwtUser) throws Exception;

    public Boolean destroyRoom(String roomId) throws Exception;

    public List<RoomResult.AudienceResult> getMembers(String roomId, JwtUser jwtUser) throws Exception;

    public Boolean setBackground(String roomId, int bgId, JwtUser jwtUser) throws Exception;
}
