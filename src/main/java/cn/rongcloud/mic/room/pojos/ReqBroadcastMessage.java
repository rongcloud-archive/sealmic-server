package cn.rongcloud.mic.room.pojos;

import javax.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Created by sunyinglong on 2020/6/8
 */
@Data
public class ReqBroadcastMessage {

    private String fromUserId; //发送人用户id

    @NotBlank(message = "objectName should not be blank")
    private String objectName; //消息类型，参考融云消息类型表.消息标志；可自定义消息类型，长度不超过 32 个字符，您在自定义消息时需要注意，不要以 "RC:" 开头，以避免与融云系统内置消息的 ObjectName 重名。（必传）

    @NotBlank(message = "content should not be blank")
    private String content; //发送内容
}
