/**
 * 管理端模型与登录配置接口
 * 包含全局配置读写、分类级配置读写、缓存刷新
 */
import request from './request'

/**
 * 获取全局模型/登录配置
 * @returns {Promise<Object>}
 */
export function getModelConfig() {
  return request({ url: '/admin/kb/model-config', method: 'GET' })
}

/**
 * 更新全局模型/登录配置
 * @param {Object} data 配置数据
 * @returns {Promise<void>}
 */
export function updateModelConfig(data) {
  return request({ url: '/admin/kb/model-config', method: 'PUT', data })
}

/**
 * 获取指定分类的模型配置
 * @param {number|string} categoryId 分类 ID
 * @returns {Promise<Object>}
 */
export function getCategoryModelConfig(categoryId) {
  return request({ url: `/admin/kb/model-config/category/${categoryId}`, method: 'GET' })
}

/**
 * 更新指定分类的模型配置
 * @param {number|string} categoryId 分类 ID
 * @param {Object} data 配置数据
 * @returns {Promise<void>}
 */
export function updateCategoryModelConfig(categoryId, data) {
  return request({ url: `/admin/kb/model-config/category/${categoryId}`, method: 'PUT', data })
}

/**
 * 重置指定分类的模型配置为全局默认
 * @param {number|string} categoryId 分类 ID
 * @returns {Promise<void>}
 */
export function resetCategoryModelConfig(categoryId) {
  return request({ url: `/admin/kb/model-config/category/${categoryId}`, method: 'DELETE' })
}

/**
 * 刷新模型配置缓存
 * @param {number|string} [categoryId] 可选分类 ID，不传则刷新全局缓存
 * @returns {Promise<void>}
 */
export function refreshModelConfigCache(categoryId) {
  const url = categoryId
    ? `/admin/kb/model-config/category/${categoryId}/refresh-cache`
    : '/admin/kb/model-config/refresh-cache'
  return request({ url, method: 'POST' })
}
