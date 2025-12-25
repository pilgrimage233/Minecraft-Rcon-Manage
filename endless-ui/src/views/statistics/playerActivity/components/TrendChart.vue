<template>
  <div class="trend-chart-container">
    <div class="chart-header">
      <h3>{{ title }}</h3>
      <div class="chart-controls">
        <el-radio-group v-model="chartType" size="small" @change="handleTypeChange">
          <el-radio-button label="line">折线图</el-radio-button>
          <el-radio-button label="bar">柱状图</el-radio-button>
          <el-radio-button label="area">面积图</el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <div ref="chart" :style="{ height: height }" class="chart"></div>

    <div class="chart-legend">
      <div v-for="(item, index) in legendData" :key="index" class="legend-item">
        <span :style="{ backgroundColor: item.color }" class="legend-color"></span>
        <span class="legend-text">{{ item.name }}</span>
        <span class="legend-value">{{ item.value }}</span>
      </div>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'

export default {
  name: 'TrendChart',
  props: {
    title: {
      type: String,
      default: '趋势图'
    },
    data: {
      type: Array,
      default: () => []
    },
    height: {
      type: String,
      default: '400px'
    }
  },

  data() {
    return {
      chart: null,
      chartType: 'line',
      legendData: [
        {name: '活跃玩家', color: '#409EFF', value: 0},
        {name: '新增玩家', color: '#67C23A', value: 0},
        {name: '在线时长', color: '#E6A23C', value: 0}
      ]
    }
  },

  watch: {
    data: {
      handler() {
        this.updateChart()
      },
      deep: true
    }
  },

  mounted() {
    this.initChart()
  },

  beforeDestroy() {
    if (this.chart) {
      this.chart.dispose()
    }
  },

  methods: {
    initChart() {
      this.chart = echarts.init(this.$refs.chart)
      this.updateChart()

      // 响应式
      window.addEventListener('resize', this.handleResize)
    },

    updateChart() {
      if (!this.chart || !this.data.length) return

      const dates = this.data.map(item => this.formatDate(item.statsDate))
      const activePlayerData = this.data.map(item => item.activePlayerCount || 0)
      const newPlayerData = this.data.map(item => item.newPlayerCount || 0)
      const onlineTimeData = this.data.map(item => Math.round((item.totalOnlineMinutes || 0) / 60))

      // 更新图例数据
      this.legendData[0].value = activePlayerData[activePlayerData.length - 1] || 0
      this.legendData[1].value = newPlayerData[newPlayerData.length - 1] || 0
      this.legendData[2].value = onlineTimeData[onlineTimeData.length - 1] || 0

      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'cross'
          },
          formatter: (params) => {
            let result = `${params[0].axisValue}<br/>`
            params.forEach(param => {
              const unit = param.seriesName.includes('时长') ? '小时' : '人'
              result += `${param.marker}${param.seriesName}: ${param.value}${unit}<br/>`
            })
            return result
          }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          boundaryGap: this.chartType === 'bar',
          data: dates,
          axisLine: {
            lineStyle: {
              color: '#e0e0e0'
            }
          },
          axisLabel: {
            color: '#666'
          }
        },
        yAxis: [
          {
            type: 'value',
            name: '人数',
            position: 'left',
            axisLine: {
              lineStyle: {
                color: '#e0e0e0'
              }
            },
            axisLabel: {
              color: '#666'
            },
            splitLine: {
              lineStyle: {
                color: '#f0f0f0'
              }
            }
          },
          {
            type: 'value',
            name: '时长(小时)',
            position: 'right',
            axisLine: {
              lineStyle: {
                color: '#e0e0e0'
              }
            },
            axisLabel: {
              color: '#666'
            }
          }
        ],
        series: [
          {
            name: '活跃玩家',
            type: this.chartType,
            smooth: true,
            data: activePlayerData,
            itemStyle: {
              color: '#409EFF'
            },
            areaStyle: this.chartType === 'area' ? {
              color: {
                type: 'linear',
                x: 0,
                y: 0,
                x2: 0,
                y2: 1,
                colorStops: [{
                  offset: 0, color: 'rgba(64, 158, 255, 0.3)'
                }, {
                  offset: 1, color: 'rgba(64, 158, 255, 0.1)'
                }]
              }
            } : null
          },
          {
            name: '新增玩家',
            type: this.chartType,
            smooth: true,
            data: newPlayerData,
            itemStyle: {
              color: '#67C23A'
            },
            areaStyle: this.chartType === 'area' ? {
              color: {
                type: 'linear',
                x: 0,
                y: 0,
                x2: 0,
                y2: 1,
                colorStops: [{
                  offset: 0, color: 'rgba(103, 194, 58, 0.3)'
                }, {
                  offset: 1, color: 'rgba(103, 194, 58, 0.1)'
                }]
              }
            } : null
          },
          {
            name: '在线时长',
            type: this.chartType,
            smooth: true,
            yAxisIndex: 1,
            data: onlineTimeData,
            itemStyle: {
              color: '#E6A23C'
            },
            areaStyle: this.chartType === 'area' ? {
              color: {
                type: 'linear',
                x: 0,
                y: 0,
                x2: 0,
                y2: 1,
                colorStops: [{
                  offset: 0, color: 'rgba(230, 162, 60, 0.3)'
                }, {
                  offset: 1, color: 'rgba(230, 162, 60, 0.1)'
                }]
              }
            } : null
          }
        ]
      }

      this.chart.setOption(option, true)
    },

    handleTypeChange() {
      this.updateChart()
    },

    handleResize() {
      if (this.chart) {
        this.chart.resize()
      }
    },

    formatDate(date) {
      if (!date) return ''
      const d = new Date(date)
      return (d.getMonth() + 1) + '/' + d.getDate()
    }
  }
}
</script>

<style lang="scss" scoped>
.trend-chart-container {
  width: 100%;

  .chart-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h3 {
      margin: 0;
      color: #333;
      font-size: 18px;
    }
  }

  .chart {
    width: 100%;
  }

  .chart-legend {
    display: flex;
    justify-content: center;
    margin-top: 20px;

    .legend-item {
      display: flex;
      align-items: center;
      margin: 0 20px;

      .legend-color {
        width: 12px;
        height: 12px;
        border-radius: 2px;
        margin-right: 8px;
      }

      .legend-text {
        color: #666;
        margin-right: 8px;
      }

      .legend-value {
        font-weight: bold;
        color: #333;
      }
    }
  }
}
</style>
