/**
 * 系统UserRoleMyBatis 数据访问接口。
 */
package com.xiaoma.server.mapper.admin;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaoma.server.entity.admin.SysUserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统UserRoleMyBatis 数据访问接口。
 * 本接口定义了 SysUserRoleMapper 的公共契约与数据结构。
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);

    void deleteByUserId(@Param("userId") Long userId);
}
