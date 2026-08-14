<!-- 管理端登录页 -->
<template>
  <!-- 登录页根容器：居中布局 -->
  <div class="login-page">
    <!-- 登录卡片 -->
    <div class="login-box">
      <!-- 系统标题 -->
      <h2>{{ appConfig.loginTitle }}</h2>
      <!-- 登录表单：支持回车提交 -->
      <el-form :model="form" @keyup.enter="submit">
        <el-form-item>
          <!-- 用户名输入框 -->
          <el-input v-model="form.username" placeholder="用户名" size="large" :prefix-icon="User" />
        </el-form-item>
        <el-form-item>
          <!-- 密码输入框，支持显示/隐藏密码 -->
          <el-input v-model="form.password" type="password" placeholder="密码" size="large" :prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item>
          <!-- 登录按钮，宽度占满表单 -->
          <el-button type="primary" size="large" :loading="loading" style="width: 100%" @click="submit">登录</el-button>
        </el-form-item>
      </el-form>
      <!-- 默认账号提示 -->
      <p class="tip">默认账号：admin / admin123</p>
    </div>
  </div>
</template>

<script setup>
/**
 * 登录页逻辑
 * - 收集用户名/密码
 * - 调用登录接口并保存 token
 * - 成功后跳转首页
 */
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { login } from '@/api/auth'
import { appConfig } from '@/config/app'

// Vue Router 实例
const router = useRouter()
// 登录按钮 loading 状态
const loading = ref(false)
// 登录表单数据
const form = reactive({ username: '', password: '' })

/**
 * 提交登录
 */
async function submit() {
  // 校验必填
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    // 调用登录接口
    const data = await login(form)
    // 保存 token 到本地存储
    localStorage.setItem('adminToken', data.token)
    ElMessage.success('登录成功')
    // 跳转首页
    router.push('/')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* 登录页全屏居中背景 */
.login-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
}
/* 登录卡片样式 */
.login-box {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}
h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}
.tip {
  text-align: center;
  color: #999;
  font-size: 13px;
  margin-top: 10px;
}
</style>
