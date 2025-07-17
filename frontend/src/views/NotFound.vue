<template>
  <div class="not-found-container">
    <div class="not-found-content">
      <div class="error-animation">
        <div class="error-number">4</div>
        <div class="error-icon">🎓</div>
        <div class="error-number">4</div>
      </div>
      
      <h1 class="error-title">页面走丢了</h1>
      <p class="error-description">
        抱歉，您访问的页面不存在或已被移动。<br>
        请检查URL是否正确，或返回首页继续学习。
      </p>
      
      <div class="error-actions">
        <el-button type="primary" size="large" @click="goHome">
          <el-icon><House /></el-icon>
          返回首页
        </el-button>
        <el-button size="large" @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回上页
        </el-button>
      </div>
      
      <div class="helpful-links">
        <h3>您可能想要：</h3>
        <div class="links-grid">
          <div class="link-item" @click="goToCourses">
            <div class="link-icon">📚</div>
            <div class="link-text">
              <div class="link-title">浏览课程</div>
              <div class="link-desc">查看所有可用的培训课程</div>
            </div>
          </div>
          
          <div class="link-item" @click="goToExams">
            <div class="link-icon">📝</div>
            <div class="link-text">
              <div class="link-title">参加考试</div>
              <div class="link-desc">完成待完成的考试任务</div>
            </div>
          </div>
          
          <div class="link-item" @click="contactSupport">
            <div class="link-icon">🆘</div>
            <div class="link-text">
              <div class="link-title">联系支持</div>
              <div class="link-desc">获取技术支持和帮助</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { House, ArrowLeft } from '@element-plus/icons-vue'

const router = useRouter()

// 页面跳转方法
const goHome = () => {
  router.push('/dashboard')
}

const goBack = () => {
  if (window.history.length > 1) {
    router.go(-1)
  } else {
    router.push('/dashboard')
  }
}

const goToCourses = () => {
  router.push('/courses')
}

const goToExams = () => {
  router.push('/exams')
}

const contactSupport = () => {
  ElMessage.info('请联系技术支持：support@company.com')
}
</script>

<style scoped>
.not-found-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.not-found-content {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 60px 40px;
  text-align: center;
  max-width: 600px;
  width: 100%;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  animation: fadeInUp 0.8s ease forwards;
}

.error-animation {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
  margin-bottom: 30px;
}

.error-number {
  font-size: 120px;
  font-weight: bold;
  color: #667eea;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
  animation: bounce 2s infinite;
}

.error-icon {
  font-size: 80px;
  animation: rotate 3s linear infinite;
}

.error-title {
  font-size: 36px;
  font-weight: 700;
  color: #333;
  margin: 0 0 15px 0;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.error-description {
  font-size: 16px;
  color: #666;
  line-height: 1.6;
  margin: 0 0 40px 0;
}

.error-actions {
  display: flex;
  gap: 15px;
  justify-content: center;
  margin-bottom: 50px;
}

.helpful-links h3 {
  color: #333;
  margin: 0 0 25px 0;
  font-size: 20px;
}

.links-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 15px;
}

.link-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 20px;
  background: rgba(102, 126, 234, 0.05);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  text-align: left;
}

.link-item:hover {
  background: rgba(102, 126, 234, 0.1);
  transform: translateX(5px);
}

.link-icon {
  font-size: 32px;
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 10px;
}

.link-text {
  flex: 1;
}

.link-title {
  font-weight: 600;
  color: #333;
  margin-bottom: 5px;
  font-size: 16px;
}

.link-desc {
  color: #666;
  font-size: 14px;
}

/* 动画效果 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes bounce {
  0%, 20%, 50%, 80%, 100% {
    transform: translateY(0);
  }
  40% {
    transform: translateY(-10px);
  }
  60% {
    transform: translateY(-5px);
  }
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .not-found-content {
    padding: 40px 20px;
  }
  
  .error-animation {
    gap: 10px;
  }
  
  .error-number {
    font-size: 80px;
  }
  
  .error-icon {
    font-size: 60px;
  }
  
  .error-title {
    font-size: 28px;
  }
  
  .error-actions {
    flex-direction: column;
    align-items: center;
  }
  
  .error-actions .el-button {
    width: 200px;
  }
}

@media (max-width: 480px) {
  .error-number {
    font-size: 60px;
  }
  
  .error-icon {
    font-size: 40px;
  }
  
  .error-title {
    font-size: 24px;
  }
  
  .link-item {
    flex-direction: column;
    text-align: center;
    gap: 10px;
  }
}
</style>