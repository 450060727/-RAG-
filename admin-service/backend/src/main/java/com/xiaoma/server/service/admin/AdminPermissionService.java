package com.xiaoma.server.service.admin;

/**
 * 后台权限服务。
 * 加载用户角色、菜单、权限码，并维护 Redis 权限缓存。
 *
 * @author xiaoma
 * @since 2026-07-23
 */

import com.xiaoma.server.dto.admin.AdminMenuTreeResponse;
import com.xiaoma.server.dto.admin.AdminRoleResponse;
import com.xiaoma.server.entity.admin.SysMenu;
import com.xiaoma.server.entity.admin.SysRole;
import com.xiaoma.server.mapper.admin.SysDeptMapper;
import com.xiaoma.server.mapper.admin.SysMenuMapper;
import com.xiaoma.server.mapper.admin.SysRoleDeptMapper;
import com.xiaoma.server.mapper.admin.SysRoleMapper;
import com.xiaoma.server.mapper.admin.SysUserRoleMapper;
import com.xiaoma.server.service.RedisService;
import com.xiaoma.server.service.kb.KbModelConfigService;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@SuppressWarnings("deprecation")
@Service
public class AdminPermissionService {

    // 权限码 Redis 缓存前缀
    public static final String PERMS_KEY_PREFIX = "xiaoma:admin:perms:";
    // 菜单树 Redis 缓存前缀（当前未实际缓存对象）
    public static final String MENUS_KEY_PREFIX = "xiaoma:admin:menus:";

    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysRoleDeptMapper sysRoleDeptMapper;
    private final SysMenuMapper sysMenuMapper;
    private final SysDeptMapper sysDeptMapper;
    private final RedisService redisService;
    private final KbModelConfigService modelConfigService;

    /**
     * 构造后台权限服务。
     *
     * @param sysUserRoleMapper  用户角色 Mapper
     * @param sysRoleMapper      角色 Mapper
     * @param sysRoleDeptMapper  角色部门 Mapper
     * @param sysMenuMapper      菜单 Mapper
     * @param sysDeptMapper      部门 Mapper
     * @param redisService       Redis 服务
     * @param modelConfigService 模型配置服务
     */
    public AdminPermissionService(SysUserRoleMapper sysUserRoleMapper,
                                  SysRoleMapper sysRoleMapper,
                                  SysRoleDeptMapper sysRoleDeptMapper,
                                  SysMenuMapper sysMenuMapper,
                                  SysDeptMapper sysDeptMapper,
                                  RedisService redisService,
                                  KbModelConfigService modelConfigService) {
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sysRoleDeptMapper = sysRoleDeptMapper;
        this.sysMenuMapper = sysMenuMapper;
        this.sysDeptMapper = sysDeptMapper;
        this.redisService = redisService;
        this.modelConfigService = modelConfigService;
    }

    /**
     * 获取后台会话 TTL。
     *
     * @return 会话有效期
     */
    private Duration sessionTtl() {
        return Duration.ofMinutes(modelConfigService.current().getSessionTtlMinutes());
    }

    /**
     * 获取用户角色 ID 列表。
     *
     * @param userId 用户 ID
     * @return 角色 ID 列表
     */
    public List<Long> getRoleIds(Long userId) {
        return sysUserRoleMapper.selectRoleIdsByUserId(userId);
    }

    /**
     * 获取用户角色实体列表（过滤已禁用角色，含数据范围）。
     *
     * @param userId 用户 ID
     * @return 角色实体列表
     */
    public List<SysRole> getRoleEntities(Long userId) {
        List<Long> roleIds = getRoleIds(userId);
        if (roleIds.isEmpty()) {
            return List.of();
        }
        return sysRoleMapper.selectBatchIds(roleIds).stream()
                .filter(r -> r.getStatus() != null && r.getStatus() == 0)
                .toList();
    }

    /**
     * 获取用户角色详情列表。
     *
     * @param userId 用户 ID
     * @return 角色响应列表
     */
    public List<AdminRoleResponse> getRoles(Long userId) {
        return getRoleEntities(userId).stream()
                .map(r -> new AdminRoleResponse(r.getId(), r.getName(), r.getCode(), null, null, null, null))
                .toList();
    }

    /**
     * 获取用户可见菜单树。
     *
     * @param userId 用户 ID
     * @return 菜单树响应列表
     */
    public List<AdminMenuTreeResponse> getMenuTree(Long userId) {
        String key = MENUS_KEY_PREFIX + userId;
        // 暂不缓存菜单树对象，每次实时查；如需缓存可序列化 JSON
        List<SysMenu> menus = getMenus(userId);
        return buildMenuTree(menus);
    }

    /**
     * 获取用户权限码集合，优先从 Redis 缓存读取。
     *
     * @param userId 用户 ID
     * @return 权限码集合
     */
    public Set<String> getPermissions(Long userId) {
        String key = PERMS_KEY_PREFIX + userId;
        Set<String> cached = redisService.sMembers(key);
        if (cached != null && !cached.isEmpty()) {
            return cached;
        }
        Set<String> perms = loadPermissions(userId);
        if (!perms.isEmpty()) {
            redisService.sAdd(key, perms.toArray(new String[0]));
            redisService.expire(key, sessionTtl().toMinutes(), TimeUnit.MINUTES);
        }
        return perms;
    }

    /**
     * 刷新用户权限缓存。
     *
     * @param userId 用户 ID
     */
    public void refreshPermissions(Long userId) {
        redisService.delete(PERMS_KEY_PREFIX + userId);
        redisService.delete(MENUS_KEY_PREFIX + userId);
    }

    /**
     * 判断用户是否拥有指定权限。
     *
     * @param userId     用户 ID
     * @param permission 权限码
     * @return true 表示拥有权限
     */
    public boolean hasPermission(Long userId, String permission) {
        return getPermissions(userId).contains(permission);
    }

    /**
     * 加载用户权限码集合。
     *
     * @param userId 用户 ID
     * @return 权限码集合
     */
    private Set<String> loadPermissions(Long userId) {
        List<SysMenu> menus = getMenus(userId);
        return menus.stream()
                .map(SysMenu::getPermission)
                .filter(Objects::nonNull)
                .filter(p -> !p.isBlank())
                .collect(Collectors.toSet());
    }

    /**
     * 根据角色 ID 查询用户可见菜单列表。
     *
     * @param userId 用户 ID
     * @return 菜单实体列表
     */
    private List<SysMenu> getMenus(Long userId) {
        List<Long> roleIds = getRoleIds(userId);
        if (roleIds.isEmpty()) {
            return List.of();
        }
        return sysMenuMapper.selectByRoleIds(roleIds);
    }

    /**
     * 构建菜单树形结构。
     *
     * @param menus 菜单实体列表
     * @return 菜单树响应列表
     */
    private List<AdminMenuTreeResponse> buildMenuTree(List<SysMenu> menus) {
        Map<Long, MutableMenuNode> map = new LinkedHashMap<>();
        for (SysMenu m : menus) {
            map.put(m.getId(), new MutableMenuNode(toTreeNode(m)));
        }
        List<AdminMenuTreeResponse> roots = new ArrayList<>();
        for (MutableMenuNode node : map.values()) {
            Long parentId = node.data.parentId();
            if (parentId == null || parentId == 0 || !map.containsKey(parentId)) {
                roots.add(node.data);
            } else {
                map.get(parentId).children.add(node.data);
            }
        }
        return roots.stream()
                .map(n -> rebuildWithChildren(n, map))
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
                : mutable.children.stream()
                        .map(c -> rebuildWithChildren(c, map))
                        .toList();
        return new AdminMenuTreeResponse(
                node.id(), node.parentId(), node.name(), node.type(), node.permission(),
                node.path(), node.component(), node.icon(), node.sort(), children
        );
    }

    /**
     * 将菜单实体转换为树形响应 DTO。
     *
     * @param m 菜单实体
     * @return 菜单树响应
     */
    private AdminMenuTreeResponse toTreeNode(SysMenu m) {
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
