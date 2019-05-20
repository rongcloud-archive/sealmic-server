package cn.rongcloud.common;

import lombok.Getter;

/**
 * Created by weiqinxiao on 2019/2/25.
 */
public enum ErrorEnum {
    ERR_SUCCESS(0x0000, "OK"),
    ERR_OTHER(0x00FF, "Error"),
    ERR_REQUEST_PARA_ERR(1, "Missing or invalid parameter"),
    ERR_INVALID_AUTH(2, "Invalid or expired authorization"),
    ERR_ACCESS_DENIED(3, "Access denied"),
    ERR_BAD_REQUEST(4, "Bad request"),

    //IM error
    ERR_IM_TOKEN_ERROR(10, "IM token error"),
    ERR_CREATE_ROOM_ERROR(11, "Create room error"),
    ERR_MESSAGE_ERROR(13, "IM Message send error"),
    ERR_DESTROY_ROOM_ERROR(14, "Destroy room error"),


    //room error
    ERR_ROOM_NOT_EXIST(20, "Room not exist"),
    ERR_USER_NOT_EXIST_IN_ROOM(21, "User not exist in room"),
    ERR_EXIT_ROOM_ERROR(22, "Exit room error"),
    ERR_MIC_POSITION_ERROR(24, "Mic position error"),
    ERR_MIC_POSITION_LOCKED(25, "Mic position locked"),
    ERR_ROOM_OVER_MAX_COUNT(26, "Room count over max"),
    ERR_ROOM_MEMBER_OVER_MAX_COUNT(27, "Room member count over max"),
    ERR_MIC_POSITION_DUPLICATE_JOIN(28, "Duplicated join mic position"),
    ;

    private @Getter int errCode;
    private @Getter String errMsg;
    private ErrorEnum(int errCode, String errMsg) {
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

    public static ErrorEnum getEnumByValue(long errCode) {
        for(ErrorEnum item : ErrorEnum.values()) {
            if(item.getErrCode() == errCode) {
                return item;
            }
        }

        throw new IllegalArgumentException();
    }
}
