/**
 * KbDocument。
 */
package com.xiaoma.server.entity.kb;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * KbDocument 实体。
 * 本类定义了 KbDocument 的公共契约与数据结构。
 */
@TableName("kb_document")
public class KbDocument {

    @TableId(type = IdType.AUTO)
    private Integer id; // id 字段
    private Integer categoryId; // categoryId 字段
    private String title; // title 字段
    private String sourceType; // sourceType 字段
    private String fileType; // fileType 字段
    private String filePath; // filePath 字段
    private Long fileSize; // fileSize 字段
    private String fileMime; // fileMime 字段
    private Integer chunkCount; // chunkCount 字段
    private Integer status; // status 字段
    private Integer createdBy; // createdBy 字段
    private LocalDateTime createdAt; // createdAt 字段
    private LocalDateTime updatedAt; // updatedAt 字段

    /**
     * 获取 Id。
     * @return 返回值说明
     */
    public Integer getId() {
        return id;
    }

    /**
     * 设置 Id。
     * @param id 参数说明
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 获取 CategoryId。
     * @return 返回值说明
     */
    public Integer getCategoryId() {
        return categoryId;
    }

    /**
     * 设置 CategoryId。
     * @param categoryId 参数说明
     */
    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    /**
     * 获取 Title。
     * @return 返回值说明
     */
    public String getTitle() {
        return title;
    }

    /**
     * 设置 Title。
     * @param title 参数说明
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 获取 SourceType。
     * @return 返回值说明
     */
    public String getSourceType() {
        return sourceType;
    }

    /**
     * 设置 SourceType。
     * @param sourceType 参数说明
     */
    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    /**
     * 获取 FileType。
     * @return 返回值说明
     */
    public String getFileType() {
        return fileType;
    }

    /**
     * 设置 FileType。
     * @param fileType 参数说明
     */
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    /**
     * 获取 FilePath。
     * @return 返回值说明
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * 设置 FilePath。
     * @param filePath 参数说明
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * 获取 FileSize。
     * @return 返回值说明
     */
    public Long getFileSize() {
        return fileSize;
    }

    /**
     * 设置 FileSize。
     * @param fileSize 参数说明
     */
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    /**
     * 获取 FileMime。
     * @return 返回值说明
     */
    public String getFileMime() {
        return fileMime;
    }

    /**
     * 设置 FileMime。
     * @param fileMime 参数说明
     */
    public void setFileMime(String fileMime) {
        this.fileMime = fileMime;
    }

    /**
     * 获取 ChunkCount。
     * @return 返回值说明
     */
    public Integer getChunkCount() {
        return chunkCount;
    }

    /**
     * 设置 ChunkCount。
     * @param chunkCount 参数说明
     */
    public void setChunkCount(Integer chunkCount) {
        this.chunkCount = chunkCount;
    }

    /**
     * 获取 Status。
     * @return 返回值说明
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * 设置 Status。
     * @param status 参数说明
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * 获取 CreatedBy。
     * @return 返回值说明
     */
    public Integer getCreatedBy() {
        return createdBy;
    }

    /**
     * 设置 CreatedBy。
     * @param createdBy 参数说明
     */
    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * 获取 CreatedAt。
     * @return 返回值说明
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 设置 CreatedAt。
     * @param createdAt 参数说明
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * 获取 UpdatedAt。
     * @return 返回值说明
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 设置 UpdatedAt。
     * @param updatedAt 参数说明
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
