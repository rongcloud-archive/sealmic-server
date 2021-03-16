package cn.rongcloud.mic.user.pojos;

import javax.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Created by sunyinglong on 2020/5/25.
 */
@Data
public class ReqSendCode {
    @NotBlank(message = "mobile should not be blank")
    private String mobile;
}
