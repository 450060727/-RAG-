/**
 * 管理端 Vue Router 配置
 * - 定义登录页与后台布局子路由
 * - 全局前置守卫处理未登录跳转与用户信息预取
 */
// 管理端 Vue Router 配置
import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

// 路由表：/login 公开，其余均需登录
const routes = [
  {
    path: '/login',
    component: () => import('@/views/login/index.vue'),
    meta: { public: true }
  },
  {
    path: '/',
    component: () => import('@/views/layout/index.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', component: () => import('@/views/dashboard/index.vue'), meta: { title: '首页' } },
      { path: 'system/user', component: () => import('@/views/system/user/index.vue'), meta: { title: '用户管理' } },
      { path: 'system/dept', component: () => import('@/views/system/dept/index.vue'), meta: { title: '部门管理' } },
      { path: 'system/role', component: () => import('@/views/system/role/index.vue'), meta: { title: '角色管理' } },
      { path: 'system/menu', component: () => import('@/views/system/menu/index.vue'), meta: { title: '菜单管理' } },
      { path: 'registered/user', component: () => import('@/views/registered/user/index.vue'), meta: { title: '注册用户管理' } },
      { path: 'kb/category', component: () => import('@/views/kb/category/index.vue'), meta: { title: '知识库分类' } },
      { path: 'kb/document', component: () => import('@/views/kb/document/index.vue'), meta: { title: '知识库文档' } },
      { path: 'kb/feedback', component: () => import('@/views/kb/feedback/index.vue'), meta: { title: '反馈审核' } },
      { path: 'system-config/model-config', component: () => import('@/views/kb/model-config/index.vue'), props: { mode: 'model' }, meta: { title: '模型配置' } },
      { path: 'system-config/login-config', component: () => import('@/views/kb/model-config/index.vue'), props: { mode: 'login' }, meta: { title: '登录配置' } },
      { path: 'profile', component: () => import('@/views/profile/index.vue'), meta: { title: '个人资料' } },
      { path: 'password', component: () => import('@/views/password/index.vue'), meta: { title: '修改密码' } }
    ]
  }
]

// 创建 router 实例，使用 HTML5 History 模式
const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局路由守卫：未登录跳转登录页，首次进入获取用户信息
router.beforeEach(async (to, from, next) => {
  // 从本地存储读取 token
  const token = localStorage.getItem('adminToken')
  // 公开路由直接放行
  if (to.meta?.public) {
    next()
    return
  }
  // 无 token 强制跳转登录
  if (!token) {
    next('/login')
    return
  }
  const userStore = useUserStore()
  // 未加载过用户信息时预取
  if (!userStore.info) {
    try {
      await userStore.fetchInfo()
    } catch (e) {
      next('/login')
      return
    }
  }
  next()
})

export default router
