<template>
  <div class="exams-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>考试中心</h1>
      <p>选择考试开始答题</p>
    </div>

    <!-- 筛选器 -->
    <el-card class="filter-card">
      <div class="filter-section">
        <el-button-group class="filter-buttons">
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
            placeholder="搜索考试..."
            prefix-icon="Search"
            clearable
            @input="handleSearch"
          />
        </div>
      </div>
    </el-card>

    <!-- 考试列表 -->
    <div class="exams-grid" v-loading="loading">
      <div
        v-for="exam in filteredExams"
        :key="exam.id"
        class="exam-card"
        :class="{ urgent: exam.urgent }"
      >
        <div class="exam-header">
          <div class="exam-title">{{ exam.title }}</div>
          <div class="exam-status" :class="getStatusClass(exam.status)">
            {{ getStatusText(exam.status) }}
          </div>
        </div>
        
        <div class="exam-content">
          <p class="exam-description">{{ exam.description }}</p>
          
          <div class="exam-meta">
            <div class="meta-row">
              <div class="meta-item">
                <el-icon><Clock /></el-icon>
                <span>考试时长：{{ exam.duration }}分钟</span>
              </div>
              <div class="meta-item">
                <el-icon><Document /></el-icon>
                <span>题目数量：{{ exam.questionCount }}题</span>
              </div>
            </div>
            <div class="meta-row">
              <div class="meta-item">
                <el-icon><Calendar /></el-icon>
                <span>截止时间：{{ formatDateTime(exam.endTime) }}</span>
              </div>
              <div class="meta-item">
                <el-icon><Trophy /></el-icon>
                <span>及格分数：{{ exam.passScore }}分</span>
              </div>
            </div>
          </div>
          
          <!-- 考试结果（如果已完成） -->
          <div v-if="exam.userResult" class="exam-result">
            <div class="result-score" :class="{ passed: exam.userResult.passed }">
              <span class="score-label">得分：</span>
              <span class="score-value">{{ exam.userResult.score }}分</span>
              <span class="score-status">
                {{ exam.userResult.passed ? '通过' : '未通过' }}
              </span>
            </div>
            <div class="result-details">
              <span>正确率：{{ formatPercent(exam.userResult.accuracyRate) }}</span>
              <span>用时：{{ formatDuration(exam.userResult.duration) }}</span>
            </div>
          </div>
        </div>
        
        <div class="exam-actions">
          <!-- 可以考试 -->
          <template v-if="exam.canTakeExam && !exam.userResult">
            <el-button type="primary" size="large" @click="startExam(exam)">
              开始考试
            </el-button>
            <el-button size="large" @click="viewDetails(exam)">
              查看详情
            </el-button>
          </template>
          
          <!-- 已完成 -->
          <template v-else-if="exam.userResult">
            <el-button type="success" size="large" @click="viewResult(exam)">
              查看结果
            </el-button>
            <el-button size="large" @click="viewDetails(exam)">
              查看详情
            </el-button>
            <el-button 
              v-if="!exam.userResult.passed && exam.retryCount > 0"
              type="warning" 
              size="large" 
              @click="retakeExam(exam)"
            >
              重新考试
            </el-button>
          </template>
          
          <!-- 未解锁 -->
          <template v-else>
            <el-button size="large" disabled>
              未开放
            </el-button>
            <el-button size="large" @click="viewDetails(exam)">
              查看详情
            </el-button>
          </template>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <el-empty v-if="!loading && filteredExams.length === 0" description="暂无考试数据" />

    <!-- 分页 -->
    <div class="pagination-container" v-if="filteredExams.length > 0">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        :total="total"
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { Clock, Document, Calendar, Trophy, Search } from '@element-plus/icons-vue'
import { getExamListAPI, getExamStatusAPI } from '@/api/exam'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const exams = ref([])
const searchKeyword = ref('')
const activeFilter = ref('all')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 筛选器配置
const filters = ref([
  { key: 'all', label: '全部考试' },
  { key: 'available', label: '可考试' },
  { key: 'completed', label: '已完成' },
  { key: 'locked', label: '未开放' }
])

// 计算属性
const filteredExams = computed(() => {
  let result = exams.value

  // 状态筛选
  if (activeFilter.value !== 'all') {
    result = result.filter(exam => {
      switch (activeFilter.value) {
        case 'available':
          return exam.canTakeExam && !exam.userResult
        case 'completed':
          return exam.userResult
        case 'locked':
          return !exam.canTakeExam
        default:
          return true
      }
    })
  }

  // 搜索筛选
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(exam => 
      exam.title.toLowerCase().includes(keyword) ||
      exam.description.toLowerCase().includes(keyword)
    )
  }

  return result
})

// 生命周期
onMounted(() => {
  loadExams()
})

// 方法
const loadExams = async () => {
  try {
    loading.value = true
    const response = await getExamListAPI()
    
    if (response.code === 200) {
      exams.value = response.data || []
      total.value = exams.value.length
      
      // 加载每个考试的状态
      await loadExamStatuses()
    } else {
      ElMessage.error(response.message || '获取考试列表失败')
    }
  } catch (error) {
    console.error('获取考试列表失败:', error)
    ElMessage.error('获取考试列表失败')
  } finally {
    loading.value = false
  }
}

const loadExamStatuses = async () => {
  try {
    const statusPromises = exams.value.map(async (exam) => {
      try {
        const response = await getExamStatusAPI(exam.id)
        if (response.code === 200) {
          exam.userResult = response.data.hasAttempted ? response.data : null
        }
      } catch (error) {
        console.error(`获取考试状态失败 - ${exam.id}:`, error)
      }
    })
    
    await Promise.all(statusPromises)
  } catch (error) {
    console.error('获取考试状态失败:', error)
  }
}

const setActiveFilter = (filterKey) => {
  activeFilter.value = filterKey
  currentPage.value = 1
}

const handleSearch = () => {
  currentPage.value = 1
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
}

const handleCurrentChange = (page) => {
  currentPage.value = page
}

const getStatusClass = (status) => {
  const classMap = {
    'PUBLISHED': 'available',
    'DRAFT': 'draft',
    'ENDED': 'ended',
    'CANCELLED': 'cancelled'
  }
  return classMap[status] || 'default'
}

const getStatusText = (status) => {
  const statusMap = {
    'PUBLISHED': '可考试',
    'DRAFT': '草稿',
    'ENDED': '已结束',
    'CANCELLED': '已取消'
  }
  return statusMap[status] || status
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '--'
  
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const formatPercent = (rate) => {
  return rate ? `${rate.toFixed(1)}%` : '0%'
}

const formatDuration = (minutes) => {
  if (!minutes) return '0分钟'
  
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  
  if (hours > 0) {
    return `${hours}小时${mins}分钟`
  } else {
    return `${mins}分钟`
  }
}

const startExam = async (exam) => {
  try {
    await ElMessageBox.confirm(
      `确定要开始「${exam.title}」考试吗？考试时长${exam.duration}分钟，开始后不能暂停。`,
      '开始考试',
      {
        confirmButtonText: '开始考试',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    router.push(`/exams/${exam.id}/take`)
  } catch {
    // 用户取消
  }
}

const viewResult = (exam) => {
  router.push(`/exams/${exam.id}/result`)
}

const retakeExam = async (exam) => {
  try {
    await ElMessageBox.confirm(
      `确定要重新考试吗？这将覆盖您之前的考试记录。`,
      '重新考试',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    router.push(`/exams/${exam.id}/take`)
  } catch {
    // 用户取消
  }
}

const viewDetails = (exam) => {
  ElMessage.info(`查看考试详情功能开发中...`)
}
</script>

<style scoped>
.exams-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
}

.page-header h1 {
  margin: 0 0 10px 0;
  color: #333;
}

.page-header p {
  margin: 0;
  color: #666;
}

.filter-card {
  margin-bottom: 30px;
}

.filter-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
}

.filter-buttons {
  display: flex;
  gap: 10px;
}

.search-box {
  width: 300px;
}

.exams-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.exam-card {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 20px;
  background: #fff;
  transition: all 0.3s ease;
}

.exam-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.exam-card.urgent {
  border-left: 4px solid #f56c6c;
}

.exam-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 15px;
}

.exam-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  flex: 1;
}

.exam-status {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  white-space: nowrap;
}

.exam-status.available {
  background: #e8f5e8;
  color: #52c41a;
}

.exam-status.ended {
  background: #f5f5f5;
  color: #999;
}

.exam-status.draft {
  background: #fff7e6;
  color: #fa8c16;
}

.exam-content {
  margin-bottom: 20px;
}

.exam-description {
  color: #666;
  margin-bottom: 15px;
  line-height: 1.5;
}

.exam-meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.meta-row {
  display: flex;
  gap: 20px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #666;
}

.exam-result {
  margin-top: 15px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 6px;
}

.result-score {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.score-value {
  font-weight: bold;
  font-size: 16px;
}

.score-status {
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
}

.result-score.passed .score-value {
  color: #52c41a;
}

.result-score.passed .score-status {
  background: #e8f5e8;
  color: #52c41a;
}

.result-score:not(.passed) .score-value {
  color: #f56c6c;
}

.result-score:not(.passed) .score-status {
  background: #fee;
  color: #f56c6c;
}

.result-details {
  display: flex;
  gap: 15px;
  font-size: 12px;
  color: #666;
}

.exam-actions {
  display: flex;
  gap: 10px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

@media (max-width: 768px) {
  .exams-grid {
    grid-template-columns: 1fr;
  }
  
  .filter-section {
    flex-direction: column;
    gap: 15px;
  }
  
  .search-box {
    width: 100%;
  }
  
  .exam-actions {
    flex-direction: column;
  }
}
</style>