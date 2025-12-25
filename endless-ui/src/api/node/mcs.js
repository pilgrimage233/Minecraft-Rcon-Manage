import request from '@/utils/request'

// 查询实例管理列表
export function listMcs(query) {
  return request({
    url: '/node/mcs/list',
    method: 'get',
    params: query
  })
}

// 查询实例管理详细
export function getMcs(id) {
  return request({
    url: '/node/mcs/' + id,
    method: 'get'
  })
}

// 新增实例管理
export function addMcs(data) {
  return request({
    url: '/node/mcs',
    method: 'post',
    data: data
  })
}

// 修改实例管理
export function updateMcs(data) {
  return request({
    url: '/node/mcs',
    method: 'put',
    data: data
  })
}

// 删除实例管理
export function delMcs(id) {
  return request({
    url: '/node/mcs/' + id,
    method: 'delete'
  })
}

// 获取服务器在线玩家
export function getServerPlayers(nodeId, serverId) {
  return request({
    url: `/node/mcs/${nodeId}/servers/${serverId}/players`,
    method: 'get'
  })
}

// 对玩家执行操作
export function playerAction(nodeId, serverId, playerName, data) {
  return request({
    url: `/node/mcs/${nodeId}/servers/${serverId}/players/${playerName}/action`,
    method: 'post',
    data: data
  })
}

// Query连接诊断
export function queryDiagnostic(nodeId, serverId) {
  return request({
    url: `/node/mcs/${nodeId}/servers/${serverId}/query-diagnostic`,
    method: 'get'
  })
}
