import request from '@/utils/request'

// 获取聚合数据（旧接口，保留兼容）
export function getAggregateData() {
  return request({
    url: '/api/v1/aggregateQuery',
    method: 'get'
  })
}

// 获取基础统计数据
export function getBasicStats() {
  return request({
    url: '/api/v1/home/basicStats',
    method: 'get',
    timeout: 5000
  })
}

// 获取节点统计信息
export function getNodeStats() {
  return request({
    url: '/api/v1/home/nodeStats',
    method: 'get',
    timeout: 5000
  })
}

// 获取游戏时长排行榜
export function getTopPlayers() {
  return request({
    url: '/api/v1/home/topPlayers',
    method: 'get',
    timeout: 5000
  })
}

// 获取在线玩家信息
export function getOnlinePlayerInfo() {
  return request({
    url: '/api/v1/home/onlinePlayerInfo',
    method: 'get',
    timeout: 10000
  })
} 