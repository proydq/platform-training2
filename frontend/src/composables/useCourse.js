// frontend/src/composables/useCourse.js - 课程管理逻辑复用（完整版）
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getCourseListAPI,
  createCourseAPI,
  updateCourseAPI,
  deleteCourseAPI,
  publishCourseAPI,
  unpublishCourseAPI,
  getCourseChaptersAPI,
  createChapterAPI,
  updateChapterAPI,
  deleteChapterAPI,
  getDifficultyLevelText,
  getCourseStatusText
} from '@/api/course'

/**
 * 课程管理Composable
 * 提供课程相关的所有业务逻辑
 */
export function useCourse() {
  // ==================== 响应式数据 ====================
  const loading = ref(false)
  const saving = ref(false)
  const courses = ref([])

  // 分页信息
  const pagination = reactive({
    current: 1,
    size: 20,
    total: 0
  })

  // 筛选条件
  const filters = reactive({
    keyword: '',
    category: '',
    difficultyLevel: '',
    status: '',
    instructorId: ''
  })

  // ==================== 计算属性 ====================
  const hasFilters = computed(() => {
    return Object.values(filters).some(value => value !== '')
  })

  const isEmpty = computed(() => {
    return !loading.value && courses.value.length === 0
  })

  const totalPages = computed(() => {
    return Math.ceil(pagination.total / pagination.size)
  })

  // ==================== 课程列表管理 ====================

  /**
   * 加载课程列表
   * @param {Object} options - 额外的查询参数
   */
    // 修复第87行附近的代码
  const loadCourses = async (options = {}) => {
      try {
        loading.value = true

        const params = {
          page: pagination.current - 1, // 后端从0开始
          size: pagination.size,
          ...filters,
          ...options
        }

        console.log('请求课程列表参数:', params)

        const response = await getCourseListAPI(params)

        if (response.code === 200) {
          courses.value = response.data.content || []
          pagination.total = response.data.totalElements || 0

          console.log('课程列表加载成功:', {
            总数: pagination.total,
            当前页: pagination.current,
            课程数: courses.value.length
          })

          return response.data  // 修复：添加完整的return语句
        } else {
          ElMessage.error(response.message || '获取课程列表失败')
          return null
        }
      } catch (error) {
        console.error('获取课程列表失败:', error)
        ElMessage.error('获取课程列表失败，请检查网络连接')
        return null
      } finally {
        loading.value = false
      }
    }

  /**
   * 搜索课程
   */
  const searchCourses = async () => {
    pagination.current = 1
    return await loadCourses()
  }

  /**
   * 重置筛选条件
   */
  const resetFilters = async () => {
    Object.assign(filters, {
      keyword: '',
      category: '',
      difficultyLevel: '',
      status: '',
      instructorId: ''
    })
    pagination.current = 1
    return await loadCourses()
  }

  /**
   * 分页处理
   * @param {Number} page - 页码
   * @param {Number} size - 每页大小
   */
  const handlePagination = async (page, size = null) => {
    pagination.current = page
    if (size && size !== pagination.size) {
      pagination.size = size
      pagination.current = 1 // 改变页大小时回到第一页
    }
    return await loadCourses()
  }

  // ==================== 课程CRUD操作 ====================

  /**
   * 创建课程
   * @param {Object} courseData - 课程数据
   */
  const createCourse = async (courseData) => {
    try {
      saving.value = true

      const response = await createCourseAPI(courseData)

      if (response.code === 200) {
        ElMessage.success('课程创建成功')
        await loadCourses() // 刷新列表
        return response.data
      } else {
        ElMessage.error(response.message || '课程创建失败')
        return null
      }
    } catch (error) {
      ElMessage.error('创建课程失败，请重试')
      return null
    } finally {
      saving.value = false
    }
  }

  /**
   * 更新课程
   * @param {String} courseId - 课程ID
   * @param {Object} courseData - 课程数据
   */
  const updateCourse = async (courseId, courseData) => {
    try {
      saving.value = true

      const response = await updateCourseAPI(courseId, courseData)

      if (response.code === 200) {
        ElMessage.success('课程更新成功')
        await loadCourses() // 刷新列表
        return response.data
      } else {
        ElMessage.error(response.message || '课程更新失败')
        return null
      }
    } catch (error) {
      ElMessage.error('更新课程失败，请重试')
      return null
    } finally {
      saving.value = false
    }
  }

  /**
   * 删除课程
   * @param {String} courseId - 课程ID
   * @param {String} courseName - 课程名称
   */
  const deleteCourse = async (courseId, courseName = '') => {
    try {
      await ElMessageBox.confirm(
        `确定要删除课程"${courseName}"吗？删除后无法恢复。`,
        '确认删除',
        {
          confirmButtonText: '确定删除',
          cancelButtonText: '取消',
          type: 'warning',
          dangerouslyUseHTMLString: true
        }
      )


      const response = await deleteCourseAPI(courseId)

      if (response.code === 200) {
        ElMessage.success('课程删除成功')

        // 如果当前页没有数据了，回到上一页
        if (courses.value.length === 1 && pagination.current > 1) {
          pagination.current -= 1
        }

        await loadCourses() // 刷新列表
        return true
      } else {
        ElMessage.error(response.message || '课程删除失败')
        return false
      }
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('删除课程失败，请重试')
      }
      return false
    }
  }

  /**
   * 发布课程
   * @param {String} courseId - 课程ID
   * @param {String} courseName - 课程名称
   */
  const publishCourse = async (courseId, courseName = '') => {
    try {

      const response = await publishCourseAPI(courseId)

      if (response && response.status === 200) {
        ElMessage.success(`课程"${courseName}"发布成功`)
        await loadCourses() // 刷新列表
        return true
      } else {
        ElMessage.error('课程发布失败')
        return false
      }
    } catch (error) {
      ElMessage.error('发布课程失败，请重试')
      return false
    }
  }

  /**
   * 下架课程
   * @param {String} courseId - 课程ID
   * @param {String} courseName - 课程名称
   */
  const unpublishCourse = async (courseId, courseName = '') => {
    try {
      await ElMessageBox.confirm(
        `确定要下架课程"${courseName}"吗？下架后学员将无法继续学习。`,
        '确认下架',
        {
          confirmButtonText: '确定下架',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )


      const response = await unpublishCourseAPI(courseId)

      if (response.status === 200) {
        ElMessage.success(`课程"${courseName}"下架成功`)
        await loadCourses() // 刷新列表
        return true
      } else {
        ElMessage.error(response.data?.message || '课程下架失败')
        return false
      }
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('下架课程失败，请重试')
      }
      return false
    }
  }

  /**
   * 批量删除课程
   * @param {Array} courseIds - 课程ID数组
   */
  const batchDeleteCourses = async (courseIds) => {
    try {
      await ElMessageBox.confirm(
        `确定要删除选中的 ${courseIds.length} 门课程吗？删除后无法恢复。`,
        '批量删除确认',
        {
          confirmButtonText: '确定删除',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )


      const deletePromises = courseIds.map(id => deleteCourseAPI(id))
      const results = await Promise.allSettled(deletePromises)

      const successCount = results.filter(result =>
        result.status === 'fulfilled' && result.value.code === 200
      ).length

      const failCount = courseIds.length - successCount

      if (successCount > 0) {
        ElMessage.success(`成功删除 ${successCount} 门课程${failCount > 0 ? `，${failCount} 门课程删除失败` : ''}`)
        await loadCourses()
      } else {
        ElMessage.error('批量删除失败')
      }

      return { successCount, failCount }
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('批量删除失败，请重试')
      }
      return { successCount: 0, failCount: courseIds.length }
    }
  }

  // ==================== 工具方法 ====================

  /**
   * 格式化日期
   * @param {String} dateStr - 日期字符串
   */
  const formatDate = (dateStr) => {
    if (!dateStr) return '--'
    try {
      const date = new Date(dateStr)
      return date.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
      })
    } catch (error) {
      return '--'
    }
  }

  /**
   * 格式化时间
   * @param {String} dateStr - 日期字符串
   */
  const formatDateTime = (dateStr) => {
    if (!dateStr) return '--'
    try {
      const date = new Date(dateStr)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    } catch (error) {
      return '--'
    }
  }

  /**
   * 获取状态颜色
   * @param {Number} status - 状态值
   */
  const getStatusColor = (status) => {
    const colorMap = {
      0: '#ffc107', // 草稿 - 黄色
      1: '#28a745', // 已发布 - 绿色
      2: '#dc3545'  // 已下架 - 红色
    }
    return colorMap[status] || '#6c757d'
  }

  /**
   * 获取难度级别标签类型
   * @param {Number} level - 难度级别
   */
  const getDifficultyType = (level) => {
    const typeMap = {
      1: '',        // 入门级 - 默认
      2: 'success', // 初级 - 绿色
      3: 'warning', // 中级 - 橙色
      4: 'danger',  // 高级 - 红色
      5: 'info'     // 专家级 - 蓝色
    }
    return typeMap[level] || ''
  }

  /**
   * 格式化价格
   * @param {Number} price - 价格
   */
  const formatPrice = (price) => {
    if (price === 0 || price === null || price === undefined) {
      return '免费'
    }
    return `¥${Number(price).toFixed(2)}`
  }

  /**
   * 格式化时长
   * @param {Number} minutes - 分钟数
   */
  const formatDuration = (minutes) => {
    if (!minutes || minutes === 0) return '--'

    if (minutes < 60) {
      return `${minutes}分钟`
    }

    const hours = Math.floor(minutes / 60)
    const mins = minutes % 60

    if (mins === 0) {
      return `${hours}小时`
    } else {
      return `${hours}小时${mins}分钟`
    }
  }

  /**
   * 获取课程统计信息
   */
  const getCourseStats = () => {
    const stats = {
      total: courses.value.length,
      draft: 0,
      published: 0,
      unpublished: 0,
      //free: 0,
      //paid: 0
    }

    courses.value.forEach(course => {
      // 统计状态
      switch (course.status) {
        case 0:
          stats.draft++
          break
        case 1:
          stats.published++
          break
        case 2:
          stats.unpublished++
          break
      }

      // 统计价格
      /*if (course.price === 0) {
        stats.free++
      } else {
        stats.paid++
      }*/
    })

    return stats
  }

  /**
   * 导出课程数据
   * @param {Array} selectedCourses - 选中的课程（可选）
   */
  const exportCourses = (selectedCourses = null) => {
    try {
      const coursesToExport = selectedCourses || courses.value

      const csvData = coursesToExport.map(course => ({
        课程名称: course.title,
        课程分类: course.category,
        难度级别: getDifficultyLevelText(course.difficultyLevel),
        课程时长: formatDuration(course.duration),
        //课程价格: formatPrice(course.price),
        讲师: course.instructorName || course.instructorId,
        状态: getCourseStatusText(course.status),
        学员数量: course.studentCount || 0,
        评分: course.rating || 0,
        创建时间: formatDateTime(course.createTime)
      }))

      // 生成CSV内容
      const headers = Object.keys(csvData[0])
      const csvContent = [
        headers.join(','),
        ...csvData.map(row =>
          headers.map(header => `"${row[header] || ''}"`).join(',')
        )
      ].join('\n')

      // 下载文件
      const blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8;' })
      const link = document.createElement('a')
      link.href = URL.createObjectURL(blob)
      link.download = `课程数据_${new Date().toISOString().slice(0, 10)}.csv`
      link.click()

      ElMessage.success('课程数据导出成功')
    } catch (error) {
      ElMessage.error('导出失败，请重试')
    }
  }

  // ==================== 返回接口 ====================
  return {
    // 响应式数据
    loading,
    saving,
    courses,
    pagination,
    filters,

    // 计算属性
    hasFilters,
    isEmpty,
    totalPages,

    // 列表管理
    loadCourses,
    searchCourses,
    resetFilters,
    handlePagination,

    // CRUD操作
    createCourse,
    updateCourse,
    deleteCourse,
    publishCourse,
    unpublishCourse,
    batchDeleteCourses,

    // 工具方法
    formatDate,
    formatDateTime,
    getStatusColor,
    getDifficultyType,
    formatPrice,
    formatDuration,
    getDifficultyLevelText,
    getCourseStatusText,

    // 统计和导出
    getCourseStats,
    exportCourses
  }
}

// ==================== 章节管理 Composable ====================
export function useCourseChapter() {
  const loading = ref(false)
  const saving = ref(false)
  const chapters = ref([])

  /**
   * 加载课程章节
   * @param {String} courseId - 课程ID
   * @param {Number} status - 章节状态（可选）
   */
  const loadChapters = async (courseId, status = null) => {
    try {
      loading.value = true

      const response = await getCourseChaptersAPI(courseId, status)

      if (response.code === 200) {
        chapters.value = response.data || []
        return response.data
      } else {
        ElMessage.error(response.message || '获取章节列表失败')
        return []
      }
    } catch (error) {
      ElMessage.error('获取章节列表失败，请检查网络连接')
      return []
    } finally {
      loading.value = false
    }
  }

  /**
   * 创建章节
   * @param {String} courseId - 课程ID
   * @param {Object} chapterData - 章节数据
   */
  const createChapter = async (courseId, chapterData) => {
    try {
      saving.value = true

      const response = await createChapterAPI(courseId, chapterData)

      if (response.code === 200) {
        ElMessage.success('章节创建成功')
        await loadChapters(courseId) // 刷新章节列表
        return response.data
      } else {
        ElMessage.error(response.message || '章节创建失败')
        return null
      }
    } catch (error) {
      ElMessage.error('创建章节失败，请重试')
      return null
    } finally {
      saving.value = false
    }
  }

  /**
   * 更新章节
   * @param {String} courseId - 课程ID
   * @param {String} chapterId - 章节ID
   * @param {Object} chapterData - 章节数据
   */
  const updateChapter = async (courseId, chapterId, chapterData) => {
    try {
      saving.value = true

      const response = await updateChapterAPI(courseId, chapterId, chapterData)

      if (response.code === 200) {
        ElMessage.success('章节更新成功')
        await loadChapters(courseId) // 刷新章节列表
        return response.data
      } else {
        ElMessage.error(response.message || '章节更新失败')
        return null
      }
    } catch (error) {
      ElMessage.error('更新章节失败，请重试')
      return null
    } finally {
      saving.value = false
    }
  }

  /**
   * 删除章节
   * @param {String} courseId - 课程ID
   * @param {String} chapterId - 章节ID
   * @param {String} chapterTitle - 章节标题
   */
  const deleteChapter = async (courseId, chapterId, chapterTitle = '') => {
    try {
      await ElMessageBox.confirm(
        `确定要删除章节"${chapterTitle}"吗？删除后无法恢复。`,
        '确认删除',
        {
          confirmButtonText: '确定删除',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )


      const response = await deleteChapterAPI(courseId, chapterId)

      if (response.code === 200) {
        ElMessage.success('章节删除成功')
        await loadChapters(courseId) // 刷新章节列表
        return true
      } else {
        ElMessage.error(response.message || '章节删除失败')
        return false
      }
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('删除章节失败，请重试')
      }
      return false
    }
  }

  return {
    // 响应式数据
    loading,
    saving,
    chapters,

    // 章节操作
    loadChapters,
    createChapter,
    updateChapter,
    deleteChapter
  }
}
