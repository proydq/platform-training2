<!-- DocumentViewer.vue -->
<template>
  <div class="document-viewer-container">
    <!-- PDF 查看器 -->
    <div v-if="isPDF" class="pdf-viewer">
      <div class="pdf-toolbar">
        <button @click="previousPage" :disabled="pageNum <= 1" class="doc-btn">
          上一页
        </button>
        <span class="page-info">{{ pageNum }} / {{ pageCount }}</span>
        <button @click="nextPage" :disabled="pageNum >= pageCount" class="doc-btn">
          下一页
        </button>
        <button @click="zoomIn" class="doc-btn">放大</button>
        <button @click="zoomOut" class="doc-btn">缩小</button>
        <button @click="resetZoom" class="doc-btn">重置</button>
      </div>
      <div class="pdf-canvas-container" ref="canvasContainer">
        <canvas ref="pdfCanvas"></canvas>
      </div>
    </div>

    <!-- Office 文档查看器 (使用微软在线预览) -->
    <div v-else-if="isOfficeDoc" class="office-viewer">
      <iframe
        :src="officeViewerUrl"
        frameborder="0"
        width="100%"
        height="100%"
        allowfullscreen
      ></iframe>
    </div>

    <!-- 文本文件查看器 -->
    <div v-else-if="isTextFile" class="text-viewer">
      <div class="text-toolbar">
        <button @click="downloadFile" class="doc-btn">下载文件</button>
        <button @click="copyText" class="doc-btn">复制内容</button>
      </div>
      <pre class="text-content">{{ textContent }}</pre>
    </div>

    <!-- 图片查看器 -->
    <div v-else-if="isImage" class="image-viewer">
      <div class="image-toolbar">
        <button @click="zoomIn" class="doc-btn">放大</button>
        <button @click="zoomOut" class="doc-btn">缩小</button>
        <button @click="resetZoom" class="doc-btn">原始大小</button>
        <button @click="downloadFile" class="doc-btn">下载</button>
      </div>
      <div class="image-container">
        <img :src="url" :style="imageStyle" @load="onImageLoad" />
      </div>
    </div>

    <!-- 不支持的文件类型 -->
    <div v-else class="unsupported-viewer">
      <div class="unsupported-content">
        <i class="file-icon">📄</i>
        <h3>{{ fileName }}</h3>
        <p>该文件类型暂不支持在线预览</p>
        <button @click="downloadFile" class="doc-btn doc-btn-primary">
          下载文件
        </button>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="doc-loading">
      <div class="loading-spinner"></div>
      <p>{{ loadingText }}</p>
    </div>

    <!-- 错误状态 -->
    <div v-if="error" class="doc-error">
      <i class="error-icon">⚠️</i>
      <p>{{ error }}</p>
      <button @click="retry" class="doc-btn">重试</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  url: {
    type: String,
    required: true
  },
  fileName: {
    type: String,
    default: '文档'
  }
})

// 文件类型判断
const fileExt = computed(() => {
  if (!props.url) return ''
  const match = props.url.match(/\.([^.]+)$/)
  return match ? match[1].toLowerCase() : ''
})

const isPDF = computed(() => fileExt.value === 'pdf')
const isOfficeDoc = computed(() => ['doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx'].includes(fileExt.value))
const isTextFile = computed(() => ['txt', 'md', 'json', 'xml', 'csv', 'log'].includes(fileExt.value))
const isImage = computed(() => ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'svg', 'webp'].includes(fileExt.value))

// PDF相关状态
let pdfjsLib = null
let pdfDoc = null
const pdfCanvas = ref(null)
const pageNum = ref(1)
const pageCount = ref(0)
const scale = ref(1.5)

// 文本内容
const textContent = ref('')

// 图片缩放
const imageScale = ref(1)
const imageStyle = computed(() => ({
  transform: `scale(${imageScale.value})`,
  transition: 'transform 0.3s'
}))

// 通用状态
const loading = ref(false)
const loadingText = ref('加载中...')
const error = ref('')

// Office文档在线预览URL（使用微软Office Online）
const officeViewerUrl = computed(() => {
  if (!isOfficeDoc.value) return ''
  // 确保URL是完整的绝对路径
  const fullUrl = props.url.startsWith('http')
    ? props.url
    : `${window.location.origin}${props.url}`
  return `https://view.officeapps.live.com/op/embed.aspx?src=${encodeURIComponent(fullUrl)}`
})

// 加载PDF.js
const loadPdfJs = async () => {
  if (window.pdfjsLib) {
    pdfjsLib = window.pdfjsLib
    return
  }

  const script = document.createElement('script')
  script.src = 'https://cdnjs.cloudflare.com/ajax/libs/pdf.js/2.11.338/pdf.min.js'

  return new Promise((resolve, reject) => {
    script.onload = () => {
      pdfjsLib = window.pdfjsLib
      pdfjsLib.GlobalWorkerOptions.workerSrc = 'https://cdnjs.cloudflare.com/ajax/libs/pdf.js/2.11.338/pdf.worker.min.js'
      resolve()
    }
    script.onerror = reject
    document.head.appendChild(script)
  })
}

// 加载PDF文档
const loadPDF = async () => {
  try {
    loading.value = true
    loadingText.value = '加载PDF...'
    await loadPdfJs()

    const loadingTask = pdfjsLib.getDocument(props.url)
    pdfDoc = await loadingTask.promise
    pageCount.value = pdfDoc.numPages

    // 渲染第一页
    renderPage(1)
  } catch (err) {
    console.error('PDF加载失败:', err)
    error.value = 'PDF文件加载失败'
  } finally {
    loading.value = false
  }
}

// 渲染PDF页面
const renderPage = async (num) => {
  if (!pdfDoc) return

  try {
    const page = await pdfDoc.getPage(num)
    const viewport = page.getViewport({ scale: scale.value })
    const canvas = pdfCanvas.value
    const context = canvas.getContext('2d')

    canvas.height = viewport.height
    canvas.width = viewport.width

    const renderContext = {
      canvasContext: context,
      viewport: viewport
    }

    await page.render(renderContext).promise
  } catch (err) {
    console.error('页面渲染失败:', err)
    error.value = '页面渲染失败'
  }
}

// 加载文本文件
const loadTextFile = async () => {
  try {
    loading.value = true
    loadingText.value = '加载文本文件...'

    const response = await fetch(props.url)
    if (!response.ok) throw new Error('文件加载失败')

    textContent.value = await response.text()
  } catch (err) {
    console.error('文本文件加载失败:', err)
    error.value = '文本文件加载失败'
  } finally {
    loading.value = false
  }
}

// PDF翻页
const previousPage = () => {
  if (pageNum.value <= 1) return
  pageNum.value--
  renderPage(pageNum.value)
}

const nextPage = () => {
  if (pageNum.value >= pageCount.value) return
  pageNum.value++
  renderPage(pageNum.value)
}

// 缩放功能
const zoomIn = () => {
  if (isPDF.value) {
    scale.value = Math.min(scale.value + 0.25, 3)
    renderPage(pageNum.value)
  } else if (isImage.value) {
    imageScale.value = Math.min(imageScale.value + 0.25, 3)
  }
}

const zoomOut = () => {
  if (isPDF.value) {
    scale.value = Math.max(scale.value - 0.25, 0.5)
    renderPage(pageNum.value)
  } else if (isImage.value) {
    imageScale.value = Math.max(imageScale.value - 0.25, 0.5)
  }
}

const resetZoom = () => {
  if (isPDF.value) {
    scale.value = 1.5
    renderPage(pageNum.value)
  } else if (isImage.value) {
    imageScale.value = 1
  }
}

// 下载文件
const downloadFile = () => {
  const a = document.createElement('a')
  a.href = props.url
  a.download = props.fileName
  a.click()
}

// 复制文本
const copyText = async () => {
  try {
    await navigator.clipboard.writeText(textContent.value)
    ElMessage.success('复制成功')
  } catch (err) {
    ElMessage.error('复制失败')
  }
}

// 图片加载完成
const onImageLoad = () => {
  loading.value = false
}

// 重试加载
const retry = () => {
  error.value = ''
  loadDocument()
}

// 加载文档
const loadDocument = () => {
  if (isPDF.value) {
    loadPDF()
  } else if (isTextFile.value) {
    loadTextFile()
  } else if (isImage.value) {
    loading.value = true
    loadingText.value = '加载图片...'
  }
}

// 监听URL变化
watch(() => props.url, (newUrl) => {
  if (newUrl) {
    error.value = ''
    pageNum.value = 1
    textContent.value = ''
    imageScale.value = 1
    loadDocument()
  }
})

onMounted(() => {
  loadDocument()
})
</script>

<style scoped>
.document-viewer-container {
  width: 100%;
  height: 100%;
  position: relative;
  background: #f5f5f5;
  display: flex;
  flex-direction: column;
}

/* 工具栏样式 */
.pdf-toolbar,
.text-toolbar,
.image-toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  background: #fff;
  border-bottom: 1px solid #e0e0e0;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.page-info {
  margin: 0 10px;
  font-weight: 500;
}

.doc-btn {
  padding: 6px 12px;
  border: 1px solid #ddd;
  background: #fff;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
}

.doc-btn:hover:not(:disabled) {
  background: #f0f0f0;
  border-color: #ccc;
}

.doc-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.doc-btn-primary {
  background: #667eea;
  color: white;
  border-color: #667eea;
}

.doc-btn-primary:hover {
  background: #5a67d8;
}

/* PDF查看器 */
.pdf-viewer {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.pdf-canvas-container {
  flex: 1;
  overflow: auto;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: 20px;
}

.pdf-canvas-container canvas {
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  background: white;
}

/* Office查看器 */
.office-viewer {
  flex: 1;
  position: relative;
}

.office-viewer iframe {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

/* 文本查看器 */
.text-viewer {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.text-content {
  flex: 1;
  padding: 20px;
  margin: 0;
  background: white;
  overflow: auto;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-wrap: break-word;
}

/* 图片查看器 */
.image-viewer {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.image-container {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: auto;
  padding: 20px;
}

.image-container img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

/* 不支持的文件类型 */
.unsupported-viewer {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
}

.unsupported-content {
  text-align: center;
  padding: 40px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.file-icon {
  font-size: 64px;
  display: block;
  margin-bottom: 20px;
}

.unsupported-content h3 {
  margin: 10px 0;
  color: #333;
}

.unsupported-content p {
  color: #666;
  margin-bottom: 20px;
}

/* 加载状态 */
.doc-loading {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.9);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  z-index: 10;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 10px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 错误状态 */
.doc-error {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  background: white;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.error-icon {
  font-size: 48px;
  display: block;
  margin-bottom: 10px;
}

.doc-error p {
  color: #e53e3e;
  margin-bottom: 20px;
}
</style>
