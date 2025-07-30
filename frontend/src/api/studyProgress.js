// api/studyProgress.js - 学习进度相关API接口
import request from '@/utils/request'

/**
 * 开始学习课程
 * @param {string} courseId - 课程ID
 */
export function startStudyAPI(courseId) {
  return request({
    url: `/api/v1/study-records/start/${courseId}`,
    method: 'POST'
  })
}

/**
 * 更新学习进度
 * @param {string} recordId - 学习记录ID
 * @param {Object} data - 进度数据
 * @param {number} data.progressPercent - 进度百分比 (0-100)
 * @param {string} data.lastPosition - 最后学习位置
 * @param {number} data.studyTime - 学习时长(分钟)
 */
export function updateStudyProgressAPI(recordId, data) {
  return request({
    url: `/api/v1/study-records/${recordId}`,
    method: 'PUT',
    data: {
      progressPercent: data.progress || data.progressPercent || 0,
      lastPosition: data.lastPosition || '',
      studyDuration: data.studyTime || 0
    }
  })
}

/**
 * 根据课程ID获取学习记录
 * @param {string} courseId - 课程ID
 */
export function getStudyRecordByCourseAPI(courseId) {
  return request({
    url: `/api/v1/study-records/course/${courseId}`,
    method: 'GET'
  })
}

/**
 * 完成学习记录
 * @param {string} recordId - 学习记录ID
 */
export function completeStudyAPI(recordId) {
  return request({
    url: `/api/v1/study-records/${recordId}/complete`,
    method: 'POST'
  })
}

/**
 * 获取学习概览
 */
export function getStudyOverviewAPI() {
  return request({
    url: '/api/v1/study-progress/overview',
    method: 'GET'
  })
}

/**
 * 设置学习目标
 * @param {number} goalDuration - 目标时长(分钟)
 */
export function setStudyGoalAPI(goalDuration) {
  return request({
    url: '/api/v1/study-progress/goal',
    method: 'POST',
    params: { goalDuration }
  })
}

/**
 * 获取学习趋势
 * @param {number} days - 统计天数
 */
export function getStudyTrendAPI(days = 30) {
  return request({
    url: '/api/v1/study-progress/trend',
    method: 'GET',
    params: { days }
  })
}

/**
 * 获取学习日历数据
 * @param {number} year - 年份
 * @param {number} month - 月份
 */
export function getStudyCalendarAPI(year, month) {
  return request({
    url: '/api/v1/study-progress/calendar',
    method: 'GET',
    params: { year, month }
  })
}

/**
 * 获取成就统计
 */
export function getAchievementsAPI() {
  return request({
    url: '/api/v1/study-progress/achievements',
    method: 'GET'
  })
}