import request from '../request'

export interface UploadFileResponse {
  fileId: number
  originalName: string
  fileType: string
  fileSize: number
}

export function uploadTranscript(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<any, UploadFileResponse>('/files/transcripts', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export function uploadAttachment(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<any, UploadFileResponse>('/files/attachments', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export function getFilePreviewUrl(fileId: number) {
  return `/api/files/${fileId}/preview`
}

export function getFileDownloadUrl(fileId: number) {
  return `/api/files/${fileId}/download`
}
