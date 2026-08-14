/**
 * 管理端注册用户管理接口
 * 包含分页查询、状态变更
 */
import request from './request'

/**
 * 分页查询前端注册用户
 * @param {Object} params 查询参数
 * @param {string} [params.keyword] 关键词（邮箱/昵称）
 * @param {number} [params.status] 状态
 * @param {number} [params.page] 页码
 * @param {number} [params.size] 每页条数
 * @returns {Promise<{records: Array, total: number}>}
 */
export function getRegisteredUsers(params) {
  return request({ url: '/admin/registered-users', method: 'GET', params })
}

/**
 * 变更前端注册用户状态
 * @param {number|string} id 用户 ID
 * @param {number} status 状态 0 正常 / 1 禁用
 * @returns {Promise<void>}
 */
export function changeRegisteredUserStatus(id, status) {
  return request({ url: `/admin/registered-users/${id}/status`, method: 'PUT', data: { status } })
}
