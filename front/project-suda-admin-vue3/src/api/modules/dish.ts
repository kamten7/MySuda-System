import request from '../request'
import type { ApiResponse, PageResult } from '../types/common'

export interface DishFlavor {
  id?: number
  dishId?: number
  name: string
  value: string
}

export interface DishRecord {
  id: number
  name: string
  categoryId: number
  categoryName?: string
  price: number
  image: string
  description: string
  status: number
  flavors: DishFlavor[]
  createTime: string
  updateTime: string
}

export interface DishQuery {
  page: number
  pageSize: number
  name?: string
  categoryId?: number
  status?: number
}

export interface DishForm {
  id?: number
  name: string
  categoryId: number
  price: number
  image: string
  description: string
  flavors: DishFlavor[]
}

export function getDishPage(params: DishQuery) {
  return request.get<ApiResponse<PageResult<DishRecord>>>('/dish/page', { params })
}

export function addDish(data: DishForm) {
  return request.post<ApiResponse<null>>('/dish', data)
}

export function editDish(data: DishForm) {
  return request.put<ApiResponse<null>>('/dish', data)
}

export function deleteDish(ids: number[]) {
  return request.delete<ApiResponse<null>>('/dish', {
    params: { ids: ids.join(',') },
  })
}

export function queryDishById(id: number | string) {
  return request.get<ApiResponse<DishRecord>>(`/dish/${id}`)
}

export function toggleDishStatus(status: number, id: number) {
  return request.post<ApiResponse<null>>(`/dish/status/${status}`, null, {
    params: { id },
  })
}

export function getDishList(categoryId?: number) {
  return request.get<ApiResponse<DishRecord[]>>('/dish/list', {
    params: { categoryId },
  })
}
