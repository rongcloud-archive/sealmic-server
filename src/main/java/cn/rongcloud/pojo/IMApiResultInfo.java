package cn.rongcloud.pojo;

import lombok.Data;

/**
 * Created by weiqinxiao on 2019/2/28.
 */
@Data
public class IMApiResultInfo {
    // 返回码，200 为正常。
    Integer code;
    // 错误信息。
    String errorMessage;

    public boolean isSuccess() {
        return code == 200;
    }
}
