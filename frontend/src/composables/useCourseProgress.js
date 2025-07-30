import { computed } from 'vue'

export function useCourseProgress(courseData) {
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

  return {
    courseProgress,
    completedLessons,
    totalLessons
  }
}