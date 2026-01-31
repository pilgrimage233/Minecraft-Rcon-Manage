import request from '@/utils/request'

// 查询备份列表
export function listBackup(query) {
  return request({
    url: '/system/backup/list',
    method: 'get',
    params: query
  })
}

// 查询备份详细
export function getBackup(backupId) {
  return request({
    url: '/system/backup/' + backupId,
    method: 'get'
  })
}

// 手动执行备份
export function manualBackup() {
  return request({
    url: '/system/backup/manual',
    method: 'post'
  })
}

// 恢复表数据
export function restoreTable(backupId, tableName) {
  return request({
    url: '/system/backup/restore',
    method: 'post',
    params: {
      backupId: backupId,
      tableName: tableName
    }
  })
}

// 全量回滚
export function restoreAllTables(backupId) {
  return request({
    url: '/system/backup/restoreAll',
    method: 'post',
    params: {
      backupId: backupId
    }
  })
}

// 删除备份
export function delBackup(backupId) {
  return request({
    url: '/system/backup/' + backupId,
    method: 'delete'
  })
}

// 获取备份统计信息
export function getStatistics() {
  return request({
    url: '/system/backup/statistics',
    method: 'get'
  })
}

// 获取可备份的表列表
export function getBackupTables() {
  return request({
    url: '/system/backup/tables',
    method: 'get'
  })
}
