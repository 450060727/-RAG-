/**
 * 管理端 Vite 构建配置
 * - 配置 Vue 插件、路径别名 @ -> src
 * - 配置开发服务器端口与 /api 代理
 */
import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'


// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
  // 根据当前 mode 加载环境变量，解决 import.meta.env 在 config 中不可用的问题
  const env = loadEnv(mode, process.cwd(), '')
  return {
    // 注册 Vite 插件
    plugins: [vue()],
    // 路径解析配置
    resolve: {
      // 别名：@ 指向 src 目录，方便业务模块引用
      alias: {
        '@': resolve(__dirname, 'src')
      }
    },
    // 开发服务器配置
    server: {
      // 开发服务器端口
      port: 5174,
      // 接口代理：/api 开头的请求转发到后台服务
      proxy: {
        '/api': {
          // 代理目标，可通过环境变量 VITE_API_BASE_URL 覆盖
          target: env.VITE_API_BASE_URL || 'http://192.168.101.156:8082',
          // 修改请求头 origin，避免后端跨域校验失败
          changeOrigin: true
        }
      }
    }
  }
})
