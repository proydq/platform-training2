import request from '@/utils/request'

export function getMyCourses() {
  return request({
    url: '/api/v1/user-study/my-courses',
    method: 'GET'
  })
}
