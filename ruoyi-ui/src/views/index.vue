<template>
  <div class="dashboard-container">
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
        <el-card>
          <div slot="header">
            <span>实时在线信息</span>
          </div>
          <div v-if="onlineInfo">
            <div class="online-info">
              <div>服务器：{{ Object.keys(onlineInfo)[0] }}</div>
              <div>在线人数：{{ onlineInfo[Object.keys(onlineInfo)[0]]['在线人数'] }}</div>
              <div>查询时间：{{ onlineInfo['查询时间'] }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 游戏时长排行榜 -->
      <el-col :span="12">
        <el-card>
          <div slot="header">
            <span>游戏时长排行榜 TOP 10</span>
          </div>
          <el-table :data="topTenPlayers" style="width: 100%">
            <el-table-column label="玩家名称" prop="userName"/>
            <el-table-column label="QQ" prop="qq"/>
            <el-table-column label="游戏时长(分钟)" prop="gameTime"/>
            <el-table-column label="最后在线时间" prop="lastOnlineTime" width="160"/>
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
      topTenPlayers: []
    }
  },
  computed: {
    statsCards() {
      return [
        {
          label: '服务器数量',
          value: this.statsData.serverCount || 0,
          type: 'primary',
          icon: 'el-icon-s-platform',
          desc: '当前运行的服务器数量'
        },
        {
          label: '白名单数量',
          value: this.statsData.whiteListCount || 0,
          type: 'success',
          icon: 'el-icon-user',
          desc: '已通过的白名单玩家'
        },
        {
          label: '申请数量',
          value: this.statsData.applyCount || 0,
          type: 'info',
          icon: 'el-icon-document',
          desc: '累计白名单申请数'
        },
        {
          label: '未通过数量',
          value: this.statsData.notPassCount || 0,
          type: 'warning',
          icon: 'el-icon-warning',
          desc: '未通过的申请数量'
        },
        {
          label: '封禁数量',
          value: this.statsData.banCount || 0,
          type: 'danger',
          icon: 'el-icon-lock',
          desc: '当前封禁玩家数量'
        },
        {
          label: '管理员数量',
          value: this.statsData.opCount || 0,
          type: 'purple',
          icon: 'el-icon-s-custom',
          desc: '当前服务器管理员'
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
      try {
        const response = await getAggregateData()
        if (response.code === 200) {
          this.statsData = response.data
          this.onlineInfo = response.data.onlinePlayer
          this.topTenPlayers = response.data.topTen
        }
      } catch (error) {
        console.error('获取统计数据失败:', error)
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.dashboard-container {
  padding: 15px;
  background-color: #f0f2f5;

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

  .online-info {
    font-size: 14px;
    line-height: 2;
  }
}
</style>
