/**
 * 管理端系统用户管理接口
 * 包含分页查询、单条查询、新增、修改、状态变更、重置密码、删除
 */
import request from './request'

/**
 * 分页查询系统用户
 * @param {Object} params 查询参数
 * @param {string} [params.keyword] 关键词（用户名/姓名）
 * @param {number} [params.status] 状态
 * @param {number} [params.page] 页码
 * @param {number} [params.size] 每页条数
 * @returns {Promise<{records: Array, total: number}>}
 */
export function getUsers(params) {
  return request({ url: '/admin/users', method: 'GET', params })
}

/**
 * 根据 ID 获取系统用户详情
 * @param {number|string} id 用户 ID
 * @returns {Promise<Object>}
 */
export function getUser(id) {
  return request({ url: `/admin/users/${id}`, method: 'GET' })
}

/**
 * 新增系统用户
 * @param {Object} data 用户数据
 * @returns {Promise<{initialPassword: string}>} 返回初始密码
 */
export function createUser(data) {
  return request({ url: '/admin/users', method: 'POST', data })
}

/**
 * 修改系统用户
 * @param {number|string} id 用户 ID
 * @param {Object} data 用户数据
 * @returns {Promise<void>}
 */
export function updateUser(id, data) {
  return request({ url: `/admin/users/${id}`, method: 'PUT', data })
}

/**
 * 修改用户状态
 * @param {number|string} id 用户 ID
 * @param {number} status 状态 0 正常 / 1 禁用
 * @returns {Promise<void>}
 */
export function changeUserStatus(id, status) {
  return request({ url: `/admin/users/${id}/status`, method: 'PUT', data: { status } })
}

/**
 * 重置用户密码
 * @param {number|string} id 用户 ID
 * @returns {Promise<void>}
 */
export function resetUserPassword(id) {
  return request({ url: `/admin/users/${id}/password`, method: 'PUT' })
}

/**
 * 删除系统用户
 * @param {number|string} id 用户 ID
 * @returns {Promise<void>}
 */
export function deleteUser(id) {
  return request({ url: `/admin/users/${id}`, method: 'DELETE' })
}
