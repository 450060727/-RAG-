/**
 * 系统RoleMenuMyBatis 数据访问接口。
 */
package com.xiaoma.server.mapper.admin;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaoma.server.entity.admin.SysRoleMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统RoleMenuMyBatis 数据访问接口。
 * 本接口定义了 SysRoleMenuMapper 的公共契约与数据结构。
 */
@Mapper
public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenu> {

    void deleteByRoleId(@Param("roleId") Long roleId);

    void deleteByMenuId(@Param("menuId") Long menuId);

    List<Long> selectRoleIdsByMenuId(@Param("menuId") Long menuId);
}
