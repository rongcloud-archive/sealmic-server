package cn.rongcloud.mic.appversion.pojos;

import javax.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Created by sunyinglong on 2020/6/16
 */
@Data
public class ReqAppVersionCreate {

    @NotBlank(message = "platform should not be blank")
    private String platform;

    @NotBlank(message = "downloadUrl should not be blank")
    private String downloadUrl;

    @NotBlank(message = "version should not be blank")
    private String version;

    @NotBlank(message = "versionCode should not be blank")
    private Long versionCode;

    @NotBlank(message = "forceUpgrade should not be blank")
    private Boolean forceUpgrade;

    private String releaseNote;
}
