// api/auth.js - 认证相关API
import request from '@/utils/request'

// 登录接口
export function loginAPI(loginForm) {
  return request({
    url: '/api/v1/auth/login',
    method: 'post',
    data: {
      username: loginForm.username,
      password: loginForm.password
    }
  })
}

// 获取用户信息接口
export function getUserInfoAPI() {
  return request({
    url: '/api/v1/auth/userinfo',
    method: 'get'
  })
}

// 退出登录接口
export function logoutAPI() {
  return request({
    url: '/api/v1/auth/logout',
    method: 'post'
  })
}

// 刷新token接口
export function refreshTokenAPI() {
  return request({
    url: '/api/v1/auth/refresh',
    method: 'post'
  })
}