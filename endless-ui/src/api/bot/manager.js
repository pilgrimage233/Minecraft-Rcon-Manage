import request from '@/utils/request'

// 查询QQ机器人管理员列表
export function listManager(query) {
  return request({
    url: '/bot/manager/list',
    method: 'get',
    params: query
  })
}

// 查询QQ机器人管理员详细
export function getManager(id) {
  return request({
    url: '/bot/manager/' + id,
    method: 'get'
  })
}

// 新增QQ机器人管理员
export function addManager(data) {
  return request({
    url: '/bot/manager',
    method: 'post',
    data: data
  })
}

// 修改QQ机器人管理员
export function updateManager(data) {
  return request({
    url: '/bot/manager',
    method: 'put',
    data: data
  })
}

// 删除QQ机器人管理员
export function delManager(id) {
  return request({
    url: '/bot/manager/' + id,
    method: 'delete'
  })
}
