// composables/useFileUpload.js
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import {
  uploadCourseCoverAPI,
  uploadCourseMaterialAPI,
  uploadCourseVideoAPI
} from '@/api/course'

export function useFileUpload() {
  // 上传状态
  const uploading = reactive({
    cover: false,
    material: false,
    video: false // 🆕 新增视频上传状态
  })

  const uploadProgress = reactive({
    cover: 0,
    material: 0,
    video: 0 // 🆕 新增视频上传进度
  })

  // 🆕 视频上传专用状态
  const uploadSpeed = ref('')
  const estimatedTime = ref('')

  // 文件列表状态
  const fileListState = reactive({
    cover: [],
    materials: [],
    videos: [] // 🆕 新增视频文件列表
  })

  // 🔧 文件验证函数
  const validateImageFile = (file) => {
    const isImage = file.type.startsWith('image/')
    const isLt5M = file.size / 1024 / 1024 < 5

    if (!isImage) {
      ElMessage.error('只能上传图片格式的文件！')
      return false
    }
    if (!isLt5M) {
      ElMessage.error('图片大小不能超过 5MB！')
      return false
    }
    return true
  }

  const validateDocumentFile = (file) => {
    const allowedTypes = ['.pdf', '.doc', '.docx', '.ppt', '.pptx', '.xls', '.xlsx', '.txt', '.zip', '.rar']
    const fileName = file.name.toLowerCase()
    const isValidType = allowedTypes.some(type => fileName.endsWith(type))

    if (!isValidType) {
      ElMessage.error('不支持的文件格式！')
      return false
    }

    const isLt50M = file.size / 1024 / 1024 < 50
    if (!isLt50M) {
      ElMessage.error('文件大小不能超过 50MB！')
      return false
    }

    return true
  }

  // 🆕 视频文件验证
  const validateVideoFile = (file) => {
    const allowedTypes = ['video/mp4', 'video/avi', 'video/mov', 'video/wmv', 'video/x-flv', 'video/webm', 'video/x-matroska']
    const isValidType = allowedTypes.includes(file.type) || /\.(mp4|avi|mov|wmv|flv|webm|mkv)$/i.test(file.name)

    if (!isValidType) {
      ElMessage.error('只能上传 MP4、AVI、MOV、WMV、FLV、WebM、MKV 格式的视频文件！')
      return false
    }

    const isLt500M = file.size / 1024 / 1024 < 500
    if (!isLt500M) {
      ElMessage.error('视频文件大小不能超过 500MB！')
      return false
    }

    return true
  }

  // 🔧 上传处理函数

  // 封面上传
  const handleCoverUpload = async (options) => {
    const { file } = options
    try {
      uploading.cover = true
      uploadProgress.cover = 0

      const response = await uploadCourseCoverAPI(file)


      // 🔧 修复：根据实际响应结构判断成功
      if (response && (response.code === 0 || response.code === 200 || response.data)) {
        const coverInfo = {
          name: file.name,
          url: response.data?.url || response.url || URL.createObjectURL(file),
          uid: Date.now()
        }
        fileListState.cover = [coverInfo]
        ElMessage.success('封面上传成功！')
      } else {
        throw new Error(response?.message || '封面上传失败')
      }
    } catch (error) {
      ElMessage.error('封面上传失败')
    } finally {
      uploading.cover = false
      uploadProgress.cover = 0
    }
  }

  const handleCoverRemove = () => {
    fileListState.cover = []
  }

  // 文档上传
  const handleMaterialUpload = async (options) => {
    const { file } = options
    try {
      uploading.material = true
      uploadProgress.material = 0

      const response = await uploadCourseMaterialAPI(file)


      // 🔧 修复：根据实际响应结构判断成功
      if (response && (response.code === 0 || response.code === 200 || response.data)) {
        const materialInfo = {
          name: file.name,
          originalName: file.name,
          url: response.data?.url || response.url || URL.createObjectURL(file),
          size: file.size,
          uid: Date.now()
        }
        fileListState.materials.push(materialInfo)
        ElMessage.success(`资料 "${file.name}" 上传成功！`)
      } else {
        throw new Error(response?.message || '文档上传失败')
      }
    } catch (error) {
      ElMessage.error('资料上传失败')
    } finally {
      uploading.material = false
      uploadProgress.material = 0
    }
  }

  const handleMaterialRemove = (file) => {
    const index = fileListState.materials.findIndex(m => m.uid === file.uid)
    if (index > -1) {
      fileListState.materials.splice(index, 1)
    }
  }

  // 🆕 视频上传
  const handleVideoUpload = async (options) => {
    const { file, onProgress } = options

    try {
      uploading.video = true
      uploadProgress.video = 0

      // 创建上传进度处理函数
      const progressHandler = (progressEvent) => {
        if (progressEvent.lengthComputable) {
          const progress = Math.round((progressEvent.loaded * 100) / progressEvent.total)
          uploadProgress.video = progress

          // 计算上传速度
          const timeElapsed = (Date.now() - startTime) / 1000 // 秒
          const bytesPerSecond = progressEvent.loaded / timeElapsed
          const mbPerSecond = (bytesPerSecond / (1024 * 1024)).toFixed(2)
          uploadSpeed.value = `${mbPerSecond} MB/s`

          // 估算剩余时间
          const remainingBytes = progressEvent.total - progressEvent.loaded
          const remainingSeconds = remainingBytes / bytesPerSecond
          const minutes = Math.floor(remainingSeconds / 60)
          const seconds = Math.floor(remainingSeconds % 60)
          estimatedTime.value = `${minutes}:${seconds.toString().padStart(2, '0')}`

          if (onProgress) {
            onProgress({ percent: progress })
          }
        }
      }

      const startTime = Date.now()

      // 调用视频上传API
      const response = await uploadCourseVideoAPI(file, progressHandler)


      // 🔧 修复：根据实际响应结构判断成功
      if (response && (response.code === 0 || response.code === 200 || response.data)) {
        const videoInfo = {
          name: file.name,
          originalName: file.name,
          url: response.data?.url || response.url || URL.createObjectURL(file),
          size: file.size,
          uid: Date.now(),
          status: 'success'
        }

        // 添加到视频列表
        fileListState.videos.push(videoInfo)

        ElMessage.success(`视频 "${file.name}" 上传成功！`)
      } else {
        throw new Error(response?.message || '视频上传失败')
      }
    } catch (error) {
      ElMessage.error(`视频上传失败: ${error.message}`)

      // 上传失败时的处理
      options.onError && options.onError(error)
    } finally {
      uploading.video = false
      uploadProgress.video = 0
      uploadSpeed.value = ''
      estimatedTime.value = ''
    }
  }

  const handleVideoRemove = (file) => {

    const index = fileListState.videos.findIndex(v => v.uid === file.uid || v.url === file.url)
    if (index > -1) {
      fileListState.videos.splice(index, 1)
      ElMessage.success('视频已移除')
    }
  }

  // 🔧 工具函数
  const setFileList = (type, files) => {
    if (['cover', 'materials', 'videos'].includes(type)) {
      fileListState[type] = files || []
    }
  }

  const clearAllFiles = () => {
    fileListState.cover = []
    fileListState.materials = []
    fileListState.videos = []
  }

  const formatFileSize = (bytes) => {
    if (bytes === 0) return '0 B'

    const k = 1024
    const sizes = ['B', 'KB', 'MB', 'GB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))

    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
  }

  const getFileExtension = (filename) => {
    return filename.slice((filename.lastIndexOf('.') - 1 >>> 0) + 2).toUpperCase()
  }

  // 🆕 获取文件类型图标
  const getFileTypeIcon = (filename) => {
    const ext = filename.toLowerCase().split('.').pop()
    const iconMap = {
      // 图片
      jpg: '🖼️', jpeg: '🖼️', png: '🖼️', gif: '🖼️', bmp: '🖼️',
      // 视频
      mp4: '🎥', avi: '🎥', mov: '🎥', wmv: '🎥', flv: '🎥', webm: '🎥', mkv: '🎥',
      // 文档
      pdf: '📄', doc: '📝', docx: '📝', txt: '📄',
      // 表格
      xls: '📊', xlsx: '📊',
      // 演示
      ppt: '📽️', pptx: '📽️',
      // 压缩包
      zip: '📦', rar: '📦', '7z': '📦',
      // 音频
      mp3: '🎵', wav: '🎵', flac: '🎵'
    }
    return iconMap[ext] || '📎'
  }

  return {
    // 状态
    uploading,
    uploadProgress,
    uploadSpeed,
    estimatedTime,
    fileListState,

    // 验证函数
    validateImageFile,
    validateDocumentFile,
    validateVideoFile, // 🆕

    // 上传处理函数
    handleCoverUpload,
    handleCoverRemove,
    handleMaterialUpload,
    handleMaterialRemove,
    handleVideoUpload, // 🆕
    handleVideoRemove, // 🆕

    // 工具函数
    setFileList,
    clearAllFiles,
    formatFileSize,
    getFileExtension,
    getFileTypeIcon // 🆕
  }
}
