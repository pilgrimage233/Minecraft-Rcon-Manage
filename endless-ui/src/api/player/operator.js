import request from '@/utils/request'

// 查询管理员名单列表
export function listOperator(query) {
  return request({
    url: '/player/operator/list',
    method: 'get',
    params: query
  })
}

// 查询管理员名单详细
export function getOperator(id) {
  return request({
    url: '/player/operator/' + id,
    method: 'get'
  })
}

// 新增管理员名单
export function addOperator(data) {
  return request({
    url: '/player/operator',
    method: 'post',
    data: data
  })
}

// 修改管理员名单
export function updateOperator(data) {
  return request({
    url: '/player/operator',
    method: 'put',
    data: data
  })
}

// 删除管理员名单
export function delOperator(id) {
  return request({
    url: '/player/operator/' + id,
    method: 'delete'
  })
}
