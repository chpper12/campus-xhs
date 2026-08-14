package com.chenpperr.xhs.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chenpperr.xhs.entity.Post;
import com.chenpperr.xhs.entity.PostLike;
import com.chenpperr.xhs.mapper.PostLikeMapper;
import com.chenpperr.xhs.mapper.PostMapper;
import com.chenpperr.xhs.service.LikeService;
import com.chenpperr.xhs.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 点赞数据定时同步任务
 * <p>
 * 每 15 分钟将 Redis 中的点赞数据批量刷回 MySQL：
 * 1. SCAN 扫描所有 post:like:{postId} 的 Hash key
 * 2. hGetAll 读取每个 Hash 的全部点赞用户
 * 3. 用 Redis 中的全量数据覆盖 MySQL（先删后插，保证最终一致）
 * 4. 更新 post 表的 like_count 冗余字段
 */
@Slf4j
@Component
@RequiredArgsConstructor
@EnableScheduling  // 开启定时任务功能（必须加，否则 @Scheduled 不生效）
public class LikeSyncTask {

    private final RedisUtil redisUtil;
    private final PostLikeMapper postLikeMapper;
    private final PostMapper postMapper;
    // 注入 LikeService 以使用 saveBatch 批量插入（比逐条 insert 快很多）
    private final LikeService likeService;

    /**
     * 匹配 post:like:{postId} 中的 postId
     */
    private static final Pattern LIKE_KEY_PATTERN = Pattern.compile("^post:like:(\\d+)$");

    /**
     * 每 15 分钟执行一次（从第 30 秒开始，避免整点压力）
     */
    @Scheduled(cron = "30 */15 * * * ?")
    public void syncLikesToDb() {
        log.info("====== 开始同步 Redis 点赞数据到 MySQL ======");
        long start = System.currentTimeMillis();

        // 1. SCAN 扫描所有 post:like:* 的 key
        Set<String> likeKeys = redisUtil.scanKeys("post:like:*");
        if (likeKeys.isEmpty()) {
            log.info("没有需要同步的点赞数据，跳过");
            return;
        }
        log.info("扫描到 {} 个点赞 Hash key", likeKeys.size());

        int totalSynced = 0;
        int postUpdated = 0;

        // 2. 遍历每个 key，逐个帖子同步
        for (String key : likeKeys) {
            Matcher matcher = LIKE_KEY_PATTERN.matcher(key);
            if (!matcher.find()) {
                log.warn("key 格式不匹配，跳过: {}", key);
                continue;
            }

            Long postId = Long.parseLong(matcher.group(1));

            try {
                int synced = syncOnePost(postId, key);
                totalSynced += synced;
                postUpdated++;
            } catch (Exception e) {
                log.error("同步帖子点赞失败, postId={}", postId, e);
            }
        }

        long elapsed = System.currentTimeMillis() - start;
        log.info("====== 同步完成：同步 {} 条点赞记录，更新 {} 个帖子，耗时 {}ms ======",
                totalSynced, postUpdated, elapsed);
    }

    /**
     * 同步单个帖子的点赞数据
     * <p>
     * 策略：以 Redis 为准，先删后插，保证最终一致性。
     * 理由：用户可能点赞又取消，Redis 是当前状态，MySQL 应与之完全同步。
     *
     * 注意：这里不用 @Transactional，因为同类内部调用不走 AOP 代理，事务不会生效。
     * 改为在 delete + insert 外层 try-catch，单个帖子失败不影响其他帖子。
     *
     * @param postId 帖子ID
     * @param key    Redis Hash key
     * @return 本次同步的点赞记录数
     */
    public int syncOnePost(Long postId, String key) {
        // 3. 读取该帖子在 Redis 中的所有点赞用户 {userId -> "1"}
        Map<Object, Object> likedUsers = redisUtil.hGetAll(key);
        LocalDateTime now = LocalDateTime.now();

        // 4. 以 Redis 为准：先删除该帖子的旧记录，再批量插入
        postLikeMapper.delete(
                new LambdaQueryWrapper<PostLike>().eq(PostLike::getPostId, postId)
        );

        if (likedUsers.isEmpty()) {
            // Redis 中没有点赞记录，只更新 like_count = 0
            updatePostLikeCount(postId, 0, now);
            return 0;
        }

        // 5. 构造点赞记录列表
        List<PostLike> batchLikes = new ArrayList<>();
        for (Map.Entry<Object, Object> entry : likedUsers.entrySet()) {
            Long userId = Long.parseLong(entry.getKey().toString());
            batchLikes.add(PostLike.builder()
                    .postId(postId)
                    .userId(userId)
                    .createTime(now)
                    .build());
        }

        // 6. 批量插入（IService.saveBatch 内部会自动分批，每批 1000 条，比逐条 insert 快）
        likeService.saveBatch(batchLikes);

        // 7. 更新 post 表的 like_count
        updatePostLikeCount(postId, batchLikes.size(), now);

        return batchLikes.size();
    }

    /**
     * 更新 post 表的 like_count
     */
    private void updatePostLikeCount(Long postId, int likeCount, LocalDateTime now) {
        Post post = new Post();
        post.setId(postId);
        post.setLikeCount(likeCount);
        post.setUpdateTime(now);
        postMapper.updateById(post);
    }
}
