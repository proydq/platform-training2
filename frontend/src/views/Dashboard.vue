<template>
  <div class="dashboard">
    <!-- 统计卡片网格 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-number">24</div>
        <div class="stat-label">已完成课程</div>
      </div>
      <div class="stat-card">
        <div class="stat-number">8</div>
        <div class="stat-label">进行中课程</div>
      </div>
      <div class="stat-card">
        <div class="stat-number">12</div>
        <div class="stat-label">待完成考试</div>
      </div>
      <div class="stat-card">
        <div class="stat-number">87%</div>
        <div class="stat-label">平均成绩</div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 左侧：学习进度和最新通知 -->
      <div class="left-section">
        <!-- 学习进度卡片 -->
        <div class="card">
          <h2>📈 学习进度</h2>
          <div class="progress-list">
            <div class="progress-item">
              <div class="progress-info">
                <span class="course-name">产品设计基础</span>
                <span class="progress-percent">85%</span>
              </div>
              <div class="progress-bar">
                <div class="progress-fill" :style="{ width: '85%' }"></div>
              </div>
            </div>
            <div class="progress-item">
              <div class="progress-info">
                <span class="course-name">用户体验设计</span>
                <span class="progress-percent">60%</span>
              </div>
              <div class="progress-bar">
                <div class="progress-fill" :style="{ width: '60%' }"></div>
              </div>
            </div>
            <div class="progress-item">
              <div class="progress-info">
                <span class="course-name">数据分析方法</span>
                <span class="progress-percent">30%</span>
              </div>
              <div class="progress-bar">
                <div class="progress-fill" :style="{ width: '30%' }"></div>
              </div>
            </div>
            <div class="progress-item">
              <div class="progress-info">
                <span class="course-name">项目管理实践</span>
                <span class="progress-percent">95%</span>
              </div>
              <div class="progress-bar">
                <div class="progress-fill" :style="{ width: '95%' }"></div>
              </div>
            </div>
          </div>
          <button class="btn btn-primary" @click="goToCourses">查看全部课程</button>
        </div>

        <!-- 最新通知 -->
        <div class="card">
          <h2>📢 最新通知</h2>
          <div class="notification-list">
            <div class="notification-item">
              <div class="notification-icon">📚</div>
              <div class="notification-content">
                <div class="notification-title">新课程上线</div>
                <div class="notification-desc">《产品设计进阶》课程已上线，快来学习吧！</div>
                <div class="notification-time">2小时前</div>
              </div>
            </div>
            <div class="notification-item">
              <div class="notification-icon">🎯</div>
              <div class="notification-content">
                <div class="notification-title">考试提醒</div>
                <div class="notification-desc">《用户体验设计》考试将于明天开始</div>
                <div class="notification-time">1天前</div>
              </div>
            </div>
            <div class="notification-item">
              <div class="notification-icon">🏆</div>
              <div class="notification-content">
                <div class="notification-title">成绩公布</div>
                <div class="notification-desc">《数据分析基础》考试成绩已公布</div>
                <div class="notification-time">3天前</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：快速操作和待办事项 -->
      <div class="right-section">
        <!-- 快速操作 -->
        <div class="card">
          <h2>⚡ 快速操作</h2>
          <div class="quick-actions">
            <button class="action-btn primary" @click="startLearning">
              <div class="action-icon">📖</div>
              <div class="action-text">开始学习</div>
            </button>
            <button class="action-btn secondary" @click="takeExam">
              <div class="action-icon">📝</div>
              <div class="action-text">参加考试</div>
            </button>
            <button class="action-btn secondary" @click="viewProgress">
              <div class="action-icon">📊</div>
              <div class="action-text">查看进度</div>
            </button>
            <button class="action-btn secondary" @click="downloadCertificate">
              <div class="action-icon">🏅</div>
              <div class="action-text">下载证书</div>
            </button>
          </div>
        </div>

        <!-- 待办事项 -->
        <div class="card">
          <h2>✅ 待办事项</h2>
          <div class="todo-list">
            <div class="todo-item pending">
              <div class="todo-content">
                <div class="todo-title">完成《产品设计基础》第3章</div>
                <div class="todo-desc">预计需要 30 分钟</div>
              </div>
              <button class="btn btn-primary btn-sm" @click="startTodo(1)">开始</button>
            </div>
            <div class="todo-item warning">
              <div class="todo-content">
                <div class="todo-title">参加《用户体验设计》考试</div>
                <div class="todo-desc">截止：明天 18:00</div>
              </div>
              <button class="btn btn-primary btn-sm" @click="startTodo(2)">考试</button>
            </div>
            <div class="todo-item info">
              <div class="todo-content">
                <div class="todo-title">提交《数据分析》作业</div>
                <div class="todo-desc">截止：本周五</div>
              </div>
              <button class="btn btn-primary btn-sm" @click="startTodo(3)">提交</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()

// 方法
const goToCourses = () => {
  router.push('/courses')
}

const startLearning = () => {
  ElMessage.success('开始学习')
  router.push('/courses')
}

const takeExam = () => {
  ElMessage.info('跳转到考试中心')
  router.push('/exams')
}

const viewProgress = () => {
  ElMessage.info('查看学习进度')
}

const downloadCertificate = () => {
  ElMessage.info('下载证书功能开发中...')
}

const startTodo = (id) => {
  ElMessage.success(`开始待办事项 ${id}`)
}

onMounted(() => {
  console.log('仪表板页面已加载')
})
</script>

<style scoped>
.dashboard {
  width: 100%;
}

/* 统计卡片网格 */
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
  padding: 25px;
  text-align: center;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.stat-number {
  font-size: 36px;
  font-weight: 700;
  color: #667eea;
  margin-bottom: 10px;
}

.stat-label {
  color: #666;
  font-size: 14px;
}

/* 主要内容区域 */
.main-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
}

.left-section,
.right-section {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 30px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
}

.card:hover {
  transform: translateY(-5px);
}

.card h2 {
  color: #667eea;
  font-size: 20px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}

/* 学习进度样式 */
.progress-list {
  margin-bottom: 20px;
}

.progress-item {
  margin-bottom: 15px;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
}

.course-name {
  font-weight: 500;
  color: #333;
}

.progress-percent {
  color: #667eea;
  font-weight: 600;
}

.progress-bar {
  height: 8px;
  background: #f0f0f0;
  border-radius: 4px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 4px;
  transition: width 0.3s ease;
}

/* 通知列表样式 */
.notification-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.notification-item {
  display: flex;
  gap: 15px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 10px;
  transition: all 0.3s ease;
}

.notification-item:hover {
  background: #e9ecef;
  transform: translateX(5px);
}

.notification-icon {
  font-size: 24px;
  flex-shrink: 0;
}

.notification-content {
  flex: 1;
}

.notification-title {
  font-weight: 600;
  color: #333;
  margin-bottom: 5px;
}

.notification-desc {
  color: #666;
  font-size: 14px;
  margin-bottom: 5px;
}

.notification-time {
  color: #999;
  font-size: 12px;
}

/* 快速操作样式 */
.quick-actions {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
}

.action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 20px;
  border: none;
  border-radius: 15px;
  cursor: pointer;
  transition: all 0.3s ease;
  text-decoration: none;
}

.action-btn.primary {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
}

.action-btn.secondary {
  background: #f8f9fa;
  color: #333;
  border: 1px solid #ddd;
}

.action-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.action-icon {
  font-size: 24px;
}

.action-text {
  font-weight: 500;
  font-size: 14px;
}

/* 待办事项样式 */
.todo-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.todo-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 15px;
  border-radius: 10px;
  border-left: 4px solid #ddd;
  background: #f8f9fa;
}

.todo-item.pending {
  border-left-color: #28a745;
  background: #e8f5e8;
}

.todo-item.warning {
  border-left-color: #ffc107;
  background: #fff3cd;
}

.todo-item.info {
  border-left-color: #17a2b8;
  background: #d1ecf1;
}

.todo-content {
  flex: 1;
}

.todo-title {
  font-weight: 600;
  color: #333;
  margin-bottom: 5px;
}

.todo-desc {
  color: #666;
  font-size: 14px;
}

/* 按钮样式 */
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
  color: white;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
}

.btn-sm {
  padding: 8px 16px;
  font-size: 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-content {
    grid-template-columns: 1fr;
    gap: 20px;
  }
  
  .stats-grid {
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 15px;
  }
  
  .quick-actions {
    grid-template-columns: 1fr;
  }
  
  .card {
    padding: 20px;
  }
}

@media (max-width: 480px) {
  .card h2 {
    font-size: 18px;
  }
  
  .stat-number {
    font-size: 28px;
  }
  
  .action-btn {
    padding: 15px;
  }
}
</style>