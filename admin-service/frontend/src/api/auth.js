/**
 * 管理端认证相关接口
 * 包含登录、登出、修改密码、获取当前用户、获取用户菜单
 */
import request from './request'

/**
 * 管理员登录
 * @param {Object} data 登录参数
 * @param {string} data.username 用户名
 * @param {string} data.password 密码
 * @returns {Promise<{token: string}>} 登录成功返回 token
 */
export function login(data) {
  return request({ url: '/admin/auth/login', method: 'POST', data })
}

/**
 * 管理员登出
 * @returns {Promise<void>}
 */
export function logout() {
  return request({ url: '/admin/auth/logout', method: 'POST' })
}

/**
 * 修改当前管理员密码
 * @param {Object} data 密码参数
 * @param {string} data.oldPassword 原密码
 * @param {string} data.newPassword 新密码
 * @returns {Promise<void>}
 */
export function changePassword(data) {
  return request({ url: '/admin/auth/password', method: 'PUT', data })
}

/**
 * 获取当前登录管理员信息
 * @returns {Promise<Object>} 用户信息、菜单、权限
 */
export function getMe() {
  return request({ url: '/admin/auth/me', method: 'GET' })
}

/**
 * 获取当前管理员可访问的菜单树
 * @returns {Promise<Array>} 菜单树
 */
export function getMenus() {
  return request({ url: '/admin/auth/menus', method: 'GET' })
}
