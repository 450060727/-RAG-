/**
 * 知识库Indexing业务服务类。
 */
package com.xiaoma.server.service.kb;

import com.xiaoma.server.common.BizException;
import com.xiaoma.server.config.KbProperties;
import com.xiaoma.server.entity.kb.KbDocument;
import com.xiaoma.server.entity.kb.KbSegment;
import com.xiaoma.server.mapper.kb.KbDocumentMapper;
import com.xiaoma.server.mapper.kb.KbSegmentMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 知识库Indexing业务服务类。
 * 本类定义了 KbIndexingService 的公共契约与数据结构。
 */
@Service
public class KbIndexingService {

    private static final Logger log = LoggerFactory.getLogger(KbIndexingService.class);

    private final KbDocumentMapper kbDocumentMapper; // kbDocument 数据访问
    private final KbSegmentMapper kbSegmentMapper; // kbSegment 数据访问
    private final KbEmbeddingClient embeddingClient; // embedding 客户端
    private final TextChunker textChunker; // 文本分片器
    private final DocumentParser documentParser; // 文档解析器
    private final MilvusService milvusService; // milvus 服务
    private final KbProperties kbProperties; // kb 配置属性

    /**
     * 构造 KbIndexingService 实例。
     */
    public KbIndexingService(KbDocumentMapper kbDocumentMapper, KbSegmentMapper kbSegmentMapper,
                             KbEmbeddingClient embeddingClient, TextChunker textChunker,
                             DocumentParser documentParser, MilvusService milvusService,
                             KbProperties kbProperties) {
        this.kbDocumentMapper = kbDocumentMapper;
        this.kbSegmentMapper = kbSegmentMapper;
        this.embeddingClient = embeddingClient;
        this.textChunker = textChunker;
        this.documentParser = documentParser;
        this.milvusService = milvusService;
        this.kbProperties = kbProperties;
    }

    @Retryable(
            retryFor = {PessimisticLockingFailureException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )
    /**
     * 创建文本类型文档，保存原文并触发索引。
     *
     * @param categoryId 分类 ID
     * @param title      文档标题
     * @param content    文档正文
     * @param adminId    操作管理员 ID
     * @return 保存后的文档实体
     */
    @Transactional
    public KbDocument createTextDocument(Integer categoryId, String title, String content, Integer adminId) {
        KbDocument doc = new KbDocument();
        doc.setCategoryId(categoryId);
        doc.setTitle(title);
        doc.setSourceType("TEXT");
        doc.setFileType("txt");
        doc.setChunkCount(0);
        doc.setStatus(2); // 索引中状态
        doc.setCreatedBy(adminId);
        doc.setCreatedAt(LocalDateTime.now());
        doc.setUpdatedAt(LocalDateTime.now());
        kbDocumentMapper.insert(doc);

        // 对文本内容进行分片、向量化并写入 Milvus
        indexDocument(doc.getId(), categoryId, title, "TEXT", "txt", content);
        return doc;
    }

    @Retryable(
            retryFor = {PessimisticLockingFailureException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )
    /**
     * createQaPair 方法。
     * @param categoryId 参数说明
     * @param question 参数说明
     * @param answer 参数说明
     * @param adminId 参数说明
     * @return 返回值说明
     */
    @Transactional
    public KbDocument createQaPair(Integer categoryId, String question, String answer, Integer adminId) {
        String content = "Q: " + question + "\nA: " + answer;
        KbDocument doc = new KbDocument();
        doc.setCategoryId(categoryId);
        doc.setTitle(question);
        doc.setSourceType("QA_PAIR");
        doc.setFileType("qa");
        doc.setChunkCount(0);
        doc.setStatus(2);
        doc.setCreatedBy(adminId);
        doc.setCreatedAt(LocalDateTime.now());
        doc.setUpdatedAt(LocalDateTime.now());
        kbDocumentMapper.insert(doc);

        indexDocument(doc.getId(), categoryId, question, "QA_PAIR", "qa", content);
        return doc;
    }

    @Retryable(
            retryFor = {PessimisticLockingFailureException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )
    /**
     * createUploadDocument 方法。
     * @return 返回值说明
     */
    @Transactional
    public KbDocument createUploadDocument(Integer categoryId, String originalName, String mimeType,
                                           byte[] bytes, Integer adminId) {
        String ext = getExtension(originalName);
        String fileType = resolveFileType(mimeType, ext);
        String storePath = saveFile(categoryId, originalName, bytes);

        KbDocument doc = new KbDocument();
        doc.setCategoryId(categoryId);
        doc.setTitle(originalName);
        doc.setSourceType("UPLOAD");
        doc.setFileType(fileType);
        doc.setFilePath(storePath);
        doc.setFileSize((long) bytes.length);
        doc.setFileMime(mimeType);
        doc.setChunkCount(0);
        doc.setStatus(2);
        doc.setCreatedBy(adminId);
        doc.setCreatedAt(LocalDateTime.now());
        doc.setUpdatedAt(LocalDateTime.now());
        kbDocumentMapper.insert(doc);

        try {
            String text = parseUpload(fileType, storePath, originalName, bytes);
            indexDocument(doc.getId(), categoryId, originalName, "UPLOAD", fileType, text);
        } catch (Exception e) {
            doc.setStatus(9);
            kbDocumentMapper.updateById(doc);
            throw e;
        }
        return doc;
    }

    /**
     * indexDocument 方法。
     */
    public void indexDocument(Integer docId, Integer categoryId, String title,
                              String sourceType, String fileType, String text) {
        KbDocument doc = kbDocumentMapper.selectById(docId);
        if (doc == null) {
            throw new BizException("文档不存在");
        }

        // 清理旧切片
        kbSegmentMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<KbSegment>().eq("doc_id", docId));
        try {
            milvusService.deleteByDocId(docId);
        } catch (Exception e) {
            log.warn("删除旧向量失败 docId={}: {}", docId, e.getMessage());
        }

        if (text == null || text.isBlank()) {
            doc.setChunkCount(0);
            doc.setStatus(0);
            kbDocumentMapper.updateById(doc);
            return;
        }

        List<String> chunks = textChunker.split(text);
        if (chunks.isEmpty()) {
            doc.setChunkCount(0);
            doc.setStatus(0);
            kbDocumentMapper.updateById(doc);
            return;
        }

        List<float[]> vectors = embeddingClient.embed(chunks);
        List<Long> milvusIds = milvusService.insert(categoryId, docId, sourceType, fileType, title, chunks, vectors);

        List<KbSegment> segments = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            KbSegment seg = new KbSegment();
            seg.setDocId(docId);
            seg.setCategoryId(categoryId);
            seg.setContent(chunks.get(i));
            seg.setVectorId(milvusIds.size() > i ? String.valueOf(milvusIds.get(i)) : null);
            seg.setSortOrder(i);
            seg.setStatus(0);
            seg.setCreatedAt(LocalDateTime.now());
            segments.add(seg);
        }
        if (!segments.isEmpty()) {
            kbSegmentMapper.insertBatch(segments);
        }

        doc.setChunkCount(chunks.size());
        doc.setStatus(0);
        kbDocumentMapper.updateById(doc);
    }

    private String parseUpload(String fileType, String filePath, String originalName, byte[] bytes) {
        if ("image".equals(fileType) || "audio".equals(fileType) || "video".equals(fileType)) {
            // MVP 阶段多媒体仅保留文件，文本描述暂为空；后续接入 OCR/ASR
            return "";
        }
        return documentParser.parse(originalName, bytes);
    }

    private String saveFile(Integer categoryId, String originalName, byte[] bytes) {
        try {
            String base = kbProperties.getUpload().getPath();
            String month = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
            Path dir = Paths.get(base, String.valueOf(categoryId), month);
            Files.createDirectories(dir);
            String ext = getExtension(originalName);
            String fileName = UUID.randomUUID().toString().replace("-", "") + (ext.isEmpty() ? "" : "." + ext);
            Path target = dir.resolve(fileName);
            Files.write(target, bytes);
            return target.toAbsolutePath().toString();
        } catch (Exception e) {
            throw new BizException("文件保存失败: " + e.getMessage());
        }
    }

    private String getExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    private String resolveFileType(String mimeType, String ext) {
        if (mimeType != null) {
            if (mimeType.startsWith("image/")) return "image";
            if (mimeType.startsWith("audio/")) return "audio";
            if (mimeType.startsWith("video/")) return "video";
            if (mimeType.equals("application/pdf")) return "pdf";
            if (mimeType.equals("application/msword") || mimeType.contains("wordprocessingml")) return "docx";
        }
        if ("pdf".equals(ext)) return "pdf";
        if ("docx".equals(ext) || "doc".equals(ext)) return "docx";
        if ("md".equals(ext) || "markdown".equals(ext)) return "md";
        if ("jpg".equals(ext) || "jpeg".equals(ext) || "png".equals(ext) || "webp".equals(ext)) return "image";
        if ("mp3".equals(ext) || "wav".equals(ext)) return "audio";
        if ("mp4".equals(ext) || "mkv".equals(ext)) return "video";
        return "txt";
    }
}
