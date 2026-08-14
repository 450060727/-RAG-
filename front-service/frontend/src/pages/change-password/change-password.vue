<!-- 用户端修改密码页 -->
<template>
  <view class="page">
    <view class="card">
      <!-- 页面标题 -->
      <view class="title">修改密码</view>
      <!-- 原密码输入 -->
      <input class="input" v-model="oldPassword" password placeholder="原密码" />
      <!-- 新密码输入 -->
      <input class="input" v-model="newPassword" password placeholder="新密码（至少 6 位）" />
      <!-- 确认新密码 -->
      <input class="input" v-model="confirmPassword" password placeholder="确认新密码" />
      <!-- 确认修改按钮 -->
      <button class="btn" :disabled="loading" @click="submit">确 认 修 改</button>
    </view>
  </view>
</template>

<script setup>
/**
 * 修改密码页逻辑
 * - 校验原密码、新密码一致性
 * - 成功后清除 token 并重新登录
 */
import { ref } from 'vue'
import { request } from '@/api/request.js'

// 原密码
const oldPassword = ref('')
// 新密码
const newPassword = ref('')
// 确认新密码
const confirmPassword = ref('')
// 提交按钮 loading
const loading = ref(false)

/**
 * 提交修改密码
 */
async function submit() {
  if (!oldPassword.value || !newPassword.value || !confirmPassword.value) {
    uni.showToast({ title: '请填写完整', icon: 'none' })
    return
  }
  if (newPassword.value.length < 6) {
    uni.showToast({ title: '新密码至少 6 位', icon: 'none' })
    return
  }
  if (newPassword.value !== confirmPassword.value) {
    uni.showToast({ title: '两次输入的新密码不一致', icon: 'none' })
    return
  }
  loading.value = true
  try {
    await request({
      url: '/api/user/password',
      method: 'PUT',
      data: { oldPassword: oldPassword.value, newPassword: newPassword.value }
    })
    uni.removeStorageSync('token')
    uni.showToast({ title: '修改成功，请重新登录', icon: 'success' })
    setTimeout(() => uni.reLaunch({ url: '/pages/login/login' }), 800)
  } catch (e) {
    uni.showToast({ title: e.message, icon: 'none' })
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.page { min-height: 100vh; display: flex; justify-content: center; }
.card { width: 100%; max-width: 420px; padding: 40px 24px; }
.title { font-size: 24px; font-weight: 600; margin-bottom: 24px; text-align: center; }
.input { background: #fff; border: 1px solid #e5e5e5; border-radius: 8px; padding: 12px; margin-bottom: 16px; font-size: 15px; }
.btn { background: #07c160; color: #fff; border-radius: 8px; }
</style>
