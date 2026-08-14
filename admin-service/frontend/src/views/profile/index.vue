<!-- 管理端个人资料页 -->
<template>
  <div>
    <!-- 页面标题 -->
    <h2>个人资料</h2>
    <!-- 用户信息描述列表：两列布局 -->
    <el-descriptions :column="2" border v-if="userStore.info">
      <el-descriptions-item label="用户名">{{ userStore.info.username }}</el-descriptions-item>
      <el-descriptions-item label="姓名">{{ userStore.info.realName }}</el-descriptions-item>
      <el-descriptions-item label="手机号">{{ userStore.info.phone }}</el-descriptions-item>
      <el-descriptions-item label="邮箱">{{ userStore.info.email }}</el-descriptions-item>
      <el-descriptions-item label="部门">{{ userStore.info.deptName }}</el-descriptions-item>
    </el-descriptions>
  </div>
</template>

<script setup>
/**
 * 个人资料页逻辑
 * - 从用户状态库读取并展示当前登录管理员信息
 */
import { useUserStore } from '@/stores/user'
// 用户状态库实例
const userStore = useUserStore()
</script>
