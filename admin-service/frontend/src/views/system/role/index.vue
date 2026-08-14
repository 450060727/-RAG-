<!-- 管理端角色管理页 -->
<template>
  <div class="page">
    <!-- 操作工具栏 -->
    <div class="toolbar">
      <!-- 新增角色按钮：受权限控制 -->
      <el-button type="primary" v-if="userStore.hasPermission('system:role:create')" @click="openDialog()">新增角色</el-button>
    </div>

    <!-- 角色表格 -->
    <el-table :data="list" border v-loading="loading">
      <el-table-column prop="name" label="角色名称" />
      <el-table-column prop="code" label="角色编码" />
      <el-table-column prop="dataScope" label="数据范围">
        <template #default="{ row }">
          {{ scopeMap[row.dataScope] }}
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" />
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button link type="primary" v-if="userStore.hasPermission('system:role:update')" @click="openDialog(row)">编辑</el-button>
          <el-button link type="primary" v-if="userStore.hasPermission('system:role:assignMenu')" @click="openMenuDialog(row)">分配菜单</el-button>
          <el-button link type="danger" v-if="userStore.hasPermission('system:role:delete')" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 角色表单弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑角色' : '新增角色'" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="角色名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="角色编码">
          <!-- 编辑时编码不可修改 -->
          <el-input v-model="form.code" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="数据范围">
          <el-select v-model="form.dataScope" placeholder="选择数据范围" style="width: 100%">
            <el-option label="全部数据" value="ALL" />
            <el-option label="本部门数据" value="DEPT" />
            <el-option label="本部门及以下" value="DEPT_AND_CHILD" />
            <el-option label="自定义部门" value="CUSTOM" />
          </el-select>
        </el-form-item>
        <el-form-item label="自定义部门" v-if="form.dataScope === 'CUSTOM'">
          <!-- 自定义部门多选 -->
          <el-tree-select
            v-model="form.deptIds"
            :data="deptTree"
            :props="{ label: 'name', value: 'id', children: 'children' }"
            placeholder="选择部门"
            multiple
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 分配菜单弹窗 -->
    <el-dialog v-model="menuDialogVisible" title="分配菜单" width="500px">
      <!-- 菜单树，支持勾选 -->
      <el-tree
        ref="menuTreeRef"
        :data="menuTree"
        :props="{ label: 'name', children: 'children' }"
        node-key="id"
        show-checkbox
        default-expand-all
      />
      <template #footer>
        <el-button @click="menuDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="menuSubmitting" @click="submitMenus">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 角色管理页逻辑
 * - 角色 CRUD
 * - 为角色分配菜单权限
 */
import { reactive, ref, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRoles, createRole, updateRole, deleteRole, getRoleMenus, assignRoleMenus } from '@/api/role'
import { getDeptTree } from '@/api/dept'
import { getMenuTree } from '@/api/menu'
import { useUserStore } from '@/stores/user'

// 用户状态库实例
const userStore = useUserStore()

// 表格 loading 状态
const loading = ref(false)
// 角色列表
const list = ref([])
// 角色弹窗显示状态
const dialogVisible = ref(false)
// 是否为编辑模式
const isEdit = ref(false)
// 角色表单提交 loading
const submitting = ref(false)
// 角色表单数据
const form = reactive({ id: null, name: '', code: '', dataScope: 'ALL', remark: '', deptIds: [] })

// 数据范围枚举映射
const scopeMap = {
  ALL: '全部数据',
  DEPT: '本部门数据',
  DEPT_AND_CHILD: '本部门及以下',
  CUSTOM: '自定义部门'
}

// 部门树数据
const deptTree = ref([])

// 页面挂载后加载角色与部门数据
onMounted(() => {
  load()
  loadDepts()
})

/**
 * 加载角色列表
 */
async function load() {
  loading.value = true
  try {
    list.value = await getRoles()
  } finally {
    loading.value = false
  }
}

/**
 * 加载部门树
 */
async function loadDepts() {
  try {
    deptTree.value = await getDeptTree()
  } catch (e) {}
}

/**
 * 打开新增/编辑弹窗
 * @param {Object} [row] 当前行数据，传入则为编辑
 */
function openDialog(row) {
  isEdit.value = !!row
  if (row) {
    Object.assign(form, {
      id: row.id, name: row.name, code: row.code,
      dataScope: row.dataScope, remark: row.remark,
      deptIds: row.deptIds || []
    })
  } else {
    Object.assign(form, { id: null, name: '', code: '', dataScope: 'ALL', remark: '', deptIds: [] })
  }
  dialogVisible.value = true
}

/**
 * 提交角色表单
 */
async function submit() {
  // 必填校验
  if (!form.name || !form.code) {
    ElMessage.warning('请填写必填项')
    return
  }
  submitting.value = true
  try {
    const data = {
      name: form.name,
      code: form.code,
      dataScope: form.dataScope,
      remark: form.remark,
      // 自定义数据范围时才提交部门 ID
      deptIds: form.dataScope === 'CUSTOM' ? form.deptIds : []
    }
    if (isEdit.value) {
      await updateRole(form.id, data)
    } else {
      await createRole(data)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } finally {
    submitting.value = false
  }
}

/**
 * 删除角色
 * @param {Object} row 当前行数据
 */
async function remove(row) {
  await ElMessageBox.confirm('确定删除该角色吗？', '提示', { type: 'warning' })
  await deleteRole(row.id)
  ElMessage.success('删除成功')
  load()
}

// 分配菜单相关状态
const menuDialogVisible = ref(false)
const menuSubmitting = ref(false)
const menuTree = ref([])
// 菜单树组件引用
const menuTreeRef = ref()
// 当前正在分配菜单的角色 ID
const currentRoleId = ref(null)

/**
 * 打开分配菜单弹窗
 * @param {Object} row 当前行数据
 */
async function openMenuDialog(row) {
  currentRoleId.value = row.id
  menuTree.value = await getMenuTree()
  const selected = await getRoleMenus(row.id)
  menuDialogVisible.value = true
  // 弹窗渲染完成后回填已勾选菜单
  nextTick(() => {
    menuTreeRef.value.setCheckedKeys(selected)
  })
}

/**
 * 提交角色菜单分配
 */
async function submitMenus() {
  // 获取全选与半选节点
  const checked = menuTreeRef.value.getCheckedKeys()
  const half = menuTreeRef.value.getHalfCheckedKeys()
  const menuIds = [...new Set([...checked, ...half])]
  menuSubmitting.value = true
  try {
    await assignRoleMenus(currentRoleId.value, { menuIds })
    ElMessage.success('分配成功')
    menuDialogVisible.value = false
  } finally {
    menuSubmitting.value = false
  }
}
</script>

<style scoped>
.page { padding: 20px; }
.toolbar { margin-bottom: 16px; }
</style>
