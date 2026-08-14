/**
 * mapper/kb 模块的 KbModelConfigMapper 类/接口定义。
 */
package com.xiaoma.server.mapper.kb;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaoma.server.entity.kb.KbModelConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
/**
 * KbModelConfigMapper 接口。
 */
public interface KbModelConfigMapper extends BaseMapper<KbModelConfig> {

    @Select("SELECT * FROM kb_model_config WHERE is_default = 1 LIMIT 1")
    /**
     * selectDefault 方法。
     * @return 返回值说明
     */
    KbModelConfig selectDefault();

    @Select("SELECT * FROM kb_model_config WHERE id = #{id} LIMIT 1")
    KbModelConfig selectByIdLocked(@Param("id") Integer id);
}
