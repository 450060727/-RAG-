/**
 * common 模块的 ErrorCode 类/接口定义。
 */
package com.xiaoma.server.common;

/**
 * 统一错误码枚举。
 * 约定：
 * - code = 0  ：业务成功
 * - code = 1  ：通用业务错误（HTTP 200，由 BizException 抛出）
 * - code = 401：未登录或登录已过期（HTTP 401）
 * - code = 403：无权限或账号被禁用（HTTP 403）
 * - code = 500：系统内部错误（HTTP 500，不暴露细节）
 */
public enum ErrorCode {

    /** 成功 */
    OK(0, "ok"),
    /** 通用业务错误 */
    BUSINESS_ERROR(1, "业务错误"),
    /** 未登录或登录已过期 */
    UNAUTHORIZED(401, "未登录或登录已过期"),
    /** 无权限访问 */
    FORBIDDEN(403, "无权限访问"),
    /** 系统内部错误，对外隐藏具体原因 */
    INTERNAL_ERROR(500, "系统繁忙，请稍后重试"),
    /** 参数校验失败 */
    BAD_REQUEST(400, "请求参数错误");

    private final int code; // code 字段
    private final String defaultMessage; // defaultMessage 字段

    ErrorCode(int code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    /**
     * 获取 Code。
     * @return 返回值说明
     */
    public int getCode() {
        return code;
    }

    /**
     * 获取 DefaultMessage。
     * @return 返回值说明
     */
    public String getDefaultMessage() {
        return defaultMessage;
    }
}
