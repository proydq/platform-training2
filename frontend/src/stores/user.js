// stores/user.js - 修复版本，避免无限请求和调试信息
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))
  const backendStatus = ref('offline') // 默认离线，避免初始请求
  
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
  
  // 检查后端状态 - 添加防抖机制
  let checkingBackend = false
  const checkBackendStatus = async () => {
    if (checkingBackend) return backendStatus.value === 'online'
    
    checkingBackend = true
    try {
      // 简单的fetch请求，避免axios拦截器
      const response = await fetch('http://localhost:8080/api/v1/health', {
        method: 'GET',
        timeout: 3000
      })
      
      if (response.ok) {
        backendStatus.value = 'online'
        return true
      } else {
        backendStatus.value = 'offline'
        return false
      }
    } catch (error) {
      backendStatus.value = 'offline'
      return false
    } finally {
      checkingBackend = false
    }
  }
  
  // API登录 - 使用fetch避免循环
  const loginWithAPI = async (username, password) => {
    try {
      const response = await fetch('http://localhost:8080/api/v1/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ username, password })
      })
      
      if (response.ok) {
        const data = await response.json()
        
        if (data.code === 200 && data.data) {
          const { token: apiToken, user } = data.data
          
          token.value = apiToken
          userInfo.value = user
          
          localStorage.setItem('token', apiToken)
          localStorage.setItem('userInfo', JSON.stringify(user))
          
          ElMessage.success(`欢迎回来，${user.name}！`)
          return true
        }
      }
      
      throw new Error('API登录失败')
    } catch (error) {
      throw error
    }
  }
  
  // 测试数据登录
  const loginWithTestData = (username, password) => {
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
      
      ElMessage.success(`欢迎回来，${testUser.name}！`)
      return true
    }
    
    return false
  }
  
  // 智能登录 - 简化逻辑
  const login = async (username, password) => {
    // 首先尝试测试数据登录（快速响应）
    if (loginWithTestData(username, password)) {
      return true
    }
    
    // 如果测试数据失败，尝试API登录
    try {
      const isBackendOnline = await checkBackendStatus()
      if (isBackendOnline) {
        return await loginWithAPI(username, password)
      }
    } catch (error) {
      // API登录失败，已经尝试过测试数据了
    }
    
    ElMessage.error('用户名或密码错误')
    return false
  }
  
  // 退出登录
  const logout = () => {
    token.value = ''
    userInfo.value = {}
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }
  
  // 初始化认证状态 - 简化，不做API请求
  const initializeAuth = () => {
    const localToken = localStorage.getItem('token')
    const localUserInfo = localStorage.getItem('userInfo')
    
    if (localToken && localUserInfo) {
      try {
        token.value = localToken
        userInfo.value = JSON.parse(localUserInfo)
      } catch (error) {
        logout()
      }
    }
  }
  
  return {
    // 状态
    token,
    userInfo,
    backendStatus,
    
    // 计算属性
    isLoggedIn,
    userName,
    userAvatar,
    userRole,
    
    // 方法
    login,
    logout,
    initializeAuth,
    checkBackendStatus
  }
})