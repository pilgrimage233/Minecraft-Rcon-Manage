<template>
  <div class="dashboard-container">
    <!-- 测试按钮 - 仅在有更新时显示 -->
    <el-button
      v-if="hasUpdate"
      size="mini"
      style="margin-bottom: 16px;"
      type="primary"
      @click="testUpdateDialog"
    >
      查看更新
    </el-button>

    <!-- 版本更新弹窗 -->
    <el-dialog
      :close-on-click-modal="false"
      :visible.sync="updateDialogVisible"
      center
      class="update-dialog"
      title="版本更新提示"
      width="500px"
    >
      <div class="update-dialog-content">
        <div class="update-icon">
          <i class="el-icon-download"></i>
        </div>
        <div class="update-title">发现新版本可用！</div>
        <div class="version-compare">
          <div class="version-item current">
            <div class="version-label">当前版本</div>
            <div class="version-number">{{ currentVersion }}</div>
          </div>
          <div class="version-arrow">
            <i class="el-icon-right"></i>
          </div>
          <div class="version-item latest">
            <div class="version-label">最新版本</div>
            <div class="version-number">{{ latestVersion }}</div>
          </div>
        </div>
        <div v-if="releaseNotes" class="release-notes">
          <div class="notes-header">
            <i class="el-icon-document"></i>
            <span>更新内容</span>
          </div>
          <div class="notes-body" v-html="parsedReleaseNotes"></div>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="remindLater">3天后提醒</el-button>
        <el-button type="primary" @click="goToDownload">
          <i class="el-icon-download"></i>
          立即更新
        </el-button>
      </span>
    </el-dialog>

    <!-- 统计数据卡片 -->
    <div v-loading="loadingStates.basicStats || loadingStates.nodeStats" class="stats-section"
         element-loading-text="加载中...">
      <div class="section-header">
        <i class="el-icon-data-analysis"></i>
        <span>数据概览</span>
      </div>
      <el-row :gutter="16">
        <el-col v-for="(item, index) in statsCards" :key="index" :lg="4" :md="8" :sm="12" :xs="12">
          <div :class="['stats-card', item.type]" @click="handleCardClick(item)">
            <div class="card-icon">
              <i :class="item.icon"></i>
            </div>
            <div class="card-info">
              <div class="card-value">
                <span class="number">{{ animatedValues[index] || 0 }}</span>
              </div>
              <div class="card-label">{{ item.label }}</div>
            </div>
            <div class="card-wave"></div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 节点状态卡片 -->
    <div v-if="nodeList.length > 0 || loadingStates.nodeStats" v-loading="loadingStates.nodeStats" class="node-section"
         element-loading-text="加载节点信息...">
      <div class="section-header">
        <i class="el-icon-connection"></i>
        <span>节点状态</span>
        <el-tag v-if="!loadingStates.nodeStats" :type="onlineNodeCount === nodeCount ? 'success' : 'warning'"
                class="node-status-tag" size="small">
          {{ onlineNodeCount }}/{{ nodeCount }} 在线
        </el-tag>
      </div>
      <el-row :gutter="16">
        <el-col v-for="node in nodeList" :key="node.id" :lg="6" :md="8" :sm="12" :xs="24">
          <div :class="['node-card', getNodeStatusClass(node.status)]" @click="openNodeDialog(node)">
            <div class="node-header">
              <div class="node-name">
                <i class="el-icon-monitor"></i>
                <span>{{ node.name }}</span>
              </div>
              <div class="node-status-wrapper">
                <span :class="['status-pulse', getNodeStatusClass(node.status)]"></span>
                <el-tag :type="getNodeTagType(node.status)" effect="dark" size="mini">
                  {{ getNodeStatusText(node.status) }}
                </el-tag>
              </div>
            </div>
            <div class="node-info">
              <div class="info-item">
                <span class="label">系统:</span>
                <span class="value">{{ node.osType || '未知' }}</span>
              </div>
              <div class="info-item">
                <span class="label">版本:</span>
                <span class="value">{{ node.version || '未知' }}</span>
              </div>
              <div class="info-item">
                <span class="label">心跳:</span>
                <span class="value">{{ formatHeartbeat(node.lastHeartbeat) }}</span>
              </div>
            </div>
            <div class="node-action-hint">
              <i class="el-icon-right"></i>
              <span>点击管理实例</span>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <el-row :gutter="20" style="margin-top: 20px">
      <!-- 在线玩家信息 -->
      <el-col :lg="12" :md="24">
        <el-card v-loading="loadingStates.onlinePlayer" class="dashboard-card online-card"
                 element-loading-text="加载在线信息...">
          <div slot="header" class="card-header">
            <div class="header-left">
              <i class="el-icon-user"></i>
              <span>实时在线信息</span>
              <el-tag v-if="serverList.length > 0 && !loadingStates.onlinePlayer" effect="plain" size="mini"
                      type="success">
                {{ totalOnlinePlayers }} 人在线
              </el-tag>
            </div>
            <div class="header-right">
              <el-button v-if="serverList.length > maxDisplayServers" type="text" @click="showAllServers">
                查看全部 ({{ serverList.length }})
              </el-button>
              <el-button :loading="refreshing" type="text" @click="getStats">
                <i v-if="!refreshing" class="el-icon-refresh"></i> 刷新
              </el-button>
            </div>
          </div>
          <div v-if="serverList.length > 0 && !loadingStates.onlinePlayer" class="online-info-wrapper">
            <div v-for="(server, index) in displayServerList" :key="index" class="server-status"
                 @click="showServerDetail(server)">
              <div :class="['status-icon', server.isError ? 'error' : 'online']">
                <i :class="server.isError ? 'el-icon-warning' : 'el-icon-success'"></i>
              </div>
              <div class="status-info">
                <div class="server-name">{{ server.name }}</div>
                <div v-if="!server.isError" class="player-count">
                  <span class="count">{{ server.playerCount }}</span>
                  <span class="label">在线玩家</span>
                </div>
                <div v-else class="error-msg">{{ server.errorMsg }}</div>
              </div>
              <div class="server-arrow">
                <i class="el-icon-arrow-right"></i>
              </div>
            </div>
            <div class="query-time">
              <i class="el-icon-time"></i>
              <span>{{ queryTime }}</span>
            </div>
          </div>
          <div v-else-if="!loadingStates.onlinePlayer" class="empty-state">
            <i class="el-icon-warning-outline"></i>
            <span>暂无在线数据</span>
          </div>
        </el-card>
      </el-col>

      <!-- 全部服务器弹窗 -->
      <el-dialog :visible.sync="allServersDialogVisible" append-to-body title="全部服务器在线信息" width="600px">
        <div class="all-servers-list">
          <div v-for="(server, index) in serverList" :key="index" class="server-item" @click="showServerDetail(server)">
            <div :class="['status-dot', server.isError ? 'error' : 'online']"></div>
            <div class="server-info">
              <div class="server-name">{{ server.name }}</div>
              <div v-if="!server.isError" class="player-info">
                <span class="player-count">{{ server.playerCount }} 人在线</span>
              </div>
              <div v-else class="error-info">{{ server.errorMsg }}</div>
            </div>
            <i class="el-icon-arrow-right"></i>
          </div>
        </div>
        <div class="dialog-footer">
          <span class="query-time">查询时间: {{ queryTime }}</span>
        </div>
      </el-dialog>

      <!-- 服务器详情弹窗 -->
      <el-dialog :title="currentServer.name + ' - 在线玩家'" :visible.sync="serverDetailDialogVisible" append-to-body
                 width="500px">
        <div v-if="currentServer && !currentServer.isError" class="server-detail">
          <div class="detail-header">
            <div class="player-total">
              <span class="number">{{ currentServer.playerCount }}</span>
              <span class="label">在线玩家</span>
            </div>
          </div>
          <div class="player-list">
            <div v-if="currentServer.players && currentServer.players.length > 0">
              <el-tag v-for="(player, idx) in currentServer.players" :key="idx" class="player-tag" effect="plain"
                      size="medium">
                {{ player }}
              </el-tag>
            </div>
            <div v-else class="no-players">
              <i class="el-icon-user"></i>
              <span>当前没有玩家在线</span>
            </div>
          </div>
        </div>
        <div v-else class="server-error">
          <i class="el-icon-warning"></i>
          <span>{{ currentServer.errorMsg }}</span>
        </div>
      </el-dialog>

      <!-- 游戏时长排行榜 -->
      <el-col :lg="12" :md="24">
        <el-card v-loading="loadingStates.topPlayers" class="dashboard-card rank-card"
                 element-loading-text="加载排行榜...">
          <div slot="header" class="card-header">
            <div class="header-left">
              <i class="el-icon-trophy"></i>
              <span>游戏时长排行榜</span>
              <el-tag effect="plain" size="mini" type="warning">TOP 10</el-tag>
            </div>
          </div>
          <el-table
            :data="topTenPlayers"
            :header-cell-style="{ background: '#fafafa', color: '#606266', fontWeight: '600' }"
            stripe
            style="width: 100%"
            size="small"
          >
            <el-table-column align="center" label="排名" width="70">
              <template slot-scope="scope">
                <div :class="'rank-' + (scope.$index + 1)" class="rank-badge">
                  <span v-if="scope.$index < 3">
                    <i :class="getRankIcon(scope.$index)"></i>
                  </span>
                  <span v-else>{{ scope.$index + 1 }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="玩家" prop="userName">
              <template slot-scope="scope">
                <div class="player-cell">
                  <el-avatar :size="28" icon="el-icon-user"></el-avatar>
                  <span class="player-name">{{ scope.row.userName }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="QQ" prop="qq" width="120"/>
            <el-table-column label="游戏时长" width="130">
              <template slot-scope="scope">
                <span class="game-time">{{ formatGameTime(scope.row.gameTime) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="最后在线" prop="lastOnlineTime" width="160"/>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 节点实例管理弹窗 -->
    <NodeInstanceDialog v-model="nodeDialogVisible" :node="selectedNode"/>
  </div>
</template>

<script>
import {getBasicStats, getNodeStats, getOnlinePlayerInfo, getTopPlayers} from '@/api/dashboard'
import {mapActions, mapState} from 'vuex'
import NodeInstanceDialog from './index/NodeInstanceDialog.vue'
import {marked} from 'marked'
import DOMPurify from 'dompurify'

export default {
  name: 'Index',
  components: {
    NodeInstanceDialog
  },
  data() {
    return {
      statsData: {},
      onlineInfo: null,
      topTenPlayers: [],
      loading: false,
      refreshing: false,
      animatedValues: [],
      nodeList: [],
      nodeCount: 0,
      onlineNodeCount: 0,
      // 在线玩家相关
      serverList: [],
      queryTime: '',
      maxDisplayServers: 3,
      allServersDialogVisible: false,
      serverDetailDialogVisible: false,
      currentServer: {},
      // 节点实例弹窗
      nodeDialogVisible: false,
      selectedNode: {},
      // 加载状态
      loadingStates: {
        basicStats: false,
        nodeStats: false,
        topPlayers: false,
        onlinePlayer: false
      },
      // 版本更新弹窗
      updateDialogVisible: false
    }
  },
  computed: {
    ...mapState('version', [
      'currentVersion',
      'latestVersion',
      'hasUpdate',
      'releaseNotes',
      'downloadUrl'
    ]),
    // 解析 Markdown 格式的更新说明
    parsedReleaseNotes() {
      if (!this.releaseNotes) return ''
      try {
        // 使用 marked 解析 markdown
        const rawHtml = marked(this.releaseNotes)
        // 使用 DOMPurify 清理 HTML，防止 XSS 攻击
        return DOMPurify.sanitize(rawHtml)
      } catch (error) {
        console.error('解析 Markdown 失败:', error)
        // 如果解析失败，返回纯文本
        return this.releaseNotes
      }
    },
    displayServerList() {
      return this.serverList.slice(0, this.maxDisplayServers)
    },
    totalOnlinePlayers() {
      return this.serverList.reduce((total, server) => {
        return total + (server.isError ? 0 : (server.playerCount || 0))
      }, 0)
    },
    statsCards() {
      return [
        {
          label: '服务器数量',
          value: this.statsData.serverCount || 0,
          type: 'primary',
          icon: 'el-icon-s-platform',
          route: '/mc/serverlist'
        },
        {
          label: '节点数量',
          value: this.nodeCount || 0,
          type: 'cyan',
          icon: 'el-icon-connection',
          route: '/nodelist/server'
        },
        {
          label: '白名单数量',
          value: this.statsData.whiteListCount || 0,
          type: 'success',
          icon: 'el-icon-user',
          route: '/mc/whitelist'
        },
        {
          label: '申请数量',
          value: this.statsData.applyCount || 0,
          type: 'info',
          icon: 'el-icon-document',
          route: '/mc/whitelist'
        },
        {
          label: '封禁数量',
          value: this.statsData.banCount || 0,
          type: 'danger',
          icon: 'el-icon-lock',
          route: '/mc/banlist'
        },
        {
          label: '管理员数量',
          value: this.statsData.opCount || 0,
          type: 'purple',
          icon: 'el-icon-s-custom',
          route: '/mc/operator'
        }
      ]
    }
  },
  created() {
    this.getStats()
    this.checkUpdateAndShow()
  },
  watch: {
    // 监听 hasUpdate 的变化
    hasUpdate(newVal) {
      if (newVal) {
        this.showUpdateDialogIfNeeded()
      }
    }
  },
  methods: {
    ...mapActions('version', ['checkUpdate']),
    // 检查更新并显示弹窗
    async checkUpdateAndShow() {
      await this.checkUpdate()
      await this.$nextTick()

      // 由于 hasUpdate 可能异步更新，通过 watch 来处理
      // 如果已经是 true，直接显示
      if (this.hasUpdate) {
        this.showUpdateDialogIfNeeded()
      }
    },
    // 显示更新弹窗（如果需要）
    showUpdateDialogIfNeeded() {
      if (!this.hasUpdate) return

      // 检查是否在3天内已提醒过
      const remindKey = `update_remind_${this.latestVersion}`
      const lastRemindTime = localStorage.getItem(remindKey)

      if (lastRemindTime) {
        const threeDaysInMs = 3 * 24 * 60 * 60 * 1000
        const timePassed = Date.now() - parseInt(lastRemindTime)
        if (timePassed < threeDaysInMs) {
          return // 3天内不再提醒
        }
      }

      // 显示更新弹窗
      this.updateDialogVisible = true
    },
    // 3天后提醒
    remindLater() {
      const remindKey = `update_remind_${this.latestVersion}`
      localStorage.setItem(remindKey, Date.now().toString())
      this.updateDialogVisible = false
      this.$message.success('已设置3天后提醒')
    },
    // 查看更新（清除提醒记录并显示弹窗）
    testUpdateDialog() {
      // 清除所有提醒记录
      Object.keys(localStorage)
        .filter(k => k.startsWith('update_remind_'))
        .forEach(k => localStorage.removeItem(k))
      // 显示弹窗
      this.updateDialogVisible = true
    },
    async getStats() {
      if (this.refreshing) return
      this.refreshing = true

      // 并行加载所有数据，互不阻塞
      const promises = [
        this.loadBasicStats(),
        this.loadNodeStats(),
        this.loadTopPlayers(),
        this.loadOnlinePlayerInfo()
      ]

      // 等待所有请求完成
      await Promise.allSettled(promises)

      this.refreshing = false
    },
    // 加载基础统计数据
    async loadBasicStats() {
      this.loadingStates.basicStats = true
      try {
        const response = await getBasicStats()
        if (response.code === 200) {
          this.statsData = {
            ...this.statsData,
            ...response.data
          }
          this.animateValues()
        }
      } catch (error) {
        console.error('获取基础统计数据失败:', error)
        this.$message.warning('基础统计数据加载失败')
      } finally {
        this.loadingStates.basicStats = false
      }
    },
    // 加载节点统计信息
    async loadNodeStats() {
      this.loadingStates.nodeStats = true
      try {
        const response = await getNodeStats()
        if (response.code === 200) {
          this.nodeCount = response.data.nodeCount || 0
          this.onlineNodeCount = response.data.onlineNodeCount || 0
          this.nodeList = response.data.nodeList || []
          this.statsData = {
            ...this.statsData,
            nodeCount: this.nodeCount
          }
          this.animateValues()
        }
      } catch (error) {
        console.error('获取节点统计信息失败:', error)
        this.$message.warning('节点统计信息加载失败')
      } finally {
        this.loadingStates.nodeStats = false
      }
    },
    // 加载游戏时长排行榜
    async loadTopPlayers() {
      this.loadingStates.topPlayers = true
      try {
        const response = await getTopPlayers()
        if (response.code === 200) {
          this.topTenPlayers = response.data || []
        }
      } catch (error) {
        console.error('获取游戏时长排行榜失败:', error)
        this.$message.warning('游戏时长排行榜加载失败')
      } finally {
        this.loadingStates.topPlayers = false
      }
    },
    // 加载在线玩家信息
    async loadOnlinePlayerInfo() {
      this.loadingStates.onlinePlayer = true
      try {
        const response = await getOnlinePlayerInfo()
        if (response.code === 200) {
          this.onlineInfo = response.data
          this.parseOnlineInfo(response.data)
        }
      } catch (error) {
        console.error('获取在线玩家信息失败:', error)
        // 在线玩家信息失败不显示错误提示，因为这个接口最容易超时
        this.serverList = []
        this.queryTime = ''
      } finally {
        this.loadingStates.onlinePlayer = false
      }
    },
    animateValues() {
      this.statsCards.forEach((card, index) => {
        this.$set(this.animatedValues, index, 0)
        const target = card.value
        const duration = 1000
        const step = target / (duration / 16)
        let current = 0
        const timer = setInterval(() => {
          current += step
          if (current >= target) {
            this.$set(this.animatedValues, index, target)
            clearInterval(timer)
          } else {
            this.$set(this.animatedValues, index, Math.floor(current))
          }
        }, 16)
      })
    },
    formatGameTime(minutes) {
      if (!minutes) return '0分钟'
      const hours = Math.floor(minutes / 60)
      const remainingMinutes = minutes % 60
      if (hours > 0) {
        return `${hours}小时${remainingMinutes}分钟`
      }
      return `${remainingMinutes}分钟`
    },
    formatHeartbeat(time) {
      if (!time) return '未知'
      const date = new Date(time)
      const now = new Date()
      const diff = now - date
      if (diff < 60000) return '刚刚'
      if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
      if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
      return `${Math.floor(diff / 86400000)}天前`
    },
    getNodeStatusClass(status) {
      const map = {'0': 'online', '1': 'offline', '2': 'error'}
      return map[status] || 'offline'
    },
    getNodeStatusText(status) {
      const map = {'0': '在线', '1': '离线', '2': '故障'}
      return map[status] || '未知'
    },
    getNodeTagType(status) {
      const map = {'0': 'success', '1': 'info', '2': 'danger'}
      return map[status] || 'info'
    },
    getRankIcon(index) {
      const icons = ['el-icon-medal', 'el-icon-medal', 'el-icon-medal']
      return icons[index] || ''
    },
    handleCardClick(item) {
      if (item.route) {
        this.$router.push(item.route)
      }
    },
    goToDownload() {
      if (this.downloadUrl) {
        window.open(this.downloadUrl, '_blank')
        this.updateDialogVisible = false
      }
    },
    // 解析在线玩家信息
    parseOnlineInfo(onlinePlayer) {
      if (!onlinePlayer) {
        this.serverList = []
        this.queryTime = ''
        return
      }
      this.queryTime = onlinePlayer['查询时间'] || ''
      const servers = []
      Object.keys(onlinePlayer).forEach(key => {
        if (key === '查询时间') return
        const value = onlinePlayer[key]
        if (typeof value === 'string') {
          // 错误信息
          servers.push({
            name: key,
            isError: true,
            errorMsg: value,
            playerCount: 0,
            players: []
          })
        } else if (typeof value === 'object') {
          // 正常数据
          const playerStr = value['在线玩家'] || '[]'
          let players = []
          try {
            // 解析 "[player1, player2]" 格式
            const cleaned = playerStr.replace(/^\[|\]$/g, '')
            if (cleaned) {
              players = cleaned.split(',').map(p => p.trim()).filter(p => p)
            }
          } catch (e) {
            players = []
          }
          servers.push({
            name: key,
            isError: false,
            playerCount: value['在线人数'] || 0,
            players: players
          })
        }
      })
      this.serverList = servers
    },
    showAllServers() {
      this.allServersDialogVisible = true
    },
    showServerDetail(server) {
      this.currentServer = server
      this.serverDetailDialogVisible = true
    },
    // 打开节点实例管理弹窗
    openNodeDialog(node) {
      this.selectedNode = node
      this.nodeDialogVisible = true
    }
  }
}
</script>


<style lang="scss" scoped>
.dashboard-container {
  padding: 24px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8eb 100%);
  min-height: calc(100vh - 84px);
}

.section-header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;

  i {
    margin-right: 8px;
    font-size: 18px;
    color: #409EFF;
  }

  .node-status-tag {
    margin-left: 12px;
  }
}

.stats-section {
  margin-bottom: 24px;
}

.stats-card {
  position: relative;
  padding: 20px;
  border-radius: 12px;
  cursor: pointer;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  min-height: 100px;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 24px rgba(0, 0, 0, 0.15);
  }

  .card-icon {
    width: 56px;
    height: 56px;
    border-radius: 12px;
    background: rgba(255, 255, 255, 0.25);
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 16px;
    flex-shrink: 0;

    i {
      font-size: 26px;
      color: #fff;
    }
  }

  .card-info {
    flex: 1;
    z-index: 1;
  }

  .card-value {
    .number {
      font-size: 28px;
      font-weight: 700;
      color: #fff;
      line-height: 1.2;
    }
  }

  .card-label {
    font-size: 14px;
    color: rgba(255, 255, 255, 0.9);
    margin-top: 4px;
  }

  .card-wave {
    position: absolute;
    bottom: -50%;
    left: -50%;
    width: 200%;
    height: 200%;
    background: radial-gradient(ellipse at center, rgba(255, 255, 255, 0.1) 0%, transparent 70%);
    animation: wave 8s infinite linear;
  }

  &.primary {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  }

  &.cyan {
    background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  }

  &.success {
    background: linear-gradient(135deg, #56ab2f 0%, #a8e063 100%);
  }

  &.info {
    background: linear-gradient(135deg, #606c88 0%, #3f4c6b 100%);
  }

  &.warning {
    background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  }

  &.danger {
    background: linear-gradient(135deg, #eb3349 0%, #f45c43 100%);
  }

  &.purple {
    background: linear-gradient(135deg, #8e2de2 0%, #4a00e0 100%);
  }
}

@keyframes wave {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.node-section {
  margin-bottom: 24px;
}

.node-card {
  position: relative;
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
  border-left: 4px solid #dcdfe6;
  cursor: pointer;

  &:hover {
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12);
    transform: translateY(-2px);

    .node-action-hint {
      opacity: 1;
    }
  }

  &.online {
    border-left-color: #67c23a;
  }

  &.offline {
    border-left-color: #909399;
  }

  &.error {
    border-left-color: #f56c6c;
  }

  .node-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
  }

  .node-name {
    display: flex;
    align-items: center;
    font-weight: 600;
    color: #303133;

    i {
      margin-right: 8px;
      color: #409EFF;
    }
  }

  .node-info {
    .info-item {
      display: flex;
      font-size: 13px;
      margin-bottom: 6px;

      .label {
        color: #909399;
        width: 45px;
      }

      .value {
        color: #606266;
        flex: 1;
      }
    }
  }

  .node-status-wrapper {
    display: flex;
    align-items: center;
    gap: 8px;

    .status-pulse {
      display: block;
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background: #909399;

      &.online {
        background: #67c23a;
        animation: node-pulse 2s infinite;
      }

      &.offline {
        background: #909399;
      }

      &.error {
        background: #f56c6c;
        animation: node-pulse 1s infinite;
      }
    }
  }

  .node-action-hint {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    margin-top: 10px;
    padding-top: 10px;
    border-top: 1px dashed #e4e7ed;
    font-size: 12px;
    color: #409EFF;
    opacity: 0;
    transition: opacity 0.3s;

    i {
      margin-right: 4px;
    }
  }
}

@keyframes node-pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(103, 194, 58, 0.4);
  }
  70% {
    box-shadow: 0 0 0 6px rgba(103, 194, 58, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(103, 194, 58, 0);
  }
}

.dashboard-card {
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  margin-bottom: 20px;
  transition: all 0.3s;

  &:hover {
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12);
  }

  ::v-deep .el-card__header {
    padding: 16px 20px;
    border-bottom: 1px solid #f0f0f0;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .header-left {
    display: flex;
    align-items: center;

    i {
      margin-right: 8px;
      font-size: 18px;
      color: #409EFF;
    }

    span {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
    }

    .el-tag {
      margin-left: 8px;
    }
  }
}

.online-card {
  .online-info-wrapper {
    padding: 10px 0;
  }

  .server-status {
    display: flex;
    align-items: center;
    padding: 14px 16px;
    background: linear-gradient(135deg, #f6f8fc 0%, #f0f4f8 100%);
    border-radius: 10px;
    margin-bottom: 10px;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      background: linear-gradient(135deg, #e8f4fd 0%, #e0ecf4 100%);
      transform: translateX(4px);
    }

    .status-icon {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 14px;
      flex-shrink: 0;

      &.online {
        background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);

        i {
          font-size: 20px;
          color: #fff;
        }
      }

      &.error {
        background: linear-gradient(135deg, #f56c6c 0%, #f78989 100%);

        i {
          font-size: 20px;
          color: #fff;
        }
      }
    }

    .status-info {
      flex: 1;

      .server-name {
        font-size: 14px;
        font-weight: 600;
        color: #303133;
        margin-bottom: 2px;
      }

      .player-count {
        display: flex;
        align-items: baseline;

        .count {
          font-size: 20px;
          font-weight: 700;
          color: #409EFF;
          margin-right: 4px;
        }

        .label {
          font-size: 12px;
          color: #909399;
        }
      }

      .error-msg {
        font-size: 12px;
        color: #f56c6c;
      }
    }

    .server-arrow {
      color: #c0c4cc;
      font-size: 14px;
    }
  }

  .query-time {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    font-size: 13px;
    color: #909399;
    margin-top: 8px;

    i {
      margin-right: 4px;
    }
  }

  .empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 40px;
    color: #909399;

    i {
      font-size: 48px;
      margin-bottom: 12px;
    }
  }
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

// 全部服务器弹窗样式
.all-servers-list {
  max-height: 400px;
  overflow-y: auto;

  .server-item {
    display: flex;
    align-items: center;
    padding: 14px 16px;
    border-radius: 8px;
    margin-bottom: 8px;
    background: #f8f9fa;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      background: #e8f4fd;
      transform: translateX(4px);
    }

    .status-dot {
      width: 10px;
      height: 10px;
      border-radius: 50%;
      margin-right: 12px;
      flex-shrink: 0;

      &.online {
        background: #67c23a;
        box-shadow: 0 0 8px rgba(103, 194, 58, 0.5);
      }

      &.error {
        background: #f56c6c;
      }
    }

    .server-info {
      flex: 1;

      .server-name {
        font-size: 14px;
        font-weight: 600;
        color: #303133;
      }

      .player-info {
        font-size: 12px;
        color: #67c23a;
        margin-top: 2px;
      }

      .error-info {
        font-size: 12px;
        color: #f56c6c;
        margin-top: 2px;
      }
    }

    .el-icon-arrow-right {
      color: #c0c4cc;
    }
  }
}

.dialog-footer {
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
  text-align: right;

  .query-time {
    font-size: 12px;
    color: #909399;
  }
}

// 服务器详情弹窗样式
.server-detail {
  .detail-header {
    text-align: center;
    padding: 20px 0;
    background: linear-gradient(135deg, #f6f8fc 0%, #f0f4f8 100%);
    border-radius: 10px;
    margin-bottom: 20px;

    .player-total {
      .number {
        font-size: 48px;
        font-weight: 700;
        color: #409EFF;
      }

      .label {
        display: block;
        font-size: 14px;
        color: #909399;
        margin-top: 4px;
      }
    }
  }

  .player-list {
    .player-tag {
      margin: 4px;
    }

    .no-players {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 30px;
      color: #909399;

      i {
        font-size: 36px;
        margin-bottom: 10px;
      }
    }
  }
}

.server-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px;
  color: #f56c6c;

  i {
    font-size: 48px;
    margin-bottom: 12px;
  }
}

.rank-card {
  .rank-badge {
    width: 28px;
    height: 28px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 600;
    font-size: 14px;
    margin: 0 auto;

    &.rank-1 {
      background: linear-gradient(135deg, #ffd700 0%, #ffec8b 100%);
      color: #8b6914;

      i {
        color: #8b6914;
      }
    }

    &.rank-2 {
      background: linear-gradient(135deg, #c0c0c0 0%, #e8e8e8 100%);
      color: #666;

      i {
        color: #666;
      }
    }

    &.rank-3 {
      background: linear-gradient(135deg, #cd7f32 0%, #daa06d 100%);
      color: #5c3d1e;

      i {
        color: #5c3d1e;
      }
    }
  }

  .player-cell {
    display: flex;
    align-items: center;

    .player-name {
      margin-left: 10px;
      font-weight: 500;
    }
  }

  .game-time {
    color: #409EFF;
    font-weight: 500;
  }

  ::v-deep .el-table {
    border-radius: 8px;

    th {
      font-weight: 600;
    }

    .el-table__row:hover > td {
      background-color: #f5f7fa !important;
    }
  }
}

// 版本更新弹窗样式
::v-deep .update-dialog {
  .el-dialog__header {
    padding: 20px 20px 10px;
    text-align: center;
    border-bottom: 1px solid #ebeef5;
  }

  .el-dialog__title {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
  }

  .el-dialog__body {
    padding: 30px 40px;
  }

  .el-dialog__footer {
    padding: 15px 20px 20px;
    text-align: center;
    border-top: 1px solid #ebeef5;
  }

  .update-dialog-content {
    text-align: center;

    .update-icon {
      width: 80px;
      height: 80px;
      margin: 0 auto 20px;
      background: linear-gradient(135deg, #409EFF 0%, #66b1ff 100%);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);

      i {
        font-size: 40px;
        color: #fff;
      }
    }

    .update-title {
      font-size: 20px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 24px;
    }

    .version-compare {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 20px;
      margin-bottom: 24px;
      padding: 20px;
      background: #f5f7fa;
      border-radius: 8px;

      .version-item {
        flex: 1;
        max-width: 140px;

        .version-label {
          font-size: 13px;
          color: #909399;
          margin-bottom: 8px;
        }

        .version-number {
          font-size: 18px;
          font-weight: 600;
          color: #606266;
          padding: 8px 12px;
          background: #fff;
          border-radius: 6px;
          border: 1px solid #dcdfe6;
        }

        &.latest .version-number {
          color: #67c23a;
          border-color: #67c23a;
          background: #f0f9ff;
        }
      }

      .version-arrow {
        color: #409EFF;
        font-size: 20px;
        flex-shrink: 0;
      }
    }

    .release-notes {
      text-align: left;
      margin-top: 20px;

      .notes-header {
        display: flex;
        align-items: center;
        margin-bottom: 12px;
        font-size: 14px;
        font-weight: 600;
        color: #303133;

        i {
          margin-right: 6px;
          color: #409EFF;
        }
      }

      .notes-body {
        background: #f5f7fa;
        border-radius: 6px;
        padding: 12px 16px;
        font-size: 14px;
        color: #606266;
        line-height: 1.8;
        max-height: 200px;
        overflow-y: auto;
        border: 1px solid #e4e7ed;
        text-align: left;

        // Markdown 样式
        ::v-deep {
          h1, h2, h3, h4, h5, h6 {
            margin: 12px 0 8px;
            font-weight: 600;
            color: #303133;
            line-height: 1.4;
          }

          h1 {
            font-size: 18px;
          }

          h2 {
            font-size: 16px;
          }

          h3 {
            font-size: 15px;
          }

          h4, h5, h6 {
            font-size: 14px;
          }

          p {
            margin: 8px 0;
          }

          ul, ol {
            margin: 8px 0;
            padding-left: 24px;
          }

          li {
            margin: 4px 0;
          }

          code {
            background: #e6effb;
            padding: 2px 6px;
            border-radius: 3px;
            font-family: 'Courier New', Courier, monospace;
            font-size: 13px;
            color: #409EFF;
          }

          pre {
            background: #282c34;
            color: #abb2bf;
            padding: 12px;
            border-radius: 4px;
            overflow-x: auto;
            margin: 8px 0;

            code {
              background: transparent;
              padding: 0;
              color: inherit;
            }
          }

          blockquote {
            border-left: 3px solid #409EFF;
            padding-left: 12px;
            margin: 8px 0;
            color: #606266;
          }

          a {
            color: #409EFF;
            text-decoration: none;

            &:hover {
              text-decoration: underline;
            }
          }

          strong {
            font-weight: 600;
            color: #303133;
          }

          em {
            font-style: italic;
          }

          hr {
            border: none;
            border-top: 1px solid #dcdfe6;
            margin: 12px 0;
          }

          table {
            border-collapse: collapse;
            width: 100%;
            margin: 8px 0;

            th, td {
              border: 1px solid #dcdfe6;
              padding: 6px 12px;
              text-align: left;
            }

            th {
              background: #f5f7fa;
              font-weight: 600;
            }
          }
        }
      }
    }
  }

  .dialog-footer {
    .el-button {
      padding: 10px 20px;
    }
  }
}

::v-deep .el-loading-spinner {
  .el-loading-text {
    color: #409EFF;
    font-size: 16px;
    margin: 3px 0;
  }

  .path {
    stroke: #409EFF;
  }
}
</style>
