<template>
  <div v-loading.fullscreen.lock="loading" class="dashboard-container" element-loading-text="数据加载中...">
    <el-row :gutter="15">
      <!-- 统计数据卡片 -->
      <el-col v-for="(item, index) in statsCards" :key="index" :lg="4" :md="8" :sm="12" :xs="12">
        <el-card :class="item.type" class="stats-card" shadow="hover">
          <div class="card-content">
            <div class="icon-wrapper">
              <i :class="item.icon"></i>
            </div>
            <div class="stats-content">
              <div class="stats-value">{{ item.value }}</div>
              <div class="stats-label">{{ item.label }}</div>
              <div class="stats-desc">{{ item.desc }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <!-- 在线玩家信息 -->
      <el-col :span="12">
        <el-card class="dashboard-card">
          <div slot="header" class="card-header">
            <span><i class="el-icon-user"></i> 实时在线信息</span>
            <el-button type="text" @click="getStats">
              <i class="el-icon-refresh"></i> 刷新
            </el-button>
          </div>
          <div v-if="onlineInfo" class="online-info-wrapper">
            <div class="online-info-item">
              <div class="info-label">服务器</div>
              <div class="info-value">{{ Object.keys(onlineInfo)[0] }}</div>
            </div>
            <div class="online-info-item">
              <div class="info-label">在线人数</div>
              <div class="info-value highlight">{{ onlineInfo[Object.keys(onlineInfo)[0]]['在线人数'] }}</div>
            </div>
            <div class="online-info-item">
              <div class="info-label">查询时间</div>
              <div class="info-value">{{ onlineInfo['查询时间'] }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 游戏时长排行榜 -->
      <el-col :span="12">
        <el-card class="dashboard-card">
          <div slot="header" class="card-header">
            <span><i class="el-icon-trophy"></i> 游戏时长排行榜 TOP 10</span>
          </div>
          <el-table
            :data="topTenPlayers"
            :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
            stripe
            style="width: 100%"
          >
            <el-table-column label="排名" width="70">
              <template slot-scope="scope">
                <div class="rank-cell">
                  <span :class="['rank-number', scope.$index < 3 ? 'top-' + (scope.$index + 1) : '']">
                    {{ scope.$index + 1 }}
                  </span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="玩家名称" prop="userName"/>
            <el-table-column label="QQ" prop="qq"/>
            <el-table-column label="游戏时长" prop="gameTime">
              <template slot-scope="scope">
                {{ formatGameTime(scope.row.gameTime) }}
              </template>
            </el-table-column>
            <el-table-column label="最后在线" prop="lastOnlineTime" width="160"/>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import {getAggregateData} from '@/api/dashboard'

export default {
  name: 'Index',
  data() {
    return {
      // 统计数据
      statsData: {},
      // 在线信息
      onlineInfo: null,
      // TOP 10玩家
      topTenPlayers: [],
      // 加载状态
      loading: true
    }
  },
  computed: {
    statsCards() {
      return [
        {
          label: '服务器数量',
          value: this.statsData.serverCount || 0,
          type: 'primary',
          icon: 'el-icon-s-platform'
        },
        {
          label: '白名单数量',
          value: this.statsData.whiteListCount || 0,
          type: 'success',
          icon: 'el-icon-user'
        },
        {
          label: '申请数量',
          value: this.statsData.applyCount || 0,
          type: 'info',
          icon: 'el-icon-document'
        },
        {
          label: '未通过数量',
          value: this.statsData.notPassCount || 0,
          type: 'warning',
          icon: 'el-icon-warning'
        },
        {
          label: '封禁数量',
          value: this.statsData.banCount || 0,
          type: 'danger',
          icon: 'el-icon-lock'
        },
        {
          label: '管理员数量',
          value: this.statsData.opCount || 0,
          type: 'purple',
          icon: 'el-icon-s-custom'
        }
      ]
    }
  },
  created() {
    this.getStats()
  },
  methods: {
    // 获取统计数据
    async getStats() {
      this.loading = true
      try {
        const response = await getAggregateData()
        if (response.code === 200) {
          this.statsData = response.data
          this.onlineInfo = response.data.onlinePlayer
          this.topTenPlayers = response.data.topTen
        }
      } catch (error) {
        console.error('获取统计数据失败:', error)
        this.$message.error('数据加载失败，请稍后重试')
      } finally {
        // 添加一个小延时，确保动画流畅
        setTimeout(() => {
          this.loading = false
        }, 500)
      }
    },
    // 格式化游戏时长
    formatGameTime(minutes) {
      const hours = Math.floor(minutes / 60)
      const remainingMinutes = minutes % 60
      return `${hours}小时${remainingMinutes}分钟`
    }
  }
}
</script>

<style lang="scss" scoped>
.dashboard-container {
  padding: 20px;
  background-color: #f0f2f5;
  min-height: calc(100vh - 84px);

  .stats-card {
    height: 130px;
    cursor: pointer;
    transition: all 0.3s;
    color: #fff;
    margin-bottom: 15px;

    .card-content {
      height: 100%;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: space-around;
      text-align: center;
      padding: 10px;
    }

    .icon-wrapper {
      width: 36px;
      height: 36px;
      border-radius: 50%;
      background: rgba(255, 255, 255, 0.2);
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 5px;

      i {
        font-size: 18px;
      }
    }

    .stats-content {
      width: 100%;
      display: flex;
      flex-direction: column;
      gap: 2px;
    }

    .stats-value {
      font-size: 22px;
      font-weight: bold;
      line-height: 1.2;
    }

    .stats-label {
      font-size: 14px;
      font-weight: 500;
    }

    .stats-desc {
      font-size: 12px;
      opacity: 0.8;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      padding: 0 5px;
    }

    &.primary {
      background: linear-gradient(135deg, rgba(24, 144, 255, 0.9) 0%, rgba(54, 163, 255, 0.9) 100%);
    }

    &.success {
      background: linear-gradient(135deg, rgba(82, 196, 26, 0.9) 0%, rgba(115, 209, 61, 0.9) 100%);
    }

    &.info {
      background: linear-gradient(135deg, rgba(144, 147, 153, 0.9) 0%, rgba(166, 169, 173, 0.9) 100%);
    }

    &.warning {
      background: linear-gradient(135deg, rgba(250, 173, 20, 0.9) 0%, rgba(255, 197, 61, 0.9) 100%);
    }

    &.danger {
      background: linear-gradient(135deg, rgba(255, 77, 79, 0.9) 0%, rgba(255, 120, 117, 0.9) 100%);
    }

    &.purple {
      background: linear-gradient(135deg, rgba(114, 46, 209, 0.9) 0%, rgba(146, 84, 222, 0.9) 100%);
    }

    &:hover {
      transform: translateY(-3px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }
  }

  .dashboard-card {
    margin-bottom: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);

    &:hover {
      box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.1);
      transition: all 0.3s;
    }
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    span {
      font-size: 16px;
      font-weight: 500;

      i {
        margin-right: 8px;
        color: #409EFF;
      }
    }
  }

  .online-info-wrapper {
    padding: 20px;
    display: flex;
    flex-direction: column;
    gap: 15px;
  }

  .online-info-item {
    display: flex;
    align-items: center;
    padding: 10px 15px;
    background: #f8f9fa;
    border-radius: 6px;

    .info-label {
      color: #606266;
      font-size: 14px;
      width: 80px;
    }

    .info-value {
      color: #303133;
      font-size: 14px;
      font-weight: 500;

      &.highlight {
        color: #409EFF;
        font-size: 18px;
        font-weight: bold;
      }
    }
  }

  .rank-cell {
    display: flex;
    justify-content: center;

    .rank-number {
      width: 24px;
      height: 24px;
      line-height: 24px;
      text-align: center;
      border-radius: 50%;
      font-weight: bold;
      font-size: 14px;

      &.top-1 {
        background: #ffd700;
        color: #fff;
      }

      &.top-2 {
        background: #c0c0c0;
        color: #fff;
      }

      &.top-3 {
        background: #cd7f32;
        color: #fff;
      }
    }
  }

  ::v-deep .el-card__header {
    padding: 15px 20px;
    border-bottom: 1px solid #ebeef5;
  }

  ::v-deep .el-table {
    border-radius: 8px;
    overflow: hidden;

    th {
      font-weight: 500;
    }
  }

  // 使用 ::v-deep 替代 /deep/
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
}
</style>
