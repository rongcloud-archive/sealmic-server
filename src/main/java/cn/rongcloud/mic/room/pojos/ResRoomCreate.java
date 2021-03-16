package cn.rongcloud.mic.room.pojos;

import java.util.Date;
import lombok.Data;

/**
 * Created by sunyinglong on 2020/6/3
 */
@Data
public class ResRoomCreate {

    private String roomId;

    private String roomName;

    private String themePictureUrl;

    private Integer type;

    private Date createDt;
}
