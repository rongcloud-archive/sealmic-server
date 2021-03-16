package cn.rongcloud.mic.room.message;

import cn.rongcloud.common.im.BaseMessage;
import lombok.Data;


/**
 * Created by sunyinglong on 2020/6/3
 */
@Data
public class TransferHostMessage extends BaseMessage {

    // 0: 转让 1: 拒绝
    private  int cmd;

    //操作者id
    private String operatorId;

    //操作名称
    private String operatorName;

    //目标用户id
    private String targetUserId;

    //目标用户名称
    private String targetUserName;

    @Override
    public String getObjectName() {
        return "RCMic:transferHostMsg";
    }
}
