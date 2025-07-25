// frontend/src/api/course.js - 最小化修复版本
import request from '@/utils/request'

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

// 🔧 修复：创建课程API - 只修复章节数据映射问题
export function createCourseAPI(data) {
  const requestData = {
    title: data.title,
    description: data.description,
    category: data.category,
    difficultyLevel: getDifficultyLevel(data.level),
    estimatedDuration: data.duration,
    price: data.price || 0,
    isRequired: data.isRequired || false,
    coverImageUrl: data.coverImage || '',

    // 🔧 传递新格式的资料信息（包含文件名）
    materials:
      data.materials && data.materials.length > 0
        ? data.materials.map((material) => ({
          url: typeof material === 'string' ? material : material.url,
          name:
            typeof material === 'object'
              ? material.originalName || material.name || '学习资料'
              : '学习资料',
        }))
        : [],

    // 兼容旧格式
    materialUrls: data.materials
      ? data.materials.map((m) => (typeof m === 'string' ? m : m.url)).join(',')
      : '',

    videos:
      data.videos && data.videos.length > 0
        ? data.videos.map((video) => ({
          url: typeof video === 'string' ? video : video.url,
          name:
            typeof video === 'object'
              ? video.originalName || video.name || '视频资料'
              : '视频资料',
        }))
        : [],

    videoUrls: data.videos
      ? data.videos.map((v) => (typeof v === 'string' ? v : v.url)).join(',')
      : '',

    // 🔧 关键修复：完整的章节数据映射
    chapters: data.chapters && data.chapters.length > 0
      ? data.chapters.map((chapter, index) => {
        // 验证必填字段
        if (!chapter.title?.trim()) {
          throw new Error(`第 ${index + 1} 个章节的标题不能为空`)
        }

        return {
          title: chapter.title.trim(),
          description: chapter.description || '',

          // 🔧 关键修复：添加必填的 chapterType 字段
          chapterType: chapter.chapterType || chapter.type || 'document',

          // 🔧 关键修复：字段名映射 order -> sortOrder
          sortOrder: Number(chapter.order || chapter.sortOrder) || (index + 1),

          duration: Number(chapter.duration) || 0,
          content: chapter.content || '',
          videoUrl: chapter.videoUrl || '',
          // 将文本内容映射到后端的 contentUrl 字段
          contentUrl: chapter.content || chapter.contentUrl || chapter.videoUrl || '',
          status: typeof chapter.status === 'number' ? chapter.status : 0,

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

    // 🔧 修复：章节数据映射
    chapters: data.chapters
      ? data.chapters.map((chapter, index) => ({
        id: chapter.id,
        title: chapter.title,
        description: chapter.description || '',
        chapterType: chapter.chapterType || chapter.type || 'document',
        sortOrder: chapter.sortOrder || chapter.order || (index + 1),
        duration: chapter.duration || 0,
        content: chapter.content || '',
        videoUrl: chapter.videoUrl || '',
        // 将文本内容映射到后端的 contentUrl 字段
        contentUrl: chapter.content || chapter.contentUrl || chapter.videoUrl || '',
        status: typeof chapter.status === 'number' ? chapter.status : 0,
        isFree: Boolean(chapter.isFree),
        requirements: chapter.requirements || '',
        learningObjectives: chapter.learningObjectives || '',
        fileSize: chapter.fileSize || null,
        materialUrls: chapter.materialUrls || '',
        videoUrls: chapter.videoUrls || ''
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
    method: 'PUT',
    rawResponse: true
  })
}

// 下架课程
export function unpublishCourseAPI(courseId) {
  return request({
    url: `/api/v1/courses/${courseId}/unpublish`,
    method: 'PUT',
    rawResponse: true
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

// 🔧 修复：创建章节API
export function createChapterAPI(courseId, data) {
  return request({
    url: `/api/v1/courses/${courseId}/chapters`,
    method: 'POST',
    data: {
      title: data.title,
      description: data.description || '',
      chapterType: data.chapterType || 'document',
      duration: data.duration || 0,
      sortOrder: data.sortOrder || data.order || 1,
      content: data.content || '',
      videoUrl: data.videoUrl || '',
      // 文本内容映射到后端字段
      contentUrl: data.content || data.contentUrl || '',
      status: typeof data.status === 'number' ? data.status : 0,
      materialUrls: data.materialUrls || '',
      isFree: Boolean(data.isFree),
      requirements: data.requirements || '',
      learningObjectives: data.learningObjectives || '',
      fileSize: data.fileSize || null,
      fileFormat: data.fileFormat || null
    },
  })
}

// 🔧 修复：更新章节API
export function updateChapterAPI(courseId, chapterId, data) {
  return request({
    url: `/api/v1/courses/${courseId}/chapters/${chapterId}`,
    method: 'PUT',
    data: {
      title: data.title,
      description: data.description || '',
      chapterType: data.chapterType || 'document',
      duration: data.duration || 0,
      sortOrder: data.sortOrder || data.order || 1,
      content: data.content || '',
      videoUrl: data.videoUrl || '',
      // 文本内容映射到后端字段
      contentUrl: data.content || data.contentUrl || '',
      status: typeof data.status === 'number' ? data.status : 0,
      materialUrls: data.materialUrls || '',
      isFree: Boolean(data.isFree),
      requirements: data.requirements || '',
      learningObjectives: data.learningObjectives || '',
      fileSize: data.fileSize || null,
      fileFormat: data.fileFormat || null
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

// 获取最新课程
export function getLatestCoursesAPI(page = 0, size = 10) {
  return request({
    url: '/api/v1/courses/latest',
    method: 'GET',
    params: { page, size },
  })
}

/**
 * ==================== 统计API ====================
 */

// 获取课程统计
export function getCourseStatsAPI() {
  return request({
    url: '/api/v1/courses/stats',
    method: 'GET',
  })
}

// 获取讲师课程统计
export function getInstructorStatsAPI(instructorId) {
  return request({
    url: `/api/v1/courses/instructor/${instructorId}/stats`,
    method: 'GET',
  })
}
