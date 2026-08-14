package com.chenpperr.xhs.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chenpperr.xhs.common.PageResult;
import com.chenpperr.xhs.entity.PostLike;
import com.chenpperr.xhs.vo.PostCardVO;

/**
 * 点赞 Service 接口
 */
public interface LikeService extends IService<PostLike> {

    /**
     * 点赞/取消点赞（切换状态）
     *
     * @param userId 用户ID
     * @param postId 笔记ID
     * @return 切换后的点赞状态：true=已点赞，false=已取消
     */
    boolean toggleLike(Long userId, Long postId);

    /**
     * 查询当前用户是否已点赞某篇笔记
     *
     * @param userId 用户ID
     * @param postId 笔记ID
     * @return true=已点赞，false=未点赞
     */
    boolean isLike(Long userId, Long postId);

    /**
     * 查询笔记的点赞总数
     *
     * @param postId 笔记ID
     * @return 点赞数
     */
    Long getLikeCount(Long postId);

    /**
     * 分页查询某用户点赞过的笔记列表
     *
     * @param userId 用户ID
     * @param page   分页对象
     * @return 点赞过的笔记卡片列表
     */
    PageResult<PostCardVO> getUserLikedPosts(Long userId, Page<?> page);

}
