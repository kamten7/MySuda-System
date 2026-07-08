/** API Response wrapper matching backend Result<T> */
export interface ApiResponse<T = unknown> {
  code: number // 1 = success, 0 = failure
  msg: string
  data: T
}

/** Page result matching backend PageResult */
export interface PageResult<T = unknown> {
  total: number
  records: T[]
}

/** Common pagination query params */
export interface PageParams {
  page: number
  pageSize: number
  [key: string]: unknown
}
