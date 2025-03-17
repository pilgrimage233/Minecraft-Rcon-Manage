import request from '@/utils/request'

// 查询指令管理列表
export function listCommand(query) {
  return request({
    url: '/mc/command/list',
    method: 'get',
    params: query
  })
}

// 查询指令管理详细
export function getCommand(id) {
  return request({
    url: '/mc/command/' + id,
    method: 'get'
  })
}

// 新增指令管理
export function addCommand(data) {
  return request({
    url: '/mc/command',
    method: 'post',
    data: data
  })
}

// 修改指令管理
export function updateCommand(data) {
  return request({
    url: '/mc/command',
    method: 'put',
    data: data
  })
}

// 删除指令管理
export function delCommand(id) {
  return request({
    url: '/mc/command/' + id,
    method: 'delete'
  })
}
