package cn.rongcloud.im.message;

import cn.rongcloud.im.BaseMessage;
import cn.rongcloud.pojo.RoomMicPositionInfo;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 * Created by weiqinxiao on 2019/3/25.
 */
public class MicPositionChangeMessage extends BaseMessage {
    private @Getter @Setter int cmd;
    private @Getter @Setter String targetUserId;
    private @Getter @Setter int fromPosition;
    private @Getter @Setter int toPosition;
    private @Getter @Setter List<RoomMicPositionInfo> micPositions;

    @Override
    public String getObjectName() {
        return "SM:MPChangeMsg";
    }
}
