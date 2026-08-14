/**
 * 知识库Init业务服务类。
 */
package com.xiaoma.server.service.kb;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xiaoma.server.entity.kb.KbCategory;
import com.xiaoma.server.mapper.kb.KbCategoryMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 知识库Init业务服务类。
 * 本类定义了 KbInitService 的公共契约与数据结构。
 */
@Service
public class KbInitService {

    private final KbCategoryMapper kbCategoryMapper; // kbCategory 数据访问

    /**
     * 构造 KbInitService 实例。
     * @param kbCategoryMapper 参数说明
     */
    public KbInitService(KbCategoryMapper kbCategoryMapper) {
        this.kbCategoryMapper = kbCategoryMapper;
    }

    /**
     * initDefaultCategory 方法。
     */
    @PostConstruct
    public void initDefaultCategory() {
        Long count = kbCategoryMapper.selectCount(new QueryWrapper<KbCategory>());
        if (count != null && count > 0) {
            return;
        }
        KbCategory category = new KbCategory();
        category.setParentId(0);
        category.setName("默认知识库");
        category.setDescription("系统自动创建的默认知识库分类");
        category.setStatus(0);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        kbCategoryMapper.insert(category);
    }
}
