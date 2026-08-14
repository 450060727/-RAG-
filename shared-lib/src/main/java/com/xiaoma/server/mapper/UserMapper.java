/**
 * UserMyBatis 数据访问接口。
 */
package com.xiaoma.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaoma.server.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * UserMyBatis 数据访问接口。
 * 本接口定义了 UserMapper 的公共契约与数据结构。
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    User selectByEmail(@Param("email") String email);

    boolean existsByEmail(@Param("email") String email);

    int updateNameById(@Param("id") Long id, @Param("name") String name);

    int updatePasswordById(@Param("id") Long id, @Param("password") String password);
}
