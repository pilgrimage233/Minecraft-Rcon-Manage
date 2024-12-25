<template>
  <div class="app-wrapper">
    <div class="server-status-container">
      <div class="status-header">
        <div class="header-title">
          <i class="el-icon-monitor"></i>
          <span>服务器状态</span>
          <el-button
              :loading="loading"
              class="refresh-btn"
              size="small"
              @click="getOnlinePlayer(true)"
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
          <el-button v-loading.fullscreen.lock="loading"
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
import {onMounted, reactive} from 'vue';
import {ElMessage} from 'element-plus';
import axios from 'axios';
import {Refresh} from '@element-plus/icons-vue'

const http = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'https://application.shenzhuo.vip', // 使用环境变量
  timeout: 5000
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

const submitForm = () => {
  if (!form.userName || !form.qqNum || !form.onlineFlag) {
    ElMessage.error('请填写完整信息');
  } else if (!/^\d{5,11}$/.test(form.qqNum)) {
    ElMessage.error('QQ号格式错误');
  } else {
    loading = true;
    // 先获取用户IP
    fetch('https://api.ipify.org?format=json')
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
          loading = false;
        })
        .catch((error) => {
          console.error('提交表单请求出错：', error);
          ElMessage.error('提交表单时发生错误，请检查网络或联系管理员');
          loading = false;
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
      #e6c3ff, /* 淡紫色 */ #ffcad4, /* 淡粉色 */ #ffd4bc, /* 淡橘色 */ #fff3b2 /* 淡黄色 */, #c3ffec /* 淡绿色 */, #c3f3ff /* 淡蓝色 */
  );
  animation: warmGradient 7s ease infinite;
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
}

.submit-btn:hover {
  transform: scale(1.05);
}

:deep(.el-input__inner) {
  border-radius: 8px;
}

:deep(.el-textarea__inner) {
  border-radius: 8px;
  min-height: 100px;
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
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(8px);
  border-radius: 12px;
  padding: 15px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  z-index: 1000;
}

.server-status-container:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
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
}

.server-name {
  font-weight: 500;
  color: #303133;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
}

.server-name i {
  margin-right: 8px;
  color: #409EFF;
  flex-shrink: 0;
}

.online-info {
  padding-left: 8px;
}

.player-list {
  margin-top: 8px;
}

.players-label {
  color: #606266;
  font-size: 13px;
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.players-label i {
  margin-right: 8px;
  flex-shrink: 0;
}

.players-container {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.player-tag {
  font-size: 12px;
  margin: 0;
}

.query-time {
  margin-top: 12px;
  padding-top: 8px;
  border-top: 1px dashed rgba(0, 0, 0, 0.1);
  text-align: center;
}

.query-time i {
  margin-right: 8px;
  flex-shrink: 0;
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

.server-block {
  padding: 12px 0;
  margin-bottom: 12px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
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
</style>