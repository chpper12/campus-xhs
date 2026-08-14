package com.chenpperr.xhs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chenpperr.xhs.common.PageResult;
import com.chenpperr.xhs.entity.Notice;
import com.chenpperr.xhs.entity.User;
import com.chenpperr.xhs.mapper.NoticeMapper;
import com.chenpperr.xhs.service.NoticeService;
import com.chenpperr.xhs.service.UserService;
import com.chenpperr.xhs.vo.NoticeVO;
import com.chenpperr.xhs.vo.UserSimpleVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知 业务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements NoticeService {

    private final UserService userService;

    /**
     * 分页查询某用户的通知列表
     *
     * 流程：
     *   1. LambdaQueryWrapper 构建查询条件：eq(toUserId) + orderByDesc(createTime)
     *   2. this.page(page, wrapper) 执行分页查询
     *   3. 遍历结果，每条 Notice 转 NoticeVO（查 UserService 拿触发者信息）
     *   4. 封装成 PageResult 返回
     */
    @Override
    public PageResult<NoticeVO> getNoticePage(Long toUserId, Page<Notice> page) {
        // 第 1 步：构建查询条件
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notice::getToUserId, toUserId)
               .orderByDesc(Notice::getCreateTime);

        // 第 2 步：执行分页查询
        Page<Notice> noticePage = this.page(page, wrapper);

        // 第 3 步：Entity → VO 转换
        List<NoticeVO> voList = noticePage.getRecords().stream()
                .map(this::convertToNoticeVO)
                .toList();

        return PageResult.of(noticePage.getTotal(), voList);
    }

    /**
     * 创建通知
     *
     * 流程：
     *   1. 构造 Notice Entity
     *   2. this.save(notice) 保存到数据库
     *
     * @param toUserId   接收者用户ID
     * @param fromUserId 触发者用户ID
     * @param type       通知类型（like/comment/follow）
     * @param content    通知内容
     * @param postId     关联笔记ID
     */
    @Override
    public void createNotice(Long toUserId, Long fromUserId, String type, String content, Long postId) {
        Notice notice = Notice.builder()
                .toUserId(toUserId)
                .fromUserId(fromUserId)
                .type(type)
                .content(content)
                .postId(postId)
                .isRead(0)
                .createTime(LocalDateTime.now())
                .build();

        this.save(notice);
        log.info("创建通知：{} → {}，类型：{}", fromUserId, toUserId, type);
    }

    /**
     * 标记通知为已读
     *
     * 流程：
     *   1. 查出通知，校验是否存在
     *   2. 设置 isRead = 1
     *   3. 更新数据库
     */
    @Override
    public void markAsRead(Long noticeId) {
        Notice notice = this.getById(noticeId);
        if (notice == null) {
            return;
        }
        notice.setIsRead(1);
        this.updateById(notice);
    }

    // ============================== Entity → VO 转换 ==============================

    /**
     * 将 Notice 实体转换为 NoticeVO
     *
     * 转换逻辑：
     *   1. 查触发者信息（fromUser）
     *   2. isRead 从 Integer(0/1) 转为 Boolean
     *
     * @param notice 通知实体
     * @return 通知视图对象
     */
    private NoticeVO convertToNoticeVO(Notice notice) {
        // 查触发者信息
        UserSimpleVO fromUser = buildUserSimpleVO(notice.getFromUserId());

        return NoticeVO.builder()
                .id(notice.getId())
                .type(notice.getType())
                .content(notice.getContent())
                .fromUser(fromUser)
                .postId(notice.getPostId())
                .isRead(notice.getIsRead() == 1)  // Integer(0/1) → Boolean
                .createTime(notice.getCreateTime())
                .build();
    }

    /**
     * 构建用户简要信息 VO
     *
     * @param userId 用户ID
     * @return 用户简要信息，用户不存在时返回空壳对象（避免 NPE）
     */
    private UserSimpleVO buildUserSimpleVO(Long userId) {
        User user = userService.getById(userId);
        if (user == null) {
            return UserSimpleVO.builder()
                    .userId(userId)
                    .nickname("已注销用户")
                    .avatar(null)
                    .build();
        }
        return UserSimpleVO.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .build();
    }
}
