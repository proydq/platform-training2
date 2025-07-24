<template>
  <div class="video-container">
    <div class="video-player" @click="$emit('toggleControls')">
      <div v-if="!isPlaying" class="video-placeholder">
        <div class="play-button" @click="$emit('playVideo')">▶</div>
        <h3>{{ lessonData?.title }}</h3>
        <p>{{ lessonData?.description }}</p>
      </div>

      <div class="video-controls" :class="{ show: showControls }">
        <div class="progress-bar-container">
          <div class="video-progress-bar" @click="$emit('seekVideo', $event)">
            <div class="video-progress-fill" :style="{ width: videoProgress + '%' }"></div>
          </div>
        </div>
        <div class="control-buttons">
          <div class="control-left">
            <button class="control-btn" @click="$emit('togglePlay')">{{ isPlaying ? '⏸️' : '▶️' }}</button>
            <button class="control-btn" @click="$emit('skipBackward')">⏪</button>
            <button class="control-btn" @click="$emit('skipForward')">⏩</button>
            <div class="time-display">
              <span>{{ formatTime(currentTime) }}</span> / <span>{{ formatTime(totalTime) }}</span>
            </div>
          </div>
          <div class="control-right">
            <select
              class="speed-selector"
              :value="playbackSpeed"
              @change="$emit('changeSpeed', $event.target.value)"
            >
              <option value="0.5">0.5x</option>
              <option value="1">1.0x</option>
              <option value="1.25">1.25x</option>
              <option value="1.5">1.5x</option>
              <option value="2">2.0x</option>
            </select>
            <button class="control-btn" @click="$emit('toggleSubtitles')">CC</button>
            <button class="control-btn" @click="$emit('toggleFullscreen')">⛶</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  lessonData: Object,
  isPlaying: Boolean,
  showControls: Boolean,
  videoProgress: Number,
  currentTime: Number,
  totalTime: Number,
  playbackSpeed: String
})

defineEmits([
  'playVideo',
  'togglePlay',
  'toggleControls',
  'seekVideo',
  'skipBackward',
  'skipForward',
  'changeSpeed',
  'toggleSubtitles',
  'toggleFullscreen'
])

const formatTime = (seconds) => {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins}:${secs.toString().padStart(2, '0')}`
}
</script>

<style scoped>
/* 视频播放器区域 */
.video-container {
  position: relative;
  background: #000;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  overflow: hidden;
}

.video-player {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  position: relative;
  cursor: pointer;
}

.video-placeholder {
  text-align: center;
  padding: 40px;
}

.play-button {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  border: 3px solid rgba(255, 255, 255, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-bottom: 20px;
}

.play-button:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: scale(1.1);
}

.video-controls {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(transparent, rgba(0,0,0,0.8));
  padding: 20px;
  display: none;
}

.video-controls.show {
  display: block;
}

.progress-bar-container {
  margin-bottom: 15px;
}

.video-progress-bar {
  width: 100%;
  height: 6px;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 3px;
  cursor: pointer;
}

.video-progress-fill {
  height: 100%;
  background: #667eea;
  border-radius: 3px;
  transition: width 0.3s ease;
}

.control-buttons {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.control-left, .control-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.control-btn {
  background: none;
  border: none;
  color: white;
  font-size: 18px;
  cursor: pointer;
  padding: 8px;
  border-radius: 4px;
  transition: background 0.3s;
}

.control-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.time-display {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
}

.speed-selector {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  cursor: pointer;
}

@media (max-width: 768px) {
  .video-container {
    min-height: 250px;
  }
}
</style>
