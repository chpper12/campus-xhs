package com.chenpperr.xhs.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册请求参数（DTO：前端 → 后端）
 */
@Data
public class RegisterDTO {

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 20, message = "用户名长度在2-20个字符")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度在6-20个字符")
    private String password;

    /**
     * 昵称（可选，默认等于用户名）
     */
    @Size(max = 30, message = "昵称不超过30个字符")
    private String nickname;
}
