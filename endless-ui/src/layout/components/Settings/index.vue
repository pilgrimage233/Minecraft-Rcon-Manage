<template>
  <el-drawer :append-to-body="true" :show-close="false" :visible="visible" :with-header="false" size="280px">
    <div class="drawer-container">
      <div>
        <div class="setting-drawer-content">
          <div class="setting-drawer-title">
            <h3 class="drawer-title">主题风格设置</h3>
          </div>
          <div class="setting-drawer-block-checbox">
            <div class="setting-drawer-block-checbox-item" @click="handleTheme('theme-dark')">
              <img alt="dark" src="@/assets/images/dark.svg">
              <div v-if="sideTheme === 'theme-dark'" class="setting-drawer-block-checbox-selectIcon"
                   style="display: block;">
                <i aria-label="图标: check" class="anticon anticon-check">
                  <svg :fill="theme" aria-hidden="true" class="" data-icon="check" focusable="false"
                       height="1em" viewBox="64 64 896 896" width="1em">
                    <path
                      d="M912 190h-69.9c-9.8 0-19.1 4.5-25.1 12.2L404.7 724.5 207 474a32 32 0 0 0-25.1-12.2H112c-6.7 0-10.4 7.7-6.3 12.9l273.9 347c12.8 16.2 37.4 16.2 50.3 0l488.4-618.9c4.1-5.1.4-12.8-6.3-12.8z"/>
                  </svg>
                </i>
              </div>
            </div>
            <div class="setting-drawer-block-checbox-item" @click="handleTheme('theme-light')">
              <img alt="light" src="@/assets/images/light.svg">
              <div v-if="sideTheme === 'theme-light'" class="setting-drawer-block-checbox-selectIcon"
                   style="display: block;">
                <i aria-label="图标: check" class="anticon anticon-check">
                  <svg :fill="theme" aria-hidden="true" class="" data-icon="check" focusable="false"
                       height="1em" viewBox="64 64 896 896" width="1em">
                    <path
                      d="M912 190h-69.9c-9.8 0-19.1 4.5-25.1 12.2L404.7 724.5 207 474a32 32 0 0 0-25.1-12.2H112c-6.7 0-10.4 7.7-6.3 12.9l273.9 347c12.8 16.2 37.4 16.2 50.3 0l488.4-618.9c4.1-5.1.4-12.8-6.3-12.8z"/>
                  </svg>
                </i>
              </div>
            </div>
          </div>

          <div class="drawer-item">
            <span>主题颜色</span>
            <theme-picker style="float: right;height: 26px;margin: -3px 8px 0 0;" @change="themeChange"/>
          </div>

          <div class="preset-themes">
            <div class="preset-themes-title">预置主题方案</div>
            <div class="preset-themes-list">
              <div
                v-for="themeItem in presetThemes"
                :key="themeItem.themeColor"
                :class="['preset-theme-item', { active: theme === themeItem.themeColor }]"
                :title="themeItem.name"
                @click="selectPresetTheme(themeItem)"
              >
                <div :style="{ background: themeItem.preview }" class="theme-preview">
                  <i v-if="theme === themeItem.themeColor" class="el-icon-check"></i>
                </div>
                <div class="theme-name">{{ themeItem.name.split(' ')[1] }}</div>
              </div>
            </div>
          </div>
        </div>

        <el-divider/>

        <h3 class="drawer-title">系统布局配置</h3>

        <div class="drawer-item">
          <span>开启 TopNav</span>
          <el-switch v-model="topNav" class="drawer-switch"/>
        </div>

        <div class="drawer-item">
          <span>开启 Tags-Views</span>
          <el-switch v-model="tagsView" class="drawer-switch"/>
        </div>

        <div class="drawer-item">
          <span>固定 Header</span>
          <el-switch v-model="fixedHeader" class="drawer-switch"/>
        </div>

        <div class="drawer-item">
          <span>显示 Logo</span>
          <el-switch v-model="sidebarLogo" class="drawer-switch"/>
        </div>

        <div class="drawer-item">
          <span>动态标题</span>
          <el-switch v-model="dynamicTitle" class="drawer-switch"/>
        </div>

        <el-divider/>

        <el-button icon="el-icon-document-add" plain size="small" type="primary" @click="saveSetting">保存配置
        </el-button>
        <el-button icon="el-icon-refresh" plain size="small" @click="resetSetting">重置配置</el-button>
      </div>
    </div>
  </el-drawer>
</template>

<script>
import ThemePicker from '@/components/ThemePicker'

export default {
  components: {ThemePicker},
  data() {
    return {
      theme: this.$store.state.settings.theme,
      sideTheme: this.$store.state.settings.sideTheme,
      presetThemes: [
        {
          name: 'Indigo 靛蓝',
          themeColor: '#6366f1',
          sidebarBg: 'linear-gradient(180deg, #1e1b4b 0%, #312e81 100%)',
          sidebarBgLight: '#ffffff',
          preview: 'linear-gradient(135deg, #1e1b4b 0%, #6366f1 100%)'
        },
        {
          name: 'Blue 蓝色',
          themeColor: '#3b82f6',
          sidebarBg: 'linear-gradient(180deg, #1e3a8a 0%, #1e40af 100%)',
          sidebarBgLight: '#ffffff',
          preview: 'linear-gradient(135deg, #1e3a8a 0%, #3b82f6 100%)'
        },
        {
          name: 'Purple 紫色',
          themeColor: '#a855f7',
          sidebarBg: 'linear-gradient(180deg, #581c87 0%, #6b21a8 100%)',
          sidebarBgLight: '#ffffff',
          preview: 'linear-gradient(135deg, #581c87 0%, #a855f7 100%)'
        },
        {
          name: 'Pink 粉色',
          themeColor: '#ec4899',
          sidebarBg: 'linear-gradient(180deg, #831843 0%, #9f1239 100%)',
          sidebarBgLight: '#ffffff',
          preview: 'linear-gradient(135deg, #831843 0%, #ec4899 100%)'
        },
        {
          name: 'Red 红色',
          themeColor: '#ef4444',
          sidebarBg: 'linear-gradient(180deg, #7f1d1d 0%, #991b1b 100%)',
          sidebarBgLight: '#ffffff',
          preview: 'linear-gradient(135deg, #7f1d1d 0%, #ef4444 100%)'
        },
        {
          name: 'Orange 橙色',
          themeColor: '#f97316',
          sidebarBg: 'linear-gradient(180deg, #7c2d12 0%, #9a3412 100%)',
          sidebarBgLight: '#ffffff',
          preview: 'linear-gradient(135deg, #7c2d12 0%, #f97316 100%)'
        },
        {
          name: 'Amber 琥珀',
          themeColor: '#f59e0b',
          sidebarBg: 'linear-gradient(180deg, #78350f 0%, #92400e 100%)',
          sidebarBgLight: '#ffffff',
          preview: 'linear-gradient(135deg, #78350f 0%, #f59e0b 100%)'
        },
        {
          name: 'Green 绿色',
          themeColor: '#10b981',
          sidebarBg: 'linear-gradient(180deg, #064e3b 0%, #065f46 100%)',
          sidebarBgLight: '#ffffff',
          preview: 'linear-gradient(135deg, #064e3b 0%, #10b981 100%)'
        },
        {
          name: 'Teal 青色',
          themeColor: '#14b8a6',
          sidebarBg: 'linear-gradient(180deg, #134e4a 0%, #115e59 100%)',
          sidebarBgLight: '#ffffff',
          preview: 'linear-gradient(135deg, #134e4a 0%, #14b8a6 100%)'
        },
        {
          name: 'Cyan 蓝绿',
          themeColor: '#06b6d4',
          sidebarBg: 'linear-gradient(180deg, #164e63 0%, #155e75 100%)',
          sidebarBgLight: '#ffffff',
          preview: 'linear-gradient(135deg, #164e63 0%, #06b6d4 100%)'
        },
        {
          name: 'Sky 天蓝',
          themeColor: '#0ea5e9',
          sidebarBg: 'linear-gradient(180deg, #0c4a6e 0%, #075985 100%)',
          sidebarBgLight: '#ffffff',
          preview: 'linear-gradient(135deg, #0c4a6e 0%, #0ea5e9 100%)'
        },
        {
          name: 'Slate 石板',
          themeColor: '#64748b',
          sidebarBg: 'linear-gradient(180deg, #1e293b 0%, #334155 100%)',
          sidebarBgLight: '#ffffff',
          preview: 'linear-gradient(135deg, #1e293b 0%, #64748b 100%)'
        }
      ]
    };
  },
  computed: {
    visible: {
      get() {
        return this.$store.state.settings.showSettings
      }
    },
    fixedHeader: {
      get() {
        return this.$store.state.settings.fixedHeader
      },
      set(val) {
        this.$store.dispatch('settings/changeSetting', {
          key: 'fixedHeader',
          value: val
        })
      }
    },
    topNav: {
      get() {
        return this.$store.state.settings.topNav
      },
      set(val) {
        this.$store.dispatch('settings/changeSetting', {
          key: 'topNav',
          value: val
        })
        if (!val) {
          this.$store.dispatch('app/toggleSideBarHide', false);
          this.$store.commit("SET_SIDEBAR_ROUTERS", this.$store.state.permission.defaultRoutes);
        }
      }
    },
    tagsView: {
      get() {
        return this.$store.state.settings.tagsView
      },
      set(val) {
        this.$store.dispatch('settings/changeSetting', {
          key: 'tagsView',
          value: val
        })
      }
    },
    sidebarLogo: {
      get() {
        return this.$store.state.settings.sidebarLogo
      },
      set(val) {
        this.$store.dispatch('settings/changeSetting', {
          key: 'sidebarLogo',
          value: val
        })
      }
    },
    dynamicTitle: {
      get() {
        return this.$store.state.settings.dynamicTitle
      },
      set(val) {
        this.$store.dispatch('settings/changeSetting', {
          key: 'dynamicTitle',
          value: val
        })
      }
    },
  },
  methods: {
    themeChange(val) {
      this.$store.dispatch('settings/changeSetting', {
        key: 'theme',
        value: val
      })
      this.theme = val;
    },
    selectPresetTheme(themeItem) {
      // 设置主题色
      this.themeChange(themeItem.themeColor);

      // 设置侧边栏背景色
      this.$store.dispatch('settings/changeSetting', {
        key: 'sidebarBg',
        value: themeItem.sidebarBg
      });

      this.$store.dispatch('settings/changeSetting', {
        key: 'sidebarBgLight',
        value: themeItem.sidebarBgLight
      });
    },
    handleTheme(val) {
      this.$store.dispatch('settings/changeSetting', {
        key: 'sideTheme',
        value: val
      })
      this.sideTheme = val;
    },
    saveSetting() {
      this.$modal.loading("正在保存到本地，请稍候...");
      const sidebarBg = this.$store.state.settings.sidebarBg;
      const sidebarBgLight = this.$store.state.settings.sidebarBgLight;
      this.$cache.local.set(
        "layout-setting",
        `{
            "topNav":${this.topNav},
            "tagsView":${this.tagsView},
            "fixedHeader":${this.fixedHeader},
            "sidebarLogo":${this.sidebarLogo},
            "dynamicTitle":${this.dynamicTitle},
            "sideTheme":"${this.sideTheme}",
            "theme":"${this.theme}",
            "sidebarBg":"${sidebarBg}",
            "sidebarBgLight":"${sidebarBgLight}"
          }`
      );
      setTimeout(this.$modal.closeLoading(), 1000)
    },
    resetSetting() {
      this.$modal.loading("正在清除设置缓存并刷新，请稍候...");
      this.$cache.local.remove("layout-setting")
      setTimeout("window.location.reload()", 1000)
    }
  }
}
</script>

<style lang="scss" scoped>
@import "~@/assets/styles/variables.scss";

.setting-drawer-content {
  .setting-drawer-title {
    margin-bottom: 16px;
    color: #1e293b;
    font-size: 15px;
    line-height: 24px;
    font-weight: 600;
  }

  .setting-drawer-block-checbox {
    display: flex;
    justify-content: flex-start;
    align-items: center;
    margin-top: 12px;
    margin-bottom: 24px;
    gap: 12px;

    .setting-drawer-block-checbox-item {
      position: relative;
      border-radius: 8px;
      cursor: pointer;
      overflow: hidden;
      transition: all 0.2s ease;
      border: 2px solid transparent;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      }

      img {
        width: 56px;
        height: 56px;
        display: block;
      }

      .setting-drawer-block-checbox-selectIcon {
        position: absolute;
        top: 0;
        right: 0;
        width: 100%;
        height: 100%;
        padding-top: 18px;
        padding-left: 28px;
        color: $primary-color;
        font-weight: 700;
        font-size: 16px;
        background: rgba(99, 102, 241, 0.1);
      }
    }
  }
}

.drawer-container {
  padding: 24px;
  font-size: 14px;
  line-height: 1.6;
  word-wrap: break-word;

  .drawer-title {
    margin-bottom: 16px;
    color: #1e293b;
    font-size: 15px;
    line-height: 24px;
    font-weight: 600;
  }

  .drawer-item {
    color: #475569;
    font-size: 14px;
    padding: 14px 0;
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-bottom: 1px solid #f1f5f9;

    &:last-child {
      border-bottom: none;
    }
  }

  ::v-deep .el-divider {
    margin: 20px 0;
    background-color: #f1f5f9;
  }

  ::v-deep .el-button {
    margin-top: 8px;
    margin-right: 8px;
  }
}

.preset-themes {
  margin-top: 16px;

  .preset-themes-title {
    font-size: 13px;
    color: #64748b;
    margin-bottom: 12px;
  }

  .preset-themes-list {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 12px;

    .preset-theme-item {
      cursor: pointer;
      transition: all 0.2s ease;

      &:hover {
        transform: translateY(-2px);

        .theme-preview {
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
        }
      }

      &.active {
        .theme-preview {
          box-shadow: 0 4px 16px rgba(0, 0, 0, 0.3);
          transform: scale(1.05);
          border: 2px solid rgba(99, 102, 241, 0.5);
        }
      }

      .theme-preview {
        width: 100%;
        height: 50px;
        border-radius: 8px;
        transition: all 0.2s ease;
        display: flex;
        align-items: center;
        justify-content: center;
        box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
        position: relative;
        overflow: hidden;

        i {
          color: #fff;
          font-size: 20px;
          font-weight: bold;
          text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
          z-index: 1;
        }
      }

      .theme-name {
        text-align: center;
        font-size: 12px;
        color: #64748b;
        margin-top: 6px;
        font-weight: 500;
      }
    }
  }
}
</style>
