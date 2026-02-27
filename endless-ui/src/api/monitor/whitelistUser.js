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

// 查询白名单注册用户列表
export function listRegisteredUsers(query) {
  return request({
    url: '/monitor/whitelist-user/registered/list',
    method: 'get',
    params: query
  })
}

// 更新白名单用户角色
export function updateWhitelistUserRole(data) {
  return request({
    url: '/monitor/whitelist-user/role',
    method: 'put',
    data: data
  })
}
