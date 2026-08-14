package com.xiaoma.server.controller.admin;

/**
 * 后台知识库反馈控制器，处理 /api/admin/kb/feedback 相关请求。
 *
 * @author xiaoma
 * @since 2026-07-23
 */

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaoma.server.annotation.RequirePermission;
import com.xiaoma.server.common.Result;
import com.xiaoma.server.dto.admin.AdminKbFeedbackPageRequest;
import com.xiaoma.server.dto.admin.AdminKbFeedbackResponse;
import com.xiaoma.server.service.kb.KbFeedbackService;
import com.xiaoma.server.service.kb.KbWriteBackService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/kb/feedback")
public class AdminKbFeedbackController {

    private final KbFeedbackService kbFeedbackService;
    private final KbWriteBackService kbWriteBackService;

    /**
     * 构造后台知识库反馈控制器。
     *
     * @param kbFeedbackService 知识库反馈服务
     * @param kbWriteBackService 知识库写回服务
     */
    public AdminKbFeedbackController(KbFeedbackService kbFeedbackService,
                                     KbWriteBackService kbWriteBackService) {
        this.kbFeedbackService = kbFeedbackService;
        this.kbWriteBackService = kbWriteBackService;
    }

    /**
     * 接口路径：GET /
     * 用途：分页查询知识库反馈列表。
     * 权限要求：kb:feedback:list
     *
     * @param req 分页查询条件
     * @return 反馈分页结果
     */
    @RequirePermission("kb:feedback:list")
    @GetMapping
    public Result<Page<AdminKbFeedbackResponse>> page(AdminKbFeedbackPageRequest req) {
        return Result.ok(kbFeedbackService.page(req));
    }

    /**
     * 接口路径：PUT /{id}/approve
     * 用途：审批通过反馈并写回知识库。
     * 权限要求：kb:feedback:approve
     *
     * @param id      反馈 ID
     * @param adminId 当前管理员 ID
     * @return 空结果
     */
    @RequirePermission("kb:feedback:approve")
    @PutMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Integer id,
                                    @RequestAttribute("adminId") Long adminId) {
        kbWriteBackService.approvePending(id, adminId.intValue());
        return Result.ok();
    }

    /**
     * 接口路径：PUT /{id}/reject
     * 用途：审批拒绝反馈。
     * 权限要求：kb:feedback:reject
     *
     * @param id 反馈 ID
     * @return 空结果
     */
    @RequirePermission("kb:feedback:reject")
    @PutMapping("/{id}/reject")
    public Result<Void> reject(@PathVariable Integer id) {
        kbWriteBackService.rejectPending(id);
        return Result.ok();
    }
}
