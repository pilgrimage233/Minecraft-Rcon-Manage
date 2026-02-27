import request from '@/utils/request'

// 查询白名单列表
export function listWhitelist(query) {
  return request({
    url: '/mc/whitelist/list',
    method: 'get',
    params: query
  })
}

// 查询白名单详细
export function getWhitelist(id) {
  return request({
    url: '/mc/whitelist/' + id,
    method: 'get'
  })
}

// 新增白名单
export function addWhitelist(data) {
  return request({
    url: '/mc/whitelist',
    method: 'post',
    data: data
  })
}

// 修改白名单
export function updateWhitelist(data) {
  return request({
    url: '/mc/whitelist',
    method: 'put',
    data: data
  })
}

// 删除白名单
export function delWhitelist(id) {
  return request({
    url: '/mc/whitelist/' + id,
    method: 'delete'
  })
}

// 获取服务器列表
export function getServerList() {
  return request({
    url: '/server/serverlist/getServerList',
    method: 'get'
  })
}

// 管理员手动添加白名单
export function addWhiteListForAdmin(data) {
  return request({
    url: '/mc/whitelist/addWhiteListForAdmin',
    method: 'post',
    data: data
  })
}

// 下载白名单模板
export function downloadTemplate() {
  return request({
    url: '/mc/whitelist/downloadTemplate',
    method: 'get',
    responseType: 'blob'
  })
}

// 导入白名单模板
export function importTemplate(data) {
  return request({
    url: '/mc/whitelist/importTemplate',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 根据白名单ID查询ID更改历史
export function getChangeHistoryByWhitelistId(whitelistId) {
  return request({
    url: '/mc/whitelist/changeHistory/byWhitelistId',
    method: 'get',
    params: {whitelistId}
  })
}

// 查询投票模板
export function listWhitelistVoteTemplates() {
  return request({
    url: '/mc/whitelist/vote/template/list',
    method: 'get'
  })
}

// 新增自定义投票模板
export function createCustomWhitelistVoteTemplate(data) {
  return request({
    url: '/mc/whitelist/vote/template/custom',
    method: 'post',
    data: data
  })
}

// 查询投票列表
export function listWhitelistVotes(query) {
  return request({
    url: '/mc/whitelist/vote/list',
    method: 'get',
    params: query
  })
}

// 查询投票详情
export function getWhitelistVoteDetail(id) {
  return request({
    url: '/mc/whitelist/vote/' + id,
    method: 'get'
  })
}

// 发起投票
export function createWhitelistVote(data) {
  return request({
    url: '/mc/whitelist/vote/create',
    method: 'post',
    data: data
  })
}

// 跟投
export function castWhitelistVote(data) {
  return request({
    url: '/mc/whitelist/vote/cast',
    method: 'post',
    data: data
  })
}
