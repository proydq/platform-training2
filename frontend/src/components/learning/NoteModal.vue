<template>
  <teleport to="body">
    <transition name="modal-fade">
      <div v-if="show" class="modal-overlay" @click="handleOverlayClick">
        <div class="modal-container" @click.stop>
          <!-- 模态框头部 -->
          <div class="modal-header">
            <h3 class="modal-title">
              <span class="title-icon">📝</span>
              {{ isEditing ? '编辑笔记' : '添加笔记' }}
            </h3>
            <button @click="$emit('hide')" class="close-btn">
              <span class="close-icon">✕</span>
            </button>
          </div>

          <!-- 模态框内容 -->
          <div class="modal-body">
            <form @submit.prevent="handleSave" class="note-form">
              <!-- 时间点和标签 -->
              <div class="form-row">
                <div class="form-group">
                  <label class="form-label">
                    <span class="label-icon">🕐</span>
                    时间点
                  </label>
                  <input
                    v-model="localNote.timestamp"
                    type="text"
                    class="form-input"
                    placeholder="00:00"
                    readonly
                  >
                </div>
                <div class="form-group">
                  <label class="form-label">
                    <span class="label-icon">🏷️</span>
                    标签
                  </label>
                  <select v-model="localNote.tag" class="form-select">
                    <option value="important">💡 重要概念</option>
                    <option value="question">❓ 疑问</option>
                    <option value="insight">💭 心得体会</option>
                    <option value="practice">🎯 实践要点</option>
                    <option value="summary">📋 总结</option>
                  </select>
                </div>
              </div>

              <!-- 笔记内容 -->
              <div class="form-group">
                <label class="form-label">
                  <span class="label-icon">📄</span>
                  笔记内容
                  <span class="word-count">{{ wordCount }}/500字</span>
                </label>
                <textarea
                  ref="contentTextarea"
                  v-model="localNote.content"
                  class="form-textarea"
                  placeholder="在这里记录你的学习心得、疑问或重要知识点..."
                  maxlength="500"
                  @input="updateWordCount"
                ></textarea>
              </div>

              <!-- 快捷插入 -->
              <div class="quick-insert">
                <span class="quick-insert-label">快捷插入：</span>
                <div class="quick-buttons">
                  <button
                    type="button"
                    v-for="template in quickTemplates"
                    :key="template.key"
                    @click="insertTemplate(template.content)"
                    class="quick-btn"
                  >
                    {{ template.label }}
                  </button>
                </div>
              </div>

              <!-- 操作按钮 -->
              <div class="modal-actions">
                <button type="button" @click="$emit('hide')" class="btn btn-secondary">
                  取消
                </button>
                <button
                  type="submit"
                  class="btn btn-primary"
                  :disabled="!localNote.content.trim()"
                >
                  <span class="btn-icon">💾</span>
                  {{ isEditing ? '更新笔记' : '保存笔记' }}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </transition>
  </teleport>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'

const props = defineProps({
  show: {
    type: Boolean,
    required: true
  },
  note: {
    type: Object,
    default: () => ({
      content: '',
      timestamp: '00:00',
      tag: 'important'
    })
  },
  currentTime: {
    type: String,
    default: '00:00'
  }
})

const emit = defineEmits(['hide', 'save'])

// 本地笔记数据
const localNote = ref({
  content: '',
  timestamp: '00:00',
  tag: 'important'
})

const contentTextarea = ref(null)
const wordCount = ref(0)

// 是否为编辑模式
const isEditing = computed(() => {
  return props.note.id !== undefined
})

// 快捷模板
const quickTemplates = [
  { key: 'important', label: '重点', content: '🔥 重点：' },
  { key: 'question', label: '疑问', content: '❓ 疑问：' },
  { key: 'summary', label: '总结', content: '📝 总结：' },
  { key: 'todo', label: '待办', content: '✅ 待办：' }
]

// 监听显示状态
watch(() => props.show, (newVal) => {
  if (newVal) {
    // 初始化本地数据
    localNote.value = {
      ...props.note,
      timestamp: props.note.timestamp || props.currentTime
    }
    updateWordCount()

    // 聚焦到文本框
    nextTick(() => {
      if (contentTextarea.value) {
        contentTextarea.value.focus()
        // 如果是新建笔记，光标移到末尾；如果是编辑，选中所有内容
        if (isEditing.value) {
          contentTextarea.value.select()
        } else {
          contentTextarea.value.setSelectionRange(
            contentTextarea.value.value.length,
            contentTextarea.value.value.length
          )
        }
      }
    })
  }
})

// 更新字数统计
const updateWordCount = () => {
  wordCount.value = localNote.value.content.length
}

// 插入模板
const insertTemplate = (template) => {
  const textarea = contentTextarea.value
  if (!textarea) return

  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const beforeText = localNote.value.content.substring(0, start)
  const afterText = localNote.value.content.substring(end)

  // 如果光标在行首或前面是空格，直接插入；否则先插入换行
  const needNewLine = start > 0 && beforeText[start - 1] !== '\n' && beforeText[start - 1] !== ' '
  const insertText = needNewLine ? `\n${template}` : template

  localNote.value.content = beforeText + insertText + afterText
  updateWordCount()

  // 更新光标位置
  nextTick(() => {
    const newPosition = start + insertText.length
    textarea.setSelectionRange(newPosition, newPosition)
    textarea.focus()
  })
}

// 处理覆盖层点击
const handleOverlayClick = () => {
  // 如果有未保存的内容，提示用户
  if (localNote.value.content.trim() && !isEditing.value) {
    if (confirm('有未保存的内容，确定要关闭吗？')) {
      emit('hide')
    }
  } else {
    emit('hide')
  }
}

// 处理保存
const handleSave = () => {
  if (!localNote.value.content.trim()) return

  const noteData = {
    ...localNote.value,
    content: localNote.value.content.trim(),
    createdAt: isEditing.value ? props.note.createdAt : new Date().toISOString()
  }

  emit('save', noteData)
}

// 键盘快捷键
const handleKeydown = (event) => {
  // Ctrl/Cmd + Enter 保存
  if ((event.ctrlKey || event.metaKey) && event.key === 'Enter') {
    event.preventDefault()
    handleSave()
  }
  // Esc 关闭
  if (event.key === 'Escape') {
    event.preventDefault()
    handleOverlayClick()
  }
}

// 监听键盘事件
watch(() => props.show, (newVal) => {
  if (newVal) {
    document.addEventListener('keydown', handleKeydown)
  } else {
    document.removeEventListener('keydown', handleKeydown)
  }
})
</script>

<style scoped>
/* 模态框覆盖层 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  backdrop-filter: blur(4px);
  padding: 20px;
}

/* 模态框容器 */
.modal-container {
  background: white;
  border-radius: 20px;
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.3);
  width: 100%;
  max-width: 600px;
  max-height: 90vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  animation: modalSlideIn 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

/* 模态框头部 */
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 25px 30px;
  border-bottom: 1px solid #e9ecef;
  background: linear-gradient(135deg, #f8f9ff, #ffffff);
}

.modal-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #2c3e50;
}

.title-icon {
  font-size: 22px;
}

.close-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 8px;
  border-radius: 50%;
  color: #666;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
}

.close-btn:hover {
  background: #f8f9fa;
  color: #333;
}

.close-icon {
  font-size: 18px;
}

/* 模态框内容 */
.modal-body {
  padding: 30px;
  overflow-y: auto;
  flex: 1;
}

/* 表单样式 */
.note-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 500;
  color: #2c3e50;
  font-size: 14px;
}

.label-icon {
  margin-right: 6px;
}

.word-count {
  font-size: 12px;
  color: #666;
  font-weight: normal;
}

.form-input,
.form-select {
  padding: 12px;
  border: 2px solid #e9ecef;
  border-radius: 10px;
  font-size: 14px;
  transition: all 0.3s ease;
  background: #f8f9fa;
}

.form-input:focus,
.form-select:focus {
  outline: none;
  border-color: #667eea;
  background: white;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.form-textarea {
  padding: 15px;
  border: 2px solid #e9ecef;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.6;
  resize: vertical;
  min-height: 120px;
  font-family: inherit;
  transition: all 0.3s ease;
  background: #f8f9fa;
}

.form-textarea:focus {
  outline: none;
  border-color: #667eea;
  background: white;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.form-textarea::placeholder {
  color: #999;
}

/* 快捷插入 */
.quick-insert {
  padding: 15px;
  background: #f8f9ff;
  border-radius: 10px;
  border: 1px solid #e3f2fd;
}

.quick-insert-label {
  font-size: 12px;
  color: #666;
  margin-bottom: 8px;
  display: block;
}

.quick-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.quick-btn {
  background: white;
  border: 1px solid #667eea;
  color: #667eea;
  padding: 6px 12px;
  border-radius: 15px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.quick-btn:hover {
  background: #667eea;
  color: white;
  transform: translateY(-1px);
}

/* 操作按钮 */
.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  padding-top: 20px;
  border-top: 1px solid #f1f3f4;
}

.btn {
  padding: 12px 24px;
  border-radius: 25px;
  border: none;
  cursor: pointer;
  font-weight: 500;
  font-size: 14px;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn-secondary {
  background: #f8f9fa;
  color: #666;
  border: 1px solid #e9ecef;
}

.btn-secondary:hover {
  background: #e9ecef;
  color: #333;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.2);
}

.btn-icon {
  font-size: 14px;
}

/* 动画 */
@keyframes modalSlideIn {
  from {
    opacity: 0;
    transform: scale(0.8) translateY(-50px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

/* 模态框过渡 */
.modal-fade-enter-active {
  transition: all 0.3s ease;
}

.modal-fade-leave-active {
  transition: all 0.3s ease;
}

.modal-fade-enter-from {
  opacity: 0;
}

.modal-fade-leave-to {
  opacity: 0;
}

.modal-fade-enter-from .modal-container {
  transform: scale(0.8) translateY(-50px);
}

.modal-fade-leave-to .modal-container {
  transform: scale(0.8) translateY(-50px);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .modal-container {
    margin: 10px;
    max-height: calc(100vh - 20px);
  }

  .modal-header,
  .modal-body {
    padding: 20px;
  }

  .form-row {
    grid-template-columns: 1fr;
    gap: 15px;
  }

  .quick-buttons {
    justify-content: center;
  }

  .modal-actions {
    flex-direction: column-reverse;
  }

  .btn {
    width: 100%;
    justify-content: center;
  }
}

/* 减少动画模式 */
@media (prefers-reduced-motion: reduce) {
  .modal-container {
    animation: none;
  }

  .modal-fade-enter-active,
  .modal-fade-leave-active {
    transition: opacity 0.2s ease;
  }

  .btn,
  .quick-btn,
  .form-input,
  .form-textarea {
    transition: none;
  }
}

/* 高对比度模式 */
@media (prefers-contrast: high) {
  .modal-container {
    border: 2px solid #333;
  }

  .form-input,
  .form-select,
  .form-textarea {
    border-color: #333;
  }

  .btn-primary {
    background: #0066cc;
  }
}
</style>
