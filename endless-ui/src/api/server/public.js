import request from '@/utils/request'

// 查询公开命令列表
export function listPublic(query) {
  return request({
    url: '/server/public/list',
    method: 'get',
    params: query
  })
}

// 查询公开命令详细
export function getPublic(id) {
  return request({
    url: '/server/public/' + id,
    method: 'get'
  })
}

// 新增公开命令
export function addPublic(data) {
  return request({
    url: '/server/public',
    method: 'post',
    data: data
  })
}

// 修改公开命令
export function updatePublic(data) {
  return request({
    url: '/server/public',
    method: 'put',
    data: data
  })
}

// 删除公开命令
export function delPublic(id) {
  return request({
    url: '/server/public/' + id,
    method: 'delete'
  })
}
