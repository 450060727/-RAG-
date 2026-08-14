package com.xiaoma.server.controller;

/**
 * 前台认证控制器，处理 /api/auth 相关公开请求。
 *
 * @author xiaoma
 * @since 2026-07-23
 */

import com.xiaoma.server.common.Result;
import com.xiaoma.server.dto.LoginRequest;
import com.xiaoma.server.dto.LoginResponse;
import com.xiaoma.server.dto.RegisterRequest;
import com.xiaoma.server.dto.ResetPasswordRequest;
import com.xiaoma.server.dto.SendCodeRequest;
import com.xiaoma.server.dto.SendResetCodeRequest;
import com.xiaoma.server.service.AuthService;
import com.xiaoma.server.util.TokenExtractor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final TokenExtractor tokenExtractor;

    /**
     * 构造前台认证控制器。
     *
     * @param authService    前台认证服务
     * @param tokenExtractor Token 提取器
     */
    public AuthController(AuthService authService, TokenExtractor tokenExtractor) {
        this.authService = authService;
        this.tokenExtractor = tokenExtractor;
    }

    /**
     * 接口路径：POST /send-code
     * 用途：发送注册验证码。
     * 权限要求：公开接口，无需登录。
     *
     * @param req 发送验证码请求
     * @return 空结果
     */
    @PostMapping("/send-code")
    public Result<Void> sendCode(@Valid @RequestBody SendCodeRequest req) {
        authService.sendCode(req.email());
        return Result.ok();
    }

    /**
     * 接口路径：POST /send-reset-code
     * 用途：发送重置密码验证码。
     * 权限要求：公开接口，无需登录。
     *
     * @param req 发送重置验证码请求
     * @return 空结果
     */
    @PostMapping("/send-reset-code")
    public Result<Void> sendResetCode(@Valid @RequestBody SendResetCodeRequest req) {
        authService.sendResetCode(req.email());
        return Result.ok();
    }

    /**
     * 接口路径：POST /register
     * 用途：用户注册。
     * 权限要求：公开接口，无需登录。
     *
     * @param req 注册请求
     * @return 空结果
     */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest req) {
        authService.register(req.email(), req.code(), req.password(), req.name());
        return Result.ok();
    }

    /**
     * 接口路径：POST /reset-password
     * 用途：重置密码。
     * 权限要求：公开接口，无需登录。
     *
     * @param req 重置密码请求
     * @return 空结果
     */
    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        authService.resetPassword(req.email(), req.code(), req.password());
        return Result.ok();
    }

    /**
     * 接口路径：POST /login
     * 用途：用户登录并返回 JWT token。
     * 权限要求：公开接口，无需登录。
     *
     * @param req 登录请求
     * @return 登录响应，包含 JWT token
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return Result.ok(new LoginResponse(authService.login(req.email(), req.password())));
    }

    /**
     * 接口路径：POST /logout
     * 用途：退出登录，清除当前会话。
     * 权限要求：公开接口，无需登录。
     *
     * @param request HTTP 请求，用于提取当前 token
     * @return 空结果
     */
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        Optional<String> token = tokenExtractor.extract(request);
        token.ifPresent(authService::logout);
        return Result.ok();
    }
}
