package com.xiaoma.server.controller.admin;

/**
 * 后台知识库分类控制器，处理 /api/admin/kb/categories 相关请求。
 *
 * @author xiaoma
 * @since 2026-07-23
 */

import com.xiaoma.server.annotation.RequirePermission;
import com.xiaoma.server.common.Result;
import com.xiaoma.server.dto.admin.AdminKbCategoryRequest;
import com.xiaoma.server.dto.admin.AdminKbCategoryResponse;
import com.xiaoma.server.dto.admin.AdminKbCategoryTreeResponse;
import com.xiaoma.server.service.kb.KbCategoryService;
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
@RequestMapping("/api/admin/kb/categories")
public class AdminKbCategoryController {

    private final KbCategoryService kbCategoryService;

    /**
     * 构造后台知识库分类控制器。
     *
     * @param kbCategoryService 知识库分类服务
     */
    public AdminKbCategoryController(KbCategoryService kbCategoryService) {
        this.kbCategoryService = kbCategoryService;
    }

    /**
     * 接口路径：GET /
     * 用途：查询知识库分类列表。
     * 权限要求：kb:category:list
     *
     * @return 分类列表
     */
    @RequirePermission("kb:category:list")
    @GetMapping
    public Result<List<AdminKbCategoryResponse>> list() {
        return Result.ok(kbCategoryService.list());
    }

    /**
     * 接口路径：GET /tree
     * 用途：查询知识库分类树形列表。
     * 权限要求：kb:category:list
     *
     * @return 分类树形列表
     */
    @RequirePermission("kb:category:list")
    @GetMapping("/tree")
    public Result<List<AdminKbCategoryTreeResponse>> tree() {
        return Result.ok(kbCategoryService.tree());
    }

    /**
     * 接口路径：POST /
     * 用途：新增知识库分类。
     * 权限要求：kb:category:create
     *
     * @param req 分类请求
     * @return 空结果
     */
    @RequirePermission("kb:category:create")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody AdminKbCategoryRequest req) {
        kbCategoryService.create(req);
        return Result.ok();
    }

    /**
     * 接口路径：PUT /{id}
     * 用途：更新知识库分类。
     * 权限要求：kb:category:update
     *
     * @param id  分类 ID
     * @param req 分类请求
     * @return 空结果
     */
    @RequirePermission("kb:category:update")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Integer id, @Valid @RequestBody AdminKbCategoryRequest req) {
        kbCategoryService.update(id, req);
        return Result.ok();
    }

    /**
     * 接口路径：DELETE /{id}
     * 用途：删除知识库分类。
     * 权限要求：kb:category:delete
     *
     * @param id 分类 ID
     * @return 空结果
     */
    @RequirePermission("kb:category:delete")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        kbCategoryService.delete(id);
        return Result.ok();
    }
}
