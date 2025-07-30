import { ref, computed } from 'vue'

export function useChapterNavigation(courseData) {
  const currentChapter = ref(1)
  const currentLesson = ref(null)
  const expandedChapters = ref([])

  const currentLessonData = computed(() => {
    const chapter = courseData.value.chapters.find((c) => c.id === currentChapter.value)
    if (!chapter) return {}
    const lesson = chapter.lessons.find((l) => l.id === currentLesson.value)
    return lesson || {}
  })

  const hasPreviousLesson = computed(() => {
    if (!courseData.value.chapters.length) return false
    
    const firstChapter = courseData.value.chapters[0]
    if (!firstChapter || !firstChapter.lessons.length) return false
    
    // 如果当前是第一章的第一课，则没有上一课
    return !(currentChapter.value === firstChapter.id && currentLesson.value === firstChapter.lessons[0].id)
  })

  const hasNextLesson = computed(() => {
    if (!courseData.value.chapters.length) return false
    
    const lastChapter = courseData.value.chapters[courseData.value.chapters.length - 1]
    if (!lastChapter || !lastChapter.lessons.length) return false
    
    const lastLesson = lastChapter.lessons[lastChapter.lessons.length - 1]
    // 如果当前是最后章的最后课，则没有下一课
    return !(currentChapter.value === lastChapter.id && currentLesson.value === lastLesson.id)
  })

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
  }

  const previousLesson = () => {
    const currentChapterData = courseData.value.chapters.find((c) => c.id === currentChapter.value)
    if (!currentChapterData) {
      console.error('❌ 找不到当前章节数据:', currentChapter.value)
      return
    }

    const currentLessonIndex = currentChapterData.lessons.findIndex(
      (l) => l.id === currentLesson.value,
    )

    if (currentLessonIndex > 0) {
      // 同一章节的上一课
      const previousLessonData = currentChapterData.lessons[currentLessonIndex - 1]
      console.log('⬅️ 跳转到同章节上一课:', previousLessonData.title)
      selectLesson(currentChapter.value, previousLessonData.id)
    } else {
      // 找到上一个章节
      const currentChapterIndex = courseData.value.chapters.findIndex((c) => c.id === currentChapter.value)
      if (currentChapterIndex > 0) {
        const previousChapter = courseData.value.chapters[currentChapterIndex - 1]
        if (previousChapter && previousChapter.lessons.length > 0) {
          const lastLesson = previousChapter.lessons[previousChapter.lessons.length - 1]
          console.log('⬅️ 跳转到上一章节最后一课:', lastLesson.title)
          selectLesson(previousChapter.id, lastLesson.id)
        }
      } else {
        console.log('ℹ️ 已经是第一课了')
      }
    }
  }

  const nextLesson = () => {
    const currentChapterData = courseData.value.chapters.find((c) => c.id === currentChapter.value)
    if (!currentChapterData) {
      console.error('❌ 找不到当前章节数据:', currentChapter.value)
      return
    }

    const currentLessonIndex = currentChapterData.lessons.findIndex(
      (l) => l.id === currentLesson.value,
    )
    
    console.log('📍 当前课程位置:', { 
      chapterId: currentChapter.value, 
      lessonId: currentLesson.value, 
      lessonIndex: currentLessonIndex,
      totalLessons: currentChapterData.lessons.length
    })

    if (currentLessonIndex < currentChapterData.lessons.length - 1) {
      // 同一章节的下一课
      const nextLessonData = currentChapterData.lessons[currentLessonIndex + 1]
      console.log('➡️ 跳转到同章节下一课:', nextLessonData.title)
      selectLesson(currentChapter.value, nextLessonData.id)
    } else {
      // 找到下一个章节
      const currentChapterIndex = courseData.value.chapters.findIndex((c) => c.id === currentChapter.value)
      if (currentChapterIndex < courseData.value.chapters.length - 1) {
        const nextChapter = courseData.value.chapters[currentChapterIndex + 1]
        if (nextChapter && nextChapter.lessons.length > 0) {
          console.log('➡️ 跳转到下一章节:', nextChapter.title)
          selectLesson(nextChapter.id, nextChapter.lessons[0].id)
        }
      } else {
        console.log('ℹ️ 已经是最后一课了')
      }
    }
  }

  const markComplete = () => {
    const chapter = courseData.value.chapters.find((c) => c.id === currentChapter.value)
    if (chapter) {
      const lesson = chapter.lessons.find((l) => l.id === currentLesson.value)
      if (lesson) {
        lesson.completed = true
        return true
      }
    }
    return false
  }

  const markAllComplete = () => {
    courseData.value.chapters.forEach(chapter => {
      chapter.lessons.forEach(lesson => {
        lesson.completed = true
      })
    })
  }

  const initializeFirstLesson = () => {
    if (courseData.value.chapters.length > 0) {
      currentChapter.value = courseData.value.chapters[0].id
      currentLesson.value = courseData.value.chapters[0].lessons[0].id
      expandedChapters.value.push(courseData.value.chapters[0].id)
    }
  }

  return {
    currentChapter,
    currentLesson,
    expandedChapters,
    currentLessonData,
    hasPreviousLesson,
    hasNextLesson,
    toggleChapter,
    selectLesson,
    previousLesson,
    nextLesson,
    markComplete,
    markAllComplete,
    initializeFirstLesson
  }
}