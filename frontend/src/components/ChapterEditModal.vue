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
          <div class="upload-text">点击或拖拽视频文件到此处</div>
          <div class="upload-hint">支持 MP4、AVI、MOV 等格式，最大 500MB</div>
        </div>
        <div v-else class="file-preview video-preview">
          <video :src="form.videoUrl" controls class="preview-video"></video>
          <div class="file-info">
            <div class="file-name">{{ form.videoFile.name }}</div>
            <div class="file-size">{{ formatFileSize(form.videoFile.size) }}</div>
          </div>
          <div class="file-actions">
            <el-button size="small" @click="removeVideo">
              <el-icon><Delete /></el-icon>
              移除
            </el-button>
          </div>
        </div>
      </div>

      <!-- 文档上传区域 -->
      <div class="form-item" v-if="showDocumentUpload">
        <label class="form-label">文档文件 <span class="required">*</span></label>
        <div v-if="!form.documentFile" class="upload-area" @click="selectDocument">
          <div class="upload-icon">📄</div>
          <div class="upload-text">点击或拖拽文档文件到此处</div>
          <div class="upload-hint">支持 PDF、Word、PPT、Excel 等格式，最大 50MB</div>
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
          <div class="upload-text">点击或拖拽音频文件到此处</div>
          <div class="upload-hint">支持 MP3、WAV、AAC 等格式，最大 200MB</div>
        </div>
        <div v-else class="file-preview audio-preview">
          <audio :src="form.audioUrl" controls class="preview-audio"></audio>
          <div class="file-info">
            <div class="file-name">{{ form.audioFile.name }}</div>
            <div class="file-size">{{ formatFileSize(form.audioFile.size) }}</div>
          </div>
          <div class="file-actions">
            <el-button size="small" type="danger" @click="removeAudio">
              <el-icon><Delete /></el-icon>
              移除
            </el-button>
          </div>
        </div>
      </div>

      <!-- 章节时长 -->
      <div class="form-item">
        <label class="form-label">章节时长（分钟）</label>
        <el-input-number
          v-model="form.duration"
          :min="0"
          :max="999"
          controls-position="right"
          style="width: 200px;"
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
          placeholder="请输入章节描述，帮助学员了解本节内容"
          maxlength="500"
          show-word-limit
        />
      </div>

      <!-- 补充资料 -->
      <div class="form-item">
        <label class="form-label">补充资料</label>
        <div class="supplementary-section">
          <div class="supplementary-header">
            <el-button size="small" type="primary" @click="addSupplementary">
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
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Delete, View, Close } from '@element-plus/icons-vue'

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
      supplementaryFiles: newVal.supplementaryFiles || [],
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

const handleClose = () => {
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
    const chapterData = {
      ...form.value,
      id: props.chapterData?.id
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

// 文件选择方法
const selectVideo = () => {
  // 实际实现中应调用文件选择器
  ElMessage.info('选择视频文件功能待实现')
}

const selectDocument = () => {
  ElMessage.info('选择文档文件功能待实现')
}

const selectAudio = () => {
  ElMessage.info('选择音频文件功能待实现')
}

const addSupplementary = () => {
  if (form.value.supplementaryFiles.length >= 10) {
    ElMessage.warning('最多只能添加10个补充资料')
    return
  }
  ElMessage.info('添加补充资料功能待实现')
}

const removeVideo = () => {
  form.value.videoFile = null
  form.value.videoUrl = ''
}

const removeDocument = () => {
  form.value.documentFile = null
  form.value.documentUrl = ''
}

const removeAudio = () => {
  form.value.audioFile = null
  form.value.audioUrl = ''
}

const removeSupplementary = (index) => {
  form.value.supplementaryFiles.splice(index, 1)
}

const previewDocument = () => {
  ElMessage.info('文档预览功能待实现')
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
  return getDocumentIcon(filename)
}
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
