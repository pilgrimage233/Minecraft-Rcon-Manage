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
              <el-tooltip v-if="wsConnectionMode" :content="getConnectionModeTooltip()" placement="bottom">
                <el-tag
                  :type="wsConnectionMode === 'direct' ? 'success' : 'warning'"
                  class="connection-mode-tag"
                  effect="dark"
                  size="small">
                  <i :class="wsConnectionMode === 'direct' ? 'el-icon-link' : 'el-icon-share'"></i>
                  {{ wsConnectionMode === 'direct' ? '直连' : '代理' }}
                </el-tag>
              </el-tooltip>
            </div>
            <div class="terminal-controls">
              <el-tooltip content="连接模式设置" placement="bottom">
                <el-dropdown class="connection-mode-dropdown" trigger="click" @command="handleConnectionModeChange">
                  <span class="connection-mode-trigger">
                    <i class="el-icon-setting"></i>
                  </span>
                  <el-dropdown-menu slot="dropdown" class="connection-mode-menu">
                    <el-dropdown-item :class="{ 'is-active': wsPreferredMode === 'auto' }" command="auto">
                      <i class="el-icon-magic-stick"></i>
                      <span>自动选择</span>
                      <span v-if="wsPreferredMode === 'auto'" class="mode-check">✓</span>
                    </el-dropdown-item>
                    <el-dropdown-item :class="{ 'is-active': wsPreferredMode === 'direct' }" command="direct">
                      <i class="el-icon-link"></i>
                      <span>强制直连</span>
                      <span v-if="wsPreferredMode === 'direct'" class="mode-check">✓</span>
                    </el-dropdown-item>
                    <el-dropdown-item :class="{ 'is-active': wsPreferredMode === 'proxy' }" command="proxy">
                      <i class="el-icon-share"></i>
                      <span>强制代理</span>
                      <span v-if="wsPreferredMode === 'proxy'" class="mode-check">✓</span>
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </el-dropdown>
              </el-tooltip>
              <span class="terminal-dot red"></span>
              <span class="terminal-dot yellow"></span>
              <span class="terminal-dot green"></span>
            </div>
          </div>
          <div class="terminal-wrapper">
            <div ref="terminal" class="terminal" @click="focusInput">
              <pre
                v-if="!consoleText"
                class="content empty-content"
              > </pre>
              <div
                v-else
                class="content"
                v-html="formattedConsoleText"
              ></div>
            </div>
          </div>
          <div class="cmd-bar">
            <el-autocomplete
              v-model="command"
              :fetch-suggestions="queryCommandSearch"
              :trigger-on-focus="false"
              class="cmd-input"
              placeholder="输入指令并回车，例如：say hello"
              prefix-icon="el-icon-edit-outline"
              size="small"
              @keyup.enter.native="sendCommand"
              @select="handleCommandSelect"
            >
              <template slot="prepend">
                <i class="el-icon-right"></i>
              </template>
              <el-button slot="append" :loading="cmdLoading" icon="el-icon-s-promotion" type="primary"
                         @click="sendCommand">发送
              </el-button>
              <template slot-scope="{ item }">
                <div class="command-suggestion">
                  <span class="command-name">{{ item.value }}</span>
                  <span class="command-desc">{{ item.description }}</span>
                </div>
              </template>
            </el-autocomplete>
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
import {mcConfigTranslations} from "@/views/node/mcs/mcConfigTranslations"
import AnsiToHtml from 'ansi-to-html';

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
      ansiConverter: null, // ANSI转HTML转换器
      // 命令补全列表
      commandSuggestions: [
        // 基础命令
        {value: 'help', description: '显示帮助信息'},
        {value: 'stop', description: '停止服务器'},
        {value: 'save-all', description: '保存所有世界数据'},
        {value: 'save-on', description: '启用自动保存'},
        {value: 'save-off', description: '禁用自动保存'},

        // 玩家管理
        {value: 'kick <玩家> [原因]', description: '踢出玩家'},
        {value: 'ban <玩家> [原因]', description: '封禁玩家'},
        {value: 'ban-ip <IP> [原因]', description: '封禁IP地址'},
        {value: 'pardon <玩家>', description: '解除玩家封禁'},
        {value: 'pardon-ip <IP>', description: '解除IP封禁'},
        {value: 'banlist [players|ips]', description: '查看封禁列表'},
        {value: 'whitelist add <玩家>', description: '添加白名单'},
        {value: 'whitelist remove <玩家>', description: '移除白名单'},
        {value: 'whitelist list', description: '查看白名单'},
        {value: 'whitelist on', description: '启用白名单'},
        {value: 'whitelist off', description: '关闭白名单'},
        {value: 'whitelist reload', description: '重载白名单'},
        {value: 'op <玩家>', description: '给予玩家管理员权限'},
        {value: 'deop <玩家>', description: '移除玩家管理员权限'},

        // 游戏模式
        {value: 'gamemode survival [玩家]', description: '设置生存模式'},
        {value: 'gamemode creative [玩家]', description: '设置创造模式'},
        {value: 'gamemode adventure [玩家]', description: '设置冒险模式'},
        {value: 'gamemode spectator [玩家]', description: '设置旁观模式'},

        // 时间和天气
        {value: 'time set day', description: '设置为白天'},
        {value: 'time set night', description: '设置为夜晚'},
        {value: 'time set <时间>', description: '设置时间'},
        {value: 'time add <时间>', description: '增加时间'},
        {value: 'time query daytime', description: '查询游戏时间'},
        {value: 'weather clear [持续时间]', description: '设置晴天'},
        {value: 'weather rain [持续时间]', description: '设置雨天'},
        {value: 'weather thunder [持续时间]', description: '设置雷雨'},

        // 传送
        {value: 'tp <玩家> <目标玩家>', description: '传送玩家到另一玩家'},
        {value: 'tp <玩家> <x> <y> <z>', description: '传送玩家到坐标'},
        {value: 'teleport <玩家> <x> <y> <z>', description: '传送玩家到坐标'},

        // 给予物品
        {value: 'give <玩家> <物品> [数量]', description: '给予玩家物品'},
        {value: 'clear <玩家> [物品]', description: '清空玩家物品'},

        // 效果
        {value: 'effect give <玩家> <效果> [持续时间] [等级]', description: '给予玩家效果'},
        {value: 'effect clear <玩家> [效果]', description: '清除玩家效果'},

        // 经验
        {value: 'xp add <玩家> <数量> [points|levels]', description: '给予玩家经验'},
        {value: 'xp set <玩家> <数量> [points|levels]', description: '设置玩家经验'},
        {value: 'xp query <玩家> [points|levels]', description: '查询玩家经验'},

        // 难度
        {value: 'difficulty peaceful', description: '设置和平难度'},
        {value: 'difficulty easy', description: '设置简单难度'},
        {value: 'difficulty normal', description: '设置普通难度'},
        {value: 'difficulty hard', description: '设置困难难度'},

        // 世界管理
        {value: 'seed', description: '显示世界种子'},
        {value: 'setworldspawn [x] [y] [z]', description: '设置世界出生点'},
        {value: 'spawnpoint <玩家> [x] [y] [z]', description: '设置玩家出生点'},
        {value: 'gamerule <规则> [值]', description: '设置游戏规则'},
        {value: 'gamerule keepInventory true', description: '死亡不掉落'},
        {value: 'gamerule doDaylightCycle false', description: '停止时间流逝'},
        {value: 'gamerule doMobSpawning false', description: '禁止生物生成'},
        {value: 'gamerule doFireTick false', description: '禁止火焰蔓延'},
        {value: 'gamerule mobGriefing false', description: '禁止生物破坏方块'},

        // 聊天和消息
        {value: 'say <消息>', description: '向所有玩家发送消息'},
        {value: 'tell <玩家> <消息>', description: '向玩家发送私聊消息'},
        {value: 'msg <玩家> <消息>', description: '向玩家发送私聊消息'},
        {value: 'w <玩家> <消息>', description: '向玩家发送私聊消息'},
        {value: 'me <动作>', description: '发送动作消息'},
        {value: 'title <玩家> title <文本>', description: '显示标题'},
        {value: 'title <玩家> subtitle <文本>', description: '显示副标题'},
        {value: 'title <玩家> actionbar <文本>', description: '显示快捷栏文本'},

        // 服务器信息
        {value: 'list', description: '列出在线玩家'},
        {value: 'list uuids', description: '列出在线玩家及UUID'},
        {value: 'tps', description: '查看服务器TPS'},
        {value: 'perf', description: '查看性能信息'},
        {value: 'timings', description: '性能分析工具'},

        // 插件管理 (Bukkit/Spigot/Paper)
        {value: 'plugins', description: '列出所有插件'},
        {value: 'pl', description: '列出所有插件'},
        {value: 'reload', description: '重载服务器配置'},
        {value: 'reload confirm', description: '确认重载服务器'},
        {value: 'version', description: '查看服务器版本'},
        {value: 'ver', description: '查看服务器版本'},

        // 权限管理 (LuckPerms)
        {value: 'lp user <玩家> permission set <权限> true', description: 'LP: 给予玩家权限'},
        {value: 'lp user <玩家> permission unset <权限>', description: 'LP: 移除玩家权限'},
        {value: 'lp user <玩家> parent add <组>', description: 'LP: 添加玩家到组'},
        {value: 'lp user <玩家> parent remove <组>', description: 'LP: 从组移除玩家'},
        {value: 'lp group <组> permission set <权限> true', description: 'LP: 给予组权限'},
        {value: 'lp group list', description: 'LP: 列出所有组'},

        // 世界编辑 (WorldEdit)
        {value: '//wand', description: 'WE: 获取选区工具'},
        {value: '//pos1', description: 'WE: 设置第一个点'},
        {value: '//pos2', description: 'WE: 设置第二个点'},
        {value: '//set <方块>', description: 'WE: 填充选区'},
        {value: '//replace <旧方块> <新方块>', description: 'WE: 替换方块'},
        {value: '//copy', description: 'WE: 复制选区'},
        {value: '//paste', description: 'WE: 粘贴选区'},
        {value: '//undo', description: 'WE: 撤销操作'},
        {value: '//redo', description: 'WE: 重做操作'},

        // 领地管理 (Residence)
        {value: 'res create <领地名>', description: 'Res: 创建领地'},
        {value: 'res remove <领地名>', description: 'Res: 删除领地'},
        {value: 'res tp <领地名>', description: 'Res: 传送到领地'},
        {value: 'res pset <领地名> <玩家> <权限> true', description: 'Res: 设置玩家权限'},

        // 经济管理 (Vault/EssentialsX)
        {value: 'eco give <玩家> <金额>', description: '给予玩家金钱'},
        {value: 'eco take <玩家> <金额>', description: '扣除玩家金钱'},
        {value: 'eco set <玩家> <金额>', description: '设置玩家金钱'},
        {value: 'balance <玩家>', description: '查看玩家余额'},
        {value: 'bal <玩家>', description: '查看玩家余额'},
        {value: 'pay <玩家> <金额>', description: '支付给玩家'},

        // EssentialsX 常用命令
        {value: 'spawn', description: 'Ess: 传送到出生点'},
        {value: 'home [名称]', description: 'Ess: 传送到家'},
        {value: 'sethome [名称]', description: 'Ess: 设置家'},
        {value: 'delhome [名称]', description: 'Ess: 删除家'},
        {value: 'warp <传送点>', description: 'Ess: 传送到传送点'},
        {value: 'setwarp <传送点>', description: 'Ess: 设置传送点'},
        {value: 'delwarp <传送点>', description: 'Ess: 删除传送点'},
        {value: 'tpa <玩家>', description: 'Ess: 请求传送到玩家'},
        {value: 'tpahere <玩家>', description: 'Ess: 请求玩家传送到你'},
        {value: 'tpaccept', description: 'Ess: 接受传送请求'},
        {value: 'tpdeny', description: 'Ess: 拒绝传送请求'},
        {value: 'back', description: 'Ess: 返回上一个位置'},

        // 调试命令
        {value: 'debug start', description: '开始调试'},
        {value: 'debug stop', description: '停止调试'},
        {value: 'debug report', description: '生成调试报告'}
      ],
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
      // WebSocket连接模式: 'auto' | 'direct' | 'proxy'
      wsConnectionMode: null,
      wsPreferredMode: 'auto', // 用户偏好设置
      wsDirectFailed: false, // 直连是否失败过
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
    // 初始化 ANSI 转换器
    this.ansiConverter = new AnsiToHtml({
      fg: '#FFF',
      bg: '#000',
      newline: false,
      escapeXML: true,
      stream: false
    });

    // 检查必要参数
    if (!this.serverId) {
      this.$message.error('缺少必要的参数：serverId');
      this.$router.push('/node/mcs/index');
      return;
    }

    // 从localStorage读取用户偏好的连接模式
    const savedMode = localStorage.getItem('wsConnectionMode');
    if (savedMode && ['auto', 'direct', 'proxy'].includes(savedMode)) {
      this.wsPreferredMode = savedMode;
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
    // 格式化控制台文本（将 ANSI 代码转换为 HTML）
    formattedConsoleText() {
      if (!this.consoleText || !this.ansiConverter) return ''
      try {
        // 将文本按行分割，逐行转换
        const lines = this.consoleText.split('\n')
        const htmlLines = lines.map(line => this.ansiConverter.toHtml(line))
        return htmlLines.join('<br>')
      } catch (error) {
        console.error('ANSI转换失败:', error)
        // 如果转换失败，返回纯文本（转义HTML）
        return this.consoleText.replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/\n/g, '<br>')
      }
    },
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
    async fetchConsoleWsInfo() {
      if (!this.instanceInfo) return;

      // 根据用户偏好和历史失败情况决定连接模式
      if (this.wsPreferredMode === 'proxy' || (this.wsPreferredMode === 'auto' && this.wsDirectFailed)) {
        // 使用代理模式
        this.setupProxyMode();
      } else if (this.wsPreferredMode === 'direct') {
        // 强制直连模式
        await this.setupDirectMode();
      } else {
        // 自动模式：优先尝试直连
        try {
          await this.setupDirectMode();
        } catch (error) {
          console.warn('直连模式失败，切换到代理模式:', error);
          this.wsDirectFailed = true;
          this.setupProxyMode();
        }
      }
    },

    // 设置直连模式
    async setupDirectMode() {
      try {
        const response = await getNodeInstanceConsole({
          id: this.instanceInfo.nodeId,
          serverId: this.serverId
        });

        if (response && response.data) {
          this.wsInfo = {
            wsUrl: response.data.wsUrl || '',
            console: response.data.console || '',
            subscribe: response.data.subscribe || '',
            token: response.data.token || ''
          };
          this.wsConnectionMode = 'direct';
          console.log('使用直连模式');
        } else {
          throw new Error('获取节点WebSocket信息失败');
        }
      } catch (error) {
        console.error('设置直连模式失败:', error);
        throw error;
      }
    },

    // 设置代理模式
    setupProxyMode() {
      // 根据环境构建WebSocket URL
      // 开发环境：使用devServer代理，路径为 /dev-api/ws
      // 生产环境：使用nginx代理，路径为 /prod-api/ws
      const baseApi = process.env.VUE_APP_BASE_API || '/prod-api';
      const wsUrl = `${baseApi}/ws`;

      this.wsInfo = {
        wsUrl: wsUrl,  // 主控端WebSocket地址（通过代理）
        console: '/topic/node-console/',  // 订阅主控端转发的topic
        subscribe: '/app/node/console/subscribe',  // 订阅指令路径
        token: this.$store.getters.token || ''  // 使用当前用户token
      };
      this.wsConnectionMode = 'proxy';
      console.log('使用代理模式, WebSocket URL:', wsUrl);
    },
    connectWs() {
      if (!this.wsInfo.wsUrl || !this.wsInfo.console || !this.wsInfo.subscribe) {
        console.warn('WebSocket连接信息不完整')
        return
      }

      const SockJS = require('sockjs-client/dist/sockjs.min.js')
      const Stomp = require('stompjs')

      try {
        const sock = new SockJS(this.wsInfo.wsUrl)
        this.stompClient = Stomp.over(sock)
        this.stompClient.debug = null

        // 设置连接超时
        const connectTimeout = setTimeout(() => {
          if (!this.stompClient || !this.stompClient.connected) {
            console.error('WebSocket连接超时');
            this.handleConnectionFailure();
          }
        }, 10000); // 10秒超时

        const connectHeaders = this.wsConnectionMode === 'direct'
          ? {"X-Endless-Token": this.wsInfo.token}
          : {};

        this.stompClient.connect(connectHeaders, () => {
          clearTimeout(connectTimeout);
          console.log(`WebSocket连接成功 (${this.wsConnectionMode}模式)`);

          // 根据连接模式订阅不同的topic
          let topic, subscribePayload;

          if (this.wsConnectionMode === 'direct') {
            // 直连模式：订阅节点端topic
            topic = this.wsInfo.console + this.instanceInfo.nodeInstancesId;
            subscribePayload = {
              serverId: this.instanceInfo.nodeInstancesId,
              token: this.wsInfo.token
            };
          } else {
            // 代理模式：订阅主控端转发的topic
            topic = this.wsInfo.console + this.instanceInfo.nodeId + '/' + this.instanceInfo.nodeInstancesId;
            subscribePayload = {
              nodeId: this.instanceInfo.nodeId,
              serverId: this.instanceInfo.nodeInstancesId
            };
          }

          this.subscription = this.stompClient.subscribe(topic, (msg) => {
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
          this.stompClient.send(this.wsInfo.subscribe, {}, JSON.stringify(subscribePayload))

          console.log('已订阅控制台:', topic)

          // 连接成功，显示提示
          this.$message.success(`控制台已连接 (${this.wsConnectionMode === 'direct' ? '直连' : '代理'}模式)`);
        }, (error) => {
          clearTimeout(connectTimeout);
          console.error('WebSocket连接失败:', error);
          this.handleConnectionFailure();
        })
      } catch (error) {
        console.error('WebSocket连接异常:', error);
        this.handleConnectionFailure();
      }
    },

    // 处理连接失败
    handleConnectionFailure() {
      // 如果是自动模式且直连失败，尝试切换到代理模式
      if (this.wsPreferredMode === 'auto' && this.wsConnectionMode === 'direct' && !this.wsDirectFailed) {
        console.log('直连失败，自动切换到代理模式');
        this.wsDirectFailed = true;
        this.$message.warning('直连失败，正在切换到代理模式...');

        // 断开当前连接
        this.disconnectWs();

        // 重新获取连接信息并连接
        this.fetchConsoleWsInfo().then(() => {
          this.connectWs();
        });
      } else {
        // 其他情况，尝试重连
        this.$message.error('WebSocket连接失败，5秒后重试...');
        setTimeout(() => this.connectWs(), 5000);
      }
    },

    // 处理连接模式切换
    handleConnectionModeChange(mode) {
      if (mode === this.wsPreferredMode) return;

      this.wsPreferredMode = mode;
      this.wsDirectFailed = false; // 重置失败标记

      // 保存用户偏好到localStorage
      localStorage.setItem('wsConnectionMode', mode);

      const modeText = mode === 'auto' ? '自动' : mode === 'direct' ? '直连' : '代理';
      this.$message.info(`已切换到${modeText}模式，正在重新连接...`);

      // 断开当前连接
      this.disconnectWs();

      // 重新获取连接信息并连接
      this.fetchConsoleWsInfo().then(() => {
        this.connectWs();
      });
    },
    // 获取连接模式提示文本
    getConnectionModeTooltip() {
      if (this.wsConnectionMode === 'direct') {
        return '直连模式：直接连接节点端，延迟最低';
      } else {
        return '代理模式：通过主控端代理连接，兼容性最好';
      }
    },
    disconnectWs() {
      try {
        // 发送取消订阅指令（仅代理模式需要）
        if (this.stompClient && this.stompClient.connected && this.instanceInfo && this.wsConnectionMode === 'proxy') {
          this.stompClient.send('/app/node/console/unsubscribe', {}, JSON.stringify({
            nodeId: this.instanceInfo.nodeId,
            serverId: this.serverId
          }))
        }
      } catch (e) {
        console.error("发送取消订阅指令失败:", e)
      }

      try {
        if (this.subscription) {
          this.subscription.unsubscribe()
        }
      } catch (e) {
        console.error("取消订阅失败:", e)
      }

      try {
        if (this.stompClient) {
          this.stompClient.disconnect(() => {
            console.log('WebSocket已断开')
          })
        }
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
    // 命令搜索
    queryCommandSearch(queryString, cb) {
      const commands = this.commandSuggestions
      const results = queryString
        ? commands.filter(this.createCommandFilter(queryString))
        : commands
      cb(results)
    },
    // 创建命令过滤器
    createCommandFilter(queryString) {
      return (command) => {
        const query = queryString.toLowerCase()
        const value = command.value.toLowerCase()
        const desc = command.description.toLowerCase()
        // 匹配命令名称或描述
        return value.indexOf(query) === 0 || desc.indexOf(query) !== -1
      }
    },
    // 选择命令
    handleCommandSelect(item) {
      // 如果命令包含参数占位符，将光标移到第一个参数位置
      const hasPlaceholder = item.value.includes('<') || item.value.includes('[')
      if (hasPlaceholder) {
        this.$nextTick(() => {
          // 查找输入框元素
          const input = this.$el.querySelector('.cmd-input input')
          if (input) {
            // 查找第一个 < 或 [ 的位置
            const firstPlaceholder = Math.min(
              item.value.indexOf('<') !== -1 ? item.value.indexOf('<') : Infinity,
              item.value.indexOf('[') !== -1 ? item.value.indexOf('[') : Infinity
            )
            if (firstPlaceholder !== Infinity) {
              input.focus()
              input.setSelectionRange(firstPlaceholder, firstPlaceholder)
            }
          }
        })
      }
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


