/**
 * 管理端统一 Axios 请求封装
 * - 统一 baseURL、超时、请求/响应拦截
 * - 业务错误统一弹窗，401/403 自动跳转登录
 */
// 管理端统一 Axios 请求封装
import axios from 'axios'
import { ElMessage } from 'element-plus'

/**
 * 创建 axios 实例：
 * - baseURL 统一为 /api，由 vite 代理转发到后台服务
 * - timeout 10 秒
 */
const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器：自动注入 adminToken
request.interceptors.request.use(config => {
  // 从本地存储读取管理员 token
  const token = localStorage.getItem('adminToken')
  if (token) {
    // 在请求头中附加 Bearer token
    config.headers.Authorization = 'Bearer ' + token
  }
  return config
})

// 响应拦截器：统一处理业务码与 401/403 跳转
request.interceptors.response.use(
  response => {
    const body = response.data
    // 业务约定：code !== 0 表示业务错误
    if (body.code !== 0) {
      ElMessage.error(body.message || '请求失败')
      return Promise.reject(new Error(body.message || '请求失败'))
    }
    // 直接返回 data，业务层无需再解包
    return body.data
  },
  error => {
    const status = error.response?.status
    const message = error.response?.data?.message || error.message || '网络异常'
    // 401/403 统一清除 token 并跳转登录页
    if (status === 401 || status === 403) {
      localStorage.removeItem('adminToken')
      window.location.href = '/login'
    }
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default request
