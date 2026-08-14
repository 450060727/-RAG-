/**
 * User。
 */
package com.xiaoma.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * User 实体。
 * 本类定义了 User 的公共契约与数据结构。
 */
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id; // id 字段

    private String email; // email 字段

    /** BCrypt 哈希，不是明文 */
    private String password; // password 字段

    private String name; // name 字段

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
     * 获取 Email。
     * @return 返回值说明
     */
    public String getEmail() {
        return email;
    }

    /**
     * 设置 Email。
     * @param email 参数说明
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * 获取 Password。
     * @return 返回值说明
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设置 Password。
     * @param password 参数说明
     */
    public void setPassword(String password) {
        this.password = password;
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
