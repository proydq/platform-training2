import request from '@/utils/request'

export function getStudyOverview() {
  return request({
    url: '/user-study/overview',
    method: 'GET'
  })
}
