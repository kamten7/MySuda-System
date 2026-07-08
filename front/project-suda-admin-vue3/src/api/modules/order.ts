import request from '../request'
import type { ApiResponse, PageResult } from '../types/common'

export interface OrderRecord {
  id: number
  number: string
  status: number // 1=待付款 2=待接单 3=待派送 4=派送中 5=已完成 6=已取消
  userId: number
  userName?: string
  consignee: string
  phone: string
  address: string
  amount: number
  orderTime: string
  estimatedDeliveryTime?: string
  deliveryTime?: string
  cancelTime?: string
  cancelReason?: string
  rejectionReason?: string
  payMethod?: number
  remark?: string
  orderDishes?: string
  orderDetailList?: OrderDetail[]
}

export interface OrderDetail {
  name: string
  number: number
  amount: number
  image?: string
}

export interface OrderQuery {
  page: number
  pageSize: number
  number?: string
  phone?: string
  beginTime?: string
  endTime?: string
  status?: number
}

export interface OrderStatistics {
  toBeConfirmed: number
  confirmed: number
  deliveryInProgress: number
}

export interface OrderActionParams {
  id: number
  cancelReason?: string
  rejectionReason?: string
}

export function getOrderPage(params: OrderQuery) {
  return request.get<ApiResponse<PageResult<OrderRecord>>>('/order/conditionSearch', { params })
}

export function getOrderStatistics() {
  return request.get<ApiResponse<OrderStatistics>>('/order/statistics')
}

export function getOrderDetail(orderId: number) {
  return request.get<ApiResponse<OrderRecord>>(`/order/details/${orderId}`)
}

export function confirmOrder(data: { id: number }) {
  return request.put<ApiResponse<null>>('/order/confirm', data)
}

export function rejectOrder(data: { id: number; rejectionReason: string }) {
  return request.put<ApiResponse<null>>('/order/rejection', data)
}

export function cancelOrder(data: { id: number; cancelReason: string }) {
  return request.put<ApiResponse<null>>('/order/cancel', data)
}

export function deliverOrder(id: number) {
  return request.put<ApiResponse<null>>(`/order/delivery/${id}`)
}

export function completeOrder(id: number) {
  return request.put<ApiResponse<null>>(`/order/complete/${id}`)
}
