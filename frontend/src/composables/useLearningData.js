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
