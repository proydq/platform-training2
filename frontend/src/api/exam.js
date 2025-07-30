// frontend/src/api/exam.js - 考试相关API接口
import request from '@/utils/request'

/**
 * ==================== 考试管理API ====================
 */

// 获取考试列表（分页）
export function getExamListAPI(params = {}) {
  return request({
    url: '/exams',
    method: 'GET',
    params: {
      page: params.page || 0,
      size: params.size || 20,
      keyword: params.keyword || '',
      status: params.status || '',
      category: params.category || '',
      difficultyLevel: params.difficultyLevel || '',
      sortBy: params.sortBy || 'createTime',
      sortDir: params.sortDir || 'desc'
    }
  })
}

// 获取我的考试列表
export function getMyExamsAPI(params = {}) {
  return request({
    url: '/exams/my',
    method: 'GET',
    params: {
      page: params.page || 0,
      size: params.size || 20,
      status: params.status || '',
      sortBy: params.sortBy || 'createTime',
      sortDir: params.sortDir || 'desc'
    }
  })
}

// 获取考试详情
export function getExamDetailAPI(examId) {
  return request({
    url: `/api/v1/exams/${examId}`,
    method: 'GET'
  })
}

// 获取考试状态（用户是否已参加）
export function getExamStatusAPI(examId) {
  return request({
    url: `/api/v1/exams/${examId}/status`,
    method: 'GET'
  })
}

// 创建考试
export function createExamAPI(data) {
  return request({
    url: '/exams',
    method: 'POST',
    data: {
      title: data.title,
      description: data.description,
      category: data.category,
      difficultyLevel: data.difficultyLevel || 1,
      duration: data.duration,
      questionCount: data.questionCount,
      passScore: data.passScore || 60,
      maxAttempts: data.maxAttempts || 1,
      startTime: data.startTime,
      endTime: data.endTime,
      instructions: data.instructions || '',
      isPublished: data.isPublished || false,
      allowReview: data.allowReview || true,
      randomizeQuestions: data.randomizeQuestions || false,
      showResults: data.showResults || true
    }
  })
}

// 更新考试
export function updateExamAPI(examId, data) {
  return request({
    url: `/api/v1/exams/${examId}`,
    method: 'PUT',
    data: {
      title: data.title,
      description: data.description,
      category: data.category,
      difficultyLevel: data.difficultyLevel,
      duration: data.duration,
      questionCount: data.questionCount,
      passScore: data.passScore,
      maxAttempts: data.maxAttempts,
      startTime: data.startTime,
      endTime: data.endTime,
      instructions: data.instructions,
      isPublished: data.isPublished,
      allowReview: data.allowReview,
      randomizeQuestions: data.randomizeQuestions,
      showResults: data.showResults
    }
  })
}

// 删除考试
export function deleteExamAPI(examId) {
  return request({
    url: `/api/v1/exams/${examId}`,
    method: 'DELETE'
  })
}

// 发布考试
export function publishExamAPI(examId) {
  return request({
    url: `/api/v1/exams/${examId}/publish`,
    method: 'POST'
  })
}

// 取消发布考试
export function unpublishExamAPI(examId) {
  return request({
    url: `/api/v1/exams/${examId}/unpublish`,
    method: 'POST'
  })
}

/**
 * ==================== 考试参与API ====================
 */

// 开始考试（获取考试题目）
export function startExamAPI(examId) {
  return request({
    url: `/api/v1/exams/${examId}/start`,
    method: 'POST'
  })
}

// 提交考试答案
export function submitExamAPI(examId, data) {
  return request({
    url: `/api/v1/exams/${examId}/submit`,
    method: 'POST',
    data: {
      examId: examId,
      answers: data.answers,
      duration: data.duration || 0,
      submitTime: data.submitTime || Date.now()
    }
  })
}

// 保存考试答案（草稿）
export function saveExamAnswersAPI(examId, data) {
  return request({
    url: `/api/v1/exams/${examId}/save`,
    method: 'POST',
    data: {
      answers: data.answers,
      currentQuestionIndex: data.currentQuestionIndex || 0
    }
  })
}

// 获取考试进行中的状态
export function getExamSessionAPI(examId) {
  return request({
    url: `/api/v1/exams/${examId}/session`,
    method: 'GET'
  })
}

/**
 * ==================== 考试结果API ====================
 */

// 获取考试结果
export function getExamResultAPI(examId) {
  return request({
    url: `/api/v1/exams/${examId}/result`,
    method: 'GET'
  })
}

// 获取考试结果详情（包含答题详情）
export function getExamResultDetailAPI(examId, resultId) {
  return request({
    url: `/api/v1/exams/${examId}/results/${resultId}`,
    method: 'GET'
  })
}

// 获取用户的所有考试结果
export function getUserExamResultsAPI(params = {}) {
  return request({
    url: '/exam-results/my',
    method: 'GET',
    params: {
      page: params.page || 0,
      size: params.size || 20,
      examId: params.examId || '',
      passed: params.passed || '',
      sortBy: params.sortBy || 'createTime',
      sortDir: params.sortDir || 'desc'
    }
  })
}

/**
 * ==================== 题目管理API ====================
 */

// 获取考试题目列表
export function getExamQuestionsAPI(examId, params = {}) {
  return request({
    url: `/api/v1/exams/${examId}/questions`,
    method: 'GET',
    params: {
      page: params.page || 0,
      size: params.size || 50,
      questionType: params.questionType || '',
      difficulty: params.difficulty || ''
    }
  })
}

// 获取题目详情
export function getQuestionDetailAPI(questionId) {
  return request({
    url: `/api/v1/questions/${questionId}`,
    method: 'GET'
  })
}

// 创建题目
export function createQuestionAPI(examId, data) {
  return request({
    url: `/api/v1/exams/${examId}/questions`,
    method: 'POST',
    data: {
      questionType: data.questionType,
      questionText: data.questionText,
      options: data.options || [],
      correctAnswer: data.correctAnswer,
      explanation: data.explanation || '',
      points: data.points || 1,
      difficulty: data.difficulty || 1,
      tags: data.tags || '',
      order: data.order || 1
    }
  })
}

// 更新题目
export function updateQuestionAPI(questionId, data) {
  return request({
    url: `/api/v1/questions/${questionId}`,
    method: 'PUT',
    data: {
      questionType: data.questionType,
      questionText: data.questionText,
      options: data.options,
      correctAnswer: data.correctAnswer,
      explanation: data.explanation,
      points: data.points,
      difficulty: data.difficulty,
      tags: data.tags,
      order: data.order
    }
  })
}

// 删除题目
export function deleteQuestionAPI(questionId) {
  return request({
    url: `/api/v1/questions/${questionId}`,
    method: 'DELETE'
  })
}

// 批量导入题目
export function importQuestionsAPI(examId, file) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('examId', examId)

  return request({
    url: `/api/v1/exams/${examId}/questions/import`,
    method: 'POST',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 导出题目
export function exportQuestionsAPI(examId) {
  return request({
    url: `/api/v1/exams/${examId}/questions/export`,
    method: 'GET',
    responseType: 'blob'
  })
}

/**
 * ==================== 统计分析API ====================
 */

// 获取考试统计信息
export function getExamStatisticsAPI(examId) {
  return request({
    url: `/api/v1/exams/${examId}/statistics`,
    method: 'GET'
  })
}

// 获取用户考试统计
export function getUserExamStatisticsAPI(userId) {
  return request({
    url: `/api/v1/users/${userId}/exam-statistics`,
    method: 'GET'
  })
}

// 获取考试排行榜
export function getExamLeaderboardAPI(examId, params = {}) {
  return request({
    url: `/api/v1/exams/${examId}/leaderboard`,
    method: 'GET',
    params: {
      page: params.page || 0,
      size: params.size || 50
    }
  })
}

/**
 * ==================== 错题集API ====================
 */

// 获取用户错题集
export function getWrongQuestionsAPI(params = {}) {
  return request({
    url: '/wrong-questions/my',
    method: 'GET',
    params: {
      page: params.page || 0,
      size: params.size || 20,
      examId: params.examId || '',
      questionType: params.questionType || '',
      mastered: params.mastered || ''
    }
  })
}

// 标记错题为已掌握
export function markQuestionMasteredAPI(questionId) {
  return request({
    url: `/api/v1/wrong-questions/${questionId}/master`,
    method: 'POST'
  })
}

// 练习错题
export function practiceWrongQuestionsAPI(params = {}) {
  return request({
    url: '/wrong-questions/practice',
    method: 'POST',
    data: {
      examId: params.examId || '',
      questionCount: params.questionCount || 10,
      questionType: params.questionType || '',
      onlyUnmastered: params.onlyUnmastered || true
    }
  })
}

/**
 * ==================== 模拟考试API ====================
 */

// 创建模拟考试
export function createMockExamAPI(data) {
  return request({
    url: '/mock-exams',
    method: 'POST',
    data: {
      title: data.title,
      description: data.description,
      sourceExamId: data.sourceExamId,
      questionCount: data.questionCount,
      duration: data.duration,
      category: data.category
    }
  })
}

// 获取模拟考试列表
export function getMockExamsAPI(params = {}) {
  return request({
    url: '/mock-exams',
    method: 'GET',
    params: {
      page: params.page || 0,
      size: params.size || 20,
      category: params.category || ''
    }
  })
}

/**
 * ==================== 搜索和筛选API ====================
 */

// 搜索考试
export function searchExamsAPI(params) {
  return request({
    url: '/exams/search',
    method: 'GET',
    params
  })
}

// 获取热门考试
export function getPopularExamsAPI(page = 0, size = 10) {
  return request({
    url: '/exams/popular',
    method: 'GET',
    params: { page, size }
  })
}

// 获取推荐考试
export function getRecommendedExamsAPI(page = 0, size = 10) {
  return request({
    url: '/exams/recommended',
    method: 'GET',
    params: { page, size }
  })
}

/**
 * ==================== 考试分类API ====================
 */

// 获取考试分类列表
export function getExamCategoriesAPI() {
  return request({
    url: '/exam-categories',
    method: 'GET'
  })
}

// 根据分类获取考试
export function getExamsByCategoryAPI(category, params = {}) {
  return request({
    url: `/api/v1/exam-categories/${category}/exams`,
    method: 'GET',
    params: {
      page: params.page || 0,
      size: params.size || 20,
      sortBy: params.sortBy || 'createTime',
      sortDir: params.sortDir || 'desc'
    }
  })
}

/**
 * ==================== 工具函数 ====================
 */

// 格式化考试状态
export function formatExamStatus(status) {
  const statusMap = {
    'DRAFT': '草稿',
    'PUBLISHED': '已发布',
    'STARTED': '进行中',
    'ENDED': '已结束',
    'CANCELLED': '已取消'
  }
  return statusMap[status] || status
}

// 格式化考试难度
export function formatExamDifficulty(level) {
  const levelMap = {
    1: '初级',
    2: '中级',
    3: '高级',
    4: '专家'
  }
  return levelMap[level] || '未知'
}

// 格式化题目类型
export function formatQuestionType(type) {
  const typeMap = {
    'SINGLE_CHOICE': '单选题',
    'MULTIPLE_CHOICE': '多选题',
    'TRUE_FALSE': '判断题',
    'FILL_BLANK': '填空题',
    'SHORT_ANSWER': '简答题',
    'ESSAY': '论述题'
  }
  return typeMap[type] || type
}

// 计算考试剩余时间
export function calculateRemainingTime(endTime) {
  const now = new Date().getTime()
  const end = new Date(endTime).getTime()
  const remaining = end - now

  if (remaining <= 0) {
    return { expired: true, text: '已结束' }
  }

  const days = Math.floor(remaining / (1000 * 60 * 60 * 24))
  const hours = Math.floor((remaining % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))
  const minutes = Math.floor((remaining % (1000 * 60 * 60)) / (1000 * 60))

  if (days > 0) {
    return { expired: false, text: `${days}天${hours}小时` }
  } else if (hours > 0) {
    return { expired: false, text: `${hours}小时${minutes}分钟` }
  } else {
    return { expired: false, text: `${minutes}分钟` }
  }
}
