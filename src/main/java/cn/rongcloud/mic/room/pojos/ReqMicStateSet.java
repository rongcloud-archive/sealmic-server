package cn.rongcloud.mic.room.pojos;

import javax.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Created by sunyinglong on 2020/5/25.
 */
@Data
public class ReqMicStateSet {
    @NotBlank(message = "roomId should not be blank")
    private String roomId;
    @NotBlank(message = "state should not be blank")
    private Integer state;
    @NotBlank(message = "position should not be blank")
    private Integer position;
}
