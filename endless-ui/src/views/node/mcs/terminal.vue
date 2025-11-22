<template>
  <div class="app-container">
    <el-card class="header-card" shadow="hover">
      <div class="header">
        <div class="title-section">
          <div class="title">
            <span
              :class="{ running: statusTag==='success', stopped: statusTag==='info', pulsing: statusTag==='success' }"
              class="status-dot"></span>
            <i class="el-icon-cpu title-icon"></i>
            <span class="title-text">{{ instanceInfo ? instanceInfo.name : '加载中...' }}</span>
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
              <i :class="statusTag==='success' ? 'el-icon-success' : 'el-icon-warning'"></i>
              {{ statusText }}
            </el-tag>
            <el-tag v-if="serverStatus && serverStatus.runtime && serverStatus.runtime.runtimeFormatted"
                    class="meta-tag"
                    size="small">
              <i class="el-icon-time"></i>
              运行时长: {{ serverStatus.runtime.runtimeFormatted }}
            </el-tag>
            <el-tag
              v-if="serverStatus && serverStatus.processInfo && serverStatus.processInfo.resourceUsage && hasCpuPercent(serverStatus.processInfo.resourceUsage.cpuPercent)"
              class="meta-tag"
              size="small">
              <i class="el-icon-cpu"></i>
              CPU: {{ formatCpuPercent(serverStatus.processInfo.resourceUsage.cpuPercent) }}%
            </el-tag>
            <el-tag
              v-if="serverStatus && serverStatus.processInfo && serverStatus.processInfo.resourceUsage && serverStatus.processInfo.resourceUsage.memoryMB"
              class="meta-tag"
              size="small">
              <i class="el-icon-pie-chart"></i>
              内存: {{ formatMemory(serverStatus.processInfo.resourceUsage.memoryMB) }}
            </el-tag>
          </div>
        </div>
        <div class="actions">
          <el-button :loading="opLoading" class="action-btn" icon="el-icon-video-play" size="small" type="success"
                     @click="handleStart">启动
          </el-button>
          <el-button :loading="opLoading" class="action-btn" icon="el-icon-video-pause" size="small"
                     @click="handleStop">停止
          </el-button>
          <el-button :loading="opLoading" class="action-btn" icon="el-icon-refresh" size="small" type="warning"
                     @click="handleRestart">重启
          </el-button>
          <el-button :loading="opLoading" class="action-btn" icon="el-icon-close" size="small" type="danger"
                     @click="handleKill">强制终止
          </el-button>
        </div>
      </div>
    </el-card>

    <el-row :gutter="16">
      <el-col :span="16">
        <el-card class="terminal-card" shadow="hover">
          <div slot="header" class="terminal-header">
            <div class="terminal-title">
              <i class="el-icon-monitor"></i>
              <span>控制台输出</span>
            </div>
            <div class="terminal-controls">
              <span class="terminal-dot red"></span>
              <span class="terminal-dot yellow"></span>
              <span class="terminal-dot green"></span>
            </div>
          </div>
          <div class="terminal-wrapper">
            <div ref="terminal" class="terminal" @click="focusInput">
              <pre :class="{ 'empty-content': !consoleText }" class="content">{{ consoleText || ' ' }}</pre>
            </div>
          </div>
          <div class="cmd-bar">
            <el-input
              v-model="command"
              class="cmd-input"
              placeholder="输入指令并回车，例如：say hello"
              prefix-icon="el-icon-edit-outline"
              size="small"
              @keyup.enter.native="sendCommand"
            >
              <template slot="prepend">
                <i class="el-icon-right"></i>
              </template>
              <el-button slot="append" :loading="cmdLoading" icon="el-icon-s-promotion" type="primary"
                         @click="sendCommand">发送
              </el-button>
            </el-input>
          </div>

          <!-- 快捷配置文件访问区域 -->
          <div v-if="quickConfigFiles.length > 0" class="quick-config-section">
            <div class="quick-config-header">
              <i class="el-icon-setting"></i>
              <span>快捷配置</span>
              <span class="config-count">{{ quickConfigFiles.length }} 个文件</span>
            </div>
            <div class="quick-config-buttons">
              <el-tooltip
                v-for="file in quickConfigFiles"
                :key="file.fullPath"
                :content="`编辑 ${file.name}`"
                effect="dark"
                placement="top"
              >
                <el-button
                  class="config-quick-btn"
                  size="mini"
                  @click="handleEditConfig(file)"
                >
                  <i class="el-icon-document"></i>
                  <span>{{ file.name }}</span>
                </el-button>
              </el-tooltip>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
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
        <el-card class="side-card status-card" shadow="hover">
          <div slot="header" class="card-header">
            <i class="el-icon-data-line"></i>
            <span>运行状态</span>
          </div>
          <el-descriptions v-if="serverStatus" :column="1" border class="info-descriptions" size="small">
            <el-descriptions-item label="运行状态">
              <el-tag :type="serverStatus.isRunning ? 'success' : 'info'" size="small">
                <i :class="serverStatus.isRunning ? 'el-icon-success' : 'el-icon-warning'"></i>
                {{ serverStatus.isRunning ? '运行中' : '已停止' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item v-if="serverStatus.runtime" label="运行时长">
              <i class="el-icon-time"></i>
              {{ serverStatus.runtime.runtimeFormatted || '0秒' }}
            </el-descriptions-item>
            <el-descriptions-item v-if="serverStatus.runtime && serverStatus.runtime.startTime" label="启动时间">
              <i class="el-icon-time"></i>
              {{ new Date(serverStatus.runtime.startTime).toLocaleString() }}
            </el-descriptions-item>
            <el-descriptions-item v-if="serverStatus.config && serverStatus.config.version" label="版本">
              <i class="el-icon-document"></i>
              {{ serverStatus.config.version }}
            </el-descriptions-item>
            <el-descriptions-item v-if="serverStatus.processInfo && serverStatus.processInfo.command"
                                  label="Java">
              <i class="el-icon-document"></i>
              {{ serverStatus.processInfo.command }}
            </el-descriptions-item>
            <el-descriptions-item v-if="serverStatus.config && serverStatus.config.coreType" label="核心类型">
              <i class="el-icon-cpu"></i>
              {{ serverStatus.config.coreType }}
            </el-descriptions-item>
            <el-descriptions-item v-if="serverStatus.config && serverStatus.config.port" label="端口">
              <i class="el-icon-connection"></i>
              {{ serverStatus.config.port }}
            </el-descriptions-item>
            <el-descriptions-item v-if="serverStatus.config && serverStatus.config.memoryMb" label="内存">
              <i class="el-icon-pie-chart"></i>
              {{ serverStatus.config.memoryMb }} MB
            </el-descriptions-item>
            <el-descriptions-item v-if="serverStatus.processInfo && serverStatus.processInfo.pid" label="PID">
              <i class="el-icon-link"></i>
              {{ serverStatus.processInfo.pid }}
            </el-descriptions-item>
            <el-descriptions-item
              v-if="serverStatus.processInfo && serverStatus.processInfo.resourceUsage && serverStatus.processInfo.resourceUsage.memoryMB"
              label="内存占用">
              <i class="el-icon-pie-chart"></i>
              {{ serverStatus.processInfo.resourceUsage.memoryMB.toFixed(2) }} MB
            </el-descriptions-item>
            <el-descriptions-item
              v-if="serverStatus.processInfo && serverStatus.processInfo.resourceUsage && hasCpuPercent(serverStatus.processInfo.resourceUsage.cpuPercent)"
              label="CPU使用率">
              <i class="el-icon-cpu"></i>
              {{ formatCpuPercent(serverStatus.processInfo.resourceUsage.cpuPercent) }}%
            </el-descriptions-item>
            <el-descriptions-item
              v-if="serverStatus.processInfo && (serverStatus.processInfo.totalCpuDurationSeconds != null || serverStatus.processInfo.totalCpuDurationNanos != null)"
              label="CPU累计时间">
              <i class="el-icon-time"></i>
              {{
                formatCpuDuration(serverStatus.processInfo.totalCpuDurationSeconds, serverStatus.processInfo.totalCpuDurationNanos)
              }}
            </el-descriptions-item>
          </el-descriptions>
          <div v-else class="loading-placeholder">
            <i class="el-icon-loading"></i>
            <span>加载状态中...</span>
          </div>
        </el-card>
        <el-card class="side-card file-card" shadow="hover">
          <div slot="header" class="card-header">
            <i class="el-icon-folder"></i>
            <span>文件浏览</span>
            <el-button
              :disabled="!canGoParent"
              class="parent-btn"
              icon="el-icon-back"
              style="float: right; padding: 3px 8px"
              type="text"
              @click="goParent"
            >
              上级
            </el-button>
          </div>
          <div class="path-container">
            <i class="el-icon-location-outline"></i>
            <div class="path">{{ currentPath || (instanceInfo && instanceInfo.serverPath) || '/' }}</div>
          </div>
          <el-skeleton :count="4" :loading="filesLoading" animated>
            <el-scrollbar class="file-list-scrollbar">
              <div class="file-list-content">
                <div v-if="fileItems.length === 0 && !filesLoading" class="empty-files">
                  <i class="el-icon-document-delete"></i>
                  <span>目录为空</span>
                </div>
                <div
                  v-for="item in fileItems"
                  :key="item.fullPath"
                  class="file-row"
                  @click="handleFileClick(item)"
                  @dblclick="enter(item)"
                >
                  <i
                    :class="item.isDir ? 'el-icon-folder file-icon folder-icon' : 'el-icon-document file-icon file-icon-doc'"/>
                  <span :title="item.name" class="name">{{ item.name }}</span>
                  <div v-if="!item.isDir" class="file-actions">
                    <i class="el-icon-view" title="预览" @click.stop="handlePreview(item)"></i>
                    <i
                      v-if="isMcConfigFile(item.name)"
                      class="el-icon-edit"
                      title="编辑配置"
                      @click.stop="handleEditConfig(item)"
                    ></i>
                  </div>
                </div>
              </div>
            </el-scrollbar>
          </el-skeleton>
        </el-card>
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
        <!-- 文本文件预览 -->
        <div v-if="previewFile && isTextFile(previewFile.name)" class="text-preview">
          <el-scrollbar class="preview-scrollbar">
            <pre class="preview-text">{{ previewContent }}</pre>
          </el-scrollbar>
        </div>
        <!-- 图片预览 -->
        <div v-else-if="previewFile && isImageFile(previewFile.name)" class="image-preview-container">
          <el-scrollbar class="preview-scrollbar">
            <div class="image-wrapper">
              <img :alt="previewFile.name" :src="previewUrl" class="preview-image"/>
            </div>
          </el-scrollbar>
        </div>
        <!-- 不支持预览 -->
        <div v-else class="unsupported-preview">
          <i class="el-icon-warning"></i>
          <p>此文件类型不支持预览</p>
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="previewDialogVisible = false">关 闭</el-button>
        <el-button
          v-if="previewFile && isMcConfigFile(previewFile.name)"
          type="primary"
          @click="openEditDialog"
        >
          编辑配置
        </el-button>
      </div>
    </el-dialog>

    <!-- MC配置文件编辑对话框 -->
    <el-dialog
      :close-on-click-modal="false"
      :title="editFile ? `编辑配置 - ${editFile.name}` : '编辑配置'"
      :visible.sync="editDialogVisible"
      class="config-edit-dialog"
      width="90%"
    >
      <el-row :gutter="20">
        <!-- 左侧：原始配置 -->
        <el-col :span="showTranslation ? 12 : 24">
          <el-card class="editor-panel-card" shadow="never">
            <div slot="header" class="editor-panel-header">
              <div class="header-left">
                <i class="el-icon-document"></i>
                <span>原始配置</span>
              </div>
              <el-button
                icon="el-icon-s-tools"
                plain
                size="mini"
                type="primary"
                @click="showTranslation = !showTranslation"
              >
                {{ showTranslation ? '隐藏' : '显示' }}配置说明
              </el-button>
            </div>
            <el-scrollbar style="height: 60vh;">
              <div v-loading="editContentLoading" class="editor-wrapper">
                <monaco-editor
                  v-if="editDialogVisible && editContent !== ''"
                  :key="editFile ? editFile.fullPath : 'editor'"
                  v-model="editContent"
                  :language="editorOptions.language"
                  :options="editorOptions"
                  class="config-editor"
                  @editorDidMount="onEditorMount"
                />
                <div v-else-if="editDialogVisible && editContent === '' && !editContentLoading" class="editor-empty">
                  <i class="el-icon-loading"></i>
                  <span>正在加载文件内容...</span>
                </div>
              </div>
            </el-scrollbar>
          </el-card>
        </el-col>
        <!-- 右侧：配置说明 -->
        <el-col v-if="showTranslation" :span="12">
          <el-card class="config-panel-card" shadow="never">
            <div slot="header" class="config-panel-header">
              <i class="el-icon-s-tools"></i>
              <span>配置说明</span>
              <el-tag size="mini" type="info">{{ translatedConfig.length }} 项</el-tag>
            </div>
            <el-scrollbar style="height: 60vh;">
              <div class="translation-panel">
                <div v-for="(item, index) in translatedConfig" :key="index" class="config-item">
                  <div class="config-item-header">
                    <span class="config-item-key">{{ item.key }}</span>
                    <el-tag v-if="item.type" class="config-item-type" size="mini">{{ item.type }}</el-tag>
                  </div>
                  <div class="config-item-body">
                    <div class="config-item-value">
                      <!-- 布尔类型使用开关 -->
                      <el-switch
                        v-if="item.isBool"
                        v-model="item.boolValue"
                        active-color="#67c23a"
                        inactive-color="#dcdfe6"
                        @change="updateConfigValue(item)"
                      >
                      </el-switch>
                      <!-- 其他类型使用输入框 -->
                      <el-input
                        v-else
                        v-model="item.value"
                        placeholder="请输入值"
                        size="small"
                        @input="updateConfigValue(item)"
                      >
                        <i slot="prefix" class="el-icon-edit"></i>
                      </el-input>
                    </div>
                    <div class="config-item-desc">
                      <i class="el-icon-info"></i>
                      <span>{{ item.zhDesc }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </el-scrollbar>
          </el-card>
        </el-col>
      </el-row>
      <div slot="footer" class="dialog-footer">
        <el-button @click="editDialogVisible = false">取 消</el-button>
        <el-button :loading="saveLoading" type="primary" @click="handleSaveConfig">保 存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {getMcs} from '@/api/node/mcs'
import {
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
import MonacoEditor from 'monaco-editor-vue'
import './terminal.scss'
import {mcConfigTranslations} from "@/views/node/mcs/mcConfigTranslations";

export default {
  name: 'McsTerminal',
  components: {
    MonacoEditor
  },
  data() {
    return {
      serverId: Number(this.$route.query.serverId) || null,
      instanceInfo: null,
      consoleText: '',
      command: '',
      pollTimer: null,
      statusTimer: null, // 状态轮询定时器
      cmdLoading: false,
      opLoading: false,
      statusText: '未知',
      statusTag: 'warning',
      serverStatus: null, // 服务器状态信息
      // 右侧
      filesLoading: false,
      currentPath: '',
      fileItems: [],
      // WebSocket连接信息
      wsInfo: {
        wsUrl: '',
        console: '',
        subscribe: '',
        token: ''
      },
      // 文件预览相关
      previewDialogVisible: false,
      previewFile: null,
      previewContent: '',
      previewUrl: '',
      previewLoading: false,
      // 编辑配置相关
      editDialogVisible: false,
      editFile: null,
      editContent: '',
      editContentLoading: false,
      saveLoading: false,
      showTranslation: true,
      updateTimer: null, // 防抖定时器
      editorOptions: {
        theme: 'vs-dark',
        language: 'properties',
        automaticLayout: true,
        minimap: {enabled: true},
        wordWrap: 'on',
        fontSize: 14,
        lineNumbers: 'on',
        renderWhitespace: 'selection'
      },
      // MC配置文件翻译
      mcConfigTranslations
    }
  },
  created() {
    // 检查必要参数
    if (!this.serverId) {
      this.$message.error('缺少必要的参数：serverId');
      this.$router.push('/node/mcs/index');
      return;
    }

    // 获取实例信息（会在获取成功后自动启动状态轮询）
    this.getInstanceInfo();
  },
  watch: {
    // 监听路由参数变化，当切换不同服务器时重新加载数据
    '$route.query.serverId': {
      handler(newServerId, oldServerId) {
        // 只有当 serverId 真正变化时才重新加载
        if (newServerId && newServerId !== oldServerId) {
          // 更新 serverId
          this.serverId = Number(newServerId);

          // 断开旧的 WebSocket 连接
          this.disconnectWs();

          // 停止旧的状态轮询
          this.stopStatusPolling();

          // 清空控制台内容
          this.consoleText = '';

          // 清空文件列表
          this.fileItems = [];
          this.currentPath = '';

          // 清空状态信息
          this.serverStatus = null;
          this.instanceInfo = null;

          // 重新获取实例信息
          this.getInstanceInfo();
        }
      },
      immediate: false
    }
  },
  computed: {
    canGoParent() {
      if (!this.currentPath) return false
      const norm = this.currentPath.replace(/\\/g, '/').replace(/\/$/, '')
      const base = (this.instanceInfo && this.instanceInfo.serverPath || '').replace(/\\/g, '/').replace(/\/$/, '')
      return norm.length > base.length
    },
    // 快捷配置文件列表
    quickConfigFiles() {
      if (!this.fileItems || this.fileItems.length === 0) return []
      const mcConfigFiles = [
        'server.properties',
        'bukkit.yml',
        'spigot.yml',
        'paper.yml',
        'paper-global.yml',
        'paper-world-defaults.yml',
        'velocity.toml',
        'bungeecord.yml',
        'config.yml'
      ]
      return this.fileItems.filter(item =>
          !item.isDir && mcConfigFiles.some(configFile =>
            item.name.toLowerCase() === configFile.toLowerCase()
          )
      ).slice(0, 8) // 最多显示8个配置文件
    },
    translatedConfig() {
      if (!this.editFile || !this.editContent) return []
      const fileName = this.editFile.name.toLowerCase()
      const translations = this.mcConfigTranslations[fileName] || {}
      const lines = this.editContent.split('\n')
      const result = []

      lines.forEach((line, index) => {
        const trimmed = line.trim()
        if (!trimmed || trimmed.startsWith('#')) return

        const match = trimmed.match(/^([^=:#]+)[=:](.+)$/)
        if (match) {
          const key = match[1].trim()
          const value = match[2].trim()
          const translation = translations[key] || {}

          // 判断是否为布尔类型
          const isBool = value.toLowerCase() === 'true' || value.toLowerCase() === 'false'
          const boolValue = value.toLowerCase() === 'true'

          // 自动推断类型（如果没有翻译）
          let autoType = ''
          if (!translation.type) {
            if (isBool) {
              autoType = '布尔'
            } else if (/^\d+$/.test(value)) {
              autoType = '数字'
            } else if (/^\d+\.\d+$/.test(value)) {
              autoType = '小数'
            } else {
              autoType = '文本'
            }
          }

          result.push({
            key: key,
            value: value,
            originalValue: value,
            lineIndex: index,
            zhDesc: translation.zh || '暂无说明',
            type: translation.type || autoType,
            isBool: isBool,
            boolValue: boolValue
          })
        }
      })

      return result
    }
  },
  beforeDestroy() {
    this.disconnectWs()
    // 停止状态轮询
    this.stopStatusPolling()
    // 清理预览URL
    if (this.previewUrl) {
      URL.revokeObjectURL(this.previewUrl)
    }
    // 清理防抖定时器
    if (this.updateTimer) {
      clearTimeout(this.updateTimer)
    }
  },
  methods: {
    // 获取实例信息
    async getInstanceInfo() {
      try {
        const response = await getMcs(this.serverId);
        if (response.code === 200 && response.data) {
          this.instanceInfo = response.data;
          // 初始化文件浏览
          this.initInfoAndFiles();
          // 先获取历史日志
          await this.fetchConsoleHistory();
          // 获取WebSocket信息并连接
          this.fetchConsoleWsInfo().then(() => {
            this.connectWs();
          });
          // 立即获取一次状态
          this.fetchServerStatus();
          // 确保状态轮询已启动
          if (!this.statusTimer) {
            this.startStatusPolling();
          }
        } else {
          this.$message.error('获取实例信息失败');
          this.$router.push('/node/mcs/index');
        }
      } catch (error) {
        console.error('获取实例信息失败:', error);
        this.$message.error('获取实例信息失败');
        this.$router.push('/node/mcs/index');
      }
    },
    // 获取控制台历史日志
    async fetchConsoleHistory() {
      if (!this.instanceInfo) return;
      try {
        const response = await getNodeInstanceConsoleHistory({
          id: this.instanceInfo.nodeId,
          serverId: this.serverId
        });
        if (response.code === 200 && response.data && response.data.logs) {
          // 将历史日志合并到控制台文本中
          const historyLogs = response.data.logs;
          if (historyLogs && historyLogs.length > 0) {
            this.consoleText = historyLogs.join('\n');
            // 滚动到底部
            this.$nextTick(() => {
              this.scrollToBottom();
            });
          }
        }
      } catch (error) {
        console.error('获取控制台历史日志失败:', error);
        // 不显示错误提示，因为历史日志是可选的
      }
    },
    // 获取服务器状态
    async fetchServerStatus() {
      if (!this.instanceInfo) return;
      try {
        const response = await getNodeInstanceStatus({
          id: this.instanceInfo.nodeId,
          serverId: this.serverId
        });
        if (response.code === 200 && response.data) {
          this.serverStatus = response.data;
          // 更新运行状态显示
          this.updateStatusDisplay(response.data);
        }
      } catch (error) {
        console.error('获取服务器状态失败:', error);
      }
    },
    // 更新状态显示
    updateStatusDisplay(status) {
      if (!status) return;

      // 更新运行状态
      if (status.isRunning) {
        this.statusText = '运行中';
        this.statusTag = 'success';
      } else {
        this.statusText = '已停止';
        this.statusTag = 'info';
      }
    },
    // 开始状态轮询
    startStatusPolling() {
      // 如果已经启动，先停止
      if (this.statusTimer) {
        this.stopStatusPolling();
      }
      // 立即执行一次
      if (this.instanceInfo) {
        this.fetchServerStatus();
      }
      // 每10秒轮询一次
      this.statusTimer = setInterval(() => {
        if (this.instanceInfo) {
          this.fetchServerStatus();
        }
      }, 10000);
    },
    // 停止状态轮询
    stopStatusPolling() {
      if (this.statusTimer) {
        clearInterval(this.statusTimer);
        this.statusTimer = null;
      }
    },
    // 检查是否有CPU使用率数据
    hasCpuPercent(cpuPercent) {
      if (cpuPercent == null || cpuPercent === undefined) return false;
      // 如果是字符串，检查是否为空
      if (typeof cpuPercent === 'string') {
        const trimmed = cpuPercent.trim();
        if (trimmed === '' || trimmed === 'PercentProcessorTime') return false;
        const num = parseFloat(trimmed);
        return !isNaN(num);
      }
      // 如果是数字，检查是否有效
      return !isNaN(cpuPercent);
    },
    // 格式化CPU使用率
    formatCpuPercent(cpuPercent) {
      if (cpuPercent == null || cpuPercent === undefined) return '0';
      // 如果是字符串，尝试转换为数字
      const num = typeof cpuPercent === 'string' ? parseFloat(cpuPercent.trim()) : cpuPercent;
      if (isNaN(num)) return '0';
      // 保留2位小数
      return num.toFixed(2);
    },
    // 格式化内存
    formatMemory(memoryMB) {
      if (!memoryMB) return '0 MB';
      const num = typeof memoryMB === 'string' ? parseFloat(memoryMB) : memoryMB;
      if (isNaN(num)) return '0 MB';
      if (num >= 1024) {
        return (num / 1024).toFixed(2) + ' GB';
      }
      return num.toFixed(2) + ' MB';
    },
    // 格式化CPU累计时间
    formatCpuDuration(seconds, nanos) {
      if (seconds == null && nanos == null) return '-';

      let totalSeconds = 0;
      if (seconds != null) {
        totalSeconds = typeof seconds === 'string' ? parseFloat(seconds) : seconds;
      }

      let totalNanos = 0;
      if (nanos != null) {
        totalNanos = typeof nanos === 'string' ? parseFloat(nanos) : nanos;
        // 将纳秒转换为秒（1秒 = 1,000,000,000 纳秒）
        totalSeconds += totalNanos / 1000000000;
      }

      if (isNaN(totalSeconds) || totalSeconds < 0) return '-';

      // 格式化时间
      const hours = Math.floor(totalSeconds / 3600);
      const minutes = Math.floor((totalSeconds % 3600) / 60);
      const secs = Math.floor(totalSeconds % 60);
      const milliseconds = Math.floor((totalSeconds % 1) * 1000);

      const parts = [];
      if (hours > 0) parts.push(hours + '小时');
      if (minutes > 0) parts.push(minutes + '分钟');
      if (secs > 0 || parts.length === 0) parts.push(secs + '秒');
      if (milliseconds > 0 && parts.length < 3) parts.push(milliseconds + '毫秒');

      return parts.join(' ') || '0秒';
    },
    initInfoAndFiles() {
      // 若没有目录，从后端获取实例详情接口中加载；否则直接列当前目录
      if (this.instanceInfo && this.instanceInfo.serverPath) {
        this.currentPath = this.instanceInfo.serverPath
        this.loadFiles(this.currentPath)
      }
    },
    loadFiles(path) {
      if (!this.instanceInfo) return;
      this.filesLoading = true
      this.currentPath = path
      // 使用主控端文件列表接口
      const {getFileList} = require('@/api/node/server')
      getFileList({id: this.instanceInfo.nodeId, path}).then(res => {
        // 兼容不同返回结构
        const list = (res.data && (res.data.files || res.data.list || res.data)) || []
        const mappedItems = list.map(it => {
          // 处理目录标识：优先使用 isDirectory，然后是 isDir，最后是其他字段
          const isDir = it.isDirectory !== undefined ? it.isDirectory :
            (it.isDir !== undefined ? it.isDir :
              (it.directory !== undefined ? it.directory :
                (it.type === 'DIR' || it.dir || false)))

          // 处理文件名
          const fileName = it.name || it.fileName || (it.path ? it.path.split(/[\\/]/).pop() : null) || '未知'

          // 处理完整路径：优先使用 path，然后是 fullPath，最后拼接
          const fullPath = it.path || it.fullPath ||
            (path ? (path.replace(/[\\/]$/, '') + '/' + fileName) : fileName)

          return {
            name: fileName,
            isDir: isDir,
            fullPath: fullPath,
            // 保留原始数据，方便后续使用
            totalSpace: it.totalSpace || 0,
            lastModified: it.lastModified || 0
          }
        })

        // 排序：目录在前，文件在后，然后按名称排序
        this.fileItems = mappedItems.sort((a, b) => {
          // 先按目录/文件排序
          if (a.isDir && !b.isDir) return -1
          if (!a.isDir && b.isDir) return 1
          // 同类型按名称排序（支持中文和数字排序）
          return a.name.localeCompare(b.name, 'zh-CN', {numeric: true, sensitivity: 'base'})
        })
      }).finally(() => {
        this.filesLoading = false
      })
    },
    enter(item) {
      if (item.isDir) {
        this.loadFiles(item.fullPath)
      }
    },
    goParent() {
      if (!this.currentPath) return
      const segs = this.currentPath.replace(/\\/g, '/').split('/').filter(Boolean)
      if (segs.length <= 1) return
      segs.pop()
      const parent = (this.currentPath.startsWith('/') ? '/' : '') + segs.join('/')
      this.loadFiles(parent)
    },
    fetchConsoleWsInfo() {
      if (!this.instanceInfo) return Promise.resolve();
      // 获取WebSocket连接信息
      return getNodeInstanceConsole({id: this.instanceInfo.nodeId, serverId: this.serverId}).then(res => {
        if (res && res.data) {
          this.wsInfo = {
            wsUrl: res.data.wsUrl || '',
            console: res.data.console || '',
            subscribe: res.data.subscribe || '',
            token: res.data.token || ''
          }
        }
      }).catch(() => {
        // 获取失败时使用默认配置
        this.wsInfo = {
          wsUrl: '',
          console: '',
          subscribe: '',
          token: ''
        }
      })
    },
    connectWs() {
      if (!this.wsInfo.wsUrl || !this.wsInfo.console || !this.wsInfo.subscribe || !this.wsInfo.token) {
        console.warn('WebSocket连接信息不完整，使用默认连接方式')
        return
      }

      // 使用后端返回的WebSocket连接信息
      const SockJS = require('sockjs-client/dist/sockjs.min.js')
      const Stomp = require('stompjs')

      try {
        const sock = new SockJS(this.wsInfo.wsUrl)
        this.stompClient = Stomp.over(sock)
        this.stompClient.debug = null

        this.stompClient.connect({
          "X-Endless-Token": this.wsInfo.token
        }, () => {
          // 订阅控制台主题
          this.subscription = this.stompClient.subscribe(this.wsInfo.console + this.instanceInfo.nodeInstancesId, (msg) => {
            try {
              const body = JSON.parse(msg.body)
              if (body.line) {
                this.consoleText += (this.consoleText ? '\n' : '') + body.line
              } else if (body.error) {
                this.consoleText += (this.consoleText ? '\n' : '') + `[ERROR] ${body.error}`
              } else if (body.message) {
                this.consoleText += (this.consoleText ? '\n' : '') + body.message
              } else if (body.console) {
                this.consoleText = body.console
              }
              this.$nextTick(() => this.scrollToBottom())
            } catch (e) {
              console.error('处理WebSocket消息失败:', e)
            }
          })

          // 发送订阅指令
          this.stompClient.send(this.wsInfo.subscribe, {}, JSON.stringify({
            serverId: this.instanceInfo.nodeInstancesId,
            token: this.wsInfo.token
          }))
        }, () => {
          // 断线重连
          setTimeout(() => this.connectWs(), 2000)
        })
      } catch (error) {
        console.error('WebSocket连接失败，使用默认连接方式:', error)
      }
    },
    disconnectWs() {
      try {
        if (this.subscription) this.subscription.unsubscribe()
      } catch (e) {
        console.error("取消订阅失败:", e)
      }
      try {
        if (this.stompClient) this.stompClient.disconnect(() => {
        })
      } catch (e) {
        console.error("断开WebSocket连接失败:", e)
      }
      this.subscription = null
      this.stompClient = null
      // 清理WebSocket连接信息
      this.wsInfo = {
        wsUrl: '',
        console: '',
        subscribe: '',
        token: ''
      }
    },
    scrollToBottom() {
      const el = this.$refs.terminal
      if (!el) return
      el.scrollTop = el.scrollHeight
    },
    focusInput() {
      // 用于未来可扩展聚焦输入
    },
    sendCommand() {
      if (!this.command || !this.instanceInfo) return
      this.cmdLoading = true
      sendNodeInstanceCommand({
        id: this.instanceInfo.nodeId,
        serverId: this.serverId,
        command: this.command
      }).then(() => {
        this.command = ''
      }).finally(() => {
        this.cmdLoading = false
      })
    },
    handleStart() {
      if (!this.instanceInfo) return
      this.opLoading = true
      startNodeInstance({id: this.instanceInfo.nodeId, serverId: this.serverId}).then(() => {
        this.$message.success('启动指令已发送')
        // 延迟一下再获取状态，等待服务器启动
        setTimeout(() => this.fetchServerStatus(), 2000)
      }).finally(() => this.opLoading = false)
    },
    handleStop() {
      if (!this.instanceInfo) return
      this.opLoading = true
      stopNodeInstance({id: this.instanceInfo.nodeId, serverId: this.serverId}).then(() => {
        this.$message.success('停止指令已发送')
        // 延迟一下再获取状态
        setTimeout(() => this.fetchServerStatus(), 2000)
      }).finally(() => this.opLoading = false)
    },
    handleRestart() {
      if (!this.instanceInfo) return
      this.opLoading = true
      restartNodeInstance({id: this.instanceInfo.nodeId, serverId: this.serverId}).then(() => {
        this.$message.success('重启指令已发送')
        // 延迟一下再获取状态
        setTimeout(() => this.fetchServerStatus(), 3000)
      }).finally(() => this.opLoading = false)
    },
    handleKill() {
      if (!this.instanceInfo) return
      this.opLoading = true
      killNodeInstance({id: this.instanceInfo.nodeId, serverId: this.serverId}).then(() => {
        this.$message.success('强制终止指令已发送')
        // 延迟一下再获取状态
        setTimeout(() => this.fetchServerStatus(), 2000)
      }).finally(() => this.opLoading = false)
    },
    // 文件相关方法
    handleFileClick(item) {
      // 单击文件时不做任何操作，双击才进入
    },
    // 判断是否为MC配置文件
    isMcConfigFile(filename) {
      if (!filename) return false
      const mcConfigFiles = [
        'server.properties',
        'bukkit.yml',
        'spigot.yml',
        'paper.yml',
        'paper-global.yml',
        'paper-world-defaults.yml',
        'velocity.toml',
        'bungeecord.yml',
        'config.yml'
      ]
      return mcConfigFiles.some(file => filename.toLowerCase() === file.toLowerCase())
    },
    // 判断是否为文本文件
    isTextFile(filename) {
      if (!filename) return false
      const textExtensions = ['.txt', '.json', '.xml', '.html', '.css', '.js', '.md', '.log', '.yml', '.yaml', '.properties', '.conf', '.ini', '.toml', '.cfg', '.sh', '.bat']
      return textExtensions.some(ext => filename.toLowerCase().endsWith(ext))
    },
    // 判断是否为图片文件
    isImageFile(filename) {
      if (!filename) return false
      const imageExtensions = ['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp', '.svg', '.ico']
      return imageExtensions.some(ext => filename.toLowerCase().endsWith(ext))
    },
    // 预览文件
    async handlePreview(file) {
      if (!file || !this.instanceInfo) return

      this.previewFile = file
      this.previewLoading = true
      this.previewDialogVisible = true

      try {
        if (this.isTextFile(file.name)) {
          const response = await downloadFile(this.instanceInfo.nodeId, file.fullPath)
          const blob = new Blob([response], {type: 'text/plain'})
          const reader = new FileReader()
          reader.onload = (e) => {
            this.previewContent = e.target.result
            this.previewLoading = false
          }
          reader.readAsText(blob)
        } else if (this.isImageFile(file.name)) {
          const response = await downloadFile(this.instanceInfo.nodeId, file.fullPath)
          const ext = file.name.substring(file.name.lastIndexOf('.') + 1).toLowerCase()
          const mimeTypes = {
            'jpg': 'image/jpeg',
            'jpeg': 'image/jpeg',
            'png': 'image/png',
            'gif': 'image/gif',
            'bmp': 'image/bmp',
            'webp': 'image/webp',
            'svg': 'image/svg+xml'
          }
          const mimeType = mimeTypes[ext] || 'image/jpeg'
          const blob = new Blob([response], {type: mimeType})
          if (this.previewUrl) {
            URL.revokeObjectURL(this.previewUrl)
          }
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
    // 打开编辑对话框
    openEditDialog() {
      if (!this.previewFile) return
      this.handleEditConfig(this.previewFile)
    },
    // 编辑配置文件
    async handleEditConfig(file) {
      if (!file || !this.instanceInfo) return

      this.editFile = file
      this.editContent = ''
      this.editContentLoading = true
      this.saveLoading = false
      this.editDialogVisible = true

      try {
        const response = await downloadFile(this.instanceInfo.nodeId, file.fullPath)
        const blob = new Blob([response], {type: 'text/plain'})
        const reader = new FileReader()
        reader.onload = (e) => {
          this.editContent = e.target.result || ''
          // 根据文件类型设置编辑器语言
          if (file.name.endsWith('.yml') || file.name.endsWith('.yaml')) {
            this.editorOptions.language = 'yaml'
          } else if (file.name.endsWith('.toml')) {
            this.editorOptions.language = 'toml'
          } else {
            this.editorOptions.language = 'properties'
          }
          this.editContentLoading = false
          // 确保编辑器能够正确渲染
          this.$nextTick(() => {
            if (this.editContent === '') {
              this.$message.warning('文件内容为空')
            }
          })
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
    // 编辑器挂载完成回调
    onEditorMount(editor) {
      // 编辑器挂载后确保内容正确显示
      this.$nextTick(() => {
        if (editor && this.editContent) {
          const currentValue = editor.getValue()
          if (currentValue !== this.editContent) {
            editor.setValue(this.editContent)
          }
          // 确保编辑器获得焦点并滚动到顶部
          editor.focus()
          editor.setScrollPosition({scrollTop: 0, scrollLeft: 0})
        }
      })
    },
    // 更新配置值（使用防抖优化性能）
    updateConfigValue(row) {
      if (!this.editContent) return

      // 清除之前的定时器
      if (this.updateTimer) {
        clearTimeout(this.updateTimer)
      }

      // 使用防抖，延迟100ms后再更新编辑器
      this.updateTimer = setTimeout(() => {
        const lines = this.editContent.split('\n')
        const newValue = row.isBool ? (row.boolValue ? 'true' : 'false') : row.value

        // 查找并更新对应的配置行
        for (let i = 0; i < lines.length; i++) {
          const trimmed = lines[i].trim()
          if (trimmed && !trimmed.startsWith('#')) {
            const match = trimmed.match(/^([^=:#]+)[=:](.+)$/)
            if (match && match[1].trim() === row.key) {
              // 保持原有的分隔符（= 或 :）
              const separator = lines[i].includes('=') ? '=' : ':'
              const indent = lines[i].match(/^\s*/)[0]
              lines[i] = `${indent}${row.key}${separator}${newValue}`
              break
            }
          }
        }

        // 更新编辑器内容
        this.editContent = lines.join('\n')
      }, 100)
    },
    // 保存配置文件
    async handleSaveConfig() {
      if (!this.editFile || !this.instanceInfo || !this.editContent) return

      this.saveLoading = true
      try {
        const response = await saveFile({
          id: this.instanceInfo.nodeId,
          path: this.editFile.fullPath,
          content: this.editContent
        })

        if (response.code === 200) {
          this.$message.success('保存成功')
          this.editDialogVisible = false
          // 如果预览对话框打开，更新预览内容
          if (this.previewDialogVisible && this.previewFile && this.previewFile.name === this.editFile.name) {
            this.previewContent = this.editContent
          }
        } else {
          this.$message.error(response.msg || '保存失败')
        }
      } catch (error) {
        this.$message.error('保存失败: ' + (error.message || '未知错误'))
      } finally {
        this.saveLoading = false
      }
    }
  }
}
</script>


