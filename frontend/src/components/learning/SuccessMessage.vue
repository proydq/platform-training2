<template>
  <transition name="success-fade">
    <div v-if="show" class="success-message-overlay" @click="handleClose">
      <div class="success-message" @click.stop>
        <div class="success-icon">
          <div class="check-animation">
            <svg class="checkmark" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 52 52">
              <circle class="checkmark-circle" cx="26" cy="26" r="25" fill="none"/>
              <path class="checkmark-check" fill="none" d="m14.1 27.2l7.1 7.2 16.7-16.8"/>
            </svg>
          </div>
        </div>

        <div class="success-content">
          <h3 class="success-title">操作成功！</h3>
          <p class="success-text">{{ message }}</p>
        </div>

        <button @click="handleClose" class="close-button">
          <span class="close-icon">✕</span>
        </button>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { onMounted, ref } from 'vue'

const props = defineProps({
  show: {
    type: Boolean,
    required: true
  },
  message: {
    type: String,
    default: '操作已成功完成'
  },
  duration: {
    type: Number,
    default: 3000 // 3秒后自动关闭
  }
})

const emit = defineEmits(['close'])

// 自动关闭定时器
let autoCloseTimer = null

// 处理关闭
const handleClose = () => {
  emit('close')
  if (autoCloseTimer) {
    clearTimeout(autoCloseTimer)
    autoCloseTimer = null
  }
}

// 监听显示状态变化
const setupAutoClose = () => {
  if (props.show && props.duration > 0) {
    autoCloseTimer = setTimeout(() => {
      handleClose()
    }, props.duration)
  }
}

// 当组件显示时设置自动关闭
onMounted(() => {
  if (props.show) {
    setupAutoClose()
  }
})

// 监听 show 属性变化
import { watch } from 'vue'
watch(() => props.show, (newVal) => {
  if (newVal) {
    setupAutoClose()
  } else if (autoCloseTimer) {
    clearTimeout(autoCloseTimer)
    autoCloseTimer = null
  }
})
</script>

<style scoped>
/* 覆盖层 */
.success-message-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  backdrop-filter: blur(4px);
}

/* 成功消息容器 */
.success-message {
  background: white;
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  text-align: center;
  position: relative;
  max-width: 400px;
  min-width: 300px;
  animation: successPop 0.6s cubic-bezier(0.68, -0.55, 0.265, 1.55);
}

/* 成功图标 */
.success-icon {
  margin-bottom: 20px;
}

.check-animation {
  width: 80px;
  height: 80px;
  margin: 0 auto;
  position: relative;
}

.checkmark {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: block;
  stroke-width: 2;
  stroke: #28a745;
  stroke-miterlimit: 10;
}

.checkmark-circle {
  stroke-dasharray: 166;
  stroke-dashoffset: 166;
  stroke-width: 2;
  stroke-miterlimit: 10;
  stroke: #28a745;
  fill: none;
  animation: stroke 0.6s cubic-bezier(0.65, 0, 0.45, 1) forwards;
}

.checkmark-check {
  transform-origin: 50% 50%;
  stroke-dasharray: 48;
  stroke-dashoffset: 48;
  stroke-width: 3;
  stroke: #28a745;
  animation: stroke 0.3s cubic-bezier(0.65, 0, 0.45, 1) 0.8s forwards;
}

/* 内容区域 */
.success-content {
  margin-bottom: 20px;
}

.success-title {
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 10px 0;
}

.success-text {
  font-size: 16px;
  color: #666;
  line-height: 1.5;
  margin: 0;
}

/* 关闭按钮 */
.close-button {
  position: absolute;
  top: 15px;
  right: 15px;
  background: none;
  border: none;
  cursor: pointer;
  padding: 8px;
  border-radius: 50%;
  transition: all 0.3s ease;
  color: #999;
}

.close-button:hover {
  background: #f8f9fa;
  color: #666;
}

.close-icon {
  font-size: 18px;
  line-height: 1;
}

/* 动画效果 */
@keyframes stroke {
  100% {
    stroke-dashoffset: 0;
  }
}

@keyframes successPop {
  0% {
    opacity: 0;
    transform: scale(0.3) translateY(-50px);
  }
  50% {
    opacity: 1;
    transform: scale(1.05) translateY(0);
  }
  70% {
    transform: scale(0.95);
  }
  100% {
    opacity: 1;
    transform: scale(1);
  }
}

/* 进入/离开过渡 */
.success-fade-enter-active {
  transition: all 0.3s ease;
}

.success-fade-leave-active {
  transition: all 0.3s ease;
}

.success-fade-enter-from {
  opacity: 0;
  transform: scale(0.8);
}

.success-fade-leave-to {
  opacity: 0;
  transform: scale(0.8);
}

/* 响应式设计 */
@media (max-width: 480px) {
  .success-message {
    margin: 20px;
    padding: 30px 20px;
    min-width: auto;
    width: calc(100% - 40px);
  }

  .check-animation {
    width: 60px;
    height: 60px;
  }

  .checkmark {
    width: 60px;
    height: 60px;
  }

  .success-title {
    font-size: 20px;
  }

  .success-text {
    font-size: 14px;
  }
}

/* 高对比度模式 */
@media (prefers-contrast: high) {
  .success-message {
    border: 2px solid #28a745;
  }

  .checkmark-circle,
  .checkmark-check {
    stroke: #28a745;
    stroke-width: 3;
  }
}

/* 减少动画模式 */
@media (prefers-reduced-motion: reduce) {
  .success-message {
    animation: none;
  }

  .checkmark-circle,
  .checkmark-check {
    animation: none;
    stroke-dashoffset: 0;
  }

  .success-fade-enter-active,
  .success-fade-leave-active {
    transition: opacity 0.2s ease;
  }
}
</style>
