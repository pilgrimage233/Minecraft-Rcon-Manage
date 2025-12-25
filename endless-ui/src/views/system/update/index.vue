<template>
  <div class="app-container">
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span>系统更新</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="checkUpdate">
          <i class="el-icon-refresh"></i> 检查更新
        </el-button>
      </div>

      <div v-loading="loading">
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="version-info">
              <h3>版本信息</h3>
              <p><strong>当前版本:</strong> {{ updateInfo.currentVersion || '未知' }}</p>
              <p><strong>最新版本:</strong> {{ updateInfo.latestVersion || '检查中...' }}</p>
              <p><strong>更新状态:</strong>
                <el-tag :type="updateInfo.hasUpdate ? 'warning' : 'success'">
                  {{ updateInfo.hasUpdate ? '有新版本可用' : '已是最新版本' }}
                </el-tag>
              </p>
            </div>
          </el-col>

          <el-col :span="12">
            <div class="update-actions">
              <h3>操作</h3>
              <el-button
                :disabled="!updateInfo.hasUpdate || updating"
                :loading="updating"
                type="primary"
                @click="executeUpdate"
              >
                <i class="el-icon-download"></i>
                {{ updating ? '更新中...' : '一键更新' }}
              </el-button>

              <el-button
                v-if="updateInfo.downloadUrl"
                type="info"
                @click="openDownloadPage"
              >
                <i class="el-icon-link"></i>
                手动下载
              </el-button>
            </div>
          </el-col>
        </el-row>

        <el-divider></el-divider>

        <div v-if="updateInfo.releaseNotes" class="release-notes">
          <h3>更新说明</h3>
          <div class="notes-content" v-html="formatReleaseNotes(updateInfo.releaseNotes)"></div>
        </div>

        <el-alert
          v-if="updateInfo.hasUpdate"
          :closable="false"
          description="更新过程中系统会自动重启，请确保没有重要操作正在进行。建议在业务低峰期进行更新。"
          show-icon
          title="更新提醒"
          type="warning"
        >
        </el-alert>
      </div>
    </el-card>
  </div>
</template>

<script>
import {checkUpdate, executeUpdate, getUpdateStatus} from '@/api/system/update'

export default {
  name: 'SystemUpdate',
  data() {
    return {
      loading: false,
      updating: false,
      updateInfo: {
        currentVersion: '',
        latestVersion: '',
        hasUpdate: false,
        releaseNotes: '',
        downloadUrl: ''
      }
    }
  },
  created() {
    this.checkUpdate()
    this.checkUpdateStatus()
  },
  methods: {
    async checkUpdate() {
      this.loading = true
      try {
        const response = await checkUpdate()
        if (response.code === 200) {
          this.updateInfo = response.data
        } else {
          this.$message.error(response.msg || '检查更新失败')
        }
      } catch (error) {
        this.$message.error('检查更新失败: ' + error.message)
      } finally {
        this.loading = false
      }
    },

    async executeUpdate() {
      this.$confirm('确定要执行一键更新吗？更新完成后可能需要手动重启系统。', '确认更新', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        this.updating = true
        try {
          const response = await executeUpdate()
          if (response.code === 200) {
            const {environment, needManualRestart} = response.data || {}

            this.$message.success(response.msg || '更新已开始执行')

            if (needManualRestart) {
              this.showManualRestartDialog(environment)
            } else {
              this.showRestartCountdown()
            }
          } else {
            this.$message.error(response.msg || '更新执行失败')
            this.updating = false
          }
        } catch (error) {
          this.$message.error('更新执行失败: ' + error.message)
          this.updating = false
        }
      }).catch(() => {
        // 用户取消
      })
    },

    showRestartCountdown() {
      let countdown = 10
      const timer = setInterval(() => {
        if (countdown > 0) {
          this.$message({
            message: `系统将在 ${countdown} 秒后自动重启...`,
            type: 'info',
            duration: 1000
          })
          countdown--
        } else {
          clearInterval(timer)
          this.$message.info('系统正在重启，请稍后刷新页面...')
          this.updating = false
        }
      }, 1000)
    },

    showManualRestartDialog(environment) {
      const restartInstructions = this.getRestartInstructions(environment)

      this.$alert(restartInstructions, '更新完成 - 需要手动重启', {
        confirmButtonText: '我知道了',
        type: 'warning',
        dangerouslyUseHTMLString: true,
        callback: () => {
          this.updating = false
          this.$message.info('请按照提示手动重启系统以完成更新')
        }
      })
    },

    getRestartInstructions(environment) {
      const instructions = {
        docker: `
          <p><strong>检测到Docker环境，请使用以下命令重启：</strong></p>
          <ul>
            <li><code>docker restart &lt;容器名称&gt;</code></li>
            <li>或者 <code>docker-compose restart</code></li>
            <li>或者 <code>docker stop &lt;容器名称&gt; && docker start &lt;容器名称&gt;</code></li>
          </ul>
        `,
        systemd: `
          <p><strong>检测到系统服务环境，请使用以下命令重启：</strong></p>
          <ul>
            <li><code>sudo systemctl restart &lt;服务名称&gt;</code></li>
          </ul>
        `,
        windows: `
          <p><strong>Windows环境重启方法：</strong></p>
          <ul>
            <li>如果是Windows服务：<code>net stop &lt;服务名&gt; && net start &lt;服务名&gt;</code></li>
            <li>如果是直接运行：关闭当前程序，重新运行JAR文件</li>
          </ul>
        `,
        linux: `
          <p><strong>Linux环境重启方法：</strong></p>
          <ul>
            <li>1. 找到Java进程：<code>ps aux | grep java</code></li>
            <li>2. 终止进程：<code>kill &lt;进程ID&gt;</code></li>
            <li>3. 重新启动：<code>java -jar &lt;jar文件名&gt;</code></li>
          </ul>
        `,
        unknown: `
          <p><strong>通用重启方法：</strong></p>
          <ul>
            <li>1. 终止当前Java进程</li>
            <li>2. 重新运行JAR文件</li>
          </ul>
        `
      }

      return instructions[environment] || instructions.unknown
    },

    async checkUpdateStatus() {
      try {
        const response = await getUpdateStatus()
        if (response.code === 200 && response.data.restartRequired) {
          this.$notify({
            title: '系统更新完成',
            message: '检测到系统已更新完成，需要重启以生效。请查看重启说明。',
            type: 'warning',
            duration: 0,
            onClick: () => {
              this.showManualRestartDialog(response.data.environment)
            }
          })
        }
      } catch (error) {
        console.warn('检查更新状态失败:', error)
      }
    },

    openDownloadPage() {
      if (this.updateInfo.downloadUrl) {
        window.open(this.updateInfo.downloadUrl, '_blank')
      }
    },

    formatReleaseNotes(notes) {
      if (!notes) return ''
      return notes
        .replace(/\n/g, '<br>')
        .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
        .replace(/\*(.*?)\*/g, '<em>$1</em>')
    }
  }
}
</script>

<style scoped>
.version-info, .update-actions {
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 4px;
  margin-bottom: 20px;
}

.version-info h3, .update-actions h3 {
  margin-top: 0;
  color: #303133;
}

.version-info p {
  margin: 10px 0;
  color: #606266;
}

.release-notes {
  margin-top: 20px;
}

.release-notes h3 {
  color: #303133;
  margin-bottom: 15px;
}

.notes-content {
  padding: 15px;
  background-color: #f9f9f9;
  border-left: 4px solid #409eff;
  border-radius: 4px;
  line-height: 1.6;
  color: #606266;
}

.update-actions .el-button {
  margin-right: 10px;
  margin-bottom: 10px;
}
</style>
