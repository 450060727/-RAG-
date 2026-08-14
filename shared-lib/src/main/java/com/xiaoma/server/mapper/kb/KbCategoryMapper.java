/**
 * 知识库CategoryMyBatis 数据访问接口。
 */
package com.xiaoma.server.mapper.kb;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaoma.server.entity.kb.KbCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 知识库CategoryMyBatis 数据访问接口。
 * 本接口定义了 KbCategoryMapper 的公共契约与数据结构。
 */
@Mapper
public interface KbCategoryMapper extends BaseMapper<KbCategory> {
}
