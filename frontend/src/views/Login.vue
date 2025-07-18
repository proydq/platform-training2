<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-logo">🎓</div>
      <h1 class="login-title">智能培训系统</h1>
      <p class="login-subtitle">公司内部产品培训平台</p>
      
      <form class="login-form" @submit.prevent="handleLogin">
        <div class="form-group">
          <input
            v-model="loginForm.username"
            type="text"
            class="form-input"
            placeholder="请输入用户名"
            required
          />
        </div>

        <div class="form-group">
          <input
            v-model="loginForm.password"
            type="password"
            class="form-input"
            placeholder="请输入密码"
            required
            @keyup.enter="handleLogin"
          />
        </div>

        <div class="login-options">
          <label class="remember-me">
            <input v-model="loginForm.remember" type="checkbox" />
            记住我
          </label>
          <a href="#" class="forgot-password" @click.prevent>忘记密码？</a>
        </div>

        <button
          type="submit"
          class="login-btn"
          :disabled="isLoading"
        >
          <span v-if="isLoading" class="loading-spinner"></span>
          {{ isLoading ? '登录中...' : '登录' }}
        </button>
      </form>

      <div class="register-link">
        还没有账号？<a href="#" @click.prevent>联系管理员开通</a>
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
  password: '',
  remember: false
})

// 登录处理
const handleLogin = async () => {
  if (!loginForm.username || !loginForm.password) {
    ElMessage.error('请输入用户名和密码')
    return
  }

  isLoading.value = true
  
  try {
    const success = await userStore.login(loginForm.username, loginForm.password)
    
    if (success) {
      // 延迟跳转，确保状态更新完成
      setTimeout(() => {
        router.push('/dashboard')
      }, 300)
    }
  } catch (error) {
    ElMessage.error('登录失败，请重试')
  } finally {
    isLoading.value = false
  }
}

// 快速登录 - 保留功能但不显示界面
const quickLogin = (username, password) => {
  loginForm.username = username
  loginForm.password = password
  handleLogin()
}

onMounted(() => {
  // 简化挂载逻辑，避免无限请求
  if (userStore.isLoggedIn) {
    router.replace('/dashboard')
  }
})
</script>

<style scoped>
/* 按原始HTML设计的样式 */
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
  animation: fadeInUp 0.6s ease forwards;
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

/* 原始表单样式 */
.login-form {
  text-align: left;
  margin-bottom: 30px;
}

.form-group {
  margin-bottom: 25px;
}

.form-input {
  width: 100%;
  padding: 15px;
  border: 2px solid #e0e0e0;
  border-radius: 10px;
  font-size: 16px;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.8);
  box-sizing: border-box;
}

.form-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.remember-me {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.remember-me input {
  width: 18px;
  height: 18px;
  cursor: pointer;
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
  padding: 16px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
}

.login-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.loading-spinner {
  display: inline-block;
  width: 20px;
  height: 20px;
  border: 2px solid #f3f3f3;
  border-top: 2px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
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

.register-link a:hover {
  text-decoration: underline;
}



/* 动画 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-card {
    padding: 30px 25px;
  }
  
  .login-logo {
    font-size: 48px;
  }
  
  .login-title {
    font-size: 24px;
  }
  
  .form-input {
    padding: 12px;
  }
  
  .login-options {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
  }
}

@media (max-width: 480px) {
  .login-container {
    padding: 10px;
  }
  
  .login-card {
    padding: 25px 20px;
  }
  

}
</style>