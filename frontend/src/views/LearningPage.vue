<template>
  <div class="learning-page">
    <div v-if="!loading" class="learning-container">
      <!-- 侧边栏 -->
      <div class="sidebar">
        <!-- 课程信息 -->
        <div class="course-info">
          <h2 class="course-title">{{ courseData.title }}</h2>
          <div class="course-meta">
            <span class="meta-item">📚 {{ courseData.category }}</span>
            <span class="meta-item">⏱️ {{ courseData.totalDuration }}</span>
            <span class="meta-item">📊 {{ courseData.level }}</span>
          </div>
        </div>

        <!-- 章节列表 -->
        <div class="chapters-list">
          <div
            v-for="chapter in courseData.chapters"
            :key="chapter.id"
            class="chapter-group"
          >
            <div
              class="chapter-header"
              @click="toggleChapter(chapter.id)"
              :class="{ expanded: expandedChapters.includes(chapter.id) }"
            >
              <span class="chapter-icon">
                {{ expandedChapters.includes(chapter.id) ? '📂' : '📁' }}
              </span>
              <span class="chapter-title">{{ chapter.title }}</span>
              <span class="chapter-progress">{{ chapter.lessons.filter(l => l.completed).length }}/{{ chapter.lessons.length }}</span>
            </div>

            <div
              v-if="expandedChapters.includes(chapter.id)"
              class="lessons-list"
            >
              <div
                v-for="lesson in chapter.lessons"
                :key="lesson.id"
                class="lesson-item"
                :class="{
                  active: currentChapter === chapter.id && currentLesson === lesson.id,
                  completed: lesson.completed,
                }"
                @click="selectLesson(chapter.id, lesson.id)"
              >
                <span class="lesson-icon">🎥</span>
                <div class="lesson-info">
                  <div class="lesson-title">{{ lesson.title }}</div>
                  <div class="lesson-meta">
                    <span>🎥 视频课程</span>
                    <span v-if="lesson.duration">• {{ lesson.duration }}</span>
                  </div>
                </div>
                <span v-if="lesson.completed" class="completed-mark">✓</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 主要内容区域 -->
      <div class="learning-main">
        <!-- 课程标题区域 -->
        <div class="lesson-header">
          <div class="lesson-info">
            <h1 class="lesson-title">{{ currentLessonData.title }}</h1>
            <div class="lesson-meta">
              <span class="lesson-type">🎥 视频课程</span>
              <span v-if="currentLessonData.duration">⏱️ {{ currentLessonData.duration }}</span>
              <span v-if="currentLessonData.updateDate">📅 更新于 {{ currentLessonData.updateDate }}</span>
            </div>
          </div>
        </div>

        <!-- 学习进度 -->
        <div class="progress-container">
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: courseProgress + '%' }"></div>
          </div>
          <div class="progress-text">
            学习进度：{{ courseProgress }}% ({{ completedLessons }}/{{ totalLessons }} 章节已完成)
          </div>
        </div>

        <!-- 视频播放器区域 - 默认显示 -->
        <div class="content-area">
          <div class="video-container">
            <div class="video-player">
              <!-- 直接显示视频播放器 -->
              <video
                v-if="getVideoUrl(currentLessonData)"
                ref="videoElement"
                controls
                preload="metadata"
                @play="onVideoPlay"
                @pause="onVideoPause"
                @loadstart="onVideoLoadStart"
                @error="onVideoError"
                @loadedmetadata="onVideoLoadedMetadata"
              >
                <source :src="getVideoUrl(currentLessonData)" type="video/mp4" />
                您的浏览器不支持视频播放
              </video>
              <!-- 当没有视频URL时显示提示信息 -->
              <div v-else class="video-placeholder">
                <div class="info-icon">📹</div>
                <h3 style="margin-bottom: 10px">{{ currentLessonData.title }}</h3>
                <p style="opacity: 0.8">视频正在准备中，请稍后...</p>
                <div style="margin-top: 20px; font-size: 12px; opacity: 0.6;">
                  <p>如果视频长时间无法加载，请联系管理员</p>
                </div>
              </div>
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
              class="btn btn-primary"
              @click="togglePlayPause"
            >
              {{ isPlaying ? '⏸️ 暂停' : '▶️ 播放' }}
            </button>
          </div>
          <div class="toolbar-right">
            <button class="btn btn-success" @click="markComplete">✓ 标记完成</button>
            <button class="btn btn-primary" @click="nextLesson" :disabled="!hasNextLesson">
              下一节 →
            </button>
          </div>
        </div>
      </div>
    </div>
    <div v-else class="loading">加载中...</div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getCourseDetailAPI as getCourseDetail } from '@/api/course'

// 路由相关
const router = useRouter()
const route = useRoute()

// 获取视频URL的统一方法
const getVideoUrl = (lessonData) => {
  if (!lessonData) return ''

  // 按优先级尝试获取视频URL
  const possibleUrls = [
    lessonData.videoUrl,
    lessonData.contentUrl,
    lessonData.content,
    lessonData.audioUrl
  ]

  for (const url of possibleUrls) {
    if (url && typeof url === 'string' && url.trim()) {
      return resolveMediaUrl(url.trim())
    }
  }

  return ''
}

// URL处理函数
const resolveMediaUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url

  // 处理不同的URL格式
  if (url.includes('/api/v1/files/course/videos/')) {
    return `http://localhost:3000${url.replace('/api/v1/files/course/videos/', '/api/v1/media/video/')}`
  }

  // 如果是相对路径，添加服务器地址
  if (url.startsWith('/')) {
    return `http://localhost:3000${url}`
  }

  // 默认处理
  return `http://localhost:3000/api/v1/media/video/${url}`
}

// 响应式数据
const currentChapter = ref(1)
const currentLesson = ref(null)
const expandedChapters = ref([])
const isPlaying = ref(false)
const videoElement = ref(null)

// 课程数据
const courseData = ref({
  title: '',
  category: '',
  totalDuration: '',
  level: '',
  chapters: [],
})
const loading = ref(true)

// 计算属性
const currentLessonData = computed(() => {
  const chapter = courseData.value.chapters.find((c) => c.id === currentChapter.value)
  if (!chapter) return {}
  const lesson = chapter.lessons.find((l) => l.id === currentLesson.value)
  return lesson || {}
})

const courseProgress = computed(() => {
  const total = courseData.value.chapters.reduce((sum, chapter) => sum + chapter.lessons.length, 0)
  const completed = courseData.value.chapters.reduce(
    (sum, chapter) => sum + chapter.lessons.filter((lesson) => lesson.completed).length,
    0,
  )
  return Math.round((completed / total) * 100)
})

const completedLessons = computed(() => {
  return courseData.value.chapters.reduce(
    (sum, chapter) => sum + chapter.lessons.filter((lesson) => lesson.completed).length,
    0,
  )
})

const totalLessons = computed(() => {
  return courseData.value.chapters.reduce((sum, chapter) => sum + chapter.lessons.length, 0)
})

const hasPreviousLesson = computed(() => {
  const chapterIndex = courseData.value.chapters.findIndex((c) => c.id === currentChapter.value)
  if (chapterIndex === -1) return false
  const lessonIndex = courseData.value.chapters[chapterIndex].lessons.findIndex(
    (l) => l.id === currentLesson.value,
  )
  if (lessonIndex > 0) return true
  return chapterIndex > 0
})

const hasNextLesson = computed(() => {
  const chapter = courseData.value.chapters.find((c) => c.id === currentChapter.value)
  if (!chapter) return false

  const currentLessonIndex = chapter.lessons.findIndex((l) => l.id === currentLesson.value)
  if (currentLessonIndex < chapter.lessons.length - 1) return true

  return currentChapter.value < courseData.value.chapters.length
})

// 视频相关方法
const onVideoPlay = () => {
  isPlaying.value = true
}

const onVideoPause = () => {
  isPlaying.value = false
}

const onVideoLoadStart = () => {
  console.log('视频开始加载')
}

const onVideoError = (error) => {
  console.error('视频加载失败:', error)
}

const onVideoLoadedMetadata = () => {
  console.log('视频元数据加载完成')
}

const togglePlayPause = () => {
  if (videoElement.value) {
    if (isPlaying.value) {
      videoElement.value.pause()
    } else {
      videoElement.value.play()
    }
  }
}

// 章节相关方法
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
  isPlaying.value = false
}

const previousLesson = () => {
  const currentChapterData = courseData.value.chapters.find((c) => c.id === currentChapter.value)
  if (!currentChapterData) return

  const currentLessonIndex = currentChapterData.lessons.findIndex(
    (l) => l.id === currentLesson.value,
  )

  if (currentLessonIndex > 0) {
    const previousLessonData = currentChapterData.lessons[currentLessonIndex - 1]
    selectLesson(currentChapter.value, previousLessonData.id)
  } else if (currentChapter.value > 1) {
    const previousChapter = courseData.value.chapters.find((c) => c.id === currentChapter.value - 1)
    if (previousChapter && previousChapter.lessons.length > 0) {
      const lastLesson = previousChapter.lessons[previousChapter.lessons.length - 1]
      selectLesson(previousChapter.id, lastLesson.id)
    }
  }
}

const nextLesson = () => {
  const currentChapterData = courseData.value.chapters.find((c) => c.id === currentChapter.value)
  if (!currentChapterData) return

  const currentLessonIndex = currentChapterData.lessons.findIndex(
    (l) => l.id === currentLesson.value,
  )

  if (currentLessonIndex < currentChapterData.lessons.length - 1) {
    const nextLessonData = currentChapterData.lessons[currentLessonIndex + 1]
    selectLesson(currentChapter.value, nextLessonData.id)
  } else {
    const nextChapter = courseData.value.chapters.find((c) => c.id === currentChapter.value + 1)
    if (nextChapter && nextChapter.lessons.length > 0) {
      selectLesson(nextChapter.id, nextChapter.lessons[0].id)
    }
  }
}

const markComplete = () => {
  const chapter = courseData.value.chapters.find((c) => c.id === currentChapter.value)
  if (chapter) {
    const lesson = chapter.lessons.find((l) => l.id === currentLesson.value)
    if (lesson) {
      lesson.completed = true
      console.log('课程已标记为完成')
    }
  }
}

// 数据格式化函数 - 强制所有内容为视频类型
const formatCourse = (courseData) => {
  return {
    id: courseData.id || 1,
    title: courseData.title || '课程标题',
    category: courseData.category || '未分类',
    totalDuration: courseData.totalDuration || '0分钟',
    level: courseData.level || courseData.difficultyLevel || '初级',
    chapters: (courseData.chapters || []).map((ch, chapterIndex) => ({
      id: ch.id || chapterIndex + 1,
      title: ch.title || `第${chapterIndex + 1}章`,
      lessons: [
        {
          id: ch.id || chapterIndex + 1,
          title: ch.title || `第${chapterIndex + 1}节`,
          type: 'video', // 强制设置为视频类型
          duration: ch.duration || '未知',
          completed: ch.status === 1,
          updateDate: ch.updateDate || '未知',
          // 统一处理所有可能的视频URL字段
          videoUrl: ch.contentUrl || ch.videoUrl || ch.mediaUrl,
          contentUrl: ch.contentUrl || ch.videoUrl || ch.mediaUrl,
          content: ch.contentUrl || ch.videoUrl || ch.mediaUrl,
          description: ch.description,
        },
      ],
    })),
  }
}

// 生命周期
onMounted(async () => {
  const courseCode = route.params.courseCode || route.params.courseId
  try {
    const res = await getCourseDetail(courseCode)
    if (res && res.code === 200) {
      const formatted = formatCourse(res.data || {})
      courseData.value = formatted
      if (formatted.chapters.length > 0) {
        currentChapter.value = formatted.chapters[0].id
        currentLesson.value = formatted.chapters[0].lessons[0].id
        expandedChapters.value.push(formatted.chapters[0].id)
      }
    }
  } catch (e) {
    console.error('获取课程详情失败', e)
  } finally {
    loading.value = false
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
  transition: all 0.3s ease;
}

.chapter-header:hover {
  background: #e9ecef;
}

.chapter-icon {
  font-size: 14px;
}

.chapter-title {
  flex: 1;
  font-weight: 600;
  color: #333;
}

.chapter-progress {
  font-size: 12px;
  color: #666;
  background: rgba(102, 126, 234, 0.1);
  padding: 2px 6px;
  border-radius: 8px;
}

.lessons-list {
  margin-left: 20px;
}

.lesson-item {
  padding: 12px 20px;
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 8px;
  margin: 5px 0;
}

.lesson-item:hover {
  background: rgba(102, 126, 234, 0.05);
}

.lesson-item.active {
  background: rgba(102, 126, 234, 0.1);
  border-left: 3px solid #667eea;
}

.lesson-item.completed {
  opacity: 0.7;
}

.lesson-icon {
  font-size: 16px;
}

.lesson-info {
  flex: 1;
}

.lesson-title {
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.lesson-meta {
  font-size: 12px;
  color: #666;
}

.completed-mark {
  color: #28a745;
  font-weight: bold;
}

/* 主要内容区域 */
.learning-main {
  flex: 1;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 15px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
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
  border-radius: 8px;
  flex: 1;
}

.video-player {
  width: 100%;
  height: 100%;
  position: relative;
}

.video-player video {
  width: 100%;
  height: 100%;
  object-fit: contain;
  background: #000;
}

/* 占位符样式 */
.video-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  text-align: center;
  padding: 40px;
  min-height: 400px;
}

.info-icon {
  font-size: 48px;
  margin-bottom: 20px;
  opacity: 0.8;
}

.video-placeholder h3 {
  color: white;
  margin-bottom: 10px;
  font-size: 20px;
}

.video-placeholder p {
  color: rgba(255, 255, 255, 0.8);
  font-size: 16px;
  line-height: 1.5;
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

.loading {
  color: #fff;
  text-align: center;
  padding: 40px;
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
