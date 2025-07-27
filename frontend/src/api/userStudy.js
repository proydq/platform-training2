import request from '@/utils/request'

export function getMyCourses() {
  return request({
    url: '/api/v1/user-study/courses',
    method: 'GET'
  })
}
