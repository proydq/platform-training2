// frontend/src/router/index.js
import { createRouter, createWebHistory } from 'vue-router'

// 🔧 修复：使用显式导入替代动态导入，避免路径解析问题
import Login from '@/views/Login.vue'
import Layout from '@/layout/index.vue'
import Dashboard from '@/views/Dashboard.vue'
import Courses from '@/views/Courses.vue'
import CourseManagement from '@/views/CourseManagement.vue'
import Exams from '@/views/Exams.vue'
import StudentManagement from '@/views/StudentManagement.vue'
import Admin from '@/views/Admin.vue'
import NotFound from '@/views/NotFound.vue'
// ✨ 新增：导入学习页面组件
import LearningPage from '@/views/LearningPage.vue'

// 🔧 备选方案：如果需要懒加载，使用绝对路径
// const Login = () => import('/src/views/Login.vue')
// const Layout = () => import('/src/layout/index.vue')
// const Dashboard = () => import('/src/views/Dashboard.vue')
// const Courses = () => import('/src/views/Courses.vue')
// const CourseManagement = () => import('/src/views/CourseManagement.vue')
// const Exams = () => import('/src/views/Exams.vue')
// const StudentManagement = () => import('/src/views/StudentManagement.vue')
// const Admin = () => import('/src/views/Admin.vue')
// const NotFound = () => import('/src/views/NotFound.vue')
// const LearningPage = () => import('/src/views/LearningPage.vue')

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
  // ✨ 新增：学习页面路由 - 独立布局，不在Layout内
  {
    path: '/learning/:courseId',
    name: 'LearningPage',
    component: LearningPage,
    meta: {
      title: '课程学习',
      requiresAuth: true
    }
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
    ]
  },
  {
    path: '/404',
    name: 'NotFound',
    component: NotFound,
    meta: { title: '页面未找到' }
  },
  {
    path: '/admin',
    component: Layout,
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Admin',
        component: Admin,
        meta: {
          title: '管理后台',
          icon: '⚙️',
          roles: ['ADMIN']
        }
      },
      {
        path: 'user-management',
        name: 'UserManagement',
        component: () => import('@/views/UserManagement.vue'),
        meta: {
          title: '用户管理',
          icon: '👤',
          roles: ['ADMIN']
        }
      }
    ]
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
      path: '/admin',
      title: '管理后台',
      icon: '⚙️',
      // 强制隐藏导航入口，但保留路由权限
      hidden: true
    },
    {
      path: '/admin/user-management',
      title: '用户管理',
      icon: '👤',
      // 子菜单仅在ADMIN角色下显示
      hidden: !['ADMIN'].includes(userRole)
    }
  ]

  return allMenus.filter(menu => !menu.hidden)
}

// 🔧 改进路由守卫 - 添加更好的错误处理
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

  // 🔧 改进权限检查
  if (to.meta.roles) {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const userRole = userInfo.role || 'STUDENT'

    if (!to.meta.roles.includes(userRole)) {
      console.log(`权限不足: 用户角色${userRole}无法访问${to.path}，跳转到仪表板`)
      // 🔧 使用replace避免历史记录堆积
      next({ path: '/dashboard', replace: true })
      return
    }
  }

  next()
})

// 🔧 增强路由错误处理
router.onError((error) => {
  console.error('路由错误:', error)

  // 🔧 如果是动态导入失败，尝试重新导航到dashboard
  if (error.message.includes('Failed to fetch dynamically imported module')) {
    console.log('动态导入失败，重定向到仪表板')
    router.replace('/dashboard').catch(err => {
      console.error('重定向失败:', err)
      // 最后的备选方案：重新加载页面
      window.location.href = '/dashboard'
    })
  }
})

export default router
