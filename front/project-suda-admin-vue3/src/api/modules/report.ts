import request from '../request'
import type { ApiResponse } from '../types/common'

export interface TurnoverReportVO {
  dateList: string[]
  turnoverList: number[]
}

export interface UserReportVO {
  dateList: string[]
  totalUserList: number[]
  newUserList: number[]
}

export interface OrderReportVO {
  dateList: string[]
  orderCountList: number[]
  validOrderCountList: number[]
  orderCompletionRate?: number
  validOrderCount?: number
  totalOrderCount?: number
}

export interface SalesTop10ReportVO {
  nameList: string[]
  numberList: number[]
}

export interface ReportQuery {
  begin: string
  end: string
}

export function getTurnoverStatistics(params: ReportQuery) {
  return request.get<ApiResponse<TurnoverReportVO>>('/report/turnoverStatistics', { params })
}

export function getUserStatistics(params: ReportQuery) {
  return request.get<ApiResponse<UserReportVO>>('/report/userStatistics', { params })
}

export function getOrderReportStatistics(params: ReportQuery) {
  return request.get<ApiResponse<OrderReportVO>>('/report/ordersStatistics', { params })
}

export function getTop10(params: ReportQuery) {
  return request.get<ApiResponse<SalesTop10ReportVO>>('/report/top10', { params })
}

export function exportReport() {
  return request.get('/report/export', { responseType: 'blob' })
}
