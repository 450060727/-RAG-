<!-- 用户端个人资料页 -->
<template>
  <view class="page">
    <!-- 未登录状态 -->
    <view class="card" v-if="!loggedIn">
      <view class="title">未登录</view>
      <button class="btn" @click="go('/pages/login/login')">去登录</button>
      <button class="btn plain" @click="go('/pages/register/register')">去注册</button>
    </view>

    <!-- 已登录状态 -->
    <view class="card" v-else>
      <view class="title">我的资料</view>
      <view class="label">邮箱</view>
      <!-- 邮箱不可编辑 -->
      <input class="input disabled" v-model="profile.email" disabled />
      <view class="label">姓名</view>
      <input class="input" v-model="profile.name" placeholder="请输入姓名" />
      <!-- 保存资料按钮 -->
      <button class="btn" :disabled="saving" @click="save">保 存</button>
      <!-- 修改密码按钮 -->
      <button class="btn plain" @click="goChangePassword">修改密码</button>
      <!-- 退出登录按钮 -->
      <button class="btn plain" @click="logout">退出登录</button>
    </view>
  </view>
</template>

<script setup>
/**
 * 个人资料页逻辑
 * - tab 页每次显示时校验登录态并加载资料
 * - 支持修改姓名、修改密码、退出登录
 */
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { request } from '@/api/request.js'

// 是否已登录
const loggedIn = ref(false)
// 保存按钮 loading
const saving = ref(false)
// 用户资料
const profile = ref({ email: '', name: '' })

/**
 * tab 页每次显示时刷新登录态与资料
 */
onShow(() => {
  const token = uni.getStorageSync('token')
  loggedIn.value = !!token
  if (token) loadProfile()
})

/**
 * 加载用户资料
 */
async function loadProfile() {
  try {
    profile.value = await request({ url: '/api/user/profile' })
  } catch (e) {
    uni.showToast({ title: e.message, icon: 'none' })
  }
}

/**
 * 保存用户资料
 */
async function save() {
  if (!profile.value.name) {
    uni.showToast({ title: '姓名不能为空', icon: 'none' })
    return
  }
  saving.value = true
  try {
    profile.value = await request({
      url: '/api/user/profile',
      method: 'PUT',
      data: { name: profile.value.name }
    })
    uni.showToast({ title: '保存成功', icon: 'success' })
  } catch (e) {
    uni.showToast({ title: e.message, icon: 'none' })
  } finally {
    saving.value = false
  }
}

/**
 * 退出登录
 */
async function logout() {
  try {
    await request({ url: '/api/auth/logout', method: 'POST' })
  } catch (e) {
    // 忽略网络错误，仍清除本地 token
  }
  uni.removeStorageSync('token')
  loggedIn.value = false
  uni.showToast({ title: '已退出登录', icon: 'none' })
}

/**
 * 跳转到普通页面
 * @param {string} url 页面路径
 */
function go(url) {
  uni.navigateTo({ url })
}

/**
 * 跳转修改密码页
 */
function goChangePassword() {
  uni.navigateTo({ url: '/pages/change-password/change-password' })
}
</script>

<style scoped>
.page { min-height: 100vh; display: flex; justify-content: center; }
.card { width: 100%; max-width: 420px; padding: 40px 24px; }
.title { font-size: 24px; font-weight: 600; margin-bottom: 24px; text-align: center; }
.label { font-size: 13px; color: #888; margin-bottom: 6px; }
.input { background: #fff; border: 1px solid #e5e5e5; border-radius: 8px; padding: 12px; margin-bottom: 16px; font-size: 15px; }
.input.disabled { background: #f0f0f0; color: #999; }
.btn { background: #07c160; color: #fff; border-radius: 8px; margin-bottom: 12px; }
.btn.plain { background: #fff; color: #e64340; border: 1px solid #e64340; }
</style>
