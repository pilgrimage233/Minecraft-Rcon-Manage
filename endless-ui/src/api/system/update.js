import request from '@/utils/request'

// 检查系统更新
export function checkUpdate() {
  return request({
    url: '/system/update/check',
    method: 'get'
  })
}
