<template>
  <div class="page-container">
    <!-- Page Header -->
    <div class="page-header">
      <h2 class="page-header-title">消息通知</h2>
    </div>

    <!-- Tabs + Actions -->
    <div class="content-card mb-16">
      <div class="message-toolbar">
        <div class="message-tabs">
          <button
            v-for="tab in tabs"
            :key="tab.value"
            class="message-tab-btn"
            :class="{ active: activeTab === tab.value }"
            @click="handleTabChange(tab.value)"
          >
            <span>{{ tab.label }}</span>
            <span v-if="tab.value === 1 && unreadCount > 0" class="tab-badge">
              {{ unreadCount > 99 ? '99+' : unreadCount }}
            </span>
          </button>
        </div>
        <div class="message-actions">
          <el-button
            v-if="activeTab === 1 && unreadMessages.length > 0"
            type="primary"
            size="small"
            :loading="batchReadLoading"
            @click="handleBatchRead"
          >
            <el-icon class="mr-8"><Select /></el-icon>
            全部已读
          </el-button>
          <el-button size="small" :icon="'Refresh'" @click="fetchMessages">
            刷新
          </el-button>
        </div>
      </div>
    </div>

    <!-- Dev Notice Banner -->
    <transition name="el-fade-in">
      <div v-if="showDevNotice" class="dev-notice mb-16">
        <div class="dev-notice-content">
          <el-icon :size="20" color="#f59e0b"><WarningFilled /></el-icon>
          <span>消息中心功能正在努力开发中，当前展示为示例数据</span>
        </div>
        <el-button
          text
          :icon="'Close'"
          class="dev-notice-close"
          @click="showDevNotice = false"
        />
      </div>
    </transition>

    <!-- Message List -->
    <div class="content-card" v-loading="loading">
      <template v-if="messages.length > 0">
        <!-- Message List -->
        <div class="message-list">
          <div
            v-for="msg in messages"
            :key="msg.id"
            class="message-item"
            :class="{
              'message-item--unread': msg.status === 1,
              'message-item--urgent': msg.type === 2,
            }"
            @click="handleMessageClick(msg)"
            @mouseenter="startAutoRead(msg)"
            @mouseleave="cancelAutoRead(msg.id)"
          >
            <!-- Message icon (left) -->
            <div class="message-item-icon">
              <el-icon :size="18" :color="getMessageIconColor(msg.type)">
                <component :is="getMessageIcon(msg.type)" />
              </el-icon>
            </div>

            <!-- Message body (center) -->
            <div class="message-item-body">
              <div class="message-item-tag">
                <span class="msg-tag" :class="getMessageTagClass(msg.type)">
                  {{ getMessageTagText(msg.type) }}
                </span>
                <span v-if="msg.status === 1" class="unread-dot"></span>
              </div>
              <div class="message-item-text">
                {{ msg.content }}
              </div>
              <!-- Type 4: expandable details on hover -->
              <transition name="el-fade-in">
                <div
                  v-if="msg.type === 4 && msg.details && hoveredMsgId === msg.id"
                  class="message-item-details"
                >
                  {{ msg.details }}
                </div>
              </transition>
            </div>

            <!-- Time (right) -->
            <div class="message-item-time">
              <span class="message-time">{{ formatMessageTime(msg.createTime) }}</span>
              <span v-if="msg.status === 2" class="message-read-badge">已读</span>
            </div>
          </div>
        </div>

        <!-- Pagination -->
        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.pageSize"
            :total="pagination.total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            small
            @change="fetchMessages"
          />
        </div>
      </template>

      <!-- Empty -->
      <el-empty v-else-if="!loading" :description="activeTab === 1 ? '暂无未读消息' : '暂无已读消息'">
        <template #image>
          <el-icon :size="60" color="#ccc"><Message /></el-icon>
        </template>
      </el-empty>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  getMessagePage,
  getCountUnread,
  batchReadMessages,
  markMessageRead,
  type MessageRecord,
} from '@/api/modules/message'
import dayjs from 'dayjs'

const router = useRouter()

// ==================== Tabs ====================
const tabs = [
  { label: '未读消息', value: 1 },
  { label: '已读消息', value: 2 },
]

const activeTab = ref(1)

function handleTabChange(value: number) {
  activeTab.value = value
  pagination.value.page = 1
  fetchMessages()
}

// ==================== Dev notice (API now working) ====================
const showDevNotice = ref(false)  // 不再默认显示

// ==================== Data ====================
const loading = ref(false)
const messages = ref<MessageRecord[]>([])
const unreadCount = ref(0)
const batchReadLoading = ref(false)
const apiFailed = ref(false)

const pagination = ref({
  page: 1,
  pageSize: 10,
  total: 0,
})

const unreadMessages = computed(() =>
  activeTab.value === 1 ? messages.value.filter((m) => m.status === 1) : [],
)

async function fetchMessages() {
  loading.value = true
  apiFailed.value = false

  try {
    // Fetch unread count
    try {
      const countRes = await getCountUnread()
      unreadCount.value = countRes.data.data ?? 0
    } catch {
      unreadCount.value = 0
    }

    // Fetch message page
    const res = await getMessagePage({
      page: pagination.value.page,
      pageSize: pagination.value.pageSize,
      status: activeTab.value,
    })

    messages.value = res.data.data.records ?? []
    pagination.value.total = res.data.data.total ?? 0
  } catch {
    apiFailed.value = true
    showDevNotice.value = true
    messages.value = []
    pagination.value.total = 0
  } finally {
    loading.value = false
  }
}

// ==================== Batch read ====================
async function handleBatchRead() {
  const unreadIds = messages.value.filter((m) => m.status === 1).map((m) => m.id)
  if (!unreadIds.length) return

  batchReadLoading.value = true
  try {
    await batchReadMessages(unreadIds)
    ElMessage.success('已全部标记为已读')
    unreadCount.value = 0
    // Refresh the list
    messages.value = messages.value.map((m) => ({ ...m, status: 2 }))
  } catch {
    ElMessage.error('操作失败')
  } finally {
    batchReadLoading.value = false
  }
}

// ==================== Single message click ====================
function handleMessageClick(msg: MessageRecord) {
  // Navigate based on message type
  const statusMap: Record<number, string> = {
    1: '1', // 待接单
    2: '1', // 急 - 待接单
    3: '3', // 待派送
    4: '3', // 催单 - 派送中
    5: '', // 今日数据 - no filter
  }

  const orderStatus = statusMap[msg.type]
  if (orderStatus !== undefined && orderStatus !== '') {
    router.push(`/order?status=${orderStatus}`)
  }

  // Mark as read immediately on click
  if (msg.status === 1) {
    markAsRead(msg)
  }
}

// ==================== Auto-read on hover (3s) ====================
const hoverTimers = ref<Record<number, ReturnType<typeof setTimeout>>>({})
const hoveredMsgId = ref<number | null>(null)

function startAutoRead(msg: MessageRecord) {
  hoveredMsgId.value = msg.id

  if (msg.status !== 1) return

  // Clear existing timer for this message
  if (hoverTimers.value[msg.id]) {
    clearTimeout(hoverTimers.value[msg.id])
  }

  // Start 3-second timer
  hoverTimers.value[msg.id] = setTimeout(() => {
    markAsRead(msg)
    hoveredMsgId.value = null
  }, 3000)
}

function cancelAutoRead(msgId: number) {
  hoveredMsgId.value = null
  if (hoverTimers.value[msgId]) {
    clearTimeout(hoverTimers.value[msgId])
    delete hoverTimers.value[msgId]
  }
}

async function markAsRead(msg: MessageRecord) {
  try {
    await markMessageRead(msg.id)
    msg.status = 2
    if (unreadCount.value > 0) {
      unreadCount.value--
    }
  } catch {
    // Silently fail for auto-read
  }
}

// ==================== Message rendering helpers ====================
function getMessageTagText(type: number): string {
  const map: Record<number, string> = {
    1: '待接单',
    2: '急',
    3: '待派送',
    4: '催单',
    5: '今日数据',
  }
  return map[type] ?? '通知'
}

function getMessageTagClass(type: number): string {
  const map: Record<number, string> = {
    1: 'msg-tag--pending',
    2: 'msg-tag--urgent',
    3: 'msg-tag--delivering',
    4: 'msg-tag--reminder',
    5: 'msg-tag--daily',
  }
  return map[type] ?? ''
}

function getMessageIcon(type: number): string {
  const map: Record<number, string> = {
    1: 'Document',
    2: 'Warning',
    3: 'Van',
    4: 'Bell',
    5: 'DataAnalysis',
  }
  return map[type] ?? 'InfoFilled'
}

function getMessageIconColor(type: number): string {
  const map: Record<number, string> = {
    1: '#409eff',
    2: '#1a56db',
    3: '#f59e0b',
    4: '#f59e0b',
    5: '#67c23a',
  }
  return map[type] ?? '#909399'
}

function formatMessageTime(time: string): string {
  if (!time) return ''
  const msgTime = dayjs(time)
  const now = dayjs()
  const diffMinutes = now.diff(msgTime, 'minute')

  if (diffMinutes < 1) return '刚刚'
  if (diffMinutes < 60) return `${diffMinutes}分钟前`

  const diffHours = now.diff(msgTime, 'hour')
  if (diffHours < 24) return `${diffHours}小时前`

  const diffDays = now.diff(msgTime, 'day')
  if (diffDays < 7) return `${diffDays}天前`

  return msgTime.format('MM-DD HH:mm')
}

// ==================== Init ====================
fetchMessages()
</script>

<style scoped lang="scss">
// ==================== Toolbar ====================
.message-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
}

.message-tabs {
  display: flex;
  gap: 0;
}

.message-tab-btn {
  position: relative;
  padding: 8px 24px;
  border: none;
  background: transparent;
  cursor: pointer;
  font-size: 15px;
  color: #666;
  border-bottom: 2px solid transparent;
  transition: all 0.25s ease;
  display: flex;
  align-items: center;
  gap: 8px;

  &:hover {
    color: #1a56db;
  }

  &.active {
    color: #1a56db;
    font-weight: 600;
    border-bottom-color: #1a56db;
  }
}

.message-actions {
  display: flex;
  gap: 8px;
}

.tab-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 20px;
  height: 18px;
  padding: 0 6px;
  background: #1a56db;
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  border-radius: 9px;
  line-height: 1;
}

// ==================== Dev Notice ====================
.dev-notice {
  background: #fdf6ec;
  border: 1px solid #f5dab1;
  border-radius: 8px;
  padding: 12px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.dev-notice-content {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #f59e0b;
  font-size: 14px;
}

.dev-notice-close {
  flex-shrink: 0;
  color: #c0c4cc;

  &:hover {
    color: #909399;
  }
}

// ==================== Message List ====================
.message-list {
  display: flex;
  flex-direction: column;
}

.message-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background 0.2s;
  border-radius: 6px;
  margin: 0 -12px;
  padding-left: 12px;
  padding-right: 12px;

  &:hover {
    background: #fafafa;
  }

  &:last-child {
    border-bottom: none;
  }

  &--unread {
    background: rgba(26, 86, 219, 0.02);

    &:hover {
      background: rgba(26, 86, 219, 0.05);
    }
  }

  &--urgent {
    animation: urgentPulse 2s ease-in-out infinite;
  }
}

@keyframes urgentPulse {
  0%,
  100% {
    background: rgba(26, 86, 219, 0.02);
  }
  50% {
    background: rgba(26, 86, 219, 0.06);
  }
}

.message-item-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-top: 2px;
}

.message-item-body {
  flex: 1;
  min-width: 0;
}

.message-item-tag {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.msg-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  line-height: 1.5;

  &--pending {
    background: rgba(64, 158, 255, 0.1);
    color: #409eff;
  }

  &--urgent {
    background: rgba(26, 86, 219, 0.1);
    color: #1a56db;
  }

  &--delivering {
    background: rgba(230, 162, 60, 0.1);
    color: #f59e0b;
  }

  &--reminder {
    background: rgba(255, 193, 7, 0.15);
    color: #b8860b;
  }

  &--daily {
    background: rgba(103, 194, 58, 0.1);
    color: #67c23a;
  }
}

.unread-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #1a56db;
  flex-shrink: 0;
}

.message-item-text {
  font-size: 14px;
  color: #333;
  line-height: 1.6;
  word-break: break-all;
}

.message-item-details {
  margin-top: 8px;
  padding: 10px 12px;
  background: #f9f9f9;
  border-radius: 6px;
  border-left: 3px solid #f59e0b;
  font-size: 13px;
  color: #666;
  line-height: 1.6;
}

.message-item-time {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
  flex-shrink: 0;
}

.message-time {
  font-size: 12px;
  color: #999;
  white-space: nowrap;
}

.message-read-badge {
  font-size: 11px;
  color: #c0c4cc;
  padding: 1px 6px;
  border: 1px solid #e4e7ed;
  border-radius: 3px;
}

// ==================== Pagination ====================
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding: 16px 0 0;
}
</style>
