<!-- frontend/src/components/ChapterEditModal.vue -->
<template>
  <el-dialog
    v-model="visible"
    :title="isEdit ? '编辑章节' : '添加章节'"
    width="800px"
    :close-on-click-modal="false"
    @close="handleClose"
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
        <div v-if="!form.videoFile && !form.videoUrl" class="upload-area" @click="selectVideo">
          <div class="upload-icon">📹</div>
          <div class="upload-text">点击上传视频</div>
          <div class="upload-hint">支持格式：MP4、AVI、MOV、WMV、FLV，最大500MB</div>
        </div>
        <div v-else class="file-preview video-preview">
          <video
            v-if="form.videoUrl"
            :src="form.videoUrl"
            controls
            class="preview-video"
          ></video>
          <div class="file-info">
            <div class="file-name">{{ getVideoFileName() }}</div>
            <div class="file-size" v-if="form.videoFile">{{ formatFileSize(form.videoFile.size) }}</div>
          </div>
          <div class="file-actions">
            <el-button size="small" @click="selectVideo">重新选择</el-button>
            <el-button size="small" type="danger" @click="removeVideo">删除</el-button>
          </div>
        </div>
      </div>

      <!-- 文档上传区域 -->
      <div class="form-item" v-if="showDocumentUpload">
        <label class="form-label">文档文件 <span class="required">*</span></label>
        <div v-if="!form.documentFile && !form.documentUrl" class="upload-area" @click="selectDocument">
          <div class="upload-icon">📄</div>
          <div class="upload-text">点击上传文档</div>
          <div class="upload-hint">支持格式：PDF、Word、PPT、Excel、TXT，最大50MB</div>
        </div>
        <div v-else class="file-preview document-preview">
          <div class="document-icon">{{ getDocumentIcon(getDocumentFileName()) }}</div>
          <div class="file-info">
            <div class="file-name">{{ getDocumentFileName() }}</div>
            <div class="file-size" v-if="form.documentFile">{{ formatFileSize(form.documentFile.size) }}</div>
          </div>
          <div class="file-actions">
            <el-button size="small" @click="previewDocument">预览</el-button>
            <el-button size="small" @click="selectDocument">重新选择</el-button>
            <el-button size="small" type="danger" @click="removeDocument">删除</el-button>
          </div>
        </div>
      </div>

      <!-- 音频上传区域 -->
      <div class="form-item" v-if="showAudioUpload">
        <label class="form-label">音频文件 <span class="required">*</span></label>
        <div v-if="!form.audioFile && !form.audioUrl" class="upload-area" @click="selectAudio">
          <div class="upload-icon">🎵</div>
          <div class="upload-text">点击上传音频</div>
          <div class="upload-hint">支持格式：MP3、WAV、AAC、M4A、FLAC，最大100MB</div>
        </div>
        <div v-else class="file-preview audio-preview">
          <audio
            v-if="form.audioUrl"
            :src="form.audioUrl"
            controls
            class="preview-audio"
          ></audio>
          <div class="file-info">
            <div class="file-name">{{ getAudioFileName() }}</div>
            <div class="file-size" v-if="form.audioFile">{{ formatFileSize(form.audioFile.size) }}</div>
          </div>
          <div class="file-actions">
            <el-button size="small" @click="selectAudio">重新选择</el-button>
            <el-button size="small" type="danger" @click="removeAudio">删除</el-button>
          </div>
        </div>
      </div>

      <!-- 时长输入 -->
      <div class="form-item">
        <label class="form-label">预计学习时长（分钟） <span class="required">*</span></label>
        <el-input-number
          v-model="form.duration"
          :min="1"
          :max="999"
          controls-position="right"
          style="width: 200px"
        />
        <span class="duration-hint">{{ durationHint }}</span>
      </div>

      <!-- 章节描述 -->
      <div class="form-item">
        <label class="form-label">章节描述</label>
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="3"
          placeholder="请输入章节描述，帮助学员了解本章内容"
          maxlength="500"
          show-word-limit
        />
      </div>

      <!-- 补充资料 -->
      <div class="form-item">
        <label class="form-label">补充资料</label>
        <div class="supplementary-section">
          <div class="supplementary-header">
            <el-button size="small" :icon="Plus" @click="addSupplementary">
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
import { Plus, Close } from '@element-plus/icons-vue'
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

// 监听弹窗显示状态
watch(() => visible.value, (newVal) => {
  if (newVal) {
    // 弹窗打开时
    if (props.chapterData) {
      // 编辑模式，填充表单数据
      console.log('编辑章节数据:', props.chapterData)

      // 处理补充资料
      let supplementaryFiles = []
      if (props.chapterData.materialUrls) {
        // 如果有 materialUrls 字符串，转换为文件对象数组
        const urls = props.chapterData.materialUrls.split(',').filter(u => u && u.trim())
        supplementaryFiles = urls.map(url => {
          const trimmedUrl = url.trim()
          return {
            name: trimmedUrl.split('/').pop() || '补充资料',
            url: trimmedUrl,
            size: 0 // 从已保存的数据中无法获取文件大小
          }
        })
      } else if (props.chapterData.supplementaryFiles && Array.isArray(props.chapterData.supplementaryFiles)) {
        // 如果已经是数组格式，直接使用
        supplementaryFiles = props.chapterData.supplementaryFiles
      }

      Object.assign(form.value, {
        title: props.chapterData.title || '',
        sortOrder: props.chapterData.sortOrder || 1,
        contentType: props.chapterData.contentType || 'document',
        duration: props.chapterData.duration || 15,
        description: props.chapterData.description || '',
        supplementaryFiles: supplementaryFiles,
        // 文件相关 - 关键修改：正确设置URL
        videoFile: null, // 文件对象无法从已保存的数据中恢复
        videoUrl: props.chapterData.videoUrl || props.chapterData.contentUrl || '',
        documentFile: null, // 文件对象无法从已保存的数据中恢复
        documentUrl: props.chapterData.documentUrl || props.chapterData.contentUrl || '',
        audioFile: null, // 文件对象无法从已保存的数据中恢复
        audioUrl: props.chapterData.audioUrl || props.chapterData.contentUrl || ''
      })

      // 根据内容类型确保正确的URL被设置
      if (props.chapterData.contentType === 'video' && props.chapterData.contentUrl) {
        form.value.videoUrl = props.chapterData.contentUrl
      } else if (props.chapterData.contentType === 'document' && props.chapterData.contentUrl) {
        form.value.documentUrl = props.chapterData.contentUrl
      } else if (props.chapterData.contentType === 'audio' && props.chapterData.contentUrl) {
        form.value.audioUrl = props.chapterData.contentUrl
      } else if (props.chapterData.contentType === 'mixed') {
        // 混合类型可能同时有视频和文档
        if (props.chapterData.videoUrl) {
          form.value.videoUrl = props.chapterData.videoUrl
        }
        if (props.chapterData.contentUrl) {
          form.value.documentUrl = props.chapterData.contentUrl
        }
      }
    } else {
      // 新增模式，重置表单
      resetForm()
    }
  }
})

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

      isChangingContentType.value = true
      // 用户确认，清理对应文件
      clearFilesByType(oldType, newType)
      isChangingContentType.value = false
    } catch {
      // 用户取消，恢复原类型
      form.value.contentType = oldType
    }
  }
})

// 清理旧的URL
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

// 处理关闭
const handleClose = () => {
  visible.value = false
  // 清理临时URL
  cleanupUrls()
}

// 根据内容类型清理文件
const clearFilesByType = (oldType, newType) => {
  switch (newType) {
    case 'video':
      form.value.documentFile = null
      form.value.documentUrl = ''
      form.value.audioFile = null
      form.value.audioUrl = ''
      break
    case 'document':
      form.value.videoFile = null
      form.value.videoUrl = ''
      form.value.audioFile = null
      form.value.audioUrl = ''
      break
    case 'audio':
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
  audio.preload = 'metadata'
  audio.onloadedmetadata = () => {
    form.value.duration = Math.ceil(audio.duration / 60) // 转换为分钟
    URL.revokeObjectURL(audio.src)
  }
  audio.src = localUrl

  try {
    const res = await uploadCourseVideoAPI(file) // 音频也可以用同样的接口
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

  for (const file of files) {
    // 验证文件大小
    if (file.size > 50 * 1024 * 1024) {
      ElMessage.error(`文件 ${file.name} 超过50MB限制`)
      continue
    }

    try {
      const res = await uploadCourseMaterialAPI(file)
      const url = res.data?.url
      if (url) {
        form.value.supplementaryFiles.push({
          name: file.name,
          size: file.size,
          url: url,
          file: file
        })
      }
    } catch (error) {
      console.error('补充资料上传失败', error)
      ElMessage.error(`文件 ${file.name} 上传失败`)
    }
  }
}

// 保存章节
const handleSave = async () => {
  if (!form.value.title) {
    ElMessage.error('请输入章节标题')
    return
  }

  const contentType = form.value.contentType

  // 验证文件上传 - 修改验证逻辑，只要有URL就算已上传
  if (contentType === 'video' && !form.value.videoUrl) {
    ElMessage.error('请上传视频文件')
    return
  }
  if (contentType === 'document' && !form.value.documentUrl) {
    ElMessage.error('请上传文档文件')
    return
  }
  if (contentType === 'audio' && !form.value.audioUrl) {
    ElMessage.error('请上传音频文件')
    return
  }
  if (contentType === 'mixed' && !form.value.videoUrl && !form.value.documentUrl) {
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
    const fileExtension = getDocumentFileName().split('.').pop().toLowerCase()
    if (fileExtension === 'pdf') {
      window.open(form.value.documentUrl, '_blank')
    } else {
      // 对于其他文档类型，提示下载
      const link = document.createElement('a')
      link.href = form.value.documentUrl
      link.download = getDocumentFileName()
      link.click()
      ElMessage.info('文档已开始下载，请在下载完成后使用相应软件打开查看')
    }
  }
}

// 新增：获取文件名的辅助函数
const getVideoFileName = () => {
  if (form.value.videoFile) {
    return form.value.videoFile.name
  } else if (form.value.videoUrl) {
    return form.value.videoUrl.split('/').pop() || '已上传视频'
  }
  return '视频文件'
}

const getDocumentFileName = () => {
  if (form.value.documentFile) {
    return form.value.documentFile.name
  } else if (form.value.documentUrl) {
    return form.value.documentUrl.split('/').pop() || '已上传文档'
  }
  return '文档文件'
}

const getAudioFileName = () => {
  if (form.value.audioFile) {
    return form.value.audioFile.name
  } else if (form.value.audioUrl) {
    return form.value.audioUrl.split('/').pop() || '已上传音频'
  }
  return '音频文件'
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
