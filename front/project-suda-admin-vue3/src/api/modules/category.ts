import request from '../request'
import type { ApiResponse, PageResult } from '../types/common'

export interface CategoryRecord {
  id: number
  name: string
  type: number // 1=菜品分类, 2=套餐分类
  sort: number
  status: number
  createTime: string
  updateTime: string
  createUser: number
  updateUser: number
}

export interface CategoryQuery {
  page: number
  pageSize: number
  name?: string
  type?: number
}

export interface CategoryForm {
  id?: number
  name: string
  type: number
  sort: number
}

export function getCategoryPage(params: CategoryQuery) {
  return request.get<ApiResponse<PageResult<CategoryRecord>>>('/category/page', { params })
}

export function addCategory(data: CategoryForm) {
  return request.post<ApiResponse<null>>('/category', data)
}

export function editCategory(data: CategoryForm) {
  return request.put<ApiResponse<null>>('/category', data)
}

export function deleteCategory(id: number) {
  return request.delete<ApiResponse<null>>('/category', { params: { id } })
}

export function toggleCategoryStatus(status: number, id: number) {
  return request.post<ApiResponse<null>>(`/category/status/${status}`, null, {
    params: { id },
  })
}

export function getCategoryList(type?: number) {
  return request.get<ApiResponse<CategoryRecord[]>>('/category/list', { params: { type } })
}
