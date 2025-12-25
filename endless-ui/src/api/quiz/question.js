import request from '@/utils/request'

// 查询白名单申请题库问题列表
export function listQuestion(query) {
  return request({
    url: '/quiz/question/list',
    method: 'get',
    params: query
  })
}

// 查询白名单申请题库问题详细
export function getQuestion(id) {
  return request({
    url: '/quiz/question/' + id,
    method: 'get'
  })
}

// 新增白名单申请题库问题
export function addQuestion(data) {
  return request({
    url: '/quiz/question',
    method: 'post',
    data: data
  })
}

// 修改白名单申请题库问题
export function updateQuestion(data) {
  return request({
    url: '/quiz/question',
    method: 'put',
    data: data
  })
}

// 删除白名单申请题库问题
export function delQuestion(id) {
  return request({
    url: '/quiz/question/' + id,
    method: 'delete'
  })
}
