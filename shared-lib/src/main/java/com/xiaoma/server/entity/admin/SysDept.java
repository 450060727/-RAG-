/**
 * SysDept。
 */
package com.xiaoma.server.entity.admin;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * SysDept 实体。
 * 本类定义了 SysDept 的公共契约与数据结构。
 */
@TableName("sys_dept")
public class SysDept {

    @TableId(type = IdType.AUTO)
    private Long id; // id 字段

    private Long parentId; // parentId 字段

    private String name; // name 字段

    private String code; // code 字段

    private Integer sort; // sort 字段

    /** 0 正常，1 禁用 */
    private Integer status; // status 字段

    private LocalDateTime createdAt; // createdAt 字段

    private LocalDateTime updatedAt; // updatedAt 字段

    /**
     * 获取 Id。
     * @return 返回值说明
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置 Id。
     * @param id 参数说明
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取 ParentId。
     * @return 返回值说明
     */
    public Long getParentId() {
        return parentId;
    }

    /**
     * 设置 ParentId。
     * @param parentId 参数说明
     */
    public void setParentId(Long parentId) {
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
     * 获取 Code。
     * @return 返回值说明
     */
    public String getCode() {
        return code;
    }

    /**
     * 设置 Code。
     * @param code 参数说明
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * 获取 Sort。
     * @return 返回值说明
     */
    public Integer getSort() {
        return sort;
    }

    /**
     * 设置 Sort。
     * @param sort 参数说明
     */
    public void setSort(Integer sort) {
        this.sort = sort;
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
