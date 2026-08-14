package com.chenpperr.xhs.controller;

import com.chenpperr.xhs.common.Result;
import com.chenpperr.xhs.common.ResultCode;
import com.chenpperr.xhs.entity.User;
import com.chenpperr.xhs.security.JwtUtil;
import com.chenpperr.xhs.security.LoginDTO;
import com.chenpperr.xhs.security.LoginVO;
import com.chenpperr.xhs.security.RegisterDTO;
import com.chenpperr.xhs.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器（登录/登出）
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * 登录接口
     *
     * 流程：
     * 1. 前端传来用户名+密码
     * 2. 验证用户名密码是否正确
     * 3. 如果正确，签发 JWT Token（发一张通行证）
     * 4. 返回 Token 给前端，前端后续请求带上这个 Token
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginDTO dto) {
        // 1. 验证用户名密码
        User user = userService.validateUser(dto.getUsername(), dto.getPassword());
        if (user == null) {
            return Result.error(ResultCode.UNAUTHORIZED, "用户名或密码错误");
        }

        // 2. 签发 Token
        String token = jwtUtil.generateToken(user.getId());

        // 3. 组装返回结果
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setBio(user.getBio());

        return Result.success(vo);
    }

    /**
     * 注册接口
     *
     * 流程：
     * 1. 前端传来用户名、密码、昵称（可选）
     * 2. 校验用户名唯一性
     * 3. 创建新用户
     * 4. 签发 JWT Token，实现自动登录
     * 5. 返回 Token 给前端
     */
    @PostMapping("/register")
    public Result<LoginVO> register(@Valid @RequestBody RegisterDTO dto) {
        try {
            LoginVO vo = userService.register(dto);
            return Result.success(vo);
        } catch (RuntimeException e) {
            return Result.error(ResultCode.BAD_REQUEST, e.getMessage());
        }
    }
}
