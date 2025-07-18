<template>
  <div class="layout-container">
    <!-- 头部导航 - 按原始设计 -->
    <div class="header">
      <div class="logo">
        <div class="logo-icon">🎓</div>
        <h1>智能培训系统</h1>
      </div>
      
      <div class="nav-menu">
        <div 
          v-for="menu in visibleMenus" 
          :key="menu.path"
          class="nav-item"
          :class="{ active: activeMenu === menu.path }"
          @click="handleMenuSelect(menu.path)"
        >
          {{ menu.title }}
        </div>
      </div>
      
      <div class="user-info">
        <div class="user-name">{{ userName }}</div>
        <div class="avatar">{{ userAvatar }}</div>
        <button class="logout-btn" @click="handleLogout">退出登录</button>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content-wrapper">
      <router-view v-slot="{ Component }">
        <transition name="fade-transform" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { generateMenus } from '@/router'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 当前激活的菜单
const activeMenu = computed(() => {
  return route.path
})

// 用户信息
const userName = computed(() => {
  return userStore.userInfo?.name || userStore.userInfo?.username || '用户'
})

const userAvatar = computed(() => {
  const name = userName.value
  return name.charAt(0).toUpperCase()
})

// 可见的菜单项
const visibleMenus = computed(() => {
  try {
    const userRole = userStore.userInfo?.role || 'STUDENT'
    console.log('当前用户角色:', userRole)
    const menus = generateMenus(userRole)
    return menus.filter(menu => !menu.hidden)
  } catch (error) {
    console.error('生成菜单失败:', error)
    // 返回默认菜单
    return [
      { path: '/dashboard', title: '仪表板', hidden: false },
      { path: '/courses', title: '我的课程', hidden: false },
      { path: '/exams', title: '考试中心', hidden: false }
    ]
  }
})

// 菜单选择处理
const handleMenuSelect = (path) => {
  if (path !== route.path) {
    router.push(path)
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
    ElMessage.success('已退出登录')
    router.replace('/login')
  } catch (error) {
    // 用户取消
    console.log('用户取消退出登录')
  }
}

onMounted(() => {
  console.log('布局组件已挂载')
  console.log('当前用户信息:', userStore.userInfo)
  console.log('当前路由:', route.path)
})
</script>

<style scoped>
/* 全屏背景布局 */
.layout-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  /* 移除max-width限制，让背景平铺整个屏幕 */
  width: 100%;
  padding: 20px;
  /* 使用固定定位确保背景覆盖整个视口 */
  position: relative;
}

/* 为了确保背景完全平铺，在body上也设置背景 */
.layout-container::before {
  content: '';
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  z-index: -1;
}

/* 内容区域居中，但背景平铺 */
.header,
.main-content-wrapper {
  max-width: 1400px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

.header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 15px;
  padding: 20px 30px;
  margin-bottom: 30px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  gap: 15px;
}

.logo-icon {
  font-size: 32px;
}

.logo h1 {
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  font-size: 24px;
  font-weight: 700;
  margin: 0;
}

.nav-menu {
  display: flex;
  gap: 30px;
}

.nav-item {
  padding: 10px 20px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 500;
  color: #333;
}

.nav-item:hover,
.nav-item.active {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  transform: translateY(-2px);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.user-name {
  font-weight: 500;
  color: #333;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: bold;
  font-size: 16px;
}

.logout-btn {
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 8px;
  color: #667eea;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 14px;
}

.logout-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-1px);
}

.main-content-wrapper {
  min-height: calc(100vh - 140px);
  position: relative;
  z-index: 1;
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
  .layout-container {
    padding: 10px;
  }
  
  .header {
    flex-direction: column;
    gap: 20px;
    padding: 20px;
  }
  
  .nav-menu {
    flex-wrap: wrap;
    gap: 15px;
    justify-content: center;
  }
  
  .logo h1 {
    font-size: 20px;
  }
  
  .user-name {
    display: none;
  }
}

@media (max-width: 480px) {
  .nav-menu {
    gap: 10px;
  }
  
  .nav-item {
    padding: 8px 15px;
    font-size: 14px;
  }
}
</style>