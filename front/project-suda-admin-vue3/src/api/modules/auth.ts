import request from '../request'
import type { ApiResponse } from '../types/common'

export interface LoginParams {
  username: string
  password: string
}

export interface LoginResult {
  id: number
  userName: string
  name: string
  token: string
}

export function login(data: LoginParams) {
  return request.post<ApiResponse<LoginResult>>('/employee/login', data)
}

export function logout() {
  return request.post<ApiResponse<null>>('/employee/logout')
}

export function editPassword(data: { oldPassword: string; newPassword: string }) {
  return request.put<ApiResponse<null>>('/employee/editPassword', data)
}
