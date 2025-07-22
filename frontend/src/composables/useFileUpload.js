// frontend/src/composables/useFileUpload.js - 文件上传逻辑复用
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { uploadCourseCoverAPI, uploadCourseMaterialAPI, uploadCourseVideoAPI } from '@/api/course'

export function useFileUpload() {
  // ==================== 响应式数据 ====================
  const uploading = ref(false)
  const uploadProgress = ref(0)

  // 文件列表状态
  const fileListState = reactive({
    cover: [],
    materials: [],
    videos: [],
  })

  // ==================== 文件类型验证 ====================

  /**
   * 验证图片文件
   */
  const validateImageFile = (file) => {
    const validTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp']
    const maxSize = 5 * 1024 * 1024 // 5MB

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

  /**
   * 验证视频文件
   */
  const validateVideoFile = (file) => {
    const validTypes = ['video/mp4', 'video/avi', 'video/mov', 'video/wmv', 'video/flv']
    const maxSize = 200 * 1024 * 1024 // 200MB

    if (!validTypes.includes(file.type)) {
      ElMessage.error('请上传 MP4、AVI、MOV、WMV 或 FLV 格式的视频')
      return false
    }

    if (file.size > maxSize) {
      ElMessage.error('视频大小不能超过 200MB')
      return false
    }

    return true
  }

  /**
   * 验证文档文件
   */
  const validateDocumentFile = (file) => {
    const validTypes = [
      'application/pdf',
      'application/msword',
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
      'application/vnd.ms-excel',
      'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      'application/vnd.ms-powerpoint',
      'application/vnd.openxmlformats-officedocument.presentationml.presentation',
      'text/plain',
      'application/zip',
      'application/x-rar-compressed',
    ]
    const maxSize = 50 * 1024 * 1024 // 50MB

    if (!validTypes.includes(file.type)) {
      ElMessage.error('请上传 PDF、Word、Excel、PPT、TXT、ZIP 或 RAR 格式的文件')
      return false
    }

    if (file.size > maxSize) {
      ElMessage.error('文件大小不能超过 50MB')
      return false
    }

    return true
  }

  // ==================== 文件上传处理 ====================

  /**
   * 上传课程封面
   */
  const handleCoverUpload = async (options) => {
    const { file, onSuccess, onError } = options

    if (!validateImageFile(file)) {
      onError(new Error('文件验证失败'))
      return
    }

    try {
      uploading.value = true
      uploadProgress.value = 0

      const response = await uploadCourseCoverAPI(file)

      if (response.code === 200) {
        const fileInfo = {
          name: file.name,
          url: response.data.url,
          size: file.size,
          type: file.type,
          uid: file.uid,
        }

        // 更新文件列表
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
      uploadProgress.value = 0
    }
  }

  /**
   * 上传教学资料
   */
  const handleMaterialUpload = async (options) => {
    const { file, onSuccess, onError } = options

    if (!validateDocumentFile(file)) {
      onError(new Error('文件验证失败'))
      return
    }

    try {
      uploading.value = true
      uploadProgress.value = 0

      const response = await uploadCourseMaterialAPI(file)

      if (response.code === 200) {
        const fileInfo = {
          name: file.name, // 🔧 保存原始文件名
          url: response.data.url,
          size: file.size,
          type: file.type,
          uid: file.uid || Date.now(),
          originalName: file.name, // 🔧 添加原始文件名字段
        }

        // 更新文件列表
        fileListState.materials.push(fileInfo)

        ElMessage.success(`${file.name} 上传成功`)
        onSuccess(response.data)
        return fileInfo
      } else {
        throw new Error(response.message || '上传失败')
      }
    } catch (error) {
      console.error('教学资料上传失败:', error)
      ElMessage.error(error.message || '教学资料上传失败')
      onError(error)
      return null
    } finally {
      uploading.value = false
      uploadProgress.value = 0
    }
  }

  /**
   * 上传课程视频
   */
  const handleVideoUpload = async (options) => {
    const { file, onSuccess, onError, onProgress } = options

    if (!validateVideoFile(file)) {
      onError(new Error('文件验证失败'))
      return
    }

    try {
      uploading.value = true
      uploadProgress.value = 0

      const progressHandler = (progressEvent) => {
        if (progressEvent.lengthComputable) {
          const progress = Math.round((progressEvent.loaded * 100) / progressEvent.total)
          uploadProgress.value = progress
          onProgress && onProgress({ percent: progress })
        }
      }

      const response = await uploadCourseVideoAPI(file, progressHandler)

      if (response.code === 200) {
        const fileInfo = {
          name: file.name,
          url: response.data.url,
          size: file.size,
          type: file.type,
          uid: file.uid,
        }

        // 添加到文件列表
        fileListState.videos.push(fileInfo)

        ElMessage.success('视频上传成功')
        onSuccess(response.data)
        return fileInfo
      } else {
        throw new Error(response.message || '上传失败')
      }
    } catch (error) {
      console.error('视频上传失败:', error)
      ElMessage.error(error.message || '视频上传失败')
      onError(error)
      return null
    } finally {
      uploading.value = false
      uploadProgress.value = 0
    }
  }

  // ==================== 文件列表管理 ====================

  /**
   * 移除封面文件
   */
  const handleCoverRemove = (file) => {
    fileListState.cover = fileListState.cover.filter((item) => item.uid !== file.uid)
    ElMessage.success('封面已移除')
  }

  /**
   * 移除资料文件
   */
  const handleMaterialRemove = (file) => {
    const index = fileListState.materials.findIndex((item) => item.uid === file.uid)
    if (index > -1) {
      fileListState.materials.splice(index, 1)
      ElMessage.success('资料已移除')
    }
  }

  /**
   * 移除视频文件
   */
  const handleVideoRemove = (file) => {
    const index = fileListState.videos.findIndex((item) => item.uid === file.uid)
    if (index > -1) {
      fileListState.videos.splice(index, 1)
      ElMessage.success('视频已移除')
    }
  }

  /**
   * 清空所有文件
   */
  const clearAllFiles = () => {
    fileListState.cover = []
    fileListState.materials = []
    fileListState.videos = []
  }

  /**
   * 设置文件列表（用于编辑时回显）
   */
  const setFileList = (type, files) => {
    if (fileListState.hasOwnProperty(type)) {
      fileListState[type] = files || []
    }
  }

  // ==================== 工具方法 ====================

  /**
   * 格式化文件大小
   */
  const formatFileSize = (bytes) => {
    if (bytes === 0) return '0 Bytes'
    const k = 1024
    const sizes = ['Bytes', 'KB', 'MB', 'GB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
  }

  /**
   * 获取文件扩展名
   */
  const getFileExtension = (filename) => {
    return filename.slice(((filename.lastIndexOf('.') - 1) >>> 0) + 2)
  }

  /**
   * 获取文件类型图标
   */
  const getFileTypeIcon = (filename) => {
    const ext = getFileExtension(filename).toLowerCase()
    const iconMap = {
      pdf: '📄',
      doc: '📝',
      docx: '📝',
      xls: '📊',
      xlsx: '📊',
      ppt: '📊',
      pptx: '📊',
      txt: '📄',
      zip: '📦',
      rar: '📦',
      jpg: '🖼️',
      jpeg: '🖼️',
      png: '🖼️',
      gif: '🖼️',
      webp: '🖼️',
      mp4: '🎬',
      avi: '🎬',
      mov: '🎬',
      wmv: '🎬',
      flv: '🎬',
    }
    return iconMap[ext] || '📄'
  }

  /**
   * 检查文件是否为图片
   */
  const isImageFile = (file) => {
    return file.type.startsWith('image/')
  }

  /**
   * 检查文件是否为视频
   */
  const isVideoFile = (file) => {
    return file.type.startsWith('video/')
  }

  /**
   * 生成文件预览URL
   */
  const generatePreviewUrl = (file) => {
    if (file.url) {
      return file.url
    }
    if (file instanceof File) {
      return URL.createObjectURL(file)
    }
    return ''
  }

  // ==================== 批量操作 ====================

  /**
   * 批量上传文件
   */
  const batchUpload = async (files, type = 'material') => {
    const results = []
    const errors = []

    for (const file of files) {
      try {
        let result = null

        switch (type) {
          case 'cover':
            result = await handleCoverUpload({
              file,
              onSuccess: () => {},
              onError: () => {},
            })
            break
          case 'material':
            result = await handleMaterialUpload({
              file,
              onSuccess: () => {},
              onError: () => {},
            })
            break
          case 'video':
            result = await handleVideoUpload({
              file,
              onSuccess: () => {},
              onError: () => {},
            })
            break
        }

        if (result) {
          results.push(result)
        }
      } catch (error) {
        errors.push({ file: file.name, error: error.message })
      }
    }

    if (errors.length > 0) {
      console.warn('部分文件上传失败:', errors)
    }

    return { results, errors }
  }

  // ==================== 返回接口 ====================
  return {
    // 响应式数据
    uploading,
    uploadProgress,
    fileListState,

    // 文件上传
    handleCoverUpload,
    handleMaterialUpload,
    handleVideoUpload,

    // 文件管理
    handleCoverRemove,
    handleMaterialRemove,
    handleVideoRemove,
    clearAllFiles,
    setFileList,

    // 工具方法
    formatFileSize,
    getFileExtension,
    getFileTypeIcon,
    isImageFile,
    isVideoFile,
    generatePreviewUrl,

    // 验证方法
    validateImageFile,
    validateVideoFile,
    validateDocumentFile,

    // 批量操作
    batchUpload,
  }
}
