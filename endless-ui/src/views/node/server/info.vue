<template>
  <div v-loading="loading" class="app-container" element-loading-text="正在加载服务器信息...">
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span>服务器主机信息</span>
        <div class="header-operations">
          <el-select v-model="pollingInterval" size="mini" style="margin-right: 15px;" @change="handleIntervalChange">
            <el-option :value="3000" label="3秒"/>
            <el-option :value="5000" label="5秒"/>
            <el-option :value="10000" label="10秒"/>
            <el-option :value="30000" label="30秒"/>
            <el-option :value="60000" label="1分钟"/>
          </el-select>
          <el-button style="padding: 3px 0" type="text" @click="goBack">返回</el-button>
        </div>
      </div>

      <!-- 关键指标卡片 -->
      <el-row :gutter="20" class="metrics-row">
        <el-col :span="6">
          <el-card class="metric-card" shadow="hover">
            <div class="metric-title">系统负载</div>
            <div class="metric-value">{{ formatLoad(loadInfo.cpu && loadInfo.cpu.load) }}</div>
            <div class="metric-detail">用户:
              {{ formatLoad(loadInfo.cpu && loadInfo.cpu.loadDetail && loadInfo.cpu.loadDetail.user) }}
            </div>
            <div class="metric-detail">系统:
              {{ formatLoad(loadInfo.cpu && loadInfo.cpu.loadDetail && loadInfo.cpu.loadDetail.system) }}
            </div>
            <el-progress
              :color="getCpuColor"
              :percentage="Math.round(loadInfo.cpu && loadInfo.cpu.load || 0)"
              :show-text="false"
              class="metric-progress"
            />
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="metric-card" shadow="hover">
            <div class="metric-title">CPU使用率</div>
            <div class="metric-value">
              {{ formatLoad(loadInfo.cpu && loadInfo.cpu.loadDetail && loadInfo.cpu.loadDetail.system) }}
            </div>
            <div class="metric-detail">物理核心: {{ serverInfo.cpu && serverInfo.cpu.physicalProcessorCount }} 核</div>
            <div class="metric-detail">逻辑核心: {{ serverInfo.cpu && serverInfo.cpu.logicalProcessorCount }} 核</div>
            <el-progress
              :percentage="Math.round(loadInfo.cpu && loadInfo.cpu.loadDetail && loadInfo.cpu.loadDetail.system || 0)"
              :show-text="false"
              class="metric-progress"
              color="#F56C6C"
            />
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="metric-card" shadow="hover">
            <div class="metric-title">内存使用率</div>
            <div class="metric-value">{{ formatLoad(loadInfo.memoryLoad) }}</div>
            <div class="metric-detail">总内存:
              {{ formatBytes(serverInfo.memory && serverInfo.memory.physical && serverInfo.memory.physical.total) }}
            </div>
            <div class="metric-detail">已用:
              {{ formatBytes(serverInfo.memory && serverInfo.memory.physical && serverInfo.memory.physical.used) }}
            </div>
            <el-progress
              :color="getMemoryColor"
              :percentage="Math.round(loadInfo.memoryLoad || 0)"
              :show-text="false"
              class="metric-progress"
            />
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="metric-card" shadow="hover">
            <div class="metric-title">网络流量</div>
            <div class="metric-value">
              <el-tooltip content="实时发送速率" placement="top">
                <div class="packet-rate">
                  <span class="direction">↑</span>
                  {{ formatNetworkSpeed(loadInfo.network && loadInfo.network.bytesSentPerSec) }}
                </div>
              </el-tooltip>
              <el-tooltip content="实时接收速率" placement="top">
                <div class="packet-rate">
                  <span class="direction">↓</span>
                  {{ formatNetworkSpeed(loadInfo.network && loadInfo.network.bytesRecvPerSec) }}
                </div>
              </el-tooltip>
            </div>
            <div class="metric-detail">
              <el-tooltip content="总发送流量" placement="right">
                <span>↑ {{ formatBytes(loadInfo.network && loadInfo.network.totalBytesSent) }}</span>
              </el-tooltip>
            </div>
            <div class="metric-detail">
              <el-tooltip content="总接收流量" placement="right">
                <span>↓ {{ formatBytes(loadInfo.network && loadInfo.network.totalBytesRecv) }}</span>
              </el-tooltip>
            </div>
            <el-progress
              :color="getNetworkColor"
              :percentage="calculateNetworkPercentage(loadInfo.network)"
              :show-text="false"
              class="metric-progress"
            />
          </el-card>
        </el-col>
      </el-row>

      <!-- CPU信息 -->
      <el-card class="info-card">
        <div slot="header">
          <span>CPU信息</span>
        </div>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="处理器">{{ serverInfo.cpu && serverInfo.cpu.name }}</el-descriptions-item>
          <el-descriptions-item label="标识符">{{ serverInfo.cpu && serverInfo.cpu.identifier }}</el-descriptions-item>
          <el-descriptions-item label="厂商">{{ serverInfo.cpu && serverInfo.cpu.vendor }}</el-descriptions-item>
          <el-descriptions-item label="架构">{{
              serverInfo.cpu && serverInfo.cpu.microarchitecture
            }}
          </el-descriptions-item>
          <el-descriptions-item label="物理核心数">{{
              serverInfo.cpu && serverInfo.cpu.physicalProcessorCount
            }}
          </el-descriptions-item>
          <el-descriptions-item label="逻辑核心数">{{
              serverInfo.cpu && serverInfo.cpu.logicalProcessorCount
            }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 内存信息 -->
      <el-card class="info-card">
        <div slot="header">
          <span>内存信息</span>
        </div>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="物理内存总量">
            {{ formatBytes(serverInfo.memory && serverInfo.memory.physical && serverInfo.memory.physical.total) }}
          </el-descriptions-item>
          <el-descriptions-item label="物理内存使用">
            {{ formatBytes(serverInfo.memory && serverInfo.memory.physical && serverInfo.memory.physical.used) }}
          </el-descriptions-item>
          <el-descriptions-item label="物理内存可用">
            {{ formatBytes(serverInfo.memory && serverInfo.memory.physical && serverInfo.memory.physical.available) }}
          </el-descriptions-item>
          <el-descriptions-item label="物理内存使用率">
            {{ formatLoad(serverInfo.memory && serverInfo.memory.physical && serverInfo.memory.physical.usedPercent) }}
          </el-descriptions-item>
          <el-descriptions-item label="交换内存总量">
            {{ formatBytes(serverInfo.memory && serverInfo.memory.swap && serverInfo.memory.swap.total) }}
          </el-descriptions-item>
          <el-descriptions-item label="交换内存使用">
            {{ formatBytes(serverInfo.memory && serverInfo.memory.swap && serverInfo.memory.swap.used) }}
          </el-descriptions-item>
          <el-descriptions-item label="交换内存使用率">
            {{ formatLoad(serverInfo.memory && serverInfo.memory.swap && serverInfo.memory.swap.usedPercent) }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 磁盘信息 -->
      <el-card class="info-card">
        <div slot="header">
          <span>磁盘信息</span>
        </div>
        <el-table :data="serverInfo.disks" style="width: 100%">
          <el-table-column label="磁盘名称" prop="name"/>
          <el-table-column label="挂载点" prop="mount"/>
          <el-table-column label="文件系统" prop="type"/>
          <el-table-column label="使用率">
            <template slot-scope="scope">
              <el-progress :color="getDiskColor(scope.row.usedPercent)"
                           :percentage="Math.round(scope.row.usedPercent)"/>
            </template>
          </el-table-column>
          <el-table-column label="总容量">
            <template slot-scope="scope">
              {{ formatBytes(scope.row.total) }}
            </template>
          </el-table-column>
          <el-table-column label="已用">
            <template slot-scope="scope">
              {{ formatBytes(scope.row.used) }}
            </template>
          </el-table-column>
          <el-table-column label="可用">
            <template slot-scope="scope">
              {{ formatBytes(scope.row.usable) }}
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 网络信息 -->
      <el-card class="info-card">
        <div slot="header">
          <span>网络信息</span>
        </div>
        <el-table :data="serverInfo.network" style="width: 100%">
          <el-table-column label="网卡名称" prop="displayName"/>
          <el-table-column label="MAC地址" prop="mac"/>
          <el-table-column label="IPv4地址">
            <template slot-scope="scope">
              {{ scope.row.ipv4 && scope.row.ipv4.join(', ') }}
            </template>
          </el-table-column>
          <el-table-column label="速度" prop="speed">
            <template slot-scope="scope">
              {{ formatSpeed(scope.row.speed) }}
            </template>
          </el-table-column>
          <el-table-column label="接收">
            <template slot-scope="scope">
              <div>数据包: {{ scope.row.packetsRecv }}</div>
              <div>字节: {{ formatBytes(scope.row.bytesRecv) }}</div>
            </template>
          </el-table-column>
          <el-table-column label="发送">
            <template slot-scope="scope">
              <div>数据包: {{ scope.row.packetsSent }}</div>
              <div>字节: {{ formatBytes(scope.row.bytesSent) }}</div>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 系统信息 -->
      <el-card class="info-card">
        <div slot="header">
          <span>系统信息</span>
        </div>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="操作系统">{{ serverInfo.os && serverInfo.os.name }}</el-descriptions-item>
          <el-descriptions-item label="版本">{{ serverInfo.os && serverInfo.os.version }}</el-descriptions-item>
          <el-descriptions-item label="制造商">{{ serverInfo.os && serverInfo.os.manufacturer }}</el-descriptions-item>
          <el-descriptions-item label="位数">{{ serverInfo.os && serverInfo.os.bitness }}位</el-descriptions-item>
          <el-descriptions-item label="启动时间">{{
              formatTime(serverInfo.os && serverInfo.os.bootTime)
            }}
          </el-descriptions-item>
          <el-descriptions-item label="运行时间">{{
              formatUptime(serverInfo.os && serverInfo.os.uptime)
            }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>
    </el-card>
  </div>
</template>

<script>
import {getServerInfo, getServerLoad} from "@/api/node/server";

export default {
  name: "ServerInfo",
  data() {
    return {
      serverInfo: {},
      loadInfo: {
        cpu: {
          load: 0,
          loadDetail: {
            system: 0,
            softirq: 0,
            idle: 100,
            steal: 0,
            irq: 0,
            iowait: 0,
            user: 0,
            nice: 0
          }
        },
        memoryLoad: 0,
        network: {
          bytesRecvPerSec: 0,
          bytesSentPerSec: 0,
          totalBytesRecv: 0,
          totalBytesSent: 0
        }
      },
      loading: true,
      timer: null,
      pollingInterval: 5000 // 默认5秒轮询一次
    };
  },
  created() {
    const id = this.$route.params.id;
    this.getInfo(id);
    this.startPolling(id);
  },
  beforeDestroy() {
    this.stopPolling();
  },
  methods: {
    getInfo(id) {
      this.loading = true;
      getServerInfo(id).then(response => {
        this.serverInfo = response.data;
        this.loading = false;
      }).catch(() => {
        this.loading = false;
      });
    },
    getLoad(id) {
      getServerLoad(id).then(response => {
        if (response.data && response.data.load) {
          this.loadInfo = response.data.load;
        }
      });
    },
    startPolling(id) {
      this.getLoad(id); // 立即执行一次
      this.timer = setInterval(() => {
        this.getLoad(id);
      }, this.pollingInterval);
    },
    stopPolling() {
      if (this.timer) {
        clearInterval(this.timer);
        this.timer = null;
      }
    },
    goBack() {
      this.$router.go(-1);
    },
    formatLoad(value) {
      if (!value) return '0%';
      return Math.round(value) + '%';
    },
    formatNetworkSpeed(bytesPerSec) {
      if (!bytesPerSec) return '0 B/s';
      const k = 1024;
      const sizes = ['B/s', 'KB/s', 'MB/s', 'GB/s'];
      const i = Math.floor(Math.log(bytesPerSec) / Math.log(k));
      return parseFloat((bytesPerSec / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    },
    calculateNetworkPercentage(network) {
      if (!network) return 0;
      // 假设最大传输速率为1Gbps
      const maxBytesPerSec = 1024 * 1024 * 1024 / 8; // 1Gbps转换为字节每秒
      const totalSpeed = (network.bytesSentPerSec || 0) + (network.bytesRecvPerSec || 0);
      const percentage = (totalSpeed / maxBytesPerSec) * 100;
      return Math.min(Math.round(percentage), 100);
    },
    getNetworkColor(percentage) {
      if (percentage < 50) return '#67C23A';
      if (percentage < 80) return '#E6A23C';
      return '#F56C6C';
    },
    formatBytes(bytes) {
      if (!bytes) return '0 B';
      const k = 1024;
      const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
      const i = Math.floor(Math.log(bytes) / Math.log(k));
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    },
    formatSpeed(speed) {
      if (!speed) return '0 Kbps';
      if (speed >= 1000000000) {
        return (speed / 1000000000).toFixed(1) + ' Gbps';
      } else if (speed >= 1000000) {
        return (speed / 1000000).toFixed(1) + ' Mbps';
      } else {
        return (speed / 1000).toFixed(1) + ' Kbps';
      }
    },
    formatTime(timestamp) {
      if (!timestamp) return '-';
      return new Date(timestamp * 1000).toLocaleString();
    },
    formatUptime(seconds) {
      if (!seconds) return '-';
      const days = Math.floor(seconds / 86400);
      const hours = Math.floor((seconds % 86400) / 3600);
      const minutes = Math.floor((seconds % 3600) / 60);
      return `${days}天 ${hours}小时 ${minutes}分钟`;
    },
    getCpuColor(percentage) {
      if (percentage < 50) return '#67C23A';
      if (percentage < 80) return '#E6A23C';
      return '#F56C6C';
    },
    getMemoryColor(percentage) {
      if (percentage < 50) return '#67C23A';
      if (percentage < 80) return '#E6A23C';
      return '#F56C6C';
    },
    getSwapColor(percentage) {
      if (percentage < 50) return '#67C23A';
      if (percentage < 80) return '#E6A23C';
      return '#F56C6C';
    },
    getDiskColor(percentage) {
      if (percentage < 50) return '#67C23A';
      if (percentage < 80) return '#E6A23C';
      return '#F56C6C';
    },
    formatPacketRate(packets) {
      if (!packets) return '0';
      return packets.toLocaleString();
    },
    formatTransferRate(bytesPerSec) {
      if (!bytesPerSec) return '0 KB/s';
      const k = 1024;
      if (bytesPerSec < k * k) {
        return (bytesPerSec / k).toFixed(2) + ' KB/s';
      } else {
        return (bytesPerSec / (k * k)).toFixed(2) + ' MB/s';
      }
    },
    handleIntervalChange(value) {
      // 重启轮询
      this.stopPolling();
      this.startPolling(this.$route.params.id);
    }
  }
};
</script>

<style lang="scss" scoped>
.metrics-row {
  margin-bottom: 20px;

  .metric-card {
    height: 160px;
    text-align: center;
    position: relative;

    .metric-title {
      font-size: 14px;
      color: #606266;
      margin-bottom: 10px;
    }

    .metric-value {
      font-size: 24px;
      font-weight: bold;
      color: #303133;
      margin-bottom: 10px;
    }

    .metric-detail {
      font-size: 12px;
      color: #909399;
      margin-bottom: 4px;
    }

    .metric-progress {
      position: absolute;
      bottom: 20px;
      left: 20px;
      right: 20px;
    }
  }
}

.info-card {
  margin-bottom: 20px;
}

.el-descriptions {
  margin: 20px;

  ::v-deep .el-descriptions-item__label {
    width: 120px;
    font-weight: bold;
  }
}

.header-operations {
  float: right;
  display: flex;
  align-items: center;
}

.metric-value {
  .packet-rate {
    font-size: 16px;
    margin-bottom: 4px;

    .direction {
      color: #409EFF;
      margin-right: 4px;
    }
  }
}
</style>
