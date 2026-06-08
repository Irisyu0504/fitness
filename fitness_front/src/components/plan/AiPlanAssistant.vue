<template>
  <div class="assistant-card">
    <div class="card-header">
      <h3>AI 训练计划助手</h3>
      <span class="ai-badge">DeepSeek</span>
    </div>

    <!-- 快速参数表单 -->
    <div class="form-section" :class="{ collapsed: chatMessages.length > 0 }">
      <div class="form-grid">
        <div class="form-item">
          <label>训练目标</label>
          <el-select v-model="form.goal" placeholder="选择目标">
            <el-option label="减脂" value="减脂" />
            <el-option label="增肌" value="增肌" />
            <el-option label="塑形" value="塑形" />
            <el-option label="提升耐力" value="提升耐力" />
            <el-option label="康复训练" value="康复训练" />
          </el-select>
        </div>
        <div class="form-item">
          <label>每周训练次数</label>
          <el-input-number v-model="form.weeklyTimes" :min="1" :max="7" controls-position="right" />
        </div>
        <div class="form-item">
          <label>每次时长(分钟)</label>
          <el-input-number v-model="form.duration" :min="15" :max="180" :step="15" controls-position="right" />
        </div>
        <div class="form-item">
          <label>器械条件</label>
          <el-select v-model="form.equipment" placeholder="选择器械">
            <el-option label="徒手" value="徒手" />
            <el-option label="哑铃" value="哑铃" />
            <el-option label="健身房" value="健身房" />
            <el-option label="跑步机" value="跑步机" />
          </el-select>
        </div>
      </div>
      <div class="form-item full-width">
        <label>当前身体情况</label>
        <el-input v-model="form.bodyCondition" type="textarea" :rows="2" placeholder="如：体重82kg，体脂率22%，有轻微膝盖不适" />
      </div>
      <div class="form-item full-width">
        <label>备注</label>
        <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="其他要求或限制" />
      </div>
      <button class="generate-btn" @click="handleQuickGenerate" :disabled="streaming">
        {{ streaming ? 'AI 思考中...' : '生成训练计划' }}
      </button>
    </div>

    <!-- 聊天区域 -->
    <div v-if="chatMessages.length > 0" class="chat-section">
      <div class="chat-messages" ref="chatContainer">
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
          placeholder="继续对话，例如：帮我调整周三的训练，换成上肢为主..."
          @keydown.enter.ctrl="handleSendChat"
        />
        <button
          class="send-btn"
          @click="handleSendChat"
          :disabled="!chatInput.trim() || streaming"
        >
          {{ streaming ? '回复中...' : '发送' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, nextTick, computed } from 'vue'
import { streamChat } from '@/api/ai.js'

const props = defineProps({
  preview: { type: Object, default: null }
})

const emit = defineEmits(['generate', 'save-preview', 'close-preview'])

const form = reactive({
  goal: '减脂',
  weeklyTimes: 4,
  duration: 60,
  equipment: '健身房',
  bodyCondition: '',
  remark: ''
})

const chatMessages = ref([]) // { role: 'user'|'assistant', content: '...' }
const chatInput = ref('')
const streaming = ref(false)
const chatContainer = ref(null)

const streamingHasContent = computed(() => {
  if (chatMessages.value.length === 0) return false
  const last = chatMessages.value[chatMessages.value.length - 1]
  return last.role === 'assistant' && last.content.length > 0
})

function buildContext() {
  return {
    '训练目标': form.goal,
    '每周训练': form.weeklyTimes + ' 次',
    '单次时长': form.duration + ' 分钟',
    '可用器材': form.equipment,
    '身体状况': form.bodyCondition || '无特殊说明',
    '备注': form.remark || '无'
  }
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

async function callAiStream(userMessage) {
  // 添加用户消息
  chatMessages.value.push({ role: 'user', content: userMessage })

  // 添加空的 assistant 消息用于流式填充
  chatMessages.value.push({ role: 'assistant', content: '' })
  const assistantIdx = chatMessages.value.length - 1

  streaming.value = true
  scrollToBottom()

  // 构造对话历史（不含最后一条空的 assistant）
  const history = chatMessages.value.slice(0, -1).map(m => ({
    role: m.role,
    content: m.content
  }))

  await streamChat(history, buildContext(), {
    onToken(token) {
      chatMessages.value[assistantIdx].content += token
      scrollToBottom()
    },
    onDone() {
      streaming.value = false
      scrollToBottom()
    },
    onError(err) {
      streaming.value = false
      if (!chatMessages.value[assistantIdx].content) {
        chatMessages.value[assistantIdx].content = '抱歉，AI 服务暂时不可用，请稍后重试。'
      }
      scrollToBottom()
    }
  })
}

async function handleQuickGenerate() {
  const userMsg = `请根据以下条件生成一份详细的训练计划：\n` +
    `- 目标：${form.goal}\n` +
    `- 每周训练：${form.weeklyTimes} 次\n` +
    `- 每次时长：${form.duration} 分钟\n` +
    `- 器械条件：${form.equipment}\n` +
    (form.bodyCondition ? `- 身体状况：${form.bodyCondition}\n` : '') +
    (form.remark ? `- 备注：${form.remark}\n` : '')

  await callAiStream(userMsg)
}

async function handleSendChat() {
  const msg = chatInput.value.trim()
  if (!msg || streaming.value) return
  chatInput.value = ''
  await callAiStream(msg)
}
</script>

<style scoped>
.assistant-card {
  background: #FFFFFF;
  border-radius: 24px;
  padding: 28px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
  height: 100%;
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.card-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
}

.ai-badge {
  padding: 4px 10px;
  font-size: 11px;
  font-weight: 700;
  color: #7EB8DA;
  background: rgba(126, 184, 218, 0.1);
  border-radius: 8px;
}

/* ── 表单 ── */
.form-section {
  flex-shrink: 0;
  transition: all 0.3s ease;
}

.form-section.collapsed {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #E8ECF1;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
  margin-bottom: 14px;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-item.full-width {
  margin-bottom: 14px;
}

.form-item label {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-secondary);
}

.form-item :deep(.el-input__wrapper),
.form-item :deep(.el-select .el-input__wrapper) {
  border-radius: 12px;
  background: #F8F6F3;
  border: 1px solid #E8ECF1;
  box-shadow: none;
}

.form-item :deep(.el-input__wrapper:hover),
.form-item :deep(.el-input__wrapper.is-focus) {
  border-color: #7EB8DA;
}

.form-item :deep(.el-textarea__inner) {
  border-radius: 12px;
  background: #F8F6F3;
  border: 1px solid #E8ECF1;
  box-shadow: none;
}

.form-item :deep(.el-textarea__inner:focus) {
  border-color: #7EB8DA;
}

.generate-btn {
  width: 100%;
  height: 44px;
  border: none;
  border-radius: 999px;
  background: linear-gradient(135deg, #7EB8DA, #B8A9C9);
  color: #FFFFFF;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s ease;
}

.generate-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(126, 184, 218, 0.3);
}

.generate-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

/* ── 聊天区域 ── */
.chat-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 4px 0;
  margin-bottom: 16px;
  min-height: 200px;
  max-height: 500px;
}

.chat-messages::-webkit-scrollbar {
  width: 4px;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: #E8ECF1;
  border-radius: 999px;
}

.chat-bubble {
  display: flex;
  flex-direction: column;
  gap: 4px;
  max-width: 90%;
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
  padding: 12px 16px;
  border-radius: 16px;
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

/* ── 聊天输入 ── */
.chat-input {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.chat-input :deep(.el-textarea__inner) {
  border-radius: 14px;
  background: #F8F6F3;
  border: 1px solid #E8ECF1;
  box-shadow: none;
  resize: none;
}

.chat-input :deep(.el-textarea__inner:focus) {
  border-color: #7EB8DA;
}

.send-btn {
  align-self: flex-end;
  padding: 8px 24px;
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
  .form-grid {
    grid-template-columns: 1fr;
  }

  .chat-messages {
    max-height: 350px;
  }
}
</style>
