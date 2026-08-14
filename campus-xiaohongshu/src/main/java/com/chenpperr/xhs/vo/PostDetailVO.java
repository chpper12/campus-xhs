package com.chenpperr.xhs.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 笔记详情视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostDetailVO implements Serializable {

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
     * 笔记内容
     */
    private String content;

    /**
     * 图片URL列表
     */
    private List<String> imageUrls;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 当前用户是否已点赞
     */
    private Boolean liked;

    /**
     * 评论数
     */
    private Integer commentCount;

    /**
     * 标签列表
     */
    private List<String> tags;

    /**
     * 作者信息
     */
    private UserSimpleVO author;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
