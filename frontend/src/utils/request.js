// frontend/src/utils/request.js - 修复版本
import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建axios实例
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 5000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    
    // 添加token - 移除mock_token检查
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    } else {
    }
    
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    if (response.config && response.config.rawResponse) {
      return response
    }
    return response.data
  },
  error => {
    
    let message = '请求失败'
    
    if (error.response) {
      const status = error.response.status
      switch (status) {
        case 401:
          message = '登录已过期'
          localStorage.removeItem('token')
          localStorage.removeItem('userInfo')
          window.location.href = '/login'
          break
        case 403:
          message = '没有权限'
          break
        case 404:
          message = '接口不存在'
          break
        case 500:
          message = '服务器错误'
          break
        default:
          message = `请求失败 (${status})`
      }
    } else if (error.code === 'ECONNABORTED') {
      message = '请求超时'
    } else if (error.message.includes('Network Error')) {
      message = '网络连接失败'
    }
    
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default request
