<template>
  <div class="take-exam-container">
    <!-- 考试头部 -->
    <div class="exam-header">
      <div class="exam-info">
        <h2>{{ examSession.title }}</h2>
        <p>{{ examSession.description }}</p>
      </div>
      <div class="exam-timer">
        <div class="timer-display">
          <el-icon><Clock /></el-icon>
          <span>剩余时间: {{ formatTime(remainingTime) }}</span>
        </div>
        <div class="exam-progress">
          <span>已答题: {{ answeredCount }}/{{ examSession.questions?.length || 0 }}</span>
        </div>
      </div>
    </div>

    <!-- 考试内容 -->
    <div class="exam-content">
      <!-- 题目导航 -->
      <div class="question-nav">
        <div class="nav-title">题目导航</div>
        <div class="nav-items">
          <div
            v-for="(question, index) in examSession.questions"
            :key="question.id"
            class="nav-item"
            :class="{
              'active': currentQuestionIndex === index,
              'answered': answers[question.id]
            }"
            @click="goToQuestion(index)"
          >
            {{ index + 1 }}
          </div>
        </div>
      </div>

      <!-- 题目内容 -->
      <div class="question-content">
        <div v-if="currentQuestion" class="question-item">
          <div class="question-header">
            <span class="question-number">第{{ currentQuestionIndex + 1 }}题</span>
            <span class="question-score">{{ currentQuestion.score }}分</span>
          </div>
          
          <div class="question-title">
            {{ currentQuestion.content }}
          </div>

          <!-- 选择题选项 -->
          <div v-if="currentQuestion.questionType === 'SINGLE_CHOICE'" class="question-options">
            <el-radio-group v-model="answers[currentQuestion.id]">
              <el-radio
                v-for="option in currentQuestion.options"
                :key="option.key"
                :label="option.key"
                class="option-item"
              >
                {{ option.key }}. {{ option.value }}
              </el-radio>
            </el-radio-group>
          </div>

          <!-- 多选题选项 -->
          <div v-else-if="currentQuestion.questionType === 'MULTIPLE_CHOICE'" class="question-options">
            <el-checkbox-group v-model="multipleAnswers[currentQuestion.id]">
              <el-checkbox
                v-for="option in currentQuestion.options"
                :key="option.key"
                :label="option.key"
                class="option-item"
              >
                {{ option.key }}. {{ option.value }}
              </el-checkbox>
            </el-checkbox-group>
          </div>

          <!-- 判断题 -->
          <div v-else-if="currentQuestion.questionType === 'TRUE_FALSE'" class="question-options">
            <el-radio-group v-model="answers[currentQuestion.id]">
              <el-radio label="true" class="option-item">正确</el-radio>
              <el-radio label="false" class="option-item">错误</el-radio>
            </el-radio-group>
          </div>

          <!-- 填空题 -->
          <div v-else-if="currentQuestion.questionType === 'FILL_BLANK'" class="question-options">
            <el-input
              v-model="answers[currentQuestion.id]"
              type="textarea"
              placeholder="请输入答案"
              :rows="3"
            />
          </div>

          <!-- 简答题 -->
          <div v-else-if="currentQuestion.questionType === 'SHORT_ANSWER'" class="question-options">
            <el-input
              v-model="answers[currentQuestion.id]"
              type="textarea"
              placeholder="请输入答案"
              :rows="5"
            />
          </div>
        </div>

        <!-- 题目操作 -->
        <div class="question-actions">
          <el-button @click="previousQuestion" :disabled="currentQuestionIndex === 0">
            <el-icon><ArrowLeft /></el-icon>
            上一题
          </el-button>
          
          <el-button 
            v-if="currentQuestionIndex < (examSession.questions?.length || 0) - 1"
            @click="nextQuestion"
            type="primary"
          >
            下一题
            <el-icon><ArrowRight /></el-icon>
          </el-button>

          <el-button 
            v-else
            @click="showSubmitDialog"
            type="success"
          >
            提交考试
          </el-button>
        </div>
      </div>
    </div>

    <!-- 提交确认对话框 -->
    <el-dialog
      v-model="submitDialogVisible"
      title="提交考试"
      width="400px"
    >
      <div class="submit-summary">
        <p>您即将提交考试，请确认：</p>
        <ul>
          <li>总题数: {{ examSession.questions?.length || 0 }}</li>
          <li>已答题: {{ answeredCount }}</li>
          <li>未答题: {{ (examSession.questions?.length || 0) - answeredCount }}</li>
        </ul>
        <p class="warning-text">提交后将无法修改答案！</p>
      </div>
      
      <template #footer>
        <el-button @click="submitDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitExam" :loading="submitting">
          确认提交
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Clock, ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { startExamAPI, submitExamAPI } from '@/api/exam'

const route = useRoute()
const router = useRouter()

// 响应式数据
const examSession = ref({})
const currentQuestionIndex = ref(0)
const answers = reactive({})
const multipleAnswers = reactive({})
const remainingTime = ref(0)
const submitDialogVisible = ref(false)
const submitting = ref(false)
const timer = ref(null)

// 计算属性
const currentQuestion = computed(() => {
  return examSession.value.questions?.[currentQuestionIndex.value]
})

const answeredCount = computed(() => {
  return Object.keys(answers).filter(key => answers[key]).length
})

// 生命周期
onMounted(() => {
  startExam()
})

onUnmounted(() => {
  if (timer.value) {
    clearInterval(timer.value)
  }
})

// 方法
const startExam = async () => {
  try {
    const examId = route.params.examId
    const response = await startExamAPI(examId)
    
    if (response.code === 200) {
      examSession.value = response.data
      remainingTime.value = examSession.value.duration * 60 // 转换为秒
      
      // 初始化答案对象
      examSession.value.questions?.forEach(question => {
        answers[question.id] = ''
        if (question.questionType === 'MULTIPLE_CHOICE') {
          multipleAnswers[question.id] = []
        }
      })
      
      // 启动计时器
      startTimer()
    } else {
      ElMessage.error(response.message || '开始考试失败')
      router.push('/exams')
    }
  } catch (error) {
    console.error('开始考试失败:', error)
    ElMessage.error('开始考试失败')
    router.push('/exams')
  }
}

const startTimer = () => {
  timer.value = setInterval(() => {
    remainingTime.value--
    
    if (remainingTime.value <= 0) {
      clearInterval(timer.value)
      ElMessage.warning('考试时间已到，自动提交')
      submitExam()
    }
  }, 1000)
}

const formatTime = (seconds) => {
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const secs = seconds % 60
  
  if (hours > 0) {
    return `${hours}:${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
  } else {
    return `${minutes}:${secs.toString().padStart(2, '0')}`
  }
}

const goToQuestion = (index) => {
  saveCurrentAnswer()
  currentQuestionIndex.value = index
}

const previousQuestion = () => {
  if (currentQuestionIndex.value > 0) {
    saveCurrentAnswer()
    currentQuestionIndex.value--
  }
}

const nextQuestion = () => {
  if (currentQuestionIndex.value < (examSession.value.questions?.length || 0) - 1) {
    saveCurrentAnswer()
    currentQuestionIndex.value++
  }
}

const saveCurrentAnswer = () => {
  const question = currentQuestion.value
  if (question && question.questionType === 'MULTIPLE_CHOICE') {
    answers[question.id] = (multipleAnswers[question.id] || []).join(',')
  }
}

const showSubmitDialog = () => {
  saveCurrentAnswer()
  submitDialogVisible.value = true
}

const submitExam = async () => {
  try {
    submitting.value = true
    saveCurrentAnswer()
    
    const examId = route.params.examId
    const submitData = {
      examId: examId,
      answers: answers
    }
    
    const response = await submitExamAPI(examId, submitData)
    
    if (response.code === 200) {
      ElMessage.success('考试提交成功')
      router.push(`/exams/${examId}/result`)
    } else {
      ElMessage.error(response.message || '提交失败')
    }
  } catch (error) {
    console.error('提交考试失败:', error)
    ElMessage.error('提交考试失败')
  } finally {
    submitting.value = false
    submitDialogVisible.value = false
  }
}

// 防止页面刷新
window.addEventListener('beforeunload', (e) => {
  e.preventDefault()
  e.returnValue = '考试正在进行中，确定要离开页面吗？'
})
</script>

<style scoped>
.take-exam-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.exam-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 30px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.exam-info h2 {
  margin: 0 0 10px 0;
  color: #333;
}

.exam-info p {
  margin: 0;
  color: #666;
}

.exam-timer {
  text-align: right;
}

.timer-display {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: bold;
  color: #e74c3c;
  margin-bottom: 10px;
}

.exam-progress {
  font-size: 14px;
  color: #666;
}

.exam-content {
  display: flex;
  gap: 30px;
}

.question-nav {
  width: 200px;
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 20px;
  height: fit-content;
}

.nav-title {
  font-weight: bold;
  margin-bottom: 15px;
  color: #333;
}

.nav-items {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 8px;
}

.nav-item {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}

.nav-item:hover {
  background: #f0f0f0;
}

.nav-item.active {
  background: #409EFF;
  color: white;
  border-color: #409EFF;
}

.nav-item.answered {
  background: #67C23A;
  color: white;
  border-color: #67C23A;
}

.question-content {
  flex: 1;
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 30px;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.question-number {
  font-weight: bold;
  color: #409EFF;
}

.question-score {
  background: #f0f9ff;
  color: #0369a1;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.question-title {
  font-size: 16px;
  line-height: 1.6;
  margin-bottom: 25px;
  color: #333;
}

.question-options {
  margin-bottom: 40px;
}

.option-item {
  display: block;
  margin-bottom: 15px;
  padding: 12px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  transition: all 0.2s;
}

.option-item:hover {
  background: #f8f9fa;
  border-color: #409EFF;
}

.question-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.submit-summary {
  padding: 20px 0;
}

.submit-summary ul {
  margin: 15px 0;
  padding-left: 20px;
}

.submit-summary li {
  margin: 8px 0;
}

.warning-text {
  color: #e74c3c;
  font-weight: bold;
  margin-top: 15px;
}
</style>