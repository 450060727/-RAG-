<!-- 管理端后台布局组件 -->
<!-- 包含左侧菜单、顶部用户信息、主内容区 -->
<template>
  <el-container class="layout">
    <!-- 左侧侧边栏 -->
    <el-aside width="220px" class="aside">
      <div class="logo">{{ appConfig.logoText }}</div>
      <!-- 侧边菜单：根据当前路由高亮 -->
      <el-menu
        :default-active="$route.path"
        router
        background-color="#001529"
        text-color="#bfcbd9"
        active-text-color="#409eff"
        collapse-transition="false"
      >
        <el-menu-item index="/dashboard">
          <span>首页</span>
        </el-menu-item>
        <!-- 系统管理子菜单 -->
        <el-sub-menu index="/system">
          <template #title>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/system/user">用户管理</el-menu-item>
          <el-menu-item index="/system/dept">部门管理</el-menu-item>
          <el-menu-item index="/system/role">角色管理</el-menu-item>
          <el-menu-item index="/system/menu">菜单管理</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/registered/user">
          <span>注册用户管理</span>
        </el-menu-item>
        <!-- 知识库管理子菜单 -->
        <el-sub-menu index="/kb">
          <template #title>
            <span>知识库管理</span>
          </template>
          <el-menu-item index="/kb/category">分类管理</el-menu-item>
          <el-menu-item index="/kb/document">文档管理</el-menu-item>
          <el-menu-item index="/kb/feedback">反馈审核</el-menu-item>
        </el-sub-menu>

        <!-- 系统配置子菜单 -->
        <el-sub-menu index="/system-config">
          <template #title>
            <span>系统配置</span>
          </template>
          <el-menu-item index="/system-config/model-config">模型配置</el-menu-item>
          <el-menu-item index="/system-config/login-config">登录配置</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <el-container>
      <!-- 顶部 header -->
      <el-header class="header">
        <div class="user-info">
          <!-- 显示用户姓名，无姓名则显示用户名 -->
          <span>{{ userStore.info?.realName || userStore.info?.username }}</span>
          <!-- 用户操作下拉菜单 -->
          <el-dropdown @command="handleCommand">
            <span class="el-dropdown-link">
              <el-icon><Arrow-Down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人资料</el-dropdown-item>
                <el-dropdown-item command="password">修改密码</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主内容区 -->
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
/**
 * 后台布局组件逻辑
 * - 提供侧边菜单导航
 * - 顶部下拉菜单支持个人资料、修改密码、退出登录
 */
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { logout } from '@/api/auth'
import { appConfig } from '@/config/app'

// Vue Router 实例
const router = useRouter()
// 用户状态库实例
const userStore = useUserStore()

/**
 * 处理顶部下拉菜单命令
 * @param {string} cmd 命令标识
 */
function handleCommand(cmd) {
  if (cmd === 'profile') router.push('/profile')
  if (cmd === 'password') router.push('/password')
  if (cmd === 'logout') doLogout()
}

/**
 * 执行退出登录：确认后调用登出接口并清理状态
 */
async function doLogout() {
  await ElMessageBox.confirm('确定退出登录吗？', '提示', { type: 'warning' })
  try {
    await logout()
  } catch (e) {
    // ignore
  }
  userStore.clear()
  ElMessage.success('已退出')
  router.push('/login')
}
</script>

<style scoped>
/* 布局占满视口高度 */
.layout { height: 100vh; }
.aside { background: #001529; color: #fff; }
.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  font-size: 18px;
  font-weight: 600;
  border-bottom: 1px solid rgba(255,255,255,0.1);
}
.header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05);
}
.user-info { display: flex; align-items: center; gap: 8px; cursor: pointer; }
.main { background: #f5f5f5; }
</style>
