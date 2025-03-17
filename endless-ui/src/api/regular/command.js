import request from '@/utils/request'

// 查询定时命令列表
export function listCommand(query) {
  return request({
    url: '/regular/command/list',
    method: 'get',
    params: query
  })
}

// 查询定时命令详细
export function getCommand(id) {
  return request({
    url: '/regular/command/' + id,
    method: 'get'
  })
}

// 新增定时命令
export function addCommand(data) {
  return request({
    url: '/regular/command',
    method: 'post',
    data: data
  })
}

// 修改定时命令
export function updateCommand(data) {
  return request({
    url: '/regular/command',
    method: 'put',
    data: data
  })
}

// 删除定时命令
export function delCommand(id) {
  return request({
    url: '/regular/command/' + id,
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

