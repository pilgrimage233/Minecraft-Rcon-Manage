import request from '@/utils/request'

// 查询时限管理列表
export function listDeadline(query) {
  return request({
    url: '/mc/deadline/list',
    method: 'get',
    params: query
  })
}

// 查询时限管理详细
export function getDeadline(id) {
  return request({
    url: '/mc/deadline/' + id,
    method: 'get'
  })
}

// 新增时限管理
export function addDeadline(data) {
  return request({
    url: '/mc/deadline',
    method: 'post',
    data: data
  })
}

// 修改时限管理
export function updateDeadline(data) {
  return request({
    url: '/mc/deadline',
    method: 'put',
    data: data
  })
}

// 删除时限管理
export function delDeadline(id) {
  return request({
    url: '/mc/deadline/' + id,
    method: 'delete'
  })
}
