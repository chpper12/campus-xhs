package com.chenpperr.xhs.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应状态码枚举
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    /**
     * 成功
     */
    SUCCESS(200, "success"),

    /**
     * 参数错误
     */
    BAD_REQUEST(400, "参数错误"),

    /**
     * 未认证
     */
    UNAUTHORIZED(401, "未认证"),

    /**
     * 无权限
     */
    FORBIDDEN(403, "无权限"),

    /**
     * 资源未找到
     */
    NOT_FOUND(404, "资源未找到"),

    /**
     * 服务器异常
     */
    INTERNAL_ERROR(500, "服务器异常");

    /**
     * 状态码
     */
    private final Integer code;

    /**
     * 消息
     */
    private final String msg;
}
