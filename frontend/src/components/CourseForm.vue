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
            <div class="chapter-info">
              <div class="chapter-title">
                <span class="chapter-number">{{ chapter.order || index + 1 }}</span>
                <span class="chapter-name">{{ chapter.title || '未命名章节' }}</span>
              </div>
              <div class="chapter-meta">
                <el-tag size="small" type="info">
                  {{ chapter.duration || 0 }}分钟
                </el-tag>
                <span class="chapter-desc">{{ chapter.description || '暂无描述' }}</span>
              </div>
            </div>
            <div class="chapter-actions">
              <el-button size="small" type="primary" @click="editChapter(index)" :icon="Edit">
                编辑
              </el-button>
              <el-button size="small" @click="moveChapterUp(index)" :disabled="index === 0" :icon="ArrowUp">
                上移
              </el-button>
              <el-button
                size="small"
                @click="moveChapterDown(index)"
                :disabled="index === sortedChapters.length - 1"
                :icon="ArrowDown"
              >
                下移
              </el-button>
              <el-button size="small" type="danger" @click="removeChapter(index)" :icon="Delete">
                删除
              </el-button>
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

    <!-- 章节编辑模态框 -->
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

// 表单数据 - 🔧 去掉 instructorId 和 price 字段
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
const chapterFormRef = ref()
const editingChapter = ref(null)
const editingChapterIndex = ref(-1)

// 配置数据
const courseCategories = ['技术培训', '产品培训', '安全培训', '管理培训', '营销培训']

const difficultyLevels = [
  { label: '入门级', value: 1 },
  { label: '初级', value: 2 },
  { label: '中级', value: 3 },
  { label: '高级', value: 4 },
  { label: '专家级', value: 5 },
]

// 表单验证规则 - 🔧 去掉 instructorId 和 price 的验证
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

// 🔧 修复 initFormData 函数 - 去掉 instructorId 和 price
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

  // 🔧 处理封面图片
  const coverImageUrl = data.coverImage || data.coverImageUrl
  setFileList(
    'cover',
    coverImageUrl
      ? [{ name: 'cover', url: coverImageUrl, uid: Date.now() }]
      : []
  )

  // 🔧 处理学习资料
  let materialsList = []
  if (data.materials && Array.isArray(data.materials) && data.materials.length > 0) {
    // 新格式：包含文件名的对象数组
    materialsList = data.materials.map((material, index) => ({
      name: material.name || material.originalName || `教学资料${index + 1}`,
      url: material.url || material,
      uid: Date.now() + index,
      originalName: material.originalName || material.name,
    }))
  } else if (data.materialUrls && typeof data.materialUrls === 'string') {
    // 兼容旧格式：逗号分隔的URL字符串
    const urls = (
      Array.isArray(data.materialUrls) ? data.materialUrls :
        data.materialUrls.split(',')
    ).filter((url) => url && url.trim())

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

// 章节管理
const addChapter = () => {
  chapterModalTitle.value = '添加章节'
  editingChapter.value = {
    id: '',
    title: '',
    description: '',
    duration: 0,
    order: form.chapters.length + 1,
    content: '',
    videoUrl: '',
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

const handleChapterSave = (chapterData) => {
  if (editingChapterIndex.value === -1) {
    // 新增章节
    chapterData.id = Date.now().toString()
    form.chapters.push(chapterData)
  } else {
    // 更新章节
    Object.assign(form.chapters[editingChapterIndex.value], chapterData)
  }
  closeChapterModal()
}

const closeChapterModal = () => {
  chapterModalVisible.value = false
  editingChapter.value = null
  editingChapterIndex.value = -1
}

// 🔧 修复表单保存函数 - 自动设置默认值
const handleSave = async () => {
  if (saving.value) return

  try {
    saving.value = true

    console.log('📝 开始表单验证，当前表单数据:', form)

    // 表单验证
    const isValid = await formRef.value.validate().catch((errors) => {
      console.error('表单验证失败:', errors)
      ElMessage.error('请完善必填信息')
      return false
    })

    if (!isValid) {
      console.log('❌ 表单验证未通过')
      return
    }

    console.log('✅ 表单验证通过')

    // 🔧 准备提交数据 - 自动设置后端需要的字段
    const submitData = {
      ...form,
      // 🔧 自动设置讲师ID（后端需要）
      instructorId: userStore.userInfo?.userId || userStore.userInfo?.id || userStore.userInfo?.username || 'default-instructor',
      // 🔧 自动设置价格为0（后端需要）
      price: 0,
      coverImage: fileListState.cover[0]?.url || '',
      // 改进材料数据格式
      materials: fileListState.materials.map((file) => ({
        name: file.name || file.originalName,
        url: file.url,
        originalName: file.originalName || file.name,
      })),
      // 兼容字段
      materialUrls: fileListState.materials.map((file) => file.url).join(','),
    }

    console.log('📤 提交数据:', submitData)

    // 发送保存事件
    emit('save', submitData)

  } catch (error) {
    console.error('保存课程出错:', error)
    ElMessage.error('保存失败，请重试')
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
  background-color: #f8f9fa;
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

.course-cover-upload {
  width: 100%;
}

.material-upload {
  width: 100%;
}

.upload-progress {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
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
  space-y: 12px;
}

.chapter-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background-color: white;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.chapter-info {
  flex: 1;
}

.chapter-title {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.chapter-number {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  background-color: #409eff;
  color: white;
  border-radius: 50%;
  font-size: 12px;
  margin-right: 12px;
}

.chapter-name {
  font-weight: 500;
  color: #303133;
}

.chapter-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.chapter-desc {
  color: #909399;
  font-size: 14px;
}

.chapter-actions {
  display: flex;
  gap: 8px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 0;
  border-top: 1px solid #e4e7ed;
  margin-top: 30px;
}
</style>
