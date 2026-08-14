package com.chenpperr.xhs.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 通知ID
     */
    private Long id;

    /**
     * 通知类型：like-点赞 comment-评论
     */
    private String type;

    /**
     * 通知文案
     * 例如："测试用户 赞了你的笔记「食堂新品红烧肉测评」"
     */
    private String content;

    /**
     * 触发者信息（谁点赞/评论了）
     */
    private UserSimpleVO fromUser;

    /**
     * 关联的笔记ID（点击通知可跳转）
     */
    private Long postId;

    /**
     * 是否已读
     */
    private Boolean isRead;

    /**
     * 通知时间
     */
    private LocalDateTime createTime;
}
