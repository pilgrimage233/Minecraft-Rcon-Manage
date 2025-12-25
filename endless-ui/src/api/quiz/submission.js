import request from '@/utils/request'

// 查询答题记录列表
export function listSubmission(query) {
  return request({
    url: '/quiz/submission/list',
    method: 'get',
    params: query
  })
}

// 查询答题记录详细
export function getSubmission(id) {
  return request({
    url: '/quiz/submission/' + id,
    method: 'get'
  })
}

// 新增答题记录
export function addSubmission(data) {
  return request({
    url: '/quiz/submission',
    method: 'post',
    data: data
  })
}

// 修改答题记录
export function updateSubmission(data) {
  return request({
    url: '/quiz/submission',
    method: 'put',
    data: data
  })
}

// 删除答题记录
export function delSubmission(id) {
  return request({
    url: '/quiz/submission/' + id,
    method: 'delete'
  })
}
