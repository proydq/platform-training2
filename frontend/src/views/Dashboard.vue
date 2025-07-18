<template>
  <div class="dashboard-container">
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
            <button class="course-btn primary" @click="goToCourses">学习</button>
          </div>
          
          <div class="course-item">
            <div class="course-icon">📊</div>
            <div class="course-info">
              <div class="course-title">市场分析与调研</div>
              <div class="course-meta">讲师：王总监 | 时长：1.5小时</div>
            </div>
            <button class="course-btn secondary" @click="goToCourses">预览</button>
          </div>
          
          <div class="course-item">
            <div class="course-icon">💼</div>
            <div class="course-info">
              <div class="course-title">商务礼仪与沟通</div>
              <div class="course-meta">讲师：张老师 | 时长：1小时</div>
            </div>
            <button class="course-btn secondary" @click="goToCourses">预览</button>
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
            <button class="exam-btn" @click="goToExams">开始考试</button>
          </div>
          
          <div class="exam-item">
            <div class="exam-info">
              <div class="exam-title">销售技能测试</div>
              <div class="exam-meta">截止：2025-01-25</div>
            </div>
            <button class="exam-btn" @click="goToExams">开始考试</button>
          </div>
          
          <div class="exam-item">
            <div class="exam-info">
              <div class="exam-title">客户服务评估</div>
              <div class="exam-meta">截止：2025-01-30</div>
            </div>
            <button class="exam-btn" @click="goToExams">开始考试</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 快捷操作卡片 -->
    <div class="quick-actions">
      <div class="action-card" @click="goToCourses">
        <div class="action-icon">📚</div>
        <div class="action-title">我的课程</div>
        <div class="action-desc">查看和学习课程</div>
      </div>
      
      <div class="action-card" @click="goToExams">
        <div class="action-icon">📝</div>
        <div class="action-title">考试中心</div>
        <div class="action-desc">参加在线考试</div>
      </div>
      
      <div class="action-card" @click="goToStudents" v-if="showStudentManagement">
        <div class="action-icon">👥</div>
        <div class="action-title">学员管理</div>
        <div class="action-desc">管理学员信息</div>
      </div>
      
      <div class="action-card" @click="goToAdmin" v-if="showAdminPanel">
        <div class="action-icon">⚙️</div>
        <div class="action-title">管理后台</div>
        <div class="action-desc">系统管理与配置</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// 计算属性
const userRole = computed(() => userStore.userInfo?.role || 'STUDENT')

// 根据角色显示菜单
const showStudentManagement = computed(() => {
  return ['ADMIN', 'TEACHER'].includes(userRole.value)
})

const showAdminPanel = computed(() => {
  return userRole.value === 'ADMIN'
})

// 页面跳转方法
const goToCourses = () => {
  router.push('/courses')
}

const goToExams = () => {
  router.push('/exams')
}

const goToStudents = () => {
  router.push('/students')
}

const goToAdmin = () => {
  router.push('/admin')
}
</script>

<style scoped>
.dashboard-container {
  /* Layout组件已经处理了最大宽度和padding */
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 15px;
  padding: 30px 25px;
  text-align: center;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  animation: fadeInUp 0.6s ease forwards;
}

.stat-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.15);
}

.stat-number {
  font-size: 36px;
  font-weight: 700;
  color: #667eea;
  margin-bottom: 8px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.stat-label {
  color: #666;
  font-size: 16px;
  font-weight: 500;
}

/* 主要内容区域 */
.main-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
  margin-bottom: 30px;
}

.content-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 15px;
  padding: 25px;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  animation: fadeInUp 0.8s ease forwards;
}

.content-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.15);
}

.card-header {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 2px solid #f0f0f0;
}

.card-header h2 {
  margin: 0;
  color: #333;
  font-size: 20px;
  font-weight: 600;
}

/* 课程列表 */
.course-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.course-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 10px;
  transition: all 0.3s ease;
}

.course-item:hover {
  background: #e9ecef;
  transform: translateX(5px);
}

.course-icon {
  font-size: 24px;
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 10px;
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
  padding: 8px 16px;
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
  gap: 15px;
}

.exam-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 15px;
  background: #fff3cd;
  border-radius: 10px;
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

/* 快捷操作 */
.quick-actions {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-top: 30px;
}

.action-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 15px;
  padding: 30px 25px;
  text-align: center;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  cursor: pointer;
  animation: fadeInUp 1s ease forwards;
}

.action-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
}

.action-icon {
  font-size: 48px;
  margin-bottom: 15px;
}

.action-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.action-desc {
  color: #666;
  font-size: 14px;
}

/* 动画 */
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
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 15px;
  }
  
  .stat-card {
    padding: 20px 15px;
  }
  
  .stat-number {
    font-size: 28px;
  }
  
  .content-card {
    padding: 20px;
  }
  
  .course-item,
  .exam-item {
    flex-direction: column;
    text-align: center;
    gap: 10px;
  }
  
  .course-info,
  .exam-info {
    text-align: center;
  }
  
  .quick-actions {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>