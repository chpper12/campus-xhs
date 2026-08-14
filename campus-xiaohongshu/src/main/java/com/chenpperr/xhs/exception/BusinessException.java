package com.chenpperr.xhs.exception;

import com.chenpperr.xhs.common.ResultCode;
import lombok.Getter;

/**
 * 自定义业务异常
 */
@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     */
    private final Integer code;

    /**
     * 构造方法（使用ResultCode）
     */
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMsg());
        this.code = resultCode.getCode();
    }

    /**
     * 构造方法（使用ResultCode + 自定义消息）
     */
    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }

    /**
     * 构造方法（自定义code + 消息）
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
