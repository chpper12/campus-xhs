package com.chenpperr.xhs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chenpperr.xhs.common.PageResult;
import com.chenpperr.xhs.entity.Post;
import com.chenpperr.xhs.entity.PostLike;
import com.chenpperr.xhs.entity.User;
import com.chenpperr.xhs.mapper.PostLikeMapper;
import com.chenpperr.xhs.mapper.PostMapper;
import com.chenpperr.xhs.service.LikeService;
import com.chenpperr.xhs.service.NoticeService;
import com.chenpperr.xhs.service.UserService;
import com.chenpperr.xhs.util.RedisUtil;
import com.chenpperr.xhs.vo.PostCardVO;
import com.chenpperr.xhs.vo.UserSimpleVO;
import com.chenpperr.xhs.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 点赞 业务实现类
 *
 * 使用 Redis Hash 结构实现高性能点赞：
 *   Key:   post:like:{postId}
 *   Field: userId
 *   Value: "1"
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LikeServiceImpl
        extends ServiceImpl<PostLikeMapper, PostLike>
        implements LikeService {

    private final RedisUtil redisUtil;
    private final PostMapper postMapper;
    private final NoticeService noticeService;
    private final UserService userService;

    /**
     * 点赞/取消点赞（切换状态）
     *
     * 流程：
     *   1. 从 Redis Hash 检查当前用户是否已点赞
     *   2. 已点赞 → 取消点赞（删除 Hash Field）
     *   3. 未点赞 → 点赞（添加 Hash Field）+ 创建通知
     *   4. 返回切换后的点赞状态
     *
     * @param userId 用户ID
     * @param postId 笔记ID
     * @return 切换后的点赞状态：true=已点赞，false=已取消
     */
    @Override
    public boolean toggleLike(Long userId, Long postId) {
        String postLikeKey = "post:like:" + postId;
        String userLikedKey = "user:liked:" + userId;
        String field = String.valueOf(userId);

        if (redisUtil.hHasKey(postLikeKey, field)) {
            // 已点赞 → 取消点赞
            redisUtil.hDelete(postLikeKey, field);
            // 从用户点赞列表中移除
            redisUtil.sRemove(userLikedKey, String.valueOf(postId));
            return false;
        } else {
            // 未点赞 → 点赞
            redisUtil.hSet(postLikeKey, field, "1");
            // 添加到用户点赞列表
            redisUtil.sAdd(userLikedKey, String.valueOf(postId));

            // 创建点赞通知（不是给自己的帖子点赞时才发通知）
            try {
                Post post = postMapper.selectById(postId);
                if (post != null && !post.getUserId().equals(userId)) {
                    User fromUser = userService.getById(userId);
                    String nickname = fromUser != null ? fromUser.getNickname() : "未知用户";
                    String title = post.getTitle().length() > 10
                            ? post.getTitle().substring(0, 10) + "..."
                            : post.getTitle();
                    noticeService.createNotice(
                            post.getUserId(),  // 接收者：帖子作者
                            userId,            // 触发者：点赞的人
                            "like",            // 类型
                            nickname + " 赞了你的笔记「" + title + "」",
                            postId
                    );
                }
            } catch (Exception e) {
                log.warn("创建点赞通知失败，不影响点赞结果：{}", e.getMessage());
            }

            return true;
        }
    }

    /**
     * 查询当前用户是否已点赞某篇笔记
     *
     * @param userId 用户ID
     * @param postId 笔记ID
     * @return true=已点赞，false=未点赞
     */
    @Override
    public boolean isLike(Long userId, Long postId) {
        String key = "post:like:" + postId;
        String field = String.valueOf(userId);
        return redisUtil.hHasKey(key, field);
    }

    /**
     * 查询笔记的点赞总数
     *
     * 流程：
     *   1. 优先从 Redis Hash 获取点赞数（高性能）
     *   2. 若 Redis 无数据，降级查询 MySQL（兜底）
     *
     * @param postId 笔记ID
     * @return 点赞数
     */
    @Override
    public Long getLikeCount(Long postId) {
        String key = "post:like:" + postId;
        Long count = redisUtil.hSize(key);

        if (count != null) {
            return count;
        }

        // Redis 无数据，降级查询 MySQL
        Post post = postMapper.selectById(postId);
        if (post == null || post.getLikeCount() == null) {
            return 0L;
        }
        return post.getLikeCount().longValue();
    }

    /**
     * 分页查询某用户点赞过的笔记列表
     *
     * 流程：
     *   1. 先从 Redis Set 获取用户点赞的 postId 列表
     *   2. 如果 Redis 无数据，降级查询数据库 post_like 表
     *   3. 分页处理，查出对应的 Post 并转 VO
     *
     * @param userId 用户ID
     * @param page   分页对象
     * @return 点赞过的笔记卡片列表
     */
    @Override
    public PageResult<PostCardVO> getUserLikedPosts(Long userId, Page<?> page) {
        String userLikedKey = "user:liked:" + userId;

        // 1. 先从 Redis 获取用户点赞的 postId 集合
        Set<String> likedPostIds = redisUtil.sMembers(userLikedKey);

        List<Long> postIdList;
        long total;

        if (likedPostIds != null && !likedPostIds.isEmpty()) {
            // Redis 有数据，使用 Redis 数据
            postIdList = likedPostIds.stream()
                    .map(Long::parseLong)
                    .sorted(Collections.reverseOrder()) // 按ID倒序（最新的在前）
                    .collect(Collectors.toList());
            total = postIdList.size();
            log.debug("从 Redis 获取用户 {} 的点赞列表，共 {} 条", userId, total);
        } else {
            // Redis 无数据，降级查询数据库
            log.debug("Redis 无数据，降级查询数据库获取用户 {} 的点赞列表", userId);
            List<PostLike> allLikes = list(new LambdaQueryWrapper<PostLike>()
                    .eq(PostLike::getUserId, userId)
                    .orderByDesc(PostLike::getCreateTime));

            postIdList = allLikes.stream()
                    .map(PostLike::getPostId)
                    .collect(Collectors.toList());
            total = postIdList.size();

            // 回填 Redis（缓存预热）
            if (!postIdList.isEmpty()) {
                for (Long postId : postIdList) {
                    redisUtil.sAdd(userLikedKey, String.valueOf(postId));
                }
                log.debug("已将用户 {} 的 {} 条点赞记录回填到 Redis", userId, total);
            }
        }

        // 2. 分页处理
        int fromIndex = (int) ((page.getCurrent() - 1) * page.getSize());
        int toIndex = Math.min(fromIndex + (int) page.getSize(), postIdList.size());

        if (fromIndex >= postIdList.size()) {
            return PageResult.of(total, new ArrayList<>());
        }

        List<Long> pagePostIds = postIdList.subList(fromIndex, toIndex);

        // 3. 批量查询 Post 并转 VO
        Long currentUserId = SecurityUtil.getCurrentUserIdOrNull();
        List<PostCardVO> voList = new ArrayList<>();

        for (Long postId : pagePostIds) {
            Post post = postMapper.selectById(postId);
            if (post == null) {
                continue;
            }

            // 判断当前用户是否点赞
            boolean liked = currentUserId != null
                    && redisUtil.hHasKey("post:like:" + post.getId(), String.valueOf(currentUserId));

            // 查询作者信息
            User author = userService.getById(post.getUserId());
            UserSimpleVO authorVO = UserSimpleVO.builder()
                    .userId(author != null ? author.getId() : post.getUserId())
                    .nickname(author != null ? author.getNickname() : "已注销用户")
                    .avatar(author != null ? author.getAvatar() : null)
                    .build();

            // 提取封面图
            String coverUrl = extractFirstImageUrl(post.getImageUrls());

            // 从 Redis 获取实时点赞数
            Long likeCount = getLikeCount(post.getId());

            voList.add(PostCardVO.builder()
                    .id(post.getId())
                    .title(post.getTitle())
                    .category(post.getCategory())
                    .coverUrl(coverUrl)
                    .likeCount(likeCount.intValue())
                    .liked(liked)
                    .author(authorVO)
                    .createTime(post.getCreateTime())
                    .build());
        }

        return PageResult.of(total, voList);
    }

    /**
     * 从 imageUrls JSON 数组中提取第一张图片 URL
     */
    private String extractFirstImageUrl(String imageUrlsJson) {
        if (imageUrlsJson == null || imageUrlsJson.isBlank()) {
            return null;
        }
        try {
            List<String> urls = cn.hutool.json.JSONUtil.toList(imageUrlsJson, String.class);
            return urls.isEmpty() ? null : urls.get(0);
        } catch (Exception e) {
            return null;
        }
    }
}