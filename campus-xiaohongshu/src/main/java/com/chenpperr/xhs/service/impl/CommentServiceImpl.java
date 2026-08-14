package com.chenpperr.xhs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chenpperr.xhs.common.PageResult;
import com.chenpperr.xhs.common.ResultCode;
import com.chenpperr.xhs.entity.Comment;
import com.chenpperr.xhs.entity.Post;
import com.chenpperr.xhs.entity.User;
import com.chenpperr.xhs.exception.BusinessException;
import com.chenpperr.xhs.mapper.CommentMapper;
import com.chenpperr.xhs.mapper.PostMapper;
import com.chenpperr.xhs.service.CommentService;
import com.chenpperr.xhs.service.NoticeService;
import com.chenpperr.xhs.service.UserService;
import com.chenpperr.xhs.vo.CommentVO;
import com.chenpperr.xhs.vo.UserSimpleVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论 业务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    private final UserService userService;
    private final NoticeService noticeService;
    private final PostMapper postMapper;

    /**
     * 分页查询某篇笔记的评论列表
     *
     * 流程：
     *   1. LambdaQueryWrapper 构建查询条件：eq(postId) + orderByDesc(createTime)
     *   2. this.page(page, wrapper) 执行分页查询（ServiceImpl 内置方法）
     *   3. 遍历结果，每条 Comment 转 CommentVO（查 UserService 拿作者信息）
     *   4. 封装成 PageResult 返回
     */
    @Override
    public PageResult<CommentVO> getCommentPage(Long postId, Page<Comment> page) {
        // 第 1 步：构建查询条件
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getPostId, postId)       // 只查这篇笔记的评论
               .orderByDesc(Comment::getCreateTime); // 最新评论在前

        // 第 2 步：执行分页查询
        Page<Comment> commentPage = this.page(page, wrapper);

        // 第 3 步：Entity → VO 转换
        List<CommentVO> voList = commentPage.getRecords().stream()
                .map(this::convertToCommentVO)
                .toList();

        // 第 4 步：封装返回
        return PageResult.of(commentPage.getTotal(), voList);
    }

    /**
     * 发表评论
     *
     * 流程：
     *   1. 构造 Comment Entity（postId、userId、content、createTime）
     *   2. this.save(comment) 保存到数据库
     *   3. 返回新评论的 ID
     */
    @Override
    public Long publishComment(Long postId, String content, Long userId) {
        // 第 1 步：构造 Entity
        Comment comment = Comment.builder()
                .postId(postId)
                .userId(userId)
                .content(content)
                .createTime(LocalDateTime.now())
                .build();

        // 第 2 步：保存到数据库（ServiceImpl 内置方法，保存后自动回填 id）
        this.save(comment);

        // 第 3 步：创建评论通知（不是给自己的帖子评论时才发通知）
        try {
            Post post = postMapper.selectById(postId);
            if (post != null && !post.getUserId().equals(userId)) {
                User fromUser = userService.getById(userId);
                String nickname = fromUser != null ? fromUser.getNickname() : "未知用户";
                String title = post.getTitle().length() > 10
                        ? post.getTitle().substring(0, 10) + "..."
                        : post.getTitle();
                String preview = content.length() > 20
                        ? content.substring(0, 20) + "..."
                        : content;
                noticeService.createNotice(
                        post.getUserId(),  // 接收者：帖子作者
                        userId,            // 触发者：评论的人
                        "comment",         // 类型
                        nickname + " 评论了你的笔记「" + title + "」：" + preview,
                        postId
                );
            }
        } catch (Exception e) {
            log.warn("创建评论通知失败，不影响评论结果：{}", e.getMessage());
        }

        // 第 4 步：返回新评论 ID
        return comment.getId();
    }

    /**
     * 删除评论（只有评论者自己可以删除）
     *
     * 流程：
     *   1. 查出评论，校验是否存在
     *   2. 校验当前用户是否是评论者（不是评论者不能删）
     *   3. 删除评论
     */
    @Override
    public void deleteComment(Long commentId, Long userId) {
        // 第 1 步：查出评论
        Comment comment = this.getById(commentId);
        if (comment == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "评论不存在或已被删除");
        }

        // 第 2 步：权限校验 — 只有评论者能删自己的评论
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权删除他人的评论");
        }

        // 第 3 步：删除评论
        this.removeById(commentId);
        log.info("用户 {} 删除了评论 {}", userId, commentId);
    }

    // ============================== Entity → VO 转换 ==============================

    /**
     * 将 Comment 实体转换为 CommentVO
     *
     * 转换逻辑：
     *   1. 脱敏：只取 id、postId、content、createTime
     *   2. author：查 User 表拿 nickname + avatar（复用 PostServiceImpl 的思路）
     *
     * @param comment 评论实体
     * @return 评论视图对象
     */
    private CommentVO convertToCommentVO(Comment comment) {
        // 查询评论者信息
        UserSimpleVO authorVO = buildAuthorVO(comment.getUserId());

        return CommentVO.builder()
                .id(comment.getId())
                .postId(comment.getPostId())
                .content(comment.getContent())
                .author(authorVO)
                .createTime(comment.getCreateTime())
                .build();
    }

    /**
     * 构建作者简要信息 VO
     *
     * @param userId 作者用户ID
     * @return 作者简要信息，用户不存在时返回空壳对象
     */
    private UserSimpleVO buildAuthorVO(Long userId) {
        User author = userService.getById(userId);
        if (author == null) {
            return UserSimpleVO.builder()
                    .userId(userId)
                    .nickname("已注销用户")
                    .avatar(null)
                    .build();
        }
        return UserSimpleVO.builder()
                .userId(author.getId())
                .nickname(author.getNickname())
                .avatar(author.getAvatar())
                .build();
    }
}
