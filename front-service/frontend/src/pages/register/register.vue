<!-- 用户端注册页 -->
<template>
  <view class="page">
    <view class="card">
      <!-- 页面标题 -->
      <view class="title">注册</view>
      <!-- 邮箱输入 -->
      <input class="input" v-model="email" type="text" placeholder="邮箱" />
      <view class="code-row">
        <!-- 验证码输入 -->
        <input class="input code-input" v-model="code" type="number" placeholder="6 位验证码" />
        <!-- 发送验证码按钮，带倒计时 -->
        <button class="code-btn" :disabled="countdown > 0" @click="sendCode">
          {{ countdown > 0 ? countdown + 's' : '发送验证码' }}
        </button>
      </view>
      <!-- 密码输入 -->
      <input class="input" v-model="password" password placeholder="密码（至少 6 位）" />
      <!-- 姓名输入 -->
      <input class="input" v-model="name" type="text" placeholder="姓名" />
      <!-- 注册按钮 -->
      <button class="btn" :disabled="loading" @click="submit">注 册</button>
      <view class="link" @click="goLogin">已有账号？去登录</view>
    </view>
  </view>
</template>

<script setup>
/**
 * 注册页逻辑
 * - 邮箱验证码注册
 * - 倒计时防止频繁发送验证码
 */
import { ref, onUnmounted } from 'vue'
import { request } from '@/api/request.js'

// 邮箱
const email = ref('')
// 验证码
const code = ref('')
// 密码
const password = ref('')
// 姓名
const name = ref('')
// 注册按钮 loading
const loading = ref(false)
// 验证码倒计时
const countdown = ref(0)
// 倒计时定时器
let timer = null

/**
 * 发送邮箱验证码
 */
async function sendCode() {
  if (!/^\S+@\S+\.\S+$/.test(email.value)) {
    uni.showToast({ title: '邮箱格式不正确', icon: 'none' })
    return
  }
  try {
    await request({ url: '/api/auth/send-code', method: 'POST', data: { email: email.value }, auth: false })
    uni.showToast({ title: '验证码已发送（未配 SMTP 时看后端控制台）', icon: 'none' })
    countdown.value = 60
    timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) clearInterval(timer)
    }, 1000)
  } catch (e) {
    uni.showToast({ title: e.message, icon: 'none' })
  }
}

/**
 * 提交注册
 */
async function submit() {
  if (!email.value || !code.value || !password.value || !name.value) {
    uni.showToast({ title: '请填写完整', icon: 'none' })
    return
  }
  if (password.value.length < 6) {
    uni.showToast({ title: '密码至少 6 位', icon: 'none' })
    return
  }
  loading.value = true
  try {
    await request({
      url: '/api/auth/register',
      method: 'POST',
      data: { email: email.value, code: code.value, password: password.value, name: name.value },
      auth: false
    })
    uni.showToast({ title: '注册成功，请登录', icon: 'success' })
    setTimeout(() => uni.redirectTo({ url: '/pages/login/login' }), 800)
  } catch (e) {
    uni.showToast({ title: e.message, icon: 'none' })
  } finally {
    loading.value = false
  }
}

/**
 * 跳转登录页
 */
function goLogin() {
  uni.redirectTo({ url: '/pages/login/login' })
}

/**
 * 组件卸载时清理定时器，避免内存泄漏
 */
onUnmounted(() => clearInterval(timer))
</script>

<style scoped>
.page { min-height: 100vh; display: flex; justify-content: center; }
.card { width: 100%; max-width: 420px; padding: 40px 24px; }
.title { font-size: 24px; font-weight: 600; margin-bottom: 24px; text-align: center; }
.input { background: #fff; border: 1px solid #e5e5e5; border-radius: 8px; padding: 12px; margin-bottom: 16px; font-size: 15px; }
.code-row { display: flex; gap: 12px; }
.code-input { flex: 1; }
.code-btn { width: 120px; height: 46px; line-height: 46px; font-size: 14px; background: #fff; color: #07c160; border: 1px solid #07c160; border-radius: 8px; padding: 0; }
.btn { background: #07c160; color: #fff; border-radius: 8px; }
.link { margin-top: 16px; text-align: center; color: #576b95; font-size: 14px; }
</style>
