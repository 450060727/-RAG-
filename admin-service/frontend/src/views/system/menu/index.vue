<!-- 管理端菜单管理页 -->
<template>
  <div class="page">
    <!-- 操作工具栏 -->
    <div class="toolbar">
      <!-- 新增菜单按钮：受权限控制 -->
      <el-button type="primary" v-if="userStore.hasPermission('system:menu:create')" @click="openDialog()">新增菜单</el-button>
    </div>

    <!-- 菜单树形表格 -->
    <el-table
      :data="treeData"
      row-key="id"
      :tree-props="{ children: 'children' }"
      border
      v-loading="loading"
      default-expand-all
    >
      <el-table-column prop="name" label="菜单名称" />
      <el-table-column prop="type" label="类型" width="80">
        <template #default="{ row }">
          {{ row.type === 1 ? '目录' : row.type === 2 ? '菜单' : '按钮' }}
        </template>
      </el-table-column>
      <el-table-column prop="permission" label="权限标识" />
      <el-table-column prop="path" label="路径" />
      <el-table-column prop="component" label="组件" />
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button link type="primary" v-if="userStore.hasPermission('system:menu:update')" @click="openDialog(row)">编辑</el-button>
          <el-button link type="danger" v-if="userStore.hasPermission('system:menu:delete')" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑菜单' : '新增菜单'" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="上级菜单">
          <!-- 上级菜单树形选择，check-strictly 避免父子联动 -->
          <el-tree-select
            v-model="form.parentId"
            :data="treeData"
            :props="{ label: 'name', value: 'id', children: 'children' }"
            placeholder="不选则为顶级菜单"
            clearable
            check-strictly
          />
        </el-form-item>
        <el-form-item label="菜单名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="类型">
          <el-radio-group v-model="form.type">
            <el-radio :label="1">目录</el-radio>
            <el-radio :label="2">菜单</el-radio>
            <el-radio :label="3">按钮/API</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="权限标识">
          <el-input v-model="form.permission" placeholder="如 system:user:create" />
        </el-form-item>
        <el-form-item label="路由路径">
          <el-input v-model="form.path" />
        </el-form-item>
        <el-form-item label="组件路径">
          <el-input v-model="form.component" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="form.icon" />
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
 * 菜单管理页逻辑
 * - 菜单树展示、新增、编辑、删除
 */
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMenuTree, createMenu, updateMenu, deleteMenu } from '@/api/menu'
import { useUserStore } from '@/stores/user'

// 用户状态库实例
const userStore = useUserStore()

// 表格 loading 状态
const loading = ref(false)
// 菜单树数据
const treeData = ref([])
// 弹窗显示状态
const dialogVisible = ref(false)
// 是否为编辑模式
const isEdit = ref(false)
// 表单提交 loading
const submitting = ref(false)
// 菜单表单数据
const form = reactive({ id: null, parentId: null, name: '', type: 2, permission: '', path: '', component: '', icon: '', sort: 0 })

// 页面挂载后加载菜单树
onMounted(() => {
  load()
})

/**
 * 加载菜单树
 */
async function load() {
  loading.value = true
  try {
    treeData.value = await getMenuTree()
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
    Object.assign(form, {
      id: row.id, parentId: row.parentId, name: row.name, type: row.type,
      permission: row.permission, path: row.path, component: row.component,
      icon: row.icon, sort: row.sort
    })
  } else {
    Object.assign(form, { id: null, parentId: null, name: '', type: 2, permission: '', path: '', component: '', icon: '', sort: 0 })
  }
  dialogVisible.value = true
}

/**
 * 提交菜单表单
 */
async function submit() {
  // 必填校验
  if (!form.name) {
    ElMessage.warning('请填写菜单名称')
    return
  }
  submitting.value = true
  try {
    const data = {
      parentId: form.parentId,
      name: form.name,
      type: form.type,
      permission: form.permission,
      path: form.path,
      component: form.component,
      icon: form.icon,
      sort: form.sort
    }
    if (isEdit.value) {
      await updateMenu(form.id, data)
    } else {
      await createMenu(data)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } finally {
    submitting.value = false
  }
}

/**
 * 删除菜单
 * @param {Object} row 当前行数据
 */
async function remove(row) {
  await ElMessageBox.confirm('确定删除该菜单吗？', '提示', { type: 'warning' })
  await deleteMenu(row.id)
  ElMessage.success('删除成功')
  load()
}
</script>

<style scoped>
.page { padding: 20px; }
.toolbar { margin-bottom: 16px; }
</style>
