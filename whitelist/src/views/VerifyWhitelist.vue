<template>
  <div class="verify-container">
    <div class="verify-card">
      <div v-if="loading" class="loading-state">
        <el-icon class="loading-icon">
          <Loading/>
        </el-icon>
        <span>正在验证...</span>
      </div>

      <div v-else-if="error" class="error-state">
        <el-icon class="error-icon">
          <Warning/>
        </el-icon>
        <span>{{ error }}</span>
        <el-button type="primary" @click="retryVerify">重试</el-button>
      </div>

      <div v-else-if="success" class="success-state">
        <el-icon class="success-icon">
          <CircleCheck/>
        </el-icon>
        <span>验证成功!</span>
        <p class="success-message">{{ successMessage }}</p>
        <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import {onMounted, ref} from 'vue'
import {useRoute} from 'vue-router'
import {CircleCheck, Loading, Warning} from '@element-plus/icons-vue'
import axios from 'axios'

const route = useRoute()
const loading = ref(true)
const error = ref(null)
const success = ref(false)
const successMessage = ref('')

const verifyCode = async () => {
  const code = route.query.code
  if (!code) {
    error.value = '验证码不能为空'
    loading.value = false
    return
  }

  try {
    const res = await axios.get(`${import.meta.env.VITE_API_URL}/mc/whitelist/verify`, {
      params: {code}
    })

    if (res.data.code === 200) {
      success.value = true
      successMessage.value = res.data.msg || '验证成功,请等待管理员审核!'
    } else {
      error.value = res.data.msg || '验证失败'
    }
  } catch (err) {
    error.value = '验证过程中出现错误,请稍后重试'
    console.error('验证失败:', err)
  } finally {
    loading.value = false
  }
}

const retryVerify = () => {
  loading.value = true
  error.value = null
  success.value = false
  verifyCode()
}

onMounted(() => {
  verifyCode()
})
</script>

<style scoped>
.verify-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: var(--theme-gradient);
  padding: 20px;
}

.verify-card {
  background: var(--theme-bg);
  padding: 40px;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  text-align: center;
  min-width: 300px;
  backdrop-filter: blur(8px);
}

.loading-state,
.error-state,
.success-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.loading-icon {
  font-size: 48px;
  color: var(--theme-primary);
  animation: rotate 2s linear infinite;
}

.error-icon {
  font-size: 48px;
  color: #f56c6c;
}

.success-icon {
  font-size: 48px;
  color: #67c23a;
}

.success-message {
  color: var(--theme-text);
  margin: 16px 0;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style> 