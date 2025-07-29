<!--
修改说明：
1. 只添加了缺失的 chapterType 字段，这是后端验证要求的必填字段
2. 保持原有的所有功能和样式不变
3. 最小化修复，解决"第 1 个章节的类型不能为空"错误
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

      <!-- 🔧 新增：章节类型字段 - 解决后端验证问题 -->
      <el-form-item label="章节类型" prop="chapterType">
        <el-select v-model="form.chapterType" placeholder="请选择章节类型" style="width: 100%">
          <el-option label="视频课程" value="video" />
          <el-option label="文档资料" value="document" />
          <el-option label="音频课程" value="audio" />
          <el-option label="测验考试" value="quiz" />
        </el-select>
        <div class="field-tip">选择章节的内容类型</div>
      </el-form-item>

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
          placeholder="请输入章节描述"
          :rows="3"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>

      <!-- 章节内容 -->
      <el-form-item label="章节内容" prop="content">
        <el-input
          v-model="form.content"
          type="textarea"
          placeholder="请输入章节内容"
          :rows="4"
          maxlength="2000"
          show-word-limit
        />
      </el-form-item>

      <!-- 关联学习资料 -->
      <div class="material-section">
        <h5>
          <el-icon><Document /></el-icon>
          关联学习资料
        </h5>
        <el-form-item prop="selectedMaterials">
          <div v-if="props.availableMaterials.length === 0" class="no-materials">
            <div class="no-materials-icon">
              <el-icon><Document /></el-icon>
            </div>
            <p>暂无可用的学习资料</p>
            <p class="tip">请先在课程基础信息中上传教学资料</p>
          </div>
          <div v-else class="material-selector">
            <div class="material-list">
              <el-checkbox-group v-model="form.selectedMaterials">
                <div
                  v-for="material in props.availableMaterials"
                  :key="material.id || material.uid"
                  class="material-item"
                >
                  <el-checkbox
                    :label="material.id || material.uid"
                    class="material-checkbox"
                  >
                    <div class="material-info">
                      <div class="material-name">
                        <el-icon><Document /></el-icon>
                        <span>{{ material.name || material.originalName || '学习资料' }}</span>
                      </div>
                      <div class="material-meta">
                        <span class="file-type">{{ getFileType(material.name) }}</span>
                        <span class="file-size">{{ formatFileSize(material.size) }}</span>
                      </div>
                    </div>
                  </el-checkbox>
                </div>
              </el-checkbox-group>
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

// 🔧 修复：表单数据添加 chapterType 字段
const form = reactive({
  id: '',
  title: '',
  description: '',
  chapterType: 'document', // 🔧 新增必填字段，设置默认值
  content: '',
  duration: 0,
  order: 1,
  status: 0,
  selectedMaterials: [] // 选中的教学资料ID数组
})

// 🔧 修复：表单验证规则添加 chapterType 验证
const rules = {
  title: [
    { required: true, message: '请输入章节标题', trigger: 'blur' },
    { min: 2, max: 100, message: '章节标题长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  chapterType: [
    { required: true, message: '请选择章节类型', trigger: 'change' }
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
    form.selectedMaterials.includes(material.id || material.uid)
  )
})

// 🔧 修复：初始化表单数据，重点修复资料选择状态
const initFormData = (data) => {

  Object.assign(form, {
    id: data.id || '',
    title: data.title || '',
    description: data.description || '',
    chapterType: data.chapterType || data.type || 'document',
    content: data.content || data.contentUrl || '',
    duration: data.duration || 0,
    order: data.order || (props.chapterIndex + 1) || 1,
    status: data.status || 0,
    // 🔧 重要修复：正确处理资料选择状态
    selectedMaterials: processSelectedMaterials(data)
  })

}

// 🔧 新增：处理选中资料的映射函数
const processSelectedMaterials = (data) => {

  // 1. 如果已经有 selectedMaterials 数组，直接使用
  if (data.selectedMaterials && Array.isArray(data.selectedMaterials)) {
    return data.selectedMaterials
  }

  // 2. 如果有 materialUrls 字符串，需要转换为对应的文件ID
  if (data.materialUrls && typeof data.materialUrls === 'string') {
    const materialUrls = data.materialUrls.split(',').filter(url => url && url.trim())

    // 🔧 关键修复：通过URL匹配找到对应的文件ID/UID
    const selectedIds = []

    materialUrls.forEach(url => {
      const trimmedUrl = url.trim()
      const normalizedUrl = normalizeUrl(trimmedUrl)
      // 在可用资料列表中找到匹配的文件，支持路径归一化
      const matchedMaterial = props.availableMaterials.find(material => {
        return normalizeUrl(material.url) === normalizedUrl
      })

      if (matchedMaterial) {
        // 使用文件的ID或UID作为选中标识
        const fileId = matchedMaterial.id || matchedMaterial.uid
        selectedIds.push(fileId)
      } else {
      }
    })

    return selectedIds
  }

  // 3. 兼容其他格式
  if (data.materialIds && Array.isArray(data.materialIds)) {
    return data.materialIds
  }

  return []
}

// 监听器
watch(() => props.chapterData, (newData) => {
  if (newData && Object.keys(newData).length > 0) {
    initFormData(newData)
  }
}, { immediate: true })

// 🔧 当可用资料加载完成后重新处理选中状态
watch(
  () => props.availableMaterials,
  (materials) => {
    if (materials && materials.length > 0 && props.chapterData) {
      form.selectedMaterials = processSelectedMaterials(props.chapterData)
    }
  },
  { deep: true }
)

// 生命周期
onMounted(() => {
  if (!props.chapterData || Object.keys(props.chapterData).length === 0) {
    form.order = (props.chapterIndex + 1) || 1
    form.chapterType = 'document' // 🔧 确保默认值
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

// 🔧 新增：统一URL格式，移除域名和查询参数
const normalizeUrl = (url) => {
  if (!url) return ''
  try {
    const u = new URL(url, window.location.origin)
    return u.pathname.replace(/^\//, '')
  } catch (e) {
    return url.replace(/^https?:\/\//, '').split('?')[0]
  }
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

// 🔧 修复：保存时确保 chapterType 字段正确传递
const handleSave = async () => {
  try {
    const valid = await formRef.value.validate()
    if (!valid) return

    // 🔧 额外验证 chapterType
    if (!form.chapterType) {
      ElMessage.error('请选择章节类型')
      return
    }

    saving.value = true

    const cleanData = {
      ...form,
      // 🔧 确保 chapterType 字段正确传递
      chapterType: form.chapterType,
      // 后端 materialUrls 字段是 String 类型，需要将数组转换为逗号分隔的字符串
      materialUrls: form.selectedMaterials.map(id => {
        const material = props.availableMaterials.find(m => m.id === id || m.uid === id)
        return material ? (material.url || material.response?.data?.url || '') : ''
      }).filter(url => url).join(',') // 转换为逗号分隔的字符串
    }

    // 清理内部使用的字段
    delete cleanData.selectedMaterials


    emit('save', cleanData)
  } catch (error) {
    ElMessage.error('表单验证失败，请检查必填项')
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
