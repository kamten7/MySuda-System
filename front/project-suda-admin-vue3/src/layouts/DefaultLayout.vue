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
import { computed } from 'vue'
import { useAppStore } from '@/stores/app'
import AppSidebar from '@/components/layout/AppSidebar.vue'
import AppNavbar from '@/components/layout/AppNavbar.vue'
import AppBreadcrumb from '@/components/layout/AppBreadcrumb.vue'

const appStore = useAppStore()

const cachedViews = computed(() => ['Dashboard', 'Statistics', 'OrderList', 'DishList', 'SetmealList', 'Category', 'EmployeeList', 'MessageCenter'])
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
