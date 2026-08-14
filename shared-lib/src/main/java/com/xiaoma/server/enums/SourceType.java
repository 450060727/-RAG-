/**
 * enums 模块的 SourceType 类/接口定义。
 */
package com.xiaoma.server.enums;

/**
 * 知识库文档来源类型。
 */
public enum SourceType {

    /** 纯文本录入 */
    TEXT("TEXT"),
    /** 文件上传 */
    UPLOAD("UPLOAD"),
    /** 问答对 */
    QA_PAIR("QA_PAIR");

    private final String code; // code 字段

    SourceType(String code) {
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
     * 根据编码解析来源类型。
     *
     * @param code 编码，可能为 null
     * @return 对应的 SourceType，未找到时返回 TEXT
     */
    public static SourceType of(String code) {
        if (code == null || code.isBlank()) {
            return TEXT;
        }
        for (SourceType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return TEXT;
    }
}
