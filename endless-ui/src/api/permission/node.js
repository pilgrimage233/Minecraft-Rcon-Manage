import request from '@/utils/request'

// 查询节点服务器权限列表
export function listNodePermission(query) {
  return request({
    url: '/permission/node/list',
    method: 'get',
    params: query
  })
}

// 查询节点服务器权限详细
export function getNodePermission(id) {
  return request({
    url: '/permission/node/' + id,
    method: 'get'
  })
}

// 新增节点服务器权限
export function addNodePermission(data) {
  return request({
    url: '/permission/node',
    method: 'post',
    data: data
  })
}

// 修改节点服务器权限
export function updateNodePermission(data) {
  return request({
    url: '/permission/node',
    method: 'put',
    data: data
  })
}

// 删除节点服务器权限
export function delNodePermission(id) {
  return request({
    url: '/permission/node/' + id,
    method: 'delete'
  })
}

// 获取用户可访问的节点服务器ID列表
export function getUserNodeServers(userId) {
  return request({
    url: '/permission/node/user/' + userId,
    method: 'get'
  })
}
