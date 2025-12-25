import request from '@/utils/request'

// 查询自定义邮件通知模板列表
export function listTemplates(query) {
  return request({
    url: '/email/templates/list',
    method: 'get',
    params: query
  })
}

// 查询自定义邮件通知模板详细
export function getTemplates(id) {
  return request({
    url: '/email/templates/' + id,
    method: 'get'
  })
}

// 新增自定义邮件通知模板
export function addTemplates(data) {
  return request({
    url: '/email/templates',
    method: 'post',
    data: data
  })
}

// 修改自定义邮件通知模板
export function updateTemplates(data) {
  return request({
    url: '/email/templates',
    method: 'put',
    data: data
  })
}

// 删除自定义邮件通知模板
export function delTemplates(id) {
  return request({
    url: '/email/templates/' + id,
    method: 'delete'
  })
}

// 下载使用文档
export function downloadDocument() {
  return request({
    url: '/email/templates/downloadDocument',
    method: 'get',
    responseType: 'blob'
  })
}

