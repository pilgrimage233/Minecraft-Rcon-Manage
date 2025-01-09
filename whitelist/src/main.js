import {createApp} from 'vue';
import {createI18n} from 'vue-i18n';
import App from './App.vue';
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';
import * as Icons from '@element-plus/icons-vue';
import router from './router';
import zhCN from './locales/zh-CN'
import enUS from './locales/en-US'
import './styles/global.css'

// 创建 i18n 实例
const i18n = createI18n({
    legacy: false, // 使用 Composition API 模式
    locale: localStorage.getItem('locale') || 'zh-CN', // 记住用户的语言选择
    messages: {
        'zh-CN': zhCN,
        'en-US': enUS
    }
});

const app = createApp(App);

// 先注册 i18n
app.use(i18n);

// 注册Element Plus
app.use(ElementPlus);

// 注册路由
app.use(router);

// 注册Element Plus图标
for (const [key, component] of Object.entries(Icons)) {
    app.component(key, component);
}

app.mount('#app');