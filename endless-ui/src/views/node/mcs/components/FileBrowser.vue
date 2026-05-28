<template>
  <el-card class="side-card file-card" shadow="hover">
    <div slot="header" class="card-header">
      <div class="file-header-title">
        <i class="el-icon-folder"></i>
        <span>文件浏览</span>
      </div>
      <div class="file-header-actions">
        <el-button
          class="expand-btn"
          icon="el-icon-full-screen"
          size="mini"
          type="text"
          @click="$emit('open-dialog')"
        >展开</el-button>
        <el-button
          :disabled="!canGoParent"
          class="parent-btn"
          icon="el-icon-back"
          size="mini"
          type="text"
          @click="$emit('go-parent')"
        >上级</el-button>
      </div>
    </div>
    <div class="path-container">
      <i class="el-icon-location-outline"></i>
      <div class="path">{{ displayPath }}</div>
    </div>
    <el-skeleton :count="4" :loading="loading" animated>
      <el-scrollbar class="file-list-scrollbar">
        <div class="file-list-content">
          <div v-if="files.length === 0 && !loading" class="empty-files">
            <i class="el-icon-document-delete"></i>
            <span>目录为空</span>
          </div>
          <div
            v-for="item in files"
            :key="item.fullPath"
            class="file-row"
            @click="$emit('file-click', item)"
            @dblclick="$emit('enter', item)"
          >
            <i
              :class="item.isDir ? 'el-icon-folder file-icon folder-icon' : 'el-icon-document file-icon file-icon-doc'"
            />
            <span :title="item.name" class="name">{{ item.name }}</span>
            <div v-if="!item.isDir" class="file-actions">
              <i class="el-icon-view" title="预览" @click.stop="$emit('preview', item)"></i>
              <i
                v-if="isEditable(item.name)"
                class="el-icon-edit"
                title="编辑文件"
                @click.stop="$emit('edit', item)"
              ></i>
              <i class="el-icon-download" title="下载文件" @click.stop="$emit('download', item)"></i>
              <i class="el-icon-delete" title="删除文件" @click.stop="$emit('delete', item)"></i>
            </div>
            <div v-else class="file-actions">
              <i class="el-icon-delete" title="删除目录" @click.stop="$emit('delete', item)"></i>
            </div>
          </div>
        </div>
      </el-scrollbar>
    </el-skeleton>
    <!-- 快捷配置文件 -->
    <div v-if="quickConfigFiles.length > 0" class="quick-config-section">
      <div class="quick-config-header">
        <i class="el-icon-setting"></i>
        <span>快捷配置</span>
        <span class="config-count">{{ quickConfigFiles.length }} 个文件</span>
      </div>
      <div class="quick-config-buttons">
        <el-tooltip
          v-for="file in quickConfigFiles"
          :key="file.fullPath"
          :content="`编辑 ${file.name}`"
          effect="dark"
          placement="top"
        >
          <el-button class="config-quick-btn" size="mini" @click="$emit('edit', file)">
            <i class="el-icon-document"></i>
            <span>{{ file.name }}</span>
          </el-button>
        </el-tooltip>
      </div>
    </div>
  </el-card>
</template>

<script>
import { MC_CONFIG_FILES, TEXT_EXTENSIONS, SPECIAL_FILE_NAMES } from '../constants'

export default {
  name: 'FileBrowser',
  props: {
    files: {
      type: Array,
      default: () => []
    },
    loading: {
      type: Boolean,
      default: false
    },
    currentPath: {
      type: String,
      default: ''
    },
    serverPath: {
      type: String,
      default: ''
    },
    canGoParent: {
      type: Boolean,
      default: false
    }
  },
  computed: {
    displayPath() {
      return this.currentPath || this.serverPath || '/'
    },
    quickConfigFiles() {
      if (!this.files?.length) return []
      return this.files
        .filter(item => !item.isDir && MC_CONFIG_FILES.some(f => item.name.toLowerCase() === f.toLowerCase()))
        .slice(0, 8)
    }
  },
  methods: {
    isEditable(filename) {
      if (!filename) return false
      if (MC_CONFIG_FILES.some(f => filename.toLowerCase() === f.toLowerCase())) return true
      return TEXT_EXTENSIONS.some(ext => filename.toLowerCase().endsWith(ext)) ||
             SPECIAL_FILE_NAMES.includes(filename.toLowerCase())
    }
  }
}
</script>

<style lang="scss" scoped>
.side-card {
  margin-bottom: 16px;
  border-radius: 12px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.file-header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.file-header-actions {
  display: flex;
  gap: 4px;
}

.path-container {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #f5f7fa;
  border-radius: 6px;
  margin-bottom: 12px;
  font-size: 12px;
  color: #606266;
}

.path {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-list-scrollbar {
  height: 300px;
}

.file-row {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  cursor: pointer;
  transition: background-color 0.2s;
  border-radius: 6px;

  &:hover {
    background: #f5f7fa;

    .file-actions {
      opacity: 1;
    }
  }
}

.file-icon {
  margin-right: 8px;
  font-size: 16px;
}

.folder-icon {
  color: #e6a23c;
}

.file-icon-doc {
  color: #409eff;
}

.name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 13px;
}

.file-actions {
  display: flex;
  gap: 8px;
  opacity: 0;
  transition: opacity 0.2s;

  i {
    cursor: pointer;
    color: #909399;
    font-size: 14px;

    &:hover {
      color: #409eff;
    }

    &.el-icon-delete:hover {
      color: #f56c6c;
    }
  }
}

.empty-files {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px;
  color: #909399;
  gap: 8px;
}

.quick-config-section {
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
}

.quick-config-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  font-size: 13px;
  color: #606266;
}

.config-count {
  color: #909399;
  font-size: 12px;
}

.quick-config-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.config-quick-btn {
  font-size: 12px;
  padding: 4px 8px;

  i {
    margin-right: 4px;
  }
}
</style>
