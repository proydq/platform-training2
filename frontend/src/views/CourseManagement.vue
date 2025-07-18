<template>
  <div class="course-management-container">
    <!-- 页面标题和操作按钮 -->
    <div class="page-header">
      <div class="header-left">
        <h2>📚 课程管理</h2>
        <div class="stats-info">
          <span>共 {{ pagination.total }} 门课程</span>
          <span v-if="hasFilters" class="filter-indicator">（已筛选）</span>
        </div>
      </div>
      <div class="header-right">
        <el-button type="primary" size="large" @click="showAddCourseModal" :loading="loading">
          <el-icon><Plus /></el-icon>
          新增课程
        </el-button>
      </div>
    </div>

    <!-- 课程筛选器 -->
    <div class="course-filter">
      <div class="filter-item">
        <span class="filter-label">搜索关键词</span>
        <el-input
          v-model="filters.keyword"
          placeholder="输入课程名称或描述"
          style="width: 240px"
          @keyup.enter="searchCourses"
          clearable
        />
      </div>
      
      <div class="filter-item">
        <span class="filter-label">课程分类</span>
        <el-select v-model="filters.category" placeholder="全部分类" style="width: 140px" clearable>
          <el-option v-for="category in courseCategories" :key="category" :label="category" :value="category" />
        </el-select>
      </div>
      
      <div class="filter-item">
        <span class="filter-label">难度级别</span>
        <el-select v-model="filters.difficultyLevel" placeholder="全部级别" style="width: 120px" clearable>
          <el-option v-for="level in difficultyLevels" :key="level.value" :label="level.label" :value="level.value" />
        </el-select>
      </div>
      
      <div class="filter-item">
        <span class="filter-label">课程状态</span>
        <el-select v-model="filters.status" placeholder="全部状态" style="width: 120px" clearable>
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
        <!-- 课程封面 -->
        <div class="course-cover">
          <img v-if="course.coverImage" :src="course.coverImage" :alt="course.title" />
          <div v-else class="default-cover">📚</div>
          <div class="course-status-badge" :style="{ backgroundColor: getStatusColor(course.status) }">
            {{ getCourseStatusText(course.status) }}
          </div>
        </div>

        <!-- 课程信息 -->
        <div class="course-content">
          <div class="course-title">{{ course.title }}</div>
          <div class="course-description">{{ course.description }}</div>
          
          <div class="course-meta">
            <el-tag size="small">{{ course.category }}</el-tag>
            <el-tag size="small" :type="getDifficultyType(course.difficultyLevel)">
              {{ getDifficultyLevelText(course.difficultyLevel) }}
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
              <el-icon><Reading /></el-icon>
              <span>{{ course.studentCount || 0 }} 人学习</span>
            </div>
            <div class="stat-item">
              <el-icon><Star /></el-icon>
              <span>{{ course.rating || 0 }}/5.0</span>
            </div>
            <div class="stat-item">
              <el-icon><Calendar /></el-icon>
              <span>{{ formatDate(course.createTime) }}</span>
            </div>
          </div>
          
          <div class="course-actions">
            <el-button size="small" @click="viewCourse(course.id)">
              <el-icon><View /></el-icon>
              查看
            </el-button>
            <el-button size="small" type="primary" @click="editCourse(course)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button 
              v-if="course.status === 0" 
              size="small" 
              type="success" 
              @click="publishCourse(course.id, course.title)"
            >
              <el-icon><VideoPlay /></el-icon>
              发布
            </el-button>
            <el-button 
              v-if="course.status === 1" 
              size="small" 
              type="warning" 
              @click="unpublishCourse(course.id, course.title)"
            >
              <el-icon><VideoPause /></el-icon>
              下架
            </el-button>
            <el-button 
              size="small" 
              type="danger" 
              @click="deleteCourse(course.id, course.title)"
            >
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-if="isEmpty" class="empty-state">
      <div class="empty-icon">📚</div>
      <div class="empty-text">
        {{ hasFilters ? '没有找到符合条件的课程' : '暂无课程数据' }}
      </div>
      <el-button v-if="!hasFilters" type="primary" @click="showAddCourseModal">
        创建第一门课程
      </el-button>
      <el-button v-else @click="resetFilters">
        清除筛选条件
      </el-button>
    </div>

    <!-- 分页 -->
    <div v-if="courses.length > 0" class="pagination-wrapper">
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="(size) => handlePagination(1, size)"
        @current-change="(page) => handlePagination(page)"
      />
    </div>

    <!-- 课程编辑模态框 -->
    <el-dialog
      v-model="courseModalVisible"
      :title="courseModalTitle"
      width="80%"
      :close-on-click-modal="false"
      destroy-on-close
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
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Plus, User, Reading, Star, Calendar, View, Edit, 
  VideoPlay, VideoPause, Delete 
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useCourse } from '@/composables/useCourse'
import CourseForm from '@/components/CourseForm.vue'

// 数据和状态
const userStore = useUserStore()

// 使用课程管理composable
const {
  loading,
  courses,
  pagination,
  filters,
  hasFilters,
  isEmpty,
  loadCourses,
  searchCourses,
  resetFilters,
  handlePagination,
  createCourse,
  updateCourse,
  deleteCourse,
  publishCourse,
  unpublishCourse,
  formatDate,
  getStatusColor,
  getDifficultyType,
  formatPrice,
  formatDuration,
  getDifficultyLevelText,
  getCourseStatusText
} = useCourse()

// 模态框状态
const courseModalVisible = ref(false)
const courseModalTitle = ref('新增课程')
const courseFormRef = ref()
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

const editCourse = (course) => {
  courseModalTitle.value = '编辑课程'
  editingCourse.value = { ...course }
  courseModalVisible.value = true
}

const viewCourse = (courseId) => {
  ElMessage.info('课程详情功能开发中')
}

const closeCourseModal = () => {
  courseModalVisible.value = false
  editingCourse.value = null
}

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
      closeCourseModal()
    }
  } catch (error) {
    console.error('保存课程失败:', error)
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

.course-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
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