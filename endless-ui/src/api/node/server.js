import request from '@/utils/request'

// 查询节点服务器列表
export function listNodeServer(query) {
  return request({
    url: '/node/server/list',
    method: 'get',
    params: query
  })
}

// 查询节点服务器详细
export function getNodeServer(id) {
  return request({
    url: '/node/server/' + id,
    method: 'get'
  })
}

// 新增节点服务器
export function addNodeServer(data) {
  return request({
    url: '/node/server',
    method: 'post',
    data: data
  })
}

// 修改节点服务器
export function updateNodeServer(data) {
  return request({
    url: '/node/server',
    method: 'put',
    data: data
  })
}

// 删除节点服务器
export function delNodeServer(id) {
  return request({
    url: '/node/server/' + id,
    method: 'delete'
  })
}