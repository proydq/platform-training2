<!-- frontend/src/components/ChapterEditModal.vue -->
<template>
  <el-dialog
    v-model="visible"
    :title="isEdit ? '编辑章节' : '添加章节'"
    width="800px"
    :close-on-click-modal="false"
    @close="handleClose"
    destroy-on-close
  >
    <div class="chapter-edit-form">
      <!-- 基本信息 -->
      <div class="form-row">
        <div class="form-col" style="flex: 2;">
          <div class="form-item">
            <label class="form-label">章节标题 <span class="required">*</span></label>
            <el-input
              v-model="form.title"
              placeholder="请输入章节标题"
              maxlength="100"
              show-word-limit
            />
          </div>
        </div>
        <div class="form-col" style="flex: 1;">
          <div class="form-item">
            <label class="form-label">排序序号 <span class="required">*</span></label>
            <el-input-number
              v-model="form.sortOrder"
              :min="1"
              :max="100"
              controls-position="right"
            />
          </div>
        </div>
      </div>

      <!-- 内容类型选择 -->
      <div class="form-item">
        <label class="form-label">内容类型 <span class="required">*</span></label>
        <div class="content-type-group">
          <div class="content-type-item" v-for="type in contentTypes" :key="type.value">
            <input
              type="radio"
              :id="`type-${type.value}`"
              :value="type.value"
              v-model="form.contentType"
              class="content-type-radio"
            />
            <label :for="`type-${type.value}`" class="content-type-label">
              <span class="content-type-icon">{{ type.icon }}</span>
              <span class="content-type-text">{{ type.label }}</span>
            </label>
          </div>
        </div>
        <div class="type-hint">{{ contentTypeHint }}</div>
      </div>

      <!-- 视频上传区域 -->
      <div class="form-item" v-if="showVideoUpload">
        <label class="form-label">视频文件 <span class="required">*</span></label>
        <div v-if="!form.videoFile" class="upload-area" @click="selectVideo">
          <div class="upload-icon">🎥</div>
          <div class="upload-text">点击选择视频文件</div>
          <div class="upload-hint">支持 MP4、AVI、MOV、WMV、FLV 格式，最大 500MB</div>
        </div>
        <div v-else class="file-preview video-preview">
          <video
            :src="form.videoUrl"
            controls
            class="preview-video"
          ></video>
          <div class="file-info">
            <div class="file-name">{{ form.videoFile.name }}</div>
            <div class="file-size">{{ formatFileSize(form.videoFile.size) }}</div>
          </div>
          <div class="file-actions">
            <el-button size="small" type="danger" @click="removeVideo">
              <el-icon><Delete /></el-icon>
              移除视频
            </el-button>
          </div>
        </div>
      </div>

      <!-- 文档上传区域 -->
      <div class="form-item" v-if="showDocumentUpload">
        <label class="form-label">文档文件 <span class="required">*</span></label>
        <div v-if="!form.documentFile" class="upload-area" @click="selectDocument">
          <div class="upload-icon">📄</div>
          <div class="upload-text">点击选择文档文件</div>
          <div class="upload-hint">支持 PDF、Word、Excel、PPT、TXT 格式，最大 50MB</div>
        </div>
        <div v-else class="file-preview document-preview">
          <div class="document-icon">{{ getDocumentIcon(form.documentFile.name) }}</div>
          <div class="file-info">
            <div class="file-name">{{ form.documentFile.name }}</div>
            <div class="file-size">{{ formatFileSize(form.documentFile.size) }}</div>
          </div>
          <div class="file-actions">
            <el-button size="small" @click="previewDocument">
              <el-icon><View /></el-icon>
              预览
            </el-button>
            <el-button size="small" type="danger" @click="removeDocument">
              <el-icon><Delete /></el-icon>
              移除
            </el-button>
          </div>
        </div>
      </div>

      <!-- 音频上传区域 -->
      <div class="form-item" v-if="showAudioUpload">
        <label class="form-label">音频文件 <span class="required">*</span></label>
        <div v-if="!form.audioFile" class="upload-area" @click="selectAudio">
          <div class="upload-icon">🎵</div>
          <div class="upload-text">点击选择音频文件</div>
          <div class="upload-hint">支持 MP3、WAV、AAC、M4A、FLAC 格式，最大 100MB</div>
        </div>
        <div v-else class="file-preview audio-preview">
          <audio
            :src="form.audioUrl"
            controls
            class="preview-audio"
          ></audio>
          <div class="file-info">
            <div class="file-name">{{ form.audioFile.name }}</div>
            <div class="file-size">{{ formatFileSize(form.audioFile.size) }}</div>
          </div>
          <div class="file-actions">
            <el-button size="small" type="danger" @click="removeAudio">
              <el-icon><Delete /></el-icon>
              移除音频
            </el-button>
          </div>
        </div>
      </div>

      <!-- 时长设置 -->
      <div class="form-item">
        <label class="form-label">预计学习时长（分钟）</label>
        <el-input-number
          v-model="form.duration"
          :min="1"
          :max="999"
          controls-position="right"
        />
        <span class="duration-hint">{{ durationHint }}</span>
      </div>

      <!-- 章节描述 -->
      <div class="form-item">
        <label class="form-label">章节描述</label>
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="4"
          placeholder="请输入章节描述，帮助学员了解本章节的主要内容"
          maxlength="500"
          show-word-limit
        />
      </div>

      <!-- 补充资料 -->
      <div class="form-item">
        <label class="form-label">补充资料</label>
        <div class="supplementary-section">
          <div class="supplementary-header">
            <el-button size="small" @click="addSupplementary">
              <el-icon><Plus /></el-icon>
              添加补充资料
            </el-button>
            <span class="supplementary-hint">最多10个文件，单个不超过50MB</span>
          </div>
          <div class="supplementary-list" v-if="form.supplementaryFiles.length > 0">
            <div
              v-for="(file, index) in form.supplementaryFiles"
              :key="index"
              class="supplementary-item"
            >
              <div class="item-icon">{{ getFileIcon(file.name) }}</div>
              <div class="item-info">
                <div class="item-name">{{ file.name }}</div>
                <div class="item-size">{{ formatFileSize(file.size) }}</div>
              </div>
              <el-button
                type="danger"
                text
                @click="removeSupplementary(index)"
                class="item-remove"
              >
                <el-icon><Close /></el-icon>
              </el-button>
            </div>
          </div>
          <el-empty
            v-else
            description="暂无补充资料"
            :image-size="60"
            class="supplementary-empty"
          />
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">
          保存章节
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete, View, Close } from '@element-plus/icons-vue'
import { uploadCourseMaterialAPI, uploadCourseVideoAPI } from '@/api/course'

// Props
const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  chapterData: {
    type: Object,
    default: null
  }
})

// Emits
const emit = defineEmits(['update:modelValue', 'save'])

// 响应式数据
const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const isEdit = computed(() => !!props.chapterData?.id)
const saving = ref(false)
const isChangingContentType = ref(false)

// 表单数据
const form = ref({
  title: '',
  sortOrder: 1,
  contentType: 'document',
  videoFile: null,
  videoUrl: '',
  documentFile: null,
  documentUrl: '',
  audioFile: null,
  audioUrl: '',
  duration: 15,
  description: '',
  supplementaryFiles: []
})

// 内容类型配置
const contentTypes = [
  { value: 'video', label: '视频课程', icon: '🎥' },
  { value: 'document', label: '文档资料', icon: '📄' },
  { value: 'audio', label: '音频课程', icon: '🎵' },
  { value: 'mixed', label: '混合内容', icon: '📦' }
]

// 计算属性
const contentTypeHint = computed(() => {
  const hints = {
    video: '上传视频文件作为主要教学内容',
    document: '上传PDF、Word等文档作为主要学习资料',
    audio: '上传音频文件，适合语音讲解类课程',
    mixed: '同时上传视频和文档，提供多样化学习体验'
  }
  return hints[form.value.contentType]
})

const showVideoUpload = computed(() =>
  ['video', 'mixed'].includes(form.value.contentType)
)

const showDocumentUpload = computed(() =>
  ['document', 'mixed'].includes(form.value.contentType)
)

const showAudioUpload = computed(() =>
  form.value.contentType === 'audio'
)

const durationHint = computed(() => {
  if (form.value.contentType === 'document') {
    return '根据文档页数估算阅读时间'
  } else if (['video', 'audio'].includes(form.value.contentType)) {
    return '将自动获取媒体文件时长'
  }
  return ''
})

// 检查是否有已上传的文件
const hasUploadedFiles = computed(() => {
  return !!(form.value.videoFile || form.value.documentFile || form.value.audioFile)
})

// 获取当前已上传的文件类型描述
const getUploadedFilesDescription = () => {
  const files = []
  if (form.value.videoFile) files.push('视频')
  if (form.value.documentFile) files.push('文档')
  if (form.value.audioFile) files.push('音频')
  return files.join('、')
}

// 方法 - 先定义 resetForm
const resetForm = () => {
  form.value = {
    title: '',
    sortOrder: 1,
    contentType: 'document',
    videoFile: null,
    videoUrl: '',
    documentFile: null,
    documentUrl: '',
    audioFile: null,
    audioUrl: '',
    duration: 15,
    description: '',
    supplementaryFiles: []
  }
}

// 监听章节数据变化 - 在 resetForm 定义之后
watch(() => props.chapterData, (newVal) => {
  if (newVal) {
    // 编辑模式，填充表单数据
    Object.assign(form.value, {
      title: newVal.title || '',
      sortOrder: newVal.sortOrder || 1,
      contentType: newVal.contentType || 'document',
      duration: newVal.duration || 15,
      description: newVal.description || '',
      supplementaryFiles: newVal.materialUrls
        ? newVal.materialUrls.split(',').filter(u => u).map(u => ({
            name: u.split('/').pop(),
            url: u,
            size: 0
          }))
        : newVal.supplementaryFiles || [],
      // 文件相关
      videoFile: newVal.videoFile || null,
      videoUrl: newVal.videoUrl || '',
      documentFile: newVal.documentFile || null,
      documentUrl: newVal.documentUrl || '',
      audioFile: newVal.audioFile || null,
      audioUrl: newVal.audioUrl || ''
    })
  } else {
    // 新增模式，重置表单
    resetForm()
  }
}, { immediate: true })

// 监听内容类型变化，清理不相关的文件
watch(() => form.value.contentType, async (newType, oldType) => {
  // 如果是初始化或者正在切换类型，不处理
  if (!oldType || isChangingContentType.value || oldType === newType) return

  // 检查是否有需要清理的文件
  let needsClearance = false
  let filesToClear = []

  switch (newType) {
    case 'video':
      if (form.value.documentFile || form.value.audioFile) {
        needsClearance = true
        if (form.value.documentFile) filesToClear.push('文档')
        if (form.value.audioFile) filesToClear.push('音频')
      }
      break
    case 'document':
      if (form.value.videoFile || form.value.audioFile) {
        needsClearance = true
        if (form.value.videoFile) filesToClear.push('视频')
        if (form.value.audioFile) filesToClear.push('音频')
      }
      break
    case 'audio':
      if (form.value.videoFile || form.value.documentFile) {
        needsClearance = true
        if (form.value.videoFile) filesToClear.push('视频')
        if (form.value.documentFile) filesToClear.push('文档')
      }
      break
    case 'mixed':
      // 混合类型不需要清理任何文件
      needsClearance = false
      break
  }

  // 如果需要清理文件，先询问用户
  if (needsClearance && filesToClear.length > 0) {
    try {
      await ElMessageBox.confirm(
        `切换内容类型将清除已上传的${filesToClear.join('、')}文件，是否继续？`,
        '提示',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )

      // 用户确认，清理文件
      cleanupFilesByType(newType)
    } catch {
      // 用户取消，恢复原来的类型
      isChangingContentType.value = true
      form.value.contentType = oldType
      setTimeout(() => {
        isChangingContentType.value = false
      }, 100)
    }
  }
})

// 根据内容类型清理文件
const cleanupFilesByType = (contentType) => {
  switch (contentType) {
    case 'video':
      // 清理文档和音频文件
      if (form.value.documentUrl && form.value.documentUrl.startsWith('blob:')) {
        URL.revokeObjectURL(form.value.documentUrl)
      }
      if (form.value.audioUrl && form.value.audioUrl.startsWith('blob:')) {
        URL.revokeObjectURL(form.value.audioUrl)
      }
      form.value.documentFile = null
      form.value.documentUrl = ''
      form.value.audioFile = null
      form.value.audioUrl = ''
      break
    case 'document':
      // 清理视频和音频文件
      if (form.value.videoUrl && form.value.videoUrl.startsWith('blob:')) {
        URL.revokeObjectURL(form.value.videoUrl)
      }
      if (form.value.audioUrl && form.value.audioUrl.startsWith('blob:')) {
        URL.revokeObjectURL(form.value.audioUrl)
      }
      form.value.videoFile = null
      form.value.videoUrl = ''
      form.value.audioFile = null
      form.value.audioUrl = ''
      break
    case 'audio':
      // 清理视频和文档文件
      if (form.value.videoUrl && form.value.videoUrl.startsWith('blob:')) {
        URL.revokeObjectURL(form.value.videoUrl)
      }
      if (form.value.documentUrl && form.value.documentUrl.startsWith('blob:')) {
        URL.revokeObjectURL(form.value.documentUrl)
      }
      form.value.videoFile = null
      form.value.videoUrl = ''
      form.value.documentFile = null
      form.value.documentUrl = ''
      break
  }
}

// 文件选择方法
const selectVideo = () => {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'video/mp4,video/avi,video/mov,video/wmv,video/flv'
  input.onchange = (e) => {
    const file = e.target.files[0]
    if (file) {
      handleVideoSelect(file)
    }
  }
  input.click()
}

const selectDocument = () => {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = '.pdf,.doc,.docx,.ppt,.pptx,.xls,.xlsx,.txt'
  input.onchange = (e) => {
    const file = e.target.files[0]
    if (file) {
      handleDocumentSelect(file)
    }
  }
  input.click()
}

const selectAudio = () => {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'audio/mp3,audio/wav,audio/aac,audio/m4a,audio/flac'
  input.onchange = (e) => {
    const file = e.target.files[0]
    if (file) {
      handleAudioSelect(file)
    }
  }
  input.click()
}

const addSupplementary = () => {
  if (form.value.supplementaryFiles.length >= 10) {
    ElMessage.warning('最多只能添加10个补充资料')
    return
  }

  const input = document.createElement('input')
  input.type = 'file'
  input.multiple = true
  input.accept = '.pdf,.doc,.docx,.ppt,.pptx,.xls,.xlsx,.txt,.zip,.rar'
  input.onchange = (e) => {
    const files = Array.from(e.target.files)
    handleSupplementarySelect(files)
  }
  input.click()
}

// 文件处理方法
const handleVideoSelect = async (file) => {
  // 验证文件大小（最大500MB）
  const maxSize = 500 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('视频文件不能超过500MB')
    return
  }

  // 验证文件类型
  const validTypes = ['video/mp4', 'video/avi', 'video/quicktime', 'video/x-ms-wmv', 'video/x-flv']
  if (!validTypes.includes(file.type) && !file.name.match(/\.(mp4|avi|mov|wmv|flv)$/i)) {
    ElMessage.error('请选择正确的视频格式文件')
    return
  }

  // 清理旧的视频URL
  if (form.value.videoUrl && form.value.videoUrl.startsWith('blob:')) {
    URL.revokeObjectURL(form.value.videoUrl)
  }

  // 设置文件并预览
  form.value.videoFile = file
  const localUrl = URL.createObjectURL(file)
  form.value.videoUrl = localUrl

  // 获取视频时长
  const video = document.createElement('video')
  video.preload = 'metadata'
  video.onloadedmetadata = () => {
    form.value.duration = Math.ceil(video.duration / 60) // 转换为分钟
    URL.revokeObjectURL(video.src)
  }
  video.src = localUrl

  try {
    const res = await uploadCourseVideoAPI(file)
    const url = res.data?.url
    if (url) {
      form.value.videoUrl = url
      URL.revokeObjectURL(localUrl)
    }
  } catch (error) {
    console.error('视频上传失败', error)
    ElMessage.error('视频上传失败')
  }

  ElMessage.success('视频文件已选择')
}

const handleDocumentSelect = async (file) => {
  // 验证文件大小（最大50MB）
  const maxSize = 50 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('文档文件不能超过50MB')
    return
  }

  // 验证文件类型
  const validExtensions = ['pdf', 'doc', 'docx', 'ppt', 'pptx', 'xls', 'xlsx', 'txt']
  const fileExtension = file.name.split('.').pop().toLowerCase()
  if (!validExtensions.includes(fileExtension)) {
    ElMessage.error('请选择正确的文档格式文件')
    return
  }

  // 清理旧的文档URL
  if (form.value.documentUrl && form.value.documentUrl.startsWith('blob:')) {
    URL.revokeObjectURL(form.value.documentUrl)
  }

  // 设置文件并预览
  form.value.documentFile = file
  const localUrl = URL.createObjectURL(file)
  form.value.documentUrl = localUrl

  try {
    const res = await uploadCourseMaterialAPI(file)
    const url = res.data?.url
    if (url) {
      form.value.documentUrl = url
      URL.revokeObjectURL(localUrl)
    }
  } catch (error) {
    console.error('文档上传失败', error)
    ElMessage.error('文档上传失败')
  }

  ElMessage.success('文档文件已选择')
}

const handleAudioSelect = async (file) => {
  // 验证文件大小（最大100MB）
  const maxSize = 100 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('音频文件不能超过100MB')
    return
  }

  // 验证文件类型
  const validTypes = ['audio/mp3', 'audio/mpeg', 'audio/wav', 'audio/aac', 'audio/m4a', 'audio/flac']
  if (!validTypes.includes(file.type) && !file.name.match(/\.(mp3|wav|aac|m4a|flac)$/i)) {
    ElMessage.error('请选择正确的音频格式文件')
    return
  }

  // 清理旧的音频URL
  if (form.value.audioUrl && form.value.audioUrl.startsWith('blob:')) {
    URL.revokeObjectURL(form.value.audioUrl)
  }

  // 设置文件并预览
  form.value.audioFile = file
  const localUrl = URL.createObjectURL(file)
  form.value.audioUrl = localUrl

  // 获取音频时长
  const audio = document.createElement('audio')
  audio.onloadedmetadata = () => {
    form.value.duration = Math.ceil(audio.duration / 60) // 转换为分钟
    URL.revokeObjectURL(audio.src)
  }
  audio.src = localUrl

  try {
    const res = await uploadCourseMaterialAPI(file)
    const url = res.data?.url
    if (url) {
      form.value.audioUrl = url
      URL.revokeObjectURL(localUrl)
    }
  } catch (error) {
    console.error('音频上传失败', error)
    ElMessage.error('音频上传失败')
  }

  ElMessage.success('音频文件已选择')
}

const handleSupplementarySelect = async (files) => {
  const remainingSlots = 10 - form.value.supplementaryFiles.length
  if (files.length > remainingSlots) {
    ElMessage.warning(`只能再添加${remainingSlots}个文件`)
    files = files.slice(0, remainingSlots)
  }

  const maxSize = 50 * 1024 * 1024 // 单个文件最大50MB
  const validFiles = []

  for (const file of files) {
    if (file.size > maxSize) {
      ElMessage.error(`文件"${file.name}"超过50MB，已跳过`)
      continue
    }
    validFiles.push(file)
  }

  for (const file of validFiles) {
    try {
      const res = await uploadCourseMaterialAPI(file)
      const url = res.data?.url
      if (url) {
        form.value.supplementaryFiles.push({ name: file.name, size: file.size, url })
      }
    } catch (error) {
      console.error('资料上传失败', error)
      ElMessage.error(`文件"${file.name}"上传失败`)
    }
  }

  if (validFiles.length > 0) {
    ElMessage.success(`成功添加${validFiles.length}个补充资料`)
  }
}

// 清理URL对象，避免内存泄漏
const cleanupUrls = () => {
  if (form.value.videoUrl && form.value.videoUrl.startsWith('blob:')) {
    URL.revokeObjectURL(form.value.videoUrl)
  }
  if (form.value.documentUrl && form.value.documentUrl.startsWith('blob:')) {
    URL.revokeObjectURL(form.value.documentUrl)
  }
  if (form.value.audioUrl && form.value.audioUrl.startsWith('blob:')) {
    URL.revokeObjectURL(form.value.audioUrl)
  }
}

const handleClose = () => {
  cleanupUrls()
  visible.value = false
  resetForm()
}

const handleSave = async () => {
  // 验证必填字段
  if (!form.value.title.trim()) {
    ElMessage.error('请输入章节标题')
    return
  }

  // 根据内容类型验证文件
  const contentType = form.value.contentType
  if (contentType === 'video' && !form.value.videoFile) {
    ElMessage.error('请上传视频文件')
    return
  }
  if (contentType === 'document' && !form.value.documentFile) {
    ElMessage.error('请上传文档文件')
    return
  }
  if (contentType === 'audio' && !form.value.audioFile) {
    ElMessage.error('请上传音频文件')
    return
  }
  if (contentType === 'mixed' && !form.value.videoFile && !form.value.documentFile) {
    ElMessage.error('请至少上传一个视频或文档文件')
    return
  }

  saving.value = true
  try {
    // 构建要保存的数据，只包含已上传的文件URL
    const chapterData = {
      id: props.chapterData?.id,
      title: form.value.title,
      sortOrder: form.value.sortOrder,
      contentType: form.value.contentType,
      duration: form.value.duration,
      description: form.value.description,
      materialUrls: form.value.supplementaryFiles
        .map((f) => f.url)
        .filter(Boolean)
        .join(',')
    }

    // 根据内容类型设置URL字段
    switch (contentType) {
      case 'video':
        chapterData.videoUrl = form.value.videoUrl
        chapterData.contentUrl = form.value.videoUrl
        break
      case 'document':
        chapterData.contentUrl = form.value.documentUrl
        break
      case 'audio':
        chapterData.contentUrl = form.value.audioUrl
        break
      case 'mixed':
        if (form.value.videoUrl) {
          chapterData.videoUrl = form.value.videoUrl
        }
        if (form.value.documentUrl) {
          chapterData.contentUrl = form.value.documentUrl
        }
        break
    }

    emit('save', chapterData)
    ElMessage.success(isEdit.value ? '章节更新成功' : '章节添加成功')
    handleClose()
  } catch (error) {
    ElMessage.error('保存失败，请重试')
  } finally {
    saving.value = false
  }
}

const removeVideo = () => {
  if (form.value.videoUrl && form.value.videoUrl.startsWith('blob:')) {
    URL.revokeObjectURL(form.value.videoUrl)
  }
  form.value.videoFile = null
  form.value.videoUrl = ''
}

const removeDocument = () => {
  if (form.value.documentUrl && form.value.documentUrl.startsWith('blob:')) {
    URL.revokeObjectURL(form.value.documentUrl)
  }
  form.value.documentFile = null
  form.value.documentUrl = ''
}

const removeAudio = () => {
  if (form.value.audioUrl && form.value.audioUrl.startsWith('blob:')) {
    URL.revokeObjectURL(form.value.audioUrl)
  }
  form.value.audioFile = null
  form.value.audioUrl = ''
}

const removeSupplementary = (index) => {
  form.value.supplementaryFiles.splice(index, 1)
}

const previewDocument = () => {
  if (form.value.documentUrl) {
    // 对于PDF文件，可以在新窗口打开
    const fileExtension = form.value.documentFile.name.split('.').pop().toLowerCase()
    if (fileExtension === 'pdf') {
      window.open(form.value.documentUrl, '_blank')
    } else {
      // 对于其他文档类型，提示下载
      const link = document.createElement('a')
      link.href = form.value.documentUrl
      link.download = form.value.documentFile.name
      link.click()
      ElMessage.info('文档已开始下载，请在下载完成后使用相应软件打开查看')
    }
  }
}

// 工具函数
const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const getDocumentIcon = (filename) => {
  const ext = filename.split('.').pop().toLowerCase()
  const iconMap = {
    pdf: '📑',
    doc: '📄',
    docx: '📄',
    xls: '📊',
    xlsx: '📊',
    ppt: '📽️',
    pptx: '📽️',
    txt: '📝'
  }
  return iconMap[ext] || '📄'
}

const getFileIcon = (filename) => {
  const ext = filename.split('.').pop().toLowerCase()
  if (['mp4', 'avi', 'mov'].includes(ext)) return '🎥'
  if (['mp3', 'wav', 'aac'].includes(ext)) return '🎵'
  if (['jpg', 'png', 'gif'].includes(ext)) return '🖼️'
  if (['zip', 'rar'].includes(ext)) return '📦'
  return getDocumentIcon(filename)
}

// 组件销毁时清理
onBeforeUnmount(() => {
  cleanupUrls()
})
</script>

<style scoped>
.chapter-edit-form {
  padding: 10px 0;
}

/* 表单布局 */
.form-row {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.form-col {
  flex: 1;
}

.form-item {
  margin-bottom: 24px;
}

.form-label {
  display: block;
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
  font-weight: 500;
}

.required {
  color: #f56c6c;
  margin-left: 2px;
}

/* 内容类型选择 */
.content-type-group {
  display: flex;
  gap: 10px;
  margin-bottom: 8px;
}

.content-type-item {
  flex: 1;
  position: relative;
}

.content-type-radio {
  position: absolute;
  opacity: 0;
}

.content-type-label {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 15px 10px;
  border: 2px solid #dcdfe6;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  text-align: center;
  background: #fff;
}

.content-type-radio:checked + .content-type-label {
  border-color: #409eff;
  background: rgba(64, 158, 255, 0.05);
  color: #409eff;
}

.content-type-label:hover {
  border-color: #c0c4cc;
}

.content-type-icon {
  font-size: 24px;
  margin-bottom: 5px;
}

.content-type-text {
  font-size: 14px;
  font-weight: 500;
}

.type-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

/* 上传区域 */
.upload-area {
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  padding: 40px;
  text-align: center;
  background: #fafafa;
  transition: all 0.3s;
  cursor: pointer;
}

.upload-area:hover {
  border-color: #409eff;
  background: #f0f7ff;
}

.upload-icon {
  font-size: 48px;
  margin-bottom: 16px;
  color: #909399;
}

.upload-text {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.upload-hint {
  font-size: 12px;
  color: #909399;
}

/* 文件预览 */
.file-preview {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
}

.video-preview .preview-video {
  width: 100%;
  max-width: 400px;
  margin: 0 auto 15px;
  border-radius: 8px;
  background: #000;
}

.audio-preview .preview-audio {
  width: 100%;
  max-width: 400px;
  margin: 0 auto 15px;
}

.document-preview {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
}

.document-icon {
  font-size: 64px;
}

.file-info {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.file-name {
  font-weight: 500;
  color: #303133;
  font-size: 16px;
}

.file-size {
  font-size: 12px;
  color: #909399;
}

.file-actions {
  display: flex;
  gap: 10px;
  justify-content: center;
  margin-top: 15px;
}

/* 时长输入 */
.duration-hint {
  margin-left: 10px;
  font-size: 12px;
  color: #909399;
}

/* 补充资料 */
.supplementary-section {
  margin-top: 10px;
}

.supplementary-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.supplementary-hint {
  font-size: 12px;
  color: #909399;
}

.supplementary-list {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 10px;
  max-height: 200px;
  overflow-y: auto;
}

.supplementary-item {
  display: flex;
  align-items: center;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 6px;
  margin-bottom: 8px;
}

.supplementary-item:last-child {
  margin-bottom: 0;
}

.item-icon {
  font-size: 24px;
  margin-right: 12px;
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-name {
  font-size: 14px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-size {
  font-size: 12px;
  color: #909399;
}

.item-remove {
  color: #f56c6c;
  padding: 4px;
}

.item-remove:hover {
  color: #f23c3c;
}

.supplementary-empty {
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
}

/* 对话框底部 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 响应式 */
@media (max-width: 768px) {
  .content-type-group {
    flex-wrap: wrap;
  }

  .content-type-item {
    flex: 0 0 48%;
  }

  .form-row {
    flex-direction: column;
  }
}
</style>
