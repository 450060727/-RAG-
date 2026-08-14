/**
 * KbCategory。
 */
package com.xiaoma.server.entity.kb;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * KbCategory 实体。
 * 本类定义了 KbCategory 的公共契约与数据结构。
 */
@TableName("kb_category")
public class KbCategory {

    @TableId(type = IdType.AUTO)
    private Integer id; // id 字段
    private Integer modelConfigId;//默认模型配置
    private Integer parentId; // parentId 字段
    private String name; // name 字段
    private String description; // description 字段
    private Integer sortOrder; // sortOrder 字段
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
     * 获取 默认模型配置。
     * @return 返回值说明
     */
    public Integer getModelConfigId() {
        return modelConfigId;
    }

    /**
     * 获取 设置认模型配置。
     * @return 返回值说明
     */
    public void setModelConfigId(Integer modelConfigId) {
        this.modelConfigId = modelConfigId;
    }

    /**
     * 设置 Id。
     * @param id 参数说明
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 获取 ParentId。
     * @return 返回值说明
     */
    public Integer getParentId() {
        return parentId;
    }

    /**
     * 设置 ParentId。
     * @param parentId 参数说明
     */
    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    /**
     * 获取 Name。
     * @return 返回值说明
     */
    public String getName() {
        return name;
    }

    /**
     * 设置 Name。
     * @param name 参数说明
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取 Description。
     * @return 返回值说明
     */
    public String getDescription() {
        return description;
    }

    /**
     * 设置 Description。
     * @param description 参数说明
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 获取 SortOrder。
     * @return 返回值说明
     */
    public Integer getSortOrder() {
        return sortOrder;
    }

    /**
     * 设置 SortOrder。
     * @param sortOrder 参数说明
     */
    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
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
