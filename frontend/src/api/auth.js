// api/auth.js
import request from '@/utils/request'

// 用户登录
export const loginAPI = (data) => {
  return request({
    url: '/api/v1/auth/login',
    method: 'post',
    data: {
      username: data.username,
      password: data.password,
      remember: data.remember
    }
  })
}

// 获取用户信息
export const getUserInfoAPI = () => {
  return request({
    url: '/api/v1/auth/userinfo',
    method: 'get'
  })
}

// 用户登出
export const logoutAPI = () => {
  return request({
    url: '/api/v1/auth/logout',
    method: 'post'
  })
}

// 刷新Token
export const refreshTokenAPI = (refreshToken) => {
  return request({
    url: '/api/v1/auth/refresh',
    method: 'post',
    data: { refreshToken }
  })
}

// 修改密码
export const changePasswordAPI = (data) => {
  return request({
    url: '/api/v1/auth/change-password',
    method: 'post',
    data: {
      oldPassword: data.oldPassword,
      newPassword: data.newPassword
    }
  })
}

// 重置密码申请
export const resetPasswordRequestAPI = (email) => {
  return request({
    url: '/api/v1/auth/reset-password-request',
    method: 'post',
    data: { email }
  })
}

// 重置密码确认
export const resetPasswordConfirmAPI = (data) => {
  return request({
    url: '/api/v1/auth/reset-password-confirm',
    method: 'post',
    data: {
      token: data.token,
      newPassword: data.newPassword
    }
  })
}