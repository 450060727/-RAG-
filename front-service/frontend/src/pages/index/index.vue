<!-- 用户端首页 -->
<!-- 提供登录、注册入口引导 -->
<template>
  <view class="page">
    <view class="card">
      <!-- 应用标题 -->
      <view class="title">{{ appConfig.title }}</view>
      <!-- 应用描述 -->
      <view class="desc">{{ appConfig.description }}</view>
      <!-- 登录按钮 -->
      <button class="btn" @click="go('/pages/login/login')">登 录</button>
      <!-- 注册按钮 -->
      <button class="btn plain" @click="go('/pages/register/register')">注 册</button>
      <view class="tip">点击底部「我的」tab 查看与编辑个人资料</view>
    </view>
  </view>
</template>

<script setup>
import { appConfig } from '@/config/app'

/**
 * 首页逻辑
 * - 跳转到登录或注册页面
 * @param {string} url 页面路径
 */
function go(url) {
  uni.navigateTo({ url })
}
</script>

<style scoped>
/* 页面容器：垂直居中 */
.page { min-height: 100vh; display: flex; justify-content: center; }
/* 卡片容器 */
.card { width: 100%; max-width: 420px; padding: 48px 24px; text-align: center; }
.title { font-size: 28px; font-weight: 700; margin-bottom: 12px; }
.desc { color: #888; font-size: 14px; margin-bottom: 32px; }
/* 主按钮 */
.btn { background: #07c160; color: #fff; border-radius: 8px; margin-bottom: 12px; }
/* 镂空按钮 */
.btn.plain { background: #fff; color: #07c160; border: 1px solid #07c160; }
.tip { margin-top: 24px; color: #aaa; font-size: 13px; }
</style>
