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
 * 通知实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("notice")
public class Notice implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 通知ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 接收者ID（帖子作者，被通知的人）
     */
    private Long toUserId;

    /**
     * 触发者ID（点赞/评论的人）
     */
    private Long fromUserId;

    /**
     * 通知类型：like-点赞 comment-评论
     */
    private String type;

    /**
     * 通知文案（已拼好的完整句子）
     * 例如："测试用户 赞了你的笔记「食堂新品红烧肉测评」"
     */
    private String content;

    /**
     * 关联的笔记ID（前端点击通知可跳转到对应笔记）
     */
    private Long postId;

    /**
     * 是否已读：0-未读 1-已读
     */
    private Integer isRead;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
