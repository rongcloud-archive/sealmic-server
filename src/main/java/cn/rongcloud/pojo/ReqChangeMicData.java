package cn.rongcloud.pojo;

import lombok.Data;

/**
 * Created by weiqinxiao on 2019/3/21.
 */
@Data
public class ReqChangeMicData {
    private String roomId;
    private String targetUserId;
    private int fromPosition;
    private int toPosition;
}
