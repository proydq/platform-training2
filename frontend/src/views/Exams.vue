<template>
  <div class="exams-container">
    <!-- 考试概览 -->
    <el-card class="overview-card">
      <template #header>
        <div class="card-header">
          <span>📊 考试概览</span>
        </div>
      </template>
      
      <div class="exam-overview">
        <div class="overview-item">
          <div class="overview-number">{{ overview.completed }}</div>
          <div class="overview-label">已完成</div>
        </div>
        <div class="overview-item">
          <div class="overview-number">{{ overview.pending }}</div>
          <div class="overview-label">待考试</div>
        </div>
        <div class="overview-item">
          <div class="overview-number">{{ overview.avgScore }}</div>
          <div class="overview-label">平均分</div>
        </div>
        <div class="overview-item">
          <div class="overview-number">{{ overview.passRate }}%</div>
          <div class="overview-label">通过率</div>
        </div>
      </div>
    </el-card>

    <!-- 筛选和搜索 -->
    <el-card class="filter-card">
      <div class="exam-filters">
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
            placeholder="搜索考试..."
            prefix-icon="Search"
            clearable
            @input="handleSearch"
          />
        </div>
      </div>
    </el-card>

    <!-- 考试列表 -->
    <div class="exams-grid">
      <div
        v-for="exam in filteredExams"
        :key="exam.id"
        class="exam-card"
        :class="{ urgent: exam.urgent }"
      >
        <div class="exam-header">
          <div class="exam-title">{{ exam.title }}</div>
          <div class="exam-status" :class="exam.status">
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
                <span>截止时间：{{ exam.deadline }}</span>
              </div>
              <div class="meta-item">
                <el-icon><Trophy /></el-icon>
                <span>及格分数：{{ exam.passScore }}分</span>
              </div>
            </div>
          </div>
          
          <!-- 考试结果（如果已完成） -->
          <div v-if="exam.status === 'completed'" class="exam-result">
            <div class="result-score" :class="{ passed: exam.score >= exam.passScore }">
              <span class="score-label">得分：</span>
              <span class="score-value">{{ exam.score }}分</span>
              <span class="score-status">
                {{ exam.score >= exam.passScore ? '通过' : '未通过' }}
              </span>
            </div>
            <div class="result-time">
              完成时间：{{ exam.completedTime }}
            </div>
          </div>
          
          <!-- 考试操作 -->
          <div class="exam-actions">
            <el-button 
              v-if="exam.status === 'available'"
              type="primary" 
              size="default"
              @click="startExam(exam)"
            >
              开始考试
            </el-button>
            
            <el-button 
              v-if="exam.status === 'completed'"
              type="success" 
              size="default"
              @click="viewResult(exam)"
            >
              查看结果
            </el-button>
            
            <el-button 
              v-if="exam.status === 'completed' && exam.score < exam.passScore"
              type="warning" 
              size="default"
              @click="retakeExam(exam)"
            >
              重新考试
            </el-button>
            
            <el-button 
              v-if="exam.status === 'locked'"
              disabled
              size="default"
            >
              未解锁
            </el-button>
            
            <el-button 
              size="default"
              @click="viewDetails(exam)"
            >
              考试详情
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 练习模式 -->
    <el-card class="practice-card">
      <template #header>
        <div class="card-header">
          <span>🎯 练习模式</span>
        </div>
      </template>
      
      <div class="practice-section">
        <div class="practice-item">
          <div class="practice-info">
            <h4>随机练习</h4>
            <p>从题库中随机抽取题目进行练习</p>
          </div>
          <el-button type="primary" @click="startPractice('random')">
            开始练习
          </el-button>
        </div>
        
        <div class="practice-item">
          <div class="practice-info">
            <h4>错题重做</h4>
            <p>复习之前做错的题目</p>
          </div>
          <el-button type="warning" @click="startPractice('wrong')">
            错题练习
          </el-button>
        </div>
        
        <div class="practice-item">
          <div class="practice-info">
            <h4>专项练习</h4>
            <p>针对特定知识点进行练习</p>
          </div>
          <el-button @click="startPractice('specific')">
            专项练习
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[9, 18, 36]"
        :total="totalExams"
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

const router = useRouter()

// 概览数据
const overview = ref({
  completed: 8,
  pending: 4,
  avgScore: 87,
  passRate: 85
})

// 筛选器
const filters = [
  { key: 'all', label: '全部' },
  { key: 'available', label: '可考试' },
  { key: 'completed', label: '已完成' },
  { key: 'locked', label: '未解锁' }
]

const activeFilter = ref('all')
const searchKeyword = ref('')

// 分页
const currentPage = ref(1)
const pageSize = ref(9)
const totalExams = ref(12)

// 考试数据
const exams = ref([
  {
    id: 1,
    title: '产品知识考试',
    description: '测试对公司核心产品功能和特性的掌握程度',
    status: 'available',
    duration: 60,
    questionCount: 30,
    deadline: '2025-01-20',
    passScore: 80,
    urgent: true,
    attempts: 0,
    maxAttempts: 3
  },
  {
    id: 2,
    title: '销售技能测试',
    description: '评估销售沟通技巧和客户处理能力',
    status: 'completed',
    duration: 45,
    questionCount: 25,
    deadline: '2025-01-15',
    passScore: 75,
    score: 92,
    completedTime: '2025-01-14 14:30',
    urgent: false,
    attempts: 1,
    maxAttempts: 2
  },
  {
    id: 3,
    title: '客户服务规范',
    description: '检验对客户服务标准流程的理解',
    status: 'completed',
    duration: 30,
    questionCount: 20,
    deadline: '2025-01-10',
    passScore: 80,
    score: 65,
    completedTime: '2025-01-09 10:15',
    urgent: false,
    attempts: 1,
    maxAttempts: 3
  },
  {
    id: 4,
    title: '团队协作评估',
    description: '测试团队合作和沟通协调能力',
    status: 'available',
    duration: 40,
    questionCount: 25,
    deadline: '2025-01-25',
    passScore: 70,
    urgent: false,
    attempts: 0,
    maxAttempts: 2
  },
  {
    id: 5,
    title: '安全知识测试',
    description: '检验工作场所安全规范的掌握情况',
    status: 'locked',
    duration: 35,
    questionCount: 20,
    deadline: '2025-02-01',
    passScore: 85,
    urgent: false,
    attempts: 0,
    maxAttempts: 3
  },
  {
    id: 6,
    title: '质量管理考核',
    description: '评估质量控制流程的理解和应用',
    status: 'available',
    duration: 50,
    questionCount: 30,
    deadline: '2025-01-30',
    passScore: 80,
    urgent: true,
    attempts: 0,
    maxAttempts: 2
  }
])

// 计算属性 - 筛选后的考试
const filteredExams = computed(() => {
  let result = exams.value

  // 状态筛选
  if (activeFilter.value !== 'all') {
    result = result.filter(exam => exam.status === activeFilter.value)
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
    'available': '可考试',
    'completed': '已完成',
    'locked': '未解锁',
    'in-progress': '进行中'
  }
  return statusMap[status] || status
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
    
    ElMessage.success(`开始考试：${exam.title}`)
    // router.push(`/exams/${exam.id}/take`)
  } catch {
    // 用户取消
  }
}

const viewResult = (exam) => {
  ElMessage.info(`查看考试结果：${exam.title}`)
  // router.push(`/exams/${exam.id}/result`)
}

const retakeExam = async (exam) => {
  if (exam.attempts >= exam.maxAttempts) {
    ElMessage.warning('已达到最大考试次数限制')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要重新考试吗？这是第${exam.attempts + 1}次考试，最多可考${exam.maxAttempts}次。`,
      '重新考试',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    ElMessage.success(`开始重新考试：${exam.title}`)
    // router.push(`/exams/${exam.id}/take`)
  } catch {
    // 用户取消
  }
}

const viewDetails = (exam) => {
  ElMessage.info(`查看考试详情：${exam.title}`)
  // router.push(`/exams/${exam.id}/details`)
}

const startPractice = (type) => {
  const practiceTypes = {
    'random': '随机练习',
    'wrong': '错题练习',
    'specific': '专项练习'
  }
  
  ElMessage.success(`开始${practiceTypes[type]}`)
  // router.push(`/practice/${type}`)
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
}

const handleCurrentChange = (page) => {
  currentPage.value = page
}

onMounted(() => {
  // 初始化考试数据
})
</script>

<style scoped>
.exams-container {
  max-width: 1200px;
  margin: 0 auto;
}

.overview-card,
.filter-card,
.practice-card {
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

.exam-overview {
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

.exam-filters {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
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
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 15px;
  padding: 20px;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  animation: fadeInUp 0.6s ease forwards;
}

.exam-card.urgent {
  border-left: 4px solid #f56c6c;
}

.exam-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.15);
}

.exam-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.exam-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.exam-status {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.exam-status.available {
  background: #e1f5fe;
  color: #0277bd;
}

.exam-status.completed {
  background: #e8f5e8;
  color: #2e7d32;
}

.exam-status.locked {
  background: #f1f5f9;
  color: #64748b;
}

.exam-description {
  color: #666;
  font-size: 14px;
  line-height: 1.5;
  margin: 0 0 15px 0;
}

.exam-meta {
  margin-bottom: 15px;
}

.meta-row {
  display: flex;
  gap: 20px;
  margin-bottom: 8px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: #666;
}

.exam-result {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 15px;
}

.result-score {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.score-label {
  color: #666;
  font-size: 14px;
}

.score-value {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.score-status {
  padding: 2px 8px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 500;
}

.result-score.passed .score-status {
  background: #e8f5e8;
  color: #2e7d32;
}

.result-score:not(.passed) .score-status {
  background: #ffebee;
  color: #c62828;
}

.result-time {
  font-size: 12px;
  color: #666;
}

.exam-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.exam-actions .el-button {
  flex: 1;
  min-width: 100px;
}

.practice-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.practice-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: rgba(102, 126, 234, 0.05);
  border-radius: 10px;
}

.practice-info h4 {
  margin: 0 0 5px 0;
  color: #333;
  font-size: 16px;
}

.practice-info p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .exam-filters {
    flex-direction: column;
    gap: 15px;
  }
  
  .search-box {
    width: 100%;
  }
  
  .exams-grid {
    grid-template-columns: 1fr;
    gap: 15px;
  }
  
  .meta-row {
    flex-direction: column;
    gap: 8px;
  }
  
  .exam-actions {
    flex-direction: column;
  }
  
  .practice-item {
    flex-direction: column;
    gap: 15px;
    text-align: center;
  }
}

@media (max-width: 480px) {
  .exam-overview {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .exam-header {
    flex-direction: column;
    gap: 10px;
    align-items: flex-start;
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