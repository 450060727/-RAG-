/**
 * 知识库DocumentMyBatis 数据访问接口。
 */
package com.xiaoma.server.mapper.kb;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaoma.server.entity.kb.KbDocument;
import org.apache.ibatis.annotations.Mapper;

/**
 * 知识库DocumentMyBatis 数据访问接口。
 * 本接口定义了 KbDocumentMapper 的公共契约与数据结构。
 */
@Mapper
public interface KbDocumentMapper extends BaseMapper<KbDocument> {
}
