import request from '../request'

export interface BatchPayload {
  batchName: string
  startTime: string
  endTime: string
  batchStatus: 'IN_PROGRESS' | 'FINISHED'
}

export function getBatches() {
  return request.get<never, unknown[]>('/batches')
}

export function createBatch(payload: BatchPayload) {
  return request.post<never, void>('/batches', payload)
}

export function updateBatch(batchId: number, payload: BatchPayload) {
  return request.put<never, void>(`/batches/${batchId}`, payload)
}

export function updateBatchStatus(batchId: number, status: 'IN_PROGRESS' | 'FINISHED') {
  return request.post<never, void>(`/batches/${batchId}/status`, { status })
}
