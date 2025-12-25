import request from '@/utils/request'

// 查询群组指令功能配置列表
export function listCmdconfig(query) {
  return request({
    url: '/bot/cmdconfig/list',
    method: 'get',
    params: query
  })
}

// 查询群组指令功能配置详细
export function getCmdconfig(id) {
  return request({
    url: '/bot/cmdconfig/' + id,
    method: 'get'
  })
}

// 新增群组指令功能配置
export function addCmdconfig(data) {
  return request({
    url: '/bot/cmdconfig',
    method: 'post',
    data: data
  })
}

// 修改群组指令功能配置
export function updateCmdconfig(data) {
  return request({
    url: '/bot/cmdconfig',
    method: 'put',
    data: data
  })
}

// 删除群组指令功能配置
export function delCmdconfig(id) {
  return request({
    url: '/bot/cmdconfig/' + id,
    method: 'delete'
  })
}

// 清除所有指令配置缓存
export function clearCache() {
  return request({
    url: '/bot/cmdconfig/clearCache',
    method: 'delete'
  })
}
