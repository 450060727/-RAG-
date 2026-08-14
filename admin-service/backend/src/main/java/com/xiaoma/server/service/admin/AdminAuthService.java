package com.xiaoma.server.service.admin;

/**
 * 后台认证服务。
 * 负责超级管理员初始化、登录、退出、修改密码。
 *
 * @author xiaoma
 * @since 2026-07-23
 */

import com.xiaoma.server.common.BizException;
import com.xiaoma.server.config.AdminAuthInterceptor;
import com.xiaoma.server.entity.admin.SysUser;
import com.xiaoma.server.enums.SuperAdminFlag;
import com.xiaoma.server.enums.UserStatus;
import com.xiaoma.server.mapper.admin.SysUserMapper;
import com.xiaoma.server.service.RedisService;
import com.xiaoma.server.service.kb.KbModelConfigService;
import com.xiaoma.server.util.JwtUtil;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
public class AdminAuthService {

    private static final Logger log = LoggerFactory.getLogger(AdminAuthService.class);

    // 默认超级管理员用户名
    private static final String DEFAULT_ADMIN_USERNAME = "admin";

    private final SysUserMapper sysUserMapper;
    private final JwtUtil jwtUtil;
    private final RedisService redisService;
    private final AdminPermissionService adminPermissionService;
    private final KbModelConfigService modelConfigService;
    private final BCryptPasswordEncoder encoder;
    private final String defaultAdminPassword;

    /**
     * 构造后台认证服务。
     *
     * @param sysUserMapper          用户 Mapper
     * @param jwtUtil                JWT 工具
     * @param redisService           Redis 服务
     * @param adminPermissionService 后台权限服务
     * @param modelConfigService     模型配置服务
     * @param encoder                密码加密器
     * @param defaultAdminPassword   默认超级管理员密码
     */
    public AdminAuthService(SysUserMapper sysUserMapper,
                            JwtUtil jwtUtil,
                            RedisService redisService,
                            AdminPermissionService adminPermissionService,
                            KbModelConfigService modelConfigService,
                            BCryptPasswordEncoder encoder,
                            @Value("${xiaoma.admin.default-password:admin123}") String defaultAdminPassword) {
        this.sysUserMapper = sysUserMapper;
        this.jwtUtil = jwtUtil;
        this.redisService = redisService;
        this.adminPermissionService = adminPermissionService;
        this.modelConfigService = modelConfigService;
        this.encoder = encoder;
        this.defaultAdminPassword = defaultAdminPassword;
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
     * 启动时若不存在超级管理员，则自动初始化。
     * 若使用的是默认密码，会打印强安全警告。
     */
    @PostConstruct
    public void initSuperAdmin() {
        SysUser admin = sysUserMapper.selectByUsername(DEFAULT_ADMIN_USERNAME);
        if (admin == null) {
            SysUser u = new SysUser();
            u.setUsername(DEFAULT_ADMIN_USERNAME);
            u.setPassword(encoder.encode(defaultAdminPassword));
            u.setRealName("超级管理员");
            // 0 表示账号正常
            u.setStatus(UserStatus.ENABLED.getCode());
            // 1 表示超级管理员
            u.setSuperAdmin(SuperAdminFlag.SUPER_ADMIN.getCode());
            sysUserMapper.insert(u);
            if ("admin123".equals(defaultAdminPassword)) {
                log.warn("============================================================");
                log.warn("超级管理员已使用默认密码 admin123 初始化，请立即修改密码！");
                log.warn("============================================================");
            }
        }
    }

    /**
     * 管理员登录，校验账号密码后生成 JWT 并写入 Redis 会话。
     *
     * @param username 用户名
     * @param password 密码
     * @return JWT token
     */
    public String login(String username, String password) {
        SysUser u = sysUserMapper.selectByUsername(username);
        if (u == null) {
            throw new BizException("用户名或密码错误");
        }
        if (!encoder.matches(password, u.getPassword())) {
            throw new BizException("用户名或密码错误");
        }
        if (UserStatus.isDisabled(u.getStatus())) {
            throw new BizException("账号已被禁用");
        }
        // 生成 admin 类型 JWT
        String token = jwtUtil.generate(u.getId(), "admin");
        // 写入 Redis 会话并设置过期时间
        redisService.set(AdminAuthInterceptor.SESSION_PREFIX + token, String.valueOf(u.getId()),
                sessionTtl().toMinutes(), TimeUnit.MINUTES);

        // 更新最后登录时间
        u.setLastLoginTime(LocalDateTime.now());
        sysUserMapper.updateById(u);

        // 预加载权限缓存，减少后续接口延迟
        adminPermissionService.getPermissions(u.getId());

        log.info("后台管理员登录成功: adminId={}", u.getId());
        return token;
    }

    /**
     * 退出登录，删除 Redis 中的会话记录。
     *
     * @param token 访问令牌
     */
    public void logout(String token) {
        if (StringUtils.hasText(token)) {
            redisService.delete(AdminAuthInterceptor.SESSION_PREFIX + token);
        }
    }

    /**
     * 修改当前管理员密码，修改成功后清除该用户所有后台会话。
     *
     * @param adminId     管理员 ID
     * @param oldPassword 原密码
     * @param newPassword 新密码
     */
    public void changePassword(Long adminId, String oldPassword, String newPassword) {
        SysUser u = sysUserMapper.selectById(adminId);
        if (u == null) {
            throw new BizException("用户不存在");
        }
        if (!encoder.matches(oldPassword, u.getPassword())) {
            throw new BizException("原密码错误");
        }
        u.setPassword(encoder.encode(newPassword));
        sysUserMapper.updateById(u);

        // 修改密码后踢掉该用户所有后台会话
        redisService.deleteByPattern(AdminAuthInterceptor.SESSION_PREFIX + "*");
        log.info("后台管理员修改密码: adminId={}", adminId);
    }
}
