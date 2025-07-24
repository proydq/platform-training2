<template>
  <div class="learning-page">
    <!-- 课程导航 -->
    <CourseHeader
      :course-data="courseData"
      :current-chapter-title="currentChapterTitle"
      :course-progress="courseProgress"
      @go-back="goBack"
      @toggle-bookmark="toggleBookmark"
      @show-settings="showSettings"
    />

    <!-- 主要学习区域 -->
    <div class="learning-layout">
      <!-- 课程章节侧边栏 -->
      <CourseSidebar
        :course-data="courseData"
        :current-chapter="currentChapter"
        :current-lesson="currentLesson"
        @select-lesson="selectLesson"
        @toggle-chapter="toggleChapter"
      />

      <!-- 主学习内容区 -->
      <div class="learning-main">
        <!-- 课程标题 -->
        <LessonHeader
          :lesson-data="currentLessonData"
          :watched-time="watchedTime"
          :update-date="updateDate"
        />

        <!-- 视频播放器 -->
        <VideoPlayer
          :lesson-data="currentLessonData"
          :is-playing="isPlaying"
          :show-controls="showControls"
          :video-progress="videoProgress"
          :current-time="currentTime"
          :total-time="totalTime"
          :playback-speed="playbackSpeed"
          @play-video="playVideo"
          @toggle-play="togglePlay"
          @toggle-controls="toggleVideoControls"
          @seek-video="seekVideo"
          @skip-backward="skipBackward"
          @skip-forward="skipForward"
          @change-speed="changeSpeed"
          @toggle-subtitles="toggleSubtitles"
          @toggle-fullscreen="toggleFullscreen"
        />

        <!-- 学习内容标签页 -->
        <LearningTabs
          :active-tab="activeTab"
          :lesson-data="currentLessonData || {}"
          :notes="notes"
          :quiz-data="quizData || []"
          :current-question-index="currentQuestionIndex || 0"
          :selected-answer="selectedAnswer"
          :remaining-time="remainingTime || 0"
          :note-search-keyword="noteSearchKeyword"
          @show-tab="showTab"
          @show-add-note-modal="showAddNoteModal"
          @edit-note="editNote"
          @delete-note="deleteNote"
          @share-note="shareNote"
          @jump-to-time="jumpToTime"
          @select-option="selectOption"
          @previous-question="previousQuestion"
          @next-question="nextQuestion"
          @skip-question="skipQuestion"
          @update-search="(keyword) => noteSearchKeyword = keyword"
        />

        <!-- 课程导航 -->
        <LessonNavigation
          :has-previous-lesson="hasPreviousLesson"
          :has-next-lesson="hasNextLesson"
          :previous-lesson-title="previousLessonTitle"
          :next-lesson-title="nextLessonTitle"
          @previous-lesson="previousLesson"
          @next-lesson="nextLesson"
          @complete-lesson="completeLesson"
        />
      </div>
    </div>

    <!-- 浮动添加笔记按钮 -->
    <FloatingNoteButton @show-note-modal="showAddNoteModal" />

    <!-- 添加笔记弹窗 -->
    <NoteModal
      v-if="showNoteModal"
      :new-note="newNote"
      :current-time="formatTime(currentTime)"
      @save-note="saveNote"
      @hide-modal="hideNoteModal"
    />

    <!-- 成功提示 -->
    <SuccessMessage
      v-if="showSuccessMessage"
      :message="successMessageText"
    />
  </div>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'

// 导入子组件
import CourseHeader from '@/components/learning/CourseHeader.vue'
import CourseSidebar from '@/components/learning/CourseSidebar.vue'
import LessonHeader from '@/components/learning/LessonHeader.vue'
import VideoPlayer from '@/components/learning/VideoPlayer.vue'
import LearningTabs from '@/components/learning/LearningTabs.vue'
import LessonNavigation from '@/components/learning/LessonNavigation.vue'
import FloatingNoteButton from '@/components/learning/FloatingNoteButton.vue'
import NoteModal from '@/components/learning/NoteModal.vue'
import SuccessMessage from '@/components/learning/SuccessMessage.vue'

// 导入数据和逻辑
import { useLearningData } from '@/composables/useLearningData'
import { useLearningActions } from '@/composables/useLearningActions'
import { useVideoPlayer } from '@/composables/useVideoPlayer'
import { useNotes } from '@/composables/useNotes'

const route = useRoute()
const router = useRouter()

// 使用组合式函数
const {
  courseData,
  currentChapter,
  currentLesson,
  activeTab,
  courseProgress,
  currentChapterTitle,
  currentLessonData,
  watchedTime,
  updateDate,
  hasPreviousLesson,
  hasNextLesson,
  previousLessonTitle,
  nextLessonTitle
} = useLearningData(route.params.courseId)

const {
  isPlaying,
  showControls,
  videoProgress,
  currentTime,
  totalTime,
  playbackSpeed,
  playVideo,
  togglePlay,
  toggleVideoControls,
  seekVideo,
  skipBackward,
  skipForward,
  changeSpeed,
  toggleSubtitles,
  toggleFullscreen,
  formatTime
} = useVideoPlayer()

const {
  notes,
  noteSearchKeyword,
  showNoteModal,
  newNote,
  showAddNoteModal,
  hideNoteModal,
  saveNote,
  editNote,
  deleteNote,
  shareNote,
  jumpToTime
} = useNotes(currentTime)

const {
  showSuccessMessage,
  successMessageText,
  quizData,
  currentQuestionIndex,
  selectedAnswer,
  remainingTime,
  selectLesson,
  toggleChapter,
  showTab,
  selectOption,
  previousQuestion,
  nextQuestion,
  skipQuestion,
  previousLesson,
  nextLesson,
  completeLesson,
  goBack,
  toggleBookmark,
  showSettings
} = useLearningActions(router, courseData, currentChapter, currentLesson, activeTab)
</script>

<style scoped>
/* 学习页面样式 - 简化版，继承Layout的背景和基础样式 */
.learning-page {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  color: #333;
  padding: 0; /* 移除padding，由Layout控制 */
}

/* 主要学习区域 */
.learning-layout {
  display: grid;
  grid-template-columns: 350px 1fr;
  gap: 20px;
  height: calc(100vh - 180px); /* 调整高度，考虑Layout的头部 */
}

/* 主学习内容区 */
.learning-main {
  display: flex;
  flex-direction: column;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 15px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .learning-layout {
    grid-template-columns: 300px 1fr;
  }
}

@media (max-width: 768px) {
  .learning-layout {
    grid-template-columns: 1fr;
    grid-template-rows: auto 1fr;
    height: auto;
  }
}
</style>
