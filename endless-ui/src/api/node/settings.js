import request from '@/utils/request'

// ==================== 实例运维策略 ====================

export function listSettings(query) {
  return request({ url: '/node/settings/list', method: 'get', params: query })
}

export function getSettings(id) {
  return request({ url: '/node/settings/' + id, method: 'get' })
}

export function getSettingsByServer(serverId) {
  return request({ url: '/node/settings/byServer/' + serverId, method: 'get' })
}

export function addSettings(data) {
  return request({ url: '/node/settings', method: 'post', data })
}

export function updateSettings(data) {
  return request({ url: '/node/settings', method: 'put', data })
}

export function delSettings(ids) {
  return request({ url: '/node/settings/' + ids, method: 'delete' })
}

// ==================== 邮件模板 ====================

export function listEmailTemplate(query) {
  return request({ url: '/node/email/template/list', method: 'get', params: query })
}

export function getEmailTemplate(id) {
  return request({ url: '/node/email/template/' + id, method: 'get' })
}

export function addEmailTemplate(data) {
  return request({ url: '/node/email/template', method: 'post', data })
}

export function updateEmailTemplate(data) {
  return request({ url: '/node/email/template', method: 'put', data })
}

export function delEmailTemplate(ids) {
  return request({ url: '/node/email/template/' + ids, method: 'delete' })
}

export function sendTestEmail(email) {
  return request({ url: '/node/email/template/test', method: 'post', params: { email } })
}
