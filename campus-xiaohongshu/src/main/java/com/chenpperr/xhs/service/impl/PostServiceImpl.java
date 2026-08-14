package com.chenpperr.xhs.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chenpperr.xhs.common.PageResult;
import com.chenpperr.xhs.common.ResultCode;
import com.chenpperr.xhs.dto.PostPublishDTO;
import com.chenpperr.xhs.entity.Comment;
import com.chenpperr.xhs.entity.Post;
import com.chenpperr.xhs.entity.User;
import com.chenpperr.xhs.exception.BusinessException;
import com.chenpperr.xhs.mapper.CommentMapper;
import com.chenpperr.xhs.mapper.PostMapper;
import com.chenpperr.xhs.service.FeedService;
import com.chenpperr.xhs.service.LikeService;
import com.chenpperr.xhs.service.PostService;
import com.chenpperr.xhs.service.UserService;
import com.chenpperr.xhs.util.SecurityUtil;
import com.chenpperr.xhs.vo.PostCardVO;
import com.chenpperr.xhs.vo.PostDetailVO;
import com.chenpperr.xhs.vo.UserSimpleVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 笔记 业务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    private final LikeService likeService;
    private final UserService userService;
    private final CommentMapper commentMapper;
    private final FeedService feedService;

    /**
     * 分页查询笔记列表（返回卡片 VO）
     *
     * 流程：
     *   1. LambdaQueryWrapper 构建查询条件（可选按分类过滤）
     *   2. 按最新发布时间倒序排列
     *   3. this.page(page, wrapper) 执行分页查询
     *   4. 遍历结果，每条 Post 转 PostCardVO（脱敏 + 聚合点赞状态、作者信息）
     *   5. 封装成 PageResult 返回
     *
     * @param page     分页对象
     * @param category 分类过滤条件（可选）
     * @return 分页结果（已转换为 PostCardVO）
     */
    @Override
    public PageResult<PostCardVO> getPostPage(Page<Post> page, String category) {
        // 第 1 步：构建查询条件
        LambdaQueryWrapper<Post> queryWrapper = new LambdaQueryWrapper<>();

        // 如果传了分类，则按分类精确匹配；没传则查询全部分类
        queryWrapper.eq(StringUtils.isNotBlank(category), Post::getCategory, category);

        // 小红书首页/分类页通常按最新发布时间倒序排列
        queryWrapper.orderByDesc(Post::getCreateTime);

        // 第 2 步：执行分页查询
        Page<Post> postPage = this.page(page, queryWrapper);

        // 第 3 步：Entity → VO 转换
        List<PostCardVO> voList = postPage.getRecords().stream()
                .map(this::convertToCardVO)
                .toList();

        return PageResult.of(postPage.getTotal(), voList);
    }

    /**
     * 根据 ID 获取笔记详情
     *
     * 流程：
     *   1. this.getById(id) 查询笔记
     *   2. 转换为 PostDetailVO（包含图片列表、评论数、点赞状态等）
     *
     * @param id 笔记ID
     * @return 笔记详情 VO，不存在返回 null
     */
    @Override
    public PostDetailVO getPostById(Long id) {
        Post post = this.getById(id);
        if (post == null) {
            return null;
        }
        return convertToDetailVO(post);
    }


    /**
     * 查询某用户发布的笔记列表
     *
     * @param userId 用户ID
     * @param page   分页对象
     * @return 该用户发布的笔记列表（已转换为 PostCardVO）
     */
    @Override
    public PageResult<PostCardVO> getUserPostPage(Long userId, Page<Post> page) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getUserId, userId)
                .orderByDesc(Post::getCreateTime);

        Page<Post> postPage = this.page(page, wrapper);

        List<PostCardVO> voList = postPage.getRecords().stream()
                .map(this::convertToCardVO)
                .toList();

        return PageResult.of(postPage.getTotal(), voList);
    }

    /**
     * 按关键词搜索笔记（模糊匹配 title 和 content）
     *
     * SQL 逻辑：
     *   SELECT * FROM post
     *   WHERE status = 1
     *   AND (title LIKE '%keyword%' OR content LIKE '%keyword%')
     *   ORDER BY create_time DESC
     *
     * @param keyword 搜索关键词
     * @param page    分页对象
     * @return 搜索结果（已转换为 PostCardVO）
     */
    @Override
    public PageResult<PostCardVO> searchPosts(String keyword, Page<Post> page) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getStatus, 1)
                .and(StringUtils.isNotBlank(keyword), w ->
                        w.like(Post::getTitle, keyword)
                                .or()
                                .like(Post::getContent, keyword)
                )
                .orderByDesc(Post::getCreateTime);

        Page<Post> postPage = this.page(page, wrapper);

        List<PostCardVO> voList = postPage.getRecords().stream()
                .map(this::convertToCardVO)
                .toList();

        return PageResult.of(postPage.getTotal(), voList);
    }


    /**
     * 删除笔记（只有作者自己可以删除）
     *
     * 流程：
     *   1. 查出帖子，校验是否存在
     *   2. 校验当前用户是否是作者（不是作者不能删）
     *   3. 删除帖子
     */
    @Override
    public void deletePost(Long postId, Long userId) {
        // 第 1 步：查出帖子
        Post post = this.getById(postId);
        if (post == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "笔记不存在或已被删除");
        }

        // 第 2 步：权限校验 — 只有作者能删自己的帖子
        if (!post.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权删除他人的笔记");
        }

        // 第 3 步：删除帖子
        this.removeById(postId);

        // 第 4 步：从所有用户的 Feed 中移除该笔记（异步执行，不阻塞删帖）
        try {
            feedService.removeFromAllFeeds(postId);
        } catch (Exception e) {
            log.warn("从Feed移除笔记失败，不影响删帖结果：{}", e.getMessage());
        }

        log.info("用户 {} 删除了笔记 {}", userId, postId);
    }

    /**
     * 发布笔记
     *
     * 流程：
     *   1. 构造 Post Entity（将图片URL列表转为JSON字符串存储）
     *   2. this.save(post) 保存到 MySQL（自动回填ID）
     *   3. 返回新笔记的ID
     *
     * @param dto    前端提交的笔记参数（title、category、content、imageUrls）
     * @param userId 当前登录用户ID
     * @return 发布成功后的笔记ID
     */
    @Override
    public Long publishPost(PostPublishDTO dto, Long userId) {
        // 第 1 步：构造 Post Entity
        Post post = Post.builder()
                .userId(userId)
                .title(dto.getTitle())
                .category(dto.getCategory())
                .content(dto.getContent())
                .imageUrls(JSONUtil.toJsonStr(dto.getImageUrls()))
                .likeCount(0)
                .commentCount(0)
                .status(1)
                .build();

        // 第 2 步：保存到 MySQL
        this.save(post);

        // 第 3 步：推送到粉丝的 Feed 流（异步执行，不阻塞发布流程）
        try {
            feedService.pushToFollowers(userId, post.getId());
        } catch (Exception e) {
            // Feed 推送失败不影响发布结果
            log.warn("Feed推送失败，不影响笔记发布：{}", e.getMessage());
        }

        // 第 4 步：返回笔记 ID
        return post.getId();
    }

    // ============================== Entity → VO 转换 ==============================

    /**
     * 将 Post 实体转换为列表卡片 VO
     *
     * 转换逻辑：
     *   1. 脱敏：只取 id / title / category / likeCount / createTime
     *   2. coverUrl：从 imageUrls JSON 数组取第一张图作为封面
     *   3. liked：查 Redis Hash 判断当前用户是否点赞
     *   4. author：查 User 表拿 nickname + avatar
     *
     * @param post 笔记实体
     * @return 列表卡片 VO
     */
    public PostCardVO convertToCardVO(Post post) {
        // 1. 取当前登录用户 ID（未登录返回 null）
        Long currentUserId = SecurityUtil.getCurrentUserIdOrNull();

        // 2. 判断当前用户是否点赞（userId 为 null 时直接返回 false）
        boolean liked = currentUserId != null
                && likeService.isLike(currentUserId, post.getId());

        // 3. 从 Redis 获取实时点赞数（不使用数据库冗余字段，保证数据一致）
        Long likeCount = likeService.getLikeCount(post.getId());

        // 4. 查询作者简要信息
        UserSimpleVO authorVO = buildAuthorVO(post.getUserId());

        // 5. 从 imageUrls JSON 数组提取封面图（第一张）
        String coverUrl = extractFirstImageUrl(post.getImageUrls());

        return PostCardVO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .category(post.getCategory())
                .coverUrl(coverUrl)
                .likeCount(likeCount.intValue())
                .liked(liked)
                .author(authorVO)
                .createTime(post.getCreateTime())
                .build();
    }

    /**
     * 将 Post 实体转换为笔记详情 VO
     *
     * 转换逻辑：
     *   1. 脱敏：去掉 status 字段
     *   2. JSON → List：imageUrls 从 JSON 字符串解析为 List<String>
     *   3. liked：查 Redis
     *   4. author：查 User 表
     *   5. commentCount：查 Comment 表获取实时评论数
     *
     * @param post 笔记实体
     * @return 笔记详情 VO
     */
    public PostDetailVO convertToDetailVO(Post post) {
        Long currentUserId = SecurityUtil.getCurrentUserIdOrNull();

        boolean liked = currentUserId != null
                && likeService.isLike(currentUserId, post.getId());

        // 从 Redis 获取实时点赞数（不使用数据库冗余字段，保证数据一致）
        Long likeCount = likeService.getLikeCount(post.getId());

        UserSimpleVO authorVO = buildAuthorVO(post.getUserId());

        // 查询实时评论数（不依赖冗余字段，保证准确）
        Long commentCount = commentMapper.selectCount(
                new LambdaQueryWrapper<Comment>().eq(Comment::getPostId, post.getId())
        );

        return PostDetailVO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .category(post.getCategory())
                .content(post.getContent())
                .imageUrls(parseJsonToList(post.getImageUrls()))
                .likeCount(likeCount.intValue())
                .liked(liked)
                .commentCount(commentCount.intValue())
                .tags(parseJsonToList(post.getTags()))
                .author(authorVO)
                .createTime(post.getCreateTime())
                .build();
    }

    // ============================== 私有辅助方法 ==============================

    /**
     * 构建作者简要信息 VO
     *
     * @param userId 作者用户ID
     * @return 作者简要信息，用户不存在时返回空壳对象（避免 NPE）
     */
    private UserSimpleVO buildAuthorVO(Long userId) {
        User author = userService.getById(userId);
        if (author == null) {
            // 防御性编程：用户被删除时返回空壳，前端展示「已注销用户」
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

    /**
     * 从 imageUrls JSON 数组中提取第一张图片 URL 作为封面
     *
     * @param imageUrlsJson JSON 数组字符串，如 ["https://xxx/1.jpg","https://xxx/2.jpg"]
     * @return 第一张图片 URL，无图片时返回 null
     */
    private String extractFirstImageUrl(String imageUrlsJson) {
        List<String> urls = parseJsonToList(imageUrlsJson);
        return urls.isEmpty() ? null : urls.get(0);
    }

    /**
     * JSON 数组字符串 → List<String>，解析失败返回空列表
     *
     * 使用 Hutool 的 JSONUtil.toList，比手动 split 更安全（能处理转义字符等边界情况）
     *
     * @param json JSON 数组字符串
     * @return 字符串列表
     */
    private List<String> parseJsonToList(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            // Hutool 工具：JSON 数组字符串直接转 List<String>
            return JSONUtil.toList(json, String.class);
        } catch (Exception e) {
            log.warn("JSON 解析失败: {}", json, e);
            return Collections.emptyList();
        }
    }
}