<template>
  <el-dialog
    :close-on-click-modal="false"
    :title="dialogTitle"
    :visible.sync="visible"
    class="config-edit-dialog"
    width="90%"
    @close="handleClose"
  >
    <el-row :gutter="20">
      <!-- 左侧：文件内容 -->
      <el-col :span="showTranslationPanel ? 12 : 24">
        <el-card class="editor-panel-card" shadow="never">
          <div slot="header" class="editor-panel-header">
            <div class="header-left">
              <i class="el-icon-document"></i>
              <span>{{ isMcConfig ? '原始配置' : '文件内容' }}</span>
            </div>
            <el-button
              v-if="isMcConfig"
              icon="el-icon-s-tools"
              plain
              size="mini"
              type="primary"
              @click="$emit('toggle-translation')"
            >
              {{ showTranslationPanel ? '隐藏' : '显示' }}配置说明
            </el-button>
          </div>
          <el-scrollbar style="height: 60vh;">
            <div v-loading="loading" class="editor-wrapper">
              <monaco-editor
                v-if="visible && content !== ''"
                :key="editorKey"
                v-model="localContent"
                :language="language"
                :options="editorOptions"
                class="config-editor"
                @editorDidMount="onEditorMount"
              />
              <div v-else-if="visible && content === '' && !loading" class="editor-empty">
                <i class="el-icon-loading"></i>
                <span>正在加载文件内容...</span>
              </div>
            </div>
          </el-scrollbar>
        </el-card>
      </el-col>
      <!-- 右侧：配置说明 -->
      <el-col v-if="showTranslationPanel" :span="12">
        <el-card class="config-panel-card" shadow="never">
          <div slot="header" class="config-panel-header">
            <i class="el-icon-s-tools"></i>
            <span>配置说明</span>
            <el-tag size="mini" type="info">{{ translations.length }} 项</el-tag>
          </div>
          <el-scrollbar style="height: 60vh;">
            <div class="translation-panel">
              <div v-for="(item, index) in translations" :key="index" class="config-item">
                <div class="config-item-header">
                  <span class="config-item-key">{{ item.key }}</span>
                  <div class="config-item-tags">
                    <el-tag v-if="item.type" class="config-item-type" size="mini">{{ item.type }}</el-tag>
                  </div>
                </div>
                <div class="config-item-body">
                  <div class="config-item-value">
                    <el-switch
                      v-if="item.isBool"
                      v-model="item.boolValue"
                      active-color="#67c23a"
                      inactive-color="#dcdfe6"
                      @change="updateConfigValue(item)"
                    ></el-switch>
                    <el-input
                      v-else
                      v-model="item.value"
                      placeholder="请输入值"
                      size="small"
                      @input="updateConfigValue(item)"
                    >
                      <i slot="prefix" class="el-icon-edit"></i>
                    </el-input>
                  </div>
                  <div class="config-item-desc">
                    <i class="el-icon-info"></i>
                    <span>{{ item.zhDesc }}</span>
                  </div>
                </div>
              </div>
            </div>
          </el-scrollbar>
        </el-card>
      </el-col>
    </el-row>
    <div slot="footer" class="dialog-footer">
      <el-button @click="visible = false">取 消</el-button>
      <el-button :loading="saveLoading" type="primary" @click="handleSave">保 存</el-button>
    </div>
  </el-dialog>
</template>

<script>
import MonacoEditor from 'monaco-editor-vue'
import { MC_CONFIG_FILES, EDITOR_LANGUAGE_MAP, SPECIAL_FILE_LANGUAGE_MAP, DEBOUNCE_DELAY } from '../constants'
import { mcConfigTranslations } from '../mcConfigTranslations'

export default {
  name: 'ConfigEditor',
  components: {
    MonacoEditor
  },
  props: {
    show: {
      type: Boolean,
      default: false
    },
    file: {
      type: Object,
      default: null
    },
    content: {
      type: String,
      default: ''
    },
    loading: {
      type: Boolean,
      default: false
    },
    saveLoading: {
      type: Boolean,
      default: false
    },
    showTranslation: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      localContent: '',
      updateTimer: null,
      editorOptions: {
        theme: 'vs-dark',
        language: 'properties',
        automaticLayout: true,
        minimap: { enabled: true },
        wordWrap: 'on',
        fontSize: 14,
        lineNumbers: 'on',
        renderWhitespace: 'selection'
      }
    }
  },
  computed: {
    visible: {
      get() {
        return this.show
      },
      set(val) {
        if (!val) {
          this.$emit('close')
        }
      }
    },
    dialogTitle() {
      return this.file ? `编辑文件 - ${this.file.name}` : '编辑文件'
    },
    editorKey() {
      return this.file?.fullPath || 'editor'
    },
    isMcConfig() {
      return MC_CONFIG_FILES.some(f => this.file?.name?.toLowerCase() === f.toLowerCase())
    },
    showTranslationPanel() {
      return this.showTranslation && this.isMcConfig
    },
    language() {
      const filename = this.file?.name?.toLowerCase() || ''
      // 检查扩展名映射
      for (const [ext, lang] of Object.entries(EDITOR_LANGUAGE_MAP)) {
        if (filename.endsWith(ext)) return lang
      }
      // 检查特殊文件名映射
      return SPECIAL_FILE_LANGUAGE_MAP[filename] || 'plaintext'
    },
    translations() {
      if (!this.file || !this.localContent) return []
      const fileName = this.file.name.toLowerCase()
      const translations = mcConfigTranslations[fileName] || {}
      const lines = this.localContent.split('\n')
      const result = []

      lines.forEach((line, index) => {
        const trimmed = line.trim()
        if (!trimmed || trimmed.startsWith('#')) return

        const match = trimmed.match(/^([^=:#]+)[=:](.*)$/)
        if (match) {
          const key = match[1].trim()
          const value = match[2].trim()
          const translation = translations[key] || {}

          const isBool = value.toLowerCase() === 'true' || value.toLowerCase() === 'false'
          const boolValue = value.toLowerCase() === 'true'

          let autoType = ''
          if (!translation.type) {
            if (isBool) autoType = '布尔'
            else if (/^\d+$/.test(value)) autoType = '数字'
            else if (/^\d+\.\d+$/.test(value)) autoType = '小数'
            else autoType = '文本'
          }

          result.push({
            key,
            value,
            originalValue: value,
            lineIndex: index,
            zhDesc: translation.zh || '暂无说明',
            type: translation.type || autoType,
            isBool,
            boolValue,
            existsInFile: true
          })
        }
      })

      return result.sort((a, b) => a.lineIndex - b.lineIndex)
    }
  },
  watch: {
    content: {
      handler(newVal) {
        this.localContent = newVal || ''
      },
      immediate: true
    }
  },
  beforeDestroy() {
    if (this.updateTimer) {
      clearTimeout(this.updateTimer)
    }
  },
  methods: {
    onEditorMount(editor) {
      this.$nextTick(() => {
        if (editor && this.localContent) {
          const currentValue = editor.getValue()
          if (currentValue !== this.localContent) {
            editor.setValue(this.localContent)
          }
          editor.focus()
          editor.setScrollPosition({ scrollTop: 0, scrollLeft: 0 })
        }
      })
    },
    updateConfigValue(row) {
      if (!this.localContent) return

      if (this.updateTimer) {
        clearTimeout(this.updateTimer)
      }

      this.updateTimer = setTimeout(() => {
        const lines = this.localContent.split('\n')
        const newValue = row.isBool ? (row.boolValue ? 'true' : 'false') : row.value

        for (let i = 0; i < lines.length; i++) {
          const trimmed = lines[i].trim()
          if (trimmed && !trimmed.startsWith('#')) {
            const match = trimmed.match(/^([^=:#]+)[=:](.*)$/)
            if (match && match[1].trim() === row.key) {
              const separator = lines[i].includes('=') ? '=' : ':'
              const indent = lines[i].match(/^\s*/)[0]
              lines[i] = `${indent}${row.key}${separator}${newValue}`
              break
            }
          }
        }

        this.localContent = lines.join('\n')
      }, DEBOUNCE_DELAY)
    },
    handleSave() {
      this.$emit('save', this.localContent)
    },
    handleClose() {
      this.localContent = ''
    }
  }
}
</script>

<style lang="scss" scoped>
.config-edit-dialog {
  ::v-deep {
    .el-dialog__body {
      padding: 16px 20px;
    }
  }
}

.editor-panel-card,
.config-panel-card {
  height: 100%;
  border-radius: 8px;
}

.editor-panel-header,
.config-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.editor-wrapper {
  min-height: 400px;
}

.config-editor {
  height: 60vh;
}

.editor-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #909399;
  gap: 8px;
}

.translation-panel {
  padding: 8px;
}

.config-item {
  padding: 12px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  margin-bottom: 8px;
  transition: box-shadow 0.2s;

  &:hover {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }
}

.config-item-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.config-item-key {
  font-weight: 600;
  font-size: 14px;
  color: #303133;
}

.config-item-body {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.config-item-desc {
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: flex-start;
  gap: 4px;

  i {
    margin-top: 2px;
  }
}
</style>
