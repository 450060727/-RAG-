/**
 * 管理端部门管理接口
 * 包含部门树查询、单条查询、新增、修改、删除
 */
import request from './request'

/**
 * 获取部门树
 * @returns {Promise<Array>} 部门树列表
 */
export function getDeptTree() {
  return request({ url: '/admin/depts', method: 'GET' })
}

/**
 * 根据 ID 获取部门详情
 * @param {number|string} id 部门 ID
 * @returns {Promise<Object>}
 */
export function getDept(id) {
  return request({ url: `/admin/depts/${id}`, method: 'GET' })
}

/**
 * 新增部门
 * @param {Object} data 部门数据
 * @returns {Promise<Object>}
 */
export function createDept(data) {
  return request({ url: '/admin/depts', method: 'POST', data })
}

/**
 * 修改部门
 * @param {number|string} id 部门 ID
 * @param {Object} data 部门数据
 * @returns {Promise<void>}
 */
export function updateDept(id, data) {
  return request({ url: `/admin/depts/${id}`, method: 'PUT', data })
}

/**
 * 删除部门
 * @param {number|string} id 部门 ID
 * @returns {Promise<void>}
 */
export function deleteDept(id) {
  return request({ url: `/admin/depts/${id}`, method: 'DELETE' })
}
