<template>
  <div class="course-form-container">
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <!-- 基本信息 -->
      <div class="form-section">
        <h3 class="section-title">📋 基本信息</h3>

        <el-form-item label="课程标题" prop="title">
          <el-input
            v-model="form.title"
            placeholder="请输入课程标题"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="课程分类" prop="category">
              <el-select v-model="form.category" placeholder="请选择课程分类">
                <el-option
                  v-for="item in courseCategories"
                  :key="item"
                  :label="item"
                  :value="item"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="难度等级" prop="difficulty">
              <el-select v-model="form.difficulty" placeholder="请选择难度等级">
                <el-option label="入门" value="beginner" />
                <el-option label="进阶" value="intermediate" />
                <el-option label="高级" value="advanced" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="课程封面" prop="coverImage">
          <div
            class="cover-upload"
            :style="{ backgroundImage: form.coverImage ? `url(${form.coverImage})` : 'none' }"
            @click="triggerCoverUpload"
          >
            <div v-if="!form.coverImage" class="upload-placeholder">
              <div class="cover-upload-plus">+</div>
              <div class="upload-text">选择封面</div>
            </div>
          </div>

          <el-upload
            ref="coverUploadRef"
            :file-list="coverFileList"
            :show-file-list="false"
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
          <el-button type="primary" @click="showChapterModal()">
            <el-icon><Plus /></el-icon>
            添加章节
          </el-button>
        </div>

        <div v-if="form.chapters.length === 0" class="empty-chapters">
          <el-empty description="暂无章节，点击上方按钮添加章节" :image-size="80" />
        </div>

        <draggable
          v-else
          v-model="form.chapters"
          class="chapters-list"
          item-key="id"
          handle=".drag-handle"
          @change="onChapterDragChange"
        >
          <template #item="{ element: chapter, index }">
            <div class="chapter-card">
              <div class="drag-handle">
                <el-icon><Rank /></el-icon>
              </div>

              <div class="chapter-content">
                <div class="chapter-header">
                  <div class="chapter-info">
                    <div class="chapter-title">
                      <span class="chapter-number">第{{ index + 1 }}章</span>
                      <span class="chapter-name">{{ chapter.title }}</span>
                    </div>
                    <!-- 添加内容类型标识 -->
                    <div class="chapter-meta">
                      <span class="content-type-tag">
                        <span>{{ getContentTypeIcon(chapter.contentType) }}</span>
                        <span>{{ getContentTypeText(chapter.contentType) }}</span>
                      </span>
                      <span class="chapter-duration">
                        <el-icon><Clock /></el-icon>
                        {{ chapter.duration || 0 }} 分钟
                      </span>
                      <!-- 如果有补充资料，显示数量 -->
                      <span v-if="chapter.supplementaryFiles?.length > 0" class="supplementary-count">
                        <el-icon><Document /></el-icon>
                        {{ chapter.supplementaryFiles.length }} 个补充资料
                      </span>
                    </div>
                  </div>
                  <div class="chapter-actions">
                    <el-button size="small" @click="showChapterModal(chapter, index)">
                      <el-icon><Edit /></el-icon>
                      编辑
                    </el-button>
                    <el-button size="small" type="danger" @click="deleteChapter(index)">
                      <el-icon><Delete /></el-icon>
                      删除
                    </el-button>
                  </div>
                </div>
                <!-- 如果有描述，显示描述 -->
                <div v-if="chapter.description" class="chapter-description">
                  {{ chapter.description }}
                </div>
              </div>
            </div>
          </template>
        </draggable>
      </div>
    </el-form>

    <!-- 底部操作按钮 -->
    <div class="form-actions">
      <el-button @click="handleCancel">取消</el-button>
      <el-button type="primary" @click="handleSave" :loading="saving">
        {{ isEditing ? '更新课程' : '创建课程' }}
      </el-button>
    </div>

    <!-- 章节编辑模态框 -->
    <ChapterEditModal
      v-model="chapterModalVisible"
      :chapter-data="editingChapter"
      @save="handleChapterSave"
    />
  </div>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Clock, Rank, Document } from '@element-plus/icons-vue'
import draggable from 'vuedraggable'
import ChapterEditModal from './ChapterEditModal.vue'
import axios from 'axios'
import { getCourseDetailAPI } from '@/api/course'

// Props
const props = defineProps({
  courseData: {
    type: Object,
    default: null
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
const coverUploadRef = ref()
const hiddenCoverTrigger = ref()

// 响应式数据
const saving = ref(false)
const coverFileList = ref([])
const chapterModalVisible = ref(false)
const editingChapter = ref(null)
const editingChapterIndex = ref(-1)

// 表单数据
const form = reactive({
  title: '',
  category: '',
  difficulty: 'beginner',
  coverImage: '',
  description: '',
  chapters: [],
  isRequired: false,
  tags: []
})

// 表单验证规则
const rules = {
  title: [
    { required: true, message: '请输入课程标题', trigger: 'blur' },
    { min: 2, max: 100, message: '标题长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择课程分类', trigger: 'change' }
  ],
  difficulty: [
    { required: true, message: '请选择难度等级', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入课程描述', trigger: 'blur' },
    { min: 10, max: 500, message: '描述长度在 10 到 500 个字符', trigger: 'blur' }
  ]
}

// 配置数据
const courseCategories = ['技术培训', '产品培训', '安全培训', '管理培训', '新员工培训']
// 监听课程数据变化
watch(
  () => props.courseData,
  async (newVal) => {
    if (newVal) {
      const cover =
        newVal.coverImage || newVal.coverImageUrl || newVal.coverUrl || ''

      Object.assign(form, {
        title: newVal.title || '',
        category: newVal.category || '',
        difficulty: newVal.difficulty || 'beginner',
        coverImage: cover,
        description: newVal.description || '',
        chapters: newVal.chapters || [],
        isRequired: newVal.isRequired || false,
        tags: newVal.tags || []
      })

      if (newVal.coverImage || newVal.coverImageUrl || newVal.coverUrl) {
        const coverUrl =
          newVal.coverImage || newVal.coverImageUrl || newVal.coverUrl
        coverFileList.value = [
          {
            name: coverUrl.split('/').pop(),
            url: coverUrl
          }
        ]
      } else if (props.isEditing && newVal.id) {
        try {
          const res = await getCourseDetailAPI(newVal.id)
          const url = res.data?.coverImageUrl || res.data?.coverUrl
          if (url) {
            form.coverImage = url
            coverFileList.value = [
              {
                name: url.split('/').pop(),
                url
              }
            ]
          } else {
            coverFileList.value = []
          }
        } catch (error) {
          console.error('获取课程封面失败', error)
          coverFileList.value = []
        }
      } else {
        coverFileList.value = []
      }
    }
  },
  { immediate: true, deep: true }
)

// 方法
const triggerCoverUpload = () => {
  if (hiddenCoverTrigger.value) {
    hiddenCoverTrigger.value.click()
  }
}

const beforeCoverUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件！')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB！')
    return false
  }
  return true
}

const handleCoverUpload = async ({ file, onSuccess, onError }) => {
  const formData = new FormData()
  formData.append('file', file)
  try {
    const token = localStorage.getItem('token')
    const res = await axios.post('/api/v1/upload/course-cover', formData, {
      headers: {
        Authorization: `Bearer ${token}`,
        'Content-Type': 'multipart/form-data'
      }
    })
    if (res.data?.url) {
      form.coverImage = res.data.url
      coverFileList.value = [
        {
          name: file.name,
          url: res.data.url
        }
      ]
      onSuccess(res.data, file)
    } else {
      onError()
    }
  } catch (e) {
    onError()
  }
}

// 章节相关方法
const showChapterModal = (chapter = null, index = -1) => {
  editingChapter.value = chapter
  editingChapterIndex.value = index
  chapterModalVisible.value = true
}

const handleChapterSave = (chapterData) => {
  if (editingChapterIndex.value >= 0) {
    // 编辑现有章节
    form.chapters[editingChapterIndex.value] = {
      ...form.chapters[editingChapterIndex.value],
      ...chapterData
    }
  } else {
    // 添加新章节
    const newChapter = {
      id: Date.now(), // 临时ID，实际保存时由后端生成
      ...chapterData,
      createdAt: new Date().toISOString()
    }
    form.chapters.push(newChapter)
  }

  // 重新排序章节
  form.chapters.sort((a, b) => a.sortOrder - b.sortOrder)

  // 关闭模态框
  chapterModalVisible.value = false
  editingChapter.value = null
  editingChapterIndex.value = -1
}

const deleteChapter = async (index) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这个章节吗？',
      '删除确认',
      {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }
    )

    form.chapters.splice(index, 1)
    ElMessage.success('章节已删除')
  } catch {
    // 用户取消
  }
}

const onChapterDragChange = () => {
  // 更新章节排序
  form.chapters.forEach((chapter, index) => {
    chapter.sortOrder = index + 1
  })
}

// 工具方法
const calculateTotalDuration = () => {
  return form.chapters.reduce((total, chapter) => {
    return total + (chapter.duration || 0)
  }, 0)
}

const formatDuration = (minutes) => {
  if (!minutes) return '0 分钟'
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  if (hours > 0) {
    return mins > 0 ? `${hours} 小时 ${mins} 分钟` : `${hours} 小时`
  }
  return `${mins} 分钟`
}

const getContentTypeIcon = (contentType) => {
  const icons = {
    video: '🎥',
    document: '📄',
    audio: '🎵',
    mixed: '📦'
  }
  return icons[contentType] || '📄'
}

const getContentTypeText = (contentType) => {
  const texts = {
    video: '视频课程',
    document: '文档资料',
    audio: '音频课程',
    mixed: '混合内容'
  }
  return texts[contentType] || '文档资料'
}

// 表单操作
const handleSave = async () => {
  try {
    await formRef.value.validate()

    if (form.chapters.length === 0) {
      ElMessage.warning('请至少添加一个章节')
      return
    }

    saving.value = true

    // 准备保存的数据
    const saveData = {
      ...form,
      totalDuration: calculateTotalDuration()
    }

    emit('save', saveData)
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
  max-width: 1000px;
  margin: 0 auto;
}

.form-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

/* 封面上传 */
.cover-upload {
  width: 200px;
  height: 150px;
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  background-size: cover;
  background-position: center;
  transition: all 0.3s;
}

.cover-upload:hover {
  border-color: #409eff;
}

.upload-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #fafafa;
}

.cover-upload-plus {
  font-size: 28px;
  color: #8c939d;
  margin-bottom: 8px;
}

.upload-text {
  font-size: 14px;
  color: #8c939d;
}

.upload-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

/* 章节管理 */
.chapters-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.chapters-count {
  font-size: 14px;
  color: #606266;
}

.empty-chapters {
  padding: 40px 0;
  text-align: center;
}

.chapters-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.chapter-card {
  display: flex;
  background: #f5f7fa;
  border-radius: 8px;
  padding: 16px;
  transition: all 0.3s;
}

.chapter-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.drag-handle {
  display: flex;
  align-items: center;
  padding-right: 12px;
  cursor: move;
  color: #909399;
}

.chapter-content {
  flex: 1;
}

.chapter-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.chapter-info {
  flex: 1;
}

.chapter-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.chapter-number {
  font-size: 12px;
  color: #909399;
}

.chapter-name {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.chapter-meta {
  display: flex;
  align-items: center;
  font-size: 13px;
  color: #909399;
}

.content-type-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 12px;
  background: rgba(64, 158, 255, 0.1);
  color: #409eff;
  border-radius: 16px;
  font-size: 12px;
  margin-right: 12px;
}

.chapter-duration {
  display: flex;
  align-items: center;
  gap: 4px;
}

.supplementary-count {
  margin-left: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.chapter-description {
  margin-top: 12px;
  padding: 12px;
  background: white;
  border-radius: 6px;
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
}

.chapter-actions {
  display: flex;
  gap: 8px;
}

/* 表单操作 */
.form-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #ebeef5;
}
</style>
