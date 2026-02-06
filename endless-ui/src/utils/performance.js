/**
 * 性能优化工具函数
 */

/**
 * 防抖函数
 * @param {Function} func 需要防抖的函数
 * @param {Number} wait 等待时间
 * @param {Boolean} immediate 是否立即执行
 */
export function debounce(func, wait = 300, immediate = false) {
  let timeout
  return function () {
    const context = this
    const args = arguments
    const later = function () {
      timeout = null
      if (!immediate) func.apply(context, args)
    }
    const callNow = immediate && !timeout
    clearTimeout(timeout)
    timeout = setTimeout(later, wait)
    if (callNow) func.apply(context, args)
  }
}

/**
 * 节流函数
 * @param {Function} func 需要节流的函数
 * @param {Number} wait 等待时间
 */
export function throttle(func, wait = 300) {
  let previous = 0
  return function () {
    const now = Date.now()
    const context = this
    const args = arguments
    if (now - previous > wait) {
      func.apply(context, args)
      previous = now
    }
  }
}

/**
 * 冻结对象，防止 Vue 对大数据进行响应式处理
 * @param {Object} obj 需要冻结的对象
 */
export function freezeData(obj) {
  return Object.freeze(obj)
}

/**
 * 深度冻结对象
 * @param {Object} obj 需要深度冻结的对象
 */
export function deepFreeze(obj) {
  Object.freeze(obj)
  Object.getOwnPropertyNames(obj).forEach(prop => {
    if (obj[prop] !== null
      && (typeof obj[prop] === 'object' || typeof obj[prop] === 'function')
      && !Object.isFrozen(obj[prop])) {
      deepFreeze(obj[prop])
    }
  })
  return obj
}

/**
 * 图片懒加载指令
 */
export const lazyLoadDirective = {
  inserted: (el, binding) => {
    const loadImage = () => {
      const imageElement = Array.from(el.children).find(
        el => el.nodeName === 'IMG'
      )
      if (imageElement) {
        imageElement.addEventListener('load', () => {
          setTimeout(() => el.classList.add('loaded'), 100)
        })
        imageElement.addEventListener('error', () => console.log('error'))
        imageElement.src = binding.value
      }
    }

    const handleIntersect = (entries, observer) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          loadImage()
          observer.unobserve(el)
        }
      })
    }

    const createObserver = () => {
      const options = {
        root: null,
        threshold: 0
      }
      const observer = new IntersectionObserver(handleIntersect, options)
      observer.observe(el)
    }

    if (window.IntersectionObserver) {
      createObserver()
    } else {
      loadImage()
    }
  }
}

/**
 * 清理定时器 mixin
 */
export const clearTimerMixin = {
  data() {
    return {
      _timers: []
    }
  },
  methods: {
    $_setTimeout(callback, delay) {
      const timer = setTimeout(callback, delay)
      this._timers.push(timer)
      return timer
    },
    $_setInterval(callback, delay) {
      const timer = setInterval(callback, delay)
      this._timers.push(timer)
      return timer
    },
    $_clearTimer(timer) {
      const index = this._timers.indexOf(timer)
      if (index > -1) {
        clearTimeout(timer)
        clearInterval(timer)
        this._timers.splice(index, 1)
      }
    }
  },
  beforeDestroy() {
    this._timers.forEach(timer => {
      clearTimeout(timer)
      clearInterval(timer)
    })
    this._timers = []
  }
}

/**
 * 内存监控
 */
export function monitorMemory() {
  if (performance.memory) {
    const {usedJSHeapSize, totalJSHeapSize, jsHeapSizeLimit} = performance.memory
    console.log('内存使用情况:')
    console.log(`已使用: ${(usedJSHeapSize / 1048576).toFixed(2)} MB`)
    console.log(`总计: ${(totalJSHeapSize / 1048576).toFixed(2)} MB`)
    console.log(`限制: ${(jsHeapSizeLimit / 1048576).toFixed(2)} MB`)
    console.log(`使用率: ${((usedJSHeapSize / jsHeapSizeLimit) * 100).toFixed(2)}%`)
  }
}

/**
 * 长列表优化 - 虚拟滚动配置
 */
export const virtualScrollConfig = {
  itemHeight: 50, // 每项高度
  buffer: 5, // 缓冲区项数
  throttleTime: 16 // 节流时间
}

/**
 * 大数据处理 - 分批处理
 * @param {Array} data 需要处理的数据
 * @param {Function} handler 处理函数
 * @param {Number} batchSize 每批处理数量
 */
export function processBatch(data, handler, batchSize = 100) {
  return new Promise((resolve) => {
    let index = 0
    const process = () => {
      const batch = data.slice(index, index + batchSize)
      batch.forEach(handler)
      index += batchSize

      if (index < data.length) {
        requestAnimationFrame(process)
      } else {
        resolve()
      }
    }
    process()
  })
}

/**
 * 组件缓存配置
 */
export const keepAliveConfig = {
  max: 10, // 最多缓存10个组件
  include: [], // 需要缓存的组件名称
  exclude: ['Editor', 'Terminal'] // 不缓存的组件（大内存组件）
}

/**
 * 清理 Vue 组件实例
 */
export function cleanupComponent(vm) {
  // 清理事件监听
  if (vm._events) {
    Object.keys(vm._events).forEach(event => {
      vm.$off(event)
    })
  }

  // 清理 watchers
  if (vm._watchers) {
    vm._watchers.forEach(watcher => {
      watcher.teardown()
    })
  }

  // 清理子组件
  if (vm.$children) {
    vm.$children.forEach(child => {
      cleanupComponent(child)
    })
  }
}

/**
 * 检测内存泄漏
 */
export function detectMemoryLeak() {
  if (process.env.NODE_ENV === 'development') {
    let lastUsedHeap = 0
    setInterval(() => {
      if (performance.memory) {
        const currentUsedHeap = performance.memory.usedJSHeapSize
        const diff = currentUsedHeap - lastUsedHeap
        if (diff > 10485760) { // 增长超过10MB
          console.warn('可能存在内存泄漏，内存增长:', (diff / 1048576).toFixed(2), 'MB')
        }
        lastUsedHeap = currentUsedHeap
      }
    }, 30000) // 每30秒检查一次
  }
}
