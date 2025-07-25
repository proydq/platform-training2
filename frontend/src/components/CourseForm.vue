<template>
  <div class="course-form">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
      <!-- 基础信息 -->
      <div class="form-section">
        <h4>📋 基本信息</h4>

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
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
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
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
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
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="课程时长" prop="duration">
              <el-input-number
                v-model="form.duration"
                :min="1"
                :max="1000"
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
              <el-icon v-if="!uploading.cover">
                <Plus />
              </el-icon>
              <div v-else class="upload-progress">
                <el-progress type="circle" :percentage="uploadProgress.cover" :width="50" />
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
              <el-button type="primary" :icon="Upload" :loading="uploading.material">
                上传文档
              </el-button>
            </el-upload>
            <div class="upload-tip">
              <el-text type="info" size="small">
                支持 PDF、Word、Excel、PPT、TXT、ZIP、RAR 格式，单个文件不超过
                50MB，最多上传10个文件
              </el-text>
            </div>
          </div>
        </el-form-item>

        <!-- 教学视频 -->
        <el-form-item label="教学视频">
          <div class="upload-wrapper">
            <el-upload
              ref="videoUploadRef"
              :file-list="fileListState.videos"
              :http-request="handleVideoUpload"
              :on-remove="handleVideoRemove"
              :before-upload="beforeVideoUpload"
              :on-progress="handleVideoProgress"
              multiple
              :limit="20"
              accept=".mp4,.avi,.mov,.wmv,.flv,.webm,.mkv"
              list-type="text"
              class="video-upload"
            >
              <el-button type="success" :icon="VideoPlay" :loading="uploading.video">
                上传视频
              </el-button>
            </el-upload>
            <div class="upload-tip">
              <el-text type="info" size="small">
                支持 MP4、AVI、MOV、WMV、FLV、WebM、MKV 格式，单个文件不超过 500MB，最多上传20个文件
              </el-text>
            </div>

            <!-- 视频上传进度 -->
            <div
              v-if="uploadProgress.video > 0 && uploadProgress.video < 100"
              class="upload-progress-bar"
            >
              <el-progress
                :percentage="uploadProgress.video"
                :format="(format) => `${format}% (${uploadSpeed})`"
              />
              <div class="progress-info">
                <span>正在上传视频，请稍候...</span>
                <span v-if="estimatedTime">预计剩余时间：{{ estimatedTime }}</span>
              </div>
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
          <el-text type="info" size="small"> {{ form.chapters.length }} 个章节</el-text>
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
                  <span class="chapter-number">{{ chapter.order }}</span>
                  <span class="title-text">{{ chapter.title }}</span>
                  <el-tag
                    :type="getChapterTypeTagType(chapter.chapterType)"
                    size="small"
                    class="chapter-type-tag"
                  >
                    {{ getChapterTypeText(chapter.chapterType) }}
                  </el-tag>
                </div>
                <div class="chapter-meta">
                  <span>{{ chapter.description || '暂无描述' }}</span>
                  <span class="chapter-duration">⏱️ {{ chapter.duration }}分钟</span>
                </div>
              </div>
              <div class="chapter-actions">
                <el-button type="primary" link size="small" @click="editChapter(index)">
                  <el-icon>
                    <Edit />
                  </el-icon>
                  编辑
                </el-button>
                <el-button type="danger" link size="small" @click="removeChapter(index)">
                  <el-icon>
                    <Delete />
                  </el-icon>
                  删除
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 提交按钮 -->
      <div class="form-actions">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">
          {{ props.isEditing ? '保存修改' : '创建课程' }}
        </el-button>
      </div>
    </el-form>

    <!-- 章节编辑对话框 -->
    <el-dialog
      v-model="chapterModalVisible"
      :title="chapterModalTitle"
      width="800px"
      :close-on-click-modal="false"
      class="chapter-dialog"
    >
      <el-form ref="chapterFormRef" :model="chapterForm" :rules="chapterRules" label-width="100px">
        <!-- 基础信息 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="章节标题" prop="title">
              <el-input v-model="chapterForm.title" placeholder="请输入章节标题" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序序号" prop="order">
              <el-input-number
                v-model="chapterForm.order"
                :min="1"
                :max="100"
                placeholder="章节排序"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 章节类型选择 -->
        <el-form-item label="章节类型" prop="chapterType">
          <el-radio-group
            v-model="chapterForm.chapterType"
            @change="onChapterTypeChange"
            class="chapter-type-group"
          >
            <el-radio value="video" class="type-radio">
              <div class="type-option">
                <el-icon>
                  <VideoPlay />
                </el-icon>
                <span>视频课程</span>
              </div>
            </el-radio>
            <el-radio value="document" class="type-radio">
              <div class="type-option">
                <el-icon>
                  <Document />
                </el-icon>
                <span>文档资料</span>
              </div>
            </el-radio>
            <el-radio value="audio" class="type-radio">
              <div class="type-option">
                <el-icon>
                  <Microphone />
                </el-icon>
                <span>音频课程</span>
              </div>
            </el-radio>
            <el-radio value="quiz" class="type-radio">
              <div class="type-option">
                <el-icon>
                  <Edit />
                </el-icon>
                <span>在线测验</span>
              </div>
            </el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 章节描述 -->
        <el-form-item label="章节描述">
          <el-input
            v-model="chapterForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入章节描述（可选）"
          />
        </el-form-item>

        <!-- 根据章节类型显示不同的内容配置 -->

        <!-- 视频类型配置 -->
        <div v-if="chapterForm.chapterType === 'video'" class="chapter-content-config">
          <el-form-item label="视频资源" prop="videoUrl">
            <div class="video-selector">
              <!-- 从已上传视频中选择 -->
              <el-select
                v-model="chapterForm.videoUrl"
                placeholder="选择视频文件"
                style="width: 100%"
                filterable
                clearable
              >
                <el-option
                  v-for="video in fileListState.videos"
                  :key="video.url"
                  :label="video.name"
                  :value="video.url"
                >
                  <div class="video-option">
                    <span>{{ video.name }}</span>
                    <el-tag size="small" type="info">{{ formatFileSize(video.size) }}</el-tag>
                  </div>
                </el-option>
              </el-select>

              <!-- 视频预览 -->
              <div v-if="chapterForm.videoUrl" class="video-preview">
                <video
                  :src="chapterForm.videoUrl"
                  controls
                  style="width: 100%; max-height: 200px; border-radius: 8px; margin-top: 10px"
                  preload="metadata"
                >
                  您的浏览器不支持视频播放
                </video>
              </div>
            </div>
          </el-form-item>

          <el-form-item label="视频时长">
            <el-input-number
              v-model="chapterForm.duration"
              :min="1"
              placeholder="分钟"
              style="width: 100%"
            />
            <div class="form-tip">视频实际时长，用于学习进度计算</div>
          </el-form-item>
        </div>

        <!-- 文档类型配置 -->
        <div v-if="chapterForm.chapterType === 'document'" class="chapter-content-config">
          <el-form-item label="文档资源" prop="contentUrl">
            <div class="document-selector">
              <!-- 从已上传文档中选择 -->
              <el-select
                v-model="chapterForm.contentUrl"
                placeholder="选择文档文件"
                style="width: 100%"
                filterable
                clearable
              >
                <el-option
                  v-for="material in fileListState.materials"
                  :key="material.url"
                  :label="material.name"
                  :value="material.url"
                >
                  <div class="document-option">
                    <span>{{ material.name }}</span>
                    <el-tag size="small" type="success"
                    >{{ getFileExtension(material.name) }}
                    </el-tag>
                  </div>
                </el-option>
              </el-select>

              <!-- 或者输入文本内容 -->
              <el-divider>或者</el-divider>
              <el-input
                v-model="chapterForm.content"
                type="textarea"
                :rows="6"
                placeholder="直接输入文字内容（如果不选择文档文件）"
              />
            </div>
          </el-form-item>

          <el-form-item label="预计时长">
            <el-input-number
              v-model="chapterForm.duration"
              :min="1"
              placeholder="分钟"
              style="width: 100%"
            />
            <div class="form-tip">预计学习时长</div>
          </el-form-item>
        </div>

        <!-- 音频类型配置 -->
        <div v-if="chapterForm.chapterType === 'audio'" class="chapter-content-config">
          <el-form-item label="音频资源" prop="contentUrl">
            <el-input
              v-model="chapterForm.contentUrl"
              placeholder="请输入音频文件URL或上传音频文件"
            />
            <div class="form-tip">暂时支持输入音频文件URL，后续版本将支持音频文件上传</div>
          </el-form-item>

          <el-form-item label="音频时长">
            <el-input-number
              v-model="chapterForm.duration"
              :min="1"
              placeholder="分钟"
              style="width: 100%"
            />
          </el-form-item>
        </div>

        <!-- 测验类型配置 -->
        <div v-if="chapterForm.chapterType === 'quiz'" class="chapter-content-config">
          <el-form-item label="测验内容">
            <el-input
              v-model="chapterForm.content"
              type="textarea"
              :rows="4"
              placeholder="请输入测验说明或题目内容"
            />
            <div class="form-tip">测验功能将在后续版本中完善</div>
          </el-form-item>

          <el-form-item label="预计时长">
            <el-input-number
              v-model="chapterForm.duration"
              :min="5"
              :max="120"
              placeholder="分钟"
              style="width: 100%"
            />
          </el-form-item>
        </div>

        <!-- 时长设置 -->
        <el-form-item label="时长(分钟)">
          <el-input-number
            v-model="chapterForm.duration"
            :min="1"
            placeholder="分钟"
            style="width: 100%"
          />
          <div class="form-tip">章节预计学习时长</div>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeChapterModal">取消</el-button>
          <el-button type="primary" @click="saveChapter" :loading="chapterSaving">
            {{ editingChapterIndex >= 0 ? '更新' : '添加' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Upload,
  Edit,
  Delete,
  VideoPlay,
  Document,
  Microphone,
} from '@element-plus/icons-vue'
import { uploadCourseCoverAPI, uploadCourseMaterialAPI, uploadCourseVideoAPI } from '@/api/course'

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

// 响应式数据
const formRef = ref()
const chapterFormRef = ref()
const saving = ref(false)

// 上传状态管理
const uploading = reactive({
  cover: false,
  material: false,
  video: false,
})

const uploadProgress = reactive({
  cover: 0,
  material: 0,
  video: 0,
})

const uploadSpeed = ref('')
const estimatedTime = ref('')

// 文件列表状态
const fileListState = reactive({
  cover: [],
  materials: [],
  videos: [],
})

// 表单数据
const form = reactive({
  id: '',
  title: '',
  description: '',
  category: '',
  level: '',
  price: 0,
  duration: 0,
  isRequired: false,
  coverImage: '',
  materials: [],
  chapters: [],
})

// 章节对话框状态
const chapterModalVisible = ref(false)
const chapterModalTitle = ref('添加章节')
const editingChapterIndex = ref(-1)
const chapterSaving = ref(false)

// 章节表单数据
const chapterForm = reactive({
  title: '',
  description: '',
  chapterType: 'video',
  order: 1,
  duration: 10,
  content: '',
  videoUrl: '',
  contentUrl: '',
  isFree: false,
  isPublished: false,
  status: 0,
  requirements: '',
  learningObjectives: '',
})

// 配置数据
const courseCategories = ['技术培训', '产品培训', '安全培训', '管理培训', '营销培训']

const difficultyLevels = [
  { label: '入门级', value: 1 },
  { label: '初级', value: 2 },
  { label: '中级', value: 3 },
  { label: '高级', value: 4 },
  { label: '专家级', value: 5 },
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

// 章节表单验证规则
const chapterRules = {
  title: [
    { required: true, message: '请输入章节标题', trigger: 'blur' },
    { min: 2, max: 100, message: '章节标题长度在 2 到 100 个字符', trigger: 'blur' },
  ],
  chapterType: [{ required: true, message: '请选择章节类型', trigger: 'change' }],
  order: [
    { required: true, message: '请输入排序序号', trigger: 'blur' },
    { type: 'number', min: 1, max: 100, message: '排序序号必须在 1-100 之间', trigger: 'blur' },
  ],
}

// 计算属性
const sortedChapters = computed(() => {
  return [...form.chapters].sort((a, b) => (a.order || 0) - (b.order || 0))
})

// 文件上传方法

// 封面上传
const beforeCoverUpload = (file) => {
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

const handleCoverUpload = async (options) => {
  const { file } = options
  try {
    uploading.cover = true
    const response = await uploadCourseCoverAPI(file)

    console.log('📸 封面上传响应:', response)

    if (response && (response.code === 0 || response.code === 200 || response.data)) {
      const coverInfo = {
        name: file.name,
        url: response.data?.url || response.url || URL.createObjectURL(file),
        uid: Date.now(),
      }
      fileListState.cover = [coverInfo]
      ElMessage.success('封面上传成功！')
    } else {
      throw new Error(response?.message || '封面上传失败')
    }
  } catch (error) {
    console.error('封面上传失败:', error)
    ElMessage.error('封面上传失败')
  } finally {
    uploading.cover = false
  }
}

const handleCoverRemove = () => {
  fileListState.cover = []
}

// 文档上传
const beforeMaterialUpload = (file) => {
  const allowedTypes = [
    '.pdf',
    '.doc',
    '.docx',
    '.ppt',
    '.pptx',
    '.xls',
    '.xlsx',
    '.txt',
    '.zip',
    '.rar',
  ]
  const fileName = file.name.toLowerCase()
  const isValidType = allowedTypes.some((type) => fileName.endsWith(type))

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

const handleMaterialUpload = async (options) => {
  const { file } = options
  try {
    uploading.material = true
    const response = await uploadCourseMaterialAPI(file)

    console.log('📄 文档上传响应:', response)

    if (response && (response.code === 0 || response.code === 200 || response.data)) {
      const materialInfo = {
        name: file.name,
        originalName: file.name,
        url: response.data?.url || response.url || URL.createObjectURL(file),
        size: file.size,
        uid: Date.now(),
      }
      fileListState.materials.push(materialInfo)
      ElMessage.success(`资料 "${file.name}" 上传成功！`)
    } else {
      throw new Error(response?.message || '文档上传失败')
    }
  } catch (error) {
    console.error('资料上传失败:', error)
    ElMessage.error('资料上传失败')
  } finally {
    uploading.material = false
  }
}

const handleMaterialRemove = (file) => {
  const index = fileListState.materials.findIndex((m) => m.uid === file.uid)
  if (index > -1) {
    fileListState.materials.splice(index, 1)
  }
}

// 视频上传方法
const beforeVideoUpload = (file) => {
  console.log('📹 视频上传前验证:', file.name)

  const allowedTypes = [
    'video/mp4',
    'video/avi',
    'video/mov',
    'video/wmv',
    'video/x-flv',
    'video/webm',
    'video/x-matroska',
  ]
  const isValidType =
    allowedTypes.includes(file.type) || /\.(mp4|avi|mov|wmv|flv|webm|mkv)$/i.test(file.name)

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

const handleVideoUpload = async (options) => {
  const { file, onProgress } = options

  try {
    uploading.video = true
    uploadProgress.video = 0

    const progressHandler = (progressEvent) => {
      if (progressEvent.lengthComputable) {
        const progress = Math.round((progressEvent.loaded * 100) / progressEvent.total)
        uploadProgress.video = progress

        const timeElapsed = (Date.now() - startTime) / 1000
        const bytesPerSecond = progressEvent.loaded / timeElapsed
        const mbPerSecond = (bytesPerSecond / (1024 * 1024)).toFixed(2)
        uploadSpeed.value = `${mbPerSecond} MB/s`

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
    const response = await uploadCourseVideoAPI(file, progressHandler)

    console.log('📹 视频上传响应:', response)

    if (response && (response.code === 0 || response.code === 200 || response.data)) {
      const videoInfo = {
        name: file.name,
        originalName: file.name,
        url: response.data?.url || response.url || URL.createObjectURL(file),
        size: file.size,
        uid: Date.now(),
        status: 'success',
      }

      fileListState.videos.push(videoInfo)
      ElMessage.success(`视频 "${file.name}" 上传成功！`)
      console.log('✅ 视频上传成功:', videoInfo)
    } else {
      throw new Error(response?.message || '视频上传失败')
    }
  } catch (error) {
    console.error('❌ 视频上传失败:', error)
    ElMessage.error(`视频上传失败: ${error.message}`)
    options.onError && options.onError(error)
  } finally {
    uploading.video = false
    uploadProgress.video = 0
    uploadSpeed.value = ''
    estimatedTime.value = ''
  }
}

const handleVideoRemove = (file) => {
  const index = fileListState.videos.findIndex((v) => v.uid === file.uid || v.url === file.url)
  if (index > -1) {
    fileListState.videos.splice(index, 1)
    ElMessage.success('视频已移除')
  }
}

const handleVideoProgress = (event) => {
  console.log('📊 视频上传进度:', event.percent + '%')
}

// 章节管理方法
const addChapter = () => {
  resetChapterForm()
  chapterModalTitle.value = '添加章节'
  editingChapterIndex.value = -1
  chapterForm.order = form.chapters.length + 1
  chapterModalVisible.value = true
}

const editChapter = (index) => {
  const chapter = form.chapters[index]
  Object.assign(chapterForm, {
    ...chapter,
    isPublished: chapter.status === 1,
  })
  chapterModalTitle.value = '编辑章节'
  editingChapterIndex.value = index
  chapterModalVisible.value = true
}

const removeChapter = async (index) => {
  try {
    await ElMessageBox.confirm('确定要删除这个章节吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })

    form.chapters.splice(index, 1)
    ElMessage.success('章节已删除')
  } catch {
    // 用户取消删除
  }
}

const saveChapter = async () => {
  try {
    await chapterFormRef.value.validate()
    chapterSaving.value = true

    const chapterData = {
      ...chapterForm,
      status: chapterForm.isPublished ? 1 : 0,
    }

    if (editingChapterIndex.value >= 0) {
      form.chapters[editingChapterIndex.value] = chapterData
      ElMessage.success('章节已更新')
    } else {
      form.chapters.push({ ...chapterData, id: Date.now().toString() })
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
  Object.assign(chapterForm, {
    title: '',
    description: '',
    chapterType: 'video',
    order: 1,
    duration: 10,
    content: '',
    videoUrl: '',
    contentUrl: '',
    isFree: false,
    isPublished: false,
    status: 0,
    requirements: '',
    learningObjectives: '',
  })
}

// 章节类型相关方法
const onChapterTypeChange = (newType) => {
  chapterForm.contentUrl = ''
  chapterForm.videoUrl = ''
  chapterForm.content = ''

  switch (newType) {
    case 'video':
      chapterForm.duration = 10
      break
    case 'document':
      chapterForm.duration = 15
      break
    case 'audio':
      chapterForm.duration = 8
      break
    case 'quiz':
      chapterForm.duration = 20
      break
    default:
      chapterForm.duration = 10
  }
}

const getChapterTypeText = (type) => {
  const typeMap = {
    video: '🎥 视频',
    document: '📄 文档',
    audio: '🎵 音频',
    quiz: '📝 测验',
  }
  return typeMap[type] || '📚 课程'
}

const getChapterTypeTagType = (type) => {
  const tagTypeMap = {
    video: 'primary',
    document: 'success',
    audio: 'warning',
    quiz: 'info',
  }
  return tagTypeMap[type] || 'info'
}

// 工具函数
const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const getFileExtension = (filename) => {
  return filename.slice(((filename.lastIndexOf('.') - 1) >>> 0) + 2).toUpperCase()
}

// 数据初始化
const initFormData = (data) => {
  if (!data || Object.keys(data).length === 0) return

  Object.assign(form, {
    id: data.id || '',
    title: data.title || '',
    description: data.description || '',
    category: data.category || '',
    level: data.level || data.difficultyLevel || '',
    price: data.price || 0,
    duration: data.duration || data.estimatedDuration || 0,
    isRequired: data.isRequired || false,
    chapters: processChaptersData(data.chapters || []),
  })

  // 初始化文件列表
  if (data.coverImage) {
    fileListState.cover = [
      {
        name: '课程封面',
        url: data.coverImage,
        uid: Date.now(),
      },
    ]
  }

  if (data.materials && data.materials.length > 0) {
    fileListState.materials = data.materials.map((material, index) => ({
      name: material.name || `教学资料${index + 1}`,
      url: material.url,
      originalName: material.originalName || material.name,
      uid: Date.now() + index,
    }))
  }

  if (data.videos && data.videos.length > 0) {
    fileListState.videos = data.videos.map((video, index) => ({
      name: video.name || `教学视频${index + 1}`,
      url: video.url,
      originalName: video.originalName || video.name,
      size: video.size || 0,
      uid: Date.now() + index + 1000,
    }))
  }
}

const processChaptersData = (chapters) => {
  if (!chapters || !Array.isArray(chapters) || chapters.length === 0) {
    return []
  }

  return chapters.map((chapter, index) => ({
    id: chapter.id || '',
    title: chapter.title || '',
    description: chapter.description || '',
    chapterType: chapter.chapterType || chapter.chapter_type || 'document',
    order: chapter.order || chapter.sortOrder || chapter.sort_order || index + 1,
    duration: chapter.duration || 0,
    content: chapter.content || chapter.contentUrl || chapter.content_url || '',
    videoUrl: chapter.videoUrl || chapter.video_url || '',
    contentUrl:
      chapter.contentUrl || chapter.content_url || chapter.videoUrl || chapter.video_url || '',
    materialUrls: chapter.materialUrls || chapter.material_urls || '',
    videoUrls: chapter.videoUrls || chapter.video_urls || '',
    requirements: chapter.requirements || '',
    learningObjectives: chapter.learningObjectives || chapter.learning_objectives || '',
    status: chapter.status || 0,
    isFree: Boolean(chapter.isFree || chapter.is_free),
    fileFormat: chapter.fileFormat || chapter.file_format || '',
    fileSize: chapter.fileSize || chapter.file_size || null,
    thumbnailUrl: chapter.thumbnailUrl || chapter.thumbnail_url || '',
  }))
}

// 表单提交处理
const handleSave = async () => {
  try {
    await formRef.value.validate()
    saving.value = true

    const submitData = {
      ...form,
      coverImage: fileListState.cover[0]?.url || '',
      materials: fileListState.materials.map((file) => ({
        name: file.name,
        url: file.url,
        originalName: file.originalName || file.name,
      })),
      videos: fileListState.videos.map((file) => ({
        name: file.name,
        url: file.url,
        originalName: file.originalName || file.name,
      })),
    }

    emit('save', submitData)
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error('提交失败: ' + error.message)
  } finally {
    saving.value = false
  }
}

const handleCancel = () => {
  emit('cancel')
}

// 监听器
watch(
  () => props.courseData,
  (newData) => {
    if (newData && Object.keys(newData).length > 0) {
      initFormData(newData)
    }
  },
  { immediate: true },
)

onMounted(() => {
  console.log('🚀 CourseForm组件已挂载')
})
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

.upload-progress-bar {
  margin-top: 15px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  margin-top: 10px;
  font-size: 13px;
  color: #666;
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
  transition: all 0.3s ease;
}

.chapter-item:hover {
  border-color: #409eff;
  box-shadow: 0 4px 15px rgba(64, 158, 255, 0.1);
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
  background: rgba(64, 158, 255, 0.1);
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
}

.title-text {
  font-weight: 500;
  color: #303133;
}

.chapter-type-tag {
  margin-left: 10px;
}

.chapter-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 12px;
  color: #909399;
}

.chapter-duration {
  color: #409eff;
  font-weight: 500;
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

/* 章节对话框样式 */
.chapter-dialog .chapter-type-group {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.chapter-dialog .type-radio {
  margin: 0;
  padding: 0;
}

.chapter-dialog .type-option {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  min-width: 100px;
  justify-content: center;
  background: white;
}

.chapter-dialog .type-radio.is-checked .type-option {
  border-color: #409eff;
  background: rgba(64, 158, 255, 0.1);
  color: #409eff;
}

.chapter-dialog .type-option:hover {
  border-color: #409eff;
  background: rgba(64, 158, 255, 0.05);
}

.chapter-content-config {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 10px;
  margin: 15px 0;
}

.video-option,
.document-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.video-preview {
  margin-top: 15px;
  text-align: center;
}

.form-tip {
  font-size: 12px;
  color: #666;
  margin-top: 5px;
  font-style: italic;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .chapter-dialog .chapter-type-group {
    flex-direction: column;
    gap: 10px;
  }

  .chapter-dialog .type-option {
    min-width: auto;
    width: 100%;
  }
}
</style>
