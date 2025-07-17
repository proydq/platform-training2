<template>
  <div class="layout-container">
    <!-- 头部导航 -->
    <el-header class="layout-header">
      <div class="header-left">
        <div class="logo">
          <div class="logo-icon">🎓</div>
          <h1 class="logo-title">智能培训系统</h1>
        </div>
      </div>
      
      <div class="header-center">
        <el-menu
          :default-active="activeMenu"
          class="header-menu"
          mode="horizontal"
          @select="handleMenuSelect"
        >
          <el-menu-item
            v-for="menu in visibleMenus"
            :key="menu.path"
            :index="menu.path"
          >
            <el-icon v-if="menu.icon">
              <component :is="menu.icon" />
            </el-icon>
            <span>{{ menu.title }}</span>
          </el-menu-item>
        </el-menu>
      </div>
      
      <div class="header-right">
        <div class="user-info">
          <span class="user-name">{{ userStore.userName }}</span>
          <el-avatar class="user-avatar" :size="36">
            {{ userStore.userAvatar }}
          </el-avatar>
          <el-dropdown @command="handleUserCommand">
            <el-button type="text" class="user-dropdown">
              <el-icon><ArrowDown /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人资料</el-dropdown-item>
                <el-dropdown-item command="settings">账号设置</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </el-header>
    
    <!-- 主要内容区域 -->
    <el-container class="layout-main">
      <!-- 面包屑导航 -->
      <div class="breadcrumb-container">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item to="/dashboard">首页</el-breadcrumb-item>
          <el-breadcrumb-item v-if="currentRouteMeta.title">
            {{ currentRouteMeta.title }}
          </el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      
      <!-- 页面内容 -->
      <el-main class="layout-content">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { generateMenus } from '@/router'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 当前激活的菜单
const activeMenu = computed(() => {
  const path = route.path
  // 处理子路由的情况
  if (path.includes('/courses')) return '/courses'
  if (path.includes('/exams')) return '/exams'
  if (path.includes('/student-management')) return '/student-management'
  if (path.includes('/admin')) return '/admin'
  return '/dashboard'
})

// 当前路由元信息
const currentRouteMeta = computed(() => route.meta || {})

// 可见的菜单项
const visibleMenus = computed(() => {
  const menus = generateMenus(userStore.userRole)
  return menus.filter(menu => !menu.hidden)
})

// 菜单选择处理
const handleMenuSelect = (index) => {
  if (index !== route.path) {
    router.push(index)
  }
}

// 用户下拉菜单处理
const handleUserCommand = async (command) => {
  switch (command) {
    case 'profile':
      ElMessage.info('个人资料功能开发中...')
      break
    case 'settings':
      ElMessage.info('账号设置功能开发中...')
      break
    case 'logout':
      await handleLogout()
      break
  }
}

// 退出登录处理
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要退出登录吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await userStore.logout()
    router.replace('/login')
  } catch (error) {
    // 用户取消或其他错误
  }
}

// 监听路由变化
watch(
  () => route.path,
  () => {
    // 可以在这里添加页面切换的逻辑
  }
)

onMounted(() => {
  // 初始化布局相关逻辑
})
</script>

<style scoped>
.layout-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.layout-header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.header-left {
  display: flex;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  font-size: 32px;
}

.logo-title {
  font-size: 20px;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin: 0;
}

.header-center {
  flex: 1;
  display: flex;
  justify-content: center;
}

.header-menu {
  border-bottom: none;
  background: transparent;
}

.header-menu :deep(.el-menu-item) {
  color: #333;
  font-weight: 500;
  margin: 0 10px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.header-menu :deep(.el-menu-item:hover) {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
}

.header-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border-bottom: none;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-name {
  font-weight: 500;
  color: #333;
}

.user-avatar {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  font-weight: bold;
}

.user-dropdown {
  color: #666;
  padding: 4px;
}

.layout-main {
  padding: 0;
}

.breadcrumb-container {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  padding: 15px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.breadcrumb-container :deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  color: #667eea;
  font-weight: 500;
}

.layout-content {
  padding: 20px;
  min-height: calc(100vh - 120px);
}

/* 页面切换动画 */
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s ease;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(30px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(-30px);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .layout-header {
    flex-direction: column;
    height: auto;
    padding: 10px;
    gap: 15px;
  }
  
  .header-center {
    width: 100%;
  }
  
  .header-menu {
    justify-content: center;
  }
  
  .logo-title {
    font-size: 18px;
  }
  
  .user-name {
    display: none;
  }
}

@media (max-width: 480px) {
  .layout-content {
    padding: 15px;
  }
  
  .breadcrumb-container {
    padding: 10px 15px;
  }
}
</style>