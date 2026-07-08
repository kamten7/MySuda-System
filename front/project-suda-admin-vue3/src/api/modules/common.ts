import request from '../request'
import type { ApiResponse } from '../types/common'

export function uploadFile(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<ApiResponse<string>>('/common/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}
