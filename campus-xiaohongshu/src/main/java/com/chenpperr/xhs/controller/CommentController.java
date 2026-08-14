package com.chenpperr.xhs.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chenpperr.xhs.common.PageResult;
import com.chenpperr.xhs.common.Result;
import com.chenpperr.xhs.dto.CommentPublishDTO;
import com.chenpperr.xhs.entity.Comment;
import com.chenpperr.xhs.service.CommentService;
import com.chenpperr.xhs.util.SecurityUtil;
import com.chenpperr.xhs.vo.CommentVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 评论控制器
 */
@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * GET /api/v1/posts/{postId}/comments
     * 分页获取某篇笔记的评论列表
     *
     * @param postId  笔记ID（从 URL 路径获取）
     * @param current 当前页码，默认 1
     * @param size    每页条数，默认 10
     */
    @GetMapping("/{postId}/comments")
    public Result<PageResult<CommentVO>> listComments(
            @PathVariable("postId") Long postId,
            @RequestParam(value = "current", defaultValue = "1") Long current,
            @RequestParam(value = "size", defaultValue = "10") Long size) {

        Page<Comment> pageParam = new Page<>(current, size);
        PageResult<CommentVO> commentPage = commentService.getCommentPage(postId, pageParam);

        return Result.success(commentPage);
    }

    /**
     * POST /api/v1/posts/{postId}/comments
     * 给某篇笔记发表评论
     *
     * @param postId 笔记ID（从 URL 路径获取）
     * @param dto    评论内容（只需要 content）
     */
    @PostMapping("/{postId}/comments")
    public Result<Long> publishComment(
            @PathVariable("postId") Long postId,
            @RequestBody @Valid CommentPublishDTO dto) {

        Long userId = SecurityUtil.getCurrentUserId();
        Long commentId = commentService.publishComment(postId, dto.getContent(), userId);

        return Result.success(commentId);
    }

    /**
     * DELETE /api/v1/posts/{postId}/comments/{commentId}
     * 删除自己的评论
     *
     * @param postId    笔记ID（URL 路径，用于 RESTful 语义）
     * @param commentId 评论ID（URL 路径）
     */
    @DeleteMapping("/{postId}/comments/{commentId}")
    public Result<Void> deleteComment(
            @PathVariable("postId") Long postId,
            @PathVariable("commentId") Long commentId) {

        Long userId = SecurityUtil.getCurrentUserId();
        commentService.deleteComment(commentId, userId);

        return Result.success();
    }
}
