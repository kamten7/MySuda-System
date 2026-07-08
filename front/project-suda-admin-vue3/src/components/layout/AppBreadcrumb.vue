<template>
  <div class="app-breadcrumb">
    <el-breadcrumb separator="/">
      <transition-group name="breadcrumb">
        <el-breadcrumb-item
          v-for="(item, index) in breadcrumbs"
          :key="item.path"
        >
          <span v-if="index === breadcrumbs.length - 1" class="breadcrumb-last">
            {{ item.meta?.title || item.name }}
          </span>
          <router-link v-else :to="item.path">
            {{ item.meta?.title || item.name }}
          </router-link>
        </el-breadcrumb-item>
      </transition-group>
    </el-breadcrumb>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import type { RouteLocationMatched } from 'vue-router'

const route = useRoute()

interface BreadcrumbItem {
  path: string
  name?: string
  meta?: {
    title?: string
    icon?: string
    hidden?: boolean
  }
}

const breadcrumbs = computed<BreadcrumbItem[]>(() => {
  const matched: BreadcrumbItem[] = []

  // Add dashboard as first item if not already there
  const hasDashboard = route.matched.some(
    (m: RouteLocationMatched) => m.path === '/dashboard' || m.name === 'Dashboard',
  )

  if (!hasDashboard && route.path !== '/dashboard') {
    matched.push({
      path: '/dashboard',
      name: 'Dashboard',
      meta: { title: '工作台' },
    })
  }

  route.matched.forEach((m: RouteLocationMatched) => {
    // Skip the root layout route
    if (m.path === '/' || m.path === '') return

    // Skip hidden routes
    if (m.meta?.hidden) return

    matched.push({
      path: m.path.startsWith('/') ? m.path : `/${m.path}`,
      name: m.name as string,
      meta: m.meta as Record<string, unknown>,
    })
  })

  return matched
})
</script>

<style lang="scss" scoped>
.app-breadcrumb {
  height: 40px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
  flex-shrink: 0;
}

html.dark .app-breadcrumb {
  background: #1a1a1a;
  border-bottom-color: #333;
}

.breadcrumb-last {
  color: #333;
  font-weight: 500;
}

html.dark .breadcrumb-last {
  color: #e5e5e5;
}
</style>
