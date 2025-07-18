<template>
  <div class="course-form-container">
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="120px"
      class="course-form"
    >
      <!-- 基本信息 -->
      <div class="form-section">
        <h4>📝 基本信息</h4>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="课程名称" prop="title">
              <el-input 
                v-model="form.title" 
                placeholder="请输入课程名称"
                maxlength="100"
                show-word-limit
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
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
        </el-row>
        
        <el-row :gutter="20">
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
                :max="9999"
                placeholder="分钟" 
                style="width: 100%" 
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="课程价格" prop="price">
              <el-input-number 
                v-model="form.price" 
                :min="0" 
                :max="99999"
                :precision="2" 
                placeholder="元" 
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
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="讲师ID" prop="instructorId">
              <el-input 
                v-model="form.instructorId" 
                placeholder="请输入讲师ID" 
                :disabled="userStore.userRole === 'TEACHER'"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="必修课程">
              <el-switch 
                v-model="form.isRequired"
                active-text="是"
                inactive-text="否"
              />
            </el-form-item>
          </el-col>
        </el-row>
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
              class="cover-upload"
            >
              <el-icon class="upload-icon"><Plus /></el-icon>
            </el-upload>
            <div class="upload-tips">
              <p>建议尺寸：16:9，支持 JPG、PNG 格式，大小不超过 5MB</p>
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
            >
              <el-button type="primary" :loading="uploading">
                <el-icon><Upload /></el-icon>
                选择文件
              </el-button>
            </el-upload>
            <div class="upload-tips">
              <p>支持 PDF、Word、Excel、TXT、ZIP 等格式，单个文件不超过 50MB，最多上传 10 个文件</p>
            </div>
          </div>
        </el-form-item>
        
        <!-- 上传进度 -->
        <div v-if="uploading" class="upload-progress">
          <el-progress :percentage="uploadProgress" />
          <p>正在上传中，请稍候...</p>
        </div>
      </div>

      <!-- 章节管理 -->
      <div class="form-section">
        <div class="section-header">
          <h4>📖 章节管理</h4>
          <el-button type="primary" size="small" @click="addChapter">
            <el-icon><Plus /></el-icon>
            添加章节
          </el-button>
        </div>
        
        <div v-if="form.chapters.length === 0" class="no-chapters">
          <div class="no-chapters-icon">📚</div>
          <p>暂无章节，点击"添加章节"开始创建课程内容</p>
        </div>
        
        <div v-else class="chapters-list">
          <div
            v-for="(chapter, index) in sortedChapters"
            :key="chapter.id"
            class="chapter-item"
          >
            <div class="chapter-header">
              <span class="chapter-number">第{{ index + 1 }}章</span>
              <span class="chapter-title">{{ chapter.title || '未命名章节' }}</span>
              <div class="chapter-order-controls">
                <el-button 
                  size="small" 
                  :disabled="index === 0"
                  @click="moveChapterUp(index)"
                  title="上移"
                >
                  <el-icon><ArrowUp /></el-icon>
                </el-button>
                <el-button 
                  size="small" 
                  :disabled="index === form.chapters.length - 1"
                  @click="moveChapterDown(index)"
                  title="下移"
                >
                  <el-icon><ArrowDown /></el-icon>
                </el-button>
              </div>
              <div class="chapter-actions">
                <el-button size="small" @click="editChapter(index)">
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
                <el-button size="small" type="danger" @click="removeChapter(index)">
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </div>
            </div>
            <div class="chapter-meta">
              <span>时长: {{ chapter.duration || 0 }}分钟</span>
              <span>顺序: {{ chapter.order || (index + 1) }}</span>
              <span v-if="chapter.description">{{ chapter.description }}</span>
            </div>
          </div>
        </div>
      </div>
    </el-form>

    <!-- 操作按钮 -->
    <div class="form-footer">
      <el-button @click="handleCancel">取消</el-button>
      <el-button @click="handleSaveDraft" :loading="saving">保存草稿</el-button>
      <el-button type="primary" @click="handleSave" :loading="saving">
        {{ isEditing ? '保存修改' : '创建课程' }}
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
    default: () => ({})
  },
  isEditing: {
    type: Boolean,
    default: false
  }
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
  clearAllFiles
} = useFileUpload()

// 表单数据
const form = reactive({
  id: '',
  title: '',
  description: '',
  category: '',
  level: '',
  duration: 0,
  instructorId: '',
  price: 0,
  isRequired: false,
  coverImage: '',
  materials: [],
  chapters: []
})

// 章节编辑
const chapterModalVisible = ref(false)
const chapterModalTitle = ref('添加章节')
const chapterFormRef = ref()
const editingChapter = ref(null)
const editingChapterIndex = ref(-1)

// 配置数据
const courseCategories = [
  '技术培训',
  '产品培训',
  '安全培训', 
  '管理培训',
  '营销培训'
]

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
  category: [
    { required: true, message: '请选择课程分类', trigger: 'change' }
  ],
  level: [
    { required: true, message: '请选择难度级别', trigger: 'change' }
  ],
  duration: [
    { required: true, message: '请输入课程时长', trigger: 'blur' },
    { type: 'number', min: 1, message: '课程时长必须大于0', trigger: 'blur' }
  ],
  instructorId: [
    { required: true, message: '请输入讲师ID', trigger: 'blur' }
  ],
  price: [
    { type: 'number', min: 0, message: '价格不能为负数', trigger: 'blur' }
  ]
}

// 计算属性
const formData = computed(() => {
  return {
    ...form,
    coverImage: fileListState.cover[0]?.url || '',
    materials: fileListState.materials.map(file => file.url)
  }
})

const sortedChapters = computed(() => {
  return [...form.chapters].sort((a, b) => (a.order || 0) - (b.order || 0))
})

// 监听器
watch(() => props.courseData, (newData) => {
  if (newData && Object.keys(newData).length > 0) {
    initFormData(newData)
  }
}, { immediate: true })

// 生命周期
onMounted(() => {
  if (userStore.userRole === 'TEACHER' && !props.isEditing) {
    form.instructorId = userStore.userInfo.id || userStore.userInfo.username
  }
})

// 方法
const initFormData = (data) => {
  Object.assign(form, {
    id: data.id || '',
    title: data.title || '',
    description: data.description || '',
    category: data.category || '',
    level: data.level || getDifficultyLevelText(data.difficultyLevel),
    duration: data.duration || 0,
    instructorId: data.instructorId || '',
    price: data.price || 0,
    isRequired: data.isRequired || false,
    chapters: data.chapters || []
  })
  
  setFileList('cover', data.coverImage ? [{
    name: '课程封面',
    url: data.coverImage,
    uid: Date.now()
  }] : [])
  
  setFileList('materials', (data.materials || []).map((url, index) => ({
    name: `教学资料${index + 1}`,
    url: url,
    uid: Date.now() + index
  })))
}

const getDifficultyLevelText = (level) => {
  const levelMap = {
    1: '入门级',
    2: '初级', 
    3: '中级',
    4: '高级',
    5: '专家级'
  }
  return levelMap[level] || '入门级'
}

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
    videoUrl: ''
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
  ElMessage.success('章节删除成功')
}

const moveChapterUp = (index) => {
  if (index === 0) return
  
  const sortedList = sortedChapters.value
  const currentChapter = sortedList[index]
  const prevChapter = sortedList[index - 1]
  
  // 交换order值
  const tempOrder = currentChapter.order
  currentChapter.order = prevChapter.order
  prevChapter.order = tempOrder
  
  // 在原数组中更新
  const currentOriginalIndex = getOriginalIndex(currentChapter.id)
  const prevOriginalIndex = getOriginalIndex(prevChapter.id)
  
  form.chapters[currentOriginalIndex].order = currentChapter.order
  form.chapters[prevOriginalIndex].order = prevChapter.order
  
  ElMessage.success('章节顺序调整成功')
}

const moveChapterDown = (index) => {
  const sortedList = sortedChapters.value
  if (index === sortedList.length - 1) return
  
  const currentChapter = sortedList[index]
  const nextChapter = sortedList[index + 1]
  
  // 交换order值
  const tempOrder = currentChapter.order
  currentChapter.order = nextChapter.order
  nextChapter.order = tempOrder
  
  // 在原数组中更新
  const currentOriginalIndex = getOriginalIndex(currentChapter.id)
  const nextOriginalIndex = getOriginalIndex(nextChapter.id)
  
  form.chapters[currentOriginalIndex].order = currentChapter.order
  form.chapters[nextOriginalIndex].order = nextChapter.order
  
  ElMessage.success('章节顺序调整成功')
}

const getOriginalIndex = (chapterId) => {
  return form.chapters.findIndex(c => c.id === chapterId)
}

const handleChapterSave = (chapterData) => {
  if (editingChapterIndex.value === -1) {
    // 新增章节
    form.chapters.push({
      ...chapterData,
      id: `chapter_${Date.now()}`
    })
  } else {
    // 编辑章节
    form.chapters[editingChapterIndex.value] = chapterData
  }
  
  updateChapterOrder()
  closeChapterModal()
  ElMessage.success('章节保存成功')
}

const closeChapterModal = () => {
  chapterModalVisible.value = false
  editingChapter.value = null
  editingChapterIndex.value = -1
}

const updateChapterOrder = () => {
  form.chapters.forEach((chapter, index) => {
    if (!chapter.order) {
      chapter.order = index + 1
    }
  })
}

// 表单操作
const handleSave = async () => {
  try {
    const valid = await formRef.value.validate()
    if (!valid) return
    
    saving.value = true
    emit('save', formData.value)
  } catch (error) {
    console.error('表单验证失败:', error)
  } finally {
    saving.value = false
  }
}

const handleSaveDraft = async () => {
  try {
    saving.value = true
    const draftData = { ...formData.value, status: 0 }
    emit('save', draftData)
  } catch (error) {
    console.error('保存草稿失败:', error)
  } finally {
    saving.value = false
  }
}

const handleCancel = () => {
  emit('cancel')
}

defineExpose({
  resetForm: () => {
    formRef.value?.resetFields()
    clearAllFiles()
  }
})
</script>

<style scoped>
.course-form-container {
  max-height: 70vh;
  overflow-y: auto;
  padding-right: 8px;
}

.form-section {
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 1px solid #ebeef5;
}

.form-section h4 {
  color: #303133;
  margin-bottom: 20px;
  font-size: 16px;
  font-weight: 600;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.upload-wrapper {
  width: 100%;
}

.upload-tips {
  color: #909399;
  font-size: 12px;
  margin-top: 8px;
}

.upload-progress {
  margin-top: 16px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.no-chapters {
  text-align: center;
  padding: 60px 20px;
  background: #fafbfc;
  border-radius: 8px;
  border: 2px dashed #dcdfe6;
}

.no-chapters-icon {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.6;
}

.chapters-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.chapter-item {
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 16px;
  transition: all 0.3s ease;
}

.chapter-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

.chapter-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.chapter-number {
  font-weight: 600;
  color: #409eff;
  min-width: 60px;
}

.chapter-title {
  flex: 1;
  font-weight: 500;
}

.chapter-order-controls {
  display: flex;
  gap: 4px;
}

.chapter-actions {
  display: flex;
  gap: 8px;
}

.chapter-meta {
  font-size: 12px;
  color: #909399;
  display: flex;
  gap: 16px;
}

.form-footer {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  padding: 24px 16px 16px 16px;
  background: #fff;
  border-top: 1px solid #ebeef5;
  margin: 0 -16px -16px -16px;
}

@media (max-width: 768px) {
  .chapter-header {
    flex-wrap: wrap;
  }
  
  .chapter-order-controls,
  .chapter-actions {
    width: 100%;
    justify-content: flex-end;
  }
  
  .chapter-meta {
    flex-direction: column;
    gap: 4px;
  }
}

.course-form-container::-webkit-scrollbar {
  width: 6px;
}

.course-form-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.course-form-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.course-form-container::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>