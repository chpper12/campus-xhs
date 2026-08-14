package com.chenpperr.xhs.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chenpperr.xhs.common.PageResult;
import com.chenpperr.xhs.entity.Comment;
import com.chenpperr.xhs.vo.CommentVO;

/**
 * 评论 Service 接口
 */
public interface CommentService extends IService<Comment> {

    /**
     * 分页查询某篇笔记的评论列表
     *
     * @param postId 笔记ID
     * @param page   分页对象
     * @return 分页结果（已转换为 CommentVO）
     */
    PageResult<CommentVO> getCommentPage(Long postId, Page<Comment> page);

    /**
     * 发表评论
     *
     * @param postId  笔记ID
     * @param content 评论内容
     * @param userId  用户ID
     * @return 新评论的ID
     */
    Long publishComment(Long postId, String content, Long userId);

    /**
     * 删除评论（只有评论者自己可以删除）
     *
     * @param commentId 评论ID
     * @param userId    当前登录用户ID
     */
    void deleteComment(Long commentId, Long userId);
}
