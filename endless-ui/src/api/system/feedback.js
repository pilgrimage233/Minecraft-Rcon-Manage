import request from '@/utils/request'

// 提交反馈
export function submitFeedback(data) {
  return request({
    url: '/system/feedback',
    method: 'post',
    data: data
  })
}

// 获取我的反馈历史
export function getMyFeedbackList() {
  return request({
    url: '/system/feedback/my',
    method: 'get'
  })
}

// 获取反馈详情（通过UUID）
export function getFeedbackDetail(uuid) {
  return request({
    url: `/system/feedback/${uuid}`,
    method: 'get'
  })
}

// 上传反馈附件
export function uploadFeedbackAttachment(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/system/feedback/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
