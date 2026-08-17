package com.chenpperr.xhs.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 编辑个人资料请求参数
 */
@Data
public class UpdateUserDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 昵称
     */
    @Size(max = 30, message = "昵称长度不能超过30字")
    private String nickname;

    /**
     * 个人简介
     */
    @Size(max = 200, message = "简介长度不能超过200字")
    private String bio;

    /**
     * 头像URL
     */
    @Size(max = 500, message = "头像URL过长")
    private String avatar;

    /**
     * 手机号（选填；为 null 表示不修改。@Pattern 对 null 放行，不影响选填语义）
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 邮箱（选填；为 null 表示不修改。@Email 对 null 放行，不影响选填语义）
     */
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100字")
    private String email;
}
