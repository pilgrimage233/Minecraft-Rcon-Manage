import request from '@/utils/request'

// 查询玩家详情列表
export function listDetails(query) {
  return request({
    url: '/player/details/list',
    method: 'get',
    params: query
  })
}

// 查询玩家详情详细
export function getDetails(id) {
  return request({
    url: '/player/details/' + id,
    method: 'get'
  })
}

// 新增玩家详情
export function addDetails(data) {
  return request({
    url: '/player/details',
    method: 'post',
    data: data
  })
}

// 修改玩家详情
export function updateDetails(data) {
  return request({
    url: '/player/details',
    method: 'put',
    data: data
  })
}

// 删除玩家详情
export function delDetails(id) {
  return request({
    url: '/player/details/' + id,
    method: 'delete'
  })
}
