package com.chenpperr.xhs.dto;

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
}
