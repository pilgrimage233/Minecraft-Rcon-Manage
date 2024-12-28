<template>
  <div class="app-wrapper">
    <div class="sakura-container">
      <span v-for="n in 10" :key="n" :style="{
        '--delay': `${Math.random() * 5}s`,
        '--size': `${Math.random() * 20 + 10}px`,
        '--left': `${Math.random() * 100}%`
      }" class="sakura"></span>
    </div>
    <div class="server-status-container">
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
        <div v-for="server in serverStatus.servers"
             :key="server.name"
             class="server-block">
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
        <div class="query-time">
          <i class="el-icon-time"></i>
          {{ serverStatus.queryTime }}
        </div>
      </div>
    </div>

    <div class="form-container">
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
          查看白名单成员
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
import {Refresh, User} from '@element-plus/icons-vue'
import { debounce } from 'lodash-es';

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

const submitForm = () => {
  if (!form.userName || !form.qqNum || !form.onlineFlag) {
    ElMessage.error('请填写完整信息');
  } else if (!/^\d{5,11}$/.test(form.qqNum)) {
    ElMessage.error('QQ号格式错误');
  } else {
    fullscreenLoading.value = true;  // 使用新的loading状态
    // 先获取用户IP
    fetch('https://ip.useragentinfo.com/json')
        .then(response => response.json())
        .then(data => {
          // 发送表单请求，带上IP信息
          return http.post('/mc/whitelist/apply', form, {
            headers: {
              'X-Real-IP': data.ip
            }
          });
        })
        .then((res) => {
          if (res.data.code === 200) {
            ElMessage.success(res.data.msg);
          } else {
            ElMessage.error(res.data.msg || '未知错误，请联系管理员');
          }
          fullscreenLoading.value = false;  // 关闭loading
        })
        .catch((error) => {
          console.error('提交表单请求出错：', error);
          ElMessage.error('提交表单时发生错误，请检查网络或联系管理员');
          fullscreenLoading.value = false;  // 关闭loading
        });
  }
};

const getOnlinePlayer = (reflash) => {
  loading = true;
  http.get('/server/serverlist/getOnlinePlayer').then((res) => {
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
  }).catch((error) => {
    console.error('查询在线玩家请求出错：', error);
    ElMessage.error('查询在线玩家时发生错误，请检查网络或联系管理员');
    loading = false;
  });
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
  background-image: linear-gradient(
      -45deg,
      #e0e0ff, /* 淡紫色 */ #ffd6e7, /* 玫瑰粉 */ #ffdfd0, /* 暖橘色 */ #fff4d1, /* 温暖黄 */ #d4ffe6, /* 薄荷绿 */ #cce9ff, /* 天空蓝 */ #f0e6ff, /* 浅紫色 */ #ffe6f0, /* 浅粉色 */ #ffe6e6 /* 浅珊瑚色 */
  );
  animation: warmGradient 20s ease infinite;
  padding: 20px;
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
  background-color: rgba(255, 255, 255, 0.92);
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
}

.title-container h2 {
  color: #409EFF;
  font-size: 28px;
  margin: 10px 0;
}

.description {
  text-align: center;
  color: #666;
  margin-bottom: 25px;
  font-size: 14px;
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
  color: #606266;
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
  top: 20px;
  right: 20px;
  width: 280px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 12px;
  padding: 15px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  z-index: 1001;
  overflow: hidden;
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
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
}

.header-title {
  display: flex;
  align-items: center;
  font-weight: 500;
  color: #409EFF;
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
  color: #303133;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  font-size: 15px;
  padding: 4px 8px;
  background: rgba(64, 158, 255, 0.1);
  border-radius: 6px;
  width: fit-content;
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
  transition: all 0.3s ease;
  border-radius: 10px;
  padding: 2px 10px;
  background-color: rgba(103, 194, 58, 0.1);
  border: 1px solid rgba(103, 194, 58, 0.2);
  color: #67C23A;
  font-size: 12px;
  line-height: 1.4;
  white-space: nowrap;
  display: inline-block;
}

.player-tag:hover {
  background-color: rgba(103, 194, 58, 0.2);
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(103, 194, 58, 0.1);
}

.query-time {
  font-size: 12px;
  color: #909399;
  padding: 8px 12px;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 8px;
  margin-top: 15px;
  display: flex;
  align-items: center;
  justify-content: center;
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

/* 添加查看成员按钮样式 */
.view-members-btn {
  position: absolute;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #409EFF;
  transition: all 0.3s ease;
}

.view-members-btn:hover {
  color: #66b1ff;
  transform: translateY(-50%) translateX(-2px);
}

.view-members-btn .el-icon {
  font-size: 16px;
}
</style>