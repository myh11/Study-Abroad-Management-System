export interface Result<T> {
  code: number
  message: string
  data: T
}

export interface PageQuery {
  page_num?: number
  page_size?: number
  keyword?: string
}

export interface PageResult<T> {
  total: number
  records: T[]
}

export interface OptionItem {
  label: string
  value: string | number
}

export type UserRole = 'AGENT' | 'DOMESTIC_REVIEWER' | 'SCHOOL_REVIEWER' | 'ADMIN'
