<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-logo">🎓</div>
      <h1 class="login-title">智能培训系统</h1>
      <p class="login-subtitle">公司内部产品培训平台</p>
      
      <!-- 环境提示 -->
      <div class="env-notice">
        <div class="env-status" :class="backendStatus">
          {{ backendStatusText }}
        </div>
      </div>
      
      <el-form 
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            size="large"
            prefix-icon="User"
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <div class="login-options">
          <el-checkbox v-model="loginForm.remember">记住我</el-checkbox>
          <a class="forgot-password" href="#" @click.prevent>忘记密码？</a>
        </div>

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

      <div class="register-link">
        还没有账号？<a href="#" @click.prevent>联系管理员开通</a>
      </div>
      
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

      <!-- API状态说明 -->
      <div class="api-status-info">
        <el-collapse>
          <el-collapse-item title="登录说明" name="info">
            <div class="info-content">
              <p><strong>后端API状态:</strong> {{ backendStatusText }}</p>
              <p><strong>登录方式:</strong></p>
              <ul>
                <li>🟢 后端可用时：调用真实API接口登录</li>
                <li>🟡 后端不可用时：使用测试数据模拟登录</li>
              </ul>
              <p><strong>后端地址:</strong> {{ apiBaseUrl }}</p>
            </div>
          </el-collapse>
        </el-collapse>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 响应式数据
const loginFormRef = ref()
const isLoading = ref(false)
const backendStatus = ref('checking') // checking, online, offline

const loginForm = reactive({
  username: '',
  password: '',
  remember: false
})

// 表单验证规则
const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

// 计算属性 - 修复环境变量获取
const apiBaseUrl = computed(() => {
  return import.meta.env.VITE_APP_BASE_API || 'http://localhost:8080'
})

const backendStatusText = computed(() => {
  switch (backendStatus.value) {
    case 'checking':
      return '🔄 检测后端状态中...'
    case 'online':
      return '🟢 后端API在线'
    case 'offline':
      return '🟡 后端API离线，使用测试模式'
    default:
      return '❓ 未知状态'
  }
})

// 检测后端状态
const checkBackendStatus = async () => {
  try {
    console.log('检测后端API状态...')
    console.log('API基础地址:', apiBaseUrl.value)
    backendStatus.value = 'checking'
    
    // 发送简单的健康检查请求
    const controller = new AbortController()
    const timeoutId = setTimeout(() => controller.abort(), 3000)
    
    const response = await fetch(`${apiBaseUrl.value}/api/v1/health`, {
      method: 'GET',
      signal: controller.signal
    })
    
    clearTimeout(timeoutId)
    
    if (response.ok) {
      backendStatus.value = 'online'
      console.log('后端API在线')
    } else {
      throw new Error('健康检查失败')
    }
  } catch (error) {
    backendStatus.value = 'offline'
    console.log('后端API离线，将使用测试模式:', error.message)
  }
}

// 登录处理
const handleLogin = async () => {
  console.log('开始登录流程')
  
  if (!loginFormRef.value) {
    console.error('表单引用未找到')
    return
  }

  try {
    // 表单验证
    await loginFormRef.value.validate()
    
    console.log('表单验证通过，尝试登录:', loginForm.username)
    
    isLoading.value = true
    
    // 调用登录方法（会自动处理API调用和测试数据）
    const success = await userStore.login(loginForm.username, loginForm.password)
    
    if (success) {
      // 登录成功，跳转到目标页面
      const redirect = route.query.redirect || '/dashboard'
      console.log('登录成功，跳转到:', redirect)
      
      await router.push(redirect)
    }
    // 如果登录失败，userStore.login 方法内部已经显示了错误消息
    
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

// 组件挂载时的逻辑
onMounted(async () => {
  console.log('登录页面已挂载')
  console.log('当前路由:', route.path)
  console.log('重定向参数:', route.query.redirect)
  console.log('环境变量:', {
    VITE_APP_BASE_API: import.meta.env.VITE_APP_BASE_API,
    VITE_APP_TITLE: import.meta.env.VITE_APP_TITLE,
    DEV: import.meta.env.DEV,
    PROD: import.meta.env.PROD
  })
  
  // 检测后端状态
  await checkBackendStatus()
  
  // 如果用户已经登录，直接跳转
  if (userStore.isLoggedIn) {
    console.log('用户已登录，跳转到仪表板')
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
  max-width: 500px;
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
  margin-bottom: 30px;
  font-size: 16px;
}

.env-notice {
  margin-bottom: 30px;
}

.env-status {
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
}

.env-status.checking {
  background: #fff3cd;
  color: #856404;
}

.env-status.online {
  background: #d4edda;
  color: #155724;
}

.env-status.offline {
  background: #f8d7da;
  color: #721c24;
}

.login-form {
  text-align: left;
  margin-bottom: 30px;
}

.login-form :deep(.el-input__wrapper) {
  padding: 0 15px;
  border: 2px solid #e0e0e0;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.8);
  box-shadow: none;
  transition: all 0.3s ease;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.forgot-password {
  color: #667eea;
  text-decoration: none;
  font-size: 14px;
}

.forgot-password:hover {
  text-decoration: underline;
}

.login-btn {
  width: 100%;
  height: 50px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 10px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  transition: all 0.3s ease;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
}

.register-link {
  color: #666;
  font-size: 14px;
  margin-bottom: 20px;
}

.register-link a {
  color: #667eea;
  text-decoration: none;
}

.test-accounts {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 10px;
  text-align: left;
  margin-bottom: 20px;
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

.api-status-info {
  text-align: left;
}

.info-content {
  font-size: 14px;
  color: #666;
}

.info-content p {
  margin: 8px 0;
}

.info-content ul {
  margin: 8px 0;
  padding-left: 20px;
}

.info-content li {
  margin: 4px 0;
}
</style>