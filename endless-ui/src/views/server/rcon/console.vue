<template>
  <div class="app-container">
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span>RCON控制台</span>
        <el-select v-model="selectedServerId" placeholder="请选择服务器" style="float: right; width: 200px;"
                   @change="handleServerChange">
          <el-option
            v-for="server in availableServers"
            :key="server.id"
            :label="server.nameTag"
            :value="server.id">
          </el-option>
        </el-select>
      </div>

      <!-- 连接状态 -->
      <div v-if="selectedServerId" class="connection-status">
        <el-tag :type="connectionStatus === 'connected' ? 'success' : 'danger'">
          {{ connectionStatus === 'connected' ? '已连接' : '未连接' }}
        </el-tag>
        <el-button
          v-rcon-permission="{ serverId: selectedServerId, permission: 'view' }"
          :loading="connecting"
          size="mini"
          type="primary"
          @click="connectServer">
          {{ connectionStatus === 'connected' ? '重新连接' : '连接服务器' }}
        </el-button>
      </div>

      <!-- 命令输入区域 -->
      <div v-if="selectedServerId && connectionStatus === 'connected'" class="command-section">
        <el-form :inline="true" @submit.native.prevent="executeCommand">
          <el-form-item>
            <el-input
              v-model="command"
              v-rcon-permission="{ serverId: selectedServerId, permission: 'command' }"
              placeholder="请输入RCON命令"
              style="width: 400px;"
              @keyup.enter.native="executeCommand">
            </el-input>
          </el-form-item>
          <el-form-item>
            <el-button
              v-rcon-permission="{ serverId: selectedServerId, permission: 'command' }"
              :loading="executing"
              type="primary"
              @click="executeCommand">
              执行命令
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 命令输出区域 -->
      <div v-if="selectedServerId" class="output-section">
        <el-card>
          <div slot="header">
            <span>命令输出</span>
            <el-button
              v-rcon-permission="{ serverId: selectedServerId, permission: 'view' }"
              style="float: right; padding: 3px 0"
              type="text"
              @click="clearOutput">
              清空
            </el-button>
          </div>
          <div ref="outputContainer" class="command-output">
            <div v-for="(output, index) in commandOutputs" :key="index" class="output-line">
              <span class="timestamp">{{ output.timestamp }}</span>
              <span class="command">{{ output.command }}</span>
              <div class="response" v-html="output.response"></div>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 历史命令 -->
      <div v-if="selectedServerId" class="history-section">
        <el-card>
          <div slot="header">
            <span>命令历史</span>
            <el-button
              v-rcon-permission="{ serverId: selectedServerId, permission: 'view' }"
              style="float: right; padding: 3px 0"
              type="text"
              @click="loadHistory">
              刷新
            </el-button>
          </div>
          <el-table
            v-rcon-permission="{ serverId: selectedServerId, permission: 'view' }"
            :data="historyCommands"
            max-height="300"
            size="mini">
            <el-table-column label="执行时间" prop="executeTime" width="160">
              <template slot-scope="scope">
                {{ parseTime(scope.row.executeTime) }}
              </template>
            </el-table-column>
            <el-table-column label="执行用户" prop="user" width="120"/>
            <el-table-column label="命令" min-width="200" prop="command"/>
            <el-table-column label="状态" prop="status" width="80">
              <template slot-scope="scope">
                <el-tag :type="scope.row.status === 'OK' ? 'success' : 'danger'">
                  {{ scope.row.status }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="运行时间" prop="runTime" width="100"/>
          </el-table>
        </el-card>
      </div>

      <!-- 服务器管理 -->
      <div v-if="selectedServerId" class="management-section">
        <el-card>
          <div slot="header">服务器管理</div>
          <el-row :gutter="20">
            <el-col :span="8">
              <el-button
                v-rcon-permission="{ serverId: selectedServerId, permission: 'manage' }"
                type="warning"
                @click="reloadServer">
                重载服务器
              </el-button>
            </el-col>
            <el-col :span="8">
              <el-button
                v-rcon-permission="{ serverId: selectedServerId, permission: 'manage' }"
                type="danger"
                @click="stopServer">
                停止服务器
              </el-button>
            </el-col>
            <el-col :span="8">
              <el-button
                v-rcon-permission="{ serverId: selectedServerId, permission: 'manage' }"
                type="success"
                @click="saveWorld">
                保存世界
              </el-button>
            </el-col>
          </el-row>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script>
import {listServerlist} from '@/api/server/serverlist'
import rconPermissionChecker from '@/utils/rconPermission'
import request from '@/utils/request'

export default {
  name: 'RconConsole',
  data() {
    return {
      selectedServerId: null,
      availableServers: [],
      connectionStatus: 'disconnected',
      connecting: false,
      executing: false,
      command: '',
      commandOutputs: [],
      historyCommands: []
    }
  },
  created() {
    this.loadAvailableServers()
  },
  methods: {
    async loadAvailableServers() {
      try {
        // 获取用户可访问的RCON服务器
        const serverIds = await rconPermissionChecker.getUserRconServerIds()

        // 获取所有服务器信息
        const response = await listServerlist({pageNum: 1, pageSize: 1000})
        const allServers = response.rows || []

        // 过滤用户有权限的服务器
        if (serverIds === null) {
          // 管理员可以访问所有服务器
          this.availableServers = allServers
        } else {
          this.availableServers = allServers.filter(server =>
            serverIds.includes(server.id)
          )
        }
      } catch (error) {
        console.error('加载可用服务器失败:', error)
        this.$message.error('加载服务器列表失败')
      }
    },

    handleServerChange() {
      this.connectionStatus = 'disconnected'
      this.commandOutputs = []
      this.historyCommands = []
      this.command = ''
    },

    async connectServer() {
      if (!this.selectedServerId) {
        this.$message.warning('请先选择服务器')
        return
      }

      this.connecting = true
      try {
        const response = await request({
          url: `/server/serverlist/rcon/connect/${this.selectedServerId}`,
          method: 'post'
        })

        if (response.code === 200) {
          this.connectionStatus = 'connected'
          this.$message.success('服务器连接成功')
          this.loadHistory()
        } else {
          this.$message.error(response.msg || '连接失败')
        }
      } catch (error) {
        console.error('连接服务器失败:', error)
        this.$message.error('连接服务器失败')
      } finally {
        this.connecting = false
      }
    },

    async executeCommand() {
      if (!this.command.trim()) {
        this.$message.warning('请输入命令')
        return
      }

      if (!this.selectedServerId) {
        this.$message.warning('请先选择服务器')
        return
      }

      // 检查命令权限
      const isAllowed = await rconPermissionChecker.isCommandAllowed(this.selectedServerId, this.command)
      if (!isAllowed) {
        this.$message.error('您没有权限执行此命令')
        return
      }

      this.executing = true
      try {
        const response = await request({
          url: `/server/serverlist/rcon/execute/${this.selectedServerId}`,
          method: 'post',
          data: {command: this.command}
        })

        if (response.code === 200) {
          this.commandOutputs.unshift({
            timestamp: this.parseTime(new Date()),
            command: this.command,
            response: response.data.response || '命令执行成功'
          })
          this.command = ''
          this.loadHistory()
        } else {
          this.$message.error(response.msg || '命令执行失败')
        }
      } catch (error) {
        console.error('执行命令失败:', error)
        this.$message.error('命令执行失败')
      } finally {
        this.executing = false
      }
    },

    async loadHistory() {
      if (!this.selectedServerId) return

      try {
        const response = await request({
          url: '/history/command/list',
          method: 'get',
          params: {
            serverId: this.selectedServerId,
            pageNum: 1,
            pageSize: 50
          }
        })

        if (response.code === 200) {
          this.historyCommands = response.rows || []
        }
      } catch (error) {
        console.error('加载历史命令失败:', error)
      }
    },

    clearOutput() {
      this.commandOutputs = []
    },

    async reloadServer() {
      await this.executeManagementCommand('reload')
    },

    async stopServer() {
      this.$confirm('确定要停止服务器吗？', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        await this.executeManagementCommand('stop')
      })
    },

    async saveWorld() {
      await this.executeManagementCommand('save-all')
    },

    async executeManagementCommand(cmd) {
      this.command = cmd
      await this.executeCommand()
    }
  }
}
</script>

<style lang="scss" scoped>
.connection-status {
  margin-bottom: 20px;
  padding: 10px;
  background-color: #f5f5f5;
  border-radius: 4px;

  .el-tag {
    margin-right: 10px;
  }
}

.command-section {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #fafafa;
  border-radius: 4px;
}

.output-section, .history-section, .management-section {
  margin-bottom: 20px;
}

.command-output {
  max-height: 400px;
  overflow-y: auto;
  background-color: #1e1e1e;
  color: #ffffff;
  padding: 10px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;

  .output-line {
    margin-bottom: 10px;

    .timestamp {
      color: #888;
      font-size: 12px;
    }

    .command {
      color: #4CAF50;
      font-weight: bold;
      margin-left: 10px;
    }

    .response {
      margin-top: 5px;
      padding-left: 20px;
      color: #fff;
      white-space: pre-wrap;
    }
  }
}

.management-section {
  .el-button {
    width: 100%;
  }
}
</style>
