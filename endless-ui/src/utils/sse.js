/**
 * SSE (Server-Sent Events) 工具函数
 */

/**
 * 构建 SSE 连接的完整 URL
 * @param {string} path - API 路径，例如 '/system/update/progress'
 * @returns {string} 完整的 SSE URL
 */
export function buildSSEUrl(path) {
  const baseURL = process.env.VUE_APP_BASE_API

  // 如果 baseURL 已经是完整的 URL（以 http 开头），直接拼接
  if (baseURL.startsWith('http')) {
    return baseURL + path
  }

  // 否则，使用当前页面的 origin 加上 baseURL 和 path
  // 这样在生产环境中可以正确处理相对路径
  return window.location.origin + baseURL + path
}

/**
 * 创建 SSE 连接
 * @param {string} path - API 路径
 * @param {Object} options - 配置选项
 * @param {Function} options.onProgress - 进度事件处理函数
 * @param {Function} options.onComplete - 完成事件处理函数
 * @param {Function} options.onError - 错误事件处理函数
 * @returns {EventSource} EventSource 实例
 */
export function createSSEConnection(path, options = {}) {
  const url = buildSSEUrl(path)
  const eventSource = new EventSource(url)

  if (options.onProgress) {
    eventSource.addEventListener('progress', options.onProgress)
  }

  if (options.onComplete) {
    eventSource.addEventListener('complete', options.onComplete)
  }

  if (options.onError) {
    eventSource.onerror = options.onError
  }

  return eventSource
}
