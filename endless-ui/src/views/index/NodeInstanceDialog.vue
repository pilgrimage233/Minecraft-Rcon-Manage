<template>
  <el-dialog
    :title="dialogTitle"
    :visible.sync="visible"
    append-to-body
    class="node-instance-dialog"
    width="800px"
    @close="handleClose"
  >
    <!-- 节点信息头部 -->
    <div class="node-header">
      <div class="node-basic">
        <div class="node-icon">
          <i class="el-icon-monitor"></i>
        </div>
        <div class="node-meta">
          <div class="node-name">{{ node.name }}</div>
          <div class="node-tags">
            <el-tag :type="getNodeTagType(node.status)" effect="dark" size="mini">
              {{ getNodeStatusText(node.status) }}
            </el-tag>
            <el-tag effect="plain" size="mini" type="info">{{ node.osType || '未知系统' }}</el-tag>
            <el-tag effect="plain" size="mini" type="info">v{{ node.version || '未知' }}</el-tag>
          </div>
        </div>
      </div>
      <div class="node-actions">
        <el-button :loading="loading" icon="el-icon-refresh" size="small" @click="loadInstances">
          刷新
        </el-button>
        <el-button icon="el-icon-plus" size="small" type="primary" @click="goToAddInstance">
          新增实例
        </el-button>
      </div>
    </div>

    <!-- 实例列表 -->
    <div v-loading="loading" class="instance-list">
      <div v-if="instances.length === 0 && !loading" class="empty-state">
        <i class="el-icon-box"></i>
        <p>该节点暂无实例</p>
        <el-button size="small" type="primary" @click="goToAddInstance">创建第一个实例</el-button>
      </div>

      <div v-else class="instance-grid">
        <div
          v-for="instance in instances"
          :key="instance.id"
          :class="['instance-card', getStatusClass(instance.status)]"
        >
          <div class="instance-header">
            <div class="instance-name">
              <i class="el-icon-s-platform"></i>
              <span>{{ instance.name }}</span>
            </div>
            <el-tag :type="getStatusTagType(instance.status)" effect="dark" size="mini">
              {{ getStatusText(instance.status) }}
            </el-tag>
          </div>

          <div class="instance-info">
            <div class="info-row">
              <span class="label">核心:</span>
              <span class="value">{{ instance.coreType }} {{ instance.version }}</span>
            </div>
            <div class="info-row">
              <span class="label">内存:</span>
              <span class="value">{{ instance.jvmXms }}MB - {{ instance.jvmXmx }}MB</span>
            </div>
          </div>

          <div class="instance-actions">
            <el-button
              icon="el-icon-monitor"
              size="small"
              type="primary"
              @click="goToTerminal(instance)"
            >控制台
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <div slot="footer" class="dialog-footer">
      <el-button @click="visible = false">关闭</el-button>
      <el-button type="primary" @click="goToNodeDetail">查看节点详情</el-button>
    </div>
  </el-dialog>
</template>

<script>
import {listMcs} from '@/api/node/mcs'

export default {
  name: 'NodeInstanceDialog',
  props: {
    value: {
      type: Boolean,
      default: false
    },
    node: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      loading: false,
      instances: []
    }
  },
  computed: {
    visible: {
      get() {
        return this.value
      },
      set(val) {
        this.$emit('input', val)
      }
    },
    dialogTitle() {
      return `${this.node.name || '节点'} - 实例管理`
    }
  },
  watch: {
    value(val) {
      if (val && this.node.id) {
        this.loadInstances()
      }
    }
  },
  methods: {
    async loadInstances() {
      if (!this.node.id) return
      this.loading = true
      try {
        const res = await listMcs({nodeId: this.node.id})
        if (res.code === 200) {
          // 接口返回格式: {rows: [...], total: n}
          const serverList = res.rows || []
          this.instances = serverList
        }
      } catch (error) {
        console.error('加载实例列表失败:', error)
        this.$message.error('加载实例列表失败')
      } finally {
        this.loading = false
      }
    },
    getStatusText(status) {
      const map = {'0': '未启动', '1': '运行中', '2': '已停止', '3': '异常'}
      return map[status] || '未知'
    },
    getStatusTagType(status) {
      const map = {'0': 'info', '1': 'success', '2': 'info', '3': 'danger'}
      return map[status] || 'info'
    },
    getStatusClass(status) {
      const map = {'0': 'stopped', '1': 'running', '2': 'stopped', '3': 'error'}
      return map[status] || 'stopped'
    },
    getNodeStatusText(status) {
      const map = {'0': '在线', '1': '离线', '2': '故障'}
      return map[status] || '未知'
    },
    getNodeTagType(status) {
      const map = {'0': 'success', '1': 'info', '2': 'danger'}
      return map[status] || 'info'
    },
    goToTerminal(instance) {
      this.visible = false
      this.$router.push({
        path: '/node/mcs/terminal',
        query: {serverId: instance.id}
      })
    },
    goToAddInstance() {
      this.visible = false
      this.$router.push({
        path: '/node/mcs/index',
        query: {nodeId: this.node.id, nodeUuid: this.node.uuid}
      })
    },
    goToNodeDetail() {
      this.visible = false
      this.$router.push({
        path: '/node/mcs/index',
        query: {nodeId: this.node.id, nodeUuid: this.node.uuid}
      })
    },
    handleClose() {
      this.instances = []
    }
  }
}
</script>


<style lang="scss" scoped>
.node-instance-dialog {
  ::v-deep .el-dialog {
    border-radius: 12px;
    overflow: hidden;
  }

  ::v-deep .el-dialog__header {
    background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
    padding: 16px 20px;

    .el-dialog__title {
      color: #fff;
      font-weight: 600;
    }

    .el-dialog__headerbtn .el-dialog__close {
      color: #fff;
    }
  }

  ::v-deep .el-dialog__body {
    padding: 0;
    max-height: 60vh;
    overflow-y: auto;
  }
}

.node-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: #f8f9fa;
  border-bottom: 1px solid #ebeef5;

  .node-basic {
    display: flex;
    align-items: center;

    .node-icon {
      width: 48px;
      height: 48px;
      border-radius: 10px;
      background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 14px;

      i {
        font-size: 24px;
        color: #fff;
      }
    }

    .node-meta {
      .node-name {
        font-size: 16px;
        font-weight: 600;
        color: #303133;
        margin-bottom: 6px;
      }

      .node-tags {
        display: flex;
        gap: 6px;
      }
    }
  }

  .node-actions {
    display: flex;
    gap: 8px;
  }
}

.instance-list {
  padding: 20px;
  min-height: 200px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #909399;

  i {
    font-size: 64px;
    margin-bottom: 16px;
  }

  p {
    margin-bottom: 16px;
    font-size: 14px;
  }
}

.instance-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 16px;
}

.instance-card {
  background: #fff;
  border-radius: 10px;
  padding: 16px;
  border: 1px solid #e4e7ed;
  transition: all 0.3s;

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    transform: translateY(-2px);
  }

  &.running {
    border-left: 4px solid #67c23a;
  }

  &.stopped {
    border-left: 4px solid #909399;
  }

  &.error {
    border-left: 4px solid #f56c6c;
  }

  .instance-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;

    .instance-name {
      display: flex;
      align-items: center;
      font-weight: 600;
      color: #303133;

      i {
        margin-right: 8px;
        color: #409EFF;
      }
    }
  }

  .instance-info {
    margin-bottom: 14px;
    padding: 10px;
    background: #f8f9fa;
    border-radius: 6px;

    .info-row {
      display: flex;
      font-size: 13px;
      margin-bottom: 4px;

      &:last-child {
        margin-bottom: 0;
      }

      .label {
        color: #909399;
        width: 40px;
      }

      .value {
        color: #606266;
      }
    }
  }

  .instance-actions {
    display: flex;
    justify-content: flex-end;
    align-items: center;
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
