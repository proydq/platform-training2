// frontend/src/main.js
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import App from './App.vue'
import router from './router'
import AuthImage from '@/components/AuthImage.vue'

import './assets/main.css'

const app = createApp(App)

// 注册Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 🔥 注册AuthImage为全局组件
app.component('AuthImage', AuthImage)

app.use(createPinia())
app.use(ElementPlus)
app.use(router)

app.mount('#app')