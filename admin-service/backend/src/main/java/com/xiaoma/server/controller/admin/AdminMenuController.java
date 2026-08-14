package com.xiaoma.server.controller.admin;

/**
 * 后台菜单控制器，处理 /api/admin/menus 相关请求。
 *
 * @author xiaoma
 * @since 2026-07-23
 */

import com.xiaoma.server.annotation.RequirePermission;
import com.xiaoma.server.common.Result;
import com.xiaoma.server.dto.admin.AdminMenuRequest;
import com.xiaoma.server.dto.admin.AdminMenuTreeResponse;
import com.xiaoma.server.entity.admin.SysMenu;
import com.xiaoma.server.service.admin.AdminMenuService;
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
@RequestMapping("/api/admin/menus")
public class AdminMenuController {

    private final AdminMenuService adminMenuService;

    /**
     * 构造后台菜单控制器。
     *
     * @param adminMenuService 后台菜单服务
     */
    public AdminMenuController(AdminMenuService adminMenuService) {
        this.adminMenuService = adminMenuService;
    }

    /**
     * 接口路径：GET /
     * 用途：查询菜单树形列表。
     * 权限要求：system:menu:list
     *
     * @return 菜单树形列表
     */
    @RequirePermission("system:menu:list")
    @GetMapping
    public Result<List<AdminMenuTreeResponse>> tree() {
        return Result.ok(adminMenuService.tree());
    }

    /**
     * 接口路径：POST /
     * 用途：新增菜单。
     * 权限要求：system:menu:create
     *
     * @param req 菜单请求
     * @return 空结果
     */
    @RequirePermission("system:menu:create")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody AdminMenuRequest req) {
        adminMenuService.create(req);
        return Result.ok();
    }

    /**
     * 接口路径：GET /{id}
     * 用途：查询菜单详情。
     * 权限要求：system:menu:detail
     *
     * @param id 菜单 ID
     * @return 菜单详情
     */
    @RequirePermission("system:menu:detail")
    @GetMapping("/{id}")
    public Result<SysMenu> detail(@PathVariable Long id) {
        return Result.ok(adminMenuService.detail(id));
    }

    /**
     * 接口路径：PUT /{id}
     * 用途：更新菜单。
     * 权限要求：system:menu:update
     *
     * @param id  菜单 ID
     * @param req 菜单请求
     * @return 空结果
     */
    @RequirePermission("system:menu:update")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                                @Valid @RequestBody AdminMenuRequest req) {
        adminMenuService.update(id, req);
        return Result.ok();
    }

    /**
     * 接口路径：DELETE /{id}
     * 用途：删除菜单。
     * 权限要求：system:menu:delete
     *
     * @param id 菜单 ID
     * @return 空结果
     */
    @RequirePermission("system:menu:delete")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminMenuService.delete(id);
        return Result.ok();
    }
}
