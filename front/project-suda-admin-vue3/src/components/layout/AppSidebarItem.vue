<template>
  <!-- Single leaf item -->
  <template v-if="!hasChildren">
    <el-menu-item :index="itemPath">
      <el-icon v-if="item.meta?.icon">
        <component :is="iconComponent" />
      </el-icon>
      <template #title>
        <span>{{ item.meta?.title || item.name }}</span>
      </template>
    </el-menu-item>
  </template>

  <!-- Nested sub-menu -->
  <template v-else>
    <el-sub-menu :index="itemPath" teleported>
      <template #title>
        <el-icon v-if="item.meta?.icon">
          <component :is="iconComponent" />
        </el-icon>
        <span>{{ item.meta?.title || item.name }}</span>
      </template>
      <AppSidebarItem
        v-for="child in visibleChildren"
        :key="child.path"
        :item="child"
        :base-path="childPath(child)"
      />
    </el-sub-menu>
  </template>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import type { RouteRecordRaw } from 'vue-router'
import type { Component } from 'vue'

const props = defineProps<{
  item: RouteRecordRaw
  basePath: string
}>()

const visibleChildren = computed(() => {
  if (!props.item.children) return []
  return props.item.children.filter((child: RouteRecordRaw) => {
    if (child.meta && child.meta.hidden) return false
    return true
  })
})

const hasChildren = computed(() => {
  return visibleChildren.value.length > 0
})

const itemPath = computed(() => {
  return resolvePath(props.basePath, props.item.path)
})

const iconComponent = computed<Component | null>(() => {
  if (!props.item.meta?.icon) return null
  const iconMap: Record<string, Component> = ElementPlusIconsVue
  return iconMap[props.item.meta.icon as string] || null
})

function childPath(child: RouteRecordRaw): string {
  return resolvePath(props.basePath, child.path)
}

function resolvePath(basePath: string, routePath: string): string {
  if (routePath.startsWith('/')) {
    return routePath
  }
  if (basePath.endsWith('/')) {
    return basePath + routePath
  }
  return `${basePath}/${routePath}`
}
</script>

<style lang="scss" scoped>
.el-icon {
  margin-right: 8px;
  font-size: 16px;
  width: 16px;
  height: 16px;
}
</style>
