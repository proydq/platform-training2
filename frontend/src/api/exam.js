// src/api/exam.js
import request from '@/utils/request'

/**
 * 获取考试列表
 */
export const getExamListAPI = () => {
  return request({
    url: '/api/v1/exams',
    method: 'get'
  })
}

/**
 * 获取考试详情
 */
export const getExamDetailsAPI = (examId) => {
  return request({
    url: `/api/v1/exams/${examId}`,
    method: 'get'
  })
}

/**
 * 开始考试
 */
export const startExamAPI = (examId) => {
  return request({
    url: `/api/v1/exams/${examId}/start`,
    method: 'post'
  })
}

/**
 * 提交考试答案
 */
export const submitExamAPI = (examId, data) => {
  return request({
    url: `/api/v1/exams/${examId}/submit`,
    method: 'post',
    data: data
  })
}

/**
 * 获取考试结果
 */
export const getExamResultAPI = (examId) => {
  return request({
    url: `/api/v1/exams/${examId}/result`,
    method: 'get'
  })
}

/**
 * 获取考试状态
 */
export const getExamStatusAPI = (examId) => {
  return request({
    url: `/api/v1/exams/${examId}/status`,
    method: 'get'
  })
}

/**
 * 获取题目列表
 */
export const getQuestionsAPI = (examId) => {
  return request({
    url: `/api/v1/questions/exam/${examId}`,
    method: 'get'
  })
}