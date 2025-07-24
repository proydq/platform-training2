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
      type: 'single',
      question: '产品生命周期包含几个阶段？',
      options: [
        { text: '3个', correct: false },
        { text: '4个', correct: true },
        { text: '5个', correct: false },
        { text: '6个', correct: false }
      ],
      score: 10,
      hint: '产品生命周期通常分为引入期、成长期、成熟期和衰退期',
      explanation: '产品生命周期包含四个主要阶段：引入期、成长期、成熟期和衰退期。每个阶段都有不同的特点和营销策略。'
    },
    {
      id: 2,
      type: 'single',
      question: '在产品生命周期的哪个阶段，通常会出现价格战和激烈竞争？',
      options: [
        { text: '引入期', correct: false },
        { text: '成长期', correct: false },
        { text: '成熟期', correct: true },
        { text: '衰退期', correct: false }
      ],
      score: 10,
      hint: '想想哪个阶段市场已经饱和，竞争者众多',
      explanation: '成熟期的特点是市场饱和、竞争激烈，企业为了维持市场份额经常采用价格竞争策略。'
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
    if (chapter) {
      chapter.expanded = !chapter.expanded
    }
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
      selectedAnswer.value = null
    }
  }

  const nextQuestion = () => {
    if (currentQuestionIndex.value < quizData.value.length - 1) {
      currentQuestionIndex.value++
      selectedAnswer.value = null
    } else {
      showSuccessMsg('测验提交成功！')
    }
  }

  const skipQuestion = () => {
    nextQuestion()
  }

  const previousLesson = () => {
    // 实现上一课逻辑
    if (currentLesson.value > 1) {
      currentLesson.value--
    } else if (currentChapter.value > 1) {
      currentChapter.value--
      const prevChapter = courseData.value.chapters[currentChapter.value - 1]
      currentLesson.value = prevChapter.lessons.length
    }
    showSuccessMsg('已切换到上一节课程')
  }

  const nextLesson = () => {
    // 实现下一课逻辑
    const currentChapterData = courseData.value.chapters[currentChapter.value - 1]
    if (currentLesson.value < currentChapterData.lessons.length) {
      currentLesson.value++
    } else if (currentChapter.value < courseData.value.chapters.length) {
      currentChapter.value++
      currentLesson.value = 1
    }
    showSuccessMsg('已切换到下一节课程')
  }

  const completeLesson = () => {
    const chapter = courseData.value.chapters[currentChapter.value - 1]
    const lesson = chapter.lessons[currentLesson.value - 1]
    lesson.completed = true
    showSuccessMsg('🎉 课程已标记为完成！')
  }

  const goBack = () => {
    router.push('/courses')
  }

  const toggleBookmark = () => {
    showSuccessMsg('收藏状态已切换')
  }

  const showSettings = () => {
    showSuccessMsg('打开播放设置')
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
