<template>
  <div class="notes-section">
    <!-- 笔记工具栏 -->
    <div class="notes-toolbar">
      <div class="toolbar-left">
        <div class="search-box">
          <input
            v-model="localSearchKeyword"
            type="text"
            placeholder="🔍 搜索笔记内容..."
            class="search-input"
            @input="handleSearchInput"
          />
          <button v-if="localSearchKeyword" @click="clearSearch" class="clear-btn">
            ✕
          </button>
        </div>
      </div>
      <div class="toolbar-right">
        <button @click="$emit('showAddNoteModal')" class="add-note-btn">
          <span class="btn-icon">✏️</span>
          <span class="btn-text">添加笔记</span>
        </button>
      </div>
    </div>

    <!-- 笔记统计 -->
    <div class="notes-stats">
      <div class="stat-item">
        <span class="stat-icon">📄</span>
        <span class="stat-label">总笔记</span>
        <span class="stat-value">{{ notes.length }}</span>
      </div>
      <div class="stat-item">
        <span class="stat-icon">⭐</span>
        <span class="stat-label">今日新增</span>
        <span class="stat-value">{{ todayNotesCount }}</span>
      </div>
      <div class="stat-item">
        <span class="stat-icon">📊</span>
        <span class="stat-label">总字数</span>
        <span class="stat-value">{{ totalWordsCount }}</span>
      </div>
    </div>

    <!-- 笔记列表 -->
    <div class="notes-list">
      <!-- 空状态 -->
      <div v-if="filteredNotes.length === 0" class="empty-state">
        <div class="empty-icon">📝</div>
        <h3 class="empty-title">
          {{ searchKeyword ? '未找到相关笔记' : '还没有笔记' }}
        </h3>
        <p class="empty-description">
          {{ searchKeyword ? '尝试使用其他关键词搜索' : '开始记录您的学习心得吧！' }}
        </p>
        <button v-if="!searchKeyword" @click="$emit('showAddNoteModal')" class="empty-action-btn">
          ✏️ 添加第一条笔记
        </button>
      </div>

      <!-- 笔记卡片 -->
      <div v-else class="notes-grid">
        <div
          v-for="note in filteredNotes"
          :key="note.id"
          class="note-card"
          :class="{ 'highlighted': isHighlighted(note) }"
        >
          <!-- 笔记头部 -->
          <div class="note-header">
            <button @click="$emit('jumpToTime', note.timestamp)" class="time-badge">
              <span class="time-icon">🕐</span>
              <span class="time-text">{{ formatTime(note.timestamp) }}</span>
            </button>
            <div class="note-actions">
              <button @click="$emit('editNote', note)" class="action-btn edit-btn" title="编辑">
                ✏️
              </button>
              <button @click="$emit('shareNote', note)" class="action-btn share-btn" title="分享">
                📤
              </button>
              <button @click="$emit('deleteNote', note.id)" class="action-btn delete-btn" title="删除">
                🗑️
              </button>
            </div>
          </div>

          <!-- 笔记内容 -->
          <div class="note-content">
            <p class="note-text" v-html="highlightSearchTerm(note.content)"></p>
          </div>

          <!-- 笔记元信息 -->
          <div class="note-meta">
            <span class="note-date">{{ formatDate(note.createdAt) }}</span>
            <span class="note-words">{{ getWordsCount(note.content) }} 字</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
  notes: {
    type: Array,
    required: true
  },
  searchKeyword: {
    type: String,
    default: ''
  }
})

const emit = defineEmits([
  'showAddNoteModal',
  'editNote',
  'deleteNote',
  'shareNote',
  'jumpToTime',
  'updateSearch'
])

// 本地搜索关键词
const localSearchKeyword = ref(props.searchKeyword)

// 监听搜索关键词变化
watch(() => props.searchKeyword, (newVal) => {
  localSearchKeyword.value = newVal
})

// 处理搜索输入
const handleSearchInput = () => {
  emit('updateSearch', localSearchKeyword.value)
}

// 清除搜索
const clearSearch = () => {
  localSearchKeyword.value = ''
  emit('updateSearch', '')
}

// 过滤笔记
const filteredNotes = computed(() => {
  if (!localSearchKeyword.value) return props.notes

  const keyword = localSearchKeyword.value.toLowerCase()
  return props.notes.filter(note =>
    note.content.toLowerCase().includes(keyword)
  )
})

// 今日笔记数量
const todayNotesCount = computed(() => {
  const today = new Date().toDateString()
  return props.notes.filter(note =>
    new Date(note.createdAt).toDateString() === today
  ).length
})

// 总字数统计
const totalWordsCount = computed(() => {
  return props.notes.reduce((total, note) => {
    return total + getWordsCount(note.content)
  }, 0)
})

// 检查笔记是否高亮
const isHighlighted = (note) => {
  return localSearchKeyword.value &&
    note.content.toLowerCase().includes(localSearchKeyword.value.toLowerCase())
}

// 高亮搜索关键词
const highlightSearchTerm = (text) => {
  if (!localSearchKeyword.value) return text

  const regex = new RegExp(`(${localSearchKeyword.value})`, 'gi')
  return text.replace(regex, '<mark class="search-highlight">$1</mark>')
}

// 格式化时间
const formatTime = (seconds) => {
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = Math.floor(seconds % 60)
  return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`
}

// 格式化日期
const formatDate = (dateString) => {
  const date = new Date(dateString)
  const now = new Date()
  const diff = now - date

  // 如果是今天
  if (date.toDateString() === now.toDateString()) {
    return `今天 ${date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })}`
  }

  // 如果是昨天
  const yesterday = new Date(now)
  yesterday.setDate(yesterday.getDate() - 1)
  if (date.toDateString() === yesterday.toDateString()) {
    return `昨天 ${date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })}`
  }

  // 其他日期
  return date.toLocaleDateString('zh-CN', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 计算字数
const getWordsCount = (text) => {
  return text.length
}
</script>

<style scoped>
.notes-section {
  height: 100%;
  display: flex;
  flex-direction: column;
}

/* 工具栏 */
.notes-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  gap: 15px;
}

.search-box {
  position: relative;
  flex: 1;
  max-width: 300px;
}

.search-input {
  width: 100%;
  padding: 12px 40px 12px 15px;
  border: 2px solid #e9ecef;
  border-radius: 25px;
  font-size: 14px;
  transition: all 0.3s ease;
  background: #f8f9fa;
}

.search-input:focus {
  outline: none;
  border-color: #667eea;
  background: white;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.clear-btn {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: #999;
  cursor: pointer;
  font-size: 16px;
  padding: 0;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.clear-btn:hover {
  color: #666;
}

.add-note-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border: none;
  padding: 12px 20px;
  border-radius: 25px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.add-note-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

/* 统计区域 */
.notes-stats {
  display: flex;
  gap: 15px;
  margin-bottom: 25px;
  overflow-x: auto;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 8px;
  background: white;
  border: 1px solid #e9ecef;
  border-radius: 12px;
  padding: 15px 20px;
  min-width: 120px;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.stat-icon {
  font-size: 18px;
}

.stat-label {
  font-size: 13px;
  color: #666;
}

.stat-value {
  font-size: 16px;
  font-weight: 600;
  color: #667eea;
  margin-left: auto;
}

/* 笔记列表 */
.notes-list {
  flex: 1;
  overflow-y: auto;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 300px;
  text-align: center;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 20px;
  opacity: 0.5;
}

.empty-title {
  font-size: 20px;
  color: #666;
  margin: 0 0 10px 0;
}

.empty-description {
  font-size: 14px;
  color: #999;
  margin: 0 0 25px 0;
}

.empty-action-btn {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 25px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
}

.empty-action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.3);
}

/* 笔记网格 */
.notes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

/* 笔记卡片 */
.note-card {
  background: white;
  border: 1px solid #e9ecef;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.note-card:hover {
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  transform: translateY(-3px);
}

.note-card.highlighted {
  border-color: #667eea;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.2);
}

/* 笔记头部 */
.note-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.time-badge {
  display: flex;
  align-items: center;
  gap: 5px;
  background: #667eea;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 15px;
  cursor: pointer;
  font-size: 12px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.time-badge:hover {
  background: #764ba2;
  transform: scale(1.05);
}

.note-actions {
  display: flex;
  gap: 5px;
}

.action-btn {
  background: none;
  border: 1px solid #e9ecef;
  border-radius: 6px;
  width: 32px;
  height: 32px;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.edit-btn:hover {
  border-color: #28a745;
  background: #f8fff9;
}

.share-btn:hover {
  border-color: #17a2b8;
  background: #f1fcfd;
}

.delete-btn:hover {
  border-color: #dc3545;
  background: #fff5f5;
}

/* 笔记内容 */
.note-content {
  margin-bottom: 15px;
}

.note-text {
  font-size: 14px;
  line-height: 1.6;
  color: #444;
  margin: 0;
  word-break: break-word;
}

/* 搜索高亮 */
:deep(.search-highlight) {
  background: #fff3cd;
  color: #856404;
  padding: 1px 3px;
  border-radius: 3px;
}

/* 笔记元信息 */
.note-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #999;
  border-top: 1px solid #f1f3f4;
  padding-top: 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .notes-toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .search-box {
    max-width: none;
    margin-bottom: 10px;
  }

  .notes-stats {
    flex-wrap: wrap;
  }

  .notes-grid {
    grid-template-columns: 1fr;
  }

  .note-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .note-actions {
    align-self: flex-end;
  }
}

/* 滚动条样式 */
.notes-list::-webkit-scrollbar {
  width: 6px;
}

.notes-list::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.notes-list::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.notes-list::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>
