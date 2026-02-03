<template>
  <div v-if="show" class="performance-monitor">
    <div class="monitor-header" @click="toggleExpand">
      <span>性能监控</span>
      <i :class="expanded ? 'el-icon-arrow-down' : 'el-icon-arrow-up'"></i>
    </div>
    <div v-show="expanded" class="monitor-content">
      <div class="monitor-item">
        <span class="label">内存使用:</span>
        <span :class="memoryClass" class="value">{{ memoryUsage }}</span>
      </div>
      <div class="monitor-item">
        <span class="label">FPS:</span>
        <span :class="fpsClass" class="value">{{ fps }}</span>
      </div>
      <div class="monitor-item">
        <span class="label">组件数:</span>
        <span class="value">{{ componentCount }}</span>
      </div>
      <div class="monitor-actions">
        <el-button size="mini" @click="clearCache">清理缓存</el-button>
        <el-button size="mini" @click="forceGC">强制GC</el-button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'PerformanceMonitor',
  data() {
    return {
      show: process.env.NODE_ENV === 'development',
      expanded: false,
      memoryUsage: '0 MB',
      fps: 0,
      componentCount: 0,
      memoryTimer: null,
      fpsTimer: null,
      lastTime: performance.now(),
      frames: 0
    }
  },
  computed: {
    memoryClass() {
      const usage = parseFloat(this.memoryUsage)
      if (usage > 150) return 'danger'
      if (usage > 100) return 'warning'
      return 'success'
    },
    fpsClass() {
      if (this.fps < 30) return 'danger'
      if (this.fps < 50) return 'warning'
      return 'success'
    }
  },
  mounted() {
    if (this.show) {
      this.startMonitoring()
    }
  },
  beforeDestroy() {
    this.stopMonitoring()
  },
  methods: {
    toggleExpand() {
      this.expanded = !this.expanded
    },
    startMonitoring() {
      // 监控内存
      this.memoryTimer = setInterval(() => {
        this.updateMemory()
      }, 1000)

      // 监控 FPS
      this.measureFPS()

      // 监控组件数量
      this.updateComponentCount()
    },
    stopMonitoring() {
      if (this.memoryTimer) {
        clearInterval(this.memoryTimer)
      }
      if (this.fpsTimer) {
        cancelAnimationFrame(this.fpsTimer)
      }
    },
    updateMemory() {
      if (performance.memory) {
        const used = performance.memory.usedJSHeapSize / 1048576
        this.memoryUsage = `${used.toFixed(2)} MB`
      }
    },
    measureFPS() {
      this.frames++
      const currentTime = performance.now()

      if (currentTime >= this.lastTime + 1000) {
        this.fps = Math.round((this.frames * 1000) / (currentTime - this.lastTime))
        this.frames = 0
        this.lastTime = currentTime
      }

      this.fpsTimer = requestAnimationFrame(() => this.measureFPS())
    },
    updateComponentCount() {
      // 统计当前 Vue 实例数量
      let count = 0
      const countComponents = (vm) => {
        count++
        if (vm.$children) {
          vm.$children.forEach(child => countComponents(child))
        }
      }
      countComponents(this.$root)
      this.componentCount = count
    },
    clearCache() {
      // 清理路由缓存
      const cachedViews = this.$store.state.tagsView?.cachedViews || []
      this.$store.dispatch('tagsView/delAllCachedViews')
      this.$message.success(`已清理 ${cachedViews.length} 个缓存组件`)
      this.updateComponentCount()
    },
    forceGC() {
      // 提示用户手动触发 GC
      this.$message.info('请在 Chrome DevTools 中手动触发 GC (垃圾回收)')
      console.log('在 Chrome DevTools Performance 面板中点击垃圾桶图标触发 GC')
    }
  }
}
</script>

<style lang="scss" scoped>
.performance-monitor {
  position: fixed;
  bottom: 20px;
  right: 20px;
  background: rgba(0, 0, 0, 0.8);
  color: #fff;
  border-radius: 4px;
  padding: 10px;
  min-width: 200px;
  z-index: 9999;
  font-size: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.3);

  .monitor-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    cursor: pointer;
    padding: 5px;
    font-weight: bold;

    &:hover {
      background: rgba(255, 255, 255, 0.1);
      border-radius: 4px;
    }
  }

  .monitor-content {
    margin-top: 10px;
    padding-top: 10px;
    border-top: 1px solid rgba(255, 255, 255, 0.2);
  }

  .monitor-item {
    display: flex;
    justify-content: space-between;
    margin-bottom: 8px;

    .label {
      color: #aaa;
    }

    .value {
      font-weight: bold;

      &.success {
        color: #67c23a;
      }

      &.warning {
        color: #e6a23c;
      }

      &.danger {
        color: #f56c6c;
      }
    }
  }

  .monitor-actions {
    margin-top: 10px;
    display: flex;
    gap: 5px;

    .el-button {
      flex: 1;
    }
  }
}
</style>
