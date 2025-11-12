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
      mcConfigTranslations: {
        'server.properties': {
          'motd': {zh: '服务器描述信息（MOTD）', type: '文本'},
          'server-port': {zh: '服务器端口', type: '数字'},
          'max-players': {zh: '最大玩家数', type: '数字'},
          'online-mode': {zh: '在线模式（正版验证）', type: '布尔'},
          'difficulty': {zh: '难度（peaceful/easy/normal/hard）', type: '文本'},
          'gamemode': {zh: '游戏模式（survival/creative/adventure/spectator）', type: '文本'},
          'level-name': {zh: '世界名称', type: '文本'},
          'level-seed': {zh: '世界种子', type: '文本'},
          'level-type': {zh: '世界类型（default/flat/largeBiomes/amplified）', type: '文本'},
          'spawn-monsters': {zh: '生成怪物', type: '布尔'},
          'spawn-animals': {zh: '生成动物', type: '布尔'},
          'spawn-npcs': {zh: '生成村民', type: '布尔'},
          'spawn-protection': {zh: '出生点保护半径', type: '数字'},
          'allow-flight': {zh: '允许飞行', type: '布尔'},
          'allow-nether': {zh: '允许下界', type: '布尔'},
          'pvp': {zh: '允许PVP（玩家对战）', type: '布尔'},
          'white-list': {zh: '启用白名单', type: '布尔'},
          'enforce-whitelist': {zh: '强制执行白名单', type: '布尔'},
          'view-distance': {zh: '视距（区块数）', type: '数字'},
          'simulation-distance': {zh: '模拟距离（区块数）', type: '数字'},
          'max-world-size': {zh: '最大世界大小', type: '数字'},
          'network-compression-threshold': {zh: '网络压缩阈值（字节）', type: '数字'},
          'max-tick-time': {zh: '最大tick时间（毫秒）', type: '数字'},
          'use-native-transport': {zh: '使用原生传输', type: '布尔'},
          'enable-jmx-monitoring': {zh: '启用JMX监控', type: '布尔'},
          'enable-status': {zh: '启用服务器状态', type: '布尔'},
          'enable-command-block': {zh: '启用命令方块', type: '布尔'},
          'broadcast-rcon-to-ops': {zh: '向OP广播RCON命令', type: '布尔'},
          'broadcast-console-to-ops': {zh: '向OP广播控制台消息', type: '布尔'},
          'op-permission-level': {zh: 'OP权限等级（1-4）', type: '数字'},
          'function-permission-level': {zh: '函数权限等级', type: '数字'},
          'enable-query': {zh: '启用查询', type: '布尔'},
          'query.port': {zh: '查询端口', type: '数字'},
          'enable-rcon': {zh: '启用RCON远程控制', type: '布尔'},
          'rcon.port': {zh: 'RCON端口', type: '数字'},
          'rcon.password': {zh: 'RCON密码', type: '文本'},
          'resource-pack': {zh: '资源包URL', type: '文本'},
          'resource-pack-prompt': {zh: '资源包提示信息', type: '文本'},
          'resource-pack-sha1': {zh: '资源包SHA1校验', type: '文本'},
          'require-resource-pack': {zh: '强制使用资源包', type: '布尔'},
          'generator-settings': {zh: '生成器设置', type: '文本'},
          'force-gamemode': {zh: '强制游戏模式', type: '布尔'},
          'rate-limit': {zh: '速率限制', type: '数字'},
          'hardcore': {zh: '极限模式', type: '布尔'},
          'snooper-enabled': {zh: '启用数据收集', type: '布尔'},
          'sync-chunk-writes': {zh: '同步区块写入', type: '布尔'},
          'player-idle-timeout': {zh: '玩家空闲超时（分钟）', type: '数字'},
          'entity-broadcast-range-percentage': {zh: '实体广播范围百分比', type: '数字'},
          'text-filtering-config': {zh: '文本过滤配置', type: '文本'}
        },
        'bukkit.yml': {
          'settings.allow-end': {zh: '允许末地维度', type: '布尔'},
          'settings.warn-on-overload': {zh: '服务器过载时发出警告', type: '布尔'},
          'settings.permissions-file': {zh: '权限文件路径', type: '文本'},
          'settings.update-folder': {zh: '插件更新文件夹', type: '文本'},
          'settings.plugin-profiling': {zh: '启用插件性能分析', type: '布尔'},
          'settings.connection-throttle': {zh: '连接限流（毫秒）', type: '数字'},
          'settings.query-plugins': {zh: '查询时显示插件', type: '布尔'},
          'settings.deprecated-verbose': {zh: '显示过时警告', type: '布尔'},
          'settings.shutdown-message': {zh: '关闭服务器消息', type: '文本'},
          'spawn-limits.monsters': {zh: '怪物生成数量限制', type: '数字'},
          'spawn-limits.animals': {zh: '动物生成数量限制', type: '数字'},
          'spawn-limits.water-animals': {zh: '水生动物生成数量限制', type: '数字'},
          'spawn-limits.water-ambient': {zh: '水生环境生物生成限制', type: '数字'},
          'spawn-limits.ambient': {zh: '环境生物生成数量限制', type: '数字'},
          'chunk-gc.period-in-ticks': {zh: '区块垃圾回收周期（tick）', type: '数字'},
          'chunk-gc.load-threshold': {zh: '区块GC加载阈值', type: '数字'},
          'ticks-per.animal-spawns': {zh: '动物生成间隔（tick）', type: '数字'},
          'ticks-per.monster-spawns': {zh: '怪物生成间隔（tick）', type: '数字'}
        },
        'spigot.yml': {
          'settings.bungeecord': {zh: '启用BungeeCord代理支持', type: '布尔'},
          'settings.user-cache-size': {zh: '用户缓存大小', type: '数字'},
          'settings.save-user-cache-on-stop-only': {zh: '仅在停止时保存用户缓存', type: '布尔'},
          'settings.sample-count': {zh: '服务器列表样本数量', type: '数字'},
          'settings.player-shuffle': {zh: '随机排列玩家列表', type: '数字'},
          'settings.timeout-time': {zh: '超时时间（秒）', type: '数字'},
          'settings.restart-on-crash': {zh: '崩溃时自动重启', type: '布尔'},
          'settings.restart-script': {zh: '重启脚本路径', type: '文本'},
          'settings.netty-threads': {zh: 'Netty线程数', type: '数字'},
          'settings.attribute.maxHealth.max': {zh: '最大生命值上限', type: '数字'},
          'settings.attribute.movementSpeed.max': {zh: '最大移动速度', type: '数字'},
          'settings.attribute.attackDamage.max': {zh: '最大攻击伤害', type: '数字'},
          'world-settings.default.difficulty': {zh: '默认世界难度', type: '文本'},
          'world-settings.default.spawn-monsters': {zh: '默认生成怪物', type: '布尔'},
          'world-settings.default.spawn-animals': {zh: '默认生成动物', type: '布尔'},
          'world-settings.default.view-distance': {zh: '默认视距', type: '数字'},
          'world-settings.default.mob-spawn-range': {zh: '生物生成范围', type: '数字'},
          'world-settings.default.entity-activation-range.animals': {zh: '动物激活范围', type: '数字'},
          'world-settings.default.entity-activation-range.monsters': {zh: '怪物激活范围', type: '数字'},
          'world-settings.default.entity-activation-range.raiders': {zh: '袭击者激活范围', type: '数字'},
          'world-settings.default.entity-activation-range.misc': {zh: '其他实体激活范围', type: '数字'},
          'world-settings.default.entity-tracking-range.players': {zh: '玩家追踪范围', type: '数字'},
          'world-settings.default.entity-tracking-range.animals': {zh: '动物追踪范围', type: '数字'},
          'world-settings.default.entity-tracking-range.monsters': {zh: '怪物追踪范围', type: '数字'},
          'world-settings.default.ticks-per.hopper-transfer': {zh: '漏斗传输间隔', type: '数字'},
          'world-settings.default.ticks-per.hopper-check': {zh: '漏斗检查间隔', type: '数字'},
          'world-settings.default.hopper-amount': {zh: '漏斗传输数量', type: '数字'},
          'world-settings.default.arrow-despawn-rate': {zh: '箭矢消失速率', type: '数字'},
          'world-settings.default.item-despawn-rate': {zh: '物品消失速率', type: '数字'},
          'world-settings.default.merge-radius.item': {zh: '物品合并半径', type: '数字'},
          'world-settings.default.merge-radius.exp': {zh: '经验球合并半径', type: '数字'},
          'world-settings.default.growth.cactus-modifier': {zh: '仙人掌生长速度', type: '数字'},
          'world-settings.default.growth.cane-modifier': {zh: '甘蔗生长速度', type: '数字'},
          'world-settings.default.growth.melon-modifier': {zh: '西瓜生长速度', type: '数字'},
          'world-settings.default.growth.mushroom-modifier': {zh: '蘑菇生长速度', type: '数字'},
          'world-settings.default.growth.pumpkin-modifier': {zh: '南瓜生长速度', type: '数字'},
          'world-settings.default.growth.sapling-modifier': {zh: '树苗生长速度', type: '数字'},
          'world-settings.default.growth.beetroot-modifier': {zh: '甜菜根生长速度', type: '数字'},
          'world-settings.default.growth.carrot-modifier': {zh: '胡萝卜生长速度', type: '数字'},
          'world-settings.default.growth.potato-modifier': {zh: '土豆生长速度', type: '数字'},
          'world-settings.default.growth.wheat-modifier': {zh: '小麦生长速度', type: '数字'}
        },
        'paper.yml': {
          'settings.allow-unsafe-end-portal-teleportation': {zh: '允许不安全的末地传送门传送', type: '布尔'},
          'settings.per-player-mob-spawns': {zh: '每个玩家独立计算生物生成', type: '布尔'},
          'settings.fix-entity-position-desync': {zh: '修复实体位置不同步', type: '布尔'},
          'settings.load-permissions-yml-before-plugins': {zh: '在插件之前加载权限文件', type: '布尔'},
          'settings.async-chunks.enable': {zh: '启用异步区块加载', type: '布尔'},
          'settings.async-chunks.threads': {zh: '异步区块线程数', type: '数字'},
          'settings.watchdog.early-warning-every': {zh: '看门狗早期警告间隔', type: '数字'},
          'settings.watchdog.early-warning-delay': {zh: '看门狗早期警告延迟', type: '数字'},
          'settings.spam-limiter.tab-spam-increment': {zh: 'Tab补全垃圾信息增量', type: '数字'},
          'settings.spam-limiter.tab-spam-limit': {zh: 'Tab补全垃圾信息限制', type: '数字'},
          'settings.book-size.page-max': {zh: '书本最大页数', type: '数字'},
          'settings.book-size.total-multiplier': {zh: '书本总大小倍数', type: '数字'},
          'settings.velocity-support.enabled': {zh: '启用Velocity代理支持', type: '布尔'},
          'settings.velocity-support.online-mode': {zh: 'Velocity在线模式', type: '布尔'},
          'settings.velocity-support.secret': {zh: 'Velocity密钥', type: '文本'},
          'chunk-loading.auto-configure-send-distance': {zh: '自动配置区块发送距离', type: '布尔'},
          'chunk-loading.enable-frustum-priority': {zh: '启用视锥体优先级', type: '布尔'},
          'chunk-loading.max-concurrent-sends': {zh: '最大并发发送区块数', type: '数字'},
          'chunk-loading.min-load-radius': {zh: '最小加载半径', type: '数字'},
          'chunk-loading.player-max-concurrent-loads': {zh: '玩家最大并发加载区块数', type: '数字'},
          'chunk-loading.target-player-chunk-send-rate': {zh: '目标玩家区块发送速率', type: '数字'},
          'chunk-loading.global-max-chunk-load-rate': {zh: '全局最大区块加载速率', type: '数字'},
          'chunk-loading.global-max-chunk-send-rate': {zh: '全局最大区块发送速率', type: '数字'}
        }
      }
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

<style scoped>
/* 导入高清等宽字体 */
@import url('https://fonts.googleapis.com/css2?family=JetBrains+Mono:wght@400;500;600;700&display=swap');

.app-container {
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  min-height: calc(100vh - 84px);
}

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
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.header-card >>> .el-card__body {
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
  0% {
    box-shadow: 0 0 0 0 rgba(103, 194, 58, 0.7);
  }
  70% {
    box-shadow: 0 0 0 10px rgba(103, 194, 58, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(103, 194, 58, 0);
  }
}

.meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}

.meta-tag {
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: #ffffff;
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.meta-tag:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-2px);
}

.meta-tag i {
  margin-right: 4px;
}

.status-tag {
  font-weight: 500;
}

.actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.action-btn {
  border-radius: 8px;
  transition: all 0.3s ease;
  font-weight: 500;
  border: 2px solid transparent;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.action-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
}

.action-btn:active {
  transform: translateY(-1px);
}

.terminal-card {
  border-radius: 16px;
  border: none;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  background: #ffffff;
}

.terminal-card:hover {
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.terminal-card >>> .el-card__header {
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  border-bottom: 2px solid #e9ecef;
}

.terminal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0;
}

.terminal-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #303133;
}

.terminal-title i {
  font-size: 18px;
  color: #409eff;
}

.terminal-controls {
  display: flex;
  gap: 6px;
}

.terminal-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  transition: all 0.3s ease;
}

.terminal-dot.red {
  background: #ff5f56;
}

.terminal-dot.yellow {
  background: #ffbd2e;
}

.terminal-dot.green {
  background: #27c93f;
}

.terminal-wrapper {
  background: linear-gradient(135deg, #1a1a1a 0%, #0f0f0f 100%);
  border-radius: 8px;
  padding: 3px;
  margin: 16px 0;
  box-shadow: inset 0 2px 8px rgba(0, 0, 0, 0.5), 0 0 0 1px rgba(255, 255, 255, 0.05);
  position: relative;
}

.terminal-wrapper::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: 8px;
  padding: 1px;
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.1), rgba(103, 194, 58, 0.1));
  -webkit-mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  -webkit-mask-composite: xor;
  mask-composite: exclude;
  pointer-events: none;
}

.terminal {
  background: #0d1117;
  background-image: radial-gradient(circle at 20% 50%, rgba(88, 166, 255, 0.03) 0%, transparent 50%),
  radial-gradient(circle at 80% 80%, rgba(126, 231, 135, 0.03) 0%, transparent 50%),
  linear-gradient(180deg, rgba(13, 17, 23, 0.95) 0%, #0d1117 100%);
  color: #c9d1d9;
  border-radius: 6px;
  padding: 20px 24px;
  height: 520px;
  overflow: auto;
  font-family: 'JetBrains Mono', 'SF Mono', 'Cascadia Code', 'Fira Code', 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 14px;
  font-weight: 400;
  line-height: 1.75;
  letter-spacing: 0.3px;
  position: relative;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-rendering: optimizeLegibility;
  font-feature-settings: 'liga' 1, 'calt' 1;
  box-shadow: inset 0 0 100px rgba(0, 0, 0, 0.3);
}

/* 高清字体渲染优化 */
.terminal {
  text-shadow: 0 0 1px rgba(201, 209, 217, 0.1);
  font-variant-ligatures: common-ligatures;
  font-kerning: normal;
}

/* 控制台顶部渐变遮罩 */
.terminal::before {
  content: '';
  position: sticky;
  top: 0;
  left: 0;
  right: 0;
  height: 20px;
  background: linear-gradient(180deg, rgba(13, 17, 23, 1) 0%, rgba(13, 17, 23, 0) 100%);
  pointer-events: none;
  z-index: 1;
  margin: -20px -24px 0 -24px;
  padding: 0 24px;
}

.terminal::-webkit-scrollbar {
  width: 10px;
  height: 10px;
}

.terminal::-webkit-scrollbar-track {
  background: #0d1117;
  border-radius: 5px;
  border: 1px solid rgba(255, 255, 255, 0.05);
}

.terminal::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, #30363d, #21262d);
  border-radius: 5px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
}

.terminal::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg, #484f58, #30363d);
  box-shadow: inset 0 0 6px rgba(0, 0, 0, 0.3);
}

.terminal::-webkit-scrollbar-corner {
  background: #0d1117;
}

.terminal .content {
  white-space: pre-wrap;
  word-break: break-word;
  margin: 0;
  color: #c9d1d9;
  font-family: inherit;
  font-size: inherit;
  line-height: inherit;
  letter-spacing: inherit;
  -webkit-font-smoothing: inherit;
  -moz-osx-font-smoothing: inherit;
  position: relative;
  z-index: 0;
}

/* 控制台文本样式增强 */
.terminal .content {
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.4), 0 0 1px rgba(201, 209, 217, 0.05);
  filter: contrast(1.05) brightness(1.02);
}

/* 优化长文本显示 */
.terminal .content {
  tab-size: 4;
  -moz-tab-size: 4;
}

/* 优化空控制台显示 - 当内容为空时显示提示 */
.terminal .content.empty-content {
  color: #6e7681;
  font-style: italic;
  opacity: 0.6;
}

.terminal .content.empty-content::before {
  content: '等待连接...';
}

/* 增强文本可读性 - 针对不同字符 */
.terminal .content {
  font-variant-numeric: tabular-nums;
  font-feature-settings: 'tnum' 1, 'zero' 1;
}

.cmd-bar {
  margin-top: 16px;
}

.cmd-input {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
}

.cmd-input:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.cmd-input >>> .el-input__inner {
  background: #161b22;
  color: #c9d1d9;
  border: 1px solid #30363d;
  transition: all 0.3s ease;
  font-family: 'JetBrains Mono', 'SF Mono', 'Cascadia Code', 'Fira Code', 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 14px;
  font-weight: 400;
  letter-spacing: 0.3px;
  padding: 12px 16px;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-rendering: optimizeLegibility;
}

.cmd-input >>> .el-input__inner::placeholder {
  color: #6e7681;
  opacity: 0.8;
}

.cmd-input >>> .el-input__inner:focus {
  border-color: #58a6ff;
  background: #0d1117;
  box-shadow: 0 0 0 3px rgba(88, 166, 255, 0.1);
}

.cmd-input >>> .el-input-group__prepend {
  background: linear-gradient(135deg, #21262d, #161b22);
  border: 1px solid #30363d;
  border-right: none;
  color: #7ee787;
  font-weight: 600;
  font-family: 'JetBrains Mono', 'SF Mono', 'Cascadia Code', 'Fira Code', 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 16px;
  padding: 0 12px;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-shadow: 0 0 8px rgba(126, 231, 135, 0.3);
}

.cmd-input >>> .el-input-group__append {
  border: none;
  background: transparent;
}

.cmd-input >>> .el-input-group__append .el-button {
  font-family: 'JetBrains Mono', 'SF Mono', 'Cascadia Code', 'Fira Code', 'Consolas', 'Monaco', 'Courier New', monospace;
  font-weight: 500;
  letter-spacing: 0.5px;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

/* 快捷配置文件区域 */
.quick-config-section {
  margin-top: 20px;
  padding: 16px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 10px;
  border: 2px solid #dee2e6;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.quick-config-section:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
  border-color: #409eff;
}

.quick-config-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-weight: 600;
  font-size: 15px;
  color: #303133;
}

.quick-config-header i {
  font-size: 18px;
  color: #409eff;
  animation: rotate-gear 3s linear infinite;
}

@keyframes rotate-gear {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.quick-config-header .config-count {
  margin-left: auto;
  font-size: 12px;
  font-weight: 500;
  color: #909399;
  background: rgba(64, 158, 255, 0.1);
  padding: 2px 10px;
  border-radius: 12px;
}

.quick-config-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.config-quick-btn {
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  padding: 8px 16px;
  font-size: 13px;
  font-weight: 500;
  color: #606266;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 6px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
}

.config-quick-btn:hover {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  border-color: #409eff;
  color: #ffffff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.config-quick-btn i {
  font-size: 14px;
  transition: all 0.3s ease;
}

.config-quick-btn:hover i {
  transform: scale(1.1);
}

.config-quick-btn span {
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 12px;
  letter-spacing: 0.3px;
}

.side-card {
  border-radius: 16px;
  border: none;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  margin-bottom: 16px;
  background: #ffffff;
  overflow: hidden;
}

.side-card:hover {
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.side-card >>> .el-card__header {
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  border-bottom: 2px solid #e9ecef;
  padding: 16px 20px;
}

.status-card {
  margin-top: 16px;
}

.file-card {
  margin-top: 16px;
}

.file-card >>> .el-card__body {
  padding: 12px;
  display: flex;
  flex-direction: column;
  height: auto;
  max-height: calc(100vh - 300px);
}

.file-card .path-container {
  margin-bottom: 12px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #303133;
}

.card-header i {
  font-size: 18px;
  color: #409eff;
}

.parent-btn {
  transition: all 0.3s ease;
}

.parent-btn:hover:not(:disabled) {
  color: #409eff;
  transform: translateX(-2px);
}

.path-container {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 14px 16px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 10px;
  margin-bottom: 12px;
  border: 2px solid #dee2e6;
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.path-container:hover {
  border-color: #409eff;
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.05), 0 0 0 3px rgba(64, 158, 255, 0.1);
}

.path-container i {
  color: #909399;
  margin-top: 2px;
  flex-shrink: 0;
}

.path {
  font-size: 12px;
  color: #606266;
  word-break: break-all;
  line-height: 1.5;
  font-family: 'Consolas', 'Monaco', monospace;
}

.info-descriptions {
  margin-top: 8px;
}

.info-descriptions >>> .el-descriptions-item__label {
  font-weight: 500;
  color: #606266;
}

.info-descriptions >>> .el-descriptions-item__content {
  color: #303133;
}

.info-descriptions i {
  margin-right: 6px;
  color: #909399;
}

.path-text,
.uuid-text {
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 12px;
  word-break: break-all;
}

.loading-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  color: #909399;
  gap: 8px;
}

.loading-placeholder i {
  font-size: 20px;
  animation: rotating 2s linear infinite;
}

@keyframes rotating {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.file-list-scrollbar {
  height: 500px;
  max-height: calc(100vh - 400px);
  flex: 1;
  min-height: 300px;
}

.file-list-scrollbar >>> .el-scrollbar {
  height: 100%;
}

.file-list-scrollbar >>> .el-scrollbar__wrap {
  overflow-x: hidden !important;
  overflow-y: auto !important;
  max-height: 100%;
}

.file-list-scrollbar >>> .el-scrollbar__view {
  min-height: 100%;
}

.file-list-scrollbar >>> .el-scrollbar__bar {
  opacity: 1;
}

.file-list-scrollbar >>> .el-scrollbar__bar.is-vertical {
  right: 0;
  width: 6px;
}

.file-list-scrollbar >>> .el-scrollbar__bar.is-vertical .el-scrollbar__thumb {
  background-color: rgba(144, 147, 153, 0.3);
  border-radius: 3px;
}

.file-list-content {
  min-height: 100%;
  padding: 0;
}

.file-row {
  display: flex;
  align-items: center;
  padding: 12px 14px;
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  margin-bottom: 4px;
  min-height: 44px;
  position: relative;
  width: 100%;
  border: 1px solid transparent;
}

.file-row:hover {
  background: linear-gradient(135deg, #f0f7ff 0%, #e6f4ff 100%);
  transform: translateX(6px);
  border-color: #d0e8ff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

.file-icon {
  font-size: 18px;
  margin-right: 10px;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.folder-icon {
  color: #f39c12;
}

.file-icon-doc {
  color: #3498db;
}

.file-row:hover .file-icon {
  transform: scale(1.1);
}

.file-row .name {
  flex: 1;
  min-width: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: #303133;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-all;
}

.file-actions {
  display: flex;
  gap: 8px;
  margin-left: 8px;
  opacity: 0;
  transition: opacity 0.2s ease;
  flex-shrink: 0;
}

.file-row:hover .file-actions {
  opacity: 1;
}

.file-actions i {
  font-size: 16px;
  color: #409eff;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: all 0.2s ease;
}

.file-actions i:hover {
  background: #ecf5ff;
  color: #66b1ff;
  transform: scale(1.1);
}

.file-actions i.el-icon-edit {
  color: #67c23a;
}

.file-actions i.el-icon-edit:hover {
  background: #f0f9ff;
  color: #85ce61;
}

.empty-files {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #909399;
  gap: 12px;
}

.empty-files i {
  font-size: 48px;
  opacity: 0.5;
}

.empty-files span {
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .header {
    flex-direction: column;
    align-items: flex-start;
  }

  .actions {
    width: 100%;
    justify-content: flex-start;
  }
}

@media (max-width: 768px) {
  .app-container {
    padding: 12px;
  }

  .terminal {
    height: 400px;
    font-size: 12px;
  }
}

/* 预览对话框样式 */
.preview-dialog >>> .el-dialog__body {
  padding: 20px;
  max-height: calc(80vh - 120px);
  overflow: hidden;
}

.preview-content-wrapper {
  height: 60vh;
  max-height: calc(80vh - 120px);
  display: flex;
  flex-direction: column;
}

.text-preview {
  background: #1e1e1e;
  border-radius: 8px;
  padding: 0;
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.preview-scrollbar {
  height: 100%;
  flex: 1;
}

.preview-scrollbar >>> .el-scrollbar__wrap {
  overflow-x: auto;
  overflow-y: auto;
}

.preview-text {
  color: #d4d4d4;
  font-family: 'JetBrains Mono', 'Consolas', 'Monaco', monospace;
  font-size: 14px;
  line-height: 1.6;
  margin: 0;
  padding: 16px;
  white-space: pre-wrap;
  word-break: break-word;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

.image-preview-container {
  height: 100%;
  background: #f5f7fa;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.image-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
  min-height: 100%;
}

.preview-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.unsupported-preview {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  color: #909399;
}

.unsupported-preview i {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.5;
}

/* 配置编辑对话框样式 */
.config-edit-dialog >>> .el-dialog {
  border-radius: 16px;
  overflow: hidden;
}

.config-edit-dialog >>> .el-dialog__header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px 24px;
  border-bottom: none;
}

.config-edit-dialog >>> .el-dialog__title {
  color: #ffffff;
  font-weight: 600;
  font-size: 18px;
}

.config-edit-dialog >>> .el-dialog__headerbtn .el-dialog__close {
  color: #ffffff;
  font-size: 20px;
}

.config-edit-dialog >>> .el-dialog__headerbtn:hover .el-dialog__close {
  color: #f0f0f0;
}

.config-edit-dialog >>> .el-dialog__body {
  padding: 20px;
  background: #f8f9fa;
}

.config-edit-dialog >>> .el-dialog__footer {
  padding: 16px 24px;
  background: #ffffff;
  border-top: 2px solid #e9ecef;
}

/* 编辑器面板卡片 */
.editor-panel-card {
  height: 100%;
  border-radius: 12px;
  overflow: hidden;
}

.editor-panel-card >>> .el-card__header {
  padding: 16px 20px;
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  border-bottom: 2px solid #e9ecef;
}

.editor-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.editor-panel-header .header-left {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 600;
  color: #303133;
}

.editor-panel-header .header-left i {
  font-size: 18px;
  color: #409eff;
}

.editor-wrapper {
  height: 60vh;
  min-height: 400px;
  position: relative;
}

.config-editor {
  height: 100%;
  width: 100%;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.editor-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 60vh;
  min-height: 400px;
  color: #909399;
  gap: 12px;
}

.editor-empty i {
  font-size: 32px;
  animation: rotating 2s linear infinite;
}

.editor-empty span {
  font-size: 14px;
}

/* 配置面板卡片 */
.config-panel-card {
  height: 100%;
}

.config-panel-card >>> .el-card__header {
  padding: 16px 20px;
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  border-bottom: 2px solid #e9ecef;
}

.config-panel-header {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 600;
  color: #303133;
}

.config-panel-header i {
  font-size: 18px;
  color: #409eff;
}

.config-panel-header .el-tag {
  margin-left: auto;
  background: linear-gradient(135deg, #e7f4ff 0%, #d9ecff 100%);
  border: 1px solid #b3d8ff;
  color: #409eff;
}

/* 配置面板 */
.translation-panel {
  padding: 16px;
}

/* 配置项 */
.config-item {
  background: #ffffff;
  border: 1px solid #e4e7ed;
  border-radius: 10px;
  padding: 14px 16px;
  margin-bottom: 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.04);
}

.config-item:hover {
  border-color: #409eff;
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.12);
  transform: translateY(-1px);
}

.config-item:last-child {
  margin-bottom: 0;
}

/* 配置项头部 */
.config-item-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
  padding-bottom: 10px;
  border-bottom: 1px dashed #e4e7ed;
}

.config-item-key {
  font-family: 'JetBrains Mono', 'Consolas', 'Monaco', monospace;
  font-size: 12px;
  font-weight: 600;
  color: #409eff;
  background: #f0f7ff;
  padding: 4px 10px;
  border-radius: 5px;
  border: 1px solid #d0e8ff;
  max-width: 70%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.config-item-type {
  font-size: 10px;
  padding: 3px 8px;
  background: #fff7e6;
  border: 1px solid #ffd591;
  color: #fa8c16;
  font-weight: 500;
  border-radius: 4px;
}

/* 配置项主体 */
.config-item-body {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.config-item-value {
  display: flex;
  align-items: center;
}

.config-item-value .el-switch {
  transform: scale(1.05);
}

.config-item-value .el-input {
  width: 100%;
}

.config-item-value >>> .el-input__inner {
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  padding-left: 32px;
  transition: all 0.3s ease;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 12px;
  height: 34px;
  line-height: 34px;
}

.config-item-value >>> .el-input__inner:hover {
  border-color: #c0c4cc;
}

.config-item-value >>> .el-input__inner:focus {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
}

.config-item-value >>> .el-input__prefix {
  left: 8px;
  color: #909399;
  font-size: 13px;
}

/* 配置项说明 */
.config-item-desc {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  padding: 8px 10px;
  background: #f8f9fa;
  border-radius: 6px;
  border-left: 2px solid #409eff;
}

.config-item-desc i {
  color: #409eff;
  font-size: 13px;
  margin-top: 2px;
  flex-shrink: 0;
}

.config-item-desc span {
  color: #606266;
  font-size: 12px;
  line-height: 1.5;
  flex: 1;
}
</style>


