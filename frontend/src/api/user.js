import request from '@/utils/request'

export function getUsersAPI(params = {}) {
  return request({
    url: '/api/users',
    method: 'GET',
    params: {
      page: params.page || 1,
      pageSize: params.pageSize || 10,
      role: params.role || '',
      keyword: params.keyword || ''
    }
  })
}

export function createUserAPI(data) {
  return request({
    url: '/api/users',
    method: 'POST',
    data
  })
}

export function updateUserAPI(id, data) {
  return request({
    url: `/api/users/${id}`,
    method: 'PUT',
    data
  })
}

export function deleteUserAPI(id) {
  return request({
    url: `/api/users/${id}`,
    method: 'DELETE'
  })
}

export function resetUserPasswordAPI(id) {
  return request({
    url: `/api/users/${id}/reset-password`,
    method: 'POST'
  })
}

export function getRoleOptionsAPI() {
  return request({
    url: '/api/roles',
    method: 'GET'
  })
}
