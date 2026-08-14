/**
 * SysRoleDept。
 */
package com.xiaoma.server.entity.admin;

import com.baomidou.mybatisplus.annotation.TableName;

/**
 * SysRoleDept 实体。
 * 本类定义了 SysRoleDept 的公共契约与数据结构。
 */
@TableName("sys_role_dept")
public class SysRoleDept {

    private Long roleId; // roleId 字段

    private Long deptId; // deptId 字段

    /**
     * 获取 RoleId。
     * @return 返回值说明
     */
    public Long getRoleId() {
        return roleId;
    }

    /**
     * 设置 RoleId。
     * @param roleId 参数说明
     */
    public void setRoleId(Long roleId) {
        this.roleId = roleId;
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
}
