<!-- frontend/src/views/CourseManagement.vue -->
<template>
  <div class="course-management-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2>🎓 课程管理</h2>
        <div class="stats-info">
          <span>共 {{ pagination.total }} 门课程</span>
          <span v-if="filters.category" class="filter-indicator">
            {{ filters.category }}
          </span>
          <span v-if="filters.status !== undefined" class="filter-indicator">
            {{ getStatusText(filters.status) }}
          </span>
        </div>
      </div>
      <div>
        <el-button 
          type="primary" 
          size="large" 
          @click="showAddCourseModal"
          v-if="userStore.userRole === 'ADMIN' || userStore.userRole === 'TEACHER'"
        >
          <el-icon><Plus /></el-icon>
          新增课程
        </el-button>
      </div>
    </div>

    <!-- 筛选器 -->
    <div class="course-filter">
      <div class="filter-item">
        <span class="filter-label">关键词:</span>
        <el-input
          v-model="filters.keyword"
          placeholder="搜索课程标题..."
          style="width: 200px;"
          clearable
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
      
      <div class="filter-item">
        <span class="filter-label">分类:</span>
        <el-select v-model="filters.category" placeholder="请选择分类" clearable style="width: 150px;">
          <el-option label="全部分类" value="" />
          <el-option v-for="category in courseCategories" :key="category" :label="category" :value="category" />
        </el-select>
      </div>
      
      <div class="filter-item">
        <span class="filter-label">难度:</span>
        <el-select v-model="filters.difficultyLevel" placeholder="请选择难度" clearable style="width: 120px;">
          <el-option label="全部难度" value="" />
          <el-option v-for="level in difficultyLevels" :key="level.value" :label="level.label" :value="level.value" />
        </el-select>
      </div>
      
      <div class="filter-item" v-if="userStore.userRole === 'ADMIN'">
        <span class="filter-label">状态:</span>
        <el-select v-model="filters.status" placeholder="请选择状态" clearable style="width: 120px;">
          <el-option label="全部状态" value="" />
          <el-option v-for="status in courseStatuses" :key="status.value" :label="status.label" :value="status.value" />
        </el-select>
      </div>
      
      <div class="filter-buttons">
        <el-button type="primary" @click="searchCourses" :loading="loading">搜索</el-button>
        <el-button @click="resetFilters" :loading="loading">重置</el-button>
      </div>
    </div>

    <!-- 课程网格 -->
    <div v-loading="loading" class="course-grid">
      <div
        v-for="course in courses"
        :key="course.id"
        class="course-card"
      >
        <!-- 课程封面 - 使用AuthImage组件 -->
        <div class="course-cover">
          <AuthImage 
            v-if="course.coverImage" 
            :src="course.coverImage" 
            :alt="course.title"
            :style="{ width: '100%', height: '100%' }"
            default-image="📚"
          />
          <div v-else class="default-cover">📚</div>
          <div class="course-status-badge" :style="{ backgroundColor: getStatusColor(course.status) }">
            {{ getStatusText(course.status) }}
          </div>
        </div>

        <!-- 课程信息 -->
        <div class="course-content">
          <div class="course-title">{{ course.title }}</div>
          <div class="course-description">{{ course.description }}</div>
          
          <div class="course-meta">
            <el-tag size="small">{{ course.category }}</el-tag>
            <el-tag size="small" :type="getDifficultyType(course.difficultyLevel)">
              {{ getDifficultyText(course.difficultyLevel) }}
            </el-tag>
            <el-tag size="small" type="info">{{ formatDuration(course.duration) }}</el-tag>
            <el-tag size="small" type="warning">{{ formatPrice(course.price) }}</el-tag>
          </div>
          
          <div class="course-instructor">
            <el-icon><User /></el-icon>
            <span>{{ course.instructorName || course.instructorId }}</span>
          </div>
          
          <div class="course-stats">
            <div class="stat-item">
              <el-icon><View /></el-icon>
              <span>{{ course.viewCount || 0 }}</span>
            </div>
            <div class="stat-item">
              <el-icon><Star /></el-icon>
              <span>{{ course.favoriteCount || 0 }}</span>
            </div>
            <div class="stat-item">
              <el-icon><Document /></el-icon>
              <span>{{ course.chapterCount || 0 }}章</span>
            </div>
          </div>
          
          <div class="course-actions">
            <el-button size="small" @click="viewCourse(course.id)">
              <el-icon><View /></el-icon>
              详情
            </el-button>
            <el-button 
              size="small" 
              type="primary" 
              @click="editCourse(course)"
              v-if="userStore.userRole === 'ADMIN' || course.instructorId === userStore.userInfo.id"
            >
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button 
              size="small" 
              :type="course.status === 1 ? 'warning' : 'success'"
              @click="toggleCourseStatus(course)"
              v-if="userStore.userRole === 'ADMIN' || course.instructorId === userStore.userInfo.id"
            >
              {{ course.status === 1 ? '下架' : '发布' }}
            </el-button>
            <el-button 
              size="small" 
              type="danger" 
              @click="deleteCourse(course)"
              v-if="userStore.userRole === 'ADMIN'"
            >
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-if="!loading && courses.length === 0" class="empty-state">
      <div class="empty-icon">📚</div>
      <div class="empty-text">暂无课程数据</div>
      <el-button type="primary" @click="showAddCourseModal" v-if="userStore.userRole === 'ADMIN' || userStore.userRole === 'TEACHER'">
        创建第一门课程
      </el-button>
    </div>

    <!-- 分页器 -->
    <div class="pagination-wrapper" v-if="courses.length > 0">
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 课程编辑模态框 -->
    <el-dialog
      v-model="courseModalVisible"
      :title="courseModalTitle"
      width="80%"
      :close-on-click-modal="false"
      @close="closeCourseModal"
    >
      <CourseForm
        ref="courseFormRef"
        :course-data="editingCourse"
        :is-editing="courseModalTitle === '编辑课程'"
        @save="handleCourseSave"
        @cancel="closeCourseModal"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Plus, Search, Edit, Delete, View, User, Star, Document 
} from '@element-plus/icons-vue'

// 导入组件
import CourseForm from '@/components/CourseForm.vue'
// 导入状态管理
import { useUserStore } from '@/stores/user'
import { useCourse } from '@/composables/useCourse'

// 🔧 添加这行导入
import { getCourseChaptersAPI } from '@/api/course'

// 状态管理
const userStore = useUserStore()

// 使用组合式函数
const {
  loading,
  courses,
  pagination,
  filters,
  loadCourses,
  searchCourses,
  resetFilters,
  createCourse,
  updateCourse,
  deleteCourse: deleteCourseById
} = useCourse()

// 模态框状态
const courseModalVisible = ref(false)
const courseModalTitle = ref('新增课程')
const editingCourse = ref(null)

// 配置数据
const courseCategories = [
  '技术培训',
  '产品培训', 
  '安全培训',
  '管理培训',
  '营销培训'
]

const difficultyLevels = [
  { label: '入门级', value: 1 },
  { label: '初级', value: 2 },
  { label: '中级', value: 3 },
  { label: '高级', value: 4 },
  { label: '专家级', value: 5 }
]

const courseStatuses = [
  { label: '草稿', value: 0 },
  { label: '已发布', value: 1 },
  { label: '已下架', value: 2 }
]

// 工具函数
const getStatusColor = (status) => {
  const colors = {
    0: '#909399', // 草稿 - 灰色
    1: '#67C23A', // 已发布 - 绿色
    2: '#F56C6C'  // 已下架 - 红色
  }
  return colors[status] || '#909399'
}

const getStatusText = (status) => {
  const texts = {
    0: '草稿',
    1: '已发布',
    2: '已下架'
  }
  return texts[status] || '未知'
}

const getDifficultyType = (level) => {
  const types = {
    1: 'info',
    2: 'success',
    3: 'warning',
    4: 'danger',
    5: 'danger'
  }
  return types[level] || 'info'
}

const getDifficultyText = (level) => {
  const texts = {
    1: '入门级',
    2: '初级',
    3: '中级',
    4: '高级',
    5: '专家级'
  }
  return texts[level] || '未知'
}

const formatDuration = (duration) => {
  if (!duration) return '0分钟'
  if (duration >= 60) {
    const hours = Math.floor(duration / 60)
    const minutes = duration % 60
    return minutes > 0 ? `${hours}小时${minutes}分钟` : `${hours}小时`
  }
  return `${duration}分钟`
}

const formatPrice = (price) => {
  if (!price || price === 0) return '免费'
  return `¥${price}`
}

// 分页处理
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.current = 1
  loadCourses()
}

const handleCurrentChange = (page) => {
  pagination.current = page
  loadCourses()
}

// 生命周期
onMounted(() => {
  // 如果是讲师，只显示自己的课程
  if (userStore.userRole === 'TEACHER') {
    filters.instructorId = userStore.userInfo.id || userStore.userInfo.username
  }
  loadCourses()
})

// 方法
const showAddCourseModal = () => {
  courseModalTitle.value = '新增课程'
  editingCourse.value = null
  courseModalVisible.value = true
}

const editCourse = async (course) => {
  console.log('🔧 点击编辑按钮，课程数据:', course)
  
  try {
    // 设置基本课程信息
    courseModalTitle.value = '编辑课程'
    editingCourse.value = { ...course }
    
    // 🔧 新增：获取课程章节数据
    console.log('📖 开始获取章节数据...')
    const chaptersResponse = await getCourseChaptersAPI(course.id)
    
    if (chaptersResponse.code === 200) {
      // 将章节数据添加到编辑数据中
      editingCourse.value.chapters = chaptersResponse.data || []
      console.log('✅ 章节数据获取成功:', editingCourse.value.chapters)
    } else {
      console.warn('⚠️ 获取章节数据失败:', chaptersResponse.message)
      editingCourse.value.chapters = []
    }
  } catch (error) {
    console.error('❌ 获取章节数据出错:', error)
    editingCourse.value.chapters = []
    ElMessage.warning('获取章节数据失败，但可以继续编辑课程')
  }
  
  console.log('📝 最终设置的编辑数据:', editingCourse.value)
  
  // 显示模态框
  courseModalVisible.value = true
  console.log('👁️ 模态框可见性:', courseModalVisible.value)
}

const viewCourse = (courseId) => {
  ElMessage.info('课程详情功能开发中')
}

const deleteCourse = async (course) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除课程 "${course.title}" 吗？此操作不可恢复。`,
      '删除确认',
      {
        type: 'warning',
        confirmButtonText: '确定删除',
        cancelButtonText: '取消'
      }
    )
    
    const success = await deleteCourseById(course.id, course.title)
    if (success) {
      ElMessage.success('课程删除成功')
    }
  } catch (error) {
    // 用户取消操作
  }
}

const toggleCourseStatus = (course) => {
  ElMessage.info('课程状态切换功能开发中')
}

const closeCourseModal = () => {
  courseModalVisible.value = false
  editingCourse.value = null
}

// 处理课程保存
const handleCourseSave = async (courseData) => {
  try {
    let result
    
    if (courseModalTitle.value === '新增课程') {
      // 如果是讲师，自动设置讲师ID
      if (userStore.userRole === 'TEACHER') {
        courseData.instructorId = userStore.userInfo.id || userStore.userInfo.username
      }
      result = await createCourse(courseData)
    } else {
      result = await updateCourse(editingCourse.value.id, courseData)
    }
    
    if (result) {
      ElMessage.success(courseModalTitle.value === '新增课程' ? '课程创建成功' : '课程更新成功')
      closeCourseModal()
      await loadCourses() // 刷新列表
    }
  } catch (error) {
    console.error('保存课程失败:', error)
    ElMessage.error('保存课程失败，请重试')
  }
}
</script>

<style scoped>
.course-management-container {
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header-left h2 {
  color: white;
  margin: 0 0 8px 0;
  font-size: 28px;
  font-weight: 600;
}

.stats-info {
  color: rgba(255, 255, 255, 0.8);
  font-size: 14px;
  display: flex;
  gap: 16px;
}

.filter-indicator {
  background: rgba(255, 255, 255, 0.2);
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
}

.course-filter {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  padding: 20px;
  border-radius: 15px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.filter-label {
  font-size: 14px;
  color: #666;
  font-weight: 500;
  white-space: nowrap;
}

.filter-buttons {
  display: flex;
  gap: 10px;
  margin-left: auto;
}

.course-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
  gap: 24px;
  margin-top: 20px;
}

.course-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.course-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.15);
}

.course-cover {
  position: relative;
  height: 160px;
  overflow: hidden;
}

.default-cover {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.course-status-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 4px 8px;
  border-radius: 12px;
  color: white;
  font-size: 12px;
  font-weight: 500;
}

.course-content {
  padding: 20px;
}

.course-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.course-description {
  color: #666;
  font-size: 14px;
  margin-bottom: 16px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.course-meta {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.course-instructor {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #666;
  font-size: 14px;
  margin-bottom: 16px;
}

.course-stats {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #888;
}

.course-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: white;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
  opacity: 0.6;
}

.empty-text {
  font-size: 18px;
  margin-bottom: 24px;
  opacity: 0.8;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 40px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 15px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .course-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .course-filter {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filter-buttons {
    margin-left: 0;
    width: 100%;
    justify-content: center;
  }
  
  .course-actions {
    justify-content: center;
  }
}
</style>