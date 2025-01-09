<template>
  <div :class="{ 'dark': modelValue }" class="theme-switcher">
    <el-popover
        placement="bottom"
        :width="300"
        trigger="click"
        popper-class="theme-popover"
    >
      <template #reference>
        <el-button class="theme-button">
          <el-icon>
            <Brush/>
          </el-icon>
          主题
        </el-button>
      </template>

      <div class="themes-container">
        <div class="themes-grid">
          <div
              v-for="(theme, key) in themes"
              :key="key"
              class="theme-item"
              :class="{ 'active': currentTheme === key }"
              :style="{
              background: theme.gradient
            }"
              @click="changeTheme(key)"
          >
            <div class="theme-name">{{ theme.name }}</div>
            <el-icon v-if="currentTheme === key">
              <Check/>
            </el-icon>
          </div>
        </div>

        <div class="dark-mode-toggle">
          <span>暗色模式</span>
          <el-switch
              :model-value="modelValue"
              @update:model-value="updateDarkMode"
              :active-icon="Moon"
              :inactive-icon="Sunny"
              inline-prompt
          />
        </div>
      </div>
    </el-popover>
  </div>
</template>

<script setup>
import {onMounted, ref, watch} from 'vue'
import {Brush, Check, Moon, Sunny} from '@element-plus/icons-vue'
import {getThemeVariables, themes} from '@/config/themes'
import {ElMessage} from 'element-plus'

const props = defineProps({
  modelValue: {
    type: Boolean,
    required: true
  }
})

const emit = defineEmits(['update:modelValue'])

const currentTheme = ref(localStorage.getItem('theme') || 'default')

const changeTheme = (themeName) => {
  try {
    currentTheme.value = themeName
    localStorage.setItem('theme', themeName)
    document.documentElement.removeAttribute('data-theme')
    document.documentElement.setAttribute('data-theme', themeName)
    applyTheme(themeName)
    ElMessage.success('主题切换成功')
  } catch (error) {
    console.error('Theme change error:', error)
    ElMessage.error('主题切换失败')
  }
}

const applyTheme = (themeName) => {
  const variables = getThemeVariables(themeName)
  const root = document.documentElement
  Object.entries(variables).forEach(([key, value]) => {
    root.style.setProperty(key, value)
  })
}

const updateDarkMode = (value) => {
  emit('update:modelValue', value)
}

// 监听主题变化，确保在组件重新挂载时应用正确的主题
watch(currentTheme, (newTheme) => {
  applyTheme(newTheme)
}, {immediate: true})

// 初始化主题
onMounted(() => {
  const savedTheme = localStorage.getItem('theme') || 'default'
  document.documentElement.setAttribute('data-theme', savedTheme)
  applyTheme(savedTheme)
})
</script>

<style scoped>
.theme-switcher {
  position: fixed;
  bottom: 20px;
  left: 20px;
  z-index: 1000;
}

.theme-button {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 16px;
  border-radius: 20px;
  background: var(--theme-bg) !important;
  border: 1px solid var(--theme-border) !important;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  color: var(--theme-text) !important;
  font-family: 'CustomFont', sans-serif;
}

.theme-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
  background: var(--theme-bg) !important;
}

.themes-container {
  padding: 16px;
}

.themes-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-bottom: 16px;
}

.theme-item {
  position: relative;
  height: 80px;
  border-radius: 12px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  transition: all 0.3s ease;
}

.theme-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
}

.theme-item.active {
  outline: 3px solid var(--el-color-primary);
}

.theme-name {
  color: white;
  font-size: 14px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  background: rgba(0, 0, 0, 0.2);
  padding: 4px 12px;
  border-radius: 12px;
  backdrop-filter: blur(4px);
}

.theme-item .el-icon {
  position: absolute;
  top: 8px;
  right: 8px;
  color: white;
  background: var(--el-color-primary);
  padding: 4px;
  border-radius: 50%;
}

.dark-mode-toggle {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 0;
  border-top: 1px solid var(--el-border-color);
  margin-top: 8px;
}

/* 暗色模式样式 */
.theme-switcher.dark .theme-button {
  background: var(--theme-bg-dark);
  border-color: var(--theme-border-dark);
  color: var(--theme-text-dark);
}

/* 主题特定样式 */
[data-theme="cyberpunk"] .theme-button {
  box-shadow: var(--theme-neon-shadow);
  text-shadow: var(--theme-text-shadow);
}

[data-theme="sakura"] .theme-button {
  border-color: rgba(255, 105, 180, 0.3);
}

:deep(.theme-popover) {
  background: var(--theme-bg);
  border: 1px solid rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(12px);
}

.dark :deep(.theme-popover) {
  background: rgba(35, 35, 45, 0.95);
}

.dark .dark-mode-toggle {
  color: rgba(255, 255, 255, 0.9);
  border-color: rgba(255, 255, 255, 0.1);
}
</style> 