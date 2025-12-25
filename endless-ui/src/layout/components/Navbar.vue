<template>
  <div class="navbar">
    <hamburger id="hamburger-container" :is-active="sidebar.opened" class="hamburger-container"
               @toggleClick="toggleSideBar"/>

    <breadcrumb v-if="!topNav" id="breadcrumb-container" class="breadcrumb-container"/>
    <top-nav v-if="topNav" id="topmenu-container" class="topmenu-container"/>

    <div class="right-menu">
      <template v-if="device!=='mobile'">
        <search id="header-search" class="right-menu-item"/>

        <el-tooltip content="源码地址" effect="dark" placement="bottom">
          <ruo-yi-git id="ruoyi-git" class="right-menu-item hover-effect"/>
        </el-tooltip>

        <screenfull id="screenfull" class="right-menu-item hover-effect"/>

        <el-tooltip content="意见反馈" effect="dark" placement="bottom">
          <div class="right-menu-item hover-effect" @click="openFeedbackDialog">
            <i class="el-icon-chat-dot-round"></i>
          </div>
        </el-tooltip>

      </template>

      <el-dropdown class="avatar-container right-menu-item hover-effect" trigger="click">
        <div class="avatar-wrapper">
          <img :src="avatar" class="user-avatar">
          <i class="el-icon-caret-bottom"/>
        </div>
        <el-dropdown-menu slot="dropdown">
          <router-link to="/user/profile">
            <el-dropdown-item>个人中心</el-dropdown-item>
          </router-link>
          <el-dropdown-item @click.native="setting = true">
            <span>布局设置</span>
          </el-dropdown-item>
          <el-dropdown-item divided @click.native="logout">
            <span>退出登录</span>
          </el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
    </div>

    <!-- 反馈弹窗 -->
    <el-dialog
      :close-on-click-modal="false"
      :visible.sync="feedbackDialogVisible"
      append-to-body
      class="feedback-dialog"
      title="意见反馈"
      width="600px"
    >
      <el-tabs v-model="feedbackTab">
        <el-tab-pane label="提交反馈" name="submit">
          <el-form ref="feedbackForm" :model="feedbackForm" :rules="feedbackRules" label-width="80px">
            <el-form-item label="反馈类型" prop="feedbackType">
              <el-radio-group v-model="feedbackForm.feedbackType">
                <el-radio :label="1">Bug 反馈</el-radio>
                <el-radio :label="2">功能建议</el-radio>
                <el-radio :label="3">使用问题</el-radio>
                <el-radio :label="4">其他</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="标题" prop="title">
              <el-input v-model="feedbackForm.title" maxlength="200" placeholder="请简要描述您的问题或建议"
                        show-word-limit/>
            </el-form-item>
            <el-form-item label="详细描述" prop="content">
              <el-input
                v-model="feedbackForm.content"
                :rows="4"
                maxlength="2000"
                placeholder="请详细描述您遇到的问题或建议，以便我们更好地改进"
                show-word-limit
                type="textarea"
              />
            </el-form-item>
            <el-form-item label="附件" prop="attachmentUrls">
              <el-upload
                :auto-upload="false"
                :file-list="uploadFileList"
                :limit="3"
                :on-change="handleFileChange"
                :on-exceed="handleExceed"
                :on-remove="handleFileRemove"
                accept="image/*,.pdf,.txt,.log"
                action="#"
                class="feedback-upload"
              >
                <el-button plain size="small" type="primary">
                  <i class="el-icon-upload2"></i> 选择文件
                </el-button>
                <div slot="tip" class="el-upload__tip">支持图片、PDF、TXT、LOG，单个文件不超过1MB，最多3个</div>
              </el-upload>
            </el-form-item>
            <el-form-item label="联系方式" prop="contact">
              <el-input v-model="feedbackForm.contact" placeholder="QQ/邮箱（选填，方便我们联系您）"/>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="历史反馈" name="history">
          <div v-loading="historyLoading" class="feedback-history">
            <div v-if="feedbackHistory.length === 0 && !historyLoading" class="empty-history">
              <i class="el-icon-document"></i>
              <p>暂无反馈记录</p>
            </div>
            <div v-for="item in feedbackHistory" :key="item.id" class="history-item" @click="showFeedbackDetail(item)">
              <div class="history-header">
                <el-tag :type="getFeedbackTypeTag(item.feedbackType)" size="mini">
                  {{ getFeedbackTypeName(item.feedbackType) }}
                </el-tag>
                <el-tag :type="getStatusTag(item.status)" effect="plain" size="mini">
                  {{ getStatusName(item.status) }}
                </el-tag>
                <span class="history-time">{{ item.createTime }}</span>
              </div>
              <div class="history-title">{{ item.title }}</div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
      <span slot="footer" class="dialog-footer">
        <el-button @click="feedbackDialogVisible = false">关闭</el-button>
        <el-button v-if="feedbackTab === 'submit'" :loading="feedbackSubmitting" type="primary"
                   @click="submitFeedbackHandler">
          提交反馈
        </el-button>
        <el-button v-if="feedbackTab === 'history'" plain type="primary" @click="loadFeedbackHistory">
          <i class="el-icon-refresh"></i> 刷新
        </el-button>
      </span>
    </el-dialog>

    <!-- 反馈详情弹窗 -->
    <el-dialog
      :close-on-click-modal="true"
      :visible.sync="feedbackDetailVisible"
      append-to-body
      class="feedback-detail-dialog"
      title="反馈详情"
      width="550px"
    >
      <div v-if="currentFeedback" class="feedback-detail">
        <div class="detail-row">
          <span class="label">反馈类型：</span>
          <el-tag :type="getFeedbackTypeTag(currentFeedback.feedbackType)" size="small">
            {{ getFeedbackTypeName(currentFeedback.feedbackType) }}
          </el-tag>
        </div>
        <div class="detail-row">
          <span class="label">状态：</span>
          <el-tag :type="getStatusTag(currentFeedback.status)" size="small">
            {{ getStatusName(currentFeedback.status) }}
          </el-tag>
        </div>
        <div class="detail-row">
          <span class="label">提交时间：</span>
          <span>{{ currentFeedback.createdAt }}</span>
        </div>
        <div class="detail-row">
          <span class="label">标题：</span>
          <span>{{ currentFeedback.title }}</span>
        </div>
        <div class="detail-row content-row">
          <span class="label">内容：</span>
          <div class="content-text">{{ currentFeedback.content }}</div>
        </div>
        <div v-if="currentFeedback.attachmentUrls" class="detail-row">
          <span class="label">附件：</span>
          <div class="attachment-list">
            <a v-for="(url, idx) in parseAttachments(currentFeedback.attachmentUrls)" :key="idx" :href="url"
               class="attachment-link" target="_blank">
              <i class="el-icon-document"></i> 附件{{ idx + 1 }}
            </a>
          </div>
        </div>
        <div v-if="currentFeedback.adminReply" class="admin-reply-section">
          <div class="reply-header">
            <i class="el-icon-chat-line-round"></i>
            <span>管理员回复</span>
          </div>
          <div class="reply-content">{{ currentFeedback.adminReply }}</div>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="feedbackDetailVisible = false">关闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {mapGetters} from 'vuex'
import Breadcrumb from '@/components/Breadcrumb'
import TopNav from '@/components/TopNav'
import Hamburger from '@/components/Hamburger'
import Screenfull from '@/components/Screenfull'
import SizeSelect from '@/components/SizeSelect'
import Search from '@/components/HeaderSearch'
import RuoYiGit from '@/components/RuoYi/Git'
import RuoYiDoc from '@/components/RuoYi/Doc'
import {getFeedbackDetail, getMyFeedbackList, submitFeedback, uploadFeedbackAttachment} from '@/api/system/feedback'

export default {
  components: {
    Breadcrumb,
    TopNav,
    Hamburger,
    Screenfull,
    SizeSelect,
    Search,
    RuoYiGit,
    RuoYiDoc
  },
  data() {
    return {
      feedbackDialogVisible: false,
      feedbackSubmitting: false,
      feedbackTab: 'submit',
      feedbackForm: {
        feedbackType: 1,
        title: '',
        content: '',
        contact: '',
        attachmentUrls: ''
      },
      feedbackRules: {
        feedbackType: [{required: true, message: '请选择反馈类型', trigger: 'change'}],
        title: [{required: true, message: '请输入标题', trigger: 'blur'}],
        content: [{required: true, message: '请输入详细描述', trigger: 'blur'}]
      },
      uploadFileList: [],
      feedbackHistory: [],
      historyLoading: false,
      feedbackDetailVisible: false,
      currentFeedback: null
    }
  },
  computed: {
    ...mapGetters([
      'sidebar',
      'avatar',
      'device'
    ]),
    setting: {
      get() {
        return this.$store.state.settings.showSettings
      },
      set(val) {
        this.$store.dispatch('settings/changeSetting', {
          key: 'showSettings',
          value: val
        })
      }
    },
    topNav: {
      get() {
        return this.$store.state.settings.topNav
      }
    },
    themeColor() {
      return this.$store.state.settings.theme || '#6366f1'
    }
  },
  methods: {
    toggleSideBar() {
      this.$store.dispatch('app/toggleSideBar')
    },
    async logout() {
      this.$confirm('确定注销并退出系统吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$store.dispatch('LogOut').then(() => {
          location.href = '/index';
        })
      }).catch(() => {
      });
    },
    // 打开反馈弹窗
    openFeedbackDialog() {
      this.feedbackForm = {
        feedbackType: 1,
        title: '',
        content: '',
        contact: '',
        attachmentUrls: ''
      }
      this.uploadFileList = []
      this.feedbackTab = 'submit'
      this.feedbackDialogVisible = true
      // 加载历史反馈
      this.loadFeedbackHistory()
    },
    // 加载历史反馈
    async loadFeedbackHistory() {
      this.historyLoading = true
      try {
        const response = await getMyFeedbackList()
        if (response.code === 200) {
          this.feedbackHistory = response.data || []
        }
      } catch (error) {
        console.error('加载反馈历史失败:', error)
      } finally {
        this.historyLoading = false
      }
    },
    // 文件选择变化
    handleFileChange(file, fileList) {
      // 检查文件大小（1MB = 1024 * 1024 bytes）
      const maxSize = 1 * 1024 * 1024
      if (file.size > maxSize) {
        this.$message.error('文件大小不能超过1MB')
        fileList.pop()
        return
      }
      this.uploadFileList = fileList
    },
    // 文件移除
    handleFileRemove(file, fileList) {
      this.uploadFileList = fileList
    },
    // 文件超出限制
    handleExceed() {
      this.$message.warning('最多只能上传3个文件')
    },
    // 上传附件
    async uploadAttachments() {
      if (this.uploadFileList.length === 0) {
        return ''
      }
      const urls = []
      for (const fileItem of this.uploadFileList) {
        try {
          const response = await uploadFeedbackAttachment(fileItem.raw)
          if (response.code === 200 && response.data) {
            urls.push(response.data)
          }
        } catch (error) {
          console.error('上传附件失败:', error)
        }
      }
      return urls.join(',')
    },
    // 提交反馈
    async submitFeedbackHandler() {
      this.$refs.feedbackForm.validate(async (valid) => {
        if (!valid) return

        this.feedbackSubmitting = true
        try {
          // 先上传附件
          const attachmentUrls = await this.uploadAttachments()

          const feedbackData = {
            ...this.feedbackForm,
            attachmentUrls: attachmentUrls,
            osName: navigator.userAgentData?.platform || navigator.platform || 'Unknown',
            browserInfo: navigator.userAgent,
            screenResolution: `${window.screen.width}x${window.screen.height}`
          }

          const response = await submitFeedback(feedbackData)

          if (response.code === 200) {
            this.$message.success('反馈提交成功，感谢您的宝贵意见！')
            this.feedbackTab = 'history'
            this.loadFeedbackHistory()
            // 重置表单
            this.feedbackForm = {
              feedbackType: 1,
              title: '',
              content: '',
              contact: '',
              attachmentUrls: ''
            }
            this.uploadFileList = []
          } else {
            this.$message.error(response.msg || '提交失败，请稍后重试')
          }
        } catch (error) {
          console.error('提交反馈失败:', error)
          this.$message.error('网络错误，请稍后重试')
        } finally {
          this.feedbackSubmitting = false
        }
      })
    },
    // 显示反馈详情（通过UUID从远程获取完整信息）
    async showFeedbackDetail(item) {
      this.feedbackDetailVisible = true
      this.currentFeedback = {...item, loading: true}

      try {
        const response = await getFeedbackDetail(item.uuid)
        if (response.code === 200) {
          this.currentFeedback = response.data
          // 同步更新本地列表中的状态
          const idx = this.feedbackHistory.findIndex(f => f.uuid === item.uuid)
          if (idx !== -1 && response.data.status !== undefined) {
            this.$set(this.feedbackHistory, idx, {
              ...this.feedbackHistory[idx],
              status: response.data.status
            })
          }
        } else {
          this.$message.error(response.msg || '获取详情失败')
          this.currentFeedback = item
        }
      } catch (error) {
        console.error('获取反馈详情失败:', error)
        this.currentFeedback = item
      }
    },
    // 解析附件URL
    parseAttachments(urls) {
      if (!urls) return []
      return urls.split(',').filter(url => url.trim())
    },
    // 获取反馈类型名称
    getFeedbackTypeName(type) {
      const map = {1: 'Bug反馈', 2: '功能建议', 3: '使用问题', 4: '其他'}
      return map[type] || '未知'
    },
    // 获取反馈类型标签颜色
    getFeedbackTypeTag(type) {
      const map = {1: 'danger', 2: 'success', 3: 'warning', 4: 'info'}
      return map[type] || 'info'
    },
    // 获取状态名称
    getStatusName(status) {
      const map = {0: '待处理', 1: '处理中', 2: '已解决', 3: '已关闭'}
      return map[status] || '未知'
    },
    // 获取状态标签颜色
    getStatusTag(status) {
      const map = {0: 'info', 1: 'warning', 2: 'success', 3: ''}
      return map[status] || 'info'
    }
  }
}
</script>

<style lang="scss" scoped>
@import "~@/assets/styles/variables.scss";

.navbar {
  height: $navbar-height;
  overflow: visible;
  position: relative;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  box-shadow: none;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px 0 0;

  .hamburger-container {
    line-height: $navbar-height;
    height: 100%;
    float: left;
    cursor: pointer;
    transition: all 0.3s ease;
    -webkit-tap-highlight-color: transparent;
    padding: 0 16px;
    display: flex;
    align-items: center;

    &:hover {
      background: rgba(var(--theme-color-rgb, 99, 102, 241), 0.08);
    }
  }

  .breadcrumb-container {
    float: left;
    margin-left: 8px;
  }

  .topmenu-container {
    position: absolute;
    left: 50px;
  }

  .errLog-container {
    display: inline-block;
    vertical-align: top;
  }

  .right-menu {
    display: flex;
    align-items: center;
    height: 100%;

    &:focus {
      outline: none;
    }

    .right-menu-item {
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 0 10px;
      height: 36px;
      font-size: 18px;
      color: #64748b;
      border-radius: $border-radius-sm;
      margin: 0 2px;
      transition: all 0.2s ease;

      &.hover-effect {
        cursor: pointer;

        &:hover {
          background: rgba(var(--theme-color-rgb, 99, 102, 241), 0.08);
          color: var(--theme-color, $primary-color);
        }
      }
    }

    .avatar-container {
      margin-left: 12px;

      .avatar-wrapper {
        display: flex;
        align-items: center;
        cursor: pointer;
        padding: 4px 12px 4px 4px;
        // 移除圆角，避免方形轮廓
        border-radius: 0;
        transition: all 0.2s ease;
        // 确保没有阴影
        box-shadow: none !important;
        // 移除背景色
        background: transparent !important;

        &:hover {
          // 悬浮时也不显示背景
          background: transparent !important;
          // 悬浮时也不显示阴影
          box-shadow: none !important;
        }

        .user-avatar {
          width: 36px;
          height: 36px;
          // 移除圆角，改为方形头像
          border-radius: 0;
          // 移除边框
          border: none !important;
          transition: all 0.2s ease;
          // 确保头像本身没有阴影
          box-shadow: none !important;
          // 移除可能的 outline
          outline: none !important;
        }

        .el-icon-caret-bottom {
          margin-left: 6px;
          font-size: 12px;
          color: #94a3b8;
          transition: transform 0.2s ease;
        }

        &:hover {
          .el-icon-caret-bottom {
            color: var(--theme-color, $primary-color);
          }
        }
      }
    }
  }
}

// 全局覆盖 - 确保下拉菜单容器没有额外阴影
::v-deep .el-dropdown {
  box-shadow: none !important;
}

::v-deep .avatar-container .el-dropdown {
  box-shadow: none !important;
}

// 反馈弹窗样式
::v-deep .feedback-dialog {
  .el-dialog__header {
    padding: 20px 20px 10px;
    border-bottom: 1px solid #ebeef5;
  }

  .el-dialog__title {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
  }

  .el-dialog__body {
    padding: 16px 20px;
  }

  .el-tabs__header {
    margin-bottom: 16px;
  }

  .el-form-item__label {
    font-weight: 500;
  }

  .el-radio-group {
    .el-radio {
      margin-right: 16px;
      margin-bottom: 8px;
    }
  }

  .el-textarea__inner {
    font-family: inherit;
    line-height: 1.6;
  }

  .feedback-upload {
    .el-upload__tip {
      color: #909399;
      font-size: 12px;
      margin-top: 4px;
    }
  }

  .feedback-history {
    min-height: 200px;
    max-height: 400px;
    overflow-y: auto;

    .empty-history {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 60px 0;
      color: #909399;

      i {
        font-size: 48px;
        margin-bottom: 12px;
      }

      p {
        margin: 0;
      }
    }

    .history-item {
      padding: 12px 16px;
      border: 1px solid #ebeef5;
      border-radius: 8px;
      margin-bottom: 10px;
      cursor: pointer;
      transition: all 0.2s;

      &:hover {
        border-color: #409EFF;
        background: #f5f7fa;
      }

      .history-header {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 8px;

        .history-time {
          margin-left: auto;
          font-size: 12px;
          color: #909399;
        }
      }

      .history-title {
        font-size: 14px;
        color: #303133;
        font-weight: 500;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .history-reply {
        display: flex;
        align-items: center;
        gap: 4px;
        margin-top: 8px;
        font-size: 12px;
        color: #67c23a;

        i {
          font-size: 14px;
        }
      }
    }
  }

  .el-dialog__footer {
    padding: 15px 20px 20px;
    border-top: 1px solid #ebeef5;
  }
}

// 反馈详情弹窗样式
::v-deep .feedback-detail-dialog {
  .el-dialog__header {
    padding: 20px 20px 10px;
    border-bottom: 1px solid #ebeef5;
  }

  .el-dialog__body {
    padding: 20px;
  }

  .feedback-detail {
    .detail-row {
      display: flex;
      margin-bottom: 12px;
      font-size: 14px;

      .label {
        color: #909399;
        width: 80px;
        flex-shrink: 0;
      }

      &.content-row {
        flex-direction: column;

        .content-text {
          margin-top: 8px;
          padding: 12px;
          background: #f5f7fa;
          border-radius: 6px;
          line-height: 1.6;
          white-space: pre-wrap;
          word-break: break-all;
        }
      }
    }

    .attachment-list {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;

      .attachment-link {
        display: inline-flex;
        align-items: center;
        gap: 4px;
        padding: 4px 10px;
        background: #ecf5ff;
        color: #409EFF;
        border-radius: 4px;
        font-size: 13px;
        text-decoration: none;

        &:hover {
          background: #d9ecff;
        }
      }
    }

    .admin-reply-section {
      margin-top: 20px;
      padding: 16px;
      background: linear-gradient(135deg, #f0f9eb 0%, #e1f3d8 100%);
      border-radius: 8px;
      border-left: 4px solid #67c23a;

      .reply-header {
        display: flex;
        align-items: center;
        gap: 6px;
        font-size: 14px;
        font-weight: 600;
        color: #67c23a;
        margin-bottom: 10px;
      }

      .reply-content {
        font-size: 14px;
        color: #303133;
        line-height: 1.6;
        white-space: pre-wrap;
      }
    }
  }

  .el-dialog__footer {
    padding: 15px 20px 20px;
    border-top: 1px solid #ebeef5;
  }
}
</style>
