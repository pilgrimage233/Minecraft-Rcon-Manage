import axios from 'axios'
import {ElMessage} from 'element-plus'
import router from '../router'

const request = axios.create({
    baseURL: import.meta.env.VITE_API_URL,
    timeout: 8000
})

// 添加重试机制
request.interceptors.response.use(
    response => {
        const {code, msg, data} = response.data
        if (code === 200) return data

        // 处理特定错误码
        switch (code) {
            case 401:
                ElMessage.error('未授权访问')
                router.push('/login')
                break
            case 403:
                ElMessage.error('访问被拒绝')
                break
            case 429:
                ElMessage.warning('请求过于频繁，请稍后再试')
                break
            default:
                ElMessage.error(msg || '请求失败')
        }

        return Promise.reject(new Error(msg))
    },
    error => {
        if (error.code === 'ECONNABORTED') {
            ElMessage.error('请求超时，请检查网络')
        } else {
            ElMessage.error('网络错误，请稍后重试')
        }
        return Promise.reject(error)
    }
)

export default request