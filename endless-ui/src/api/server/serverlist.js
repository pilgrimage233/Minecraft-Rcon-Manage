import request from '@/utils/request'

// 查询服务器信息列表
export function listServerlist(query) {
  return request({
    url: '/server/serverlist/list',
    method: 'get',
    params: query
  })
}

// 查询服务器信息详细
export function getServerlist(id) {
  return request({
    url: '/server/serverlist/' + id,
    method: 'get'
  })
}

// 新增服务器信息
export function addServerlist(data) {
  return request({
    url: '/server/serverlist',
    method: 'post',
    data: data
  })
}

// 修改服务器信息
export function updateServerlist(data) {
  return request({
    url: '/server/serverlist',
    method: 'put',
    data: data
  })
}

// 删除服务器信息
export function delServerlist(id) {
  return request({
    url: '/server/serverlist/' + id,
    method: 'delete'
  })
}

// 刷新缓存
export function refreshCache(id) {
  return request({
    url: '/server/serverlist/refreshCache',
    method: 'get'
  })
}
