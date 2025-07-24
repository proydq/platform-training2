<template>
  <button
    class="floating-note-btn"
    @click="$emit('click')"
    :class="{ 'pulse': showPulse }"
    @mouseenter="showTooltip = true"
    @mouseleave="showTooltip = false"
  >
    <div class="btn-icon">
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z" fill="currentColor"/>
      </svg>
    </div>

    <transition name="tooltip-fade">
      <div v-if="showTooltip" class="tooltip">
        快速添加笔记
        <div class="tooltip-arrow"></div>
      </div>
    </transition>
  </button>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

defineEmits(['click'])

const showTooltip = ref(false)
const showPulse = ref(false)

// 定期显示脉冲动画提醒用户
let pulseInterval = null

onMounted(() => {
  // 每30秒显示一次脉冲提醒
  pulseInterval = setInterval(() => {
    showPulse.value = true
    setTimeout(() => {
      showPulse.value = false
    }, 2000)
  }, 30000)
})

onUnmounted(() => {
  if (pulseInterval) {
    clearInterval(pulseInterval)
  }
})
</script>

<style scoped>
.floating-note-btn {
  position: fixed;
  bottom: 30px;
  right: 30px;
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  border-radius: 50%;
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 1000;
  position: relative;
}

.floating-note-btn:hover {
  transform: scale(1.1) translateY(-2px);
  box-shadow: 0 12px 35px rgba(102, 126, 234, 0.6);
  background: linear-gradient(135deg, #5a6fd8, #6a4190);
}

.floating-note-btn:active {
  transform: scale(0.95) translateY(0);
  transition: all 0.1s ease;
}

.btn-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.3s ease;
}

.floating-note-btn:hover .btn-icon {
  transform: rotate(5deg);
}

/* 脉冲动画 */
.floating-note-btn.pulse {
  animation: pulse 2s ease-in-out;
}

@keyframes pulse {
  0% {
    box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
  }
  50% {
    box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4),
    0 0 0 10px rgba(102, 126, 234, 0.3),
    0 0 0 20px rgba(102, 126, 234, 0.1);
    transform: scale(1.05);
  }
  100% {
    box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
    transform: scale(1);
  }
}

/* 工具提示 */
.tooltip {
  position: absolute;
  bottom: 120%;
  right: 50%;
  transform: translateX(50%);
  background: rgba(0, 0, 0, 0.9);
  color: white;
  padding: 8px 12px;
  border-radius: 6px;
  font-size: 12px;
  white-space: nowrap;
  backdrop-filter: blur(10px);
}

.tooltip-arrow {
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  border-left: 5px solid transparent;
  border-right: 5px solid transparent;
  border-top: 5px solid rgba(0, 0, 0, 0.9);
}

/* 工具提示动画 */
.tooltip-fade-enter-active,
.tooltip-fade-leave-active {
  transition: all 0.2s ease;
}

.tooltip-fade-enter-from,
.tooltip-fade-leave-to {
  opacity: 0;
  transform: translateX(50%) translateY(5px);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .floating-note-btn {
    bottom: 20px;
    right: 20px;
    width: 50px;
    height: 50px;
  }

  .btn-icon svg {
    width: 20px;
    height: 20px;
  }

  .tooltip {
    font-size: 11px;
    padding: 6px 10px;
  }
}

/* 深色模式适配 */
@media (prefers-color-scheme: dark) {
  .floating-note-btn {
    box-shadow: 0 8px 25px rgba(102, 126, 234, 0.3);
  }

  .floating-note-btn:hover {
    box-shadow: 0 12px 35px rgba(102, 126, 234, 0.5);
  }
}

/* 减少动画模式 */
@media (prefers-reduced-motion: reduce) {
  .floating-note-btn {
    transition: none;
  }

  .floating-note-btn.pulse {
    animation: none;
  }

  .btn-icon {
    transition: none;
  }

  .tooltip-fade-enter-active,
  .tooltip-fade-leave-active {
    transition: opacity 0.1s ease;
  }
}

/* 高对比度模式 */
@media (prefers-contrast: high) {
  .floating-note-btn {
    border: 2px solid white;
  }

  .tooltip {
    background: black;
    border: 1px solid white;
  }

  .tooltip-arrow {
    border-top-color: black;
  }
}
</style>
