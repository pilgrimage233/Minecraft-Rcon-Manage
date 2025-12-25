<template>
  <div class="app-container">
    <!-- 统计概览卡片 -->
    <el-row :gutter="20" class="panel-group">
      <el-col :lg="6" :sm="12" :xs="12">
        <data-card
          :trend="overviewData.activePlayerTrend"
          :value="overviewData.todayActiveCount"
          color="blue"
          icon="el-icon-user"
          title="活跃玩家"
          @click="handleSetLineChartData('activePlayer')"
        />
      </el-col>

      <el-col :lg="6" :sm="12" :xs="12">
        <data-card
          :trend="overviewData.newPlayerTrend"
          :value="overviewData.todayNewCount"
          color="green"
          icon="el-icon-plus"
          title="新增玩家"
          @click="handleSetLineChartData('newPlayer')"
        />
      </el-col>

      <el-col :lg="6" :sm="12" :xs="12">
        <data-card
          :trend="overviewData.onlineTimeTrend"
          :value="overviewData.todayOnlineHours"
          color="orange"
          icon="el-icon-time"
          title="在线时长"
          unit="小时"
          @click="handleSetLineChartData('onlineTime')"
        />
      </el-col>

      <el-col :lg="6" :sm="12" :xs="12">
        <data-card
          :decimals="1"
          :value="overviewData.growthRate"
          color="purple"
          icon="el-icon-trend-charts"
          title="增长率"
          unit="%"
          @click="handleSetLineChartData('growth')"
        />
      </el-col>
    </el-row>

    <!-- 趋势图表 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card class="box-card">
          <div slot="header" class="clearfix">
            <span>活跃度趋势分析</span>
            <div style="float: right;">
              <el-radio-group v-model="chartType" size="small" @change="handleChartTypeChange">
                <el-radio-button label="daily">日趋势</el-radio-button>
                <el-radio-button label="weekly">周趋势</el-radio-button>
                <el-radio-button label="monthly">月趋势</el-radio-button>
              </el-radio-group>
            </div>
          </div>

          <!-- 日期选择器 -->
          <div class="filter-container" style="margin-bottom: 20px;">
            <el-date-picker
              v-model="dateRange"
              end-placeholder="结束日期"
              format="yyyy-MM-dd"
              range-separator="至"
              start-placeholder="开始日期"
              style="margin-right: 10px;"
              type="daterange"
              value-format="yyyy-MM-dd"
              @change="handleDateRangeChange"
            />
            <el-button icon="el-icon-search" type="primary" @click="loadTrendData">查询</el-button>
            <el-button icon="el-icon-refresh" type="success" @click="refreshData">刷新</el-button>
          </div>

          <!-- 图表容器 -->
          <trend-chart
            :data="trendData"
            height="400px"
            title="玩家活跃度趋势"
          />
        </el-card>
      </el-col>
    </el-row>

    <!-- 详细统计表格 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card class="box-card">
          <div slot="header" class="clearfix">
            <span>活跃度统计详情</span>
          </div>
          <el-table v-loading="tableLoading" :data="statisticsData" style="width: 100%">
            <el-table-column label="日期" prop="statsDate" width="120">
              <template slot-scope="scope">
                {{ formatDate(scope.row.statsDate) }}
              </template>
            </el-table-column>
            <el-table-column label="活跃玩家" prop="activePlayerCount" width="100"/>
            <el-table-column label="新增玩家" prop="newPlayerCount" width="100"/>
            <el-table-column label="总在线时长" prop="totalOnlineMinutes" width="120">
              <template slot-scope="scope">
                {{ formatMinutes(scope.row.totalOnlineMinutes) }}
              </template>
            </el-table-column>
            <el-table-column label="平均在线时长" prop="avgOnlineMinutes" width="120">
              <template slot-scope="scope">
                {{ formatMinutes(scope.row.avgOnlineMinutes) }}
              </template>
            </el-table-column>
            <el-table-column label="峰值在线" prop="peakOnlineCount" width="100"/>
            <el-table-column label="操作" width="150">
              <template slot-scope="scope">
                <el-button size="mini" @click="viewDetails(scope.row)">详情</el-button>
                <el-button size="mini" type="primary" @click="generateReport(scope.row)">生成报告</el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <pagination
            v-show="total > 0"
            :limit.sync="queryParams.pageSize"
            :page.sync="queryParams.pageNum"
            :total="total"
            @pagination="getStatisticsList"
          />
        </el-card>
      </el-col>

      <!-- 玩家排行榜 -->
      <el-col :span="8">
        <el-card class="box-card">
          <div slot="header" class="clearfix">
            <span>活跃度排行榜</span>
            <el-button style="float: right; padding: 3px 0" type="text" @click="refreshRanking">刷新</el-button>
          </div>
          <div class="ranking-list">
            <div v-for="(player, index) in rankingData" :key="index" class="ranking-item">
              <div :class="getRankingClass(index)" class="ranking-number">{{ index + 1 }}</div>
              <div class="ranking-info">
                <div class="player-name">{{ player.playerName }}</div>
                <div class="player-time">{{ formatMinutes(player.totalOnlineMinutes) }}</div>
              </div>
              <div class="ranking-score">{{ player.totalLoginCount }}次登录</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import TrendChart from './components/TrendChart.vue'
import DataCard from './components/DataCard.vue'
import {getActivityOverview, getActivityStatsList, getActivityTrend, getPlayerRanking} from '@/api/statistics/activity'

export default {
  name: 'PlayerActivity',
  components: {
    TrendChart,
    DataCard
  },
  data() {
    return {
      // 概览数据
      overviewData: {
        todayActiveCount: 0,
        todayNewCount: 0,
        todayOnlineHours: 0,
        growthRate: 0,
        activePlayerTrend: 0,
        newPlayerTrend: 0,
        onlineTimeTrend: 0
      },

      // 图表相关
      chartType: 'daily',
      dateRange: [],
      trendData: [],

      // 表格数据
      statisticsData: [],
      tableLoading: false,
      total: 0,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        statsType: 'daily'
      },

      // 排行榜数据
      rankingData: []
    }
  },

  mounted() {
    this.initDateRange()
    this.loadOverviewData()
    this.loadTrendData()
    this.getStatisticsList()
    this.loadRankingData()
  },

  methods: {


    // 初始化日期范围
    initDateRange() {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 30) // 默认30天
      this.dateRange = [this.formatDate(start), this.formatDate(end)]
    },

    // 加载概览数据
    async loadOverviewData() {
      try {
        const response = await getActivityOverview()
        if (response.code === 200) {
          const data = response.data
          this.overviewData = {
            todayActiveCount: data.today?.activePlayerCount || 0,
            todayNewCount: data.today?.newPlayerCount || 0,
            todayOnlineHours: Math.round((data.today?.totalOnlineMinutes || 0) / 60),
            growthRate: data.playerGrowthRate || 0,
            activePlayerTrend: data.playerGrowthRate || 0,
            newPlayerTrend: data.newPlayerGrowthRate || 0,
            onlineTimeTrend: data.onlineTimeGrowthRate || 0
          }
        }
      } catch (error) {
        console.error('加载概览数据失败:', error)
      }
    },

    // 加载趋势数据
    async loadTrendData() {
      if (!this.dateRange || this.dateRange.length !== 2) return

      try {
        const response = await getActivityTrend({
          startDate: this.dateRange[0],
          endDate: this.dateRange[1],
          statsType: this.chartType
        })

        if (response.code === 200) {
          this.trendData = response.data
        }
      } catch (error) {
        console.error('加载趋势数据失败:', error)
        this.$message.error('加载趋势数据失败')
      }
    },


    // 加载排行榜数据
    async loadRankingData() {
      if (!this.dateRange || this.dateRange.length !== 2) return

      try {
        const response = await getPlayerRanking({
          startDate: this.dateRange[0],
          endDate: this.dateRange[1],
          limit: 10
        })

        if (response.code === 200) {
          this.rankingData = response.data
        }
      } catch (error) {
        console.error('加载排行榜数据失败:', error)
      }
    },

    // 获取统计列表
    async getStatisticsList() {
      this.tableLoading = true
      try {
        const response = await getActivityStatsList({
          ...this.queryParams,
          statsType: this.chartType
        })

        if (response.code === 200) {
          this.statisticsData = response.rows
          this.total = response.total
        }
      } catch (error) {
        console.error('获取统计列表失败:', error)
      } finally {
        this.tableLoading = false
      }
    },

    // 处理图表类型变化
    handleChartTypeChange() {
      this.queryParams.statsType = this.chartType
      this.loadTrendData()
      this.getStatisticsList()
    },

    // 处理日期范围变化
    handleDateRangeChange() {
      this.loadTrendData()
      this.loadRankingData()
    },

    // 处理卡片点击
    handleSetLineChartData(type) {
      // 可以根据类型切换图表显示的数据
      console.log('切换图表数据类型:', type)
    },

    // 刷新数据
    refreshData() {
      this.loadOverviewData()
      this.loadTrendData()
      this.getStatisticsList()
      this.loadRankingData()
      this.$message.success('数据已刷新')
    },

    // 刷新排行榜
    refreshRanking() {
      this.loadRankingData()
    },

    // 查看详情
    viewDetails(row) {
      this.$message.info(`查看 ${this.formatDate(row.statsDate)} 的详细数据`)
      // 这里可以打开详情对话框
    },

    // 生成报告
    generateReport(row) {
      this.$message.success(`正在生成 ${this.formatDate(row.statsDate)} 的报告`)
      // 这里可以调用生成报告的API
    },

    // 格式化日期
    formatDate(date) {
      if (!date) return ''
      const d = new Date(date)
      return d.getFullYear() + '-' +
        String(d.getMonth() + 1).padStart(2, '0') + '-' +
        String(d.getDate()).padStart(2, '0')
    },

    // 格式化分钟
    formatMinutes(minutes) {
      if (!minutes) return '0分钟'
      const hours = Math.floor(minutes / 60)
      const mins = minutes % 60
      if (hours > 0) {
        return `${hours}小时${mins > 0 ? mins + '分钟' : ''}`
      }
      return `${mins}分钟`
    },

    // 获取排名样式
    getRankingClass(index) {
      if (index === 0) return 'first'
      if (index === 1) return 'second'
      if (index === 2) return 'third'
      return ''
    }
  }
}
</script>

<style lang="scss" scoped>
.app-container {
  padding: 20px;
}

.panel-group {
  margin-bottom: 20px;
}

.chart-container {
  width: 100%;
  height: 400px;
}

.filter-container {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.ranking-list {
  .ranking-item {
    display: flex;
    align-items: center;
    padding: 12px 0;
    border-bottom: 1px solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }

    .ranking-number {
      width: 30px;
      height: 30px;
      line-height: 30px;
      text-align: center;
      border-radius: 50%;
      font-weight: bold;
      margin-right: 12px;
      background: #f5f5f5;
      color: #666;

      &.first {
        background: #ffd700;
        color: #fff;
      }

      &.second {
        background: #c0c0c0;
        color: #fff;
      }

      &.third {
        background: #cd7f32;
        color: #fff;
      }
    }

    .ranking-info {
      flex: 1;

      .player-name {
        font-weight: bold;
        color: #333;
        margin-bottom: 4px;
      }

      .player-time {
        font-size: 12px;
        color: #666;
      }
    }

    .ranking-score {
      font-size: 12px;
      color: #999;
    }
  }
}

.box-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}
</style>
