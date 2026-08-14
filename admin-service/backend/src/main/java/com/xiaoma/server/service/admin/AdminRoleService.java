package com.xiaoma.server.service.admin;

/**
 * 后台角色服务。
 * 负责角色的增删改查、数据权限部门绑定与菜单分配。
 *
 * @author xiaoma
 * @since 2026-07-23
 */

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiaoma.server.common.BizException;
import com.xiaoma.server.dto.admin.AdminRoleMenuRequest;
import com.xiaoma.server.dto.admin.AdminRoleRequest;
import com.xiaoma.server.dto.admin.AdminRoleResponse;
import com.xiaoma.server.entity.admin.SysRole;
import com.xiaoma.server.entity.admin.SysRoleDept;
import com.xiaoma.server.entity.admin.SysRoleMenu;
import com.xiaoma.server.entity.admin.SysUserRole;
import com.xiaoma.server.enums.DataScope;
import com.xiaoma.server.enums.UserStatus;
import com.xiaoma.server.mapper.admin.SysMenuMapper;
import com.xiaoma.server.mapper.admin.SysRoleDeptMapper;
import com.xiaoma.server.mapper.admin.SysRoleMapper;
import com.xiaoma.server.mapper.admin.SysRoleMenuMapper;
import com.xiaoma.server.mapper.admin.SysUserRoleMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class AdminRoleService {

    private final SysRoleMapper sysRoleMapper;
    private final SysRoleMenuMapper sysRoleMenuMapper;
    private final SysRoleDeptMapper sysRoleDeptMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysMenuMapper sysMenuMapper;
    private final AdminPermissionService adminPermissionService;

    /**
     * 构造后台角色服务。
     *
     * @param sysRoleMapper          角色 Mapper
     * @param sysRoleMenuMapper      角色菜单 Mapper
     * @param sysRoleDeptMapper      角色部门 Mapper
     * @param sysUserRoleMapper      用户角色 Mapper
     * @param sysMenuMapper          菜单 Mapper
     * @param adminPermissionService 后台权限服务
     */
    public AdminRoleService(SysRoleMapper sysRoleMapper,
                            SysRoleMenuMapper sysRoleMenuMapper,
                            SysRoleDeptMapper sysRoleDeptMapper,
                            SysUserRoleMapper sysUserRoleMapper,
                            SysMenuMapper sysMenuMapper,
                            AdminPermissionService adminPermissionService) {
        this.sysRoleMapper = sysRoleMapper;
        this.sysRoleMenuMapper = sysRoleMenuMapper;
        this.sysRoleDeptMapper = sysRoleDeptMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.sysMenuMapper = sysMenuMapper;
        this.adminPermissionService = adminPermissionService;
    }

    /**
     * 查询角色列表。
     *
     * @return 角色响应列表
     */
    public List<AdminRoleResponse> list() {
        return sysRoleMapper.selectList(new LambdaQueryWrapper<SysRole>().orderByDesc(SysRole::getCreatedAt)).stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * 查询角色详情。
     *
     * @param id 角色 ID
     * @return 角色详情
     */
    public AdminRoleResponse detail(Long id) {
        SysRole role = sysRoleMapper.selectById(id);
        if (role == null) {
            throw new BizException("角色不存在");
        }
        return toResponse(role);
    }

    /**
     * 创建角色，角色编码需唯一，并保存数据权限部门关联。
     *
     * @param req 角色请求
     */
    @Transactional
    public void create(AdminRoleRequest req) {
        checkCodeUnique(null, req.code());
        SysRole role = new SysRole();
        role.setName(req.name());
        role.setCode(req.code());
        role.setDataScope(req.dataScope());
        role.setRemark(req.remark());
        // 0 表示角色状态正常
        role.setStatus(UserStatus.ENABLED.getCode());
        sysRoleMapper.insert(role);

        saveRoleDepts(role.getId(), req.dataScope(), req.deptIds());
    }

    /**
     * 更新角色信息，角色编码需唯一，并刷新拥有该角色用户的权限缓存。
     *
     * @param id  角色 ID
     * @param req 角色请求
     */
    @Transactional
    public void update(Long id, AdminRoleRequest req) {
        SysRole role = sysRoleMapper.selectById(id);
        if (role == null) {
            throw new BizException("角色不存在");
        }
        checkCodeUnique(id, req.code());
        role.setName(req.name());
        role.setCode(req.code());
        role.setDataScope(req.dataScope());
        role.setRemark(req.remark());
        sysRoleMapper.updateById(role);

        saveRoleDepts(id, req.dataScope(), req.deptIds());
        // 刷新拥有该角色的所有用户权限缓存
        refreshUserPermissionsByRoleId(id);
    }

    /**
     * 删除角色，删除前校验是否已分配用户。
     *
     * @param id 角色 ID
     */
    @Transactional
    public void delete(Long id) {
        SysRole role = sysRoleMapper.selectById(id);
        if (role == null) {
            throw new BizException("角色不存在");
        }
        Long userCount = sysUserRoleMapper.selectCount(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, id)
        );
        if (userCount > 0) {
            throw new BizException("该角色已分配用户，不可删除");
        }
        sysRoleMapper.deleteById(id);
        sysRoleMenuMapper.deleteByRoleId(id);
        sysRoleDeptMapper.deleteByRoleId(id);
    }

    /**
     * 为角色分配菜单权限，并刷新相关用户权限缓存。
     *
     * @param roleId 角色 ID
     * @param req    角色菜单分配请求
     */
    @Transactional
    public void assignMenus(Long roleId, AdminRoleMenuRequest req) {
        SysRole role = sysRoleMapper.selectById(roleId);
        if (role == null) {
            throw new BizException("角色不存在");
        }
        sysRoleMenuMapper.deleteByRoleId(roleId);
        if (!CollectionUtils.isEmpty(req.menuIds())) {
            List<SysRoleMenu> list = req.menuIds().stream()
                    .distinct()
                    .map(mid -> {
                        SysRoleMenu rm = new SysRoleMenu();
                        rm.setRoleId(roleId);
                        rm.setMenuId(mid);
                        return rm;
                    })
                    .toList();
            for (SysRoleMenu rm : list) {
                sysRoleMenuMapper.insert(rm);
            }
        }
        refreshUserPermissionsByRoleId(roleId);
    }

    /**
     * 获取角色已分配的菜单 ID 列表。
     *
     * @param roleId 角色 ID
     * @return 菜单 ID 列表
     */
    public List<Long> getMenuIds(Long roleId) {
        return sysMenuMapper.selectMenuIdsByRoleId(roleId);
    }

    /**
     * 保存角色与部门关联，仅自定义数据权限时生效。
     *
     * @param roleId    角色 ID
     * @param dataScope 数据权限范围
     * @param deptIds   部门 ID 列表
     */
    private void saveRoleDepts(Long roleId, String dataScope, List<Long> deptIds) {
        sysRoleDeptMapper.deleteByRoleId(roleId);
        // 仅自定义数据权限时才需要绑定部门
        if (DataScope.CUSTOM == DataScope.of(dataScope) && !CollectionUtils.isEmpty(deptIds)) {
            List<SysRoleDept> list = deptIds.stream()
                    .distinct()
                    .map(did -> {
                        SysRoleDept rd = new SysRoleDept();
                        rd.setRoleId(roleId);
                        rd.setDeptId(did);
                        return rd;
                    })
                    .toList();
            for (SysRoleDept rd : list) {
                sysRoleDeptMapper.insert(rd);
            }
        }
    }

    /**
     * 刷新拥有指定角色的所有用户权限缓存。
     *
     * @param roleId 角色 ID
     */
    private void refreshUserPermissionsByRoleId(Long roleId) {
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, roleId)
        );
        for (SysUserRole ur : userRoles) {
            adminPermissionService.refreshPermissions(ur.getUserId());
        }
    }

    /**
     * 检查角色编码是否唯一。
     *
     * @param excludeId 排除的角色 ID（更新时使用）
     * @param code      角色编码
     */
    private void checkCodeUnique(Long excludeId, String code) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getCode, code);
        if (excludeId != null) {
            wrapper.ne(SysRole::getId, excludeId);
        }
        if (sysRoleMapper.selectCount(wrapper) > 0) {
            throw new BizException("角色编码已存在");
        }
    }

    /**
     * 将角色实体转换为响应 DTO。
     *
     * @param role 角色实体
     * @return 角色响应
     */
    private AdminRoleResponse toResponse(SysRole role) {
        List<Long> menuIds = sysMenuMapper.selectMenuIdsByRoleId(role.getId());
        List<Long> deptIds = sysRoleDeptMapper.selectDeptIdsByRoleId(role.getId());
        return new AdminRoleResponse(
                role.getId(), role.getName(), role.getCode(),
                role.getDataScope(), role.getRemark(), menuIds, deptIds
        );
    }
}
