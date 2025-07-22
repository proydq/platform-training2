// frontend/src/api/course.js - 完整的课程管理API接口
import request from '@/utils/request'

/**
 * ==================== 课程管理API ====================
 */

// 获取课程列表（分页）
export function getCourseListAPI(params = {}) {
  return request({
    url: '/api/v1/courses',
    method: 'GET',
    params: {
      page: params.page || 0,
      size: params.size || 20,
      keyword: params.keyword || '',
      category: params.category || '',
      difficultyLevel: params.difficultyLevel || '',
      status: params.status || '',
      isRequired: params.isRequired || '',
      instructorId: params.instructorId || '',
      sortBy: params.sortBy || 'createTime',
      sortDir: params.sortDir || 'desc',
    },
  })
}

// 获取课程详情
export function getCourseDetailAPI(courseId) {
  return request({
    url: `/api/v1/courses/${courseId}`,
    method: 'GET',
  })
}

// 创建课程
// 🔧 完整修复后的创建课程API
export function createCourseAPI(data) {
  // 数据验证
  if (!data.title?.trim()) {
    throw new Error('课程标题不能为空')
  }
  if (!data.category?.trim()) {
    throw new Error('课程分类不能为空')
  }
  if (!data.description?.trim()) {
    throw new Error('课程描述不能为空')
  }

  const requestData = {
    title: data.title.trim(),
    description: data.description.trim(),
    category: data.category.trim(),

    // 🔧 关键修复1：移除 Math.min 限制，支持完整的1-5难度级别
    difficultyLevel: getDifficultyLevel(data.level),

    estimatedDuration: Number(data.duration) || 0,

    // 🔧 关键修复2：移除前端设置 instructorId，由后端自动设置
    // instructorId: data.instructorId, // 删除这行，后端会自动从token设置

    price: Number(data.price) || 0,
    isRequired: Boolean(data.isRequired),
    coverImageUrl: data.coverImage || '',

    // 🔧 材料信息处理
    materials: data.materials && data.materials.length > 0
      ? data.materials.map((material) => ({
        url: typeof material === 'string' ? material : material.url,
        name: typeof material === 'object'
          ? material.originalName || material.name || '学习资料'
          : '学习资料',
      }))
      : [],

    // 兼容旧格式
    materialUrls: data.materials
      ? data.materials.map((m) => (typeof m === 'string' ? m : m.url)).join(',')
      : '',

    // 🔧 视频信息处理
    videos: data.videos && data.videos.length > 0
      ? data.videos.map((video) => ({
        url: typeof video === 'string' ? video : video.url,
        name: typeof video === 'object'
          ? video.originalName || video.name || '视频资料'
          : '视频资料',
      }))
      : [],

    videoUrls: data.videos
      ? data.videos.map((v) => (typeof v === 'string' ? v : v.url)).join(',')
      : '',

    // 🔧 关键修复3：章节数据映射
    chapters: data.chapters && data.chapters.length > 0
      ? data.chapters.map((chapter, index) => {
        // 验证必填字段
        if (!chapter.title?.trim()) {
          throw new Error(`第 ${index + 1} 个章节的标题不能为空`)
        }

        return {
          title: chapter.title.trim(),
          description: chapter.description || '',

          // 🔧 关键修复4：添加必填的 chapterType 字段
          chapterType: chapter.chapterType || chapter.type || 'document',

          // 🔧 关键修复5：字段名映射 order -> sortOrder
          sortOrder: Number(chapter.order || chapter.sortOrder) || (index + 1),

          duration: Number(chapter.duration) || 0,
          content: chapter.content || '',
          videoUrl: chapter.videoUrl || '',
          contentUrl: chapter.contentUrl || chapter.videoUrl || '',

          // 可选字段
          isFree: Boolean(chapter.isFree),
          requirements: chapter.requirements || '',
          learningObjectives: chapter.learningObjectives || '',
          fileSize: chapter.fileSize || null,
          fileFormat: chapter.fileFormat || null,
          thumbnailUrl: chapter.thumbnailUrl || '',
          materialUrls: chapter.materialUrls || '',
          videoUrls: chapter.videoUrls || ''
        }
      })
      : [],
  }

  // 🔧 详细验证日志
  console.log('📤 最终提交数据:', requestData)
  console.log('🔍 关键字段验证:', {
    title: requestData.title,
    category: requestData.category,
    description: requestData.description,
    difficultyLevel: requestData.difficultyLevel,
    chaptersCount: requestData.chapters.length
  })

  // 验证章节数据
  if (requestData.chapters.length > 0) {
    console.log('📚 章节数据验证:')
    requestData.chapters.forEach((chapter, index) => {
      console.log(`章节 ${index + 1}:`, {
        title: chapter.title,
        chapterType: chapter.chapterType,
        sortOrder: chapter.sortOrder,
        isValid: !!(chapter.title && chapter.chapterType && chapter.sortOrder)
      })
    })
  }

  return request({
    url: '/api/v1/courses',
    method: 'POST',
    data: requestData,
  })
}

// 更新课程
export function updateCourseAPI(courseId, data) {
  const requestData = {
    title: data.title,
    description: data.description,
    category: data.category,

    // 🔧 修复：移除 Math.min 限制
    difficultyLevel: getDifficultyLevel(data.level),

    estimatedDuration: data.duration,
    price: data.price || 0,
    isRequired: data.isRequired || false,
    coverImageUrl: data.coverImage || '',

    // 材料信息处理（保持原逻辑）
    materials: data.materials && data.materials.length > 0
      ? data.materials.map((material) => ({
        url: typeof material === 'string' ? material : material.url,
        name: typeof material === 'object'
          ? material.originalName || material.name || '学习资料'
          : '学习资料',
      }))
      : [],

    materialUrls: data.materials
      ? data.materials.map((m) => (typeof m === 'string' ? m : m.url)).join(',')
      : '',

    videos: data.videos && data.videos.length > 0
      ? data.videos.map((video) => ({
        url: typeof video === 'string' ? video : video.url,
        name: typeof video === 'object'
          ? video.originalName || video.name || '视频资料'
          : '视频资料',
      }))
      : [],

    videoUrls: data.videos
      ? data.videos.map((v) => (typeof v === 'string' ? v : v.url)).join(',')
      : '',

    chapters: data.chapters
      ? data.chapters.map((chapter) => ({
        id: chapter.id,
        title: chapter.title,
        description: chapter.description || '',
        chapterType: chapter.chapterType || 'document', // 🔧 添加
        duration: chapter.duration || 0,
        order: chapter.order,
        content: chapter.content || '',
        videoUrl: chapter.videoUrl || '',
      }))
      : [],
  }

  return request({
    url: `/api/v1/courses/${courseId}`,
    method: 'PUT',
    data: requestData,
  })
}

// 删除课程
export function deleteCourseAPI(courseId) {
  return request({
    url: `/api/v1/courses/${courseId}`,
    method: 'DELETE',
  })
}

// 发布课程
export function publishCourseAPI(courseId) {
  return request({
    url: `/api/v1/courses/${courseId}/publish`,
    method: 'POST',
  })
}

// 下架课程
export function unpublishCourseAPI(courseId) {
  return request({
    url: `/api/v1/courses/${courseId}/unpublish`,
    method: 'POST',
  })
}

/**
 * ==================== 课程章节API ====================
 */

// 获取课程章节列表
export function getCourseChaptersAPI(courseId, status = null) {
  return request({
    url: `/api/v1/courses/${courseId}/chapters`,
    method: 'GET',
    params: status ? { status } : {},
  })
}

// 获取章节详情
export function getChapterDetailAPI(courseId, chapterId) {
  return request({
    url: `/api/v1/courses/${courseId}/chapters/${chapterId}`,
    method: 'GET',
  })
}

// 创建章节
export function createChapterAPI(courseId, data) {
  return request({
    url: `/api/v1/courses/${courseId}/chapters`,
    method: 'POST',
    data: {
      title: data.title,
      description: data.description || '',
      duration: data.duration,
      sortOrder: data.order,
      content: data.content || '',
      videoUrl: data.videoUrl || '',
      materialUrls: data.materialUrls || [],
    },
  })
}

// 更新章节
export function updateChapterAPI(courseId, chapterId, data) {
  return request({
    url: `/api/v1/courses/${courseId}/chapters/${chapterId}`,
    method: 'PUT',
    data: {
      title: data.title,
      description: data.description || '',
      duration: data.duration,
      sortOrder: data.order,
      content: data.content || '',
      videoUrl: data.videoUrl || '',
      materialUrls: data.materialUrls || [],
    },
  })
}

// 删除章节
export function deleteChapterAPI(courseId, chapterId) {
  return request({
    url: `/api/v1/courses/${courseId}/chapters/${chapterId}`,
    method: 'DELETE',
  })
}

// 调整章节顺序
export function reorderChaptersAPI(courseId, chapterOrders) {
  return request({
    url: `/api/v1/courses/${courseId}/chapters/reorder`,
    method: 'POST',
    data: {
      chapterOrders: chapterOrders.map((item) => ({
        chapterId: item.id,
        sortOrder: item.order,
      })),
    },
  })
}

/**
 * ==================== 文件上传API ====================
 */

// 上传课程封面
export function uploadCourseCoverAPI(file) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('category', 'course')
  formData.append('type', 'cover')

  return request({
    url: '/api/v1/upload',
    method: 'POST',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

// 上传课程资料
export function uploadCourseMaterialAPI(file) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('category', 'course')
  formData.append('type', 'material')

  return request({
    url: '/api/v1/upload',
    method: 'POST',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

// 上传课程视频
export function uploadCourseVideoAPI(file, onProgress) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('category', 'course')
  formData.append('type', 'video')

  return request({
    url: '/api/v1/upload',
    method: 'POST',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data',
    },
    onUploadProgress: onProgress,
  })
}

// 通用文件上传
export function uploadFileAPI(file, category = 'temp', userId) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('category', category)
  formData.append('userId', userId)

  return request({
    url: '/api/v1/upload',
    method: 'POST',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

/**
 * ==================== 搜索和筛选API ====================
 */

// 搜索课程
export function searchCoursesAPI(params) {
  return request({
    url: '/api/v1/courses/search',
    method: 'GET',
    params,
  })
}

// 获取推荐课程
export function getRecommendedCoursesAPI(page = 0, size = 10) {
  return request({
    url: '/api/v1/courses/recommended',
    method: 'GET',
    params: { page, size },
  })
}

// 获取热门课程
export function getPopularCoursesAPI(page = 0, size = 10) {
  return request({
    url: '/api/v1/courses/popular',
    method: 'GET',
    params: { page, size },
  })
}

// 获取我的课程（讲师）
export function getMyCoursesAPI(page = 0, size = 20) {
  return request({
    url: '/api/v1/courses/my',
    method: 'GET',
    params: { page, size },
  })
}

// 获取管理员课程列表
export function getAdminCoursesAPI(params = {}) {
  return request({
    url: '/api/v1/courses/admin',
    method: 'GET',
    params,
  })
}

/**
 * ==================== 工具函数 ====================
 */

// 转换难度级别
function getDifficultyLevel(level) {
  const levelMap = {
    入门级: 1,
    初级: 2,
    中级: 3,
    高级: 4,
    专家级: 5,
  }
  return levelMap[level] || 1
}

// 转换难度级别（反向）
export function getDifficultyLevelText(level) {
  const levelMap = {
    1: '入门级',
    2: '初级',
    3: '中级',
    4: '高级',
    5: '专家级',
  }
  return levelMap[level] || '入门级'
}

// 转换课程状态
export function getCourseStatusText(status) {
  const statusMap = {
    0: '草稿',
    1: '已发布',
    2: '已下架',
  }
  return statusMap[status] || '未知'
}

// 转换章节状态
export function getChapterStatusText(status) {
  const statusMap = {
    0: '草稿',
    1: '已发布',
    2: '已下架',
  }
  return statusMap[status] || '未知'
}

// 格式化文件大小
export function formatFileSize(bytes) {
  if (bytes === 0) return '0 Bytes'
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 格式化时长（秒转换为时分秒）
export function formatDuration(seconds) {
  if (!seconds) return '0分钟'

  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const secs = seconds % 60

  if (hours > 0) {
    return `${hours}小时${minutes}分钟`
  } else if (minutes > 0) {
    return `${minutes}分钟`
  } else {
    return `${secs}秒`
  }
}
