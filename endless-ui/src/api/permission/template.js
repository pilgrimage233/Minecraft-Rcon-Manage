import request from '@/utils/request'

// 查询权限模板列表
export function listTemplate(query) {
  return request({
    url: '/permission/template/list',
    method: 'get',
    params: query
  })
}

// 查询权限模板详细
export function getTemplate(id) {
  return request({
    url: '/permission/template/' + id,
    method: 'get'
  })
}

// 新增权限模板
export function addTemplate(data) {
  return request({
    url: '/permission/template',
    method: 'post',
    data: data
  })
}

// 修改权限模板
export function updateTemplate(data) {
  return request({
    url: '/permission/template',
    method: 'put',
    data: data
  })
}

// 删除权限模板
export function delTemplate(id) {
  return request({
    url: '/permission/template/' + id,
    method: 'delete'
  })
}

// 根据资源类型获取模板列表
export function getTemplatesByType(resourceType) {
  return request({
    url: '/permission/template/type/' + resourceType,
    method: 'get'
  })
}
