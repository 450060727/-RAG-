/**
 * Biz业务异常。
 */
package com.xiaoma.server.common;

/**
 * Biz业务异常。
 * 本类定义了 BizException 的公共契约与数据结构。
 */
public class BizException extends RuntimeException {
    /**
     * 构造 BizException 实例。
     * @param message 参数说明
     */
    public BizException(String message) {
        super(message);
    }
}
