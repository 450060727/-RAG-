/**
 * 知识库SegmentMyBatis 数据访问接口。
 */
package com.xiaoma.server.mapper.kb;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaoma.server.entity.kb.KbSegment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 知识库SegmentMyBatis 数据访问接口。
 * 本接口定义了 KbSegmentMapper 的公共契约与数据结构。
 */
@Mapper
public interface KbSegmentMapper extends BaseMapper<KbSegment> {

    /**
     * 批量插入切片，减少单条 insert 导致的锁竞争。
     */
    int insertBatch(@Param("list") List<KbSegment> segments);
}
