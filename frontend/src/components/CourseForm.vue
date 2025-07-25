<template>
  <div class="course-form-container">
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="120px"
      class="course-form"
    >
      <!-- 基础信息 -->
      <div class="form-section">
        <h3 class="section-title">📋 基础信息</h3>

        <div class="form-row">
          <div class="form-col">
            <el-form-item label="课程名称" prop="title">
              <el-input
                v-model="form.title"
                placeholder="请输入课程名称"
                maxlength="100"
                show-word-limit
              />
            </el-form-item>
          </div>
          <div class="form-col">
            <el-form-item label="课程分类" prop="category">
              <el-select v-model="form.category" placeholder="请选择课程分类" style="width: 100%">
                <el-option
                  v-for="category in courseCategories"
                  :key="category"
                  :label="category"
                  :value="category"
                />
              </el-select>
            </el-form-item>
          </div>
        </div>

        <div class="form-row">
          <div class="form-col">
            <el-form-item label="难度级别" prop="level">
              <el-select v-model="form.level" placeholder="请选择难度级别" style="width: 100%">
                <el-option
                  v-for="level in difficultyLevels"
                  :key="level.value"
                  :label="level.label"
                  :value="level.value"
                />
              </el-select>
            </el-form-item>
          </div>
          <div class="form-col">
            <el-form-item label="课程时长">
              <el-input
                :value="formatDuration(calculateTotalDuration())"
                readonly
                placeholder="基于章节时长自动计算"
              >
                <template #prefix>
                  <el-icon><Clock /></el-icon>
                </template>
              </el-input>
            </el-form-item>
          </div>
        </div>

        <!-- 课程封面 -->
        <el-form-item label="课程封面" prop="coverImage">
          <div class="cover-upload-container">
            <!-- 左侧：当前封面 -->
            <div class="cover-current" @click="selectCoverImage">
              <img
                v-if="form.coverImage"
                :src="form.coverImage"
                alt="课程封面"
                class="cover-preview"
              />
              <div v-else class="cover-placeholder">
                <span>暂无封面</span>
              </div>
              <div v-if="form.coverImage" class="cover-overlay">
                <div class="cover-actions">
                  <el-button type="primary" size="small" @click.stop="selectCoverImage">
                    <el-icon><Picture /></el-icon>
                    更换
                  </el-button>
                  <el-button type="danger" size="small" @click.stop="removeCover">
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-button>
                </div>
              </div>
            </div>

            <!-- 右侧：上传区域 -->
            <div class="cover-upload-area" @click="selectCoverImage">
              <div class="cover-upload-plus">+</div>
              <div class="upload-text">选择封面</div>
            </div>
          </div>

          <el-upload
            ref="coverUploadRef"
            v-model:file-list="coverFileList"
            :show-file-list="false"
            action="/api/v1/upload/course-cover"
            :http-request="handleCoverUpload"
            :before-upload="beforeCoverUpload"
            accept="image/*"
            style="display: none;"
          >
            <div ref="hiddenCoverTrigger"></div>
          </el-upload>

          <div class="upload-hint">
            支持 JPG、PNG、GIF 格式，文件大小不超过 5MB
          </div>
        </el-form-item>

        <el-form-item label="课程描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请详细描述课程内容、学习目标等"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </div>

      <!-- 课程章节 -->
      <div class="form-section">
        <h3 class="section-title">🎵 课程章节</h3>

        <div class="chapters-header">
          <div class="chapters-count">
            📊 共 {{ form.chapters.length }} 个章节，总时长 {{ formatDuration(calculateTotalDuration()) }}
          </div>
          <el-button type="primary" @click="addChapter">
            <el-icon><Plus /></el-icon>
            添加章节
          </el-button>
        </div>

        <div v-if="form.chapters.length === 0" class="empty-chapters">
          <el-empty description="暂无章节，点击上方按钮添加章节" :image-size="80" />
        </div>

        <div v-else class="chapters-list">
          <div
            v-for="(chapter, index) in sortedChapters"
            :key="chapter.id || index"
            class="chapter-item"
          >
            <div class="chapter-content">
              <div class="chapter-header-info">
                <div class="chapter-title-section">
                  <div class="chapter-title">
                    <span class="chapter-number">{{ String(chapter.sortOrder || chapter.order || index + 1).padStart(2, '0') }}</span>
                    <span class="title-text">{{ chapter.title }}</span>
                  </div>
                </div>
                <div class="chapter-actions">
                  <el-button type="primary" link size="small" @click="editChapter(index)">
                    <el-icon><Edit /></el-icon>
                    编辑
                  </el-button>
                  <el-button type="danger" link size="small" @click="removeChapter(index)">
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-button>
                </div>
              </div>

              <div v-if="chapter.mediaUrl" class="media-info">
                <div class="media-meta">
                  <div class="media-name">
                    <el-icon v-if="isVideoFile(chapter.mediaUrl)"><VideoPlay /></el-icon>
                    <el-icon v-else><Microphone /></el-icon>
                    {{ getFileName(chapter.mediaUrl) }}
                    <span
                      class="media-type-badge"
                      :class="isVideoFile(chapter.mediaUrl) ? 'media-type-video' : 'media-type-audio'"
                    >
                      {{ isVideoFile(chapter.mediaUrl) ? 'VIDEO' : 'AUDIO' }}
                    </span>
                  </div>
                  <div class="media-duration">
                    <el-icon><Clock /></el-icon>
                    <span v-if="chapter.duration === -1" class="calculating">计算中...</span>
                    <span v-else>{{ formatDuration(chapter.duration || 0) }}</span>
                  </div>
                </div>
                <div class="media-size">文件大小: {{ formatFileSize(chapter.fileSize || 0) }}</div>
              </div>
              <div v-else class="no-media">
                <el-icon><Warning /></el-icon>
                <span>暂未上传音视频资料</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 提交按钮 -->
      <div class="form-actions">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">
          <el-icon><Check /></el-icon>
          {{ isEditing ? '保存修改' : '创建课程' }}
        </el-button>
      </div>
    </el-form>

    <!-- 章节编辑对话框 -->
    <el-dialog
      v-model="chapterModalVisible"
      :title="chapterModalTitle"
      width="600px"
      :close-on-click-modal="false"
      class="chapter-dialog"
    >
      <el-form ref="chapterFormRef" :model="chapterForm" :rules="chapterRules" label-width="100px">
        <el-form-item label="章节标题" prop="title">
          <el-input
            v-model="chapterForm.title"
            placeholder="请输入章节标题"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="排序序号" prop="sortOrder">
          <el-input-number
            v-model="chapterForm.sortOrder"
            :min="1"
            :max="100"
            placeholder="章节排序"
            style="width: 200px"
          />
        </el-form-item>

        <el-form-item label="音视频资料" prop="mediaUrl">
          <div class="media-upload-wrapper">
            <el-upload
              ref="mediaUploadRef"
              v-model:file-list="chapterMediaFileList"
              :show-file-list="false"
              action="/api/v1/upload/chapter-resource"
              :http-request="handleChapterUpload"
              :before-upload="beforeMediaUpload"
              accept="video/*,audio/*"
              drag
              class="media-upload"
            >
              <div v-if="!chapterForm.mediaUrl" class="upload-area">
                <el-icon class="upload-icon"><UploadFilled /></el-icon>
                <div class="upload-text">点击选择音视频文件或拖拽到此处</div>
                <div class="upload-hint">
                  支持视频格式：MP4、AVI、MOV 等 (最大 500MB)<br>
                  支持音频格式：MP3、WAV、AAC 等 (最大 200MB)
                </div>
              </div>
              <div v-else class="media-preview">
                <div class="media-info-preview">
                  <div class="media-meta-preview">
                    <div class="media-name-preview">
                      <el-icon v-if="isVideoFile(chapterForm.mediaUrl)"><VideoPlay /></el-icon>
                      <el-icon v-else><Microphone /></el-icon>
                      {{ getFileName(chapterForm.mediaUrl) }}
                      <span
                        class="media-type-badge"
                        :class="isVideoFile(chapterForm.mediaUrl) ? 'media-type-video' : 'media-type-audio'"
                      >
                        {{ isVideoFile(chapterForm.mediaUrl) ? 'VIDEO' : 'AUDIO' }}
                      </span>
                    </div>
                    <div class="media-duration-preview">
                      <el-icon><Clock /></el-icon>
                      <span v-if="chapterForm.duration === -1" class="calculating">计算中...</span>
                      <span v-else>{{ formatDuration(chapterForm.duration || 0) }}</span>
                    </div>
                  </div>
                  <div class="media-size-preview">文件大小: {{ formatFileSize(chapterForm.fileSize || 0) }}</div>
                  <el-button type="danger" size="small" @click.stop="removeMedia" style="margin-top: 8px;">
                    <el-icon><Delete /></el-icon>
                    移除文件
                  </el-button>
                </div>
              </div>
            </el-upload>
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeChapterModal">取消</el-button>
          <el-button type="primary" @click="saveChapter" :loading="chapterSaving">
            <el-icon><Check /></el-icon>
            保存章节
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Edit, Delete, Clock, Picture, VideoPlay, Microphone,
  UploadFilled, Check, Warning
} from '@element-plus/icons-vue'
import { useFileUpload } from '@/composables/useFileUpload'
import request from '@/utils/request'
import axios from 'axios'

// 工具函数提前声明，供 watch 中安全使用
function getFileName(url) {
  return url?.split('/')?.pop() || '未命名资源'
}

// Props
const props = defineProps({
  courseData: {
    type: Object,
    default: () => ({})
  },
  isEditing: {
    type: Boolean,
    default: false
  }
})

// Emits
const emit = defineEmits(['save', 'cancel'])

// Refs
const formRef = ref()
const chapterFormRef = ref()
const coverUploadRef = ref()
const mediaUploadRef = ref()
const hiddenCoverTrigger = ref()
// 文件列表
const coverFileList = ref([])
const chapterMediaFileList = ref([])

// 状态管理
const saving = ref(false)
const chapterSaving = ref(false)
const chapterModalVisible = ref(false)
const chapterModalTitle = ref('添加章节')
const editingChapterIndex = ref(-1)

// 使用文件上传组合函数
const {
  uploading,
  uploadProgress,
  uploadSpeed,
  estimatedTime,
  fileListState
} = useFileUpload()

// 表单数据
const form = ref({
  title: '',
  description: '',
  category: '',
  level: 1,
  coverImage: '',
  chapters: []
})

// 章节表单数据
const chapterForm = ref({
  title: '',
  sortOrder: 1,
  duration: 0,
  mediaUrl: '',
  videoUrl: '',
  fileSize: 0
})

// 配置数据
const courseCategories = ['技术培训', '产品培训', '安全培训', '管理培训', '营销培训']

const difficultyLevels = [
  { label: '入门级', value: 1 },
  { label: '初级', value: 2 },
  { label: '中级', value: 3 },
  { label: '高级', value: 4 },
  { label: '专家级', value: 5 }
]

// 表单验证规则
const rules = {
  title: [
    { required: true, message: '请输入课程名称', trigger: 'blur' },
    { min: 2, max: 100, message: '课程名称长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入课程描述', trigger: 'blur' },
    { min: 10, max: 500, message: '课程描述长度在 10 到 500 个字符', trigger: 'blur' }
  ],
  category: [{ required: true, message: '请选择课程分类', trigger: 'change' }],
  level: [{ required: true, message: '请选择难度级别', trigger: 'change' }]
}

// 章节表单验证规则
const chapterRules = {
  title: [
    { required: true, message: '请输入章节标题', trigger: 'blur' },
    { min: 2, max: 100, message: '章节标题长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  sortOrder: [
    { required: true, message: '请输入排序序号', trigger: 'blur' },
    { type: 'number', min: 1, max: 100, message: '排序序号必须在 1-100 之间', trigger: 'blur' }
  ]
}

// 计算属性
const sortedChapters = computed(() => {
  return [...form.value.chapters].sort((a, b) => (a.sortOrder || a.order || 0) - (b.sortOrder || b.order || 0))
})

// 监听courseData变化
watch(
  () => props.courseData,
  (newData) => {
    try {
      if (newData && Object.keys(newData).length > 0) {
        console.log('📥 CourseForm接收到courseData:', newData)
        console.log('🖼️ 检查封面字段:', {
          coverUrl: newData.coverUrl,
          coverImageUrl: newData.coverImageUrl,
          cover: newData.cover,
          coverImage: newData.coverImage
        })

      // 处理章节数据映射
      const processedChapters = (newData.chapters || []).map((chapter, index) => {
        console.log(`📁 处理第${index + 1}个章节:`, {
          原始数据: chapter,
          contentUrl: chapter.contentUrl,
          videoUrl: chapter.videoUrl,
          mediaUrl: chapter.mediaUrl
        })

        const videoUrl = chapter.videoUrl || chapter.mediaUrl || chapter.contentUrl || ''
        return {
          id: chapter.id,
          title: chapter.title || '',
          sortOrder: chapter.sortOrder || chapter.order || (index + 1),
          duration: chapter.duration || 0,
          // 🔧 关键修复：多字段映射音视频URL
          mediaUrl: chapter.mediaUrl || chapter.videoUrl || chapter.contentUrl || '',
          videoUrl,
          fileList: chapter.mediaUrl || chapter.videoUrl || chapter.contentUrl
            ? [{ name: getFileName(chapter.mediaUrl || chapter.videoUrl || chapter.contentUrl), url: chapter.mediaUrl || chapter.videoUrl || chapter.contentUrl }]
            : [],
          _fileList: videoUrl ? [{ name: getFileName(videoUrl), url: videoUrl }] : [],
          fileSize: chapter.fileSize || 0,
          chapterType: chapter.chapterType || 'video',
          description: chapter.description || '',
          status: chapter.status || 0
        }
      })

      Object.assign(form.value, {
        title: newData.title || '',
        description: newData.description || '',
        category: newData.category || '',
        level: newData.level || newData.difficultyLevel || 1,
        // 🔧 关键修复：多字段映射封面URL
        coverImage: newData.coverImageUrl || newData.coverImage || newData.coverUrl || newData.cover || '',
        chapters: processedChapters
      })

      coverFileList.value = form.value.coverImage
        ? [{ name: getFileName(form.value.coverImage), url: form.value.coverImage }]
        : []

      form.value.chapters.forEach((ch) => {
        ch._fileList = ch.videoUrl
          ? [{ name: getFileName(ch.videoUrl), url: ch.videoUrl }]
          : []
        ch.fileList = ch.mediaUrl
          ? [{ name: getFileName(ch.mediaUrl), url: ch.mediaUrl }]
          : []
      })

      console.log('✅ CourseForm数据处理完成:', form.value)
      console.log('🖼️ 最终封面URL:', form.value.coverImage)
      }
    } catch (err) {
      console.error('初始化课程数据失败', err)
    }
  },
  { immediate: true, deep: true }
)

// 监听文件列表变化，更新表单字段
watch(
  coverFileList,
  (list) => {
    form.value.coverImage = list[0]?.url || ''
  },
  { deep: true }
)

watch(
  chapterMediaFileList,
  (list) => {
    chapterForm.value.mediaUrl = list[0]?.url || ''
  },
  { deep: true }
)

// 章节管理方法
const addChapter = () => {
  resetChapterForm()
  chapterModalTitle.value = '添加章节'
  editingChapterIndex.value = -1
  chapterForm.value.sortOrder = form.value.chapters.length + 1
  chapterMediaFileList.value = []
  chapterModalVisible.value = true
}

const editChapter = (index) => {
  const chapter = form.value.chapters[index]
  console.log('📝 编辑章节数据:', chapter)

  Object.assign(chapterForm.value, {
    title: chapter.title || '',
    sortOrder: chapter.sortOrder || chapter.order || (index + 1),
    duration: chapter.duration || 0,
    // 🔧 关键修复：获取实际的媒体URL
    mediaUrl: getMediaUrl(chapter),
    videoUrl: getMediaUrl(chapter),
    fileSize: chapter.fileSize || 0
  })

  chapterMediaFileList.value = chapter.fileList && chapter.fileList.length > 0
    ? [...chapter.fileList]
    : (chapter.mediaUrl
        ? [{ name: getFileName(chapter.mediaUrl), url: chapter.mediaUrl }]
        : [])

  console.log('📝 章节表单数据:', chapterForm.value)
  chapterModalTitle.value = '编辑章节'
  editingChapterIndex.value = index
  chapterModalVisible.value = true
}

const removeChapter = async (index) => {
  try {
    await ElMessageBox.confirm('确定要删除这个章节吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    form.value.chapters.splice(index, 1)
    ElMessage.success('章节已删除')
  } catch {
    // 用户取消删除
  }
}

const saveChapter = async () => {
  try {
    await chapterFormRef.value.validate()
    chapterSaving.value = true

    const url = chapterMediaFileList.value[0]?.url || chapterForm.value.mediaUrl

    const chapterData = {
      ...chapterForm.value,
      mediaUrl: url,
      // 🔧 关键修复：同时设置多个字段确保兼容性
      videoUrl: url,
      contentUrl: url,
      fileList: chapterMediaFileList.value.slice(),
      id: editingChapterIndex.value >= 0 ?
        form.value.chapters[editingChapterIndex.value].id :
        Date.now().toString()
    }

    if (editingChapterIndex.value >= 0) {
      form.value.chapters[editingChapterIndex.value] = chapterData
      ElMessage.success('章节已更新')
    } else {
      form.value.chapters.push(chapterData)
      ElMessage.success('章节已添加')
    }

    closeChapterModal()
  } catch (error) {
    console.error('章节保存失败:', error)
  } finally {
    chapterSaving.value = false
  }
}

const closeChapterModal = () => {
  chapterModalVisible.value = false
  resetChapterForm()
}

const resetChapterForm = () => {
  // 释放之前的blob URL
  if (chapterForm.value.mediaUrl && chapterForm.value.mediaUrl.startsWith('blob:')) {
    window.URL.revokeObjectURL(chapterForm.value.mediaUrl)
  }

  Object.assign(chapterForm.value, {
    title: '',
    sortOrder: 1,
    duration: 0,
    mediaUrl: '',
    videoUrl: '',
    fileSize: 0
  })
  chapterMediaFileList.value = []
}

// 封面上传相关方法
const selectCoverImage = () => {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'image/*'
  input.onchange = handleCoverSelect
  input.click()
}

const handleCoverSelect = (event) => {
  const file = event.target.files[0]
  if (file) {
    if (beforeCoverUpload(file)) {
      handleCoverUpload(file)
    }
  }
}

const beforeCoverUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }

  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }

  return true
}

const handleCoverUpload = async (options) => {
  const file = options.file || options
  const formData = new FormData()
  formData.append('file', file)
  try {
    const res = await request.post('/api/v1/upload/course-cover', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    const url = res.url || res.data?.url
    if (url) {
      form.value.coverImage = url
      coverFileList.value = [{ name: file.name, url }]
      ElMessage.success('封面上传成功')
    } else {
      ElMessage.error('封面上传失败')
    }
  } catch (err) {
    console.error('封面上传失败:', err)
    ElMessage.error('封面上传失败')
  }
}

const removeCover = () => {
  form.value.coverImage = ''
  coverFileList.value = []
}

// 媒体文件上传相关方法
const beforeMediaUpload = (file) => {
  const isVideo = file.type.startsWith('video/')
  const isAudio = file.type.startsWith('audio/')

  if (!isVideo && !isAudio) {
    ElMessage.error('只能上传音频或视频文件!')
    return false
  }

  const maxSize = isVideo ? 500 * 1024 * 1024 : 200 * 1024 * 1024
  const fileTypeName = isVideo ? '视频' : '音频'
  const maxSizeText = isVideo ? '500MB' : '200MB'

  if (file.size > maxSize) {
    ElMessage.error(`${fileTypeName}文件大小不能超过 ${maxSizeText}!`)
    return false
  }

  return true
}

const handleChapterUpload = async (options) => {
  const file = options.file

  // 显示计算中状态
  chapterForm.value.duration = -1 // 用-1表示计算中
  chapterForm.value.fileSize = file.size

  // 先计算媒体时长
  const tempUrl = URL.createObjectURL(file)
  const mediaElement = file.type.startsWith('video/')
    ? document.createElement('video')
    : document.createElement('audio')

  mediaElement.preload = 'metadata'
  mediaElement.onloadedmetadata = () => {
    const durationInSeconds = Math.floor(mediaElement.duration)
    chapterForm.value.duration = durationInSeconds
    URL.revokeObjectURL(mediaElement.src)
  }
  mediaElement.onerror = () => {
    chapterForm.value.duration = 0
    URL.revokeObjectURL(mediaElement.src)
    ElMessage.warning('无法获取媒体时长，请检查文件格式')
  }
  mediaElement.src = tempUrl

  const formData = new FormData()
  formData.append('file', file)
  try {
    const token = localStorage.getItem('token')
    const res = await axios.post(
      '/api/v1/upload/chapter-resource',
      formData,
      {
        headers: {
          Authorization: `Bearer ${token}`,
          'Content-Type': 'multipart/form-data'
        },
        onUploadProgress: options.onProgress
      }
    )
    const url = res.data?.url || res.url
    if (url) {
      chapterForm.value.mediaUrl = url
      chapterForm.value.videoUrl = url
      chapterMediaFileList.value = [{ name: file.name, url }]
      if (options.onSuccess) options.onSuccess(res)
      ElMessage.success('文件上传成功')
    } else {
      ElMessage.error('文件上传失败')
    }
  } catch (err) {
    console.error('文件上传失败:', err)
    if (options.onError) options.onError(err)
    ElMessage.error('文件上传失败')
  }
}

const removeMedia = () => {
  // 释放blob URL
  if (chapterForm.value.mediaUrl && chapterForm.value.mediaUrl.startsWith('blob:')) {
    window.URL.revokeObjectURL(chapterForm.value.mediaUrl)
  }

  chapterForm.value.mediaUrl = ''
  chapterForm.value.duration = 0
  chapterForm.value.fileSize = 0
  chapterMediaFileList.value = []
}

// 工具函数
const calculateTotalDuration = () => {
  return form.value.chapters.reduce((total, chapter) => {
    const duration = chapter.duration || 0
    // 忽略计算中的章节（duration为-1）
    return duration > 0 ? total + duration : total
  }, 0)
}

// 🔧 新增：检查章节是否有媒体文件
const hasMediaFile = (chapter) => {
  const mediaUrl = getMediaUrl(chapter)
  return mediaUrl && mediaUrl.trim() !== ''
}

// 🔧 新增：获取媒体文件URL的统一方法
const getMediaUrl = (chapter) => {
  return chapter.mediaUrl || chapter.videoUrl || chapter.contentUrl || ''
}

const formatDuration = (seconds) => {
  if (!seconds || seconds === 0) return '0分钟'
  if (seconds === -1) return '计算中...'

  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const remainingSeconds = seconds % 60

  if (hours > 0) {
    return minutes > 0 ? `${hours}小时${minutes}分钟` : `${hours}小时`
  } else if (minutes > 0) {
    return remainingSeconds > 0 ? `${minutes}分钟${remainingSeconds}秒` : `${minutes}分钟`
  } else {
    return `${remainingSeconds}秒`
  }
}

const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const isVideoFile = (url) => {
  if (!url) return false
  const videoExtensions = ['.mp4', '.avi', '.mov', '.wmv', '.flv', '.webm', '.mkv']
  const audioExtensions = ['.mp3', '.wav', '.aac', '.ogg', '.m4a']

  const fileExtension = `.${url.toLowerCase().split('.').pop()}`

  // 优先检查文件扩展名
  if (videoExtensions.includes(fileExtension)) return true
  if (audioExtensions.includes(fileExtension)) return false

  // 检查URL中的类型标识
  if (url.includes('video/') || url.includes('mp4') || url.includes('avi')) return true
  if (url.includes('audio/') || url.includes('mp3') || url.includes('wav')) return false

  // 默认当作视频处理
  return true
}


// 表单提交相关方法
const handleSave = async () => {
  try {
    await formRef.value.validate()
    saving.value = true

    const courseData = {
      ...form.value,
      duration: calculateTotalDuration(),
      coverImage: coverFileList.value[0]?.url || form.value.coverImage
    }

    courseData.chapters = form.value.chapters.map((ch) => {
      const url = ch.fileList && ch.fileList.length > 0 ? ch.fileList[0].url : ch.mediaUrl
      return {
        ...ch,
        mediaUrl: url,
        videoUrl: url,
        contentUrl: url
      }
    })

    emit('save', courseData)
  } catch (error) {
    console.error('表单验证失败:', error)
  } finally {
    saving.value = false
  }
}

const handleCancel = () => {
  emit('cancel')
}
</script>

<style scoped>
.course-form-container {
  padding: 20px;
  max-width: 100%;
  box-sizing: border-box;
}

.course-form {
  max-width: 100%;
  width: 100%;
}

.form-section {
  margin-bottom: 32px;
}

.section-title {
  font-size: 16px;
  color: #303133;
  margin-bottom: 20px;
  padding-left: 12px;
  border-left: 4px solid #409eff;
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

/* 表单布局 */
.form-row {
  display: flex;
  gap: 20px;
  margin-bottom: 16px;
}

.form-col {
  flex: 1;
  min-width: 0;
}

.form-col-narrow {
  flex: 2;
}

.form-col-wide {
  flex: 1;
}

/* 课程时长显示 - 移除不需要的自定义样式 */

/* 封面上传样式 */
.cover-upload-container {
  display: flex;
  gap: 16px;
  align-items: flex-start;
  width: 100%;
}

.cover-current {
  flex: 0 0 160px;
  position: relative;
  background: #f5f7fa;
  border-radius: 6px;
  overflow: hidden;
  height: 90px;
  cursor: pointer;
  border: 1px solid #e4e7ed;
}

.cover-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #c0c4cc;
  font-size: 12px;
}

.cover-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.cover-current:hover .cover-overlay {
  opacity: 1;
}

.cover-actions {
  display: flex;
  gap: 4px;
}

.cover-upload-area {
  flex: 1;
  border: 2px dashed #dcdfe6;
  border-radius: 6px;
  padding: 20px 16px;
  text-align: center;
  background: #fafbfc;
  transition: all 0.3s;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 90px;
  min-height: 90px;
}

.cover-upload-area:hover {
  border-color: #409eff;
  background: rgba(64, 158, 255, 0.02);
}

.cover-upload-plus {
  font-size: 24px;
  color: #c0c4cc;
  margin-bottom: 4px;
  font-weight: 300;
  line-height: 1;
}

.upload-text {
  color: #606266;
  font-size: 12px;
  line-height: 1.2;
}

.upload-hint {
  color: #909399;
  font-size: 11px;
  margin-top: 6px;
  line-height: 1.3;
}

/* 章节相关样式 */
.chapters-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 12px 16px;
  background: #f8f9fa;
  border-radius: 6px;
}

.chapters-count {
  color: #666;
  font-size: 13px;
}

.empty-chapters {
  text-align: center;
  padding: 30px 20px;
}

.chapters-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.chapter-item {
  background: white;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.chapter-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transform: translateY(-1px);
}

.chapter-content {
  padding: 16px;
}

.chapter-header-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.chapter-title-section {
  flex: 1;
  min-width: 0;
}

.chapter-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

.chapter-number {
  background: #409eff;
  color: white;
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 11px;
  font-weight: 500;
  min-width: 24px;
  text-align: center;
  flex-shrink: 0;
}

.title-text {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chapter-actions {
  display: flex;
  gap: 4px;
  flex-shrink: 0;
}

.media-info {
  background: #f8f9fa;
  border-radius: 6px;
  padding: 12px;
  margin-top: 8px;
}

.media-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
  gap: 10px;
}

.media-name {
  font-weight: 500;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 6px;
  flex: 1;
  min-width: 0;
  font-size: 13px;
}

.media-type-badge {
  padding: 1px 4px;
  border-radius: 3px;
  font-size: 9px;
  font-weight: 500;
  text-transform: uppercase;
  flex-shrink: 0;
}

.media-type-video {
  background: rgba(64, 158, 255, 0.1);
  color: #409eff;
}

.media-type-audio {
  background: rgba(103, 194, 58, 0.1);
  color: #67c23a;
}

.media-duration {
  color: #409eff;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 3px;
  font-size: 11px;
  flex-shrink: 0;
}

.media-size {
  color: #909399;
  font-size: 11px;
}

.no-media {
  background: #fef0f0;
  border: 1px solid #fbc4c4;
  border-radius: 6px;
  padding: 12px;
  margin-top: 8px;
  color: #f56c6c;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
}

/* 章节编辑弹窗 */
.chapter-dialog {
  max-width: 600px;
}

.chapter-dialog .el-dialog__body {
  padding: 20px 24px;
}

.chapter-dialog .el-form-item {
  margin-bottom: 20px;
}

.chapter-dialog .el-form-item__label {
  font-weight: 500;
  color: #606266;
}

.media-upload-wrapper {
  width: 100%;
}

.media-upload {
  width: 100%;
}

.media-upload .el-upload {
  width: 100%;
}

.media-upload .el-upload-dragger {
  width: 100%;
  height: auto;
  min-height: 120px;
  padding: 20px;
  border-radius: 6px;
}

.upload-area {
  padding: 30px 20px;
  text-align: center;
  width: 100%;
}

.upload-icon {
  font-size: 28px;
  color: #c0c4cc;
  margin-bottom: 10px;
}

.upload-text {
  color: #606266;
  font-size: 14px;
  margin-bottom: 8px;
  line-height: 1.4;
}

.upload-hint {
  color: #909399;
  font-size: 12px;
  line-height: 1.4;
}

.media-preview {
  padding: 16px;
  width: 100%;
}

.media-info-preview {
  background: #f8f9fa;
  border-radius: 6px;
  padding: 14px;
}

.media-meta-preview {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  gap: 10px;
}

.media-name-preview {
  font-weight: 500;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 6px;
  flex: 1;
  min-width: 0;
  font-size: 13px;
}

.media-duration-preview {
  color: #409eff;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 3px;
  font-size: 11px;
  flex-shrink: 0;
}

.media-size-preview {
  color: #909399;
  font-size: 11px;
  margin-bottom: 8px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 32px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .cover-upload-container {
    flex-direction: column;
    gap: 10px;
  }

  .cover-current {
    flex: none;
    width: 100%;
    max-width: 200px;
  }

  .form-row {
    flex-direction: column;
    gap: 0;
  }

  .chapters-header {
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
  }

  .chapter-header-info {
    flex-direction: column;
    gap: 8px;
    align-items: flex-start;
  }

  .chapter-actions {
    justify-content: flex-start;
  }

  .media-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 6px;
  }

  .media-name {
    flex-wrap: wrap;
  }
}
</style>
