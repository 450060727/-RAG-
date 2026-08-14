package com.xiaoma.server.controller.kb;

/**
 * 前台知识库分类控制器，处理 /api/kb/categories 相关请求。
 *
 * @author xiaoma
 * @since 2026-07-23
 */

import com.xiaoma.server.common.Result;
import com.xiaoma.server.dto.admin.AdminKbCategoryTreeResponse;
import com.xiaoma.server.service.kb.KbCategoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/kb/categories")
public class KbCategoryController {

    private final KbCategoryService kbCategoryService;

    /**
     * 构造前台知识库分类控制器。
     *
     * @param kbCategoryService 知识库分类服务
     */
    public KbCategoryController(KbCategoryService kbCategoryService) {
        this.kbCategoryService = kbCategoryService;
    }

    /**
     * 接口路径：GET /tree
     * 用途：查询知识库分类树形列表。
     * 权限要求：公开接口，无需登录。
     *
     * @return 分类树形列表
     */
    @GetMapping("/tree")
    public Result<List<AdminKbCategoryTreeResponse>> tree() {
        return Result.ok(kbCategoryService.tree());
    }
}
