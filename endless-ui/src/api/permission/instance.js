import request from '@/utils/request'

// 查询MC实例权限列表
export function listInstancePermission(query) {
  return request({
    url: '/permission/instance/list',
    method: 'get',
    params: query
  })
}

// 查询MC实例权限详细
export function getInstancePermission(id) {
  return request({
    url: '/permission/instance/' + id,
    method: 'get'
  })
}

// 新增MC实例权限
export function addInstancePermission(data) {
  return request({
    url: '/permission/instance',
    method: 'post',
    data: data
  })
}

// 修改MC实例权限
export function updateInstancePermission(data) {
  return request({
    url: '/permission/instance',
    method: 'put',
    data: data
  })
}

// 删除MC实例权限
export function delInstancePermission(id) {
  return request({
    url: '/permission/instance/' + id,
    method: 'delete'
  })
}

// 获取用户可访问的MC实例ID列表
export function getUserInstances(userId) {
  return request({
    url: '/permission/instance/user/' + userId,
    method: 'get'
  })
}

// 检查用户对实例的特定权限
export function checkInstancePermission(userId, instanceId, permission) {
  return request({
    url: '/permission/instance/check',
    method: 'get',
    params: {userId, instanceId, permission}
  })
}
