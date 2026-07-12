<template>
  <div class="default-layout" :class="{ 'sidebar-collapsed': !appStore.sidebar.opened }">
    <!-- Sidebar -->
    <AppSidebar />

    <!-- Main content area -->
    <div class="main-container">
      <!-- Top navbar -->
      <AppNavbar />

      <!-- Breadcrumb -->
      <AppBreadcrumb />

      <!-- Page content -->
      <div class="page-content">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <keep-alive>
              <component :is="Component" />
            </keep-alive>
          </transition>
        </router-view>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElNotification } from 'element-plus'
import { useAppStore } from '@/stores/app'
import AppSidebar from '@/components/layout/AppSidebar.vue'
import AppNavbar from '@/components/layout/AppNavbar.vue'
import AppBreadcrumb from '@/components/layout/AppBreadcrumb.vue'
import { getToken } from '@/utils/auth'

const router = useRouter()
const appStore = useAppStore()

const cachedViews = computed(() => ['Dashboard', 'Statistics', 'OrderList', 'DishList', 'SetmealList', 'Category', 'EmployeeList', 'MessageCenter', 'AIChat'])

// ==================== WebSocket 实时通知 ====================
let ws: WebSocket | null = null
let reconnectTimer: ReturnType<typeof setTimeout> | null = null

function connectWebSocket() {
  const token = getToken()
  if (!token) return

  const wsUrl = (import.meta as any).env?.VITE_WS_URL || 'ws://localhost:8080/ws/'
  const clientId = localStorage.getItem('ws-client-id') || 'admin-' + Date.now()
  localStorage.setItem('ws-client-id', clientId)

  try {
    ws = new WebSocket(wsUrl + clientId)

    ws.onopen = () => {
      console.log('[WS] 已连接, clientId:', clientId)
    }

    ws.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        if (data.type === 1) {
          ElNotification({
            title: '🔔 新订单提醒',
            message: data.content || '您有新的外卖订单，请及时处理',
            type: 'success',
            duration: 8000,
            onClick: () => router.push('/order?status=1'),
          })
        } else if (data.type === 2) {
          ElNotification({
            title: '📢 客户催单',
            message: data.content || '客户正在催促订单，请尽快处理',
            type: 'warning',
            duration: 8000,
            onClick: () => router.push('/order?status=3'),
          })
        }
      } catch (e) {
        console.warn('[WS] 消息解析失败:', event.data)
      }
    }

    ws.onclose = () => {
      console.log('[WS] 连接断开, 5秒后重连')
      scheduleReconnect()
    }

    ws.onerror = () => {
      console.warn('[WS] 连接错误')
      ws?.close()
    }
  } catch (e) {
    console.warn('[WS] 连接失败')
    scheduleReconnect()
  }
}

function scheduleReconnect() {
  if (reconnectTimer) clearTimeout(reconnectTimer)
  reconnectTimer = setTimeout(connectWebSocket, 5000)
}

onMounted(() => {
  connectWebSocket()
})

onUnmounted(() => {
  ws?.close()
  if (reconnectTimer) clearTimeout(reconnectTimer)
})
</script>

<style lang="scss" scoped>
.default-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  margin-left: 220px;
  transition: margin-left 0.28s;
  overflow: hidden;
  min-width: 0;
}

.sidebar-collapsed {
  .main-container {
    margin-left: 64px;
  }
}

.page-content {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  background-color: #f3f4f7;
}

html.dark .page-content {
  background-color: #0f172a;
}

// Responsive
@media (max-width: 768px) {
  .main-container {
    margin-left: 0;
  }

  .sidebar-collapsed .main-container {
    margin-left: 0;
  }
}
</style>
