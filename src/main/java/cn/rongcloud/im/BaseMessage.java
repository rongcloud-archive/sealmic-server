package cn.rongcloud.im;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by weiqinxiao on 2019/3/1.
 */
public abstract class BaseMessage {

    public abstract String getObjectName();

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }
}
