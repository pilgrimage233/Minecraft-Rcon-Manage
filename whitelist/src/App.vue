<template>
  <el-config-provider>
    <div :class="{ 'dark': isDark }" class="app-container">
      <ThemeSwitcher v-model="isDark"/>
      <div class="route-container">
        <router-view></router-view>
      </div>
    </div>
  </el-config-provider>
</template>

<script setup>
import {onBeforeMount, onMounted, ref, watch} from 'vue'
import {ElConfigProvider} from 'element-plus'
import ThemeSwitcher from './components/common/ThemeSwitcher.vue'
import {getThemeVariables} from './config/themes'

const isDark = ref(localStorage.getItem('darkMode') === 'true')

// 检测系统主题
const checkSystemTheme = () => {
  const darkMode = window.matchMedia('(prefers-color-scheme: dark)').matches
  isDark.value = darkMode
  document.documentElement.classList.toggle('dark', darkMode)
  localStorage.setItem('darkMode', darkMode)
}

// 监听主题变化
watch(isDark, (newValue) => {
  document.documentElement.classList.toggle('dark', newValue)
  localStorage.setItem('darkMode', newValue)
}, {immediate: true})

const getRGBValues = (color) => {
  // 将颜色转换为RGB值
  const div = document.createElement('div')
  div.style.color = color
  document.body.appendChild(div)
  const rgbColor = window.getComputedStyle(div).color
  document.body.removeChild(div)
  return rgbColor.match(/\d+/g).join(', ')
}

// 初始化主题
const initTheme = () => {
  const savedTheme = localStorage.getItem('theme') || 'default'
  const variables = getThemeVariables(savedTheme)
  const root = document.documentElement

  // 设置主题变量
  Object.entries(variables).forEach(([key, value]) => {
    root.style.setProperty(key, value)
  })

  // 设置RGB变量
  root.style.setProperty('--theme-primary-rgb', getRGBValues(variables['--theme-primary']))
  root.style.setProperty('--theme-secondary-rgb', getRGBValues(variables['--theme-secondary']))
}

onBeforeMount(() => {
  initTheme()
})

onMounted(() => {
  // 监听系统主题变化
  window.matchMedia('(prefers-color-scheme: dark)')
      .addEventListener('change', checkSystemTheme)
})
</script>

<style>
:root {
  --theme-transition: all 0.3s ease;
}

.app-container {
  min-height: 100vh;
  background: var(--theme-bg);
  color: var(--theme-text);
}

#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  transition: var(--theme-transition);
}

.app-wrapper {
  background-image: var(--theme-gradient);
  min-height: 100vh;
  transition: var(--theme-transition);
}

/* 暗色模式基础样式 */
html.dark {
  color-scheme: dark;
}

html.dark body {
  background-color: var(--theme-bg);
  color: var(--theme-text);
}

/* Element Plus 暗色模式覆盖 */
html.dark .el-button {
  --el-button-bg-color: rgba(35, 35, 45, 0.8);
  --el-button-border-color: rgba(255, 255, 255, 0.1);
  --el-button-hover-bg-color: rgba(40, 40, 50, 0.9);
  --el-button-hover-border-color: rgba(255, 255, 255, 0.2);
  --el-button-text-color: rgba(255, 255, 255, 0.9);
}

html.dark .el-input__inner {
  background-color: rgba(30, 30, 40, 0.8);
  border-color: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.9);
}

html.dark .el-dialog {
  background-color: rgba(35, 35, 45, 0.95);
  border: 1px solid rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
}

html.dark .el-radio__label {
  color: #e5e5e5;
}

html.dark .el-form-item__label {
  color: #e5e5e5;
}

/* 平滑过渡 */
* {
  transition: background-color 0.3s ease, color 0.3s ease, border-color 0.3s ease;
}

/* 确保主题切换器不会被其他元素遮挡 */
.theme-switcher {
  position: fixed !important;
  z-index: 2000 !important;
}

/* 服务器状态容器的z-index调整 */
.server-status-container {
  z-index: 1900 !important;
}

.route-container {
  min-height: 100vh;
  width: 100%;
}

/* 确保所有页面都使用主题变量 */
.app-wrapper {
  background-image: var(--theme-gradient) !important;
  color: var(--theme-text) !important;
}

/* 确保所有容器都使用主题背景 */
.form-container,
.member-container {
  background: var(--theme-bg) !important;
  color: var(--theme-text) !important;
}

/* 暗色模式下的通用样式 */
.dark .form-container,
.dark .member-container {
  background: var(--theme-bg-dark) !important;
  color: var(--theme-text-dark) !important;
}
</style>