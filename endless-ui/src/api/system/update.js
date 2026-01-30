import request from '@/utils/request'

// 检查系统更新
export function checkUpdate() {
  return request({
    url: '/system/update/check',
    method: 'get'
  })
}

// 下载并安装更新
export function downloadUpdate() {
  return request({
    url: '/system/update/download',
    method: 'post',
    timeout: 300000 // 5 minutes timeout for download
  })
}
