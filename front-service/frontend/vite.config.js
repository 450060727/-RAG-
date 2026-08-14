/**
 * 用户端（uni-app）Vite 构建配置
 * - 注册 @dcloudio/vite-plugin-uni 插件，支持编译到 H5 / 微信小程序
 * - 处理 alpha 包 CJS 入口未标记 __esModule 的兼容问题
 */
import { defineConfig } from 'vite'
import uniModule from '@dcloudio/vite-plugin-uni'

// 该 alpha 包的 CJS 入口未标记 __esModule，ESM 默认导入拿到的是整个 exports 对象，
// 真正的插件函数挂在 .default 上，做一层兼容
const uni = uniModule.default || uniModule

export default defineConfig({
  // 注册 uni-app 插件
  plugins: [uni()]
})
