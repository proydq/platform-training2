// router/index.js - 修复版本
import { createRouter, createWebHistory } from 'vue-router'

// 导入页面组件
const Login = () => import('@/views/Login.vue')
const Layout = () => import('@/layout/index.vue')
const Dashboard = () => import('@/views/Dashboard.vue')
const Courses = () => import('@/views/Courses.vue')
const Exams = () => import('@/views/Exams.vue')
const TakeExam = () => import('@/views/TakeExam.vue')
const ExamResult = () => import('@/views/ExamResult.vue')
const StudentManagement = () => import('@/views/StudentManagement.vue')
const Admin = () => import('@/views/Admin.vue')

// 路由配置
const routes = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: {
      title: '登录',
      requiresAuth: false
    }
  },
  {
    path: '/',
    component: Layout,
    meta: {
      requiresAuth: true
    },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: Dashboard,
        meta: {
          title: '仪表板',
          icon: 'Dashboard'
        }
      },
      {
        path: 'courses',
        name: 'Courses',
        component: Courses,
        meta: {
          title: '我的课程',
          icon: 'Reading'
        }
      },
      {
        path: 'exams',
        name: 'Exams',
        component: Exams,
        meta: {
          title: '考试中心',
          icon: 'Document'
        }
      },
      {
        path: 'students',
        name: 'StudentManagement',
        component: StudentManagement,
        meta: {
          title: '学员管理',
          icon: 'User',
          roles: ['ADMIN', 'TEACHER']
        }
      },
      {
        path: 'admin',
        name: 'Admin',
        component: Admin,
        meta: {
          title: '管理后台',
          icon: 'Setting',
          roles: ['ADMIN']
        }
      }
    ]
  },
  {
    path: '/exams/:examId/take',
    name: 'TakeExam',
    component: TakeExam,
    meta: {
      title: '在线考试',
      requiresAuth: true
    }
  },
  {
    path: '/exams/:examId/result',
    name: 'ExamResult',
    component: ExamResult,
    meta: {
      title: '考试结果',
      requiresAuth: true
    }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard'
  }
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

// 根据用户角色生成菜单的函数
export function generateMenus(userRole) {
  // 基础菜单项（所有角色都可访问）
  const baseMenus = [
    {
      path: '/dashboard',
      title: '仪表板',
      icon: 'Dashboard',
      hidden: false
    },
    {
      path: '/courses',
      title: '我的课程',
      icon: 'Reading',
      hidden: false
    },
    {
      path: '/exams',
      title: '考试中心',
      icon: 'Document',
      hidden: false
    }
  ]

  // 管理员和教师可访问的菜单
  const teacherMenus = [
    {
      path: '/students',
      title: '学员管理',
      icon: 'User',
      hidden: false,
      roles: ['ADMIN', 'TEACHER']
    }
  ]

  // 仅管理员可访问的菜单
  const adminMenus = [
    {
      path: '/admin',
      title: '管理后台',
      icon: 'Setting',
      hidden: false,
      roles: ['ADMIN']
    }
  ]

  // 根据用户角色过滤菜单
  let menus = [...baseMenus]

  if (userRole === 'ADMIN') {
    // 管理员可以访问所有菜单
    menus = [...baseMenus, ...teacherMenus, ...adminMenus]
  } else if (userRole === 'TEACHER') {
    // 教师可以访问基础菜单和教师菜单
    menus = [...baseMenus, ...teacherMenus]
  } else if (userRole === 'STUDENT') {
    // 学生只能访问基础菜单
    menus = [...baseMenus]
  }

  return menus
}

// 简化的路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 智能培训系统` : '智能培训系统'
  
  // 简单的登录检查
  const token = localStorage.getItem('token')
  
  // 检查是否需要登录
  if (to.meta.requiresAuth !== false) {
    if (!token) {
      next('/login')
      return
    }
  }
  
  // 如果已登录用户访问登录页，重定向到首页
  if (to.path === '/login' && token) {
    next('/dashboard')
    return
  }
  
  next()
})

// 路由错误处理
router.onError((error) => {
  console.error('路由错误:', error)
})

export default router