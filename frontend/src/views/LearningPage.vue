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
                <span class="lesson-icon">{{ getlessonIcon(lesson) }}</span>
                <div class="lesson-info">
                  <div class="lesson-title">{{ lesson.title }}</div>
                  <div class="lesson-meta">
                    <span>{{ getlessonTypeText(lesson) }}</span>
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
        <div class="lesson-header">
          <h1 class="lesson-title">{{ currentLessonData.title }}</h1>
          <div class="lesson-meta">
            <span class="meta-item">⏱️ {{ currentLessonData.duration }}</span>
            <span class="meta-item">📅 更新时间：{{ currentLessonData.updateDate }}</span>
            <span v-if="currentLessonData.completed" class="completed-mark">✅ 已完成</span>
          </div>
        </div>

        <div class="content-area">
          <!-- 视频播放器 -->
          <div v-if="isVideo" class="video-container">
            <div class="video-player">
              <video
                ref="videoElement"
                :src="resolvedMediaUrl"
                controls
                controlsList="nodownload"
                @play="isPlaying = true"
                @pause="isPlaying = false"
                @ended="handleVideoEnd"
              >
                您的浏览器不支持视频播放。
              </video>
            </div>
          </div>

          <!-- 文档查看器 -->
          <div v-else-if="isDocument" class="document-container">
            <DocumentViewer
              :url="resolvedMediaUrl"
              :file-name="currentLessonData.title || '文档'"
            />
          </div>

          <!-- 音频播放器 -->
          <div v-else-if="isAudio" class="audio-container">
            <div class="audio-player">
              <audio
                ref="audioElement"
                :src="resolvedMediaUrl"
                controls
                controlsList="nodownload"
                @play="isPlaying = true"
                @pause="isPlaying = false"
                @ended="handleAudioEnd"
              >
                您的浏览器不支持音频播放。
              </audio>
            </div>
          </div>

          <!-- 其他类型或无内容 -->
          <div v-else class="content-placeholder">
            <div class="placeholder-content">
              <span class="placeholder-icon">📚</span>
              <p>请选择一个章节开始学习</p>
            </div>
          </div>
        </div>

        <!-- 进度条 -->
        <div class="progress-container">
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: courseProgress + '%' }"></div>
          </div>
          <div class="progress-text">
            课程进度：{{ completedLessons }}/{{ totalLessons }} ({{ courseProgress }}%)
          </div>
        </div>

        <!-- 底部工具栏 -->
        <div class="lesson-toolbar">
          <div class="toolbar-left">
            <button class="btn btn-secondary" @click="previousLesson" :disabled="!hasPreviousLesson">
              ← 上一节
            </button>
            <button v-if="isVideo || isAudio" class="btn btn-secondary" @click="togglePlayPause">
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
import DocumentViewer from '@/components/DocumentViewer.vue'

// 路由相关
const route = useRoute()

// 获取章节资源的统一方法
const getVideoUrl = (lessonData) => {
  if (!lessonData) {
    console.warn('⚠️ lessonData 为空')
    return ''
  }

  console.log('🔍 尝试获取资源URL，课程数据:', lessonData)

  // 按优先级尝试获取资源URL
  const possibleUrls = [
    lessonData.videoUrl,
    lessonData.contentUrl,
    lessonData.content,
    lessonData.audioUrl,
    lessonData.documentUrl
  ]

  console.log('🔍 可能的URL列表:', possibleUrls)

  for (const url of possibleUrls) {
    if (url && typeof url === 'string' && url.trim()) {
      console.log('✅ 找到有效URL:', url)
      return resolveMediaUrl(url.trim())
    }
  }

  console.warn('⚠️ 没有找到有效的资源URL')
  return ''
}

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

  // 处理不同类型的URL路径
  if (path.startsWith('/api/v1/files/course/videos/')) {
    finalUrl = `${API_BASE}${path.replace('/api/v1/files/course/videos/', '/api/v1/media/video/')}`
  } else if (path.startsWith('/api/v1/files/course/video/')) {
    finalUrl = `${API_BASE}${path.replace('/api/v1/files/course/video/', '/api/v1/media/video/')}`
  } else if (path.startsWith('/api/v1/files/course/documents/')) {
    finalUrl = `${API_BASE}${path.replace('/api/v1/files/course/documents/', '/api/v1/media/document/')}`
  } else if (path.startsWith('/api/v1/media/video/')) {
    finalUrl = `${API_BASE}${path}`
  } else if (path.startsWith('/api/v1/media/document/')) {
    finalUrl = `${API_BASE}${path}`
  } else if (path.startsWith('/')) {
    finalUrl = `${API_BASE}${path}`
  } else {
    // 根据文件扩展名判断类型
    const ext = path.split('.').pop().toLowerCase()
    if (isVideoFile(`.${ext}`)) {
      finalUrl = `${API_BASE}/api/v1/media/video/${path}`
    } else if (isDocumentFile(`.${ext}`)) {
      finalUrl = `${API_BASE}/api/v1/media/document/${path}`
    } else {
      finalUrl = `${API_BASE}/api/v1/media/video/${path}`
    }
  }

  console.log('🔗 最终URL:', finalUrl)
  return finalUrl
}

// 判断资源类型 - 扩展支持更多类型
const isVideoFile = (url) => /\.(mp4|mov|webm|avi|mkv)$/i.test(url || '')
const isAudioFile = (url) => /\.(mp3|wav|ogg|m4a)$/i.test(url || '')
const isDocumentFile = (url) => /\.(pdf|docx?|xlsx?|pptx?|txt|md|csv|json|xml)$/i.test(url || '')

// 响应式数据
const currentChapter = ref(1)
const currentLesson = ref(null)
const expandedChapters = ref([])
const isPlaying = ref(false)
const videoElement = ref(null)
const audioElement = ref(null)

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

// 当前章节资源URL
const resolvedMediaUrl = computed(() => getVideoUrl(currentLessonData.value))
const isVideo = computed(() => isVideoFile(resolvedMediaUrl.value))
const isAudio = computed(() => isAudioFile(resolvedMediaUrl.value))
const isDocument = computed(() => isDocumentFile(resolvedMediaUrl.value))

// 监听资源地址变化，强制重新加载播放器
watch(
  resolvedMediaUrl,
  async (newUrl, oldUrl) => {
    if (newUrl && newUrl !== oldUrl) {
      await nextTick()
      if (isVideo.value && videoElement.value) {
        videoElement.value.load()
        try {
          await videoElement.value.play()
        } catch (e) {
          // autoplay may be blocked
        }
      } else if (isAudio.value && audioElement.value) {
        audioElement.value.load()
        try {
          await audioElement.value.play()
        } catch (e) {
          // autoplay may be blocked
        }
      }
    }
  },
)

// 进度计算
const courseProgress = computed(() => {
  const total = courseData.value.chapters.reduce((sum, chapter) => sum + chapter.lessons.length, 0)
  const completed = courseData.value.chapters.reduce(
    (sum, chapter) => sum + chapter.lessons.filter((lesson) => lesson.completed).length,
    0,
  )
  return total > 0 ? Math.round((completed / total) * 100) : 0
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

// 导航计算
const hasPreviousLesson = computed(() => {
  const firstChapter = courseData.value.chapters[0]
  if (!firstChapter) return false
  return !(currentChapter.value === firstChapter.id && currentLesson.value === firstChapter.lessons[0]?.id)
})

const hasNextLesson = computed(() => {
  const lastChapter = courseData.value.chapters[courseData.value.chapters.length - 1]
  if (!lastChapter) return false
  const lastLesson = lastChapter.lessons[lastChapter.lessons.length - 1]
  return !(currentChapter.value === lastChapter.id && currentLesson.value === lastLesson?.id)
})

// 视频控制方法
const handleVideoEnd = () => {
  isPlaying.value = false
  ElMessage.success('视频播放完成')
}

const handleAudioEnd = () => {
  isPlaying.value = false
  ElMessage.success('音频播放完成')
}

const togglePlayPause = () => {
  if (isVideo.value && videoElement.value) {
    if (isPlaying.value) {
      videoElement.value.pause()
    } else {
      videoElement.value.play()
    }
  } else if (isAudio.value && audioElement.value) {
    if (isPlaying.value) {
      audioElement.value.pause()
    } else {
      audioElement.value.play()
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
    audioUrl: lesson?.audioUrl,
    documentUrl: lesson?.documentUrl
  })

  const mediaUrl = getVideoUrl(lesson)
  console.log('📺 解析后的资源URL：', mediaUrl)

  if (isVideoFile(mediaUrl)) {
    await nextTick()
    if (videoElement.value) {
      videoElement.value.src = mediaUrl
      videoElement.value.load()
    }
  } else if (isAudioFile(mediaUrl)) {
    await nextTick()
    if (audioElement.value) {
      audioElement.value.src = mediaUrl
      audioElement.value.load()
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
      ElMessage.success('已标记为完成')
    }
  }
}

// 获取课程图标
const getlessonIcon = (lesson) => {
  if (lesson.type === 'video') return '🎥'
  if (lesson.type === 'document') return '📄'
  if (lesson.type === 'audio') return '🎵'
  return '📚'
}

// 获取课程类型文本
const getlessonTypeText = (lesson) => {
  if (lesson.type === 'video') return '视频课程'
  if (lesson.type === 'document') return '文档资料'
  if (lesson.type === 'audio') return '音频课程'
  return '学习资料'
}

// 数据格式化函数
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

      // 尝试获取章节资源URL
      const possibleUrl = ch.contentUrl || ch.videoUrl || ch.mediaUrl || ch.content || ch.documentUrl
      console.log(`🎥 章节${chapterIndex + 1}的资源URL候选:`, {
        contentUrl: ch.contentUrl,
        videoUrl: ch.videoUrl,
        mediaUrl: ch.mediaUrl,
        content: ch.content,
        documentUrl: ch.documentUrl,
        最终选择: possibleUrl
      })

      // 判断资源类型
      let resourceType = 'unknown'
      if (possibleUrl) {
        if (isVideoFile(possibleUrl)) {
          resourceType = 'video'
        } else if (isDocumentFile(possibleUrl)) {
          resourceType = 'document'
        } else if (isAudioFile(possibleUrl)) {
          resourceType = 'audio'
        }
      }

      const formattedChapter = {
        id: ch.id || chapterIndex + 1,
        title: ch.title || `第${chapterIndex + 1}章`,
        lessons: [
          {
            id: ch.id || chapterIndex + 1,
            title: ch.title || `第${chapterIndex + 1}节`,
            type: resourceType,
            duration: ch.duration ? `${ch.duration}分钟` : '未知',
            completed: ch.status === 1,
            updateDate: ch.updateDate || '未知',
            // 统一处理所有可能的资源URL字段
            videoUrl: possibleUrl,
            contentUrl: possibleUrl,
            content: possibleUrl,
            audioUrl: possibleUrl,
            documentUrl: possibleUrl,
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
            documentUrl: chapter.documentUrl,
            allFields: chapter
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

        // 立即检查第一个资源URL
        const firstLesson = formatted.chapters[0].lessons[0]
        const mediaUrl = getVideoUrl(firstLesson)
        console.log('🎬 第一个资源最终URL:', mediaUrl)
      }
    } else {
      console.error('❌ API响应错误:', res)
      ElMessage.error('课程加载失败')
    }
  } catch (e) {
    console.error('❌ 请求失败:', e)
    console.error('错误详情:', {
      message: e.message,
      response: e.response,
      stack: e.stack
    })
    ElMessage.error('网络错误，请稍后重试')
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

/* 文档容器样式 */
.document-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}

/* 音频容器样式 */
.audio-container {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px;
}

.audio-player {
  background: white;
  padding: 30px;
  border-radius: 15px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  width: 100%;
  max-width: 600px;
}

.audio-player audio {
  width: 100%;
}

/* 占位符样式 */
.content-placeholder {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
}

.placeholder-content {
  text-align: center;
  color: #666;
}

.placeholder-icon {
  font-size: 64px;
  display: block;
  margin-bottom: 20px;
  opacity: 0.5;
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
