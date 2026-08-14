package com.xiaoma.server.controller;

/**
 * 前台用户控制器，处理 /api/user 相关请求。
 * 提供个人资料查询/更新、修改密码接口。
 *
 * @author xiaoma
 * @since 2026-07-23
 */

import com.xiaoma.server.common.BizException;
import com.xiaoma.server.common.Result;
import com.xiaoma.server.dto.ChangePasswordRequest;
import com.xiaoma.server.dto.ProfileResponse;
import com.xiaoma.server.dto.UpdateProfileRequest;
import com.xiaoma.server.entity.User;
import com.xiaoma.server.mapper.UserMapper;
import com.xiaoma.server.service.SessionService;
import com.xiaoma.server.util.TokenExtractor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserMapper userMapper;
    private final SessionService sessionService;
    private final BCryptPasswordEncoder encoder;
    private final TokenExtractor tokenExtractor;

    /**
     * 构造前台用户控制器。
     *
     * @param userMapper     用户 Mapper
     * @param sessionService 会话服务
     * @param encoder        密码加密器
     * @param tokenExtractor Token 提取器
     */
    public UserController(UserMapper userMapper, SessionService sessionService,
                          BCryptPasswordEncoder encoder, TokenExtractor tokenExtractor) {
        this.userMapper = userMapper;
        this.sessionService = sessionService;
        this.encoder = encoder;
        this.tokenExtractor = tokenExtractor;
    }

    /**
     * 接口路径：GET /profile
     * 用途：获取当前登录用户资料。
     * 权限要求：已登录。
     *
     * @param uid 当前用户 ID
     * @return 用户资料
     */
    @GetMapping("/profile")
    public Result<ProfileResponse> profile(@RequestAttribute("uid") Long uid) {
        User u = userMapper.selectById(uid);
        if (u == null) {
            throw new BizException("用户不存在");
        }
        return Result.ok(new ProfileResponse(u.getId(), u.getEmail(), u.getName()));
    }

    /**
     * 接口路径：PUT /profile
     * 用途：更新当前用户资料。
     * 权限要求：已登录。
     *
     * @param uid 当前用户 ID
     * @param req 更新请求
     * @return 更新后的资料
     */
    @PutMapping("/profile")
    public Result<ProfileResponse> update(@RequestAttribute("uid") Long uid,
                                          @Valid @RequestBody UpdateProfileRequest req) {
        User u = userMapper.selectById(uid);
        if (u == null) {
            throw new BizException("用户不存在");
        }
        userMapper.updateNameById(uid, req.name());
        return Result.ok(new ProfileResponse(u.getId(), u.getEmail(), req.name()));
    }

    /**
     * 接口路径：PUT /password
     * 用途：修改当前用户密码，修改成功后使当前 token 失效。
     * 权限要求：已登录。
     *
     * @param uid     当前用户 ID
     * @param req     修改密码请求
     * @param request HTTP 请求，用于提取当前 token
     * @return 成功结果
     */
    @PutMapping("/password")
    public Result<Void> changePassword(@RequestAttribute("uid") Long uid,
                                        @Valid @RequestBody ChangePasswordRequest req,
                                        HttpServletRequest request) {
        User u = userMapper.selectById(uid);
        if (u == null) {
            throw new BizException("用户不存在");
        }
        if (!encoder.matches(req.oldPassword(), u.getPassword())) {
            throw new BizException("原密码错误");
        }
        userMapper.updatePasswordById(uid, encoder.encode(req.newPassword()));

        // 提取当前请求 token 并使其失效
        Optional<String> tokenOpt = tokenExtractor.extract(request);
        tokenOpt.ifPresent(sessionService::delete);
        return Result.ok();
    }
}
