package cn.rongcloud.mic.appversion.pojos;

import lombok.Data;

/**
 * Created by sunyinglong on 2020/6/16
 */
@Data
public class ReqAppVersionUpdate {

    private String downloadUrl;

    private Boolean forceUpgrade;

    private String releaseNote;
}
