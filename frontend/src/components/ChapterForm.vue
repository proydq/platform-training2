<!-- ==================== 6. 章节表单组件 ==================== -->
<!-- frontend/src/components/ChapterForm.vue -->
<template>
  <div class="chapter-form-container">
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
      class="chapter-form"
    >
      <!-- 基本信息 -->
      <el-row :gutter="20">
        <el-col :span="16">
          <el-form-item label="章节标题" prop="title">
            <el-input 
              v-model="form.title" 
              placeholder="请输入章节标题"
              maxlength="100"
              show-word-limit
            />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="排序" prop="order">
            <el-input-number 
              v-model="form.order" 
              :min="1" 
              :max="999"
              style="width: 100%" 
            />
          </el-form-item>
        </el-col>
      </el-row>
      
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="章节时长" prop="duration">
            <el-input-number 
              v-model="form.duration" 
              :min="0" 
              :max="9999"
              placeholder="分钟" 
              style="width: 100%" 
            />
            <div class="field-tip">预计学习时间（分钟）</div>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="章节状态">
            <el-select v-model="form.status" style="width: 100%">
              <el-option label="草稿" :value="0" />
              <el-option label="已发布" :value="1" />
              <el-option label="已下架" :value="2" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      
      <!-- 章节描述 -->
      <el-form-item label="章节描述">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="3"
          placeholder="请输入章节描述，简要说明本章节的学习内容和目标"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>
      
      <!-- 章节内容 -->
      <el-form-item label="章节内容" prop="content">
        <el-input
          v-model="form.content"
          type="textarea"
          :rows="8"
          placeholder="请输入章节的详细内容，包括知识点、操作步骤、案例分析等"
          maxlength="2000"
          show-word-limit
        />
      </el-form-item>
      
      <!-- 多媒体资源 -->
      <div class="media-section">
        <h5>📺 多媒体资源</h5>
        
        <!-- 视频链接 -->
        <el-form-item label="视频链接">
          <el-input
            v-model="form.videoUrl"
            placeholder="请输入视频链接（支持 MP4、优酷、腾讯视频等）"
          />
          <div class="field-tip">支持直链和主流视频平台链接</div>
        </el-form-item>
        
        <!-- 资料链接 -->
        <el-form-item label="资料链接">
          <div class="material-links">
            <div 
              v-for="(link, index) in form.materialUrls" 
              :key="index"
              class="link-item"
            >
              <el-input
                v-model="form.materialUrls[index]"
                placeholder="请输入资料下载链接"
              />
              <el-button 
                type="danger" 
                size="small" 
                @click="removeMaterialLink(index)"
                style="margin-left: 8px;"
              >
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
            <el-button 
              type="primary" 
              size="small" 
              @click="addMaterialLink"
              style="margin-top: 8px;"
            >
              <el-icon><Plus /></el-icon>
              添加资料链接
            </el-button>
          </div>
          <div class="field-tip">可添加多个相关学习资料的下载链接</div>
        </el-form-item>
      </div>
    </el-form>

    <!-- 操作按钮 -->
    <div class="form-footer">
      <el-button @click="handleCancel">取消</el-button>
      <el-button @click="handlePreview">预览</el-button>
      <el-button type="primary" @click="handleSave" :loading="saving">
        保存章节
      </el-button>
    </div>

    <!-- 预览模态框 -->
    <el-dialog
      v-model="previewVisible"
      title="章节预览"
      width="70%"
      :close-on-click-modal="false"
    >
      <div class="chapter-preview">
        <div class="preview-header">
          <h3>{{ form.title || '未命名章节' }}</h3>
          <div class="preview-meta">
            <el-tag size="small">第{{ form.order }}章</el-tag>
            <el-tag size="small" type="info">{{ form.duration }}分钟</el-tag>
            <el-tag size="small" :type="getStatusType(form.status)">
              {{ getStatusText(form.status) }}
            </el-tag>
          </div>
        </div>
        
        <div v-if="form.description" class="preview-description">
          <h4>章节描述</h4>
          <p>{{ form.description }}</p>
        </div>
        
        <div v-if="form.content" class="preview-content">
          <h4>章节内容</h4>
          <div class="content-text">{{ form.content }}</div>
        </div>
        
        <div v-if="form.videoUrl" class="preview-video">
          <h4>教学视频</h4>
          <div class="video-placeholder">
            <el-icon><VideoPlay /></el-icon>
            <span>{{ form.videoUrl }}</span>
          </div>
        </div>
        
        <div v-if="form.materialUrls && form.materialUrls.length > 0" class="preview-materials">
          <h4>学习资料</h4>
          <ul>
            <li v-for="(url, index) in form.materialUrls" :key="index">
              <el-icon><Document /></el-icon>
              <span>{{ url }}</span>
            </li>
          </ul>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Delete, Plus, VideoPlay, Document } from '@element-plus/icons-vue'

// Props & Emits
const props = defineProps({
  chapterData: {
    type: Object,
    default: () => ({})
  },
  chapterIndex: {
    type: Number,
    default: -1
  }
})

const emit = defineEmits(['save', 'cancel'])

// 数据和状态
const formRef = ref()
const saving = ref(false)
const previewVisible = ref(false)

// 表单数据
const form = reactive({
  id: '',
  title: '',
  description: '',
  content: '',
  duration: 0,
  order: 1,
  status: 0,
  videoUrl: '',
  materialUrls: []
})

// 表单验证规则
const rules = {
  title: [
    { required: true, message: '请输入章节标题', trigger: 'blur' },
    { min: 2, max: 100, message: '章节标题长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  duration: [
    { required: true, message: '请输入章节时长', trigger: 'blur' },
    { type: 'number', min: 0, message: '时长不能为负数', trigger: 'blur' }
  ],
  order: [
    { required: true, message: '请输入章节排序', trigger: 'blur' },
    { type: 'number', min: 1, message: '排序必须大于0', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入章节内容', trigger: 'blur' },
    { min: 10, message: '章节内容至少10个字符', trigger: 'blur' }
  ]
}

// 监听器
watch(() => props.chapterData, (newData) => {
  if (newData && Object.keys(newData).length > 0) {
    initFormData(newData)
  }
}, { immediate: true })

// 生命周期
onMounted(() => {
  if (!props.chapterData || Object.keys(props.chapterData).length === 0) {
    form.order = (props.chapterIndex + 1) || 1
  }
})

// 方法
const initFormData = (data) => {
  Object.assign(form, {
    id: data.id || '',
    title: data.title || '',
    description: data.description || '',
    content: data.content || '',
    duration: data.duration || 0,
    order: data.order || (props.chapterIndex + 1) || 1,
    status: data.status || 0,
    videoUrl: data.videoUrl || '',
    materialUrls: data.materialUrls || []
  })
}

const addMaterialLink = () => {
  form.materialUrls.push('')
}

const removeMaterialLink = (index) => {
  form.materialUrls.splice(index, 1)
}

const getStatusType = (status) => {
  const typeMap = {
    0: '',
    1: 'success',
    2: 'danger'
  }
  return typeMap[status] || ''
}

const getStatusText = (status) => {
  const textMap = {
    0: '草稿',
    1: '已发布',
    2: '已下架'
  }
  return textMap[status] || '草稿'
}

const handleSave = async () => {
  try {
    const valid = await formRef.value.validate()
    if (!valid) return
    
    saving.value = true
    
    const cleanData = {
      ...form,
      materialUrls: form.materialUrls.filter(url => url.trim() !== '')
    }
    
    emit('save', cleanData)
  } catch (error) {
    console.error('章节保存失败:', error)
  } finally {
    saving.value = false
  }
}

const handleCancel = () => {
  emit('cancel')
}

const handlePreview = () => {
  previewVisible.value = true
}

defineExpose({
  validate: () => formRef.value?.validate()
})
</script>

<style scoped>
.chapter-form-container {
  max-height: 60vh;
  overflow-y: auto;
  padding-right: 8px;
}

.chapter-form {
  padding: 0 16px;
}

.media-section {
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.media-section h5 {
  color: #303133;
  margin-bottom: 16px;
  font-size: 14px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
}

.field-tip {
  color: #909399;
  font-size: 12px;
  margin-top: 4px;
  line-height: 1.4;
}

.material-links {
  width: 100%;
}

.link-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
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

.chapter-preview {
  padding: 20px;
}

.preview-header {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.preview-header h3 {
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 20px;
}

.preview-meta {
  display: flex;
  gap: 8px;
}

.preview-description,
.preview-content,
.preview-video,
.preview-materials {
  margin-bottom: 24px;
}

.preview-description h4,
.preview-content h4,
.preview-video h4,
.preview-materials h4 {
  margin: 0 0 12px 0;
  color: #409eff;
  font-size: 16px;
  font-weight: 600;
}

.preview-description p {
  margin: 0;
  color: #606266;
  line-height: 1.6;
}

.content-text {
  color: #606266;
  line-height: 1.8;
  white-space: pre-wrap;
  background: #f5f7fa;
  padding: 16px;
  border-radius: 8px;
  border-left: 4px solid #409eff;
}

.video-placeholder {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px;
  background: #f0f9ff;
  border: 1px solid #b3d8ff;
  border-radius: 8px;
  color: #409eff;
}

.preview-materials ul {
  margin: 0;
  padding: 0;
  list-style: none;
}

.preview-materials li {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
  color: #606266;
  border-bottom: 1px solid #f0f2f5;
}

.preview-materials li:last-child {
  border-bottom: none;
}

@media (max-width: 768px) {
  .link-item {
    flex-direction: column;
    align-items: stretch;
  }
  
  .link-item .el-button {
    margin-left: 0 !important;
    margin-top: 8px;
    align-self: flex-end;
  }
  
  .form-footer {
    flex-direction: column;
  }
  
  .preview-meta {
    flex-direction: column;
    gap: 4px;
  }
}

.chapter-form-container::-webkit-scrollbar {
  width: 6px;
}

.chapter-form-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.chapter-form-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.chapter-form-container::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>