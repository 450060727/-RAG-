package com.xiaoma.server.service.admin;

/**
 * 后台用户管理服务。
 * 负责管理员用户的增删改查、数据权限过滤、角色分配与密码重置。
 *
 * @author xiaoma
 * @since 2026-07-23
 */

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaoma.server.common.BizException;
import com.xiaoma.server.config.AdminAuthInterceptor;
import com.xiaoma.server.dto.admin.AdminPageResponse;
import com.xiaoma.server.dto.admin.AdminRoleResponse;
import com.xiaoma.server.dto.admin.AdminUserCreateRequest;
import com.xiaoma.server.dto.admin.AdminUserPageRequest;
import com.xiaoma.server.dto.admin.AdminUserResponse;
import com.xiaoma.server.dto.admin.AdminUserUpdateRequest;
import com.xiaoma.server.entity.admin.SysDept;
import com.xiaoma.server.entity.admin.SysRole;
import com.xiaoma.server.entity.admin.SysUser;
import com.xiaoma.server.entity.admin.SysUserRole;
import com.xiaoma.server.enums.DataScope;
import com.xiaoma.server.enums.SuperAdminFlag;
import com.xiaoma.server.enums.UserStatus;
import com.xiaoma.server.mapper.admin.SysDeptMapper;
import com.xiaoma.server.mapper.admin.SysRoleDeptMapper;
import com.xiaoma.server.mapper.admin.SysRoleMapper;
import com.xiaoma.server.mapper.admin.SysUserMapper;
import com.xiaoma.server.mapper.admin.SysUserRoleMapper;
import com.xiaoma.server.service.RedisService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminUserService {

    // 新建用户的默认初始密码，可通过环境变量 XIAOMA_USER_DEFAULT_PASSWORD 覆盖
    private final String defaultInitialPassword;
    private final BCryptPasswordEncoder encoder;

    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysRoleDeptMapper sysRoleDeptMapper;
    private final SysDeptMapper sysDeptMapper;
    private final AdminPermissionService adminPermissionService;
    private final RedisService redisService;

    /**
     * 构造后台用户管理服务。
     *
     * @param defaultInitialPassword 默认初始密码
     * @param encoder                密码加密器
     * @param sysUserMapper          用户 Mapper
     * @param sysUserRoleMapper      用户角色 Mapper
     * @param sysRoleMapper          角色 Mapper
     * @param sysRoleDeptMapper      角色部门 Mapper
     * @param sysDeptMapper          部门 Mapper
     * @param adminPermissionService 后台权限服务
     * @param redisService           Redis 服务
     */
    public AdminUserService(@Value("${xiaoma.user.default-password:123456}") String defaultInitialPassword,
                            BCryptPasswordEncoder encoder,
                            SysUserMapper sysUserMapper,
                            SysUserRoleMapper sysUserRoleMapper,
                            SysRoleMapper sysRoleMapper,
                            SysRoleDeptMapper sysRoleDeptMapper,
                            SysDeptMapper sysDeptMapper,
                            AdminPermissionService adminPermissionService,
                            RedisService redisService) {
        this.defaultInitialPassword = defaultInitialPassword;
        this.encoder = encoder;
        this.sysUserMapper = sysUserMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sysRoleDeptMapper = sysRoleDeptMapper;
        this.sysDeptMapper = sysDeptMapper;
        this.adminPermissionService = adminPermissionService;
        this.redisService = redisService;
    }

    /**
     * 分页查询用户列表，并根据当前管理员数据权限过滤。
     *
     * @param req            查询条件
     * @param currentAdminId 当前登录管理员 ID
     * @return 分页结果
     */
    public AdminPageResponse<AdminUserResponse> page(AdminUserPageRequest req, Long currentAdminId) {
        Page<SysUser> page = new Page<>(
                req.page() == null || req.page() < 1 ? 1 : req.page(),
                req.size() == null || req.size() < 1 ? 10 : req.size()
        );
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        // 关键字同时匹配用户名与真实姓名
        if (StringUtils.hasText(req.keyword())) {
            wrapper.and(w -> w.like(SysUser::getUsername, req.keyword())
                    .or()
                    .like(SysUser::getRealName, req.keyword()));
        }
        if (req.status() != null) {
            wrapper.eq(SysUser::getStatus, req.status());
        }

        // 数据权限过滤
        applyDataScope(wrapper, currentAdminId, req.deptId());

        wrapper.orderByDesc(SysUser::getCreatedAt);

        sysUserMapper.selectPage(page, wrapper);

        List<AdminUserResponse> records = page.getRecords().stream()
                .map(this::toResponse)
                .toList();
        return new AdminPageResponse<>(records, page.getTotal(), page.getSize(), page.getCurrent());
    }

    /**
     * 查询用户详情。
     *
     * @param id 用户 ID
     * @return 用户详情
     */
    public AdminUserResponse detail(Long id) {
        SysUser u = sysUserMapper.selectById(id);
        if (u == null) {
            throw new BizException("用户不存在");
        }
        return toResponse(u);
    }

    /**
     * 创建用户，返回初始密码。
     *
     * @param req 创建请求
     * @return 初始密码
     */
    @Transactional
    public String create(AdminUserCreateRequest req) {
        if (sysUserMapper.existsByUsername(req.username())) {
            throw new BizException("用户名已存在");
        }
        SysUser u = new SysUser();
        u.setUsername(req.username());
        u.setPassword(encoder.encode(defaultInitialPassword));
        u.setRealName(req.realName());
        u.setPhone(req.phone());
        u.setEmail(req.email());
        u.setDeptId(req.deptId());
        // 0 表示正常状态
        u.setStatus(UserStatus.ENABLED.getCode());
        u.setSuperAdmin(SuperAdminFlag.NORMAL.getCode());
        sysUserMapper.insert(u);

        saveUserRoles(u.getId(), req.roleIds());
        return defaultInitialPassword;
    }

    /**
     * 更新用户信息，超级管理员不可修改。
     *
     * @param id  用户 ID
     * @param req 更新请求
     */
    @Transactional
    public void update(Long id, AdminUserUpdateRequest req) {
        SysUser u = sysUserMapper.selectById(id);
        if (u == null) {
            throw new BizException("用户不存在");
        }
        if (isSuperAdmin(u)) {
            throw new BizException("超级管理员不可修改");
        }
        u.setRealName(req.realName());
        u.setPhone(req.phone());
        u.setEmail(req.email());
        u.setDeptId(req.deptId());
        if (req.status() != null) {
            u.setStatus(req.status());
        }
        sysUserMapper.updateById(u);

        saveUserRoles(id, req.roleIds());
        // 清除权限缓存
        adminPermissionService.refreshPermissions(id);
        // 如果用户被禁用，踢掉所有后台会话
        if (UserStatus.isDisabled(u.getStatus())) {
            redisService.deleteByPattern(AdminAuthInterceptor.SESSION_PREFIX + "*");
        }
    }

    /**
     * 重置用户密码为默认初始密码，超级管理员不可重置。
     *
     * @param id 用户 ID
     */
    @Transactional
    public void resetPassword(Long id) {
        SysUser u = sysUserMapper.selectById(id);
        if (u == null) {
            throw new BizException("用户不存在");
        }
        if (isSuperAdmin(u)) {
            throw new BizException("超级管理员不可重置密码");
        }
        u.setPassword(encoder.encode(defaultInitialPassword));
        sysUserMapper.updateById(u);
        // 踢掉该用户所有会话
        redisService.deleteByPattern(AdminAuthInterceptor.SESSION_PREFIX + "*");
    }

    /**
     * 修改用户状态，超级管理员不可禁用。
     *
     * @param id     用户 ID
     * @param status 新状态
     */
    @Transactional
    public void changeStatus(Long id, Integer status) {
        SysUser u = sysUserMapper.selectById(id);
        if (u == null) {
            throw new BizException("用户不存在");
        }
        if (isSuperAdmin(u)) {
            throw new BizException("超级管理员不可禁用");
        }
        u.setStatus(status);
        sysUserMapper.updateById(u);
        adminPermissionService.refreshPermissions(id);
        if (UserStatus.isDisabled(status)) {
            redisService.deleteByPattern(AdminAuthInterceptor.SESSION_PREFIX + "*");
        }
    }

    /**
     * 删除用户，超级管理员不可删除。
     *
     * @param id 用户 ID
     */
    @Transactional
    public void delete(Long id) {
        SysUser u = sysUserMapper.selectById(id);
        if (u == null) {
            throw new BizException("用户不存在");
        }
        if (isSuperAdmin(u)) {
            throw new BizException("超级管理员不可删除");
        }
        sysUserMapper.deleteById(id);
        sysUserRoleMapper.deleteByUserId(id);
        adminPermissionService.refreshPermissions(id);
        redisService.deleteByPattern(AdminAuthInterceptor.SESSION_PREFIX + "*");
    }

    /**
     * 保存用户角色关联。
     *
     * @param userId  用户 ID
     * @param roleIds 角色 ID 列表
     */
    private void saveUserRoles(Long userId, List<Long> roleIds) {
        sysUserRoleMapper.deleteByUserId(userId);
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        List<SysUserRole> list = roleIds.stream()
                .distinct()
                .map(rid -> {
                    SysUserRole ur = new SysUserRole();
                    ur.setUserId(userId);
                    ur.setRoleId(rid);
                    return ur;
                })
                .toList();
        for (SysUserRole ur : list) {
            sysUserRoleMapper.insert(ur);
        }
    }

    /**
     * 将实体转换为响应 DTO。
     *
     * @param u 用户实体
     * @return 响应 DTO
     */
    private AdminUserResponse toResponse(SysUser u) {
        List<Long> roleIds = sysUserRoleMapper.selectRoleIdsByUserId(u.getId());
        List<SysRole> roles = roleIds.isEmpty() ? List.of() : sysRoleMapper.selectBatchIds(roleIds);
        SysDept dept = u.getDeptId() == null ? null : sysDeptMapper.selectById(u.getDeptId());
        return new AdminUserResponse(
                u.getId(),
                u.getUsername(),
                u.getRealName(),
                u.getPhone(),
                u.getEmail(),
                u.getDeptId(),
                dept == null ? null : dept.getName(),
                u.getStatus(),
                u.getSuperAdmin(),
                u.getLastLoginTime(),
                roles.stream().map(r -> new AdminRoleResponse(r.getId(), r.getName(), r.getCode(), null, null, null, null)).toList(),
                u.getCreatedAt()
        );
    }

    /**
     * 判断是否为超级管理员。
     *
     * @param u 用户实体
     * @return true 表示超级管理员
     */
    private boolean isSuperAdmin(SysUser u) {
        return SuperAdminFlag.isSuperAdmin(u.getSuperAdmin());
    }

    /**
     * 应用数据权限过滤。
     * 超级管理员不过滤；否则根据角色最宽 data_scope 限制可见部门。
     *
     * @param wrapper        查询包装器
     * @param currentAdminId 当前管理员 ID
     * @param queryDeptId    查询指定部门 ID
     */
    private void applyDataScope(LambdaQueryWrapper<SysUser> wrapper, Long currentAdminId, Long queryDeptId) {
        SysUser current = sysUserMapper.selectById(currentAdminId);
        if (current != null && isSuperAdmin(current)) {
            if (queryDeptId != null) {
                wrapper.eq(SysUser::getDeptId, queryDeptId);
            }
            return;
        }

        List<SysRole> roles = adminPermissionService.getRoleEntities(currentAdminId);
        if (roles.isEmpty()) {
            // 无角色：只能看自己
            wrapper.eq(SysUser::getId, currentAdminId);
            return;
        }

        // 取最宽的数据权限范围
        DataScope scope = resolveWidestScope(roles);

        if (scope == DataScope.ALL) {
            if (queryDeptId != null) {
                wrapper.eq(SysUser::getDeptId, queryDeptId);
            }
            return;
        }

        Set<Long> deptIds = calcDeptIds(currentAdminId, roles, scope);
        if (queryDeptId != null) {
            // 查询部门必须在权限范围内，否则强制无结果
            deptIds = deptIds.contains(queryDeptId) ? Set.of(queryDeptId) : Collections.emptySet();
        }
        if (deptIds.isEmpty()) {
            // 强制无结果：构造 1=0 条件
            wrapper.apply("1=0");
        } else {
            wrapper.in(SysUser::getDeptId, deptIds);
        }
    }

    /**
     * 从角色列表中解析最宽的数据权限范围。
     *
     * @param roles 角色列表
     * @return 最宽的数据权限范围
     */
    private DataScope resolveWidestScope(List<SysRole> roles) {
        // 默认最严格：仅本部门
        DataScope scope = DataScope.DEPT;
        for (SysRole r : roles) {
            DataScope s = DataScope.of(r.getDataScope());
            // 范围越靠后越宽：DEPT < DEPT_AND_CHILD < CUSTOM < ALL
            if (s == DataScope.ALL) {
                return DataScope.ALL;
            }
            if (s.ordinal() > scope.ordinal()) {
                scope = s;
            }
        }
        return scope;
    }

    /**
     * 根据数据权限范围计算可见部门 ID 集合。
     *
     * @param currentAdminId 当前管理员 ID
     * @param roles          角色列表
     * @param scope          数据权限范围
     * @return 可见部门 ID 集合
     */
    private Set<Long> calcDeptIds(Long currentAdminId, List<SysRole> roles, DataScope scope) {
        SysUser current = sysUserMapper.selectById(currentAdminId);
        Long currentDeptId = current == null ? null : current.getDeptId();
        List<SysDept> allDepts = sysDeptMapper.selectList(Wrappers.emptyWrapper());
        Map<Long, List<Long>> parentToChildren = new HashMap<>();
        for (SysDept d : allDepts) {
            Long pid = d.getParentId() == null ? 0L : d.getParentId();
            parentToChildren.computeIfAbsent(pid, k -> new ArrayList<>()).add(d.getId());
        }

        Set<Long> result = new HashSet<>();
        switch (scope) {
            case DEPT -> {
                if (currentDeptId != null) {
                    result.add(currentDeptId);
                }
            }
            case DEPT_AND_CHILD -> {
                if (currentDeptId != null) {
                    collectDeptAndChildren(currentDeptId, parentToChildren, result);
                }
            }
            case CUSTOM -> {
                for (SysRole r : roles) {
                    if (DataScope.CUSTOM != DataScope.of(r.getDataScope())) {
                        continue;
                    }
                    List<Long> customDeptIds = sysRoleDeptMapper.selectDeptIdsByRoleId(r.getId());
                    for (Long did : customDeptIds) {
                        collectDeptAndChildren(did, parentToChildren, result);
                    }
                }
            }
            default -> {
                // 默认仅本部门
                if (currentDeptId != null) {
                    result.add(currentDeptId);
                }
            }
        }
        return result;
    }

    /**
     * 递归收集部门及其所有子部门。
     *
     * @param deptId           当前部门 ID
     * @param parentToChildren 父部门到子部门列表的映射
     * @param result           结果集合
     */
    private void collectDeptAndChildren(Long deptId, Map<Long, List<Long>> parentToChildren, Set<Long> result) {
        if (deptId == null || result.contains(deptId)) {
            return;
        }
        result.add(deptId);
        List<Long> children = parentToChildren.getOrDefault(deptId, List.of());
        for (Long childId : children) {
            collectDeptAndChildren(childId, parentToChildren, result);
        }
    }
}
