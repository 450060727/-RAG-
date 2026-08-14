package com.xiaoma.server.controller.admin;

/**
 * 后台用户控制器，处理 /api/admin/users 相关请求。
 *
 * @author xiaoma
 * @since 2026-07-23
 */

import com.xiaoma.server.annotation.RequirePermission;
import com.xiaoma.server.common.Result;
import com.xiaoma.server.dto.admin.AdminPageResponse;
import com.xiaoma.server.dto.admin.AdminUserCreateRequest;
import com.xiaoma.server.dto.admin.AdminUserPageRequest;
import com.xiaoma.server.dto.admin.AdminUserResponse;
import com.xiaoma.server.dto.admin.AdminUserUpdateRequest;
import com.xiaoma.server.service.admin.AdminUserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    /**
     * 构造后台用户控制器。
     *
     * @param adminUserService 后台用户服务
     */
    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    /**
     * 接口路径：GET /
     * 用途：分页查询管理员用户列表。
     * 权限要求：system:user:list
     *
     * @param req     分页查询条件
     * @param adminId 当前登录管理员 ID
     * @return 用户分页结果
     */
    @RequirePermission("system:user:list")
    @GetMapping
    public Result<AdminPageResponse<AdminUserResponse>> page(AdminUserPageRequest req,
                                                          @RequestAttribute("adminId") Long adminId) {
        return Result.ok(adminUserService.page(req, adminId));
    }

    /**
     * 接口路径：POST /
     * 用途：创建管理员用户，返回初始密码。
     * 权限要求：system:user:create
     *
     * @param req 用户创建请求
     * @return 包含初始密码的响应
     */
    @RequirePermission("system:user:create")
    @PostMapping
    public Result<Map<String, String>> create(@Valid @RequestBody AdminUserCreateRequest req) {
        String initPwd = adminUserService.create(req);
        return Result.ok(Map.of("initialPassword", initPwd));
    }

    /**
     * 接口路径：GET /{id}
     * 用途：查询管理员用户详情。
     * 权限要求：system:user:detail
     *
     * @param id 用户 ID
     * @return 用户详情
     */
    @RequirePermission("system:user:detail")
    @GetMapping("/{id}")
    public Result<AdminUserResponse> detail(@PathVariable Long id) {
        return Result.ok(adminUserService.detail(id));
    }

    /**
     * 接口路径：PUT /{id}
     * 用途：更新管理员用户信息。
     * 权限要求：system:user:update
     *
     * @param id  用户 ID
     * @param req 用户更新请求
     * @return 空结果
     */
    @RequirePermission("system:user:update")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                                @Valid @RequestBody AdminUserUpdateRequest req) {
        adminUserService.update(id, req);
        return Result.ok();
    }

    /**
     * 接口路径：PUT /{id}/status
     * 用途：修改管理员用户状态。
     * 权限要求：system:user:changeStatus
     *
     * @param id   用户 ID
     * @param body 包含 status 的请求体
     * @return 空结果
     */
    @RequirePermission("system:user:changeStatus")
    @PutMapping("/{id}/status")
    public Result<Void> changeStatus(@PathVariable Long id,
                                       @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        if (status == null || (status != 0 && status != 1)) {
            return Result.error("状态值非法");
        }
        adminUserService.changeStatus(id, status);
        return Result.ok();
    }

    /**
     * 接口路径：PUT /{id}/password
     * 用途：重置管理员用户密码。
     * 权限要求：system:user:resetPwd
     *
     * @param id 用户 ID
     * @return 空结果
     */
    @RequirePermission("system:user:resetPwd")
    @PutMapping("/{id}/password")
    public Result<Void> resetPassword(@PathVariable Long id) {
        adminUserService.resetPassword(id);
        return Result.ok();
    }

    /**
     * 接口路径：DELETE /{id}
     * 用途：删除管理员用户。
     * 权限要求：system:user:delete
     *
     * @param id 用户 ID
     * @return 空结果
     */
    @RequirePermission("system:user:delete")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminUserService.delete(id);
        return Result.ok();
    }
}
