// frontend/src/stores/user.js - 删除测试数据版本
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))
  const backendStatus = ref('online') // 默认在线
  
  // 计算属性
  const isLoggedIn = computed(() => !!token.value && Object.keys(userInfo.value).length > 0)
  const userName = computed(() => userInfo.value.realName || userInfo.value.name || userInfo.value.username || '用户')
  const userAvatar = computed(() => userName.value.charAt(0).toUpperCase())
  const userRole = computed(() => {
    // 优先使用role字段
    if (userInfo.value.role) {
      return userInfo.value.role
    }
    
    // 从roles数组中提取第一个角色的roleCode
    if (userInfo.value.roles && userInfo.value.roles.length > 0) {
      return userInfo.value.roles[0].roleCode
    }
    
    // 默认为学员角色
    return 'STUDENT'
  })
  
  // 检查后端状态
  const checkBackendStatus = async () => {
    try {
      const response = await fetch('/api/v1/auth/login', {
        method: 'HEAD',
        timeout: 3000
      })
      
      backendStatus.value = response.ok ? 'online' : 'offline'
      return response.ok
    } catch (error) {
      backendStatus.value = 'offline'
      return false
    }
  }
  
  // API登录 - 只使用真实后端
  const login = async (username, password) => {
    try {
      console.log('使用真实后端登录:', username)

      const response = await fetch('/api/v1/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ username, password })
      })
      
      if (response.ok) {
        const data = await response.json()
        
        if (data.code === 200 && data.data) {
          // 后端返回的数据结构：{accessToken, userInfo}
          const { accessToken, userInfo: userInfoData } = data.data
          
          // 处理角色信息：从roles数组中提取主要角色到role字段
          if (userInfoData.roles && userInfoData.roles.length > 0 && !userInfoData.role) {
            userInfoData.role = userInfoData.roles[0].roleCode
          }
          
          token.value = accessToken
          userInfo.value = userInfoData
          
          localStorage.setItem('token', accessToken)
          localStorage.setItem('userInfo', JSON.stringify(userInfoData))
          
          console.log('✅ 真实登录成功，Token:', accessToken.substring(0, 30) + '...')
          console.log('✅ 用户角色:', userInfoData.role || (userInfoData.roles?.[0]?.roleCode))
          ElMessage.success(`欢迎回来，${userInfoData.realName || userInfoData.username}！`)
          return true
        } else {
          ElMessage.error(data.message || '登录失败')
          return false
        }
      } else {
        const errorData = await response.json()
        ElMessage.error(errorData.message || '登录失败')
        return false
      }
    } catch (error) {
      console.error('登录失败:', error)
      ElMessage.error('连接服务器失败，请检查网络')
      return false
    }
  }
  
  // 退出登录
  const logout = () => {
    token.value = ''
    userInfo.value = {}
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    ElMessage.success('已退出登录')
  }
  
  // 初始化认证状态
  const initializeAuth = () => {
    const localToken = localStorage.getItem('token')
    const localUserInfo = localStorage.getItem('userInfo')
    
    if (localToken && localUserInfo) {
      try {
        // 只有真实JWT Token才恢复状态
        if (localToken.startsWith('eyJ')) {
          token.value = localToken
          userInfo.value = JSON.parse(localUserInfo)
          console.log('恢复登录状态:', userInfo.value.username)
        } else {
          // 清除非JWT Token
          console.log('清除无效Token:', localToken.substring(0, 20))
          logout()
        }
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
