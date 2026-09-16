package com.chenpperr.xhs.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chenpperr.xhs.common.PageResult;
import com.chenpperr.xhs.common.Result;
import com.chenpperr.xhs.domain.dto.UpdateUserDTO;
import com.chenpperr.xhs.domain.entity.Post;
import com.chenpperr.xhs.service.PostService;
import com.chenpperr.xhs.service.UserService;
import com.chenpperr.xhs.util.SecurityUtil;
import com.chenpperr.xhs.domain.vo.PostCardVO;
import com.chenpperr.xhs.domain.vo.UserProfileVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户资料控制器
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PostService postService;

    /**
     * GET /api/v1/users/{userId}
     * 获取用户资料（个人主页用）
     *
     * 返回用户基本信息 + 统计数据（笔记数、关注数、粉丝数）
     * 如果当前用户已登录且查看的是他人主页，还会返回 isFollowed 状态
     * 钱包余额 balance 仅在查看自己主页时返回，其他情况为 null
     *
     * @param userId 目标用户ID
     * @return 用户资料 VO
     */
    @GetMapping("/{userId}")
    public Result<UserProfileVO> getUserProfile(@PathVariable("userId") Long userId) {
        return Result.success(userService.getUserProfile(userId));
    }

    /**
     * GET /api/v1/users/{userId}/posts
     * 分页查询某用户发布的笔记列表（查看他人主页用）
     *
     * @param userId  目标用户ID
     * @param current 当前页码，默认 1
     * @param size    每页条数，默认 10
     * @return 笔记卡片列表
     */
    @GetMapping("/{userId}/posts")
    public Result<PageResult<PostCardVO>> getUserPosts(
            @PathVariable("userId") Long userId,
            @RequestParam(value = "current", defaultValue = "1") Long current,
            @RequestParam(value = "size", defaultValue = "10") Long size) {

        Page<Post> pageParam = new Page<>(current, size);
        PageResult<PostCardVO> postPage = postService.getUserPostPage(userId, pageParam);
        return Result.success(postPage);
    }

    /**
     * PUT /api/v1/users/me
     * 编辑当前登录用户的个人资料（昵称、简介、头像、手机号、邮箱）
     * 所有字段均为可选，只更新请求体中非 null 的字段
     *
     * @param dto 更新参数
     * @return 统一结果
     */
    @PutMapping("/me")
    public Result<Void> updateProfile(@Valid @RequestBody UpdateUserDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        userService.updateProfile(userId, dto);
        return Result.success();
    }
}
