<!-- 版本信息组件 -->
<template>
  <div :class="{ 'is-collapse': isCollapse }" class="version-info-container">
    <template v-if="!isCollapse">
      <div :class="{ 'has-update': hasUpdate }" class="version-info">
        <div class="version-row">
          <span class="version-label">当前版本</span>
          <span class="version-value">{{ currentVersion }}</span>
        </div>
        <div class="version-row">
          <span class="version-label">最新版本</span>
          <span class="version-value">{{ latestVersion }}</span>
          <el-tooltip v-if="hasUpdate" content="点击下载新版本" effect="light" placement="top">
            <i class="el-icon-download update-icon" @click="goToDownload"></i>
          </el-tooltip>
        </div>
        <div v-if="checking" class="checking-text">
          <i class="el-icon-loading"></i> 检查更新中...
        </div>
        <el-link v-else :underline="false" class="check-link" type="primary" @click="checkUpdate">
          <i class="el-icon-refresh"></i> 检查更新
        </el-link>
      </div>
    </template>
    <template v-else>
      <!-- 折叠状态下只显示图标 -->
      <div class="version-collapsed">
        <el-tooltip v-if="hasUpdate" :content="'发现新版本 ' + latestVersion" placement="right">
          <i class="el-icon-warning-outline update-available" @click="goToDownload"></i>
        </el-tooltip>
        <el-tooltip v-else :content="'当前版本 ' + currentVersion" placement="right">
          <i class="el-icon-success version-ok"></i>
        </el-tooltip>
      </div>
    </template>
  </div>
</template>

<script>
import {mapActions, mapState} from 'vuex'

export default {
  name: 'VersionInfo',
  computed: {
    ...mapState('version', [
      'currentVersion',
      'latestVersion',
      'hasUpdate',
      'downloadUrl',
      'checking'
    ]),
    ...mapState({
      isCollapse: state => !state.app.sidebar.opened,
      theme: state => state.settings.theme,
      sideTheme: state => state.settings.sideTheme
    })
  },
  created() {
    this.checkUpdate()
  },
  methods: {
    ...mapActions('version', ['checkUpdate']),
    goToDownload() {
      if (this.downloadUrl) {
        window.open(this.downloadUrl, '_blank')
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.version-info-container {
  position: fixed;
  left: 0;
  bottom: 0;
  width: 200px;
  padding: 8px 16px;
  z-index: 999;
  transition: all 0.3s;
  box-sizing: border-box;

  :deep(.theme-dark) & {
    background: var(--menuBg, #304156);
    border-top: 1px solid rgba(255, 255, 255, 0.1);
  }

  :deep(.theme-light) & {
    background: #ffffff;
    border-top: 1px solid #dcdfe6;
  }

  &.is-collapse {
    width: 54px;
    padding: 8px 0;

    .version-collapsed {
      text-align: center;

      i {
        font-size: 16px;
        cursor: pointer;
        transition: all 0.3s;
        color: #409EFF;

        &.update-available {
          animation: bounce 1s infinite;

          &:hover {
            opacity: 0.8;
          }
        }

        &.version-ok {
          color: #67C23A;
          opacity: 0.8;

          &:hover {
            opacity: 1;
          }
        }
      }
    }
  }

  .version-info {
    .version-row {
      display: flex;
      align-items: center;
      margin: 4px 0;
      font-size: 12px;
      line-height: 1.5;

      .version-label {
        margin-right: 8px;
        min-width: 56px;
        color: #409EFF;
      }

      .version-value {
        flex: 1;
        color: #409EFF;
      }

      .update-icon {
        cursor: pointer;
        margin-left: 4px;
        font-size: 14px;
        color: #409EFF;

        &:hover {
          opacity: 0.8;
        }
      }
    }

    .checking-text {
      font-size: 12px;
      text-align: center;
      margin-top: 8px;
      color: #409EFF;

      i {
        margin-right: 4px;
      }
    }

    .check-link {
      font-size: 12px;
      display: block;
      text-align: center;
      margin-top: 8px;
      color: #409EFF !important;

      &:hover {
        opacity: 0.8;
      }

      i {
        margin-right: 4px;
      }
    }
  }
}

@keyframes bounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-2px);
  }
}
</style>
