package com.chenpperr.xhs.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 笔记卡片视图对象（用于列表展示）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostCardVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 笔记ID
     */
    private Long id;

    /**
     * 笔记标题
     */
    private String title;

    /**
     * 分区
     */
    private String category;

    /**
     * 封面图URL（第一张图片）
     */
    private String coverUrl;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 当前用户是否已点赞
     */
    private Boolean liked;

    /**
     * 作者信息
     */
    private UserSimpleVO author;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
