import request from '@/utils/request'

// 查询RCON和节点实例关联列表
export function listRelation(query) {
  return request({
    url: '/server/relation/list',
    method: 'get',
    params: query
  })
}

// 根据RCON服务器ID查询关联的实例
export function getByRconServer(rconServerId) {
  return request({
    url: `/server/relation/getByRconServer/${rconServerId}`,
    method: 'get'
  })
}

// 根据实例ID查询关联的RCON服务器
export function getByInstance(instanceId) {
  return request({
    url: `/server/relation/getByInstance/${instanceId}`,
    method: 'get'
  })
}

// 查询RCON和节点实例关联详细
export function getRelation(id) {
  return request({
    url: '/server/relation/' + id,
    method: 'get'
  })
}

// 新增RCON和节点实例关联
export function addRelation(data) {
  return request({
    url: '/server/relation',
    method: 'post',
    data: data
  })
}

// 修改RCON和节点实例关联
export function updateRelation(data) {
  return request({
    url: '/server/relation',
    method: 'put',
    data: data
  })
}

// 删除RCON和节点实例关联
export function delRelation(id) {
  return request({
    url: '/server/relation/' + id,
    method: 'delete'
  })
}

// 导出RCON和节点实例关联
export function exportRelation(query) {
  return request({
    url: '/server/relation/export',
    method: 'post',
    data: query
  })
}