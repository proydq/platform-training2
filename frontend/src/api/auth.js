// api/auth.js - 认证相关API接口
import request from '@/utils/request'

// 登录接口
export function loginAPI(data) {
  return request({
    url: '/auth/login',
    method: 'POST',
    data: {
      username: data.username,
      password: data.password
    }
  })
}

// 获取用户信息
export function getUserInfoAPI() {
  return request({
    url: '/auth/userinfo',
    method: 'GET'
  })
}

// 退出登录
export function logoutAPI() {
  return request({
    url: '/auth/logout',
    method: 'POST'
  })
}

// 健康检查（测试后端连接）
export function healthCheckAPI() {
  return request({
    url: '/health',
    method: 'GET'
  })
}