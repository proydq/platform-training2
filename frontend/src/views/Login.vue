<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-logo">🎓</div>
      <h1 class="login-title">智能培训系统</h1>
      <p class="login-subtitle">公司内部产品培训平台</p>
      
      <el-form 
        ref="loginFormRef"
        :model="loginForm"
        class="login-form"
      >
        <el-form-item>
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            size="large"
            prefix-icon="User"
          />
        </el-form-item>

        <el-form-item>
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            prefix-icon="Lock"
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-button
          type="primary"
          size="large"
          class="login-btn"
          :loading="isLoading"
          @click="handleLogin"
        >
          {{ isLoading ? '登录中...' : '登录' }}
        </el-button>
      </el-form>
      
      <div class="test-accounts">
        <h4>测试账号:</h4>
        <div class="account-list">
          <div class="account-item" @click="quickLogin('admin', '123456')">
            <span class="role">管理员</span>
            <span class="credentials">admin / 123456</span>
          </div>
          <div class="account-item" @click="quickLogin('teacher01', '123456')">
            <span class="role">教师</span>
            <span class="credentials">teacher01 / 123456</span>
          </div>
          <div class="account-item" @click="quickLogin('student01', '123456')">
            <span class="role">学员</span>
            <span class="credentials">student01 / 123456</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// 响应式数据
const isLoading = ref(false)
const loginForm = reactive({
  username: '',
  password: ''
})

// 登录处理
const handleLogin = async () => {
  if (!loginForm.username || !loginForm.password) {
    ElMessage.error('请输入用户名和密码')
    return
  }

  isLoading.value = true
  
  try {
    const success = userStore.login(loginForm.username, loginForm.password)
    
    if (success) {
      ElMessage.success('登录成功！')
      setTimeout(() => {
        router.push('/dashboard')
      }, 500)
    } else {
      ElMessage.error('用户名或密码错误')
    }
  } catch (error) {
    console.error('登录失败:', error)
    ElMessage.error('登录失败，请重试')
  } finally {
    isLoading.value = false
  }
}

// 快速登录
const quickLogin = (username, password) => {
  loginForm.username = username
  loginForm.password = password
  handleLogin()
}

onMounted(() => {
  console.log('登录页面已挂载')
  
  // 如果已登录，直接跳转
  if (userStore.isLoggedIn) {
    router.replace('/dashboard')
  }
})
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 50px;
  width: 100%;
  max-width: 450px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  text-align: center;
}

.login-logo {
  font-size: 64px;
  margin-bottom: 20px;
}

.login-title {
  font-size: 28px;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 10px;
}

.login-subtitle {
  color: #666;
  margin-bottom: 40px;
  font-size: 16px;
}

.login-form {
  text-align: left;
  margin-bottom: 30px;
}

.login-btn {
  width: 100%;
  height: 50px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 10px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  margin-top: 20px;
}

.test-accounts {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 10px;
  text-align: left;
}

.test-accounts h4 {
  margin: 0 0 15px 0;
  color: #333;
  text-align: center;
}

.account-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.account-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.account-item:hover {
  background: #667eea;
  color: white;
  transform: translateX(5px);
}

.role {
  font-weight: 500;
  font-size: 14px;
}

.credentials {
  font-size: 12px;
  color: #999;
}

.account-item:hover .credentials {
  color: rgba(255, 255, 255, 0.8);
}
</style>