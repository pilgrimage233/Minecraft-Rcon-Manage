import request from '@/utils/request'

// 查询封禁管理列表
export function listBanlist(query) {
  return request({
    url: '/mc/banlist/list',
    method: 'get',
    params: query
  })
}

// 查询封禁管理详细
export function getBanlist(id) {
  return request({
    url: '/mc/banlist/' + id,
    method: 'get'
  })
}

// 新增封禁管理
export function addBanlist(data) {
  return request({
    url: '/mc/banlist',
    method: 'post',
    data: data
  })
}

// 修改封禁管理
export function updateBanlist(data) {
  return request({
    url: '/mc/banlist',
    method: 'put',
    data: data
  })
}

// 删除封禁管理
export function delBanlist(id) {
  return request({
    url: '/mc/banlist/' + id,
    method: 'delete'
  })
}
