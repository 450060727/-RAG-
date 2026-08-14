/**
 * 用户端（uni-app）入口文件
 * - 创建 SSR App 实例并返回，供 uni-app 框架接管渲染
 */
import { createSSRApp } from 'vue'
import App from './App.vue'

/**
 * 创建应用实例
 * @returns {{app: import('vue').App}} Vue 应用实例
 */
export function createApp() {
  const app = createSSRApp(App)
  return { app }
}
