<template>
  <div class="navbar">
    <hamburger id="hamburger-container" :is-active="sidebar.opened" class="hamburger-container"
               @toggleClick="toggleSideBar"/>

    <breadcrumb v-if="!topNav" id="breadcrumb-container" class="breadcrumb-container"/>
    <top-nav v-if="topNav" id="topmenu-container" class="topmenu-container"/>

    <div class="right-menu">
      <template v-if="device!=='mobile'">
        <search id="header-search" class="right-menu-item"/>

        <el-tooltip content="源码地址" effect="dark" placement="bottom">
          <ruo-yi-git id="ruoyi-git" class="right-menu-item hover-effect"/>
        </el-tooltip>

        <screenfull id="screenfull" class="right-menu-item hover-effect"/>

      </template>

      <el-dropdown class="avatar-container right-menu-item hover-effect" trigger="click">
        <div class="avatar-wrapper">
          <img :src="avatar" class="user-avatar">
          <i class="el-icon-caret-bottom"/>
        </div>
        <el-dropdown-menu slot="dropdown">
          <router-link to="/user/profile">
            <el-dropdown-item>个人中心</el-dropdown-item>
          </router-link>
          <el-dropdown-item @click.native="setting = true">
            <span>布局设置</span>
          </el-dropdown-item>
          <el-dropdown-item divided @click.native="logout">
            <span>退出登录</span>
          </el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
    </div>
  </div>
</template>

<script>
import {mapGetters} from 'vuex'
import Breadcrumb from '@/components/Breadcrumb'
import TopNav from '@/components/TopNav'
import Hamburger from '@/components/Hamburger'
import Screenfull from '@/components/Screenfull'
import SizeSelect from '@/components/SizeSelect'
import Search from '@/components/HeaderSearch'
import RuoYiGit from '@/components/RuoYi/Git'
import RuoYiDoc from '@/components/RuoYi/Doc'

export default {
  components: {
    Breadcrumb,
    TopNav,
    Hamburger,
    Screenfull,
    SizeSelect,
    Search,
    RuoYiGit,
    RuoYiDoc
  },
  computed: {
    ...mapGetters([
      'sidebar',
      'avatar',
      'device'
    ]),
    setting: {
      get() {
        return this.$store.state.settings.showSettings
      },
      set(val) {
        this.$store.dispatch('settings/changeSetting', {
          key: 'showSettings',
          value: val
        })
      }
    },
    topNav: {
      get() {
        return this.$store.state.settings.topNav
      }
    },
    themeColor() {
      return this.$store.state.settings.theme || '#6366f1'
    }
  },
  methods: {
    toggleSideBar() {
      this.$store.dispatch('app/toggleSideBar')
    },
    async logout() {
      this.$confirm('确定注销并退出系统吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$store.dispatch('LogOut').then(() => {
          location.href = '/index';
        })
      }).catch(() => {
      });
    }
  }
}
</script>

<style lang="scss" scoped>
@import "~@/assets/styles/variables.scss";

.navbar {
  height: $navbar-height;
  overflow: visible;
  position: relative;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  box-shadow: none;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px 0 0;

  .hamburger-container {
    line-height: $navbar-height;
    height: 100%;
    float: left;
    cursor: pointer;
    transition: all 0.3s ease;
    -webkit-tap-highlight-color: transparent;
    padding: 0 16px;
    display: flex;
    align-items: center;

    &:hover {
      background: rgba(var(--theme-color-rgb, 99, 102, 241), 0.08);
    }
  }

  .breadcrumb-container {
    float: left;
    margin-left: 8px;
  }

  .topmenu-container {
    position: absolute;
    left: 50px;
  }

  .errLog-container {
    display: inline-block;
    vertical-align: top;
  }

  .right-menu {
    display: flex;
    align-items: center;
    height: 100%;

    &:focus {
      outline: none;
    }

    .right-menu-item {
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 0 10px;
      height: 36px;
      font-size: 18px;
      color: #64748b;
      border-radius: $border-radius-sm;
      margin: 0 2px;
      transition: all 0.2s ease;

      &.hover-effect {
        cursor: pointer;

        &:hover {
          background: rgba(var(--theme-color-rgb, 99, 102, 241), 0.08);
          color: var(--theme-color, $primary-color);
        }
      }
    }

    .avatar-container {
      margin-left: 12px;

      .avatar-wrapper {
        display: flex;
        align-items: center;
        cursor: pointer;
        padding: 4px 12px 4px 4px;
        // 移除圆角，避免方形轮廓
        border-radius: 0;
        transition: all 0.2s ease;
        // 确保没有阴影
        box-shadow: none !important;
        // 移除背景色
        background: transparent !important;

        &:hover {
          // 悬浮时也不显示背景
          background: transparent !important;
          // 悬浮时也不显示阴影
          box-shadow: none !important;
        }

        .user-avatar {
          width: 36px;
          height: 36px;
          // 移除圆角，改为方形头像
          border-radius: 0;
          // 移除边框
          border: none !important;
          transition: all 0.2s ease;
          // 确保头像本身没有阴影
          box-shadow: none !important;
          // 移除可能的 outline
          outline: none !important;
        }

        .el-icon-caret-bottom {
          margin-left: 6px;
          font-size: 12px;
          color: #94a3b8;
          transition: transform 0.2s ease;
        }

        &:hover {
          .el-icon-caret-bottom {
            color: var(--theme-color, $primary-color);
          }
        }
      }
    }
  }
}

// 全局覆盖 - 确保下拉菜单容器没有额外阴影
::v-deep .el-dropdown {
  box-shadow: none !important;
}

::v-deep .avatar-container .el-dropdown {
  box-shadow: none !important;
}
</style>
