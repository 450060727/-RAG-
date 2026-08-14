/**
 * 用户端统一请求封装（基于 uni.request）
 * - 集中配置后端 BASE_URL
 * - 自动携带 token，统一处理 401/403/业务错误
 */
// 后端地址集中配置，可通过环境变量注入。
// H5：浏览器跨域由后端 CORS 放行（dev 宽松）。
// 小程序：开发者工具内通过 urlCheck:false 跳过域名校验，直连 localhost。
const BASE_URL = import.meta.env.VITE_API_BASE_URL || '';

/**
 * 封装 uni.request 为 Promise。
 *
 * @param {Object} options 请求参数
 * @param {string} options.url 接口路径（不含 BASE_URL）
 * @param {string} [options.method='GET'] 请求方法
 * @param {Object} [options.data={}] 请求体
 * @param {boolean} [options.auth=true] 是否自动携带 token
 * @returns {Promise<any>} 业务 data
 */
export function request({ url, method = 'GET', data = {}, auth = true }) {
  return new Promise((resolve, reject) => {
    // 默认请求头
    const header = { 'Content-Type': 'application/json' }
    // 读取本地 token
    const token = uni.getStorageSync('token')
    // 需要认证且存在 token 时注入 Authorization 头
    if (auth && token) header['Authorization'] = 'Bearer ' + token

    uni.request({
      url: BASE_URL + url,
      method,
      data,
      header,
      success(res) {
        const body = res.data || {}
        // 401：未登录或 token 过期，清除 token 并跳转登录页
        if (res.statusCode === 401) {
          uni.removeStorageSync('token')
          uni.reLaunch({ url: '/pages/login/login' })
          reject(new Error(body.message || '未登录或登录已过期'))
          return
        }
        // 403：账号被禁用
        if (res.statusCode === 403) {
          uni.removeStorageSync('token')
          uni.reLaunch({ url: '/pages/login/login' })
          reject(new Error(body.message || '账号已被禁用'))
          return
        }
        // 200 且业务 code 为 0 才算成功
        if (res.statusCode === 200 && body.code === 0) {
          resolve(body.data)
        } else {
          reject(new Error(body.message || '请求失败（' + res.statusCode + '）'))
        }
      },
      fail(err) {
        reject(new Error(err.errMsg || '网络异常，请确认后端已启动'))
      }
    })
  })
}
