// store/user.js
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))
  
  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const userName = computed(() => userInfo.value.userName || userInfo.value.username || '用户')
  const userAvatar = computed(() => userInfo.value.avatar || userName.value.charAt(0).toUpperCase())
  const userRoles = computed(() => userInfo.value.roles || [])
  
  // 方法
  const setToken = (newToken) => {
    token.value = newToken
    if (newToken) {
      localStorage.setItem('token', newToken)
    } else {
      localStorage.removeItem('token')
    }
  }
  
  const setUserInfo = (info) => {
    userInfo.value = info
    if (info && Object.keys(info).length > 0) {
      localStorage.setItem('userInfo', JSON.stringify(info))
    } else {
      localStorage.removeItem('userInfo')
    }
  }
  
  const login = (loginData) => {
    setToken(loginData.token)
    setUserInfo(loginData.userInfo || loginData.user || {})
  }
  
  const logout = () => {
    setToken('')
    setUserInfo({})
    // 可以在这里调用登出API
  }
  
  const hasRole = (roles) => {
    if (!Array.isArray(roles)) {
      roles = [roles]
    }
    return roles.some(role => userRoles.value.includes(role))
  }
  
  const hasPermission = (permission) => {
    // 简单的权限检查，可以根据实际需求扩展
    return userRoles.value.includes('ADMIN') || userRoles.value.includes(permission)
  }
  
  return {
    // 状态
    token,
    userInfo,
    
    // 计算属性
    isLoggedIn,
    userName,
    userAvatar,
    userRoles,
    
    // 方法
    setToken,
    setUserInfo,
    login,
    logout,
    hasRole,
    hasPermission
  }
})