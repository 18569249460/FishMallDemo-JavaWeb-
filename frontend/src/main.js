import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import 'element-plus/dist/index.css'
import './assets/main.css'
import App from './App.vue'
import router from './router'

// 前端入口文件：创建 Vue 应用，并挂载路由、Pinia 状态管理和 Element Plus 组件库。
// Element Plus 全局切到中文，避免组件默认空状态、按钮或提示露出英文。
createApp(App)
  .use(createPinia())
  .use(router)
  .use(ElementPlus, { locale: zhCn })
  .mount('#app')
