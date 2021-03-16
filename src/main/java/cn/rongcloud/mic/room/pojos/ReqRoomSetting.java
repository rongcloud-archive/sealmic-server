package cn.rongcloud.mic.room.pojos;

import javax.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Created by sunyinglong on 2020/5/25.
 */
@Data
public class ReqRoomSetting {
    @NotBlank(message = "roomId should not be blank")
    private String roomId;
    private Boolean allowedJoinRoom;
    private Boolean allowedFreeJoinMic;
}
