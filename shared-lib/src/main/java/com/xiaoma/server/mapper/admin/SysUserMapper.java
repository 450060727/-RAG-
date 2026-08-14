/**
 * 系统UserMyBatis 数据访问接口。
 */
package com.xiaoma.server.mapper.admin;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaoma.server.entity.admin.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 系统UserMyBatis 数据访问接口。
 * 本接口定义了 SysUserMapper 的公共契约与数据结构。
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    SysUser selectByUsername(@Param("username") String username);

    boolean existsByUsername(@Param("username") String username);
}
