package cn.rongcloud.common;

/**
 * Created by weiqinxiao on 2019/2/26.
 */
public class ApiException extends RuntimeException {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private ErrorEnum error;
    private String errDetail;
    private Object extraData;


    /**
     * 标准异常，使用预定义的的errCode和errMsg
     *
     * @param error
     */
    public ApiException(ErrorEnum error) {
        this(error, null);
    }

    /**
     * 标准异常，并携带自定义数据
     *
     * @param error
     * @param errDetail
     */
    public ApiException(ErrorEnum error, String errDetail) {
        this(error, errDetail, null);
    }

    /**
     * 自定义异常，使用预定义的的errCode和自定义message,且携带自定义数据
     *
     * @param error
     * @param errDetail
     * @param extraData
     */
    public ApiException(ErrorEnum error, String errDetail, Object extraData) {
        super(null == errDetail ? error.getErrMsg() : errDetail);
        this.error = error;
        this.errDetail = errDetail;
        this.extraData = extraData;
    }

    public ErrorEnum getError() {
        return error;
    }

    public String getErrDetail() {
        return errDetail;
    }

    public Object getExtraData() {
        return extraData;
    }
}
