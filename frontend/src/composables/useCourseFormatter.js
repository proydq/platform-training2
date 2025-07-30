import { useMediaUrl } from './useMediaUrl'

export function useCourseFormatter() {
  const { isVideoFile, isDocumentFile, isAudioFile } = useMediaUrl()

  const formatCourse = (courseData) => {
    console.log('🔄 开始格式化课程数据，原始数据:', courseData)

    const formatted = {
      id: courseData.id || 1,
      title: courseData.title || '课程标题',
      category: courseData.category || '未分类',
      totalDuration: courseData.totalDuration || courseData.estimatedDuration || '0分钟',
      level: courseData.level || courseData.difficultyLevel || '初级',
      chapters: (courseData.chapters || []).map((ch, chapterIndex) => {
        console.log(`🔄 格式化章节 ${chapterIndex + 1}:`, ch)

        const possibleUrl = ch.contentUrl || ch.videoUrl || ch.mediaUrl || ch.content || ch.documentUrl
        console.log(`🎥 章节${chapterIndex + 1}的资源URL候选:`, {
          contentUrl: ch.contentUrl,
          videoUrl: ch.videoUrl,
          mediaUrl: ch.mediaUrl,
          content: ch.content,
          documentUrl: ch.documentUrl,
          最终选择: possibleUrl
        })

        let resourceType = 'unknown'
        if (possibleUrl) {
          if (isVideoFile(possibleUrl)) {
            resourceType = 'video'
          } else if (isDocumentFile(possibleUrl)) {
            resourceType = 'document'
          } else if (isAudioFile(possibleUrl)) {
            resourceType = 'audio'
          }
        }

        const formattedChapter = {
          id: ch.id || chapterIndex + 1,
          title: ch.title || `第${chapterIndex + 1}章`,
          lessons: [
            {
              id: ch.id || chapterIndex + 1,
              title: ch.title || `第${chapterIndex + 1}节`,
              type: resourceType,
              duration: ch.duration ? `${ch.duration}分钟` : '未知',
              completed: ch.status === 1,
              updateDate: ch.updateDate || '未知',
              videoUrl: possibleUrl,
              contentUrl: possibleUrl,
              content: possibleUrl,
              audioUrl: possibleUrl,
              documentUrl: possibleUrl,
              description: ch.description,
              _originalData: ch
            },
          ],
        }

        console.log(`✅ 章节${chapterIndex + 1}格式化完成:`, formattedChapter)
        return formattedChapter
      }),
    }

    console.log('✅ 课程格式化完成，最终数据:', formatted)
    return formatted
  }

  const getLessonIcon = (lesson) => {
    if (lesson.type === 'video') return '🎥'
    if (lesson.type === 'document') return '📄'
    if (lesson.type === 'audio') return '🎵'
    return '📚'
  }

  const getLessonTypeText = (lesson) => {
    if (lesson.type === 'video') return '视频课程'
    if (lesson.type === 'document') return '文档资料'
    if (lesson.type === 'audio') return '音频课程'
    return '学习资料'
  }

  return {
    formatCourse,
    getLessonIcon,
    getLessonTypeText
  }
}