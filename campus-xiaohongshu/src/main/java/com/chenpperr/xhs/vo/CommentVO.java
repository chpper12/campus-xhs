package com.chenpperr.xhs.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评论视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 评论ID
     */
    private Long id;

    /**
     * 笔记ID
     */
    private Long postId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 评论者信息
     */
    private UserSimpleVO author;

    /**
     * 评论时间
     */
    private LocalDateTime createTime;
}
