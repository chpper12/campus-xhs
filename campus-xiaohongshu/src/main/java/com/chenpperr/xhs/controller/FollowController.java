package com.chenpperr.xhs.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chenpperr.xhs.common.PageResult;
import com.chenpperr.xhs.common.Result;
import com.chenpperr.xhs.service.FollowService;
import com.chenpperr.xhs.util.SecurityUtil;
import com.chenpperr.xhs.vo.FollowUserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 关注关系控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    /**
     * POST /api/v1/users/{userId}/follow
     * 关注某用户
     *
     * @param userId 要关注的用户ID
     * @return 操作结果
     */
    @PostMapping("/{userId}/follow")
    public Result<Void> follow(@PathVariable("userId") Long userId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        followService.follow(currentUserId, userId);
        return Result.success();
    }

    /**
     * DELETE /api/v1/users/{userId}/follow
     * 取消关注某用户
     *
     * @param userId 要取关的用户ID
     * @return 操作结果
     */
    @DeleteMapping("/{userId}/follow")
    public Result<Void> unfollow(@PathVariable("userId") Long userId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        followService.unfollow(currentUserId, userId);
        return Result.success();
    }

    /**
     * GET /api/v1/users/{userId}/followed
     * 查询当前用户是否已关注某用户
     *
     * @param userId 目标用户ID
     * @return true=已关注，false=未关注
     */
    @GetMapping("/{userId}/followed")
    public Result<Boolean> isFollowing(@PathVariable("userId") Long userId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        boolean result = followService.isFollowing(currentUserId, userId);
        return Result.success(result);
    }

    /**
     * GET /api/v1/users/{userId}/following
     * 分页查询某用户的关注列表
     *
     * @param userId 目标用户ID
     * @param page   页码
     * @param size   每页条数
     * @return 关注用户列表
     */
    @GetMapping("/{userId}/following")
    public Result<PageResult<FollowUserVO>> getFollowingList(
            @PathVariable("userId") Long userId,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size) {
        Page<?> pageObj = new Page<>(page, size);
        PageResult<FollowUserVO> result = followService.getFollowingList(userId, pageObj);
        return Result.success(result);
    }

    /**
     * GET /api/v1/users/{userId}/followers
     * 分页查询某用户的粉丝列表
     *
     * @param userId 目标用户ID
     * @param page   页码
     * @param size   每页条数
     * @return 粉丝用户列表
     */
    @GetMapping("/{userId}/followers")
    public Result<PageResult<FollowUserVO>> getFollowerList(
            @PathVariable("userId") Long userId,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size) {
        Page<?> pageObj = new Page<>(page, size);
        PageResult<FollowUserVO> result = followService.getFollowerList(userId, pageObj);
        return Result.success(result);
    }

    /**
     * GET /api/v1/users/{userId}/following-count
     * 查询某用户的关注总数
     *
     * @param userId 用户ID
     * @return 关注数
     */
    @GetMapping("/{userId}/following-count")
    public Result<Long> getFollowingCount(@PathVariable("userId") Long userId) {
        long result = followService.getFollowingCount(userId);
        return Result.success(result);
    }

    /**
     * GET /api/v1/users/{userId}/follower-count
     * 查询某用户的粉丝总数
     *
     * @param userId 用户ID
     * @return 粉丝数
     */
    @GetMapping("/{userId}/follower-count")
    public Result<Long> getFollowerCount(@PathVariable("userId") Long userId) {
        long result = followService.getFollowerCount(userId);
        return Result.success(result);
    }
}
