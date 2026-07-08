import request from '../request'
import type { ApiResponse } from '../types/common'

export function getShopStatus() {
  return request.get<ApiResponse<number>>('/shop/status')
}

export function setShopStatus(status: number) {
  return request.put<ApiResponse<null>>(`/shop/${status}`)
}
