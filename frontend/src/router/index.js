// frontend/src/router/index.js
import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'

// 布局组件（常用组件可以直接导入）
import Layout from '@/layout/index.vue'

// 路由配置
const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
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
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '仪表板', icon: '📊' }
      },
      {
        path: 'courses',
        name: 'Courses',
        component: () => import('@/views/Courses.vue'),
        meta: { title: '我的课程', icon: '📚' }
      },
      {
        path: 'course-management',
        name: 'CourseManagement',
        component: () => import('@/views/CourseManagement.vue'),
        meta: {
          title: '课程管理',
          icon: '🎓',
          roles: ['ADMIN', 'TEACHER']
        }
      },
      {
        path: 'exams',
        name: 'Exams',
        component: () => import('@/views/Exams.vue'),
        meta: { title: '考试中心', icon: '📝' }
      },
      {
        path: 'students',
        name: 'StudentManagement',
        component: () => import('@/views/StudentManagement.vue'),
        meta: {
          title: '学员管理',
          icon: '👥',
          roles: ['ADMIN', 'TEACHER']
        }
      },
      {
        path: 'admin',
        name: 'Admin',
        component: () => import('@/views/Admin.vue'),
        meta: {
          title: '管理后台',
          icon: '⚙️',
          roles: ['ADMIN']
        }
      },
      {
        path: 'admin/user-management',
        name: 'UserManagement',
        component: () => import('@/views/UserManagement.vue'),
        meta: {
          title: '用户管理',
          icon: '👤',
          roles: ['ADMIN']
        }
      },
      {
        path: 'learning/:courseId',
        name: 'LearningPage',
        component: () => import('@/views/LearningPage.vue'),
        meta: {
          title: '课程学习',
          icon: '📚',
          requiresAuth: true
        }
      },
      // 考试相关路由
      {
        path: 'exams/:examId/take',
        name: 'TakeExam',
        component: () => import('@/views/TakeExam.vue'),
        meta: {
          title: '参加考试',
          requiresAuth: true
        }
      },
      {
        path: 'exams/:examId/result',
        name: 'ExamResult',
        component: () => import('@/views/ExamResult.vue'),
        meta: {
          title: '考试结果',
          requiresAuth: true
        }
      }
    ]
  },
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: { title: '页面未找到', requiresAuth: false }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404'
  }
]

// 创建路由
const router = createRouter({
  history: createWebHistory(),
  routes,
  // 滚动行为
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

// 生成菜单方法
export const generateMenus = (userRole) => {
  const allMenus = [
    { path: '/dashboard', title: '仪表板', icon: '📊', hidden: false },
    { path: '/courses', title: '我的课程', icon: '📚', hidden: false },
    {
      path: '/course-management',
      title: '课程管理',
      icon: '🎓',
      hidden: !['ADMIN', 'TEACHER'].includes(userRole)
    },
    { path: '/exams', title: '考试中心', icon: '📝', hidden: false },
    {
      path: '/students',
      title: '学员管理',
      icon: '👥',
      hidden: !['ADMIN', 'TEACHER'].includes(userRole)
    },
    {
      path: '/admin/user-management',
      title: '用户管理',
      icon: '👤',
      hidden: !['ADMIN'].includes(userRole)
    }
  ]

  return allMenus.filter(menu => !menu.hidden)
}

// 白名单路由（不需要登录即可访问）
const whiteList = ['/login', '/404']

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 智能培训系统`
  }

  const token = localStorage.getItem('token')

  // 白名单路由直接放行
  if (whiteList.includes(to.path)) {
    next()
    return
  }

  // 需要认证的路由
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth !== false)

  if (requiresAuth && !token) {
    // 未登录，重定向到登录页
    ElMessage.warning('请先登录')
    next({
      path: '/login',
      query: { redirect: to.fullPath } // 保存目标路由，登录后跳转
    })
    return
  }

  if (to.path === '/login' && token) {
    // 已登录，访问登录页时重定向到首页
    next('/dashboard')
    return
  }

  // 权限检查
  if (to.meta.roles) {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const userRole = userInfo.role || 'STUDENT'

    if (!to.meta.roles.includes(userRole)) {
      ElMessage.error('您没有权限访问该页面')
      next({ path: '/dashboard', replace: true })
      return
    }
  }

  next()
})

// 路由后置守卫
router.afterEach((to, from) => {
  // 可以在这里添加页面加载完成后的逻辑
  // 例如：关闭全局loading、统计PV等
})

// 路由错误处理
router.onError((error) => {
  console.error('路由错误:', error)

  if (error.message.includes('Failed to fetch dynamically imported module')) {
    ElMessage.error('页面加载失败，正在刷新...')
    // 模块加载失败，可能是新部署导致的，刷新页面
    window.location.reload()
  } else if (error.message.includes('ChunkLoadError')) {
    ElMessage.error('资源加载失败，请刷新页面')
  } else {
    ElMessage.error('页面加载出错')
  }
})

export default router
