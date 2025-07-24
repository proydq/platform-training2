<template>
  <div class="course-sidebar">
    <div class="sidebar-header">
      <div class="sidebar-title">{{ courseData.title }}</div>
      <div class="sidebar-subtitle">{{ courseData.subtitle }}</div>
    </div>

    <div class="chapter-list">
      <div
        v-for="(chapter, chapterIndex) in courseData.chapters"
        :key="chapter.id"
        class="chapter-item"
      >
        <div
          class="chapter-header"
          :class="{ active: currentChapter === chapterIndex + 1 }"
          @click="handleToggleChapter(chapterIndex + 1)"
        >
          <span>{{ chapter.icon }} {{ chapter.title }}</span>
          <div>
            <span class="chapter-progress">{{ getChapterProgress(chapter) }}</span>
            <span class="chapter-arrow">{{ chapter.expanded ? '▼' : '▶' }}</span>
          </div>
        </div>

        <div class="lesson-list" :class="{ expanded: chapter.expanded }">
          <div
            v-for="(lesson, lessonIndex) in chapter.lessons"
            :key="lesson.id"
            class="lesson-item"
            :class="{
              current: currentChapter === chapterIndex + 1 && currentLesson === lessonIndex + 1,
              completed: lesson.completed
            }"
            @click="handleSelectLesson(chapterIndex + 1, lessonIndex + 1)"
          >
            <div class="lesson-icon" :class="{
              completed: lesson.completed,
              current: currentChapter === chapterIndex + 1 && currentLesson === lessonIndex + 1
            }">
              <span v-if="lesson.completed">✓</span>
              <span v-else-if="currentChapter === chapterIndex + 1 && currentLesson === lessonIndex + 1">▶</span>
              <span v-else>{{ lessonIndex + 1 }}</span>
            </div>
            <span class="lesson-title">{{ lesson.title }}</span>
            <span class="lesson-duration">{{ lesson.duration }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  courseData: {
    type: Object,
    required: true
  },
  currentChapter: {
    type: Number,
    required: true
  },
  currentLesson: {
    type: Number,
    required: true
  }
})

const emit = defineEmits(['selectLesson', 'toggleChapter'])

// 计算章节进度
const getChapterProgress = (chapter) => {
  const completed = chapter.lessons.filter(lesson => lesson.completed).length
  return `${completed}/${chapter.lessons.length}完成`
}

// 处理章节展开/收起
const handleToggleChapter = (chapterNum) => {
  emit('toggleChapter', chapterNum)
}

// 处理课程选择
const handleSelectLesson = (chapter, lesson) => {
  emit('selectLesson', chapter, lesson)
}
</script>

<style scoped>
/* 课程章节侧边栏 */
.course-sidebar {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 15px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 20px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
}

.sidebar-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 5px;
}

.sidebar-subtitle {
  font-size: 14px;
  opacity: 0.9;
}

.chapter-list {
  flex: 1;
  overflow-y: auto;
  padding: 10px 0;
}

.chapter-item {
  border-bottom: 1px solid #f0f0f0;
}

.chapter-header {
  padding: 15px 20px;
  background: #f8f9fa;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 500;
  transition: background 0.3s ease;
}

.chapter-header:hover {
  background: #e9ecef;
}

.chapter-header.active {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
}

.chapter-progress {
  font-size: 12px;
  color: #666;
  margin-right: 8px;
}

.chapter-arrow {
  font-size: 12px;
  transition: transform 0.3s ease;
}

.lesson-list {
  display: none;
  background: white;
}

.lesson-list.expanded {
  display: block;
}

.lesson-item {
  padding: 12px 40px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 10px;
  transition: all 0.3s ease;
  border-left: 3px solid transparent;
}

.lesson-item:hover {
  background: rgba(102, 126, 234, 0.05);
}

.lesson-item.current {
  background: rgba(102, 126, 234, 0.1);
  border-left-color: #667eea;
  font-weight: 500;
}

.lesson-item.completed {
  color: #28a745;
}

.lesson-icon {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  background: #e9ecef;
  color: #666;
  flex-shrink: 0;
}

.lesson-icon.completed {
  background: #28a745;
  color: white;
}

.lesson-icon.current {
  background: #667eea;
  color: white;
}

.lesson-title {
  flex: 1;
  font-size: 14px;
}

.lesson-duration {
  font-size: 12px;
  color: #666;
  flex-shrink: 0;
}

/* 自定义滚动条 */
.chapter-list::-webkit-scrollbar {
  width: 6px;
}

.chapter-list::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.chapter-list::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.chapter-list::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .course-sidebar {
    height: 300px;
  }

  .sidebar-header {
    padding: 15px;
  }

  .sidebar-title {
    font-size: 16px;
  }

  .sidebar-subtitle {
    font-size: 12px;
  }

  .chapter-header {
    padding: 12px 15px;
    font-size: 14px;
  }

  .lesson-item {
    padding: 10px 30px;
  }

  .lesson-title {
    font-size: 13px;
  }

  .lesson-duration {
    font-size: 11px;
  }
}

@media (max-width: 480px) {
  .chapter-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 5px;
  }

  .lesson-item {
    padding: 8px 25px;
    gap: 8px;
  }

  .lesson-icon {
    width: 18px;
    height: 18px;
    font-size: 10px;
  }
}

/* 动画效果 */
@keyframes slideDown {
  from {
    opacity: 0;
    max-height: 0;
  }
  to {
    opacity: 1;
    max-height: 1000px;
  }
}

.lesson-list.expanded {
  animation: slideDown 0.3s ease-in-out;
}

/* 课程状态指示器 */
.lesson-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: transparent;
  transition: background-color 0.3s ease;
}

.lesson-item.current::before {
  background: #667eea;
}

.lesson-item.completed::before {
  background: #28a745;
}

/* 进度指示器 */
.chapter-progress {
  position: relative;
}

.chapter-progress::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  height: 2px;
  background: #28a745;
  border-radius: 1px;
  transition: width 0.3s ease;
}

/* 悬停效果增强 */
.lesson-item:hover .lesson-icon {
  transform: scale(1.1);
}

.lesson-item:hover .lesson-title {
  color: #667eea;
}

/* 加载状态 */
.chapter-list.loading {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 200px;
}

.chapter-list.loading::after {
  content: '';
  width: 20px;
  height: 20px;
  border: 2px solid #f3f3f3;
  border-top: 2px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>
