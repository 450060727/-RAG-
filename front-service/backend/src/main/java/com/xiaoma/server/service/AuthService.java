package com.xiaoma.server.service;

/**
 * 前台认证服务。
 * 负责注册验证码发送、用户注册、登录、密码重置。
 * 验证码统一存储在 Redis 中，避免内存缓存重启丢失与多实例不一致问题。
 *
 * @author xiaoma
 * @since 2026-07-23
 */

import com.xiaoma.server.common.BizException;
import com.xiaoma.server.entity.User;
import com.xiaoma.server.enums.UserStatus;
import com.xiaoma.server.mapper.UserMapper;
import com.xiaoma.server.service.kb.KbModelConfigService;
import com.xiaoma.server.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.Instant;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    // Redis 中注册验证码 key 前缀
    private static final String REGISTER_CODE_PREFIX = "xiaoma:code:register:";
    // Redis 中重置密码验证码 key 前缀
    private static final String RESET_CODE_PREFIX = "xiaoma:code:reset:";

    private final UserMapper userMapper;
    private final JavaMailSender mailSender;
    private final JwtUtil jwtUtil;
    private final SessionService sessionService;
    private final KbModelConfigService modelConfigService;
    private final BCryptPasswordEncoder encoder;
    private final RedisService redisService;
    private final String mailFrom;

    /**
     * 构造前台认证服务。
     *
     * @param userMapper         用户 Mapper
     * @param mailSender         邮件发送器
     * @param jwtUtil            JWT 工具
     * @param sessionService     会话服务
     * @param modelConfigService 模型配置服务
     * @param encoder            密码加密器
     * @param redisService       Redis 服务
     * @param mailFrom           发件人邮箱
     */
    public AuthService(UserMapper userMapper,
                       JavaMailSender mailSender,
                       JwtUtil jwtUtil,
                       SessionService sessionService,
                       KbModelConfigService modelConfigService,
                       BCryptPasswordEncoder encoder,
                       RedisService redisService,
                       @Value("${spring.mail.username:}") String mailFrom) {
        this.userMapper = userMapper;
        this.mailSender = mailSender;
        this.jwtUtil = jwtUtil;
        this.sessionService = sessionService;
        this.modelConfigService = modelConfigService;
        this.encoder = encoder;
        this.redisService = redisService;
        this.mailFrom = mailFrom;
    }

    /**
     * 获取验证码有效时长（秒）。
     *
     * @return TTL 秒数
     */
    private long codeTtlSeconds() {
        return modelConfigService.current().getCodeTtlSeconds();
    }

    /**
     * 获取验证码重发间隔（秒）。
     *
     * @return 间隔秒数
     */
    private long resendIntervalSeconds() {
        return modelConfigService.current().getCodeResendIntervalSeconds();
    }

    /**
     * 发送注册验证码。
     *
     * @param email 邮箱
     */
    public void sendCode(String email) {
        sendCodeInternal(email, REGISTER_CODE_PREFIX, "注册验证码");
    }

    /**
     * 发送重置密码验证码。
     *
     * @param email 邮箱
     */
    public void sendResetCode(String email) {
        User u = userMapper.selectByEmail(email);
        if (u == null) {
            throw new BizException("该邮箱未注册");
        }
        assertNotDisabled(u);
        sendCodeInternal(email, RESET_CODE_PREFIX, "密码重置验证码");
    }

    /**
     * 发送验证码通用逻辑：校验重发间隔、生成验证码、写入 Redis、发送邮件。
     *
     * @param email   邮箱
     * @param prefix  Redis key 前缀
     * @param subject 邮件主题
     */
    private void sendCodeInternal(String email, String prefix, String subject) {
        long resendInterval = resendIntervalSeconds();
        long ttl = codeTtlSeconds();
        String key = prefix + email;
        String existing = redisService.get(key);
        CodeEntry old = existing == null ? null : parseEntry(existing);
        // 在重发间隔内禁止重复发送
        if (old != null && Instant.now().isBefore(old.lastSent().plusSeconds(resendInterval))) {
            throw new BizException("发送太频繁，请 " + resendInterval + " 秒后再试");
        }
        String code = generateCode();
        CodeEntry entry = new CodeEntry(code,
                Instant.now().plusSeconds(ttl),
                Instant.now());
        // 验证码写入 Redis，过期后自动失效
        redisService.set(key, serializeEntry(entry), ttl, java.util.concurrent.TimeUnit.SECONDS);

        sendEmail(email, code, subject);
    }

    /**
     * 生成 6 位数字验证码。
     *
     * @return 验证码
     */
    private String generateCode() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }

    /**
     * 发送邮件；未配置发件人时打印到日志作为 dev 兜底。
     *
     * @param email   收件邮箱
     * @param code    验证码
     * @param subject 主题
     */
    private void sendEmail(String email, String code, String subject) {
        if (StringUtils.hasText(mailFrom)) {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(mailFrom);
            msg.setTo(email);
            msg.setSubject(subject);
            msg.setText("你的验证码是：" + code + "，" + (codeTtlSeconds() / 60) + " 分钟内有效。");
            mailSender.send(msg);
            log.debug("已发送{}到 {}", subject, email);
        } else {
            // dev 兜底：未配置 SMTP 时打印到日志，便于本地调试
            log.warn("[DEV] 邮箱 {} 的{}：{}", email, subject, code);
        }
    }

    /**
     * 用户注册，校验验证码并创建用户。
     *
     * @param email    邮箱
     * @param code     验证码
     * @param password 密码
     * @param name     昵称
     */
    public void register(String email, String code, String password, String name) {
        validateCode(email, code, REGISTER_CODE_PREFIX);
        if (userMapper.existsByEmail(email)) {
            redisService.delete(REGISTER_CODE_PREFIX + email);
            throw new BizException("该邮箱已注册");
        }
        redisService.delete(REGISTER_CODE_PREFIX + email); // 一次性，注册成功即作废

        User u = new User();
        u.setEmail(email);
        u.setPassword(encoder.encode(password));
        u.setName(name);
        // 0 表示用户状态正常
        u.setStatus(UserStatus.ENABLED.getCode());
        userMapper.insert(u);
    }

    /**
     * 重置密码，校验验证码后更新密码。
     *
     * @param email    邮箱
     * @param code     验证码
     * @param password 新密码
     */
    public void resetPassword(String email, String code, String password) {
        validateCode(email, code, RESET_CODE_PREFIX);
        User u = userMapper.selectByEmail(email);
        if (u == null) {
            redisService.delete(RESET_CODE_PREFIX + email);
            throw new BizException("该邮箱未注册");
        }
        assertNotDisabled(u);
        redisService.delete(RESET_CODE_PREFIX + email); // 一次性

        userMapper.updatePasswordById(u.getId(), encoder.encode(password));
    }

    /**
     * 校验验证码是否有效。
     *
     * @param email  邮箱
     * @param code   验证码
     * @param prefix Redis key 前缀
     */
    private void validateCode(String email, String code, String prefix) {
        String key = prefix + email;
        String stored = redisService.get(key);
        if (stored == null) {
            throw new BizException("请先获取验证码");
        }
        CodeEntry entry = parseEntry(stored);
        if (entry == null) {
            redisService.delete(key);
            throw new BizException("验证码数据异常，请重新获取");
        }
        if (Instant.now().isAfter(entry.expireAt())) {
            redisService.delete(key);
            throw new BizException("验证码已过期，请重新获取");
        }
        if (!entry.code().equals(code)) {
            throw new BizException("验证码错误");
        }
    }

    /**
     * 用户登录，校验邮箱密码后生成 JWT 并创建会话。
     *
     * @param email    邮箱
     * @param password 密码
     * @return JWT token
     */
    public String login(String email, String password) {
        User u = userMapper.selectByEmail(email);
        if (u == null) {
            throw new BizException("邮箱或密码错误");
        }
        if (!encoder.matches(password, u.getPassword())) {
            throw new BizException("邮箱或密码错误");
        }
        assertNotDisabled(u);
        String token = jwtUtil.generate(u.getId());
        sessionService.create(token, u.getId());
        log.info("用户登录成功: userId={}", u.getId());
        return token;
    }

    /**
     * 校验用户账号未被禁用。
     *
     * @param u 用户实体
     */
    private void assertNotDisabled(User u) {
        if (UserStatus.isDisabled(u.getStatus())) {
            throw new BizException("账号已被禁用");
        }
    }

    /**
     * 退出登录，删除当前会话。
     *
     * @param token 访问令牌
     */
    public void logout(String token) {
        sessionService.delete(token);
    }

    /**
     * 将验证码条目序列化为字符串。
     *
     * @param entry 验证码条目
     * @return 序列化字符串
     */
    private String serializeEntry(CodeEntry entry) {
        return entry.code() + "|" + entry.expireAt().toEpochMilli() + "|" + entry.lastSent().toEpochMilli();
    }

    /**
     * 从字符串反序列化验证码条目。
     *
     * @param raw 原始字符串
     * @return 验证码条目，解析失败返回 null
     */
    private CodeEntry parseEntry(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String[] parts = raw.split("\\|");
        if (parts.length != 3) {
            return null;
        }
        try {
            String code = parts[0];
            Instant expireAt = Instant.ofEpochMilli(Long.parseLong(parts[1]));
            Instant lastSent = Instant.ofEpochMilli(Long.parseLong(parts[2]));
            return new CodeEntry(code, expireAt, lastSent);
        } catch (NumberFormatException e) {
            log.warn("验证码条目解析失败: {}", raw);
            return null;
        }
    }

    /**
     * 验证码内部记录，包含验证码、过期时间与最后发送时间。
     *
     * @param code     验证码
     * @param expireAt 过期时间
     * @param lastSent 最后发送时间
     */
    private record CodeEntry(String code, Instant expireAt, Instant lastSent) {
    }
}
