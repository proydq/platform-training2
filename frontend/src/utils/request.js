// ==================== 1. API接口文件 ====================
// frontend/src/api/course.js
import request from '@/utils/request'

/**
 * 课程管理API接口
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
      sortDir: params.sortDir || 'desc'
    }
  })
}

// 获取课程详情
export function getCourseDetailAPI(courseId) {
  return request({
    url: `/api/v1/courses/${courseId}`,
    method: 'GET'
  })
}

// 创建课程
export function createCourseAPI(data) {
  return request({
    url: '/api/v1/courses',
    method: 'POST',
    data: {
      title: data.title,
      description: data.description,
      category: data.category,
      difficultyLevel: getDifficultyLevel(data.level),
      duration: data.duration,
      instructorId: data.instructorId,
      price: data.price || 0,
      isRequired: data.isRequired || false,
      coverImage: data.coverImage || '',
      materials: data.materials || [],
      chapters: data.chapters ? data.chapters.map(chapter => ({
        title: chapter.title,
        description: chapter.description || '',
        duration: chapter.duration,
        sortOrder: chapter.order,
        content: chapter.content || '',
        videoUrl: chapter.videoUrl || '',
        materialUrls: chapter.materialUrls || []
      })) : []
    }
  })
}

// 更新课程
export function updateCourseAPI(courseId, data) {
  return request({
    url: `/api/v1/courses/${courseId}`,
    method: 'PUT',
    data: {
      title: data.title,
      description: data.description,
      category: data.category,
      difficultyLevel: getDifficultyLevel(data.level),
      duration: data.duration,
      instructorId: data.instructorId,
      price: data.price || 0,
      isRequired: data.isRequired || false,
      coverImage: data.coverImage || '',
      materials: data.materials || []
    }
  })
}

// 删除课程
export function deleteCourseAPI(courseId) {
  return request({
    url: `/api/v1/courses/${courseId}`,
    method: 'DELETE'
  })
}

// 发布课程
export function publishCourseAPI(courseId) {
  return request({
    url: `/api/v1/courses/${courseId}/publish`,
    method: 'POST'
  })
}

// 下架课程
export function unpublishCourseAPI(courseId) {
  return request({
    url: `/api/v1/courses/${courseId}/unpublish`,
    method: 'POST'
  })
}

// 获取课程章节列表
export function getCourseChaptersAPI(courseId, status = null) {
  return request({
    url: `/api/v1/courses/${courseId}/chapters`,
    method: 'GET',
    params: status ? { status } : {}
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
      materialUrls: data.materialUrls || []
    }
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
      materialUrls: data.materialUrls || []
    }
  })
}

// 删除章节
export function deleteChapterAPI(courseId, chapterId) {
  return request({
    url: `/api/v1/courses/${courseId}/chapters/${chapterId}`,
    method: 'DELETE'
  })
}

// 文件上传
export function uploadCourseCoverAPI(file) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('category', 'course')
  formData.append('type', 'cover')
  
  return request({
    url: '/api/v1/files/upload',
    method: 'POST',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function uploadCourseMaterialAPI(file) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('category', 'course')
  formData.append('type', 'material')
  
  return request({
    url: '/api/v1/files/upload',
    method: 'POST',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 工具函数
function getDifficultyLevel(level) {
  const levelMap = {
    '入门级': 1,
    '初级': 2,
    '中级': 3,
    '高级': 4,
    '专家级': 5
  }
  return levelMap[level] || 1
}

export function getDifficultyLevelText(level) {
  const levelMap = {
    1: '入门级',
    2: '初级',
    3: '中级',
    4: '高级',
    5: '专家级'
  }
  return levelMap[level] || '入门级'
}

export function getCourseStatusText(status) {
  const statusMap = {
    0: '草稿',
    1: '已发布',
    2: '已下架'
  }
  return statusMap[status] || '未知'
}

// ==================== 2. 课程管理Composable ====================
// frontend/src/composables/useCourse.js
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getCourseListAPI,
  createCourseAPI,
  updateCourseAPI,
  deleteCourseAPI,
  publishCourseAPI,
  unpublishCourseAPI,
  getDifficultyLevelText,
  getCourseStatusText
} from '@/api/course'

export function useCourse() {
  const loading = ref(false)
  const saving = ref(false)
  const courses = ref([])

  const pagination = reactive({
    current: 1,
    size: 20,
    total: 0
  })

  const filters = reactive({
    keyword: '',
    category: '',
    difficultyLevel: '',
    status: '',
    instructorId: ''
  })

  const hasFilters = computed(() => {
    return Object.values(filters).some(value => value !== '')
  })

  const isEmpty = computed(() => {
    return !loading.value && courses.value.length === 0
  })

  const loadCourses = async (options = {}) => {
    try {
      loading.value = true
      
      const params = {
        page: pagination.current - 1,
        size: pagination.size,
        ...filters,
        ...options
      }
      
      const response = await getCourseListAPI(params)
      
      if (response.code === 200) {
        courses.value = response.data.content || []
        pagination.total = response.data.totalElements || 0
        return response.data
      } else {
        ElMessage.error(response.message || '获取课程列表失败')
        return null
      }
    } catch (error) {
      console.error('获取课程列表失败:', error)
      ElMessage.error('获取课程列表失败')
      return null
    } finally {
      loading.value = false
    }
  }

  const searchCourses = async () => {
    pagination.current = 1
    return await loadCourses()
  }

  const resetFilters = async () => {
    Object.assign(filters, {
      keyword: '',
      category: '',
      difficultyLevel: '',
      status: '',
      instructorId: ''
    })
    pagination.current = 1
    return await loadCourses()
  }

  const handlePagination = async (page, size = null) => {
    pagination.current = page
    if (size) {
      pagination.size = size
    }
    return await loadCourses()
  }

  const createCourse = async (courseData) => {
    try {
      saving.value = true
      const response = await createCourseAPI(courseData)
      
      if (response.code === 200) {
        ElMessage.success('课程创建成功')
        await loadCourses()
        return response.data
      } else {
        ElMessage.error(response.message || '课程创建失败')
        return null
      }
    } catch (error) {
      console.error('创建课程失败:', error)
      ElMessage.error('创建课程失败')
      return null
    } finally {
      saving.value = false
    }
  }

  const updateCourse = async (courseId, courseData) => {
    try {
      saving.value = true
      const response = await updateCourseAPI(courseId, courseData)
      
      if (response.code === 200) {
        ElMessage.success('课程更新成功')
        await loadCourses()
        return response.data
      } else {
        ElMessage.error(response.message || '课程更新失败')
        return null
      }
    } catch (error) {
      console.error('更新课程失败:', error)
      ElMessage.error('更新课程失败')
      return null
    } finally {
      saving.value = false
    }
  }

  const deleteCourse = async (courseId, courseName = '') => {
    try {
      await ElMessageBox.confirm(
        `确定要删除课程"${courseName}"吗？删除后无法恢复。`,
        '确认删除',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
      
      const response = await deleteCourseAPI(courseId)
      
      if (response.code === 200) {
        ElMessage.success('课程删除成功')
        await loadCourses()
        return true
      } else {
        ElMessage.error(response.message || '课程删除失败')
        return false
      }
    } catch (error) {
      if (error !== 'cancel') {
        console.error('删除课程失败:', error)
        ElMessage.error('删除课程失败')
      }
      return false
    }
  }

  const publishCourse = async (courseId, courseName = '') => {
    try {
      const response = await publishCourseAPI(courseId)
      
      if (response.code === 200) {
        ElMessage.success(`课程"${courseName}"发布成功`)
        await loadCourses()
        return true
      } else {
        ElMessage.error(response.message || '课程发布失败')
        return false
      }
    } catch (error) {
      console.error('发布课程失败:', error)
      ElMessage.error('发布课程失败')
      return false
    }
  }

  const unpublishCourse = async (courseId, courseName = '') => {
    try {
      await ElMessageBox.confirm(
        `确定要下架课程"${courseName}"吗？`,
        '确认下架',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
      
      const response = await unpublishCourseAPI(courseId)
      
      if (response.code === 200) {
        ElMessage.success(`课程"${courseName}"下架成功`)
        await loadCourses()
        return true
      } else {
        ElMessage.error(response.message || '课程下架失败')
        return false
      }
    } catch (error) {
      if (error !== 'cancel') {
        console.error('下架课程失败:', error)
        ElMessage.error('下架课程失败')
      }
      return false
    }
  }

  const formatDate = (dateStr) => {
    if (!dateStr) return '--'
    const date = new Date(dateStr)
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    })
  }

  const getStatusColor = (status) => {
    const colorMap = {
      0: '#ffc107',
      1: '#28a745',
      2: '#dc3545'
    }
    return colorMap[status] || '#6c757d'
  }

  const getDifficultyType = (level) => {
    const typeMap = {
      1: '',
      2: 'success',
      3: 'warning',
      4: 'danger',
      5: 'info'
    }
    return typeMap[level] || ''
  }

  const formatPrice = (price) => {
    if (price === 0) return '免费'
    return `¥${price.toFixed(2)}`
  }

  const formatDuration = (minutes) => {
    if (!minutes) return '--'
    if (minutes < 60) return `${minutes}分钟`
    const hours = Math.floor(minutes / 60)
    const mins = minutes % 60
    return mins > 0 ? `${hours}小时${mins}分钟` : `${hours}小时`
  }

  return {
    loading,
    saving,
    courses,
    pagination,
    filters,
    hasFilters,
    isEmpty,
    loadCourses,
    searchCourses,
    resetFilters,
    handlePagination,
    createCourse,
    updateCourse,
    deleteCourse,
    publishCourse,
    unpublishCourse,
    formatDate,
    getStatusColor,
    getDifficultyType,
    formatPrice,
    formatDuration,
    getDifficultyLevelText,
    getCourseStatusText
  }
}

// ==================== 3. 文件上传Composable ====================
// frontend/src/composables/useFileUpload.js
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import {
  uploadCourseCoverAPI,
  uploadCourseMaterialAPI
} from '@/api/course'

export function useFileUpload() {
  const uploading = ref(false)
  const uploadProgress = ref(0)
  
  const fileListState = reactive({
    cover: [],
    materials: []
  })

  const validateImageFile = (file) => {
    const validTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp']
    const maxSize = 5 * 1024 * 1024
    
    if (!validTypes.includes(file.type)) {
      ElMessage.error('请上传 JPG、PNG、GIF 或 WEBP 格式的图片')
      return false
    }
    
    if (file.size > maxSize) {
      ElMessage.error('图片大小不能超过 5MB')
      return false
    }
    
    return true
  }

  const validateDocumentFile = (file) => {
    const validTypes = [
      'application/pdf',
      'application/msword',
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
      'application/vnd.ms-excel',
      'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      'text/plain',
      'application/zip'
    ]
    const maxSize = 50 * 1024 * 1024
    
    if (!validTypes.includes(file.type)) {
      ElMessage.error('请上传 PDF、Word、Excel、TXT 或 ZIP 格式的文件')
      return false
    }
    
    if (file.size > maxSize) {
      ElMessage.error('文件大小不能超过 50MB')
      return false
    }
    
    return true
  }

  const handleCoverUpload = async (options) => {
    const { file, onSuccess, onError } = options
    
    if (!validateImageFile(file)) {
      onError(new Error('文件验证失败'))
      return
    }
    
    try {
      uploading.value = true
      const response = await uploadCourseCoverAPI(file)
      
      if (response.code === 200) {
        const fileInfo = {
          name: file.name,
          url: response.data.url,
          uid: file.uid
        }
        
        fileListState.cover = [fileInfo]
        ElMessage.success('封面上传成功')
        onSuccess(response.data)
        return fileInfo
      } else {
        throw new Error(response.message || '上传失败')
      }
    } catch (error) {
      console.error('封面上传失败:', error)
      ElMessage.error(error.message || '封面上传失败')
      onError(error)
      return null
    } finally {
      uploading.value = false
    }
  }

  const handleMaterialUpload = async (options) => {
    const { file, onSuccess, onError } = options
    
    if (!validateDocumentFile(file)) {
      onError(new Error('文件验证失败'))
      return
    }
    
    try {
      uploading.value = true
      const response = await uploadCourseMaterialAPI(file)
      
      if (response.code === 200) {
        const fileInfo = {
          name: file.name,
          url: response.data.url,
          uid: file.uid
        }
        
        fileListState.materials.push(fileInfo)
        ElMessage.success('资料上传成功')
        onSuccess(response.data)
        return fileInfo
      } else {
        throw new Error(response.message || '上传失败')
      }
    } catch (error) {
      console.error('资料上传失败:', error)
      ElMessage.error(error.message || '资料上传失败')
      onError(error)
      return null
    } finally {
      uploading.value = false
    }
  }

  const handleCoverRemove = () => {
    fileListState.cover = []
  }

  const handleMaterialRemove = (file) => {
    const index = fileListState.materials.findIndex(item => item.uid === file.uid)
    if (index > -1) {
      fileListState.materials.splice(index, 1)
    }
  }

  const setFileList = (type, files) => {
    if (fileListState.hasOwnProperty(type)) {
      fileListState[type] = files || []
    }
  }

  const clearAllFiles = () => {
    fileListState.cover = []
    fileListState.materials = []
  }

  return {
    uploading,
    uploadProgress,
    fileListState,
    handleCoverUpload,
    handleMaterialUpload,
    handleCoverRemove,
    handleMaterialRemove,
    setFileList,
    clearAllFiles,
    validateImageFile,
    validateDocumentFile
  }
}