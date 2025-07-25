<template>
  <div class="learning-page">
    <div class="learning-container">
      <!-- 侧边栏 -->
      <div class="sidebar">
        <div class="course-info">
          <div class="course-title">{{ courseData.title || '产品经理进阶课程' }}</div>
          <div class="course-meta">
            <span class="meta-item">👨‍🏫 {{ courseData.instructor || '张教授' }}</span>
            <span class="meta-item">⏱️ {{ courseData.totalDuration || '180分钟' }}</span>
            <span class="meta-item">📚 {{ courseData.totalChapters || '12' }}章节</span>
            <span class="meta-item">🎓 {{ courseData.level || '中级' }}</span>
          </div>
        </div>

        <div class="chapters-list">
          <div
            v-for="chapter in courseData.chapters"
            :key="chapter.id"
            class="chapter-group"
          >
            <div
              class="chapter-header"
              @click="toggleChapter(chapter.id)"
            >
              <span
                class="chapter-toggle"
                :class="{ expanded: expandedChapters.includes(chapter.id) }"
              >
                ▶
              </span>
              <span>{{ chapter.title }}</span>
            </div>
            <div
              class="lesson-list"
              :class="{ show: expandedChapters.includes(chapter.id) }"
            >
              <div
                v-for="lesson in chapter.lessons"
                :key="lesson.id"
                class="lesson-item"
                :class="{ active: currentChapter === chapter.id && currentLesson === lesson.id }"
                @click="selectLesson(chapter.id, lesson.id, lesson.type)"
              >
                <div
                  class="lesson-icon"
                  :class="{
                    completed: lesson.completed,
                    current: currentChapter === chapter.id && currentLesson === lesson.id
                  }"
                >
                  {{ lesson.order }}
                </div>
                <span>{{ lesson.title }}</span>
                <span class="lesson-type-icon">{{ getTypeIcon(lesson.type) }}</span>
                <span class="lesson-duration">{{ lesson.duration }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 主学习内容区 -->
      <div class="learning-main">
        <!-- 课程标题 -->
        <div class="lesson-header">
          <div class="lesson-title">{{ currentLessonData.title || '产品经理职责概述' }}</div>
          <div class="lesson-meta">
            <span>{{ getTypeText(currentLessonData.type) }}</span>
            <span>⏱️ {{ currentLessonData.duration || '15分钟' }}</span>
            <span v-if="currentLessonData.watchedTime">👁️ 已观看 {{ currentLessonData.watchedTime }}</span>
            <span>📅 更新于 {{ currentLessonData.updateDate || '2025-01-15' }}</span>
          </div>
        </div>

        <!-- 学习进度 -->
        <div class="progress-container">
          <div class="progress-bar">
            <div
              class="progress-fill"
              :style="{ width: courseProgress + '%' }"
            ></div>
          </div>
          <div class="progress-text">
            学习进度：{{ courseProgress }}% ({{ completedLessons }}/{{ totalLessons }} 章节已完成)
          </div>
        </div>

        <!-- 内容区域 -->
        <div class="content-area">
          <!-- 视频播放器 -->
          <div v-if="currentLessonData.type === 'video'" class="video-container">
            <div class="video-player">
              <div v-if="!isVideoPlaying" class="video-placeholder" @click="playVideo">
                <div class="play-button">▶</div>
                <h3 style="margin-bottom: 10px;">{{ currentLessonData.title }}</h3>
                <p style="opacity: 0.8;">{{ currentLessonData.description }}</p>
              </div>
              <video
                v-else
                ref="videoElement"
                controls
                @play="onVideoPlay"
                @pause="onVideoPause"
              >
                <source :src="currentLessonData.videoUrl" type="video/mp4">
                您的浏览器不支持视频播放
              </video>
            </div>
          </div>

          <!-- 文档显示区域 -->
          <div v-else-if="currentLessonData.type === 'document'" class="document-container">
            <div class="document-viewer" v-html="currentLessonData.content"></div>
          </div>

          <!-- 音频播放器区域 -->
          <div v-else-if="currentLessonData.type === 'audio'" class="audio-container">
            <div class="audio-player">
              <h2>{{ currentLessonData.title }}</h2>
              <p style="margin: 20px 0; opacity: 0.9;">{{ currentLessonData.description }}</p>
              <audio controls style="width: 100%; margin-top: 20px;">
                <source :src="currentLessonData.audioUrl" type="audio/mpeg">
                您的浏览器不支持音频播放
              </audio>
            </div>
          </div>

          <!-- 测验区域 -->
          <div v-else-if="currentLessonData.type === 'quiz'" class="quiz-container">
            <div class="quiz-content" v-html="currentLessonData.content"></div>
          </div>

          <!-- 默认内容 -->
          <div v-else class="document-container">
            <div class="document-viewer">
              <h2>课程内容</h2>
              <p>正在加载课程内容，请稍候...</p>
            </div>
          </div>
        </div>

        <!-- 底部工具栏 -->
        <div class="lesson-toolbar">
          <div class="toolbar-left">
            <button
              class="btn btn-secondary"
              @click="previousLesson"
              :disabled="!hasPreviousLesson"
            >
              ← 上一节
            </button>
            <button
              v-if="currentLessonData.type === 'video' || currentLessonData.type === 'audio'"
              class="btn btn-primary"
              @click="togglePlayPause"
            >
              {{ isPlaying ? '⏸️ 暂停' : '▶️ 播放' }}
            </button>
          </div>
          <div class="toolbar-right">
            <button class="btn btn-success" @click="markComplete">
              ✓ 标记完成
            </button>
            <button
              class="btn btn-primary"
              @click="nextLesson"
              :disabled="!hasNextLesson"
            >
              下一节 →
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'

// 只保留实际使用的导入
const router = useRouter()

// 响应式数据
const currentChapter = ref(1)
const currentLesson = ref(1)
const expandedChapters = ref([1])
const isVideoPlaying = ref(false)
const isPlaying = ref(false)
const videoElement = ref(null)

// 课程数据
const courseData = ref({
  title: '产品经理进阶课程',
  instructor: '张教授',
  totalDuration: '180分钟',
  totalChapters: 12,
  level: '中级',
  chapters: [
    {
      id: 1,
      title: '第一章：产品基础知识',
      lessons: [
        {
          id: 1,
          order: 1,
          title: '产品经理职责概述',
          type: 'video',
          duration: '15分钟',
          completed: false,
          videoUrl: 'sample-video.mp4',
          description: '了解产品经理的核心职责和工作流程'
        },
        {
          id: 2,
          order: 2,
          title: '产品生命周期管理',
          type: 'document',
          duration: '20分钟',
          completed: false,
          content: `
            <h1>产品生命周期管理</h1>
            <p>产品生命周期（Product Life Cycle, PLC）是指产品从概念形成到最终退出市场的整个过程。</p>

            <h2>生命周期阶段</h2>
            <h3>1. 概念阶段</h3>
            <p>在这个阶段，产品还只是一个想法或概念。需要进行市场调研、用户需求分析、竞品分析等工作。</p>

            <h3>2. 开发阶段</h3>
            <p>将概念转化为实际产品的阶段。包括产品设计、原型制作、技术开发、测试等。</p>

            <h3>3. 引入阶段</h3>
            <p>产品正式上市销售的初期阶段。此时销量较低，需要大量市场推广。</p>

            <h3>4. 成长阶段</h3>
            <p>产品被市场接受，销量快速增长。需要扩大生产规模，优化产品功能。</p>

            <h3>5. 成熟阶段</h3>
            <p>产品销量达到峰值并趋于稳定。市场竞争激烈，需要维护市场份额。</p>

            <h3>6. 衰退阶段</h3>
            <p>由于技术进步或市场变化，产品销量开始下降。需要决定是否继续维护或退出市场。</p>
          `
        },
        {
          id: 3,
          order: 3,
          title: '产品思维培养',
          type: 'audio',
          duration: '12分钟',
          completed: false,
          audioUrl: 'sample-audio.mp3',
          description: '培养产品经理必备的产品思维能力'
        }
      ]
    },
    {
      id: 2,
      title: '第二章：市场分析方法',
      lessons: [
        {
          id: 1,
          order: 1,
          title: '市场调研技巧',
          type: 'video',
          duration: '25分钟',
          completed: false
        },
        {
          id: 2,
          order: 2,
          title: '竞品分析报告',
          type: 'document',
          duration: '30分钟',
          completed: false
        },
        {
          id: 3,
          order: 3,
          title: '市场分析测验',
          type: 'quiz',
          duration: '15分钟',
          completed: false,
          content: `
            <h1>市场分析能力测验</h1>
            <p>请根据前面学习的内容，完成以下测验题目：</p>

            <div style="background: #f8f9fa; padding: 20px; border-radius: 10px; margin: 20px 0;">
              <h3>题目1：市场调研的主要方法有哪些？</h3>
              <div style="margin: 10px 0;">
                <label><input type="radio" name="q1" value="a"> A. 问卷调查、访谈、观察</label><br>
                <label><input type="radio" name="q1" value="b"> B. 只有问卷调查</label><br>
                <label><input type="radio" name="q1" value="c"> C. 只有数据分析</label><br>
                <label><input type="radio" name="q1" value="d"> D. 只有竞品分析</label>
              </div>
            </div>

            <div style="background: #f8f9fa; padding: 20px; border-radius: 10px; margin: 20px 0;">
              <h3>题目2：SWOT分析包括哪些要素？</h3>
              <div style="margin: 10px 0;">
                <label><input type="radio" name="q2" value="a"> A. 优势、劣势、机会、威胁</label><br>
                <label><input type="radio" name="q2" value="b"> B. 只有优势和劣势</label><br>
                <label><input type="radio" name="q2" value="c"> C. 价格、质量、服务</label><br>
                <label><input type="radio" name="q2" value="d"> D. 产品、市场、技术</label>
              </div>
            </div>

            <div style="text-align: center; margin-top: 30px;">
              <button class="btn btn-primary" onclick="submitQuiz()">提交答案</button>
            </div>
          `
        }
      ]
    }
  ]
})

// 计算属性
const currentLessonData = computed(() => {
  const chapter = courseData.value.chapters.find(c => c.id === currentChapter.value)
  if (!chapter) return {}
  const lesson = chapter.lessons.find(l => l.id === currentLesson.value)
  return lesson || {}
})

const courseProgress = computed(() => {
  const total = courseData.value.chapters.reduce((sum, chapter) => sum + chapter.lessons.length, 0)
  const completed = courseData.value.chapters.reduce((sum, chapter) =>
    sum + chapter.lessons.filter(lesson => lesson.completed).length, 0)
  return Math.round((completed / total) * 100)
})

const completedLessons = computed(() => {
  return courseData.value.chapters.reduce((sum, chapter) =>
    sum + chapter.lessons.filter(lesson => lesson.completed).length, 0)
})

const totalLessons = computed(() => {
  return courseData.value.chapters.reduce((sum, chapter) => sum + chapter.lessons.length, 0)
})

const hasPreviousLesson = computed(() => {
  // 简化逻辑：检查是否有上一节课
  if (currentChapter.value === 1 && currentLesson.value === 1) return false
  return true
})

const hasNextLesson = computed(() => {
  // 简化逻辑：检查是否有下一节课
  const chapter = courseData.value.chapters.find(c => c.id === currentChapter.value)
  if (!chapter) return false

  const currentLessonIndex = chapter.lessons.findIndex(l => l.id === currentLesson.value)
  if (currentLessonIndex < chapter.lessons.length - 1) return true

  // 检查是否有下一章
  return currentChapter.value < courseData.value.chapters.length
})

// 方法
const toggleChapter = (chapterId) => {
  const index = expandedChapters.value.indexOf(chapterId)
  if (index > -1) {
    expandedChapters.value.splice(index, 1)
  } else {
    expandedChapters.value.push(chapterId)
  }
}

const selectLesson = (chapterId, lessonId) => {
  currentChapter.value = chapterId
  currentLesson.value = lessonId
  isVideoPlaying.value = false
  isPlaying.value = false
}

const getTypeIcon = (type) => {
  const icons = {
    video: '🎥',
    document: '📄',
    audio: '🎵',
    quiz: '📝'
  }
  return icons[type] || '📚'
}

const getTypeText = (type) => {
  const types = {
    video: '🎥 视频课程',
    document: '📄 文档资料',
    audio: '🎵 音频课程',
    quiz: '📝 在线测验'
  }
  return types[type] || '📚 课程内容'
}

const playVideo = () => {
  isVideoPlaying.value = true
  isPlaying.value = true
}

const onVideoPlay = () => {
  isPlaying.value = true
}

const onVideoPause = () => {
  isPlaying.value = false
}

const togglePlayPause = () => {
  if (currentLessonData.value.type === 'video' && videoElement.value) {
    if (isPlaying.value) {
      videoElement.value.pause()
    } else {
      videoElement.value.play()
    }
  }
}

const previousLesson = () => {
  // 实现上一节课逻辑
  const currentChapterData = courseData.value.chapters.find(c => c.id === currentChapter.value)
  if (!currentChapterData) return

  const currentLessonIndex = currentChapterData.lessons.findIndex(l => l.id === currentLesson.value)

  if (currentLessonIndex > 0) {
    // 同一章的上一节课
    const previousLessonData = currentChapterData.lessons[currentLessonIndex - 1]
    selectLesson(currentChapter.value, previousLessonData.id)
  } else if (currentChapter.value > 1) {
    // 上一章的最后一节课
    const previousChapter = courseData.value.chapters.find(c => c.id === currentChapter.value - 1)
    if (previousChapter && previousChapter.lessons.length > 0) {
      const lastLesson = previousChapter.lessons[previousChapter.lessons.length - 1]
      selectLesson(previousChapter.id, lastLesson.id)
    }
  }
}

const nextLesson = () => {
  // 实现下一节课逻辑
  const currentChapterData = courseData.value.chapters.find(c => c.id === currentChapter.value)
  if (!currentChapterData) return

  const currentLessonIndex = currentChapterData.lessons.findIndex(l => l.id === currentLesson.value)

  if (currentLessonIndex < currentChapterData.lessons.length - 1) {
    // 同一章的下一节课
    const nextLessonData = currentChapterData.lessons[currentLessonIndex + 1]
    selectLesson(currentChapter.value, nextLessonData.id)
  } else if (currentChapter.value < courseData.value.chapters.length) {
    // 下一章的第一节课
    const nextChapter = courseData.value.chapters.find(c => c.id === currentChapter.value + 1)
    if (nextChapter && nextChapter.lessons.length > 0) {
      const firstLesson = nextChapter.lessons[0]
      selectLesson(nextChapter.id, firstLesson.id)
    }
  }
}

const markComplete = () => {
  const chapter = courseData.value.chapters.find(c => c.id === currentChapter.value)
  if (chapter) {
    const lesson = chapter.lessons.find(l => l.id === currentLesson.value)
    if (lesson) {
      lesson.completed = true
      console.log('标记当前课程为已完成')
    }
  }
}

// 生命周期
onMounted(() => {
  // 默认展开第一章
  if (!expandedChapters.value.includes(1)) {
    expandedChapters.value.push(1)
  }
})
</script>

<style scoped>
/* 重置样式 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.learning-page {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  min-height: 100vh;
  padding: 20px;
}

.learning-container {
  display: flex;
  gap: 20px;
  max-width: 1400px;
  margin: 0 auto;
  height: calc(100vh - 40px);
}

/* 侧边栏 */
.sidebar {
  width: 350px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 15px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}

.course-info {
  padding: 20px;
  border-bottom: 1px solid #e0e0e0;
  background: rgba(102, 126, 234, 0.05);
}

.course-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 10px;
  color: #333;
}

.course-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  font-size: 12px;
  color: #666;
}

.meta-item {
  background: rgba(102, 126, 234, 0.1);
  padding: 4px 8px;
  border-radius: 10px;
}

/* 章节列表 */
.chapters-list {
  flex: 1;
  padding: 20px 0;
}

.chapter-group {
  margin-bottom: 15px;
}

.chapter-header {
  padding: 10px 20px;
  background: #f8f9fa;
  border-left: 4px solid #667eea;
  margin-bottom: 10px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 10px;
}

.chapter-header:hover {
  background: #e9ecef;
}

.chapter-toggle {
  font-size: 14px;
  transition: transform 0.3s ease;
}

.chapter-toggle.expanded {
  transform: rotate(90deg);
}

.lesson-list {
  display: none;
  padding-left: 20px;
}

.lesson-list.show {
  display: block;
}

.lesson-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  margin-bottom: 5px;
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.lesson-item:hover {
  background: rgba(102, 126, 234, 0.1);
}

.lesson-item.active {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  margin-right: 10px;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.lesson-icon {
  width: 24px;
  height: 24px;
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

.lesson-type-icon {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  margin-left: auto;
}

.lesson-duration {
  margin-left: auto;
  font-size: 12px;
  color: #666;
}

/* 修复选中状态下文字颜色问题 */
.lesson-item.active .lesson-duration {
  color: rgba(255, 255, 255, 0.9);
}

.lesson-item.active .lesson-type-icon {
  color: rgba(255, 255, 255, 0.9);
}

/* 主学习内容区 */
.learning-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 15px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.lesson-header {
  padding: 20px 30px;
  border-bottom: 1px solid #e0e0e0;
  background: rgba(102, 126, 234, 0.05);
}

.lesson-title {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 10px;
  color: #333;
}

.lesson-meta {
  display: flex;
  align-items: center;
  gap: 20px;
  color: #666;
  font-size: 14px;
}

.lesson-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 内容区域 */
.content-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 视频播放器区域 */
.video-container {
  position: relative;
  background: #000;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  overflow: hidden;
}

.video-player {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  position: relative;
}

.video-player video {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.video-placeholder {
  text-align: center;
  padding: 40px;
  cursor: pointer;
}

.play-button {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  border: 3px solid rgba(255, 255, 255, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  cursor: pointer;
  transition: all 0.3s ease;
  margin: 0 auto 20px;
}

.play-button:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: scale(1.1);
}

/* 文档显示区域 */
.document-container {
  flex: 1;
  padding: 30px;
  overflow-y: auto;
  background: #fff;
}

.document-viewer {
  max-width: 800px;
  margin: 0 auto;
  line-height: 1.8;
  font-size: 16px;
  color: #333;
}

.document-viewer :deep(h1),
.document-viewer :deep(h2),
.document-viewer :deep(h3) {
  color: #667eea;
  margin: 25px 0 15px 0;
}

.document-viewer :deep(p) {
  margin-bottom: 15px;
}

/* 音频播放器区域 */
.audio-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  padding: 40px;
}

.audio-player {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 15px;
  padding: 40px;
  text-align: center;
  backdrop-filter: blur(10px);
}

/* 测验区域 */
.quiz-container {
  flex: 1;
  padding: 30px;
  overflow-y: auto;
  background: #f8f9fa;
}

.quiz-content {
  max-width: 800px;
  margin: 0 auto;
  background: white;
  border-radius: 15px;
  padding: 30px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

/* 学习进度条 */
.progress-container {
  padding: 0 30px 20px;
}

.progress-bar {
  width: 100%;
  height: 6px;
  background: #e9ecef;
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  transition: width 0.3s ease;
  border-radius: 3px;
}

.progress-text {
  margin-top: 8px;
  font-size: 12px;
  color: #666;
  text-align: center;
}

/* 底部工具栏 */
.lesson-toolbar {
  padding: 20px 30px;
  border-top: 1px solid #e0e0e0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #f8f9fa;
}

.toolbar-left {
  display: flex;
  gap: 10px;
}

.toolbar-right {
  display: flex;
  gap: 10px;
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-primary {
  background: #667eea;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #5a6fd8;
  transform: translateY(-2px);
}

.btn-secondary {
  background: #6c757d;
  color: white;
}

.btn-secondary:hover:not(:disabled) {
  background: #5a6268;
}

.btn-success {
  background: #28a745;
  color: white;
}

.btn-success:hover:not(:disabled) {
  background: #218838;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .learning-container {
    flex-direction: column;
    height: auto;
  }

  .sidebar {
    width: 100%;
    order: 2;
    max-height: 300px;
  }

  .learning-main {
    order: 1;
    min-height: 60vh;
  }
}
</style>
