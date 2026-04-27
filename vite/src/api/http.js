import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import router from '@/router'

const http = axios.create({
  baseURL: '/',
  timeout: 15000
})

http.interceptors.request.use(cfg => {
  const userStore = useUserStore()
  if (userStore.token) cfg.headers.Authorization = 'Bearer ' + userStore.token
  return cfg
})

http.interceptors.response.use(
  res => {
    const data = res.data
    if (data && typeof data === 'object' && 'code' in data) {
      if (data.code === 0) return data
      ElMessage.error(data.msg || '操作失败')
      return Promise.reject(new Error(data.msg || 'error'))
    }
    return data
  },
  err => {
    const status = err.response?.status
    const msg = err.response?.data?.msg || err.message || '网络错误'
    if (status === 401) {
      const userStore = useUserStore()
      userStore.logout()
      router.replace('/login')
    }
    ElMessage.error(msg)
    return Promise.reject(err)
  }
)

export default http
