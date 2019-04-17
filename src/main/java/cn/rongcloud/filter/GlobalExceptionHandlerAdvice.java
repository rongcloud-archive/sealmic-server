package cn.rongcloud.filter;

/**
 * Created by weiqinxiao on 2019/2/26.
 */

import cn.rongcloud.common.ApiException;
import cn.rongcloud.common.BaseResponse;
import cn.rongcloud.common.ErrorEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandlerAdvice {
    @ResponseStatus(value = HttpStatus.OK)
    @ExceptionHandler({MissingServletRequestParameterException.class,
            IllegalArgumentException.class})
    public BaseResponse<Object> handleRequestParameterException(Exception ex) {
        logException(ex);
        return getResponseData(ErrorEnum.ERR_REQUEST_PARA_ERR, ex.getMessage());
    }

    @ResponseStatus(value = HttpStatus.OK)
    @ExceptionHandler({ServletRequestBindingException.class})
    public BaseResponse<Object> handleInvalidTokenException(Exception ex) {
        logException(ex);
        return getResponseData(ErrorEnum.ERR_INVALID_AUTH, ex.getMessage());
    }


    @ResponseStatus(value = HttpStatus.OK)
    @ExceptionHandler({DataAccessException.class})
    public BaseResponse<Object> handleDatabaseException(Exception ex) {
        logException(ex);
        return getResponseData(ErrorEnum.ERR_OTHER, ((DataAccessException) ex).getMostSpecificCause().getMessage());
    }

    // 400
    @ResponseStatus(value = HttpStatus.OK)
    @ExceptionHandler({TypeMismatchException.class,
            HttpMessageNotReadableException.class,
            MethodArgumentNotValidException.class,
            MissingServletRequestPartException.class,
            BindException.class})
    public BaseResponse<Object> handleBadRequestException(Exception ex) {
        logException(ex);
        return getResponseData(ErrorEnum.ERR_BAD_REQUEST, ex.getMessage());
    }

    @ResponseStatus(value = HttpStatus.OK)
    @ExceptionHandler({HttpRequestMethodNotSupportedException.class,
            HttpMediaTypeNotSupportedException.class,
            HttpMediaTypeNotAcceptableException.class,
            MissingPathVariableException.class,
            ConversionNotSupportedException.class,
            HttpMessageNotWritableException.class,
            IOException.class,
            RuntimeException.class,
            AsyncRequestTimeoutException.class})
    public BaseResponse<Object> handleOtherException(Exception ex) {
        logException(ex);
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        printWriter.print(ex);
        return getResponseData(ErrorEnum.ERR_OTHER,  stringWriter.toString());
    }

    @ResponseStatus(value = HttpStatus.OK)
    @ExceptionHandler(ApiException.class)
    public BaseResponse<Object> handleMiMicroAPIException(ApiException ex) {
        logException(ex);
        return getResponseData(ex);
    }

    private void logException(Exception ex) {
        log.error("caught exception:", ex);
    }

    private BaseResponse<Object> getResponseData(ApiException ex) {
        return new BaseResponse<>(ex.getError(), ex.getErrDetail(), ex.getExtraData());
    }

    private BaseResponse<Object> getResponseData(ErrorEnum err, String errMsg) {
        return new BaseResponse<>(err, errMsg, null);
    }
}