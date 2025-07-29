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
                :src="getVideoUrl(currentLessonData)"
                controls
                preload="metadata"
                @play="onVideoPlay"
                @pause="onVideoPause"
                @loadstart="onVideoLoadStart"
                @error="onVideoError"
                @loadedmetadata="onVideoLoadedMetadata"
              >
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
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getCourseDetailAPI as getCourseDetail } from '@/api/course'
import { ElMessage } from 'element-plus'
// 路由相关
const route = useRoute()

// 获取视频URL的统一方法
const getVideoUrl = (lessonData) => {
  if (!lessonData) {
    console.warn('⚠️ lessonData 为空')
    return ''
  }

  console.log('🔍 尝试获取视频URL，课程数据:', lessonData)

  // 按优先级尝试获取视频URL
  const possibleUrls = [
    lessonData.videoUrl,
    lessonData.contentUrl,
    lessonData.content,
    lessonData.audioUrl
  ]

  console.log('🔍 可能的URL列表:', possibleUrls)

  for (const url of possibleUrls) {
    if (url && typeof url === 'string' && url.trim()) {
      console.log('✅ 找到有效URL:', url)
      return resolveMediaUrl(url.trim())
    }
  }

  console.warn('⚠️ 没有找到有效的视频URL')
  return ''
}

// URL处理函数
// URL处理函数
const resolveMediaUrl = (url) => {
  console.log('🔗 原始URL:', url)

  if (!url) {
    console.warn('⚠️ URL为空')
    return ''
  }

  if (url.startsWith('blob:')) {
    console.log('✅ Blob URL，直接返回')
    return url
  }

  const API_BASE = 'http://localhost:3000'
  const path = url.replace(/^https?:\/\/[^/]+/, '')
  console.log('🔗 处理后的路径:', path)

  let finalUrl = ''

  if (path.startsWith('/api/v1/files/course/videos/')) {
    finalUrl = `${API_BASE}${path.replace('/api/v1/files/course/videos/', '/api/v1/media/video/')}`
  } else if (path.startsWith('/api/v1/files/course/video/')) {
    finalUrl = `${API_BASE}${path.replace('/api/v1/files/course/video/', '/api/v1/media/video/')}`
  } else if (path.startsWith('/api/v1/media/video/')) {
    finalUrl = `${API_BASE}${path}`
  } else if (path.startsWith('/')) {
    finalUrl = `${API_BASE}${path}`
  } else {
    finalUrl = `${API_BASE}/api/v1/media/video/${path}`
  }

  console.log('🔗 最终URL:', finalUrl)
  return finalUrl
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

// 监听视频地址变化，强制重新加载播放器
watch(
  () => getVideoUrl(currentLessonData.value),
  async (newUrl, oldUrl) => {
    if (videoElement.value && newUrl && newUrl !== oldUrl) {
      await nextTick()
      videoElement.value.load()
      try {
        await videoElement.value.play()
      } catch (e) {
        // autoplay may be blocked
      }
    }
  },
)

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
  console.log('▶️ 视频开始播放')
  isPlaying.value = true
}

const onVideoPause = () => {
  console.log('⏸️ 视频暂停')
  isPlaying.value = false
}

const onVideoLoadStart = () => {
  console.log('🔄 视频开始加载，URL:', videoElement.value?.src)
}

const onVideoError = (error) => {
  console.error('❌ 视频加载失败，错误事件:', error)

  if (videoElement.value) {
    const videoError = videoElement.value.error
    console.error('❌ 视频错误详情:', {
      错误代码: videoError?.code,
      错误消息: videoError?.message,
      视频源: videoElement.value.src,
      网络状态: videoElement.value.networkState,
      就绪状态: videoElement.value.readyState
    })

    // 错误代码说明
    switch (videoError?.code) {
      case 1:
        console.error('❌ MEDIA_ERR_ABORTED - 用户中止了视频播放')
        break
      case 2:
        console.error('❌ MEDIA_ERR_NETWORK - 网络错误')
        break
      case 3:
        console.error('❌ MEDIA_ERR_DECODE - 视频解码错误')
        break
      case 4:
        console.error('❌ MEDIA_ERR_SRC_NOT_SUPPORTED - 视频格式不支持或URL无效')
        break
    }
  }

  ElMessage.error(`视频加载失败: ${videoElement.value?.src || '无URL'}`)
}

const onVideoLoadedMetadata = () => {
  console.log('✅ 视频元数据加载完成:', {
    时长: videoElement.value?.duration,
    宽度: videoElement.value?.videoWidth,
    高度: videoElement.value?.videoHeight
  })
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

const selectLesson = async (chapterId, lessonId) => {
  currentChapter.value = chapterId
  currentLesson.value = lessonId
  isPlaying.value = false

  const chapter = courseData.value.chapters.find((c) => c.id === chapterId)
  const lesson = chapter?.lessons.find((l) => l.id === lessonId)

  // 添加详细的调试信息
  console.log('🔍 选中的课程数据:', lesson)
  console.log('🔍 原始URL字段:', {
    videoUrl: lesson?.videoUrl,
    contentUrl: lesson?.contentUrl,
    content: lesson?.content,
    audioUrl: lesson?.audioUrl
  })

  if (lesson?.type === 'video') {
    const videoUrl = getVideoUrl(lesson)
    console.log('📺 解析后的视频URL：', videoUrl)

    await nextTick()
    if (videoElement.value) {
      videoElement.value.src = videoUrl
      videoElement.value.load()
    }
  }
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
    }
  }
}

// 数据格式化函数 - 强制所有内容为视频类型
// 数据格式化函数 - 增强调试版本
const formatCourse = (courseData) => {
  console.log('🔄 开始格式化课程数据，原始数据:', courseData)

  const formatted = {
    id: courseData.id || 1,
    title: courseData.title || '课程标题',
    category: courseData.category || '未分类',
    totalDuration: courseData.totalDuration || courseData.estimatedDuration || '0分钟',
    level: courseData.level || courseData.difficultyLevel || '初级',
    chapters: (courseData.chapters || []).map((ch, chapterIndex) => {
      console.log(`🔄 格式化章节 ${chapterIndex + 1}:`, ch)

      // 尝试获取视频URL
      const possibleVideoUrl = ch.contentUrl || ch.videoUrl || ch.mediaUrl || ch.content
      console.log(`🎥 章节${chapterIndex + 1}的视频URL候选:`, {
        contentUrl: ch.contentUrl,
        videoUrl: ch.videoUrl,
        mediaUrl: ch.mediaUrl,
        content: ch.content,
        最终选择: possibleVideoUrl
      })

      const formattedChapter = {
        id: ch.id || chapterIndex + 1,
        title: ch.title || `第${chapterIndex + 1}章`,
        lessons: [
          {
            id: ch.id || chapterIndex + 1,
            title: ch.title || `第${chapterIndex + 1}节`,
            type: 'video', // 强制设置为视频类型
            duration: ch.duration ? `${ch.duration}分钟` : '未知',
            completed: ch.status === 1,
            updateDate: ch.updateDate || '未知',
            // 统一处理所有可能的视频URL字段
            videoUrl: possibleVideoUrl,
            contentUrl: possibleVideoUrl,
            content: possibleVideoUrl,
            description: ch.description,
            // 保留原始数据用于调试
            _originalData: ch
          },
        ],
      }

      console.log(`✅ 章节${chapterIndex + 1}格式化完成:`, formattedChapter)
      return formattedChapter
    }),
  }

  console.log('✅ 课程格式化完成，最终数据:', formatted)
  return formatted
}

// 生命周期
// 生命周期 - 增强调试版本
onMounted(async () => {
  console.log('🚀 LearningPage 组件已挂载')
  console.log('📍 当前路由参数:', route.params)

  const courseCode = route.params.courseCode || route.params.courseId
  console.log('📍 课程ID/Code:', courseCode)

  if (!courseCode) {
    console.error('❌ 没有找到课程ID或Code')
    loading.value = false
    return
  }

  try {
    console.log('📡 开始请求课程详情...')
    const res = await getCourseDetail(courseCode)
    console.log('📡 API响应完整数据:', res)

    if (res && res.code === 200) {
      console.log('✅ API请求成功')
      console.log('📦 原始课程数据:', res.data)

      // 特别关注章节数据
      if (res.data && res.data.chapters) {
        console.log('📚 章节数量:', res.data.chapters.length)
        res.data.chapters.forEach((chapter, index) => {
          console.log(`📖 章节${index + 1}详细信息:`, {
            id: chapter.id,
            title: chapter.title,
            chapterType: chapter.chapterType,
            contentUrl: chapter.contentUrl,
            videoUrl: chapter.videoUrl,
            mediaUrl: chapter.mediaUrl,
            content: chapter.content,
            allFields: chapter // 查看所有字段
          })
        })
      } else {
        console.warn('⚠️ 没有章节数据')
      }

      const formatted = formatCourse(res.data || {})
      console.log('🔄 格式化后的课程数据:', formatted)

      courseData.value = formatted

      if (formatted.chapters.length > 0) {
        currentChapter.value = formatted.chapters[0].id
        currentLesson.value = formatted.chapters[0].lessons[0].id
        expandedChapters.value.push(formatted.chapters[0].id)

        console.log('🎯 初始选中:', {
          章节ID: currentChapter.value,
          课程ID: currentLesson.value,
          课程数据: formatted.chapters[0].lessons[0]
        })

        // 立即检查第一个视频URL
        const firstLesson = formatted.chapters[0].lessons[0]
        const videoUrl = getVideoUrl(firstLesson)
        console.log('🎬 第一个视频最终URL:', videoUrl)
      }
    } else {
      console.error('❌ API响应错误:', res)
    }
  } catch (e) {
    console.error('❌ 请求失败:', e)
    console.error('错误详情:', {
      message: e.message,
      response: e.response,
      stack: e.stack
    })
  } finally {
    loading.value = false
    console.log('✅ 加载完成，loading状态:', loading.value)
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
