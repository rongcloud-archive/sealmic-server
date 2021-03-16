package cn.rongcloud.mic.room.pojos;

import java.io.Serializable;
import lombok.Data;

/**
 * Created by sunyinglong on 2020/6/3
 */
@Data
public class MicPositionInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer position; // 麦位
    private String userId; //用户ID，麦上无人时为空
    private Integer state; //麦位状态，0正常，1锁定，2闭麦
}
