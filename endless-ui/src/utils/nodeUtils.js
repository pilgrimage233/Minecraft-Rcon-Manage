/**
 * 前端工具函数库
 * 提供防抖、节流、重试等通用功能
 *
 * @author Memory
 */

/**
 * 防抖函数
 * @param {Function} fn 要执行的函数
 * @param {number} delay 延迟时间（毫秒）
 * @returns {Function} 防抖后的函数
 */
export function debounce(fn, delay = 300) {
  let timer = null
  return function (...args) {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => {
      fn.apply(this, args)
      timer = null
    }, delay)
  }
}

/**
 * 节流函数
 * @param {Function} fn 要执行的函数
 * @param {number} interval 间隔时间（毫秒）
 * @returns {Function} 节流后的函数
 */
export function throttle(fn, interval = 300) {
  let lastTime = 0
  return function (...args) {
    const now = Date.now()
    if (now - lastTime >= interval) {
      lastTime = now
      fn.apply(this, args)
    }
  }
}

/**
 * 带重试的异步函数执行器
 * @param {Function} fn 要执行的异步函数
 * @param {Object} options 选项
 * @param {number} options.maxRetries 最大重试次数
 * @param {number} options.delay 重试延迟（毫秒）
 * @param {Function} options.onRetry 重试时的回调
 * @returns {Promise} 执行结果
 */
export async function withRetry(fn, options = {}) {
  const {
    maxRetries = 3,
    delay = 1000,
    onRetry = null
  } = options

  let lastError = null

  for (let attempt = 0; attempt <= maxRetries; attempt++) {
    try {
      return await fn()
    } catch (error) {
      lastError = error

      if (attempt < maxRetries) {
        const retryDelay = delay * Math.pow(2, attempt) // 指数退避
        if (onRetry) {
          onRetry(attempt + 1, maxRetries, retryDelay, error)
        }
        await sleep(retryDelay)
      }
    }
  }

  throw lastError
}

/**
 * 延迟执行
 * @param {number} ms 延迟时间（毫秒）
 * @returns {Promise} Promise 对象
 */
export function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms))
}

/**
 * 格式化文件大小
 * @param {number} bytes 字节数
 * @returns {string} 格式化后的文件大小
 */
export function formatFileSize(bytes) {
  if (!bytes || bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

/**
 * 格式化网络速度
 * @param {number} bytesPerSec 每秒字节数
 * @returns {string} 格式化后的速度
 */
export function formatNetworkSpeed(bytesPerSec) {
  if (!bytesPerSec) return '0 B/s'
  const k = 1024
  const sizes = ['B/s', 'KB/s', 'MB/s', 'GB/s']
  const i = Math.floor(Math.log(bytesPerSec) / Math.log(k))
  return parseFloat((bytesPerSec / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

/**
 * 格式化百分比
 * @param {number} value 数值
 * @param {number} decimals 小数位数
 * @returns {string} 格式化后的百分比
 */
export function formatPercentage(value, decimals = 1) {
  if (value === null || value === undefined) return '0%'
  return value.toFixed(decimals) + '%'
}

/**
 * 格式化时间戳
 * @param {number} timestamp 时间戳（秒或毫秒）
 * @param {string} format 格式
 * @returns {string} 格式化后的时间
 */
export function formatTimestamp(timestamp, format = 'YYYY-MM-DD HH:mm:ss') {
  if (!timestamp) return '-'

  // 如果是秒级时间戳，转换为毫秒
  const ts = timestamp < 10000000000 ? timestamp * 1000 : timestamp
  const date = new Date(ts)

  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')

  return format
    .replace('YYYY', year)
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds)
}

/**
 * 格式化运行时间
 * @param {number} seconds 秒数
 * @returns {string} 格式化后的运行时间
 */
export function formatUptime(seconds) {
  if (!seconds) return '-'

  const days = Math.floor(seconds / 86400)
  const hours = Math.floor((seconds % 86400) / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)

  const parts = []
  if (days > 0) parts.push(`${days}天`)
  if (hours > 0) parts.push(`${hours}小时`)
  if (minutes > 0) parts.push(`${minutes}分钟`)

  return parts.length > 0 ? parts.join(' ') : '不到1分钟'
}

/**
 * 深拷贝对象
 * @param {*} obj 要拷贝的对象
 * @returns {*} 拷贝后的对象
 */
export function deepClone(obj) {
  if (obj === null || typeof obj !== 'object') {
    return obj
  }

  if (obj instanceof Date) {
    return new Date(obj.getTime())
  }

  if (obj instanceof Array) {
    return obj.map(item => deepClone(item))
  }

  if (obj instanceof Object) {
    const copy = {}
    Object.keys(obj).forEach(key => {
      copy[key] = deepClone(obj[key])
    })
    return copy
  }

  return obj
}

/**
 * 生成随机字符串
 * @param {number} length 字符串长度
 * @returns {string} 随机字符串
 */
export function generateRandomString(length = 16) {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'
  let result = ''
  for (let i = 0; i < length; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length))
  }
  return result
}

/**
 * 检查是否为移动设备
 * @returns {boolean} 是否为移动设备
 */
export function isMobile() {
  return /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent)
}

/**
 * 获取状态颜色
 * @param {string} status 状态值
 * @returns {string} 颜色值
 */
export function getStatusColor(status) {
  const colorMap = {
    '0': '#67C23A',  // 正常 - 绿色
    '1': '#909399',  // 停止 - 灰色
    '2': '#F56C6C',  // 故障 - 红色
    '3': '#E6A23C'   // 异常 - 橙色
  }
  return colorMap[status] || '#909399'
}

/**
 * 获取状态标签类型
 * @param {string} status 状态值
 * @returns {string} 标签类型
 */
export function getStatusTagType(status) {
  const typeMap = {
    '0': 'success',
    '1': 'info',
    '2': 'danger',
    '3': 'warning'
  }
  return typeMap[status] || 'info'
}

/**
 * 获取状态文本
 * @param {string} status 状态值
 * @returns {string} 状态文本
 */
export function getStatusText(status) {
  const textMap = {
    '0': '正常',
    '1': '停止',
    '2': '故障',
    '3': '异常'
  }
  return textMap[status] || '未知'
}

/**
 * 验证 IP 地址格式
 * @param {string} ip IP 地址
 * @returns {boolean} 是否有效
 */
export function isValidIP(ip) {
  if (!ip) return false
  const ipv4Pattern = /^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/
  return ipv4Pattern.test(ip)
}

/**
 * 验证端口号
 * @param {number|string} port 端口号
 * @returns {boolean} 是否有效
 */
export function isValidPort(port) {
  const num = Number(port)
  return Number.isInteger(num) && num > 0 && num <= 65535
}

/**
 * 清理 HTML 标签（防止 XSS）
 * @param {string} str 字符串
 * @returns {string} 清理后的字符串
 */
export function sanitizeHtml(str) {
  if (!str) return ''
  return str
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#x27;')
}

/**
 * 截断字符串
 * @param {string} str 字符串
 * @param {number} maxLength 最大长度
 * @param {string} suffix 后缀
 * @returns {string} 截断后的字符串
 */
export function truncateString(str, maxLength = 50, suffix = '...') {
  if (!str || str.length <= maxLength) return str
  return str.substring(0, maxLength - suffix.length) + suffix
}
