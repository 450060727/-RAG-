/**
 * TextChunker。
 */
package com.xiaoma.server.service.kb;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * TextChunker。
 * 本类定义了 TextChunker 的公共契约与数据结构。
 */
@Component
public class TextChunker {

    private static final int DEFAULT_CHUNK_SIZE = 512;
    private static final int DEFAULT_OVERLAP = 128;

    /**
     * split 方法。
     * @param text 参数说明
     * @return 返回值说明
     */
    public List<String> split(String text) {
        return split(text, DEFAULT_CHUNK_SIZE, DEFAULT_OVERLAP);
    }

    /**
     * split 方法。
     * @param text 参数说明
     * @param chunkSize 参数说明
     * @param overlap 参数说明
     * @return 返回值说明
     */
    public List<String> split(String text, int chunkSize, int overlap) {
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }
        String normalized = text.replaceAll("\\r\\n", "\n").trim();
        if (normalized.length() <= chunkSize) {
            return new ArrayList<>(List.of(normalized));
        }

        List<String> chunks = new ArrayList<>();
        int step = chunkSize - overlap;
        int start = 0;
        while (start < normalized.length()) {
            int end = Math.min(start + chunkSize, normalized.length());
            String chunk = normalized.substring(start, end).trim();
            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }
            if (end == normalized.length()) {
                break;
            }
            start += step;
        }
        return chunks;
    }
}
