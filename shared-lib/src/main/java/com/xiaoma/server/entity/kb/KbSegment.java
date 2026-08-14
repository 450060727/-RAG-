/**
 * KbSegment。
 */
package com.xiaoma.server.entity.kb;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * KbSegment 实体。
 * 本类定义了 KbSegment 的公共契约与数据结构。
 */
@TableName("kb_segment")
public class KbSegment {

    @TableId(type = IdType.AUTO)
    private Integer id; // id 字段
    private Integer docId; // docId 字段
    private Integer categoryId; // categoryId 字段
    private String content; // content 字段
    private String vectorId; // vectorId 字段
    private Integer sortOrder; // sortOrder 字段
    private Integer status; // status 字段
    private LocalDateTime createdAt; // createdAt 字段

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
     * 获取 DocId。
     * @return 返回值说明
     */
    public Integer getDocId() {
        return docId;
    }

    /**
     * 设置 DocId。
     * @param docId 参数说明
     */
    public void setDocId(Integer docId) {
        this.docId = docId;
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
     * 获取 Content。
     * @return 返回值说明
     */
    public String getContent() {
        return content;
    }

    /**
     * 设置 Content。
     * @param content 参数说明
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * 获取 VectorId。
     * @return 返回值说明
     */
    public String getVectorId() {
        return vectorId;
    }

    /**
     * 设置 VectorId。
     * @param vectorId 参数说明
     */
    public void setVectorId(String vectorId) {
        this.vectorId = vectorId;
    }

    /**
     * 获取 SortOrder。
     * @return 返回值说明
     */
    public Integer getSortOrder() {
        return sortOrder;
    }

    /**
     * 设置 SortOrder。
     * @param sortOrder 参数说明
     */
    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
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
}
