import request from '@/utils/request'

// 查询白名单用户在线列表
export function list(query) {
  return request({
    url: '/monitor/whitelist-user/list',
    method: 'get',
    params: query
  })
}

// 获取白名单用户统计
export function getSummary() {
  return request({
    url: '/monitor/whitelist-user/summary',
    method: 'get'
  })
}

// 强退白名单用户
export function forceLogout(token) {
  return request({
    url: '/monitor/whitelist-user/' + token,
    method: 'delete'
  })
}
