import request from '@/utils/request'

// 查询题库配置列表
export function listConfig(query) {
  return request({
    url: '/quiz/config/list',
    method: 'get',
    params: query
  })
}

// 查询题库配置详细
export function getConfig(id) {
  return request({
    url: '/quiz/config/' + id,
    method: 'get'
  })
}

// 新增题库配置
export function addConfig(data) {
  return request({
    url: '/quiz/config',
    method: 'post',
    data: data
  })
}

// 修改题库配置
export function updateConfig(data) {
  return request({
    url: '/quiz/config',
    method: 'put',
    data: data
  })
}

// 删除题库配置
export function delConfig(id) {
  return request({
    url: '/quiz/config/' + id,
    method: 'delete'
  })
}
