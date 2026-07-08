<template>
  <div class="app-sidebar" :class="{ collapsed: isCollapsed, 'entering': appStore.isFirstEnter }">
    <!-- LOGO: Deep blue "速" badge + brand name -->
    <div class="sidebar-logo" @click="goHome">
      <div class="logo-badge">
        <span class="logo-char">速</span>
      </div>
      <transition name="fade">
        <span v-if="!isCollapsed" class="logo-text">速达外卖</span>
      </transition>
    </div>

    <!-- Menu (NO TransitionGroup wrapper — uses CSS animation via .entering class on parent) -->
    <el-scrollbar class="sidebar-menu-wrapper">
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapsed"
        :collapse-transition="false"
        :unique-opened="true"
        :background-color="menuBgColor"
        :text-color="menuTextColor"
        :active-text-color="menuActiveTextColor"
        router
        class="sidebar-menu"
      >
        <AppSidebarItem
          v-for="route in menuRoutes"
          :key="route.path"
          :item="route"
          :base-path="'/'"
        />
      </el-menu>
    </el-scrollbar>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAppStore } from '@/stores/app'
import AppSidebarItem from './AppSidebarItem.vue'
import type { RouteRecordRaw } from 'vue-router'

const router = useRouter()
const route = useRoute()
const appStore = useAppStore()

const isCollapsed = computed(() => !appStore.sidebar.opened)
const activeMenu = computed(() => route.path)

const menuRoutes = computed(() => {
  const routes = router.options.routes
  const defaultRoute = routes.find((r: RouteRecordRaw) => r.path === '/')
  if (!defaultRoute?.children) return []
  return defaultRoute.children.filter(
    (r: RouteRecordRaw) => r.meta && !r.meta.hidden,
  )
})

const menuBgColor = '#0f172a'
const menuTextColor = '#bfcbd9'
const menuActiveTextColor = '#60a5fa'

// After animation completes, clear the first-enter flag
onMounted(() => {
  if (appStore.isFirstEnter) {
    const delay = menuRoutes.value.length * 60 + 600
    setTimeout(() => appStore.finishFirstEnter(), delay)
  }
})

function goHome() {
  router.push('/dashboard')
}
</script>

<style lang="scss" scoped>
/* ── Sidebar shell ── */
.app-sidebar {
  position: fixed; top: 0; left: 0; bottom: 0;
  width: 220px; z-index: 1001;
  background-color: #0f172a;
  transition: width 0.28s;
  display: flex; flex-direction: column; overflow: hidden;
  &.collapsed { width: 64px; }
}

.sidebar-menu-wrapper { flex:1; overflow-y:auto; overflow-x:hidden; }

/* ── Logo ── */
.sidebar-logo {
  height: 56px; display:flex; align-items:center; justify-content:center;
  gap: 10px; cursor:pointer; flex-shrink:0; padding: 0 16px;
  background: linear-gradient(135deg, #1e3a8a, #1a56db);
  overflow: hidden;
}

.logo-badge {
  width: 36px; height: 36px; flex-shrink: 0;
  background: rgba(255,255,255,0.2);
  border-radius: 8px;
  display:flex; align-items:center; justify-content:center;
}

.logo-char { font-size:20px; font-weight:700; color:#fff; line-height:1; }

.logo-text { font-size:16px; font-weight:600; color:#fff; letter-spacing:1px; white-space:nowrap; }

/* ── Menu ── */
.sidebar-menu {
  border:none !important; width:100% !important;
  :deep(.el-menu-item), :deep(.el-sub-menu__title) {
    height:48px; line-height:48px; margin:2px 8px; border-radius:6px;
    &:hover { background-color: rgba(255,255,255,0.06) !important; }
  }
  :deep(.el-menu-item.is-active) { background-color: #1a56db !important; color:#fff !important; }
  :deep(.el-sub-menu .el-menu) { background-color: rgba(0,0,0,0.15) !important; }
  :deep(.el-sub-menu__title) i { color: #bfcbd9; }
}

.collapsed .sidebar-menu :deep(.el-menu-item),
.collapsed .sidebar-menu :deep(.el-sub-menu__title) {
  margin:2px 4px; justify-content:center;
}

/* ── ENTRANCE ANIMATION ── */
/* Menu items fly in from screen center on first login.
   Uses nth-child() for staggered delays (SCSS @for loop). */
.entering :deep(.el-menu-item),
.entering :deep(.el-sub-menu__title) {
  animation: flyInFromCenter 0.55s cubic-bezier(0.22,0.61,0.36,1) both;
  @for $i from 1 through 10 {
    &:nth-child(#{$i}) { animation-delay: #{$i * 60}ms; }
  }
}

@keyframes flyInFromCenter {
  0%   { opacity: 0; transform: translateX(50vw) translateY(-15vh) scale(1.3); }
  100% { opacity: 1; transform: translateX(0) translateY(0) scale(1); }
}

/* ── Logo transition ── */
.fade-enter-active, .fade-leave-active { transition: opacity 0.3s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
