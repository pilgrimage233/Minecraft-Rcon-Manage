import request from '@/utils/request'

// 查询机器人日志列表
export function listLog(query) {
  return request({
    url: '/bot/log/list',
    method: 'get',
    params: query
  })
}

// 查询机器人日志详细
export function getLog(id) {
  return request({
    url: '/bot/log/' + id,
    method: 'get'
  })
}

// 新增机器人日志
export function addLog(data) {
  return request({
    url: '/bot/log',
    method: 'post',
    data: data
  })
}

// 修改机器人日志
export function updateLog(data) {
  return request({
    url: '/bot/log',
    method: 'put',
    data: data
  })
}

// 删除机器人日志
export function delLog(id) {
  return request({
    url: '/bot/log/' + id,
    method: 'delete'
  })
}
