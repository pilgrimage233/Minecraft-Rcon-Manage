import request from '@/utils/request'

// 查询Minecraft实例列表
export function listMinecraftInstance(query) {
  return request({
    url: '/node/mcs/list',
    method: 'get',
    params: query
  })
}

// 根据节点ID查询实例列表
export function listInstanceByNodeId(nodeId) {
  return request({
    url: `/node/mcs/listByNode/${nodeId}`,
    method: 'get'
  })
}

// 查询Minecraft实例详细
export function getMinecraftInstance(id) {
  return request({
    url: '/node/mcs/' + id,
    method: 'get'
  })
}

// 新增Minecraft实例
export function addMinecraftInstance(data) {
  return request({
    url: '/node/mcs',
    method: 'post',
    data: data
  })
}

// 修改Minecraft实例
export function updateMinecraftInstance(data) {
  return request({
    url: '/node/mcs',
    method: 'put',
    data: data
  })
}

// 删除Minecraft实例
export function delMinecraftInstance(id) {
  return request({
    url: '/node/mcs/' + id,
    method: 'delete'
  })
}