<!-- frontend/src/components/AuthImage.vue -->
<template>
  <div class="auth-image-container">
    <img 
      v-if="imageSrc" 
      :src="imageSrc" 
      :alt="alt" 
      :style="style"
      @error="handleError"
      @load="handleLoad"
    />
    <div v-else-if="loading" class="loading-placeholder">
      <el-icon class="is-loading"><Loading /></el-icon>
    </div>
    <div v-else-if="error" class="error-placeholder">
      <slot name="error">
        <div class="default-error">{{ defaultImage || '🖼️' }}</div>
      </slot>
    </div>
    <div v-else class="default-placeholder">
      <slot name="default">
        <div class="default-image">{{ defaultImage || '📚' }}</div>
      </slot>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted } from 'vue'
import { Loading } from '@element-plus/icons-vue'
import axios from 'axios'

const props = defineProps({
  src: {
    type: String,
    default: ''
  },
  alt: {
    type: String,
    default: ''
  },
  style: {
    type: [String, Object],
    default: () => ({})
  },
  defaultImage: {
    type: String,
    default: ''
  }
})

const imageSrc = ref('')
const loading = ref(false)
const error = ref(false)

// 获取JWT token
const getAuthToken = () => {
  return localStorage.getItem('token')
}

// 通过fetch获取带认证的图片
const fetchAuthImage = async (url) => {
  if (!url) return

  loading.value = true
  error.value = false
  
  try {
    const token = getAuthToken()
    if (!token) {
      error.value = true
      return
    }

    const response = await fetch(url, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}: ${response.statusText}`)
    }

    const blob = await response.blob()
    const objectUrl = URL.createObjectURL(blob)
    imageSrc.value = objectUrl
    
  } catch (err) {
    error.value = true
  } finally {
    loading.value = false
  }
}

// 清理blob URL
const cleanup = () => {
  if (imageSrc.value && imageSrc.value.startsWith('blob:')) {
    URL.revokeObjectURL(imageSrc.value)
    imageSrc.value = ''
  }
}

const handleError = () => {
  error.value = true
  cleanup()
}

const handleLoad = () => {
  error.value = false
}

// 监听src变化
watch(() => props.src, (newSrc) => {
  cleanup()
  if (newSrc) {
    // 检查是否是需要认证的URL
    if (newSrc.startsWith('/api/v1/files/') || newSrc.includes('/api/v1/files/')) {
      fetchAuthImage(newSrc)
    } else {
      // 普通图片直接使用
      imageSrc.value = newSrc
    }
  } else {
    imageSrc.value = ''
    error.value = false
  }
}, { immediate: true })

onUnmounted(() => {
  cleanup()
})
</script>

<style scoped>
.auth-image-container {
  display: inline-block;
  width: 100%;
  height: 100%;
}

.auth-image-container img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.loading-placeholder,
.error-placeholder,
.default-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f5f5;
  color: #999;
}

.default-error,
.default-image {
  font-size: 2em;
  opacity: 0.6;
}

.loading-placeholder {
  background-color: #f0f0f0;
}

.error-placeholder {
  background-color: #fee;
  color: #999;
}
</style>