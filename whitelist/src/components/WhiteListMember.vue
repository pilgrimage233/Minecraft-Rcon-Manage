<template>
  <div class="app-wrapper">
    <!-- 樱花动画效果 -->
    <div class="sakura-container">
      <span v-for="n in 10" :key="n" :style="{
        '--delay': `${Math.random() * 5}s`,
        '--size': `${Math.random() * 20 + 10}px`,
        '--left': `${Math.random() * 100}%`
      }" class="sakura"></span>
    </div>

    <div class="member-container">
      <div class="title-container">
        <i class="el-icon-user-solid"></i>
        <h2>白名单成员</h2>
        <el-button
            :loading="loading"
            class="refresh-btn"
            size="small"
            @click="getWhiteList(true)"
        >
          <el-icon>
            <Refresh/>
          </el-icon>
        </el-button>
      </div>

      <div class="description">
        以下是已通过白名单审核的玩家列表
      </div>

      <div class="servers-container">
        <div v-for="(members, server) in whitelistData"
             :key="server"
             class="server-block">
          <div class="server-name">
            <i class="el-icon-connection"></i>
            {{ server }}
          </div>
          <div class="members-container">
            <el-tag
                v-for="member in members"
                :key="member"
                class="member-tag"
                effect="light"
                @click="checkMemberDetail(member)"
            >
              {{ member }}
            </el-tag>
          </div>
        </div>
      </div>

      <!-- 添加成员详情弹窗 -->
      <el-dialog
          v-model="dialogVisible"
          :close-on-click-modal="true"
          :show-close="false"
          append-to-body
          class="member-detail-dialog"
          destroy-on-close
          title="白名单详情"
          width="460px"
      >
        <template #header="{ close }">
          <div class="dialog-header">
            <div class="dialog-title">
              <el-icon>
                <User/>
              </el-icon>
              <span>白名单详情</span>
            </div>
            <el-button
                class="close-btn"
                text
                @click="close"
            >
              <el-icon>
                <Close/>
              </el-icon>
            </el-button>
          </div>
        </template>

        <div v-if="memberDetail" class="member-detail">
          <div v-for="(value, key) in memberDetail" :key="key" class="detail-item">
            <span class="detail-label">{{ key }}</span>
            <span :class="{
              'highlight': key === '审核状态',
              'online': key === '账号类型' && value === '正版'
            }" class="detail-value">{{ value }}</span>
          </div>
        </div>

        <template #footer>
          <div class="dialog-footer">
            <el-button
                round
                @click="dialogVisible = false"
            >关闭
            </el-button>
          </div>
        </template>
      </el-dialog>

      <div v-if="lastUpdateTime" class="query-time">
        <i class="el-icon-time"></i>
        最后更新时间：{{ lastUpdateTime }}
      </div>
    </div>
  </div>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue';
import {ElMessage} from 'element-plus';
import axios from 'axios';
import {Close, Refresh, User} from '@element-plus/icons-vue'

const http = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'https://application.shenzhuo.vip',
  timeout: 8000
});

const whitelistData = reactive({});
const loading = ref(false);
const lastUpdateTime = ref('');

// 添加成员详情相关的响应式变量
const dialogVisible = ref(false);
const memberDetail = ref(null);

const getWhiteList = (showMessage = false) => {
  loading.value = true;
  http.get('/mc/whitelist/getWhiteList')
      .then((res) => {
        if (res.data.code === 200) {
          // 清空现有数据
          Object.keys(whitelistData).forEach(key => delete whitelistData[key]);

          // 处理返回的数据
          Object.entries(res.data.data).forEach(([server, membersStr]) => {
            // 处理字符串格式成员列表
            const members = membersStr
                .replace(/^\[|\]$/g, '') // 移除首尾的方括号
                .split(',')
                .map(member => member.trim())
                .filter(member => member); // 过滤空值

            whitelistData[server] = members;
          });

          lastUpdateTime.value = new Date().toLocaleString();
          if (showMessage) {
            ElMessage.success('刷新成功！');
          }
        } else {
          ElMessage.error(res.data.msg || '获取白名单列表失败');
        }
      })
      .catch((error) => {
        console.error('获取白名单列表失败：', error);
        ElMessage.error('获取白名单列表时发生错误，请检查网络或联系管理员');
      })
      .finally(() => {
        loading.value = false;
      });
};

// 查看成员详情
const checkMemberDetail = (memberId) => {
  loading.value = true;
  http.get(`/mc/whitelist/check?id=${memberId}`)
      .then((res) => {
        if (res.data.code === 200) {
          memberDetail.value = res.data.data;
          dialogVisible.value = true;
        } else {
          ElMessage.error(res.data.msg || '获取成员详情失败');
        }
      })
      .catch((error) => {
        console.error('获取成员详情失败：', error);
        ElMessage.error('获取成员详情时发生错误，请检查网络或联系管理员');
      })
      .finally(() => {
        loading.value = false;
      });
};

onMounted(() => {
  getWhiteList();
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
      #e0e0ff,
      #ffd6e7,
      #ffdfd0,
      #fff4d1,
      #d4ffe6,
      #cce9ff,
      #f0e6ff,
      #ffe6f0,
      #ffe6e6
  );
  animation: warmGradient 20s ease infinite;
  padding: 20px;
}

.member-container {
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(8px);
  padding: 30px;
  border-radius: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  width: min(100%, 800px);
  transform: translateY(0);
  transition: all 0.3s ease;
  position: relative;
  z-index: 1001;
}

.member-container:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.title-container {
  text-align: center;
  margin-bottom: 25px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.title-container h2 {
  color: #409EFF;
  font-size: 28px;
  margin: 0;
}

.description {
  text-align: center;
  color: #666;
  margin-bottom: 25px;
  font-size: 14px;
}

.servers-container {
  display: grid;
  gap: 20px;
  margin-top: 20px;
}

.server-block {
  background: rgba(255, 255, 255, 0.5);
  border-radius: 12px;
  padding: 20px;
  transition: all 0.3s ease;
  border: 1px solid rgba(64, 158, 255, 0.2);
}

.server-block:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}

.server-name {
  font-weight: 500;
  color: #409EFF;
  margin-bottom: 15px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
}

.members-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.member-tag {
  transition: all 0.3s ease;
  padding: 6px 12px;
  border-radius: 8px;
  background-color: rgba(64, 158, 255, 0.1);
  border: 1px solid rgba(64, 158, 255, 0.2);
  color: #409EFF;
  cursor: pointer;
}

.member-tag:hover {
  transform: translateY(-2px);
  background-color: rgba(64, 158, 255, 0.2);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
}

.query-time {
  margin-top: 20px;
  text-align: center;
  color: #909399;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.refresh-btn {
  padding: 6px;
  height: 32px;
  width: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.3s ease;
  background: rgba(64, 158, 255, 0.1);
  border: none;
  color: #409EFF;
}

.refresh-btn:hover {
  transform: rotate(180deg);
  background: rgba(64, 158, 255, 0.2);
}

/* 保留樱花动画相关样式 */
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

/* 更新弹窗相关样式 */
:deep(.member-detail-dialog) {
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.member-detail-dialog .el-dialog) {
  margin: 0 auto !important;
  position: relative;
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.15);
  animation: dialogFadeIn 0.3s ease-out;
  max-height: 90vh;
}

:deep(.member-detail-dialog .el-dialog__body) {
  padding: 0;
  margin: 0;
  max-height: calc(90vh - 120px);
  overflow-y: auto;
}

/* 自定义滚动条样式 */
:deep(.member-detail-dialog .el-dialog__body::-webkit-scrollbar) {
  width: 8px;
}

:deep(.member-detail-dialog .el-dialog__body::-webkit-scrollbar-thumb) {
  background: rgba(64, 158, 255, 0.3);
  border-radius: 4px;
}

:deep(.member-detail-dialog .el-dialog__body::-webkit-scrollbar-thumb:hover) {
  background: rgba(64, 158, 255, 0.5);
}

/* 确保遮罩层覆盖整个视口 */
:deep(.member-detail-dialog .el-overlay) {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
}

/* 优化动画效果 */
@keyframes dialogFadeIn {
  from {
    opacity: 0;
    transform: translateY(-30px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* 调整内容区域的样式 */
.member-detail {
  padding: 24px;
  display: grid;
  gap: 14px;
  overflow-x: hidden;
  background: rgba(255, 255, 255, 0.5);
}

/* 优化移动端显示 */
@media screen and (max-width: 768px) {
  :deep(.member-detail-dialog .el-dialog) {
    width: 90% !important;
    margin: 0 auto !important;
  }

  .detail-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }

  .detail-value {
    width: 100%;
    text-align: left;
  }
}

.dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  background: linear-gradient(135deg, #409EFF, #36cfc9);
  position: relative;
}

.dialog-title {
  display: flex;
  align-items: center;
  gap: 8px;
  color: white;
  font-size: 18px;
  font-weight: 500;
}

.dialog-title .el-icon {
  font-size: 20px;
}

.close-btn {
  color: white;
  transition: all 0.3s ease;
  width: 32px;
  height: 32px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.1);
}

.close-btn:hover {
  transform: rotate(90deg);
  background: rgba(255, 255, 255, 0.2);
}

.member-detail {
  padding: 24px;
  display: grid;
  gap: 14px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 20px;
  background: #f8f9fa;
  border-radius: 16px;
  transition: all 0.3s ease;
  border: 1px solid rgba(64, 158, 255, 0.1);
}

.detail-item:hover {
  background: #f0f7ff;
  transform: translateX(5px);
  border-color: rgba(64, 158, 255, 0.2);
}

.detail-label {
  color: #606266;
  font-weight: 500;
  min-width: 80px;
}

.detail-value {
  color: #409EFF;
  text-align: right;
  font-weight: 500;
}

.detail-value.highlight {
  color: #67C23A;
}

.detail-value.online {
  color: #E6A23C;
}

.dialog-footer {
  padding: 10px 24px 24px;
  text-align: center;
}

:deep(.dialog-footer .el-button) {
  min-width: 120px;
  height: 40px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  border-radius: 20px;
  background: linear-gradient(135deg, #409EFF, #36cfc9);
  border: none;
  color: white;
}

:deep(.dialog-footer .el-button:hover) {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(64, 158, 255, 0.25);
  opacity: 0.9;
}

:deep(.member-detail-dialog .el-dialog__header) {
  padding: 0;
  margin: 0;
}

:deep(.member-detail-dialog .el-dialog__headerbtn) {
  display: none;
}

:deep(.member-detail-dialog .el-dialog__footer) {
  padding: 0;
  margin: 0;
}

/* 添加标题栏装饰效果 */
.dialog-header::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
  height: 3px;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 2px;
}
</style>
