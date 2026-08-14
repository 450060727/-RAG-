package com.xiaoma.server.service.admin;

/**
 * 后台部门服务。
 * 负责部门的增删改查与树形结构构建。
 *
 * @author xiaoma
 * @since 2026-07-23
 */

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiaoma.server.common.BizException;
import com.xiaoma.server.dto.admin.AdminDeptRequest;
import com.xiaoma.server.dto.admin.AdminDeptTreeResponse;
import com.xiaoma.server.entity.admin.SysDept;
import com.xiaoma.server.mapper.admin.SysDeptMapper;
import com.xiaoma.server.mapper.admin.SysUserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminDeptService {

    private final SysDeptMapper sysDeptMapper;
    private final SysUserMapper sysUserMapper;

    /**
     * 构造后台部门服务。
     *
     * @param sysDeptMapper 部门 Mapper
     * @param sysUserMapper 用户 Mapper
     */
    public AdminDeptService(SysDeptMapper sysDeptMapper, SysUserMapper sysUserMapper) {
        this.sysDeptMapper = sysDeptMapper;
        this.sysUserMapper = sysUserMapper;
    }

    /**
     * 查询部门树形列表。
     *
     * @return 部门树形响应列表
     */
    public List<AdminDeptTreeResponse> tree() {
        List<SysDept> list = sysDeptMapper.selectList(
                new LambdaQueryWrapper<SysDept>().orderByAsc(SysDept::getSort, SysDept::getId)
        );
        return buildTree(list);
    }

    /**
     * 查询部门详情。
     *
     * @param id 部门 ID
     * @return 部门实体
     */
    public SysDept detail(Long id) {
        SysDept dept = sysDeptMapper.selectById(id);
        if (dept == null) {
            throw new BizException("部门不存在");
        }
        return dept;
    }

    /**
     * 创建部门，部门编码需唯一。
     *
     * @param req 部门请求
     */
    @Transactional
    public void create(AdminDeptRequest req) {
        checkCodeUnique(null, req.code());
        SysDept dept = new SysDept();
        // 父部门为空时默认为 0，表示顶层
        dept.setParentId(req.parentId() == null ? 0L : req.parentId());
        dept.setName(req.name());
        dept.setCode(req.code());
        dept.setSort(req.sort());
        dept.setStatus(0);
        sysDeptMapper.insert(dept);
    }

    /**
     * 更新部门信息，部门编码需唯一。
     *
     * @param id  部门 ID
     * @param req 部门请求
     */
    @Transactional
    public void update(Long id, AdminDeptRequest req) {
        SysDept dept = sysDeptMapper.selectById(id);
        if (dept == null) {
            throw new BizException("部门不存在");
        }
        checkCodeUnique(id, req.code());
        dept.setParentId(req.parentId() == null ? 0L : req.parentId());
        dept.setName(req.name());
        dept.setCode(req.code());
        dept.setSort(req.sort());
        sysDeptMapper.updateById(dept);
    }

    /**
     * 删除部门，删除前校验是否存在子部门或关联用户。
     *
     * @param id 部门 ID
     */
    @Transactional
    public void delete(Long id) {
        SysDept dept = sysDeptMapper.selectById(id);
        if (dept == null) {
            throw new BizException("部门不存在");
        }
        // 检查子部门
        Long childCount = sysDeptMapper.selectCount(
                new LambdaQueryWrapper<SysDept>().eq(SysDept::getParentId, id)
        );
        if (childCount > 0) {
            throw new BizException("该部门下存在子部门，不可删除");
        }
        // 检查用户关联
        Long userCount = sysUserMapper.selectCount(
                new LambdaQueryWrapper<com.xiaoma.server.entity.admin.SysUser>().eq(com.xiaoma.server.entity.admin.SysUser::getDeptId, id)
        );
        if (userCount > 0) {
            throw new BizException("该部门下存在用户，不可删除");
        }
        sysDeptMapper.deleteById(id);
    }

    /**
     * 检查部门编码是否唯一。
     *
     * @param excludeId 排除的部门 ID（更新时使用）
     * @param code      部门编码
     */
    private void checkCodeUnique(Long excludeId, String code) {
        LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDept::getCode, code);
        if (excludeId != null) {
            wrapper.ne(SysDept::getId, excludeId);
        }
        if (sysDeptMapper.selectCount(wrapper) > 0) {
            throw new BizException("部门编码已存在");
        }
    }

    /**
     * 构建部门树形结构。
     * 先将所有节点放入 Map，再按 parentId 关联父子关系。
     *
     * @param list 部门实体列表
     * @return 部门树形响应列表
     */
    private List<AdminDeptTreeResponse> buildTree(List<SysDept> list) {
        Map<Long, MutableDeptNode> map = new LinkedHashMap<>();
        for (SysDept d : list) {
            map.put(d.getId(), new MutableDeptNode(toResponse(d)));
        }
        List<AdminDeptTreeResponse> roots = new ArrayList<>();
        for (MutableDeptNode node : map.values()) {
            Long parentId = node.data.parentId();
            // 父节点为空、为 0 或不存在时作为根节点
            if (parentId == null || parentId == 0 || !map.containsKey(parentId)) {
                roots.add(rebuildWithChildren(node.data, map));
            } else {
                map.get(parentId).children.add(node.data);
            }
        }
        return roots;
    }

    /**
     * 递归重建节点及其子节点。
     *
     * @param node 当前节点响应
     * @param map  节点映射
     * @return 重建后的节点响应
     */
    private AdminDeptTreeResponse rebuildWithChildren(AdminDeptTreeResponse node, Map<Long, MutableDeptNode> map) {
        MutableDeptNode mutable = map.get(node.id());
        List<AdminDeptTreeResponse> children = mutable.children.isEmpty()
                ? List.of()
                : mutable.children.stream()
                        .map(c -> rebuildWithChildren(c, map))
                        .toList();
        return new AdminDeptTreeResponse(
                node.id(), node.parentId(), node.name(), node.code(),
                node.sort(), node.status(), children
        );
    }

    /**
     * 将部门实体转换为响应 DTO。
     *
     * @param d 部门实体
     * @return 部门树形响应
     */
    private AdminDeptTreeResponse toResponse(SysDept d) {
        return new AdminDeptTreeResponse(
                d.getId(), d.getParentId(), d.getName(), d.getCode(),
                d.getSort(), d.getStatus(), List.of()
        );
    }

    /**
     * 内部可变节点，用于树形构建过程中临时持有子节点列表。
     */
    private static class MutableDeptNode {
        final AdminDeptTreeResponse data;
        final List<AdminDeptTreeResponse> children = new ArrayList<>();

        MutableDeptNode(AdminDeptTreeResponse data) {
            this.data = data;
        }
    }
}
