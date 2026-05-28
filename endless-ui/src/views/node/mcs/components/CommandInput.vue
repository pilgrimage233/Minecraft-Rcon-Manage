<template>
  <div class="cmd-bar">
    <el-autocomplete
      v-model="command"
      :fetch-suggestions="queryCommandSearch"
      :trigger-on-focus="false"
      class="cmd-input"
      placeholder="输入指令并回车，例如：say hello"
      prefix-icon="el-icon-edit-outline"
      size="small"
      @select="handleCommandSelect"
      @keyup.enter.native="sendCommand"
    >
      <template slot="prepend">
        <i class="el-icon-right"></i>
      </template>
      <el-button
        slot="append"
        :loading="loading"
        icon="el-icon-s-promotion"
        type="primary"
        @click="sendCommand"
      >发送</el-button>
      <template slot-scope="{ item }">
        <div class="command-suggestion">
          <span class="command-name">{{ item.value }}</span>
          <span class="command-desc">{{ item.description }}</span>
        </div>
      </template>
    </el-autocomplete>
  </div>
</template>

<script>
import { MC_COMMAND_SUGGESTIONS } from '../constants'

export default {
  name: 'CommandInput',
  props: {
    terminalTheme: {
      type: String,
      default: 'github-dark'
    },
    loading: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      command: '',
      commandSuggestions: MC_COMMAND_SUGGESTIONS
    }
  },
  methods: {
    queryCommandSearch(queryString, cb) {
      const results = queryString
        ? this.commandSuggestions.filter(this.createCommandFilter(queryString))
        : this.commandSuggestions
      cb(results)
    },
    createCommandFilter(queryString) {
      return (command) => {
        const query = queryString.toLowerCase()
        return command.value.toLowerCase().startsWith(query) ||
               command.description.toLowerCase().includes(query)
      }
    },
    handleCommandSelect(item) {
      const hasPlaceholder = item.value.includes('<') || item.value.includes('[')
      if (hasPlaceholder) {
        this.$nextTick(() => {
          const input = this.$el.querySelector('.cmd-input input')
          if (input) {
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
      if (!this.command.trim()) return
      this.$emit('send', this.command)
      this.command = ''
    }
  }
}
</script>

<style lang="scss" scoped>
.cmd-bar {
  padding: 12px 16px;
  border-radius: 0 0 12px 12px;
  margin-top: -1px;
  position: relative;
  background: #ffffff;
  border-top: 1px solid #e4e7ed;
}

.cmd-input {
  width: 100%;
}

// 固定白色主题，不跟随终端主题变化
.cmd-input > > > .el-input__inner {
  background: #ffffff;
  color: #303133;
  border: 1px solid #dcdfe6;
  font-size: 14px;
  padding: 12px 16px;
}

.cmd-input > > > .el-input__inner::placeholder {
  color: #a8abb2;
  opacity: 1;
}

.cmd-input > > > .el-input__inner:focus {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
}

.cmd-input > > > .el-input-group__prepend {
  background: #f5f7fa;
  border: 1px solid #dcdfe6;
  border-right: none;
  color: #409eff;
  font-size: 16px;
  padding: 0 12px;
}

.cmd-input > > > .el-input-group__append {
  border: none;
  background: transparent;
}

.command-suggestion {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.command-name {
  font-weight: 500;
  color: #409eff;
}

.command-desc {
  color: #909399;
  font-size: 12px;
  margin-left: 16px;
}
</style>
