<template>
  <div class="dashboard-container">
    <div class="header">
      <h1>🎓 智能培训系统 - 仪表板</h1>
      <el-button class="btn btn-secondary" @click="logout">退出登录</el-button>
    </div>
    
    <div class="content">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">24</div>
              <div class="stat-label">已完成课程</div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">8</div>
              <div class="stat-label">进行中课程</div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">12</div>
              <div class="stat-label">待完成考试</div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">87%</div>
              <div class="stat-label">平均成绩</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <el-card class="content-card" style="margin-top: 20px;">
        <h3>🎉 登录成功！</h3>
        <p>欢迎使用智能培训系统，这是一个简单的测试页面。</p>
        <p>完整的功能正在开发中...</p>
        
        <div style="margin-top: 20px;">
          <el-button class="btn btn-primary">开始学习</el-button>
          <el-button class="btn btn-secondary">参加考试</el-button>
          <el-button class="btn btn-secondary">查看进度</el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const logout = () => {
  userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<style scoped>
.dashboard-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: rgba(255, 255, 255, 0.95);
  padding: 20px;
  border-radius: 15px;
  margin-bottom: 20px;
  backdrop-filter: blur(10px);
}

.header h1 {
  margin: 0;
  color: #333;
}

.content {
  max-width: 1200px;
  margin: 0 auto;
}

.stat-card {
  border-radius: 15px;
  border: none;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.stat-content {
  text-align: center;
  padding: 20px;
}

.stat-number {
  font-size: 32px;
  font-weight: bold;
  color: #667eea;
  margin-bottom: 10px;
}

.stat-label {
  color: #666;
  font-size: 14px;
}

.content-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
  border: none;
}

.content-card:hover {
  transform: translateY(-5px);
}

.btn {
  padding: 12px 24px;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  text-decoration: none;
  display: inline-block;
  text-align: center;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
}

.btn-secondary {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
}

.btn-secondary:hover {
  background: rgba(102, 126, 234, 0.2);
}
</style>