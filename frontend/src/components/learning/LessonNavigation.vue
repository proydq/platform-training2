<template>
  <div class="lesson-navigation">
    <!-- 课程完成状态 -->
    <div class="completion-section">
      <div class="completion-content">
        <div class="completion-icon">
          <div class="checkmark-container">
            <svg class="checkmark" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 52 52">
              <circle class="checkmark-circle" cx="26" cy="26" r="25" fill="none"/>
              <path class="checkmark-check" fill="none" d="m14.1 27.2l7.1 7.2 16.7-16.8"/>
            </svg>
          </div>
        </div>
        <div class="completion-text">
          <h3 class="completion-title">恭喜！课程学习完成</h3>
          <p class="completion-description">您已经完成了本节课的全部内容，继续保持学习的热情！</p>
        </div>
        <button @click="$emit('completeLesson')" class="complete-btn">
          <span class="btn-icon">🎉</span>
          <span class="btn-text">标记为已完成</span>
        </button>
      </div>
    </div>

    <!-- 导航按钮区域 -->
    <div class="navigation-controls">
      <!-- 上一课 -->
      <div class="nav-item prev-lesson">
        <button
          @click="$emit('previousLesson')"
          :disabled="!hasPrevious"
          class="nav-btn prev-btn"
          :class="{ disabled: !hasPrevious }"
        >
          <div class="nav-btn-content">
            <div class="nav-arrow">←</div>
            <div class="nav-info">
              <span class="nav-label">上一课</span>
              <span v-if="hasPrevious" class="nav-title">{{ previousTitle }}</span>
              <span v-else class="nav-title">已是第一课</span>
            </div>
          </div>
        </button>
      </div>

      <!-- 课程目录 -->
      <div class="nav-item course-menu">
        <button @click="toggleCourseMenu" class="nav-btn menu-btn">
          <div class="nav-btn-content">
            <div class="menu-icon">📚</div>
            <div class="nav-info">
              <span class="nav-label">课程目录</span>
              <span class="nav-title">查看全部课程</span>
            </div>
          </div>
        </button>

        <!-- 课程目录下拉菜单 -->
        <transition name="menu-fade">
          <div v-if="showCourseMenu" class="course-menu-dropdown">
            <div class="menu-header">
              <h4>课程章节</h4>
              <button @click="showCourseMenu = false" class="close-menu-btn">✕</button>
            </div>
            <div class="menu-content">
              <div class="chapter-list">
                <div v-for="chapter in courseChapters" :key="chapter.id" class="chapter-item">
                  <div class="chapter-header">
                    <span class="chapter-title">{{ chapter.title }}</span>
                    <span class="chapter-progress">{{ chapter.completedLessons }}/{{ chapter.totalLessons }}</span>
                  </div>
                  <div class="lesson-list">
                    <div
                      v-for="lesson in chapter.lessons"
                      :key="lesson.id"
                      class="lesson-item"
                      :class="{
                        active: lesson.id === currentLessonId,
                        completed: lesson.completed
                      }"
                      @click="goToLesson(lesson)"
                    >
                      <div class="lesson-status">
                        <span v-if="lesson.completed" class="status-icon completed">✓</span>
                        <span v-else-if="lesson.id === currentLessonId" class="status-icon current">▶</span>
                        <span v-else class="status-icon pending">○</span>
                      </div>
                      <div class="lesson-info">
                        <span class="lesson-title">{{ lesson.title }}</span>
                        <span class="lesson-duration">{{ lesson.duration }}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </transition>
      </div>

      <!-- 下一课 -->
      <div class="nav-item next-lesson">
        <button
          @click="$emit('nextLesson')"
          :disabled="!hasNext"
          class="nav-btn next-btn"
          :class="{ disabled: !hasNext }"
        >
          <div class="nav-btn-content">
            <div class="nav-info">
              <span class="nav-label">下一课</span>
              <span v-if="hasNext" class="nav-title">{{ nextTitle }}</span>
              <span v-else class="nav-title">已是最后一课</span>
            </div>
            <div class="nav-arrow">→</div>
          </div>
        </button>
      </div>
    </div>

    <!-- 学习统计 -->
    <div class="learning-stats">
      <div class="stat-item">
        <div class="stat-icon">📊</div>
        <div class="stat-content">
          <div class="stat-value">{{ learningProgress }}%</div>
          <div class="stat-label">课程进度</div>
        </div>
      </div>
      <div class="stat-item">
        <div class="stat-icon">⏱️</div>
        <div class="stat-content">
          <div class="stat-value">{{ totalLearningTime }}</div>
          <div class="stat-label">学习时长</div>
        </div>
      </div>
      <div class="stat-item">
        <div class="stat-icon">🎯</div>
        <div class="stat-content">
          <div class="stat-value">{{ completedLessons }}</div>
          <div class="stat-label">已完成</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  hasPrevious: {
    type: Boolean,
    default: false
  },
  hasNext: {
    type: Boolean,
    default: false
  },
  previousTitle: {
    type: String,
    default: ''
  },
  nextTitle: {
    type: String,
    default: ''
  }
})

defineEmits(['previousLesson', 'nextLesson', 'completeLesson'])

// 本地状态
const showCourseMenu = ref(false)
const currentLessonId = ref(2) // 当前课程ID

// 模拟课程数据
const courseChapters = ref([
  {
    id: 1,
    title: '第一章 产品基础',
    completedLessons: 2,
    totalLessons: 3,
    lessons: [
      { id: 1, title: '产品概念介绍', duration: '15分钟', completed: true },
      { id: 2, title: '产品基础知识培训', duration: '45分钟', completed: false },
      { id: 3, title: '产品设计原理', duration: '30分钟', completed: false }
    ]
  },
  {
    id: 2,
    title: '第二章 用户研究',
    completedLessons: 0,
    totalLessons: 4,
    lessons: [
      { id: 4, title: '用户研究方法', duration: '40分钟', completed: false },
      { id: 5, title: '用户画像建立', duration: '35分钟', completed: false },
      { id: 6, title: '用户调研实践', duration: '50分钟', completed: false },
      { id: 7, title: '数据分析技巧', duration: '45分钟', completed: false }
    ]
  }
])

// 计算学习统计
const learningProgress = computed(() => {
  const totalLessons = courseChapters.value.reduce((sum, chapter) => sum + chapter.totalLessons, 0)
  const completedTotal = courseChapters.value.reduce((sum, chapter) => sum + chapter.completedLessons, 0)
  return Math.round((completedTotal / totalLessons) * 100)
})

const totalLearningTime = computed(() => {
  return '2小时30分钟' // 这里可以根据实际数据计算
})

const completedLessons = computed(() => {
  return courseChapters.value.reduce((sum, chapter) => sum + chapter.completedLessons, 0)
})

// 切换课程菜单
const toggleCourseMenu = () => {
  showCourseMenu.value = !showCourseMenu.value
}

// 跳转到指定课程
const goToLesson = (lesson) => {
  currentLessonId.value = lesson.id
  showCourseMenu.value = false
  console.log('跳转到课程:', lesson.title)
  // 这里可以实现实际的课程跳转逻辑
}

// 点击外部关闭菜单
const handleClickOutside = (event) => {
  if (!event.target.closest('.course-menu')) {
    showCourseMenu.value = false
  }
}

// 监听点击事件
import { onMounted, onUnmounted } from 'vue'

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.lesson-navigation {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 0 0 15px 15px;
  padding: 30px 40px;
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.1);
}

/* 完成状态区域 */
.completion-section {
  margin-bottom: 30px;
  padding: 25px;
  background: linear-gradient(135deg, #e8f5e8, #f0f8f0);
  border-radius: 15px;
  border: 1px solid #d4edda;
}

.completion-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.completion-icon {
  flex-shrink: 0;
}

.checkmark-container {
  width: 50px;
  height: 50px;
}

.checkmark {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: block;
  stroke-width: 2;
  stroke: #28a745;
  stroke-miterlimit: 10;
}

.checkmark-circle {
  stroke-dasharray: 166;
  stroke-dashoffset: 166;
  stroke-width: 2;
  stroke-miterlimit: 10;
  stroke: #28a745;
  fill: none;
  animation: stroke 0.6s cubic-bezier(0.65, 0, 0.45, 1) forwards;
}

.checkmark-check {
  transform-origin: 50% 50%;
  stroke-dasharray: 48;
  stroke-dashoffset: 48;
  stroke-width: 3;
  stroke: #28a745;
  animation: stroke 0.3s cubic-bezier(0.65, 0, 0.45, 1) 0.8s forwards;
}

@keyframes stroke {
  100% {
    stroke-dashoffset: 0;
  }
}

.completion-text {
  flex: 1;
}

.completion-title {
  font-size: 18px;
  font-weight: 600;
  color: #155724;
  margin: 0 0 5px 0;
}

.completion-description {
  font-size: 14px;
  color: #155724;
  margin: 0;
  opacity: 0.8;
}

.complete-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  background: linear-gradient(135deg, #28a745, #20c997);
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 25px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(40, 167, 69, 0.3);
}

.complete-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(40, 167, 69, 0.4);
}

/* 导航控制区域 */
.navigation-controls {
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  gap: 20px;
  margin-bottom: 30px;
  align-items: stretch;
}

.nav-item {
  position: relative;
}

.nav-btn {
  width: 100%;
  background: white;
  border: 2px solid #e9ecef;
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
}

.nav-btn:hover:not(.disabled) {
  border-color: #667eea;
  background: #f8f9ff;
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.15);
}

.nav-btn.disabled {
  opacity: 0.5;
  cursor: not-allowed;
  background: #f8f9fa;
}

.nav-btn-content {
  display: flex;
  align-items: center;
  gap: 15px;
  width: 100%;
}

.nav-arrow {
  font-size: 20px;
  font-weight: bold;
  color: #667eea;
}

.menu-icon {
  font-size: 24px;
}

.nav-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
}

.nav-label {
  font-size: 12px;
  color: #666;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.nav-title {
  font-size: 14px;
  font-weight: 600;
  color: #2c3e50;
}

/* 课程菜单样式 */
.course-menu {
  min-width: 200px;
}

.menu-btn .nav-btn-content {
  justify-content: center;
  text-align: center;
}

.course-menu-dropdown {
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  background: white;
  border-radius: 15px;
  box-shadow: 0 15px 50px rgba(0, 0, 0, 0.15);
  border: 1px solid #e9ecef;
  width: 350px;
  z-index: 1000;
  overflow: hidden;
  margin-top: 10px;
}

.menu-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: #f8f9fa;
  border-bottom: 1px solid #e9ecef;
}

.menu-header h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
}

.close-menu-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 5px;
  color: #666;
  border-radius: 50%;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-menu-btn:hover {
  background: #e9ecef;
}

.menu-content {
  max-height: 400px;
  overflow-y: auto;
  padding: 15px 0;
}

.chapter-item {
  margin-bottom: 20px;
}

.chapter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px 10px 20px;
  border-bottom: 1px solid #f1f3f4;
}

.chapter-title {
  font-size: 14px;
  font-weight: 600;
  color: #2c3e50;
}

.chapter-progress {
  font-size: 12px;
  color: #666;
  background: #f8f9fa;
  padding: 4px 8px;
  border-radius: 10px;
}

.lesson-list {
  padding: 10px 0;
}

.lesson-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.lesson-item:hover {
  background: #f8f9fa;
}

.lesson-item.active {
  background: rgba(102, 126, 234, 0.1);
  border-left: 3px solid #667eea;
}

.lesson-item.completed {
  opacity: 0.7;
}

.lesson-status {
  flex-shrink: 0;
}

.status-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  font-size: 12px;
  font-weight: bold;
}

.status-icon.completed {
  background: #28a745;
  color: white;
}

.status-icon.current {
  background: #667eea;
  color: white;
}

.status-icon.pending {
  background: #f8f9fa;
  color: #999;
  border: 1px solid #e9ecef;
}

.lesson-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
}

.lesson-title {
  font-size: 13px;
  font-weight: 500;
  color: #2c3e50;
}

.lesson-duration {
  font-size: 11px;
  color: #666;
}

/* 学习统计 */
.learning-stats {
  display: flex;
  justify-content: center;
  gap: 40px;
  padding-top: 20px;
  border-top: 1px solid #f1f3f4;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.stat-icon {
  font-size: 20px;
  width: 40px;
  height: 40px;
  background: rgba(102, 126, 234, 0.1);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-content {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.stat-value {
  font-size: 18px;
  font-weight: 700;
  color: #2c3e50;
}

.stat-label {
  font-size: 12px;
  color: #666;
}

/* 动画 */
.menu-fade-enter-active,
.menu-fade-leave-active {
  transition: all 0.3s ease;
}

.menu-fade-enter-from,
.menu-fade-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(-10px) scale(0.95);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .lesson-navigation {
    padding: 20px;
  }

  .completion-content {
    flex-direction: column;
    text-align: center;
    gap: 15px;
  }

  .navigation-controls {
    grid-template-columns: 1fr;
    gap: 15px;
  }

  .nav-btn {
    padding: 15px;
  }

  .course-menu-dropdown {
    width: 300px;
  }

  .learning-stats {
    flex-direction: column;
    gap: 20px;
    align-items: center;
  }
}

/* 滚动条样式 */
.menu-content::-webkit-scrollbar {
  width: 4px;
}

.menu-content::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.menu-content::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 2px;
}

.menu-content::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>
