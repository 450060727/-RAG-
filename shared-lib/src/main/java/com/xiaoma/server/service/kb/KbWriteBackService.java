/**
 * 知识库WriteBack业务服务类。
 */
package com.xiaoma.server.service.kb;

import com.xiaoma.server.common.BizException;
import com.xiaoma.server.entity.kb.KbChatMessage;
import com.xiaoma.server.entity.kb.KbChatSession;
import com.xiaoma.server.mapper.kb.KbChatMessageMapper;
import com.xiaoma.server.mapper.kb.KbChatSessionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 知识库WriteBack业务服务类。
 * 本类定义了 KbWriteBackService 的公共契约与数据结构。
 */
@Service
public class KbWriteBackService {

    private final KbChatMessageMapper kbChatMessageMapper; // kbChatMessage 数据访问
    private final KbChatSessionMapper kbChatSessionMapper; // kbChatSession 数据访问
    private final KbIndexingService kbIndexingService; // kbIndexing 服务

    /**
     * 构造 KbWriteBackService 实例。
     */
    public KbWriteBackService(KbChatMessageMapper kbChatMessageMapper,
                              KbChatSessionMapper kbChatSessionMapper,
                              KbIndexingService kbIndexingService) {
        this.kbChatMessageMapper = kbChatMessageMapper;
        this.kbChatSessionMapper = kbChatSessionMapper;
        this.kbIndexingService = kbIndexingService;
    }

    /**
     * 简单启发式规则：回答非空且长度 >= 10 即可进入候选
     */
    public boolean shouldAutoWrite(String question, String answer) {
        return question != null && !question.isBlank()
                && answer != null && answer.trim().length() >= 10;
    }

    /**
     * writeBackFromFeedback 方法。
     * @param messageId 参数说明
     * @param feedback 参数说明
     * @param writeBack 参数说明
     * @param adminId 参数说明
     */
    @Transactional
    public void writeBackFromFeedback(Integer messageId, String feedback, boolean writeBack, Integer adminId) {
        KbChatMessage msg = kbChatMessageMapper.selectById(messageId);
        if (msg == null || !"assistant".equals(msg.getRole())) {
            throw new BizException("消息不存在或不是助手回复");
        }

        msg.setFeedback(feedback);
        if ("up".equals(feedback) && writeBack) {
            // 用户标记“有用并加入知识库”，先进入待审核，由管理员在后台批准后再写入知识库
            msg.setWriteBackStatus("PENDING");
        } else if ("down".equals(feedback)) {
            msg.setWriteBackStatus("REJECTED");
        }
        kbChatMessageMapper.updateById(msg);
    }

    /**
     * approvePending 方法。
     * @param messageId 参数说明
     * @param adminId 参数说明
     */
    @Transactional
    public void approvePending(Integer messageId, Integer adminId) {
        KbChatMessage msg = kbChatMessageMapper.selectById(messageId);
        if (msg == null || !"PENDING".equals(msg.getWriteBackStatus())) {
            throw new BizException("消息不存在或状态不是待审核");
        }
        KbChatMessage questionMsg = findQuestionInSession(msg.getSessionId(), messageId);
        if (questionMsg == null) {
            throw new BizException("未找到对应问题");
        }
        kbIndexingService.createQaPair(
                getSessionCategoryId(msg.getSessionId()),
                questionMsg.getContent(),
                msg.getContent(),
                adminId
        );
        msg.setWriteBackStatus("APPROVED");
        kbChatMessageMapper.updateById(msg);
    }

    /**
     * rejectPending 方法。
     * @param messageId 参数说明
     */
    @Transactional
    public void rejectPending(Integer messageId) {
        KbChatMessage msg = kbChatMessageMapper.selectById(messageId);
        if (msg == null || !"PENDING".equals(msg.getWriteBackStatus())) {
            throw new BizException("消息不存在或状态不是待审核");
        }
        msg.setWriteBackStatus("REJECTED");
        kbChatMessageMapper.updateById(msg);
    }

    private KbChatMessage findQuestionInSession(Integer sessionId, Integer beforeMessageId) {
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<KbChatMessage>();
        wrapper.eq("session_id", sessionId)
                .eq("role", "user")
                .lt("id", beforeMessageId)
                .orderByDesc("id")
                .last("limit 1");
        return kbChatMessageMapper.selectOne(wrapper);
    }

    private Integer getSessionCategoryId(Integer sessionId) {
        KbChatSession session = kbChatSessionMapper.selectById(sessionId);
        return session != null ? session.getCategoryId() : null;
    }
}
