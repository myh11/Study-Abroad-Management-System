import request from '../request'

export interface BatchPayload {
  batch_name: string
  start_time: string
  end_time: string
  batch_status: 'IN_PROGRESS' | 'FINISHED'
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

export function updateBatchStatus(batchId: number, batch_status: 'IN_PROGRESS' | 'FINISHED') {
  return request.post<never, void>(`/batches/${batchId}/status`, { batch_status })
}
