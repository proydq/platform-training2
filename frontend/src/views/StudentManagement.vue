<template>
  <div class="student-management-container">
    <!-- 统计概览 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon">👥</div>
        <div class="stat-content">
          <div class="stat-number">{{ stats.total }}</div>
          <div class="stat-label">总学员数</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon">✅</div>
        <div class="stat-content">
          <div class="stat-number">{{ stats.active }}</div>
          <div class="stat-label">活跃学员</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon">📈</div>
        <div class="stat-content">
          <div class="stat-number">{{ stats.avgProgress }}%</div>
          <div class="stat-label">平均进度</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon">🎯</div>
        <div class="stat-content">
          <div class="stat-number">{{ stats.avgScore }}</div>
          <div class="stat-label">平均成绩</div>
        </div>
      </div>
    </div>

    <!-- 筛选和操作区域 -->
    <el-card class="filter-card">
      <div class="student-filters">
        <div class="filter-left">
          <el-select
            v-model="filters.department"
            placeholder="选择部门"
            clearable
            style="width: 150px"
          >
            <el-option
              v-for="dept in departments"
              :key="dept.value"
              :label="dept.label"
              :value="dept.value"
            />
          </el-select>
          
          <el-select
            v-model="filters.status"
            placeholder="选择状态"
            clearable
            style="width: 150px"
          >
            <el-option
              v-for="status in statusOptions"
              :key="status.value"
              :label="status.label"
              :value="status.value"
            />
          </el-select>
          
          <el-input
            v-model="filters.search"
            placeholder="搜索学员姓名或邮箱"
            prefix-icon="Search"
            clearable
            style="width: 250px"
            @input="handleSearch"
          />
        </div>
        
        <div class="filter-right">
          <el-button type="primary" @click="exportData">
            <el-icon><Download /></el-icon>
            导出数据
          </el-button>
          <el-button @click="refreshData">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 学员表格 -->
    <el-card class="table-card">
      <el-table
        v-loading="loading"
        :data="paginatedStudents"
        stripe
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        
        <el-table-column label="学员信息" min-width="200">
          <template #default="{ row }">
            <div class="student-info">
              <el-avatar :size="40" class="student-avatar">
                {{ row.avatar }}
              </el-avatar>
              <div class="student-details">
                <div class="student-name">{{ row.name }}</div>
                <div class="student-email">{{ row.email }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="department" label="部门" width="120">
          <template #default="{ row }">
            <el-tag :type="getDepartmentTagType(row.department)">
              {{ getDepartmentName(row.department) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="学习进度" width="150">
          <template #default="{ row }">
            <div class="progress-cell">
              <el-progress
                :percentage="row.progress"
                :color="getProgressColor(row.progress)"
                :stroke-width="8"
              />
              <span class="progress-text">{{ row.progress }}%</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="avgScore" label="考试成绩" width="100">
          <template #default="{ row }">
            <span :class="{ 'high-score': row.avgScore >= 80, 'low-score': row.avgScore < 60 }">
              {{ row.avgScore }}分
            </span>
          </template>
        </el-table-column>
        
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="lastActive" label="最后活跃" width="120" />
        
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewStudent(row)">
              查看
            </el-button>
            <el-button size="small" type="primary" @click="editStudent(row)">
              编辑
            </el-button>
            <el-dropdown @command="(command) => handleAction(command, row)">
              <el-button size="small">
                更多<el-icon class="el-icon--right"><arrow-down /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="resetPassword">重置密码</el-dropdown-item>
                  <el-dropdown-item command="sendNotification">发送通知</el-dropdown-item>
                  <el-dropdown-item command="exportReport">导出报告</el-dropdown-item>
                  <el-dropdown-item divided command="delete" class="danger-item">
                    删除学员
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 批量操作 -->
      <div v-if="selectedStudents.length > 0" class="batch-actions">
        <span class="batch-info">已选择 {{ selectedStudents.length }} 名学员</span>
        <el-button size="small" @click="batchSendNotification">
          批量发送通知
        </el-button>
        <el-button size="small" @click="batchExport">
          批量导出
        </el-button>
        <el-button size="small" type="danger" @click="batchDelete">
          批量删除
        </el-button>
      </div>
    </el-card>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="filteredStudents.length"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 学员详情弹窗 -->
    <el-dialog
      v-model="studentDialogVisible"
      :title="dialogTitle"
      width="800px"
      @close="closeStudentDialog"
    >
      <div v-if="selectedStudent" class="student-dialog-content">
        <div class="student-profile">
          <el-avatar :size="80" class="profile-avatar">
            {{ selectedStudent.avatar }}
          </el-avatar>
          <div class="profile-info">
            <h3>{{ selectedStudent.name }}</h3>
            <p>{{ selectedStudent.email }}</p>
            <el-tag :type="getDepartmentTagType(selectedStudent.department)">
              {{ getDepartmentName(selectedStudent.department) }}
            </el-tag>
          </div>
        </div>
        
        <el-divider />
        
        <div class="student-stats">
          <div class="stat-item">
            <div class="stat-label">学习进度</div>
            <div class="stat-value">{{ selectedStudent.progress }}%</div>
          </div>
          <div class="stat-item">
            <div class="stat-label">平均成绩</div>
            <div class="stat-value">{{ selectedStudent.avgScore }}分</div>
          </div>
          <div class="stat-item">
            <div class="stat-label">完成课程</div>
            <div class="stat-value">{{ selectedStudent.completedCourses || 12 }}</div>
          </div>
          <div class="stat-item">
            <div class="stat-label">学习时长</div>
            <div class="stat-value">{{ selectedStudent.studyHours || 156 }}h</div>
          </div>
        </div>
        
        <el-divider />
        
        <div class="recent-activities">
          <h4>最近学习记录</h4>
          <el-timeline>
            <el-timeline-item
              v-for="activity in studentActivities"
              :key="activity.id"
              :timestamp="activity.time"
            >
              {{ activity.description }}
            </el-timeline-item>
          </el-timeline>
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="closeStudentDialog">关闭</el-button>
          <el-button type="primary" @click="editStudent(selectedStudent)">
            编辑信息
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download, Refresh, Search, ArrowDown } from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const selectedStudents = ref([])
const studentDialogVisible = ref(false)
const selectedStudent = ref(null)
const dialogTitle = ref('')

// 统计数据
const stats = ref({
  total: 156,
  active: 132,
  avgProgress: 75,
  avgScore: 87
})

// 筛选条件
const filters = ref({
  department: '',
  status: '',
  search: ''
})

// 分页
const currentPage = ref(1)
const pageSize = ref(20)

// 部门选项
const departments = [
  { value: 'product', label: '产品部门' },
  { value: 'technology', label: '技术部门' },
  { value: 'marketing', label: '市场部门' },
  { value: 'design', label: '设计部门' },
  { value: 'hr', label: '人力资源' }
]

// 状态选项
const statusOptions = [
  { value: 'active', label: '活跃' },
  { value: 'inactive', label: '不活跃' },
  { value: 'completed', label: '已完成' },
  { value: 'pending', label: '待激活' }
]

// 学员数据
const students = ref([
  {
    id: 1, name: '张三', email: 'zhangsan@company.com', 
    department: 'product', progress: 85, avgScore: 92, 
    status: 'active', lastActive: '2小时前', avatar: '张'
  },
  {
    id: 2, name: '李四', email: 'lisi@company.com', 
    department: 'technology', progress: 60, avgScore: 78, 
    status: 'active', lastActive: '1天前', avatar: '李'
  },
  {
    id: 3, name: '王五', email: 'wangwu@company.com', 
    department: 'marketing', progress: 30, avgScore: 45, 
    status: 'inactive', lastActive: '5天前', avatar: '王'
  },
  {
    id: 4, name: '赵六', email: 'zhaoliu@company.com', 
    department: 'design', progress: 95, avgScore: 88, 
    status: 'completed', lastActive: '1小时前', avatar: '赵'
  },
  {
    id: 5, name: '钱七', email: 'qianqi@company.com', 
    department: 'hr', progress: 70, avgScore: 82, 
    status: 'active', lastActive: '3小时前', avatar: '钱'
  }
])

// 学员活动记录
const studentActivities = ref([
  { id: 1, time: '2025-01-18 14:30', description: '完成了《产品基础培训》课程' },
  { id: 2, time: '2025-01-17 10:15', description: '参加了《销售技能测试》考试，得分92分' },
  { id: 3, time: '2025-01-16 16:45', description: '开始学习《客户服务标准》课程' },
  { id: 4, time: '2025-01-15 09:20', description: '登录系统，查看学习进度' }
])

// 计算属性
const filteredStudents = computed(() => {
  let result = students.value

  // 部门筛选
  if (filters.value.department) {
    result = result.filter(student => student.department === filters.value.department)
  }

  // 状态筛选
  if (filters.value.status) {
    result = result.filter(student => student.status === filters.value.status)
  }

  // 搜索筛选
  if (filters.value.search) {
    const keyword = filters.value.search.toLowerCase()
    result = result.filter(student => 
      student.name.toLowerCase().includes(keyword) ||
      student.email.toLowerCase().includes(keyword)
    )
  }

  return result
})

const paginatedStudents = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredStudents.value.slice(start, end)
})

// 方法
const getDepartmentName = (dept) => {
  const deptMap = {
    'product': '产品部门',
    'technology': '技术部门', 
    'marketing': '市场部门',
    'design': '设计部门',
    'hr': '人力资源'
  }
  return deptMap[dept] || dept
}

const getDepartmentTagType = (dept) => {
  const typeMap = {
    'product': 'primary',
    'technology': 'success',
    'marketing': 'warning',
    'design': 'info',
    'hr': 'danger'
  }
  return typeMap[dept] || ''
}

const getStatusText = (status) => {
  const statusMap = {
    'active': '活跃',
    'inactive': '不活跃',
    'completed': '已完成',
    'pending': '待激活'
  }
  return statusMap[status] || status
}

const getStatusTagType = (status) => {
  const typeMap = {
    'active': 'success',
    'inactive': 'info',
    'completed': 'primary',
    'pending': 'warning'
  }
  return typeMap[status] || ''
}

const getProgressColor = (progress) => {
  if (progress >= 80) return '#67c23a'
  if (progress >= 60) return '#e6a23c'
  return '#f56c6c'
}

const handleSearch = () => {
  currentPage.value = 1
}

const handleSelectionChange = (selection) => {
  selectedStudents.value = selection
}

const viewStudent = (student) => {
  selectedStudent.value = student
  dialogTitle.value = `学员详情 - ${student.name}`
  studentDialogVisible.value = true
}

const editStudent = (student) => {
  ElMessage.info(`编辑学员：${student.name}`)
  // 实现编辑功能
}

const handleAction = async (command, student) => {
  switch (command) {
    case 'resetPassword':
      await resetPassword(student)
      break
    case 'sendNotification':
      sendNotification([student])
      break
    case 'exportReport':
      exportStudentReport(student)
      break
    case 'delete':
      await deleteStudent(student)
      break
  }
}

const resetPassword = async (student) => {
  try {
    await ElMessageBox.confirm(
      `确定要重置 ${student.name} 的密码吗？`,
      '重置密码',
      { type: 'warning' }
    )
    ElMessage.success('密码重置成功，新密码已发送到学员邮箱')
  } catch {
    // 用户取消
  }
}

const sendNotification = (studentList) => {
  ElMessage.success(`向 ${studentList.length} 名学员发送通知`)
  // 实现发送通知功能
}

const exportStudentReport = (student) => {
  ElMessage.success(`导出 ${student.name} 的学习报告`)
  // 实现导出功能
}

const deleteStudent = async (student) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除学员 ${student.name} 吗？此操作不可恢复。`,
      '删除学员',
      { type: 'error' }
    )
    ElMessage.success('学员删除成功')
    // 从列表中移除
    const index = students.value.findIndex(s => s.id === student.id)
    if (index > -1) {
      students.value.splice(index, 1)
    }
  } catch {
    // 用户取消
  }
}

const batchSendNotification = () => {
  sendNotification(selectedStudents.value)
  selectedStudents.value = []
}

const batchExport = () => {
  ElMessage.success(`批量导出 ${selectedStudents.value.length} 名学员数据`)
  selectedStudents.value = []
}

const batchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedStudents.value.length} 名学员吗？`,
      '批量删除',
      { type: 'error' }
    )
    ElMessage.success('批量删除成功')
    selectedStudents.value = []
  } catch {
    // 用户取消
  }
}

const exportData = () => {
  ElMessage.success('数据导出中...')
  // 实现导出功能
}

const refreshData = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
    ElMessage.success('数据刷新成功')
  }, 1000)
}

const closeStudentDialog = () => {
  studentDialogVisible.value = false
  selectedStudent.value = null
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
}

const handleCurrentChange = (page) => {
  currentPage.value = page
}

onMounted(() => {
  // 初始化数据
})
</script>

<style scoped>
.student-management-container {
  max-width: 1400px;
  margin: 0 auto;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 15px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 15px;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.15);
}

.stat-icon {
  font-size: 24px;
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 10px;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  color: #666;
  font-size: 14px;
}

.filter-card,
.table-card {
  margin-bottom: 20px;
  border-radius: 15px;
  border: none;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
}

.student-filters {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 15px;
}

.filter-left {
  display: flex;
  gap: 15px;
  align-items: center;
}

.filter-right {
  display: flex;
  gap: 10px;
}

.student-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.student-avatar {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  font-weight: bold;
}

.student-name {
  font-weight: 500;
  color: #333;
}

.student-email {
  font-size: 12px;
  color: #666;
}

.progress-cell {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.progress-text {
  font-size: 12px;
  color: #666;
  text-align: center;
}

.high-score {
  color: #67c23a;
  font-weight: bold;
}

.low-score {
  color: #f56c6c;
  font-weight: bold;
}

.batch-actions {
  margin-top: 15px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 15px;
}

.batch-info {
  color: #666;
  font-size: 14px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.student-dialog-content {
  max-height: 600px;
  overflow-y: auto;
}

.student-profile {
  display: flex;
  align-items: center;
  gap: 20px;
}

.profile-avatar {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  font-weight: bold;
}

.profile-info h3 {
  margin: 0 0 5px 0;
  color: #333;
}

.profile-info p {
  margin: 0 0 10px 0;
  color: #666;
}

.student-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 20px;
}

.stat-item {
  text-align: center;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}

.stat-label {
  font-size: 12px;
  color: #666;
  margin-bottom: 5px;
}

.stat-value {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.recent-activities h4 {
  margin: 0 0 15px 0;
  color: #333;
}

.danger-item {
  color: #f56c6c !important;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .student-filters {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filter-left {
    flex-direction: column;
  }
  
  .student-profile {
    flex-direction: column;
    text-align: center;
  }
  
  .student-stats {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>