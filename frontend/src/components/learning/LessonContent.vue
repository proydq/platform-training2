<template>
  <div class="lesson-content">
    <!-- 视频播放器 -->
    <div v-if="isVideo" class="video-container">
      <div class="video-player">
        <video
          ref="videoElement"
          :src="mediaUrl"
          controls
          controlsList="nodownload"
          @play="$emit('play')"
          @pause="$emit('pause')"
          @ended="$emit('ended')"
        >
          您的浏览器不支持视频播放。
        </video>
      </div>
    </div>

    <!-- 文档查看器 -->
    <div v-else-if="isDocument" class="document-container">
      <DocumentViewer
        :url="mediaUrl"
        :file-name="lessonTitle || '文档'"
      />
    </div>

    <!-- 音频播放器 -->
    <div v-else-if="isAudio" class="audio-container">
      <div class="audio-player">
        <audio
          ref="audioElement"
          :src="mediaUrl"
          controls
          controlsList="nodownload"
          @play="$emit('play')"
          @pause="$emit('pause')"
          @ended="$emit('ended')"
        >
          您的浏览器不支持音频播放。
        </audio>
      </div>
    </div>

    <!-- 其他类型或无内容 -->
    <div v-else class="content-placeholder">
      <div class="placeholder-content">
        <span class="placeholder-icon">📚</span>
        <p>请选择一个章节开始学习</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import DocumentViewer from '@/components/DocumentViewer.vue'
import { useMediaUrl } from '@/composables/useMediaUrl'

const props = defineProps({
  mediaUrl: {
    type: String,
    default: ''
  },
  lessonTitle: {
    type: String,
    default: ''
  }
})

defineEmits(['play', 'pause', 'ended'])

const { isVideoFile, isAudioFile, isDocumentFile } = useMediaUrl()

const videoElement = ref(null)
const audioElement = ref(null)

const isVideo = computed(() => isVideoFile(props.mediaUrl))
const isAudio = computed(() => isAudioFile(props.mediaUrl))
const isDocument = computed(() => isDocumentFile(props.mediaUrl))

defineExpose({
  videoElement,
  audioElement
})
</script>

<style scoped>
.lesson-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 视频播放器区域 */
.video-container {
  position: relative;
  background: #000;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  overflow: hidden;
  border-radius: 8px;
  flex: 1;
}

.video-player {
  width: 100%;
  height: 100%;
  position: relative;
}

.video-player video {
  width: 100%;
  height: 100%;
  object-fit: contain;
  background: #000;
}

/* 文档容器样式 */
.document-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}

/* 音频容器样式 */
.audio-container {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px;
}

.audio-player {
  background: white;
  padding: 30px;
  border-radius: 15px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  width: 100%;
  max-width: 600px;
}

.audio-player audio {
  width: 100%;
}

/* 占位符样式 */
.content-placeholder {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
}

.placeholder-content {
  text-align: center;
  color: #666;
}

.placeholder-icon {
  font-size: 64px;
  display: block;
  margin-bottom: 20px;
  opacity: 0.5;
}
</style>