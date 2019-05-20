package cn.rongcloud.im.message;

import cn.rongcloud.im.BaseMessage;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by weiqinxiao on 2019/4/1.
 */
public class RoomDestroyedNotifyMessage extends BaseMessage {
    @Override
    public String getObjectName() {
        return "SM:RDNtfyMsg";
    }
}
