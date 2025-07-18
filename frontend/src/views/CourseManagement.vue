<template>
  <div class="course-management-container">
    <!-- 页面标题和操作按钮 -->
    <div class="page-header">
      <div class="header-left">
        <h2>📚 课程管理</h2>
      </div>
      <div class="header-right">
        <el-button type="primary" size="large" @click="showAddCourseModal">
          <el-icon><Plus /></el-icon>
          新增课程
        </el-button>
      </div>
    </div>

    <!-- 课程筛选器 -->
    <div class="course-filter">
      <div class="filter-item">
        <span class="filter-label">搜索关键词</span>
        <el-input
          v-model="searchKeyword"
          placeholder="输入课程名称或描述"
          style="width: 240px"
        />
      </div>
      
      <div class="filter-item">
        <span class="filter-label">课程分类</span>
        <el-select v-model="categoryFilter" placeholder="全部分类" style="width: 140px">
          <el-option label="全部分类" value="" />
          <el-option label="技术培训" value="技术培训" />
          <el-option label="产品培训" value="产品培训" />
          <el-option label="安全培训" value="安全培训" />
          <el-option label="管理培训" value="管理培训" />
          <el-option label="营销培训" value="营销培训" />
        </el-select>
      </div>
      
      <div class="filter-item">
        <span class="filter-label">难度级别</span>
        <el-select v-model="levelFilter" placeholder="全部级别" style="width: 120px">
          <el-option label="全部级别" value="" />
          <el-option label="入门级" value="入门级" />
          <el-option label="初级" value="初级" />
          <el-option label="中级" value="中级" />
          <el-option label="高级" value="高级" />
          <el-option label="专家级" value="专家级" />
        </el-select>
      </div>
      
      <div class="filter-item">
        <span class="filter-label">课程状态</span>
        <el-select v-model="statusFilter" placeholder="全部状态" style="width: 120px">
          <el-option label="全部状态" value="" />
          <el-option label="草稿" value="草稿" />
          <el-option label="已发布" value="已发布" />
          <el-option label="已下架" value="已下架" />
        </el-select>
      </div>
      
      <div class="filter-buttons">
        <el-button type="primary" @click="filterCourses">搜索</el-button>
        <el-button @click="resetFilters">重置</el-button>
      </div>
    </div>

    <!-- 课程网格 -->
    <div class="course-grid">
      <div
        v-for="course in filteredCourses"
        :key="course.id"
        class="course-card"
      >
        <div class="course-title">{{ course.title }}</div>
        <div class="course-description">{{ course.description }}</div>
        
        <div class="course-meta">
          <span>📚 {{ course.category }}</span>
          <span>⭐ {{ course.level }}</span>
          <span>⏰ {{ course.duration }}小时</span>
        </div>
        
        <div class="course-meta">
          <span>👨‍🏫 {{ course.instructor }}</span>
          <span>👥 {{ course.studentCount }}人</span>
          <span>状态：<strong :style="{ color: getStatusColor(course.status) }">{{ course.status }}</strong></span>
        </div>
        
        <div class="course-meta">
          <span>📁 教材：{{ (course.materials || []).length }}个</span>
          <span>🎥 视频：{{ (course.videos || []).length }}个</span>
          <span>📋 章节：{{ (course.chapters || []).length }}个</span>
        </div>
        
        <div class="course-actions">
          <el-button type="primary" size="small" @click="editCourse(course.id)">
            编辑
          </el-button>
          <el-button
            v-if="course.status === '草稿'"
            type="success"
            size="small"
            @click="publishCourse(course.id)"
          >
            发布
          </el-button>
          <el-button
            v-else
            type="warning"
            size="small"
            @click="unpublishCourse(course.id)"
          >
            下架
          </el-button>
          <el-button size="small" @click="viewCourse(course.id)">
            查看
          </el-button>
          <el-button type="danger" size="small" @click="deleteCourse(course.id)">
            删除
          </el-button>
        </div>
      </div>
    </div>

    <!-- 新增/编辑课程对话框 -->
    <el-dialog
      v-model="courseModalVisible"
      :title="courseModalTitle"
      width="800px"
      top="5vh"
      :close-on-click-modal="false"
    >
      <el-form ref="courseFormRef" :model="courseForm" :rules="courseRules" label-width="120px">
        <!-- 基本信息 -->
        <h4 style="color: #667eea; margin-bottom: 15px;">📝 基本信息</h4>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="课程标题" prop="title">
              <el-input v-model="courseForm.title" placeholder="请输入课程标题" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="课程时长(小时)" prop="duration">
              <el-input-number
                v-model="courseForm.duration"
                :min="0"
                :step="0.5"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="课程描述" prop="description">
          <el-input
            v-model="courseForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入课程描述"
          />
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="课程分类" prop="category">
              <el-select v-model="courseForm.category" placeholder="请选择分类" style="width: 100%">
                <el-option label="技术培训" value="技术培训" />
                <el-option label="产品培训" value="产品培训" />
                <el-option label="安全培训" value="安全培训" />
                <el-option label="管理培训" value="管理培训" />
                <el-option label="营销培训" value="营销培训" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="难度级别" prop="level">
              <el-select v-model="courseForm.level" placeholder="请选择难度" style="width: 100%">
                <el-option label="入门级" value="入门级" />
                <el-option label="初级" value="初级" />
                <el-option label="中级" value="中级" />
                <el-option label="高级" value="高级" />
                <el-option label="专家级" value="专家级" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="讲师" prop="instructor">
              <el-input v-model="courseForm.instructor" placeholder="请输入讲师姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="价格(元)">
              <el-input-number
                v-model="courseForm.price"
                :min="0"
                :step="0.01"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 课程封面 -->
        <h4 style="color: #667eea; margin: 25px 0 15px 0;">🖼️ 课程封面</h4>
        <el-upload
          v-model:file-list="coverFileList"
          class="upload-area"
          drag
          :limit="1"
          accept="image/*"
          :auto-upload="false"
          :on-change="handleCoverChange"
          :on-remove="handleCoverRemove"
        >
          <div class="upload-content">
            <div class="upload-icon">📸</div>
            <div class="upload-text">点击上传封面图片</div>
            <div class="upload-hint">支持 JPG、PNG 格式，建议尺寸：800x450</div>
          </div>
        </el-upload>

        <!-- 学习资料 -->
        <h4 style="color: #667eea; margin: 25px 0 15px 0;">📁 学习资料</h4>
        <el-upload
          v-model:file-list="materialFileList"
          class="upload-area"
          drag
          multiple
          accept=".pdf,.doc,.docx,.ppt,.pptx,.xls,.xlsx"
          :auto-upload="false"
          :on-change="handleMaterialChange"
          :on-remove="handleMaterialRemove"
        >
          <div class="upload-content">
            <div class="upload-icon">📄</div>
            <div class="upload-text">点击上传学习资料</div>
            <div class="upload-hint">支持 PDF、DOC、PPT 等格式，可多选</div>
          </div>
        </el-upload>

        <!-- 视频资料 -->
        <h4 style="color: #667eea; margin: 25px 0 15px 0;">🎥 视频资料</h4>
        <el-upload
          v-model:file-list="videoFileList"
          class="upload-area"
          drag
          multiple
          accept="video/*"
          :auto-upload="false"
          :on-change="handleVideoChange"
          :on-remove="handleVideoRemove"
        >
          <div class="upload-content">
            <div class="upload-icon">🎬</div>
            <div class="upload-text">点击上传视频文件</div>
            <div class="upload-hint">支持 MP4、AVI、MOV 格式，可多选</div>
          </div>
        </el-upload>

        <!-- 章节管理 -->
        <h4 style="color: #667eea; margin: 25px 0 15px 0;">📚 章节管理</h4>
        <div style="margin-bottom: 15px;">
          <el-button type="primary" @click="addChapter">添加章节</el-button>
          <span style="color: #666; margin-left: 10px;">为课程添加章节，可以将学习资料分配给不同章节</span>
        </div>
        
        <div v-if="courseForm.chapters.length === 0" class="empty-chapters">
          <p style="color: #666; text-align: center; padding: 20px;">
            暂无章节，点击"添加章节"开始创建课程内容
          </p>
        </div>
        
        <div v-else class="chapter-list">
          <div
            v-for="(chapter, index) in sortedChapters"
            :key="chapter.id"
            class="chapter-item"
          >
            <div class="chapter-header">
              <div class="chapter-title">{{ chapter.title }}</div>
              <div class="chapter-duration">{{ chapter.duration }}小时</div>
            </div>
            <div class="chapter-content">{{ chapter.content || '暂无描述' }}</div>
            <div class="chapter-resources">
              <span
                v-for="material in chapter.materials || []"
                :key="material"
                class="resource-tag"
              >
                📁 {{ material }}
              </span>
              <span
                v-for="video in chapter.videos || []"
                :key="video"
                class="resource-tag"
              >
                🎥 {{ video }}
              </span>
              <span
                v-if="(!chapter.materials || chapter.materials.length === 0) && (!chapter.videos || chapter.videos.length === 0)"
                style="color: #999; font-size: 12px;"
              >
                暂无关联资源
              </span>
            </div>
            <div style="margin-top: 15px;">
              <el-button type="primary" size="small" @click="editChapter(getOriginalIndex(chapter.id))">
                编辑
              </el-button>
              <el-button
                size="small"
                @click="moveChapterUp(getOriginalIndex(chapter.id))"
                :disabled="index === 0"
              >
                ↑
              </el-button>
              <el-button
                size="small"
                @click="moveChapterDown(getOriginalIndex(chapter.id))"
                :disabled="index === sortedChapters.length - 1"
              >
                ↓
              </el-button>
              <el-button type="danger" size="small" @click="deleteChapter(getOriginalIndex(chapter.id))">
                删除
              </el-button>
            </div>
          </div>
        </div>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeCourseModal">取消</el-button>
          <el-button type="primary" @click="saveCourse">保存课程</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 章节编辑对话框 -->
    <el-dialog
      v-model="chapterModalVisible"
      title="编辑章节"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form ref="chapterFormRef" :model="chapterForm" :rules="chapterRules" label-width="100px">
        <el-form-item label="章节标题" prop="title">
          <el-input v-model="chapterForm.title" placeholder="请输入章节标题" />
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="章节时长" prop="duration">
              <el-input-number
                v-model="chapterForm.duration"
                :min="0"
                :step="0.5"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="章节顺序" prop="order">
              <el-input-number
                v-model="chapterForm.order"
                :min="1"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="章节内容">
          <el-input
            v-model="chapterForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入章节内容描述"
          />
        </el-form-item>
        
        <!-- 关联学习资料 -->
        <el-form-item label="关联学习资料">
          <div class="resource-selection">
            <el-checkbox-group v-model="chapterForm.materials">
              <div
                v-for="material in materialFileList"
                :key="material.name"
                class="resource-item"
              >
                <el-checkbox :label="material.name">
                  <div class="resource-display">
                    <div class="file-icon materials">DOC</div>
                    <span class="file-name">{{ material.name }}</span>
                  </div>
                </el-checkbox>
              </div>
            </el-checkbox-group>
            <div v-if="materialFileList.length === 0" class="empty-resources">
              暂无可用的学习资料
            </div>
          </div>
        </el-form-item>
        
        <!-- 关联视频资料 -->
        <el-form-item label="关联视频资料">
          <div class="resource-selection">
            <el-checkbox-group v-model="chapterForm.videos">
              <div
                v-for="video in videoFileList"
                :key="video.name"
                class="resource-item"
              >
                <el-checkbox :label="video.name">
                  <div class="resource-display">
                    <div class="file-icon video">MP4</div>
                    <span class="file-name">{{ video.name }}</span>
                  </div>
                </el-checkbox>
              </div>
            </el-checkbox-group>
            <div v-if="videoFileList.length === 0" class="empty-resources">
              暂无可用的视频资料
            </div>
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeChapterModal">取消</el-button>
          <el-button type="primary" @click="saveChapter">保存章节</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

// 响应式数据
const searchKeyword = ref('')
const categoryFilter = ref('')
const levelFilter = ref('')
const statusFilter = ref('')

const courseModalVisible = ref(false)
const chapterModalVisible = ref(false)
const courseModalTitle = ref('新增课程')
const currentEditingChapterIndex = ref(-1)

// 课程表单
const courseFormRef = ref()
const courseForm = reactive({
  title: '',
  description: '',
  category: '',
  level: '',
  duration: 0,
  instructor: '',
  price: 0,
  cover: null,
  materials: [],
  videos: [],
  chapters: []
})

// 章节表单
const chapterFormRef = ref()
const chapterForm = reactive({
  title: '',
  duration: 1,
  order: 1,
  content: '',
  materials: [],
  videos: []
})

// 文件列表
const coverFileList = ref([])
const materialFileList = ref([])
const videoFileList = ref([])

// 课程数据
const courses = ref([
  {
    id: 1,
    title: "产品基础知识培训",
    description: "全面了解公司产品的基础知识，包括产品特性、优势和应用场景",
    category: "产品培训",
    level: "入门级",
    duration: 2,
    instructor: "李经理",
    price: 0,
    status: "已发布",
    createTime: "2025-01-15",
    studentCount: 45,
    materials: ["产品介绍.pdf", "功能说明.ppt"],
    videos: ["产品演示.mp4"],
    chapters: [
      { id: 1, title: "第1章 产品概述", duration: 0.5, content: "产品基本介绍", order: 1 },
      { id: 2, title: "第2章 核心功能", duration: 1, content: "核心功能详解", order: 2 },
      { id: 3, title: "第3章 应用场景", duration: 0.5, content: "实际应用案例", order: 3 }
    ]
  },
  {
    id: 2,
    title: "市场分析与调研",
    description: "学习市场分析方法和调研技巧，提升市场洞察能力",
    category: "营销培训",
    level: "中级",
    duration: 3,
    instructor: "王总监",
    price: 299,
    status: "已发布",
    createTime: "2025-01-10",
    studentCount: 32,
    materials: ["市场分析报告.pdf", "调研方法.doc"],
    videos: ["案例分析.mp4", "实战演练.mp4"],
    chapters: [
      { id: 1, title: "第1章 市场分析基础", duration: 1, content: "市场分析基本概念", order: 1 },
      { id: 2, title: "第2章 调研方法", duration: 1.5, content: "各种调研方法介绍", order: 2 },
      { id: 3, title: "第3章 数据分析", duration: 0.5, content: "数据分析技巧", order: 3 }
    ]
  },
  {
    id: 3,
    title: "团队管理技巧",
    description: "提升团队管理能力，学习有效的团队沟通和协作方法",
    category: "管理培训",
    level: "高级",
    duration: 4,
    instructor: "张主管",
    price: 499,
    status: "草稿",
    createTime: "2025-01-08",
    studentCount: 0,
    materials: ["管理手册.pdf"],
    videos: [],
    chapters: []
  }
])

// 表单验证规则
const courseRules = {
  title: [{ required: true, message: '请输入课程标题', trigger: 'blur' }],
  description: [{ required: true, message: '请输入课程描述', trigger: 'blur' }],
  category: [{ required: true, message: '请选择课程分类', trigger: 'change' }],
  level: [{ required: true, message: '请选择难度级别', trigger: 'change' }],
  duration: [{ required: true, message: '请输入课程时长', trigger: 'blur' }],
  instructor: [{ required: true, message: '请输入讲师姓名', trigger: 'blur' }]
}

const chapterRules = {
  title: [{ required: true, message: '请输入章节标题', trigger: 'blur' }],
  duration: [{ required: true, message: '请输入章节时长', trigger: 'blur' }],
  order: [{ required: true, message: '请输入章节顺序', trigger: 'blur' }]
}

// 计算属性
const filteredCourses = computed(() => {
  return courses.value.filter(course => {
    const matchKeyword = !searchKeyword.value || 
      course.title.toLowerCase().includes(searchKeyword.value.toLowerCase()) ||
      course.description.toLowerCase().includes(searchKeyword.value.toLowerCase())
    
    const matchCategory = !categoryFilter.value || course.category === categoryFilter.value
    const matchLevel = !levelFilter.value || course.level === levelFilter.value
    const matchStatus = !statusFilter.value || course.status === statusFilter.value
    
    return matchKeyword && matchCategory && matchLevel && matchStatus
  })
})

const sortedChapters = computed(() => {
  return [...courseForm.chapters].sort((a, b) => (a.order || 0) - (b.order || 0))
})

// 方法
const getStatusColor = (status) => {
  switch(status) {
    case '已发布': return '#28a745'
    case '草稿': return '#ffc107'
    case '已下架': return '#dc3545'
    default: return '#6c757d'
  }
}

const getOriginalIndex = (chapterId) => {
  return courseForm.chapters.findIndex(c => c.id === chapterId)
}

const showAddCourseModal = () => {
  courseModalTitle.value = '新增课程'
  resetCourseForm()
  courseModalVisible.value = true
}

const resetCourseForm = () => {
  Object.assign(courseForm, {
    title: '',
    description: '',
    category: '',
    level: '',
    duration: 0,
    instructor: '',
    price: 0,
    cover: null,
    materials: [],
    videos: [],
    chapters: []
  })
  
  coverFileList.value = []
  materialFileList.value = []
  videoFileList.value = []
}

const closeCourseModal = () => {
  courseModalVisible.value = false
}

const editCourse = (courseId) => {
  const course = courses.value.find(c => c.id === courseId)
  if (course) {
    courseModalTitle.value = '编辑课程'
    Object.assign(courseForm, {
      ...course,
      chapters: [...(course.chapters || [])]
    })
    
    // 模拟文件列表（实际项目中需要根据后端返回的文件信息构造）
    coverFileList.value = course.cover ? [{ name: course.cover }] : []
    materialFileList.value = (course.materials || []).map(name => ({ name }))
    videoFileList.value = (course.videos || []).map(name => ({ name }))
    
    courseModalVisible.value = true
  }
}

const saveCourse = async () => {
  try {
    await courseFormRef.value.validate()
    
    const courseData = {
      ...courseForm,
      cover: coverFileList.value[0]?.name || null,
      materials: materialFileList.value.map(f => f.name),
      videos: videoFileList.value.map(f => f.name)
    }
    
    if (courseModalTitle.value === '新增课程') {
      const newCourse = {
        id: courses.value.length + 1,
        ...courseData,
        status: '草稿',
        createTime: new Date().toISOString().split('T')[0],
        studentCount: 0
      }
      courses.value.push(newCourse)
      ElMessage.success('课程创建成功！已保存为草稿状态')
    } else {
      ElMessage.success('课程更新成功！')
    }
    
    closeCourseModal()
  } catch (error) {
    ElMessage.error('请填写所有必填信息')
  }
}

const publishCourse = (courseId) => {
  const course = courses.value.find(c => c.id === courseId)
  if (course) {
    course.status = '已发布'
    ElMessage.success('课程发布成功')
  }
}

const unpublishCourse = (courseId) => {
  const course = courses.value.find(c => c.id === courseId)
  if (course) {
    course.status = '已下架'
    ElMessage.success('课程已下架')
  }
}

const viewCourse = (courseId) => {
  ElMessage.success(`查看课程详情：课程ID ${courseId}`)
}

const deleteCourse = async (courseId) => {
  try {
    await ElMessageBox.confirm('确定要删除这门课程吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const index = courses.value.findIndex(c => c.id === courseId)
    if (index > -1) {
      courses.value.splice(index, 1)
      ElMessage.success('课程删除成功')
    }
  } catch (error) {
    // 用户取消删除
  }
}

const filterCourses = () => {
  ElMessage.success('筛选功能已执行')
}

const resetFilters = () => {
  searchKeyword.value = ''
  categoryFilter.value = ''
  levelFilter.value = ''
  statusFilter.value = ''
  ElMessage.success('筛选条件已重置')
}

// 文件上传相关方法
const handleCoverChange = (file, fileList) => {
  coverFileList.value = fileList
}

const handleCoverRemove = () => {
  coverFileList.value = []
}

const handleMaterialChange = (file, fileList) => {
  materialFileList.value = fileList
}

const handleMaterialRemove = (file, fileList) => {
  materialFileList.value = fileList
}

const handleVideoChange = (file, fileList) => {
  videoFileList.value = fileList
}

const handleVideoRemove = (file, fileList) => {
  videoFileList.value = fileList
}

// 章节管理方法
const addChapter = () => {
  const newChapter = {
    id: courseForm.chapters.length + 1,
    title: `第${courseForm.chapters.length + 1}章`,
    duration: 1,
    content: '',
    materials: [],
    videos: [],
    order: courseForm.chapters.length + 1
  }
  courseForm.chapters.push(newChapter)
}

const editChapter = (index) => {
  if (index < 0 || index >= courseForm.chapters.length) {
    ElMessage.error('章节索引无效')
    return
  }
  
  currentEditingChapterIndex.value = index
  const chapter = courseForm.chapters[index]
  
  Object.assign(chapterForm, {
    title: chapter.title,
    duration: chapter.duration,
    order: chapter.order,
    content: chapter.content || '',
    materials: [...(chapter.materials || [])],
    videos: [...(chapter.videos || [])]
  })
  
  chapterModalVisible.value = true
}

const closeChapterModal = () => {
  chapterModalVisible.value = false
  currentEditingChapterIndex.value = -1
}

const saveChapter = async () => {
  try {
    await chapterFormRef.value.validate()
    
    // 检查章节顺序是否重复
    const existingOrders = courseForm.chapters
      .map((chapter, index) => index === currentEditingChapterIndex.value ? null : chapter.order)
      .filter(order => order !== null)
    
    if (existingOrders.includes(chapterForm.order)) {
      ElMessage.error('章节顺序不能重复，请选择其他顺序')
      return
    }
    
    // 更新章节信息
    const chapter = courseForm.chapters[currentEditingChapterIndex.value]
    Object.assign(chapter, { ...chapterForm })
    
    closeChapterModal()
    ElMessage.success('章节信息已更新')
  } catch (error) {
    ElMessage.error('请填写所有必填信息')
  }
}

const moveChapterUp = (index) => {
  if (index > 0) {
    const chapter = courseForm.chapters[index]
    const prevChapter = courseForm.chapters[index - 1]
    
    // 交换顺序
    const tempOrder = chapter.order
    chapter.order = prevChapter.order
    prevChapter.order = tempOrder
  }
}

const moveChapterDown = (index) => {
  if (index < courseForm.chapters.length - 1) {
    const chapter = courseForm.chapters[index]
    const nextChapter = courseForm.chapters[index + 1]
    
    // 交换顺序
    const tempOrder = chapter.order
    chapter.order = nextChapter.order
    nextChapter.order = tempOrder
  }
}

const deleteChapter = async (index) => {
  try {
    await ElMessageBox.confirm('确定要删除这个章节吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    courseForm.chapters.splice(index, 1)
    // 重新分配顺序
    courseForm.chapters.forEach((chapter, idx) => {
      chapter.order = idx + 1
    })
  } catch (error) {
    // 用户取消删除
  }
}

onMounted(() => {
  console.log('课程管理页面已加载')
})
</script>

<style scoped>
.course-management-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #667eea;
  font-size: 24px;
  font-weight: 600;
}

/* 课程筛选器样式 */
.course-filter {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  padding: 20px;
  border-radius: 15px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.filter-label {
  font-size: 14px;
  color: #666;
  font-weight: 500;
  white-space: nowrap;
  min-width: fit-content;
}

.filter-buttons {
  display: flex;
  gap: 10px;
  margin-left: auto;
}

/* 课程网格样式 */
.course-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.course-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 15px;
  padding: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.course-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.course-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
  line-height: 1.4;
}

.course-description {
  color: #666;
  font-size: 14px;
  margin-bottom: 16px;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.course-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  font-size: 12px;
  color: #888;
  flex-wrap: wrap;
  gap: 8px;
}

.course-meta span {
  display: inline-flex;
  align-items: center;
  white-space: nowrap;
  padding: 2px 6px;
  background: rgba(102, 126, 234, 0.1);
  border-radius: 4px;
}

.course-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

/* 上传区域样式 */
.upload-area {
  border: 2px dashed #667eea;
  border-radius: 10px;
  background: rgba(102, 126, 234, 0.05);
  transition: all 0.3s ease;
}

.upload-area:hover {
  border-color: #764ba2;
  background: rgba(102, 126, 234, 0.1);
}

.upload-content {
  text-align: center;
  padding: 30px;
}

.upload-icon {
  font-size: 48px;
  color: #667eea;
  margin-bottom: 15px;
}

.upload-text {
  color: #667eea;
  font-weight: 500;
  margin-bottom: 10px;
}

.upload-hint {
  color: #666;
  font-size: 12px;
}

/* 章节列表样式 */
.empty-chapters {
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid #e0e0e0;
  border-radius: 10px;
  padding: 20px;
  text-align: center;
}

.chapter-list {
  margin-top: 20px;
}

.chapter-item {
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid #e0e0e0;
  border-radius: 10px;
  padding: 20px;
  margin-bottom: 15px;
  transition: all 0.3s ease;
}

.chapter-item:hover {
  border-color: #667eea;
  transform: translateY(-2px);
}

.chapter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.chapter-title {
  font-weight: 600;
  color: #333;
  font-size: 16px;
}

.chapter-duration {
  color: #667eea;
  font-size: 14px;
  font-weight: 500;
}

.chapter-content {
  color: #666;
  line-height: 1.6;
  margin-bottom: 15px;
  font-size: 14px;
}

.chapter-resources {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 15px;
}

.resource-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  background: rgba(102, 126, 234, 0.15);
  color: #667eea;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

/* 资源选择样式 */
.resource-selection {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 15px;
  background: #f8f9fa;
  min-height: 80px;
  max-height: 200px;
  overflow-y: auto;
}

.resource-item {
  margin-bottom: 12px;
}

.resource-display {
  display: flex;
  align-items: center;
  gap: 10px;
}

.file-icon {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: bold;
  font-size: 10px;
  flex-shrink: 0;
}

.file-icon.materials { background: #0d6efd; }
.file-icon.video { background: #6610f2; }

.file-name {
  flex: 1;
  font-size: 14px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.empty-resources {
  color: #999;
  text-align: center;
  padding: 20px;
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .course-filter {
    flex-direction: column;
    align-items: stretch;
    gap: 15px;
  }
  
  .filter-item {
    justify-content: space-between;
  }
  
  .filter-buttons {
    margin-left: 0;
    justify-content: center;
  }
  
  .course-grid {
    grid-template-columns: 1fr;
  }
  
  .page-header {
    flex-direction: column;
    gap: 15px;
    align-items: stretch;
  }
  
  .course-actions {
    justify-content: center;
  }
}

/* Element Plus 样式覆盖 */
:deep(.el-dialog__body) {
  max-height: 70vh;
  overflow-y: auto;
}

:deep(.el-upload-dragger) {
  background: transparent;
  border: none;
  width: 100%;
}

:deep(.el-checkbox-group) {
  width: 100%;
}

:deep(.el-checkbox) {
  width: 100%;
  margin-right: 0;
  margin-bottom: 8px;
}

:deep(.el-checkbox__label) {
  width: 100%;
  padding-left: 8px;
}
</style>