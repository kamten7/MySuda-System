import request from '../request'
import type { ApiResponse, PageResult } from '../types/common'

export interface EmployeeRecord {
  id: number
  name: string
  username: string
  password?: string
  phone: string
  sex: string
  idNumber: string
  status: number
  createTime: string
  updateTime: string
  createUser: number
  updateUser: number
}

export interface EmployeeQuery {
  page: number
  pageSize: number
  name?: string
}

export interface EmployeeForm {
  id?: number
  name: string
  username: string
  phone: string
  sex: string
  idNumber: string
}

export function getEmployeePage(params: EmployeeQuery) {
  return request.get<ApiResponse<PageResult<EmployeeRecord>>>('/employee/page', { params })
}

export function addEmployee(data: EmployeeForm) {
  return request.post<ApiResponse<null>>('/employee', data)
}

export function editEmployee(data: EmployeeForm) {
  return request.put<ApiResponse<null>>('/employee', data)
}

export function queryEmployeeById(id: number | string) {
  return request.get<ApiResponse<EmployeeRecord>>(`/employee/${id}`)
}

export function toggleEmployeeStatus(status: number, id: number) {
  return request.post<ApiResponse<null>>(`/employee/status/${status}`, null, {
    params: { id },
  })
}
