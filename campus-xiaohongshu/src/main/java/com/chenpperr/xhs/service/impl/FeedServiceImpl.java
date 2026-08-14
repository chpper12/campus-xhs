package com.chenpperr.xhs.service.impl;

import com.chenpperr.xhs.entity.Post;
import com.chenpperr.xhs.service.FeedService;
import com.chenpperr.xhs.service.PostService;
import com.chenpperr.xhs.util.RedisUtil;
import com.chenpperr.xhs.vo.PostCardVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Feed 流 业务实现类
 *
 * 基于 Redis ZSet 实现推模式 Feed 流：
 *   Key:   feed:{userId}
 *   Score: 发布时间戳（毫秒）
 *   Member: 笔记ID（postId）
 *
 * 设计要点：
 *   1. 推模式：发布笔记时推送到每个粉丝的 Feed ZSet
 *   2. 容量限制：每个用户的 Feed 最多保留 20 条（超出自动淘汰最旧的）
 *   3. 降级查询：Feed 为空时降级查数据库最新帖子
 *   4. 已删除帖子过滤：读取时跳过已删除的帖子
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {

    private final RedisUtil redisUtil;

    @Lazy
    @Autowired
    private PostService postService;

    /**
     * Feed 容量上限
     */
    private static final int FEED_MAX_SIZE = 20;

    /**
     * 推送笔记到粉丝的 Feed 流
     *
     * 流程：
     *   1. 从 Redis Set user:follower:{authorId} 获取所有粉丝ID
     *   2. 遍历粉丝，将 postId 推送到每个粉丝的 Feed ZSet
     *   3. 推送失败的跳过，记录日志（不影响其他粉丝）
     *   4. 维护 Feed 容量：超过 FEED_MAX_SIZE 时淘汰最旧的
     *
     * @param authorId 作者用户ID
     * @param postId   笔记ID
     */
    @Override
    public void pushToFollowers(Long authorId, Long postId) {
        // 1. 获取所有粉丝ID（从 Redis Set 读取）
        String followerKey = "user:follower:" + authorId;
        Set<String> followers = redisUtil.sMembers(followerKey);

        if (followers == null || followers.isEmpty()) {
            log.debug("用户{}没有粉丝，跳过Feed推送", authorId);
            return;
        }

        // 2. 当前时间戳作为 score
        double score = System.currentTimeMillis();

        // 3. 遍历粉丝，逐个推送
        int successCount = 0;
        for (String followerId : followers) {
            try {
                String feedKey = "feed:" + followerId;

                // 推送到 Feed ZSet
                redisUtil.zAdd(feedKey, String.valueOf(postId), score);

                // 维护容量：超过上限时淘汰最旧的
                trimFeed(feedKey);

                successCount++;
            } catch (Exception e) {
                // 单个粉丝推送失败，跳过继续（不影响其他粉丝）
                log.warn("推送Feed失败，followerId={}, postId={}, error={}",
                        followerId, postId, e.getMessage());
            }
        }

        log.info("Feed推送完成：authorId={}, postId={}, 粉丝数={}, 成功推送={}",
                authorId, postId, followers.size(), successCount);
    }

    /**
     * 拉取当前用户的 Feed 流（首页动态）
     *
     * 流程：
     *   1. 从 feed:{userId} ZSet 按时间倒序取 postId 列表
     *   2. 如果 Feed 为空，降级查数据库最新帖子
     *   3. 逐个查 Post 表获取详情，过滤已删除的帖子
     *   4. 转换为 PostCardVO 返回
     *
     * @param userId 当前用户ID
     * @param offset 偏移量
     * @param limit  每页条数
     * @return 笔记卡片列表
     */
    @Override
    public List<PostCardVO> getFeed(Long userId, int offset, int limit) {
        String feedKey = "feed:" + userId;

        // 1. 从 ZSet 按时间倒序取 postId 列表
        Set<String> postIds = redisUtil.zReverseRange(feedKey, offset, offset + limit - 1);

        // 2. Feed 为空时降级查数据库
        if (postIds == null || postIds.isEmpty()) {
            log.debug("用户{}的Feed为空，降级查数据库", userId);
            return getFeedFromDB(limit);
        }

        // 3. 逐个查 Post 详情，过滤已删除的帖子
        List<PostCardVO> feedList = new ArrayList<>();
        for (String postIdStr : postIds) {
            try {
                Long postId = Long.parseLong(postIdStr);
                Post post = postService.getById(postId);

                // 跳过已删除或不存在的帖子
                if (post == null || post.getStatus() != 1) {
                    continue;
                }

                PostCardVO cardVO = ((PostServiceImpl) postService).convertToCardVO(post);
                feedList.add(cardVO);
            } catch (Exception e) {
                log.warn("查询Feed中帖子失败，postId={}, error={}", postIdStr, e.getMessage());
            }
        }

        return feedList;
    }

    /**
     * 从所有用户的 Feed 中删除指定笔记
     *
     * 实现方式：SCAN 扫描所有 feed:* 的 key，逐个删除 postId
     * 注意：这是一个耗时操作，建议异步执行
     *
     * @param postId 笔记ID
     */
    @Override
    public void removeFromAllFeeds(Long postId) {
        Set<String> feedKeys = redisUtil.scanKeys("feed:*");
        if (feedKeys == null || feedKeys.isEmpty()) {
            return;
        }

        int count = 0;
        for (String feedKey : feedKeys) {
            try {
                redisUtil.zRemove(feedKey, String.valueOf(postId));
                count++;
            } catch (Exception e) {
                log.warn("从Feed删除笔记失败，feedKey={}, postId={}", feedKey, postId);
            }
        }

        log.info("已从{}个用户的Feed中删除笔记{}", count, postId);
    }

    /**
     * 降级方案：从数据库查询最新帖子
     *
     * 场景：用户的 Feed ZSet 为空时使用（新注册用户、Redis 数据丢失等）
     *
     * @param limit 查询条数
     * @return 笔记卡片列表
     */
    private List<PostCardVO> getFeedFromDB(int limit) {
        // 复用 PostService 的分页查询方法
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Post> page =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, limit);

        var result = postService.getPostPage(page, null);
        return result.getList();
    }

    /**
     * 维护 Feed 容量，超过上限时淘汰最旧的
     *
     * 实现：ZREMRANGEBYRANK 删除 score 最小（最旧）的元素
     *
     * @param feedKey Feed 的 Redis Key
     */
    private void trimFeed(String feedKey) {
        Long size = redisUtil.zSize(feedKey);
        if (size != null && size > FEED_MAX_SIZE) {
            // 删除索引 FEED_MAX_SIZE 之后的所有元素（最旧的）
            // ZREMRANGEBYRANK key 0 (size - FEED_MAX_SIZE - 1)
            redisUtil.zRemoveRange(feedKey, 0, size - FEED_MAX_SIZE - 1);
        }
    }
}
