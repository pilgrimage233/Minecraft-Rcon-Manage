<template>
  <div v-if="shouldShowSakura" class="sakura-container">
    <span v-for="n in count"
          :key="n"
          :style="getSakuraStyle(n)"
          class="sakura">
    </span>
  </div>
</template>

<script setup>
import {computed} from 'vue'

const props = defineProps({
  count: {
    type: Number,
    default: 10
  },
  isDark: {
    type: Boolean,
    default: false
  },
  currentTheme: {
    type: String,
    default: 'default'
  }
})

// 只在默认主题和樱花主题显示樱花特效
const shouldShowSakura = computed(() => {
  return ['default', 'sakura'].includes(props.currentTheme)
})

// 优化性能：预计算樱花样式
const sakuraStyles = computed(() => {
  return Array(props.count).fill(null).map(() => ({
    '--delay': `${Math.random() * 5}s`,
    '--size': `${Math.random() * 20 + 10}px`,
    '--left': `${Math.random() * 100}%`,
    'opacity': props.isDark ? 0.3 : 0.7
  }))
})

const getSakuraStyle = (index) => sakuraStyles.value[index]
</script>

<style scoped>
.sakura-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;
}

.sakura {
  position: absolute;
  top: -10%;
  left: var(--left);
  width: var(--size);
  height: var(--size);
  background: #ffd7e6;
  border-radius: 100% 0 100% 100%;
  animation: fall var(--delay) linear infinite;
  transform-origin: center;
  opacity: var(--opacity, 0.7);
  will-change: transform;
}

/* 添加GPU加速 */
.sakura::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: inherit;
  border-radius: inherit;
  transform: rotate(45deg);
  will-change: transform;
}

@keyframes fall {
  0% {
    top: -10%;
    transform: rotate(0deg) translateX(0);
    opacity: 0;
  }
  10% {
    opacity: var(--opacity, 0.7);
  }
  90% {
    opacity: var(--opacity, 0.7);
  }
  100% {
    top: 100%;
    transform: rotate(360deg) translateX(100px);
    opacity: 0;
  }
}
</style>