<template>
  <el-card class="side-card players-card" shadow="hover">
    <div slot="header" class="card-header">
      <i class="el-icon-user"></i>
      <span>在线玩家</span>
      <div class="player-header-actions">
        <el-tooltip content="刷新玩家列表" placement="top">
          <el-button
            :loading="loading"
            icon="el-icon-refresh"
            size="mini"
            type="text"
            @click="$emit('refresh')"
          ></el-button>
        </el-tooltip>
        <el-tooltip content="自动刷新" placement="top">
          <el-button
            :type="autoRefresh ? 'primary' : 'text'"
            icon="el-icon-timer"
            size="mini"
            @click="$emit('toggle-auto-refresh')"
          ></el-button>
        </el-tooltip>
        <el-tooltip content="诊断Query连接" placement="top">
          <el-button
            :loading="diagnosticLoading"
            icon="el-icon-s-tools"
            size="mini"
            type="text"
            @click="$emit('diagnostic')"
          ></el-button>
        </el-tooltip>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading && !playersData" class="loading-placeholder">
      <i class="el-icon-loading"></i>
      <span>加载玩家信息...</span>
    </div>

    <!-- 玩家数据 -->
    <div v-else-if="playersData">
      <div class="players-stats">
        <el-tag size="small" type="success">
          <i class="el-icon-user"></i>
          在线: {{ playerCountOnline }}/{{ playerCountMax }}
        </el-tag>
      </div>

      <div v-if="hasPlayers" class="players-list">
        <div
          v-for="player in playersData.players"
          :key="player.name"
          class="player-item"
        >
          <div class="player-info">
            <div class="player-avatar">
              <img
                :alt="player.name"
                :src="`https://crafatar.com/avatars/${player.name}?size=32&overlay`"
                @error="handleAvatarError"
              >
            </div>
            <div class="player-details">
              <div class="player-name">{{ player.name }}</div>
              <div v-if="player.joinTime" class="player-time">
                {{ formatPlayerTime(player.joinTime) }}
              </div>
            </div>
          </div>
          <div class="player-actions">
            <el-dropdown trigger="click" @command="(action) => $emit('player-action', player.name, action)">
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

    <!-- 无数据状态 -->
    <div v-else class="no-players">
      <i class="el-icon-warning"></i>
      <span>无法获取玩家信息</span>
      <el-button size="mini" type="text" @click="$emit('refresh')">重试</el-button>
    </div>
  </el-card>
</template>

<script>
import { DEFAULT_AVATAR_SVG } from '../constants'

export default {
  name: 'PlayerList',
  props: {
    playersData: {
      type: Object,
      default: null
    },
    loading: {
      type: Boolean,
      default: false
    },
    autoRefresh: {
      type: Boolean,
      default: false
    },
    diagnosticLoading: {
      type: Boolean,
      default: false
    }
  },
  computed: {
    playerCountOnline() {
      return this.playersData && this.playersData.playerCount ? this.playersData.playerCount.online || 0 : 0
    },
    playerCountMax() {
      return this.playersData && this.playersData.playerCount ? this.playersData.playerCount.max || 0 : 0
    },
    hasPlayers() {
      return this.playersData && this.playersData.players && this.playersData.players.length > 0
    }
  },
  methods: {
    handleAvatarError(event) {
      event.target.src = DEFAULT_AVATAR_SVG
    },
    formatPlayerTime(joinTime) {
      if (!joinTime) return ''
      const now = new Date().getTime()
      const join = new Date(joinTime).getTime()
      const diff = Math.floor((now - join) / 1000)

      if (diff < 60) return `${diff}秒前加入`
      if (diff < 3600) return `${Math.floor(diff / 60)}分钟前加入`
      if (diff < 86400) return `${Math.floor(diff / 3600)}小时前加入`
      return `${Math.floor(diff / 86400)}天前加入`
    }
  }
}
</script>

<style lang="scss" scoped>
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

.player-header-actions {
  margin-left: auto;
  display: flex;
  gap: 4px;
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

.players-stats {
  margin-bottom: 12px;
}

.players-list {
  max-height: 300px;
  overflow-y: auto;
}

.player-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  border-radius: 8px;
  transition: background-color 0.2s;

  &:hover {
    background-color: #f5f7fa;
  }
}

.player-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.player-avatar img {
  width: 32px;
  height: 32px;
  border-radius: 4px;
}

.player-name {
  font-weight: 500;
  font-size: 14px;
}

.player-time {
  font-size: 12px;
  color: #909399;
}

.no-players {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24px;
  color: #909399;
  gap: 8px;
}
</style>
