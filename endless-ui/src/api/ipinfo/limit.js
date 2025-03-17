import request from '@/utils/request'

// 查询IP限流列表
export function listLimit(query) {
  return request({
    url: '/ipinfo/limit/list',
    method: 'get',
    params: query
  })
}

// 查询IP限流详细
export function getLimit(id) {
  return request({
    url: '/ipinfo/limit/' + id,
    method: 'get'
  })
}

// 新增IP限流
export function addLimit(data) {
  return request({
    url: '/ipinfo/limit',
    method: 'post',
    data: data
  })
}

// 修改IP限流
export function updateLimit(data) {
  return request({
    url: '/ipinfo/limit',
    method: 'put',
    data: data
  })
}

// 删除IP限流
export function delLimit(id) {
  return request({
    url: '/ipinfo/limit/' + id,
    method: 'delete'
  })
}
