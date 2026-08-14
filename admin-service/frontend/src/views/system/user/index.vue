<!-- 管理端系统用户管理页 -->
<template>
  <div class="page">
    <!-- 搜索与操作工具栏 -->
    <div class="toolbar">
      <!-- 搜索表单 -->
      <el-form :inline="true" :model="query">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="用户名/姓名" clearable />
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
      <!-- 新增用户按钮：受权限控制 -->
      <el-button type="primary" v-if="userStore.hasPermission('system:user:create')" @click="openDialog()">新增用户</el-button>
    </div>

    <!-- 用户表格 -->
    <el-table :data="list" v-loading="loading" border>
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="realName" label="姓名" />
      <el-table-column prop="phone" label="手机号" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column prop="deptName" label="部门" />
      <el-table-column prop="roles" label="角色">
        <template #default="{ row }">
          <el-tag v-for="r in row.roles" :key="r.id" size="small" style="margin-right: 4px">{{ r.name }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'success' : 'danger'">{{ row.status === 0 ? '正常' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button link type="primary" v-if="userStore.hasPermission('system:user:update')" @click="openDialog(row)">编辑</el-button>
          <el-button link :type="row.status === 0 ? 'danger' : 'success'" v-if="userStore.hasPermission('system:user:changeStatus')" @click="toggleStatus(row)">{{ row.status === 0 ? '禁用' : '启用' }}</el-button>
          <el-button link type="warning" v-if="userStore.hasPermission('system:user:resetPwd')" @click="resetPwd(row)">重置密码</el-button>
          <el-button link type="danger" v-if="userStore.hasPermission('system:user:delete')" @click="remove(row)">删除</el-button>
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑用户' : '新增用户'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名">
          <!-- 编辑时用户名不可修改 -->
          <el-input v-model="form.username" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="部门">
          <!-- 部门树形选择，check-strictly 避免父子联动 -->
          <el-tree-select
            v-model="form.deptId"
            :data="deptTree"
            :props="{ label: 'name', value: 'id', children: 'children' }"
            placeholder="选择部门"
            clearable
            check-strictly
          />
        </el-form-item>
        <el-form-item label="角色">
          <!-- 角色多选 -->
          <el-select v-model="form.roleIds" multiple placeholder="选择角色" style="width: 100%">
            <el-option v-for="r in roleList" :key="r.id" :label="r.name" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" v-if="isEdit">
          <el-radio-group v-model="form.status">
            <el-radio :label="0">正常</el-radio>
            <el-radio :label="1">禁用</el-radio>
          </el-radio-group>
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
 * 系统用户管理页逻辑
 * - 用户分页查询、新增、编辑、状态切换、重置密码、删除
 * - 依赖部门树与角色列表进行表单选择
 */
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUsers, createUser, updateUser, changeUserStatus, resetUserPassword, deleteUser } from '@/api/user'
import { getDeptTree } from '@/api/dept'
import { getRoles } from '@/api/role'
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

// 弹窗显示状态
const dialogVisible = ref(false)
// 是否为编辑模式
const isEdit = ref(false)
// 表单提交 loading
const submitting = ref(false)
// 用户表单数据
const form = reactive({ id: null, username: '', realName: '', phone: '', email: '', deptId: null, roleIds: [], status: 0 })

// 部门树数据
const deptTree = ref([])
// 角色列表数据
const roleList = ref([])

// 页面挂载后加载数据
onMounted(() => {
  load()
  loadDepts()
  loadRoles()
})

/**
 * 加载角色列表
 */
async function loadRoles() {
  try {
    roleList.value = await getRoles()
  } catch (e) {
    // ignore
  }
}

/**
 * 加载部门树
 */
async function loadDepts() {
  try {
    deptTree.value = await getDeptTree()
  } catch (e) {
    // ignore
  }
}

/**
 * 加载用户分页数据
 */
async function load() {
  loading.value = true
  try {
    const data = await getUsers(query)
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
 * 打开新增/编辑弹窗
 * @param {Object} [row] 当前行数据，传入则为编辑
 */
function openDialog(row) {
  isEdit.value = !!row
  if (row) {
    // 编辑时回填数据
    Object.assign(form, {
      id: row.id,
      username: row.username,
      realName: row.realName,
      phone: row.phone,
      email: row.email,
      deptId: row.deptId,
      roleIds: row.roles ? row.roles.map(r => r.id) : [],
      status: row.status
    })
  } else {
    // 新增时重置表单
    Object.assign(form, { id: null, username: '', realName: '', phone: '', email: '', deptId: null, roleIds: [], status: 0 })
  }
  dialogVisible.value = true
}

/**
 * 提交用户表单
 */
async function submit() {
  // 必填校验
  if (!form.username || !form.realName || !form.deptId) {
    ElMessage.warning('请填写必填项')
    return
  }
  submitting.value = true
  try {
    if (isEdit.value) {
      // 编辑：更新用户信息
      await updateUser(form.id, {
        realName: form.realName,
        phone: form.phone,
        email: form.email,
        deptId: form.deptId,
        roleIds: form.roleIds,
        status: form.status
      })
      ElMessage.success('保存成功')
    } else {
      // 新增：创建用户并展示初始密码
      const res = await createUser({
        username: form.username,
        realName: form.realName,
        phone: form.phone,
        email: form.email,
        deptId: form.deptId,
        roleIds: form.roleIds
      })
      ElMessage.success(`新增成功，初始密码：${res.initialPassword}`)
    }
    dialogVisible.value = false
    load()
  } finally {
    submitting.value = false
  }
}

/**
 * 切换用户状态（启用/禁用）
 * @param {Object} row 当前行数据
 */
async function toggleStatus(row) {
  const status = row.status === 0 ? 1 : 0
  const action = status === 1 ? '禁用' : '启用'
  await ElMessageBox.confirm(`确定${action}该用户吗？`, '提示', { type: 'warning' })
  await changeUserStatus(row.id, status)
  ElMessage.success(`${action}成功`)
  load()
}

/**
 * 重置用户密码为默认 123456
 * @param {Object} row 当前行数据
 */
async function resetPwd(row) {
  await ElMessageBox.confirm('确定重置该用户密码为 123456 吗？', '提示', { type: 'warning' })
  await resetUserPassword(row.id)
  ElMessage.success('重置成功，初始密码：123456')
}

/**
 * 删除用户
 * @param {Object} row 当前行数据
 */
async function remove(row) {
  await ElMessageBox.confirm('确定删除该用户吗？', '提示', { type: 'warning' })
  await deleteUser(row.id)
  ElMessage.success('删除成功')
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
