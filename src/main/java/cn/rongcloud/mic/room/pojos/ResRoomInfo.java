package cn.rongcloud.mic.room.pojos;

import java.util.Date;
import lombok.Data;

/**
 * Created by sunyinglong on 2020/6/3
 */
@Data
public class ResRoomInfo {

    private String roomId;

    private String roomName;

    private String themePictureUrl;

    private Boolean allowedJoinRoom;

    private Boolean allowedFreeJoinMic;

    private Date updateDt;
}
