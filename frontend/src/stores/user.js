// stores/user.js - 最小化版本，暂时不使用API调用
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))
  
  // 计算属性
  const isLoggedIn = computed(() => !!token.value && Object.keys(userInfo.value).length > 0)
  const userName = computed(() => userInfo.value.name || userInfo.value.username || '用户')
  const userAvatar = computed(() => userName.value.charAt(0).toUpperCase())
  const userRole = computed(() => userInfo.value.role || 'STUDENT')
  
  // 测试用户数据
  const testUsers = {
    admin: {
      username: 'admin',
      name: '系统管理员',
      role: 'ADMIN'
    },
    teacher01: {
      username: 'teacher01', 
      name: '张老师',
      role: 'TEACHER'
    },
    student01: {
      username: 'student01',
      name: '李同学', 
      role: 'STUDENT'
    }
  }
  
  // 简化的登录方法 - 只使用测试数据
  const login = (username, password) => {
    console.log('登录尝试:', username)
    
    const testUser = testUsers[username]
    if (testUser && password === '123456') {
      const mockToken = `mock_token_${username}_${Date.now()}`
      const userInfoData = {
        ...testUser,
        loginTime: new Date().toISOString()
      }
      
      token.value = mockToken
      userInfo.value = userInfoData
      
      localStorage.setItem('token', mockToken)
      localStorage.setItem('userInfo', JSON.stringify(userInfoData))
      
      console.log('登录成功:', userInfoData)
      return true
    }
    
    console.log('登录失败')
    return false
  }
  
  // 退出登录
  const logout = () => {
    token.value = ''
    userInfo.value = {}
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    console.log('已退出登录')
  }
  
  // 初始化
  const initializeAuth = () => {
    const localToken = localStorage.getItem('token')
    const localUserInfo = localStorage.getItem('userInfo')
    
    if (localToken && localUserInfo) {
      try {
        token.value = localToken
        userInfo.value = JSON.parse(localUserInfo)
        console.log('恢复登录状态')
      } catch (error) {
        console.error('恢复登录状态失败:', error)
        logout()
      }
    }
  }
  
  return {
    // 状态
    token,
    userInfo,
    
    // 计算属性
    isLoggedIn,
    userName,
    userAvatar,
    userRole,
    
    // 方法
    login,
    logout,
    initializeAuth
  }
})