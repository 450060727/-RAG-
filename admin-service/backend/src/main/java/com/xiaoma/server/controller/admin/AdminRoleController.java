package com.xiaoma.server.controller.admin;

/**
 * 后台角色控制器，处理 /api/admin/roles 相关请求。
 *
 * @author xiaoma
 * @since 2026-07-23
 */

import com.xiaoma.server.annotation.RequirePermission;
import com.xiaoma.server.common.Result;
import com.xiaoma.server.dto.admin.AdminRoleMenuRequest;
import com.xiaoma.server.dto.admin.AdminRoleRequest;
import com.xiaoma.server.dto.admin.AdminRoleResponse;
import com.xiaoma.server.service.admin.AdminRoleService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/roles")
public class AdminRoleController {

    private final AdminRoleService adminRoleService;

    /**
     * 构造后台角色控制器。
     *
     * @param adminRoleService 后台角色服务
     */
    public AdminRoleController(AdminRoleService adminRoleService) {
        this.adminRoleService = adminRoleService;
    }

    /**
     * 接口路径：GET /
     * 用途：查询角色列表。
     * 权限要求：system:role:list
     *
     * @return 角色列表
     */
    @RequirePermission("system:role:list")
    @GetMapping
    public Result<List<AdminRoleResponse>> list() {
        return Result.ok(adminRoleService.list());
    }

    /**
     * 接口路径：POST /
     * 用途：新增角色。
     * 权限要求：system:role:create
     *
     * @param req 角色请求
     * @return 空结果
     */
    @RequirePermission("system:role:create")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody AdminRoleRequest req) {
        adminRoleService.create(req);
        return Result.ok();
    }

    /**
     * 接口路径：GET /{id}
     * 用途：查询角色详情。
     * 权限要求：system:role:detail
     *
     * @param id 角色 ID
     * @return 角色详情
     */
    @RequirePermission("system:role:detail")
    @GetMapping("/{id}")
    public Result<AdminRoleResponse> detail(@PathVariable Long id) {
        return Result.ok(adminRoleService.detail(id));
    }

    /**
     * 接口路径：PUT /{id}
     * 用途：更新角色。
     * 权限要求：system:role:update
     *
     * @param id  角色 ID
     * @param req 角色请求
     * @return 空结果
     */
    @RequirePermission("system:role:update")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                                @Valid @RequestBody AdminRoleRequest req) {
        adminRoleService.update(id, req);
        return Result.ok();
    }

    /**
     * 接口路径：DELETE /{id}
     * 用途：删除角色。
     * 权限要求：system:role:delete
     *
     * @param id 角色 ID
     * @return 空结果
     */
    @RequirePermission("system:role:delete")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminRoleService.delete(id);
        return Result.ok();
    }

    /**
     * 接口路径：GET /{id}/menus
     * 用途：查询角色已分配菜单 ID 列表。
     * 权限要求：system:role:assignMenu
     *
     * @param id 角色 ID
     * @return 菜单 ID 列表
     */
    @RequirePermission("system:role:assignMenu")
    @GetMapping("/{id}/menus")
    public Result<List<Long>> menus(@PathVariable Long id) {
        return Result.ok(adminRoleService.getMenuIds(id));
    }

    /**
     * 接口路径：PUT /{id}/menus
     * 用途：为角色分配菜单权限。
     * 权限要求：system:role:assignMenu
     *
     * @param id  角色 ID
     * @param req 角色菜单分配请求
     * @return 空结果
     */
    @RequirePermission("system:role:assignMenu")
    @PutMapping("/{id}/menus")
    public Result<Void> assignMenus(@PathVariable Long id,
                                      @Valid @RequestBody AdminRoleMenuRequest req) {
        adminRoleService.assignMenus(id, req);
        return Result.ok();
    }
}
