/**
 * 系统RoleMyBatis 数据访问接口。
 */
package com.xiaoma.server.mapper.admin;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaoma.server.entity.admin.SysRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统RoleMyBatis 数据访问接口。
 * 本接口定义了 SysRoleMapper 的公共契约与数据结构。
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {
}
