<template>
  <div class="app-container">
    <!-- 头部信息 -->
    <terminal-header
      :instance-info="instanceInfo"
      :server-id="serverId"
      :status-tag="statusTag"
      :status-text="statusText"
      :server-status="serverStatus"
      :op-loading="opLoading"
      @start="handleStart"
      @stop="handleStop"
      @restart="handleRestart"
      @kill="handleKill"
    />

    <el-row :gutter="16">
      <el-col :span="16">
        <!-- 控制台输出 -->
        <console-output
          ref="consoleOutput"
          :console-html="consoleTextHtml"
          :auto-scroll-enabled="autoScrollEnabled"
          :connection-mode="wsConnectionMode"
          :ws-preferred-mode="wsPreferredMode"
          :terminal-theme="terminalTheme"
          :ws-refresh-loading="wsRefreshLoading"
          @toggle-auto-scroll="toggleAutoScroll"
          @clear-console="handleClearConsole"
          @refresh-ws="refreshWebSocket"
          @theme-change="handleThemeChange"
          @connection-mode-change="handleConnectionModeChange"
          @focus-input="focusInput"
          @scroll="handleTerminalScroll"
        />
        <!-- 命令输入 -->
        <command-input
          :terminal-theme="terminalTheme"
          :loading="cmdLoading"
          @send="sendCommand"
        />
      </el-col>
      <el-col :span="8">
        <!-- 实例信息 -->
        <el-card class="side-card" shadow="hover">
          <div slot="header" class="card-header">
            <i class="el-icon-info"></i>
            <span>实例信息</span>
          </div>
          <el-descriptions v-if="instanceInfo" :column="1" border class="info-descriptions" size="small">
            <el-descriptions-item label="实例名称">
              <i class="el-icon-document"></i>
              {{ instanceInfo.name || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="服务器路径">
              <i class="el-icon-folder-opened"></i>
              <span class="path-text">{{ instanceInfo.serverPath || '-' }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="远程实例ID">
              <i class="el-icon-link"></i>
              {{ instanceInfo.nodeInstancesId || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="节点ID">
              <i class="el-icon-connection"></i>
              {{ instanceInfo.nodeId || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="节点UUID">
              <i class="el-icon-key"></i>
              <span class="uuid-text">{{ instanceInfo.nodeUuid || '-' }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="服务器ID">
              <i class="el-icon-link"></i>
              {{ instanceInfo.id || '-' }}
            </el-descriptions-item>
          </el-descriptions>
          <div v-else class="loading-placeholder">
            <i class="el-icon-loading"></i>
            <span>加载中...</span>
          </div>
        </el-card>

        <!-- 在线玩家管理 -->
        <player-list
          :players-data="playersData"
          :loading="playersLoading"
          :auto-refresh="autoRefreshPlayers"
          :diagnostic-loading="diagnosticLoading"
          @refresh="refreshPlayers"
          @toggle-auto-refresh="toggleAutoRefresh"
          @diagnostic="runQueryDiagnostic"
          @player-action="handlePlayerAction"
        />

        <!-- 运行状态 -->
        <server-status :status="serverStatus" />

        <!-- 文件浏览 -->
        <file-browser
          :files="fileItems"
          :loading="filesLoading"
          :current-path="currentPath"
          :server-path="instanceInfo ? instanceInfo.serverPath : ''"
          :can-go-parent="canGoParent"
          @open-dialog="openFileDialog"
          @go-parent="goParent"
          @file-click="handleFileClick"
          @enter="enter"
          @preview="handlePreview"
          @edit="handleEditConfig"
          @download="handleDownloadFile"
          @delete="handleDeleteFile"
        />
      </el-col>
    </el-row>

    <!-- 文件预览对话框 -->
    <el-dialog
      :close-on-click-modal="false"
      :title="previewFile ? previewFile.name : '文件预览'"
      :visible.sync="previewDialogVisible"
      class="preview-dialog"
      width="80%"
    >
      <div v-loading="previewLoading" class="preview-content-wrapper">
        <div v-if="previewFile && isTextFile(previewFile.name)" class="text-preview">
          <el-scrollbar class="preview-scrollbar">
            <pre class="preview-text">{{ previewContent }}</pre>
          </el-scrollbar>
        </div>
        <div v-else-if="previewFile && isImageFile(previewFile.name)" class="image-preview-container">
          <el-scrollbar class="preview-scrollbar">
            <div class="image-wrapper">
              <img :alt="previewFile.name" :src="previewUrl" class="preview-image" />
            </div>
          </el-scrollbar>
        </div>
        <div v-else class="unsupported-preview">
          <i class="el-icon-warning"></i>
          <p>此文件类型不支持预览</p>
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="previewDialogVisible = false">关 闭</el-button>
        <el-button
          v-if="previewFile"
          icon="el-icon-download"
          @click="handleDownloadFile(previewFile)"
        >下载文件</el-button>
        <el-button
          v-if="previewFile && isEditableFile(previewFile.name)"
          icon="el-icon-edit"
          type="primary"
          @click="openEditDialog"
        >编辑文件</el-button>
      </div>
    </el-dialog>

    <!-- 文件浏览独立对话框 -->
    <el-dialog
      :close-on-click-modal="false"
      :visible.sync="fileDialogVisible"
      class="file-browser-dialog"
      title="文件浏览器"
      width="80%"
    >
      <div class="file-dialog-content">
        <div class="file-dialog-header">
          <div class="path-container">
            <i class="el-icon-location-outline"></i>
            <div class="path">{{ currentPath || (instanceInfo && instanceInfo.serverPath) || '/' }}</div>
          </div>
          <div class="file-dialog-actions">
            <el-button :disabled="!canGoParent" icon="el-icon-back" size="small" @click="goParent">上级目录</el-button>
            <el-button icon="el-icon-refresh" size="small" @click="refreshFiles">刷新</el-button>
          </div>
        </div>
        <el-scrollbar class="file-dialog-scrollbar">
          <div v-loading="filesLoading" class="file-dialog-list">
            <div v-if="fileItems.length === 0 && !filesLoading" class="empty-files">
              <i class="el-icon-document-delete"></i>
              <span>目录为空</span>
            </div>
            <div
              v-for="item in fileItems"
              :key="item.fullPath"
              class="file-dialog-row"
              @click="handleFileClick(item)"
              @dblclick="enter(item)"
            >
              <div class="file-info">
                <i
                  :class="item.isDir ? 'el-icon-folder file-icon folder-icon' : 'el-icon-document file-icon file-icon-doc'"
                />
                <span :title="item.name" class="name">{{ item.name }}</span>
                <el-tag v-if="item.isDir" class="dir-tag" size="mini" type="info">目录</el-tag>
                <el-tag v-else-if="isMcConfigFile(item.name)" class="config-tag" size="mini" type="success">配置</el-tag>
              </div>
              <div v-if="!item.isDir" class="file-actions">
                <el-button icon="el-icon-view" size="mini" type="text" @click.stop="handlePreview(item)">预览</el-button>
                <el-button
                  v-if="isEditableFile(item.name)"
                  icon="el-icon-edit"
                  size="mini"
                  type="text"
                  @click.stop="handleEditConfig(item)"
                >编辑</el-button>
                <el-button icon="el-icon-download" size="mini" type="text" @click.stop="handleDownloadFile(item)">下载</el-button>
                <el-button icon="el-icon-delete" size="mini" type="text" @click.stop="handleDeleteFile(item)">删除</el-button>
              </div>
              <div v-else class="file-actions">
                <el-button icon="el-icon-delete" size="mini" type="text" @click.stop="handleDeleteFile(item)">删除</el-button>
              </div>
            </div>
          </div>
        </el-scrollbar>
      </div>
    </el-dialog>

    <!-- 配置编辑器 -->
    <config-editor
      :show="editDialogVisible"
      :file="editFile"
      :content="editContent"
      :loading="editContentLoading"
      :save-loading="saveLoading"
      :show-translation="showTranslation"
      @close="editDialogVisible = false"
      @save="handleSaveConfig"
      @toggle-translation="showTranslation = !showTranslation"
    />
  </div>
</template>

<script>
import { getMcs, getServerPlayers, playerAction, queryDiagnostic } from '@/api/node/mcs'
import {
  deleteFile,
  downloadFile,
  getNodeInstanceConsole,
  getNodeInstanceConsoleHistory,
  getNodeInstanceStatus,
  killNodeInstance,
  restartNodeInstance,
  saveFile,
  sendNodeInstanceCommand,
  startNodeInstance,
  stopNodeInstance
} from '@/api/node/server'
import AnsiToHtml from 'ansi-to-html'
import SockJS from 'sockjs-client/dist/sockjs.min.js'
import Stomp from 'stompjs'

// 子组件
import TerminalHeader from './components/TerminalHeader.vue'
import ConsoleOutput from './components/ConsoleOutput.vue'
import CommandInput from './components/CommandInput.vue'
import PlayerList from './components/PlayerList.vue'
import FileBrowser from './components/FileBrowser.vue'
import ServerStatus from './components/ServerStatus.vue'
import ConfigEditor from './components/ConfigEditor.vue'

// 常量
import {
  CONSOLE_MAX_LINES,
  STATUS_POLL_INTERVAL,
  PLAYERS_REFRESH_INTERVAL,
  WS_CONNECT_TIMEOUT,
  WS_RECONNECT_DELAY,
  SCROLL_BOTTOM_THRESHOLD,
  TERMINAL_THEMES,
  WS_CONNECTION_MODES,
  LOG_LEVEL_COLORS,
  LOG_LEVEL_PATTERN,
  PLAYER_ACTION_DESCRIPTIONS,
  ACTIONS_REQUIRING_REASON,
  MC_CONFIG_FILES,
  TEXT_EXTENSIONS,
  SPECIAL_FILE_NAMES,
  IMAGE_EXTENSIONS,
  IMAGE_MIME_TYPES,
  TERMINAL_THEME_NAMES
} from './constants'

export default {
  name: 'McsTerminal',
  components: {
    TerminalHeader,
    ConsoleOutput,
    CommandInput,
    PlayerList,
    FileBrowser,
    ServerStatus,
    ConfigEditor
  },
  data() {
    return {
      serverId: Number(this.$route.query.serverId) || null,
      instanceInfo: null,
      consoleTextHtml: '',
      consoleLineCount: 0,
      consolePendingLines: [],
      consoleRafId: null,
      autoScrollEnabled: true,
      cmdLoading: false,
      opLoading: false,
      statusText: '未知',
      statusTag: 'warning',
      serverStatus: null,
      ansiConverter: null,
      // 玩家管理
      playersData: null,
      playersLoading: false,
      autoRefreshPlayers: false,
      playersTimer: null,
      diagnosticLoading: false,
      // 文件浏览
      filesLoading: false,
      currentPath: '',
      fileItems: [],
      // WebSocket
      wsInfo: { wsUrl: '', console: '', subscribe: '', token: '' },
      wsConnectionMode: null,
      wsPreferredMode: WS_CONNECTION_MODES.AUTO,
      wsDirectFailed: false,
      wsRefreshLoading: false,
      stompClient: null,
      subscription: null,
      // 定时器
      statusTimer: null,
      // 终端主题
      terminalTheme: 'github-dark',
      // 文件预览
      previewDialogVisible: false,
      previewFile: null,
      previewContent: '',
      previewUrl: '',
      previewLoading: false,
      // 文件浏览对话框
      fileDialogVisible: false,
      // 配置编辑
      editDialogVisible: false,
      editFile: null,
      editContent: '',
      editContentLoading: false,
      saveLoading: false,
      showTranslation: true
    }
  },
  computed: {
    canGoParent() {
      if (!this.currentPath) return false
      const norm = this.currentPath.replace(/\\/g, '/').replace(/\/$/, '')
      const base = (this.instanceInfo?.serverPath || '').replace(/\\/g, '/').replace(/\/$/, '')
      return norm.length > base.length
    }
  },
  watch: {
    '$route.query.serverId': {
      handler(newServerId, oldServerId) {
        if (newServerId && newServerId !== oldServerId) {
          this.serverId = Number(newServerId)
          this.disconnectWs()
          this.stopStatusPolling()
          this.clearConsoleOutput()
          this.fileItems = []
          this.currentPath = ''
          this.serverStatus = null
          this.instanceInfo = null
          this.getInstanceInfo()
        }
      },
      immediate: false
    }
  },
  created() {
    this.ansiConverter = new AnsiToHtml({
      fg: '#FFF',
      bg: '#000',
      newline: false,
      escapeXML: true,
      stream: false
    })

    if (!this.serverId) {
      this.$message.error('缺少必要的参数：serverId')
      this.$router.push('/node/mcs/index')
      return
    }

    // 读取用户偏好
    const savedMode = localStorage.getItem('wsConnectionMode')
    if (savedMode && Object.values(WS_CONNECTION_MODES).includes(savedMode)) {
      this.wsPreferredMode = savedMode
    }

    const savedTheme = localStorage.getItem('terminalTheme')
    if (savedTheme && TERMINAL_THEMES.includes(savedTheme)) {
      this.terminalTheme = savedTheme
    }

    this.getInstanceInfo()
  },
  beforeDestroy() {
    this.disconnectWs()
    this.clearConsoleOutput()
    this.stopStatusPolling()
    if (this.previewUrl) URL.revokeObjectURL(this.previewUrl)
    if (this.playersTimer) {
      clearInterval(this.playersTimer)
      this.playersTimer = null
    }
  },
  methods: {
    // ==================== 实例信息 ====================
    async getInstanceInfo() {
      try {
        const response = await getMcs(this.serverId)
        if (response.code === 200 && response.data) {
          this.instanceInfo = response.data
          this.initInfoAndFiles()
          await this.fetchConsoleHistory()
          await this.fetchConsoleWsInfo()
          this.connectWs()
          const status = await this.fetchServerStatus()
          if (!this.statusTimer) this.startStatusPolling()
          if (status?.isRunning) {
            this.refreshPlayers()
          } else {
            this.playersData = { success: true, onlinePlayers: 0, maxPlayers: 0, players: [] }
          }
        } else {
          this.$message.error('获取实例信息失败')
          this.$router.push('/node/mcs/index')
        }
      } catch (error) {
        console.error('获取实例信息失败:', error)
        this.$message.error('获取实例信息失败')
        this.$router.push('/node/mcs/index')
      }
    },

    // ==================== 控制台历史 ====================
    async fetchConsoleHistory() {
      if (!this.instanceInfo) return
      try {
        const response = await getNodeInstanceConsoleHistory({
          id: this.instanceInfo.nodeId,
          serverId: this.serverId
        })
        if (response.code === 200 && response.data?.logs) {
          this.setConsoleLines(response.data.logs || [])
        }
      } catch (error) {
        console.error('获取控制台历史日志失败:', error)
      }
    },

    // ==================== 服务器状态 ====================
    async fetchServerStatus() {
      if (!this.instanceInfo) return null
      try {
        const response = await getNodeInstanceStatus({
          id: this.instanceInfo.nodeId,
          serverId: this.serverId
        })
        if (response.code === 200 && response.data) {
          this.serverStatus = response.data
          this.updateStatusDisplay(response.data)
          return response.data
        }
      } catch (error) {
        console.error('获取服务器状态失败:', error)
      }
      return null
    },

    updateStatusDisplay(status) {
      if (!status) return
      this.statusText = status.isRunning ? '运行中' : '已停止'
      this.statusTag = status.isRunning ? 'success' : 'info'
    },

    startStatusPolling() {
      if (this.statusTimer) this.stopStatusPolling()
      if (this.instanceInfo) this.fetchServerStatus()
      this.statusTimer = setInterval(() => {
        if (this.instanceInfo) this.fetchServerStatus()
      }, STATUS_POLL_INTERVAL)
    },

    stopStatusPolling() {
      if (this.statusTimer) {
        clearInterval(this.statusTimer)
        this.statusTimer = null
      }
    },

    // ==================== WebSocket 连接 ====================
    async fetchConsoleWsInfo() {
      if (!this.instanceInfo) return

      if (this.wsPreferredMode === WS_CONNECTION_MODES.PROXY ||
          (this.wsPreferredMode === WS_CONNECTION_MODES.AUTO && this.wsDirectFailed)) {
        this.setupProxyMode()
      } else if (this.wsPreferredMode === WS_CONNECTION_MODES.DIRECT) {
        await this.setupDirectMode()
      } else {
        try {
          await this.setupDirectMode()
        } catch (error) {
          console.warn('直连模式失败，切换到代理模式:', error)
          this.wsDirectFailed = true
          this.setupProxyMode()
        }
      }
    },

    async setupDirectMode() {
      try {
        const response = await getNodeInstanceConsole({
          id: this.instanceInfo.nodeId,
          serverId: this.serverId
        })
        if (response?.data) {
          this.wsInfo = {
            wsUrl: response.data.wsUrl || '',
            console: response.data.console || '',
            subscribe: response.data.subscribe || '',
            token: response.data.token || ''
          }
          this.wsConnectionMode = WS_CONNECTION_MODES.DIRECT
        } else {
          throw new Error('获取节点WebSocket信息失败')
        }
      } catch (error) {
        console.error('设置直连模式失败:', error)
        throw error
      }
    },

    setupProxyMode() {
      const baseApi = process.env.VUE_APP_BASE_API || '/prod-api'
      this.wsInfo = {
        wsUrl: `${baseApi}/ws`,
        console: '/topic/node-console/',
        subscribe: '/app/node/console/subscribe',
        token: this.$store.getters.token || ''
      }
      this.wsConnectionMode = WS_CONNECTION_MODES.PROXY
    },

    connectWs() {
      if (!this.wsInfo.wsUrl || !this.wsInfo.console || !this.wsInfo.subscribe) {
        console.warn('WebSocket连接信息不完整')
        return
      }

      try {
        const sock = new SockJS(this.wsInfo.wsUrl)
        this.stompClient = Stomp.over(sock)
        this.stompClient.debug = null

        const connectTimeout = setTimeout(() => {
          if (!this.stompClient?.connected) {
            console.error('WebSocket连接超时')
            this.handleConnectionFailure()
          }
        }, WS_CONNECT_TIMEOUT)

        const connectHeaders = this.wsConnectionMode === WS_CONNECTION_MODES.DIRECT
          ? { 'X-Endless-Token': this.wsInfo.token }
          : {}

        this.stompClient.connect(connectHeaders, () => {
          clearTimeout(connectTimeout)
          console.log(`WebSocket连接成功 (${this.wsConnectionMode}模式)`)

          let topic, subscribePayload
          if (this.wsConnectionMode === WS_CONNECTION_MODES.DIRECT) {
            topic = this.wsInfo.console + this.instanceInfo.nodeInstancesId
            subscribePayload = {
              serverId: this.instanceInfo.nodeInstancesId,
              token: this.wsInfo.token
            }
          } else {
            topic = this.wsInfo.console + this.instanceInfo.nodeId + '/' + this.instanceInfo.nodeInstancesId
            subscribePayload = {
              nodeId: this.instanceInfo.nodeId,
              serverId: this.instanceInfo.nodeInstancesId
            }
          }

          this.subscription = this.stompClient.subscribe(topic, (msg) => {
            try {
              const body = JSON.parse(msg.body)
              if (body.line) this.queueConsoleLine(body.line)
              else if (body.error) this.queueConsoleLine(`[ERROR] ${body.error}`)
              else if (body.message) this.queueConsoleLine(body.message)
              else if (body.console) this.setConsoleText(body.console)
            } catch (e) {
              console.error('处理WebSocket消息失败:', e)
            }
          })

          this.stompClient.send(this.wsInfo.subscribe, {}, JSON.stringify(subscribePayload))
          this.$message.success(`控制台已连接 (${this.wsConnectionMode === WS_CONNECTION_MODES.DIRECT ? '直连' : '代理'}模式)`)
        }, (error) => {
          clearTimeout(connectTimeout)
          console.error('WebSocket连接失败:', error)
          this.handleConnectionFailure()
        })
      } catch (error) {
        console.error('WebSocket连接异常:', error)
        this.handleConnectionFailure()
      }
    },

    handleConnectionFailure() {
      if (this.wsPreferredMode === WS_CONNECTION_MODES.AUTO &&
          this.wsConnectionMode === WS_CONNECTION_MODES.DIRECT &&
          !this.wsDirectFailed) {
        console.log('直连失败，自动切换到代理模式')
        this.wsDirectFailed = true
        this.$message.warning('直连失败，正在切换到代理模式...')
        this.disconnectWs()
        this.fetchConsoleWsInfo().then(() => this.connectWs())
      } else {
        this.$message.error('WebSocket连接失败，5秒后重试...')
        setTimeout(() => this.connectWs(), WS_RECONNECT_DELAY)
      }
    },

    disconnectWs() {
      try {
        if (this.stompClient?.connected && this.instanceInfo &&
            this.wsConnectionMode === WS_CONNECTION_MODES.PROXY) {
          this.stompClient.send('/app/node/console/unsubscribe', {}, JSON.stringify({
            nodeId: this.instanceInfo.nodeId,
            serverId: this.serverId
          }))
        }
      } catch (e) {
        console.error('发送取消订阅指令失败:', e)
      }

      try { this.subscription?.unsubscribe() } catch (e) { console.error('取消订阅失败:', e) }
      try { this.stompClient?.disconnect(() => console.log('WebSocket已断开')) } catch (e) { console.error('断开WebSocket连接失败:', e) }

      this.subscription = null
      this.stompClient = null
      this.wsInfo = { wsUrl: '', console: '', subscribe: '', token: '' }
    },

    // ==================== 控制台输出 ====================
    scrollToBottom() {
      this.$refs.consoleOutput?.scrollToBottom()
    },

    isTerminalNearBottom(threshold = SCROLL_BOTTOM_THRESHOLD) {
      const el = this.$refs.consoleOutput?.$refs.terminal
      if (!el) return true
      return el.scrollHeight - el.scrollTop - el.clientHeight <= threshold
    },

    maybeScrollToBottom(force = false) {
      if (force || (this.autoScrollEnabled && this.isTerminalNearBottom())) {
        this.$nextTick(() => this.scrollToBottom())
      }
    },

    handleTerminalScroll() { /* 仅用于触发视图更新 */ },

    toggleAutoScroll() {
      this.autoScrollEnabled = !this.autoScrollEnabled
      if (this.autoScrollEnabled) {
        this.$nextTick(() => this.scrollToBottom())
        this.$message.success('已开启自动滚动')
      } else {
        this.$message.info('已暂停自动滚动')
      }
    },

    handleClearConsole() {
      this.clearConsoleOutput()
      this.$message.success('控制台已清空')
    },

    clearConsoleOutput() {
      if (this.consoleRafId != null) {
        cancelAnimationFrame(this.consoleRafId)
        this.consoleRafId = null
      }
      this.consolePendingLines = []
      this.consoleTextHtml = ''
      this.consoleLineCount = 0
    },

    setConsoleText(text) {
      const lines = text ? String(text).split('\n') : []
      this.setConsoleLines(lines)
    },

    setConsoleLines(lines) {
      if (!this.ansiConverter) return
      const normalized = (lines || []).map(line => line == null ? '' : String(line))
      const keepLines = normalized.slice(-CONSOLE_MAX_LINES)
      this.consoleTextHtml = keepLines.map(line => this.formatLogLine(line)).join('<br>')
      this.consoleLineCount = keepLines.length
      this.maybeScrollToBottom(true)
    },

    queueConsoleLine(line) {
      this.consolePendingLines.push(line == null ? '' : String(line))
      if (this.consoleRafId != null) return
      this.consoleRafId = requestAnimationFrame(() => {
        this.consoleRafId = null
        this.flushConsoleLines()
      })
    },

    flushConsoleLines() {
      if (!this.consolePendingLines.length) return
      const pending = this.consolePendingLines
      this.consolePendingLines = []
      const htmlBatch = pending.map(line => this.formatLogLine(line)).join('<br>')

      this.consoleTextHtml = this.consoleTextHtml
        ? this.consoleTextHtml + '<br>' + htmlBatch
        : htmlBatch
      this.consoleLineCount += pending.length

      if (this.consoleLineCount > CONSOLE_MAX_LINES) {
        const htmlLines = this.consoleTextHtml.split('<br>')
        const keepLines = htmlLines.slice(-CONSOLE_MAX_LINES)
        this.consoleTextHtml = keepLines.join('<br>')
        this.consoleLineCount = keepLines.length
      }

      this.maybeScrollToBottom(false)
    },

    formatLogLine(line) {
      if (!line) return ''
      const match = line.match(LOG_LEVEL_PATTERN)

      if (match) {
        const prefix = match[1]
        const level = match[2].toUpperCase()
        const content = match[3]
        const levelColor = LOG_LEVEL_COLORS[level] || '#FFF'
        const coloredPrefix = `<span style="color: ${levelColor};">${this.escapeHtml(prefix)}</span>`
        return coloredPrefix + this.ansiConverter.toHtml(content)
      }
      return this.ansiConverter.toHtml(line)
    },

    escapeHtml(text) {
      const div = document.createElement('div')
      div.textContent = text
      return div.innerHTML
    },

    focusInput() { /* 预留 */ },

    // ==================== 主题和连接模式 ====================
    handleThemeChange(theme) {
      if (theme === this.terminalTheme) return
      this.terminalTheme = theme
      localStorage.setItem('terminalTheme', theme)
      this.$message.success(`已切换到 ${TERMINAL_THEME_NAMES[theme]} 主题`)
    },

    handleConnectionModeChange(mode) {
      if (mode === this.wsPreferredMode) return
      this.wsPreferredMode = mode
      this.wsDirectFailed = false
      localStorage.setItem('wsConnectionMode', mode)
      const modeText = mode === WS_CONNECTION_MODES.AUTO ? '自动' :
                       mode === WS_CONNECTION_MODES.DIRECT ? '直连' : '代理'
      this.$message.info(`已切换到${modeText}模式，正在重新连接...`)
      this.disconnectWs()
      this.fetchConsoleWsInfo().then(() => this.connectWs())
    },

    async refreshWebSocket() {
      if (this.wsRefreshLoading) return
      this.wsRefreshLoading = true
      try {
        this.$message.info('正在刷新终端连接...')
        this.disconnectWs()
        await new Promise(resolve => setTimeout(resolve, 500))
        await this.fetchConsoleWsInfo()
        this.connectWs()
        this.$message.success('终端连接已刷新')
      } catch (error) {
        console.error('刷新WebSocket连接失败:', error)
        this.$message.error('刷新终端连接失败: ' + (error.message || '未知错误'))
      } finally {
        this.wsRefreshLoading = false
      }
    },

    // ==================== 操作命令 ====================
    sendCommand(command) {
      if (!command || !this.instanceInfo) return
      this.cmdLoading = true
      sendNodeInstanceCommand({
        id: this.instanceInfo.nodeId,
        serverId: this.serverId,
        command
      }).finally(() => { this.cmdLoading = false })
    },

    handleStart() {
      if (!this.instanceInfo) return
      this.opLoading = true
      startNodeInstance({ id: this.instanceInfo.nodeId, serverId: this.serverId })
        .then(() => {
          this.$message.success('启动指令已发送')
          setTimeout(() => this.fetchServerStatus(), 2000)
        })
        .finally(() => { this.opLoading = false })
    },

    handleStop() {
      if (!this.instanceInfo) return
      this.opLoading = true
      stopNodeInstance({ id: this.instanceInfo.nodeId, serverId: this.serverId })
        .then(() => {
          this.$message.success('停止指令已发送')
          setTimeout(() => this.fetchServerStatus(), 2000)
        })
        .finally(() => { this.opLoading = false })
    },

    handleRestart() {
      if (!this.instanceInfo) return
      this.opLoading = true
      restartNodeInstance({ id: this.instanceInfo.nodeId, serverId: this.serverId })
        .then(() => {
          this.$message.success('重启指令已发送')
          setTimeout(() => this.fetchServerStatus(), 3000)
        })
        .finally(() => { this.opLoading = false })
    },

    handleKill() {
      if (!this.instanceInfo) return
      this.opLoading = true
      killNodeInstance({ id: this.instanceInfo.nodeId, serverId: this.serverId })
        .then(() => {
          this.$message.success('强制终止指令已发送')
          setTimeout(() => this.fetchServerStatus(), 2000)
        })
        .finally(() => { this.opLoading = false })
    },

    // ==================== 文件操作 ====================
    initInfoAndFiles() {
      if (this.instanceInfo?.serverPath) {
        this.currentPath = this.instanceInfo.serverPath
        this.loadFiles(this.currentPath)
      }
    },

    loadFiles(path) {
      if (!this.instanceInfo) return
      this.filesLoading = true
      this.currentPath = path
      const { getFileList } = require('@/api/node/server')
      getFileList({ id: this.instanceInfo.nodeId, path })
        .then(res => {
          const list = (res.data && (res.data.files || res.data.list || res.data)) || []
          this.fileItems = list.map(it => {
            const isDir = it.isDirectory ?? it.isDir ?? it.directory ?? (it.type === 'DIR' || it.dir || false)
            const fileName = it.name || it.fileName || (it.path ? it.path.split(/[\\/]/).pop() : null) || '未知'
            const fullPath = it.path || it.fullPath || (path ? (path.replace(/[\\/]$/, '') + '/' + fileName) : fileName)
            return { name: fileName, isDir, fullPath, totalSpace: it.totalSpace || 0, lastModified: it.lastModified || 0 }
          }).sort((a, b) => {
            if (a.isDir && !b.isDir) return -1
            if (!a.isDir && b.isDir) return 1
            return a.name.localeCompare(b.name, 'zh-CN', { numeric: true, sensitivity: 'base' })
          })
        })
        .catch(error => {
          console.error('加载文件列表失败:', error)
          this.$message.error('加载文件列表失败')
        })
        .finally(() => { this.filesLoading = false })
    },

    enter(item) {
      if (item.isDir) this.loadFiles(item.fullPath)
    },

    goParent() {
      if (!this.currentPath) return
      const segs = this.currentPath.replace(/\\/g, '/').split('/').filter(Boolean)
      if (segs.length <= 1) return
      segs.pop()
      const parent = (this.currentPath.startsWith('/') ? '/' : '') + segs.join('/')
      this.loadFiles(parent)
    },

    handleFileClick() { /* 单击不做操作 */ },

    refreshFiles() {
      this.loadFiles(this.currentPath || this.instanceInfo?.serverPath || '/')
    },

    openFileDialog() {
      this.fileDialogVisible = true
    },

    isMcConfigFile(filename) {
      return MC_CONFIG_FILES.some(f => filename?.toLowerCase() === f.toLowerCase())
    },

    isTextFile(filename) {
      if (!filename) return false
      return TEXT_EXTENSIONS.some(ext => filename.toLowerCase().endsWith(ext)) ||
             SPECIAL_FILE_NAMES.includes(filename.toLowerCase())
    },

    isImageFile(filename) {
      return IMAGE_EXTENSIONS.some(ext => filename?.toLowerCase().endsWith(ext))
    },

    isEditableFile(filename) {
      if (!filename) return false
      if (this.isMcConfigFile(filename)) return true
      return TEXT_EXTENSIONS.some(ext => filename.toLowerCase().endsWith(ext)) ||
             SPECIAL_FILE_NAMES.includes(filename.toLowerCase())
    },

    async handlePreview(file) {
      if (!file || !this.instanceInfo) return
      this.previewFile = file
      this.previewLoading = true
      this.previewDialogVisible = true

      try {
        if (this.isTextFile(file.name)) {
          const response = await downloadFile(this.instanceInfo.nodeId, file.fullPath)
          const blob = new Blob([response], { type: 'text/plain' })
          const reader = new FileReader()
          reader.onload = (e) => {
            this.previewContent = e.target.result
            this.previewLoading = false
          }
          reader.readAsText(blob)
        } else if (this.isImageFile(file.name)) {
          const response = await downloadFile(this.instanceInfo.nodeId, file.fullPath)
          const ext = file.name.substring(file.name.lastIndexOf('.') + 1).toLowerCase()
          const mimeType = IMAGE_MIME_TYPES[ext] || 'image/jpeg'
          const blob = new Blob([response], { type: mimeType })
          if (this.previewUrl) URL.revokeObjectURL(this.previewUrl)
          this.previewUrl = URL.createObjectURL(blob)
          this.previewLoading = false
        } else {
          this.previewLoading = false
        }
      } catch (error) {
        this.previewLoading = false
        this.$message.error('加载文件失败: ' + (error.message || '未知错误'))
      }
    },

    openEditDialog() {
      if (this.previewFile) this.handleEditConfig(this.previewFile)
    },

    async handleEditConfig(file) {
      if (!file || !this.instanceInfo) return
      this.editFile = file
      this.editContent = ''
      this.editContentLoading = true
      this.saveLoading = false
      this.editDialogVisible = true

      try {
        const response = await downloadFile(this.instanceInfo.nodeId, file.fullPath)
        const blob = new Blob([response], { type: 'text/plain' })
        const reader = new FileReader()
        reader.onload = (e) => {
          this.editContent = e.target.result || ''
          this.editContentLoading = false
          if (this.editContent === '') this.$message.warning('文件内容为空')
        }
        reader.onerror = () => {
          this.editContentLoading = false
          this.$message.error('读取文件内容失败')
        }
        reader.readAsText(blob)
      } catch (error) {
        this.editContentLoading = false
        this.$message.error('加载文件内容失败: ' + (error.message || '未知错误'))
        this.editDialogVisible = false
      }
    },

    async handleSaveConfig(content) {
      if (!this.editFile || !this.instanceInfo || !content) return
      this.saveLoading = true
      try {
        const response = await saveFile({
          id: this.instanceInfo.nodeId,
          path: this.editFile.fullPath,
          content
        })
        if (response.code === 200) {
          this.$message.success('保存成功')
          this.editDialogVisible = false
          if (this.previewDialogVisible && this.previewFile?.name === this.editFile.name) {
            this.previewContent = content
          }
        } else {
          this.$message.error(response.msg || '保存失败')
        }
      } catch (error) {
        this.$message.error('保存失败: ' + (error.message || '未知错误'))
      } finally {
        this.saveLoading = false
      }
    },

    async handleDownloadFile(file) {
      if (!file || !this.instanceInfo || file.isDir) return
      try {
        this.$message.info('正在准备下载...')
        const response = await downloadFile(this.instanceInfo.nodeId, file.fullPath)
        const blob = new Blob([response])
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = file.name
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)
        this.$message.success('文件下载成功')
      } catch (error) {
        console.error('下载文件失败:', error)
        this.$message.error('下载文件失败: ' + (error.message || '未知错误'))
      }
    },

    async handleDeleteFile(file) {
      if (!file || !this.instanceInfo) return
      const fileType = file.isDir ? '目录' : '文件'
      const confirmMessage = `确定要删除${fileType} "${file.name}" 吗？${file.isDir ? '此操作将删除目录及其所有内容，' : ''}此操作不可恢复！`

      try {
        await this.$confirm(confirmMessage, '删除确认', {
          confirmButtonText: '确定删除',
          cancelButtonText: '取消',
          type: 'warning'
        })
        const response = await deleteFile({ id: this.instanceInfo.nodeId, path: file.fullPath })
        if (response.code === 200) {
          this.$message.success(`${fileType}删除成功`)
          this.refreshFiles()
        } else {
          this.$message.error(response.msg || `删除${fileType}失败`)
        }
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error(`删除${fileType}失败: ` + (error.message || '未知错误'))
        }
      }
    },

    // ==================== 玩家管理 ====================
    async refreshPlayers() {
      if (!this.instanceInfo) return

      let isRunning = this.serverStatus?.isRunning ?? null
      if (isRunning === null) {
        const latestStatus = await this.fetchServerStatus()
        isRunning = !!latestStatus?.isRunning
      }

      if (!isRunning) {
        this.playersData = { success: true, onlinePlayers: 0, maxPlayers: 0, players: [] }
        return
      }

      this.playersLoading = true
      try {
        const response = await getServerPlayers(this.instanceInfo.nodeId, this.serverId)
        if (response.code === 200 && response.data?.success) {
          this.playersData = response.data
        } else {
          this.playersData = null
          console.warn('获取玩家信息失败:', response.data?.error || '未知错误')
        }
      } catch (error) {
        console.error('获取玩家信息失败:', error)
        this.playersData = null
      } finally {
        this.playersLoading = false
      }
    },

    toggleAutoRefresh() {
      this.autoRefreshPlayers = !this.autoRefreshPlayers
      if (this.autoRefreshPlayers) {
        this.refreshPlayers()
        this.playersTimer = setInterval(() => this.refreshPlayers(), PLAYERS_REFRESH_INTERVAL)
        this.$message.success('已启用玩家列表自动刷新')
      } else {
        if (this.playersTimer) {
          clearInterval(this.playersTimer)
          this.playersTimer = null
        }
        this.$message.info('已关闭玩家列表自动刷新')
      }
    },

    async handlePlayerAction(playerName, action) {
      if (!this.instanceInfo || !playerName) return

      const actionDesc = PLAYER_ACTION_DESCRIPTIONS[action] || action
      let reason = ''

      if (ACTIONS_REQUIRING_REASON.includes(action)) {
        try {
          const { value } = await this.$prompt(`请输入${actionDesc}原因（可选）:`, `${actionDesc}玩家`, {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            inputPlaceholder: '输入原因...'
          })
          reason = value || ''
        } catch {
          return
        }
      }

      try {
        await this.$confirm(`确定要对玩家 ${playerName} 执行"${actionDesc}"操作吗？`, '确认操作', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
      } catch {
        return
      }

      try {
        const response = await playerAction(this.instanceInfo.nodeId, this.serverId, playerName, { action, reason })
        if (response.code === 200 && response.data?.success) {
          this.$message.success(`成功对玩家 ${playerName} 执行${actionDesc}操作`)
          setTimeout(() => this.refreshPlayers(), 1000)
        } else {
          this.$message.error(`操作失败: ${response.data?.error || '未知错误'}`)
        }
      } catch (error) {
        console.error('玩家操作失败:', error)
        this.$message.error(`操作失败: ${error.message || '网络错误'}`)
      }
    },

    async runQueryDiagnostic() {
      if (!this.instanceInfo) return
      this.diagnosticLoading = true
      try {
        const response = await queryDiagnostic(this.instanceInfo.nodeId, this.serverId)
        if (response.code === 200 && response.data) {
          this.showDiagnosticResults(response.data)
        } else {
          this.$message.error('诊断失败: ' + (response.msg || '未知错误'))
        }
      } catch (error) {
        console.error('Query诊断失败:', error)
        this.$message.error('诊断失败: ' + (error.message || '网络错误'))
      } finally {
        this.diagnosticLoading = false
      }
    },

    showDiagnosticResults(diagnostic) {
      const h = this.$createElement
      const content = []

      content.push(h('h4', '基本信息'))
      content.push(h('p', [
        h('strong', '服务器ID: '), diagnostic.serverId, h('br'),
        h('strong', '游戏端口: '), diagnostic.gamePort, h('br'),
        h('strong', 'Query端口: '), diagnostic.queryPort, h('br'),
        h('strong', '服务器运行: '), diagnostic.serverRunning ? '是' : '否'
      ]))

      content.push(h('h4', 'server.properties配置'))
      const props = diagnostic.serverProperties
      if (props?.exists) {
        content.push(h('p', [
          h('strong', 'enable-query: '), props['enable-query'] || '未设置', h('br'),
          h('strong', 'query.port: '), props['query.port'] || '未设置', h('br'),
          h('strong', 'server-port: '), props['server-port'] || '未设置'
        ]))
      } else {
        content.push(h('p', { style: 'color: #f56c6c;' }, 'server.properties文件不存在'))
      }

      content.push(h('h4', '连接测试结果'))
      const tests = diagnostic.connectionTests
      Object.keys(tests).forEach(host => {
        const test = tests[host]
        const steps = []

        if (test.socketCreated === true) steps.push(h('span', { style: 'color: #67c23a;' }, '✓ Socket创建'))
        else steps.push(h('span', { style: 'color: #f56c6c;' }, '✗ Socket创建失败'))

        if (test.handshakeSuccess === true) steps.push(h('span', { style: 'color: #67c23a;' }, '✓ 握手成功'))
        else if (test.handshakeSuccess === false) steps.push(h('span', { style: 'color: #f56c6c;' }, '✗ 握手失败'))

        if (test.statusSuccess === true) steps.push(h('span', { style: 'color: #67c23a;' }, `✓ 状态获取成功 (${test.onlinePlayers}/${test.maxPlayers})`))
        else if (test.statusSuccess === false) steps.push(h('span', { style: 'color: #f56c6c;' }, '✗ 状态获取失败'))

        content.push(h('div', { style: 'margin-bottom: 10px;' }, [
          h('strong', `${host}:${test.port}`), h('br'),
          ...steps.map(step => h('div', { style: 'margin-left: 20px;' }, step))
        ]))

        if (test.error) content.push(h('div', { style: 'color: #f56c6c; margin-left: 20px; font-size: 12px;' }, `错误: ${test.error}`))
        if (test.handshakeError) content.push(h('div', { style: 'color: #f56c6c; margin-left: 20px; font-size: 12px;' }, `握手错误: ${test.handshakeError}`))
        if (test.statusError) content.push(h('div', { style: 'color: #f56c6c; margin-left: 20px; font-size: 12px;' }, `状态错误: ${test.statusError}`))
      })

      if (diagnostic.suggestions?.length > 0) {
        content.push(h('h4', '修复建议'))
        content.push(h('ul', { style: 'margin: 0; padding-left: 20px;' },
          diagnostic.suggestions.map(s => h('li', { style: 'margin-bottom: 5px;' }, s))
        ))
      }

      this.$msgbox({
        title: 'Query连接诊断结果',
        message: h('div', { style: 'max-height: 400px; overflow-y: auto;' }, content),
        showCancelButton: false,
        confirmButtonText: '关闭'
      })
    }
  }
}
</script>

<style lang="scss" scoped>
@import './terminal.scss';

.app-container {
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  min-height: calc(100vh - 84px);
}

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

.path-text {
  word-break: break-all;
}

.uuid-text {
  font-family: monospace;
  font-size: 12px;
}

// 文件浏览对话框
.file-dialog-content {
  .file-dialog-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
  }

  .path-container {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 12px;
    background: #f5f7fa;
    border-radius: 6px;
    font-size: 12px;
    color: #606266;
    flex: 1;
    margin-right: 16px;
  }

  .file-dialog-actions {
    display: flex;
    gap: 8px;
  }

  .file-dialog-scrollbar {
    height: 50vh;
  }

  .file-dialog-list {
    min-height: 200px;
  }

  .file-dialog-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 10px 16px;
    cursor: pointer;
    transition: background-color 0.2s;
    border-radius: 6px;

    &:hover {
      background: #f5f7fa;
    }
  }

  .file-info {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .file-icon {
    font-size: 18px;
  }

  .folder-icon { color: #e6a23c; }
  .file-icon-doc { color: #409eff; }

  .file-actions {
    display: flex;
    gap: 8px;
  }

  .empty-files {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 40px;
    color: #909399;
    gap: 8px;
  }
}

// 预览对话框
.preview-content-wrapper {
  min-height: 200px;
}

.preview-scrollbar {
  height: 60vh;
}

.preview-text {
  font-family: 'Consolas', 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-all;
  padding: 16px;
  margin: 0;
}

.image-wrapper {
  display: flex;
  justify-content: center;
  padding: 16px;
}

.preview-image {
  max-width: 100%;
  max-height: 60vh;
  object-fit: contain;
}

.unsupported-preview {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #909399;
  gap: 8px;
}
</style>


