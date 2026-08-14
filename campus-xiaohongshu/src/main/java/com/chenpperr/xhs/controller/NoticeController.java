package com.chenpperr.xhs.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chenpperr.xhs.common.PageResult;
import com.chenpperr.xhs.common.Result;
import com.chenpperr.xhs.entity.Notice;
import com.chenpperr.xhs.service.NoticeService;
import com.chenpperr.xhs.util.SecurityUtil;
import com.chenpperr.xhs.vo.NoticeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 通知控制器
 */
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    /**
     * GET /api/v1/notifications?current=1&size=10
     * 分页获取当前用户的通知列表
     *
     * @param current 当前页码，默认 1
     * @param size    每页条数，默认 10
     */
    @GetMapping
    public Result<PageResult<NoticeVO>> listNotifications(
            @RequestParam(value = "current", defaultValue = "1") Long current,
            @RequestParam(value = "size", defaultValue = "10") Long size) {

        Long toUserId = SecurityUtil.getCurrentUserId();
        Page<Notice> pageParam = new Page<>(current, size);
        PageResult<NoticeVO> pageResult = noticeService.getNoticePage(toUserId, pageParam);

        return Result.success(pageResult);
    }

    /**
     * PUT /api/v1/notifications/{id}/read
     * 标记单条通知为已读
     *
     * @param id 通知ID
     */
    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(@PathVariable("id") Long id) {
        noticeService.markAsRead(id);
        return Result.success();
    }
}
