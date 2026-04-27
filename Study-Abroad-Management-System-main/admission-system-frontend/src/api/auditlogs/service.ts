import request from '../request'

export function getAuditLogs(params?: Record<string, string | number | undefined>) {
  return request.get<never, unknown[]>('/audit-logs', { params })
}

export function getStatusHistories(applicationId: number) {
  return request.get<never, unknown[]>('/audit-logs/status-histories', {
    params: { applicationId },
  })
}
