import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import router from '@/router'

const request = axios.create({
  baseURL: '/',
  timeout: 30000
})

request.interceptors.request.use(config => {
  const user = useUserStore()
  if (user.token) {
    config.headers['Authorization'] = 'Bearer ' + user.token
  }
  return config
})

request.interceptors.response.use(
  response => {
    const data = response.data
    // 二进制/非 JSON 原样返回
    if (typeof data !== 'object' || data === null || !('code' in data)) {
      return data
    }
    if (data.code === 200) {
      return data.data
    }
    if (data.code === 401) {
      const user = useUserStore()
      user.logout()
      ElMessage.error(data.message || '未登录，请重新登录')
      router.push('/login')
      return Promise.reject(data)
    }
    ElMessage.error(data.message || '请求失败')
    return Promise.reject(data)
  },
  error => {
    const status = error.response?.status
    if (status === 401) {
      const user = useUserStore()
      user.logout()
      router.push('/login')
    }
    ElMessage.error(error.response?.data?.message || error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default request
