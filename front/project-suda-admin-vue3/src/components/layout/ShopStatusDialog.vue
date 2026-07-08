<template>
  <el-dialog
    :model-value="visible"
    title="店铺状态设置"
    width="400px"
    :close-on-click-modal="false"
    @update:model-value="$emit('update:visible', $event)"
  >
    <div class="shop-status-content">
      <div class="current-status">
        <span class="label">当前状态：</span>
        <el-tag :type="localStatus === 1 ? 'success' : 'info'" size="large">
          {{ localStatus === 1 ? '营业中' : '打烊中' }}
        </el-tag>
      </div>
      <div class="toggle-row">
        <span class="label">切换状态：</span>
        <el-switch
          v-model="switchValue"
          active-text="营业中"
          inactive-text="打烊中"
          :loading="loading"
          inline-prompt
          @change="(val: any) => handleToggle(val)"
        />
      </div>
    </div>
    <template #footer>
      <el-button @click="$emit('update:visible', false)">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { setShopStatus } from '@/api/modules/shop'

const props = defineProps<{
  visible: boolean
  currentStatus: number
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
  success: []
}>()

const localStatus = ref(props.currentStatus)
const loading = ref(false)

const switchValue = computed({
  get: () => localStatus.value === 1,
  set: () => {},
})

watch(
  () => props.currentStatus,
  (val) => {
    localStatus.value = val
  },
)

async function handleToggle(val: boolean) {
  const newStatus = val ? 1 : 0
  loading.value = true
  try {
    const res = await setShopStatus(newStatus)
    if (res.data.code === 1) {
      localStatus.value = newStatus
      ElMessage.success(newStatus === 1 ? '已切换为营业中' : '已切换为打烊中')
      emit('success')
    }
  } catch {
    // Revert on error
    localStatus.value = localStatus.value === 1 ? 0 : 1
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.shop-status-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 10px 0;
}

.current-status,
.toggle-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.label {
  font-size: 14px;
  color: #666;
  min-width: 80px;
}
</style>
