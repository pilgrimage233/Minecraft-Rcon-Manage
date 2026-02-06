<template>
  <div id="app">
    <router-view/>
    <theme-picker/>
  </div>
</template>

<script>
import ThemePicker from "@/components/ThemePicker";
import {detectMemoryLeak} from "@/utils/performance";

export default {
  name: "App",
  components: {ThemePicker},
  metaInfo() {
    return {
      title: this.$store.state.settings.dynamicTitle && this.$store.state.settings.title,
      titleTemplate: title => {
        return title ? `${title} - ${process.env.VUE_APP_TITLE}` : process.env.VUE_APP_TITLE
      }
    }
  },
  mounted() {
    // 开发环境启用内存泄漏检测
    if (process.env.NODE_ENV === 'development') {
      detectMemoryLeak();
    }
  }
};
</script>
<style scoped>
#app .theme-picker {
  display: none;
}
</style>
