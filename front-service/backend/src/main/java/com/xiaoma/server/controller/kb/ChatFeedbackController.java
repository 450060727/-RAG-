package com.xiaoma.server.controller.kb;

/**
 * 前台对话反馈控制器，处理 /api/kb/chat/{messageId}/feedback 相关请求。
 *
 * @author xiaoma
 * @since 2026-07-23
 */

import com.xiaoma.server.common.Result;
import com.xiaoma.server.dto.kb.KbChatFeedbackRequest;
import com.xiaoma.server.service.kb.KbWriteBackService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/kb/chat")
public class ChatFeedbackController {

    private final KbWriteBackService kbWriteBackService;

    /**
     * 构造前台对话反馈控制器。
     *
     * @param kbWriteBackService 知识库写回服务
     */
    public ChatFeedbackController(KbWriteBackService kbWriteBackService) {
        this.kbWriteBackService = kbWriteBackService;
    }

    /**
     * 接口路径：POST /{messageId}/feedback
     * 用途：提交对话消息反馈，可选择是否写回知识库。
     * 权限要求：已登录。
     *
     * @param messageId 消息 ID
     * @param req       反馈请求
     * @param uid       当前用户 ID
     * @return 空结果
     */
    @PostMapping("/{messageId}/feedback")
    public Result<Void> feedback(@PathVariable Integer messageId,
                                    @Valid @RequestBody KbChatFeedbackRequest req,
                                    @RequestAttribute("uid") Long uid) {
        kbWriteBackService.writeBackFromFeedback(
                messageId,
                req.feedback(),
                req.writeBack() != null && req.writeBack(),
                uid.intValue()
        );
        return Result.ok();
    }
}
