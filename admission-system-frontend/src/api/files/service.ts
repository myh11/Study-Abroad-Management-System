import request from '../request'

export interface UploadFileResponse {
  file_id: number
  file_key: string
  original_name: string
}

export function uploadTranscript(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<never, UploadFileResponse>('/files/transcripts', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export function uploadAttachment(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<never, UploadFileResponse>('/files/attachments', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export function getFilePreviewUrl(fileId: number) {
  return `/api/files/${fileId}/preview`
}

export function getFileDownloadUrl(fileId: number) {
  return `/api/files/${fileId}/download`
}
