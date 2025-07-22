<template>
  <div class="courses-container">
    <!-- 学习概览 -->
    <div class="overview-card card">
      <h2>📊 学习概览</h2>
      <div class="course-overview">
        <div class="overview-item completed">
          <div class="overview-number">{{ courseStats.published || 0 }}</div>
          <div class="overview-label">已发布</div>
        </div>
        <div class="overview-item in-progress">
          <div class="overview-number">{{ courseStats.draft || 0 }}</div>
          <div class="overview-label">草稿</div>
        </div>
        <div class="overview-item not-started">
          <div class="overview-number">{{ courseStats.unpublished || 0 }}</div>
          <div class="overview-label">已下架</div>
        </div>
        <div class="overview-item study-time">
          <div class="overview-number">{{ courseStats.total || 0 }}</div>
          <div class="overview-label">总课程</div>
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
      <div v-if="recommendedCourse" class="recommended-course">
        <div class="course-icon recommended">🚀</div>
        <div class="course-info">
          <div class="course-title">{{ recommendedCourse.title }}</div>
          <div class="course-meta">🤖 推荐课程 | 讲师：{{ recommendedCourse.instructorName || '系统推荐' }}</div>
        </div>
        <button class="btn-recommended" @click="startCourse(recommendedCourse.id)">开始学习</button>
      </div>
    </div>

    <!-- 课程列表 -->
    <div class="courses-card card">
      <h2>📚 课程列表</h2>

      <!-- 搜索栏 -->
      <div class="search-section">
        <input
          v-model="searchFilters.keyword"
          type="text"
          placeholder="搜索课程名称..."
          class="search-input"
          @input="handleSearch"
        />
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-state">
        <div class="loading-spinner">⏳</div>
        <div class="loading-text">正在加载课程数据...</div>
      </div>

      <!-- 课程列表内容 -->
      <div v-else class="course-list">
        <!-- 真实课程数据循环 -->
        <div
          v-for="course in filteredCourses"
          :key="course.id"
          class="course-item"
          :class="getCourseStatusClass(course.status)"
          :data-category="getCourseCategory(course.status)"
        >
          <!-- 🔧 优化：课程封面图展示 -->
          <div class="course-cover">
            <img
              v-if="course.coverImageUrl"
              :src="course.coverImageUrl"
              :alt="course.title"
              class="cover-image"
              @error="handleImageError"
            />
            <div v-else class="default-cover">
              {{ getCourseIcon(course.category) }}
            </div>
            <!-- 状态徽章 -->
            <div class="status-badge" :class="getStatusBadgeClass(course.status)">
              {{ getCourseStatusText(course.status) }}
            </div>
          </div>

          <div class="course-content">
            <div class="course-title">{{ course.title }}</div>
            <div class="course-description">{{ course.description }}</div>
            <div class="course-meta">
              <span class="meta-item">
                <i class="icon">👨‍🏫</i>
                {{ course.instructorName || course.instructorId }}
              </span>
              <span class="meta-item">
                <i class="icon">⏱️</i>
                {{ formatDuration(course.estimatedDuration) }}
              </span>
              <span class="meta-item">
                <i class="icon">📂</i>
                {{ course.category }}
              </span>
            </div>

            <div class="course-progress-section">
              <div class="progress-bar">
                <div
                  class="progress-fill"
                  :class="getProgressClass(course.status)"
                  :style="{ width: getProgressWidth(course.status) }"
                ></div>
              </div>
              <span class="progress-text">{{ getStatusText(course.status) }}</span>
            </div>
          </div>

          <div class="course-actions">
            <button
              v-if="course.status === 1"
              class="btn-continue"
              @click="startCourse(course.id)"
            >
              开始学习
            </button>
            <button
              v-else-if="course.status === 0"
              class="btn-draft"
              @click="viewCourse(course.id)"
            >
              查看详情
            </button>
            <button
              v-else
              class="btn-unavailable"
              disabled
            >
              暂不可用
            </button>
            <button class="btn-favorite" @click="toggleFavorite(course.id)">💖</button>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-if="filteredCourses.length === 0" class="empty-state">
          <div class="empty-icon">📚</div>
          <div class="empty-text">{{ getEmptyText() }}</div>
        </div>
      </div>

      <!-- 分页器 -->
      <div v-if="filteredCourses.length > 0 && pagination.total > pagination.size" class="pagination-wrapper">
        <div class="pagination-info">
          共 {{ pagination.total }} 门课程，当前第 {{ pagination.current }}/{{ Math.ceil(pagination.total / pagination.size) }} 页
        </div>
        <div class="pagination-controls">
          <button
            class="pagination-btn"
            :disabled="pagination.current === 1"
            @click="handlePageChange(pagination.current - 1)"
          >
            上一页
          </button>
          <span class="pagination-current">{{ pagination.current }}</span>
          <button
            class="pagination-btn"
            :disabled="pagination.current >= Math.ceil(pagination.total / pagination.size)"
            @click="handlePageChange(pagination.current + 1)"
          >
            下一页
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { useCourse } from '@/composables/useCourse'
import { getCourseStatusText, getDifficultyLevelText } from '@/api/course'

// 使用useCourse获取真实数据
const {
  loading,
  courses,
  pagination,
  filters: courseFilters,
  loadCourses,
  getCourseStats,
  formatDuration
} = useCourse()

// 响应式数据
const activeFilter = ref('all')
const recommendedCourse = ref(null)
const courseStats = ref({
  total: 0,
  published: 0,
  draft: 0,
  unpublished: 0
})

// 搜索筛选条件
const searchFilters = reactive({
  keyword: '',
  category: ''
})

// 筛选器配置
const filters = ref([
  { key: 'all', label: '全部' },
  { key: 'published', label: '已发布' },
  { key: 'draft', label: '草稿' },
  { key: 'unpublished', label: '已下架' }
])

// 计算属性 - 根据筛选条件过滤课程
const filteredCourses = computed(() => {
  let result = courses.value || []

  // 按状态筛选
  if (activeFilter.value !== 'all') {
    const statusMap = {
      'published': 1,
      'draft': 0,
      'unpublished': 2
    }
    const targetStatus = statusMap[activeFilter.value]
    if (targetStatus !== undefined) {
      result = result.filter(course => course.status === targetStatus)
    }
  }

  // 按关键词搜索
  if (searchFilters.keyword.trim()) {
    const keyword = searchFilters.keyword.trim().toLowerCase()
    result = result.filter(course =>
      course.title?.toLowerCase().includes(keyword) ||
      course.description?.toLowerCase().includes(keyword) ||
      course.category?.toLowerCase().includes(keyword)
    )
  }

  return result
})

// 方法定义
const setActiveFilter = (filterKey) => {
  activeFilter.value = filterKey
  console.log('切换筛选器:', filterKey)
}

const handleSearch = () => {
  console.log('搜索课程:', searchFilters.keyword)
}

const handlePageChange = async (page) => {
  if (page >= 1 && page <= Math.ceil(pagination.total / pagination.size)) {
    pagination.current = page
    await loadCourses()
  }
}

// 🔧 图片处理方法
const handleImageError = (event) => {
  console.log('图片加载失败，使用默认图标')
  event.target.style.display = 'none'
  event.target.nextElementSibling.style.display = 'flex'
}

// 工具方法
const getCourseIcon = (category) => {
  const iconMap = {
    '技术培训': '💻',
    '产品培训': '📱',
    '安全培训': '🔒',
    '管理培训': '👔',
    '营销培训': '📈',
    '设计培训': '🎨'
  }
  return iconMap[category] || '📚'
}

const getCourseStatusClass = (status) => {
  const classMap = {
    0: 'draft',
    1: 'published',
    2: 'unpublished'
  }
  return classMap[status] || 'unknown'
}

const getCourseCategory = (status) => {
  const categoryMap = {
    0: 'draft',
    1: 'published',
    2: 'unpublished'
  }
  return categoryMap[status] || 'unknown'
}

const getProgressClass = (status) => {
  return status === 1 ? 'published' : status === 0 ? 'draft' : 'unpublished'
}

const getProgressWidth = (status) => {
  return status === 1 ? '100%' : status === 0 ? '50%' : '0%'
}

const getStatusText = (status) => {
  const textMap = {
    0: '草稿状态',
    1: '可学习',
    2: '已下架'
  }
  return textMap[status] || '未知状态'
}

const getStatusBadgeClass = (status) => {
  const classMap = {
    0: 'badge-draft',
    1: 'badge-published',
    2: 'badge-unpublished'
  }
  return classMap[status] || 'badge-unknown'
}

const getEmptyText = () => {
  if (searchFilters.keyword.trim()) {
    return `没有找到包含"${searchFilters.keyword}"的课程`
  }
  if (activeFilter.value !== 'all') {
    const labelMap = {
      'published': '已发布',
      'draft': '草稿',
      'unpublished': '已下架'
    }
    return `暂无${labelMap[activeFilter.value]}课程`
  }
  return '暂无课程数据，请联系管理员添加课程'
}

// 操作方法
const startCourse = (courseId) => {
  ElMessage.success(`正在启动课程: ${courseId}`)
}

const viewCourse = (courseId) => {
  ElMessage.info(`查看课程详情: ${courseId}`)
}

const toggleFavorite = (courseId) => {
  ElMessage.success(`收藏状态已切换: ${courseId}`)
}

// 初始化数据加载
const initData = async () => {
  try {
    console.log('初始化课程列表数据...')

    await loadCourses()
    courseStats.value = getCourseStats()

    const publishedCourses = courses.value.filter(course => course.status === 1)
    if (publishedCourses.length > 0) {
      recommendedCourse.value = publishedCourses[0]
    }

    console.log('课程数据初始化完成:', {
      总课程数: courses.value.length,
      统计数据: courseStats.value,
      推荐课程: recommendedCourse.value?.title
    })

  } catch (error) {
    console.error('初始化课程数据失败:', error)
    ElMessage.error('加载课程数据失败，请刷新页面重试')
  }
}

// 组件挂载时加载数据
onMounted(() => {
  initData()
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
  flex-wrap: wrap;
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
  margin-bottom: 20px;
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

.course-info {
  flex: 1;
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

/* 搜索部分 */
.search-section {
  margin-bottom: 20px;
}

.search-input {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  transition: border-color 0.3s ease;
}

.search-input:focus {
  outline: none;
  border-color: #667eea;
}

/* 加载状态 */
.loading-state {
  text-align: center;
  padding: 40px;
  color: #666;
}

.loading-spinner {
  font-size: 24px;
  margin-bottom: 10px;
}

.loading-text {
  font-size: 14px;
}

/* 🔧 优化：课程列表样式 */
.course-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.course-item {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  border: 1px solid #e9ecef;
}

.course-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

/* 🔧 重点优化：课程封面样式 */
.course-cover {
  position: relative;
  width: 100%;
  height: 180px; /* 固定高度，16:9 比例 */
  overflow: hidden;
  background: #f8f9fa;
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover; /* 保持比例，裁剪超出部分 */
  object-position: center; /* 居中裁剪 */
  transition: transform 0.3s ease;
}

.course-item:hover .cover-image {
  transform: scale(1.05); /* 悬停时轻微放大 */
}

.default-cover {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  color: #666;
}

/* 状态徽章 */
.status-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  backdrop-filter: blur(10px);
}

.badge-draft {
  background: rgba(255, 193, 7, 0.9);
  color: white;
}

.badge-published {
  background: rgba(40, 167, 69, 0.9);
  color: white;
}

.badge-unpublished {
  background: rgba(220, 53, 69, 0.9);
  color: white;
}

/* 课程内容区域 */
.course-content {
  padding: 16px;
}

.course-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2; /* 最多显示2行 */
  -webkit-box-orient: vertical;
}

.course-description {
  font-size: 14px;
  color: #666;
  margin-bottom: 12px;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2; /* 最多显示2行 */
  -webkit-box-orient: vertical;
}

.course-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 12px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #666;
}

.meta-item .icon {
  font-size: 14px;
}

.course-progress-section {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.progress-bar {
  flex: 1;
  height: 4px;
  background: #e9ecef;
  border-radius: 2px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  transition: width 0.3s ease;
}

.progress-fill.published {
  background: #28a745;
}

.progress-fill.draft {
  background: #ffc107;
}

.progress-fill.unpublished {
  background: #dc3545;
}

.progress-text {
  font-size: 12px;
  color: #666;
  white-space: nowrap;
}

/* 课程操作按钮 */
.course-actions {
  display: flex;
  gap: 8px;
  padding: 0 16px 16px;
}

.course-actions button {
  flex: 1;
  padding: 8px 12px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 12px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.btn-continue {
  background: #28a745;
  color: white;
}

.btn-draft {
  background: #ffc107;
  color: #333;
}

.btn-unavailable {
  background: #6c757d;
  color: white;
  cursor: not-allowed;
}

.btn-favorite {
  flex: 0 0 auto;
  background: transparent;
  color: #dc3545;
  border: 1px solid #dc3545;
  padding: 6px 10px;
  min-width: 40px;
}

.btn-favorite:hover {
  background: #dc3545;
  color: white;
}

.course-actions button:not(:disabled):hover {
  transform: translateY(-1px);
}

/* 空状态 */
.empty-state {
  grid-column: 1 / -1; /* 占满整行 */
  text-align: center;
  padding: 60px 20px;
  color: #666;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-text {
  font-size: 16px;
}

/* 分页器 */
.pagination-wrapper {
  margin-top: 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 15px;
}

.pagination-info {
  color: #666;
  font-size: 14px;
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 10px;
}

.pagination-btn {
  padding: 8px 16px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.pagination-btn:not(:disabled):hover {
  background: #f8f9fa;
  border-color: #667eea;
}

.pagination-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.pagination-current {
  padding: 8px 12px;
  background: #667eea;
  color: white;
  border-radius: 6px;
  font-weight: 500;
}

/* 🔧 响应式设计优化 */
@media (max-width: 768px) {
  .course-list {
    grid-template-columns: 1fr; /* 移动端单列显示 */
    gap: 16px;
  }

  .course-cover {
    height: 160px; /* 移动端稍微降低高度 */
  }

  .course-filters {
    flex-wrap: wrap;
  }

  .pagination-wrapper {
    flex-direction: column;
    text-align: center;
  }

  .recommended-course {
    flex-direction: column;
    text-align: center;
    gap: 12px;
  }

  .course-meta {
    flex-direction: column;
    gap: 8px;
  }
}

@media (max-width: 480px) {
  .course-cover {
    height: 140px; /* 小屏幕进一步降低 */
  }

  .course-content {
    padding: 12px;
  }

  .course-actions {
    padding: 0 12px 12px;
    flex-direction: column;
    gap: 8px;
  }

  .course-actions button {
    width: 100%;
  }
}
</style>
