<template>
  <div
      class="app-wrapper"
      @click.self="handleFormBlur"
      @touchstart.self="handleFormBlur"
  >
    <SakuraBackground
        :isDark="isDark"
        :currentTheme="currentTheme"
    />
    <div
        class="server-status-container"
        :class="{ 'form-focused': isFormFocused }"
    >
      <div class="status-header">
        <div class="header-title">
          <i class="el-icon-monitor"></i>
          <span>服务器状态</span>
          <el-button
              :loading="loading"
              class="refresh-btn"
              size="small"
              @click="debouncedRefresh(true)"
          >
            <el-icon>
              <Refresh/>
            </el-icon>
          </el-button>
        </div>
      </div>
      <div class="status-content">
        <div v-if="initialLoading" class="loading-state">
          <el-icon class="loading-icon">
            <Loading/>
          </el-icon>
          <span>加载中...</span>
        </div>

        <template v-else>
          <div v-for="server in serverStatus.servers"
               :key="server.name"
               class="server-block animate-in">
            <div class="server-name">
              <i class="el-icon-connection"></i>
              {{ server.name }}
            </div>
            <div class="online-info">
              <div v-if="server.players.length > 0" class="player-list">
                <div class="players-label">
                  <i class="el-icon-user"></i>
                  在线玩家 ({{ server.playerCount }})：
                </div>
                <div class="players-container">
                  <el-tag
                      v-for="player in server.players"
                      :key="player"
                      class="player-tag"
                      effect="light"
                      size="small"
                  >
                    {{ player }}
                  </el-tag>
                </div>
              </div>
            </div>
          </div>
          <div class="query-time animate-in">
            <i class="el-icon-time"></i>
            {{ serverStatus.queryTime }}
          </div>
        </template>
      </div>
    </div>

    <div
        class="form-container"
        :class="{ 'focused': isFormFocused }"
        @click="handleFormFocus"
        @touchstart="handleFormFocus"
    >
      <div class="title-container">
        <i class="el-icon-user-solid"></i>
        <h2>白名单申请</h2>
        <el-button
            class="view-members-btn"
            text
            type="primary"
            @click="$router.push('/whitelist-members')"
        >
          <el-icon>
            <User/>
          </el-icon>
          查看成员
        </el-button>
      </div>

      <div class="description">
        欢迎加入我们的服务器！请填写以下信息完成白名单申请。
      </div>

      <el-form :model="form" class="animated-form" label-width="auto">
        <el-form-item label="ID：">
          <el-input v-model="form.userName" placeholder="请输入游戏名称"></el-input>
        </el-form-item>
        <el-form-item label="QQ：">
          <el-input v-model="form.qqNum" placeholder="请输入QQ号"></el-input>
        </el-form-item>
        <el-form-item label="正版：">
          <el-radio-group v-model="form.onlineFlag">
            <el-radio label="1">是</el-radio>
            <el-radio label="0">否</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="描述：" label-width="auto">
          <el-input v-model="form.remark" placeholder="请输入描述 非必填" type="textarea"></el-input>
        </el-form-item>
        <div class="button-group">
          <el-button v-loading.fullscreen.lock="fullscreenLoading"
                     class="submit-btn"
                     type="primary"
                     @click="submitForm">
            <i class="el-icon-check"></i> 提交申请
          </el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue';
import {ElMessage} from 'element-plus';
import axios from 'axios';
import {Loading, Refresh, User} from '@element-plus/icons-vue'
import {debounce} from 'lodash-es';
import SakuraBackground from './common/SakuraBackground.vue'

const http = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'https://application.shenzhuo.vip', // 使用环境变量
  timeout: 8000
});

const form = reactive({
  userName: '',
  qqNum: '',
  onlineFlag: '',
  remark: ''
});

let loading = false;

const serverStatus = reactive({
  servers: [], // 存储所有服务器的状态
  queryTime: '-'
});

// 添加全屏loading状态
const fullscreenLoading = ref(false);

// 添加防抖处理的刷新函数
const debouncedRefresh = debounce((refresh) => {
  getOnlinePlayer(refresh);
}, 500);

// 添加初始加载状态
const initialLoading = ref(true);

const submitForm = () => {
  if (!form.userName || !form.qqNum || !form.onlineFlag) {
    ElMessage.error('请填写完整信息');
  } else if (!/^\d{5,11}$/.test(form.qqNum)) {
    ElMessage.error('QQ号格式错误');
  } else {
    fullscreenLoading.value = true;

    // 尝试获取用户IP，添加备用接
    const getIpFromPrimarySource = () => {
      return fetch('https://ip.useragentinfo.com/json', {
        // 忽略SSL错误
        mode: 'no-cors'
      })
          .then(response => response.json())
          .catch(error => {
            console.warn('主要IP获取接口失败，尝试备用接口:', error);
            return getIpFromBackupSource();
          });
    };

    // 备用IP获取接口
    const getIpFromBackupSource = () => {
      return fetch('https://ipinfo.io/json')
          .then(response => response.json())
          .catch(error => {
            console.warn('备用IP获取接口也失败:', error);
            // 返回一个空对象，表示无法获取IP
            return {};
          });
    };

    // 开始获取IP
    getIpFromPrimarySource()
        .then(data => {
          // 准备请求头
          const headers = {
            'origin': window.location.origin
          };

          // 如果成功获取到IP，添加到请求头
          if (data && data.ip) {
            headers['X-Real-IP'] = data.ip;
          }

          // 发送表单请求
          return http.post('/mc/whitelist/apply', form, {headers});
        })
        .then((res) => {
          if (res.data.code === 200) {
            ElMessage.success(res.data.msg);
          } else {
            ElMessage.error(res.data.msg || '未知错误，请联系管理员');
          }
          fullscreenLoading.value = false;
        })
        .catch((error) => {
          console.error('提交表单请求出错：', error);
          ElMessage.error('提交表单时发生错误，请检查网络或联系管理员');
          fullscreenLoading.value = false;
        });
  }
};

const getOnlinePlayer = (reflash) => {
  loading = true;
  http.get('/api/v1/getOnlinePlayer').then((res) => {
    if (res.data.code === 200) {
      const data = res.data.data;
      // 重置服务器列表
      serverStatus.servers = [];

      // 遍历所有服务器数据
      Object.entries(data).forEach(([serverName, serverData]) => {
        // 跳过查询时间字段
        if (serverName === '查询时间') {
          serverStatus.queryTime = serverData;
          return;
        }

        // 处理服务器数据
        try {
          // 处理在线玩家字符串
          let players = [];
          const playersStr = serverData['在线玩家'];
          if (playersStr) {
            players = playersStr.replace(/^\[|\]$/g, '').split(',')
                .map(p => p.trim())
                .filter(p => p);
          }

          serverStatus.servers.push({
            name: serverName,
            playerCount: serverData['在线人数'],
            players: players
          });
        } catch (e) {
          console.error(`处理服务器 ${serverName} 数据失败:`, e);
        }
      });
      if (reflash) {
        ElMessage.success('刷新成功！');
      }
    } else {
      ElMessage.error(res.data.msg || '获取服务器状态失败');
    }
    loading = false;
    initialLoading.value = false; // 设置初始加载状态为 false
  }).catch((error) => {
    console.error('查询在线玩家请求出错：', error);
    ElMessage.error('查询在线玩家时发生错误，请检查网络或联系管理员');
    loading = false;
    initialLoading.value = false; // 错误时也需要关闭加载状态
  });
};

// 获取当前主题
const currentTheme = ref(localStorage.getItem('theme') || 'default')

const isFormFocused = ref(false);

const handleFormFocus = () => {
  isFormFocused.value = true;
};

const handleFormBlur = () => {
  isFormFocused.value = false;
};

onMounted(() => {
  getOnlinePlayer();
});
</script>

<style scoped>
.app-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  min-height: 100vh;
  background-size: 400% 400%;
  background-image: var(--theme-gradient);
  animation: warmGradient 20s ease infinite;
  padding: 20px;
  font-family: 'CustomFont', sans-serif;
}

@keyframes warmGradient {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}

.form-container {
  background: var(--theme-bg);
  color: var(--theme-text);
  backdrop-filter: blur(8px);
  padding: 30px;
  border-radius: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  width: min(100%, 500px);
  transform: translateY(0);
  transition: all 0.3s ease;
}

.form-container:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.title-container {
  text-align: center;
  margin-bottom: 25px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 10px;
}

.title-container h2 {
  color: var(--theme-primary);
  font-size: 28px;
  margin: 10px 0;
  flex-grow: 0; /* 防止标题占据过多空间 */
}

.description {
  text-align: center;
  color: #333;
  margin-bottom: 25px;
  font-size: 14px;
  font-weight: 500;
}

.animated-form :deep(.el-form-item) {
  transition: all 0.3s ease;
  margin-bottom: 25px;
}

.animated-form :deep(.el-form-item:hover) {
  transform: translateX(5px);
}

.button-group {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

.submit-btn {
  min-width: 140px;
  height: 40px;
  border-radius: 20px;
  font-weight: 500;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  font-family: 'CustomFont', sans-serif;
}

.submit-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(
      90deg,
      transparent,
      rgba(255, 255, 255, 0.2),
      transparent
  );
  transition: 0.5s;
}

.submit-btn:hover::before {
  left: 100%;
}

.submit-btn:hover {
  transform: scale(1.05);
}

:deep(.el-input__inner) {
  border-radius: 8px;
  border: 1px solid rgba(64, 158, 255, 0.2);
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.8);
  height: 40px;
  transform: translateY(0);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  font-family: 'CustomFont', sans-serif;
}

:deep(.el-input__inner:hover) {
  border-color: #409EFF;
}

:deep(.el-input__inner:focus) {
  border-color: #409EFF;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
  background: #fff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
}

:deep(.el-textarea__inner) {
  border-radius: 8px;
  border: 1px solid rgba(64, 158, 255, 0.2);
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.8);
  min-height: 100px;
  padding: 12px;
}

:deep(.el-textarea__inner:hover) {
  border-color: #409EFF;
}

:deep(.el-textarea__inner:focus) {
  border-color: #409EFF;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
  background: #fff;
}

:deep(.el-form-item) {
  margin-bottom: 25px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #303133;
}

.form-container :deep(.el-input__wrapper),
.form-container :deep(.el-textarea__wrapper) {
  box-shadow: none !important;
  padding: 0;
  background: none;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.form-container {
  animation: fadeIn 0.6s ease-out;
}

.server-status-container {
  position: fixed;
  width: 280px;
  background: var(--theme-bg);
  color: var(--theme-text);
  backdrop-filter: blur(12px);
  border: 1px solid var(--theme-border);
  border-radius: 12px;
  padding: 15px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  z-index: 1001;

  /* 默认在右上角 */
  top: 20px;
  right: 20px;
}

.server-status-container:hover {
  transform: translateX(-5px);
  box-shadow: 4px 8px 32px rgba(0, 0, 0, 0.12);
}

.status-header {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--theme-border);
}

.header-title {
  display: flex;
  align-items: center;
  font-weight: 500;
  color: var(--theme-primary);
}

.header-title i {
  margin-right: 8px;
}

.status-content {
  font-size: 14px;
  padding: 0 8px;
  width: 100%;
  box-sizing: border-box;
}

.server-name {
  font-weight: 500;
  color: var(--theme-primary);
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  font-size: 15px;
  padding: 4px 8px;
  background: rgba(var(--theme-primary-rgb), 0.1);
  border-radius: 6px;
}

.server-name i {
  margin-right: 8px;
  color: #409EFF;
  font-size: 16px;
}

.online-info {
  padding-left: 4px;
  width: 100%;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
}

.player-list {
  display: flex;
  flex-direction: column;
  width: 100%;
}

.players-label {
  color: #606266;
  font-size: 14px;
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  padding: 4px 0;
  padding-left: 4px;
}

.players-label i {
  margin-right: 8px;
  color: #67C23A;
  font-size: 15px;
}

.players-container {
  display: inline-flex;
  flex-wrap: wrap;
  gap: 4px;
  padding-left: 8px;
  width: auto;
  min-width: 0;
}

.player-tag {
  background-color: rgba(var(--theme-secondary-rgb), 0.1);
  border: 1px solid var(--theme-border);
  color: var(--theme-text);
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.player-tag:hover {
  background-color: rgba(var(--theme-primary-rgb), 0.15);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(var(--theme-primary-rgb), 0.15);
}

/* 在线指示点 */
.online-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: var(--theme-secondary);
  display: inline-block;
  margin-left: 4px;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% {
    transform: scale(0.95);
    box-shadow: 0 0 0 0 rgba(var(--theme-secondary-rgb), 0.4);
  }
  70% {
    transform: scale(1);
    box-shadow: 0 0 0 6px rgba(var(--theme-secondary-rgb), 0);
  }
  100% {
    transform: scale(0.95);
    box-shadow: 0 0 0 0 rgba(var(--theme-secondary-rgb), 0);
  }
}

.query-time {
  font-size: 12px;
  color: var(--theme-text);
  padding: 8px;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 8px;
  margin-top: 15px;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0.6;
}

.query-time i {
  margin-right: 6px;
  color: #909399;
  font-size: 14px;
}

.refresh-btn {
  padding: 2px;
  height: 20px;
  width: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  margin-left: 8px;
  background-color: #f4f4f5;
  border: none;
  transform: scale(1);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.refresh-btn:hover {
  transform: rotate(180deg);
  background-color: #ecf5ff;
  color: #409EFF;
}

.refresh-btn:focus {
  color: #409EFF;
  background-color: #ecf5ff;
}

.refresh-btn:active {
  transform: scale(0.95);
}

.server-block {
  transition: all 0.3s ease;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 15px;
  background: rgba(255, 255, 255, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.2);
  width: 100%;
  box-sizing: border-box;
  overflow: hidden;
}

.server-block:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.player-count i,
.players-label i {
  margin-right: 8px;
  flex-shrink: 0;
}

.header-title :deep(.refresh-btn) {
  padding: 2px;
  height: 24px;
  width: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  margin-left: 8px;
  background: none;
  border: none;
  color: #909399;
}

.header-title :deep(.refresh-btn .el-icon) {
  font-size: 16px;
}

.header-title :deep(.refresh-btn:hover) {
  transform: rotate(180deg);
  background: none;
  color: #409EFF;
}

.header-title :deep(.refresh-btn:focus) {
  color: #409EFF;
  background: none;
  outline: none;
}

.header-title :deep(.refresh-btn i) {
  margin: 0;
}

/* 优化服务器块样式 */
.server-block {
  transition: all 0.3s ease;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 15px;
  background: rgba(255, 255, 255, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.2);
  width: 100%;
}

.server-block:hover {
  background: rgba(255, 255, 255, 0.8);
}

/* 优化玩家标签样式 */
.player-tag {
  transition: all 0.3s ease;
  border-radius: 10px;
  padding: 2px 10px;
  background-color: rgba(103, 194, 58, 0.1);
  border: 1px solid rgba(103, 194, 58, 0.2);
  color: #67C23A;
  font-size: 12px;
  line-height: 1.4;
  white-space: nowrap;
}

.player-tag:hover {
  background-color: rgba(103, 194, 58, 0.2);
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(103, 194, 58, 0.1);
}

/* 优化查询时间样式 */
.query-time {
  font-size: 12px;
  color: #909399;
  padding: 8px;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 8px;
  margin-top: 15px;
}

/* 添加错误信息样式 */
.error-message {
  color: #f56c6c;
  font-size: 13px;
  display: flex;
  align-items: center;
  padding: 12px;
  background: rgba(245, 108, 108, 0.1);
  border-radius: 8px;
  margin: 8px 0;
  animation: shake 0.5s cubic-bezier(0.36, 0.07, 0.19, 0.97) both;
}

.error-message i {
  margin-right: 8px;
  font-size: 16px;
}

@keyframes shake {
  10%, 90% {
    transform: translateX(-1px);
  }
  20%, 80% {
    transform: translateX(2px);
  }
  30%, 50%, 70% {
    transform: translateX(-4px);
  }
  40%, 60% {
    transform: translateX(4px);
  }
}

/* 优化表单容器 */
.form-container {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

/* 优化提交按钮 */
.submit-btn {
  background: linear-gradient(45deg, #409EFF, #36cfc9);
  border: none;
  box-shadow: 0 4px 15px rgba(64, 158, 255, 0.3);
}

.submit-btn:hover {
  background: linear-gradient(45deg, #66b1ff, #5cdbd3);
  box-shadow: 0 6px 20px rgba(64, 158, 255, 0.4);
}

/* 优化单选按钮组样式 */
:deep(.el-radio) {
  margin-right: 20px;
  transition: all 0.3s ease;
}

:deep(.el-radio:hover) {
  transform: translateY(-2px);
}

:deep(.el-radio__input.is-checked + .el-radio__label) {
  color: #409EFF;
  font-weight: 500;
}

/* 樱花容器 */
.sakura-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;
}

/* 樱花样式 */
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
  opacity: 0.7;
}

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
}

/* 樱花飘落动画 */
@keyframes fall {
  0% {
    top: -10%;
    transform: rotate(0deg) translateX(0);
    opacity: 0;
  }
  10% {
    opacity: 0.7;
  }
  90% {
    opacity: 0.7;
  }
  100% {
    top: 100%;
    transform: rotate(360deg) translateX(100px);
    opacity: 0;
  }
}

/* 修改服务器状态容器的z-index确保在樱花上层 */
.server-status-container {
  z-index: 1001;
}

/* 修改表单容器的z-index确保在樱花上层 */
.form-container {
  z-index: 1001;
  position: relative;
}

/* 修改查看成员按钮样式 */
.view-members-btn {
  position: absolute;
  right: 20px;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: var(--theme-primary);
  transition: all 0.3s ease;
  padding: 6px 12px;
  border-radius: 15px;
  background: rgba(var(--theme-primary-rgb), 0.1);
  font-family: 'CustomFont', sans-serif;
}

.view-members-btn:hover {
  color: var(--theme-secondary);
  transform: translateY(-50%) translateX(-2px);
  background: rgba(var(--theme-primary-rgb), 0.15);
}

.view-members-btn .el-icon {
  font-size: 16px;
}

/* 主题特定样式 */
[data-theme="sakura"] .title-container h2 {
  text-shadow: 0 0 10px rgba(255, 105, 180, 0.3);
}

[data-theme="sakura"] .view-members-btn:hover {
  text-shadow: 0 0 8px rgba(255, 105, 180, 0.3);
}

/* 暗色模式下的标题和按钮颜色 */
.dark .title-container h2,
.dark .view-members-btn {
  color: var(--theme-text-dark);
}

.dark .view-members-btn:hover {
  color: var(--theme-secondary);
}

/* 暗色模式样式 */
html.dark .app-wrapper {
  background: #1e1e2e; /* 使用纯色背景 */
}

html.dark .form-container {
  background-color: rgba(35, 35, 45, 0.7);
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(10px);
}

html.dark .server-status-container {
  background-color: rgba(35, 35, 45, 0.7);
  border: 1px solid rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
}

html.dark .description {
  color: rgba(255, 255, 255, 0.9);
}

html.dark .server-name {
  color: rgba(255, 255, 255, 0.9);
}

html.dark .player-tag {
  background-color: rgba(64, 158, 255, 0.1);
  border-color: rgba(64, 158, 255, 0.2);
  color: rgba(255, 255, 255, 0.9);
}

html.dark .sakura {
  opacity: 0.3;
  filter: brightness(0.8);
}

/* 修改输入框样式 */
html.dark :deep(.el-input__inner),
html.dark :deep(.el-textarea__inner) {
  background-color: rgba(30, 30, 40, 0.8) !important;
  border-color: rgba(255, 255, 255, 0.1) !important;
  color: rgba(255, 255, 255, 0.9) !important;
  box-shadow: none !important;
}

/* 输入框和文本框的悬停和焦点状态 */
html.dark :deep(.el-input__inner:hover),
html.dark :deep(.el-textarea__inner:hover),
html.dark :deep(.el-input__inner:focus),
html.dark :deep(.el-textarea__inner:focus) {
  border-color: rgba(64, 158, 255, 0.3) !important;
  background-color: rgba(35, 35, 45, 0.9) !important;
}

/* 输入框和文本框的包装器样式 */
html.dark :deep(.el-input__wrapper),
html.dark :deep(.el-textarea__wrapper) {
  background-color: transparent !important;
  box-shadow: none !important;
}

html.dark :deep(.el-radio__label) {
  color: rgba(255, 255, 255, 0.9);
}

html.dark :deep(.el-form-item__label) {
  color: rgba(255, 255, 255, 0.9);
}

/* 赛博朋克主题特殊样式 */
[data-theme="cyberpunk"] .server-status-container {
  background: rgba(15, 10, 42, 0.85);
  border-color: var(--theme-border);
  box-shadow: var(--theme-neon-shadow);
}

[data-theme="cyberpunk"] .header-title,
[data-theme="cyberpunk"] .server-name {
  text-shadow: var(--theme-text-shadow);
}

[data-theme="cyberpunk"] .player-tag {
  background: rgba(0, 255, 221, 0.1);
  border-color: rgba(246, 24, 246, 0.3);
  color: #00ffd5;
  box-shadow: var(--theme-neon-shadow);
  text-shadow: var(--theme-text-shadow);
}

[data-theme="cyberpunk"] .refresh-btn {
  background: rgba(246, 24, 246, 0.2);
  box-shadow: var(--theme-neon-shadow);
}

[data-theme="cyberpunk"] .refresh-btn:hover {
  background: rgba(246, 24, 246, 0.3);
}

/* 暗色模式样式 */
.dark .server-status-container {
  background: var(--theme-bg-dark);
  border-color: var(--theme-border-dark);
}

.dark .server-name {
  color: var(--theme-text-dark);
  background: rgba(255, 255, 255, 0.1);
}

.dark .player-tag {
  background-color: rgba(255, 255, 255, 0.1) !important;
  border-color: rgba(255, 255, 255, 0.2) !important;
  color: rgba(255, 255, 255, 0.9) !important;
}

.dark .player-tag:hover {
  background-color: rgba(255, 255, 255, 0.15) !important;
  box-shadow: 0 4px 12px rgba(255, 255, 255, 0.1);
}

.dark .query-time {
  color: var(--theme-text-dark);
  opacity: 0.6;
}

/* 添加移动端样式 */
@media (max-width: 768px) {
  .server-status-container {
    position: fixed;
    bottom: 20px;
    top: auto;
    right: 50%;
    transform: translateX(50%);
    width: calc(100% - 40px);
    max-width: 400px;
  }

  /* 当表单被点击时，服务器状态面板移到底部并降低透明度 */
  .server-status-container.form-focused {
    opacity: 0.6;
    transform: translateX(50%) translateY(90%);
  }

  .form-container {
    position: relative;
    z-index: 1000;
    transition: all 0.3s ease;
  }

  /* 表单被点击时的效果 */
  .form-container.focused {
    z-index: 1002;
    transform: translateY(0) scale(1.01);
  }

  /* 修改查看成员按钮样式 */
  .view-members-btn {
    right: 15px;
    font-size: 13px;
    padding: 4px 10px;
  }

  .title-container {
    padding: 0 5px;
  }

  .title-container h2 {
    margin-right: 80px;
    font-size: 24px;
  }
}

/* 特小屏幕适配 */
@media (max-width: 360px) {
  .view-members-btn {
    right: 10px;
    font-size: 12px;
    padding: 3px 8px;
  }

  .title-container h2 {
    margin-right: 70px;
    font-size: 22px;
  }
}

/* 主题特定样式 */
[data-theme="sakura"] .player-tag {
  background-color: rgba(255, 105, 180, 0.1);
  border-color: rgba(255, 105, 180, 0.2);
  color: #d4317c;
}

[data-theme="sakura"] .player-tag:hover {
  background-color: rgba(255, 105, 180, 0.2);
  box-shadow: 0 4px 12px rgba(255, 105, 180, 0.2);
  text-shadow: 0 0 8px rgba(255, 105, 180, 0.3);
}

[data-theme="cyberpunk"] .player-tag {
  background: rgba(0, 255, 221, 0.1);
  border-color: rgba(246, 24, 246, 0.3);
  color: #00ffd5;
  box-shadow: var(--theme-neon-shadow);
  text-shadow: var(--theme-text-shadow);
}

[data-theme="cyberpunk"] .player-tag:hover {
  background: rgba(0, 255, 221, 0.2);
  box-shadow: 0 0 15px rgba(0, 255, 221, 0.4);
}

/* 暗色模式样式 */
.dark .player-tag {
  background-color: rgba(255, 255, 255, 0.1);
  border-color: var(--theme-border-dark);
  color: var(--theme-text-dark);
}

.dark .player-tag:hover {
  background-color: rgba(255, 255, 255, 0.15);
  border-color: var(--theme-border-dark);
}

/* 在线玩家标签特殊样式 */
.player-tag.el-tag--success {
  background-color: rgba(var(--theme-secondary-rgb), 0.15);
  border-color: var(--theme-secondary);
  color: var(--theme-secondary);
}

.player-tag.el-tag--success:hover {
  background-color: rgba(var(--theme-secondary-rgb), 0.25);
  box-shadow: 0 4px 12px rgba(var(--theme-secondary-rgb), 0.2);
}

/* 修改森林主题下的标签样式，提高可读性 */
[data-theme="forest"] .player-tag {
  background-color: rgba(255, 255, 255, 0.15);
  border-color: rgba(144, 238, 144, 0.3);
  color: #2c5530; /* 深绿色文字 */
  font-weight: 500; /* 加粗文字 */
}

[data-theme="forest"] .player-tag:hover {
  background-color: rgba(255, 255, 255, 0.25);
  border-color: rgba(144, 238, 144, 0.4);
  box-shadow: 0 4px 12px rgba(144, 238, 144, 0.2);
}

/* 暗色模式下的森林主题 */
.dark[data-theme="forest"] .player-tag {
  background-color: rgba(144, 238, 144, 0.15);
  border-color: rgba(144, 238, 144, 0.3);
  color: #90EE90; /* 亮绿色文字 */
}

.dark[data-theme="forest"] .player-tag:hover {
  background-color: rgba(144, 238, 144, 0.25);
  box-shadow: 0 4px 12px rgba(144, 238, 144, 0.15);
}

/* 添加海洋主题特定样式 */
[data-theme="ocean"] .form-container {
  background: rgba(240, 248, 255, 0.95);
  border-color: rgba(100, 181, 246, 0.3);
  backdrop-filter: blur(20px);
  box-shadow: 0 8px 32px rgba(25, 118, 210, 0.15),
  0 2px 8px rgba(25, 118, 210, 0.1);
}

[data-theme="ocean"] .title-container h2 {
  color: #1976D2;
  text-shadow: 0 0 10px rgba(25, 118, 210, 0.2);
}

[data-theme="ocean"] .description {
  color: #0D47A1;
}

[data-theme="ocean"] .server-status-container {
  background: rgba(240, 248, 255, 0.95);
  border-color: rgba(100, 181, 246, 0.3);
  backdrop-filter: blur(20px);
  box-shadow: 0 8px 32px rgba(25, 118, 210, 0.15),
  0 2px 8px rgba(25, 118, 210, 0.1);
}

[data-theme="ocean"] .server-name {
  color: #1976D2;
  background: rgba(25, 118, 210, 0.1);
}

[data-theme="ocean"] .player-tag {
  background: rgba(3, 169, 244, 0.1);
  border-color: rgba(3, 169, 244, 0.3);
  color: #0277BD;
  font-weight: 500;
}

[data-theme="ocean"] .player-tag:hover {
  background: rgba(3, 169, 244, 0.2);
  box-shadow: 0 4px 12px rgba(3, 169, 244, 0.2);
  text-shadow: 0 0 8px rgba(3, 169, 244, 0.3);
}

[data-theme="ocean"] .header-title {
  color: #1976D2;
}

[data-theme="ocean"] .view-members-btn {
  color: #1976D2;
}

[data-theme="ocean"] .view-members-btn:hover {
  color: #0277BD;
  text-shadow: 0 0 8px rgba(3, 169, 244, 0.3);
}

/* 暗色模式下的海洋主题 */
.dark[data-theme="ocean"] .form-container {
  background: rgba(13, 71, 161, 0.85);
  border-color: rgba(100, 181, 246, 0.2);
  backdrop-filter: blur(20px);
  box-shadow: 0 8px 32px rgba(13, 71, 161, 0.3),
  0 2px 8px rgba(13, 71, 161, 0.2);
}

.dark[data-theme="ocean"] .server-status-container {
  background: rgba(13, 71, 161, 0.85);
  border-color: rgba(100, 181, 246, 0.2);
  backdrop-filter: blur(20px);
  box-shadow: 0 8px 32px rgba(13, 71, 161, 0.3),
  0 2px 8px rgba(13, 71, 161, 0.2);
}

.dark[data-theme="ocean"] .title-container h2 {
  color: #64B5F6;
  text-shadow: 0 0 10px rgba(100, 181, 246, 0.3);
}

.dark[data-theme="ocean"] .description {
  color: #90CAF9;
}

.dark[data-theme="ocean"] .server-name {
  color: #64B5F6;
  background: rgba(100, 181, 246, 0.15);
}

.dark[data-theme="ocean"] .player-tag {
  background: rgba(3, 169, 244, 0.15);
  border-color: rgba(3, 169, 244, 0.3);
  color: #81D4FA;
}

.dark[data-theme="ocean"] .player-tag:hover {
  background: rgba(3, 169, 244, 0.25);
  box-shadow: 0 4px 12px rgba(3, 169, 244, 0.2);
}

.dark[data-theme="ocean"] .header-title {
  color: #64B5F6;
}

.dark[data-theme="ocean"] .view-members-btn {
  color: #64B5F6;
}

.dark[data-theme="ocean"] .view-members-btn:hover {
  color: #81D4FA;
}

/* 添加极光主题特定样式 */
[data-theme="aurora"] .form-container {
  background: rgba(224, 247, 250, 0.95);
  border-color: rgba(77, 208, 225, 0.3);
  backdrop-filter: blur(20px);
  box-shadow: 0 8px 32px rgba(38, 198, 218, 0.15),
  0 2px 8px rgba(38, 198, 218, 0.1);
}

[data-theme="aurora"] .title-container h2 {
  color: #00838F;
  text-shadow: 0 0 15px rgba(38, 198, 218, 0.3);
}

[data-theme="aurora"] .description {
  color: #006064;
}

[data-theme="aurora"] .server-status-container {
  background: rgba(224, 247, 250, 0.95);
  border-color: rgba(77, 208, 225, 0.3);
  backdrop-filter: blur(20px);
  box-shadow: 0 8px 32px rgba(38, 198, 218, 0.15),
  0 2px 8px rgba(38, 198, 218, 0.1);
}

[data-theme="aurora"] .server-name {
  color: #00838F;
  background: rgba(38, 198, 218, 0.1);
}

[data-theme="aurora"] .player-tag {
  background: rgba(38, 198, 218, 0.1);
  border-color: rgba(0, 191, 165, 0.3);
  color: #00838F;
  font-weight: 500;
}

[data-theme="aurora"] .player-tag:hover {
  background: rgba(38, 198, 218, 0.2);
  box-shadow: 0 4px 12px rgba(38, 198, 218, 0.2);
  text-shadow: 0 0 8px rgba(38, 198, 218, 0.3);
}

[data-theme="aurora"] .header-title {
  color: #00838F;
}

[data-theme="aurora"] .view-members-btn {
  color: #00838F;
}

[data-theme="aurora"] .view-members-btn:hover {
  color: #00ACC1;
  text-shadow: 0 0 8px rgba(38, 198, 218, 0.3);
}

/* 暗色模式下的极光主题 */
.dark[data-theme="aurora"] .form-container {
  background: rgba(0, 96, 100, 0.85);
  border-color: rgba(77, 208, 225, 0.2);
  backdrop-filter: blur(20px);
  box-shadow: 0 8px 32px rgba(0, 96, 100, 0.3),
  0 2px 8px rgba(0, 96, 100, 0.2);
}

.dark[data-theme="aurora"] .server-status-container {
  background: rgba(0, 96, 100, 0.85);
  border-color: rgba(77, 208, 225, 0.2);
  backdrop-filter: blur(20px);
  box-shadow: 0 8px 32px rgba(0, 96, 100, 0.3),
  0 2px 8px rgba(0, 96, 100, 0.2);
}

.dark[data-theme="aurora"] .title-container h2 {
  color: #4DD0E1;
  text-shadow: 0 0 15px rgba(77, 208, 225, 0.4);
}

.dark[data-theme="aurora"] .description {
  color: #B2EBF2;
}

.dark[data-theme="aurora"] .server-name {
  color: #4DD0E1;
  background: rgba(77, 208, 225, 0.15);
}

.dark[data-theme="aurora"] .player-tag {
  background: rgba(38, 198, 218, 0.15);
  border-color: rgba(0, 191, 165, 0.3);
  color: #80DEEA;
}

.dark[data-theme="aurora"] .player-tag:hover {
  background: rgba(38, 198, 218, 0.25);
  box-shadow: 0 4px 12px rgba(38, 198, 218, 0.2);
}

.dark[data-theme="aurora"] .header-title {
  color: #4DD0E1;
}

.dark[data-theme="aurora"] .view-members-btn {
  color: #4DD0E1;
}

.dark[data-theme="aurora"] .view-members-btn:hover {
  color: #80DEEA;
}

/* 添加加载状态样式 */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  color: var(--theme-text);
  gap: 10px;
}

.loading-icon {
  font-size: 24px;
  animation: rotate 1s linear infinite;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

/* 添加内容淡入动画 */
.animate-in {
  animation: fadeIn 0.3s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 适配暗色模式 */
.dark .loading-state {
  color: var(--theme-text-dark);
}

/* 主题特定样式 */
[data-theme="sakura"] .loading-state {
  color: var(--theme-primary);
}

[data-theme="cyberpunk"] .loading-state {
  color: #00ffd5;
  text-shadow: var(--theme-text-shadow);
}

[data-theme="ocean"] .loading-state {
  color: #1976D2;
}

.dark[data-theme="ocean"] .loading-state {
  color: #64B5F6;
}

[data-theme="aurora"] .loading-state {
  color: #00838F;
}

.dark[data-theme="aurora"] .loading-state {
  color: #4DD0E1;
}

</style>