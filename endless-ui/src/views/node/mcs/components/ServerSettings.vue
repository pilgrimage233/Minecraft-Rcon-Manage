<template>
  <el-dialog :visible.sync="visible" title="实例运维设置" width="720px" append-to-body @close="handleClose">
    <el-form ref="form" :model="form" :rules="rules" label-width="140px" size="small">
      <el-tabs v-model="activeTab">
        <!-- 崩溃与重启 -->
        <el-tab-pane label="崩溃与重启" name="crash">
          <el-form-item label="崩溃重启">
            <el-switch v-model="form.crashRestartEnabled" :active-value="1" :inactive-value="0" />
          </el-form-item>
          <el-form-item v-if="form.crashRestartEnabled" label="延迟重启(秒)">
            <el-input-number v-model="form.crashRestartDelaySec" :max="300" :min="0" />
          </el-form-item>
          <el-form-item v-if="form.crashRestartEnabled" label="最大重试次数">
            <el-input-number v-model="form.crashRestartMaxRetry" :max="10" :min="1" />
          </el-form-item>
          <el-divider />
          <el-form-item label="定时重启">
            <el-switch v-model="form.scheduledRestartEnabled" :active-value="1" :inactive-value="0" />
          </el-form-item>
          <el-form-item v-if="form.scheduledRestartEnabled" label="重启时间(cron)">
            <el-input v-model="form.scheduledRestartCron" placeholder="0 0 4 * * ?" style="width: 260px" />
            <span style="color: #909399; font-size: 12px; margin-left: 8px;">例: 每天凌晨4点</span>
          </el-form-item>
          <el-divider />
          <el-form-item label="优雅关闭超时(秒)">
            <el-input-number v-model="form.gracefulShutdownTimeoutSec" :max="120" :min="5" />
            <span style="color: #909399; font-size: 12px; margin-left: 8px;">超时后强制终止</span>
          </el-form-item>
        </el-tab-pane>

        <!-- 持久在线 -->
        <el-tab-pane label="持久在线" name="alive">
          <el-form-item label="持久在线(保活)">
            <el-switch v-model="form.keepAliveEnabled" :active-value="1" :inactive-value="0" />
            <span style="color: #909399; font-size: 12px; margin-left: 8px;">服务器停止时自动重启</span>
          </el-form-item>
          <el-form-item v-if="form.keepAliveEnabled" label="检查间隔(秒)">
            <el-input-number v-model="form.keepAliveCheckIntervalSec" :max="600" :min="30" />
          </el-form-item>
        </el-tab-pane>

        <!-- 定时开关 -->
        <el-tab-pane label="定时开关" name="schedule">
          <el-form-item label="定时开机">
            <el-switch v-model="form.scheduledStartEnabled" :active-value="1" :inactive-value="0" />
          </el-form-item>
          <el-form-item v-if="form.scheduledStartEnabled" label="开机时间(cron)">
            <el-input v-model="form.scheduledStartCron" placeholder="0 0 8 * * ?" style="width: 260px" />
          </el-form-item>
          <el-divider />
          <el-form-item label="定时关机">
            <el-switch v-model="form.scheduledStopEnabled" :active-value="1" :inactive-value="0" />
          </el-form-item>
          <el-form-item v-if="form.scheduledStopEnabled" label="关机时间(cron)">
            <el-input v-model="form.scheduledStopCron" placeholder="0 0 23 * * ?" style="width: 260px" />
          </el-form-item>
        </el-tab-pane>

        <!-- 备份管理 -->
        <el-tab-pane label="备份管理" name="backup">
          <el-form-item label="定时备份">
            <el-switch v-model="form.backupEnabled" :active-value="1" :inactive-value="0" />
          </el-form-item>
          <el-form-item v-if="form.backupEnabled" label="备份时间(cron)">
            <el-input v-model="form.backupCron" placeholder="0 0 */6 * * ?" style="width: 260px" />
          </el-form-item>
          <el-form-item label="备份保留份数">
            <el-input-number v-model="form.backupRetainCount" :max="50" :min="1" />
          </el-form-item>
          <el-divider />
          <el-form-item label="备份完成通知">
            <el-switch v-model="form.backupNotifyEnabled" :active-value="1" :inactive-value="0" />
          </el-form-item>
        </el-tab-pane>

        <!-- 告警通知 -->
        <el-tab-pane label="告警通知" name="alert">
          <el-form-item label="通知邮箱" prop="notifyEmail">
            <el-input v-model="form.notifyEmail" placeholder="接收告警通知的邮箱" style="width: 300px" />
          </el-form-item>
          <el-divider />
          <el-form-item label="崩溃通知">
            <el-switch v-model="form.crashNotifyEnabled" :active-value="1" :inactive-value="0" />
          </el-form-item>
          <el-form-item label="磁盘告警">
            <el-switch v-model="form.diskAlertEnabled" :active-value="1" :inactive-value="0" />
          </el-form-item>
          <el-form-item v-if="form.diskAlertEnabled" label="磁盘阈值(GB)">
            <el-input-number v-model="form.diskAlertThresholdGb" :max="100" :min="1" />
            <span style="color: #909399; font-size: 12px; margin-left: 8px;">剩余空间低于此值告警</span>
          </el-form-item>
          <el-divider />
          <el-form-item label="TPS采集方式">
            <el-select v-model="form.tpsMode" style="width: 200px">
              <el-option label="AUTO (自动)" value="AUTO" />
              <el-option label="TPS命令" value="TPS_COMMAND" />
              <el-option label="Spark API" value="SPARK_API" />
              <el-option label="禁用" value="DISABLED" />
            </el-select>
          </el-form-item>
          <el-form-item v-if="form.tpsMode === 'SPARK_API'" label="Spark端口">
            <el-input-number v-model="form.sparkApiPort" :max="65535" :min="1" />
          </el-form-item>
          <el-form-item label="TPS告警">
            <el-switch v-model="form.tpsAlertEnabled" :active-value="1" :inactive-value="0" />
          </el-form-item>
          <el-form-item v-if="form.tpsAlertEnabled" label="TPS阈值">
            <el-input-number v-model="form.tpsAlertThreshold" :max="20" :min="1" :step="0.5" :precision="1" />
            <span style="color: #909399; font-size: 12px; margin-left: 8px;">TPS低于此值告警</span>
          </el-form-item>
        </el-tab-pane>

        <!-- 邮件模板 -->
        <el-tab-pane label="邮件模板" name="template">
          <email-template-editor />
        </el-tab-pane>
      </el-tabs>
    </el-form>
    <div slot="footer">
      <el-button size="small" @click="visible = false">取 消</el-button>
      <el-button size="small" type="primary" :loading="submitting" @click="submitForm">保 存</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { getSettingsByServer, addSettings, updateSettings } from '@/api/node/settings'
import EmailTemplateEditor from './EmailTemplateEditor.vue'

export default {
  name: 'ServerSettings',
  components: { EmailTemplateEditor },
  data() {
    return {
      visible: false,
      submitting: false,
      activeTab: 'crash',
      serverId: null,
      nodeId: null,
      existingId: null,
      form: this.getDefaultForm(),
      rules: {
        notifyEmail: [
          { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    getDefaultForm() {
      return {
        crashRestartEnabled: 0,
        crashRestartDelaySec: 10,
        crashRestartMaxRetry: 3,
        keepAliveEnabled: 0,
        keepAliveCheckIntervalSec: 60,
        scheduledStartEnabled: 0,
        scheduledStartCron: '',
        scheduledStopEnabled: 0,
        scheduledStopCron: '',
        scheduledRestartEnabled: 0,
        scheduledRestartCron: '',
        gracefulShutdownTimeoutSec: 30,
        notifyEmail: '',
        crashNotifyEnabled: 0,
        backupNotifyEnabled: 0,
        backupEnabled: 0,
        backupCron: '',
        backupRetainCount: 5,
        diskAlertEnabled: 0,
        diskAlertThresholdGb: 5,
        tpsMode: 'AUTO',
        tpsAlertEnabled: 0,
        tpsAlertThreshold: 15.0,
        sparkApiPort: null
      }
    },
    open(row) {
      this.serverId = row.id || row.nodeInstancesId
      this.nodeId = row.nodeId
      this.existingId = null
      this.form = this.getDefaultForm()
      this.activeTab = 'crash'
      this.visible = true
      this.loadSettings()
    },
    async loadSettings() {
      if (!this.serverId) return
      try {
        const res = await getSettingsByServer(this.serverId)
        if (res.code === 200 && res.data) {
          this.existingId = res.data.id
          Object.keys(this.form).forEach(key => {
            if (res.data[key] !== null && res.data[key] !== undefined) {
              this.form[key] = res.data[key]
            }
          })
        }
      } catch (e) {
        console.warn('加载设置失败:', e)
      }
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        this.submitting = true
        const data = {
          ...this.form,
          nodeId: this.nodeId,
          nodeServerId: this.serverId
        }
        if (this.existingId) {
          data.id = this.existingId
          updateSettings(data).then(res => {
            if (res.code === 200) {
              this.$message.success('保存成功')
              this.visible = false
            }
          }).finally(() => { this.submitting = false })
        } else {
          addSettings(data).then(res => {
            if (res.code === 200) {
              this.$message.success('保存成功')
              this.visible = false
            }
          }).finally(() => { this.submitting = false })
        }
      })
    },
    handleClose() {
      this.$refs.form && this.$refs.form.resetFields()
    }
  }
}
</script>
