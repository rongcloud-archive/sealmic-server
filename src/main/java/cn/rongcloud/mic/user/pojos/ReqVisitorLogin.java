package cn.rongcloud.mic.user.pojos;

import javax.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Created by sunyinglong on 2020/5/25.
 */
@Data
public class ReqVisitorLogin {
    @NotBlank(message = "deviceId should not be blank")
    private String deviceId;
    @NotBlank(message = "userName should not be blank")
    private String userName;
    private String portrait;
}
