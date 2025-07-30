// composables/useProgressTracking.js - 学习进度追踪
import { ref, onMounted, onUnmounted } from 'vue'
import { startStudyAPI, updateStudyProgressAPI, getStudyRecordByCourseAPI, completeStudyAPI } from '@/api/studyProgress'
import { ElMessage } from 'element-plus'

export function useProgressTracking(getProgressCallback = null) {
  // 学习会话状态
  const sessionStartTime = ref(null)
  const currentStudyTime = ref(0) // 当前会话学习时间(秒)
  const totalStudyTime = ref(0) // 总学习时间(分钟)
  const lastSaveTime = ref(Date.now())
  const isTracking = ref(false)
  const isPaused = ref(false) // 是否暂停计时

  // 暂停相关状态
  const pausedTime = ref(0) // 累计暂停时间(毫秒)
  const pauseStartTime = ref(null) // 暂停开始时间

  // 进度保存状态
  const isSaving = ref(false)
  const lastSavedProgress = ref(0)
  const currentRecordId = ref(null) // 当前学习记录ID
  
  // 进度获取回调函数
  const progressCallback = ref(getProgressCallback)

  // 定时器
  let progressTimer = null
  let saveTimer = null

  /**
   * 开始学习会话追踪
   * @param {string} courseId - 课程ID
   * @param {string} chapterId - 章节ID (目前不使用，因为后端是按课程记录的)
   * @param {boolean} startPaused - 是否初始暂停状态
   */
  const startTracking = async (courseId, chapterId, startPaused = false) => {
    if (isTracking.value) {
      console.log('⚠️ 已在追踪中，先停止现有追踪')
      stopTracking()
    }

    console.log('🎯 开始学习进度追踪:', { courseId, chapterId, startPaused })
    
    try {
      // 获取或创建学习记录
      const recordResponse = await getStudyRecordByCourseAPI(courseId)
      
      if (recordResponse && recordResponse.data) {
        currentRecordId.value = recordResponse.data.id
        console.log('📝 找到现有学习记录:', currentRecordId.value)
      } else {
        // 创建新的学习记录
        const startResponse = await startStudyAPI(courseId)
        if (startResponse && startResponse.data) {
          currentRecordId.value = startResponse.data.id
          console.log('📝 创建新学习记录:', currentRecordId.value)
        }
      }
      
      sessionStartTime.value = Date.now()
      lastSaveTime.value = Date.now()
      isTracking.value = true
      isPaused.value = startPaused // 设置初始暂停状态
      currentStudyTime.value = 0
      pausedTime.value = 0
      
      // 如果初始暂停，设置暂停开始时间
      if (startPaused) {
        pauseStartTime.value = Date.now()
        console.log('⏸️ 媒体课程：初始状态暂停计时，等待播放开始')
      } else {
        console.log('▶️ 文档课程：立即开始计时')
      }

      // 每秒更新学习时间
      progressTimer = setInterval(() => {
        if (isTracking.value && !isPaused.value) {
          const now = Date.now()
          const totalElapsed = now - sessionStartTime.value
          const effectiveStudyTime = totalElapsed - pausedTime.value
          currentStudyTime.value = Math.floor(effectiveStudyTime / 1000)
        }
      }, 1000)

      // 每30秒自动保存一次进度
      saveTimer = setInterval(() => {
        if (isTracking.value && currentRecordId.value) {
          // 获取当前实时进度
          const currentProgress = progressCallback.value ? progressCallback.value() : lastSavedProgress.value
          autoSaveProgress(currentProgress, `auto-save-${Date.now()}`)
        }
      }, 30000)
      
    } catch (error) {
      console.error('❌ 开始学习追踪失败:', error)
      ElMessage.error('开始学习记录失败')
    }
  }

  /**
   * 暂停计时
   */
  const pauseTracking = () => {
    if (!isTracking.value || isPaused.value) return
    
    isPaused.value = true
    pauseStartTime.value = Date.now()
    console.log('⏸️ 暂停学习计时')
  }

  /**
   * 恢复计时
   */
  const resumeTracking = () => {
    if (!isTracking.value || !isPaused.value) return
    
    if (pauseStartTime.value) {
      pausedTime.value += Date.now() - pauseStartTime.value
      pauseStartTime.value = null
    }
    
    isPaused.value = false
    console.log('▶️ 恢复学习计时')
  }

  /**
   * 停止学习会话追踪
   */
  const stopTracking = () => {
    console.log('⏹️ 停止学习进度追踪')
    
    // 如果正在暂停中，先结束暂停状态
    if (isPaused.value && pauseStartTime.value) {
      pausedTime.value += Date.now() - pauseStartTime.value
    }
    
    isTracking.value = false
    isPaused.value = false
    
    if (progressTimer) {
      clearInterval(progressTimer)
      progressTimer = null
    }
    
    if (saveTimer) {
      clearInterval(saveTimer)
      saveTimer = null
    }

    // 更新总学习时间
    if (sessionStartTime.value) {
      const sessionMinutes = Math.floor(currentStudyTime.value / 60)
      totalStudyTime.value += sessionMinutes
    }

    // 重置状态
    sessionStartTime.value = null
    currentStudyTime.value = 0
    pausedTime.value = 0
    pauseStartTime.value = null
  }

  /**
   * 自动保存进度(静默保存，不显示消息)
   */
  const autoSaveProgress = async (progress = null, position = '') => {
    if (isSaving.value || !currentRecordId.value) return

    try {
      isSaving.value = true
      
      const studyMinutes = Math.floor((Date.now() - lastSaveTime.value) / 1000 / 60)
      
      // 如果没有提供进度，使用上次保存的进度
      const progressToSave = progress !== null ? progress : lastSavedProgress.value
      
      await updateStudyProgressAPI(currentRecordId.value, {
        progress: progressToSave,
        lastPosition: position,
        studyTime: studyMinutes
      })

      lastSaveTime.value = Date.now()
      
      if (progress !== null) {
        lastSavedProgress.value = progress
      }

      console.log('💾 自动保存进度成功:', { 
        recordId: currentRecordId.value, 
        progress: progressToSave, 
        studyTime: studyMinutes,
        position: position
      })
      
    } catch (error) {
      console.error('💾 自动保存进度失败:', error)
    } finally {
      isSaving.value = false
    }
  }

  /**
   * 手动保存进度(显示保存消息)
   */
  const saveProgress = async (progress, position = '') => {
    if (isSaving.value || !currentRecordId.value) return

    try {
      isSaving.value = true
      
      const studyMinutes = Math.floor((Date.now() - lastSaveTime.value) / 1000 / 60)
      
      await updateStudyProgressAPI(currentRecordId.value, {
        progress,
        lastPosition: position,
        studyTime: studyMinutes
      })

      lastSaveTime.value = Date.now()
      lastSavedProgress.value = progress

      ElMessage.success('学习进度已保存')
      console.log('💾 手动保存进度成功:', { recordId: currentRecordId.value, progress, studyTime: studyMinutes })
      
    } catch (error) {
      console.error('💾 手动保存进度失败:', error)
      ElMessage.error('保存进度失败，请重试')
    } finally {
      isSaving.value = false
    }
  }

  /**
   * 标记章节完成
   */
  const markChapterComplete = async (progress = 100) => {
    if (!currentRecordId.value) {
      console.warn('⚠️ 没有活跃的学习记录，无法标记完成')
      return false
    }

    try {
      // 先保存100%的进度
      await saveProgress(progress)
      
      // 标记学习记录完成
      await completeStudyAPI(currentRecordId.value)
      
      ElMessage.success('🎉 恭喜完成本课程！')
      console.log('✅ 课程完成:', { recordId: currentRecordId.value })
      
      return true
    } catch (error) {
      console.error('❌ 标记课程完成失败:', error)
      ElMessage.error('标记完成失败，请重试')
      return false
    }
  }

  /**
   * 获取当前会话学习时间(格式化显示)
   */
  const getFormattedStudyTime = () => {
    const minutes = Math.floor(currentStudyTime.value / 60)
    const seconds = currentStudyTime.value % 60
    return `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`
  }

  /**
   * 获取学习记录信息
   */
  const getStudyRecord = async (courseId) => {
    try {
      const response = await getStudyRecordByCourseAPI(courseId)
      return response?.data || null
    } catch (error) {
      // 如果是学习记录不存在的错误（3002），这是正常情况，不记录错误
      if (error.response?.data?.code === 3002) {
        console.log('ℹ️ 用户还没有此课程的学习记录，将创建新的记录')
        return null
      }
      console.error('❌ 获取学习记录失败:', error)
      return null
    }
  }

  /**
   * 检查课程是否已完成
   */
  const isCourseCompleted = async (courseId) => {
    const record = await getStudyRecord(courseId)
    return record?.studyStatus === 'COMPLETED'
  }

  /**
   * 设置进度获取回调函数
   */
  const setProgressCallback = (callback) => {
    progressCallback.value = callback
  }

  /**
   * 页面离开时的清理工作
   */
  const cleanup = async (currentProgress = 0, position = '') => {
    if (isTracking.value && currentRecordId.value) {
      // 获取实时进度
      const finalProgress = progressCallback.value ? progressCallback.value() : currentProgress
      // 最后保存一次进度
      await autoSaveProgress(finalProgress, position)
    }
    stopTracking()
  }

  // 组件卸载时清理
  onUnmounted(() => {
    stopTracking()
  })

  // 页面即将离开时保存进度
  onMounted(() => {
    const handleBeforeUnload = (event) => {
      if (isTracking.value) {
        // 注意：这里只能做同步操作，异步保存可能不会完成
      }
    }

    window.addEventListener('beforeunload', handleBeforeUnload)
    
    return () => {
      window.removeEventListener('beforeunload', handleBeforeUnload)
    }
  })

  return {
    // 状态
    isTracking,
    isSaving,
    isPaused,
    currentStudyTime,
    totalStudyTime,
    currentRecordId,
    
    // 方法
    startTracking,
    stopTracking,
    pauseTracking,
    resumeTracking,
    autoSaveProgress,
    saveProgress,
    markChapterComplete,
    getFormattedStudyTime,
    getStudyRecord,
    isCourseCompleted,
    setProgressCallback,
    cleanup
  }
}