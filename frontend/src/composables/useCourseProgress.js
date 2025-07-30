import { computed } from 'vue'

export function useCourseProgress(courseData, navigation = null) {
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

  // 当前章节进度
  const currentChapterProgress = computed(() => {
    if (!navigation || !navigation.currentChapter) return 0
    
    const currentChapter = courseData.value.chapters.find(
      chapter => chapter.id === navigation.currentChapter.value
    )
    
    if (!currentChapter || !currentChapter.lessons.length) return 0
    
    const completedInChapter = currentChapter.lessons.filter(lesson => lesson.completed).length
    const totalInChapter = currentChapter.lessons.length
    
    return Math.round((completedInChapter / totalInChapter) * 100)
  })

  // 当前章节已完成课程数
  const currentChapterCompletedLessons = computed(() => {
    if (!navigation || !navigation.currentChapter) return 0
    
    const currentChapter = courseData.value.chapters.find(
      chapter => chapter.id === navigation.currentChapter.value
    )
    
    if (!currentChapter) return 0
    
    return currentChapter.lessons.filter(lesson => lesson.completed).length
  })

  // 当前章节总课程数
  const currentChapterTotalLessons = computed(() => {
    if (!navigation || !navigation.currentChapter) return 0
    
    const currentChapter = courseData.value.chapters.find(
      chapter => chapter.id === navigation.currentChapter.value
    )
    
    return currentChapter ? currentChapter.lessons.length : 0
  })

  const syncProgressFromRecord = (studyRecord) => {
    if (!studyRecord || !studyRecord.progressPercent) return

    // 基于进度百分比更新本地章节完成状态
    const targetProgress = studyRecord.progressPercent
    const total = totalLessons.value
    const shouldBeCompleted = Math.floor((targetProgress / 100) * total)

    let completed = 0
    outerLoop: for (const chapter of courseData.value.chapters) {
      for (const lesson of chapter.lessons) {
        if (completed < shouldBeCompleted) {
          lesson.completed = true
          completed++
        } else {
          break outerLoop
        }
      }
    }

    console.log(`📊 根据后端记录同步进度: ${targetProgress}% (${completed}/${total})`)
  }

  return {
    courseProgress,
    completedLessons,
    totalLessons,
    currentChapterProgress,
    currentChapterCompletedLessons,
    currentChapterTotalLessons,
    syncProgressFromRecord
  }
}