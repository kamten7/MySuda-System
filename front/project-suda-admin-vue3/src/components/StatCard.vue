<template>
  <div class="stat-card bg-white rounded-xl p-5 shadow-sm border border-gray-100 hover:shadow-md transition-shadow duration-300">
    <div v-if="loading" class="animate-pulse flex gap-4">
      <div class="flex-shrink-0 w-12 h-12 rounded-full bg-gray-200"></div>
      <div class="flex-1 min-w-0">
        <div class="h-4 bg-gray-200 rounded w-16 mb-2"></div>
        <div class="h-7 bg-gray-200 rounded w-24"></div>
      </div>
    </div>
    <template v-else>
      <div class="flex items-center gap-4">
        <div :class="['icon-wrapper', `icon-${color}`]">
          <el-icon :size="22">
            <component :is="icon" />
          </el-icon>
        </div>
        <div class="flex-1 min-w-0">
          <div class="text-sm text-gray-400 mb-1">{{ title }}</div>
          <div class="flex items-baseline gap-1">
            <span class="text-2xl font-bold text-gray-800">{{ value }}</span>
            <span v-if="unit" class="text-sm text-gray-400">{{ unit }}</span>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    title: string
    value: string | number
    unit?: string
    icon: string
    color: 'red' | 'green' | 'blue' | 'orange' | 'purple'
    loading?: boolean
  }>(),
  {
    unit: '',
    loading: false,
  },
)
</script>

<style scoped>
.icon-wrapper {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.icon-red    { background: #dbeafe; color: #1a56db; }
.icon-green  { background: #e8f5e9; color: #2e7d32; }
.icon-blue   { background: #e3f2fd; color: #1565c0; }
.icon-orange { background: #fff3e0; color: #e65100; }
.icon-purple { background: #f3e5f5; color: #7b1fa2; }
</style>
