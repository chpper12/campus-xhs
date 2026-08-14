package com.chenpperr.xhs.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chenpperr.xhs.common.PageResult;
import com.chenpperr.xhs.entity.Follow;
import com.chenpperr.xhs.vo.FollowUserVO;

/**
 * 关注关系 Service 接口
 */
public interface FollowService extends IService<Follow> {

    /**
     * 关注某用户
     *
     * @param userId       当前用户ID
     * @param followUserId 要关注的用户ID
     */
    void follow(Long userId, Long followUserId);

    /**
     * 取消关注某用户
     *
     * @param userId       当前用户ID
     * @param followUserId 要取关的用户ID
     */
    void unfollow(Long userId, Long followUserId);

    /**
     * 查询当前用户是否已关注某用户
     *
     * @param userId       当前用户ID
     * @param followUserId 目标用户ID
     * @return true=已关注，false=未关注
     */
    boolean isFollowing(Long userId, Long followUserId);

    /**
     * 分页查询某用户的关注列表
     *
     * @param userId 目标用户ID
     * @param page   分页对象
     * @return 关注用户列表
     */
    PageResult<FollowUserVO> getFollowingList(Long userId, Page<?> page);

    /**
     * 分页查询某用户的粉丝列表
     *
     * @param userId 目标用户ID
     * @param page   分页对象
     * @return 粉丝用户列表
     */
    PageResult<FollowUserVO> getFollowerList(Long userId, Page<?> page);

    /**
     * 查询某用户的关注总数
     *
     * @param userId 用户ID
     * @return 关注数
     */
    Long getFollowingCount(Long userId);

    /**
     * 查询某用户的粉丝总数
     *
     * @param userId 用户ID
     * @return 粉丝数
     */
    Long getFollowerCount(Long userId);
}
