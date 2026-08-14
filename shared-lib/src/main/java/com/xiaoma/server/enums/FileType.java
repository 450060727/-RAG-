/**
 * enums 模块的 FileType 类/接口定义。
 */
package com.xiaoma.server.enums;

/**
 * 上传文件类型。
 */
public enum FileType {

    /** 文本 */
    TEXT("text"),
    /** PDF */
    PDF("pdf"),
    /** Word 文档 */
    DOCX("docx"),
    /** DOC 文档 */
    DOC("doc"),
    /** 图片 */
    IMAGE("image"),
    /** 音频 */
    AUDIO("audio"),
    /** 视频 */
    VIDEO("video"),
    /** 问答对 */
    QA("qa"),
    /** 未知类型 */
    UNKNOWN("unknown");

    private final String code; // code 字段

    FileType(String code) {
        this.code = code;
    }

    /**
     * 获取 Code。
     * @return 返回值说明
     */
    public String getCode() {
        return code;
    }

    /**
     * 根据编码解析文件类型。
     *
     * @param code 编码，可能为 null
     * @return 对应的 FileType，未找到时返回 UNKNOWN
     */
    public static FileType of(String code) {
        if (code == null || code.isBlank()) {
            return UNKNOWN;
        }
        for (FileType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return UNKNOWN;
    }
}
