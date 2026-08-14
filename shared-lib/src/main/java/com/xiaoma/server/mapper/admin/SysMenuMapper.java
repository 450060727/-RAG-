/**
 * 系统MenuMyBatis 数据访问接口。
 */
package com.xiaoma.server.mapper.admin;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaoma.server.entity.admin.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统MenuMyBatis 数据访问接口。
 * 本接口定义了 SysMenuMapper 的公共契约与数据结构。
 */
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    List<SysMenu> selectByRoleIds(@Param("roleIds") List<Long> roleIds);

    List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);
}
