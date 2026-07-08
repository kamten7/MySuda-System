import request from '../request'
import type { ApiResponse, PageResult } from '../types/common'

export interface SetmealDish {
  id?: number
  dishId: number
  name: string
  price: number
  copies: number
}

export interface SetmealRecord {
  id: number
  name: string
  categoryId: number
  categoryName?: string
  price: number
  image: string
  description: string
  status: number
  setmealDishes: SetmealDish[]
  createTime: string
  updateTime: string
}

export interface SetmealQuery {
  page: number
  pageSize: number
  name?: string
  categoryId?: number
  status?: number
}

export interface SetmealForm {
  id?: number
  name: string
  categoryId: number
  price: number
  image: string
  description: string
  setmealDishes: SetmealDish[]
}

export function getSetmealPage(params: SetmealQuery) {
  return request.get<ApiResponse<PageResult<SetmealRecord>>>('/setmeal/page', { params })
}

export function addSetmeal(data: SetmealForm) {
  return request.post<ApiResponse<null>>('/setmeal', data)
}

export function editSetmeal(data: SetmealForm) {
  return request.put<ApiResponse<null>>('/setmeal', data)
}

export function deleteSetmeal(ids: number[]) {
  return request.delete<ApiResponse<null>>('/setmeal', {
    params: { ids: ids.join(',') },
  })
}

export function querySetmealById(id: number | string) {
  return request.get<ApiResponse<SetmealRecord>>(`/setmeal/${id}`)
}

export function toggleSetmealStatus(status: number, id: number) {
  return request.post<ApiResponse<null>>(`/setmeal/status/${status}`, null, {
    params: { id },
  })
}
