<template>
  <div class="learning-page">
    <!-- 系统顶部导航 -->
    <div class="system-header">
      <div class="container">
        <div class="system-nav">
          <div class="logo">
            <div class="logo-icon">🎓</div>
            <h1>智能培训系统</h1>
          </div>

          <div class="nav-menu">
            <div class="nav-item" @click="goToPage('/dashboard')">📊 仪表板</div>
            <div class="nav-item active" @click="goToPage('/courses')">📚 我的课程</div>
            <div class="nav-item" @click="goToPage('/exams')">📝 考试中心</div>
            <div v-if="showStudentManagement" class="nav-item" @click="goToPage('/students')">👥 学员管理</div>
            <div v-if="showAdminPanel" class="nav-item" @click="goToPage('/admin')">⚙️ 管理后台</div>
          </div>

          <div class="user-info">
            <div class="user-name">{{ userName }}</div>
            <div class="avatar">{{ userAvatar }}</div>
            <button class="logout-btn" @click="handleLogout">退出登录</button>
          </div>
        </div>
      </div>
    </div>

    <div class="container">
      <!-- 课程导航 -->
      <header class="header">
        <div class="course-breadcrumb">
          <a href="#" @click="goBack">📚 我的课程</a>
          <span>></span>
          <span>{{ courseData.title }}</span>
          <span>></span>
          <span>{{ currentChapterTitle }}</span>
        </div>
        <div class="course-header-actions">
          <div class="progress-info">
            <span>总进度：</span>
            <div class="progress-bar-small">
              <div class="progress-fill-small" :style="{ width: courseProgress + '%' }"></div>
            </div>
            <span>{{ courseProgress }}%</span>
          </div>
          <button class="btn btn-secondary" @click="toggleBookmark">🔖 收藏</button>
          <button class="btn btn-secondary" @click="showSettings">⚙️ 设置</button>
        </div>
      </header>

      <!-- 主要学习区域 -->
      <div class="learning-layout">
        <!-- 课程章节侧边栏 -->
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
                @click="toggleChapter(chapterIndex + 1)"
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
                  @click="selectLesson(chapterIndex + 1, lessonIndex + 1)"
                >
                  <div class="lesson-icon" :class="{
                    completed: lesson.completed,
                    current: currentChapter === chapterIndex + 1 && currentLesson === lessonIndex + 1
                  }">
                    <span v-if="lesson.completed">✓</span>
                    <span v-else-if="currentChapter === chapterIndex + 1 && currentLesson === lessonIndex + 1">▶</span>
                    <span v-else>{{ lessonIndex + 1 }}</span>
                  </div>
                  <span>{{ lesson.title }}</span>
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
            <div class="lesson-title">{{ currentLessonData?.title }}</div>
            <div class="lesson-meta">
              <span>📹 视频课程</span>
              <span>⏱️ {{ currentLessonData?.duration }}</span>
              <span>👁️ 已观看 {{ watchedTime }}</span>
              <span>📅 更新于 {{ updateDate }}</span>
            </div>
          </div>

          <!-- 视频播放器 -->
          <div class="video-container">
            <div class="video-player" @click="toggleVideoControls">
              <div v-if="!isPlaying" class="video-placeholder">
                <div class="play-button" @click="playVideo">▶</div>
                <h3>{{ currentLessonData?.title }}</h3>
                <p>{{ currentLessonData?.description }}</p>
              </div>

              <div class="video-controls" :class="{ show: showControls }">
                <div class="progress-bar-container">
                  <div class="video-progress-bar" @click="seekVideo">
                    <div class="video-progress-fill" :style="{ width: videoProgress + '%' }"></div>
                  </div>
                </div>
                <div class="control-buttons">
                  <div class="control-left">
                    <button class="control-btn" @click="togglePlay">{{ isPlaying ? '⏸️' : '▶️' }}</button>
                    <button class="control-btn" @click="skipBackward">⏪</button>
                    <button class="control-btn" @click="skipForward">⏩</button>
                    <div class="time-display">
                      <span>{{ formatTime(currentTime) }}</span> / <span>{{ formatTime(totalTime) }}</span>
                    </div>
                  </div>
                  <div class="control-right">
                    <select class="speed-selector" v-model="playbackSpeed" @change="changeSpeed">
                      <option value="0.5">0.5x</option>
                      <option value="1">1.0x</option>
                      <option value="1.25">1.25x</option>
                      <option value="1.5">1.5x</option>
                      <option value="2">2.0x</option>
                    </select>
                    <button class="control-btn" @click="toggleSubtitles">CC</button>
                    <button class="control-btn" @click="toggleFullscreen">⛶</button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 学习内容标签页 -->
          <div class="content-tabs">
            <button
              v-for="tab in tabs"
              :key="tab.key"
              class="tab-button"
              :class="{ active: activeTab === tab.key }"
              @click="showTab(tab.key)"
            >
              {{ tab.label }}
            </button>
          </div>

          <div class="tab-content">
            <!-- 课程介绍 -->
            <div v-if="activeTab === 'intro'" class="tab-panel active">
              <div class="course-intro">
                <h3>📚 本节课程内容</h3>
                <p>{{ currentLessonData?.intro }}</p>

                <div class="highlight-box">
                  <strong>🎯 学习目标</strong>
                  <ul>
                    <li v-for="goal in currentLessonData?.goals" :key="goal">{{ goal }}</li>
                  </ul>
                </div>

                <div v-html="currentLessonData?.content"></div>
              </div>
            </div>

            <!-- 我的笔记 -->
            <div v-if="activeTab === 'notes'" class="tab-panel active">
              <div class="notes-section">
                <div class="notes-toolbar">
                  <input
                    type="text"
                    class="note-search"
                    placeholder="搜索笔记内容..."
                    v-model="noteSearchKeyword"
                  >
                  <button class="btn btn-primary" @click="showAddNoteModal">➕ 添加笔记</button>
                </div>

                <div class="notes-list">
                  <div v-for="note in filteredNotes" :key="note.id" class="note-item">
                    <div class="note-header">
                      <div class="note-time">{{ note.time }} · {{ note.location }}</div>
                      <div class="note-timestamp" @click="jumpToTime(note.timestamp)">{{ note.timestamp }}</div>
                    </div>
                    <div class="note-content">{{ note.content }}</div>
                    <div class="note-actions">
                      <button class="note-btn" @click="editNote(note.id)">编辑</button>
                      <button class="note-btn" @click="deleteNote(note.id)">删除</button>
                      <button class="note-btn" @click="shareNote(note.id)">分享</button>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 课堂测验 -->
            <div v-if="activeTab === 'quiz'" class="tab-panel active">
              <div class="quiz-section">
                <div class="quiz-header">
                  <h3>🧠 课堂测验 - {{ currentLessonData?.title }}</h3>
                  <div class="quiz-progress">
                    <span>题目 {{ currentQuestionIndex + 1 }} / {{ quizData.length }}</span>
                    <div class="quiz-progress-bar">
                      <div class="quiz-progress-fill" :style="{ width: quizProgress + '%' }"></div>
                    </div>
                    <span>剩余时间: {{ formatTime(remainingTime) }}</span>
                  </div>
                </div>

                <div class="quiz-content" v-if="currentQuestion">
                  <div class="question-card">
                    <div class="question-header">
                      <div class="question-number">第 {{ currentQuestionIndex + 1 }} 题</div>
                      <div class="question-type">{{ currentQuestion.type }}</div>
                    </div>

                    <div class="question-text">{{ currentQuestion.question }}</div>

                    <div class="options-list">
                      <div
                        v-for="(option, index) in currentQuestion.options"
                        :key="index"
                        class="option-item"
                        :class="{ selected: selectedAnswer === index }"
                        @click="selectOption(index)"
                      >
                        <div class="option-letter">{{ String.fromCharCode(65 + index) }}</div>
                        <div class="option-text">{{ option }}</div>
                      </div>
                    </div>
                  </div>
                </div>

                <div class="quiz-actions">
                  <button class="btn btn-secondary" @click="previousQuestion" :disabled="currentQuestionIndex === 0">← 上一题</button>
                  <div style="display: flex; gap: 10px;">
                    <button class="btn btn-secondary" @click="skipQuestion">跳过</button>
                    <button class="btn btn-primary" @click="nextQuestion">
                      {{ currentQuestionIndex === quizData.length - 1 ? '提交' : '下一题 →' }}
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <!-- 相关资料 -->
            <div v-if="activeTab === 'resources'" class="tab-panel active">
              <div class="resources-section">
                <h3>📎 课程相关资料</h3>

                <div class="resources-list">
                  <div v-for="resource in currentLessonData?.resources" :key="resource.id" class="resource-item">
                    <div class="resource-icon">{{ resource.icon }}</div>
                    <div class="resource-info">
                      <div class="resource-title">{{ resource.title }}</div>
                      <div class="resource-meta">{{ resource.type }} · {{ resource.size }} · 下载 {{ resource.downloads }} 次</div>
                    </div>
                    <button class="btn btn-primary">{{ resource.action }}</button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 课程导航 -->
          <div class="lesson-navigation">
            <button
              class="btn btn-secondary nav-btn"
              @click="previousLesson"
              :disabled="!hasPreviousLesson"
            >
              ← {{ previousLessonTitle }}
            </button>

            <button class="lesson-complete-btn" @click="completeLesson">
              ✓ 标记为已完成
            </button>

            <button
              class="btn btn-primary nav-btn"
              @click="nextLesson"
              :disabled="!hasNextLesson"
            >
              {{ nextLessonTitle }} →
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 浮动添加笔记按钮 -->
    <button class="add-note-btn" @click="showAddNoteModal" title="添加笔记">✎</button>

    <!-- 添加笔记弹窗 -->
    <div class="modal-overlay" :class="{ show: showNoteModal }" @click="hideNoteModal">
      <div class="modal" @click.stop>
        <div class="modal-header">
          <div class="modal-title">📝 添加学习笔记</div>
          <button class="modal-close" @click="hideNoteModal">×</button>
        </div>

        <form class="note-form" @submit.prevent="saveNote">
          <div class="form-row">
            <div class="form-group">
              <label>时间点</label>
              <input type="text" v-model="newNote.timestamp" placeholder="04:23">
            </div>
            <div class="form-group">
              <label>标签</label>
              <select v-model="newNote.tag">
                <option>重要概念</option>
                <option>疑问</option>
                <option>心得体会</option>
                <option>实践要点</option>
              </select>
            </div>
          </div>

          <div class="form-group">
            <label>笔记内容</label>
            <textarea
              v-model="newNote.content"
              class="note-textarea"
              placeholder="在这里记录你的学习心得、疑问或重要知识点..."
              required
            ></textarea>
          </div>

          <div class="form-actions">
            <button type="button" class="btn btn-secondary" @click="hideNoteModal">取消</button>
            <button type="submit" class="btn btn-primary">保存笔记</button>
          </div>
        </form>
      </div>
    </div>

    <!-- 成功提示 -->
    <div class="success-message" :class="{ show: showSuccessMessage }">
      {{ successMessageText }}
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 用户信息
const userName = computed(() => {
  return userStore.userInfo?.name || userStore.userInfo?.username || '用户'
})

const userAvatar = computed(() => {
  const name = userName.value
  return name.charAt(0).toUpperCase()
})

const userRole = computed(() => {
  return userStore.userInfo?.role || 'STUDENT'
})

const showStudentManagement = computed(() => {
  return ['ADMIN', 'TEACHER'].includes(userRole.value)
})

const showAdminPanel = computed(() => {
  return userRole.value === 'ADMIN'
})

// 导航方法
const goToPage = (path) => {
  router.push(path)
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要退出登录吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await userStore.logout()
    ElMessage.success('已退出登录')
    router.replace('/login')
  } catch (error) {
    // 用户取消
    console.log('用户取消退出登录')
  }
}

// 响应式数据
const currentChapter = ref(1)
const currentLesson = ref(2)
const isPlaying = ref(false)
const showControls = ref(false)
const videoProgress = ref(35)
const currentTime = ref(263) // 4:23 = 263秒
const totalTime = ref(720) // 12:00 = 720秒
const playbackSpeed = ref('1')
const courseProgress = ref(35)
const activeTab = ref('intro')
const noteSearchKeyword = ref('')
const showNoteModal = ref(false)
const showSuccessMessage = ref(false)
const successMessageText = ref('')

// 测验相关
const currentQuestionIndex = ref(1)
const selectedAnswer = ref(2) // 默认选中第3个选项
const remainingTime = ref(512) // 8:32 = 512秒

// 新笔记
const newNote = ref({
  timestamp: '04:23',
  tag: '重要概念',
  content: ''
})

// 标签页配置
const tabs = [
  { key: 'intro', label: '📋 课程介绍' },
  { key: 'notes', label: '📝 我的笔记 (3)' },
  { key: 'quiz', label: '🧠 课堂测验' },
  { key: 'resources', label: '📎 相关资料' }
]

// 课程数据
const courseData = ref({
  id: route.params.courseId || 'product-basic',
  title: '产品基础知识培训',
  subtitle: '共4章 · 12节课 · 预计2小时',
  chapters: [
    {
      id: 1,
      title: '第1章：产品概述',
      icon: '📘',
      expanded: true,
      lessons: [
        { id: 1, title: '什么是产品？', duration: '8分钟', completed: true },
        { id: 2, title: '产品的生命周期', duration: '12分钟', completed: false },
        { id: 3, title: '产品经理的角色', duration: '15分钟', completed: false }
      ]
    },
    {
      id: 2,
      title: '第2章：市场分析',
      icon: '📊',
      expanded: false,
      lessons: [
        { id: 1, title: '市场调研方法', duration: '18分钟', completed: false },
        { id: 2, title: '竞品分析', duration: '22分钟', completed: false },
        { id: 3, title: '用户画像构建', duration: '16分钟', completed: false },
        { id: 4, title: '需求分析', duration: '20分钟', completed: false }
      ]
    },
    {
      id: 3,
      title: '第3章：产品设计',
      icon: '🎨',
      expanded: false,
      lessons: [
        { id: 1, title: '原型设计', duration: '25分钟', completed: false },
        { id: 2, title: '交互设计', duration: '20分钟', completed: false },
        { id: 3, title: '用户体验测试', duration: '18分钟', completed: false }
      ]
    },
    {
      id: 4,
      title: '第4章：产品运营',
      icon: '🚀',
      expanded: false,
      lessons: [
        { id: 1, title: '产品上线与推广', duration: '30分钟', completed: false },
        { id: 2, title: '数据分析与优化', duration: '28分钟', completed: false }
      ]
    }
  ]
})

// 当前课程信息
const currentChapterTitle = computed(() => {
  const chapter = courseData.value.chapters[currentChapter.value - 1]
  return chapter ? chapter.title : ''
})

const currentLessonData = computed(() => {
  const chapter = courseData.value.chapters[currentChapter.value - 1]
  if (chapter && chapter.lessons[currentLesson.value - 1]) {
    const lesson = chapter.lessons[currentLesson.value - 1]
    return {
      ...lesson,
      intro: '在这节课中，我们将深入了解产品的完整生命周期，从概念诞生到最终退市的全过程。',
      description: '了解产品从概念到退市的完整生命周期',
      goals: [
        '掌握产品生命周期的四个主要阶段',
        '了解每个阶段的特点和策略重点',
        '学会分析产品当前所处的生命周期阶段',
        '制定相应的产品管理策略'
      ],
      content: `
        <h3>🔄 产品生命周期的四个阶段</h3>
        <h4>1. 引入期 (Introduction Stage)</h4>
        <p>产品刚刚进入市场的阶段，特点包括：</p>
        <ul>
          <li>销量增长缓慢，市场接受度有限</li>
          <li>营销成本高，需要大量投入进行市场教育</li>
          <li>产品价格通常较高</li>
        </ul>
      `,
      resources: [
        {
          id: 1,
          title: '产品生命周期分析模板.xlsx',
          type: 'Excel表格',
          size: '285KB',
          downloads: 156,
          icon: '📄',
          action: '下载'
        },
        {
          id: 2,
          title: '经典产品案例分析.pdf',
          type: 'PDF文档',
          size: '2.1MB',
          downloads: 203,
          icon: '📊',
          action: '下载'
        }
      ]
    }
  }
  return null
})

// 笔记数据
const notes = ref([
  {
    id: 1,
    time: '2分钟前',
    location: '第1章第2节',
    timestamp: '02:15',
    content: '产品生命周期的四个阶段很重要，需要根据不同阶段制定不同的策略。引入期主要是市场教育，成长期要扩大份额。'
  },
  {
    id: 2,
    time: '5分钟前',
    location: '第1章第2节',
    timestamp: '05:30',
    content: '成熟期的特点：竞争激烈、价格战、利润率下降。这时候需要寻找新的增长点，可能需要产品创新或进入新市场。'
  },
  {
    id: 3,
    time: '昨天',
    location: '第1章第1节',
    timestamp: '08:45',
    content: '产品经理的核心职责是连接用户需求和技术实现，需要具备市场洞察力、沟通能力和项目管理能力。'
  }
])

// 测验数据
const quizData = ref([
  {
    id: 1,
    type: '单选题',
    question: '产品生命周期包含几个阶段？',
    options: ['3个', '4个', '5个', '6个'],
    correct: 1
  },
  {
    id: 2,
    type: '单选题',
    question: '在产品生命周期的哪个阶段，通常会出现价格战和激烈竞争？',
    options: ['引入期', '成长期', '成熟期', '衰退期'],
    correct: 2
  }
])

// 计算属性
const filteredNotes = computed(() => {
  if (!noteSearchKeyword.value) return notes.value
  return notes.value.filter(note =>
    note.content.toLowerCase().includes(noteSearchKeyword.value.toLowerCase())
  )
})

const currentQuestion = computed(() => {
  return quizData.value[currentQuestionIndex.value]
})

const quizProgress = computed(() => {
  return ((currentQuestionIndex.value + 1) / quizData.value.length) * 100
})

const watchedTime = computed(() => {
  return formatTime(currentTime.value)
})

const updateDate = computed(() => {
  return '2025-01-15'
})

const hasPreviousLesson = computed(() => {
  if (currentChapter.value === 1 && currentLesson.value === 1) return false
  return true
})

const hasNextLesson = computed(() => {
  const chapter = courseData.value.chapters[currentChapter.value - 1]
  if (currentLesson.value < chapter.lessons.length) return true
  if (currentChapter.value < courseData.value.chapters.length) return true
  return false
})

const previousLessonTitle = computed(() => {
  if (!hasPreviousLesson.value) return ''
  if (currentLesson.value > 1) {
    const chapter = courseData.value.chapters[currentChapter.value - 1]
    return `上一节：${chapter.lessons[currentLesson.value - 2].title}`
  }
  if (currentChapter.value > 1) {
    const prevChapter = courseData.value.chapters[currentChapter.value - 2]
    return `上一节：${prevChapter.lessons[prevChapter.lessons.length - 1].title}`
  }
  return ''
})

const nextLessonTitle = computed(() => {
  if (!hasNextLesson.value) return ''
  const chapter = courseData.value.chapters[currentChapter.value - 1]
  if (currentLesson.value < chapter.lessons.length) {
    return `下一节：${chapter.lessons[currentLesson.value].title}`
  }
  if (currentChapter.value < courseData.value.chapters.length) {
    const nextChapter = courseData.value.chapters[currentChapter.value]
    return `下一节：${nextChapter.lessons[0].title}`
  }
  return ''
})

// 方法
const goBack = () => {
  router.push('/courses')
}

const getChapterProgress = (chapter) => {
  const completed = chapter.lessons.filter(lesson => lesson.completed).length
  return `${completed}/${chapter.lessons.length}完成`
}

const toggleChapter = (chapterNum) => {
  const chapter = courseData.value.chapters[chapterNum - 1]
  chapter.expanded = !chapter.expanded
}

const selectLesson = (chapter, lesson) => {
  currentChapter.value = chapter
  currentLesson.value = lesson
  showSuccessMsg(`已切换到第${chapter}章第${lesson}节`)
}

const playVideo = () => {
  isPlaying.value = true
  showControls.value = true
}

const togglePlay = () => {
  isPlaying.value = !isPlaying.value
}

const toggleVideoControls = () => {
  showControls.value = !showControls.value
}

const seekVideo = (event) => {
  // 实现视频拖拽功能
  const rect = event.target.getBoundingClientRect()
  const percent = (event.clientX - rect.left) / rect.width
  videoProgress.value = percent * 100
  currentTime.value = percent * totalTime.value
}

const skipBackward = () => {
  currentTime.value = Math.max(0, currentTime.value - 10)
  videoProgress.value = (currentTime.value / totalTime.value) * 100
}

const skipForward = () => {
  currentTime.value = Math.min(totalTime.value, currentTime.value + 10)
  videoProgress.value = (currentTime.value / totalTime.value) * 100
}

const changeSpeed = () => {
  showSuccessMsg(`播放速度已调整为 ${playbackSpeed.value}x`)
}

const toggleSubtitles = () => {
  showSuccessMsg('字幕功能切换')
}

const toggleFullscreen = () => {
  showSuccessMsg('全屏模式切换')
}

const showTab = (tabKey) => {
  activeTab.value = tabKey
}

const showAddNoteModal = () => {
  newNote.value.timestamp = formatTime(currentTime.value)
  showNoteModal.value = true
}

const hideNoteModal = () => {
  showNoteModal.value = false
  newNote.value.content = ''
}

const saveNote = () => {
  const note = {
    id: Date.now(),
    time: '刚刚',
    location: `第${currentChapter.value}章第${currentLesson.value}节`,
    timestamp: newNote.value.timestamp,
    content: newNote.value.content
  }
  notes.value.unshift(note)
  hideNoteModal()
  showSuccessMsg('笔记保存成功')
}

const editNote = (id) => {
  showSuccessMsg(`编辑笔记 ${id}`)
}

const deleteNote = (id) => {
  notes.value = notes.value.filter(note => note.id !== id)
  showSuccessMsg('笔记删除成功')
}

const shareNote = (id) => {
  showSuccessMsg(`分享笔记 ${id}`)
}

const jumpToTime = (timestamp) => {
  const [minutes, seconds] = timestamp.split(':').map(Number)
  currentTime.value = minutes * 60 + seconds
  videoProgress.value = (currentTime.value / totalTime.value) * 100
  showSuccessMsg(`已跳转到 ${timestamp}`)
}

const selectOption = (index) => {
  selectedAnswer.value = index
}

const previousQuestion = () => {
  if (currentQuestionIndex.value > 0) {
    currentQuestionIndex.value--
  }
}

const nextQuestion = () => {
  if (currentQuestionIndex.value < quizData.value.length - 1) {
    currentQuestionIndex.value++
    selectedAnswer.value = null
  } else {
    showSuccessMsg('测验提交成功')
  }
}

const skipQuestion = () => {
  nextQuestion()
}

const previousLesson = () => {
  if (hasPreviousLesson.value) {
    if (currentLesson.value > 1) {
      currentLesson.value--
    } else if (currentChapter.value > 1) {
      currentChapter.value--
      const chapter = courseData.value.chapters[currentChapter.value - 1]
      currentLesson.value = chapter.lessons.length
    }
    showSuccessMsg('已切换到上一节课程')
  }
}

const nextLesson = () => {
  if (hasNextLesson.value) {
    const chapter = courseData.value.chapters[currentChapter.value - 1]
    if (currentLesson.value < chapter.lessons.length) {
      currentLesson.value++
    } else if (currentChapter.value < courseData.value.chapters.length) {
      currentChapter.value++
      currentLesson.value = 1
    }
    showSuccessMsg('已切换到下一节课程')
  }
}

const completeLesson = () => {
  const chapter = courseData.value.chapters[currentChapter.value - 1]
  const lesson = chapter.lessons[currentLesson.value - 1]
  lesson.completed = true
  showSuccessMsg('课程已标记为完成')
}

const toggleBookmark = () => {
  showSuccessMsg('收藏状态已切换')
}

const showSettings = () => {
  showSuccessMsg('设置面板')
}

const formatTime = (seconds) => {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins}:${secs.toString().padStart(2, '0')}`
}

const showSuccessMsg = (message) => {
  successMessageText.value = message
  showSuccessMessage.value = true
  setTimeout(() => {
    showSuccessMessage.value = false
  }, 3000)
}

// 生命周期
onMounted(() => {
  // 模拟视频播放进度更新
  const interval = setInterval(() => {
    if (isPlaying.value && currentTime.value < totalTime.value) {
      currentTime.value++
      videoProgress.value = (currentTime.value / totalTime.value) * 100
    }
  }, 1000)

  // 测验倒计时
  const quizInterval = setInterval(() => {
    if (remainingTime.value > 0) {
      remainingTime.value--
    }
  }, 1000)

  onUnmounted(() => {
    clearInterval(interval)
    clearInterval(quizInterval)
  })
})
</script>

<style scoped>
.learning-page {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  min-height: 100vh;
  color: #333;
}

/* 系统顶部导航 - 与Layout组件完全一致 */
.system-header {
  background: transparent;
  position: relative;
  z-index: 1000;
  padding: 20px;
}

.system-nav {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 15px;
  padding: 20px 30px;
  margin-bottom: 30px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1400px;
  margin: 0 auto 30px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 15px;
}

.logo-icon {
  font-size: 32px;
}

.logo h1 {
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  font-size: 24px;
  font-weight: 700;
  margin: 0;
}

.nav-menu {
  display: flex;
  gap: 30px;
}

.nav-item {
  padding: 10px 20px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 500;
  color: #333;
}

.nav-item:hover,
.nav-item.active {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  transform: translateY(-2px);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.user-name {
  font-weight: 500;
  color: #333;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: bold;
  font-size: 16px;
}

.logout-btn {
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 8px;
  color: #667eea;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 14px;
}

.logout-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-1px);
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 课程导航 */
.header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 15px;
  padding: 15px 30px;
  margin-bottom: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.course-breadcrumb {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #666;
  font-size: 14px;
}

.course-breadcrumb a {
  color: #667eea;
  text-decoration: none;
  cursor: pointer;
}

.course-breadcrumb a:hover {
  text-decoration: underline;
}

.course-header-actions {
  display: flex;
  align-items: center;
  gap: 15px;
}

.progress-info {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 16px;
  background: rgba(102, 126, 234, 0.1);
  border-radius: 20px;
}

.progress-bar-small {
  width: 100px;
  height: 6px;
  background: #e0e0e0;
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill-small {
  height: 100%;
  background: linear-gradient(90deg, #667eea, #764ba2);
  border-radius: 3px;
  transition: width 0.3s ease;
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  text-decoration: none;
  display: inline-block;
  text-align: center;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
}

.btn-secondary {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
  border: 1px solid rgba(102, 126, 234, 0.2);
}

.btn-secondary:hover {
  background: rgba(102, 126, 234, 0.2);
}

/* 主要学习区域 */
.learning-layout {
  display: grid;
  grid-template-columns: 350px 1fr;
  gap: 20px;
  height: calc(100vh - 120px);
}

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
}

.lesson-list {
  display: none;
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
}

.lesson-icon.completed {
  background: #28a745;
  color: white;
}

.lesson-icon.current {
  background: #667eea;
  color: white;
}

.lesson-duration {
  margin-left: auto;
  font-size: 12px;
  color: #666;
}

/* 主学习内容区 */
.learning-main {
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
  cursor: pointer;
}

.video-placeholder {
  text-align: center;
  padding: 40px;
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
  margin-bottom: 20px;
}

.play-button:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: scale(1.1);
}

.video-controls {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(transparent, rgba(0,0,0,0.8));
  padding: 20px;
  display: none;
}

.video-controls.show {
  display: block;
}

.progress-bar-container {
  margin-bottom: 15px;
}

.video-progress-bar {
  width: 100%;
  height: 6px;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 3px;
  cursor: pointer;
}

.video-progress-fill {
  height: 100%;
  background: #667eea;
  border-radius: 3px;
  transition: width 0.3s ease;
}

.control-buttons {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.control-left, .control-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.control-btn {
  background: none;
  border: none;
  color: white;
  font-size: 18px;
  cursor: pointer;
  padding: 8px;
  border-radius: 4px;
  transition: background 0.3s;
}

.control-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.time-display {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
}

.speed-selector {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  cursor: pointer;
}

/* 学习内容标签页 */
.content-tabs {
  display: flex;
  border-bottom: 1px solid #e0e0e0;
  background: #f8f9fa;
}

.tab-button {
  padding: 15px 25px;
  background: none;
  border: none;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  color: #666;
  transition: all 0.3s ease;
  border-bottom: 3px solid transparent;
}

.tab-button.active {
  color: #667eea;
  border-bottom-color: #667eea;
  background: white;
}

.tab-button:hover {
  color: #667eea;
  background: rgba(102, 126, 234, 0.05);
}

.tab-content {
  flex: 1;
  overflow-y: auto;
}

.tab-panel {
  padding: 30px;
  height: 100%;
  overflow-y: auto;
}

/* 课程介绍内容 */
.course-intro {
  line-height: 1.8;
}

.course-intro h3 {
  color: #667eea;
  margin-bottom: 15px;
  margin-top: 25px;
}

.course-intro h3:first-child {
  margin-top: 0;
}

.course-intro p {
  margin-bottom: 15px;
  color: #555;
}

.course-intro ul {
  margin-left: 20px;
  margin-bottom: 15px;
}

.course-intro li {
  margin-bottom: 8px;
  color: #555;
}

.highlight-box {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1), rgba(118, 75, 162, 0.1));
  border-left: 4px solid #667eea;
  padding: 20px;
  border-radius: 8px;
  margin: 20px 0;
}

/* 笔记功能 */
.notes-section {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.notes-toolbar {
  padding: 20px 30px;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #f8f9fa;
}

.note-search {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  margin-right: 15px;
}

.notes-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px 30px;
}

.note-item {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 10px;
  margin-bottom: 15px;
  border-left: 4px solid #667eea;
  transition: all 0.3s ease;
}

.note-item:hover {
  background: #e9ecef;
  transform: translateY(-2px);
}

.note-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.note-time {
  font-size: 12px;
  color: #666;
}

.note-timestamp {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  cursor: pointer;
}

.note-content {
  color: #555;
  line-height: 1.6;
}

.note-actions {
  margin-top: 10px;
  display: flex;
  gap: 10px;
}

.note-btn {
  padding: 4px 8px;
  background: none;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.note-btn:hover {
  background: #667eea;
  color: white;
  border-color: #667eea;
}

/* 测验功能 */
.quiz-section {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.quiz-header {
  padding: 20px 30px;
  background: #f8f9fa;
  border-bottom: 1px solid #e0e0e0;
}

.quiz-progress {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 15px 0;
}

.quiz-progress-bar {
  flex: 1;
  height: 8px;
  background: #e0e0e0;
  border-radius: 4px;
  margin: 0 15px;
  overflow: hidden;
}

.quiz-progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #667eea, #764ba2);
  transition: width 0.3s ease;
}

.quiz-content {
  flex: 1;
  padding: 30px;
  overflow-y: auto;
}

.question-card {
  background: white;
  border-radius: 15px;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.question-number {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
}

.question-type {
  color: #666;
  font-size: 14px;
}

.question-text {
  font-size: 18px;
  font-weight: 500;
  margin-bottom: 25px;
  line-height: 1.6;
}

.options-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.option-item {
  padding: 15px 20px;
  border: 2px solid #e0e0e0;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 12px;
}

.option-item:hover {
  border-color: #667eea;
  background: rgba(102, 126, 234, 0.05);
}

.option-item.selected {
  border-color: #667eea;
  background: rgba(102, 126, 234, 0.1);
}

.option-letter {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 500;
  color: #666;
}

.option-item.selected .option-letter {
  background: #667eea;
  color: white;
}

.quiz-actions {
  padding: 20px 30px;
  border-top: 1px solid #e0e0e0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #f8f9fa;
}

/* 资源列表 */
.resources-section h3 {
  margin-bottom: 20px;
}

.resources-list {
  display: grid;
  gap: 15px;
}

.resource-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 10px;
  border-left: 4px solid #667eea;
}

.resource-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  background: #e3f2fd;
}

.resource-info {
  flex: 1;
}

.resource-title {
  font-weight: 500;
  margin-bottom: 5px;
}

.resource-meta {
  color: #666;
  font-size: 14px;
}

/* 导航按钮 */
.lesson-navigation {
  padding: 20px 30px;
  border-top: 1px solid #e0e0e0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #f8f9fa;
}

.nav-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.nav-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.lesson-complete-btn {
  background: linear-gradient(135deg, #28a745, #20c997);
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 8px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.lesson-complete-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(40, 167, 69, 0.4);
}

/* 浮动按钮 */
.add-note-btn {
  position: fixed;
  bottom: 30px;
  right: 30px;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border: none;
  font-size: 24px;
  cursor: pointer;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.4);
  transition: all 0.3s ease;
  z-index: 100;
}

.add-note-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 25px rgba(102, 126, 234, 0.5);
}

/* 弹窗样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: none;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-overlay.show {
  display: flex;
}

.modal {
  background: white;
  border-radius: 15px;
  padding: 30px;
  max-width: 500px;
  width: 90%;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.modal-title {
  font-size: 20px;
  font-weight: 600;
}

.modal-close {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #666;
}

.note-form {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.form-row {
  display: flex;
  gap: 15px;
}

.form-group {
  flex: 1;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: 500;
}

.form-group input,
.form-group select {
  width: 100%;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.note-textarea {
  min-height: 120px;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  resize: vertical;
  font-family: inherit;
  width: 100%;
}

.form-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  margin-top: 20px;
}

/* 成功提示 */
.success-message {
  position: fixed;
  top: 20px;
  right: 20px;
  background: #28a745;
  color: white;
  padding: 15px 20px;
  border-radius: 8px;
  font-weight: 500;
  z-index: 1001;
  transform: translateX(100%);
  transition: transform 0.3s ease;
}

.success-message.show {
  transform: translateX(0);
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .learning-layout {
    grid-template-columns: 300px 1fr;
  }
}

@media (max-width: 768px) {
  .system-header {
    padding: 10px;
  }

  .system-nav {
    flex-direction: column;
    gap: 20px;
    padding: 20px;
  }

  .nav-menu {
    flex-wrap: wrap;
    gap: 15px;
    justify-content: center;
  }

  .logo h1 {
    font-size: 20px;
  }

  .user-name {
    display: none;
  }

  .learning-layout {
    grid-template-columns: 1fr;
    grid-template-rows: auto 1fr;
    height: auto;
  }

  .course-sidebar {
    height: 300px;
  }

  .video-container {
    min-height: 250px;
  }

  .lesson-header {
    padding: 15px 20px;
  }

  .lesson-title {
    font-size: 20px;
  }

  .tab-panel {
    padding: 20px;
  }

  .add-note-btn {
    bottom: 20px;
    right: 20px;
    width: 48px;
    height: 48px;
    font-size: 20px;
  }
}

@media (max-width: 480px) {
  .nav-menu {
    gap: 10px;
  }

  .nav-item {
    padding: 8px 15px;
    font-size: 14px;
  }
}
</style>
