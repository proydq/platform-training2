// frontend/src/router/index.js
import { createRouter, createWebHistory } from 'vue-router'

// 组件导入
import Login from '@/views/Login.vue'
import Layout from '@/layout/index.vue'
import Dashboard from '@/views/Dashboard.vue'
import Courses from '@/views/Courses.vue'
import CourseManagement from '@/views/CourseManagement.vue'
import Exams from '@/views/Exams.vue'
import StudentManagement from '@/views/StudentManagement.vue'
import Admin from '@/views/Admin.vue'
import UserManagement from '@/views/UserManagement.vue' // ✅ 必须添加这个导入
import NotFound from '@/views/NotFound.vue'
//import LearningPage from '@/views/LearningPage.vue'
import EnhancedLearning from '@/views/EnhancedLearning.vue' // 使用新的增强版学习页面
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
          roles: ['ADMIN', 'TEACHER']
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
          roles: ['ADMIN', 'TEACHER']
        }
      },
      {
        path: 'admin',
        name: 'Admin',
        component: Admin,
        meta: {
          title: '管理后台',
          icon: '⚙️',
          roles: ['ADMIN']
        }
      },
      // ✅ 这是关键！添加用户管理路由
      {
        path: 'admin/user-management',
        name: 'UserManagement',
        component: UserManagement,
        meta: {
          title: '用户管理',
          icon: '👤',
          roles: ['ADMIN']
        }
      },
      /*{
        path: 'learning/:courseId',
        name: 'LearningPage',
        component: LearningPage,
        meta: {
          title: '课程学习',
          icon: '📚',
          requiresAuth: true
        }
      }*/
      {
        path: 'learning/:courseId',
        name: 'EnhancedLearning', // 更改名称
        component: EnhancedLearning, // 使用新的增强版学习页面
        meta: {
          title: '课程学习',
          icon: '📚',
          requiresAuth: true
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

  console.log('用户角色:', userRole, '生成菜单:', allMenus.filter(menu => !menu.hidden))
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
    return
  }

  if (to.path === '/login' && token) {
    console.log('已登录，跳转到仪表板')
    next('/dashboard')
    return
  }

  // 权限检查
  if (to.meta.roles) {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const userRole = userInfo.role || 'STUDENT'

    if (!to.meta.roles.includes(userRole)) {
      console.log(`权限不足: 用户角色${userRole}无法访问${to.path}，跳转到仪表板`)
      next({ path: '/dashboard', replace: true })
      return
    }
  }

  next()
})

// 路由错误处理
router.onError((error) => {
  console.error('路由错误:', error)

  if (error.message.includes('Failed to fetch dynamically imported module')) {
    console.log('动态导入失败，重定向到仪表板')
    router.replace('/dashboard').catch(err => {
      console.error('重定向失败:', err)
      window.location.href = '/dashboard'
    })
  }
})

export default router
