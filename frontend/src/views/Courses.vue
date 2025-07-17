<template>
  <div class="courses-container">
    <!-- 课程概览 -->
    <el-card class="overview-card">
      <template #header>
        <div class="card-header">
          <span>📊 学习概览</span>
        </div>
      </template>
      
      <div class="course-overview">
        <div class="overview-item">
          <div class="overview-number">{{ overview.completed }}</div>
          <div class="overview-label">已完成</div>
        </div>
        <div class="overview-item">
          <div class="overview-number">{{ overview.inProgress }}</div>
          <div class="overview-label">进行中</div>
        </div>
        <div class="overview-item">
          <div class="overview-number">{{ overview.pending }}</div>
          <div class="overview-label">待开始</div>
        </div>
        <div class="overview-item">
          <div class="overview-number">{{ overview.totalHours }}h</div>
          <div class="overview-label">学习时长</div>
        </div>
      </div>
    </el-card>

    <!-- 筛选器 -->
    <el-card class="filter-card">
      <div class="course-filters">
        <el-button-group>
          <el-button 
            v-for="filter in filters"
            :key="filter.key"
            :type="activeFilter === filter.key ? 'primary' : ''"
            @click="setActiveFilter(filter.key)"
          >
            {{ filter.label }}
          </el-button>
        </el-button-group>
        
        <div class="search-box">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索课程..."
            prefix-icon="Search"
            clearable
            @input="handleSearch"
          />
        </div>
      </div>
    </el-card>

    <!-- 课程列表 -->
    <div class="courses-grid">
      <div
        v-for="course in filteredCourses"
        :key="course.id"
        class="course-card"
        @click="goToCourse(course.id)"
      >
        <div class="course-image">
          <div class="course-icon">{{ course.icon }}</div>
          <div class="course-status" :class="course.status">
            {{ getStatusText(course.status) }}
          </div>
        </div>
        
        <div class="course-content">
          <h3 class="course-title">{{ course.title }}</h3>
          <p class="course-description">{{ course.description }}</p>
          
          <div class="course-meta">
            <div class="meta-item">
              <el-icon><Clock /></el-icon>
              <span>{{ course.duration }}</span>
            </div>
            <div class="meta-item">
              <el-icon><User /></el-icon>
              <span>{{ course.instructor }}</span>
            </div>
            <div class="meta-item">
              <el-icon><Star /></el-icon>
              <span>{{ course.rating }}</span>
            </div>
          </div>
          
          <div class="course-progress">
            <div class="progress-info">
              <span>学习进度</span>
              <span class="progress-text">{{ course.progress }}%</span>
            </div>
            <el-progress 
              :percentage="course.progress" 
              :color="getProgressColor(course.progress)"
              :stroke-width="6"
            />
          </div>
          
          <div class="course-actions">
            <el-button 
              type="primary" 
              size="default"
              :disabled="course.status === 'locked'"
              @click.stop="startCourse(course)"
            >
              {{ getActionText(course.status, course.progress) }}
            </el-button>
            <el-button 
              size="default"
              @click.stop="viewCourseDetails(course.id)"
            >
              详情
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[12, 24, 48]"
        :total="totalCourses"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Clock, User, Star, Search } from '@element-plus/icons-vue'

const router = useRouter()

// 概览数据
const overview = ref({
  completed: 24,
  inProgress: 8,
  pending: 4,
  totalHours: 156
})

// 筛选器
const filters = [
  { key: 'all', label: '全部' },
  { key: 'in-progress', label: '进行中' },
  { key: 'completed', label: '已完成' },
  { key: 'pending', label: '待开始' }
]

const activeFilter = ref('all')
const searchKeyword = ref('')

// 分页
const currentPage = ref(1)
const pageSize = ref(12)
const totalCourses = ref(36)

// 课程数据
const courses = ref([
  {
    id: 1,
    title: '产品基础培训',
    description: '了解公司产品的核心功能和特性，掌握基本的产品知识',
    icon: '📱',
    status: 'in-progress',
    progress: 85,
    duration: '4小时',
    instructor: '张经理',
    rating: 4.8,
    category: 'product'
  },
  {
    id: 2,
    title: '销售技巧提升',
    description: '学习有效的销售沟通技巧，提升客户转化率',
    icon: '💼',
    status: 'completed',
    progress: 100,
    duration: '6小时',
    instructor: '李总监',
    rating: 4.9,
    category: 'sales'
  },
  {
    id: 3,
    title: '客户服务标准',
    description: '掌握专业的客户服务标准和处理流程',
    icon: '🎧',
    status: 'in-progress',
    progress: 45,
    duration: '3小时',
    instructor: '王主管',
    rating: 4.7,
    category: 'service'
  },
  {
    id: 4,
    title: '团队协作与沟通',
    description: '提升团队协作能力，改善内部沟通效率',
    icon: '🤝',
    status: 'pending',
    progress: 0,
    duration: '5小时',
    instructor: '刘老师',
    rating: 4.6,
    category: 'teamwork'
  },
  {
    id: 5,
    title: '数据分析基础',
    description: '学习基本的数据分析方法和工具使用',
    icon: '📊',
    status: 'locked',
    progress: 0,
    duration: '8小时',
    instructor: '陈分析师',
    rating: 4.8,
    category: 'analysis'
  },
  {
    id: 6,
    title: '项目管理实务',
    description: '掌握项目管理的基本理念和实操技能',
    icon: '📋',
    status: 'completed',
    progress: 100,
    duration: '7小时',
    instructor: '赵PM',
    rating: 4.9,
    category: 'management'
  }
])

// 计算属性 - 筛选后的课程
const filteredCourses = computed(() => {
  let result = courses.value

  // 状态筛选
  if (activeFilter.value !== 'all') {
    result = result.filter(course => course.status === activeFilter.value)
  }

  // 搜索筛选
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(course => 
      course.title.toLowerCase().includes(keyword) ||
      course.description.toLowerCase().includes(keyword) ||
      course.instructor.toLowerCase().includes(keyword)
    )
  }

  return result
})

// 方法
const setActiveFilter = (filterKey) => {
  activeFilter.value = filterKey
  currentPage.value = 1
}

const handleSearch = () => {
  currentPage.value = 1
}

const getStatusText = (status) => {
  const statusMap = {
    'completed': '已完成',
    'in-progress': '进行中',
    'pending': '待开始',
    'locked': '未解锁'
  }
  return statusMap[status] || status
}

const getProgressColor = (progress) => {
  if (progress === 100) return '#67c23a'
  if (progress >= 50) return '#e6a23c'
  return '#409eff'
}

const getActionText = (status, progress) => {
  if (status === 'locked') return '未解锁'
  if (status === 'completed') return '重新学习'
  if (status === 'in-progress') return '继续学习'
  return '开始学习'
}

const startCourse = (course) => {
  if (course.status === 'locked') {
    ElMessage.warning('该课程尚未解锁')
    return
  }
  
  ElMessage.success(`开始学习：${course.title}`)
  // router.push(`/courses/${course.id}/learn`)
}

const viewCourseDetails = (courseId) => {
  ElMessage.info(`查看课程详情：${courseId}`)
  // router.push(`/courses/${courseId}`)
}

const goToCourse = (courseId) => {
  viewCourseDetails(courseId)
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
}

const handleCurrentChange = (page) => {
  currentPage.value = page
}

onMounted(() => {
  // 初始化课程数据
})
</script>

<style scoped>
.courses-container {
  max-width: 1200px;
  margin: 0 auto;
}

.overview-card,
.filter-card {
  margin-bottom: 20px;
  border-radius: 15px;
  border: none;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
}

.card-header {
  font-weight: 600;
  font-size: 16px;
}

.course-overview {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 20px;
}

.overview-item {
  text-align: center;
  padding: 15px;
  background: rgba(102, 126, 234, 0.05);
  border-radius: 10px;
}

.overview-number {
  font-size: 24px;
  font-weight: bold;
  color: #667eea;
  margin-bottom: 5px;
}

.overview-label {
  color: #666;
  font-size: 14px;
}

.course-filters {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
}

.search-box {
  width: 300px;
}

.courses-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.course-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 15px;
  padding: 20px;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  cursor: pointer;
  animation: fadeInUp 0.6s ease forwards;
}

.course-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.15);
}

.course-image {
  position: relative;
  text-align: center;
  margin-bottom: 20px;
}

.course-icon {
  font-size: 48px;
  margin-bottom: 10px;
}

.course-status {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.course-status.completed {
  background: #f0f9ff;
  color: #0ea5e9;
}

.course-status.in-progress {
  background: #fef3c7;
  color: #d97706;
}

.course-status.pending {
  background: #f3e8ff;
  color: #9333ea;
}

.course-status.locked {
  background: #f1f5f9;
  color: #64748b;
}

.course-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0 0 10px 0;
}

.course-description {
  color: #666;
  font-size: 14px;
  line-height: 1.5;
  margin: 0 0 15px 0;
}

.course-meta {
  display: flex;
  gap: 15px;
  margin-bottom: 15px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: #666;
}

.course-progress {
  margin-bottom: 20px;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 12px;
  color: #666;
}

.progress-text {
  font-weight: 500;
  color: #333;
}

.course-actions {
  display: flex;
  gap: 10px;
}

.course-actions .el-button {
  flex: 1;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .course-filters {
    flex-direction: column;
    gap: 15px;
  }
  
  .search-box {
    width: 100%;
  }
  
  .courses-grid {
    grid-template-columns: 1fr;
    gap: 15px;
  }
  
  .course-meta {
    flex-direction: column;
    gap: 8px;
  }
  
  .course-actions {
    flex-direction: column;
  }
}

@media (max-width: 480px) {
  .course-overview {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .overview-number {
    font-size: 20px;
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
</style>