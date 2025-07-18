// main.js - 修复版本
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import zhCn from 'element-plus/es/locale/lang/zh-cn'

import App from './App.vue'
import router from './router'
import { useUserStore } from './stores/user'

// 创建应用实例
const app = createApp(App)
const pinia = createPinia()

// 注册所有Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 使用插件
app.use(pinia)
app.use(router)
app.use(ElementPlus, {
  locale: zhCn,
})

// 在路由准备就绪之前初始化用户状态
router.isReady().then(() => {
  // 初始化用户状态
  const userStore = useUserStore()
  userStore.initializeAuth()
  
  console.log('应用初始化完成')
  console.log('当前登录状态:', userStore.isLoggedIn)
  console.log('当前用户信息:', userStore.userInfo)
})

// 挂载应用
app.mount('#app')