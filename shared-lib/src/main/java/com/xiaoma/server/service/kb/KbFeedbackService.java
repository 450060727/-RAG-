/**
 * 知识库Feedback业务服务类。
 */
package com.xiaoma.server.service.kb;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaoma.server.dto.admin.AdminKbFeedbackPageRequest;
import com.xiaoma.server.dto.admin.AdminKbFeedbackResponse;
import com.xiaoma.server.mapper.kb.KbChatMessageMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 知识库Feedback业务服务类。
 * 本类定义了 KbFeedbackService 的公共契约与数据结构。
 */
@Service
public class KbFeedbackService {

    private final KbChatMessageMapper kbChatMessageMapper; // kbChatMessage 数据访问

    /**
     * 构造 KbFeedbackService 实例。
     * @param kbChatMessageMapper 参数说明
     */
    public KbFeedbackService(KbChatMessageMapper kbChatMessageMapper) {
        this.kbChatMessageMapper = kbChatMessageMapper;
    }

    /**
     * page 方法。
     * @param req 参数说明
     * @return 返回值说明
     */
    public Page<AdminKbFeedbackResponse> page(AdminKbFeedbackPageRequest req) {
        long current = req.page() != null && req.page() > 0 ? req.page() : 1;
        long size = req.size() != null && req.size() > 0 ? req.size() : 10;
        String status = req.writeBackStatus() != null && !req.writeBackStatus().isBlank()
                ? req.writeBackStatus()
                : "PENDING";

        List<Map<String, Object>> rows = kbChatMessageMapper.selectPendingFeedback(status);
        List<AdminKbFeedbackResponse> records = rows.stream()
                .skip((current - 1) * size)
                .limit(size)
                .map(this::toResponse)
                .collect(Collectors.toList());

        Page<AdminKbFeedbackResponse> page = new Page<>(current, size, rows.size());
        page.setRecords(records);
        return page;
    }

    private AdminKbFeedbackResponse toResponse(Map<String, Object> row) {
        return new AdminKbFeedbackResponse(
                (Integer) row.get("id"),
                (Integer) row.get("sessionId"),
                (String) row.get("question"),
                (String) row.get("answer"),
                row.get("useLocal") != null ? ((Number) row.get("useLocal")).intValue() : 0,
                row.get("confidence") != null ? new BigDecimal(row.get("confidence").toString()) : null,
                (String) row.get("feedback"),
                (String) row.get("writeBackStatus"),
                null
        );
    }
}
