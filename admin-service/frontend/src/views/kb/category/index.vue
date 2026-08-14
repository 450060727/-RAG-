<!-- 管理端知识库分类管理页 -->
<template>
  <div class="page">
    <!-- 操作工具栏 -->
    <div class="toolbar">
      <!-- 新增分类按钮：受权限控制 -->
      <el-button type="primary" v-if="userStore.hasPermission('kb:category:create')" @click="openForm()">新增分类</el-button>
    </div>

    <!-- 分类树形表格 -->
    <el-table :data="treeData" row-key="id" default-expand-all border v-loading="loading">
      <el-table-column prop="name" label="分类名称" />
      <el-table-column prop="description" label="描述" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'success' : 'danger'">{{ row.status === 0 ? '正常' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button link type="primary" v-if="userStore.hasPermission('kb:category:update')" @click="openForm(row)">编辑</el-button>
          <el-button link type="danger" v-if="userStore.hasPermission('kb:category:delete')" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="visible" :title="form.id ? '编辑分类' : '新增分类'" width="560px">
      <el-form :model="form" label-width="120px">
        <el-form-item label="上级分类">
          <!-- 上级分类树形选择，顶级为“顶级分类” -->
          <el-tree-select
            v-model="form.parentId"
            :data="[{ id: 0, name: '顶级分类', children: treeData }]"
            :props="{ label: 'name', value: 'id' }"
            check-strictly
            clearable
          />
        </el-form-item>
        <el-form-item label="分类名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="0">正常</el-radio>
            <el-radio :label="1">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="submit" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 知识库分类管理页逻辑
 * - 分类树展示、新增、编辑、删除
 */
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getKbCategories, createKbCategory, updateKbCategory, deleteKbCategory } from '@/api/kb'

// 用户状态库实例
const userStore = useUserStore()
// 表格 loading 状态
const loading = ref(false)
// 分类树数据
const treeData = ref([])
// 弹窗显示状态
const visible = ref(false)
// 保存按钮 loading
const saving = ref(false)
// 分类表单数据
const form = reactive({
  id: null,
  parentId: 0,
  name: '',
  description: '',
  status: 0
})

// 页面挂载后加载分类树
onMounted(() => load())

/**
 * 加载分类树
 */
async function load() {
  loading.value = true
  try {
    treeData.value = await getKbCategories()
  } finally {
    loading.value = false
  }
}

// 当前编辑的原始行数据，用于合并提交
const originalRow = ref(null)

/**
 * 打开新增/编辑弹窗
 * @param {Object} [row] 当前行数据，传入则为编辑
 */
function openForm(row) {
  originalRow.value = row
  if (row) {
    Object.assign(form, row)
  } else {
    Object.assign(form, {
      id: null,
      parentId: 0,
      name: '',
      description: '',
      status: 0
    })
  }
  visible.value = true
}

/**
 * 提交分类表单
 */
async function submit() {
  saving.value = true
  try {
    // 编辑时合并原始数据与表单数据
    const payload = originalRow.value
        ? { ...originalRow.value, ...form }
        : { ...form }
    if (form.id) {
      await updateKbCategory(form.id, payload)
    } else {
      await createKbCategory(payload)
    }
    ElMessage.success('保存成功')
    visible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

/**
 * 删除分类
 * @param {Object} row 当前行数据
 */
async function remove(row) {
  try {
    await ElMessageBox.confirm('确认删除该分类？子分类也会被删除。', '提示', { type: 'warning' })
    await deleteKbCategory(row.id)
    ElMessage.success('删除成功')
    await load()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '删除失败')
    }
  }
}
</script>

<style scoped>
.page { padding: 20px; }
.toolbar { margin-bottom: 16px; }
</style>
