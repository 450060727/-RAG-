<!-- 管理端模型配置 / 登录配置页 -->
<!-- 通过 props.mode 区分 'model'（模型配置）与 'login'（登录配置） -->
<template>
  <div class="page">
    <el-card>
      <template #header>
        <div class="header">
          <!-- 根据 mode 动态显示标题 -->
          <span>{{ props.mode === 'login' ? '登录配置' : '模型配置' }}</span>
          <div>
            <el-button type="primary" @click="save" :loading="saving">保存</el-button>
            <el-button @click="refreshCache" :loading="refreshing">刷新缓存</el-button>
          </div>
        </div>
      </template>

      <!-- 配置标签页 -->
      <el-tabs v-model="activeTab">
        <!-- 模型配置相关标签页 -->
        <template v-if="props.mode === 'model'">
        <el-tab-pane label="Milvus" name="milvus">
          <el-form :model="form.milvus" label-width="140px">
            <el-form-item label="Host"><el-input v-model="form.milvus.host" /></el-form-item>
            <el-form-item label="Port"><el-input-number v-model="form.milvus.port" /></el-form-item>
            <el-form-item label="用户名"><el-input v-model="form.milvus.username" /></el-form-item>
            <el-form-item label="密码"><el-input v-model="form.milvus.password" type="password" /></el-form-item>
            <el-form-item label="Database"><el-input v-model="form.milvus.database" /></el-form-item>
            <el-form-item label="Collection"><el-input v-model="form.milvus.collection" /></el-form-item>
            <el-form-item label="向量维度"><el-input-number v-model="form.milvus.vectorDim" /></el-form-item>
            <el-form-item label="Metric Type"><el-input v-model="form.milvus.metricType" /></el-form-item>
            <el-form-item label="Index Type"><el-input v-model="form.milvus.indexType" /></el-form-item>
            <el-form-item label="Consistency"><el-input v-model="form.milvus.consistencyLevel" /></el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="Embedding" name="embedding">
          <el-form :model="form.embedding" label-width="140px">
            <el-form-item label="向量服务">
              <el-radio-group v-model="form.embedding.provider">
                <el-radio-button label="ollama">本地 Embedding（Ollama）</el-radio-button>
                <el-radio-button label="siliconflow">硅基流动（SiliconFlow）</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <!-- SiliconFlow 向量服务配置项 -->
            <template v-if="form.embedding.provider === 'siliconflow'">
              <el-form-item label="模型"><el-input v-model="form.embedding.model" placeholder="如 BAAI/bge-m3" /></el-form-item>
              <el-form-item label="API Key"><el-input v-model="form.embedding.apiKey" type="password" /></el-form-item>
              <el-form-item label="Base URL"><el-input v-model="form.embedding.baseUrl" /></el-form-item>
            </template>
            <!-- Ollama 向量服务配置项 -->
            <template v-if="form.embedding.provider === 'ollama'">
              <el-form-item label="Ollama URL"><el-input v-model="form.embedding.ollamaUrl" /></el-form-item>
              <el-form-item label="Ollama Model"><el-input v-model="form.embedding.ollamaModel" /></el-form-item>
            </template>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="Rerank" name="rerank">
          <el-form :model="form.rerank" label-width="140px">
            <el-form-item label="启用">
              <el-switch :active-value="1" :inactive-value="0" v-model="form.rerank.enabled" />
            </el-form-item>
            <el-form-item label="Rerank 服务">
              <el-radio-group v-model="form.rerank.provider">
                <el-radio-button label="ollama">本地 Rerank（Ollama）</el-radio-button>
                <el-radio-button label="siliconflow">硅基流动（SiliconFlow）</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <!-- SiliconFlow Rerank 配置项 -->
            <template v-if="form.rerank.provider === 'siliconflow'">
              <el-form-item label="模型"><el-input v-model="form.rerank.model" placeholder="如 BAAI/bge-reranker-v2-m3" /></el-form-item>
              <el-form-item label="API Key"><el-input v-model="form.rerank.apiKey" type="password" /></el-form-item>
              <el-form-item label="Base URL"><el-input v-model="form.rerank.baseUrl" /></el-form-item>
              <el-form-item label="Max Chunks Per Doc"><el-input-number v-model="form.rerank.maxChunksPerDoc" /></el-form-item>
            </template>
            <!-- Ollama Rerank 配置项 -->
            <template v-if="form.rerank.provider === 'ollama'">
              <el-form-item label="Rerank URL"><el-input v-model="form.rerank.ollamaUrl" /></el-form-item>
              <el-form-item label="Rerank Model"><el-input v-model="form.rerank.model" placeholder="如 dengcao/Qwen3-Reranker-0.6B:Q8_0" /></el-form-item>
              <el-form-item label="并发数"><el-input-number v-model="form.rerank.concurrency" /></el-form-item>
              <el-form-item label="Temperature"><el-input-number v-model="form.rerank.temperature" :precision="2" /></el-form-item>
              <el-form-item label="Prompt 模板"><el-input v-model="form.rerank.promptTemplate" type="textarea" :rows="6" /></el-form-item>
            </template>
            <el-form-item label="TopK"><el-input-number v-model="form.rerank.topK" /></el-form-item>
            <el-form-item label="Limit"><el-input-number v-model="form.rerank.limit" /></el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="Chat" name="chat">
          <el-form :model="form.chat" label-width="140px">
            <el-form-item label="对话服务">
              <el-radio-group v-model="form.chat.provider">
                <el-radio-button label="ollama">本地 Chat（Ollama）</el-radio-button>
                <el-radio-button label="siliconflow">硅基流动（SiliconFlow）</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <!-- SiliconFlow Chat 配置项 -->
            <template v-if="form.chat.provider === 'siliconflow'">
              <el-form-item label="模型"><el-input v-model="form.chat.model" placeholder="如 deepseek-ai/DeepSeek-V2.5" /></el-form-item>
              <el-form-item label="API Key"><el-input v-model="form.chat.apiKey" type="password" /></el-form-item>
              <el-form-item label="Base URL"><el-input v-model="form.chat.baseUrl" /></el-form-item>
            </template>
            <!-- Ollama Chat 配置项 -->
            <template v-if="form.chat.provider === 'ollama'">
              <el-form-item label="Ollama URL"><el-input v-model="form.chat.ollamaUrl" /></el-form-item>
              <el-form-item label="Chat Model"><el-input v-model="form.chat.model" placeholder="如 qwen:1.8b" /></el-form-item>
            </template>
            <el-form-item label="Temperature"><el-input-number v-model="form.chat.temperature" :precision="2" /></el-form-item>
            <el-form-item label="Max Tokens"><el-input-number v-model="form.chat.maxTokens" /></el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="Search" name="search">
          <el-form :model="form.search" label-width="180px">
            <el-form-item label="本地命中阈值"><el-input-number v-model="form.search.localThreshold" :precision="3" /></el-form-item>
            <el-form-item label="TopK"><el-input-number v-model="form.search.topK" /></el-form-item>
            <el-form-item label="默认分类 ID"><el-input-number v-model="form.search.defaultCategoryId" /></el-form-item>
            <el-form-item label="历史轮数"><el-input-number v-model="form.search.historyRounds" /></el-form-item>
            <el-form-item label="历史最大字符"><el-input-number v-model="form.search.historyMaxChars" /></el-form-item>
            <el-form-item label="上下文阈值"><el-input-number v-model="form.search.contextThreshold" :precision="3" /></el-form-item>
            <el-form-item label="上下文最大字符"><el-input-number v-model="form.search.contextMaxChars" /></el-form-item>
            <el-form-item label="自动回写">
              <el-switch :active-value="1" :inactive-value="0" v-model="form.search.autoWriteEnabled" />
            </el-form-item>
            <el-form-item label="自动回写阈值"><el-input-number v-model="form.search.autoWriteThreshold" :precision="3" /></el-form-item>
          </el-form>
        </el-tab-pane>
        </template>

        <!-- 登录配置相关标签页 -->
        <template v-if="props.mode === 'login'">
        <el-tab-pane label="Auth/Session" name="authSession">
          <el-form :model="form.authSession" label-width="180px">
            <el-form-item label="JWT Secret"><el-input v-model="form.authSession.jwtSecret" /></el-form-item>
            <el-form-item label="JWT 过期（分钟）"><el-input-number v-model="form.authSession.jwtExpireMinutes" /></el-form-item>
            <el-form-item label="验证码 TTL（秒）"><el-input-number v-model="form.authSession.codeTtlSeconds" /></el-form-item>
            <el-form-item label="验证码重发间隔（秒）"><el-input-number v-model="form.authSession.codeResendIntervalSeconds" /></el-form-item>
            <el-form-item label="会话 TTL（分钟）"><el-input-number v-model="form.authSession.sessionTtlMinutes" /></el-form-item>
            <el-form-item label="会话续签阈值（分钟）"><el-input-number v-model="form.authSession.sessionRenewThresholdMinutes" /></el-form-item>
          </el-form>
        </el-tab-pane>
        </template>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
/**
 * 模型配置 / 登录配置页逻辑
 * - 通过 props.mode 区分两种配置视图
 * - 从后端加载配置，支持保存与刷新缓存
 * - 切换 provider 时自动填充默认 API Key
 */
import { ref, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getModelConfig, updateModelConfig, refreshModelConfigCache } from '@/api/modelConfig'

// 组件 props：mode 决定显示模型配置还是登录配置
const props = defineProps({
  mode: {
    type: String,
    default: 'model' // 'model' | 'login'
  }
})

// 当前激活的标签页
const activeTab = ref(props.mode === 'login' ? 'authSession' : 'milvus')
// 保存按钮 loading
const saving = ref(false)
// 刷新缓存按钮 loading
const refreshing = ref(false)

/**
 * 返回默认配置对象
 * @returns {Object} 默认配置
 */
const defaultForm = () => ({
  milvus: { host: '', port: 19530, username: '', password: '', database: '', collection: '', vectorDim: 1024, metricType: 'COSINE', indexType: 'HNSW', consistencyLevel: 'Bounded' },
  embedding: { provider: 'ollama', model: 'BAAI/bge-m3', apiKey: 'sk-avfyfwkuldvsrpmlcsmpcvudhxwtysdraiqftbymikzydhyh', baseUrl: 'https://api.siliconflow.cn/v1', qianwenUrl: 'https://dashscope.aliyuncs.com/compatible-mode/v1', ollamaUrl: 'http://localhost:11434/api/embed', ollamaModel: 'qwen3-embedding:0.6b-q8_0' },
  rerank: { enabled: 1, provider: 'ollama', model: 'dengcao/Qwen3-Reranker-0.6B:Q8_0', apiKey: 'sk-avfyfwkuldvsrpmlcsmpcvudhxwtysdraiqftbymikzydhyh', baseUrl: 'https://api.siliconflow.cn/v1', ollamaUrl: 'http://127.0.0.1:11434', concurrency: 4, topK: 5, limit: 5, promptTemplate: 'Given the following query and passage, determine whether the passage is relevant to the query. Output only a floating point number between 0 and 1, where 1 means highly relevant.\n\nQuery: {query}\nPassage: {passage}\n\nRelevance score:', temperature: 0.0, maxChunksPerDoc: 512 },
  chat: { provider: 'ollama', model: 'qwen:1.8b', apiKey: 'sk-avfyfwkuldvsrpmlcsmpcvudhxwtysdraiqftbymikzydhyh', baseUrl: 'https://api.siliconflow.cn/v1', ollamaUrl: 'http://127.0.0.1:11434', temperature: 0.7, maxTokens: 2048 },
  search: { localThreshold: 0.75, topK: 5, defaultCategoryId: 1, historyRounds: 3, historyMaxChars: 3000, contextThreshold: 0.55, contextMaxChars: 4000, autoWriteEnabled: 0, autoWriteThreshold: 0.30 },
  authSession: { jwtSecret: '', jwtExpireMinutes: 43200, codeTtlSeconds: 300, codeResendIntervalSeconds: 60, sessionTtlMinutes: 120, sessionRenewThresholdMinutes: 60 }
})

// 表单配置对象
const form = ref(defaultForm())

// 默认 API Key，切换 provider 时自动填充
const API_KEY = 'sk-avfyfwkuldvsrpmlcsmpcvudhxwtysdraiqftbymikzydhyh'

/**
 * 监听 Embedding provider 切换，自动填充默认 API Key
 */
watch(() => form.value.embedding.provider, (val) => {
  if (val === 'siliconflow' && !form.value.embedding.apiKey) {
    form.value.embedding.apiKey = API_KEY
  }
})

/**
 * 监听 Chat provider 切换，自动填充默认 API Key
 */
watch(() => form.value.chat.provider, (val) => {
  if (val === 'siliconflow' && !form.value.chat.apiKey) {
    form.value.chat.apiKey = API_KEY
  }
})

/**
 * 监听 Rerank provider 切换，自动填充默认 API Key
 */
watch(() => form.value.rerank.provider, (val) => {
  if (val === 'siliconflow' && !form.value.rerank.apiKey) {
    form.value.rerank.apiKey = API_KEY
  }
})

/**
 * 加载配置数据
 */
async function load() {
  try {
    const data = await getModelConfig()
    if (data) {
      form.value = {
        milvus: data.milvus || defaultForm().milvus,
        embedding: data.embedding || defaultForm().embedding,
        rerank: data.rerank || defaultForm().rerank,
        chat: data.chat || defaultForm().chat,
        search: data.search || defaultForm().search,
        authSession: data.authSession || defaultForm().authSession
      }
    }
  } catch (e) {
    ElMessage.error('加载配置失败')
  }
}

/**
 * 保存配置
 */
async function save() {
  saving.value = true
  try {
    await updateModelConfig(form.value)
    ElMessage.success('保存成功')
    await load()
  } catch (e) {
    // request.js 已统一提示错误
  } finally {
    saving.value = false
  }
}

/**
 * 刷新配置缓存
 */
async function refreshCache() {
  refreshing.value = true
  try {
    await refreshModelConfigCache()
    ElMessage.success('缓存已刷新')
  } catch (e) {
    // request.js 已统一提示错误
  } finally {
    refreshing.value = false
  }
}

// 页面挂载后加载配置
onMounted(load)
</script>

<style scoped>
.page { padding: 20px; }
.header { display: flex; justify-content: space-between; align-items: center; }
.header span { font-weight: 600; }
</style>
