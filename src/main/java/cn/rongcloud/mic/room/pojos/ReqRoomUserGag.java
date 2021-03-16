package cn.rongcloud.mic.room.pojos;

import java.util.List;
import javax.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Created by sunyinglong on 2020/6/8
 */
@Data
public class ReqRoomUserGag {

    @NotBlank(message = "roomId should not be blank")
    private String roomId;

    @NotBlank(message = "operation should not be blank")
    private String operation;

    @NotBlank(message = "userIds should not be blank")
    private List<String> userIds;
}
