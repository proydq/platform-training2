<template>
  <header class="system-header">
    <div class="header-container">
      <!-- 左侧 Logo 和导航 -->
      <div class="header-left">
        <div class="logo-section">
          <div class="logo">
            <span class="logo-icon">🎓</span>
            <span class="logo-text">智能培训系统</span>
          </div>
        </div>

        <nav class="main-nav">
          <router-link to="/dashboard" class="nav-item">
            <span class="nav-icon">📊</span>
            <span class="nav-text">仪表板</span>
          </router-link>
          <router-link to="/courses" class="nav-item">
            <span class="nav-icon">📚</span>
            <span class="nav-text">我的课程</span>
          </router-link>
        </nav>
      </div>

      <!-- 右侧用户信息和工具 -->
      <div class="header-right">
        <!-- 搜索框 -->
        <div class="search-container">
          <div class="search-box">
            <input
              v-model="searchKeyword"
              type="text"
              placeholder="搜索课程、笔记..."
              class="search-input"
              @keyup.enter="handleSearch"
            />
            <button @click="handleSearch" class="search-btn">
              <span class="search-icon">🔍</span>
            </button>
          </div>
        </div>

        <!-- 通知 -->
        <div class="notification-container">
          <button @click="toggleNotifications" class="notification-btn">
            <span class="notification-icon">🔔</span>
            <span v-if="unreadCount > 0" class="notification-badge">{{ unreadCount }}</span>
          </button>

          <transition name="dropdown-fade">
            <div v-if="showNotifications" class="notification-dropdown">
              <div class="dropdown-header">
                <h3>通知</h3>
                <button @click="markAllAsRead" class="mark-read-btn">全部已读</button>
              </div>
              <div class="notification-list">
                <div
                  v-for="notification in notifications"
                  :key="notification.id"
                  class="notification-item"
                  :class="{ unread: !notification.read }"
                >
                  <div class="notification-icon">{{ notification.icon }}</div>
                  <div class="notification-content">
                    <div class="notification-title">{{ notification.title }}</div>
                    <div class="notification-time">{{ formatTime(notification.time) }}</div>
                  </div>
                </div>
              </div>
            </div>
          </transition>
        </div>

        <!-- 用户菜单 -->
        <div class="user-container">
          <button @click="toggleUserMenu" class="user-btn">
            <img :src="userAvatar" :alt="userName" class="user-avatar" />
            <span class="user-name">{{ userName }}</span>
            <span class="dropdown-arrow">▼</span>
          </button>

          <transition name="dropdown-fade">
            <div v-if="showUserMenu" class="user-dropdown">
              <div class="dropdown-header">
                <img :src="userAvatar" :alt="userName" class="user-avatar-large" />
                <div class="user-info">
                  <div class="user-name-large">{{ userName }}</div>
                  <div class="user-role">{{ userRole }}</div>
                </div>
              </div>
              <div class="dropdown-divider"></div>
              <div class="dropdown-menu">
                <button @click="goToProfile" class="menu-item">
                  <span class="menu-icon">👤</span>
                  <span class="menu-text">个人资料</span>
                </button>
                <button @click="goToSettings" class="menu-item">
                  <span class="menu-icon">⚙️</span>
                  <span class="menu-text">设置</span>
                </button>
                <button @click="toggleTheme" class="menu-item">
                  <span class="menu-icon">{{ isDarkMode ? '☀️' : '🌙' }}</span>
                  <span class="menu-text">{{ isDarkMode ? '浅色模式' : '深色模式' }}</span>
                </button>
                <div class="dropdown-divider"></div>
                <button @click="logout" class="menu-item logout">
                  <span class="menu-icon">🚪</span>
                  <span class="menu-text">退出登录</span>
                </button>
              </div>
            </div>
          </transition>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 响应式数据
const searchKeyword = ref('')
const showNotifications = ref(false)
const showUserMenu = ref(false)
const isDarkMode = ref(false)

// 用户信息
const userName = ref('张三')
const userRole = ref('产品经理')
const userAvatar = ref('https://api.dicebear.com/7.x/avataaars/svg?seed=Zhang')

// 通知数据
const notifications = ref([
  {
    id: 1,
    title: '新课程《高级数据分析》已发布',
    icon: '📚',
    time: '2025-01-20T10:30:00Z',
    read: false
  },
  {
    id: 2,
    title: '你的学习进度已达到80%',
    icon: '🎯',
    time: '2025-01-20T09:15:00Z',
    read: false
  },
  {
    id: 3,
    title: '课程《产品基础》有新的讨论',
    icon: '💬',
    time: '2025-01-19T16:45:00Z',
    read: true
  }
])

// 计算未读通知数量
const unreadCount = computed(() => {
  return notifications.value.filter(n => !n.read).length
})

// 搜索处理
const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    console.log('搜索:', searchKeyword.value)
    // 这里可以实现搜索逻辑
  }
}

// 切换通知面板
const toggleNotifications = () => {
  showNotifications.value = !showNotifications.value
  showUserMenu.value = false
}

// 切换用户菜单
const toggleUserMenu = () => {
  showUserMenu.value = !showUserMenu.value
  showNotifications.value = false
}

// 标记所有通知为已读
const markAllAsRead = () => {
  notifications.value.forEach(n => n.read = true)
}

// 格式化时间
const formatTime = (timeString) => {
  const date = new Date(timeString)
  const now = new Date()
  const diff = now - date

  if (diff < 3600000) { // 1小时内
    const minutes = Math.floor(diff / 60000)
    return `${minutes}分钟前`
  } else if (diff < 86400000) { // 24小时内
    const hours = Math.floor(diff / 3600000)
    return `${hours}小时前`
  } else {
    const days = Math.floor(diff / 86400000)
    return `${days}天前`
  }
}

// 导航操作
const goToProfile = () => {
  router.push('/profile')
  showUserMenu.value = false
}

const goToSettings = () => {
  router.push('/settings')
  showUserMenu.value = false
}

const toggleTheme = () => {
  isDarkMode.value = !isDarkMode.value
  // 这里可以实现主题切换逻辑
  document.body.classList.toggle('dark-mode', isDarkMode.value)
}

const logout = () => {
  if (confirm('确定要退出登录吗？')) {
    // 清除登录状态
    localStorage.removeItem('token')
    router.push('/login')
  }
  showUserMenu.value = false
}

// 点击外部关闭下拉菜单
const handleClickOutside = (event) => {
  if (!event.target.closest('.notification-container')) {
    showNotifications.value = false
  }
  if (!event.target.closest('.user-container')) {
    showUserMenu.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.system-header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 1000;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.header-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 70px;
}

/* 左侧区域 */
.header-left {
  display: flex;
  align-items: center;
  gap: 40px;
}

.logo-section {
  display: flex;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  text-decoration: none;
  color: #2c3e50;
}

.logo-icon {
  font-size: 28px;
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.main-nav {
  display: flex;
  gap: 20px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border-radius: 25px;
  text-decoration: none;
  color: #666;
  font-weight: 500;
  transition: all 0.3s ease;
}

.nav-item:hover {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
}

.nav-item.router-link-active {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
}

.nav-icon {
  font-size: 16px;
}

/* 右侧区域 */
.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

/* 搜索框 */
.search-container {
  position: relative;
}

.search-box {
  display: flex;
  align-items: center;
  background: #f8f9fa;
  border-radius: 25px;
  padding: 8px 16px;
  transition: all 0.3s ease;
}

.search-box:focus-within {
  background: white;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.search-input {
  border: none;
  background: none;
  outline: none;
  width: 200px;
  padding: 0;
  font-size: 14px;
}

.search-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px;
  margin-left: 8px;
  border-radius: 50%;
  color: #666;
  transition: all 0.3s ease;
}

.search-btn:hover {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
}

/* 通知 */
.notification-container {
  position: relative;
}

.notification-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 10px;
  border-radius: 50%;
  color: #666;
  transition: all 0.3s ease;
  position: relative;
}

.notification-btn:hover {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
}

.notification-badge {
  position: absolute;
  top: 5px;
  right: 5px;
  background: #ff4757;
  color: white;
  border-radius: 50%;
  width: 18px;
  height: 18px;
  font-size: 11px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
}

/* 用户菜单 */
.user-container {
  position: relative;
}

.user-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  background: none;
  border: none;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 25px;
  transition: all 0.3s ease;
}

.user-btn:hover {
  background: rgba(102, 126, 234, 0.1);
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: 2px solid #e9ecef;
}

.user-name {
  font-weight: 500;
  color: #2c3e50;
}

.dropdown-arrow {
  font-size: 10px;
  color: #666;
  transition: transform 0.3s ease;
}

.user-btn:hover .dropdown-arrow {
  transform: rotate(180deg);
}

/* 下拉菜单通用样式 */
.notification-dropdown,
.user-dropdown {
  position: absolute;
  top: 100%;
  right: 0;
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
  border: 1px solid #e9ecef;
  min-width: 280px;
  z-index: 1001;
  overflow: hidden;
}

.dropdown-header {
  padding: 20px;
  background: #f8f9fa;
  border-bottom: 1px solid #e9ecef;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dropdown-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
}

.mark-read-btn {
  background: none;
  border: none;
  color: #667eea;
  cursor: pointer;
  font-size: 12px;
  font-weight: 500;
}

/* 通知列表 */
.notification-list {
  max-height: 300px;
  overflow-y: auto;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 15px 20px;
  border-bottom: 1px solid #f1f3f4;
  transition: background 0.3s ease;
}

.notification-item:hover {
  background: #f8f9fa;
}

.notification-item.unread {
  background: rgba(102, 126, 234, 0.05);
  border-left: 3px solid #667eea;
}

.notification-item .notification-icon {
  font-size: 20px;
  flex-shrink: 0;
}

.notification-content {
  flex: 1;
}

.notification-title {
  font-size: 14px;
  font-weight: 500;
  color: #2c3e50;
  margin-bottom: 4px;
}

.notification-time {
  font-size: 12px;
  color: #666;
}

/* 用户下拉菜单 */
.user-dropdown .dropdown-header {
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 12px;
}

.user-avatar-large {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  border: 3px solid #e9ecef;
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.user-name-large {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
}

.user-role {
  font-size: 12px;
  color: #666;
}

.dropdown-divider {
  height: 1px;
  background: #e9ecef;
  margin: 0;
}

.dropdown-menu {
  padding: 8px 0;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  padding: 12px 20px;
  background: none;
  border: none;
  cursor: pointer;
  font-size: 14px;
  color: #2c3e50;
  transition: all 0.3s ease;
  text-align: left;
}

.menu-item:hover {
  background: #f8f9fa;
}

.menu-item.logout {
  color: #dc3545;
}

.menu-item.logout:hover {
  background: rgba(220, 53, 69, 0.1);
}

.menu-icon {
  font-size: 16px;
  flex-shrink: 0;
}

/* 下拉动画 */
.dropdown-fade-enter-active,
.dropdown-fade-leave-active {
  transition: all 0.3s ease;
}

.dropdown-fade-enter-from,
.dropdown-fade-leave-to {
  opacity: 0;
  transform: translateY(-10px) scale(0.95);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-container {
    padding: 0 15px;
  }

  .header-left {
    gap: 20px;
  }

  .main-nav {
    display: none;
  }

  .search-input {
    width: 150px;
  }

  .user-name {
    display: none;
  }

  .notification-dropdown,
  .user-dropdown {
    min-width: 250px;
  }
}

/* 深色模式 */
.dark-mode .system-header {
  background: rgba(30, 30, 30, 0.95);
  border-bottom-color: rgba(255, 255, 255, 0.1);
}

.dark-mode .logo-text {
  color: white;
}

.dark-mode .nav-item {
  color: #ccc;
}

.dark-mode .search-box {
  background: rgba(255, 255, 255, 0.1);
}

.dark-mode .notification-dropdown,
.dark-mode .user-dropdown {
  background: #2c2c2c;
  border-color: #444;
}
</style>
