<template>
  <div class="app-wrapper">
    <SakuraBackground
        :isDark="isDark"
        :currentTheme="currentTheme"
    />
    <!-- 樱花动画效果 -->
    <!-- <div class="sakura-container">
      <span v-for="n in 10" :key="n" :style="{
        '--delay': `${Math.random() * 5}s`,
        '--size': `${Math.random() * 20 + 10}px`,
        '--left': `${Math.random() * 100}%`
      }" class="sakura"></span>
    </div> -->

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

      <!-- 添加加载状态 -->
      <div v-if="initialLoading" class="loading-state">
        <el-icon class="loading-icon">
          <Loading/>
        </el-icon>
        <span>加载中...</span>
      </div>

      <!-- 使用 v-else 包裹现有内容 -->
      <template v-else>
        <div class="servers-container">
          <div v-for="(members, server) in whitelistData"
               :key="server"
               class="server-block animate-in">
            <div class="server-name">
              <i class="el-icon-connection"></i>
              {{ server }}
            </div>
            <div class="members-container">
              <el-tag
                  v-for="member in members"
                  :key="member"
                  :type="onlinePlayers.has(member) ? 'success' : null"
                  class="member-tag"
                  effect="light"
                  @click="checkMemberDetail(member)"
              >
                {{ member }}
                <span v-if="onlinePlayers.has(member)" class="online-dot"></span>
              </el-tag>
            </div>
          </div>
        </div>

        <div v-if="lastUpdateTime" class="query-time animate-in">
          <i class="el-icon-time"></i>
          最后更新时间：{{ lastUpdateTime }}
        </div>
      </template>

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
            <el-popover
                v-if="key === 'QQ号'"
                :width="120"
                placement="right"
                popper-class="qq-avatar-popover"
                trigger="hover"
            >
              <template #reference>
                <span :class="{
                  'highlight': key === '审核状态',
                  'online': key === '账号类型' && value === '正版'
                }" class="detail-value">{{ value }}</span>
              </template>
              <div class="qq-avatar-container">
                <img
                    :alt="`QQ: ${value}`"
                    :src="`https://q1.qlogo.cn/g?b=qq&nk=${value}&s=640`"
                    class="qq-avatar"
                />
              </div>
            </el-popover>
            <el-popover
                v-else-if="key === '游戏ID' && memberDetail['账号类型'] === '正版'"
                :width="200"
                placement="right"
                popper-class="skin-viewer-popover"
                trigger="hover"
                @show="loadAndRenderSkin(value)"
            >
              <template #reference>
                <span class="detail-value minecraft-id">{{ value }}</span>
              </template>
              <div class="skin-viewer-container">
                <canvas
                    id="skin-viewer-canvas"
                    ref="skinViewerContainer"
                    class="skin-viewer"
                    height="240"
                    width="180"
                ></canvas>
                <div v-if="!loadingSkin && !skinLoadError" class="skin-viewer-controls">
                  <el-button-group>
                    <el-button
                        :type="isAnimating ? 'primary' : 'default'"
                        size="small"
                        title="播放/暂停"
                        @click="toggleAnimation"
                    >
                      <el-icon>
                        <VideoPlay v-if="!isAnimating"/>
                        <VideoPause v-else/>
                      </el-icon>
                    </el-button>
                    <el-button
                        :type="isWalking ? 'primary' : 'default'"
                        size="small"
                        title="行走动画"
                        @click="toggleWalk"
                    >
                      <el-icon>
                        <Position/>
                      </el-icon>
                    </el-button>
                    <el-button
                        size="small"
                        title="截图"
                        @click="takeScreenshot"
                    >
                      <el-icon>
                        <Camera/>
                      </el-icon>
                    </el-button>
                    <el-button
                        size="small"
                        title="重置视角"
                        @click="resetView"
                    >
                      <el-icon>
                        <Refresh/>
                      </el-icon>
                    </el-button>
                  </el-button-group>
                </div>
                <div v-if="loadingSkin" class="skin-viewer-loading">
                  <el-progress
                      :percentage="loadingProgress"
                      :show-text="false"
                      :stroke-width="4"
                      :width="60"
                      type="circle"
                  >
                    <template #default>
                      <span class="progress-text">{{ loadingProgress }}%</span>
                    </template>
                  </el-progress>
                </div>
                <div v-if="skinLoadError" class="skin-viewer-error">
                  <el-icon>
                    <Warning/>
                  </el-icon>
                  <span>{{ skinLoadError }}</span>
                  <el-button
                      link
                      size="small"
                      type="primary"
                      @click="retryLoadSkin"
                  >重试
                  </el-button>
                </div>
              </div>
            </el-popover>
            <span
                v-else
                :class="{
                'highlight': key === '审核状态',
                'online': key === '账号类型' && value === '正版'
              }"
                class="detail-value"
            >{{ value }}</span>
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
    </div>
  </div>
</template>

<script setup>
import {nextTick, onMounted, onUnmounted, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import axios from 'axios';
import {Camera, Close, Loading, Position, Refresh, User, VideoPause, VideoPlay, Warning} from '@element-plus/icons-vue';
// 导入 skinview3d
import * as skinview3d from 'skinview3d';
import SakuraBackground from './common/SakuraBackground.vue'

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

const skinViewerContainer = ref(null);
const loadingSkin = ref(false);
let skinViewer = null;

// 添加错误状态
const skinLoadError = ref(null);
const currentUsername = ref(null);

// 添加新的响应式状态
const isAnimating = ref(true);
const isWalking = ref(true);
const loadingProgress = ref(0);

// 在 script setup 顶部添加缓存相关的变量
const skinCache = new Map(); // 用于缓存皮肤数据
const skinViewerCache = new Map(); // 用于缓存皮肤查看器实例
let currentAnimationFrame = null; // 用于跟踪当前动画帧

// 添加在线玩家状态的响应式变量
const onlinePlayers = ref(new Set());

// 获取当前主题
const currentTheme = ref(localStorage.getItem('theme') || 'default')

// 添加初始加载状态
const initialLoading = ref(true);

const getWhiteList = (showMessage = false) => {
  loading.value = true;
  Promise.all([
    http.get('/api/v1/getWhiteList'),
    http.get('/api/v1/getOnlinePlayer')
  ])
      .then(([whitelistRes, onlineRes]) => {
        if (whitelistRes.data.code === 200) {
          // 处理白名单数据
          Object.keys(whitelistData).forEach(key => delete whitelistData[key]);
          Object.entries(whitelistRes.data.data).forEach(([server, membersStr]) => {
            const members = membersStr
                .replace(/^\[|\]$/g, '')
                .split(',')
                .map(member => member.trim())
                .filter(member => member);
            whitelistData[server] = members;
          });
        }

        // 处理在线玩家数据
        if (onlineRes.data.code === 200) {
          const onlineData = onlineRes.data.data;
          const onlineSet = new Set();

          // 遍历所有服务器的在线玩家
          Object.entries(onlineData).forEach(([serverName, serverData]) => {
            if (serverName !== '查询时间' && serverData['在线玩家']) {
              const players = serverData['在线玩家']
                  .replace(/^\[|\]$/g, '')
                  .split(',')
                  .map(p => p.trim())
                  .filter(p => p);
              players.forEach(player => onlineSet.add(player));
            }
          });

          onlinePlayers.value = onlineSet;
        }

        lastUpdateTime.value = new Date().toLocaleString();
        if (showMessage) {
          ElMessage.success('刷新成功');
        }
      })
      .catch((error) => {
        console.error('获取数据失败：', error);
        ElMessage.error('获取数据时发生错误，请检查网络或联系管理员');
      })
      .finally(() => {
        loading.value = false;
        initialLoading.value = false; // 设置初始加载状态为 false
      });
};

// 查看员详情
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

// 重试加载功能
const retryLoadSkin = () => {
  if (currentUsername.value) {
    loadAndRenderSkin(currentUsername.value);
  }
};

// 修改加载函数
const loadAndRenderSkin = async (username) => {
  try {
    // 检查缓存中是否已有该玩家的皮肤数据
    if (skinCache.has(username)) {
      console.log('Using cached skin data for:', username);
      const cachedData = skinCache.get(username);
      await renderSkin(username, cachedData);
      return;
    }

    skinLoadError.value = null;
    loadingSkin.value = true;
    loadingProgress.value = 0;
    currentUsername.value = username;

    console.log('Loading skin for:', username);

    const response = await http.get(`/mojang/user/${username}`);
    console.log('Mojang API response:', response.data);

    if (response.data.code === 200) {
      const skinData = response.data.data;
      // 将皮肤数据存入缓存
      skinCache.set(username, skinData);
      await renderSkin(username, skinData);
    } else {
      skinLoadError.value = response.data.msg || '获取皮肤数据失败';
      throw new Error(response.data.msg || '获取皮肤数据失败');
    }
  } catch (error) {
    console.error('加载皮肤失败：', error);
    skinLoadError.value = '无法加载玩家皮肤';
    ElMessage.error('无法加载玩家皮肤');
  } finally {
    loadingSkin.value = false;
  }
};

// 抽离渲染逻辑为单独的函数
const renderSkin = async (username, skinData) => {
  await nextTick();

  const canvas = document.getElementById('skin-viewer-canvas');
  if (!canvas) return;

  try {
    // 停止当前的动画循环
    if (currentAnimationFrame) {
      cancelAnimationFrame(currentAnimationFrame);
      currentAnimationFrame = null;
    }

    // 如果存在当前的查看器，先清理它
    if (skinViewer) {
      skinViewer.renderPaused = true;
      if (skinViewer.animation) {
        skinViewer.animation.paused = true;
      }
    }

    // 创建新的查看器实例
    const viewer = new skinview3d.SkinViewer({
      canvas: canvas,
      width: 180,
      height: 240,
      renderPaused: true
    });

    // 加载皮肤
    const baseUrl = import.meta.env.VITE_API_URL || 'https://application.shenzhuo.vip';
    const skinUrl = `${baseUrl}/mojang/texture?url=${encodeURIComponent(skinData.skin.url)}`;

    const loadingPromise = viewer.loadSkin(skinUrl);
    loadingPromise.onProgress = (progress) => {
      loadingProgress.value = Math.round(progress * 100);
    };
    await loadingPromise;

    // 设置模型类型
    viewer.playerObject.skin.modelType = skinData.skin?.metadata?.model === 'slim' ? 'slim' : 'default';

    // 设置相机
    viewer.camera.position.set(30, 0, -40);
    viewer.camera.lookAt(0, 0, 0);

    // 设置动画
    viewer.animation = new skinview3d.WalkingAnimation();
    viewer.animation.speed = 0.6;
    viewer.animation.paused = !isWalking.value;

    // 设置旋转动画
    let rotation = 0;
    const animate = () => {
      if (viewer && !viewer.renderPaused) {
        if (isAnimating.value) {
          rotation += 0.01;
          viewer.playerObject.rotation.y = rotation;
        }
        viewer.render();
        currentAnimationFrame = requestAnimationFrame(animate);
      }
    };

    // 存入缓存并设置为当前查看器
    skinViewerCache.set(username, {
      viewer,
      rotation,
      animate
    });

    skinViewer = viewer;
    viewer.renderPaused = false;
    animate();

  } catch (error) {
    console.error('皮肤渲染初始化失败：', error);
    skinLoadError.value = '皮肤加载失败';
    throw new Error('皮肤渲染初始化失败');
  }
};

// 修改清理函数
const cleanupViewer = () => {
  if (currentAnimationFrame) {
    cancelAnimationFrame(currentAnimationFrame);
    currentAnimationFrame = null;
  }

  if (skinViewer) {
    try {
      skinViewer.renderPaused = true;
      if (skinViewer.animation) {
        skinViewer.animation.paused = true;
      }
    } catch (e) {
      console.error('暂停皮肤查看器失败：', e);
    }
  }
};

// 修改弹窗监听
watch(dialogVisible, (newVal) => {
  if (!newVal) {
    cleanupViewer();
  }
});

// 修改组件卸载时的清理
onUnmounted(() => {
  cleanupViewer();

  // 清理所有缓存的查看器实例
  skinViewerCache.forEach(({viewer}) => {
    try {
      viewer.dispose();
    } catch (e) {
      console.error('清理缓存的查看器实例失败：', e);
    }
  });

  skinViewerCache.clear();
  skinCache.clear();
});

// 修改动画控制函数
const toggleAnimation = () => {
  isAnimating.value = !isAnimating.value;
};

const toggleWalk = () => {
  if (skinViewer?.animation) {
    isWalking.value = !isWalking.value;
    skinViewer.animation.paused = !isWalking.value;
  }
};

// 修改重置视角函数
const resetView = () => {
  if (skinViewer) {
    skinViewer.camera.position.set(30, 0, -40);
    skinViewer.camera.lookAt(0, 0, 0);
  }
};

// 修改截图功能
const takeScreenshot = () => {
  if (skinViewer) {
    try {
      // 创建一个临时画布
      const tempCanvas = document.createElement('canvas');
      tempCanvas.width = 180;
      tempCanvas.height = 240;
      const ctx = tempCanvas.getContext('2d');

      // 确保渲染最新的帧
      skinViewer.render();

      // 将 WebGL 画布内容复制到临时画布
      ctx.drawImage(skinViewer.canvas, 0, 0);

      // 添加半透明的渐变背景
      const gradient = ctx.createLinearGradient(0, 0, 180, 240);
      gradient.addColorStop(0, 'rgba(26, 26, 26, 0.8)');
      gradient.addColorStop(1, 'rgba(44, 44, 44, 0.8)');

      // 保存当前状态
      ctx.save();
      // 设置混合模式
      ctx.globalCompositeOperation = 'destination-over';
      // 绘制背景
      ctx.fillStyle = gradient;
      ctx.fillRect(0, 0, 180, 240);
      // 恢复状态
      ctx.restore();

      // 添加水印
      ctx.fillStyle = 'rgba(255, 255, 255, 0.3)';
      ctx.font = '10px Arial';
      ctx.textAlign = 'right';
      ctx.fillText(`${currentUsername.value} - ${new Date().toLocaleDateString()}`, 170, 230);

      // 获取图片数据
      const dataUrl = tempCanvas.toDataURL('image/png');

      // 创建下载链接
      const link = document.createElement('a');
      link.download = `${currentUsername.value}_skin_${Date.now()}.png`;
      link.href = dataUrl;
      link.click();

      ElMessage.success('截图已保存');
    } catch (error) {
      console.error('截图失败：', error);
      ElMessage.error('截图失败');
    }
  }
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
  font-family: 'CustomFont', sans-serif;
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
  color: var(--theme-primary);
  font-size: 28px;
  margin: 0;
  text-shadow: var(--theme-text-shadow);
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
  padding: 16px;
  transition: all 0.3s ease;
  border: 1px solid rgba(64, 158, 255, 0.2);
  margin-bottom: 12px;
}

.server-block:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}

.server-name {
  font-weight: 500;
  color: var(--theme-primary);
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
}

.members-container {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  padding: 4px;
}

.member-tag {
  background-color: rgba(var(--theme-secondary-rgb), 0.1);
  border: 1px solid var(--theme-border);
  color: var(--theme-text);
  padding: 3px 10px;
  border-radius: 10px;
  font-size: 12px;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin: 2px;
  height: 24px;
  line-height: 1;
}

.member-tag:hover {
  background-color: rgba(var(--theme-primary-rgb), 0.15);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(var(--theme-primary-rgb), 0.15);
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

/* 保遮罩层覆盖整个视口 */
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
  background: rgba(var(--theme-primary), 0.05);
  border-radius: 16px;
  transition: all 0.3s ease;
  border: 1px solid rgba(var(--theme-primary), 0.1);
  position: relative;
}

.detail-item:not(:last-child)::after {
  content: '';
  position: absolute;
  bottom: -8px;
  left: 50%;
  transform: translateX(-50%);
  width: calc(100% - 40px);
  height: 1px;
  background: linear-gradient(
      to right,
      transparent,
      var(--theme-primary),
      transparent
  );
  opacity: 0.2;
}

.dark .detail-item:not(:last-child)::after {
  background: linear-gradient(
      to right,
      transparent,
      rgba(255, 255, 255, 0.2),
      transparent
  );
}

[data-theme="cyberpunk"] .detail-item:not(:last-child)::after {
  background: linear-gradient(
      to right,
      transparent,
      var(--theme-secondary),
      transparent
  );
  box-shadow: 0 0 10px var(--theme-secondary);
  opacity: 0.3;
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

/* 添加QQ头像关样式 */
.qq-avatar-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 4px;
}

.qq-avatar {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  object-fit: cover;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
}

.qq-avatar:hover {
  transform: scale(1.05);
}

/* 自定义popover样式 */
:deep(.qq-avatar-popover) {
  padding: 8px;
  border-radius: 12px;
  border: none;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(8px);
}

/* 为QQ号添加特殊样式 */
.detail-value:has(+ .el-popover) {
  cursor: pointer;
  text-decoration: underline;
  text-decoration-style: dotted;
  text-decoration-color: #409EFF;
  text-underline-offset: 4px;
}

.detail-value:has(+ .el-popover):hover {
  color: #66b1ff;
}

/* 移动端适配 */
@media screen and (max-width: 768px) {
  :deep(.qq-avatar-popover) {
    max-width: 90vw;
  }

  .qq-avatar {
    width: 80px;
    height: 80px;
  }
}

/* 添加 Minecraft 皮肤查看器相关样式 */
.skin-viewer {
  width: 100%;
  height: 100%;
  background: transparent;
  display: block;
}

.skin-viewer-container {
  position: relative;
  width: 180px;
  height: 240px;
  background: linear-gradient(135deg, #1a1a1a, #2c2c2c);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: inset 0 0 20px rgba(0, 0, 0, 0.3);
}

.skin-viewer-loading {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  font-size: 14px;
  backdrop-filter: blur(4px);
}

:deep(.skin-viewer-popover) {
  padding: 12px;
  border-radius: 16px;
  border: none;
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.3);
  background: rgba(26, 26, 26, 0.95);
  backdrop-filter: blur(12px);
}

.minecraft-id {
  cursor: pointer;
  text-decoration: underline;
  text-decoration-style: dotted;
  text-decoration-color: #E6A23C;
  text-underline-offset: 4px;
  transition: all 0.3s ease;
}

.minecraft-id:hover {
  color: #E6A23C;
  text-shadow: 0 0 8px rgba(230, 162, 60, 0.3);
}

/* 移动端适配 */
@media screen and (max-width: 768px) {
  :deep(.skin-viewer-popover) {
    max-width: 90vw;
  }

  .skin-viewer-container {
    width: 150px;
    height: 200px;
  }
}

.skin-viewer-error {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  background: rgba(0, 0, 0, 0.7);
  color: #f56c6c;
  font-size: 14px;
  backdrop-filter: blur(4px);
}

.skin-viewer-error .el-icon {
  font-size: 24px;
  margin-bottom: 4px;
}

/* 添加控制按钮样式 */
.skin-viewer-controls {
  position: absolute;
  bottom: 12px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 8px;
  z-index: 1;
  background: rgba(0, 0, 0, 0.5);
  padding: 6px;
  border-radius: 8px;
  backdrop-filter: blur(8px);
}

.skin-viewer-controls .el-button {
  padding: 6px;
  border: none;
  background: transparent;
  color: white;
}

.skin-viewer-controls .el-button:hover {
  background: rgba(255, 255, 255, 0.1);
}

.skin-viewer-controls .el-button.el-button--primary {
  background: rgba(64, 158, 255, 0.2);
}

.progress-text {
  font-size: 12px;
  color: white;
}

/* 优化加载动 */
.skin-viewer-loading {
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(8px);
}

:deep(.el-progress-circle) {
  --el-progress-color: #409EFF;
}

/* 添加在线状态小圆点的样式 */
.online-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  margin-left: 3px;
  background-color: var(--theme-secondary);
  display: inline-block;
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

/* 修改成员标签的悬停效果 */
.member-tag {
  transition: all 0.3s ease;
  padding: 6px 12px;
  border-radius: 8px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
}

.member-tag:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
}

/* 在线玩家标签的特殊样式 */
.member-tag.el-tag--success {
  background-color: rgba(var(--theme-secondary-rgb), 0.15);
  border-color: var(--theme-secondary);
  color: var(--theme-secondary);
}

.member-tag.el-tag--success:hover {
  background-color: rgba(var(--theme-secondary-rgb), 0.25);
  box-shadow: 0 4px 12px rgba(var(--theme-secondary-rgb), 0.2);
}

/* 在 style 标签内添加 */
.dark .member-container {
  background-color: rgba(35, 35, 45, 0.7);
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(10px);
}

.dark .server-block {
  background-color: rgba(30, 30, 40, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.dark .description {
  color: rgba(255, 255, 255, 0.7);
}

.dark .member-tag {
  background-color: rgba(255, 255, 255, 0.1);
  border-color: var(--theme-border-dark);
  color: var(--theme-text-dark);
}

.dark .dialog-header {
  background: rgba(30, 30, 40, 0.95);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.dark .member-detail {
  background-color: rgba(30, 30, 40, 0.6);
}

.dark .detail-item {
  background-color: rgba(35, 35, 45, 0.6);
  border-color: rgba(255, 255, 255, 0.1);
}

.dark .detail-label {
  color: rgba(255, 255, 255, 0.7);
}

.dark .detail-value {
  color: rgba(255, 255, 255, 0.9);
}

.server-block:not(:last-child) {
  position: relative;
  margin-bottom: 20px;
}

.server-block:not(:last-child)::after {
  content: '';
  position: absolute;
  bottom: -10px;
  left: 50%;
  transform: translateX(-50%);
  width: calc(100% - 40px);
  height: 1px;
  background: linear-gradient(
      to right,
      transparent,
      var(--theme-primary),
      transparent
  );
  opacity: 0.2;
}

.dark .server-block:not(:last-child)::after {
  background: linear-gradient(
      to right,
      transparent,
      rgba(255, 255, 255, 0.2),
      transparent
  );
}

[data-theme="cyberpunk"] .server-block:not(:last-child)::after {
  background: linear-gradient(
      to right,
      transparent,
      var(--theme-secondary),
      transparent
  );
  box-shadow: 0 0 10px var(--theme-secondary);
  opacity: 0.3;
}

/* 主题特定样式 */
[data-theme="sakura"] .member-tag {
  background-color: rgba(255, 105, 180, 0.1);
  border-color: rgba(255, 105, 180, 0.2);
  color: #d4317c;
}

[data-theme="sakura"] .member-tag:hover {
  background-color: rgba(255, 105, 180, 0.2);
  box-shadow: 0 4px 12px rgba(255, 105, 180, 0.2);
  text-shadow: 0 0 8px rgba(255, 105, 180, 0.3);
}

[data-theme="cyberpunk"] .member-tag {
  background: rgba(0, 255, 221, 0.1);
  border-color: rgba(246, 24, 246, 0.3);
  color: #00ffd5;
  box-shadow: var(--theme-neon-shadow);
  text-shadow: var(--theme-text-shadow);
}

[data-theme="cyberpunk"] .member-tag:hover {
  background: rgba(0, 255, 221, 0.2);
  box-shadow: 0 0 15px rgba(0, 255, 221, 0.4);
}

/* 暗色模式样式 */
.dark .member-tag {
  background-color: rgba(255, 255, 255, 0.1);
  border-color: var(--theme-border-dark);
  color: var(--theme-text-dark);
}

.dark .member-tag:hover {
  background-color: rgba(255, 255, 255, 0.15);
  border-color: var(--theme-border-dark);
}

/* 在线指示点 */
.online-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  margin-left: 3px;
  background-color: var(--theme-secondary);
  display: inline-block;
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

/* 添加加载状态样式 */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  color: var(--theme-text);
  gap: 10px;
  min-height: 200px; /* 添加最小高度确保加载状态显示美观 */
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
