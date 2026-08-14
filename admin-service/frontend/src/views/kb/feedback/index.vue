<!-- 管理端反馈审核页 -->
<template>
  <div class="page">
    <el-card shadow="never">
      <template #header>
        <span>反馈审核（候选 QA 对）</span>
      </template>
      <p>此处展示用户标记“有用并加入知识库”或自动规则生成的候选问答对，支持批准/拒绝。</p>
      <!-- 反馈表格 -->
      <el-table :data="list" border v-loading="loading">
        <el-table-column prop="question" label="问题" />
        <el-table-column prop="answer" label="回答" />
        <el-table-column prop="useLocal" label="是否命中本地" width="120" />
        <el-table-column prop="writeBackStatus" label="状态" width="100" />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button link type="primary" v-if="userStore.hasPermission('kb:feedback:approve')" @click="approve(row)">批准</el-button>
            <el-button link type="danger" v-if="userStore.hasPermission('kb:feedback:reject')" @click="reject(row)">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
/**
 * 反馈审核页逻辑
 * - 加载待审核的候选 QA 对
 * - 支持批准（加入知识库）或驳回
 */
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getKbFeedback, approveKbFeedback, rejectKbFeedback } from '@/api/kb'

// 用户状态库实例
const userStore = useUserStore()
// 反馈列表
const list = ref([])
// 表格 loading 状态
const loading = ref(false)

// 页面挂载后加载反馈列表
onMounted(() => load())

/**
 * 加载待审核反馈列表
 */
async function load() {
  loading.value = true
  try {
    const data = await getKbFeedback({ writeBackStatus: 'PENDING', page: 1, size: 50 })
    list.value = data.records || []
  } finally {
    loading.value = false
  }
}

/**
 * 批准反馈
 * @param {Object} row 当前反馈行
 */
async function approve(row) {
  await approveKbFeedback(row.id)
  ElMessage.success('已批准')
  await load()
}

/**
 * 驳回反馈
 * @param {Object} row 当前反馈行
 */
async function reject(row) {
  await rejectKbFeedback(row.id)
  ElMessage.success('已驳回')
  await load()
}
</script>

<style scoped>
.page { padding: 20px; }
</style>
