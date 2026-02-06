import Vue from 'vue'

import Cookies from 'js-cookie'
import '@/assets/styles/index.scss' // global css
import '@/assets/styles/ruoyi.scss' // ruoyi css
import App from './App'
import store from './store'
import router from './router'
import directive from './directive' // directive
import plugins from './plugins' // plugins
import {download} from '@/utils/request'

import './assets/icons' // icon
import './permission' // permission control
import {getDicts} from "@/api/system/dict/data";
import {getConfigKey} from "@/api/system/config";
import {addDateRange, handleTree, parseTime, resetForm, selectDictLabel, selectDictLabels} from "@/utils/ruoyi";
// 分页组件
import Pagination from "@/components/Pagination";
// 自定义表格工具组件
import RightToolbar from "@/components/RightToolbar"
// 富文本组件
import Editor from "@/components/Editor"
// 文件上传组件
import FileUpload from "@/components/FileUpload"
// 图片上传组件
import ImageUpload from "@/components/ImageUpload"
// 图片预览组件
import ImagePreview from "@/components/ImagePreview"
// 字典标签组件
import DictTag from '@/components/DictTag'
// 头部标签组件
import VueMeta from 'vue-meta'
// 字典数据组件
import DictData from '@/components/DictData'

// 按需引入Element UI组件以减少包体积
// 生产环境使用CDN，开发环境使用本地
if (process.env.NODE_ENV === 'development') {
  const Element = require('element-ui')
  require('./assets/styles/element-variables.scss')
  Vue.use(Element, {
    size: Cookies.get('size') || 'medium'
  })
}

// 全局方法挂载
Vue.prototype.getDicts = getDicts
Vue.prototype.getConfigKey = getConfigKey
Vue.prototype.parseTime = parseTime
Vue.prototype.resetForm = resetForm
Vue.prototype.addDateRange = addDateRange
Vue.prototype.selectDictLabel = selectDictLabel
Vue.prototype.selectDictLabels = selectDictLabels
Vue.prototype.download = download
Vue.prototype.handleTree = handleTree

// 全局组件挂载
Vue.component('DictTag', DictTag)
Vue.component('Pagination', Pagination)
Vue.component('RightToolbar', RightToolbar)
Vue.component('Editor', Editor)
Vue.component('FileUpload', FileUpload)
Vue.component('ImageUpload', ImageUpload)
Vue.component('ImagePreview', ImagePreview)

Vue.use(directive)
Vue.use(plugins)
Vue.use(VueMeta)
DictData.install()

/**
 * If you don't want to use mock-server
 * you want to use MockJs for mock api
 * you can execute: mockXHR()
 *
 * Currently MockJs will be used in the production environment,
 * please remove it before going online! ! !
 */

// 生产环境使用CDN的Element UI
if (process.env.NODE_ENV === 'production') {
  Vue.use(window.ELEMENT, {
    size: Cookies.get('size') || 'medium'
  })
}

Vue.config.productionTip = false
// 性能优化：关闭开发工具提示
Vue.config.devtools = process.env.NODE_ENV === 'development'
// 性能优化：关闭生产环境的警告
Vue.config.silent = process.env.NODE_ENV === 'production'

new Vue({
  el: '#app',
  router,
  store,
  render: h => h(App)
})
