package cn.rongcloud.pojo;

import lombok.Data;

/**
 * Created by weiqinxiao on 2019/3/1.
 */
@Data
public class ReqRoomData {
    private String roomId;
    private String subject;
    private int roomType;
    private int bgId;
}
