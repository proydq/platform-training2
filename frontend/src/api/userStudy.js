import request from '@/utils/request'

export function getMyCourses() {
  return request({
    url: '/user-study/my-courses',
    method: 'GET'
  })
}
