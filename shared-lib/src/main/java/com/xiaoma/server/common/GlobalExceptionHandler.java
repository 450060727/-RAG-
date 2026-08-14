/**
 * GlobalException处理器。
 */
package com.xiaoma.server.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * GlobalException处理器。
 * 本类定义了 GlobalExceptionHandler 的公共契约与数据结构。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * biz 方法。
     * @param e 参数说明
     * @return 返回值说明
     */
    @ExceptionHandler(BizException.class)
    public Result<Void> biz(BizException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.error(e.getMessage());
    }

    /** @Valid 校验失败：取第一条错误信息返回 */
    /**
     * invalid 方法。
     * @param e 参数说明
     * @return 返回值说明
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> invalid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(f -> f.getDefaultMessage())
                .orElse("参数错误");
        log.warn("参数校验失败: {}", msg);
        return Result.error(msg);
    }

    /**
     * 事务回滚异常：通常是因为内层事务抛了异常但被吞掉，只保留 rollback-only 标记。
     * 这里把真正的根因挖出来返回给前端，方便定位。
     */
    @ExceptionHandler(UnexpectedRollbackException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> unexpectedRollback(UnexpectedRollbackException e) {
        Throwable root = rootCause(e);
        String msg = root != null && root.getMessage() != null
                ? root.getMessage()
                : e.getMessage();
        log.error("事务回滚，根因: {}", msg, e);
        return Result.error("操作失败：" + msg);
    }

    /**
     * other 方法。
     * @param e 参数说明
     * @return 返回值说明
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> other(Exception e) {
        log.error("服务器错误: {}", e.getMessage(), e);
        return Result.error("服务器错误：" + e.getMessage());
    }

    /**
     * 获取最底层的异常原因。
     */
    private Throwable rootCause(Throwable e) {
        Throwable cause = e;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause;
    }
}
