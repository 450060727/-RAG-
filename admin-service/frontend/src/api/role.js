/**
 * 管理端角色管理接口
 * 包含角色 CRUD 与角色菜单分配
 */
import request from './request'

/**
 * 获取角色列表
 * @returns {Promise<Array>}
 */
export function getRoles() {
  return request({ url: '/admin/roles', method: 'GET' })
}

/**
 * 根据 ID 获取角色详情
 * @param {number|string} id 角色 ID
 * @returns {Promise<Object>}
 */
export function getRole(id) {
  return request({ url: `/admin/roles/${id}`, method: 'GET' })
}

/**
 * 新增角色
 * @param {Object} data 角色数据
 * @returns {Promise<Object>}
 */
export function createRole(data) {
  return request({ url: '/admin/roles', method: 'POST', data })
}

/**
 * 修改角色
 * @param {number|string} id 角色 ID
 * @param {Object} data 角色数据
 * @returns {Promise<void>}
 */
export function updateRole(id, data) {
  return request({ url: `/admin/roles/${id}`, method: 'PUT', data })
}

/**
 * 删除角色
 * @param {number|string} id 角色 ID
 * @returns {Promise<void>}
 */
export function deleteRole(id) {
  return request({ url: `/admin/roles/${id}`, method: 'DELETE' })
}

/**
 * 获取角色已分配的菜单 ID 列表
 * @param {number|string} id 角色 ID
 * @returns {Promise<Array<number>>}
 */
export function getRoleMenus(id) {
  return request({ url: `/admin/roles/${id}/menus`, method: 'GET' })
}

/**
 * 为角色分配菜单
 * @param {number|string} id 角色 ID
 * @param {Object} data 菜单 ID 集合，如 { menuIds: [] }
 * @returns {Promise<void>}
 */
export function assignRoleMenus(id, data) {
  return request({ url: `/admin/roles/${id}/menus`, method: 'PUT', data })
}
