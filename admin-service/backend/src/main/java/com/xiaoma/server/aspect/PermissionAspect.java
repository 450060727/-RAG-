package com.xiaoma.server.aspect;

/**
 * 权限校验切面。
 * 拦截带有 @RequirePermission 注解的方法，校验当前登录管理员是否具备指定权限。
 *
 * @author xiaoma
 * @since 2026-07-23
 */

import com.xiaoma.server.annotation.RequirePermission;
import com.xiaoma.server.common.BizException;
import com.xiaoma.server.config.AdminAuthInterceptor;
import com.xiaoma.server.entity.admin.SysUser;
import com.xiaoma.server.enums.SuperAdminFlag;
import com.xiaoma.server.mapper.admin.SysUserMapper;
import com.xiaoma.server.service.admin.AdminPermissionService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class PermissionAspect {

    private final SysUserMapper sysUserMapper;
    private final AdminPermissionService adminPermissionService;

    /**
     * 构造权限校验切面。
     *
     * @param sysUserMapper          用户 Mapper
     * @param adminPermissionService 后台权限服务
     */
    public PermissionAspect(SysUserMapper sysUserMapper, AdminPermissionService adminPermissionService) {
        this.sysUserMapper = sysUserMapper;
        this.adminPermissionService = adminPermissionService;
    }

    /**
     * 在方法执行前校验权限，超级管理员自动跳过。
     *
     * @param point             连接点
     * @param requirePermission 方法上的权限注解
     */
    @Before("@annotation(requirePermission)")
    public void checkPermission(JoinPoint point, RequirePermission requirePermission) {
        Long adminId = getAdminId();
        if (adminId == null) {
            throw new BizException("未登录");
        }
        SysUser user = sysUserMapper.selectById(adminId);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        // 超级管理员跳过权限校验
        if (SuperAdminFlag.isSuperAdmin(user.getSuperAdmin())) {
            return;
        }
        String permission = requirePermission.value();
        if (!adminPermissionService.hasPermission(adminId, permission)) {
            throw new BizException("无操作权限");
        }
    }

    /**
     * 从请求属性中获取当前管理员 ID。
     *
     * @return 管理员 ID，未登录时返回 null
     */
    private Long getAdminId() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return null;
        }
        HttpServletRequest request = attrs.getRequest();
        Object adminId = request.getAttribute(AdminAuthInterceptor.ADMIN_ID_ATTR);
        return adminId == null ? null : (Long) adminId;
    }
}
