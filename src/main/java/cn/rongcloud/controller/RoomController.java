package cn.rongcloud.controller;

import cn.rongcloud.common.ApiException;
import cn.rongcloud.common.BaseResponse;
import cn.rongcloud.common.JwtUser;
import cn.rongcloud.filter.JwtFilter;
import cn.rongcloud.pojo.*;
import cn.rongcloud.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by weiqinxiao on 2019/2/25.
 */
@RestController
@RequestMapping("/room")
public class RoomController {
    @Autowired
    RoomService roomService;

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public BaseResponse<RoomBaseResult> createRoom(@RequestBody ReqRoomData data,
                                                   @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser jwtUser)
            throws ApiException, Exception {
        RoomBaseResult roomBaseResult = roomService.createRoom(data.getRoomType(), data.getSubject(), jwtUser);
        return new BaseResponse<>(roomBaseResult);
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public BaseResponse<List<RoomBaseResult>> getRoomList(@RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser jwtUser)
            throws ApiException, Exception {
        List<RoomBaseResult> roomResultList = roomService.getRoomList(jwtUser);
        return new BaseResponse<>(roomResultList);
    }

    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    public BaseResponse<RoomResult> getRoomInfo(@RequestParam String roomId,
                                                @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser jwtUser)
            throws ApiException, Exception {
        RoomResult result = roomService.getRoomDetail(roomId, jwtUser);
        return new BaseResponse<>(result);
    }

    @RequestMapping(value = "/destroy", method = RequestMethod.POST)
    public BaseResponse<Boolean> destroyRoom(@RequestBody ReqRoomData data,
                                             @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser jwtUser)
            throws ApiException, Exception {
        boolean result = roomService.destroyRoom(data.getRoomId(), jwtUser);
        return new BaseResponse<>(result);
    }

    @RequestMapping(value = "/mic/control", method = RequestMethod.POST)
    public BaseResponse<Boolean> controlMic(@RequestBody ReqControlMicData data,
                                            @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser jwtUser)
            throws ApiException, Exception {
        boolean result = roomService.controlMic(data.getRoomId(), data.getCmd(), data.getTargetUserId(), data.getTargetPosition(), jwtUser);
        return new BaseResponse<>(result);
    }

    @RequestMapping(value = "/mic/change", method = RequestMethod.POST)
    public BaseResponse<Boolean> changeMicPosition(@RequestBody ReqChangeMicData data,
                                                   @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser jwtUser) throws Exception {
        boolean result = roomService.changeMicPosition(data.getRoomId(), data.getFromPosition(), data.getToPosition(), jwtUser);
        return new BaseResponse<>(result);
    }

    @RequestMapping(value = "/mic/join", method = RequestMethod.POST)
    public BaseResponse<Boolean> joinMicPosition(@RequestBody ReqControlMicData data,
                                                 @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser jwtUser) throws Exception {
        boolean result = roomService.joinMic(data.getRoomId(), data.getTargetPosition(), jwtUser);
        return new BaseResponse<>(result);
    }

    @RequestMapping(value = "/join", method = RequestMethod.POST)
    public BaseResponse<RoomResult> joinRoom(@RequestBody ReqControlMicData data,
                                             @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser jwtUser) throws Exception {
        RoomResult result = roomService.joinRoom(data.getRoomId(), jwtUser);
        return new BaseResponse<>(result);
    }

    @RequestMapping(value = "/mic/leave", method = RequestMethod.POST)
    public BaseResponse<Boolean> leaveMic(@RequestBody ReqControlMicData data,
                                          @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser jwtUser) throws Exception {
        boolean result = roomService.leaveMic(data.getRoomId(), data.getTargetPosition(), jwtUser);
        return new BaseResponse<>(result);
    }

    @RequestMapping(value = "/leave", method = RequestMethod.POST)
    public BaseResponse<Boolean> leaveRoom(@RequestBody ReqRoomData data,
                                           @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser jwtUser) throws Exception {
        boolean result = roomService.leaveRoom(data.getRoomId(), jwtUser);
        return new BaseResponse<>(result);
    }

    @RequestMapping(value = "/member/list", method = RequestMethod.GET)
    public BaseResponse<List<RoomResult.AudienceResult>> getMemberList(@RequestParam String roomId,
                                                            @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser jwtUser)
            throws ApiException, Exception {
        List<RoomResult.AudienceResult> roomResultList = roomService.getMembers(roomId, jwtUser);
        return new BaseResponse<>(roomResultList);
    }

    @RequestMapping(value = "/background", method = RequestMethod.POST)
    public BaseResponse<Boolean> setBackground(@RequestBody ReqRoomData data,
                                           @RequestAttribute(value = JwtFilter.JWT_AUTH_DATA) JwtUser jwtUser) throws Exception {
        boolean result = roomService.setBackground(data.getRoomId(), data.getBgId(), jwtUser);
        return new BaseResponse<>(result);
    }
}
