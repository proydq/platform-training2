<template>
  <div class="course-header">
    <!-- 面包屑导航 -->
    <div class="breadcrumb-section">
      <nav class="breadcrumb">
        <button @click="$emit('goBack')" class="back-btn">
          <span class="back-icon">←</span>
          <span class="back-text">返回课程列表</span>
        </button>

        <div class="breadcrumb-divider">/</div>

        <div class="breadcrumb-items">
          <span class="breadcrumb-item">{{ courseData.title || '产品基础知识培训' }}</span>
          <div class="breadcrumb-divider">/</div>
          <span class="breadcrumb-item current">{{ currentChapterTitle || '第1章 产品概念' }}</span>
        </div>
      </nav>
    </div>

    <!-- 课程信息和工具栏 -->
    <div class="course-info-section">
      <!-- 左侧课程信息 -->
      <div class="course-info">
        <div class="course-meta">
          <span class="course-category">{{ courseData.category || '📱 产品培训' }}</span>
          <span class="course-difficulty">{{ courseData.difficulty || '中级' }}</span>
          <span class="course-duration">{{ courseData.totalDuration || '共3小时' }}</span>
        </div>

        <h1 class="course-title">{{ courseData.title || '产品基础知识培训' }}</h1>

        <div class="course-instructor">
          <img
            :src="courseData.instructor?.avatar || 'https://api.dicebear.com/7.x/avataaars/svg?seed=instructor'"
            :alt="courseData.instructor?.name || '李经理'"
            class="instructor-avatar"
          />
          <div class="instructor-info">
            <span class="instructor-label">讲师</span>
            <span class="instructor-name">{{ courseData.instructor?.name || '李经理' }}</span>
          </div>
        </div>
      </div>

      <!-- 右侧工具栏 -->
      <div class="course-toolbar">
        <!-- 课程进度 -->
        <div class="progress-container">
          <div class="progress-info">
            <span class="progress-label">总体进度</span>
            <span class="progress-value">{{ Math.round(courseProgress) }}%</span>
          </div>
          <div class="progress-bar">
            <div
              class="progress-fill"
              :style="{ width: `${courseProgress}%` }"
            ></div>
          </div>
          <div class="progress-details">
            {{ completedLessons }}/{{ totalLessons }} 课时已完成
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="action-buttons">
          <button
            @click="$emit('toggleBookmark')"
            class="action-btn bookmark-btn"
            :class="{ active: isBookmarked }"
            title="收藏课程"
          >
            <span class="btn-icon">{{ isBookmarked ? '⭐' : '☆' }}</span>
            <span class="btn-text">{{ isBookmarked ? '已收藏' : '收藏' }}</span>
          </button>

          <button @click="shareCourse" class="action-btn share-btn" title="分享课程">
            <span class="btn-icon">📤</span>
            <span class="btn-text">分享</span>
          </button>

          <button @click="$emit('showSettings')" class="action-btn settings-btn" title="播放设置">
            <span class="btn-icon">⚙️</span>
            <span class="btn-text">设置</span>
          </button>
        </div>
      </div>
    </div>

    <!-- 课程标签 -->
    <div class="course-tags">
      <span
        v-for="tag in courseTags"
        :key="tag.name"
        class="course-tag"
        :class="`tag-${tag.type}`"
      >
        <span class="tag-icon">{{ tag.icon }}</span>
        {{ tag.name }}
      </span>
    </div>

    <!-- 分享弹窗 -->
    <transition name="modal-fade">
      <div v-if="showShareModal" class="share-modal-overlay" @click="closeShareModal">
        <div class="share-modal" @click.stop>
          <div class="share-modal-header">
            <h3>分享课程</h3>
            <button @click="closeShareModal" class="close-btn">✕</button>
          </div>
          <div class="share-modal-content">
            <div class="share-options">
              <button @click="shareToWeChat" class="share-option">
                <span class="share-icon">💬</span>
                <span class="share-label">微信</span>
              </button>
              <button @click="shareToQQ" class="share-option">
                <span class="share-icon">🐧</span>
                <span class="share-label">QQ</span>
              </button>
              <button @click="shareToWeibo" class="share-option">
                <span class="share-icon">📱</span>
                <span class="share-label">微博</span>
              </button>
              <button @click="copyLink" class="share-option">
                <span class="share-icon">🔗</span>
                <span class="share-label">复制链接</span>
              </button>
            </div>
            <div class="share-link">
              <label>课程链接</label>
              <div class="link-input-container">
                <input
                  ref="linkInput"
                  :value="shareLink"
                  readonly
                  class="link-input"
                />
                <button @click="copyLink" class="copy-btn">复制</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  courseData: {
    type: Object,
    required: true
  },
  currentChapterTitle: {
    type: String,
    default: ''
  },
  courseProgress: {
    type: Number,
    default: 0
  }
})

defineEmits(['goBack', 'toggleBookmark', 'showSettings'])

// 本地状态
const showShareModal = ref(false)
const isBookmarked = ref(false)
const linkInput = ref(null)

// 计算属性
const courseTags = computed(() => {
  return props.courseData.tags || [
    { name: '基础课程', type: 'level', icon: '📚' },
    { name: '产品设计', type: 'category', icon: '🎨' },
    { name: '必修', type: 'required', icon: '⭐' },
    { name: '在线学习', type: 'format', icon: '💻' }
  ]
})

const completedLessons = computed(() => {
  return props.courseData.completedLessons || 5
})

const totalLessons = computed(() => {
  return props.courseData.totalLessons || 12
})

const shareLink = computed(() => {
  return `${window.location.origin}/learning/${props.courseData.id || 'product-basic'}`
})

// 分享相关方法
const shareCourse = () => {
  showShareModal.value = true
}

const closeShareModal = () => {
  showShareModal.value = false
}

const shareToWeChat = () => {
  // 实现微信分享逻辑
  closeShareModal()
}

const shareToQQ = () => {
  // 实现QQ分享逻辑
  closeShareModal()
}

const shareToWeibo = () => {
  // 实现微博分享逻辑
  closeShareModal()
}

const copyLink = async () => {
  try {
    await navigator.clipboard.writeText(shareLink.value)
    // 可以显示复制成功提示
  } catch (err) {
    // 降级方案
    if (linkInput.value) {
      linkInput.value.select()
      document.execCommand('copy')
    }
  }
  closeShareModal()
}
</script>

<style scoped>
.course-header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 15px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
  overflow: hidden;
}

/* 面包屑导航 */
.breadcrumb-section {
  padding: 15px 25px;
  background: linear-gradient(135deg, #f8f9fa, #ffffff);
  border-bottom: 1px solid #e9ecef;
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  background: none;
  border: 1px solid #e9ecef;
  border-radius: 20px;
  padding: 6px 12px;
  cursor: pointer;
  color: #666;
  transition: all 0.3s ease;
  font-size: 13px;
}

.back-btn:hover {
  background: #667eea;
  color: white;
  border-color: #667eea;
}

.back-icon {
  font-size: 14px;
}

.breadcrumb-divider {
  color: #ccc;
  font-weight: 300;
}

.breadcrumb-items {
  display: flex;
  align-items: center;
  gap: 8px;
}

.breadcrumb-item {
  color: #666;
}

.breadcrumb-item.current {
  color: #667eea;
  font-weight: 500;
}

/* 课程信息区域 */
.course-info-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 25px;
  gap: 30px;
}

/* 左侧课程信息 */
.course-info {
  flex: 1;
}

.course-meta {
  display: flex;
  gap: 15px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.course-category,
.course-difficulty,
.course-duration {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
  padding: 4px 12px;
  border-radius: 15px;
  font-size: 12px;
  font-weight: 500;
}

.course-difficulty {
  background: rgba(255, 193, 7, 0.1);
  color: #ffc107;
}

.course-duration {
  background: rgba(40, 167, 69, 0.1);
  color: #28a745;
}

.course-title {
  font-size: 24px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 15px 0;
  line-height: 1.3;
}

.course-instructor {
  display: flex;
  align-items: center;
  gap: 12px;
}

.instructor-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 2px solid #e9ecef;
}

.instructor-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.instructor-label {
  font-size: 11px;
  color: #666;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.instructor-name {
  font-size: 14px;
  font-weight: 600;
  color: #2c3e50;
}

/* 右侧工具栏 */
.course-toolbar {
  display: flex;
  flex-direction: column;
  gap: 20px;
  min-width: 300px;
}

/* 进度条 */
.progress-container {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #e9ecef;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.progress-label {
  font-size: 13px;
  color: #666;
  font-weight: 500;
}

.progress-value {
  font-size: 16px;
  font-weight: 700;
  color: #667eea;
}

.progress-bar {
  width: 100%;
  height: 8px;
  background: #e9ecef;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 8px;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #667eea, #764ba2);
  border-radius: 4px;
  transition: width 0.8s cubic-bezier(0.4, 0, 0.2, 1);
}

.progress-details {
  font-size: 12px;
  color: #666;
  text-align: center;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 10px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  background: white;
  border: 1px solid #e9ecef;
  border-radius: 20px;
  padding: 8px 12px;
  cursor: pointer;
  font-size: 12px;
  font-weight: 500;
  transition: all 0.3s ease;
  flex: 1;
  justify-content: center;
}

.action-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.bookmark-btn:hover {
  border-color: #ffc107;
  background: #fff3cd;
  color: #856404;
}

.bookmark-btn.active {
  background: #ffc107;
  color: white;
  border-color: #ffc107;
}

.share-btn:hover {
  border-color: #667eea;
  background: #f8f9ff;
  color: #667eea;
}

.settings-btn:hover {
  border-color: #6c757d;
  background: #f8f9fa;
  color: #495057;
}

.btn-icon {
  font-size: 14px;
}

/* 课程标签 */
.course-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 20px 25px;
  background: #f8f9fa;
  border-top: 1px solid #e9ecef;
}

.course-tag {
  display: flex;
  align-items: center;
  gap: 4px;
  background: white;
  border: 1px solid #e9ecef;
  border-radius: 15px;
  padding: 4px 10px;
  font-size: 11px;
  font-weight: 500;
  color: #666;
}

.tag-icon {
  font-size: 12px;
}

/* 标签类型样式 */
.tag-level {
  border-color: #667eea;
  color: #667eea;
  background: rgba(102, 126, 234, 0.05);
}

.tag-category {
  border-color: #28a745;
  color: #28a745;
  background: rgba(40, 167, 69, 0.05);
}

.tag-required {
  border-color: #dc3545;
  color: #dc3545;
  background: rgba(220, 53, 69, 0.05);
}

.tag-format {
  border-color: #17a2b8;
  color: #17a2b8;
  background: rgba(23, 162, 184, 0.05);
}

/* 分享模态框 */
.share-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  backdrop-filter: blur(4px);
}

.share-modal {
  background: white;
  border-radius: 15px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  width: 90%;
  max-width: 450px;
  overflow: hidden;
}

.share-modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 25px;
  border-bottom: 1px solid #e9ecef;
  background: #f8f9fa;
}

.share-modal-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
}

.close-btn {
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
  font-size: 16px;
}

.close-btn:hover {
  background: #e9ecef;
}

.share-modal-content {
  padding: 25px;
}

.share-options {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 15px;
  margin-bottom: 25px;
}

.share-option {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  background: none;
  border: 1px solid #e9ecef;
  border-radius: 10px;
  padding: 15px 10px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.share-option:hover {
  background: #f8f9fa;
  border-color: #667eea;
}

.share-icon {
  font-size: 24px;
}

.share-label {
  font-size: 12px;
  color: #666;
  font-weight: 500;
}

.share-link {
  border-top: 1px solid #f1f3f4;
  padding-top: 20px;
}

.share-link label {
  display: block;
  font-size: 13px;
  color: #666;
  margin-bottom: 8px;
  font-weight: 500;
}

.link-input-container {
  display: flex;
  gap: 8px;
}

.link-input {
  flex: 1;
  padding: 10px 12px;
  border: 1px solid #e9ecef;
  border-radius: 6px;
  font-size: 13px;
  background: #f8f9fa;
}

.copy-btn {
  background: #667eea;
  color: white;
  border: none;
  border-radius: 6px;
  padding: 10px 16px;
  cursor: pointer;
  font-size: 12px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.copy-btn:hover {
  background: #5a6fd8;
}

/* 模态框动画 */
.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: all 0.3s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}

.modal-fade-enter-from .share-modal,
.modal-fade-leave-to .share-modal {
  transform: scale(0.8) translateY(-50px);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .course-info-section {
    flex-direction: column;
    gap: 20px;
  }

  .course-toolbar {
    min-width: auto;
  }

  .course-title {
    font-size: 20px;
  }

  .action-buttons {
    flex-direction: column;
  }

  .course-tags {
    justify-content: center;
  }

  .share-options {
    grid-template-columns: repeat(2, 1fr);
  }

  .breadcrumb {
    flex-wrap: wrap;
    gap: 8px;
  }

  .breadcrumb-items {
    flex-wrap: wrap;
  }
}

@media (max-width: 480px) {
  .course-header {
    margin-bottom: 15px;
  }

  .breadcrumb-section,
  .course-info-section,
  .course-tags {
    padding: 15px 20px;
  }

  .course-meta {
    gap: 8px;
  }

  .progress-container {
    padding: 15px;
  }
}
</style>
