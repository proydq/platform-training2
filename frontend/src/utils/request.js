// utils/request.js
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

// 创建axios实例
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 显示加载状态（可选）
    // 为每个请求添加token
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    
    // 添加请求ID用于追踪
    config.headers['X-Request-ID'] = `req_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
    
    console.log('发送请求:', {
      url: config.url,
      method: config.method,
      data: config.data,
      params: config.params
    })
    
    return config
  },
  error => {
    console.error('请求配置错误:', error)
    ElMessage.error('请求配置错误')
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const { data, status, config } = response
    
    console.log('收到响应:', {
      url: config.url,
      status,
      data
    })
    
    // 成功响应处理
    if (status === 200) {
      // 如果后端返回的是标准格式 {success: boolean, data: any, message: string}
      if (data && typeof data.success === 'boolean') {
        if (data.success) {
          return data
        } else {
          // 业务逻辑错误
          ElMessage.error(data.message || '操作失败')
          return Promise.reject(new Error(data.message || '操作失败'))
        }
      }
      
      // 如果后端直接返回数据
      return {
        success: true,
        data: data,
        message: 'success'
      }
    }
    
    return response
  },
  error => {
    console.error('响应错误:', error)
    
    const { response, code, message } = error
    
    // 网络错误
    if (code === 'ECONNABORTED' || message.includes('timeout')) {
      ElMessage.error('请求超时，请检查网络连接')
      return Promise.reject(new Error('请求超时'))
    }
    
    if (!response) {
      ElMessage.error('网络连接失败，请检查网络')
      return Promise.reject(new Error('网络连接失败'))
    }
    
    const { status, data } = response
    const userStore = useUserStore()
    
    // 根据状态码处理不同情况
    switch (status) {
      case 400:
        ElMessage.error(data?.message || '请求参数错误')
        break
        
      case 401:
        // 未授权，可能是token过期
        ElMessage.error('登录已过期，请重新登录')
        userStore.logout()
        router.replace('/login')
        break
        
      case 403:
        ElMessage.error('权限不足，无法访问')
        break
        
      case 404:
        ElMessage.error('请求的资源不存在')
        break
        
      case 422:
        // 验证错误
        if (data?.errors && typeof data.errors === 'object') {
          const errorMessages = Object.values(data.errors).flat()
          ElMessage.error(errorMessages.join('；'))
        } else {
          ElMessage.error(data?.message || '数据验证失败')
        }
        break
        
      case 429:
        ElMessage.error('请求过于频繁，请稍后再试')
        break
        
      case 500:
        ElMessage.error('服务器内部错误')
        break
        
      case 502:
      case 503:
      case 504:
        ElMessage.error('服务器暂时不可用，请稍后再试')
        break
        
      default:
        ElMessage.error(data?.message || `请求失败 (${status})`)
    }
    
    return Promise.reject(error)
  }
)

// Token刷新逻辑
let isRefreshing = false
let failedQueue = []

const processQueue = (error, token = null) => {
  failedQueue.forEach(prom => {
    if (error) {
      prom.reject(error)
    } else {
      prom.resolve(token)
    }
  })
  
  failedQueue = []
}

// 添加重试机制
const retryRequest = (config, retryCount = 0) => {
  const maxRetries = 3
  
  if (retryCount >= maxRetries) {
    return Promise.reject(new Error('重试次数超过限制'))
  }
  
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve(request(config))
    }, Math.pow(2, retryCount) * 1000) // 指数退避
  }).catch(() => {
    return retryRequest(config, retryCount + 1)
  })
}

// 文件上传的特殊处理
export const uploadFile = (url, file, onProgress) => {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url,
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    onUploadProgress: (progressEvent) => {
      if (onProgress && typeof onProgress === 'function') {
        const percentCompleted = Math.round(
          (progressEvent.loaded * 100) / progressEvent.total
        )
        onProgress(percentCompleted)
      }
    }
  })
}

// 下载文件
export const downloadFile = (url, filename) => {
  return request({
    url,
    method: 'get',
    responseType: 'blob'
  }).then(response => {
    const blob = new Blob([response.data])
    const downloadUrl = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = downloadUrl
    link.download = filename
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(downloadUrl)
  })
}

export default request