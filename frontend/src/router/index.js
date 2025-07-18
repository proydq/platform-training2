// utils/request.js - 修复Vite环境变量的Axios封装
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

// 创建axios实例 - 修复Vite环境变量
const service = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API || 'http://localhost:8080', // 修复：使用Vite环境变量
  timeout: 10000, // 请求超时时间
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    console.log('发送请求:', config.method?.toUpperCase(), config.url, config.data)
    
    // 添加token到请求头
    const token = localStorage.getItem('token')
    
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    console.log('收到响应:', response.status, response.config.url, response.data)
    
    const res = response.data
    
    // 这里根据后端的返回格式进行调整
    // 假设后端返回格式为: { code: 200, message: 'success', data: {...} }
    if (res.code !== undefined) {
      // 成功响应
      if (res.code === 200) {
        return res
      }
      
      // 业务错误
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    
    // 如果没有code字段，直接返回数据
    return res
  },
  error => {
    console.error('响应错误:', error)
    
    let message = '网络错误'
    
    if (error.response) {
      const status = error.response.status
      
      switch (status) {
        case 401:
          message = '登录已过期，请重新登录'
          // 清除本地存储的认证信息
          localStorage.removeItem('token')
          localStorage.removeItem('userInfo')
          // 跳转到登录页
          window.location.href = '/login'
          break
        case 403:
          message = '没有权限访问'
          break
        case 404:
          message = '请求的资源不存在'
          break
        case 500:
          message = '服务器内部错误'
          break
        default:
          message = error.response.data?.message || `请求失败 (${status})`
      }
    } else if (error.code === 'ECONNABORTED') {
      message = '请求超时，请稍后重试'
    } else if (error.message.includes('Network Error')) {
      message = '网络连接失败，请检查网络设置'
    }
    
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default service