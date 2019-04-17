package cn.rongcloud.im.message;

import cn.rongcloud.im.BaseMessage;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by weiqinxiao on 2019/3/25.
 */
public class RoomMemberChangedMessage extends BaseMessage {
    private @Getter @Setter int cmd; //1 join, 2 leave, 3 kick,
    private @Getter @Setter String targetUserId;
    private @Getter @Setter int targetPosition = -1; //-1 无效，>=0 有效的麦位

    @Override
    public String getObjectName() {
        return "SM:RMChangeMsg";
    }
}
