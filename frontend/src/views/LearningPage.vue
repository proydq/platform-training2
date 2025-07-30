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
            @play="handleMediaPlay"
            @pause="handleMediaPause"
            @ended="handleMediaEnd"
          />
        </div>

        <!-- 进度条 -->
        <div class="progress-container">
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: progress.currentChapterProgress.value + '%' }"></div>
          </div>
          <div class="progress-text">
            本章进度：{{ progress.currentChapterCompletedLessons.value }}/{{ progress.currentChapterTotalLessons.value }} ({{ progress.currentChapterProgress.value }}%)
            <span class="course-progress">
              | 课程总进度：{{ progress.completedLessons.value }}/{{ progress.totalLessons.value }} ({{ progress.courseProgress.value }}%)
            </span>
            <span v-if="progressTracking.isTracking.value" class="study-time">
              | 本节学习：{{ progressTracking.getFormattedStudyTime() }}
              <span v-if="progressTracking.isPaused.value" class="paused-indicator">
                (等待播放)
              </span>
              <span v-if="!progressTracking.isPaused.value" class="active-indicator">
                (学习中)
              </span>
            </span>
            <span v-if="!navigation.currentLessonData.value.completed" class="progress-hint">
              | 完成学习后请点击"标记完成"
            </span>
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
            <button 
              class="btn"
              :class="navigation.currentLessonData.value.completed ? 'btn-completed' : 'btn-success'"
              @click="handleMarkComplete"
              :disabled="navigation.currentLessonData.value.completed"
            >
              {{ navigation.currentLessonData.value.completed ? '✅ 已完成' : '✓ 标记完成' }}
            </button>
            <button class="btn btn-primary" @click="handleNextLesson" :disabled="!navigation.hasNextLesson.value">
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
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
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
import { useProgressTracking } from '@/composables/useProgressTracking'

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
const progress = useCourseProgress(courseData, navigation)
const formatter = useCourseFormatter()
const mediaPlayer = useMediaPlayer()
const progressTracking = useProgressTracking(() => progress.courseProgress.value)

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
  // 保存之前章节的进度
  if (progressTracking.isTracking.value) {
    const currentProgress = progress.courseProgress.value
    await progressTracking.cleanup(currentProgress)
  }

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

  // 检查是否是媒体课程（更严格的判断逻辑）
  const hasVideoUrl = !!lesson?.videoUrl && lesson.videoUrl.trim() !== ''
  const hasMediaFile = lesson?.contentUrl && /\.(mp4|avi|mov|wmv|flv|webm|mkv|mp3|wav|flac|aac|ogg)$/i.test(lesson.contentUrl)
  const isVideoType = lesson?.chapterType === 'video'
  const isAudioType = lesson?.chapterType === 'audio'
  
  // 综合判断：优先看 chapterType，其次看是否有实际的视频URL
  let isMediaLesson = false
  
  // 第一优先级：明确的类型标记
  if (isVideoType || isAudioType) {
    isMediaLesson = true
    console.log('✅ 根据 chapterType 判断为媒体课程')
  }
  // 第二优先级：有实际的视频URL但类型不明确
  else if (hasVideoUrl) {
    isMediaLesson = true
    console.log('✅ 根据 videoUrl 判断为媒体课程')
  }
  // 第三优先级：URL中包含视频文件扩展名
  else if (hasMediaFile) {
    isMediaLesson = true
    console.log('✅ 根据文件扩展名判断为媒体课程')
  }
  // 默认：文档课程
  else {
    isMediaLesson = false
    console.log('✅ 判断为文档课程，立即开始计时')
  }
  
  console.log('🎯 最终判断结果:', {
    chapterType: lesson?.chapterType,
    videoUrl: lesson?.videoUrl ? '有' : '无',
    contentUrl: lesson?.contentUrl ? lesson.contentUrl.substring(0, 50) + '...' : '无',
    isVideoType,
    isAudioType,
    hasVideoUrl,
    hasMediaFile,
    isMediaLesson: isMediaLesson
  })
  
  console.log('🔍 详细媒体类型检查:', {
    lesson: lesson,
    videoUrl: lesson?.videoUrl,
    contentUrl: lesson?.contentUrl,
    chapterType: lesson?.chapterType,
    hasVideoUrl,
    hasMediaFile,
    isVideoType,
    isAudioType,
    finalResult: isMediaLesson
  })
  
  console.log('📚 课程类型检查:', {
    lessonTitle: lesson?.title,
    chapterType: lesson?.chapterType,
    videoUrl: lesson?.videoUrl,
    contentUrl: lesson?.contentUrl,
    isMediaLesson: isMediaLesson,
    计时策略: isMediaLesson ? '初始暂停，等待播放' : '立即开始计时'
  })
  
  // 开始新章节的进度追踪，如果是媒体课程则初始暂停
  progressTracking.startTracking(route.params.courseId, chapterId, isMediaLesson)

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
        // 暂停时也暂停计时
        progressTracking.pauseTracking()
      } else {
        await video.play()
        // 播放时恢复计时
        progressTracking.resumeTracking()
      }
    } else if (isAudio.value && lessonContentRef.value.audioElement) {
      const audio = lessonContentRef.value.audioElement
      console.log('🎥 音频播放器操作:', mediaPlayer.isPlaying.value ? '暂停' : '播放')
      
      if (mediaPlayer.isPlaying.value) {
        audio.pause()
        // 暂停时也暂停计时
        progressTracking.pauseTracking()
      } else {
        await audio.play()
        // 播放时恢复计时
        progressTracking.resumeTracking()
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

const handleMarkComplete = async () => {
  const courseId = route.params.courseId
  const chapterId = navigation.currentChapter.value
  
  if (!courseId || !chapterId) {
    ElMessage.error('无法获取课程或章节信息')
    return
  }

  try {
    // 1. 先标记当前章节为完成状态
    const marked = navigation.markComplete()
    if (!marked) {
      ElMessage.error('标记章节完成失败')
      return
    }

    console.log('✅ 本地章节已标记完成')
    ElMessage.success('✅ 章节已完成！')

    // 2. 计算新的课程进度
    const newProgress = progress.courseProgress.value
    console.log('📊 新的课程进度:', newProgress + '%')

    // 3. 保存进度到后端
    await progressTracking.saveProgress(newProgress, `chapter-${chapterId}`)

    // 4. 如果是课程的最后一章，标记整个课程完成
    if (newProgress >= 100) {
      console.log('🎉 课程全部完成，标记课程完成状态')
      await progressTracking.markChapterComplete(100)
      ElMessage.success('🎊 恭喜完成整个课程！')
    }

    // 5. 自动跳转到下一章节（如果有的话）
    if (navigation.hasNextLesson.value) {
      setTimeout(() => {
        navigation.nextLesson()
      }, 1000)
    }

  } catch (error) {
    console.error('❌ 标记完成失败:', error)
    ElMessage.error('标记完成失败，请重试')
  }
}

// 处理媒体播放事件
const handleMediaPlay = () => {
  console.log('▶️ 媒体开始播放')
  mediaPlayer.handlePlay()
  
  // 如果是视频或音频，恢复学习时间计时
  if (isVideo.value || isAudio.value) {
    progressTracking.resumeTracking()
  }
}

// 处理媒体暂停事件
const handleMediaPause = () => {
  console.log('⏸️ 媒体暂停播放')
  mediaPlayer.handlePause()
  
  // 如果是视频或音频，暂停学习时间计时
  if (isVideo.value || isAudio.value) {
    progressTracking.pauseTracking()
  }
}

const handleMediaEnd = () => {
  console.log('🔚 媒体播放结束')
  mediaPlayer.isPlaying.value = false
  
  // 如果是视频或音频，暂停学习时间计时
  if (isVideo.value || isAudio.value) {
    progressTracking.pauseTracking()
  }
  
  const mediaType = isVideo.value ? '视频' : isAudio.value ? '音频' : '媒体'
  ElMessage.success(`${mediaType}播放完成`)
}

// 处理下一节按钮点击
const handleNextLesson = async () => {
  console.log('🔽 点击下一节按钮')
  console.log('📍 当前状态:', {
    hasNext: navigation.hasNextLesson.value,
    currentChapter: navigation.currentChapter.value,
    currentLesson: navigation.currentLesson.value,
    totalChapters: courseData.value.chapters.length
  })

  if (!navigation.hasNextLesson.value) {
    console.log('⚠️ 没有下一节课程')
    ElMessage.info('已经是最后一节课了')
    return
  }

  // 保存当前进度
  if (progressTracking.isTracking.value) {
    const currentProgress = progress.courseProgress.value
    await progressTracking.cleanup(currentProgress)
  }

  // 跳转到下一节
  navigation.nextLesson()
  
  // 重新开始进度追踪
  const courseId = route.params.courseId
  if (navigation.currentChapter.value) {
    // 获取新的当前课程数据
    const currentChapter = courseData.value.chapters.find(c => c.id === navigation.currentChapter.value)
    const currentLesson = currentChapter?.lessons.find(l => l.id === navigation.currentLesson.value)
    
    // 检查新课程是否是媒体课程（综合判断）
    let isNewLessonMedia = false
    
    if (currentLesson) {
      const newIsVideoType = currentLesson.chapterType === 'video'
      const newIsAudioType = currentLesson.chapterType === 'audio'
      const newHasVideoUrl = !!currentLesson.videoUrl && currentLesson.videoUrl.trim() !== ''
      const newHasMediaFile = currentLesson.contentUrl && /\.(mp4|avi|mov|wmv|flv|webm|mkv|mp3|wav|flac|aac|ogg)$/i.test(currentLesson.contentUrl)
      
      isNewLessonMedia = newIsVideoType || newIsAudioType || newHasVideoUrl || newHasMediaFile
    }
    
    console.log('📺 下一节课程类型检查:', {
      lessonTitle: currentLesson?.title,
      chapterType: currentLesson?.chapterType,
      isNewLessonMedia: isNewLessonMedia
    })
    
    progressTracking.startTracking(courseId, navigation.currentChapter.value, isNewLessonMedia)
  }
}

// 同步课程完成状态
const syncCourseCompletionStatus = async (courseId) => {
  try {
    console.log('🔄 同步课程完成状态:', courseId)
    
    const studyRecord = await progressTracking.getStudyRecord(courseId)
    if (studyRecord) {
      console.log('📊 学习记录:', studyRecord)
      
      // 如果课程已完成，更新本地状态
      if (studyRecord.studyStatus === 'COMPLETED') {
        console.log('✅ 课程已完成，更新本地状态')
        navigation.markAllComplete()
        ElMessage.info('📚 此课程已完成')
      } else if (studyRecord.progressPercent > 0) {
        console.log(`📈 课程进度: ${studyRecord.progressPercent}%`)
        // 可以根据进度更新本地章节完成状态
        progress.syncProgressFromRecord(studyRecord)
      }
    }
  } catch (error) {
    console.error('❌ 同步课程完成状态失败:', error)
    // 不显示错误消息，静默失败
  }
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

        // 同步课程完成状态
        await syncCourseCompletionStatus(courseCode)

        // 开始第一个章节的进度追踪
        if (navigation.currentChapter.value) {
          // 设置进度回调，让追踪器能实时获取课程进度
          progressTracking.setProgressCallback(() => {
            const currentProgress = progress.courseProgress.value
            console.log('📊 获取实时课程进度:', currentProgress + '%')
            return currentProgress
          })
          
          // 检查第一个课程是否是媒体课程（综合判断）
          const firstLesson = formatted.chapters[0].lessons[0]
          let isFirstLessonMedia = false
          
          if (firstLesson) {
            const firstIsVideoType = firstLesson.chapterType === 'video'
            const firstIsAudioType = firstLesson.chapterType === 'audio'
            const firstHasVideoUrl = !!firstLesson.videoUrl && firstLesson.videoUrl.trim() !== ''
            const firstHasMediaFile = firstLesson.contentUrl && /\.(mp4|avi|mov|wmv|flv|webm|mkv|mp3|wav|flac|aac|ogg)$/i.test(firstLesson.contentUrl)
            
            isFirstLessonMedia = firstIsVideoType || firstIsAudioType || firstHasVideoUrl || firstHasMediaFile
            
            console.log('🎯 初始课程媒体判断:', {
              title: firstLesson.title,
              chapterType: firstLesson.chapterType,
              hasVideoUrl: firstHasVideoUrl,
              hasMediaFile: firstHasMediaFile,
              result: isFirstLessonMedia
            })
          }
          
          console.log('📚 初始课程类型检查:', {
            lessonTitle: firstLesson?.title,
            chapterType: firstLesson?.chapterType,
            videoUrl: firstLesson?.videoUrl,
            contentUrl: firstLesson?.contentUrl,
            isFirstLessonMedia: isFirstLessonMedia,
            计时策略: isFirstLessonMedia ? '初始暂停，等待播放' : '立即开始计时'
          })
          
          // 开始第一个章节的进度追踪，如果是媒体课程则初始暂停
          progressTracking.startTracking(courseCode, navigation.currentChapter.value, isFirstLessonMedia)
        }
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

// 组件卸载时清理进度追踪
onUnmounted(async () => {
  console.log('🧹 LearningPage 组件即将卸载，清理进度追踪')
  
  if (progressTracking.isTracking.value) {
    const currentProgress = progress.courseProgress.value
    await progressTracking.cleanup(currentProgress)
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

.course-progress {
  color: #6c757d;
  font-size: 11px;
}

.study-time {
  color: #667eea;
  font-weight: 500;
}

.paused-indicator {
  color: #ff9800;
  font-size: 11px;
  font-weight: normal;
}

.active-indicator {
  color: #4caf50;
  font-size: 11px;
  font-weight: normal;
}

.progress-hint {
  color: #ffa726;
  font-weight: 500;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { opacity: 1; }
  50% { opacity: 0.6; }
  100% { opacity: 1; }
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

.btn-completed {
  background: #6c757d;
  color: white;
  cursor: default;
}

.btn-completed:disabled {
  opacity: 0.8;
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