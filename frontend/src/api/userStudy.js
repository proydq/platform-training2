import request from '@/utils/request'

export function getStudyOverview() {
  return request({ url: '/api/v1/user-study/overview', method: 'GET' })
}

export function getMyCourses() {
  return request({ url: '/api/v1/user-study/courses', method: 'GET' })
}

export function getRecommendations() {
  return request({ url: '/api/v1/user-study/recommendation', method: 'GET' })
}
