<template>
  <div class="lesson-header">
    <!-- 课程标题区域 -->
    <div class="lesson-title-section">
      <div class="lesson-meta">
        <span class="lesson-chapter">{{ lessonData.chapter || '第1章' }}</span>
        <span class="meta-separator">·</span>
        <span class="lesson-section">{{ lessonData.section || '第2节' }}</span>
      </div>

      <h1 class="lesson-title">{{ lessonData.title || '产品基础知识培训' }}</h1>

      <div class="lesson-description">
        {{ lessonData.description || '了解产品设计的基本概念和核心原理，掌握用户体验设计的基础知识。' }}
      </div>
    </div>

    <!-- 学习进度区域 -->
    <div class="progress-section">
      <!-- 观看时长 -->
      <div class="watch-stats">
        <div class="stat-item">
          <div class="stat-icon">⏱️</div>
          <div class="stat-content">
            <div class="stat-label">已观看</div>
            <div class="stat-value">{{ watchedTime || '8分钟' }}</div>
          </div>
        </div>

        <div class="stat-item">
          <div class="stat-icon">📅</div>
          <div class="stat-content">
            <div class="stat-label">最后更新</div>
            <div class="stat-value">{{ formatUpdateDate(updateDate) }}</div>
          </div>
        </div>

        <div class="stat-item">
          <div class="stat-icon">👥</div>
          <div class="stat-content">
            <div class="stat-label">学习人数</div>
            <div class="stat-value">{{ lessonData.studentCount || '128' }}人</div>
          </div>
        </div>
      </div>

      <!-- 学习进度条 -->
      <div class="progress-container">
        <div class="progress-info">
          <span class="progress-label">学习进度</span>
          <span class="progress-percentage">{{ Math.round(currentProgress) }}%</span>
        </div>
        <div class="progress-bar">
          <div
            class="progress-fill"
            :style="{ width: `${currentProgress}%` }"
          ></div>
        </div>
      </div>
    </div>

    <!-- 课程标签 -->
    <div class="lesson-tags">
      <span
        v-for="tag in lessonTags"
        :key="tag.name"
        class="lesson-tag"
        :class="`tag-${tag.type}`"
      >
        <span class="tag-icon">{{ tag.icon }}</span>
        {{ tag.name }}
      </span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  lessonData: {
    type: Object,
    required: true
  },
  watchedTime: {
    type: String,
    default: ''
  },
  updateDate: {
    type: String,
    default: ''
  }
})

// 计算当前学习进度
const currentProgress = computed(() => {
  return props.lessonData.progress || 35
})

// 课程标签
const lessonTags = computed(() => {
  return props.lessonData.tags || [
    { name: '基础课程', type: 'level', icon: '📚' },
    { name: '45分钟', type: 'duration', icon: '⏰' },
    { name: '必修', type: 'required', icon: '⭐' }
  ]
})

// 格式化更新日期
const formatUpdateDate = (dateString) => {
  if (!dateString) return '2天前'

  const date = new Date(dateString)
  const now = new Date()
  const diffTime = now - date
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))

  if (diffDays === 0) return '今天'
  if (diffDays === 1) return '昨天'
  if (diffDays <= 7) return `${diffDays}天前`
  if (diffDays <= 30) return `${Math.ceil(diffDays / 7)}周前`

  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  })
}
</script>

<style scoped>
.lesson-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 30px 40px;
  border-radius: 15px 15px 0 0;
  position: relative;
  overflow: hidden;
}

.lesson-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  z-index: 0;
}

.lesson-header > * {
  position: relative;
  z-index: 1;
}

/* 课程标题区域 */
.lesson-title-section {
  margin-bottom: 25px;
}

.lesson-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  font-size: 14px;
  opacity: 0.9;
}

.lesson-chapter,
.lesson-section {
  background: rgba(255, 255, 255, 0.2);
  padding: 4px 12px;
  border-radius: 15px;
  font-weight: 500;
}

.meta-separator {
  opacity: 0.6;
}

.lesson-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 15px 0;
  line-height: 1.3;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.lesson-description {
  font-size: 16px;
  line-height: 1.6;
  opacity: 0.9;
  max-width: 600px;
}

/* 学习进度区域 */
.progress-section {
  margin-bottom: 20px;
}

.watch-stats {
  display: flex;
  gap: 30px;
  margin-bottom: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.stat-icon {
  font-size: 20px;
  opacity: 0.9;
}

.stat-content {
  display: flex;
  flex-direction: column;
}

.stat-label {
  font-size: 12px;
  opacity: 0.8;
  margin-bottom: 2px;
}

.stat-value {
  font-size: 14px;
  font-weight: 600;
}

/* 进度条 */
.progress-container {
  background: rgba(255, 255, 255, 0.15);
  border-radius: 12px;
  padding: 20px;
  backdrop-filter: blur(10px);
}

.progress-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.progress-label {
  font-size: 14px;
  font-weight: 500;
}

.progress-percentage {
  font-size: 16px;
  font-weight: 700;
}

.progress-bar {
  width: 100%;
  height: 8px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 4px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #28a745, #20c997);
  border-radius: 4px;
  transition: width 0.8s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.progress-fill::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  bottom: 0;
  right: 0;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.2),
    transparent
  );
  animation: shimmer 2s infinite;
}

@keyframes shimmer {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

/* 课程标签 */
.lesson-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.lesson-tag {
  display: flex;
  align-items: center;
  gap: 6px;
  background: rgba(255, 255, 255, 0.2);
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
}

.lesson-tag:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-1px);
}

.tag-icon {
  font-size: 14px;
}

/* 标签类型样式 */
.tag-level {
  background: rgba(40, 167, 69, 0.3);
  border-color: rgba(40, 167, 69, 0.4);
}

.tag-duration {
  background: rgba(255, 193, 7, 0.3);
  border-color: rgba(255, 193, 7, 0.4);
}

.tag-required {
  background: rgba(220, 53, 69, 0.3);
  border-color: rgba(220, 53, 69, 0.4);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .lesson-header {
    padding: 20px 25px;
  }

  .lesson-title {
    font-size: 24px;
  }

  .lesson-description {
    font-size: 14px;
  }

  .watch-stats {
    flex-direction: column;
    gap: 15px;
  }

  .stat-item {
    justify-content: space-between;
    background: rgba(255, 255, 255, 0.1);
    padding: 12px 15px;
    border-radius: 10px;
  }

  .progress-container {
    padding: 15px;
  }

  .lesson-tags {
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .lesson-header {
    padding: 15px 20px;
  }

  .lesson-title {
    font-size: 20px;
  }

  .lesson-meta {
    flex-wrap: wrap;
  }

  .watch-stats {
    gap: 10px;
  }

  .stat-item {
    padding: 10px 12px;
  }

  .lesson-tag {
    font-size: 12px;
    padding: 5px 12px;
  }
}

/* 深色模式适配 */
@media (prefers-color-scheme: dark) {
  .lesson-header::before {
    background: rgba(0, 0, 0, 0.2);
  }

  .progress-container {
    background: rgba(0, 0, 0, 0.2);
  }
}

/* 高对比度模式 */
@media (prefers-contrast: high) {
  .lesson-header {
    border: 2px solid white;
  }

  .lesson-tag {
    border-width: 2px;
  }

  .progress-bar {
    border: 1px solid rgba(255, 255, 255, 0.5);
  }
}

/* 减少动画模式 */
@media (prefers-reduced-motion: reduce) {
  .progress-fill {
    transition: none;
  }

  .progress-fill::after {
    animation: none;
  }

  .lesson-tag {
    transition: none;
  }
}
</style>
