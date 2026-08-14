package com.xiaoma.server.service.admin;

/**
 * 后台菜单服务。
 * 负责菜单的增删改查与树形结构构建。
 *
 * @author xiaoma
 * @since 2026-07-23
 */

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiaoma.server.common.BizException;
import com.xiaoma.server.dto.admin.AdminMenuRequest;
import com.xiaoma.server.dto.admin.AdminMenuTreeResponse;
import com.xiaoma.server.entity.admin.SysMenu;
import com.xiaoma.server.mapper.admin.SysMenuMapper;
import com.xiaoma.server.mapper.admin.SysRoleMenuMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminMenuService {

    private final SysMenuMapper sysMenuMapper;
    private final SysRoleMenuMapper sysRoleMenuMapper;

    /**
     * 构造后台菜单服务。
     *
     * @param sysMenuMapper     菜单 Mapper
     * @param sysRoleMenuMapper 角色菜单 Mapper
     */
    public AdminMenuService(SysMenuMapper sysMenuMapper, SysRoleMenuMapper sysRoleMenuMapper) {
        this.sysMenuMapper = sysMenuMapper;
        this.sysRoleMenuMapper = sysRoleMenuMapper;
    }

    /**
     * 查询菜单树形列表。
     *
     * @return 菜单树形响应列表
     */
    public List<AdminMenuTreeResponse> tree() {
        List<SysMenu> list = sysMenuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>().orderByAsc(SysMenu::getSort, SysMenu::getId)
        );
        return buildTree(list);
    }

    /**
     * 查询菜单详情。
     *
     * @param id 菜单 ID
     * @return 菜单实体
     */
    public SysMenu detail(Long id) {
        SysMenu menu = sysMenuMapper.selectById(id);
        if (menu == null) {
            throw new BizException("菜单不存在");
        }
        return menu;
    }

    /**
     * 创建菜单。
     *
     * @param req 菜单请求
     */
    @Transactional
    public void create(AdminMenuRequest req) {
        SysMenu menu = new SysMenu();
        // 父菜单为空时默认为 0，表示顶层
        menu.setParentId(req.parentId() == null ? 0L : req.parentId());
        menu.setName(req.name());
        menu.setType(req.type());
        menu.setPermission(req.permission());
        menu.setPath(req.path());
        menu.setComponent(req.component());
        menu.setIcon(req.icon());
        menu.setSort(req.sort());
        menu.setStatus(0);
        sysMenuMapper.insert(menu);
    }

    /**
     * 更新菜单信息。
     *
     * @param id  菜单 ID
     * @param req 菜单请求
     */
    @Transactional
    public void update(Long id, AdminMenuRequest req) {
        SysMenu menu = sysMenuMapper.selectById(id);
        if (menu == null) {
            throw new BizException("菜单不存在");
        }
        menu.setParentId(req.parentId() == null ? 0L : req.parentId());
        menu.setName(req.name());
        menu.setType(req.type());
        menu.setPermission(req.permission());
        menu.setPath(req.path());
        menu.setComponent(req.component());
        menu.setIcon(req.icon());
        menu.setSort(req.sort());
        sysMenuMapper.updateById(menu);
    }

    /**
     * 删除菜单，删除前校验是否存在子菜单，并清理角色菜单关联。
     *
     * @param id 菜单 ID
     */
    @Transactional
    public void delete(Long id) {
        SysMenu menu = sysMenuMapper.selectById(id);
        if (menu == null) {
            throw new BizException("菜单不存在");
        }
        // 检查子菜单
        Long childCount = sysMenuMapper.selectCount(
                new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getParentId, id)
        );
        if (childCount > 0) {
            throw new BizException("该菜单下存在子菜单，不可删除");
        }
        // 清理角色菜单关联
        sysRoleMenuMapper.deleteByMenuId(id);
        sysMenuMapper.deleteById(id);
    }

    /**
     * 构建菜单树形结构。
     *
     * @param list 菜单实体列表
     * @return 菜单树形响应列表
     */
    private List<AdminMenuTreeResponse> buildTree(List<SysMenu> list) {
        Map<Long, MutableMenuNode> map = new LinkedHashMap<>();
        for (SysMenu m : list) {
            map.put(m.getId(), new MutableMenuNode(toResponse(m)));
        }
        List<MutableMenuNode> roots = new ArrayList<>();
        for (MutableMenuNode node : map.values()) {
            Long parentId = node.data.parentId();
            if (parentId == null || parentId == 0 || !map.containsKey(parentId)) {
                roots.add(node);
            } else {
                map.get(parentId).children.add(node.data);
            }
        }
        return roots.stream()
                .map(r -> rebuildWithChildren(r.data, map))
                .toList();
    }

    /**
     * 递归重建节点及其子节点。
     *
     * @param node 当前节点响应
     * @param map  节点映射
     * @return 重建后的节点响应
     */
    private AdminMenuTreeResponse rebuildWithChildren(AdminMenuTreeResponse node, Map<Long, MutableMenuNode> map) {
        MutableMenuNode mutable = map.get(node.id());
        List<AdminMenuTreeResponse> children = mutable.children.isEmpty()
                ? List.of()
                : mutable.children.stream().map(c -> rebuildWithChildren(c, map)).toList();
        return new AdminMenuTreeResponse(
                node.id(), node.parentId(), node.name(), node.type(), node.permission(),
                node.path(), node.component(), node.icon(), node.sort(), children
        );
    }

    /**
     * 将菜单实体转换为响应 DTO。
     *
     * @param m 菜单实体
     * @return 菜单树形响应
     */
    private AdminMenuTreeResponse toResponse(SysMenu m) {
        return new AdminMenuTreeResponse(
                m.getId(), m.getParentId(), m.getName(), m.getType(), m.getPermission(),
                m.getPath(), m.getComponent(), m.getIcon(), m.getSort(), List.of()
        );
    }

    /**
     * 内部可变节点，用于树形构建过程中临时持有子节点列表。
     */
    private static class MutableMenuNode {
        final AdminMenuTreeResponse data;
        final List<AdminMenuTreeResponse> children = new ArrayList<>();

        MutableMenuNode(AdminMenuTreeResponse data) {
            this.data = data;
        }
    }
}
