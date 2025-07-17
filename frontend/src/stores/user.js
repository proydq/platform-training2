// stores/user.js - 最简化版本
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  // 简单的用户状态
  const isLoggedIn = ref(false)
  const userInfo = ref(null)

  // 简单的登录方法
  const login = (username, password) => {
    // 简单验证
    if (username === 'admin' && password === '123456') {
      isLoggedIn.value = true
      userInfo.value = {
        username: 'admin',
        name: '管理员'
      }
      return true
    } else if (username === 'student01' && password === '123456') {
      isLoggedIn.value = true
      userInfo.value = {
        username: 'student01',
        name: '学员01'
      }
      return true
    }
    return false
  }

  // 登出方法
  const logout = () => {
    isLoggedIn.value = false
    userInfo.value = null
  }

  return {
    isLoggedIn,
    userInfo,
    login,
    logout
  }
})