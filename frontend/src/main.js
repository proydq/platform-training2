// main.js - 修复版本，正确的导入顺序
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'

import App from './App.vue'
import router from './router'

// 创建应用
const app = createApp(App)

// 创建 Pinia 实例
const pinia = createPinia()

// 安装插件 - 顺序很重要
app.use(pinia)
app.use(router)
app.use(ElementPlus, {
  locale: zhCn
})

// 挂载应用
app.mount('#app')

console.log('应用已启动')