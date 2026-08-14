package com.chenpperr.xhs.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chenpperr.xhs.common.PageResult;
import com.chenpperr.xhs.common.Result;
import com.chenpperr.xhs.service.LikeService;
import com.chenpperr.xhs.util.SecurityUtil;
import com.chenpperr.xhs.vo.PostCardVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 点赞控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    /**
     * POST /api/v1/posts/{postId}/like
     * 点赞/取消点赞（切换状态）
     *
     * @param postId 笔记ID
     * @return 切换后的点赞状态：true=已点赞，false=已取消
     */
    @PostMapping("/{postId}/like")
    public Result<Boolean> toggleLike(@PathVariable("postId") Long postId) {
        Long userId = SecurityUtil.getCurrentUserId();
        boolean result = likeService.toggleLike(userId, postId);
        return Result.success(result);
    }

    /**
     * GET /api/v1/posts/{postId}/liked
     * 查询当前用户是否已点赞
     *
     * @param postId 笔记ID
     * @return true=已点赞，false=未点赞
     */
    @GetMapping("/{postId}/liked")
    public Result<Boolean> isLike(@PathVariable("postId") Long postId) {
        Long userId = SecurityUtil.getCurrentUserId();
        boolean result = likeService.isLike(userId, postId);
        return Result.success(result);
    }

    /**
     * GET /api/v1/posts/{postId}/like-count
     * 查询笔记的点赞总数
     *
     * @param postId 笔记ID
     * @return 点赞数
     */
    @GetMapping("/{postId}/like-count")
    public Result<Long> getLikeCount(@PathVariable("postId") Long postId) {
        long result = likeService.getLikeCount(postId);
        return Result.success(result);
    }

    /**
     * GET /api/v1/posts/my/liked
     * 查询当前登录用户点赞过的笔记列表（个人主页「赞过」Tab 用）
     *
     * @param current 当前页码，默认 1
     * @param size    每页条数，默认 10
     * @return 点赞过的笔记卡片列表
     */
    @GetMapping("/my/liked")
    public Result<PageResult<PostCardVO>> myLikedPosts(
            @RequestParam(value = "current", defaultValue = "1") Long current,
            @RequestParam(value = "size", defaultValue = "10") Long size) {

        Long userId = SecurityUtil.getCurrentUserId();
        Page<?> pageParam = new Page<>(current, size);
        PageResult<PostCardVO> result = likeService.getUserLikedPosts(userId, pageParam);
        return Result.success(result);
    }
}
