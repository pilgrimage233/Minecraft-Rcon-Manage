<template>
  <section class="app-main">
    <transition mode="out-in" name="fade-transform">
      <keep-alive :include="cachedViews">
        <router-view v-if="!$route.meta.link" :key="key"/>
      </keep-alive>
    </transition>
    <iframe-toggle/>
  </section>
</template>

<script>
import iframeToggle from "./IframeToggle/index"

export default {
  name: 'AppMain',
  components: {iframeToggle},
  computed: {
    cachedViews() {
      return this.$store.state.tagsView.cachedViews
    },
    key() {
      return this.$route.path
    }
  }
}
</script>

<style lang="scss" scoped>
@import "~@/assets/styles/variables.scss";

.app-main {
  /* navbar height = 56px */
  min-height: calc(100vh - #{$navbar-height});
  width: 100%;
  position: relative;
  overflow: hidden;
  background: #f1f5f9;
}

.fixed-header + .app-main {
  padding-top: $navbar-height;
}

.hasTagsView {
  .app-main {
    /* navbar + tags-view = 56 + 38 = 94px */
    min-height: calc(100vh - #{$navbar-height} - #{$tags-height});
  }

  .fixed-header + .app-main {
    padding-top: calc(#{$navbar-height} + #{$tags-height});
  }
}
</style>

<style lang="scss">
@import "~@/assets/styles/variables.scss";

// fix css style bug in open el-dialog
.el-popup-parent--hidden {
  .fixed-header {
    padding-right: 6px;
  }
}

// 自定义滚动条
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background-color: transparent;
}

::-webkit-scrollbar-thumb {
  background-color: #cbd5e1;
  border-radius: 4px;
  transition: background-color 0.2s ease;

  &:hover {
    background-color: #94a3b8;
  }
}

// 页面过渡动画
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.2s ease;
}

.fade-transform-enter {
  opacity: 0;
  transform: translateX(-10px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(10px);
}
</style>
