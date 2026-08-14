package com.chenpperr.xhs.service;

import com.chenpperr.xhs.vo.PostCardVO;

import java.util.List;

/**
 * Feed 流 Service 接口
 *
 * 基于 Redis ZSet 实现推模式 Feed 流：
 *   Key:   feed:{userId}
 *   Score: 发布时间戳（毫秒）
 *   Member: 笔记ID（postId）
 */
public interface FeedService {

    /**
     * 推送笔记到粉丝的 Feed 流
     *
     * 场景：用户发布笔记时调用，将笔记ID推送到所有粉丝的 Feed ZSet
     *
     * @param authorId 作者用户ID
     * @param postId   笔记ID
     */
    void pushToFollowers(Long authorId, Long postId);

    /**
     * 拉取当前用户的 Feed 流（首页动态）
     *
     * 流程：
     *   1. 从 feed:{userId} ZSet 按时间倒序取最新的 N 条 postId
     *   2. 查 Post 表获取详情
     *   3. 过滤已删除的帖子
     *   4. 转换为 PostCardVO 返回
     *
     * @param userId 当前用户ID
     * @param offset 偏移量（分页用）
     * @param limit  每页条数
     * @return 笔记卡片列表
     */
    List<PostCardVO> getFeed(Long userId, int offset, int limit);

    /**
     * 从所有用户的 Feed 中删除指定笔记
     *
     * 场景：笔记被删除时调用（异步执行，不阻塞主流程）
     *
     * @param postId 笔记ID
     */
    void removeFromAllFeeds(Long postId);
}
