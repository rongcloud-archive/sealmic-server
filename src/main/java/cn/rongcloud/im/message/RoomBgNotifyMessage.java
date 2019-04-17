package cn.rongcloud.im.message;

import cn.rongcloud.im.BaseMessage;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by weiqinxiao on 2019/4/1.
 */
public class RoomBgNotifyMessage extends BaseMessage {
    private @Getter @Setter int bgId;

    public RoomBgNotifyMessage(int bgId) {
        this.bgId = bgId;
    }

    @Override
    public String getObjectName() {
        return "SM:RBgNtfyMsg";
    }
}
