<template>
  <div class="system-header">
    <div class="container">
      <div class="system-nav">
        <div class="logo">
          <div class="logo-icon">🎓</div>
          <h1>智能培训系统</h1>
        </div>

        <div class="nav-menu">
          <div class="nav-item" @click="goToPage('/dashboard')">📊 仪表板</div>
          <div class="nav-item active" @click="goToPage('/courses')">📚 我的课程</div>
          <div class="nav-item" @click="goToPage('/exams')">📝 考试中心</div>
          <div v-if="showStudentManagement" class="nav-item" @click="goToPage('/students')">👥 学员管理</div>
          <div v-if="showAdminPanel" class="nav-item" @click="goToPage('/admin')">⚙️ 管理后台</div>
        </div>

        <div class="user-info">
          <div class="user-name">{{ userName }}</div>
          <div class="avatar">{{ userAvatar }}</div>
          <button class="logout-btn" @click="handleLogout">退出登录</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// 用户信息
const userName = computed(() => {
  return userStore.userInfo?.name || userStore.userInfo?.username || '用户'
})

const userAvatar = computed(() => {
  const name = userName.value
  return name.charAt(0).toUpperCase()
})

const userRole = computed(() => {
  return userStore.userInfo?.role || 'STUDENT'
})

const showStudentManagement = computed(() => {
  return ['ADMIN', 'TEACHER'].includes(userRole.value)
})

const showAdminPanel = computed(() => {
  return userRole.value === 'ADMIN'
})

// 导航方法
const goToPage = (path) => {
  router.push(path)
}

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
</script>

<style scoped>
/* 系统顶部导航 - 与Layout组件完全一致 */
.system-header {
  background: transparent;
  position: relative;
  z-index: 1000;
  padding: 20px;
}

.system-nav {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 15px;
  padding: 20px 30px;
  margin-bottom: 30px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1400px;
  margin: 0 auto 30px;
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

.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .system-header {
    padding: 10px;
  }

  .system-nav {
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
