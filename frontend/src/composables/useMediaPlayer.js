import { ref, nextTick } from 'vue'
import { ElMessage } from 'element-plus'

export function useMediaPlayer() {
  const isPlaying = ref(false)
  const videoElement = ref(null)
  const audioElement = ref(null)

  const handlePlay = () => {
    isPlaying.value = true
  }

  const handlePause = () => {
    isPlaying.value = false
  }

  const handleVideoEnd = () => {
    isPlaying.value = false
    ElMessage.success('视频播放完成')
  }

  const handleAudioEnd = () => {
    isPlaying.value = false
    ElMessage.success('音频播放完成')
  }

  const togglePlayPause = (mediaType) => {
    if (mediaType === 'video' && videoElement.value) {
      if (isPlaying.value) {
        videoElement.value.pause()
      } else {
        videoElement.value.play()
      }
    } else if (mediaType === 'audio' && audioElement.value) {
      if (isPlaying.value) {
        audioElement.value.pause()
      } else {
        audioElement.value.play()
      }
    }
  }

  const loadMedia = async (mediaUrl, mediaType) => {
    if (!mediaUrl) return

    await nextTick()
    
    if (mediaType === 'video' && videoElement.value) {
      videoElement.value.src = mediaUrl
      videoElement.value.load()
      try {
        await videoElement.value.play()
      } catch {
        // autoplay may be blocked
      }
    } else if (mediaType === 'audio' && audioElement.value) {
      audioElement.value.src = mediaUrl
      audioElement.value.load()
      try {
        await audioElement.value.play()
      } catch {
        // autoplay may be blocked
      }
    }
  }

  return {
    isPlaying,
    videoElement,
    audioElement,
    handlePlay,
    handlePause,
    handleVideoEnd,
    handleAudioEnd,
    togglePlayPause,
    loadMedia
  }
}