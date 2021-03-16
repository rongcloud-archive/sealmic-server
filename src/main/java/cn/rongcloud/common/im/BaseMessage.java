package cn.rongcloud.common.im;


import cn.rongcloud.common.utils.GsonUtil;

/**
 * Created by sunyinglong on 2020/6/25
 */
public abstract class BaseMessage {

    public abstract String getObjectName();

    @Override
    public String toString() {
        return GsonUtil.toJson(this);
    }
}
