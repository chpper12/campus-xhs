package com.chenpperr.xhs.security;

import lombok.Data;

/**
 * 登录响应结果（VO：后端 → 前端）
 */
@Data
public class LoginVO {

    /**
     * JWT Token（通行证）
     */
    private String token;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 个人简介
     */
    private String bio;
}
