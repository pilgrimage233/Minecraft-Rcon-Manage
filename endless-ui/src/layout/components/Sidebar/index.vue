<template>
  <div :class="{'has-logo':showLogo}"
       :style="{
         background: sidebarBackground,
         '--theme-color': settings.theme,
         '--theme-color-light': lightenColor(settings.theme, 10),
         '--theme-text-color': getTextColor(settings.theme)
       }">
    <logo v-if="showLogo" :collapse="isCollapse"/>
    <el-scrollbar :class="settings.sideTheme" wrap-class="scrollbar-wrapper">
      <el-menu
        :active-text-color="settings.theme"
        :collapse="isCollapse"
        :collapse-transition="false"
        :default-active="activeMenu"
        :text-color="settings.sideTheme === 'theme-dark' ? variables.menuColor : variables.menuLightColor"
        :unique-opened="true"
        background-color="transparent"
        mode="vertical"
      >
        <sidebar-item
          v-for="(route, index) in sidebarRouters"
          :key="route.path  + index"
          :base-path="route.path"
          :item="route"
        />
      </el-menu>
    </el-scrollbar>
    <version-info/>
  </div>
</template>

<script>
import {mapGetters, mapState} from "vuex";
import Logo from "./Logo";
import SidebarItem from "./SidebarItem";
import variables from "@/assets/styles/variables.scss";
import VersionInfo from '@/components/VersionInfo'

export default {
  components: {SidebarItem, Logo, VersionInfo},
  mounted() {
    // 确保背景色已初始化
    if (!this.settings.sidebarBg) {
      this.$store.dispatch('settings/changeSetting', {
        key: 'sidebarBg',
        value: 'linear-gradient(180deg, #1e1b4b 0%, #312e81 100%)'
      });
    }
    if (!this.settings.sidebarBgLight) {
      this.$store.dispatch('settings/changeSetting', {
        key: 'sidebarBgLight',
        value: '#ffffff'
      });
    }
  },
  computed: {
    ...mapState(["settings"]),
    ...mapGetters(["sidebarRouters", "sidebar"]),
    activeMenu() {
      const route = this.$route;
      const {meta, path} = route;
      // if set path, the sidebar will highlight the path you set
      if (meta.activeMenu) {
        return meta.activeMenu;
      }
      return path;
    },
    showLogo() {
      return this.$store.state.settings.sidebarLogo;
    },
    variables() {
      return variables;
    },
    isCollapse() {
      return !this.sidebar.opened;
    },
    // 确保背景色始终有值
    sidebarBackground() {
      if (this.settings.sideTheme === 'theme-dark') {
        return this.settings.sidebarBg || 'linear-gradient(180deg, #1e1b4b 0%, #312e81 100%)';
      } else {
        return this.settings.sidebarBgLight || '#ffffff';
      }
    }
  },
  methods: {
    // 颜色加亮函数
    lightenColor(color, percent) {
      const num = parseInt(color.replace("#", ""), 16);
      const amt = Math.round(2.55 * percent);
      const R = (num >> 16) + amt;
      const G = (num >> 8 & 0x00FF) + amt;
      const B = (num & 0x0000FF) + amt;
      return "#" + (0x1000000 + (R < 255 ? R < 1 ? 0 : R : 255) * 0x10000 +
        (G < 255 ? G < 1 ? 0 : G : 255) * 0x100 +
        (B < 255 ? B < 1 ? 0 : B : 255))
        .toString(16).slice(1);
    },
    // 根据背景色亮度判断文字颜色
    getTextColor(bgColor) {
      // 计算颜色亮度
      const color = bgColor.replace("#", "");
      const r = parseInt(color.substr(0, 2), 16);
      const g = parseInt(color.substr(2, 2), 16);
      const b = parseInt(color.substr(4, 2), 16);
      // 使用 YIQ 公式计算亮度
      const yiq = ((r * 299) + (g * 587) + (b * 114)) / 1000;
      // 如果亮度大于 180，使用深色文字，否则使用白色文字
      return yiq >= 180 ? '#1e293b' : '#ffffff';
    }
  }
};
</script>

<style lang="scss" scoped>
.scrollbar-wrapper {
  height: calc(100% - 120px) !important; // 为版本信息留出空间
}
</style>
