package cn.rongcloud.mic.room.message;

import cn.rongcloud.common.im.BaseMessage;
import lombok.Data;

/**
 * Created by sunyinglong on 2020/6/12
 */
@Data
public class ChrmKVNoticeMessage extends BaseMessage {

    private Integer type; //聊天室中对属性操作后发送通知的类型，1 为设置属性内容、2 为删除属性内容。

    private String key; //聊天室中属性名称，大小不超过 128 个字符。

    private String value; //属性对应的内容，大小不超过 4096 个字符。

    private String extra; //通过消息中携带的附加信息，对应到设置属性接口中的 notificationExtra 值。

    @Data
    public static class ChrmKVChange{
        private Integer changeType; // 1 正常上麦 2 正常下麦 3 观众接管主持麦位 4 主播接管主持麦位 5 主持麦位转让
    }
    @Override
    public String getObjectName() {
        return "RC:chrmKVNotiMsg";
    }
}
