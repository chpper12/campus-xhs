package com.chenpperr.xhs.controller;

import com.chenpperr.xhs.common.PageResult;
import com.chenpperr.xhs.common.Result;
import com.chenpperr.xhs.service.FeedService;
import com.chenpperr.xhs.util.SecurityUtil;
import com.chenpperr.xhs.vo.PostCardVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Feed 流控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/feed")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    /**
     * GET /api/v1/feed
     * 获取首页动态（Feed 流）
     *
     * @param page 页码（从1开始）
     * @param size 每页条数（默认10）
     * @return 笔记卡片列表
     */
    @GetMapping
    public Result<List<PostCardVO>> getFeed(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        Long userId = SecurityUtil.getCurrentUserId();

        // 计算偏移量
        int offset = (page - 1) * size;

        List<PostCardVO> feedList = feedService.getFeed(userId, offset, size);
        return Result.success(feedList);
    }
}
