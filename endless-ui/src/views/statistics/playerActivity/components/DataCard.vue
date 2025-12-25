<template>
  <div :class="cardClass" class="data-card" @click="handleClick">
    <div class="card-icon">
      <i :class="iconClass"></i>
    </div>
    <div class="card-content">
      <div class="card-title">{{ title }}</div>
      <div class="card-value">
        <count-to
          :decimals="decimals"
          :end-val="value"
          :start-val="0"
          class="number"
        />
        <span v-if="unit" class="unit">{{ unit }}</span>
      </div>
      <div v-if="trend !== null" class="card-trend">
        <i :class="trendIcon"></i>
        <span class="trend-text">{{ trendText }}</span>
      </div>
    </div>
  </div>
</template>

<script>
import CountTo from 'vue-count-to'

export default {
  name: 'DataCard',
  components: {
    CountTo
  },
  props: {
    title: {
      type: String,
      required: true
    },
    value: {
      type: Number,
      default: 0
    },
    unit: {
      type: String,
      default: ''
    },
    icon: {
      type: String,
      default: 'el-icon-data-line'
    },
    color: {
      type: String,
      default: 'blue'
    },
    trend: {
      type: Number,
      default: null
    },
    decimals: {
      type: Number,
      default: 0
    },
    clickable: {
      type: Boolean,
      default: true
    }
  },

  computed: {
    cardClass() {
      return [
        `card-${this.color}`,
        {'clickable': this.clickable}
      ]
    },

    iconClass() {
      return this.icon
    },

    trendIcon() {
      if (this.trend > 0) return 'el-icon-top trend-up'
      if (this.trend < 0) return 'el-icon-bottom trend-down'
      return 'el-icon-minus trend-stable'
    },

    trendText() {
      if (this.trend === null) return ''
      const abs = Math.abs(this.trend)
      if (this.trend > 0) return `+${abs.toFixed(1)}%`
      if (this.trend < 0) return `-${abs.toFixed(1)}%`
      return '0%'
    }
  },

  methods: {
    handleClick() {
      if (this.clickable) {
        this.$emit('click')
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.data-card {
  display: flex;
  align-items: center;
  padding: 24px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;

  &.clickable {
    cursor: pointer;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 20px 0 rgba(0, 0, 0, 0.15);
    }
  }

  .card-icon {
    width: 60px;
    height: 60px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 20px;

    i {
      font-size: 28px;
      color: #fff;
    }
  }

  .card-content {
    flex: 1;

    .card-title {
      font-size: 14px;
      color: #666;
      margin-bottom: 8px;
    }

    .card-value {
      display: flex;
      align-items: baseline;
      margin-bottom: 8px;

      .number {
        font-size: 28px;
        font-weight: bold;
        color: #333;
      }

      .unit {
        font-size: 14px;
        color: #666;
        margin-left: 4px;
      }
    }

    .card-trend {
      display: flex;
      align-items: center;
      font-size: 12px;

      i {
        margin-right: 4px;

        &.trend-up {
          color: #67C23A;
        }

        &.trend-down {
          color: #F56C6C;
        }

        &.trend-stable {
          color: #909399;
        }
      }

      .trend-text {
        color: inherit;
      }
    }
  }

  // 不同颜色主题
  &.card-blue .card-icon {
    background: linear-gradient(135deg, #409EFF, #66B1FF);
  }

  &.card-green .card-icon {
    background: linear-gradient(135deg, #67C23A, #85CE61);
  }

  &.card-orange .card-icon {
    background: linear-gradient(135deg, #E6A23C, #EEBE77);
  }

  &.card-red .card-icon {
    background: linear-gradient(135deg, #F56C6C, #F78989);
  }

  &.card-purple .card-icon {
    background: linear-gradient(135deg, #909399, #B1B3B8);
  }
}
</style>
