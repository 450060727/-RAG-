<!-- 用户端智能问答页 -->
<template>
  <view class="page">
    <!-- 顶部分类选择栏 -->
    <view class="header-glow">
      <view class="category-bar">
        <!-- 知识库分类选择器 -->
        <picker mode="selector" :range="categoryNames" :value="categoryIndex" @change="onCategoryChange">
          <view class="category-picker">
            <text class="picker-icon">◈</text>
            <text>{{ categoryNames[categoryIndex] || '选择知识库' }}</text>
          </view>
        </picker>
      </view>
    </view>

    <!-- 消息列表：scroll-y 启用纵向滚动 -->
    <scroll-view class="chat-list" scroll-y :scroll-top="scrollTop" enhanced :show-scrollbar="false">
      <!-- 遍历消息列表 -->
      <view v-for="(msg, index) in messages" :key="index" class="msg-row" :class="msg.role">
        <view class="avatar" :class="msg.role">{{ msg.role === 'user' ? '我' : 'AI' }}</view>
        <view class="bubble">
          <text class="content">{{ msg.content }}</text>
          <!-- 参考来源：可折叠 -->
          <view v-if="msg.role === 'assistant' && msg.sources && msg.sources.length" class="sources">
            <view class="source-title" @click="msg.showSources = !msg.showSources">参考来源（{{ msg.sources.length }}）</view>
            <view v-if="msg.showSources" class="source-list">
              <view v-for="(s, i) in msg.sources" :key="i" class="source-item">
                <text class="source-name">[{{ i + 1 }}] {{ s.title }}</text>
                <text class="source-content">{{ s.content }}</text>
              </view>
            </view>
          </view>
          <!-- 反馈操作 -->
          <view v-if="msg.role === 'assistant'" class="actions">
            <button size="mini" type="default" @click="feedback(msg, 'up', true)">👍 有用并加入知识库</button>
            <button size="mini" type="default" @click="feedback(msg, 'up', false)">👍 有用</button>
            <button size="mini" type="default" @click="feedback(msg, 'down', false)">👎 无用</button>
          </view>
          <view v-if="msg.feedback" class="feedback-tag">已反馈：{{ msg.feedback === 'up' ? '有用' : '无用' }}</view>
        </view>
      </view>
      <!-- 加载中占位 -->
      <view v-if="loading" class="msg-row assistant">
        <view class="avatar assistant">AI</view>
        <view class="bubble">
          <text class="content thinking">思考中<text class="dot">...</text></text>
        </view>
      </view>
    </scroll-view>

    <!-- 底部输入区 -->
    <view class="input-area">
      <view class="input-bar">
        <input class="input" v-model="question" placeholder="请输入问题..." placeholder-class="input-placeholder"
          confirm-type="send" @confirm="send" />
        <button class="send-btn" type="primary" @click="send" :disabled="loading || !question.trim()">
          <text class="send-icon">➤</text>
        </button>
      </view>
      <!-- 适配底部安全区 -->
      <view class="safe-area"></view>
    </view>
  </view>
</template>

<script setup>
/**
 * 智能问答页逻辑
 * - 选择知识库分类后发起对话
 * - 展示用户问题与 AI 回答，支持查看参考来源与反馈
 * - 未登录时跳转登录页
 */
import { ref, onMounted, nextTick } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { chat, chatFeedback, getKbCategories } from '@/api/kb.js'

// 知识库分类数据
const categories = ref([])
// 分类名称列表（用于 picker）
const categoryNames = ref(['默认知识库'])
// 当前选中的分类索引
const categoryIndex = ref(0)

// 会话状态
const messages = ref([])
// 当前输入问题
const question = ref('')
// 回答加载中状态
const loading = ref(false)
// 滚动位置，用于滚动到底部
const scrollTop = ref(0)
// 当前会话 ID
const sessionId = ref(null)

/**
 * 页面显示时校验登录态
 */
onShow(() => {
  const token = uni.getStorageSync('token')
  if (!token) {
    uni.navigateTo({ url: '/pages/login/login' })
  }
})

/**
 * 页面加载后拉取分类列表
 */
onMounted(async () => {
  try {
    categories.value = await getKbCategories()
    categoryNames.value = categories.value.map(c => c.name)
    if (categories.value.length) {
      categoryIndex.value = 0
    }
  } catch (e) {
    console.warn('加载分类失败', e)
  }
})

/**
 * 切换分类时清空当前会话与消息
 * @param {Object} e picker 变化事件
 */
function onCategoryChange(e) {
  categoryIndex.value = e.detail.value
  sessionId.value = null
  messages.value = []
}

/**
 * 发送问题并请求回答
 */
async function send() {
  const q = question.value.trim()
  if (!q || loading.value) return

  const token = uni.getStorageSync('token')
  if (!token) {
    uni.navigateTo({ url: '/pages/login/login' })
    return
  }

  // 先展示用户消息
  messages.value.push({ role: 'user', content: q })
  question.value = ''
  scrollToBottom()

  loading.value = true
  try {
    const categoryId = categories.value[categoryIndex.value]?.id || 1
    const res = await chat({ categoryId, question: q, sessionId: sessionId.value })
    sessionId.value = res.sessionId
    messages.value.push({
      role: 'assistant',
      content: res.answer,
      sources: res.sources || [],
      messageId: res.messageId,
      showSources: false,
      feedback: null
    })
    scrollToBottom()
  } catch (e) {
    messages.value.push({ role: 'assistant', content: '抱歉，回答生成失败：' + (e.message || '请稍后重试') })
    scrollToBottom()
  } finally {
    loading.value = false
  }
}

/**
 * 提交反馈
 * @param {Object} msg 消息对象
 * @param {string} type 反馈类型 up/down
 * @param {boolean} writeBack 是否回写知识库
 */
async function feedback(msg, type, writeBack) {
  if (!msg.messageId || msg.feedback) return
  try {
    await chatFeedback(msg.messageId, { feedback: type, writeBack })
    msg.feedback = type
    uni.showToast({ title: writeBack ? '已加入知识库候选' : '反馈已提交', icon: 'none' })
  } catch (e) {
    uni.showToast({ title: e.message || '反馈失败', icon: 'none' })
  }
}

/**
 * 滚动到底部
 */
function scrollToBottom() {
  nextTick(() => {
    scrollTop.value = scrollTop.value + 9999
  })
}
</script>

<style scoped>
/* 页面容器：纵向 flex，渐变背景 */
.page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, #f0f7ff 0%, #f8fbff 50%, #f5f0ff 100%);
  position: relative;
  overflow: hidden;
  box-sizing: border-box;
}

/* 背景光晕装饰层：pointer-events:none 避免遮挡点击 */
.page::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background:
    radial-gradient(circle at 20% 30%, rgba(14, 165, 233, 0.06) 0%, transparent 40%),
    radial-gradient(circle at 80% 70%, rgba(99, 102, 241, 0.04) 0%, transparent 40%);
  pointer-events: none;
}

.header-glow {
  position: relative;
  z-index: 10;
  background: rgba(255, 255, 255, 0.9);
  border-bottom: 1px solid rgba(14, 165, 233, 0.12);
  backdrop-filter: blur(20px);
  box-shadow: 0 4px 24px rgba(14, 165, 233, 0.06);
}

.category-bar {
  padding: 12px 16px;
}

.category-picker {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  color: #0ea5e9;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.picker-icon {
  font-size: 16px;
  color: #6366f1;
}

.chat-list {
  flex: 1;
  padding: 16px 16px 80px 16px;
  overflow-y: auto;
  position: relative;
  z-index: 1;
  box-sizing: border-box;
}

.msg-row {
  display: flex;
  margin-bottom: 18px;
  align-items: flex-start;
  gap: 10px;
}

/* 用户消息行：靠右对齐 */
.msg-row.user {
  flex-direction: row-reverse;
}

.avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  flex-shrink: 0;
  color: #fff;
}

.avatar.user {
  background: linear-gradient(135deg, #0ea5e9, #6366f1);
  box-shadow: 0 4px 12px rgba(14, 165, 233, 0.3);
}

.avatar.assistant {
  background: linear-gradient(135deg, #6366f1, #0ea5e9);
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
}

.bubble {
  max-width: 74%;
  padding: 12px 14px;
  border-radius: 18px;
  background: #ffffff;
  border: 1px solid rgba(14, 165, 233, 0.1);
  box-shadow: 0 4px 16px rgba(14, 165, 233, 0.08);
}

/* 用户消息气泡：渐变背景 */
.msg-row.user .bubble {
  background: linear-gradient(135deg, #0ea5e9, #6366f1);
  border: none;
  box-shadow: 0 4px 16px rgba(14, 165, 233, 0.25);
}

.msg-row.assistant .bubble {
  background: #ffffff;
}

.content {
  font-size: 15px;
  line-height: 1.6;
  word-break: break-all;
  color: #334155;
}

.msg-row.user .content {
  color: #fff;
}

.thinking {
  color: #0ea5e9;
}

/* 思考中省略号闪烁动画 */
.dot {
  animation: blink 1.5s infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}

.sources {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed rgba(14, 165, 233, 0.2);
}

.source-title {
  font-size: 13px;
  color: #0ea5e9;
  font-weight: 600;
}

.source-list {
  margin-top: 8px;
}

.source-item {
  margin-bottom: 8px;
  padding: 8px;
  background: #f8fafc;
  border-radius: 8px;
  border-left: 3px solid #0ea5e9;
}

.source-name {
  font-size: 12px;
  color: #64748b;
}

/* 多行文本截断，webkit 私有属性兼容 */
.source-content {
  font-size: 12px;
  color: #475569;
  margin-top: 4px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.actions {
  margin-top: 10px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.feedback-tag {
  margin-top: 8px;
  font-size: 12px;
  color: #0ea5e9;
}

/* 底部输入区：固定定位，适配安全区 */
.input-area {
  position: fixed;
  left: 0;
  right: 0;
  bottom: var(--window-bottom, 0px);
  z-index: 100;
  padding: 10px 16px;
  background: rgba(255, 255, 255, 0.96);
  border-top: 1px solid rgba(14, 165, 233, 0.12);
  backdrop-filter: blur(20px);
  box-shadow: 0 -4px 24px rgba(14, 165, 233, 0.08);
}

.input-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  max-width: 800px;
  margin: 0 auto;
}

.input {
  flex: 1;
  height: 44px;
  padding: 0 18px;
  background: #f1f5f9;
  border-radius: 24px;
  border: 1px solid rgba(14, 165, 233, 0.15);
  font-size: 15px;
  color: #334155;
  box-shadow: inset 0 1px 4px rgba(0, 0, 0, 0.04);
}

.input-placeholder {
  color: #94a3b8;
}

/* 发送按钮：圆形渐变 */
.send-btn {
  width: 46px;
  height: 46px;
  border-radius: 50%;
  background: linear-gradient(135deg, #0ea5e9, #6366f1) !important;
  border: none !important;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 16px rgba(14, 165, 233, 0.3);
  padding: 0;
  margin: 0;
}

/* 去除 uni-app 按钮默认 after 边框 */
.send-btn::after {
  border: none;
}

.send-icon {
  color: #fff;
  font-size: 16px;
  margin-left: 2px;
}

.safe-area {
  height: env(safe-area-inset-bottom);
  min-height: 4px;
}

/* 隐藏 scroll-view 与页面滚动条 */
.chat-list ::-webkit-scrollbar,
page ::-webkit-scrollbar {
  display: none;
}
</style>
