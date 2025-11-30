import request from '@/utils/request'

// 查询节点Java多版本环境管理列表
export function listEnv(query) {
  return request({
    url: '/node/env/list',
    method: 'get',
    params: query
  })
}

// 查询节点Java多版本环境管理详细
export function getEnv(id) {
  return request({
    url: '/node/env/' + id,
    method: 'get'
  })
}

// 新增节点Java多版本环境管理
export function addEnv(data) {
  return request({
    url: '/node/env',
    method: 'post',
    data: data
  })
}

// 修改节点Java多版本环境管理
export function updateEnv(data) {
  return request({
    url: '/node/env',
    method: 'put',
    data: data
  })
}

// 删除节点Java多版本环境管理
export function delEnv(id) {
  return request({
    url: '/node/env/' + id,
    method: 'delete'
  })
}

// 验证Java环境
export function verifyEnv(data) {
  return request({
    url: '/node/env/verify',
    method: 'post',
    data: data
  })
}

// 扫描节点Java环境
export function scanEnv(nodeId) {
  return request({
    url: '/node/env/scan/' + nodeId,
    method: 'get'
  })
}

// 一键安装Java环境
export function installJava(data) {
  return request({
    url: '/node/env/install',
    method: 'post',
    data: data,
    timeout: 600000 // 10分钟超时
  })
}
