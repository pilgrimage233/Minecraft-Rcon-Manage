<template>
  <el-card class="terminal-card" shadow="hover">
    <div slot="header" class="terminal-header">
      <div class="terminal-title">
        <i class="el-icon-monitor"></i>
        <span>控制台输出</span>
        <el-tooltip v-if="connectionMode" :content="connectionModeTooltip" placement="bottom">
          <el-tag
            :type="connectionMode === 'direct' ? 'success' : 'warning'"
            class="connection-mode-tag"
            effect="dark"
            size="small"
          >
            <i :class="connectionMode === 'direct' ? 'el-icon-link' : 'el-icon-share'"></i>
            {{ connectionMode === 'direct' ? '直连' : '代理' }}
          </el-tag>
        </el-tooltip>
      </div>
      <div class="terminal-controls">
        <el-tooltip :content="autoScrollEnabled ? '暂停自动滚动' : '恢复自动滚动'" placement="bottom">
          <el-button
            :icon="autoScrollEnabled ? 'el-icon-video-pause' : 'el-icon-video-play'"
            class="control-btn"
            size="mini"
            type="text"
            @click="$emit('toggle-auto-scroll')"
          ></el-button>
        </el-tooltip>
        <el-tooltip content="清空控制台" placement="bottom">
          <el-button
            class="control-btn"
            icon="el-icon-delete"
            size="mini"
            type="text"
            @click="$emit('clear-console')"
          ></el-button>
        </el-tooltip>
        <el-tooltip content="刷新终端连接" placement="bottom">
          <el-button
            :loading="wsRefreshLoading"
            class="control-btn"
            icon="el-icon-refresh"
            size="mini"
            type="text"
            @click="$emit('refresh-ws')"
          ></el-button>
        </el-tooltip>
        <!-- 主题选择器 -->
        <el-tooltip content="终端主题" placement="bottom">
          <el-dropdown class="theme-dropdown" trigger="click" @command="$emit('theme-change', $event)">
            <span class="theme-trigger">
              <i class="el-icon-brush"></i>
            </span>
            <el-dropdown-menu slot="dropdown" class="theme-menu">
              <el-dropdown-item
                v-for="(name, key) in themeNames"
                :key="key"
                :class="{ 'is-active': terminalTheme === key }"
                :command="key"
              >
                <i :class="themeIcons[key] || 'el-icon-view'"></i>
                <span>{{ name }}</span>
                <span v-if="terminalTheme === key" class="theme-check">✓</span>
              </el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </el-tooltip>
        <!-- 连接模式选择器 -->
        <el-tooltip content="连接模式设置" placement="bottom">
          <el-dropdown class="connection-mode-dropdown" trigger="click" @command="$emit('connection-mode-change', $event)">
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
      <div
        ref="terminal"
        :class="['terminal', `terminal-theme-${terminalTheme}`]"
        @click="$emit('focus-input')"
        @scroll.passive="$emit('scroll')"
      >
        <pre v-if="!consoleHtml" class="content empty-content"> </pre>
        <div v-else class="content" v-html="consoleHtml"></div>
      </div>
    </div>
  </el-card>
</template>

<script>
import { TERMINAL_THEME_NAMES } from '../constants'

export default {
  name: 'ConsoleOutput',
  props: {
    consoleHtml: {
      type: String,
      default: ''
    },
    autoScrollEnabled: {
      type: Boolean,
      default: true
    },
    connectionMode: {
      type: String,
      default: null
    },
    wsPreferredMode: {
      type: String,
      default: 'auto'
    },
    terminalTheme: {
      type: String,
      default: 'github-dark'
    },
    wsRefreshLoading: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      themeNames: TERMINAL_THEME_NAMES,
      themeIcons: {
        'github-dark': 'el-icon-moon',
        'dracula': 'el-icon-star-off',
        'monokai': 'el-icon-sunny',
        'solarized-dark': 'el-icon-cloudy',
        'one-dark': 'el-icon-view',
        'terminal-green': 'el-icon-cpu',
        'warm-light': 'el-icon-sunny'
      }
    }
  },
  computed: {
    connectionModeTooltip() {
      return this.connectionMode === 'direct'
        ? '直连模式：直接连接节点端，延迟最低'
        : '代理模式：通过主控端代理连接，兼容性最好'
    }
  },
  methods: {
    scrollToBottom() {
      const el = this.$refs.terminal
      if (el) {
        el.scrollTop = el.scrollHeight
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.terminal-card {
  border-radius: 12px 12px 0 0;
  height: 100%;
}

.terminal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.terminal-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.terminal-controls {
  display: flex;
  align-items: center;
  gap: 4px;
}

.control-btn {
  color: #606266;
  &:hover {
    color: #409eff;
  }
}

.connection-mode-tag {
  margin-left: 8px;
}

.theme-dropdown,
.connection-mode-dropdown {
  margin: 0 4px;
}

.theme-trigger,
.connection-mode-trigger {
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: background-color 0.3s;

  &:hover {
    background-color: rgba(0, 0, 0, 0.05);
  }
}

.terminal-wrapper {
  position: relative;
  height: 500px;
  border-radius: 8px;
  overflow: hidden;
}

.terminal {
  height: 100%;
  overflow-y: auto;
  padding: 16px;
  font-family: 'JetBrains Mono', 'Consolas', 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.6;
  scroll-behavior: smooth;

  &::-webkit-scrollbar {
    width: 8px;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(255, 255, 255, 0.2);
    border-radius: 4px;

    &:hover {
      background: rgba(255, 255, 255, 0.3);
    }
  }
}

.content {
  white-space: pre-wrap;
  word-break: break-all;
}

.empty-content {
  color: #909399;
  font-style: italic;
}

.terminal-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  display: inline-block;
  margin-left: 6px;

  &.red { background: #ff5f56; }
  &.yellow { background: #ffbd2e; }
  &.green { background: #27c93f; }
}

// 主题样式
.terminal-theme-github-dark {
  background: #0d1117;
  color: #c9d1d9;
}

.terminal-theme-dracula {
  background: #282a36;
  color: #f8f8f2;
}

.terminal-theme-monokai {
  background: #272822;
  color: #f8f8f2;
}

.terminal-theme-solarized-dark {
  background: #002b36;
  color: #839496;
}

.terminal-theme-one-dark {
  background: #282c34;
  color: #abb2bf;
}

.terminal-theme-terminal-green {
  background: #000000;
  color: #00ff00;
}

.terminal-theme-warm-light {
  background: #faf8f5;
  color: #434343;
}
</style>
