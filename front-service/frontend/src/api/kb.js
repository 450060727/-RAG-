/**
 * 用户端知识库/问答相关接口
 * 包含对话、反馈、分类查询
 */
import { request } from './request.js'

/**
 * 发送对话问题
 * @param {Object} data 请求参数
 * @param {number} data.categoryId 知识库分类 ID
 * @param {string} data.question 用户问题
 * @param {string} [data.sessionId] 会话 ID
 * @returns {Promise<{answer: string, sessionId: string, messageId: string, sources: Array}>}
 */
export function chat(data) {
  return request({ url: '/api/kb/chat', method: 'POST', data })
}

/**
 * 提交对某条回答的反馈
 * @param {string} messageId 消息 ID
 * @param {Object} data 反馈数据
 * @param {string} data.feedback 反馈类型 up/down
 * @param {boolean} data.writeBack 是否回写知识库
 * @returns {Promise<void>}
 */
export function chatFeedback(messageId, data) {
  return request({ url: `/api/kb/chat/${messageId}/feedback`, method: 'POST', data })
}

/**
 * 获取知识库分类树
 * @returns {Promise<Array>}
 */
export function getKbCategories() {
  return request({ url: '/api/kb/categories/tree', method: 'GET' })
}
