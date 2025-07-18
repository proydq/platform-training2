<template>
  <div class="dashboard-container">
    <!-- 头部导航 -->
    <div class="header">
      <div class="logo">
        <div class="logo-icon">🎓</div>
        <h1>智能培训系统</h1>
      </div>
      
      <div class="nav-menu">
        <div class="nav-item active">
          <span class="nav-icon">📊</span>
          <span>仪表板</span>
        </div>
        <div class="nav-item" @click="goToPage('/courses')">
          <span class="nav-icon">📚</span>
          <span>我的课程</span>
        </div>
        <div class="nav-item" @click="goToPage('/exams')">
          <span class="nav-icon">📝</span>
          <span>考试中心</span>
        </div>
        <div class="nav-item" @click="goToPage('/students')" v-if="showStudentManagement">
          <span class="nav-icon">👥</span>
          <span>学员管理</span>
        </div>
        <div class="nav-item" @click="goToPage('/admin')" v-if="showAdminPanel">
          <span class="nav-icon">⚙️</span>
          <span>管理后台</span>
        </div>
      </div>
      
      <div class="user-info">
        <span class="user-name">{{ userName }}</span>
        <div class="avatar">{{ userAvatar }}</div>
        <button class="logout-btn" @click="handleLogout">退出</button>
      </div>
    </div>

    <!-- 统计卡片网格 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-number">156</div>
        <div class="stat-label">总学员数</div>
      </div>
      <div class="stat-card">
        <div class="stat-number">48</div>
        <div class="stat-label">课程总数</div>
      </div>
      <div class="stat-card">
        <div class="stat-number">89%</div>
        <div class="stat-label">系统活跃度</div>
      </div>
      <div class="stat-card">
        <div class="stat-number">2,340</div>
        <div class="stat-label">总学习时长</div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 最新课程 -->
      <div class="content-card">
        <div class="card-header">
          <h2>📚 最新课程</h2>
        </div>
        <div class="course-list">
          <div class="course-item">
            <div class="course-icon">📱</div>
            <div class="course-info">
              <div class="course-title">产品基础知识培训</div>
              <div class="course-meta">讲师：李经理 | 时长：2小时</div>
            </div>
            <button class="course-btn primary">学习</button>
          </div>
          
          <div class="course-item">
            <div class="course-icon">📊</div>
            <div class="course-info">
              <div class="course-title">市场分析与调研</div>
              <div class="course-meta">讲师：王总监 | 时长：1.5小时</div>
            </div>
            <button class="course-btn secondary">预览</button>
          </div>
          
          <div class="course-item">
            <div class="course-icon">💼</div>
            <div class="course-info">
              <div class="course-title">商务礼仪与沟通</div>
              <div class="course-meta">讲师：张老师 | 时长：1小时</div>
            </div>
            <button class="course-btn secondary">预览</button>
          </div>
        </div>
      </div>

      <!-- 考试安排 -->
      <div class="content-card">
        <div class="card-header">
          <h2>📝 考试安排</h2>
        </div>
        <div class="exam-list">
          <div class="exam-item">
            <div class="exam-info">
              <div class="exam-title">产品知识考试</div>
              <div class="exam-meta">截止：2025-01-20</div>
            </div>
            <button class="exam-btn">开始考试</button>
          </div>
          
          <div class="exam-item">
            <div class="exam-info">
              <div class="exam-title">销售技能测试</div>
              <div class="exam-meta">截止：2025-01-25</div>
            </div>
            <button class="exam-btn">开始考试</button>
          </div>
          
          <div class="exam-item">
            <div class="exam-info">
              <div class="exam-title">客户服务评估</div>
              <div class="exam-meta">截止：2025-01-30</div>
            </div>
            <button class="exam-btn">开始考试</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// 计算属性
const userName = computed(() => userStore.userName)
const userAvatar = computed(() => userStore.userAvatar)
const userRole = computed(() => userStore.userRole)

// 根据角色显示菜单
const showStudentManagement = computed(() => ['ADMIN', 'TEACHER'].includes(userRole.value))
const showAdminPanel = computed(() => userRole.value === 'ADMIN')

// 页面跳转
const goToPage = (path) => {
  router.push(path)
}

// 退出登录
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示')
    userStore.logout()
    router.push('/login')
    ElMessage.success('已退出登录')
  } catch {
    // 用户取消
  }
}
</script>

<style scoped>
.dashboard-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

/* 头部导航 */
.header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 15px;
  padding: 20px 30px;
  margin-bottom: 30px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  gap: 15px;
}

.logo-icon {
  font-size: 32px;
}

.logo h1 {
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  font-size: 24px;
  font-weight: 700;
  margin: 0;
}

.nav-menu {
  display: flex;
  gap: 20px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 500;
  color: #333;
}

.nav-item:hover {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
  transform: translateY(-2px);
}

.nav-item.active {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  transform: translateY(-2px);
}

.nav-icon {
  font-size: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.user-name {
  font-weight: 500;
  color: #333;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: bold;
  font-size: 16px;
}

.logout-btn {
  padding: 8px 16px;
  background: rgba(102, 126, 234, 0.1);
  border: 1px solid rgba(102, 126, 234, 0.3);
  border-radius: 8px;
  color: #667eea;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 14px;
  font-weight: 500;
}

.logout-btn:hover {
  background: rgba(102, 126, 234, 0.2);
  transform: translateY(-1px);
}

/* 统计卡片网格 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 15px;
  padding: 30px 25px;
  text-align: center;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.stat-number {
  font-size: 42px;
  font-weight: 700;
  color: #667eea;
  margin-bottom: 10px;
}

.stat-label {
  color: #666;
  font-size: 14px;
  font-weight: 500;
}

/* 主要内容区域 */
.main-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
}

.content-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 30px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
}

.content-card:hover {
  transform: translateY(-5px);
}

.card-header {
  margin-bottom: 25px;
}

.card-header h2 {
  color: #667eea;
  font-size: 20px;
  font-weight: 600;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

/* 课程列表 */
.course-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.course-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.course-item:hover {
  background: #e9ecef;
  transform: translateX(5px);
}

.course-icon {
  width: 50px;
  height: 50px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: white;
  flex-shrink: 0;
}

.course-info {
  flex: 1;
}

.course-title {
  font-weight: 600;
  color: #333;
  margin-bottom: 5px;
  font-size: 16px;
}

.course-meta {
  color: #666;
  font-size: 14px;
}

.course-btn {
  padding: 8px 20px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.course-btn.primary {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
}

.course-btn.primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.course-btn.secondary {
  background: #f8f9fa;
  color: #667eea;
  border: 1px solid #e9ecef;
}

.course-btn.secondary:hover {
  background: #e9ecef;
  transform: translateY(-2px);
}

/* 考试列表 */
.exam-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.exam-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px;
  background: #fff3cd;
  border-radius: 12px;
  border-left: 4px solid #ffc107;
  transition: all 0.3s ease;
}

.exam-item:hover {
  background: #ffeaa7;
  transform: translateX(5px);
}

.exam-info {
  flex: 1;
}

.exam-title {
  font-weight: 600;
  color: #333;
  margin-bottom: 5px;
  font-size: 16px;
}

.exam-meta {
  color: #856404;
  font-size: 14px;
}

.exam-btn {
  padding: 10px 20px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.exam-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .main-content {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .dashboard-container {
    padding: 10px;
  }
  
  .header {
    flex-direction: column;
    gap: 20px;
    padding: 20px;
  }
  
  .nav-menu {
    flex-wrap: wrap;
    gap: 10px;
    justify-content: center;
  }
  
  .nav-item {
    padding: 8px 16px;
    font-size: 14px;
  }
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 15px;
  }
  
  .stat-card {
    padding: 20px 15px;
  }
  
  .stat-number {
    font-size: 32px;
  }
  
  .content-card {
    padding: 20px;
  }
  
  .course-item,
  .exam-item {
    flex-direction: column;
    text-align: center;
    gap: 15px;
  }
  
  .course-info,
  .exam-info {
    text-align: center;
  }
}

@media (max-width: 480px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .nav-menu {
    flex-direction: column;
    width: 100%;
  }
  
  .nav-item {
    justify-content: center;
  }
}
</style>