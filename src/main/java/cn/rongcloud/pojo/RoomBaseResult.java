package cn.rongcloud.pojo;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.Date;

/**
 * Created by weiqinxiao on 2019/2/28.
 */
@Data
public class RoomBaseResult {
    private String roomId;
    private Date createDt;
    private int memCount;
    private String creatorUserId;
    private String subject;
    private int roomType;
    private int bgId;


    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }
}
