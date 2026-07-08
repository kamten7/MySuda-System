import request from '../request'
import type { ApiResponse } from '../types/common'

export interface BusinessDataVO {
  turnover: number
  validOrderCount: number
  orderCompletionRate: number
  unitPrice: number
  newUsers: number
}

export interface OrderOverViewVO {
  allOrders: number
  cancelledOrders: number
  completedOrders: number
  deliveredOrders: number
  waitingOrders: number
}

export interface DishOverViewVO {
  sold: number
  discontinued: number
}

export interface SetmealOverViewVO {
  sold: number
  discontinued: number
}

export function getBusinessData() {
  return request.get<ApiResponse<BusinessDataVO>>('/workspace/businessData')
}

export function getOverviewOrders() {
  return request.get<ApiResponse<OrderOverViewVO>>('/workspace/overviewOrders')
}

export function getOverviewDishes() {
  return request.get<ApiResponse<DishOverViewVO>>('/workspace/overviewDishes')
}

export function getOverviewSetmeals() {
  return request.get<ApiResponse<SetmealOverViewVO>>('/workspace/overviewSetmeals')
}
