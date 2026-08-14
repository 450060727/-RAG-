/**
 * 系统RoleDeptMyBatis 数据访问接口。
 */
package com.xiaoma.server.mapper.admin;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaoma.server.entity.admin.SysRoleDept;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统RoleDeptMyBatis 数据访问接口。
 * 本接口定义了 SysRoleDeptMapper 的公共契约与数据结构。
 */
@Mapper
public interface SysRoleDeptMapper extends BaseMapper<SysRoleDept> {

    void deleteByRoleId(@Param("roleId") Long roleId);

    List<Long> selectDeptIdsByRoleId(@Param("roleId") Long roleId);
}
