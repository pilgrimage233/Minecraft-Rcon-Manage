import request from '@/utils/request'

// 手动触发同步所有服务器数据
export function syncAllServerData() {
  return request({
    url: '/quartz/sync/all',
    method: 'post'
  })
}

// 手动触发同步指定服务器数据
export function syncServerData(rconServerId) {
  return request({
    url: `/quartz/sync/server/${rconServerId}`,
    method: 'post'
  })
}