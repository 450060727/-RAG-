package com.xiaoma.server.controller.admin;

/**
 * 后台知识库文档控制器，处理 /api/admin/kb/documents 相关请求。
 *
 * @author xiaoma
 * @since 2026-07-23
 */

import com.xiaoma.server.annotation.RequirePermission;
import com.xiaoma.server.common.BizException;
import com.xiaoma.server.common.Result;
import com.xiaoma.server.config.KbProperties;
import com.xiaoma.server.dto.admin.AdminKbDocumentPageRequest;
import com.xiaoma.server.dto.admin.AdminKbDocumentResponse;
import com.xiaoma.server.dto.admin.AdminKbTextInputRequest;
import com.xiaoma.server.entity.kb.KbDocument;
import com.xiaoma.server.service.kb.KbDocumentService;
import com.xiaoma.server.service.kb.KbIndexingService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/admin/kb/documents")
public class AdminKbDocumentController {

    private final KbDocumentService kbDocumentService;
    private final KbIndexingService kbIndexingService;
    private final KbProperties kbProperties;

    /**
     * 构造后台知识库文档控制器。
     *
     * @param kbDocumentService 知识库文档服务
     * @param kbIndexingService 知识库索引服务
     * @param kbProperties      知识库配置属性
     */
    public AdminKbDocumentController(KbDocumentService kbDocumentService,
                                     KbIndexingService kbIndexingService,
                                     KbProperties kbProperties) {
        this.kbDocumentService = kbDocumentService;
        this.kbIndexingService = kbIndexingService;
        this.kbProperties = kbProperties;
    }

    /**
     * 接口路径：GET /
     * 用途：分页查询知识库文档列表。
     * 权限要求：kb:document:list
     *
     * @param req 分页查询条件
     * @return 文档分页结果
     */
    @RequirePermission("kb:document:list")
    @GetMapping
    public Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<AdminKbDocumentResponse>> page(AdminKbDocumentPageRequest req) {
        return Result.ok(kbDocumentService.page(req));
    }

    /**
     * 接口路径：POST /upload
     * 用途：上传文件并创建知识库文档。
     * 权限要求：kb:document:create
     *
     * @param categoryId 分类 ID
     * @param file       上传文件
     * @param adminId    当前管理员 ID
     * @return 空结果
     * @throws Exception 文件处理异常
     */
    @RequirePermission("kb:document:create")
    @PostMapping("/upload")
    public Result<Void> upload(@RequestParam Integer categoryId,
                                  @RequestParam MultipartFile file,
                                  @RequestAttribute("adminId") Long adminId) throws Exception {
        validateFile(file);
        kbIndexingService.createUploadDocument(categoryId, file.getOriginalFilename(),
                file.getContentType(), file.getBytes(), adminId.intValue());
        return Result.ok();
    }

    /**
     * 接口路径：POST /text
     * 用途：保存文本内容并创建知识库文档。
     * 权限要求：kb:document:create
     *
     * @param req     文本输入请求
     * @param adminId 当前管理员 ID
     * @return 空结果
     */
    @RequirePermission("kb:document:create")
    @PostMapping("/text")
    public Result<Void> saveText(@Valid @RequestBody AdminKbTextInputRequest req,
                                    @RequestAttribute("adminId") Long adminId) {
        kbIndexingService.createTextDocument(req.categoryId(), req.title(), req.content(), adminId.intValue());
        return Result.ok();
    }

    /**
     * 接口路径：DELETE /{id}
     * 用途：删除知识库文档。
     * 权限要求：kb:document:delete
     *
     * @param id 文档 ID
     * @return 空结果
     */
    @RequirePermission("kb:document:delete")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        kbDocumentService.delete(id);
        return Result.ok();
    }

    /**
     * 接口路径：GET /{id}/content
     * 用途：查询知识库文档文本内容。
     * 权限要求：kb:document:list
     *
     * @param id 文档 ID
     * @return 文档内容
     */
    @RequirePermission("kb:document:list")
    @GetMapping("/{id}/content")
    public Result<String> content(@PathVariable Integer id) {
        return Result.ok(kbDocumentService.getContent(id));
    }

    /**
     * 接口路径：GET /{id}/download
     * 用途：下载知识库上传文件。
     * 权限要求：kb:document:list
     *
     * @param id 文档 ID
     * @return 文件资源响应
     * @throws Exception 文件处理异常
     */
    @RequirePermission("kb:document:list")
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Integer id) throws Exception {
        KbDocument doc = kbDocumentService.getById(id);
        if (doc == null) {
            throw new BizException("文档不存在");
        }
        if (!"UPLOAD".equals(doc.getSourceType())) {
            throw new BizException("仅支持下载上传文件");
        }
        Path path = Paths.get(doc.getFilePath());
        if (!Files.exists(path)) {
            throw new BizException("文件不存在");
        }
        Resource resource = new UrlResource(path.toUri());
        String fileName = doc.getTitle() != null ? doc.getTitle() : path.getFileName().toString();
        String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                .contentType(MediaType.parseMediaType(doc.getFileMime() != null ? doc.getFileMime() : "application/octet-stream"))
                .body(resource);
    }

    /**
     * 校验上传文件是否为空且 MIME 类型在允许列表中。
     *
     * @param file 上传文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new com.xiaoma.server.common.BizException("文件不能为空");
        }
        String mime = file.getContentType();
        List<String> allowed = kbProperties.getUpload().getAllowedMimeTypes();
        if (mime == null || !allowed.contains(mime)) {
            throw new com.xiaoma.server.common.BizException("不支持的文件类型: " + mime);
        }
    }
}
