import request from '@/utils/request'

// 查询白名单列表
export function listWhitelist(query) {
  return request({
    url: '/mc/whitelist/list',
    method: 'get',
    params: query
  })
}

// 查询白名单详细
export function getWhitelist(id) {
  return request({
    url: '/mc/whitelist/' + id,
    method: 'get'
  })
}

// 新增白名单
export function addWhitelist(data) {
  return request({
    url: '/mc/whitelist',
    method: 'post',
    data: data
  })
}

// 修改白名单
export function updateWhitelist(data) {
  return request({
    url: '/mc/whitelist',
    method: 'put',
    data: data
  })
}

// 删除白名单
export function delWhitelist(id) {
  return request({
    url: '/mc/whitelist/' + id,
    method: 'delete'
  })
}

// 获取服务器列表
export function getServerList() {
  return request({
    url: '/server/serverlist/getServerList',
    method: 'get'
  })
}
