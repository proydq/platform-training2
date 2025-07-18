<template>
  <div class="exam-result-container">
    <div class="result-header">
      <h1>考试结果</h1>
      <p>{{ examResult.examTitle }}</p>
    </div>

    <div class="result-content">
      <!-- 成绩卡片 -->
      <div class="score-card">
        <div class="score-display">
          <div class="score-main">
            <span class="score-number">{{ examResult.score }}</span>
            <span class="score-total">/ {{ examResult.totalScore }}</span>
          </div>
          <div class="score-status" :class="statusClass">
            <el-icon>
              <component :is="statusIcon" />
            </el-icon>
            <span>{{ examResult.passed ? '通过' : '未通过' }}</span>
          </div>
        </div>
        
        <div class="score-details">
          <div class="detail-item">
            <span class="label">正确题数</span>
            <span class="value">{{ examResult.correctCount }} / {{ examResult.totalCount }}</span>
          </div>
          <div class="detail-item">
            <span class="label">正确率</span>
            <span class="value">{{ formatPercent(examResult.accuracyRate) }}</span>
          </div>
          <div class="detail-item">
            <span class="label">用时</span>
            <span class="value">{{ formatDuration(examResult.duration) }}</span>
          </div>
          <div class="detail-item">
            <span class="label">及格分</span>
            <span class="value">{{ examResult.passScore }}分</span>
          </div>
        </div>
      </div>

      <!-- 统计图表 -->
      <div class="chart-card">
        <h3>答题统计</h3>
        <div class="chart-container">
          <div class="chart-item">
            <div class="chart-circle correct">
              <div class="circle-content">
                <div class="number">{{ examResult.correctCount }}</div>
                <div class="label">正确</div>
              </div>
            </div>
          </div>
          <div class="chart-item">
            <div class="chart-circle wrong">
              <div class="circle-content">
                <div class="number">{{ examResult.wrongCount }}</div>
                <div class="label">错误</div>
              </div>
            </div>
          </div>
          <div class="chart-item">
            <div class="chart-circle total">
              <div class="circle-content">
                <div class="number">{{ examResult.totalCount }}</div>
                <div class="label">总题数</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 时间信息 -->
      <div class="time-card">
        <h3>考试时间</h3>
        <div class="time-info">
          <div class="time-item">
            <span class="time-label">开始时间</span>
            <span class="time-value">{{ formatDateTime(examResult.startTime) }}</span>
          </div>
          <div class="time-item">
            <span class="time-label">结束时间</span>
            <span class="time-value">{{ formatDateTime(examResult.endTime) }}</span>
          </div>
          <div class="time-item">
            <span class="time-label">总用时</span>
            <span class="time-value">{{ formatDuration(examResult.duration) }}</span>
          </div>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="action-buttons">
        <el-button size="large" @click="backToExams">
          <el-icon><ArrowLeft /></el-icon>
          返回考试中心
        </el-button>
        
        <el-button size="large" type="primary" @click="viewAnswers">
          <el-icon><View /></el-icon>
          查看答案解析
        </el-button>
        
        <el-button size="large" type="success" @click="retakeExam" v-if="!examResult.passed">
          <el-icon><Refresh /></el-icon>
          重新考试
        </el-button>
        
        <el-button size="large" @click="downloadCertificate" v-if="examResult.passed">
          <el-icon><Download /></el-icon>
          下载证书
        </el-button>
      </div>
    </div>

    <!-- 恭喜通过对话框 -->
    <el-dialog
      v-model="congratsVisible"
      title="恭喜通过！"
      width="400px"
      :show-close="false"
      :close-on-click-modal="false"
    >
      <div class="congrats-content">
        <div class="congrats-icon">🎉</div>
        <p>恭喜您顺利通过考试！</p>
        <p>您的成绩：<strong>{{ examResult.score }}分</strong></p>
      </div>
      <template #footer>
        <el-button type="primary" @click="congratsVisible = false">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, View, Refresh, Download, SuccessFilled, CircleCloseFilled } from '@element-plus/icons-vue'
import { getExamResultAPI } from '@/api/exam'

const route = useRoute()
const router = useRouter()

// 响应式数据
const examResult = ref({})
const congratsVisible = ref(false)
const loading = ref(false)

// 计算属性
const statusClass = computed(() => {
  return examResult.value.passed ? 'passed' : 'failed'
})

const statusIcon = computed(() => {
  return examResult.value.passed ? SuccessFilled : CircleCloseFilled
})

// 生命周期
onMounted(() => {
  loadExamResult()
})

// 方法
const loadExamResult = async () => {
  try {
    loading.value = true
    const examId = route.params.examId
    const response = await getExamResultAPI(examId)
    
    if (response.code === 200) {
      examResult.value = response.data
      
      // 如果通过了考试，显示恭喜对话框
      if (examResult.value.passed) {
        setTimeout(() => {
          congratsVisible.value = true
        }, 500)
      }
    } else {
      ElMessage.error(response.message || '获取考试结果失败')
      router.push('/exams')
    }
  } catch (error) {
    console.error('获取考试结果失败:', error)
    ElMessage.error('获取考试结果失败')
    router.push('/exams')
  } finally {
    loading.value = false
  }
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

const backToExams = () => {
  router.push('/exams')
}

const viewAnswers = () => {
  router.push(`/exams/${route.params.examId}/answers`)
}

const retakeExam = () => {
  router.push(`/exams/${route.params.examId}/take`)
}

const downloadCertificate = () => {
  ElMessage.success('证书下载功能开发中...')
}
</script>

<style scoped>
.exam-result-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.result-header {
  text-align: center;
  margin-bottom: 40px;
}

.result-header h1 {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 32px;
}

.result-header p {
  margin: 0;
  color: #666;
  font-size: 18px;
}

.result-content {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.score-card {
  background: #fff;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border: 1px solid #e0e0e0;
}

.score-display {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 30px;
  border-bottom: 2px solid #f0f0f0;
}

.score-main {
  display: flex;
  align-items: baseline;
}

.score-number {
  font-size: 60px;
  font-weight: bold;
  color: #409EFF;
}

.score-total {
  font-size: 24px;
  color: #666;
  margin-left: 10px;
}

.score-status {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
  font-weight: bold;
  padding: 12px 24px;
  border-radius: 8px;
}

.score-status.passed {
  background: #f0f9ff;
  color: #059669;
}

.score-status.failed {
  background: #fef2f2;
  color: #dc2626;
}

.score-details {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 20px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.detail-item .label {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.detail-item .value {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.chart-card {
  background: #fff;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border: 1px solid #e0e0e0;
}

.chart-card h3 {
  margin: 0 0 20px 0;
  color: #333;
  text-align: center;
}

.chart-container {
  display: flex;
  justify-content: space-around;
  align-items: center;
}

.chart-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.chart-circle {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.chart-circle.correct {
  background: linear-gradient(135deg, #67C23A, #85CE61);
}

.chart-circle.wrong {
  background: linear-gradient(135deg, #F56C6C, #F78989);
}

.chart-circle.total {
  background: linear-gradient(135deg, #409EFF, #66B1FF);
}

.circle-content {
  color: white;
  text-align: center;
}

.circle-content .number {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 4px;
}

.circle-content .label {
  font-size: 12px;
}

.time-card {
  background: #fff;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border: 1px solid #e0e0e0;
}

.time-card h3 {
  margin: 0 0 20px 0;
  color: #333;
  text-align: center;
}

.time-info {
  display: flex;
  justify-content: space-around;
  align-items: center;
}

.time-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
}

.time-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.time-value {
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 20px;
}

.congrats-content {
  text-align: center;
  padding: 20px;
}

.congrats-icon {
  font-size: 48px;
  margin-bottom: 20px;
}

.congrats-content p {
  margin: 10px 0;
  font-size: 16px;
}

.congrats-content strong {
  color: #409EFF;
  font-size: 20px;
}

@media (max-width: 768px) {
  .score-display {
    flex-direction: column;
    gap: 20px;
  }
  
  .score-details {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .chart-container {
    flex-direction: column;
    gap: 20px;
  }
  
  .time-info {
    flex-direction: column;
    gap: 15px;
  }
  
  .action-buttons {
    flex-direction: column;
    align-items: center;
  }
}
</style>