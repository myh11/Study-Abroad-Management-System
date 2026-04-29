import request from '../request'

export interface DashboardMetric {
  key: string
  label: string
  value: number
}

export interface DashboardSummary {
  role: string
  schoolCode?: string | null
  metrics: DashboardMetric[]
}

export function getDashboardSummary() {
  return request.get<never, DashboardSummary>('/dashboard/summary')
}
