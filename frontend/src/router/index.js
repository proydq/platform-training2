// router/index.js - 紧急修复版本，最简路由配置
import { createRouter, createWebHistory } from 'vue-router'

// 简化的组件导入
const Login = () => import('@/views/Login.vue')
const Dashboard = () => import('@/views/Dashboard.vue')

// 最简路由配置
const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/dashboard',
    name: 'Dashboard', 
    component: Dashboard
  }
]

// 创建路由
const router = createRouter({
  history: createWebHistory(),
  routes
})

// 简化的路由守卫
router.beforeEach((to, from, next) => {
  console.log('路由跳转:', from.path, '->', to.path)
  
  const token = localStorage.getItem('token')
  
  if (to.path === '/dashboard' && !token) {
    console.log('需要登录，跳转到登录页')
    next('/login')
  } else if (to.path === '/login' && token) {
    console.log('已登录，跳转到仪表板')
    next('/dashboard')
  } else {
    next()
  }
})

export default router