/**
 * WebSocket 连接管理器
 * 提供连接池、自动重连、心跳检测等功能
 *
 * @author Memory
 */
import SockJS from 'sockjs-client/dist/sockjs.min.js'
import Stomp from 'stompjs'

// 连接状态常量
const CONNECTION_STATUS = {
  DISCONNECTED: 'disconnected',
  CONNECTING: 'connecting',
  CONNECTED: 'connected',
  RECONNECTING: 'reconnecting'
}

// 默认配置
const DEFAULT_OPTIONS = {
  connectTimeout: 10000,      // 连接超时时间（毫秒）
  reconnectDelay: 5000,       // 重连延迟（毫秒）
  maxReconnectAttempts: 10,   // 最大重连次数
  heartbeatInterval: 30000,   // 心跳间隔（毫秒）
  heartbeatEnabled: true      // 是否启用心跳
}

/**
 * WebSocket 连接类
 */
export class WebSocketConnection {
  /**
   * 构造函数
   * @param {string} url WebSocket URL
   * @param {Object} options 配置选项
   */
  constructor(url, options = {}) {
    this.url = url
    this.options = { ...DEFAULT_OPTIONS, ...options }
    this.stompClient = null
    this.subscription = null
    this.status = CONNECTION_STATUS.DISCONNECTED
    this.reconnectAttempts = 0
    this.reconnectTimer = null
    this.heartbeatTimer = null
    this.connectTimeoutTimer = null
    this.listeners = new Map()
  }

  /**
   * 连接 WebSocket
   * @param {Object} headers 连接头信息
   * @returns {Promise} 连接结果
   */
  connect(headers = {}) {
    return new Promise((resolve, reject) => {
      if (this.status === CONNECTION_STATUS.CONNECTED || this.status === CONNECTION_STATUS.CONNECTING) {
        resolve()
        return
      }

      this.status = CONNECTION_STATUS.CONNECTING
      this.emit('connecting')

      try {
        const sock = new SockJS(this.url)
        this.stompClient = Stomp.over(sock)
        this.stompClient.debug = null // 禁用调试日志

        // 设置连接超时
        this.connectTimeoutTimer = setTimeout(() => {
          if (this.status === CONNECTION_STATUS.CONNECTING) {
            this.status = CONNECTION_STATUS.DISCONNECTED
            this.emit('timeout')
            reject(new Error('连接超时'))
          }
        }, this.options.connectTimeout)

        this.stompClient.connect(headers, () => {
          clearTimeout(this.connectTimeoutTimer)
          this.status = CONNECTION_STATUS.CONNECTED
          this.reconnectAttempts = 0
          this.emit('connected')

          // 启动心跳
          if (this.options.heartbeatEnabled) {
            this.startHeartbeat()
          }

          resolve()
        }, (error) => {
          clearTimeout(this.connectTimeoutTimer)
          this.status = CONNECTION_STATUS.DISCONNECTED
          this.emit('error', error)
          reject(error)
        })
      } catch (error) {
        clearTimeout(this.connectTimeoutTimer)
        this.status = CONNECTION_STATUS.DISCONNECTED
        this.emit('error', error)
        reject(error)
      }
    })
  }

  /**
   * 断开连接
   */
  disconnect() {
    this.stopHeartbeat()
    this.stopReconnect()

    if (this.subscription) {
      try {
        this.subscription.unsubscribe()
      } catch (e) {
        console.error('取消订阅失败:', e)
      }
      this.subscription = null
    }

    if (this.stompClient) {
      try {
        this.stompClient.disconnect(() => {
          console.log('WebSocket 已断开')
        })
      } catch (e) {
        console.error('断开连接失败:', e)
      }
      this.stompClient = null
    }

    this.status = CONNECTION_STATUS.DISCONNECTED
    this.emit('disconnected')
  }

  /**
   * 订阅主题
   * @param {string} topic 主题路径
   * @param {Function} callback 消息回调
   * @param {Object} headers 订阅头信息
   * @returns {Object} 订阅对象
   */
  subscribe(topic, callback, headers = {}) {
    if (!this.stompClient || this.status !== CONNECTION_STATUS.CONNECTED) {
      console.warn('WebSocket 未连接，无法订阅')
      return null
    }

    try {
      this.subscription = this.stompClient.subscribe(topic, (message) => {
        try {
          const body = JSON.parse(message.body)
          callback(body)
        } catch (e) {
          console.error('解析消息失败:', e)
          callback(message.body)
        }
      }, headers)

      this.emit('subscribed', topic)
      return this.subscription
    } catch (e) {
      console.error('订阅失败:', e)
      return null
    }
  }

  /**
   * 发送消息
   * @param {string} destination 目标路径
   * @param {Object} headers 头信息
   * @param {string|Object} body 消息体
   */
  send(destination, headers = {}, body = {}) {
    if (!this.stompClient || this.status !== CONNECTION_STATUS.CONNECTED) {
      console.warn('WebSocket 未连接，无法发送消息')
      return false
    }

    try {
      const bodyStr = typeof body === 'object' ? JSON.stringify(body) : body
      this.stompClient.send(destination, headers, bodyStr)
      return true
    } catch (e) {
      console.error('发送消息失败:', e)
      return false
    }
  }

  /**
   * 启动自动重连
   */
  startReconnect() {
    if (this.reconnectTimer) {
      return
    }

    this.status = CONNECTION_STATUS.RECONNECTING
    this.emit('reconnecting')

    const attemptReconnect = () => {
      if (this.reconnectAttempts >= this.options.maxReconnectAttempts) {
        this.stopReconnect()
        this.emit('reconnectFailed')
        console.error('重连失败，已达到最大重连次数')
        return
      }

      this.reconnectAttempts++
      const delay = this.options.reconnectDelay * Math.pow(1.5, this.reconnectAttempts - 1)
      const jitter = delay * (0.5 + Math.random() * 0.5) // 添加抖动

      console.log(`将在 ${Math.round(jitter / 1000)} 秒后进行第 ${this.reconnectAttempts} 次重连`)

      this.reconnectTimer = setTimeout(async () => {
        try {
          await this.connect()
          console.log('重连成功')
        } catch (error) {
          console.warn('重连失败:', error.message)
          attemptReconnect()
        }
      }, jitter)
    }

    attemptReconnect()
  }

  /**
   * 停止自动重连
   */
  stopReconnect() {
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }
    this.reconnectAttempts = 0
  }

  /**
   * 启动心跳
   */
  startHeartbeat() {
    this.stopHeartbeat()

    this.heartbeatTimer = setInterval(() => {
      if (this.status === CONNECTION_STATUS.CONNECTED) {
        try {
          this.stompClient.send('/app/heartbeat', {}, JSON.stringify({ timestamp: Date.now() }))
        } catch (e) {
          console.warn('心跳发送失败:', e)
        }
      }
    }, this.options.heartbeatInterval)
  }

  /**
   * 停止心跳
   */
  stopHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer)
      this.heartbeatTimer = null
    }
  }

  /**
   * 添加事件监听器
   * @param {string} event 事件名称
   * @param {Function} listener 监听函数
   */
  on(event, listener) {
    if (!this.listeners.has(event)) {
      this.listeners.set(event, [])
    }
    this.listeners.get(event).push(listener)
  }

  /**
   * 移除事件监听器
   * @param {string} event 事件名称
   * @param {Function} listener 监听函数
   */
  off(event, listener) {
    if (!this.listeners.has(event)) {
      return
    }
    const listeners = this.listeners.get(event)
    const index = listeners.indexOf(listener)
    if (index > -1) {
      listeners.splice(index, 1)
    }
  }

  /**
   * 触发事件
   * @param {string} event 事件名称
   * @param {...*} args 参数
   */
  emit(event, ...args) {
    if (!this.listeners.has(event)) {
      return
    }
    this.listeners.get(event).forEach(listener => {
      try {
        listener(...args)
      } catch (e) {
        console.error('事件监听器执行失败:', e)
      }
    })
  }

  /**
   * 获取连接状态
   * @returns {string} 连接状态
   */
  getStatus() {
    return this.status
  }

  /**
   * 是否已连接
   * @returns {boolean} 是否已连接
   */
  isConnected() {
    return this.status === CONNECTION_STATUS.CONNECTED
  }
}

/**
 * WebSocket 连接池管理器
 */
export class WebSocketPool {
  constructor() {
    this.connections = new Map()
  }

  /**
   * 获取或创建连接
   * @param {string} key 连接键
   * @param {string} url WebSocket URL
   * @param {Object} options 配置选项
   * @returns {WebSocketConnection} 连接对象
   */
  getConnection(key, url, options = {}) {
    if (!this.connections.has(key)) {
      const connection = new WebSocketConnection(url, options)
      this.connections.set(key, connection)
    }
    return this.connections.get(key)
  }

  /**
   * 移除连接
   * @param {string} key 连接键
   */
  removeConnection(key) {
    if (this.connections.has(key)) {
      const connection = this.connections.get(key)
      connection.disconnect()
      this.connections.delete(key)
    }
  }

  /**
   * 断开所有连接
   */
  disconnectAll() {
    this.connections.forEach((connection, key) => {
      connection.disconnect()
    })
    this.connections.clear()
  }

  /**
   * 获取连接数量
   * @returns {number} 连接数量
   */
  getSize() {
    return this.connections.size
  }
}

// 导出单例实例
export const webSocketPool = new WebSocketPool()

// 导出常量
export { CONNECTION_STATUS, DEFAULT_OPTIONS }
