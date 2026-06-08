<template>
  <!-- 会员页面不显示 -->
  <div v-if="!isMembershipPage" class="global-ai-chat">
    <!-- 悬浮按钮 -->
    <button
      class="ai-fab"
      :class="{ open: panelOpen }"
      @click="togglePanel"
      aria-label="AI 助手"
    >
      <svg v-if="!panelOpen" viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
      </svg>
      <svg v-else viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
      </svg>
    </button>

    <!-- 滑出面板 -->
    <Transition name="slide-panel">
      <div v-if="panelOpen" class="ai-panel">
        <div class="panel-header">
          <h3>AI 健身教练</h3>
        </div>

        <!-- 非会员提示 -->
        <div v-if="!isVip" class="vip-gate">
          <div class="vip-icon">
            <svg viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="#7EB8DA" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/>
            </svg>
          </div>
          <p class="vip-title">开通会员解锁 AI 助手</p>
          <p class="vip-desc">AI 智能健身教练，为你定制专属训练计划</p>
          <button class="vip-btn" @click="goMembership">查看会员方案</button>
        </div>

        <!-- 聊天区域（会员可见） -->
        <template v-else>
          <div class="chat-messages" ref="chatContainer">
            <div v-if="chatMessages.length === 0" class="chat-empty">
              <p>你好！我是你的 AI 健身教练</p>
              <p>可以问我任何健身相关的问题</p>
            </div>
            <div
              v-for="(msg, i) in chatMessages"
              :key="i"
              class="chat-bubble"
              :class="msg.role"
            >
              <div class="bubble-label">{{ msg.role === 'user' ? '你' : 'AI 教练' }}</div>
              <div class="bubble-content" v-html="formatMessage(msg.content)"></div>
            </div>
            <div v-if="streaming && !streamingHasContent" class="chat-bubble assistant">
              <div class="bubble-label">AI 教练</div>
              <div class="bubble-content thinking">正在思考...</div>
            </div>
          </div>

          <div class="chat-input">
            <el-input
              v-model="chatInput"
              type="textarea"
              :rows="2"
              placeholder="输入你的问题... Ctrl+Enter 发送"
              @keydown.enter.ctrl="handleSend"
              @keydown.enter.meta="handleSend"
            />
            <button
              class="send-btn"
              @click="handleSend"
              :disabled="!chatInput.trim() || streaming"
            >
              {{ streaming ? '回复中...' : '发送' }}
            </button>
          </div>
        </template>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { streamChat } from '@/api/ai.js'
import { request } from '@/api/request.js'

const route = useRoute()
const router = useRouter()

const panelOpen = ref(false)
const isVip = ref(false)
const chatMessages = ref([])
const chatInput = ref('')
const streaming = ref(false)
const chatContainer = ref(null)

const isMembershipPage = computed(() => route.path === '/membership')

const streamingHasContent = computed(() => {
  if (chatMessages.value.length === 0) return false
  const last = chatMessages.value[chatMessages.value.length - 1]
  return last.role === 'assistant' && last.content.length > 0
})

async function checkVipStatus() {
  try {
    const data = await request('/user/vip-status')
    isVip.value = data.isVip
  } catch {
    isVip.value = false
  }
}

function togglePanel() {
  panelOpen.value = !panelOpen.value
  if (panelOpen.value) {
    checkVipStatus()
  }
}

function goMembership() {
  panelOpen.value = false
  router.push('/membership')
}

function formatMessage(content) {
  return content
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    // 去掉行首的 markdown 标题符号（# ## ### 等）
    .replace(/^#{1,6}\s*/gm, '')
    // 去掉 emoji（Unicode emoji 范围）
    .replace(/[\u{1F000}-\u{1FFFF}\u{2600}-\u{27BF}\u{FE00}-\u{FEFF}]/gu, '')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\n/g, '<br>')
}

function scrollToBottom() {
  nextTick(() => {
    if (chatContainer.value) {
      chatContainer.value.scrollTop = chatContainer.value.scrollHeight
    }
  })
}

async function handleSend() {
  const msg = chatInput.value.trim()
  if (!msg || streaming.value || !isVip.value) return
  chatInput.value = ''

  chatMessages.value.push({ role: 'user', content: msg })
  chatMessages.value.push({ role: 'assistant', content: '' })
  const assistantIdx = chatMessages.value.length - 1

  streaming.value = true
  scrollToBottom()

  const history = chatMessages.value.slice(0, -1).map(m => ({
    role: m.role,
    content: m.content
  }))

  await streamChat(history, {}, {
    onToken(token) {
      // 检查是否是错误消息
      if (token.startsWith('[ERROR]')) {
        chatMessages.value[assistantIdx].content = token.replace('[ERROR] ', '')
        isVip.value = false
        streaming.value = false
        return
      }
      chatMessages.value[assistantIdx].content += token
      scrollToBottom()
    },
    onDone() {
      streaming.value = false
      scrollToBottom()
    },
    onError() {
      streaming.value = false
      if (!chatMessages.value[assistantIdx].content) {
        chatMessages.value[assistantIdx].content = '服务暂时不可用，请稍后重试'
      }
      scrollToBottom()
    }
  })
}

onMounted(() => {
  // 预检查 VIP 状态
  const token = localStorage.getItem('token')
  if (token) {
    checkVipStatus()
  }
})
</script>

<style scoped>
.global-ai-chat {
  position: relative;
  z-index: 9999;
}

/* ── 悬浮按钮 ── */
.ai-fab {
  position: fixed;
  right: 24px;
  top: 50%;
  transform: translateY(-50%);
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: #7EB8DA;
  color: #FFFFFF;
  border: none;
  box-shadow: 0 4px 16px rgba(126, 184, 218, 0.4);
  cursor: pointer;
  display: grid;
  place-items: center;
  transition: all 0.3s ease;
  z-index: 10000;
}

.ai-fab:hover {
  transform: translateY(-50%) scale(1.08);
  box-shadow: 0 6px 24px rgba(126, 184, 218, 0.5);
}

.ai-fab.open {
  right: 424px;
  background: #6AABC8;
}

/* ── 滑出面板 ── */
.ai-panel {
  position: fixed;
  top: 0;
  right: 0;
  width: 400px;
  height: 100vh;
  background: #FFFFFF;
  box-shadow: -4px 0 24px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  z-index: 9999;
}

.slide-panel-enter-active,
.slide-panel-leave-active {
  transition: transform 0.3s cubic-bezier(0.22, 1, 0.36, 1);
}

.slide-panel-enter-from,
.slide-panel-leave-to {
  transform: translateX(100%);
}

/* ── 面板头部 ── */
.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid #E8ECF1;
  flex-shrink: 0;
}

.panel-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
}

.panel-badge {
  padding: 4px 10px;
  font-size: 11px;
  font-weight: 700;
  color: #7EB8DA;
  background: rgba(126, 184, 218, 0.1);
  border-radius: 8px;
}

/* ── 非会员拦截 ── */
.vip-gate {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  text-align: center;
}

.vip-icon {
  margin-bottom: 20px;
  opacity: 0.7;
}

.vip-title {
  margin: 0 0 8px;
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
}

.vip-desc {
  margin: 0 0 24px;
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.5;
}

.vip-btn {
  padding: 12px 32px;
  background: #7EB8DA;
  color: #FFFFFF;
  border: none;
  border-radius: 999px;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
}

.vip-btn:hover {
  background: #6AABC8;
  transform: translateY(-1px);
}

/* ── 聊天区域 ── */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.chat-messages::-webkit-scrollbar {
  width: 4px;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: #E8ECF1;
  border-radius: 999px;
}

.chat-empty {
  text-align: center;
  padding: 40px 20px;
  color: var(--text-tertiary);
}

.chat-empty p {
  margin: 0 0 8px;
  font-size: 14px;
}

.chat-empty p:first-child {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-secondary);
}

.chat-bubble {
  display: flex;
  flex-direction: column;
  gap: 4px;
  max-width: 88%;
}

.chat-bubble.user {
  align-self: flex-end;
}

.chat-bubble.assistant {
  align-self: flex-start;
}

.bubble-label {
  font-size: 11px;
  font-weight: 700;
  color: var(--text-tertiary);
  padding: 0 4px;
}

.chat-bubble.user .bubble-label {
  text-align: right;
}

.bubble-content {
  padding: 10px 14px;
  border-radius: 14px;
  font-size: 13px;
  line-height: 1.7;
  color: var(--text-primary);
  word-break: break-word;
}

.chat-bubble.user .bubble-content {
  background: #7EB8DA;
  color: #FFFFFF;
  border-bottom-right-radius: 4px;
}

.chat-bubble.assistant .bubble-content {
  background: #F8F9FA;
  border: 1px solid #E8ECF1;
  border-bottom-left-radius: 4px;
}

.bubble-content.thinking {
  color: var(--text-tertiary);
  font-style: italic;
}

.bubble-content :deep(strong) {
  font-weight: 700;
}

/* ── 输入区域 ── */
.chat-input {
  flex-shrink: 0;
  padding: 12px 16px;
  border-top: 1px solid #E8ECF1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.chat-input :deep(.el-textarea__inner) {
  border-radius: 12px;
  background: #F8F6F3;
  border: 1px solid #E8ECF1;
  box-shadow: none;
  resize: none;
  font-size: 13px;
}

.chat-input :deep(.el-textarea__inner:focus) {
  border-color: #7EB8DA;
}

.send-btn {
  align-self: flex-end;
  padding: 6px 20px;
  border: none;
  border-radius: 999px;
  background: #7EB8DA;
  color: #FFFFFF;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
}

.send-btn:hover:not(:disabled) {
  background: #6AABC8;
}

.send-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

@media (max-width: 640px) {
  .ai-panel {
    width: 100%;
  }

  .ai-fab.open {
    right: 24px;
    z-index: 10001;
  }
}
</style>
