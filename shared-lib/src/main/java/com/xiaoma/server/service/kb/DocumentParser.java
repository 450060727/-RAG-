/**
 * DocumentParser。
 */
package com.xiaoma.server.service.kb;

import com.xiaoma.server.common.BizException;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * DocumentParser。
 * 本类定义了 DocumentParser 的公共契约与数据结构。
 */
@Component
public class DocumentParser {

    private final Tika tika = new Tika();

    /**
     * parse 方法。
     * @param fileName 参数说明
     * @param bytes 参数说明
     * @return 返回值说明
     */
    public String parse(String fileName, byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".txt") || lower.endsWith(".md") || lower.endsWith(".markdown")) {
            return new String(bytes, StandardCharsets.UTF_8);
        }
        try (InputStream is = new ByteArrayInputStream(bytes)) {
            Metadata metadata = new Metadata();
            metadata.set("resourceName", fileName);
            return tika.parseToString(is, metadata, 100_000);
        } catch (Exception e) {
            throw new BizException("文档解析失败: " + e.getMessage());
        }
    }
}
