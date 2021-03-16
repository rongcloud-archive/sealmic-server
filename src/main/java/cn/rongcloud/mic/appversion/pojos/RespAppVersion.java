package cn.rongcloud.mic.appversion.pojos;

import lombok.Data;

/**
 * Created by sunyinglong on 2020/6/16
 */
@Data
public class RespAppVersion {

    private String platform;

    private String downloadUrl;

    private String version;

    private Long versionCode;

    private Boolean forceUpgrade;

    private String releaseNote;
}
