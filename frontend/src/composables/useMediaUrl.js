
export function useMediaUrl() {
  const API_BASE = ''  // 使用相对路径，通过vite代理

  const isVideoFile = (url) => /\.(mp4|mov|webm|avi|mkv)$/i.test(url || '')
  const isAudioFile = (url) => /\.(mp3|wav|ogg|m4a)$/i.test(url || '')
  const isDocumentFile = (url) => /\.(pdf|docx?|xlsx?|pptx?|txt|md|csv|json|xml)$/i.test(url || '')

  const resolveMediaUrl = (url) => {
    console.log('🔗 原始URL:', url)

    if (!url) {
      console.warn('⚠️ URL为空')
      return ''
    }

    if (url.startsWith('blob:')) {
      console.log('✅ Blob URL，直接返回')
      return url
    }

    const path = url.replace(/^https?:\/\/[^/]+/, '')
    console.log('🔗 处理后的路径:', path)

    let finalUrl = ''

    if (path.startsWith('/api/files/course/videos/')) {
      finalUrl = `${API_BASE}${path.replace('/api/files/course/videos/', '/api/media/video/')}`
    } else if (path.startsWith('/api/files/course/video/')) {
      finalUrl = `${API_BASE}${path.replace('/api/files/course/video/', '/api/media/video/')}`
    } else if (path.startsWith('/api/files/course/documents/')) {
      finalUrl = `${API_BASE}${path.replace('/api/files/course/documents/', '/api/media/document/')}`
    } else if (path.startsWith('/api/media/video/')) {
      finalUrl = `${API_BASE}${path}`
    } else if (path.startsWith('/api/media/document/')) {
      finalUrl = `${API_BASE}${path}`
    } else if (path.startsWith('/')) {
      finalUrl = `${API_BASE}${path}`
    } else {
      const ext = path.split('.').pop().toLowerCase()
      if (isVideoFile(`.${ext}`)) {
        finalUrl = `${API_BASE}/api/media/video/${path}`
      } else if (isDocumentFile(`.${ext}`)) {
        finalUrl = `${API_BASE}/api/media/document/${path}`
      } else {
        finalUrl = `${API_BASE}/api/media/video/${path}`
      }
    }

    console.log('🔗 最终URL:', finalUrl)
    return finalUrl
  }

  const getMediaUrl = (lessonData) => {
    if (!lessonData) {
      console.warn('⚠️ lessonData 为空')
      return ''
    }

    console.log('🔍 尝试获取资源URL，课程数据:', lessonData)

    const possibleUrls = [
      lessonData.videoUrl,
      lessonData.contentUrl,
      lessonData.content,
      lessonData.audioUrl,
      lessonData.documentUrl
    ]

    console.log('🔍 可能的URL列表:', possibleUrls)

    for (const url of possibleUrls) {
      if (url && typeof url === 'string' && url.trim()) {
        console.log('✅ 找到有效URL:', url)
        return resolveMediaUrl(url.trim())
      }
    }

    console.warn('⚠️ 没有找到有效的资源URL')
    return ''
  }

  const getMediaType = (url) => {
    if (isVideoFile(url)) return 'video'
    if (isAudioFile(url)) return 'audio'
    if (isDocumentFile(url)) return 'document'
    return 'unknown'
  }

  return {
    isVideoFile,
    isAudioFile,
    isDocumentFile,
    resolveMediaUrl,
    getMediaUrl,
    getMediaType
  }
}