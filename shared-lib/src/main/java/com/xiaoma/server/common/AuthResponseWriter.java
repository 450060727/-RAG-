/**
 * common 模块的 AuthResponseWriter 类/接口定义。
 */
package com.xiaoma.server.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 认证失败响应写入工具。
 * 统一处理 401/403 的 JSON 输出，避免在多个拦截器中重复拼接响应字符串。
 */
public final class AuthResponseWriter {

    private AuthResponseWriter() {
        // 工具类禁止实例化
    }

    // 复用 ObjectMapper 实例，避免重复创建
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * 写入 401 未登录响应。
     *
     * @param response HTTP 响应对象
     * @throws IOException 写入失败时抛出
     */
    public static void writeUnauthorized(HttpServletResponse response) throws IOException {
        write(response, HttpServletResponse.SC_UNAUTHORIZED, Result.unauthorized());
    }

    /**
     * 写入 403 禁止访问响应。
     *
     * @param response HTTP 响应对象
     * @param message  自定义提示信息，为 null 时使用默认提示
     * @throws IOException 写入失败时抛出
     */
    public static void writeForbidden(HttpServletResponse response, String message) throws IOException {
        write(response, HttpServletResponse.SC_FORBIDDEN, Result.forbidden(message));
    }

    /**
     * 将 Result 对象以 JSON 形式写入响应。
     *
     * @param response HTTP 响应对象
     * @param status   HTTP 状态码
     * @param result   响应体
     * @throws IOException 序列化或写入失败时抛出
     */
    private static void write(HttpServletResponse response, int status, Result<?> result) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        MAPPER.writeValue(response.getWriter(), result);
    }
}
