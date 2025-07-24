// composables/useLearningData.js
import { ref, computed } from 'vue'

export function useLearningData(courseId) {
  const currentChapter = ref(1)
  const currentLesson = ref(2)
  const activeTab = ref('intro')
  const courseProgress = ref(35)

  // 课程数据
  const courseData = ref({
    id: courseId || 'product-basic',
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

  // 计算属性
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

  const watchedTime = computed(() => {
    return '4分钟'
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

  return {
    courseData,
    currentChapter,
    currentLesson,
    activeTab,
    courseProgress,
    currentChapterTitle,
    currentLessonData,
    watchedTime,
    updateDate,
    hasPreviousLesson,
    hasNextLesson,
    previousLessonTitle,
    nextLessonTitle
  }
}

// composables/useVideoPlayer.js
import { ref } from 'vue'

export function useVideoPlayer() {
  const isPlaying = ref(false)
  const showControls = ref(false)
  const videoProgress = ref(35)
  const currentTime = ref(263) // 4:23 = 263秒
  const totalTime = ref(720) // 12:00 = 720秒
  const playbackSpeed = ref('1')

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

  const changeSpeed = (speed) => {
    playbackSpeed.value = speed
  }

  const toggleSubtitles = () => {
    console.log('字幕功能切换')
  }

  const toggleFullscreen = () => {
    console.log('全屏模式切换')
  }

  const formatTime = (seconds) => {
    const mins = Math.floor(seconds / 60)
    const secs = seconds % 60
    return `${mins}:${secs.toString().padStart(2, '0')}`
  }

  return {
    isPlaying,
    showControls,
    videoProgress,
    currentTime,
    totalTime,
    playbackSpeed,
    playVideo,
    togglePlay,
    toggleVideoControls,
    seekVideo,
    skipBackward,
    skipForward,
    changeSpeed,
    toggleSubtitles,
    toggleFullscreen,
    formatTime
  }
}

// composables/useNotes.js
import { ref, computed } from 'vue'

export function useNotes(currentTime) {
  const noteSearchKeyword = ref('')
  const showNoteModal = ref(false)
  const newNote = ref({
    timestamp: '04:23',
    tag: '重要概念',
    content: ''
  })

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

  const filteredNotes = computed(() => {
    if (!noteSearchKeyword.value) return notes.value
    return notes.value.filter(note =>
      note.content.toLowerCase().includes(noteSearchKeyword.value.toLowerCase())
    )
  })

  const showAddNoteModal = () => {
    const formatTime = (seconds) => {
      const mins = Math.floor(seconds / 60)
      const secs = seconds % 60
      return `${mins}:${secs.toString().padStart(2, '0')}`
    }
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
      location: '第1章第2节',
      timestamp: newNote.value.timestamp,
      content: newNote.value.content
    }
    notes.value.unshift(note)
    hideNoteModal()
  }

  const editNote = (id) => {
    console.log(`编辑笔记 ${id}`)
  }

  const deleteNote = (id) => {
    notes.value = notes.value.filter(note => note.id !== id)
  }

  const shareNote = (id) => {
    console.log(`分享笔记 ${id}`)
  }

  const jumpToTime = (timestamp) => {
    const [minutes, seconds] = timestamp.split(':').map(Number)
    currentTime.value = minutes * 60 + seconds
    console.log(`已跳转到 ${timestamp}`)
  }

  return {
    notes,
    noteSearchKeyword,
    showNoteModal,
    newNote,
    filteredNotes,
    showAddNoteModal,
    hideNoteModal,
    saveNote,
    editNote,
    deleteNote,
    shareNote,
    jumpToTime
  }
}

// composables/useLearningActions.js
import { ref } from 'vue'

export function useLearningActions(router, courseData, currentChapter, currentLesson, activeTab) {
  const showSuccessMessage = ref(false)
  const successMessageText = ref('')

  // 测验相关
  const currentQuestionIndex = ref(1)
  const selectedAnswer = ref(2)
  const remainingTime = ref(512)

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

  const showSuccessMsg = (message) => {
    successMessageText.value = message
    showSuccessMessage.value = true
    setTimeout(() => {
      showSuccessMessage.value = false
    }, 3000)
  }

  const selectLesson = (chapter, lesson) => {
    currentChapter.value = chapter
    currentLesson.value = lesson
    showSuccessMsg(`已切换到第${chapter}章第${lesson}节`)
  }

  const toggleChapter = (chapterNum) => {
    const chapter = courseData.value.chapters[chapterNum - 1]
    chapter.expanded = !chapter.expanded
  }

  const showTab = (tabKey) => {
    activeTab.value = tabKey
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
    showSuccessMsg('已切换到上一节课程')
  }

  const nextLesson = () => {
    showSuccessMsg('已切换到下一节课程')
  }

  const completeLesson = () => {
    const chapter = courseData.value.chapters[currentChapter.value - 1]
    const lesson = chapter.lessons[currentLesson.value - 1]
    lesson.completed = true
    showSuccessMsg('课程已标记为完成')
  }

  const goBack = () => {
    router.push('/courses')
  }

  const toggleBookmark = () => {
    showSuccessMsg('收藏状态已切换')
  }

  const showSettings = () => {
    showSuccessMsg('设置面板')
  }

  return {
    showSuccessMessage,
    successMessageText,
    quizData,
    currentQuestionIndex,
    selectedAnswer,
    remainingTime,
    selectLesson,
    toggleChapter,
    showTab,
    selectOption,
    previousQuestion,
    nextQuestion,
    skipQuestion,
    previousLesson,
    nextLesson,
    completeLesson,
    goBack,
    toggleBookmark,
    showSettings,
    showSuccessMsg
  }
}
