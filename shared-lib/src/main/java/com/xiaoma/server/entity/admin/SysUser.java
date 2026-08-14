/**
 * SysUser。
 */
package com.xiaoma.server.entity.admin;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * SysUser 实体。
 * 本类定义了 SysUser 的公共契约与数据结构。
 */
@TableName("sys_user")
public class SysUser {

    @TableId(type = IdType.AUTO)
    private Long id; // id 字段

    private String username; // username 字段

    /** BCrypt 哈希 */
    private String password; // password 字段

    private String realName; // realName 字段

    private String phone; // phone 字段

    private String email; // email 字段

    private String avatar; // avatar 字段

    private Long deptId; // deptId 字段

    /** 0 正常，1 禁用 */
    private Integer status; // status 字段

    /** 0 普通，1 超级管理员 */
    private Integer superAdmin; // superAdmin 字段

    private LocalDateTime lastLoginTime; // lastLoginTime 字段

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
     * 获取 Username。
     * @return 返回值说明
     */
    public String getUsername() {
        return username;
    }

    /**
     * 设置 Username。
     * @param username 参数说明
     */
    public void setUsername(String username) {
        this.username = username;
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
     * 获取 RealName。
     * @return 返回值说明
     */
    public String getRealName() {
        return realName;
    }

    /**
     * 设置 RealName。
     * @param realName 参数说明
     */
    public void setRealName(String realName) {
        this.realName = realName;
    }

    /**
     * 获取 Phone。
     * @return 返回值说明
     */
    public String getPhone() {
        return phone;
    }

    /**
     * 设置 Phone。
     * @param phone 参数说明
     */
    public void setPhone(String phone) {
        this.phone = phone;
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
     * 获取 Avatar。
     * @return 返回值说明
     */
    public String getAvatar() {
        return avatar;
    }

    /**
     * 设置 Avatar。
     * @param avatar 参数说明
     */
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    /**
     * 获取 DeptId。
     * @return 返回值说明
     */
    public Long getDeptId() {
        return deptId;
    }

    /**
     * 设置 DeptId。
     * @param deptId 参数说明
     */
    public void setDeptId(Long deptId) {
        this.deptId = deptId;
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
     * 获取 SuperAdmin。
     * @return 返回值说明
     */
    public Integer getSuperAdmin() {
        return superAdmin;
    }

    /**
     * 设置 SuperAdmin。
     * @param superAdmin 参数说明
     */
    public void setSuperAdmin(Integer superAdmin) {
        this.superAdmin = superAdmin;
    }

    /**
     * 获取 LastLoginTime。
     * @return 返回值说明
     */
    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }

    /**
     * 设置 LastLoginTime。
     * @param lastLoginTime 参数说明
     */
    public void setLastLoginTime(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
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
