/**
 * 知识库Category业务服务类。
 */
package com.xiaoma.server.service.kb;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xiaoma.server.dto.admin.AdminKbCategoryRequest;
import com.xiaoma.server.dto.admin.AdminKbCategoryResponse;
import com.xiaoma.server.dto.admin.AdminKbCategoryTreeResponse;
import com.xiaoma.server.entity.kb.KbCategory;
import com.xiaoma.server.mapper.kb.KbCategoryMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 知识库Category业务服务类。
 * 本类定义了 KbCategoryService 的公共契约与数据结构。
 */
@Service
public class KbCategoryService {

    private final KbCategoryMapper kbCategoryMapper; // kbCategory 数据访问

    /**
     * 构造 KbCategoryService 实例。
     * @param kbCategoryMapper 参数说明
     */
    public KbCategoryService(KbCategoryMapper kbCategoryMapper) {
        this.kbCategoryMapper = kbCategoryMapper;
    }

    /**
     * list 方法。
     * @return 返回值说明
     */
    public List<AdminKbCategoryResponse> list() {
        List<KbCategory> all = kbCategoryMapper.selectList(new QueryWrapper<KbCategory>().orderByAsc("sort_order", "id"));
        return buildTree(all);
    }

    /**
     * tree 方法。
     * @return 返回值说明
     */
    public List<AdminKbCategoryTreeResponse> tree() {
        List<KbCategory> all = kbCategoryMapper.selectList(new QueryWrapper<KbCategory>().orderByAsc("sort_order", "id"));
        return buildSimpleTree(all, 0);
    }

    /**
     * 获取 ById。
     * @param id 参数说明
     * @return 返回值说明
     */
    public KbCategory getById(Integer id) {
        return kbCategoryMapper.selectById(id);
    }

    /**
     * create 方法。
     * @param req 参数说明
     */
    @Transactional
    public void create(AdminKbCategoryRequest req) {
        KbCategory entity = new KbCategory();
        BeanUtils.copyProperties(req, entity);
        if (entity.getParentId() == null) {
            entity.setParentId(0);
        }
        kbCategoryMapper.insert(entity);
    }

    /**
     * update 方法。
     * @param id 参数说明
     * @param req 参数说明
     */
    @Transactional
    public void update(Integer id, AdminKbCategoryRequest req) {
        KbCategory entity = kbCategoryMapper.selectById(id);
        if (entity == null) {
            return;
        }
        BeanUtils.copyProperties(req, entity);
        entity.setId(id);
        kbCategoryMapper.updateById(entity);
    }

    /**
     * delete 方法。
     * @param id 参数说明
     */
    @Transactional
    public void delete(Integer id) {
        kbCategoryMapper.deleteById(id);
        kbCategoryMapper.delete(new QueryWrapper<KbCategory>().eq("parent_id", id));
    }

    private List<AdminKbCategoryResponse> buildTree(List<KbCategory> all) {
        Map<Integer, AdminKbCategoryResponse> map = all.stream()
                .collect(Collectors.toMap(KbCategory::getId, this::toResponse));
        List<AdminKbCategoryResponse> roots = new ArrayList<>();
        for (KbCategory c : all) {
            AdminKbCategoryResponse node = map.get(c.getId());
            if (c.getParentId() == null || c.getParentId() == 0) {
                roots.add(node);
            } else {
                AdminKbCategoryResponse parent = map.get(c.getParentId());
                if (parent != null) {
                    parent.children().add(node);
                } else {
                    roots.add(node);
                }
            }
        }
        return roots;
    }

    private AdminKbCategoryResponse toResponse(KbCategory c) {
        return new AdminKbCategoryResponse(
                c.getId(),
                c.getParentId(),
                c.getName(),
                c.getDescription(),
                c.getSortOrder(),
                c.getStatus(),
                c.getCreatedAt(),
                c.getUpdatedAt(),
                new ArrayList<>()
        );
    }

    private List<AdminKbCategoryTreeResponse> buildSimpleTree(List<KbCategory> all, int parentId) {
        List<AdminKbCategoryTreeResponse> list = new ArrayList<>();
        for (KbCategory c : all) {
            if ((parentId == 0 && (c.getParentId() == null || c.getParentId() == 0))
                    || (c.getParentId() != null && c.getParentId() == parentId)) {
                list.add(new AdminKbCategoryTreeResponse(
                        c.getId(),
                        c.getParentId(),
                        c.getName(),
                        buildSimpleTree(all, c.getId())
                ));
            }
        }
        return list;
    }
}
