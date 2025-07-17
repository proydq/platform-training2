// router/index.js - 最简化版本，移除所有权限检查
import { createRouter, createWebHistory } from 'vue-router'

// 导入页面组件
const Login = () => import('@/views/Login.vue')
const Dashboard = () => import('@/views/Dashboard.vue')

// 路由配置 - 移除所有复杂的权限检查
const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: {
      title: '登录'
    }
  },
  {
    path: '/dashboard',
    name: 'Dashboard', 
    component: Dashboard,
    meta: {
      title: '仪表板'
    }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/login'
  }
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

// 简化的路由守卫 - 只设置页面标题
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 智能培训系统` : '智能培训系统'
  next()
})

// 错误处理
router.onError((error) => {
  console.error('路由错误:', error)
})

export default router