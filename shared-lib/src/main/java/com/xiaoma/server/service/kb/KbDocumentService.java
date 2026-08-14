/**
 * 知识库Document业务服务类。
 */
package com.xiaoma.server.service.kb;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaoma.server.config.KbProperties;
import com.xiaoma.server.dto.admin.AdminKbDocumentPageRequest;
import com.xiaoma.server.dto.admin.AdminKbDocumentResponse;
import com.xiaoma.server.entity.kb.KbDocument;
import com.xiaoma.server.entity.kb.KbSegment;
import com.xiaoma.server.mapper.kb.KbDocumentMapper;
import com.xiaoma.server.mapper.kb.KbSegmentMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 知识库Document业务服务类。
 * 本类定义了 KbDocumentService 的公共契约与数据结构。
 */
@Service
public class KbDocumentService {

    private static final long DEFAULT_PAGE = 1;
    private static final long DEFAULT_SIZE = 10;

    private final KbDocumentMapper kbDocumentMapper; // kbDocument 数据访问
    private final KbSegmentMapper kbSegmentMapper; // kbSegment 数据访问
    private final MilvusService milvusService; // milvus 服务
    private final KbProperties kbProperties; // kb 配置属性

    /**
     * 构造 KbDocumentService 实例。
     */
    public KbDocumentService(KbDocumentMapper kbDocumentMapper, KbSegmentMapper kbSegmentMapper,
                             MilvusService milvusService, KbProperties kbProperties) {
        this.kbDocumentMapper = kbDocumentMapper;
        this.kbSegmentMapper = kbSegmentMapper;
        this.milvusService = milvusService;
        this.kbProperties = kbProperties;
    }

    /**
     * page 方法。
     * @param req 参数说明
     * @return 返回值说明
     */
    public Page<AdminKbDocumentResponse> page(AdminKbDocumentPageRequest req) {
        long current = req.page() != null && req.page() > 0 ? req.page() : DEFAULT_PAGE;
        long size = req.size() != null && req.size() > 0 ? req.size() : DEFAULT_SIZE;
        Page<KbDocument> page = new Page<>(current, size);
        QueryWrapper<KbDocument> wrapper = new QueryWrapper<>();
        wrapper.ne("status", 1);
        if (req.categoryId() != null) {
            wrapper.eq("category_id", req.categoryId());
        }
        if (req.sourceType() != null && !req.sourceType().isBlank()) {
            wrapper.eq("source_type", req.sourceType());
        }
        if (req.status() != null) {
            wrapper.eq("status", req.status());
        }
        if (req.keyword() != null && !req.keyword().isBlank()) {
            wrapper.like("title", req.keyword());
        }
        wrapper.orderByDesc("created_at");

        Page<KbDocument> result = kbDocumentMapper.selectPage(page, wrapper);
        List<AdminKbDocumentResponse> records = result.getRecords().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        Page<AdminKbDocumentResponse> respPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        respPage.setRecords(records);
        return respPage;
    }

    /**
     * 获取 ById。
     * @param id 参数说明
     * @return 返回值说明
     */
    public KbDocument getById(Integer id) {
        return kbDocumentMapper.selectById(id);
    }

    /**
     * 获取 Content。
     * @param id 参数说明
     * @return 返回值说明
     */
    public String getContent(Integer id) {
        KbDocument doc = kbDocumentMapper.selectById(id);
        if (doc == null) {
            throw new com.xiaoma.server.common.BizException("文档不存在");
        }
        if (!"TEXT".equals(doc.getSourceType()) && !"QA_PAIR".equals(doc.getSourceType())) {
            throw new com.xiaoma.server.common.BizException("仅支持查看文本或 QA 对类型文档");
        }
        List<KbSegment> segments = kbSegmentMapper.selectList(
                new QueryWrapper<KbSegment>().eq("doc_id", id).orderByAsc("sort_order"));
        if (segments == null || segments.isEmpty()) {
            return "";
        }
        return segments.stream()
                .map(KbSegment::getContent)
                .collect(Collectors.joining("\n\n"));
    }

    /**
     * delete 方法。
     * @param id 参数说明
     */
    @Transactional
    public void delete(Integer id) {
        KbDocument doc = kbDocumentMapper.selectById(id);
        if (doc == null) {
            return;
        }
        // 删除 Milvus 向量
        try {
            milvusService.deleteByDocId(id);
        } catch (Exception e) {
            // ignore
        }
        // 删除本地文件
        if (doc.getFilePath() != null) {
            try {
                Path path = Paths.get(doc.getFilePath());
                Files.deleteIfExists(path);
            } catch (Exception e) {
                // ignore
            }
        }
        // 删除切片
        kbSegmentMapper.delete(new QueryWrapper<KbSegment>().eq("doc_id", id));
        // 逻辑删除文档
        doc.setStatus(1);
        kbDocumentMapper.updateById(doc);
    }

    private AdminKbDocumentResponse toResponse(KbDocument doc) {
        AdminKbDocumentResponse resp = new AdminKbDocumentResponse(
                doc.getId(),
                doc.getCategoryId(),
                null,
                doc.getTitle(),
                doc.getSourceType(),
                doc.getFileType(),
                doc.getFilePath(),
                doc.getFileSize(),
                doc.getFileMime(),
                doc.getChunkCount(),
                doc.getStatus(),
                doc.getCreatedBy(),
                null,
                doc.getCreatedAt(),
                doc.getUpdatedAt()
        );
        return resp;
    }
}
