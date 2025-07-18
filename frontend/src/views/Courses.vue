<template>
  <div class="courses-container">
    <!-- 学习概览 -->
    <div class="overview-card card">
      <h2>📊 学习概览</h2>
      <div class="course-overview">
        <div class="overview-item completed">
          <div class="overview-number">24</div>
          <div class="overview-label">已完成</div>
        </div>
        <div class="overview-item in-progress">
          <div class="overview-number">8</div>
          <div class="overview-label">进行中</div>
        </div>
        <div class="overview-item not-started">
          <div class="overview-number">4</div>
          <div class="overview-label">待开始</div>
        </div>
        <div class="overview-item study-time">
          <div class="overview-number">156h</div>
          <div class="overview-label">学习时长</div>
        </div>
      </div>
      
      <!-- 课程分类筛选 -->
      <div class="course-filters">
        <button 
          v-for="filter in filters" 
          :key="filter.key"
          class="filter-btn"
          :class="{ active: activeFilter === filter.key }"
          @click="setActiveFilter(filter.key)"
        >
          {{ filter.label }}
        </button>
      </div>
      
      <!-- 推荐课程 -->
      <h3 style="margin-bottom: 15px;">🎯 为你推荐</h3>
      <div class="recommended-course">
        <div class="course-icon recommended">🚀</div>
        <div class="course-info">
          <div class="course-title">AI产品设计实战</div>
          <div class="course-meta">🤖 基于你的学习记录推荐 | 讲师：AI专家</div>
        </div>
        <button class="btn-recommended" @click="startCourse('ai-design')">开始学习</button>
      </div>
    </div>

    <!-- 课程列表 -->
    <div class="courses-card card">
      <h2>📚 我的课程</h2>
      
      <!-- 搜索栏 -->
      <div class="search-section">
        <input 
          v-model="searchKeyword"
          type="text" 
          placeholder="搜索课程名称..." 
          class="search-input"
          @input="searchCourses"
        />
      </div>
      
      <div class="course-list">
        <!-- 进行中的课程 -->
        <div class="course-item in-progress" data-category="in-progress">
          <div class="course-icon">📱</div>
          <div class="course-content">
            <div class="course-title">产品基础知识培训</div>
            <div class="course-meta">讲师：李经理 | 时长：2小时 | 🎥 12个视频</div>
            <div class="course-progress-section">
              <div class="progress-bar">
                <div class="progress-fill" style="width: 65%"></div>
              </div>
              <span class="progress-text">进度：65%</span>
              <span class="course-status status-progress">进行中</span>
            </div>
          </div>
          <div class="course-actions">
            <button class="btn-continue" @click="continueCourse('product-basic')">继续学习</button>
            <button class="btn-favorite" @click="toggleFavorite('product-basic')">💖</button>
          </div>
        </div>

        <!-- 已完成的课程 -->
        <div class="course-item completed" data-category="completed">
          <div class="course-icon">🎨</div>
          <div class="course-content">
            <div class="course-title">UI设计基础课程</div>
            <div class="course-meta">讲师：设计师张三 | 时长：3小时 | 🎥 18个视频</div>
            <div class="course-progress-section">
              <div class="progress-bar">
                <div class="progress-fill completed" style="width: 100%"></div>
              </div>
              <span class="progress-text">已完成</span>
              <span class="course-status status-completed">✅ 已完成</span>
              <span class="course-score">得分：92分</span>
            </div>
          </div>
          <div class="course-actions">
            <button class="btn-review" @click="reviewCourse('ui-basic')">复习</button>
            <button class="btn-certificate" @click="downloadCertificate('ui-basic')">📜 证书</button>
            <button class="btn-favorite active" @click="toggleFavorite('ui-basic')">💖</button>
          </div>
        </div>

        <!-- 待开始的课程 -->
        <div class="course-item not-started" data-category="not-started">
          <div class="course-icon locked">🔒</div>
          <div class="course-content">
            <div class="course-title">高级数据分析</div>
            <div class="course-meta">讲师：数据专家 | 时长：4小时 | 🎥 24个视频</div>
            <div class="course-progress-section">
              <div class="progress-bar">
                <div class="progress-fill" style="width: 0%"></div>
              </div>
              <span class="progress-text">待开始</span>
              <span class="course-status status-locked">🔒 需要完成前置课程</span>
            </div>
          </div>
          <div class="course-actions">
            <button class="btn-prerequisites" @click="viewPrerequisites('data-analysis')">查看前置要求</button>
            <button class="btn-favorite" @click="toggleFavorite('data-analysis')">💖</button>
          </div>
        </div>

        <!-- 新课程 -->
        <div class="course-item new" data-category="not-started">
          <div class="course-icon new">✨</div>
          <div class="course-content">
            <div class="course-title">
              <span>前端开发实战</span>
              <span class="new-badge">NEW</span>
            </div>
            <div class="course-meta">讲师：前端专家 | 时长：6小时 | 🎥 30个视频</div>
            <div class="course-progress-section">
              <div class="progress-bar">
                <div class="progress-fill" style="width: 0%"></div>
              </div>
              <span class="progress-text">全新课程</span>
              <span class="course-status status-new">🚀 立即开始</span>
            </div>
          </div>
          <div class="course-actions">
            <button class="btn-start" @click="startCourse('frontend-dev')">开始学习</button>
            <button class="btn-favorite" @click="toggleFavorite('frontend-dev')">💖</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 学习路径推荐 -->
    <div class="learning-path-card card">
      <h2>🛤️ 学习路径</h2>
      <div class="learning-paths">
        <div class="path-item">
          <div class="path-header">
            <h3>产品经理成长路径</h3>
            <span class="path-progress">2/5 完成</span>
          </div>
          <div class="path-courses">
            <div class="path-course completed">产品基础</div>
            <div class="path-course completed">用户研究</div>
            <div class="path-course current">产品设计</div>
            <div class="path-course">数据分析</div>
            <div class="path-course">产品运营</div>
          </div>
          <button class="btn-continue-path" @click="continuePath('pm-path')">继续路径</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

// 响应式数据
const searchKeyword = ref('')
const activeFilter = ref('all')

// 筛选器配置
const filters = ref([
  { key: 'all', label: '全部' },
  { key: 'in-progress', label: '进行中' },
  { key: 'completed', label: '已完成' },
  { key: 'not-started', label: '待开始' },
  { key: 'favorites', label: '⭐ 收藏' }
])

// 方法
const setActiveFilter = (filterKey) => {
  activeFilter.value = filterKey
  filterCourses(filterKey)
}

const filterCourses = (category) => {
  const courses = document.querySelectorAll('.course-item')
  
  courses.forEach(course => {
    if (category === 'all' || course.dataset.category === category) {
      course.style.display = 'flex'
    } else {
      course.style.display = 'none'
    }
  })
}

const searchCourses = () => {
  const searchTerm = searchKeyword.value.toLowerCase()
  const courses = document.querySelectorAll('.course-item')
  
  courses.forEach(course => {
    const title = course.querySelector('.course-title').textContent.toLowerCase()
    if (title.includes(searchTerm)) {
      course.style.display = 'flex'
    } else {
      course.style.display = 'none'
    }
  })
}

const startCourse = (courseId) => {
  ElMessage.success(`正在启动课程: ${courseId}`)
}

const continueCourse = (courseId) => {
  ElMessage.success(`继续学习课程: ${courseId}`)
}

const reviewCourse = (courseId) => {
  ElMessage.success(`正在加载复习模式: ${courseId}`)
}

const downloadCertificate = (courseId) => {
  ElMessage.success(`正在下载证书: ${courseId}`)
}

const viewPrerequisites = (courseId) => {
  ElMessage.warning(`查看课程前置要求: ${courseId}`)
}

const toggleFavorite = (courseId) => {
  ElMessage.success(`已${Math.random() > 0.5 ? '添加到' : '取消'}收藏: ${courseId}`)
}

const continuePath = (pathId) => {
  ElMessage.success(`继续学习路径: ${pathId}`)
}

onMounted(() => {
  // 初始化
})
</script>

<style scoped>
.courses-container {
  /* Layout已处理容器样式 */
}

.card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 15px;
  padding: 25px;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.card h2 {
  margin: 0 0 20px 0;
  color: #333;
  font-size: 20px;
  font-weight: 600;
}

/* 学习概览 */
.course-overview {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 15px;
  margin-bottom: 20px;
}

.overview-item {
  text-align: center;
  padding: 15px;
  border-radius: 10px;
}

.overview-item.completed {
  background: #e3f2fd;
}

.overview-item.in-progress {
  background: #fff3e0;
}

.overview-item.not-started {
  background: #f3e5f5;
}

.overview-item.study-time {
  background: #e8f5e8;
}

.overview-number {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 5px;
}

.overview-item.completed .overview-number {
  color: #1976d2;
}

.overview-item.in-progress .overview-number {
  color: #f57c00;
}

.overview-item.not-started .overview-number {
  color: #7b1fa2;
}

.overview-item.study-time .overview-number {
  color: #2e7d32;
}

.overview-label {
  color: #666;
  font-size: 14px;
}

/* 课程筛选 */
.course-filters {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.filter-btn {
  padding: 8px 16px;
  border: 1px solid #ddd;
  background: #f8f9fa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: #666;
  font-size: 14px;
}

.filter-btn.active {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border-color: transparent;
}

.filter-btn:hover {
  transform: translateY(-1px);
}

/* 推荐课程 */
.recommended-course {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 10px;
  color: white;
  margin-bottom: 10px;
}

.course-icon.recommended {
  width: 60px;
  height: 60px;
  background: rgba(255,255,255,0.2);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.btn-recommended {
  background: rgba(255,255,255,0.2);
  border: none;
  color: white;
  padding: 10px 20px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-recommended:hover {
  background: rgba(255,255,255,0.3);
}

/* 搜索栏 */
.search-section {
  margin-bottom: 20px;
}

.search-input {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
}

/* 课程列表 */
.course-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.course-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 10px;
  border-left: 4px solid #ddd;
  transition: all 0.3s ease;
}

.course-item:hover {
  transform: translateX(5px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.course-item.in-progress {
  border-left-color: #ffc107;
}

.course-item.completed {
  border-left-color: #28a745;
}

.course-item.not-started {
  border-left-color: #6c757d;
}

.course-item.new {
  border-left-color: #007bff;
  background: linear-gradient(135deg, #e3f2fd, #f3e5f5);
}

.course-icon {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 20px;
  flex-shrink: 0;
}

.course-icon.locked {
  background: #6c757d;
}

.course-icon.new {
  background: linear-gradient(135deg, #007bff, #6610f2);
}

.course-content {
  flex: 1;
}

.course-title {
  font-weight: 600;
  margin-bottom: 5px;
  color: #333;
  display: flex;
  align-items: center;
  gap: 10px;
}

.new-badge {
  background: #dc3545;
  color: white;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: bold;
}

.course-meta {
  color: #666;
  font-size: 14px;
  margin-bottom: 5px;
}

.course-progress-section {
  display: flex;
  align-items: center;
  gap: 10px;
}

.progress-bar {
  width: 100px;
  height: 6px;
  background: #e9ecef;
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: #ffc107;
  transition: width 0.3s ease;
}

.progress-fill.completed {
  background: #28a745;
}

.progress-text {
  font-size: 12px;
  color: #666;
  min-width: 60px;
}

.course-status {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 12px;
  font-weight: 500;
}

.status-progress {
  background: #fff3cd;
  color: #856404;
}

.status-completed {
  background: #d4edda;
  color: #155724;
}

.status-locked {
  background: #f8d7da;
  color: #721c24;
}

.status-new {
  background: #d1ecf1;
  color: #0c5460;
}

.course-score {
  font-size: 12px;
  color: #28a745;
  font-weight: 600;
}

.course-actions {
  display: flex;
  gap: 5px;
  flex-shrink: 0;
}

.course-actions button {
  padding: 6px 12px;
  border: none;
  border-radius: 6px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-continue {
  background: #ffc107;
  color: #333;
}

.btn-review {
  background: #6c757d;
  color: white;
}

.btn-certificate {
  background: #28a745;
  color: white;
}

.btn-start {
  background: #007bff;
  color: white;
}

.btn-prerequisites {
  background: #6c757d;
  color: white;
}

.btn-favorite {
  background: transparent;
  color: #dc3545;
  border: 1px solid #dc3545;
}

.btn-favorite.active {
  background: #dc3545;
  color: white;
}

.course-actions button:hover {
  transform: translateY(-1px);
}

/* 学习路径 */
.learning-paths {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.path-item {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 10px;
  border: 1px solid #e9ecef;
}

.path-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.path-header h3 {
  margin: 0;
  color: #333;
}

.path-progress {
  color: #666;
  font-size: 14px;
}

.path-courses {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
  flex-wrap: wrap;
}

.path-course {
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 14px;
  border: 2px solid #e9ecef;
  background: white;
}

.path-course.completed {
  background: #28a745;
  color: white;
  border-color: #28a745;
}

.path-course.current {
  background: #ffc107;
  color: #333;
  border-color: #ffc107;
}

.btn-continue-path {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-continue-path:hover {
  transform: translateY(-2px);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .course-item {
    flex-direction: column;
    text-align: center;
    gap: 10px;
  }
  
  .course-actions {
    justify-content: center;
  }
  
  .course-filters {
    flex-wrap: wrap;
  }
  
  .path-courses {
    justify-content: center;
  }
}
</style>