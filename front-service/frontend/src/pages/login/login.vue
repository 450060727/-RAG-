<!-- 用户端登录页 -->
<template>
  <view class="page">
    <view class="card">
      <!-- 页面标题 -->
      <view class="title">登录</view>
      <!-- 邮箱输入 -->
      <input class="input" v-model="email" type="text" placeholder="邮箱" />
      <!-- 密码输入 -->
      <input class="input" v-model="password" password placeholder="密码" />
      <!-- 登录按钮 -->
      <button class="btn" :disabled="loading" @click="submit">登 录</button>
      <view class="link" @click="goRegister">没有账号？去注册</view>
      <view class="link" @click="goResetPassword">忘记密码？</view>
    </view>
  </view>
</template>

<script setup>
/**
 * 登录页逻辑
 * - 邮箱 + 密码登录
 * - 保存 token 后跳转到 profile tab
 */
import { ref } from 'vue'
import { request } from '@/api/request.js'

// 邮箱响应式变量
const email = ref('')
// 密码响应式变量
const password = ref('')
// 登录按钮 loading 状态
const loading = ref(false)

/**
 * 提交登录
 */
async function submit() {
  if (!email.value || !password.value) {
    uni.showToast({ title: '请输入邮箱和密码', icon: 'none' })
    return
  }
  loading.value = true
  try {
    const data = await request({
      url: '/api/auth/login',
      method: 'POST',
      data: { email: email.value, password: password.value },
      auth: false // 登录接口不需要 token
    })
    // 保存 token 并跳转到 profile tab 页
    uni.setStorageSync('token', data.token)
    uni.showToast({ title: '登录成功', icon: 'success' })
    // tab 页必须用 switchTab 跳转
    setTimeout(() => uni.switchTab({ url: '/pages/profile/profile' }), 500)
  } catch (e) {
    uni.showToast({ title: e.message, icon: 'none' })
  } finally {
    loading.value = false
  }
}

/**
 * 跳转注册页
 */
function goRegister() {
  uni.navigateTo({ url: '/pages/register/register' })
}

/**
 * 跳转重置密码页
 */
function goResetPassword() {
  uni.navigateTo({ url: '/pages/reset-password/reset-password' })
}
</script>

<style scoped>
.page { min-height: 100vh; display: flex; justify-content: center; }
.card { width: 100%; max-width: 420px; padding: 40px 24px; }
.title { font-size: 24px; font-weight: 600; margin-bottom: 24px; text-align: center; }
.input { background: #fff; border: 1px solid #e5e5e5; border-radius: 8px; padding: 12px; margin-bottom: 16px; font-size: 15px; }
.btn { background: #07c160; color: #fff; border-radius: 8px; }
.link { margin-top: 16px; text-align: center; color: #576b95; font-size: 14px; }
</style>
