<template>
  <div class="courses-wrapper container">
    <el-card class="stats-card">
      <h2>📊 学习概览</h2>
      <div class="stats-grid">
        <div v-for="stat in stats" :key="stat.label" class="stat-card" :class="stat.type">
          <div class="stat-number">{{ stat.value }}</div>
          <div class="stat-label">{{ stat.label }}</div>
        </div>
      </div>
      <div class="course-filters">
        <el-button
          v-for="item in filterOptions"
          :key="item.key"
          size="small"
          class="filter-btn"
          :type="activeFilter === item.key ? 'primary' : 'default'"
          round
          @click="activeFilter = item.key"
        >{{ item.label }}</el-button>
      </div>
      <h3 class="recommend-title">🎯 为你推荐</h3>
      <div class="recommended-course">
        <div class="rec-icon">{{ recommended.icon }}</div>
        <div class="rec-info">
          <div class="rec-title">{{ recommended.title }}</div>
          <div class="rec-desc">{{ recommended.reason }} | 讲师：{{ recommended.instructor }}</div>
        </div>
        <el-button class="rec-btn" size="small" @click="startCourse(recommended.id)">开始学习</el-button>
      </div>
    </el-card>

    <el-card class="courses-card">
      <h2>📚 我的课程</h2>
      <el-input
        v-model="keyword"
        placeholder="搜索课程名称..."
        class="search-input"
        clearable
      />
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
            <div class="course-meta">讲师：{{ course.instructor }} | 时长：{{ course.duration }}<template v-if="course.videos"> | 🎥 {{ course.videos }}个视频</template></div>
            <template v-if="course.status === 'in-progress'">
              <div class="course-progress">
                <div class="progress-bar"><div class="progress-fill" :style="{ width: course.progress + '%' }"></div></div>
                <span class="progress-text">{{ course.progress }}%</span>
              </div>
            </template>
            <template v-else-if="course.status === 'completed'">
              <div class="course-completed">✅ 完成时间：{{ course.date }} <el-tag size="small" type="success">{{ course.grade }}</el-tag></div>
            </template>
            <template v-else>
              <div class="course-lock">⚠️ {{ course.prerequisite }}</div>
            </template>
          </div>
          <div class="course-actions">
            <el-button v-if="course.status === 'in-progress'" type="primary" size="small" @click="continueCourse(course.id)">继续学习</el-button>
            <el-button v-else-if="course.status === 'completed'" size="small" @click="reviewCourse(course.id)">复习</el-button>
            <el-button v-else disabled size="small">暂未解锁</el-button>
            <el-button v-if="course.status === 'completed'" size="small" @click="downloadCertificate(course.id)">📜 证书</el-button>
            <el-button v-if="course.status === 'not-started'" size="small" @click="viewPrerequisites(course.id)">查看要求</el-button>
            <el-button size="small" @click="toggleFavorite(course)">
              {{ course.favorite ? '取消收藏' : '⭐ 收藏' }}
            </el-button>
          </div>
        </div>
        <div v-if="filteredCourses.length === 0" class="empty">暂无匹配课程</div>
      </div>
    </el-card>

    <el-card class="path-card">
      <h2>🛤️ 学习路径</h2>
      <div class="learning-path">
        <div v-for="path in paths" :key="path.id" class="path-item" :style="{ background: path.bg }">
          <h3>{{ path.title }}</h3>
          <div class="path-steps">
            <div v-for="step in path.steps" :key="step.name">{{ step.icon }} {{ step.name }} ({{ step.status }})</div>
          </div>
          <div class="path-progress">
            <div class="progress-bar"><div class="progress-fill" :style="{ width: path.progress + '%' }"></div></div>
            <span class="progress-text">{{ path.progress }}%</span>
          </div>
          <el-button size="small" class="path-btn" @click="continuePath(path.id)">继续路径</el-button>
        </div>
      </div>
    </el-card>

    <el-card class="achieve-card">
      <h2>🏆 学习成就</h2>
      <div class="achievements">
        <div v-for="ach in achievements" :key="ach.title" class="achievement-item" :style="ach.style">
          <div class="ach-icon">{{ ach.icon }}</div>
          <h3>{{ ach.title }}</h3>
          <p>{{ ach.condition }}</p>
          <div class="ach-time">{{ ach.time }}</div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'

const stats = [
  { label: '已完成', value: 24, type: 'done' },
  { label: '进行中', value: 8, type: 'doing' },
  { label: '待开始', value: 4, type: 'todo' },
  { label: '学习时长', value: '156h', type: 'time' }
]

const filterOptions = [
  { key: 'all', label: '全部' },
  { key: 'in-progress', label: '进行中' },
  { key: 'completed', label: '已完成' },
  { key: 'not-started', label: '待开始' },
  { key: 'favorites', label: '⭐ 收藏' }
]

const courses = ref([
  {
    id: 'product-basic',
    title: '产品基础知识培训',
    instructor: '李经理',
    duration: '2小时',
    videos: 12,
    progress: 75,
    status: 'in-progress',
    icon: '📱',
    bg: 'linear-gradient(135deg, #667eea, #764ba2)',
    favorite: false
  },
  {
    id: 'data-analysis',
    title: '数据分析基础',
    instructor: '王专家',
    duration: '3小时',
    grade: '92分',
    date: '2025-01-10',
    status: 'completed',
    icon: '✅',
    bg: 'linear-gradient(135deg, #28a745, #20c997)',
    favorite: false
  },
  {
    id: 'advanced-research',
    title: '高级用户研究方法',
    instructor: '刘教授',
    duration: '4小时',
    prerequisite: '需要先完成"用户体验基础"课程',
    status: 'not-started',
    icon: '📊',
    bg: 'linear-gradient(135deg, #6c757d, #495057)',
    favorite: false
  }
])

const recommended = {
  id: 'ai-design',
  title: 'AI产品设计实战',
  instructor: 'AI专家',
  reason: '🤖 基于你的学习记录推荐',
  icon: '🚀'
}

const paths = [
  {
    id: 'product-manager',
    title: '🎯 产品经理成长路径',
    steps: [
      { name: '产品基础知识', status: '已完成', icon: '✅' },
      { name: '用户研究方法', status: '进行中', icon: '🔄' },
      { name: '产品设计实战', status: '待开始', icon: '⏳' },
      { name: '数据驱动决策', status: '待开始', icon: '⏳' }
    ],
    progress: 40,
    bg: 'linear-gradient(135deg, #667eea, #764ba2)'
  },
  {
    id: 'data-analyst',
    title: '📊 数据分析师路径',
    steps: [
      { name: '数据分析基础', status: '已完成', icon: '✅' },
      { name: 'Excel高级应用', status: '已完成', icon: '✅' },
      { name: 'Python数据分析', status: '进行中', icon: '🔄' },
      { name: '机器学习入门', status: '待开始', icon: '⏳' }
    ],
    progress: 60,
    bg: 'linear-gradient(135deg, #28a745, #20c997)'
  }
]

const achievements = [
  {
    icon: '🥇',
    title: '学习达人',
    condition: '连续学习30天',
    time: '获得时间：2025-01-15',
    style: 'background:#fff3cd;border:2px solid #ffc107;'
  },
  {
    icon: '📚',
    title: '知识大师',
    condition: '完成20门课程',
    time: '获得时间：2025-01-12',
    style: 'background:#d4edda;border:2px solid #28a745;'
  },
  {
    icon: '🎯',
    title: '考试王者',
    condition: '所有考试95分以上',
    time: '进度：8/10',
    style: 'background:#f8f9fa;border:2px dashed #6c757d;opacity:0.7;'
  }
]

const keyword = ref('')
const activeFilter = ref('all')

const filteredCourses = computed(() => {
  return courses.value.filter(c => {
    const matchKeyword = c.title.toLowerCase().includes(keyword.value.toLowerCase())
    let matchFilter = true
    if (activeFilter.value === 'favorites') {
      matchFilter = c.favorite
    } else if (activeFilter.value !== 'all') {
      matchFilter = c.status === activeFilter.value
    }
    return matchKeyword && matchFilter
  })
})

const startCourse = id => {
  ElMessage.success(`正在启动课程: ${id}`)
}
const continueCourse = id => {
  ElMessage.success(`继续学习课程: ${id}`)
}
const toggleFavorite = course => {
  course.favorite = !course.favorite
  ElMessage.success(course.favorite ? '已收藏' : '已取消收藏')
}
const reviewCourse = id => {
  ElMessage.success(`正在加载复习模式: ${id}`)
}
const downloadCertificate = id => {
  ElMessage.success(`正在下载证书: ${id}`)
}
const viewPrerequisites = id => {
  ElMessage.warning(`查看课程前置要求: ${id}`)
}
const continuePath = id => {
  ElMessage.success(`继续学习路径: ${id}`)
}
</script>

<style scoped>
.courses-wrapper {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 20px;
}
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 15px;
  margin-bottom: 20px;
}
.stat-card {
  text-align: center;
  padding: 15px;
  border-radius: 10px;
  background: #f8f9fa;
}
.stat-card.done { background: #e3f2fd; }
.stat-card.doing { background: #fff3e0; }
.stat-card.todo { background: #f3e5f5; }
.stat-card.time { background: #e8f5e8; }
.stat-number { font-size: 24px; font-weight: bold; }
.stat-label { color: #666; font-size: 14px; }
.course-filters {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}
.recommend-title { margin-bottom: 10px; }
.recommended-course {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  border-radius: 10px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
}
.rec-icon {
  width: 60px;
  height: 60px;
  background: rgba(255,255,255,0.2);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}
.rec-info { flex: 1; }
.rec-title { font-weight: 600; margin-bottom: 5px; }
.rec-desc { font-size: 14px; opacity: 0.9; }
.rec-btn { background: rgba(255,255,255,0.2); border:none; color:white; }
.courses-card { margin-top: 20px; }
.search-input { margin-bottom: 20px; }
.course-list { display: flex; flex-direction: column; gap: 10px; }
.course-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  border-radius: 10px;
  background: #f8f9fa;
  border-left: 4px solid #ffc107;
}
.course-item.completed { border-color: #28a745; }
.course-item.not-started { border-color: #6c757d; }
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
.course-info { flex: 1; }
.course-title { font-weight: 600; margin-bottom: 5px; }
.course-meta { color: #666; font-size: 14px; margin-bottom: 5px; }
.course-progress, .path-progress { display: flex; align-items: center; gap: 10px; }
.progress-bar { width: 100px; height: 6px; background: #e9ecef; border-radius: 3px; overflow: hidden; }
.progress-fill { height: 100%; background: #28a745; }
.progress-text { font-size: 12px; }
.course-completed { display: flex; align-items: center; gap: 10px; color: #28a745; font-size: 14px; }
.course-lock { color: #dc3545; font-size: 14px; }
.course-actions { display: flex; flex-direction: column; gap: 5px; }
.empty { text-align: center; padding: 20px; color: #666; }
.learning-path { display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 20px; }
.path-item {
  color: white;
  padding: 20px;
  border-radius: 15px;
}
.path-steps { font-size: 14px; margin-bottom: 15px; }
.path-btn { background: rgba(255,255,255,0.2); border:none; color:white; }
.achievements { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 20px; }
.achievement-item {
  padding: 20px;
  border-radius: 15px;
  text-align: center;
}
.ach-icon { font-size: 48px; margin-bottom: 10px; }
.ach-time { margin-top: 10px; font-size: 12px; color: inherit; }
@media (max-width: 768px) {
  .courses-wrapper { grid-template-columns: 1fr; }
  .course-actions { flex-direction: row; flex-wrap: wrap; }
}
</style>
