package com.xiaoma.server.controller.admin;

/**
 * 后台部门控制器，处理 /api/admin/depts 相关请求。
 *
 * @author xiaoma
 * @since 2026-07-23
 */

import com.xiaoma.server.annotation.RequirePermission;
import com.xiaoma.server.common.Result;
import com.xiaoma.server.dto.admin.AdminDeptRequest;
import com.xiaoma.server.dto.admin.AdminDeptTreeResponse;
import com.xiaoma.server.entity.admin.SysDept;
import com.xiaoma.server.service.admin.AdminDeptService;
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
@RequestMapping("/api/admin/depts")
public class AdminDeptController {

    private final AdminDeptService adminDeptService;

    /**
     * 构造后台部门控制器。
     *
     * @param adminDeptService 后台部门服务
     */
    public AdminDeptController(AdminDeptService adminDeptService) {
        this.adminDeptService = adminDeptService;
    }

    /**
     * 接口路径：GET /
     * 用途：查询部门树形列表。
     * 权限要求：system:dept:list
     *
     * @return 部门树形列表
     */
    @RequirePermission("system:dept:list")
    @GetMapping
    public Result<List<AdminDeptTreeResponse>> tree() {
        return Result.ok(adminDeptService.tree());
    }

    /**
     * 接口路径：POST /
     * 用途：新增部门。
     * 权限要求：system:dept:create
     *
     * @param req 部门请求
     * @return 空结果
     */
    @RequirePermission("system:dept:create")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody AdminDeptRequest req) {
        adminDeptService.create(req);
        return Result.ok();
    }

    /**
     * 接口路径：GET /{id}
     * 用途：查询部门详情。
     * 权限要求：system:dept:detail
     *
     * @param id 部门 ID
     * @return 部门详情
     */
    @RequirePermission("system:dept:detail")
    @GetMapping("/{id}")
    public Result<SysDept> detail(@PathVariable Long id) {
        return Result.ok(adminDeptService.detail(id));
    }

    /**
     * 接口路径：PUT /{id}
     * 用途：更新部门。
     * 权限要求：system:dept:update
     *
     * @param id  部门 ID
     * @param req 部门请求
     * @return 空结果
     */
    @RequirePermission("system:dept:update")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                                @Valid @RequestBody AdminDeptRequest req) {
        adminDeptService.update(id, req);
        return Result.ok();
    }

    /**
     * 接口路径：DELETE /{id}
     * 用途：删除部门。
     * 权限要求：system:dept:delete
     *
     * @param id 部门 ID
     * @return 空结果
     */
    @RequirePermission("system:dept:delete")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminDeptService.delete(id);
        return Result.ok();
    }
}
