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
              <el-tooltip content="刷新终端连接" placement="bottom">
                <el-button
                  :loading="wsRefreshLoading"
                  class="refresh-ws-btn"
                  icon="el-icon-refresh"
                  size="mini"
                  type="text"
                  @click="refreshWebSocket"
                >
                </el-button>
              </el-tooltip>
              <el-tooltip content="终端主题" placement="bottom">
                <el-dropdown class="theme-dropdown" trigger="click" @command="handleThemeChange">
                  <span class="theme-trigger">
                    <i class="el-icon-brush"></i>
                  </span>
                  <el-dropdown-menu slot="dropdown" class="theme-menu">
                    <el-dropdown-item :class="{ 'is-active': terminalTheme === 'github-dark' }" command="github-dark">
                      <i class="el-icon-moon"></i>
                      <span>GitHub Dark</span>
                      <span v-if="terminalTheme === 'github-dark'" class="theme-check">✓</span>
                    </el-dropdown-item>
                    <el-dropdown-item :class="{ 'is-active': terminalTheme === 'dracula' }" command="dracula">
                      <i class="el-icon-star-off"></i>
                      <span>Dracula</span>
                      <span v-if="terminalTheme === 'dracula'" class="theme-check">✓</span>
                    </el-dropdown-item>
                    <el-dropdown-item :class="{ 'is-active': terminalTheme === 'monokai' }" command="monokai">
                      <i class="el-icon-sunny"></i>
                      <span>Monokai</span>
                      <span v-if="terminalTheme === 'monokai'" class="theme-check">✓</span>
                    </el-dropdown-item>
                    <el-dropdown-item :class="{ 'is-active': terminalTheme === 'solarized-dark' }"
                                      command="solarized-dark">
                      <i class="el-icon-cloudy"></i>
                      <span>Solarized Dark</span>
                      <span v-if="terminalTheme === 'solarized-dark'" class="theme-check">✓</span>
                    </el-dropdown-item>
                    <el-dropdown-item :class="{ 'is-active': terminalTheme === 'one-dark' }" command="one-dark">
                      <i class="el-icon-view"></i>
                      <span>One Dark</span>
                      <span v-if="terminalTheme === 'one-dark'" class="theme-check">✓</span>
                    </el-dropdown-item>
                    <el-dropdown-item :class="{ 'is-active': terminalTheme === 'terminal-green' }"
                                      command="terminal-green">
                      <i class="el-icon-cpu"></i>
                      <span>Terminal Green</span>
                      <span v-if="terminalTheme === 'terminal-green'" class="theme-check">✓</span>
                    </el-dropdown-item>
                    <el-dropdown-item :class="{ 'is-active': terminalTheme === 'warm-light' }" command="warm-light">
                      <i class="el-icon-sunny"></i>
                      <span>Warm-Light</span>
                      <span v-if="terminalTheme === 'warm-light'" class="theme-check">✓</span>
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </el-dropdown>
              </el-tooltip>
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
            <div ref="terminal" :class="['terminal', `terminal-theme-${terminalTheme}`]" @click="focusInput">
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
          <div :class="['cmd-bar', `cmd-bar-theme-${terminalTheme}`]">
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

        <!-- 在线玩家管理 -->
        <el-card class="side-card players-card" shadow="hover">
          <div slot="header" class="card-header">
            <i class="el-icon-user"></i>
            <span>在线玩家</span>
            <div class="player-header-actions">
              <el-tooltip content="刷新玩家列表" placement="top">
                <el-button
                  :loading="playersLoading"
                  icon="el-icon-refresh"
                  size="mini"
                  type="text"
                  @click="refreshPlayers">
                </el-button>
              </el-tooltip>
              <el-tooltip content="自动刷新" placement="top">
                <el-button
                  :type="autoRefreshPlayers ? 'primary' : 'text'"
                  icon="el-icon-timer"
                  size="mini"
                  @click="toggleAutoRefresh">
                </el-button>
              </el-tooltip>
              <el-tooltip content="诊断Query连接" placement="top">
                <el-button
                  :loading="diagnosticLoading"
                  icon="el-icon-s-tools"
                  size="mini"
                  type="text"
                  @click="runQueryDiagnostic">
                </el-button>
              </el-tooltip>
            </div>
          </div>

          <div v-if="playersLoading && !playersData" class="loading-placeholder">
            <i class="el-icon-loading"></i>
            <span>加载玩家信息...</span>
          </div>

          <div v-else-if="playersData">
            <!-- 玩家统计 -->
            <div class="players-stats">
              <el-tag size="small" type="success">
                <i class="el-icon-user"></i>
                在线: {{ playersData.playerCount.online }}/{{ playersData.playerCount.max }}
              </el-tag>
            </div>

            <!-- 玩家列表 -->
            <div v-if="playersData.players && playersData.players.length > 0" class="players-list">
              <div
                v-for="player in playersData.players"
                :key="player.name"
                class="player-item">
                <div class="player-info">
                  <div class="player-avatar">
                    <img
                      :alt="player.name"
                      :src="`https://crafatar.com/avatars/${player.name}?size=32&overlay`"
                      @error="handleAvatarError">
                  </div>
                  <div class="player-details">
                    <div class="player-name">{{ player.name }}</div>
                    <div v-if="player.joinTime" class="player-time">
                      {{ formatPlayerTime(player.joinTime) }}
                    </div>
                  </div>
                </div>
                <div class="player-actions">
                  <el-dropdown trigger="click" @command="(action) => handlePlayerAction(player.name, action)">
                    <el-button icon="el-icon-more" size="mini" type="text"></el-button>
                    <el-dropdown-menu slot="dropdown">
                      <el-dropdown-item command="kick">
                        <i class="el-icon-close"></i>踢出
                      </el-dropdown-item>
                      <el-dropdown-item command="ban">
                        <i class="el-icon-circle-close"></i>封禁
                      </el-dropdown-item>
                      <el-dropdown-item command="op">
                        <i class="el-icon-star-on"></i>设为管理员
                      </el-dropdown-item>
                      <el-dropdown-item command="deop">
                        <i class="el-icon-star-off"></i>取消管理员
                      </el-dropdown-item>
                      <el-dropdown-item command="gamemode-creative" divided>
                        <i class="el-icon-magic-stick"></i>创造模式
                      </el-dropdown-item>
                      <el-dropdown-item command="gamemode-survival">
                        <i class="el-icon-sword"></i>生存模式
                      </el-dropdown-item>
                      <el-dropdown-item command="gamemode-adventure">
                        <i class="el-icon-map-location"></i>冒险模式
                      </el-dropdown-item>
                      <el-dropdown-item command="gamemode-spectator">
                        <i class="el-icon-view"></i>观察者模式
                      </el-dropdown-item>
                      <el-dropdown-item command="whitelist-add" divided>
                        <i class="el-icon-plus"></i>加入白名单
                      </el-dropdown-item>
                      <el-dropdown-item command="whitelist-remove">
                        <i class="el-icon-minus"></i>移出白名单
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </el-dropdown>
                </div>
              </div>
            </div>

            <div v-else class="no-players">
              <i class="el-icon-user"></i>
              <span>暂无在线玩家</span>
            </div>
          </div>

          <div v-else class="no-players">
            <i class="el-icon-warning"></i>
            <span>无法获取玩家信息</span>
            <el-button size="mini" type="text" @click="refreshPlayers">重试</el-button>
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
            <div class="file-header-title">
              <i class="el-icon-folder"></i>
              <span>文件浏览</span>
            </div>
            <div class="file-header-actions">
              <el-button
                class="expand-btn"
                icon="el-icon-full-screen"
                size="mini"
                type="text"
                @click="openFileDialog"
              >
                展开
              </el-button>
              <el-button
                :disabled="!canGoParent"
                class="parent-btn"
                icon="el-icon-back"
                size="mini"
                type="text"
                @click="goParent"
              >
                上级
              </el-button>
            </div>
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
                      v-if="isEditableFile(item.name)"
                      class="el-icon-edit"
                      title="编辑文件"
                      @click.stop="handleEditConfig(item)"
                    ></i>
                    <i class="el-icon-download" title="下载文件" @click.stop="handleDownloadFile(item)"></i>
                    <i class="el-icon-delete" title="删除文件" @click.stop="handleDeleteFile(item)"></i>
                  </div>
                  <div v-else class="file-actions">
                    <i class="el-icon-delete" title="删除目录" @click.stop="handleDeleteFile(item)"></i>
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
          v-if="previewFile"
          icon="el-icon-download"
          @click="handleDownloadFile(previewFile)"
        >
          下载文件
        </el-button>
        <el-button
          v-if="previewFile && isEditableFile(previewFile.name)"
          icon="el-icon-edit"
          type="primary"
          @click="openEditDialog"
        >
          编辑文件
        </el-button>
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
            <el-button
              :disabled="!canGoParent"
              icon="el-icon-back"
              size="small"
              @click="goParent"
            >
              上级目录
            </el-button>
            <el-button
              icon="el-icon-refresh"
              size="small"
              @click="refreshFiles"
            >
              刷新
            </el-button>
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
                <el-tag v-else-if="isMcConfigFile(item.name)" class="config-tag" size="mini" type="success">配置
                </el-tag>
              </div>
              <div v-if="!item.isDir" class="file-actions">
                <el-button
                  icon="el-icon-view"
                  size="mini"
                  type="text"
                  @click.stop="handlePreview(item)"
                >
                  预览
                </el-button>
                <el-button
                  v-if="isEditableFile(item.name)"
                  icon="el-icon-edit"
                  size="mini"
                  type="text"
                  @click.stop="handleEditConfig(item)"
                >
                  编辑
                </el-button>
                <el-button
                  icon="el-icon-download"
                  size="mini"
                  type="text"
                  @click.stop="handleDownloadFile(item)"
                >
                  下载
                </el-button>
                <el-button
                  icon="el-icon-delete"
                  size="mini"
                  type="text"
                  @click.stop="handleDeleteFile(item)"
                >
                  删除
                </el-button>
              </div>
              <div v-else class="file-actions">
                <el-button
                  icon="el-icon-delete"
                  size="mini"
                  type="text"
                  @click.stop="handleDeleteFile(item)"
                >
                  删除
                </el-button>
              </div>
            </div>
          </div>
        </el-scrollbar>
      </div>
    </el-dialog>

    <!-- 文件编辑对话框 -->
    <el-dialog
      :close-on-click-modal="false"
      :title="editFile ? `编辑文件 - ${editFile.name}` : '编辑文件'"
      :visible.sync="editDialogVisible"
      class="config-edit-dialog"
      width="90%"
    >
      <el-row :gutter="20">
        <!-- 左侧：文件内容 -->
        <el-col :span="(showTranslation && isMcConfigFile(editFile ? editFile.name : '')) ? 12 : 24">
          <el-card class="editor-panel-card" shadow="never">
            <div slot="header" class="editor-panel-header">
              <div class="header-left">
                <i class="el-icon-document"></i>
                <span>{{ isMcConfigFile(editFile ? editFile.name : '') ? '原始配置' : '文件内容' }}</span>
              </div>
              <el-button
                v-if="isMcConfigFile(editFile ? editFile.name : '')"
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
        <el-col v-if="showTranslation && isMcConfigFile(editFile ? editFile.name : '')" :span="12">
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
                    <div class="config-item-tags">
                      <el-tag v-if="item.type" class="config-item-type" size="mini">{{ item.type }}</el-tag>
                    </div>
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
import {getMcs, getServerPlayers, playerAction, queryDiagnostic} from '@/api/node/mcs'
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
      // 玩家管理相关
      playersData: null, // 玩家数据
      playersLoading: false, // 玩家加载状态
      autoRefreshPlayers: false, // 自动刷新玩家
      playersTimer: null, // 玩家刷新定时器
      diagnosticLoading: false, // 诊断加载状态
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
      // 终端主题
      terminalTheme: 'github-dark', // 默认主题
      // WebSocket刷新状态
      wsRefreshLoading: false,
      // 文件预览相关
      previewDialogVisible: false,
      previewFile: null,
      previewContent: '',
      previewUrl: '',
      previewLoading: false,
      // 文件浏览独立对话框
      fileDialogVisible: false,
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

    // 从localStorage读取用户偏好的终端主题
    const savedTheme = localStorage.getItem('terminalTheme');
    if (savedTheme && ['github-dark', 'dracula', 'monokai', 'solarized-dark', 'one-dark', 'terminal-green', 'warm-light'].includes(savedTheme)) {
      this.terminalTheme = savedTheme;
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
        const htmlLines = lines.map(line => this.formatLogLine(line))
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

      // 只处理文件中实际存在的配置项
      lines.forEach((line, index) => {
        const trimmed = line.trim()
        if (!trimmed || trimmed.startsWith('#')) return

        // 修改正则表达式，允许空值
        const match = trimmed.match(/^([^=:#]+)[=:](.*)$/)
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
            boolValue: boolValue,
            existsInFile: true
          })
        }
      })

      // 按照在文件中的顺序排序（保持原有顺序）
      return result.sort((a, b) => a.lineIndex - b.lineIndex)
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
    // 清理玩家刷新定时器
    if (this.playersTimer) {
      clearInterval(this.playersTimer)
      this.playersTimer = null
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
          // 初始化玩家数据
          this.refreshPlayers();
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

    // 处理主题切换
    handleThemeChange(theme) {
      if (theme === this.terminalTheme) return;

      this.terminalTheme = theme;

      // 保存用户偏好到localStorage
      localStorage.setItem('terminalTheme', theme);

      const themeNames = {
        'github-dark': 'GitHub Dark',
        'dracula': 'Dracula',
        'monokai': 'Monokai',
        'solarized-dark': 'Solarized Dark',
        'one-dark': 'One Dark',
        'terminal-green': 'Terminal Green',
        'warm-light': 'Warm-Light'
      };

      this.$message.success(`已切换到 ${themeNames[theme]} 主题`);
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
      const textExtensions = [
        '.txt', '.json', '.xml', '.html', '.css', '.js', '.md', '.log',
        '.yml', '.yaml', '.properties', '.conf', '.ini', '.toml', '.cfg',
        '.sh', '.bat', '.cmd', '.ps1', '.py', '.java', '.c', '.cpp', '.h',
        '.cs', '.php', '.sql', '.vue', '.ts', '.tsx', '.jsx', '.go', '.rs',
        '.rb', '.pl', '.lua', '.r', '.scala', '.kt', '.swift', '.dart',
        '.dockerfile', '.gitignore', '.gitattributes', '.editorconfig',
        '.env', '.example', '.sample', '.template', '.backup', '.bak',
        '.config', '.settings', '.prefs', '.options', '.rc', '.profile'
      ]
      return textExtensions.some(ext => filename.toLowerCase().endsWith(ext)) ||
        // 检查无扩展名的常见配置文件
        ['dockerfile', 'makefile', 'readme', 'license', 'changelog', 'authors', 'contributors'].includes(filename.toLowerCase())
    },
    // 判断是否为图片文件
    isImageFile(filename) {
      if (!filename) return false
      const imageExtensions = ['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp', '.svg', '.ico']
      return imageExtensions.some(ext => filename.toLowerCase().endsWith(ext))
    },
    // 判断文件是否可编辑
    isEditableFile(filename) {
      if (!filename) return false
      // MC配置文件肯定可编辑
      if (this.isMcConfigFile(filename)) return true
      // 文本文件可编辑
      if (this.isTextFile(filename)) return true
      // 其他特殊情况
      return false
    },
    // 根据文件名获取编辑器语言
    getEditorLanguage(filename) {
      if (!filename) return 'plaintext'

      const ext = filename.toLowerCase()
      const languageMap = {
        // Web 相关
        '.html': 'html',
        '.htm': 'html',
        '.css': 'css',
        '.js': 'javascript',
        '.jsx': 'javascript',
        '.ts': 'typescript',
        '.tsx': 'typescript',
        '.vue': 'html',

        // 配置文件
        '.json': 'json',
        '.xml': 'xml',
        '.yml': 'yaml',
        '.yaml': 'yaml',
        '.toml': 'toml',
        '.ini': 'ini',
        '.conf': 'ini',
        '.cfg': 'ini',
        '.properties': 'properties',

        // 脚本
        '.sh': 'shell',
        '.bash': 'shell',
        '.bat': 'bat',
        '.cmd': 'bat',
        '.ps1': 'powershell',

        // 标记语言
        '.md': 'markdown',
        '.markdown': 'markdown',

        // 其他
        '.log': 'plaintext',
        '.txt': 'plaintext'
      }

      // 查找匹配的扩展名
      for (const [extension, language] of Object.entries(languageMap)) {
        if (ext.endsWith(extension)) {
          return language
        }
      }

      // 特殊文件名处理
      const specialFiles = {
        'dockerfile': 'dockerfile',
        'makefile': 'makefile',
        'readme': 'markdown',
        'license': 'plaintext',
        'changelog': 'markdown'
      }

      const baseName = filename.toLowerCase()
      if (specialFiles[baseName]) {
        return specialFiles[baseName]
      }

      // 默认返回纯文本
      return 'plaintext'
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
          this.editorOptions.language = this.getEditorLanguage(file.name)
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
            // 修改正则表达式，允许空值
            const match = trimmed.match(/^([^=:#]+)[=:](.*)$/)
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
    },
    // 打开文件浏览独立对话框
    openFileDialog() {
      this.fileDialogVisible = true
    },
    // 刷新文件列表
    refreshFiles() {
      if (this.currentPath) {
        this.loadFiles(this.currentPath)
      } else if (this.instanceInfo && this.instanceInfo.serverPath) {
        this.loadFiles(this.instanceInfo.serverPath)
      }
    },
    // 下载文件
    async handleDownloadFile(file) {
      if (!file || !this.instanceInfo || file.isDir) return

      try {
        this.$message.info('正在准备下载...')

        // 调用下载API
        const response = await downloadFile(this.instanceInfo.nodeId, file.fullPath)

        // 创建下载链接
        const blob = new Blob([response])
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = file.name

        // 触发下载
        document.body.appendChild(link)
        link.click()

        // 清理
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)

        this.$message.success('文件下载成功')
      } catch (error) {
        console.error('下载文件失败:', error)
        this.$message.error('下载文件失败: ' + (error.message || '未知错误'))
      }
    },
    // 删除文件
    async handleDeleteFile(file) {
      if (!file || !this.instanceInfo) return

      const fileType = file.isDir ? '目录' : '文件'
      const confirmMessage = `确定要删除${fileType} "${file.name}" 吗？${file.isDir ? '此操作将删除目录及其所有内容，' : ''}此操作不可恢复！`

      try {
        await this.$confirm(confirmMessage, '删除确认', {
          confirmButtonText: '确定删除',
          cancelButtonText: '取消',
          type: 'warning',
          dangerouslyUseHTMLString: false
        })

        const response = await deleteFile({
          id: this.instanceInfo.nodeId,
          path: file.fullPath
        })

        if (response.code === 200) {
          this.$message.success(`${fileType}删除成功`)
          // 刷新文件列表
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

    // ========== 玩家管理相关方法 ==========

    // 刷新玩家列表
    async refreshPlayers() {
      if (!this.instanceInfo) return;

      this.playersLoading = true;
      try {
        const response = await getServerPlayers(this.instanceInfo.nodeId, this.serverId);

        if (response.code === 200 && response.data && response.data.success) {
          this.playersData = response.data;
        } else {
          this.playersData = null;
          console.warn('获取玩家信息失败:', response.data ? response.data.error : '未知错误');
        }
      } catch (error) {
        console.error('获取玩家信息失败:', error);
        this.playersData = null;
      } finally {
        this.playersLoading = false;
      }
    },

    // 切换自动刷新玩家列表
    toggleAutoRefresh() {
      this.autoRefreshPlayers = !this.autoRefreshPlayers;

      if (this.autoRefreshPlayers) {
        // 立即刷新一次
        this.refreshPlayers();
        // 启动定时器，每30秒刷新一次
        this.playersTimer = setInterval(() => {
          this.refreshPlayers();
        }, 30000);
        this.$message.success('已启用玩家列表自动刷新');
      } else {
        // 停止定时器
        if (this.playersTimer) {
          clearInterval(this.playersTimer);
          this.playersTimer = null;
        }
        this.$message.info('已关闭玩家列表自动刷新');
      }
    },

    // 对玩家执行操作
    async handlePlayerAction(playerName, action) {
      if (!this.instanceInfo || !playerName) return;

      // 构建操作描述
      const actionDescriptions = {
        'kick': '踢出',
        'ban': '封禁',
        'ban-ip': 'IP封禁',
        'pardon': '解封',
        'pardon-ip': 'IP解封',
        'op': '设为管理员',
        'deop': '取消管理员',
        'whitelist-add': '加入白名单',
        'whitelist-remove': '移出白名单',
        'gamemode-creative': '设为创造模式',
        'gamemode-survival': '设为生存模式',
        'gamemode-adventure': '设为冒险模式',
        'gamemode-spectator': '设为观察者模式',
        'tp-to-spawn': '传送到出生点'
      };

      const actionDesc = actionDescriptions[action] || action;

      // 对于需要原因的操作，弹出输入框
      let reason = '';
      if (['kick', 'ban', 'ban-ip'].includes(action)) {
        try {
          const {value} = await this.$prompt(`请输入${actionDesc}原因（可选）:`, `${actionDesc}玩家`, {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            inputPlaceholder: '输入原因...'
          });
          reason = value || '';
        } catch {
          return; // 用户取消
        }
      }

      // 确认操作
      try {
        await this.$confirm(`确定要对玩家 ${playerName} 执行"${actionDesc}"操作吗？`, '确认操作', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        });
      } catch {
        return; // 用户取消
      }

      try {
        const response = await playerAction(
          this.instanceInfo.nodeId,
          this.serverId,
          playerName,
          {
            action: action,
            reason: reason
          }
        );

        if (response.code === 200 && response.data && response.data.success) {
          this.$message.success(`成功对玩家 ${playerName} 执行${actionDesc}操作`);
          // 刷新玩家列表
          setTimeout(() => {
            this.refreshPlayers();
          }, 1000);
        } else {
          this.$message.error(`操作失败: ${response.data ? response.data.error : '未知错误'}`);
        }
      } catch (error) {
        console.error('玩家操作失败:', error);
        this.$message.error(`操作失败: ${error.message || '网络错误'}`);
      }
    },

    // 格式化玩家在线时间
    formatPlayerTime(joinTime) {
      if (!joinTime) return '';

      const now = new Date().getTime();
      const join = new Date(joinTime).getTime();
      const diff = Math.floor((now - join) / 1000); // 秒

      if (diff < 60) {
        return `${diff}秒前加入`;
      } else if (diff < 3600) {
        return `${Math.floor(diff / 60)}分钟前加入`;
      } else if (diff < 86400) {
        return `${Math.floor(diff / 3600)}小时前加入`;
      } else {
        return `${Math.floor(diff / 86400)}天前加入`;
      }
    },

    // 处理头像加载错误
    handleAvatarError(event) {
      // 使用默认头像
      event.target.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMzIiIGhlaWdodD0iMzIiIHZpZXdCb3g9IjAgMCAzMiAzMiIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHJlY3Qgd2lkdGg9IjMyIiBoZWlnaHQ9IjMyIiBmaWxsPSIjNDA5RUZGIi8+CjxwYXRoIGQ9Ik0xNiA4QzEzLjc5IDggMTIgOS43OSAxMiAxMkMxMiAxNC4yMSAxMy43OSAxNiAxNiAxNkMxOC4yMSAxNiAyMCAxNC4yMSAyMCAxMkMyMCA5Ljc5IDE4LjIxIDggMTYgOFpNMTYgMjJDMTIuNjcgMjIgNiAyMy4zNCA2IDI2LjY3VjI4SDE2SDI2VjI2LjY3QzI2IDIzLjM0IDE5LjMzIDIyIDE6IDIyWiIgZmlsbD0id2hpdGUiLz4KPC9zdmc+';
    },

    // 运行Query诊断
    async runQueryDiagnostic() {
      if (!this.instanceInfo) return;

      this.diagnosticLoading = true;
      try {
        const response = await queryDiagnostic(this.instanceInfo.nodeId, this.serverId);

        if (response.code === 200 && response.data) {
          this.showDiagnosticResults(response.data);
        } else {
          this.$message.error('诊断失败: ' + (response.msg || '未知错误'));
        }
      } catch (error) {
        console.error('Query诊断失败:', error);
        this.$message.error('诊断失败: ' + (error.message || '网络错误'));
      } finally {
        this.diagnosticLoading = false;
      }
    },

    // 格式化单行日志（处理日志级别颜色）
    formatLogLine(line) {
      if (!line) return ''

      // 使用正则匹配日志前缀格式：[时间 级别]: 内容
      const logPattern = /^(\[[\d:]+\s+(INFO|WARN|WARNING|ERROR|SEVERE|DEBUG|TRACE)\]:\s*)(.*)/i
      const match = line.match(logPattern)

      if (match) {
        const prefix = match[1]  // [13:52:30 INFO]:
        const level = match[2].toUpperCase()  // INFO, WARN, ERROR等
        const content = match[3]  // 实际日志内容

        // 根据日志级别设置颜色
        let levelColor = '#FFF'  // 默认白色
        switch (level) {
          case 'INFO':
            levelColor = '#67c23a'  // 绿色
            break
          case 'WARN':
          case 'WARNING':
            levelColor = '#e6a23c'  // 黄色
            break
          case 'ERROR':
          case 'SEVERE':
            levelColor = '#f56c6c'  // 红色
            break
          case 'DEBUG':
            levelColor = '#909399'  // 灰色
            break
          case 'TRACE':
            levelColor = '#c0c4cc'  // 浅灰色
            break
        }

        // 构建带颜色的HTML
        const coloredPrefix = `<span style="color: ${levelColor};">${this.escapeHtml(prefix)}</span>`
        const processedContent = this.ansiConverter.toHtml(content)

        return coloredPrefix + processedContent
      } else {
        // 如果不匹配日志格式，直接使用ANSI转换
        return this.ansiConverter.toHtml(line)
      }
    },

    // HTML转义函数
    escapeHtml(text) {
      const div = document.createElement('div')
      div.textContent = text
      return div.innerHTML
    },

    // 刷新WebSocket连接
    async refreshWebSocket() {
      if (this.wsRefreshLoading) return;

      this.wsRefreshLoading = true;
      try {
        this.$message.info('正在刷新终端连接...');

        // 断开当前WebSocket连接
        this.disconnectWs();

        // 等待一小段时间确保连接完全断开
        await new Promise(resolve => setTimeout(resolve, 500));

        // 重新获取WebSocket信息并连接
        await this.fetchConsoleWsInfo();
        this.connectWs();

        this.$message.success('终端连接已刷新');
      } catch (error) {
        console.error('刷新WebSocket连接失败:', error);
        this.$message.error('刷新终端连接失败: ' + (error.message || '未知错误'));
      } finally {
        this.wsRefreshLoading = false;
      }
    },

    // 显示诊断结果
    showDiagnosticResults(diagnostic) {
      const h = this.$createElement;

      // 构建诊断结果内容
      const content = [];

      // 基本信息
      content.push(h('h4', '基本信息'));
      content.push(h('p', [
        h('strong', '服务器ID: '), diagnostic.serverId, h('br'),
        h('strong', '游戏端口: '), diagnostic.gamePort, h('br'),
        h('strong', 'Query端口: '), diagnostic.queryPort, h('br'),
        h('strong', '服务器运行: '), diagnostic.serverRunning ? '是' : '否'
      ]));

      // server.properties配置
      content.push(h('h4', 'server.properties配置'));
      const props = diagnostic.serverProperties;
      if (props.exists) {
        content.push(h('p', [
          h('strong', 'enable-query: '), props['enable-query'] || '未设置', h('br'),
          h('strong', 'query.port: '), props['query.port'] || '未设置', h('br'),
          h('strong', 'server-port: '), props['server-port'] || '未设置'
        ]));
      } else {
        content.push(h('p', {style: 'color: #f56c6c;'}, 'server.properties文件不存在'));
      }

      // 连接测试结果
      content.push(h('h4', '连接测试结果'));
      const tests = diagnostic.connectionTests;
      Object.keys(tests).forEach(host => {
        const test = tests[host];

        // 创建测试步骤显示
        const steps = [];

        // Socket创建
        if (test.socketCreated === true) {
          steps.push(h('span', {style: 'color: #67c23a;'}, '✓ Socket创建'));
        } else {
          steps.push(h('span', {style: 'color: #f56c6c;'}, '✗ Socket创建失败'));
        }

        // 握手测试
        if (test.handshakeSuccess === true) {
          steps.push(h('span', {style: 'color: #67c23a;'}, '✓ 握手成功'));
        } else if (test.handshakeSuccess === false) {
          steps.push(h('span', {style: 'color: #f56c6c;'}, '✗ 握手失败'));
        }

        // 状态获取
        if (test.statusSuccess === true) {
          steps.push(h('span', {style: 'color: #67c23a;'}, `✓ 状态获取成功 (${test.onlinePlayers}/${test.maxPlayers})`));
        } else if (test.statusSuccess === false) {
          steps.push(h('span', {style: 'color: #f56c6c;'}, '✗ 状态获取失败'));
        }

        content.push(h('div', {style: 'margin-bottom: 10px;'}, [
          h('strong', `${host}:${test.port}`),
          h('br'),
          ...steps.map(step => h('div', {style: 'margin-left: 20px;'}, step))
        ]));

        // 显示错误信息
        if (test.error) {
          content.push(h('div', {
            style: 'color: #f56c6c; margin-left: 20px; font-size: 12px;'
          }, `错误: ${test.error}`));
        }
        if (test.handshakeError) {
          content.push(h('div', {
            style: 'color: #f56c6c; margin-left: 20px; font-size: 12px;'
          }, `握手错误: ${test.handshakeError}`));
        }
        if (test.statusError) {
          content.push(h('div', {
            style: 'color: #f56c6c; margin-left: 20px; font-size: 12px;'
          }, `状态错误: ${test.statusError}`));
        }
      });

      // 修复建议
      if (diagnostic.suggestions && diagnostic.suggestions.length > 0) {
        content.push(h('h4', '修复建议'));
        const suggestions = diagnostic.suggestions.map(suggestion =>
          h('li', {style: 'margin-bottom: 5px;'}, suggestion)
        );
        content.push(h('ul', {style: 'margin: 0; padding-left: 20px;'}, suggestions));
      }

      // 显示对话框
      this.$msgbox({
        title: 'Query连接诊断结果',
        message: h('div', {style: 'max-height: 400px; overflow-y: auto;'}, content),
        showCancelButton: false,
        confirmButtonText: '关闭',
        dangerouslyUseHTMLString: false
      });
    }
  }
}
</script>


