<!-- 管理端修改密码页 -->
<template>
  <div>
    <!-- 页面标题 -->
    <h2>修改密码</h2>
    <!-- 修改密码表单 -->
    <el-form :model="form" label-width="100px" style="max-width: 400px">
      <el-form-item label="原密码">
        <!-- 原密码输入 -->
        <el-input v-model="form.oldPassword" type="password" show-password />
      </el-form-item>
      <el-form-item label="新密码">
        <!-- 新密码输入 -->
        <el-input v-model="form.newPassword" type="password" show-password />
      </el-form-item>
      <el-form-item>
        <!-- 保存按钮 -->
        <el-button type="primary" :loading="loading" @click="submit">保存</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
/**
 * 修改密码页逻辑
 * - 收集原密码与新密码
 * - 调用接口修改成功后清除 token 并跳转登录页
 */
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { changePassword } from '@/api/auth'

// 保存按钮 loading 状态
const loading = ref(false)
// 密码表单数据
const form = reactive({ oldPassword: '', newPassword: '' })

/**
 * 提交修改密码
 */
async function submit() {
  // 校验必填
  if (!form.oldPassword || !form.newPassword) {
    ElMessage.warning('请填写完整')
    return
  }
  loading.value = true
  try {
    // 调用修改密码接口
    await changePassword(form)
    ElMessage.success('修改成功，请重新登录')
    // 清除本地 token
    localStorage.removeItem('adminToken')
    // 跳转登录页
    window.location.href = '/login'
  } finally {
    loading.value = false
  }
}
</script>
