/**
 * SysMenu。
 */
package com.xiaoma.server.entity.admin;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * SysMenu 实体。
 * 本类定义了 SysMenu 的公共契约与数据结构。
 */
@TableName("sys_menu")
public class SysMenu {

    @TableId(type = IdType.AUTO)
    private Long id; // id 字段

    private Long parentId; // parentId 字段

    private String name; // name 字段

    /** 1 目录，2 菜单，3 按钮/API */
    private Integer type; // type 字段

    /** 权限字符串，如 system:user:create */
    private String permission; // permission 字段

    private String path; // path 字段

    private String component; // component 字段

    private String icon; // icon 字段

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
     * 获取 Type。
     * @return 返回值说明
     */
    public Integer getType() {
        return type;
    }

    /**
     * 设置 Type。
     * @param type 参数说明
     */
    public void setType(Integer type) {
        this.type = type;
    }

    /**
     * 获取 Permission。
     * @return 返回值说明
     */
    public String getPermission() {
        return permission;
    }

    /**
     * 设置 Permission。
     * @param permission 参数说明
     */
    public void setPermission(String permission) {
        this.permission = permission;
    }

    /**
     * 获取 Path。
     * @return 返回值说明
     */
    public String getPath() {
        return path;
    }

    /**
     * 设置 Path。
     * @param path 参数说明
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 获取 Component。
     * @return 返回值说明
     */
    public String getComponent() {
        return component;
    }

    /**
     * 设置 Component。
     * @param component 参数说明
     */
    public void setComponent(String component) {
        this.component = component;
    }

    /**
     * 获取 Icon。
     * @return 返回值说明
     */
    public String getIcon() {
        return icon;
    }

    /**
     * 设置 Icon。
     * @param icon 参数说明
     */
    public void setIcon(String icon) {
        this.icon = icon;
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
