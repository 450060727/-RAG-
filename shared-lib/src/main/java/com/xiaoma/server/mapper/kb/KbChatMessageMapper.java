/**
 * 知识库ChatMessageMyBatis 数据访问接口。
 */
package com.xiaoma.server.mapper.kb;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaoma.server.entity.kb.KbChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 知识库ChatMessageMyBatis 数据访问接口。
 * 本接口定义了 KbChatMessageMapper 的公共契约与数据结构。
 */
@Mapper
public interface KbChatMessageMapper extends BaseMapper<KbChatMessage> {

    List<Map<String, Object>> selectPendingFeedback(@Param("status") String status);
}
