package com.chenpperr.xhs.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chenpperr.xhs.common.PageResult;
import com.chenpperr.xhs.entity.Notice;
import com.chenpperr.xhs.vo.NoticeVO;

/**
 * 通知 Service 接口
 */
public interface NoticeService extends IService<Notice> {

    /**
     * 分页查询某用户的通知列表
     *
     * @param toUserId 接收者用户ID
     * @param page     分页对象
     * @return 分页结果（已转换为 NoticeVO）
     */
    PageResult<NoticeVO> getNoticePage(Long toUserId, Page<Notice> page);

    /**
     * 创建通知
     *
     * @param toUserId   接收者用户ID
     * @param fromUserId 触发者用户ID
     * @param type       通知类型（like/comment/follow）
     * @param content    通知内容
     * @param postId     关联笔记ID
     */
    void createNotice(Long toUserId, Long fromUserId, String type, String content, Long postId);

    /**
     * 标记通知为已读
     *
     * @param noticeId 通知ID
     */
    void markAsRead(Long noticeId);

}
