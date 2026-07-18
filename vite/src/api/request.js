import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

// 请求拦截: 附带 token
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('foodie_token')
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截: 解包统一响应 R
request.interceptors.response.use(
  (response) => {
    const res = response.data
    // 非 JSON(如文件流)直接返回
    if (res == null || typeof res !== 'object' || res.code === undefined) {
      return res
    }
    if (res.code === 200) {
      return res
    }
    if (res.code === 401) {
      ElMessage.error(res.msg || '登录已失效, 请重新登录')
      localStorage.removeItem('foodie_token')
      localStorage.removeItem('foodie_user')
      if (!location.hash.includes('/login')) {
        location.hash = '#/login'
      }
      return Promise.reject(new Error(res.msg || '未登录'))
    }
    ElMessage.error(res.msg || '请求失败')
    return Promise.reject(new Error(res.msg || 'error'))
  },
  (error) => {
    ElMessage.error(error.message || '网络异常')
    return Promise.reject(error)
  }
)

export default request
