<template>
  <div class="ai-chat-container">
    <!-- ==================== 欢迎页 / 聊天区 ==================== -->
    <div v-if="messages.length === 0" class="welcome-area">
      <div class="welcome-icon">
        <el-icon :size="64"><ChatDotRound /></el-icon>
      </div>
      <h2 class="welcome-title">AI 智能分析助手</h2>
      <p class="welcome-desc">
        我是速达外卖的数据分析助手，可以帮你查询和分析店铺业务数据。
        <br />直接输入问题，我会边想边回答。
      </p>

      <!-- 一键诊断按钮 -->
      <el-button
        type="warning"
        :icon="TrendCharts"
        :loading="isDiagnosing"
        size="large"
        round
        class="diagnose-btn"
        @click="startDiagnose()"
      >
        {{ isDiagnosing ? '正在生成报告…' : '📊 一键经营诊断' }}
      </el-button>

      <div class="example-questions">
        <p class="example-label">💡 试试这些问题：</p>
        <div
          v-for="(q, i) in exampleQuestions"
          :key="i"
          class="example-chip"
          @click="sendMessage(q)"
        >
          {{ q }}
        </div>
      </div>
    </div>

    <!-- ==================== 消息列表 ==================== -->
    <div v-else class="message-list" ref="messageListRef">
      <div
        v-for="(msg, i) in messages"
        :key="i"
        class="message-item"
        :class="msg.role"
      >
        <!-- 头像 -->
        <div class="msg-avatar">
          <el-avatar v-if="msg.role === 'user'" :size="36" icon="UserFilled" />
          <el-avatar v-else :size="36" style="background-color: #1a56db">
            <el-icon><Cpu /></el-icon>
          </el-avatar>
        </div>
        <!-- 气泡 -->
        <div class="msg-body">
          <div class="msg-role-label">
            {{ msg.role === 'user' ? '你' : 'AI 助手' }}
          </div>
          <div class="msg-content" v-text="msg.content" />
          <!-- 打字中光标 -->
          <span v-if="msg.typing" class="typing-cursor">▌</span>
        </div>
      </div>
    </div>

    <!-- ==================== 底部输入区 ==================== -->
    <div class="input-area">
      <el-input
        v-model="inputText"
        type="textarea"
        :rows="2"
        placeholder="输入你想问的问题，例如：最近7天营业额多少？"
        :disabled="isStreaming"
        resize="none"
        @keydown.enter.exact.prevent="sendMessage(inputText)"
      />
      <el-button
        type="primary"
        :icon="isStreaming ? undefined : Promotion"
        :loading="isStreaming"
        :disabled="!inputText.trim()"
        class="send-btn"
        @click="sendMessage(inputText)"
      >
        {{ isStreaming ? '思考中…' : '发送' }}
      </el-button>
      <el-button
        v-if="isStreaming"
        type="danger"
        plain
        @click="stopStreaming"
      >
        停止
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue'
import { ChatDotRound, Cpu, Promotion, TrendCharts } from '@element-plus/icons-vue'
import { streamQuery, streamDiagnose } from '@/api/modules/ai'

// ==================== 数据 ====================

interface ChatMessage {
  role: 'user' | 'ai'
  content: string
  typing?: boolean // 是否正在打字中
}

const messages = ref<ChatMessage[]>([])
const inputText = ref('')
const isStreaming = ref(false)
const isDiagnosing = ref(false)
const messageListRef = ref<HTMLElement | null>(null)
let cancelStream: (() => void) | null = null

const exampleQuestions = [
  '最近7天哪些菜品卖得最好？',
  '本周的营业额大概有多少？',
  '本月新增了多少用户？',
  '今天订单的总体情况怎么样？',
]

// ==================== 方法 ====================

/** 滚动到底部 */
async function scrollToBottom() {
  await nextTick()
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight
  }
}

/** 发送消息 */
function sendMessage(text: string) {
  const question = text.trim()
  if (!question || isStreaming.value) return

  // 加入用户消息
  messages.value.push({ role: 'user', content: question })
  inputText.value = ''

  // 创建一个空的 AI 消息（用来往里填 token）
  const aiMsg: ChatMessage = { role: 'ai', content: '', typing: true }
  messages.value.push(aiMsg)
  isStreaming.value = true
  scrollToBottom()

  // 调用流式 API
  cancelStream = streamQuery(
    question,
    // onToken — 每收到一个字就追加上去
    (token) => {
      aiMsg.content += token
      scrollToBottom()
    },
    // onDone — AI 说完了
    () => {
      aiMsg.typing = false
      isStreaming.value = false
      cancelStream = null
      scrollToBottom()
    },
    // onError
    (err) => {
      aiMsg.typing = false
      if (!aiMsg.content) {
        aiMsg.content = `❌ ${err}`
      } else {
        aiMsg.content += `\n\n❌ ${err}`
      }
      isStreaming.value = false
      cancelStream = null
      scrollToBottom()
    },
  )
}

/** 一键诊断 */
function startDiagnose() {
  if (isStreaming.value || isDiagnosing.value) return

  messages.value.push({ role: 'user', content: '请帮我生成一份经营诊断报告' })
  const aiMsg: ChatMessage = { role: 'ai', content: '', typing: true }
  messages.value.push(aiMsg)
  isStreaming.value = true
  isDiagnosing.value = true
  scrollToBottom()

  cancelStream = streamDiagnose(
    (token) => {
      aiMsg.content += token
      scrollToBottom()
    },
    () => {
      aiMsg.typing = false
      isStreaming.value = false
      isDiagnosing.value = false
      cancelStream = null
      scrollToBottom()
    },
    (err) => {
      aiMsg.typing = false
      aiMsg.content = aiMsg.content || `❌ ${err}`
      isStreaming.value = false
      isDiagnosing.value = false
      cancelStream = null
      scrollToBottom()
    },
  )
}

/** 停止流式输出 */
function stopStreaming() {
  cancelStream?.()
  const lastMsg = messages.value[messages.value.length - 1]
  if (lastMsg && lastMsg.typing) {
    lastMsg.typing = false
    if (!lastMsg.content) lastMsg.content = '（已取消）'
  }
  isStreaming.value = false
  cancelStream = null
}
</script>

<style lang="scss" scoped>
.ai-chat-container {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 130px); // 减去顶部栏 + padding
  max-width: 900px;
  margin: 0 auto;
  position: relative;
}

// ==================== 欢迎区 ====================
.welcome-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 40px 20px;

  .welcome-icon {
    color: #1a56db;
    margin-bottom: 16px;
    opacity: 0.8;
  }

  .welcome-title {
    font-size: 24px;
    font-weight: 700;
    color: #1f2937;
    margin: 0 0 8px;
  }

  .welcome-desc {
    font-size: 14px;
    color: #6b7280;
    line-height: 1.8;
    margin: 0 0 24px;
  }

  .diagnose-btn {
    margin-bottom: 24px;
    font-size: 15px;
    padding: 12px 32px;
  }

  .example-questions {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    justify-content: center;
    max-width: 600px;

    .example-label {
      width: 100%;
      font-size: 13px;
      color: #9ca3af;
      margin: 0;
    }

    .example-chip {
      padding: 8px 16px;
      border-radius: 20px;
      background: #eff6ff;
      color: #1a56db;
      font-size: 13px;
      cursor: pointer;
      transition: all 0.2s;
      border: 1px solid #dbeafe;

      &:hover {
        background: #1a56db;
        color: #fff;
        border-color: #1a56db;
      }
    }
  }
}

// ==================== 消息列表 ====================
.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px 0;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.message-item {
  display: flex;
  gap: 12px;

  &.user {
    flex-direction: row-reverse;

    .msg-role-label {
      text-align: right;
    }

    .msg-content {
      background: #1a56db;
      color: #fff;
      border-radius: 16px 4px 16px 16px;
    }
  }

  &.ai {
    .msg-content {
      background: #fff;
      color: #1f2937;
      border-radius: 4px 16px 16px 16px;
      border: 1px solid #e5e7eb;
      white-space: pre-wrap;
      word-break: break-word;
    }
  }
}

.msg-avatar {
  flex-shrink: 0;
}

.msg-body {
  max-width: 75%;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.msg-role-label {
  font-size: 12px;
  color: #9ca3af;
  margin: 0 8px;
}

.msg-content {
  padding: 12px 16px;
  font-size: 14px;
  line-height: 1.7;
}

// ==================== 打字光标 ====================
.typing-cursor {
  display: inline;
  animation: blink 1s step-end infinite;
  color: #1a56db;
  font-weight: bold;
  margin-left: 2px;
}

@keyframes blink {
  50% {
    opacity: 0;
  }
}

// ==================== 输入区 ====================
.input-area {
  display: flex;
  gap: 10px;
  padding: 16px 0 0;
  border-top: 1px solid #e5e7eb;
  background: inherit;
  align-items: flex-end;

  :deep(.el-textarea__inner) {
    border-radius: 12px;
    font-size: 14px;
    line-height: 1.6;
  }

  .send-btn {
    flex-shrink: 0;
    height: 44px;
    border-radius: 12px;
    padding: 0 24px;
  }
}

// ==================== 暗色模式 ====================
html.dark {
  .welcome-area {
    .welcome-title {
      color: #f1f5f9;
    }
    .welcome-desc {
      color: #94a3b8;
    }
    .example-chip {
      background: #1e293b;
      border-color: #334155;
      &:hover {
        background: #1a56db;
      }
    }
  }

  .message-item.ai .msg-content {
    background: #1e293b;
    color: #e2e8f0;
    border-color: #334155;
  }

  .input-area {
    border-top-color: #334155;
    :deep(.el-textarea__inner) {
      background: #1e293b;
      color: #e2e8f0;
      border-color: #334155;
    }
  }
}
</style>
