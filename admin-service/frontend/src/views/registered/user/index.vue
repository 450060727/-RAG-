<!-- 管理端注册用户管理页 -->
<template>
  <div class="page">
    <!-- 搜索工具栏 -->
    <div class="toolbar">
      <el-form :inline="true" :model="query">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="邮箱/昵称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="正常" :value="0" />
            <el-option label="禁用" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load">查询</el-button>
          <el-button @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 注册用户表格 -->
    <el-table :data="list" v-loading="loading" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column prop="name" label="昵称" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'success' : 'danger'">{{ row.status === 0 ? '正常' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="注册时间" />
      <el-table-column prop="updatedAt" label="更新时间" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button
            link
            :type="row.status === 0 ? 'danger' : 'success'"
            v-if="userStore.hasPermission('registered:user:changeStatus')"
            @click="toggleStatus(row)"
          >
            {{ row.status === 0 ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页组件 -->
    <el-pagination
      v-model:current-page="query.page"
      v-model:page-size="query.size"
      :total="total"
      layout="total, prev, pager, next"
      @change="load"
      style="margin-top: 16px"
    />
  </div>
</template>

<script setup>
/**
 * 前端注册用户管理页逻辑
 * - 分页查询、状态启用/禁用
 */
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRegisteredUsers, changeRegisteredUserStatus } from '@/api/registeredUser'
import { useUserStore } from '@/stores/user'

// 用户状态库实例
const userStore = useUserStore()

// 表格 loading 状态
const loading = ref(false)
// 用户列表数据
const list = ref([])
// 总记录数
const total = ref(0)
// 查询条件
const query = reactive({ keyword: '', status: undefined, page: 1, size: 10 })

// 页面挂载后加载数据
onMounted(() => {
  load()
})

/**
 * 加载注册用户分页数据
 */
async function load() {
  loading.value = true
  try {
    const data = await getRegisteredUsers(query)
    list.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

/**
 * 重置查询条件并重新加载
 */
function reset() {
  query.keyword = ''
  query.status = undefined
  query.page = 1
  load()
}

/**
 * 切换用户状态（启用/禁用）
 * @param {Object} row 当前行数据
 */
async function toggleStatus(row) {
  const status = row.status === 0 ? 1 : 0
  const action = status === 1 ? '禁用' : '启用'
  await ElMessageBox.confirm(`确定${action}该用户吗？`, '提示', { type: 'warning' })
  await changeRegisteredUserStatus(row.id, status)
  ElMessage.success(`${action}成功`)
  load()
}
</script>

<style scoped>
.page { padding: 20px; }
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}
</style>
