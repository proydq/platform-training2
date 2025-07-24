<template>
  <div class="quiz-section">
    <!-- 测验状态检查 -->
    <div v-if="!quizData || quizData.length === 0" class="no-quiz">
      <div class="no-quiz-icon">🧠</div>
      <h3 class="no-quiz-title">暂无测验内容</h3>
      <p class="no-quiz-description">本课程暂时没有配置测验题目</p>
    </div>

    <!-- 测验内容 -->
    <div v-else class="quiz-content">
      <!-- 测验头部 -->
      <div class="quiz-header">
        <div class="quiz-progress-info">
          <h2 class="quiz-title">📝 课堂测验</h2>
          <div class="progress-text">
            第 {{ currentQuestionIndex + 1 }} 题 / 共 {{ quizData.length }} 题
          </div>
        </div>

        <!-- 进度条 -->
        <div class="progress-bar">
          <div
            class="progress-fill"
            :style="{ width: `${(currentQuestionIndex / quizData.length) * 100}%` }"
          ></div>
        </div>

        <!-- 剩余时间 -->
        <div class="time-remaining">
          <span class="time-icon">⏰</span>
          <span class="time-text">{{ formatTime(remainingTime) }}</span>
        </div>
      </div>

      <!-- 当前题目 -->
      <div class="question-container">
        <div class="question-header">
          <div class="question-type">
            {{ currentQuestion.type === 'single' ? '单选题' : '多选题' }}
          </div>
          <div class="question-score">{{ currentQuestion.score || 10 }} 分</div>
        </div>

        <div class="question-content">
          <h3 class="question-text">{{ currentQuestion.question }}</h3>

          <!-- 题目图片（如果有） -->
          <div v-if="currentQuestion.image" class="question-image">
            <img :src="currentQuestion.image" :alt="currentQuestion.question" />
          </div>
        </div>

        <!-- 选项列表 -->
        <div class="options-list">
          <div
            v-for="(option, index) in currentQuestion.options"
            :key="index"
            class="option-item"
            :class="{
              'selected': isSelected(index),
              'correct': showAnswer && option.correct,
              'incorrect': showAnswer && isSelected(index) && !option.correct
            }"
            @click="selectOption(index)"
          >
            <div class="option-indicator">
              <span class="option-letter">{{ String.fromCharCode(65 + index) }}</span>
              <div class="option-check" v-if="isSelected(index)">✓</div>
            </div>
            <div class="option-content">
              <span class="option-text">{{ option.text }}</span>
              <div v-if="showAnswer && option.explanation" class="option-explanation">
                💡 {{ option.explanation }}
              </div>
            </div>
          </div>
        </div>

        <!-- 答案解析 -->
        <div v-if="showAnswer && currentQuestion.explanation" class="answer-explanation">
          <div class="explanation-header">
            <span class="explanation-icon">💡</span>
            <span class="explanation-title">答案解析</span>
          </div>
          <p class="explanation-content">{{ currentQuestion.explanation }}</p>
        </div>
      </div>

      <!-- 测验控制按钮 -->
      <div class="quiz-controls">
        <div class="control-left">
          <button
            @click="$emit('previousQuestion')"
            :disabled="currentQuestionIndex === 0"
            class="control-btn secondary-btn"
          >
            ← 上一题
          </button>
        </div>

        <div class="control-center">
          <button @click="toggleHint" class="hint-btn" :class="{ active: showHint }">
            💡 {{ showHint ? '隐藏提示' : '显示提示' }}
          </button>

          <button @click="$emit('skipQuestion')" class="skip-btn">
            ⏭️ 跳过
          </button>
        </div>

        <div class="control-right">
          <button
            @click="nextQuestion"
            class="control-btn primary-btn"
          >
            {{ isLastQuestion ? '完成测验' : '下一题' }} →
          </button>
        </div>
      </div>

      <!-- 提示信息 -->
      <div v-if="showHint && currentQuestion.hint" class="hint-section">
        <div class="hint-header">
          <span class="hint-icon">💡</span>
          <span class="hint-title">提示</span>
        </div>
        <p class="hint-content">{{ currentQuestion.hint }}</p>
      </div>

      <!-- 测验完成页面 -->
      <div v-if="showResults" class="quiz-results">
        <div class="results-header">
          <div class="results-icon">🎉</div>
          <h2 class="results-title">测验完成！</h2>
        </div>

        <div class="results-stats">
          <div class="stat-card">
            <div class="stat-value">{{ correctAnswers }}</div>
            <div class="stat-label">正确答案</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ Math.round(accuracy) }}%</div>
            <div class="stat-label">正确率</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ totalScore }}</div>
            <div class="stat-label">总分</div>
          </div>
        </div>

        <div class="results-actions">
          <button @click="reviewAnswers" class="review-btn">
            📝 查看答案
          </button>
          <button @click="retakeQuiz" class="retake-btn">
            🔄 重新测验
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  quizData: {
    type: Array,
    required: true
  },
  currentQuestionIndex: {
    type: Number,
    required: true
  },
  selectedAnswer: {
    type: Number,
    default: null
  },
  remainingTime: {
    type: Number,
    required: true
  }
})

const emit = defineEmits([
  'selectOption',
  'previousQuestion',
  'nextQuestion',
  'skipQuestion'
])

// 本地状态
const showHint = ref(false)
const showAnswer = ref(false)
const showResults = ref(false)
const userAnswers = ref([])

// 当前题目
const currentQuestion = computed(() => {
  return props.quizData[props.currentQuestionIndex] || {}
})

// 是否为最后一题
const isLastQuestion = computed(() => {
  return props.currentQuestionIndex === props.quizData.length - 1
})

// 检查选项是否被选中
const isSelected = (optionIndex) => {
  return props.selectedAnswer === optionIndex
}

// 选择选项
const selectOption = (optionIndex) => {
  if (showAnswer.value) return
  emit('selectOption', optionIndex)
}

// 切换提示显示
const toggleHint = () => {
  showHint.value = !showHint.value
}

// 下一题
const nextQuestion = () => {
  if (isLastQuestion.value) {
    showResults.value = true
  } else {
    emit('nextQuestion')
    showHint.value = false
    showAnswer.value = false
  }
}

// 格式化时间
const formatTime = (seconds) => {
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = Math.floor(seconds % 60)
  return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`
}

// 测验结果相关计算
const correctAnswers = computed(() => {
  // 这里应该根据实际的答案数据来计算
  return Math.floor(props.quizData.length * 0.8) // 示例：80%正确率
})

const accuracy = computed(() => {
  return (correctAnswers.value / props.quizData.length) * 100
})

const totalScore = computed(() => {
  return correctAnswers.value * 10 // 假设每题10分
})

// 查看答案
const reviewAnswers = () => {
  showResults.value = false
  showAnswer.value = true
}

// 重新测验
const retakeQuiz = () => {
  showResults.value = false
  showAnswer.value = false
  showHint.value = false
  // 重置到第一题
  emit('selectOption', null)
}
</script>

<style scoped>
.quiz-section {
  color: #333;
  height: 100%;
  display: flex;
  flex-direction: column;
}

/* 无测验状态 */
.no-quiz {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 300px;
  text-align: center;
}

.no-quiz-icon {
  font-size: 64px;
  margin-bottom: 20px;
  opacity: 0.5;
}

.no-quiz-title {
  font-size: 20px;
  color: #666;
  margin: 0 0 10px 0;
}

.no-quiz-description {
  font-size: 14px;
  color: #999;
  margin: 0;
}

/* 测验内容 */
.quiz-content {
  height: 100%;
  display: flex;
  flex-direction: column;
}

/* 测验头部 */
.quiz-header {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  padding: 25px;
  border-radius: 15px;
  margin-bottom: 25px;
}

.quiz-progress-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.quiz-title {
  font-size: 24px;
  font-weight: 600;
  margin: 0;
}

.progress-text {
  font-size: 14px;
  opacity: 0.9;
}

.progress-bar {
  width: 100%;
  height: 8px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 15px;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #28a745, #20c997);
  border-radius: 4px;
  transition: width 0.3s ease;
}

.time-remaining {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 500;
}

/* 题目容器 */
.question-container {
  flex: 1;
  background: white;
  border-radius: 15px;
  padding: 25px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.question-type {
  background: #e3f2fd;
  color: #1976d2;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.question-score {
  color: #667eea;
  font-weight: 600;
  font-size: 14px;
}

.question-content {
  margin-bottom: 25px;
}

.question-text {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  line-height: 1.6;
  margin: 0 0 15px 0;
}

.question-image {
  text-align: center;
  margin: 15px 0;
}

.question-image img {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

/* 选项列表 */
.options-list {
  margin-bottom: 25px;
}

.option-item {
  display: flex;
  align-items: flex-start;
  background: #f8f9fa;
  border: 2px solid #e9ecef;
  border-radius: 12px;
  padding: 15px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.option-item:hover {
  background: #e3f2fd;
  border-color: #667eea;
}

.option-item.selected {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-color: #667eea;
  color: white;
}

.option-item.correct {
  background: linear-gradient(135deg, #28a745, #20c997);
  border-color: #28a745;
  color: white;
}

.option-item.incorrect {
  background: linear-gradient(135deg, #dc3545, #fd7e14);
  border-color: #dc3545;
  color: white;
}

.option-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 35px;
  height: 35px;
  background: white;
  border-radius: 50%;
  margin-right: 15px;
  flex-shrink: 0;
  position: relative;
  font-weight: 600;
  color: #667eea;
}

.option-item.selected .option-indicator,
.option-item.correct .option-indicator,
.option-item.incorrect .option-indicator {
  background: rgba(255, 255, 255, 0.2);
  color: white;
}

.option-check {
  position: absolute;
  top: -5px;
  right: -5px;
  width: 20px;
  height: 20px;
  background: #28a745;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: white;
}

.option-content {
  flex: 1;
}

.option-text {
  font-size: 15px;
  line-height: 1.5;
}

.option-explanation {
  margin-top: 10px;
  padding: 10px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 6px;
  font-size: 13px;
  opacity: 0.9;
}

/* 答案解析 */
.answer-explanation {
  background: #fff3cd;
  border: 1px solid #ffeaa7;
  border-radius: 10px;
  padding: 20px;
  margin-bottom: 20px;
}

.explanation-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.explanation-title {
  font-weight: 600;
  color: #856404;
}

.explanation-content {
  color: #856404;
  line-height: 1.6;
  margin: 0;
}

/* 测验控制 */
.quiz-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  gap: 15px;
}

.control-left,
.control-right {
  flex: 1;
}

.control-center {
  display: flex;
  gap: 10px;
}

.control-right {
  text-align: right;
}

.control-btn {
  padding: 12px 24px;
  border-radius: 25px;
  border: none;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
  font-size: 14px;
}

.primary-btn {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.primary-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.secondary-btn {
  background: #f8f9fa;
  color: #666;
  border: 1px solid #e9ecef;
}

.secondary-btn:hover {
  background: #e9ecef;
}

.secondary-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.hint-btn,
.skip-btn {
  padding: 8px 16px;
  border-radius: 20px;
  border: 1px solid #e9ecef;
  background: white;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.3s ease;
}

.hint-btn.active {
  background: #667eea;
  color: white;
  border-color: #667eea;
}

/* 提示区域 */
.hint-section {
  background: #e8f4fd;
  border: 1px solid #b8daff;
  border-radius: 10px;
  padding: 20px;
  margin-bottom: 20px;
}

.hint-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.hint-title {
  font-weight: 600;
  color: #0c5460;
}

.hint-content {
  color: #0c5460;
  line-height: 1.6;
  margin: 0;
}

/* 测验结果 */
.quiz-results {
  text-align: center;
  padding: 40px 20px;
}

.results-header {
  margin-bottom: 30px;
}

.results-icon {
  font-size: 64px;
  margin-bottom: 15px;
}

.results-title {
  font-size: 28px;
  color: #2c3e50;
  margin: 0;
}

.results-stats {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: white;
  border: 1px solid #e9ecef;
  border-radius: 15px;
  padding: 25px;
  min-width: 120px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.stat-value {
  font-size: 32px;
  font-weight: 600;
  color: #667eea;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.results-actions {
  display: flex;
  justify-content: center;
  gap: 15px;
}

.review-btn,
.retake-btn {
  padding: 12px 24px;
  border-radius: 25px;
  border: none;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
}

.review-btn {
  background: #28a745;
  color: white;
}

.retake-btn {
  background: #667eea;
  color: white;
}

.review-btn:hover,
.retake-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.2);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .quiz-header {
    padding: 20px;
  }

  .quiz-progress-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .question-container {
    padding: 20px;
  }

  .quiz-controls {
    flex-direction: column;
    gap: 15px;
  }

  .control-center {
    order: -1;
    justify-content: center;
  }

  .results-stats {
    flex-direction: column;
    align-items: center;
  }

  .results-actions {
    flex-direction: column;
    align-items: center;
  }
}
</style>
