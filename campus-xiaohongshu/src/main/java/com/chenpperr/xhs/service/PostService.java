package com.chenpperr.xhs.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chenpperr.xhs.common.PageResult;
import com.chenpperr.xhs.dto.PostPublishDTO;
import com.chenpperr.xhs.entity.Post;
import com.chenpperr.xhs.vo.PostCardVO;
import com.chenpperr.xhs.vo.PostDetailVO;

/**
 * 笔记 业务接口
 */
public interface PostService extends IService<Post> {

    /**
     * 分页查询笔记列表（返回卡片 VO）
     *
     * @param page     分页对象
     * @param category 分类过滤条件
     * @return 分页结果（已转换为 PostCardVO）
     */
    PageResult<PostCardVO> getPostPage(Page<Post> page, String category);

    /**
     * 根据 ID 获取笔记详情
     *
     * @param id 笔记ID
     * @return 笔记详情 VO，不存在返回 null
     */
    PostDetailVO getPostById(Long id);

    /**
     * 按关键词搜索笔记（模糊匹配 title 和 content）
     *
     * @param keyword 搜索关键词
     * @param page    分页对象
     * @return 搜索结果（已转换为 PostCardVO）
     */
    PageResult<PostCardVO> searchPosts(String keyword, Page<Post> page);

    /**
     * 查询某用户发布的笔记列表
     *
     * @param userId 用户ID
     * @param page   分页对象
     * @return 该用户发布的笔记列表（已转换为 PostCardVO）
     */
    PageResult<PostCardVO> getUserPostPage(Long userId, Page<Post> page);


    /**
     * 发布笔记
     *
     * @param dto       前端提交的笔记参数
     * @param userId   用户ID
     * @return 发布成功后的笔记ID
     */
    Long publishPost(PostPublishDTO dto, Long userId);

    /**
     * 删除笔记（只有作者自己可以删除）
     *
     * @param postId 笔记ID
     * @param userId 当前登录用户ID
     */
    void deletePost(Long postId, Long userId);

}