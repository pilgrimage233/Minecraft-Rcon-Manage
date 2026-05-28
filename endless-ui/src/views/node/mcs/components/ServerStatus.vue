<template>
  <el-card class="side-card status-card" shadow="hover">
    <div slot="header" class="card-header">
      <i class="el-icon-data-line"></i>
      <span>运行状态</span>
    </div>
    <el-descriptions v-if="status" :column="1" border class="info-descriptions" size="small">
      <el-descriptions-item label="运行状态">
        <el-tag :type="status.isRunning ? 'success' : 'info'" size="small">
          <i :class="status.isRunning ? 'el-icon-success' : 'el-icon-warning'"></i>
          {{ status.isRunning ? '运行中' : '已停止' }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item v-if="runtime && runtime.runtimeFormatted" label="运行时长">
        <i class="el-icon-time"></i>
        {{ runtime.runtimeFormatted }}
      </el-descriptions-item>
      <el-descriptions-item v-if="runtime && runtime.startTime" label="启动时间">
        <i class="el-icon-time"></i>
        {{ formatDate(runtime.startTime) }}
      </el-descriptions-item>
      <el-descriptions-item v-if="config && config.version" label="版本">
        <i class="el-icon-document"></i>
        {{ config.version }}
      </el-descriptions-item>
      <el-descriptions-item v-if="processInfo && processInfo.command" label="Java">
        <i class="el-icon-document"></i>
        {{ processInfo.command }}
      </el-descriptions-item>
      <el-descriptions-item v-if="config && config.coreType" label="核心类型">
        <i class="el-icon-cpu"></i>
        {{ config.coreType }}
      </el-descriptions-item>
      <el-descriptions-item v-if="config && config.port" label="端口">
        <i class="el-icon-connection"></i>
        {{ config.port }}
      </el-descriptions-item>
      <el-descriptions-item v-if="config && config.memoryMb" label="内存">
        <i class="el-icon-pie-chart"></i>
        {{ config.memoryMb }} MB
      </el-descriptions-item>
      <el-descriptions-item v-if="processInfo && processInfo.pid" label="PID">
        <i class="el-icon-link"></i>
        {{ processInfo.pid }}
      </el-descriptions-item>
      <el-descriptions-item v-if="resourceUsage && resourceUsage.memoryMB" label="内存占用">
        <i class="el-icon-pie-chart"></i>
        {{ resourceUsage.memoryMB.toFixed(2) }} MB
      </el-descriptions-item>
      <el-descriptions-item v-if="hasCpu" label="CPU使用率">
        <i class="el-icon-cpu"></i>
        {{ cpuPercent }}%
      </el-descriptions-item>
      <el-descriptions-item v-if="hasCpuDuration" label="CPU累计时间">
        <i class="el-icon-time"></i>
        {{ cpuDuration }}
      </el-descriptions-item>
    </el-descriptions>
    <div v-else class="loading-placeholder">
      <i class="el-icon-loading"></i>
      <span>加载状态中...</span>
    </div>
  </el-card>
</template>

<script>
export default {
  name: 'ServerStatus',
  props: {
    status: {
      type: Object,
      default: null
    }
  },
  computed: {
    runtime() {
      return this.status?.runtime
    },
    config() {
      return this.status?.config
    },
    processInfo() {
      return this.status?.processInfo
    },
    resourceUsage() {
      return this.processInfo?.resourceUsage
    },
    hasCpu() {
      const cpu = this.resourceUsage?.cpuPercent
      if (cpu == null) return false
      if (typeof cpu === 'string') {
        const trimmed = cpu.trim()
        if (trimmed === '' || trimmed === 'PercentProcessorTime') return false
        return !isNaN(parseFloat(trimmed))
      }
      return !isNaN(cpu)
    },
    cpuPercent() {
      const cpu = this.resourceUsage?.cpuPercent
      if (cpu == null) return '0'
      const num = typeof cpu === 'string' ? parseFloat(cpu.trim()) : cpu
      return isNaN(num) ? '0' : num.toFixed(2)
    },
    hasCpuDuration() {
      return this.processInfo?.totalCpuDurationSeconds != null || this.processInfo?.totalCpuDurationNanos != null
    },
    cpuDuration() {
      const seconds = this.processInfo?.totalCpuDurationSeconds
      const nanos = this.processInfo?.totalCpuDurationNanos
      if (seconds == null && nanos == null) return '-'

      let totalSeconds = 0
      if (seconds != null) {
        totalSeconds = typeof seconds === 'string' ? parseFloat(seconds) : seconds
      }
      if (nanos != null) {
        const totalNanos = typeof nanos === 'string' ? parseFloat(nanos) : nanos
        totalSeconds += totalNanos / 1000000000
      }

      if (isNaN(totalSeconds) || totalSeconds < 0) return '-'

      const hours = Math.floor(totalSeconds / 3600)
      const minutes = Math.floor((totalSeconds % 3600) / 60)
      const secs = Math.floor(totalSeconds % 60)
      const milliseconds = Math.floor((totalSeconds % 1) * 1000)

      const parts = []
      if (hours > 0) parts.push(hours + '小时')
      if (minutes > 0) parts.push(minutes + '分钟')
      if (secs > 0 || parts.length === 0) parts.push(secs + '秒')
      if (milliseconds > 0 && parts.length < 3) parts.push(milliseconds + '毫秒')

      return parts.join(' ') || '0秒'
    }
  },
  methods: {
    formatDate(date) {
      try {
        return new Date(date).toLocaleString()
      } catch {
        return date
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.side-card {
  margin-bottom: 16px;
  border-radius: 12px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.info-descriptions {
  ::v-deep {
    .el-descriptions-item__label {
      width: 100px;
    }
  }
}

.loading-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24px;
  color: #909399;
  gap: 8px;
}
</style>
