package com.chenpperr.xhs.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应体
 */
@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String msg;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 私有构造方法
     */
    private Result() {}

    /**
     * 成功响应（无数据）
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 成功响应（有数据）
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMsg(ResultCode.SUCCESS.getMsg());
        result.setData(data);
        return result;
    }

    /**
     * 成功响应（自定义消息）
     */
    public static <T> Result<T> success(String msg, T data) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMsg(msg);
        result.setData(data);
        return result;
    }

    /**
     * 失败响应（使用状态码）
     */
    public static <T> Result<T> error(ResultCode resultCode) {
        Result<T> result = new Result<>();
        result.setCode(resultCode.getCode());
        result.setMsg(resultCode.getMsg());
        return result;
    }

    /**
     * 失败响应（自定义消息）
     */
    public static <T> Result<T> error(Integer code, String msg) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }

    /**
     * 失败响应（使用状态码+自定义消息）
     */
    public static <T> Result<T> error(ResultCode resultCode, String msg) {
        Result<T> result = new Result<>();
        result.setCode(resultCode.getCode());
        result.setMsg(msg);
        return result;
    }
}
