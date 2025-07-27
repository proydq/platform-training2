<template>
  <div class="courses-container">
    <!-- 主要内容区域 - 两列布局 -->
    <div class="main-content">
      <!-- 学习概览 -->
      <div class="overview-card">
        <h2>📊 学习概览</h2>
        <div class="course-overview">
          <div class="overview-item blue">
            <div class="overview-number">{{ overview.completed }}</div>
            <div class="overview-label">已完成</div>
          </div>
          <div class="overview-item orange">
            <div class="overview-number">{{ overview.inProgress }}</div>
            <div class="overview-label">进行中</div>
          </div>
          <div class="overview-item purple">
            <div class="overview-number">{{ overview.notStarted }}</div>
            <div class="overview-label">待开始</div>
          </div>
          <div class="overview-item green">
            <div class="overview-number">{{ formatHours(overview.totalTime) }}</div>
            <div class="overview-label">学习时长</div>
          </div>
        </div>

        <!-- 课程分类筛选 -->
        <!--        <div class="course-filters">
                  <button
                    v-for="filter in filterOptions"
                    :key="filter.key"
                    class="filter-btn"
                    :class="{ active: activeFilter === filter.key }"
                    @click="activeFilter = filter.key"
                  >
                    {{ filter.label }}
                  </button>
                </div>-->

        <!-- 推荐课程 -->
        <!--        <h3 class="recommend-title">🎯 为你推荐</h3>
                <div class="recommended-course">
                  <div class="rec-icon">{{ recommended.icon }}</div>
                  <div class="rec-info">
                    <div class="rec-title">{{ recommended.title }}</div>
                    <div class="rec-desc">{{ recommended.reason }} | 讲师：{{ recommended.instructor }}</div>
                  </div>
                  <button class="rec-btn" @click="startCourse(recommended.id)">开始学习</button>
                </div>-->
      </div>

      <!-- 我的课程 -->
      <div class="courses-card">
        <h2>📚 我的课程</h2>

        <!-- 搜索栏 -->
        <div class="search-container">
          <input
            type="text"
            v-model="searchKeyword"
            placeholder="搜索课程名称..."
            class="search-input"
          />
        </div>

        <div class="course-list">
          <div
            v-for="course in filteredCourses"
            :key="course.id"
            class="course-item"
            :class="course.status"
          >
            <div class="course-icon" :style="{ background: course.bg }">{{ course.icon }}</div>
            <div class="course-info">
              <div class="course-title">{{ course.title }}</div>
              <div class="course-meta">
                讲师：{{ course.instructor }} | 时长：{{ course.duration
                }}<span v-if="course.videos"> | 🎥 {{ course.videos }}个视频</span>
              </div>

              <!-- 进行中课程显示进度 -->
              <div v-if="course.status === 'in-progress'" class="course-progress">
                <div class="progress-bar">
                  <div class="progress-fill" :style="{ width: course.progress + '%' }"></div>
                </div>
                <span class="progress-text">{{ course.progress }}%</span>
              </div>

              <!-- 已完成课程显示完成信息 -->
              <div v-else-if="course.status === 'completed'" class="course-completed">
                <span class="completed-text">✅ 完成时间：{{ course.completedDate }}</span>
                <span class="grade-badge">{{ course.grade }}分</span>
              </div>

              <!-- 待开始课程显示锁定信息 -->
              <div v-else-if="course.status === 'not-started'" class="course-lock">
                ⚠️ {{ course.prerequisite }}
              </div>
            </div>

            <div class="course-actions">
              <!-- 进行中课程 -->
              <template v-if="course.status === 'in-progress'">
                <button class="btn btn-primary" @click="continueCourse(course.id)">继续学习</button>
                <button class="btn btn-secondary small" @click="toggleFavorite(course)">
                  ⭐ 收藏
                </button>
              </template>

              <!-- 已完成课程 -->
              <template v-else-if="course.status === 'completed'">
                <button class="btn btn-secondary" @click="reviewCourse(course.id)">复习</button>
                <button class="btn btn-secondary small" @click="downloadCertificate(course.id)">
                  📜 证书
                </button>
              </template>

              <!-- 待开始课程 -->
              <template v-else-if="course.status === 'not-started'">
                <button class="btn btn-disabled" disabled>暂未解锁</button>
                <button class="btn btn-secondary small" @click="viewPrerequisites(course.id)">
                  查看要求
                </button>
              </template>
            </div>
          </div>

          <!-- 空状态 -->
          <div v-if="filteredCourses.length === 0" class="empty-state">
            <div class="empty-icon">📚</div>
            <div class="empty-text">暂无符合条件的课程</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 学习路径 -->
    <div class="learning-path-card">
      <h2>🛤️ 学习路径</h2>
      <div class="learning-path">
        <div
          v-for="path in learningPaths"
          :key="path.id"
          class="path-item"
          :style="{ background: path.bg }"
        >
          <h3>{{ path.title }}</h3>
          <div class="path-steps">
            <div v-for="step in path.steps" :key="step.name" class="step-item">
              {{ step.icon }} {{ step.name }} ({{ step.status }})
            </div>
          </div>
          <div class="path-progress">
            <div class="progress-bar">
              <div class="progress-fill" :style="{ width: path.progress + '%' }"></div>
            </div>
            <span class="progress-text">{{ path.progress }}%</span>
          </div>
          <button class="path-btn" @click="continuePath(path.id)">继续路径</button>
        </div>
      </div>
    </div>

    <!-- 学习成就 -->
    <div class="achievements-card">
      <h2>🏆 学习成就</h2>
      <div class="achievements">
        <div
          v-for="achievement in achievements"
          :key="achievement.title"
          class="achievement-item"
          :style="achievement.style"
        >
          <div class="ach-icon">{{ achievement.icon }}</div>
          <h3 class="ach-title">{{ achievement.title }}</h3>
          <p class="ach-condition">{{ achievement.condition }}</p>
          <div class="ach-time">{{ achievement.time }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getStudyOverview } from '@/api/study'
import { getMyCourses } from '@/api/userStudy'

const router = useRouter()

// 响应式数据
const overview = ref({
  completed: 0,
  inProgress: 0,
  notStarted: 0,
  totalTime: 0
})
const searchKeyword = ref('')
const activeFilter = ref('all')

// 筛选选项
const filterOptions = [
  { key: 'all', label: '全部' },
  { key: 'in-progress', label: '进行中' },
  { key: 'completed', label: '已完成' },
  { key: 'not-started', label: '待开始' },
  { key: 'favorites', label: '⭐ 收藏' },
]

function formatHours(seconds) {
  return `${Math.floor(seconds / 3600)}h`
}

// 推荐课程
const recommended = {
  id: 'ai-design',
  icon: '🚀',
  title: 'AI产品设计实战',
  reason: '🤖 基于你的学习记录推荐',
  instructor: 'AI专家',
}

// 我的课程列表
const courseList = ref([])

// 辅助函数：生成随机图标和背景
function randomIcon() {
  const icons = ['📚', '🎯', '📊', '🚀', '🔍', '💡']
  return icons[Math.floor(Math.random() * icons.length)]
}

function randomBg() {
  const bgs = [
    'linear-gradient(135deg, #667eea, #764ba2)',
    'linear-gradient(135deg, #28a745, #20c997)',
    'linear-gradient(135deg, #6c757d, #495057)',
    'linear-gradient(135deg, #ff9a9e, #fad0c4)'
  ]
  return bgs[Math.floor(Math.random() * bgs.length)]
}

// 学习路径
const learningPaths = [
  {
    id: 'product-manager',
    title: '🎯 产品经理成长路径',
    steps: [
      { name: '产品基础知识', status: '已完成', icon: '✅' },
      { name: '用户研究方法', status: '进行中', icon: '🔄' },
      { name: '产品设计实战', status: '待开始', icon: '⏳' },
      { name: '数据驱动决策', status: '待开始', icon: '⏳' },
    ],
    progress: 40,
    bg: 'linear-gradient(135deg, #667eea, #764ba2)',
  },
  {
    id: 'data-analyst',
    title: '📊 数据分析师路径',
    steps: [
      { name: '数据分析基础', status: '已完成', icon: '✅' },
      { name: 'Excel高级应用', status: '已完成', icon: '✅' },
      { name: 'Python数据分析', status: '进行中', icon: '🔄' },
      { name: '机器学习入门', status: '待开始', icon: '⏳' },
    ],
    progress: 60,
    bg: 'linear-gradient(135deg, #28a745, #20c997)',
  },
]

// 学习成就
const achievements = [
  {
    icon: '🥇',
    title: '学习达人',
    condition: '连续学习30天',
    time: '获得时间：2025-01-15',
    style: 'background:#fff3cd;border:2px solid #ffc107;',
  },
  {
    icon: '📚',
    title: '知识大师',
    condition: '完成20门课程',
    time: '获得时间：2025-01-12',
    style: 'background:#d4edda;border:2px solid #28a745;',
  },
  {
    icon: '🎯',
    title: '考试王者',
    condition: '所有考试95分以上',
    time: '进度：8/10',
    style: 'background:#f8f9fa;border:2px dashed #6c757d;opacity:0.7;',
  },
]

// 计算属性：根据搜索关键词筛选课程
const filteredCourses = computed(() =>
  courseList.value.filter((course) =>
    course.title.toLowerCase().includes(searchKeyword.value.toLowerCase())
  )
)

// 方法
const startCourse = (id) => {
  // 跳转到学习页面
  router.push(`/learning/${id}`)
}

const continueCourse = (id) => {
  // 跳转到学习页面
  router.push(`/learning/${id}`)
}

const reviewCourse = (id) => {
  ElMessage.success(`正在加载复习模式: ${id}`)
}

const downloadCertificate = (id) => {
  ElMessage.success(`正在下载证书: ${id}`)
}

const viewPrerequisites = (id) => {
  ElMessage.warning(`查看课程前置要求: ${id}`)
}

const toggleFavorite = (course) => {
  course.favorite = !course.favorite
  ElMessage.success(course.favorite ? '已添加到收藏' : '已取消收藏')
}

const continuePath = (id) => {
  ElMessage.success(`继续学习路径: ${id}`)
}

onMounted(async () => {
  try {
    const res = await getStudyOverview()
    overview.value = res?.data || res
  } catch (e) {
    console.error('获取学习概览失败:', e)
  }

  try {
    const res = await getMyCourses()
    if (res.success) {
      courseList.value = (res.data || []).map((c) => ({
        id: c.id || c.courseId,
        title: c.title,
        instructor: c.instructor || '',
        duration: c.duration,
        videos: c.videos ?? 0,
        status: c.status,
        progress: c.progress ?? 0,
        completedDate: c.completedDate || '',
        grade: c.grade || '',
        prerequisite: c.prerequisite || '',
        icon: c.icon || randomIcon(),
        bg: c.bg || randomBg(),
        favorite: c.isFavorited ?? false,
      }))
    }
  } catch (e) {
    console.error('获取课程数据失败:', e)
  }
})
</script>

<style scoped>
.courses-container {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

/* 主要内容区域 - 两列布局 */
.main-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
  margin-bottom: 30px;
}

/* 学习概览卡片 */
.overview-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 30px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.overview-card h2 {
  color: #667eea;
  font-size: 20px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}

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

.overview-item.blue {
  background: #e3f2fd;
}

.overview-item.orange {
  background: #fff3e0;
}

.overview-item.purple {
  background: #f3e5f5;
}

.overview-item.green {
  background: #e8f5e8;
}

.overview-number {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 5px;
}

.overview-item.blue .overview-number {
  color: #1976d2;
}

.overview-item.orange .overview-number {
  color: #f57c00;
}

.overview-item.purple .overview-number {
  color: #7b1fa2;
}

.overview-item.green .overview-number {
  color: #2e7d32;
}

.overview-label {
  color: #666;
  font-size: 14px;
}

/* 筛选按钮 */
.course-filters {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.filter-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 20px;
  background: #f8f9fa;
  color: #666;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 14px;
}

.filter-btn:hover {
  background: #e9ecef;
}

.filter-btn.active {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
}

/* 推荐课程 */
.recommend-title {
  margin-bottom: 15px;
  font-size: 16px;
}

.recommended-course {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  border-radius: 10px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  margin-bottom: 20px;
}

.rec-icon {
  width: 60px;
  height: 60px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.rec-info {
  flex: 1;
}

.rec-title {
  font-weight: 600;
  margin-bottom: 5px;
}

.rec-desc {
  font-size: 14px;
  opacity: 0.9;
}

.rec-btn {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  padding: 8px 16px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.rec-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

/* 课程卡片 */
.courses-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 30px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.courses-card h2 {
  color: #667eea;
  font-size: 20px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}

/* 搜索框 */
.search-container {
  margin-bottom: 20px;
}

.search-input {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #ddd;
  border-radius: 10px;
  font-size: 14px;
  transition: all 0.3s ease;
}

.search-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
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
  border-radius: 10px;
  background: #f8f9fa;
  border-left: 4px solid #ffc107;
  transition: all 0.3s ease;
}

.course-item:hover {
  transform: translateX(5px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.course-item.completed {
  border-color: #28a745;
}

.course-item.not-started {
  border-color: #6c757d;
}

.course-icon {
  width: 60px;
  height: 60px;
  border-radius: 10px;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.course-info {
  flex: 1;
}

.course-title {
  font-weight: 600;
  margin-bottom: 5px;
  font-size: 16px;
}

.course-meta {
  color: #666;
  font-size: 14px;
  margin-bottom: 5px;
}

/* 进度条 */
.course-progress {
  display: flex;
  align-items: center;
  gap: 10px;
}

.progress-bar {
  width: 120px;
  height: 8px;
  background: #e0e0e0;
  border-radius: 4px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #667eea, #764ba2);
  transition: width 0.3s ease;
}

.progress-text {
  font-size: 12px;
  color: #666;
}

/* 完成状态 */
.course-completed {
  display: flex;
  align-items: center;
  gap: 10px;
}

.completed-text {
  color: #28a745;
  font-size: 14px;
}

.grade-badge {
  background: #28a745;
  color: white;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
}

/* 锁定状态 */
.course-lock {
  color: #dc3545;
  font-size: 14px;
}

/* 操作按钮 */
.course-actions {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.btn-secondary {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
}

.btn-secondary:hover {
  background: rgba(102, 126, 234, 0.2);
}

.btn-disabled {
  background: #f8f9fa;
  color: #666;
  cursor: not-allowed;
}

.btn.small {
  font-size: 12px;
  padding: 6px 12px;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #666;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-text {
  font-size: 16px;
}

/* 学习路径 */
.learning-path-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 30px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  margin-bottom: 30px;
}

.learning-path-card h2 {
  color: #667eea;
  font-size: 20px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.learning-path {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.path-item {
  color: white;
  padding: 20px;
  border-radius: 15px;
}

.path-item h3 {
  margin-bottom: 15px;
  font-size: 16px;
}

.path-steps {
  font-size: 14px;
  margin-bottom: 15px;
}

.step-item {
  margin-bottom: 5px;
}

.path-progress {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;
}

.path-btn {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  padding: 8px 16px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.path-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

/* 学习成就 */
.achievements-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 30px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.achievements-card h2 {
  color: #667eea;
  font-size: 20px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.achievements {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.achievement-item {
  padding: 20px;
  border-radius: 15px;
  text-align: center;
}

.ach-icon {
  font-size: 48px;
  margin-bottom: 10px;
}

.ach-title {
  margin-bottom: 10px;
  font-size: 16px;
}

.ach-condition {
  color: #666;
  font-size: 14px;
  margin-bottom: 10px;
}

.ach-time {
  font-size: 12px;
  color: inherit;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .courses-container {
    padding: 15px;
  }

  .main-content {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .course-item {
    flex-direction: column;
    align-items: stretch;
    text-align: center;
  }

  .course-actions {
    flex-direction: row;
    justify-content: center;
    flex-wrap: wrap;
  }

  .course-overview {
    grid-template-columns: repeat(auto-fit, minmax(100px, 1fr));
  }

  .learning-path {
    grid-template-columns: 1fr;
  }

  .achievements {
    grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  }
}

/* 动画效果 */
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

.overview-card,
.courses-card,
.learning-path-card,
.achievements-card {
  animation: fadeInUp 0.6s ease forwards;
}
</style>
