<!-- 
修改说明：
1. 移除了原来的"资料链接"手动输入方式
2. 改为选择已上传的教学资料建立绑定关系
3. 使用复选框列表展示可选的教学资料
4. 显示文件大小、类型等信息
5. 保留视频链接功能

使用方法：
在 CourseForm.vue 中传入 availableMaterials 属性：
<ChapterForm
  :available-materials="fileListState.materials"
  ...其他属性
/>
-->
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
        <div class="field-tip">支持 Markdown 格式</div>
      </el-form-item>
      
      <!-- 关联资料 -->
      <div class="material-section">
        <h5>📁 关联资料</h5>
        
        <!-- 选择已上传的教学资料 -->
        <el-form-item label="教学资料">
          <div class="material-selector">
            <el-checkbox-group v-model="form.selectedMaterials" class="material-list">
              <div 
                v-for="material in availableMaterials" 
                :key="material.id"
                class="material-item"
              >
                <el-checkbox :label="material.id" class="material-checkbox">
                  <div class="material-info">
                    <div class="material-name">
                      <el-icon><Document /></el-icon>
                      <span>{{ material.name }}</span>
                    </div>
                    <div class="material-meta">
                      <span class="file-size">{{ formatFileSize(material.size) }}</span>
                      <span class="file-type">{{ getFileType(material.name) }}</span>
                    </div>
                  </div>
                </el-checkbox>
              </div>
            </el-checkbox-group>
            
            <div v-if="availableMaterials.length === 0" class="no-materials">
              <div class="no-materials-icon">📄</div>
              <p>暂无可选择的教学资料</p>
              <p class="tip">请先在"课程资源"中上传教学资料</p>
            </div>
          </div>
          <div class="field-tip">选择与本章节相关的教学资料，学员可在学习过程中下载查看</div>
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
        
        <div v-if="selectedMaterialsList.length > 0" class="preview-materials">
          <h4>关联资料</h4>
          <ul>
            <li v-for="material in selectedMaterialsList" :key="material.id">
              <el-icon><Document /></el-icon>
              <span>{{ material.name }}</span>
              <span class="material-size">{{ formatFileSize(material.size) }}</span>
            </li>
          </ul>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Document } from '@element-plus/icons-vue'

// Props & Emits
const props = defineProps({
  chapterData: {
    type: Object,
    default: () => ({})
  },
  chapterIndex: {
    type: Number,
    default: -1
  },
  // 从父组件传入的可用教学资料列表
  availableMaterials: {
    type: Array,
    default: () => []
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
  selectedMaterials: [] // 选中的教学资料ID数组
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

// 计算属性
const selectedMaterialsList = computed(() => {
  return props.availableMaterials.filter(material => 
    form.selectedMaterials.includes(material.id)
  )
})

// 🔧 核心修复：将 initFormData 函数声明移到 watch 之前
const initFormData = (data) => {
  Object.assign(form, {
    id: data.id || '',
    title: data.title || '',
    description: data.description || '',
    content: data.content || '',
    duration: data.duration || 0,
    order: data.order || (props.chapterIndex + 1) || 1,
    status: data.status || 0,
    selectedMaterials: data.selectedMaterials || data.materialIds || []
  })
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

// 工具方法
const formatFileSize = (size) => {
  if (!size) return '未知大小'
  if (size < 1024) return `${size}B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)}KB`
  return `${(size / (1024 * 1024)).toFixed(1)}MB`
}

const getFileType = (filename) => {
  if (!filename) return ''
  const ext = filename.split('.').pop()?.toUpperCase()
  return ext || ''
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
      materialIds: form.selectedMaterials // 将选中的资料ID传给后端
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

.material-section {
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.material-section h5 {
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

.material-selector {
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  max-height: 300px;
  overflow-y: auto;
}

.material-list {
  width: 100%;
  padding: 16px;
}

.material-item {
  margin-bottom: 12px;
}

.material-item:last-child {
  margin-bottom: 0;
}

.material-checkbox {
  width: 100%;
  margin-right: 0;
}

.material-checkbox :deep(.el-checkbox__label) {
  width: 100%;
  padding-left: 8px;
}

.material-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.material-name {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #303133;
}

.material-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #909399;
}

.file-size {
  background: #f0f2f5;
  padding: 2px 6px;
  border-radius: 4px;
}

.file-type {
  background: #e7f4ff;
  color: #409eff;
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: 500;
}

.no-materials {
  text-align: center;
  padding: 40px 20px;
  color: #909399;
}

.no-materials-icon {
  font-size: 32px;
  margin-bottom: 12px;
  opacity: 0.6;
}

.no-materials .tip {
  font-size: 12px;
  color: #c0c4cc;
  margin-top: 4px;
}

.form-footer {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  padding: 20px 16px 0 16px;
  border-top: 1px solid #ebeef5;
  margin-top: 24px;
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
  margin-bottom: 12px;
  color: #303133;
}

.preview-meta {
  display: flex;
  gap: 8px;
}

.preview-description,
.preview-content,
.preview-materials {
  margin-bottom: 24px;
}

.preview-description h4,
.preview-content h4,
.preview-materials h4 {
  color: #303133;
  margin-bottom: 12px;
  font-size: 16px;
}

.content-text {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 8px;
  white-space: pre-wrap;
  word-break: break-word;
}

.preview-materials ul {
  list-style: none;
  padding: 0;
}

.preview-materials li {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
  border-bottom: 1px solid #f0f2f5;
}

.preview-materials li:last-child {
  border-bottom: none;
}

.material-size {
  margin-left: auto;
  font-size: 12px;
  color: #909399;
}

.chapter-form-container::-webkit-scrollbar,
.material-selector::-webkit-scrollbar {
  width: 6px;
}

.chapter-form-container::-webkit-scrollbar-track,
.material-selector::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.chapter-form-container::-webkit-scrollbar-thumb,
.material-selector::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.chapter-form-container::-webkit-scrollbar-thumb:hover,
.material-selector::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

@media (max-width: 768px) {
  .preview-meta {
    flex-wrap: wrap;
    gap: 4px;
  }
  
  .material-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
  
  .form-footer {
    flex-direction: column;
    gap: 12px;
  }
}
</style>