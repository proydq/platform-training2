import request from '@/utils/request'

export function getStudyOverview() {
  return request({
    url: '/api/v1/user-study/overview',
    method: 'GET'
  })
}
