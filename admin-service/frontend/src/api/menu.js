/**
 * 管理端菜单管理接口
 * 包含菜单树查询、单条查询、新增、修改、删除
 */
import request from './request'

/**
 * 获取菜单树
 * @returns {Promise<Array>} 菜单树列表
 */
export function getMenuTree() {
  return request({ url: '/admin/menus', method: 'GET' })
}

/**
 * 根据 ID 获取菜单详情
 * @param {number|string} id 菜单 ID
 * @returns {Promise<Object>}
 */
export function getMenu(id) {
  return request({ url: `/admin/menus/${id}`, method: 'GET' })
}

/**
 * 新增菜单
 * @param {Object} data 菜单数据
 * @returns {Promise<Object>}
 */
export function createMenu(data) {
  return request({ url: '/admin/menus', method: 'POST', data })
}

/**
 * 修改菜单
 * @param {number|string} id 菜单 ID
 * @param {Object} data 菜单数据
 * @returns {Promise<void>}
 */
export function updateMenu(id, data) {
  return request({ url: `/admin/menus/${id}`, method: 'PUT', data })
}

/**
 * 删除菜单
 * @param {number|string} id 菜单 ID
 * @returns {Promise<void>}
 */
export function deleteMenu(id) {
  return request({ url: `/admin/menus/${id}`, method: 'DELETE' })
}
