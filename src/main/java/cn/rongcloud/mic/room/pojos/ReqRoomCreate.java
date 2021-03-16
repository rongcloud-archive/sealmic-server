package cn.rongcloud.mic.room.pojos;

import javax.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Created by sunyinglong on 2020/6/3
 */
@Data
public class ReqRoomCreate {

    @NotBlank(message = "room name should not be blank")
    private String name;

    @NotBlank(message = "room themePictureUrl should not be blank")
    private String themePictureUrl;

}
