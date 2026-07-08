<template>
  <div class="empty-state">
    <el-empty :description="descriptionText" :image-size="160">
      <template v-if="$slots.default" #default>
        <slot />
      </template>
    </el-empty>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    description?: string
    isSearch?: boolean
  }>(),
  {
    description: '',
    isSearch: false,
  },
)

const descriptionText = computed(() => {
  if (props.description) return props.description
  return props.isSearch ? '没有找到匹配的结果' : '暂无数据'
})
</script>

<style lang="scss" scoped>
.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 60px 20px;
}
</style>
