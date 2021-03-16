package cn.rongcloud.mic.common.rest;

import cn.rongcloud.common.utils.GsonUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RestResult {

    @JsonProperty("code")
    private int code;

    @JsonInclude(Include.NON_NULL)
    @JsonProperty("msg")
    private String msg;

    @JsonProperty("data")
    private Object result = null;

    public RestResult setCode(int code) {
        this.code = code;
        return this;
    }

    public int getCode() {
        return this.code;
    }

    public RestResult setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public String getMsg() {
        return this.msg;
    }

    public RestResult setResult(Object value) {
        this.result = value;
        return this;
    }

    public Object getResult() {
        return this.result;
    }

    public Object build() {
        return this;
    }

    public static RestResult success() {
        return new RestResult().setCode(RestResultCode.ERR_SUCCESS.getCode());
    }

    public static RestResult success(Object o) {
        return RestResult.success().setResult(o);
    }

    public static RestResult generic(RestResultCode code) {
        return new RestResult().setCode(code.getCode()).setMsg(code.getMsg());
    }

    public static RestResult generic(RestResultCode code, String msg) {
        return new RestResult().setCode(code.getCode()).setMsg(msg);
    }

    public static RestResult generic(RestResultCode code, Object object) {
        RestResult result = new RestResult().setCode(code.getCode()).setResult(object);
        if (code != RestResultCode.ERR_SUCCESS) {
            result.setMsg(code.getMsg());
        }
        return result;
    }

    public static RestResult generic(int code, String msg) {
        return new RestResult().setCode(code).setMsg(msg);
    }

    @JsonIgnore
    public boolean isSuccess() {
        return this.code == RestResultCode.ERR_SUCCESS.getCode();
    }

    @Override
    public String toString() {
        return GsonUtil.toJson(this, RestResult.class);
    }
}
