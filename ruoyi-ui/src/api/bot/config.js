import request from '@/utils/request'

// 查询QQ机器人配置列表
export function listConfig(query) {
  return request({
    url: '/bot/config/list',
    method: 'get',
    params: query
  })
}

// 查询QQ机器人配置详细
export function getConfig(id) {
  return request({
    url: '/bot/config/' + id,
    method: 'get'
  })
}

// 新增QQ机器人配置
export function addConfig(data) {
  return request({
    url: '/bot/config',
    method: 'post',
    data: data
  })
}

// 修改QQ机器人配置
export function updateConfig(data) {
  return request({
    url: '/bot/config',
    method: 'put',
    data: data
  })
}

// 删除QQ机器人配置
export function delConfig(id) {
  return request({
    url: '/bot/config/' + id,
    method: 'delete'
  })
}
