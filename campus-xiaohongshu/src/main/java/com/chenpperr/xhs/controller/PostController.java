package com.chenpperr.xhs.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chenpperr.xhs.common.PageResult;
import com.chenpperr.xhs.common.Result;
import com.chenpperr.xhs.common.ResultCode;
import com.chenpperr.xhs.dto.AiPolishDTO;
import com.chenpperr.xhs.dto.PostPublishDTO;
import com.chenpperr.xhs.entity.Post;
import com.chenpperr.xhs.service.AiService;
import com.chenpperr.xhs.service.PostService;
import com.chenpperr.xhs.util.SecurityUtil;
import com.chenpperr.xhs.vo.AiPolishVO;
import com.chenpperr.xhs.vo.PostCardVO;
import com.chenpperr.xhs.vo.PostDetailVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 笔记控制器
 */
@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final AiService aiService;

    /**
     * GET /api/v1/posts
     * 分页获取笔记列表（支持按 category 分类过滤）
     *
     * @param current  当前页码，默认 1
     * @param size     每页条数，默认 10
     * @param category 分类标签（可选）
     */
    @GetMapping
    public Result<PageResult<PostCardVO>> listPosts(
            @RequestParam(value = "current", defaultValue = "1") Long current,
            @RequestParam(value = "size", defaultValue = "10") Long size,
            @RequestParam(value = "category", required = false) String category) {

        Page<Post> pageParam = new Page<>(current, size);
        PageResult<PostCardVO> postPage = postService.getPostPage(pageParam, category);

        return Result.success(postPage);
    }

    /**
     * GET /api/v1/posts/{id}
     * 根据 ID 获取笔记详情
     *
     * @param id 笔记ID
     */
    @GetMapping("/{id}")
    public Result<PostDetailVO> getPostById(@PathVariable("id") Long id) {
        PostDetailVO detailVO = postService.getPostById(id);
        if (detailVO == null) {
            return Result.error(ResultCode.NOT_FOUND, "该笔记不存在或已被删除");
        }
        return Result.success(detailVO);
    }

    /**
     * GET /api/v1/posts/search
     * 按关键词搜索笔记（模糊匹配 title 和 content）
     *
     * @param keyword 搜索关键词（必填）
     * @param current 当前页码，默认 1
     * @param size    每页条数，默认 10
     */
    @GetMapping("/search")
    public Result<PageResult<PostCardVO>> searchPosts(
            @RequestParam("keyword") String keyword,
            @RequestParam(value = "current", defaultValue = "1") Long current,
            @RequestParam(value = "size", defaultValue = "10") Long size) {

        Page<Post> pageParam = new Page<>(current, size);
        PageResult<PostCardVO> result = postService.searchPosts(keyword, pageParam);
        return Result.success(result);
    }

    /**
     * GET /api/v1/posts/my
     * 查询当前登录用户发布的笔记列表（个人主页用）
     *
     * @param current 当前页码，默认 1
     * @param size    每页条数，默认 10
     */
    @GetMapping("/my")
    public Result<PageResult<PostCardVO>> myPosts(
            @RequestParam(value = "current", defaultValue = "1") Long current,
            @RequestParam(value = "size", defaultValue = "10") Long size) {

        Long userId = SecurityUtil.getCurrentUserId();
        Page<Post> pageParam = new Page<>(current, size);
        PageResult<PostCardVO> postPage = postService.getUserPostPage(userId, pageParam);

        return Result.success(postPage);
    }

    /**
     * POST /api/v1/posts
     * 发布笔记
     *
     * @param dto 笔记发布参数（title、category、content、imageUrls）
     */
    @PostMapping
    public Result<Long> publishPost(@RequestBody @Valid PostPublishDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        Long postId = postService.publishPost(dto, userId);
        return Result.success(postId);
    }

    /**
     * POST /api/v1/posts/ai-polish
     * AI润色文本并提取标签
     *
     * @param dto 包含用户原始文本
     * @return 润色后的文本和标签列表
     */
    @PostMapping("/ai-polish")
    public Result<AiPolishVO> aiPolish(@RequestBody @Valid AiPolishDTO dto) {
        AiPolishVO result = aiService.polishAndExtractTags(dto.getContent());
        return Result.success(result);
    }

    /**
     * DELETE /api/v1/posts/{id}
     * 删除自己的笔记
     *
     * @param id 笔记ID
     */
    @DeleteMapping("/{id}")
    public Result<Void> deletePost(@PathVariable("id") Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        postService.deletePost(id, userId);
        return Result.success();
    }
}