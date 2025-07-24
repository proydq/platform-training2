<template>
  <div class="learning-tabs">
    <!-- 标签页导航 -->
    <div class="content-tabs">
      <button
        v-for="tab in tabs"
        :key="tab.key"
        class="tab-button"
        :class="{ active: activeTab === tab.key }"
        @click="$emit('showTab', tab.key)"
      >
        {{ tab.label }}
      </button>
    </div>

    <div class="tab-content">
      <!-- 课程介绍 -->
      <div v-if="activeTab === 'intro'" class="tab-panel active">
        <CourseIntro :lesson-data="lessonData" />
      </div>

      <!-- 我的笔记 -->
      <div v-if="activeTab === 'notes'" class="tab-panel active">
        <NotesSection
          :notes="notes"
          :search-keyword="noteSearchKeyword"
          @show-add-note-modal="$emit('showAddNoteModal')"
          @edit-note="$emit('editNote', $event)"
          @delete-note="$emit('deleteNote', $event)"
          @share-note="$emit('shareNote', $event)"
          @jump-to-time="$emit('jumpToTime', $event)"
          @update-search="$emit('updateSearch', $event)"
        />
      </div>

      <!-- 课堂测验 -->
      <div v-if="activeTab === 'quiz'" class="tab-panel active">
        <QuizSection
          :quiz-data="quizData"
          :current-question-index="currentQuestionIndex"
          :selected-answer="selectedAnswer"
          :remaining-time="remainingTime"
          @select-option="$emit('selectOption', $event)"
          @previous-question="$emit('previousQuestion')"
          @next-question="$emit('nextQuestion')"
          @skip-question="$emit('skipQuestion')"
        />
      </div>

      <!-- 相关资料 -->
      <div v-if="activeTab === 'resources'" class="tab-panel active">
        <ResourcesSection :lesson-data="lessonData" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import CourseIntro from './CourseIntro.vue'
import NotesSection from './NotesSection.vue'
import QuizSection from './QuizSection.vue'
import ResourcesSection from './ResourcesSection.vue'

const props = defineProps({
  activeTab: {
    type: String,
    required: true
  },
  lessonData: {
    type: Object,
    required: true
  },
  notes: {
    type: Array,
    required: true
  },
  quizData: {
    type: Array,
    required: true
  },
  currentQuestionIndex: {
    type: Number,
    required: true
  },
  selectedAnswer: {
    type: Number,
    default: null
  },
  remainingTime: {
    type: Number,
    required: true
  },
  noteSearchKeyword: {
    type: String,
    default: ''
  }
})

defineEmits([
  'showTab',
  'showAddNoteModal',
  'editNote',
  'deleteNote',
  'shareNote',
  'jumpToTime',
  'selectOption',
  'previousQuestion',
  'nextQuestion',
  'skipQuestion',
  'updateSearch'
])

// 标签页配置
const tabs = computed(() => [
  { key: 'intro', label: '📋 课程介绍' },
  { key: 'notes', label: `📝 我的笔记 (${props.notes.length})` },
  { key: 'quiz', label: '🧠 课堂测验' },
  { key: 'resources', label: '📎 相关资料' }
])
</script>

<style scoped>
/* 学习内容标签页 */
.learning-tabs {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.content-tabs {
  display: flex;
  border-bottom: 1px solid #e0e0e0;
  background: #f8f9fa;
  flex-shrink: 0;
}

.tab-button {
  padding: 15px 25px;
  background: none;
  border: none;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  color: #666;
  transition: all 0.3s ease;
  border-bottom: 3px solid transparent;
  position: relative;
  overflow: hidden;
}

.tab-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(102, 126, 234, 0.1), transparent);
  transition: left 0.5s ease;
}

.tab-button:hover::before {
  left: 100%;
}

.tab-button.active {
  color: #667eea;
  border-bottom-color: #667eea;
  background: white;
  font-weight: 600;
}

.tab-button:hover {
  color: #667eea;
  background: rgba(102, 126, 234, 0.05);
}

.tab-content {
  flex: 1;
  overflow: hidden;
}

.tab-panel {
  height: 100%;
  overflow-y: auto;
  padding: 30px;
}

/* 自定义滚动条 */
.tab-panel::-webkit-scrollbar {
  width: 8px;
}

.tab-panel::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.tab-panel::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

.tab-panel::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .content-tabs {
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
  }

  .tab-button {
    padding: 12px 20px;
    font-size: 13px;
    white-space: nowrap;
    flex-shrink: 0;
  }

  .tab-panel {
    padding: 20px;
  }
}

@media (max-width: 480px) {
  .content-tabs {
    padding: 0 10px;
  }

  .tab-button {
    padding: 10px 15px;
    font-size: 12px;
  }

  .tab-panel {
    padding: 15px;
  }
}

/* 标签页切换动画 */
.tab-panel {
  animation: fadeInUp 0.3s ease-in-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 标签指示器动画 */
.tab-button.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #667eea, #764ba2);
  animation: slideIn 0.3s ease-in-out;
}

@keyframes slideIn {
  from {
    transform: scaleX(0);
  }
  to {
    transform: scaleX(1);
  }
}
</style>
