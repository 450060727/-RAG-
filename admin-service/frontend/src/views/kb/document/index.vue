<!-- 管理端知识库文档管理页 -->
<template>
  <div class="page">
    <el-row :gutter="16">
      <!-- 左侧分类树 -->
      <el-col :span="5">
        <el-card shadow="never">
          <template #header>
            <span>知识库分类</span>
          </template>
          <!-- 分类树，点击节点筛选右侧文档 -->
          <el-tree
            :data="categories"
            :props="{ label: 'name', children: 'children' }"
            node-key="id"
            highlight-current
            default-expand-all
            @node-click="onCategoryClick"
          />
        </el-card>
      </el-col>
      <!-- 右侧文档列表 -->
      <el-col :span="19">
        <el-card shadow="never">
          <template #header>
            <div class="toolbar">
              <!-- 标题关键词搜索 -->
              <el-input v-model="query.keyword" placeholder="文档标题" clearable style="width: 200px" />
              <!-- 来源类型筛选 -->
              <el-select v-model="query.sourceType" placeholder="来源" clearable style="width: 120px; margin-left: 8px;">
                <el-option label="上传" value="UPLOAD" />
                <el-option label="手动输入" value="TEXT" />
                <el-option label="QA 对" value="QA_PAIR" />
              </el-select>
              <el-button type="primary" style="margin-left: 8px;" @click="load">查询</el-button>
              <div style="flex: 1" />
              <!-- 新增手动输入/上传文件按钮：受权限控制 -->
              <el-button type="primary" v-if="userStore.hasPermission('kb:document:create')" @click="textVisible = true">手动输入</el-button>
              <el-button type="primary" v-if="userStore.hasPermission('kb:document:create')" @click="uploadVisible = true">上传文件</el-button>
            </div>
          </template>

          <!-- 文档表格 -->
          <el-table :data="list" v-loading="loading" border>
            <el-table-column prop="title" label="标题" show-overflow-tooltip />
            <el-table-column prop="sourceType" label="来源" width="100" />
            <el-table-column prop="fileType" label="类型" width="80" />
            <el-table-column prop="chunkCount" label="切片数" width="80" />
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }">
                <el-tag v-if="row.status === 0" type="success">正常</el-tag>
                <el-tag v-else-if="row.status === 2" type="warning">解析中</el-tag>
                <el-tag v-else-if="row.status === 9" type="danger">解析失败</el-tag>
                <el-tag v-else type="info">已删除</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="创建时间" width="170" />
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <el-button link type="primary" v-if="row.sourceType === 'TEXT' || row.sourceType === 'QA_PAIR'" @click="viewContent(row)">查看</el-button>
                <el-button link type="primary" v-if="row.sourceType === 'UPLOAD' && row.filePath" @click="downloadFile(row)">下载</el-button>
                <el-button link type="danger" v-if="userStore.hasPermission('kb:document:delete')" @click="remove(row)">删除</el-button>
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
            style="margin-top: 16px;"
          />
        </el-card>
      </el-col>
    </el-row>

    <!-- 手动输入弹窗 -->
    <el-dialog v-model="textVisible" title="手动输入文本" width="600px">
      <el-form :model="textForm" label-width="80px">
        <el-form-item label="分类">
          <!-- 分类树形选择，顶级分类为“顶级分类” -->
          <el-tree-select
            v-model="textForm.categoryId"
            :data="[{ id: 0, name: '顶级分类', children: categories }]"
            :props="{ label: 'name', value: 'id' }"
            check-strictly
          />
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="textForm.title" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="textForm.content" type="textarea" :rows="8" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="textVisible = false">取消</el-button>
        <el-button type="primary" @click="submitText" :loading="submitting">保存</el-button>
      </template>
    </el-dialog>

    <!-- 上传文件弹窗 -->
    <el-dialog v-model="uploadVisible" title="上传文件" width="520px">
      <el-form :model="uploadForm" label-width="80px">
        <el-form-item label="分类">
          <!-- 分类树形选择 -->
          <el-tree-select
            v-model="uploadForm.categoryId"
            :data="[{ id: 0, name: '顶级分类', children: categories }]"
            :props="{ label: 'name', value: 'id' }"
            check-strictly
          />
        </el-form-item>
        <el-form-item label="文件">
          <!-- 文件上传组件，手动上传 -->
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            :on-change="onFileChange"
            :on-remove="() => uploadForm.file = null"
          >
            <el-button type="primary">选择文件</el-button>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="uploadVisible = false">取消</el-button>
        <el-button type="primary" @click="submitUpload" :loading="submitting">上传</el-button>
      </template>
    </el-dialog>
    <!-- 查看文本/QA 内容弹窗 -->
    <el-dialog v-model="viewVisible" title="文档内容" width="700px">
      <pre style="max-height: 500px; overflow: auto; white-space: pre-wrap; word-break: break-word;">{{ viewContentText }}</pre>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 知识库文档管理页逻辑
 * - 左侧分类树筛选，右侧文档分页列表
 * - 支持手动录入文本、上传文件、查看、下载、删除
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getKbCategoryTree, getKbDocuments, saveKbText, uploadKbDocument, deleteKbDocument, getKbDocumentContent, downloadKbDocument } from '@/api/kb'

// 用户状态库实例
const userStore = useUserStore()
// 分类树数据
const categories = ref([])
// 文档列表数据
const list = ref([])
// 总记录数
const total = ref(0)
// 表格 loading 状态
const loading = ref(false)
// 查询条件
const query = reactive({ categoryId: null, keyword: '', sourceType: '', page: 1, size: 10 })

// 手动输入弹窗显示状态
const textVisible = ref(false)
// 上传弹窗显示状态
const uploadVisible = ref(false)
// 查看内容弹窗显示状态
const viewVisible = ref(false)
// 当前查看的文档内容
const viewContentText = ref('')
// 表单提交 loading
const submitting = ref(false)
// 手动输入表单
const textForm = reactive({ categoryId: null, title: '', content: '' })
// 上传表单
const uploadForm = reactive({ categoryId: null, file: null })
// el-upload 组件引用
const uploadRef = ref(null)

// 页面挂载后加载分类与文档
onMounted(async () => {
  categories.value = await getKbCategoryTree()
  await load()
})

/**
 * 加载文档分页数据
 */
async function load() {
  loading.value = true
  try {
    const data = await getKbDocuments({ ...query })
    list.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

/**
 * 点击分类节点时筛选文档
 * @param {Object} data 分类节点数据
 */
function onCategoryClick(data) {
  query.categoryId = data.id === 0 ? null : data.id
  query.page = 1
  load()
}

/**
 * 提交手动输入的文本
 */
async function submitText() {
  // 校验必填
  if (!textForm.categoryId || !textForm.title || !textForm.content) {
    ElMessage.warning('请填写完整')
    return
  }
  submitting.value = true
  try {
    await saveKbText({ ...textForm })
    ElMessage.success('保存成功')
    textVisible.value = false
    await load()
  } finally {
    submitting.value = false
  }
}

/**
 * 文件选择变化回调
 * @param {Object} file el-upload 文件对象
 */
function onFileChange(file) {
  uploadForm.file = file.raw
}

/**
 * 提交文件上传
 */
async function submitUpload() {
  // 校验必填
  if (!uploadForm.categoryId || !uploadForm.file) {
    ElMessage.warning('请选择分类和文件')
    return
  }
  submitting.value = true
  try {
    const fd = new FormData()
    fd.append('categoryId', uploadForm.categoryId)
    fd.append('file', uploadForm.file)
    await uploadKbDocument(fd)
    ElMessage.success('上传成功')
    uploadVisible.value = false
    uploadRef.value?.clearFiles?.()
    uploadForm.file = null
    await load()
  } finally {
    submitting.value = false
  }
}

/**
 * 查看文本/QA 文档内容
 * @param {Object} row 当前文档行
 */
async function viewContent(row) {
  try {
    const data = await getKbDocumentContent(row.id)
    viewContentText.value = data || ''
    viewVisible.value = true
  } catch (e) {
    ElMessage.error(e.message || '获取内容失败')
  }
}

/**
 * 下载上传的文档文件
 * @param {Object} row 当前文档行
 */
async function downloadFile(row) {
  try {
    const res = await downloadKbDocument(row.id)
    const blob = new Blob([res.data])
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = row.title || 'download'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
  } catch (e) {
    ElMessage.error(e.message || '下载失败')
  }
}

/**
 * 删除文档
 * @param {Object} row 当前文档行
 */
async function remove(row) {
  try {
    await ElMessageBox.confirm('确认删除该文档？', '提示', { type: 'warning' })
    await deleteKbDocument(row.id)
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
.toolbar { display: flex; align-items: center; }
</style>
