<!-- 管理端部门管理页 -->
<template>
  <div class="page">
    <!-- 操作工具栏 -->
    <div class="toolbar">
      <!-- 新增部门按钮：受权限控制 -->
      <el-button type="primary" v-if="userStore.hasPermission('system:dept:create')" @click="openDialog()">新增部门</el-button>
    </div>

    <!-- 部门树形表格 -->
    <el-table
      :data="treeData"
      row-key="id"
      :tree-props="{ children: 'children' }"
      border
      v-loading="loading"
      default-expand-all
    >
      <el-table-column prop="name" label="部门名称" />
      <el-table-column prop="code" label="部门编码" />
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'success' : 'danger'">{{ row.status === 0 ? '正常' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button link type="primary" v-if="userStore.hasPermission('system:dept:update')" @click="openDialog(row)">编辑</el-button>
          <el-button link type="danger" v-if="userStore.hasPermission('system:dept:delete')" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑部门' : '新增部门'" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="上级部门">
          <!-- 上级部门树形选择，check-strictly 避免父子联动 -->
          <el-tree-select
            v-model="form.parentId"
            :data="treeData"
            :props="{ label: 'name', value: 'id', children: 'children' }"
            placeholder="不选则为顶级部门"
            clearable
            check-strictly
          />
        </el-form-item>
        <el-form-item label="部门名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="部门编码">
          <el-input v-model="form.code" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 部门管理页逻辑
 * - 部门树展示、新增、编辑、删除
 */
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDeptTree, createDept, updateDept, deleteDept } from '@/api/dept'
import { useUserStore } from '@/stores/user'

// 用户状态库实例
const userStore = useUserStore()

// 表格 loading 状态
const loading = ref(false)
// 部门树数据
const treeData = ref([])
// 弹窗显示状态
const dialogVisible = ref(false)
// 是否为编辑模式
const isEdit = ref(false)
// 表单提交 loading
const submitting = ref(false)
// 部门表单数据
const form = reactive({ id: null, parentId: null, name: '', code: '', sort: 0 })

// 页面挂载后加载部门树
onMounted(() => {
  load()
})

/**
 * 加载部门树
 */
async function load() {
  loading.value = true
  try {
    treeData.value = await getDeptTree()
  } finally {
    loading.value = false
  }
}

/**
 * 打开新增/编辑弹窗
 * @param {Object} [row] 当前行数据，传入则为编辑
 */
function openDialog(row) {
  isEdit.value = !!row
  if (row) {
    Object.assign(form, { id: row.id, parentId: row.parentId, name: row.name, code: row.code, sort: row.sort })
  } else {
    Object.assign(form, { id: null, parentId: null, name: '', code: '', sort: 0 })
  }
  dialogVisible.value = true
}

/**
 * 提交部门表单
 */
async function submit() {
  // 必填校验
  if (!form.name || !form.code) {
    ElMessage.warning('请填写必填项')
    return
  }
  submitting.value = true
  try {
    const data = { parentId: form.parentId, name: form.name, code: form.code, sort: form.sort }
    if (isEdit.value) {
      await updateDept(form.id, data)
      ElMessage.success('保存成功')
    } else {
      await createDept(data)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    load()
  } finally {
    submitting.value = false
  }
}

/**
 * 删除部门
 * @param {Object} row 当前行数据
 */
async function remove(row) {
  await ElMessageBox.confirm('确定删除该部门吗？', '提示', { type: 'warning' })
  await deleteDept(row.id)
  ElMessage.success('删除成功')
  load()
}
</script>

<style scoped>
.page { padding: 20px; }
.toolbar { margin-bottom: 16px; }
</style>
