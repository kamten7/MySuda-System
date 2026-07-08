<template>
  <div class="image-upload">
    <el-upload
      class="image-uploader"
      :action="uploadAction"
      :headers="uploadHeaders"
      :show-file-list="false"
      :on-success="handleSuccess"
      :on-error="handleError"
      :before-upload="beforeUpload"
      accept="image/*"
    >
      <img v-if="modelValue" :src="modelValue" class="uploaded-image" />
      <div v-else class="upload-placeholder">
        <el-icon><Plus /></el-icon>
        <span>上传图片</span>
      </div>
    </el-upload>
    <div v-if="modelValue" class="mt-2 flex items-center gap-2">
      <el-button type="danger" size="small" @click="handleRemove">删除图片</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getToken } from '@/utils/cookies'

const props = defineProps<{
  modelValue: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

const uploadAction = `${import.meta.env.VITE_API_BASE_URL}/common/upload`
const uploadHeaders = {
  token: getToken() || '',
}

function beforeUpload(file: File) {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }
  return true
}

function handleSuccess(response: { code: number; msg: string; data: string }) {
  if (response.code === 1) {
    emit('update:modelValue', response.data)
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(response.msg || '上传失败')
  }
}

function handleError() {
  ElMessage.error('上传失败，请重试')
}

function handleRemove() {
  emit('update:modelValue', '')
}
</script>

<style lang="scss" scoped>
.image-uploader {
  :deep(.el-upload) {
    border: 2px dashed #d9d9d9;
    border-radius: 8px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: border-color 0.3s;

    &:hover {
      border-color: #1a56db;
    }
  }
}

.upload-placeholder {
  width: 180px;
  height: 180px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #8c939d;
  font-size: 14px;

  .el-icon {
    font-size: 28px;
  }
}

.uploaded-image {
  width: 180px;
  height: 180px;
  object-fit: cover;
  display: block;
}
</style>
