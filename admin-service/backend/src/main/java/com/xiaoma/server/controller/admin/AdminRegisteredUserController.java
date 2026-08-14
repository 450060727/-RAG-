package com.xiaoma.server.controller.admin;

/**
 * 后台注册用户控制器，处理 /api/admin/registered-users 相关请求。
 *
 * @author xiaoma
 * @since 2026-07-23
 */

import com.xiaoma.server.annotation.RequirePermission;
import com.xiaoma.server.common.Result;
import com.xiaoma.server.dto.admin.AdminPageResponse;
import com.xiaoma.server.dto.admin.AdminRegisteredUserPageRequest;
import com.xiaoma.server.dto.admin.AdminRegisteredUserResponse;
import com.xiaoma.server.service.admin.AdminRegisteredUserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/registered-users")
public class AdminRegisteredUserController {

    private final AdminRegisteredUserService adminRegisteredUserService;

    /**
     * 构造后台注册用户控制器。
     *
     * @param adminRegisteredUserService 后台注册用户服务
     */
    public AdminRegisteredUserController(AdminRegisteredUserService adminRegisteredUserService) {
        this.adminRegisteredUserService = adminRegisteredUserService;
    }

    /**
     * 接口路径：GET /
     * 用途：分页查询前台注册用户列表。
     * 权限要求：registered:user:list
     *
     * @param req 分页查询条件
     * @return 用户分页结果
     */
    @RequirePermission("registered:user:list")
    @GetMapping
    public Result<AdminPageResponse<AdminRegisteredUserResponse>> page(AdminRegisteredUserPageRequest req) {
        return Result.ok(adminRegisteredUserService.page(req));
    }

    /**
     * 接口路径：PUT /{id}/status
     * 用途：修改前台注册用户状态。
     * 权限要求：registered:user:changeStatus
     *
     * @param id   用户 ID
     * @param body 包含 status 的请求体
     * @return 空结果
     */
    @RequirePermission("registered:user:changeStatus")
    @PutMapping("/{id}/status")
    public Result<Void> changeStatus(@PathVariable Long id,
                                       @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        adminRegisteredUserService.changeStatus(id, status);
        return Result.ok();
    }
}
