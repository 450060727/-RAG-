package com.xiaoma.server.controller.kb;

/**
 * 前台知识库对话控制器，处理 /api/kb/chat 相关请求。
 *
 * @author xiaoma
 * @since 2026-07-23
 */

import com.xiaoma.server.common.Result;
import com.xiaoma.server.dto.kb.KbChatRequest;
import com.xiaoma.server.dto.kb.KbChatResponse;
import com.xiaoma.server.service.kb.KbChatService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/kb/chat")
public class ChatController {

    private final KbChatService kbChatService;

    /**
     * 构造前台知识库对话控制器。
     *
     * @param kbChatService 知识库对话服务
     */
    public ChatController(KbChatService kbChatService) {
        this.kbChatService = kbChatService;
    }

    /**
     * 接口路径：POST /
     * 用途：执行知识库对话问答。
     * 权限要求：已登录。
     *
     * @param req 对话请求
     * @param uid 当前用户 ID
     * @return 对话响应
     */
    @PostMapping
    public Result<KbChatResponse> chat(@Valid @RequestBody KbChatRequest req,
                                         @RequestAttribute("uid") Long uid) {
        return Result.ok(kbChatService.chat(uid, req));
    }
}
