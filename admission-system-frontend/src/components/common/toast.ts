import { ElMessage } from 'element-plus'

export const toast = {
  success(message: string) {
    ElMessage.success(message)
  },
  error(message: string) {
    ElMessage.error(message)
  },
  info(message: string) {
    ElMessage.info(message)
  },
  warning(message: string) {
    ElMessage.warning(message)
  },
}
