<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-logo">🎓</div>
      <h1 class="login-title">智能培训系统</h1>
      <p class="login-subtitle">公司内部产品培训平台</p>
      
      <el-form class="login-form">
        <el-form-item>
          <el-input
            v-model="username"
            placeholder="请输入用户名"
            size="large"
            prefix-icon="User"
          />
        </el-form-item>
        
        <el-form-item>
          <el-input
            v-model="password"
            type="password"
            placeholder="请输入密码"
            size="large"
            prefix-icon="Lock"
          />
        </el-form-item>
        
        <el-button
          type="primary"
          size="large"
          class="login-btn"
          @click="handleLogin"
        >
          登录
        </el-button>
      </el-form>
      
      <div class="test-accounts">
        <h4>测试账号:</h4>
        <p>管理员: admin / 123456</p>
        <p>学员: student01 / 123456</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const username = ref('')
const password = ref('')

const handleLogin = () => {
  if (!username.value || !password.value) {
    ElMessage.error('请输入用户名和密码')
    return
  }
  
  // 使用状态管理登录
  if (userStore.login(username.value, password.value)) {
    ElMessage.success('登录成功！')
    router.push('/dashboard')
  } else {
    ElMessage.error('用户名或密码错误')
  }
}
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
}

.test-accounts {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 10px;
  text-align: left;
}

.test-accounts h4 {
  margin: 0 0 10px 0;
  color: #333;
}

.test-accounts p {
  margin: 5px 0;
  color: #666;
  font-size: 14px;
}
</style>