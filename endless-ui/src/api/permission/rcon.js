import request from '@/utils/request'

// 查询RCON服务器权限列表
export function listRconPermission(query) {
  return request({
    url: '/permission/rcon/list',
    method: 'get',
    params: query
  })
}

// 查询RCON服务器权限详细
export function getRconPermission(id) {
  return request({
    url: '/permission/rcon/' + id,
    method: 'get'
  })
}

// 新增RCON服务器权限
export function addRconPermission(data) {
  return request({
    url: '/permission/rcon',
    method: 'post',
    data: data
  })
}

// 修改RCON服务器权限
export function updateRconPermission(data) {
  return request({
    url: '/permission/rcon',
    method: 'put',
    data: data
  })
}

// 删除RCON服务器权限
export function delRconPermission(id) {
  return request({
    url: '/permission/rcon/' + id,
    method: 'delete'
  })
}

// 获取用户可访问的RCON服务器ID列表
export function getUserRconServers(userId) {
  return request({
    url: '/permission/rcon/user/' + userId,
    method: 'get'
  })
}

// 检查用户RCON权限
export function checkRconPermission(serverId, permission) {
  return request({
    url: '/permission/rcon/check',
    method: 'get',
    params: {
      serverId: serverId,
      permission: permission
    }
  })
}

// 检查命令是否允许执行
export function checkCommandPermission(serverId, command) {
  return request({
    url: '/permission/rcon/checkCommand',
    method: 'post',
    data: {
      serverId: serverId,
      command: command
    }
  })
}
