package com.xiaoma.server.service.admin;

/**
 * 后台前台注册用户服务。
 * 负责前台注册用户的分页查询与状态管理。
 *
 * @author xiaoma
 * @since 2026-07-23
 */

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaoma.server.common.BizException;
import com.xiaoma.server.dto.admin.AdminPageResponse;
import com.xiaoma.server.dto.admin.AdminRegisteredUserPageRequest;
import com.xiaoma.server.dto.admin.AdminRegisteredUserResponse;
import com.xiaoma.server.entity.User;
import com.xiaoma.server.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class AdminRegisteredUserService {

    private final UserMapper userMapper;

    /**
     * 构造后台前台注册用户服务。
     *
     * @param userMapper 用户 Mapper
     */
    public AdminRegisteredUserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * 分页查询前台注册用户列表。
     *
     * @param req 分页查询条件
     * @return 用户分页结果
     */
    public AdminPageResponse<AdminRegisteredUserResponse> page(AdminRegisteredUserPageRequest req) {
        Page<User> page = new Page<>(
                req.page() == null || req.page() < 1 ? 1 : req.page(),
                req.size() == null || req.size() < 1 ? 10 : req.size()
        );
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        // 关键字同时匹配邮箱与昵称
        if (StringUtils.hasText(req.keyword())) {
            wrapper.and(w -> w.like(User::getEmail, req.keyword())
                    .or()
                    .like(User::getName, req.keyword()));
        }
        if (req.status() != null) {
            wrapper.eq(User::getStatus, req.status());
        }
        wrapper.orderByDesc(User::getCreatedAt);

        userMapper.selectPage(page, wrapper);

        List<AdminRegisteredUserResponse> records = page.getRecords().stream()
                .map(this::toResponse)
                .toList();
        return new AdminPageResponse<>(records, page.getTotal(), page.getSize(), page.getCurrent());
    }

    /**
     * 修改前台注册用户状态。
     *
     * @param id     用户 ID
     * @param status 新状态，仅允许 0 或 1
     */
    @Transactional
    public void changeStatus(Long id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BizException("状态值非法");
        }
        User u = userMapper.selectById(id);
        if (u == null) {
            throw new BizException("用户不存在");
        }
        u.setStatus(status);
        userMapper.updateById(u);
    }

    /**
     * 将用户实体转换为响应 DTO。
     *
     * @param u 用户实体
     * @return 用户响应
     */
    private AdminRegisteredUserResponse toResponse(User u) {
        return new AdminRegisteredUserResponse(
                u.getId(),
                u.getEmail(),
                u.getName(),
                u.getStatus(),
                u.getCreatedAt(),
                u.getUpdatedAt()
        );
    }
}
