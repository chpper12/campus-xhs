package com.chenpperr.xhs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chenpperr.xhs.common.ResultCode;
import com.chenpperr.xhs.domain.dto.UpdateUserDTO;
import com.chenpperr.xhs.domain.entity.Post;
import com.chenpperr.xhs.domain.entity.User;
import com.chenpperr.xhs.domain.vo.UserProfileVO;
import com.chenpperr.xhs.exception.BusinessException;
import com.chenpperr.xhs.mapper.UserMapper;
import com.chenpperr.xhs.security.JwtUtil;
import com.chenpperr.xhs.security.LoginVO;
import com.chenpperr.xhs.security.RegisterDTO;
import com.chenpperr.xhs.service.FollowService;
import com.chenpperr.xhs.service.PostService;
import com.chenpperr.xhs.service.UserService;
import com.chenpperr.xhs.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;


/**
 * 用户 Service 实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    /**
     * FollowService/PostService 都注入了 UserService，直接构造器注入会形成循环依赖
     * （Spring Boot 默认禁止循环引用），用 @Lazy 延迟注入打破循环（同 FeedServiceImpl 先例）
     */
    @Lazy
    @Autowired
    private FollowService followService;

    @Lazy
    @Autowired
    private PostService postService;


    @Override
    public User findByUsername(String username) {
        return getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
    }

    @Override
    public User validateUser(String username, String password) {
        User user = findByUsername(username);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            return user;
        }
        return null;
    }

    @Override
    public void updateProfile(Long userId, UpdateUserDTO dto) {
        // 只更新非 null 字段（前端传了什么就改什么，没传的不动）
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<User>()
                .eq(User::getId, userId)
                .set(dto.getNickname() != null, User::getNickname, dto.getNickname())
                .set(dto.getBio() != null, User::getBio, dto.getBio())
                .set(dto.getAvatar() != null, User::getAvatar, dto.getAvatar())
                .set(dto.getPhone() != null, User::getPhone, dto.getPhone())
                .set(dto.getEmail() != null, User::getEmail, dto.getEmail());

        update(updateWrapper);
    }

    @Override
    public LoginVO register(RegisterDTO dto) {
        // 1. 检查用户名是否已存在
        User existingUser = findByUsername(dto.getUsername());
        if (existingUser != null) {
            throw new RuntimeException("用户名已被注册");
        }

        // 2. 构建新用户（昵称默认等于用户名）
        User user = User.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .nickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername())
                .avatar("")
                .bio("")
                .status(1)
                .build();

        // 3. 插入数据库
        save(user);

        // 4. 签发 Token，实现自动登录
        String token = jwtUtil.generateToken(user.getId());

        // 5. 组装返回结果
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setBio(user.getBio());

        return vo;
    }

    @Override
    public UserProfileVO getUserProfile(Long userId) {
        // 1. 查询用户是否存在
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }

        // 2. 查询统计数据
        Long postCount = postService.count(
                new LambdaQueryWrapper<Post>().eq(Post::getUserId, userId));
        Long followingCount = followService.getFollowingCount(userId);
        Long followerCount = followService.getFollowerCount(userId);

        // 3. 判断当前登录用户是否已关注此人（未登录或查看自己时为 null）
        Boolean isFollowed = null;
        Long currentUserId = SecurityUtil.getCurrentUserIdOrNull();
        boolean isSelf = currentUserId != null && currentUserId.equals(userId);
        if (currentUserId != null && !isSelf) {
            isFollowed = followService.isFollowing(currentUserId, userId);
        }

        // 4. 组装 VO（余额仅自己可见：查看他人主页或未登录时为 null）
        return UserProfileVO.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .bio(user.getBio())
                .phone(user.getPhone())
                .email(user.getEmail())
                .postCount(postCount)
                .followingCount(followingCount)
                .followerCount(followerCount)
                .isFollowed(isFollowed)
                .balance(isSelf ? user.getBalance() : null)
                .build();
    }
}
