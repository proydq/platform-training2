// composables/useVideoPlayer.js
import { ref } from 'vue'

export function useVideoPlayer() {
  const isPlaying = ref(false)
  const showControls = ref(false)
  const videoProgress = ref(35)
  const currentTime = ref(263) // 4:23 = 263秒
  const totalTime = ref(720) // 12:00 = 720秒
  const playbackSpeed = ref('1')

  const playVideo = () => {
    isPlaying.value = true
    showControls.value = true
  }

  const togglePlay = () => {
    isPlaying.value = !isPlaying.value
  }

  const toggleVideoControls = () => {
    showControls.value = !showControls.value
  }

  const seekVideo = (event) => {
    const rect = event.target.getBoundingClientRect()
    const percent = (event.clientX - rect.left) / rect.width
    videoProgress.value = percent * 100
    currentTime.value = percent * totalTime.value
  }

  const skipBackward = () => {
    currentTime.value = Math.max(0, currentTime.value - 10)
    videoProgress.value = (currentTime.value / totalTime.value) * 100
  }

  const skipForward = () => {
    currentTime.value = Math.min(totalTime.value, currentTime.value + 10)
    videoProgress.value = (currentTime.value / totalTime.value) * 100
  }

  const changeSpeed = (speed) => {
    playbackSpeed.value = speed
  }

  const toggleSubtitles = () => {
  }

  const toggleFullscreen = () => {
  }

  const formatTime = (seconds) => {
    const mins = Math.floor(seconds / 60)
    const secs = seconds % 60
    return `${mins}:${secs.toString().padStart(2, '0')}`
  }

  return {
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
  }
}
