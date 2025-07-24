<template>
  <div class="lesson-navigation">
    <!-- 导航按钮区域 -->
    <div class="navigation-controls">
      <button
        class="nav-btn prev-btn"
        :disabled="!hasPrevious"
        @click="$emit('previousLesson')"
      >
        <span class="nav-icon">←</span>
        <div class="nav-content">
          <div class="nav-label">上一节</div>
          <div class="nav-title" v-if="previousTitle">{{ previousTitle }}</div>
        </div>
      </button>

      <div class="lesson-actions">
        <button class="action-btn bookmark-btn" @click="toggleBookmark">
          <span class="action-icon">🔖</span>
          <span class="action-text">收藏</span>
        </button>

        <button class="action-btn notes-btn" @click="showNotesPanel">
          <span class="action-icon">📝</span>
          <span class="action-text">笔记</span>
        </button>

        <button class="action-btn progress-btn" @click="toggleCourseMenu">
          <span class="action-icon">📚</span>
          <span class="action-text">课程目录</span>
        </button>

        <button
          class="complete-btn"
          @click="$emit('completeLesson')"
        >
          <span class="complete-icon">✓</span>
          <span class="complete-text">完成学习</span>
        </button>
      </div>

      <button
        class="nav-btn next-btn"
        :disabled="!hasNext"
        @click="$emit('nextLesson')"
      >
        <div class="nav-content">
          <div class="nav-label">下一节</div>
          <div class="nav-title" v-if="nextTitle">{{ nextTitle }}</div>
        </div>
        <span class="nav-icon">→</span>
      </button>
    </div>

    <!-- 课程菜单面板 -->
    <div v-if="showCourseMenu" class="course-menu-panel">
      <div class="course-menu-header">
        <h3>课程目录</h3>
        <button @click="toggleCourseMenu" class="close-btn">×</button>
      </div>

      <div class="course-chapters">
        <div
          v-for="chapter in courseChapters"
          :key="chapter.id"
          class="chapter-section"
        >
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
                current: lesson.id === currentLessonId,
                completed: lesson.completed
              }"
              @click="goToLesson(lesson)"
            >
              <div class="lesson-status">
                <span v-if="lesson.completed" class="status-icon completed">✓</span>
                <span v-else-if="lesson.id === currentLessonId" class="status-icon current">▶</span>
                <span v-else class="status-icon">○</span>
              </div>
              <div class="lesson-info">
                <div class="lesson-title">{{ lesson.title }}</div>
                <div class="lesson-duration">{{ lesson.duration }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 学习统计面板 -->
    <div class="learning-stats">
      <div class="stat-item">
        <div class="stat-icon">📈</div>
        <div class="stat-content">
          <div class="stat-value">{{ learningProgress }}%</div>
          <div class="stat-label">学习进度</div>
        </div>
      </div>
      <div class="stat-item">
        <div class="stat-icon">⏰</div>
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

// 修复：直接接收props，不赋值给变量
defineProps({
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

// 切换收藏状态
const toggleBookmark = () => {
  console.log('切换收藏状态')
}

// 显示笔记面板
const showNotesPanel = () => {
  console.log('显示笔记面板')
}
</script>

<style scoped>
.lesson-navigation {
  background: #f8f9fa;
  border-top: 1px solid #e9ecef;
  position: relative;
}

/* 导航控制区域 */
.navigation-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 30px;
  gap: 20px;
}

.nav-btn {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  background: white;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 14px;
  min-width: 200px;
}

.nav-btn:hover:not(:disabled) {
  background: #f8f9fa;
  border-color: #667eea;
  transform: translateY(-2px);
}

.nav-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.nav-icon {
  font-size: 16px;
  color: #667eea;
}

.nav-content {
  flex: 1;
}

.nav-label {
  font-size: 12px;
  color: #666;
  margin-bottom: 2px;
}

.nav-title {
  font-weight: 500;
  color: #333;
}

.prev-btn {
  justify-content: flex-start;
}

.next-btn {
  justify-content: flex-end;
  flex-direction: row-reverse;
}

/* 中间操作区域 */
.lesson-actions {
  display: flex;
  align-items: center;
  gap: 15px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: white;
  border: 1px solid #e9ecef;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 13px;
}

.action-btn:hover {
  background: #f8f9fa;
  border-color: #667eea;
}

.action-icon {
  font-size: 14px;
}

.complete-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: linear-gradient(135deg, #28a745, #20c997);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 500;
}

.complete-btn:hover {
  background: linear-gradient(135deg, #20c997, #28a745);
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(40, 167, 69, 0.3);
}

/* 课程菜单面板 */
.course-menu-panel {
  position: absolute;
  bottom: 100%;
  left: 50%;
  transform: translateX(-50%);
  width: 600px;
  max-height: 400px;
  background: white;
  border: 1px solid #e9ecef;
  border-radius: 12px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  overflow: hidden;
}

.course-menu-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 25px;
  background: #f8f9fa;
  border-bottom: 1px solid #e9ecef;
}

.course-menu-header h3 {
  margin: 0;
  font-size: 16px;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #666;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  background: #e9ecef;
}

.course-chapters {
  max-height: 300px;
  overflow-y: auto;
  padding: 15px 0;
}

.chapter-section {
  margin-bottom: 20px;
}

.chapter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 25px;
  background: #f8f9fa;
  font-weight: 500;
  color: #333;
}

.chapter-progress {
  font-size: 12px;
  color: #666;
}

.lesson-list {
  padding: 0;
}

.lesson-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 25px;
  cursor: pointer;
  transition: background 0.3s ease;
}

.lesson-item:hover {
  background: #f8f9fa;
}

.lesson-item.current {
  background: rgba(102, 126, 234, 0.1);
  border-left: 3px solid #667eea;
}

.lesson-item.completed {
  opacity: 0.8;
}

.status-icon {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
}

.status-icon.completed {
  background: #28a745;
  color: white;
}

.status-icon.current {
  background: #667eea;
  color: white;
}

.lesson-info {
  flex: 1;
}

.lesson-title {
  font-weight: 500;
  color: #333;
  margin-bottom: 2px;
}

.lesson-duration {
  font-size: 12px;
  color: #666;
}

/* 学习统计 */
.learning-stats {
  display: flex;
  justify-content: center;
  gap: 40px;
  padding: 15px 30px;
  background: linear-gradient(135deg, #f8f9fa, #ffffff);
  border-top: 1px solid #e9ecef;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.stat-icon {
  font-size: 20px;
}

.stat-content {
  text-align: center;
}

.stat-value {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  line-height: 1;
}

.stat-label {
  font-size: 12px;
  color: #666;
  margin-top: 2px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .navigation-controls {
    flex-direction: column;
    gap: 15px;
    padding: 15px 20px;
  }

  .nav-btn {
    min-width: auto;
    width: 100%;
  }

  .lesson-actions {
    width: 100%;
    justify-content: space-around;
  }

  .course-menu-panel {
    width: 90vw;
    left: 50%;
    transform: translateX(-50%);
  }

  .learning-stats {
    gap: 20px;
    padding: 10px 20px;
  }
}
</style>
