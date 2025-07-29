<template>
  <div class="enhanced-learning-container">
    <!-- 保留原有的系统头部导航 -->
<!--    <SystemHeader v-if="!hideSystemHeader" />-->

    <div class="learning-wrapper">
      <!-- 侧边栏 -->
      <div class="sidebar" :class="{ collapsed: sidebarCollapsed }">
        <!-- 折叠按钮 -->
        <button class="collapse-btn" @click="toggleSidebar">
          {{ sidebarCollapsed ? '➤' : '◀' }}
        </button>

        <!-- 课程信息 -->
        <div class="course-info" v-if="!sidebarCollapsed">
          <h2 class="course-title">{{ courseData.title }}</h2>
          <div class="course-meta">
            <span class="meta-item">📚 {{ courseData.category }}</span>
            <span class="meta-item">⏱️ 总时长 {{ courseData.totalDuration }}</span>
            <span class="meta-item">🎯 {{ courseData.difficulty }}</span>
            <span class="meta-item">📊 完成度 {{ courseProgress }}%</span>
          </div>
        </div>

        <!-- 章节列表 -->
        <div class="chapters-list" v-if="!sidebarCollapsed">
          <div v-for="chapter in courseData.chapters" :key="chapter.id" class="chapter-group">
            <div
              class="chapter-header"
              :class="{ expanded: expandedChapters.includes(chapter.id), active: currentChapter?.id === chapter.id }"
              @click="toggleChapter(chapter.id)"
            >
              <span class="chapter-icon">▶</span>
              <span class="chapter-title">{{ chapter.title }}</span>
              <span class="chapter-progress">{{ chapter.completedLessons }}/{{ chapter.lessons.length }} 完成</span>
            </div>

            <transition name="expand">
              <div v-if="expandedChapters.includes(chapter.id)" class="lessons-list">
                <div
                  v-for="lesson in chapter.lessons"
                  :key="lesson.id"
                  class="lesson-item"
                  :class="{ active: currentLesson?.id === lesson.id }"
                  @click="selectLesson(chapter, lesson)"
                >
                  <div class="lesson-icon" :class="getLessonIconClass(lesson)">
                    {{ getLessonIcon(lesson) }}
                  </div>
                  <div class="lesson-info">
                    <div class="lesson-title">{{ lesson.title }}</div>
                    <div class="lesson-meta">
                      <span>{{ getContentTypeIcon(lesson.contentType) }} {{ getContentTypeText(lesson.contentType) }}</span>
                      <span>{{ lesson.duration }}分钟</span>
                    </div>
                  </div>
                </div>
              </div>
            </transition>
          </div>
        </div>
      </div>

      <!-- 主内容区 -->
      <div class="main-content">
        <!-- 内容头部 -->
        <div class="content-header">
          <h1 class="content-title">{{ currentLesson?.title }}</h1>
          <div class="content-meta">
            <span>{{ getContentTypeIcon(currentLesson?.contentType) }} {{ getContentTypeText(currentLesson?.contentType) }}</span>
            <span>⏱️ 预计学习时间 {{ currentLesson?.duration }}分钟</span>
            <span>📅 更新于 {{ formatDate(currentLesson?.updatedAt) }}</span>
            <span>👁️ 已学习 {{ currentLesson?.studyDuration || 0 }}分钟</span>
          </div>
        </div>

        <!-- 内容类型切换 -->
        <div class="content-tabs">
          <div
            v-for="tab in contentTabs"
            :key="tab.key"
            class="tab-item"
            :class="{ active: activeTab === tab.key }"
            @click="switchTab(tab.key)"
          >
            <span>{{ tab.icon }}</span>
            <span>{{ tab.label }}</span>
          </div>
        </div>

        <!-- 内容区域 -->
        <div class="content-area">
          <!-- 主内容展示 -->
          <div v-if="activeTab === 'main'" class="main-content-section">
            <!-- 视频内容 -->
            <div v-if="currentLesson?.contentType === 'video'" class="video-container">
              <video
                ref="videoPlayer"
                :src="currentLesson.videoUrl"
                controls
                @timeupdate="onVideoTimeUpdate"
                @ended="onVideoEnded"
                class="video-player"
              >
                您的浏览器不支持视频播放
              </video>
            </div>

            <!-- 文档内容 -->
            <div v-else-if="currentLesson?.contentType === 'document'" class="document-viewer">
              <div class="document-toolbar">
                <div class="toolbar-left">
                  <button class="toolbar-btn" @click="zoomIn">
                    <span>🔍</span>
                    <span>放大</span>
                  </button>
                  <button class="toolbar-btn" @click="zoomOut">
                    <span>🔍</span>
                    <span>缩小</span>
                  </button>
                  <button class="toolbar-btn" @click="fitWidth">
                    <span>↔️</span>
                    <span>适应宽度</span>
                  </button>
                  <button class="toolbar-btn" @click="fullscreen">
                    <span>⛶</span>
                    <span>全屏</span>
                  </button>
                </div>
                <div class="toolbar-right">
                  <button class="toolbar-btn" @click="downloadDoc">
                    <span>⬇️</span>
                    <span>下载文档</span>
                  </button>
                  <button class="toolbar-btn" @click="printDoc">
                    <span>🖨️</span>
                    <span>打印</span>
                  </button>
                </div>
              </div>

              <!-- PDF查看器 -->
              <div class="pdf-viewer">
                <iframe
                  v-if="currentLesson?.documentUrl"
                  :src="currentLesson.documentUrl"
                  width="100%"
                  height="800"
                  frameborder="0"
                ></iframe>
                <div v-else class="pdf-placeholder">
                  <h2>{{ currentLesson?.documentName || '文档预览' }}</h2>
                  <p>文档正在加载中...</p>
                </div>
              </div>
            </div>

            <!-- 音频内容 -->
            <div v-else-if="currentLesson?.contentType === 'audio'" class="audio-container">
              <div class="audio-player-wrapper">
                <div class="audio-icon">🎵</div>
                <audio
                  ref="audioPlayer"
                  :src="currentLesson.audioUrl"
                  controls
                  @timeupdate="onAudioTimeUpdate"
                  @ended="onAudioEnded"
                  class="audio-player"
                >
                  您的浏览器不支持音频播放
                </audio>
              </div>
            </div>

            <!-- 混合内容 -->
            <div v-else-if="currentLesson?.contentType === 'mixed'" class="mixed-content">
              <!-- 可以同时显示视频和文档 -->
              <el-tabs v-model="mixedActiveTab">
                <el-tab-pane label="视频讲解" name="video">
                  <div class="video-container">
                    <video
                      v-if="currentLesson.videoUrl"
                      :src="currentLesson.videoUrl"
                      controls
                      class="video-player"
                    >
                      您的浏览器不支持视频播放
                    </video>
                  </div>
                </el-tab-pane>
                <el-tab-pane label="文档资料" name="document">
                  <div class="document-viewer">
                    <iframe
                      v-if="currentLesson.documentUrl"
                      :src="currentLesson.documentUrl"
                      width="100%"
                      height="600"
                      frameborder="0"
                    ></iframe>
                  </div>
                </el-tab-pane>
              </el-tabs>
            </div>
          </div>

          <!-- 相关资料 -->
          <div v-if="activeTab === 'materials'" class="materials-section">
            <h3 class="section-title">
              <span>📚</span>
              <span>本节相关资料</span>
            </h3>
            <div class="materials-grid">
              <div
                v-for="material in currentLesson?.supplementaryFiles"
                :key="material.id"
                class="material-card"
                @click="viewMaterial(material)"
              >
                <div class="material-header">
                  <div class="material-icon">{{ getFileIcon(material.name) }}</div>
                  <div class="material-info">
                    <div class="material-title">{{ material.name }}</div>
                    <div class="material-meta">
                      {{ material.type }} • {{ formatFileSize(material.size) }} • 更新于{{ formatDate(material.updatedAt) }}
                    </div>
                  </div>
                </div>
                <div class="material-actions">
                  <button class="btn btn-secondary" @click.stop="previewMaterial(material)">预览</button>
                  <button class="btn btn-primary" @click.stop="downloadMaterial(material)">下载</button>
                </div>
              </div>
            </div>

            <!-- 如果没有补充资料 -->
            <el-empty v-if="!currentLesson?.supplementaryFiles?.length" description="暂无相关资料" />
          </div>

          <!-- 学习笔记 -->
          <div v-if="activeTab === 'notes'" class="notes-section">
            <div class="notes-header">
              <h3 class="section-title">
                <span>📝</span>
                <span>我的学习笔记</span>
              </h3>
              <el-button type="primary" size="small" @click="saveNotes">保存笔记</el-button>
            </div>
            <el-input
              v-model="lessonNotes"
              type="textarea"
              :rows="15"
              placeholder="在这里记录您的学习心得和要点..."
              class="notes-editor"
            />
          </div>
        </div>

        <!-- 学习进度 -->
        <div class="progress-section">
          <div class="progress-text">
            <span>章节进度</span>
            <span>{{ chapterProgress }}% 完成</span>
          </div>
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: chapterProgress + '%' }"></div>
          </div>
          <div class="navigation-buttons">
            <el-button @click="previousLesson" :disabled="!hasPreviousLesson">
              ← 上一节
            </el-button>
            <el-button type="primary" @click="completeAndNext">
              {{ currentLesson?.completed ? '进入下一节' : '标记完成并进入下一节' }} →
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElEmpty, ElTabs, ElTabPane, ElButton, ElInput } from 'element-plus'
import SystemHeader from '@/components/learning/SystemHeader.vue'
import { getCourseDetailAPI, updateStudyProgressAPI, getCourseChaptersAPI } from '@/api/course'

// 路由
const route = useRoute()
const router = useRouter()

// Props
const props = defineProps({
  hideSystemHeader: {
    type: Boolean,
    default: false
  }
})

// 响应式数据
const sidebarCollapsed = ref(false)
const expandedChapters = ref([])
const activeTab = ref('main')
const mixedActiveTab = ref('video')
const courseData = ref({
  title: '',
  category: '',
  totalDuration: '',
  difficulty: '',
  chapters: []
})
const currentChapter = ref(null)
const currentLesson = ref(null)
const lessonNotes = ref('')
const courseProgress = ref(0)
const chapterProgress = ref(0)

// 内容标签配置
const contentTabs = computed(() => {
  const tabs = [
    { key: 'main', icon: getContentTypeIcon(currentLesson.value?.contentType), label: '主内容' }
  ]

  if (currentLesson.value?.supplementaryFiles?.length > 0) {
    tabs.push({ key: 'materials', icon: '📎', label: '相关资料' })
  }

  tabs.push({ key: 'notes', icon: '📝', label: '学习笔记' })

  return tabs
})

// 计算属性
const hasPreviousLesson = computed(() => {
  if (!currentChapter.value || !currentLesson.value) return false

  const chapterIndex = courseData.value.chapters.findIndex(c => c.id === currentChapter.value.id)
  const lessonIndex = currentChapter.value.lessons.findIndex(l => l.id === currentLesson.value.id)

  return chapterIndex > 0 || lessonIndex > 0
})

const hasNextLesson = computed(() => {
  if (!currentChapter.value || !currentLesson.value) return false

  const chapterIndex = courseData.value.chapters.findIndex(c => c.id === currentChapter.value.id)
  const lessonIndex = currentChapter.value.lessons.findIndex(l => l.id === currentLesson.value.id)

  const isLastChapter = chapterIndex === courseData.value.chapters.length - 1
  const isLastLesson = lessonIndex === currentChapter.value.lessons.length - 1

  return !(isLastChapter && isLastLesson)
})

// 方法
const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

const toggleChapter = (chapterId) => {
  const index = expandedChapters.value.indexOf(chapterId)
  if (index > -1) {
    expandedChapters.value.splice(index, 1)
  } else {
    expandedChapters.value.push(chapterId)
  }
}

const selectLesson = (chapter, lesson) => {
  currentChapter.value = chapter
  currentLesson.value = lesson

  // 加载课程笔记
  loadLessonNotes(lesson.id)

  // 更新学习记录
  updateStudyRecord()

  // 切换到主内容标签
  activeTab.value = 'main'
}

const switchTab = (tab) => {
  activeTab.value = tab
}

// 获取课程图标
const getLessonIcon = (lesson) => {
  if (lesson.completed) return '✓'
  if (lesson.id === currentLesson.value?.id) return '▶'
  return lesson.sortOrder || '•'
}

const getLessonIconClass = (lesson) => {
  if (lesson.completed) return 'completed'
  if (lesson.id === currentLesson.value?.id) return 'current'
  return ''
}

// 内容类型相关
const getContentTypeIcon = (type) => {
  const icons = {
    video: '🎥',
    document: '📄',
    audio: '🎵',
    mixed: '📦'
  }
  return icons[type] || '📄'
}

const getContentTypeText = (type) => {
  const texts = {
    video: '视频',
    document: '文档',
    audio: '音频',
    mixed: '混合内容'
  }
  return texts[type] || '文档'
}

// 文件相关
const getFileIcon = (filename) => {
  const ext = filename.split('.').pop().toLowerCase()
  const iconMap = {
    pdf: '📑',
    doc: '📄',
    docx: '📄',
    xls: '📊',
    xlsx: '📊',
    ppt: '📽️',
    pptx: '📽️',
    mp4: '🎥',
    mp3: '🎵',
    jpg: '🖼️',
    png: '🖼️'
  }
  return iconMap[ext] || '📎'
}

// 文档操作
const zoomIn = () => {
  ElMessage.info('放大文档')
}

const zoomOut = () => {
  ElMessage.info('缩小文档')
}

const fitWidth = () => {
  ElMessage.info('适应宽度')
}

const fullscreen = () => {
  ElMessage.info('全屏查看')
}

const downloadDoc = () => {
  if (currentLesson.value?.documentUrl) {
    window.open(currentLesson.value.documentUrl, '_blank')
  }
}

const printDoc = () => {
  window.print()
}

// 资料操作
const viewMaterial = (material) => {
}

const previewMaterial = (material) => {
  ElMessage.info(`预览: ${material.name}`)
}

const downloadMaterial = (material) => {
  if (material.url) {
    window.open(material.url, '_blank')
  }
}

// 导航操作
const previousLesson = () => {
  // 实现上一节逻辑
  const chapterIndex = courseData.value.chapters.findIndex(c => c.id === currentChapter.value.id)
  const lessonIndex = currentChapter.value.lessons.findIndex(l => l.id === currentLesson.value.id)

  if (lessonIndex > 0) {
    selectLesson(currentChapter.value, currentChapter.value.lessons[lessonIndex - 1])
  } else if (chapterIndex > 0) {
    const prevChapter = courseData.value.chapters[chapterIndex - 1]
    selectLesson(prevChapter, prevChapter.lessons[prevChapter.lessons.length - 1])
  }
}

const completeAndNext = async () => {
  // 标记当前课程完成
  if (!currentLesson.value.completed) {
    currentLesson.value.completed = true
    await updateStudyProgressAPI({
      courseId: route.params.courseId,
      lessonId: currentLesson.value.id,
      progress: 100
    })
  }

  // 进入下一节
  if (hasNextLesson.value) {
    const chapterIndex = courseData.value.chapters.findIndex(c => c.id === currentChapter.value.id)
    const lessonIndex = currentChapter.value.lessons.findIndex(l => l.id === currentLesson.value.id)

    if (lessonIndex < currentChapter.value.lessons.length - 1) {
      selectLesson(currentChapter.value, currentChapter.value.lessons[lessonIndex + 1])
    } else if (chapterIndex < courseData.value.chapters.length - 1) {
      const nextChapter = courseData.value.chapters[chapterIndex + 1]
      selectLesson(nextChapter, nextChapter.lessons[0])
    }
  } else {
    ElMessage.success('恭喜您完成本课程的学习！')
    router.push('/courses')
  }
}

// 视频/音频事件
const onVideoTimeUpdate = (e) => {
  // 更新学习进度
  const progress = (e.target.currentTime / e.target.duration) * 100
  updateProgress(progress)
}

const onVideoEnded = () => {
  ElMessage.success('视频播放完成')
}

const onAudioTimeUpdate = (e) => {
  const progress = (e.target.currentTime / e.target.duration) * 100
  updateProgress(progress)
}

const onAudioEnded = () => {
  ElMessage.success('音频播放完成')
}

// 辅助方法
const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN')
}

const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const updateProgress = (progress) => {
  // 更新进度逻辑
}

const updateStudyRecord = () => {
  // 更新学习记录
}

const loadLessonNotes = (lessonId) => {
  // 加载课程笔记
  lessonNotes.value = localStorage.getItem(`lesson_notes_${lessonId}`) || ''
}

const saveNotes = () => {
  if (currentLesson.value) {
    localStorage.setItem(`lesson_notes_${currentLesson.value.id}`, lessonNotes.value)
    ElMessage.success('笔记保存成功')
  }
}

// 加载课程数据
const loadCourseData = async () => {
  try {
    const res = await getCourseDetailAPI(route.params.courseId)

    // 处理不同的响应格式
    let courseDetail = null
    if (res.data) {
      courseDetail = res.data
    } else if (res.code === 200 && res.data) {
      courseDetail = res.data
    } else if (res.success && res.data) {
      courseDetail = res.data
    } else {
      // 如果是直接返回的数据
      courseDetail = res
    }

    // 确保有 chapters 数组
    if (!courseDetail.chapters) {
      // 尝试获取章节数据
      try {
        const chaptersRes = await getCourseChaptersAPI(route.params.courseId)
        courseDetail.chapters = chaptersRes.data || []
      } catch (e) {
        courseDetail.chapters = []
      }
    }

    // 转换章节数据格式，确保每个章节都有 lessons 数组
    if (courseDetail.chapters && Array.isArray(courseDetail.chapters)) {
      courseDetail.chapters = courseDetail.chapters.map(chapter => {
        // 将章节本身作为一个 lesson（兼容旧数据格式）
        const lessons = chapter.lessons || [{
          id: chapter.id,
          title: chapter.title,
          sortOrder: 1,
          contentType: chapter.chapterType || chapter.contentType || 'document',
          duration: chapter.duration || 15,
          completed: false,
          videoUrl: chapter.videoUrl || '',
          documentUrl: chapter.contentUrl || chapter.content || '',
          audioUrl: chapter.audioUrl || '',
          description: chapter.description || '',
          supplementaryFiles: []
        }]

        return {
          id: chapter.id,
          title: chapter.title,
          sortOrder: chapter.sortOrder || chapter.order || 1,
          completedLessons: 0,
          lessons: lessons
        }
      })
    } else {
      courseDetail.chapters = []
    }

    // 设置课程基本信息
    courseData.value = {
      id: courseDetail.id,
      title: courseDetail.title || '未命名课程',
      category: courseDetail.category || '未分类',
      difficulty: courseDetail.difficulty || courseDetail.difficultyLevel || '中级',
      totalDuration: courseDetail.totalDuration || courseDetail.estimatedDuration || '未知',
      instructor: courseDetail.instructor || {
        name: courseDetail.instructorId || '未知讲师',
        avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=instructor'
      },
      chapters: courseDetail.chapters
    }


    // 自动选择第一个章节的第一个课程
    if (courseData.value.chapters && courseData.value.chapters.length > 0) {
      const firstChapter = courseData.value.chapters[0]
      expandedChapters.value = [firstChapter.id]

      if (firstChapter.lessons && firstChapter.lessons.length > 0) {
        selectLesson(firstChapter, firstChapter.lessons[0])
      }
    }

    // 计算课程进度
    calculateProgress()
  } catch (error) {
    ElMessage.error('加载课程失败，请稍后重试')

    // 设置默认数据避免渲染错误
    courseData.value = {
      title: '加载失败',
      category: '',
      totalDuration: '',
      difficulty: '',
      chapters: []
    }
  }
}

const calculateProgress = () => {
  // 计算整体进度
  let totalLessons = 0
  let completedLessons = 0

  courseData.value.chapters.forEach(chapter => {
    totalLessons += chapter.lessons.length
    completedLessons += chapter.lessons.filter(l => l.completed).length
  })

  courseProgress.value = totalLessons > 0 ? Math.round((completedLessons / totalLessons) * 100) : 0

  // 计算当前章节进度
  if (currentChapter.value) {
    const completed = currentChapter.value.lessons.filter(l => l.completed).length
    const total = currentChapter.value.lessons.length
    chapterProgress.value = total > 0 ? Math.round((completed / total) * 100) : 0
  }
}

// 生命周期
onMounted(() => {
  loadCourseData()
})

// 监听课程完成状态变化
watch(() => currentLesson.value?.completed, () => {
  calculateProgress()
})
</script>

<style scoped>
.enhanced-learning-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.learning-wrapper {
  display: flex;
  gap: 20px;
  max-width: 1600px;
  margin: 0 auto;
  padding: 20px;
  height: calc(100vh - 90px);
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
  position: relative;
  transition: width 0.3s ease;
}

.sidebar.collapsed {
  width: 60px;
}

.collapse-btn {
  position: absolute;
  right: -12px;
  top: 20px;
  width: 24px;
  height: 24px;
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 10;
  transition: all 0.3s ease;
}

.collapse-btn:hover {
  background: #f5f5f5;
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

.chapters-list {
  flex: 1;
  padding: 10px 0;
}

.chapter-group {
  margin-bottom: 10px;
}

.chapter-header {
  padding: 12px 20px;
  background: #f8f9fa;
  border-left: 4px solid #667eea;
  margin-bottom: 5px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 10px;
  transition: all 0.3s ease;
  user-select: none;
}

.chapter-header:hover {
  background: #e9ecef;
}

.chapter-header.active {
  background: rgba(102, 126, 234, 0.1);
}

.chapter-icon {
  font-size: 14px;
  transition: transform 0.3s ease;
}

.chapter-header.expanded .chapter-icon {
  transform: rotate(90deg);
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
  padding: 2px 8px;
  border-radius: 8px;
}

.lessons-list {
  margin-left: 20px;
}

.lesson-item {
  padding: 10px 20px;
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 8px;
  margin: 3px 0;
}

.lesson-item:hover {
  background: rgba(102, 126, 234, 0.05);
}

.lesson-item.active {
  background: rgba(102, 126, 234, 0.1);
  border-left: 3px solid #667eea;
}

.lesson-icon {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  background: #f0f0f0;
  border-radius: 50%;
}

.lesson-icon.completed {
  background: #28a745;
  color: white;
}

.lesson-icon.current {
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

.lesson-meta {
  font-size: 12px;
  color: #666;
  display: flex;
  gap: 10px;
}

/* 主内容区 */
.main-content {
  flex: 1;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 15px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.content-header {
  padding: 20px 30px;
  border-bottom: 1px solid #e0e0e0;
  background: rgba(102, 126, 234, 0.05);
}

.content-title {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 10px;
  color: #333;
}

.content-meta {
  display: flex;
  align-items: center;
  gap: 20px;
  color: #666;
  font-size: 14px;
}

/* 内容类型切换标签 */
.content-tabs {
  display: flex;
  padding: 0 30px;
  background: #f8f9fa;
  border-bottom: 1px solid #e0e0e0;
}

.tab-item {
  padding: 15px 20px;
  cursor: pointer;
  border-bottom: 3px solid transparent;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}

.tab-item:hover {
  background: rgba(102, 126, 234, 0.05);
}

.tab-item.active {
  border-bottom-color: #667eea;
  color: #667eea;
  font-weight: 500;
}

/* 内容区域 */
.content-area {
  flex: 1;
  overflow-y: auto;
  padding: 30px;
}

/* 视频内容 */
.video-container {
  background: #000;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 20px;
  aspect-ratio: 16/9;
  display: flex;
  align-items: center;
  justify-content: center;
}

.video-player {
  width: 100%;
  height: 100%;
}

/* 音频内容 */
.audio-container {
  display: flex;
  justify-content: center;
  padding: 60px 0;
}

.audio-player-wrapper {
  background: #f8f9fa;
  border-radius: 15px;
  padding: 40px;
  text-align: center;
}

.audio-icon {
  font-size: 80px;
  margin-bottom: 30px;
}

.audio-player {
  width: 400px;
}

/* 文档内容 */
.document-viewer {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
  min-height: 500px;
}

.document-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 10px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.toolbar-left,
.toolbar-right {
  display: flex;
  gap: 10px;
}

.toolbar-btn {
  padding: 8px 16px;
  border: 1px solid #dcdfe6;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 5px;
}

.toolbar-btn:hover {
  background: #f5f7fa;
  border-color: #667eea;
  color: #667eea;
}

.pdf-viewer {
  width: 100%;
  height: 800px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  background: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.pdf-placeholder {
  text-align: center;
  color: #666;
}

/* 相关资料 */
.materials-section {
  margin-top: 30px;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.materials-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 15px;
}

.material-card {
  background: white;
  border-radius: 10px;
  padding: 20px;
  border: 1px solid #e0e0e0;
  transition: all 0.3s ease;
  cursor: pointer;
}

.material-card:hover {
  border-color: #667eea;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.1);
  transform: translateY(-2px);
}

.material-header {
  display: flex;
  align-items: start;
  gap: 15px;
  margin-bottom: 10px;
}

.material-icon {
  width: 48px;
  height: 48px;
  background: #f0f2f5;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.material-info {
  flex: 1;
}

.material-title {
  font-weight: 500;
  color: #303133;
  margin-bottom: 5px;
}

.material-meta {
  font-size: 12px;
  color: #909399;
}

.material-actions {
  display: flex;
  gap: 10px;
  margin-top: 15px;
}

/* 笔记编辑器 */
.notes-section {
  background: white;
  border-radius: 10px;
  padding: 20px;
}

.notes-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.notes-editor {
  width: 100%;
}

/* 学习进度 */
.progress-section {
  padding: 20px 30px;
  background: rgba(102, 126, 234, 0.05);
  border-top: 1px solid #e0e0e0;
}

.progress-bar {
  height: 8px;
  background: #e0e0e0;
  border-radius: 4px;
  overflow: hidden;
  margin: 10px 0;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #667eea, #764ba2);
  transition: width 0.3s ease;
}

.progress-text {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  color: #666;
}

.navigation-buttons {
  display: flex;
  justify-content: space-between;
  margin-top: 15px;
}

/* 按钮样式 */
.btn {
  padding: 8px 16px;
  border-radius: 6px;
  border: none;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-primary {
  background: #667eea;
  color: white;
}

.btn-primary:hover {
  background: #5a67d8;
}

.btn-secondary {
  background: white;
  color: #606266;
  border: 1px solid #dcdfe6;
}

.btn-secondary:hover {
  background: #f5f7fa;
  border-color: #c0c4cc;
}

/* 过渡动画 */
.expand-enter-active,
.expand-leave-active {
  transition: all 0.3s ease;
  overflow: hidden;
}

.expand-enter-from,
.expand-leave-to {
  max-height: 0;
  opacity: 0;
}

.expand-enter-to,
.expand-leave-from {
  max-height: 500px;
  opacity: 1;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .learning-wrapper {
    flex-direction: column;
    height: auto;
  }

  .sidebar {
    width: 100%;
    height: auto;
    max-height: 400px;
  }

  .sidebar.collapsed {
    max-height: 60px;
  }

  .materials-grid {
    grid-template-columns: 1fr;
  }
}
</style>
