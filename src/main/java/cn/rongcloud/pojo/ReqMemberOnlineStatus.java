package cn.rongcloud.pojo;

import lombok.Data;

@Data
public class ReqMemberOnlineStatus {
    private String userId;
    private String status;//状态：0：online 在线、1：offline 离线、2：logout 登出。
    private String os;//操作系统，iOS 、 Android 或 Websocket，用户上线时同步。
    private String time;
}
