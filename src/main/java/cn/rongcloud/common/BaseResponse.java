package cn.rongcloud.common;

import lombok.Getter;
import lombok.Setter;

import static cn.rongcloud.common.ErrorEnum.ERR_SUCCESS;

/**
 * Created by weiqinxiao on 2019/2/25.
 */
public class BaseResponse<T> {
    private @Getter int errCode;
    private @Setter @Getter String errMsg;
    private @Setter @Getter String errDetail;
    private @Getter BaseResponseResult data;

    public BaseResponse() {
        this(ERR_SUCCESS);
    }

    public BaseResponse(T data) {
        this(ERR_SUCCESS);
        setData(data);
    }

    public void setData(T data) {
        this.data = new BaseResponseResult<T>(data);
    }

    public BaseResponse(ErrorEnum errorEnum) {
        setErr(errorEnum, "");
    }

    public BaseResponse(ErrorEnum err, String errDetail, T data) {
        setErr(err, errDetail);
        setData(data);
    }

    public void setErr(ErrorEnum error, String errDetail) {
        this.errCode = error.getErrCode();
        this.errMsg = error.getErrMsg();
        this.errDetail = errDetail;
    }

    class BaseResponseResult<R> {
        private @Setter @Getter R result;

        BaseResponseResult(R result) {
            this.result = result;
        }
    }
}
