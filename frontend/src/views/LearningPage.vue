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
              @click="navigation.toggleChapter(chapter.id)"
              :class="{ expanded: navigation.expandedChapters.value.includes(chapter.id) }"
            >
              <span class="chapter-icon">
                {{ navigation.expandedChapters.value.includes(chapter.id) ? '📂' : '📁' }}
              </span>
              <span class="chapter-title">{{ chapter.title }}</span>
              <span class="chapter-progress">{{ chapter.lessons.filter(l => l.completed).length }}/{{ chapter.lessons.length }}</span>
            </div>

            <div
              v-if="navigation.expandedChapters.value.includes(chapter.id)"
              class="lessons-list"
            >
              <div
                v-for="lesson in chapter.lessons"
                :key="lesson.id"
                class="lesson-item"
                :class="{
                  active: navigation.currentChapter.value === chapter.id && navigation.currentLesson.value === lesson.id,
                  completed: lesson.completed,
                }"
                @click="handleSelectLesson(chapter.id, lesson.id)"
              >
                <span class="lesson-icon">{{ formatter.getLessonIcon(lesson) }}</span>
                <div class="lesson-info">
                  <div class="lesson-title">{{ lesson.title }}</div>
                  <div class="lesson-meta">
                    <span>{{ formatter.getLessonTypeText(lesson) }}</span>
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
          <h1 class="lesson-title">{{ navigation.currentLessonData.value.title }}</h1>
          <div class="lesson-meta">
            <span class="meta-item">⏱️ {{ navigation.currentLessonData.value.duration }}</span>
            <span class="meta-item">📅 更新时间：{{ navigation.currentLessonData.value.updateDate }}</span>
            <span v-if="navigation.currentLessonData.value.completed" class="completed-mark">✅ 已完成</span>
          </div>
        </div>

        <div class="content-area">
          <LessonContent
            ref="lessonContentRef"
            :media-url="resolvedMediaUrl"
            :lesson-title="navigation.currentLessonData.value.title"
            @play="mediaPlayer.handlePlay"
            @pause="mediaPlayer.handlePause"
            @ended="handleMediaEnd"
          />
        </div>

        <!-- 进度条 -->
        <div class="progress-container">
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: progress.courseProgress.value + '%' }"></div>
          </div>
          <div class="progress-text">
            课程进度：{{ progress.completedLessons.value }}/{{ progress.totalLessons.value }} ({{ progress.courseProgress.value }}%)
          </div>
        </div>

        <!-- 底部工具栏 -->
        <div class="lesson-toolbar">
          <div class="toolbar-left">
            <button class="btn btn-secondary" @click="navigation.previousLesson" :disabled="!navigation.hasPreviousLesson.value">
              ← 上一节
            </button>
            <button v-if="isVideo || isAudio" class="btn btn-secondary" @click="handleTogglePlayPause">
              {{ mediaPlayer.isPlaying.value ? '⏸️ 暂停' : '▶️ 播放' }}
            </button>
          </div>
          <div class="toolbar-right">
            <button class="btn btn-success" @click="handleMarkComplete">✓ 标记完成</button>
            <button class="btn btn-primary" @click="navigation.nextLesson" :disabled="!navigation.hasNextLesson.value">
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
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { getCourseDetailAPI as getCourseDetail } from '@/api/course'
import { ElMessage } from 'element-plus'
import LessonContent from '@/components/learning/LessonContent.vue'

// Composables
import { useMediaUrl } from '@/composables/useMediaUrl'
import { useChapterNavigation } from '@/composables/useChapterNavigation'
import { useCourseProgress } from '@/composables/useCourseProgress'
import { useCourseFormatter } from '@/composables/useCourseFormatter'
import { useMediaPlayer } from '@/composables/useMediaPlayer'

// 路由相关
const route = useRoute()

// 课程数据
const courseData = ref({
  title: '',
  category: '',
  totalDuration: '',
  level: '',
  chapters: [],
})
const loading = ref(true)

// Composables
const mediaUrl = useMediaUrl()
const navigation = useChapterNavigation(courseData)
const progress = useCourseProgress(courseData)
const formatter = useCourseFormatter()
const mediaPlayer = useMediaPlayer()

// Refs
const lessonContentRef = ref(null)

// 计算属性
const resolvedMediaUrl = computed(() => mediaUrl.getMediaUrl(navigation.currentLessonData.value))
const isVideo = computed(() => mediaUrl.isVideoFile(resolvedMediaUrl.value))
const isAudio = computed(() => mediaUrl.isAudioFile(resolvedMediaUrl.value))

// 监听资源地址变化
watch(
  resolvedMediaUrl,
  async (newUrl, oldUrl) => {
    if (newUrl && newUrl !== oldUrl) {
      console.log('🔄 媒体URL变化:', newUrl)
      // 重置播放状态
      mediaPlayer.isPlaying.value = false
    }
  },
)

// 事件处理函数
const handleSelectLesson = async (chapterId, lessonId) => {
  navigation.selectLesson(chapterId, lessonId)
  mediaPlayer.isPlaying.value = false

  const chapter = courseData.value.chapters.find((c) => c.id === chapterId)
  const lesson = chapter?.lessons.find((l) => l.id === lessonId)

  console.log('🔍 选中的课程数据:', lesson)
  console.log('🔍 原始URL字段:', {
    videoUrl: lesson?.videoUrl,
    contentUrl: lesson?.contentUrl,
    content: lesson?.content,
    audioUrl: lesson?.audioUrl,
    documentUrl: lesson?.documentUrl
  })

  const resolvedUrl = mediaUrl.getMediaUrl(lesson)
  console.log('📺 解析后的资源URL：', resolvedUrl)

  // 等待组件渲染完成
  await nextTick()
  
  if (lessonContentRef.value) {
    console.log('📺 建立媒体播放器引用')
    // 不需要手动加载，让video/audio标签自动加载
  }
}

const handleTogglePlayPause = async () => {
  if (!lessonContentRef.value) {
    console.warn('⚠️ LessonContent组件引用不存在')
    return
  }

  try {
    if (isVideo.value && lessonContentRef.value.videoElement) {
      const video = lessonContentRef.value.videoElement
      console.log('🎥 视频播放器操作:', mediaPlayer.isPlaying.value ? '暂停' : '播放')
      
      if (mediaPlayer.isPlaying.value) {
        video.pause()
      } else {
        await video.play()
      }
    } else if (isAudio.value && lessonContentRef.value.audioElement) {
      const audio = lessonContentRef.value.audioElement
      console.log('🎥 音频播放器操作:', mediaPlayer.isPlaying.value ? '暂停' : '播放')
      
      if (mediaPlayer.isPlaying.value) {
        audio.pause()
      } else {
        await audio.play()
      }
    } else {
      console.warn('⚠️ 没有找到有效的媒体元素', {
        isVideo: isVideo.value,
        isAudio: isAudio.value,
        hasVideoElement: !!lessonContentRef.value.videoElement,
        hasAudioElement: !!lessonContentRef.value.audioElement
      })
    }
  } catch (error) {
    console.error('⚠️ 媒体播放错误:', error)
    ElMessage.error('播放失败，请稍后重试')
  }
}

const handleMarkComplete = () => {
  if (navigation.markComplete()) {
    ElMessage.success('已标记为完成')
  }
}

const handleMediaEnd = () => {
  mediaPlayer.isPlaying.value = false
  const mediaType = isVideo.value ? '视频' : isAudio.value ? '音频' : '媒体'
  ElMessage.success(`${mediaType}播放完成`)
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

      const formatted = formatter.formatCourse(res.data || {})
      console.log('🔄 格式化后的课程数据:', formatted)

      courseData.value = formatted

      if (formatted.chapters.length > 0) {
        navigation.initializeFirstLesson()

        console.log('🎯 初始选中:', {
          章节ID: navigation.currentChapter.value,
          课程ID: navigation.currentLesson.value,
          课程数据: formatted.chapters[0].lessons[0]
        })

        // 立即检查第一个资源URL
        const firstLesson = formatted.chapters[0].lessons[0]
        const mediaUrl_ = mediaUrl.getMediaUrl(firstLesson)
        console.log('🎬 第一个资源最终URL:', mediaUrl_)
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