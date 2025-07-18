// stores/user.js - 支持API调用的用户状态管理
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { loginAPI, getUserInfoAPI, logoutAPI } from '@/api/auth'
import { ElMessage } from 'element-plus'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))
  
  // 计算属性
  const isLoggedIn = computed(() => !!token.value && Object.keys(userInfo.value).length > 0)
  const userName = computed(() => userInfo.value.name || userInfo.value.username || '用户')
  const userAvatar = computed(() => userInfo.value.avatar || userName.value.charAt(0).toUpperCase())
  const userRole = computed(() => userInfo.value.role || 'STUDENT')
  
  // 测试用户数据（如果后端接口不可用时的备用方案）
  const testUsers = {
    admin: {
      username: 'admin',
      name: '系统管理员',
      role: 'ADMIN',
      avatar: '/avatars/admin.jpg'
    },
    teacher01: {
      username: 'teacher01', 
      name: '张老师',
      role: 'TEACHER',
      avatar: '/avatars/teacher.jpg'
    },
    student01: {
      username: 'student01',
      name: '李同学', 
      role: 'STUDENT',
      avatar: '/avatars/student.jpg'
    }
  }
  
  // 保存token
  const setToken = (newToken) => {
    token.value = newToken
    if (newToken) {
      localStorage.setItem('token', newToken)
    } else {
      localStorage.removeItem('token')
    }
  }
  
  // 保存用户信息
  const setUserInfo = (info) => {
    userInfo.value = info
    if (info && Object.keys(info).length > 0) {
      localStorage.setItem('userInfo', JSON.stringify(info))
    } else {
      localStorage.removeItem('userInfo')
    }
  }
  
  // 登录方法 - 优先调用API，失败时使用测试数据
  const login = async (username, password) => {
    console.log('开始登录流程:', username)
    
    try {
      // 首先尝试调用后端API
      console.log('尝试调用后端登录API...')
      const response = await loginAPI({ username, password })
      
      console.log('后端登录响应:', response)
      
      if (response.code === 200 && response.data) {
        // API登录成功
        const { token: apiToken, user } = response.data
        
        setToken(apiToken)
        setUserInfo(user)
        
        console.log('API登录成功:', user)
        ElMessage.success(`欢迎回来，${user.name || username}！`)
        return true
      } else {
        throw new Error(response.message || '登录失败')
      }
      
    } catch (apiError) {
      console.warn('API登录失败，尝试使用测试数据:', apiError.message)
      
      // API失败时使用测试数据
      const testUser = testUsers[username]
      if (testUser && password === '123456') {
        // 模拟成功登录
        const mockToken = `mock_token_${username}_${Date.now()}`
        const userInfoData = {
          ...testUser,
          loginTime: new Date().toISOString()
        }
        
        setToken(mockToken)
        setUserInfo(userInfoData)
        
        console.log('测试登录成功:', userInfoData)
        ElMessage.success(`欢迎回来，${testUser.name}！(测试模式)`)
        return true
      } else {
        console.log('测试登录失败: 用户名或密码错误')
        ElMessage.error('用户名或密码错误')
        return false
      }
    }
  }
  
  // 退出登录
  const logout = async () => {
    console.log('开始退出登录流程')
    
    try {
      // 尝试调用后端退出接口
      if (token.value && !token.value.includes('mock_token')) {
        console.log('调用后端退出登录API...')
        await logoutAPI()
        console.log('后端退出登录成功')
      }
    } catch (error) {
      console.warn('后端退出登录失败:', error.message)
      // 即使后端调用失败，也要清除本地状态
    }
    
    // 清除本地状态
    setToken('')
    setUserInfo({})
    
    console.log('本地登录状态已清除')
    return true
  }
  
  // 获取用户信息（用于刷新用户数据）
  const fetchUserInfo = async () => {
    if (!token.value) {
      return false
    }
    
    try {
      console.log('获取用户信息...')
      const response = await getUserInfoAPI()
      
      if (response.code === 200 && response.data) {
        setUserInfo(response.data)
        console.log('用户信息更新成功:', response.data)
        return true
      } else {
        throw new Error(response.message || '获取用户信息失败')
      }
    } catch (error) {
      console.error('获取用户信息失败:', error.message)
      // 如果获取用户信息失败，可能token已过期
      if (error.response?.status === 401) {
        await logout()
      }
      return false
    }
  }
  
  // 初始化认证状态
  const initializeAuth = async () => {
    console.log('初始化认证状态...')
    
    const localToken = localStorage.getItem('token')
    const localUserInfo = localStorage.getItem('userInfo')
    
    if (localToken && localUserInfo) {
      try {
        const user = JSON.parse(localUserInfo)
        
        // 设置本地状态
        token.value = localToken
        userInfo.value = user
        
        // 如果不是测试token，尝试验证有效性
        if (!localToken.includes('mock_token')) {
          console.log('验证token有效性...')
          const isValid = await fetchUserInfo()
          if (!isValid) {
            console.log('Token已失效，清除本地状态')
            await logout()
          }
        } else {
          console.log('使用测试token，跳过验证')
        }
        
        console.log('认证状态初始化完成')
      } catch (error) {
        console.error('初始化认证状态失败:', error)
        await logout()
      }
    } else {
      console.log('未找到本地认证信息')
    }
  }
  
  // 检查权限
  const hasPermission = (requiredRoles) => {
    if (!requiredRoles || requiredRoles.length === 0) {
      return true
    }
    return requiredRoles.includes(userRole.value)
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
    setToken,
    setUserInfo,
    login,
    logout,
    fetchUserInfo,
    initializeAuth,
    hasPermission
  }
})