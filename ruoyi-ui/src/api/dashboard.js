import request from '@/utils/request'

// 获取聚合数据
export function getAggregateData() {
  return request({
    url: '/api/v1/aggregateQuery',
    method: 'get'
  })
} 