/**
 * KbChatMessage。
 */
package com.xiaoma.server.entity.kb;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * KbChatMessage 实体。
 * 本类定义了 KbChatMessage 的公共契约与数据结构。
 */
@TableName("kb_chat_message")
public class KbChatMessage {

    @TableId(type = IdType.AUTO)
    private Integer id; // id 字段
    private Integer sessionId; // sessionId 字段
    private String role; // role 字段
    private String content; // content 字段
    private String sourcesJson; // sourcesJson 字段
    private Integer useLocal; // useLocal 字段
    private BigDecimal confidence; // confidence 字段
    private String llmResponseJson; // llmResponseJson 字段
    private String feedback; // feedback 字段
    private String writeBackStatus; // writeBackStatus 字段
    private LocalDateTime createdAt; // createdAt 字段

    /**
     * 获取 Id。
     * @return 返回值说明
     */
    public Integer getId() {
        return id;
    }

    /**
     * 设置 Id。
     * @param id 参数说明
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 获取 SessionId。
     * @return 返回值说明
     */
    public Integer getSessionId() {
        return sessionId;
    }

    /**
     * 设置 SessionId。
     * @param sessionId 参数说明
     */
    public void setSessionId(Integer sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * 获取 Role。
     * @return 返回值说明
     */
    public String getRole() {
        return role;
    }

    /**
     * 设置 Role。
     * @param role 参数说明
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * 获取 Content。
     * @return 返回值说明
     */
    public String getContent() {
        return content;
    }

    /**
     * 设置 Content。
     * @param content 参数说明
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * 获取 SourcesJson。
     * @return 返回值说明
     */
    public String getSourcesJson() {
        return sourcesJson;
    }

    /**
     * 设置 SourcesJson。
     * @param sourcesJson 参数说明
     */
    public void setSourcesJson(String sourcesJson) {
        this.sourcesJson = sourcesJson;
    }

    /**
     * 获取 UseLocal。
     * @return 返回值说明
     */
    public Integer getUseLocal() {
        return useLocal;
    }

    /**
     * 设置 UseLocal。
     * @param useLocal 参数说明
     */
    public void setUseLocal(Integer useLocal) {
        this.useLocal = useLocal;
    }

    /**
     * 获取 Confidence。
     * @return 返回值说明
     */
    public BigDecimal getConfidence() {
        return confidence;
    }

    /**
     * 设置 Confidence。
     * @param confidence 参数说明
     */
    public void setConfidence(BigDecimal confidence) {
        this.confidence = confidence;
    }

    /**
     * 获取 LlmResponseJson。
     * @return 返回值说明
     */
    public String getLlmResponseJson() {
        return llmResponseJson;
    }

    /**
     * 设置 LlmResponseJson。
     * @param llmResponseJson 参数说明
     */
    public void setLlmResponseJson(String llmResponseJson) {
        this.llmResponseJson = llmResponseJson;
    }

    /**
     * 获取 Feedback。
     * @return 返回值说明
     */
    public String getFeedback() {
        return feedback;
    }

    /**
     * 设置 Feedback。
     * @param feedback 参数说明
     */
    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    /**
     * 获取 WriteBackStatus。
     * @return 返回值说明
     */
    public String getWriteBackStatus() {
        return writeBackStatus;
    }

    /**
     * 设置 WriteBackStatus。
     * @param writeBackStatus 参数说明
     */
    public void setWriteBackStatus(String writeBackStatus) {
        this.writeBackStatus = writeBackStatus;
    }

    /**
     * 获取 CreatedAt。
     * @return 返回值说明
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 设置 CreatedAt。
     * @param createdAt 参数说明
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
