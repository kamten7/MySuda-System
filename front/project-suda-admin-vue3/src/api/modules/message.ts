import request from '../request'
import type { ApiResponse, PageResult } from '../types/common'

export interface MessageRecord {
  id: number
  content: string
  type: number
  status: number
  details: string
  createTime: string
}

export function getMessagePage(params: { page: number; pageSize: number; status?: number }) {
  return request.get<ApiResponse<PageResult<MessageRecord>>>('/messages/page', { params })
}

export function getCountUnread() {
  return request.get<ApiResponse<number>>('/messages/countUnread')
}

export function batchReadMessages(ids: number[]) {
  return request.put<ApiResponse<null>>('/messages/batch', ids)
}

export function markMessageRead(id: number) {
  return request.put<ApiResponse<null>>(`/messages/${id}`)
}
