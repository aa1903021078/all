import { ElMessage } from 'element-plus'
import router from '../router'
import axios from "axios";

const request = axios.create({
    baseURL: import.meta.env.VITE_BASE_URL,
    timeout: 30000  // 后台接口超时时间设置
})

// request 拦截器
// 可以自请求发送前对请求做一些处理
request.interceptors.request.use(config => {
    // 只有非文件上传请求才设置默认 Content-Type
    if (!config.headers['Content-Type']) {
        config.headers['Content-Type'] = 'application/json;charset=utf-8';
    }

    // 从 localStorage 获取 token 并添加到请求头（用于权限验证）
    const token = localStorage.getItem('token')
    if (token) {
        config.headers['Authorization'] = 'Bearer ' + token
    }

    return config
}, error => {
    // 对请求错误做点什么
    return Promise.reject(error)
});

// response 拦截器
// 可以在接口响应后统一处理结果
request.interceptors.response.use(
    response => {
        let res = response.data;
        // 如果是返回的文件
        if (response.config.responseType === 'blob') {
            return res
        }
        // 兼容服务端返回的字符串数据
        if (typeof res === 'string') {
            res = res ? JSON.parse(res) : res
        }
        // 当权限验证不通过（401）时给出提示并跳转到登录页
        if (res.code === '401') {
            ElMessage.error(res.msg || '登录过期，请重新登录');
            // 清除token
            localStorage.removeItem('token')
            // 跳转到登录页
            router.push("/login")
        }
        return res;
    },
        error => {
            // 对响应错误做点什么
        console.log('err' + error)
        ElMessage.error('网络错误：' + error.message)
        return Promise.reject(error)
    }
)

// 导出请求对象
export default request
