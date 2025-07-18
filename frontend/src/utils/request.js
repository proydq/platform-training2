// utils/request.js - 最小化版本，避免循环引用
import axios from 'axios'

// 最简单的axios实例，避免复杂配置
const service = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 简化的请求拦截器
service.interceptors.request.use(
  config => {
    // 添加token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 简化的响应拦截器
service.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    console.error('响应错误:', error)
    
    // 简化错误处理，避免循环引用
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      // 使用简单的页面跳转
      window.location.href = '/login'
    }
    
    return Promise.reject(error)
  }
)

export default service