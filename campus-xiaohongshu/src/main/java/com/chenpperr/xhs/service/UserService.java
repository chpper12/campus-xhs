package com.chenpperr.xhs.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chenpperr.xhs.dto.UpdateUserDTO;
import com.chenpperr.xhs.entity.User;
import com.chenpperr.xhs.security.LoginVO;
import com.chenpperr.xhs.security.RegisterDTO;

/**
 * 用户 Service 接口
 */
public interface UserService extends IService<User> {

    /**
     * 根据用户名查找用户
     *
     * @param username 用户名
     * @return 用户实体，不存在返回 null
     */
    User findByUsername(String username);

    /**
     * 验证用户名密码
     *
     * @param username 用户名
     * @param password 密码
     * @return 验证成功返回用户，失败返回 null
     */
    User validateUser(String username, String password);

    /**
     * 更新当前登录用户的个人资料
     *
     * @param userId 当前用户ID
     * @param dto    更新参数（昵称、简介、头像）
     */
    void updateProfile(Long userId, UpdateUserDTO dto);

    /**
     * 用户注册
     *
     * @param dto 注册参数（用户名、密码、昵称）
     * @return 注册成功返回 LoginVO（包含 Token，实现自动登录）
     * @throws RuntimeException 用户名已被注册时抛出
     */
    LoginVO register(RegisterDTO dto);
}
