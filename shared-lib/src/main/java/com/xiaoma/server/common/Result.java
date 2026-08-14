/**
 * 统一响应结果。
 * 约定：
 * - code = 0  ：成功
 * - code = 1  ：业务错误（HTTP 200）
 * - code = 401：未登录（HTTP 401）
 * - code = 403：无权限（HTTP 403）
 * - code = 500：服务器内部错误（HTTP 500）
 *
 * @param <T> 响应数据类型
 */
package com.xiaoma.server.common;

/**
 * 统一返回体。
 * 约定：code=0 成功；code=1 业务错误（HTTP 200）；code=401 未登录（HTTP 401）。
 */
public class Result<T> {
    // 状态码
    private int code;
    // 提示信息
    private String message;
    // 响应数据
    private T data;

    /**
     * 默认构造。
     */
    public Result() {
    }

    /**
     * 全参构造。
     *
     * @param code    状态码
     * @param message 提示信息
     * @param data    响应数据
     */
    public Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功响应，携带数据。
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return 成功结果
     */
    public static <T> Result<T> ok(T data) {
        return new Result<>(0, "ok", data);
    }

    /**
     * 成功响应，无数据。
     *
     * @return 成功结果
     */
    public static Result<Void> ok() {
        return new Result<>(0, "ok", null);
    }

    /**
     * 通用业务错误。
     *
     * @param message 错误提示
     * @return 错误结果
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(1, message, null);
    }

    /**
     * 指定错误码与提示。
     *
     * @param code    错误码
     * @param message 错误提示
     * @return 错误结果
     */
    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 基于 ErrorCode 生成错误结果，使用默认提示。
     *
     * @param errorCode 错误码枚举
     * @return 错误结果
     */
    public static <T> Result<T> error(ErrorCode errorCode) {
        return new Result<>(errorCode.getCode(), errorCode.getDefaultMessage(), null);
    }

    /**
     * 未登录或登录已过期。
     *
     * @return 401 错误结果
     */
    public static <T> Result<T> unauthorized() {
        return error(ErrorCode.UNAUTHORIZED);
    }

    /**
     * 无权限访问。
     *
     * @param message 自定义提示，可为 null（使用默认）
     * @return 403 错误结果
     */
    public static <T> Result<T> forbidden(String message) {
        String msg = message == null || message.isBlank() ? ErrorCode.FORBIDDEN.getDefaultMessage() : message;
        return new Result<>(ErrorCode.FORBIDDEN.getCode(), msg, null);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
