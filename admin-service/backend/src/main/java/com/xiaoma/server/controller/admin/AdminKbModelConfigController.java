package com.xiaoma.server.controller.admin;

/**
 * 后台知识库模型配置控制器，处理 /api/admin/kb/model-config 相关请求。
 *
 * @author xiaoma
 * @since 2026-07-23
 */

import com.xiaoma.server.annotation.RequirePermission;
import com.xiaoma.server.common.Result;
import com.xiaoma.server.dto.admin.AdminKbModelConfigRequest;
import com.xiaoma.server.dto.admin.AdminKbModelConfigResponse;
import com.xiaoma.server.entity.kb.KbModelConfig;
import com.xiaoma.server.service.kb.KbModelConfigConverter;
import com.xiaoma.server.service.kb.KbModelConfigService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/kb/model-config")
public class AdminKbModelConfigController {

    private final KbModelConfigService modelConfigService;

    /**
     * 构造后台知识库模型配置控制器。
     *
     * @param modelConfigService 模型配置服务
     */
    public AdminKbModelConfigController(KbModelConfigService modelConfigService) {
        this.modelConfigService = modelConfigService;
    }

    /**
     * 接口路径：GET /
     * 用途：查询默认模型配置。
     * 权限要求：kb:model-config:view
     *
     * @return 默认模型配置响应
     */
    @RequirePermission("kb:model-config:view")
    @GetMapping
    public Result<AdminKbModelConfigResponse> getDefault() {
        return Result.ok(KbModelConfigConverter.toResponse(modelConfigService.current()));
    }

    /**
     * 接口路径：PUT /
     * 用途：更新默认模型配置。
     * 权限要求：kb:model-config:update
     *
     * @param req 模型配置请求
     * @return 空结果
     */
    @RequirePermission("kb:model-config:update")
    @PutMapping
    public Result<Void> updateDefault(@Valid @RequestBody AdminKbModelConfigRequest req) {
        modelConfigService.saveDefault(KbModelConfigConverter.fromRequest(req));
        return Result.ok();
    }

    /**
     * 接口路径：GET /category/{categoryId}
     * 用途：查询指定分类的模型配置。
     * 权限要求：kb:model-config:view
     *
     * @param categoryId 分类 ID
     * @return 分类模型配置响应
     */
    @RequirePermission("kb:model-config:view")
    @GetMapping("/category/{categoryId}")
    public Result<AdminKbModelConfigResponse> getByCategory(@PathVariable Integer categoryId) {
        return Result.ok(KbModelConfigConverter.toResponse(modelConfigService.current(categoryId)));
    }

    /**
     * 接口路径：PUT /category/{categoryId}
     * 用途：更新指定分类的模型配置。
     * 权限要求：kb:model-config:update
     *
     * @param categoryId 分类 ID
     * @param req        模型配置请求
     * @return 空结果
     */
    @RequirePermission("kb:model-config:update")
    @PutMapping("/category/{categoryId}")
    public Result<Void> updateByCategory(@PathVariable Integer categoryId,
                                         @Valid @RequestBody AdminKbModelConfigRequest req) {
        modelConfigService.saveForCategory(categoryId, KbModelConfigConverter.fromRequest(req));
        return Result.ok();
    }

    /**
     * 接口路径：DELETE /category/{categoryId}
     * 用途：重置指定分类的模型配置为默认。
     * 权限要求：kb:model-config:update
     *
     * @param categoryId 分类 ID
     * @return 空结果
     */
    @RequirePermission("kb:model-config:update")
    @DeleteMapping("/category/{categoryId}")
    public Result<Void> resetCategory(@PathVariable Integer categoryId) {
        modelConfigService.resetCategory(categoryId);
        return Result.ok();
    }

    /**
     * 接口路径：POST /refresh-cache
     * 用途：刷新默认模型配置缓存。
     * 权限要求：kb:model-config:update
     *
     * @return 空结果
     */
    @RequirePermission("kb:model-config:update")
    @PostMapping("/refresh-cache")
    public Result<Void> refreshDefaultCache() {
        modelConfigService.refreshDefaultCache();
        return Result.ok();
    }

    /**
     * 接口路径：POST /category/{categoryId}/refresh-cache
     * 用途：刷新指定分类的模型配置缓存。
     * 权限要求：kb:model-config:update
     *
     * @param categoryId 分类 ID
     * @return 空结果
     */
    @RequirePermission("kb:model-config:update")
    @PostMapping("/category/{categoryId}/refresh-cache")
    public Result<Void> refreshCategoryCache(@PathVariable Integer categoryId) {
        KbModelConfig config = modelConfigService.current(categoryId);
        modelConfigService.refreshCache(config.getId());
        return Result.ok();
    }
}
