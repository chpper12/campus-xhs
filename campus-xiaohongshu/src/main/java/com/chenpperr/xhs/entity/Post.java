package com.chenpperr.xhs.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 笔记实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("post")
public class Post implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 笔记ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 作者ID
     */
    private Long userId;

    /**
     * 笔记标题
     */
    private String title;

    /**
     * 分区：推荐/穿搭/美食/职场/情感/家居/游戏/旅行/健身/视频
     */
    private String category;

    /**
     * 用户原始内容
     */
    private String content;

    /**
     * AI润色后的内容
     */
    private String polishedContent;

    /**
     * AI提取的标签，JSON数组格式
     */
    private String tags;

    /**
     * 图片URL列表，JSON数组格式
     */
    private String imageUrls;

    /**
     * 点赞数（冗余字段）
     */
    private Integer likeCount;

    /**
     * 评论数（冗余字段）
     */
    private Integer commentCount;

    /**
     * 状态：0-草稿 1-已发布 2-已删除
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}