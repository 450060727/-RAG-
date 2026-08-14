package com.xiaoma.server.service.admin;

/**
 * 后台菜单初始化服务。
 * 幂等创建系统配置相关菜单，并同步给已拥有「知识库管理」目录权限的角色。
 *
 * @author xiaoma
 * @since 2026-07-23
 */

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiaoma.server.entity.admin.SysMenu;
import com.xiaoma.server.entity.admin.SysRoleMenu;
import com.xiaoma.server.enums.MenuType;
import com.xiaoma.server.enums.UserStatus;
import com.xiaoma.server.mapper.admin.SysMenuMapper;
import com.xiaoma.server.mapper.admin.SysRoleMenuMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminMenuInitService {

    private static final Logger log = LoggerFactory.getLogger(AdminMenuInitService.class);

    // 系统配置目录菜单常量
    private static final String SYSTEM_CONFIG_PARENT_NAME = "系统配置";
    private static final String SYSTEM_CONFIG_PERMISSION = "system-config";
    private static final String SYSTEM_CONFIG_PATH = "/system-config";

    // 模型配置菜单常量
    private static final String MODEL_CONFIG_NAME = "模型配置";
    private static final String MODEL_CONFIG_PATH = "/system-config/model-config";
    private static final String MODEL_CONFIG_PERMISSION = "system-config:model-config";

    // 登录配置菜单常量
    private static final String LOGIN_CONFIG_NAME = "登录配置";
    private static final String LOGIN_CONFIG_PATH = "/system-config/login-config";
    private static final String LOGIN_CONFIG_PERMISSION = "system-config:login-config";

    // 兼容旧权限，页面内按钮/API 仍沿用 kb:model-config:*
    private static final String MODEL_CONFIG_API_PERMISSION = "kb:model-config";

    private final SysMenuMapper sysMenuMapper;
    private final SysRoleMenuMapper sysRoleMenuMapper;

    /**
     * 构造后台菜单初始化服务。
     *
     * @param sysMenuMapper     菜单 Mapper
     * @param sysRoleMenuMapper 角色菜单 Mapper
     */
    public AdminMenuInitService(SysMenuMapper sysMenuMapper, SysRoleMenuMapper sysRoleMenuMapper) {
        this.sysMenuMapper = sysMenuMapper;
        this.sysRoleMenuMapper = sysRoleMenuMapper;
    }

    /**
     * 应用启动时初始化系统配置菜单，失败仅记录警告不阻断启动。
     */
    @PostConstruct
    public void init() {
        try {
            Long systemConfigParentId = ensureSystemConfigParentMenu();
            Long modelConfigMenuId = ensureModelConfigMenu(systemConfigParentId);
            Long loginConfigMenuId = ensureLoginConfigMenu(systemConfigParentId);
            ensureModelConfigApiMenus(modelConfigMenuId);
            ensureLoginConfigApiMenus(loginConfigMenuId);
            assignToRolesWithKbAccess(systemConfigParentId, modelConfigMenuId, loginConfigMenuId);
        } catch (Exception e) {
            log.warn("系统配置菜单初始化失败: {}", e.getMessage());
        }
    }

    /**
     * 确保「系统配置」目录菜单存在，不存在则创建。
     *
     * @return 目录菜单 ID
     */
    private Long ensureSystemConfigParentMenu() {
        SysMenu parent = sysMenuMapper.selectOne(
                new LambdaQueryWrapper<SysMenu>()
                        .eq(SysMenu::getName, SYSTEM_CONFIG_PARENT_NAME)
                        .eq(SysMenu::getType, MenuType.DIRECTORY.getCode())
                        .last("LIMIT 1")
        );
        if (parent != null) {
            return parent.getId();
        }
        SysMenu menu = new SysMenu();
        menu.setParentId(0L);
        menu.setName(SYSTEM_CONFIG_PARENT_NAME);
        menu.setType(MenuType.DIRECTORY.getCode());
        menu.setPermission(SYSTEM_CONFIG_PERMISSION);
        menu.setPath(SYSTEM_CONFIG_PATH);
        menu.setComponent(null);
        menu.setIcon("Setting");
        menu.setSort(40);
        menu.setStatus(UserStatus.ENABLED.getCode());
        menu.setCreatedAt(LocalDateTime.now());
        menu.setUpdatedAt(LocalDateTime.now());
        sysMenuMapper.insert(menu);
        log.info("已创建「系统配置」目录菜单");
        return menu.getId();
    }

    /**
     * 确保「模型配置」菜单存在，兼容旧路径迁移。
     *
     * @param parentId 父目录菜单 ID
     * @return 模型配置菜单 ID
     */
    private Long ensureModelConfigMenu(Long parentId) {
        // 兼容：若已存在旧路径的模型配置菜单，直接迁移到新路径
        SysMenu menu = sysMenuMapper.selectOne(
                new LambdaQueryWrapper<SysMenu>()
                        .eq(SysMenu::getName, MODEL_CONFIG_NAME)
                        .eq(SysMenu::getType, MenuType.MENU.getCode())
                        .last("LIMIT 1")
        );
        if (menu != null) {
            if (!MODEL_CONFIG_PATH.equals(menu.getPath()) || !MODEL_CONFIG_PERMISSION.equals(menu.getPermission())) {
                menu.setParentId(parentId);
                menu.setPath(MODEL_CONFIG_PATH);
                menu.setPermission(MODEL_CONFIG_PERMISSION + ":view");
                menu.setComponent("kb/model-config/index");
                menu.setUpdatedAt(LocalDateTime.now());
                sysMenuMapper.updateById(menu);
                log.info("已迁移「模型配置」菜单到系统配置下");
            }
            return menu.getId();
        }
        menu = new SysMenu();
        menu.setParentId(parentId);
        menu.setName(MODEL_CONFIG_NAME);
        menu.setType(MenuType.MENU.getCode());
        menu.setPermission(MODEL_CONFIG_PERMISSION + ":view");
        menu.setPath(MODEL_CONFIG_PATH);
        menu.setComponent("kb/model-config/index");
        menu.setIcon("Cpu");
        menu.setSort(10);
        menu.setStatus(UserStatus.ENABLED.getCode());
        menu.setCreatedAt(LocalDateTime.now());
        menu.setUpdatedAt(LocalDateTime.now());
        sysMenuMapper.insert(menu);
        log.info("已创建「模型配置」菜单");
        return menu.getId();
    }

    /**
     * 确保「登录配置」菜单存在，兼容旧路径迁移。
     *
     * @param parentId 父目录菜单 ID
     * @return 登录配置菜单 ID
     */
    private Long ensureLoginConfigMenu(Long parentId) {
        SysMenu menu = sysMenuMapper.selectOne(
                new LambdaQueryWrapper<SysMenu>()
                        .eq(SysMenu::getName, LOGIN_CONFIG_NAME)
                        .eq(SysMenu::getType, MenuType.MENU.getCode())
                        .last("LIMIT 1")
        );
        if (menu != null) {
            if (!LOGIN_CONFIG_PATH.equals(menu.getPath())) {
                menu.setParentId(parentId);
                menu.setPath(LOGIN_CONFIG_PATH);
                menu.setUpdatedAt(LocalDateTime.now());
                sysMenuMapper.updateById(menu);
            }
            return menu.getId();
        }
        menu = new SysMenu();
        menu.setParentId(parentId);
        menu.setName(LOGIN_CONFIG_NAME);
        menu.setType(MenuType.MENU.getCode());
        menu.setPermission(LOGIN_CONFIG_PERMISSION + ":view");
        menu.setPath(LOGIN_CONFIG_PATH);
        menu.setComponent("kb/model-config/index");
        menu.setIcon("Lock");
        menu.setSort(20);
        menu.setStatus(UserStatus.ENABLED.getCode());
        menu.setCreatedAt(LocalDateTime.now());
        menu.setUpdatedAt(LocalDateTime.now());
        sysMenuMapper.insert(menu);
        log.info("已创建「登录配置」菜单");
        return menu.getId();
    }

    /**
     * 确保模型配置页面下的 API 按钮菜单存在。
     *
     * @param parentId 模型配置菜单 ID
     */
    private void ensureModelConfigApiMenus(Long parentId) {
        ensureApiMenu(parentId, "查看", MODEL_CONFIG_API_PERMISSION + ":view", 1);
        ensureApiMenu(parentId, "修改", MODEL_CONFIG_API_PERMISSION + ":update", 2);
    }

    /**
     * 确保登录配置页面下的 API 按钮菜单存在。
     *
     * @param parentId 登录配置菜单 ID
     */
    private void ensureLoginConfigApiMenus(Long parentId) {
        // 登录配置页面复用同一套模型配置 API，所以按钮权限也沿用 kb:model-config
        ensureApiMenu(parentId, "查看", MODEL_CONFIG_API_PERMISSION + ":view", 1);
        ensureApiMenu(parentId, "修改", MODEL_CONFIG_API_PERMISSION + ":update", 2);
    }

    /**
     * 确保指定 API 按钮菜单存在，不存在则创建。
     *
     * @param parentId   父菜单 ID
     * @param suffix     按钮名称后缀
     * @param permission 按钮权限码
     * @param sort       排序号
     */
    private void ensureApiMenu(Long parentId, String suffix, String permission, int sort) {
        SysMenu menu = sysMenuMapper.selectOne(
                new LambdaQueryWrapper<SysMenu>()
                        .eq(SysMenu::getPermission, permission)
                        .eq(SysMenu::getParentId, parentId)
                        .last("LIMIT 1")
        );
        if (menu != null) {
            return;
        }
        SysMenu parent = sysMenuMapper.selectById(parentId);
        menu = new SysMenu();
        menu.setParentId(parentId);
        menu.setName((parent != null ? parent.getName() : "") + suffix);
        menu.setType(MenuType.BUTTON.getCode());
        menu.setPermission(permission);
        menu.setPath(null);
        menu.setComponent(null);
        menu.setIcon(null);
        menu.setSort(sort);
        menu.setStatus(UserStatus.ENABLED.getCode());
        menu.setCreatedAt(LocalDateTime.now());
        menu.setUpdatedAt(LocalDateTime.now());
        sysMenuMapper.insert(menu);
    }

    /**
     * 查找拥有旧「知识库管理」目录权限的角色，自动授予新系统配置菜单。
     *
     * @param parentId          系统配置目录菜单 ID
     * @param modelConfigMenuId 模型配置菜单 ID
     * @param loginConfigMenuId 登录配置菜单 ID
     */
    private void assignToRolesWithKbAccess(Long parentId, Long modelConfigMenuId, Long loginConfigMenuId) {
        // 查找拥有旧「知识库管理」目录权限的角色
        SysMenu kbParent = sysMenuMapper.selectOne(
                new LambdaQueryWrapper<SysMenu>()
                        .eq(SysMenu::getName, "知识库管理")
                        .eq(SysMenu::getType, MenuType.DIRECTORY.getCode())
                        .last("LIMIT 1")
        );
        List<Long> roleIds = null;
        if (kbParent != null) {
            roleIds = sysRoleMenuMapper.selectRoleIdsByMenuId(kbParent.getId());
        }
        // 若未找到，则回退到按系统配置目录分配
        if (roleIds == null || roleIds.isEmpty()) {
            roleIds = sysRoleMenuMapper.selectRoleIdsByMenuId(parentId);
        }
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }

        // 收集模型配置与登录配置下的所有子菜单
        List<Long> modelChildIds = sysMenuMapper.selectList(
                        new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getParentId, modelConfigMenuId))
                .stream()
                .map(SysMenu::getId)
                .toList();
        List<Long> loginChildIds = sysMenuMapper.selectList(
                        new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getParentId, loginConfigMenuId))
                .stream()
                .map(SysMenu::getId)
                .toList();

        // 为每个角色分配目录、页面及按钮菜单权限
        for (Long roleId : roleIds) {
            insertRoleMenuIfAbsent(roleId, parentId);
            insertRoleMenuIfAbsent(roleId, modelConfigMenuId);
            insertRoleMenuIfAbsent(roleId, loginConfigMenuId);
            for (Long childId : modelChildIds) {
                insertRoleMenuIfAbsent(roleId, childId);
            }
            for (Long childId : loginChildIds) {
                insertRoleMenuIfAbsent(roleId, childId);
            }
        }
        log.info("已为 {} 个角色分配系统配置菜单权限", roleIds.size());
    }

    /**
     * 幂等插入角色菜单关联。
     *
     * @param roleId 角色 ID
     * @param menuId 菜单 ID
     */
    private void insertRoleMenuIfAbsent(Long roleId, Long menuId) {
        Long count = sysRoleMenuMapper.selectCount(
                new LambdaQueryWrapper<SysRoleMenu>()
                        .eq(SysRoleMenu::getRoleId, roleId)
                        .eq(SysRoleMenu::getMenuId, menuId)
        );
        if (count != null && count > 0) {
            return;
        }
        SysRoleMenu rm = new SysRoleMenu();
        rm.setRoleId(roleId);
        rm.setMenuId(menuId);
        sysRoleMenuMapper.insert(rm);
    }
}
