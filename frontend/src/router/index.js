// router/index.js - 添加课程管理模块
import { createRouter, createWebHistory } from 'vue-router'

// 组件导入
const Login = () => import('@/views/Login.vue')
const Layout = () => import('@/layout/index.vue')
const Dashboard = () => import('@/views/Dashboard.vue')
const Courses = () => import('@/views/Courses.vue')
const CourseManagement = () => import('@/views/CourseManagement.vue') // 新增课程管理组件
const Exams = () => import('@/views/Exams.vue')
const StudentManagement = () => import('@/views/StudentManagement.vue')
const Admin = () => import('@/views/Admin.vue')
const NotFound = () => import('@/views/NotFound.vue')

// 路由配置
const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: Dashboard,
        meta: { title: '仪表板', icon: '📊' }
      },
      {
        path: 'courses',
        name: 'Courses',
        component: Courses,
        meta: { title: '我的课程', icon: '📚' }
      },
      {
        path: 'course-management',
        name: 'CourseManagement',
        component: CourseManagement,
        meta: { 
          title: '课程管理', 
          icon: '🎓',
          roles: ['ADMIN', 'TEACHER'] // 仅管理员和讲师可访问
        }
      },
      {
        path: 'exams',
        name: 'Exams',
        component: Exams,
        meta: { title: '考试中心', icon: '📝' }
      },
      {
        path: 'students',
        name: 'StudentManagement',
        component: StudentManagement,
        meta: { 
          title: '学员管理', 
          icon: '👥',
          roles: ['ADMIN', 'TEACHER'] // 权限控制
        }
      },
      {
        path: 'admin',
        name: 'Admin',
        component: Admin,
        meta: { 
          title: '管理后台', 
          icon: '⚙️',
          roles: ['ADMIN'] // 仅管理员可访问
        }
      }
    ]
  },
  {
    path: '/404',
    name: 'NotFound',
    component: NotFound,
    meta: { title: '页面未找到' }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404'
  }
]

// 创建路由
const router = createRouter({
  history: createWebHistory(),
  routes
})

// 生成菜单方法 - 更新菜单配置
export const generateMenus = (userRole) => {
  const allMenus = [
    { path: '/dashboard', title: '仪表板', icon: '📊', hidden: false },
    { path: '/courses', title: '我的课程', icon: '📚', hidden: false },
    { 
      path: '/course-management', 
      title: '课程管理', 
      icon: '🎓', 
      hidden: !['ADMIN', 'TEACHER'].includes(userRole) // 仅管理员和讲师可见
    },
    { path: '/exams', title: '考试中心', icon: '📝', hidden: false },
    { 
      path: '/students', 
      title: '学员管理', 
      icon: '👥', 
      hidden: !['ADMIN', 'TEACHER'].includes(userRole) 
    },
    { 
      path: '/admin', 
      title: '管理后台', 
      icon: '⚙️', 
      hidden: userRole !== 'ADMIN' // 仅管理员可见
    }
  ]
  
  return allMenus.filter(menu => !menu.hidden)
}

// 路由守卫
router.beforeEach((to, from, next) => {
  console.log('路由跳转:', from.path, '->', to.path)
  
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 智能培训系统`
  }
  
  const token = localStorage.getItem('token')
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth !== false)
  
  if (requiresAuth && !token) {
    console.log('需要登录，跳转到登录页')
    next('/login')
  } else if (to.path === '/login' && token) {
    console.log('已登录，跳转到仪表板')
    next('/dashboard')
  } else {
    // 检查角色权限
    if (to.meta.roles) {
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      const userRole = userInfo.role
      
      if (!to.meta.roles.includes(userRole)) {
        console.log('权限不足，跳转到仪表板')
        next('/dashboard')
        return
      }
    }
    
    next()
  }
})

// 路由错误处理
router.onError((error) => {
  console.error('路由错误:', error)
})

export default router