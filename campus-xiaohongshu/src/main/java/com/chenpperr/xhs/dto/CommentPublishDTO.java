package com.chenpperr.xhs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 发表评论请求参数
 *
 * 注意：postId 从 URL 路径获取（/api/v1/posts/{postId}/comments），
 * 不在 body 里传递，所以 DTO 里只有 content。
 */
@Data
public class CommentPublishDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 评论内容
     */
    @NotBlank(message = "评论内容不能为空")
    @Size(max = 500, message = "评论内容长度不能超过500字")
    private String content;
}
