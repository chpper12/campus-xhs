package com.chenpperr.xhs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chenpperr.xhs.dto.UpdateUserDTO;
import com.chenpperr.xhs.entity.User;
import com.chenpperr.xhs.mapper.UserMapper;
import com.chenpperr.xhs.security.JwtUtil;
import com.chenpperr.xhs.security.LoginVO;
import com.chenpperr.xhs.security.RegisterDTO;
import com.chenpperr.xhs.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
                .set(dto.getAvatar() != null, User::getAvatar, dto.getAvatar());

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
}
