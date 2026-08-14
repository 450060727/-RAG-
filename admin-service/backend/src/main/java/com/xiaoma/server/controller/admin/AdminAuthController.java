package com.xiaoma.server.controller.admin;

/**
 * 后台认证控制器，处理 /api/admin/auth 相关请求。
 *
 * @author xiaoma
 * @since 2026-07-23
 */

import com.xiaoma.server.common.Result;
import com.xiaoma.server.config.AdminAuthInterceptor;
import com.xiaoma.server.dto.LoginResponse;
import com.xiaoma.server.dto.admin.AdminChangePasswordRequest;
import com.xiaoma.server.dto.admin.AdminLoginRequest;
import com.xiaoma.server.dto.admin.AdminMeResponse;
import com.xiaoma.server.dto.admin.AdminMenuTreeResponse;
import com.xiaoma.server.entity.admin.SysUser;
import com.xiaoma.server.mapper.admin.SysDeptMapper;
import com.xiaoma.server.mapper.admin.SysUserMapper;
import com.xiaoma.server.service.admin.AdminAuthService;
import com.xiaoma.server.service.admin.AdminPermissionService;
import com.xiaoma.server.util.TokenExtractor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;
    private final AdminPermissionService adminPermissionService;
    private final SysUserMapper sysUserMapper;
    private final SysDeptMapper sysDeptMapper;
    private final TokenExtractor tokenExtractor;

    /**
     * 构造后台认证控制器。
     *
     * @param adminAuthService       后台认证服务
     * @param adminPermissionService 后台权限服务
     * @param sysUserMapper          用户 Mapper
     * @param sysDeptMapper          部门 Mapper
     * @param tokenExtractor         Token 提取器
     */
    public AdminAuthController(AdminAuthService adminAuthService,
                               AdminPermissionService adminPermissionService,
                               SysUserMapper sysUserMapper,
                               SysDeptMapper sysDeptMapper,
                               TokenExtractor tokenExtractor) {
        this.adminAuthService = adminAuthService;
        this.adminPermissionService = adminPermissionService;
        this.sysUserMapper = sysUserMapper;
        this.sysDeptMapper = sysDeptMapper;
        this.tokenExtractor = tokenExtractor;
    }

    /**
     * 接口路径：POST /login
     * 用途：管理员登录并返回 JWT token。
     * 权限要求：公开接口，无需额外权限。
     *
     * @param req 登录请求
     * @return 登录响应，包含 JWT token
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody AdminLoginRequest req) {
        return Result.ok(new LoginResponse(adminAuthService.login(req.username(), req.password())));
    }

    /**
     * 接口路径：POST /logout
     * 用途：退出登录，清除当前会话。
     * 权限要求：公开接口，无需额外权限。
     *
     * @param request HTTP 请求，用于提取当前 token
     * @return 空结果
     */
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        Optional<String> token = tokenExtractor.extract(request);
        token.ifPresent(adminAuthService::logout);
        return Result.ok();
    }

    /**
     * 接口路径：PUT /password
     * 用途：修改当前管理员密码。
     * 权限要求：已登录。
     *
     * @param adminId 当前管理员 ID
     * @param req     修改密码请求
     * @return 空结果
     */
    @PutMapping("/password")
    public Result<Void> changePassword(@RequestAttribute("adminId") Long adminId,
                                         @Valid @RequestBody AdminChangePasswordRequest req) {
        adminAuthService.changePassword(adminId, req.oldPassword(), req.newPassword());
        return Result.ok();
    }

    /**
     * 接口路径：GET /me
     * 用途：获取当前登录管理员详情。
     * 权限要求：已登录。
     *
     * @param adminId 当前管理员 ID
     * @return 管理员详情
     */
    @GetMapping("/me")
    public Result<AdminMeResponse> me(@RequestAttribute("adminId") Long adminId) {
        SysUser u = sysUserMapper.selectById(adminId);
        if (u == null) {
            return Result.error("用户不存在");
        }
        String deptName = u.getDeptId() == null ? null
                : java.util.Optional.ofNullable(sysDeptMapper.selectById(u.getDeptId()))
                        .map(com.xiaoma.server.entity.admin.SysDept::getName)
                        .orElse(null);
        return Result.ok(new AdminMeResponse(
                u.getId(), u.getUsername(), u.getRealName(), u.getPhone(), u.getEmail(), u.getAvatar(),
                u.getDeptId(), deptName, u.getSuperAdmin(),
                adminPermissionService.getRoles(adminId),
                adminPermissionService.getMenuTree(adminId),
                adminPermissionService.getPermissions(adminId)
        ));
    }

    /**
     * 接口路径：GET /menus
     * 用途：获取当前管理员可见菜单树。
     * 权限要求：已登录。
     *
     * @param adminId 当前管理员 ID
     * @return 菜单树列表
     */
    @GetMapping("/menus")
    public Result<List<AdminMenuTreeResponse>> menus(@RequestAttribute("adminId") Long adminId) {
        return Result.ok(adminPermissionService.getMenuTree(adminId));
    }
}
