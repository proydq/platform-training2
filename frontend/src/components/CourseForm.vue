<template>
  <div class="course-form">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" size="default">

      <!-- 基本信息 -->
      <div class="form-section">
        <h4>📝 基本信息</h4>

        <el-form-item label="课程名称" prop="title">
          <el-input
            v-model="form.title"
            placeholder="请输入课程名称"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="课程分类" prop="category">
              <el-select
                v-model="form.category"
                placeholder="请选择课程分类"
                style="width: 100%"
              >
                <el-option
                  v-for="category in courseCategories"
                  :key="category"
                  :label="category"
                  :value="category"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="难度级别" prop="level">
              <el-select
                v-model="form.level"
                placeholder="请选择难度级别"
                style="width: 100%"
              >
                <el-option
                  v-for="level in difficultyLevels"
                  :key="level.value"
                  :label="level.label"
                  :value="level.label"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="课程时长" prop="duration">
              <el-input-number
                v-model="form.duration"
                :min="1"
                :max="10000"
                placeholder="分钟"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="课程描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请输入课程描述，详细介绍课程内容和学习目标"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="必修课程">
          <el-switch v-model="form.isRequired" active-text="是" inactive-text="否" />
        </el-form-item>
      </div>

      <!-- 课程资源 -->
      <div class="form-section">
        <h4>📁 课程资源</h4>

        <!-- 课程封面 -->
        <el-form-item label="课程封面">
          <div class="upload-wrapper">
            <el-upload
              ref="coverUploadRef"
              :file-list="fileListState.cover"
              :http-request="handleCoverUpload"
              :on-remove="handleCoverRemove"
              :before-upload="beforeCoverUpload"
              :limit="1"
              accept="image/*"
              list-type="picture-card"
              class="course-cover-upload"
            >
              <el-icon v-if="!uploading"><Plus /></el-icon>
              <div v-else class="upload-progress">
                <el-progress type="circle" :percentage="uploadProgress" :width="50" />
              </div>
            </el-upload>
            <div class="upload-tip">
              <el-text type="info" size="small">
                支持 JPG、PNG、GIF 格式，文件大小不超过 5MB
              </el-text>
            </div>
          </div>
        </el-form-item>

        <!-- 教学资料 -->
        <el-form-item label="教学资料">
          <div class="upload-wrapper">
            <el-upload
              ref="materialUploadRef"
              :file-list="fileListState.materials"
              :http-request="handleMaterialUpload"
              :on-remove="handleMaterialRemove"
              :before-upload="beforeMaterialUpload"
              multiple
              :limit="10"
              accept=".pdf,.doc,.docx,.ppt,.pptx,.xls,.xlsx,.txt,.zip,.rar"
              list-type="text"
              class="material-upload"
            >
              <el-button type="primary" :icon="Upload" :loading="uploading">
                上传文档
              </el-button>
            </el-upload>
            <div class="upload-tip">
              <el-text type="info" size="small">
                支持 PDF、Word、Excel、PPT、TXT、ZIP、RAR 格式，单个文件不超过 50MB，最多上传10个文件
              </el-text>
            </div>
          </div>
        </el-form-item>
      </div>

      <!-- 课程章节 -->
      <div class="form-section">
        <h4>📚 课程章节</h4>

        <div class="chapter-header">
          <el-button type="primary" size="small" @click="addChapter" :icon="Plus">
            添加章节
          </el-button>
          <el-text type="info" size="small">
            {{ form.chapters.length }} 个章节
          </el-text>
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
              <div class="chapter-info">
                <div class="chapter-title">
                  <span class="chapter-number">{{ index + 1 }}.</span>
                  <span class="title-text">{{ chapter.title || '未命名章节' }}</span>
                  <el-tag :type="getChapterTypeTagType(chapter.chapterType || 'document')" size="small">
                    {{ getChapterTypeText(chapter.chapterType || 'document') }}
                  </el-tag>
                </div>
                <div class="chapter-meta">
                  <span>时长: {{ chapter.duration || 0 }}分钟</span>
                  <span v-if="chapter.description">{{ chapter.description }}</span>
                </div>
              </div>
              <div class="chapter-actions">
                <el-button size="small" @click="editChapter(index)" :icon="Edit">
                  编辑
                </el-button>
                <el-button
                  size="small"
                  @click="moveChapterUp(index)"
                  :disabled="index === 0"
                  :icon="ArrowUp"
                />
                <el-button
                  size="small"
                  @click="moveChapterDown(index)"
                  :disabled="index === sortedChapters.length - 1"
                  :icon="ArrowDown"
                />
                <el-button
                  size="small"
                  type="danger"
                  @click="removeChapter(index)"
                  :icon="Delete"
                >
                  删除
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-form>

    <!-- 操作按钮 -->
    <div class="form-actions">
      <el-button @click="handleCancel">取消</el-button>
      <el-button type="primary" @click="handleSave" :loading="saving">
        {{ props.isEditing ? '保存修改' : '创建课程' }}
      </el-button>
    </div>

    <!-- 章节编辑弹窗 -->
    <el-dialog
      v-model="chapterModalVisible"
      :title="chapterModalTitle"
      width="60%"
      :close-on-click-modal="false"
    >
      <ChapterForm
        ref="chapterFormRef"
        :chapter-data="editingChapter"
        :chapter-index="editingChapterIndex"
        :available-materials="fileListState.materials"
        @save="handleChapterSave"
        @cancel="closeChapterModal"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Upload, Edit, Delete, ArrowUp, ArrowDown } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useFileUpload } from '@/composables/useFileUpload'
import ChapterForm from './ChapterForm.vue'

// Props & Emits
const props = defineProps({
  courseData: {
    type: Object,
    default: () => ({}),
  },
  isEditing: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['save', 'cancel'])

// 数据和状态
const userStore = useUserStore()
const formRef = ref()
const saving = ref(false)

// 使用文件上传composable
const {
  uploading,
  uploadProgress,
  fileListState,
  handleCoverUpload,
  handleMaterialUpload,
  handleCoverRemove,
  handleMaterialRemove,
  validateImageFile,
  validateDocumentFile,
  setFileList,
  clearAllFiles,
} = useFileUpload()

// 表单数据
const form = reactive({
  id: '',
  title: '',
  description: '',
  category: '',
  level: '',
  duration: 0,
  isRequired: false,
  coverImage: '',
  materials: [],
  chapters: [],
})

// 章节编辑
const chapterModalVisible = ref(false)
const chapterModalTitle = ref('添加章节')
const editingChapter = ref(null)
const editingChapterIndex = ref(-1)

// 🔧 配置数据
const courseCategories = ['技术培训', '产品培训', '安全培训', '管理培训', '营销培训']

const difficultyLevels = [
  { label: '入门级', value: 1 },
  { label: '初级', value: 2 },
  { label: '中级', value: 3 },
  { label: '高级', value: 4 },
  { label: '专家级', value: 5 },
]

// 🔧 章节类型配置
const chapterTypes = [
  { label: '视频课程', value: 'video' },
  { label: '文档资料', value: 'document' },
  { label: '音频课程', value: 'audio' },
  { label: '测验考试', value: 'quiz' }
]

// 表单验证规则
const rules = {
  title: [
    { required: true, message: '请输入课程名称', trigger: 'blur' },
    { min: 2, max: 100, message: '课程名称长度在 2 到 100 个字符', trigger: 'blur' },
  ],
  description: [
    { required: true, message: '请输入课程描述', trigger: 'blur' },
    { min: 10, max: 500, message: '课程描述长度在 10 到 500 个字符', trigger: 'blur' },
  ],
  category: [{ required: true, message: '请选择课程分类', trigger: 'change' }],
  level: [{ required: true, message: '请选择难度级别', trigger: 'change' }],
  duration: [
    { required: true, message: '请输入课程时长', trigger: 'blur' },
    { type: 'number', min: 1, message: '课程时长必须大于0', trigger: 'blur' },
  ],
}

// 计算属性
const formData = computed(() => {
  return {
    ...form,
    coverImage: fileListState.cover[0]?.url || '',
    materials: fileListState.materials.map((file) => file.url),
  }
})

const sortedChapters = computed(() => {
  return [...form.chapters].sort((a, b) => (a.order || 0) - (b.order || 0))
})

const getDifficultyLevelText = (level) => {
  const levelMap = {
    1: '入门级',
    2: '初级',
    3: '中级',
    4: '高级',
    5: '专家级',
  }
  return levelMap[level] || '入门级'
}

// 🔧 章节类型显示函数
const getChapterTypeText = (type) => {
  const typeMap = {
    video: '视频',
    document: '文档',
    audio: '音频',
    quiz: '测验'
  }
  return typeMap[type] || '文档'
}

const getChapterTypeTagType = (type) => {
  const tagTypeMap = {
    video: 'primary',
    document: '',
    audio: 'warning',
    quiz: 'success'
  }
  return tagTypeMap[type] || ''
}

// 🔧 修复：初始化表单数据
const initFormData = (data) => {
  console.log('🏗️ 初始化表单数据，原始数据:', data)

  Object.assign(form, {
    id: data.id || '',
    title: data.title || '',
    description: data.description || '',
    category: data.category || '',
    level: data.level || getDifficultyLevelText(data.difficultyLevel),
    duration: data.duration || data.estimatedDuration || 0,
    isRequired: data.isRequired || false,
    chapters: data.chapters || [],
  })

  // 处理封面图片
  const coverImageUrl = data.coverImage || data.coverImageUrl
  setFileList(
    'cover',
    coverImageUrl
      ? [{ name: 'cover', url: coverImageUrl, uid: Date.now() }]
      : []
  )

  // 处理学习资料
  let materialsList = []
  if (data.materials && Array.isArray(data.materials) && data.materials.length > 0) {
    materialsList = data.materials.map((material, index) => ({
      name: material.name || material.originalName || `教学资料${index + 1}`,
      url: material.url || material,
      uid: Date.now() + index,
      originalName: material.originalName || material.name,
    }))
  } else if (data.materialUrls && typeof data.materialUrls === 'string') {
    const urls = data.materialUrls.split(',').filter((url) => url && url.trim())
    materialsList = urls.map((url, index) => ({
      name: `教学资料${index + 1}`,
      url: url.trim(),
      uid: Date.now() + index,
    }))
  }

  setFileList('materials', materialsList)

  console.log('📊 章节数据:', data.chapters)
  console.log('✅ 数据初始化完成')
}

// 监听器
watch(
  () => props.courseData,
  (newData) => {
    console.log('📨 CourseForm 接收到数据:', newData)
    if (newData && Object.keys(newData).length > 0) {
      console.log('🔄 开始初始化表单数据')
      initFormData(newData)
    }
  },
  { immediate: true }
)

// 生命周期
onMounted(() => {
  console.log('🚀 CourseForm组件已挂载')
})

const beforeCoverUpload = (file) => {
  return validateImageFile(file)
}

const beforeMaterialUpload = (file) => {
  return validateDocumentFile(file)
}

// 🔧 修复：章节管理 - 使用原来的 ChapterForm 组件结构
const addChapter = () => {
  chapterModalTitle.value = '添加章节'
  editingChapter.value = {
    id: '',
    title: '',
    description: '',
    chapterType: 'document', // 🔧 添加默认类型
    duration: 0,
    order: form.chapters.length + 1,
    content: '',
    videoUrl: '',
    contentUrl: '',
    requirements: '',
    learningObjectives: '',
    materialUrls: '',
    videoUrls: ''
  }
  editingChapterIndex.value = -1
  chapterModalVisible.value = true
}

const editChapter = (index) => {
  chapterModalTitle.value = '编辑章节'
  const originalIndex = getOriginalIndex(sortedChapters.value[index].id)
  editingChapter.value = { ...form.chapters[originalIndex] }
  editingChapterIndex.value = originalIndex
  chapterModalVisible.value = true
}

// 🔧 使用原来的 handleChapterSave 函数名
const handleChapterSave = (chapterData) => {
  console.log('📝 保存章节数据:', chapterData)

  if (editingChapterIndex.value === -1) {
    // 新增章节
    chapterData.id = Date.now().toString()
    form.chapters.push(chapterData)
    ElMessage.success('章节添加成功')
  } else {
    // 更新章节
    Object.assign(form.chapters[editingChapterIndex.value], chapterData)
    ElMessage.success('章节更新成功')
  }
  closeChapterModal()
}

const removeChapter = (index) => {
  const originalIndex = getOriginalIndex(sortedChapters.value[index].id)
  form.chapters.splice(originalIndex, 1)
  updateChapterOrder()
}

const moveChapterUp = (index) => {
  if (index === 0) return
  const currentChapter = sortedChapters.value[index]
  const prevChapter = sortedChapters.value[index - 1]

  const currentOriginalIndex = getOriginalIndex(currentChapter.id)
  const prevOriginalIndex = getOriginalIndex(prevChapter.id)

  const tempOrder = form.chapters[currentOriginalIndex].order
  form.chapters[currentOriginalIndex].order = form.chapters[prevOriginalIndex].order
  form.chapters[prevOriginalIndex].order = tempOrder
}

const moveChapterDown = (index) => {
  if (index === sortedChapters.value.length - 1) return
  const currentChapter = sortedChapters.value[index]
  const nextChapter = sortedChapters.value[index + 1]

  const currentOriginalIndex = getOriginalIndex(currentChapter.id)
  const nextOriginalIndex = getOriginalIndex(nextChapter.id)

  const tempOrder = form.chapters[currentOriginalIndex].order
  form.chapters[currentOriginalIndex].order = form.chapters[nextOriginalIndex].order
  form.chapters[nextOriginalIndex].order = tempOrder
}

const getOriginalIndex = (chapterId) => {
  return form.chapters.findIndex(chapter => chapter.id === chapterId)
}

const updateChapterOrder = () => {
  form.chapters.forEach((chapter, index) => {
    chapter.order = index + 1
  })
}

const closeChapterModal = () => {
  chapterModalVisible.value = false
  editingChapter.value = null
  editingChapterIndex.value = -1
}

// 🔧 完整修复后的表单保存函数
const handleSave = async () => {
  if (saving.value) return

  try {
    saving.value = true
    console.log('📝 开始表单验证，当前表单数据:', form)

    // 1. 基础表单验证
    const isValid = await formRef.value.validate().catch((errors) => {
      console.error('表单验证失败:', errors)
      ElMessage.error('请完善必填信息')
      return false
    })

    if (!isValid) {
      console.log('❌ 表单验证未通过')
      return
    }

    console.log('✅ 基础表单验证通过')

    // 2. 🔧 章节数据验证
    if (form.chapters && form.chapters.length > 0) {
      console.log('🔍 验证章节数据...')

      for (let i = 0; i < form.chapters.length; i++) {
        const chapter = form.chapters[i]

        // 验证必填字段
        if (!chapter.title?.trim()) {
          ElMessage.error(`第 ${i + 1} 个章节的标题不能为空`)
          return
        }

        if (!chapter.chapterType) {
          ElMessage.error(`第 ${i + 1} 个章节的类型不能为空`)
          return
        }

        if (!chapter.order || chapter.order < 1) {
          ElMessage.error(`第 ${i + 1} 个章节的排序序号无效`)
          return
        }

        // 🔧 确保 chapterType 是有效值
        const validTypes = ['video', 'document', 'audio', 'quiz']
        if (!validTypes.includes(chapter.chapterType)) {
          console.warn(`章节 ${i + 1} 的类型 "${chapter.chapterType}" 无效，设置为默认类型`)
          form.chapters[i].chapterType = 'document'
        }

        console.log(`✅ 章节 ${i + 1} 验证通过:`, {
          title: chapter.title,
          chapterType: chapter.chapterType,
          order: chapter.order
        })
      }

      console.log('✅ 所有章节验证通过')
    }

    // 3. 🔧 准备提交数据
    const submitData = {
      ...form,

      // 基础字段验证
      title: form.title?.trim(),
      description: form.description?.trim(),
      category: form.category?.trim(),

      // 🔧 移除 instructorId，后端会自动从token设置
      // instructorId: instructorId, // 删除这行

      // 数值字段
      price: Number(form.price) || 0,
      duration: Number(form.duration) || 0,

      // 文件相关
      coverImage: fileListState.cover[0]?.url || '',
      materials: fileListState.materials.map((file) => ({
        name: file.name || file.originalName,
        url: file.url,
        originalName: file.originalName || file.name,
      })),
      materialUrls: fileListState.materials.map((file) => file.url).join(','),
    }

    // 4. 🔧 最终验证必填字段
    const requiredFields = {
      title: submitData.title,
      description: submitData.description,
      category: submitData.category
    }

    const missingFields = Object.entries(requiredFields)
      .filter(([key, value]) => !value)
      .map(([key]) => key)

    if (missingFields.length > 0) {
      console.error('❌ 缺少必填字段:', missingFields)
      ElMessage.error(`缺少必填字段: ${missingFields.join(', ')}`)
      return
    }

    console.log('📤 最终提交数据:', submitData)

    // 5. 发送保存事件
    emit('save', submitData)

  } catch (error) {
    console.error('保存课程出错:', error)
    ElMessage.error(`保存失败: ${error.message || '请重试'}`)
  } finally {
    saving.value = false
  }
}

const handleCancel = () => {
  emit('cancel')
}
</script>

<style scoped>
.course-form {
  padding: 20px;
}

.form-section {
  margin-bottom: 30px;
  padding: 20px;
  background: #fafafa;
  border-radius: 8px;
}

.form-section h4 {
  margin: 0 0 20px 0;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
}

.upload-wrapper {
  width: 100%;
}

.upload-tip {
  margin-top: 8px;
}

.chapter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.empty-chapters {
  text-align: center;
  padding: 40px 0;
}

.chapters-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.chapter-item {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  background: white;
}

.chapter-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
  font-weight: 600;
  color: #409eff;
}

.title-text {
  font-weight: 500;
  color: #303133;
}

.chapter-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 12px;
  color: #909399;
}

.chapter-actions {
  display: flex;
  gap: 8px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
