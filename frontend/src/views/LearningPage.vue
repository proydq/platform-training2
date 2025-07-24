<template>
  <div class="learning-page">
    <!-- 参考Layout的头部导航样式 -->
    <div class="header">
      <div class="logo">
        <div class="logo-icon">🎓</div>
        <h1>智能培训系统</h1>
      </div>

      <div class="nav-menu">
        <div
          class="nav-item"
          :class="{ active: false }"
          @click="router.push('/dashboard')"
        >
          仪表板
        </div>
        <div
          class="nav-item active"
          @click="router.push('/courses')"
        >
          我的课程
        </div>
        <div
          class="nav-item"
          :class="{ active: false }"
          @click="router.push('/exams')"
        >
          考试中心
        </div>
      </div>

      <div class="user-info">
        <div class="user-name">{{ userName }}</div>
        <div class="avatar">{{ userAvatar }}</div>
        <button class="logout-btn" @click="handleLogout">退出登录</button>
      </div>
    </div>

    <div class="container">
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
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

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
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 用户信息（参考Layout）
const userName = computed(() => {
  return userStore.userInfo?.name || userStore.userInfo?.username || '用户'
})

const userAvatar = computed(() => {
  const name = userName.value
  return name.charAt(0).toUpperCase()
})

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
  filteredNotes,
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
  showSettings,
  showSuccessMsg
} = useLearningActions(router, courseData, currentChapter, currentLesson, activeTab)

// 退出登录处理（参考Layout）
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要退出登录吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await userStore.logout()
    ElMessage.success('已退出登录')
    router.replace('/login')
  } catch (error) {
    // 用户取消
    console.log('用户取消退出登录')
  }
}
</script>

<style scoped>
/* 全屏背景布局 - 参考Layout样式 */
.learning-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  width: 100%;
  padding: 20px;
  position: relative;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  color: #333;
}

/* 为了确保背景完全平铺，参考Layout */
.learning-page::before {
  content: '';
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  z-index: -1;
}

/* 头部导航 - 完全参考Layout样式 */
.header {
  max-width: 1400px;
  margin: 0 auto 30px auto;
  position: relative;
  z-index: 1;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 15px;
  padding: 20px 30px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  gap: 15px;
}

.logo-icon {
  font-size: 32px;
}

.logo h1 {
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  color: transparent;
  margin: 0;
  font-size: 24px;
  font-weight: 700;
}

.nav-menu {
  display: flex;
  gap: 30px;
}

.nav-item {
  cursor: pointer;
  padding: 12px 20px;
  border-radius: 25px;
  font-weight: 500;
  color: #666;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.nav-item:hover {
  color: #667eea;
  background: rgba(102, 126, 234, 0.1);
  transform: translateY(-2px);
}

.nav-item.active {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.user-name {
  font-weight: 500;
  color: #2c3e50;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 16px;
}

.logout-btn {
  padding: 8px 16px;
  background: rgba(220, 53, 69, 0.1);
  color: #dc3545;
  border: 1px solid rgba(220, 53, 69, 0.2);
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s ease;
}

.logout-btn:hover {
  background: #dc3545;
  color: white;
  transform: translateY(-2px);
}

/* 内容区域 */
.container {
  max-width: 1400px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

/* 主要学习区域 */
.learning-layout {
  display: grid;
  grid-template-columns: 350px 1fr;
  gap: 20px;
  height: calc(100vh - 180px);
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

  .header {
    padding: 15px 20px;
  }

  .nav-menu {
    gap: 15px;
  }

  .nav-item {
    padding: 8px 16px;
  }
}

@media (max-width: 768px) {
  .learning-layout {
    grid-template-columns: 1fr;
    grid-template-rows: auto 1fr;
    height: auto;
  }

  .header {
    flex-direction: column;
    gap: 20px;
    padding: 20px;
  }

  .nav-menu {
    width: 100%;
    justify-content: center;
  }

  .user-info {
    width: 100%;
    justify-content: space-between;
  }

  .logo h1 {
    font-size: 20px;
  }
}

/* 页面切换动画 */
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.5s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style>
