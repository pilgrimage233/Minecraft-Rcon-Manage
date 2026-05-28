<template>
  <el-card class="header-card" shadow="hover">
    <div class="header">
      <div class="title-section">
        <div class="title">
          <span
            :class="{ running: statusTag === 'success', stopped: statusTag === 'info', pulsing: statusTag === 'success' }"
            class="status-dot"
          ></span>
          <i class="el-icon-cpu title-icon"></i>
          <span class="title-text">{{ instanceName }}</span>
        </div>
        <div v-if="instanceInfo" class="meta">
          <el-tag class="meta-tag" size="small">
            <i class="el-icon-connection"></i>
            Node ID: {{ instanceInfo.nodeId }}
          </el-tag>
          <el-tag class="meta-tag" size="small">
            <i class="el-icon-server"></i>
            Server ID: {{ serverId }}
          </el-tag>
          <el-tag :type="statusTag" class="meta-tag status-tag" size="small">
            <i :class="statusTag === 'success' ? 'el-icon-success' : 'el-icon-warning'"></i>
            {{ statusText }}
          </el-tag>
          <el-tag
            v-if="runtimeFormatted"
            class="meta-tag"
            size="small"
          >
            <i class="el-icon-time"></i>
            运行时长: {{ runtimeFormatted }}
          </el-tag>
          <el-tag
            v-if="hasCpu"
            class="meta-tag"
            size="small"
          >
            <i class="el-icon-cpu"></i>
            CPU: {{ cpuPercent }}%
          </el-tag>
          <el-tag
            v-if="hasMemory"
            class="meta-tag"
            size="small"
          >
            <i class="el-icon-pie-chart"></i>
            内存: {{ memoryDisplay }}
          </el-tag>
        </div>
      </div>
      <div class="actions">
        <el-button
          :loading="opLoading"
          class="action-btn"
          icon="el-icon-video-play"
          size="small"
          type="success"
          @click="$emit('start')"
        >启动</el-button>
        <el-button
          :loading="opLoading"
          class="action-btn"
          icon="el-icon-video-pause"
          size="small"
          @click="$emit('stop')"
        >停止</el-button>
        <el-button
          :loading="opLoading"
          class="action-btn"
          icon="el-icon-refresh"
          size="small"
          type="warning"
          @click="$emit('restart')"
        >重启</el-button>
        <el-button
          :loading="opLoading"
          class="action-btn"
          icon="el-icon-close"
          size="small"
          type="danger"
          @click="$emit('kill')"
        >强制终止</el-button>
      </div>
    </div>
  </el-card>
</template>

<script>
export default {
  name: 'TerminalHeader',
  props: {
    instanceInfo: {
      type: Object,
      default: null
    },
    serverId: {
      type: [Number, String],
      default: null
    },
    statusTag: {
      type: String,
      default: 'warning'
    },
    statusText: {
      type: String,
      default: '未知'
    },
    serverStatus: {
      type: Object,
      default: null
    },
    opLoading: {
      type: Boolean,
      default: false
    }
  },
  computed: {
    instanceName() {
      return this.instanceInfo?.name || '加载中...'
    },
    runtimeFormatted() {
      return this.serverStatus?.runtime?.runtimeFormatted
    },
    resourceUsage() {
      return this.serverStatus?.processInfo?.resourceUsage
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
    hasMemory() {
      return !!this.resourceUsage?.memoryMB
    },
    memoryDisplay() {
      const mem = this.resourceUsage?.memoryMB
      if (!mem) return '0 MB'
      const num = typeof mem === 'string' ? parseFloat(mem) : mem
      if (isNaN(num)) return '0 MB'
      return num >= 1024 ? (num / 1024).toFixed(2) + ' GB' : num.toFixed(2) + ' MB'
    }
  }
}
</script>

<style lang="scss" scoped>
.header-card {
  margin-bottom: 20px;
  border-radius: 16px;
  border: none;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.3);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.header-card::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, transparent 70%);
  animation: shimmer 8s linear infinite;
  pointer-events: none;
}

@keyframes shimmer {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.header-card ::v-deep .el-card__body {
  padding: 24px;
  position: relative;
  z-index: 1;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 16px;
}

.title-section {
  flex: 1;
  min-width: 300px;
}

.title {
  font-weight: 600;
  font-size: 20px;
  display: flex;
  align-items: center;
  color: #ffffff;
  margin-bottom: 12px;
}

.title-icon {
  margin: 0 8px 0 4px;
  font-size: 20px;
}

.title-text {
  font-weight: 600;
}

.status-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  display: inline-block;
  margin-right: 10px;
  background: #dcdfe6;
  box-shadow: 0 0 0 0 rgba(103, 194, 58, 0.7);
  transition: all 0.3s ease;
}

.status-dot.running {
  background: #67c23a;
  box-shadow: 0 0 0 0 rgba(103, 194, 58, 0.7);
}

.status-dot.stopped {
  background: #909399;
}

.status-dot.pulsing {
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { box-shadow: 0 0 0 0 rgba(103, 194, 58, 0.7); }
  70% { box-shadow: 0 0 0 10px rgba(103, 194, 58, 0); }
  100% { box-shadow: 0 0 0 0 rgba(103, 194, 58, 0); }
}

.meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.meta-tag {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: #ffffff;
}

.actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
</style>
