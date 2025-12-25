import request from '@/utils/request'

// 查询权限操作日志列表
export function listPermissionLog(query) {
  return request({
    url: '/permission/log/list',
    method: 'get',
    params: query
  })
}

// 查询权限操作日志详细
export function getPermissionLog(id) {
  return request({
    url: '/permission/log/' + id,
    method: 'get'
  })
}

// 删除权限操作日志
export function delPermissionLog(id) {
  return request({
    url: '/permission/log/' + id,
    method: 'delete'
  })
}

// 清空权限操作日志
export function cleanPermissionLog() {
  return request({
    url: '/permission/log/clean',
    method: 'delete'
  })
}
