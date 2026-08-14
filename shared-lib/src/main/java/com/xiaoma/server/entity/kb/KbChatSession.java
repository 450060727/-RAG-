/**
 * KbChatSession。
 */
package com.xiaoma.server.entity.kb;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * KbChatSession 实体。
 * 本类定义了 KbChatSession 的公共契约与数据结构。
 */
@TableName("kb_chat_session")
public class KbChatSession {

    @TableId(type = IdType.AUTO)
    private Integer id; // id 字段
    private Integer userId; // userId 字段
    private Integer categoryId; // categoryId 字段
    private String title; // title 字段
    private Integer status; // status 字段
    private LocalDateTime createdAt; // createdAt 字段
    private LocalDateTime updatedAt; // updatedAt 字段

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
     * 获取 UserId。
     * @return 返回值说明
     */
    public Integer getUserId() {
        return userId;
    }

    /**
     * 设置 UserId。
     * @param userId 参数说明
     */
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    /**
     * 获取 CategoryId。
     * @return 返回值说明
     */
    public Integer getCategoryId() {
        return categoryId;
    }

    /**
     * 设置 CategoryId。
     * @param categoryId 参数说明
     */
    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    /**
     * 获取 Title。
     * @return 返回值说明
     */
    public String getTitle() {
        return title;
    }

    /**
     * 设置 Title。
     * @param title 参数说明
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 获取 Status。
     * @return 返回值说明
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * 设置 Status。
     * @param status 参数说明
     */
    public void setStatus(Integer status) {
        this.status = status;
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

    /**
     * 获取 UpdatedAt。
     * @return 返回值说明
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 设置 UpdatedAt。
     * @param updatedAt 参数说明
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
