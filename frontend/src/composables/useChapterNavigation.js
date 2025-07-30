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
        return true
      }
    }
    return false
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
    initializeFirstLesson
  }
}