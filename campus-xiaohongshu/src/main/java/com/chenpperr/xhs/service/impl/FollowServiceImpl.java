package com.chenpperr.xhs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chenpperr.xhs.common.PageResult;
import com.chenpperr.xhs.common.ResultCode;
import com.chenpperr.xhs.entity.Follow;
import com.chenpperr.xhs.entity.User;
import com.chenpperr.xhs.exception.BusinessException;
import com.chenpperr.xhs.mapper.FollowMapper;
import com.chenpperr.xhs.service.FollowService;
import com.chenpperr.xhs.service.NoticeService;
import com.chenpperr.xhs.service.UserService;
import com.chenpperr.xhs.util.RedisUtil;
import com.chenpperr.xhs.vo.FollowUserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 关注关系 业务实现类
 *
 * 使用 Redis Set 结构缓存关注关系：
 *   Key:   user:following:{userId}   → 我关注了谁（Set<followUserId>）
 *   Key:   user:follower:{userId}    → 谁关注了我（Set<userId>）
 *
 * 好处：
 *   1. 判断是否关注 O(1) — SISMEMBER
 *   2. 关注数/粉丝数 O(1) — SCARD
 *   3. 后续 Feed 流：发布笔记时遍历 user:follower:{authorId} 推送到粉丝的 Feed ZSet
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FollowServiceImpl
        extends ServiceImpl<FollowMapper, Follow>
        implements FollowService {

    private final RedisUtil redisUtil;
    private final UserService userService;
    private final NoticeService noticeService;

    /**
     * 关注某用户
     *
     * 流程：
     *   1. 校验不能关注自己
     *   2. 从 Redis 判断是否已关注（幂等）
     *   3. 写入数据库
     *   4. 更新 Redis 缓存（两个 Set 同步更新）
     *   5. 创建关注通知
     *
     * @param userId       当前用户ID
     * @param followUserId 要关注的用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void follow(Long userId, Long followUserId) {
        // 1. 校验不能关注自己
        if (userId.equals(followUserId)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "不能关注自己");
        }

        // 2. 判断是否已关注
        if (isFollowing(userId, followUserId)) {
            return; // 幂等，已关注则忽略
        }

        // 3. 写入数据库
        Follow follow = Follow.builder()
                .userId(userId)
                .followUserId(followUserId)
                .build();
        save(follow);

        // 4. 更新 Redis 缓存
        String followingKey = "user:following:" + userId;
        String followerKey = "user:follower:" + followUserId;
        redisUtil.sAdd(followingKey, String.valueOf(followUserId));
        redisUtil.sAdd(followerKey, String.valueOf(userId));

        // 5. 创建关注通知
        try {
            User fromUser = userService.getById(userId);
            String nickname = fromUser != null ? fromUser.getNickname() : "未知用户";
            noticeService.createNotice(
                    followUserId,     // 接收者：被关注的人
                    userId,           // 触发者：关注的人
                    "follow",         // 类型
                    nickname + " 关注了你",
                    null              // 无关联笔记
            );
        } catch (Exception e) {
            log.warn("创建关注通知失败，不影响关注结果：{}", e.getMessage());
        }

        log.info("用户{}关注了用户{}", userId, followUserId);
    }

    /**
     * 取消关注某用户
     *
     * 流程：
     *   1. 从 Redis 判断是否已关注
     *   2. 删除数据库记录
     *   3. 更新 Redis 缓存
     *
     * @param userId       当前用户ID
     * @param followUserId 要取关的用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfollow(Long userId, Long followUserId) {
        // 1. 判断是否已关注
        if (!isFollowing(userId, followUserId)) {
            return; // 未关注则忽略
        }

        // 2. 删除数据库记录
        remove(new LambdaQueryWrapper<Follow>()
                .eq(Follow::getUserId, userId)
                .eq(Follow::getFollowUserId, followUserId));

        // 3. 更新 Redis 缓存
        String followingKey = "user:following:" + userId;
        String followerKey = "user:follower:" + followUserId;
        redisUtil.sRemove(followingKey, String.valueOf(followUserId));
        redisUtil.sRemove(followerKey, String.valueOf(userId));

        log.info("用户{}取消关注了用户{}", userId, followUserId);
    }

    /**
     * 查询当前用户是否已关注某用户
     *
     * 优先从 Redis 查询，降级查数据库
     *
     * @param userId       当前用户ID
     * @param followUserId 目标用户ID
     * @return true=已关注，false=未关注
     */
    @Override
    public boolean isFollowing(Long userId, Long followUserId) {
        String key = "user:following:" + userId;
        return Boolean.TRUE.equals(redisUtil.sIsMember(key, String.valueOf(followUserId)));
    }

    /**
     * 分页查询某用户的关注列表
     *
     * @param userId 目标用户ID
     * @param page   分页对象
     * @return 关注用户列表
     */
    @Override
    public PageResult<FollowUserVO> getFollowingList(Long userId, Page<?> page) {
        // 分页查询关注记录
        Page<Follow> followPage = new Page<>(page.getCurrent(), page.getSize());
        Page<Follow> result = page(followPage, new LambdaQueryWrapper<Follow>()
                .eq(Follow::getUserId, userId)
                .orderByDesc(Follow::getCreateTime));

        // 转换为 VO
        Long currentUserId = null;
        try {
            currentUserId = com.chenpperr.xhs.util.SecurityUtil.getCurrentUserIdOrNull();
        } catch (Exception ignored) {
        }

        List<FollowUserVO> voList = buildFollowUserVOList(
                result.getRecords(), true, currentUserId);

        return PageResult.of(result.getTotal(), voList);
    }

    /**
     * 分页查询某用户的粉丝列表
     *
     * @param userId 目标用户ID
     * @param page   分页对象
     * @return 粉丝用户列表
     */
    @Override
    public PageResult<FollowUserVO> getFollowerList(Long userId, Page<?> page) {
        // 分页查询粉丝记录
        Page<Follow> followPage = new Page<>(page.getCurrent(), page.getSize());
        Page<Follow> result = page(followPage, new LambdaQueryWrapper<Follow>()
                .eq(Follow::getFollowUserId, userId)
                .orderByDesc(Follow::getCreateTime));

        // 转换为 VO
        Long currentUserId = null;
        try {
            currentUserId = com.chenpperr.xhs.util.SecurityUtil.getCurrentUserIdOrNull();
        } catch (Exception ignored) {
        }

        List<FollowUserVO> voList = buildFollowUserVOList(
                result.getRecords(), false, currentUserId);

        return PageResult.of(result.getTotal(), voList);
    }

    /**
     * 查询某用户的关注总数
     *
     * @param userId 用户ID
     * @return 关注数
     */
    @Override
    public Long getFollowingCount(Long userId) {
        String key = "user:following:" + userId;
        Long count = redisUtil.sSize(key);
        if (count != null && count > 0) {
            return count;
        }
        // 降级查数据库
        return count(new LambdaQueryWrapper<Follow>().eq(Follow::getUserId, userId));
    }

    /**
     * 查询某用户的粉丝总数
     *
     * @param userId 用户ID
     * @return 粉丝数
     */
    @Override
    public Long getFollowerCount(Long userId) {
        String key = "user:follower:" + userId;
        Long count = redisUtil.sSize(key);
        if (count != null && count > 0) {
            return count;
        }
        // 降级查数据库
        return count(new LambdaQueryWrapper<Follow>().eq(Follow::getFollowUserId, userId));
    }

    /**
     * 构建关注/粉丝用户 VO 列表
     *
     * @param records       关注记录列表
     * @param isFollowing    true=关注列表（取 followUserId），false=粉丝列表（取 userId）
     * @param currentUserId 当前登录用户ID（用于判断互关）
     * @return VO 列表
     */
    private List<FollowUserVO> buildFollowUserVOList(List<Follow> records,
                                                      boolean isFollowing,
                                                      Long currentUserId) {
        List<FollowUserVO> voList = new ArrayList<>();
        for (Follow record : records) {
            Long targetUserId = isFollowing ? record.getFollowUserId() : record.getUserId();
            User user = userService.getById(targetUserId);
            if (user == null) {
                continue;
            }

            // 判断是否互相关注
            Boolean isMutual = false;
            if (currentUserId != null) {
                isMutual = isFollowing(currentUserId, targetUserId)
                        && isFollowing(targetUserId, currentUserId);
            }

            voList.add(FollowUserVO.builder()
                    .userId(user.getId())
                    .nickname(user.getNickname())
                    .avatar(user.getAvatar())
                    .isMutual(isMutual)
                    .build());
        }
        return voList;
    }
}
