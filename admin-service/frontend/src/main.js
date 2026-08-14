/**
 * 管理端入口文件
 * - 创建 Vue 应用实例
 * - 依次注册 Pinia、Vue Router、Element Plus
 * - 挂载到 #app
 */
import { createApp } from 'vue'
// Element Plus 组件库
import ElementPlus from 'element-plus'
// Element Plus 默认样式
import 'element-plus/dist/index.css'
// 自定义科技感主题样式
import './styles/tech-theme.css'
// Pinia 状态管理
import { createPinia } from 'pinia'
// 根组件
import App from './App.vue'
// 路由配置
import router from './router'

// 创建 Vue 应用实例
const app = createApp(App)
// 注册 Pinia 状态管理
app.use(createPinia())
// 注册 Vue Router
app.use(router)
// 注册 Element Plus UI 库
app.use(ElementPlus)
// 挂载到 DOM 节点 #app
app.mount('#app')
