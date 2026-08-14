/**
 * 管理端知识库管理接口
 * 包含分类管理、文档上传/文本录入/下载/删除、反馈审核
 */
import request from './request'
import axios from 'axios'

/**
 * 获取知识库分类列表
 * @returns {Promise<Array>}
 */
export function getKbCategories() {
  return request({ url: '/admin/kb/categories', method: 'GET' })
}

/**
 * 获取知识库分类树
 * @returns {Promise<Array>}
 */
export function getKbCategoryTree() {
  return request({ url: '/admin/kb/categories/tree', method: 'GET' })
}

/**
 * 新增知识库分类
 * @param {Object} data 分类数据
 * @returns {Promise<Object>}
 */
export function createKbCategory(data) {
  return request({ url: '/admin/kb/categories', method: 'POST', data })
}

/**
 * 修改知识库分类
 * @param {number|string} id 分类 ID
 * @param {Object} data 分类数据
 * @returns {Promise<void>}
 */
export function updateKbCategory(id, data) {
  return request({ url: `/admin/kb/categories/${id}`, method: 'PUT', data })
}

/**
 * 删除知识库分类
 * @param {number|string} id 分类 ID
 * @returns {Promise<void>}
 */
export function deleteKbCategory(id) {
  return request({ url: `/admin/kb/categories/${id}`, method: 'DELETE' })
}

/**
 * 分页查询知识库文档
 * @param {Object} params 查询参数
 * @param {number} [params.categoryId] 分类 ID
 * @param {string} [params.keyword] 标题关键词
 * @param {string} [params.sourceType] 来源类型
 * @param {number} [params.page] 页码
 * @param {number} [params.size] 每页条数
 * @returns {Promise<{records: Array, total: number}>}
 */
export function getKbDocuments(params) {
  return request({ url: '/admin/kb/documents', method: 'GET', params })
}

/**
 * 上传知识库文档
 * @param {FormData} data 包含 categoryId 与 file 的表单数据
 * @returns {Promise<Object>}
 */
export function uploadKbDocument(data) {
  return request({
    url: '/admin/kb/documents/upload',
    method: 'POST',
    data,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/**
 * 保存手动输入的文本文档
 * @param {Object} data 文档数据
 * @param {number} data.categoryId 分类 ID
 * @param {string} data.title 标题
 * @param {string} data.content 内容
 * @returns {Promise<Object>}
 */
export function saveKbText(data) {
  return request({ url: '/admin/kb/documents/text', method: 'POST', data })
}

/**
 * 删除知识库文档
 * @param {number|string} id 文档 ID
 * @returns {Promise<void>}
 */
export function deleteKbDocument(id) {
  return request({ url: `/admin/kb/documents/${id}`, method: 'DELETE' })
}

/**
 * 获取文档文本内容
 * @param {number|string} id 文档 ID
 * @returns {Promise<string>}
 */
export function getKbDocumentContent(id) {
  return request({ url: `/admin/kb/documents/${id}/content`, method: 'GET' })
}

/**
 * 下载知识库文档
 * @param {number|string} id 文档 ID
 * @returns {Promise<BlobResponse>} 返回 blob 数据
 */
export function downloadKbDocument(id) {
  return axios({
    url: `/api/admin/kb/documents/${id}/download`,
    method: 'GET',
    responseType: 'blob',
    headers: {
      Authorization: 'Bearer ' + localStorage.getItem('adminToken')
    }
  })
}

/**
 * 分页查询用户反馈
 * @param {Object} params 查询参数
 * @returns {Promise<{records: Array, total: number}>}
 */
export function getKbFeedback(params) {
  return request({ url: '/admin/kb/feedback', method: 'GET', params })
}

/**
 * 批准反馈（加入知识库）
 * @param {number|string} id 反馈 ID
 * @returns {Promise<void>}
 */
export function approveKbFeedback(id) {
  return request({ url: `/admin/kb/feedback/${id}/approve`, method: 'PUT' })
}

/**
 * 驳回反馈
 * @param {number|string} id 反馈 ID
 * @returns {Promise<void>}
 */
export function rejectKbFeedback(id) {
  return request({ url: `/admin/kb/feedback/${id}/reject`, method: 'PUT' })
}
