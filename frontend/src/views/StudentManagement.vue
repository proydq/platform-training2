<template>
  <div class="student-management-container">
    <!-- 顶部统计概览 - 独立卡片 -->
    <div class="stats-overview">
      <div class="stat-item">
        <div class="stat-number">156</div>
        <div class="stat-label">总学员数</div>
      </div>
      <div class="stat-item">
        <div class="stat-number">89%</div>
        <div class="stat-label">活跃率</div>
      </div>
      <div class="stat-item">
        <div class="stat-number">78%</div>
        <div class="stat-label">平均完成率</div>
      </div>
      <div class="stat-item">
        <div class="stat-number">85</div>
        <div class="stat-label">平均分</div>
      </div>
    </div>

    <!-- 主要内容卡片 -->
    <div class="main-card">
      <!-- 页面标题和操作按钮 -->
      <div class="page-header">
        <h2>👥 学员管理</h2>
        <div class="header-actions">
          <button class="btn btn-primary" @click="exportData">📥 导出数据</button>
          <button class="btn btn-secondary" @click="sendBatchNotification">📢 批量通知</button>
          <button class="btn btn-secondary" @click="addNewStudent">➕ 添加学员</button>
        </div>
      </div>
      
      <!-- 搜索和筛选区域 -->
      <div class="student-filters">
        <input 
          v-model="filters.search"
          type="text" 
          placeholder="搜索学员姓名或邮箱..." 
          @input="searchStudents"
        />
        <select v-model="filters.department" @change="filterStudents">
          <option value="">全部部门</option>
          <option value="product">产品部</option>
          <option value="technology">技术部</option>
          <option value="marketing">市场部</option>
          <option value="design">设计部</option>
          <option value="hr">人事部</option>
        </select>
        <select v-model="filters.status" @change="filterStudents">
          <option value="">全部状态</option>
          <option value="active">活跃</option>
          <option value="inactive">不活跃</option>
          <option value="completed">已完成</option>
        </select>
        <button class="btn btn-secondary" @click="refreshData">🔄 刷新</button>
      </div>
      
      <!-- 学员表格 -->
      <table class="student-table">
        <thead>
          <tr>
            <th>学员信息</th>
            <th>部门</th>
            <th>学习进度</th>
            <th>考试成绩</th>
            <th>状态</th>
            <th>最后活跃</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="student in paginatedStudents" :key="student.id">
            <td>
              <div class="student-info">
                <div class="student-avatar">{{ student.avatar }}</div>
                <div class="student-details">
                  <div class="student-name">{{ student.name }}</div>
                  <div class="student-email">{{ student.email }}</div>
                </div>
              </div>
            </td>
            <td>
              <span class="department-tag">{{ getDepartmentLabel(student.department) }}</span>
            </td>
            <td>
              <div class="progress-container">
                <div class="progress-bar">
                  <div class="progress-fill" :style="{ width: student.progress + '%' }"></div>
                </div>
                <span class="progress-text">{{ student.progress }}%</span>
              </div>
            </td>
            <td>
              <span class="score-text" :class="getScoreClass(student.avgScore)">
                {{ student.avgScore }}分
              </span>
            </td>
            <td>
              <span class="status-badge" :class="getStatusClass(student.status)">
                {{ getStatusLabel(student.status) }}
              </span>
            </td>
            <td>{{ student.lastActive }}</td>
            <td>
              <div class="action-buttons">
                <button class="action-btn view-btn" @click="viewStudent(student)">查看</button>
                <button class="action-btn edit-btn" @click="editStudent(student)">编辑</button>
                <button class="action-btn message-btn" @click="sendMessage(student)">消息</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      
      <!-- 分页控件 - 在同一个卡片背景下 -->
      <div class="pagination-section">
        <div class="pagination-info">
          <span>共 <strong>{{ totalStudents }}</strong> 名学员</span>
          <span class="page-size-selector">
            每页显示 
            <select v-model="pageSize" @change="handlePageSizeChange">
              <option value="10">10</option>
              <option value="20">20</option>
              <option value="50">50</option>
            </select> 条
          </span>
        </div>
        <div class="pagination-controls">
          <button 
            class="btn btn-secondary" 
            @click="previousPage" 
            :disabled="currentPage === 1"
          >
            上一页
          </button>
          <span class="page-info">
            第 <strong>{{ currentPage }}</strong> 页，共 <strong>{{ totalPages }}</strong> 页
          </span>
          <button 
            class="btn btn-secondary" 
            @click="nextPage" 
            :disabled="currentPage === totalPages"
          >
            下一页
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'

// 响应式数据
const filters = ref({
  department: '',
  status: '',
  search: ''
})

const currentPage = ref(1)
const pageSize = ref(20)

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
  },
  {
    id: 6, name: '孙八', email: 'sunba@company.com', 
    department: 'product', progress: 40, avgScore: 65, 
    status: 'active', lastActive: '4小时前', avatar: '孙'
  },
  {
    id: 7, name: '周九', email: 'zhoujiu@company.com', 
    department: 'technology', progress: 90, avgScore: 95, 
    status: 'active', lastActive: '1小时前', avatar: '周'
  },
  {
    id: 8, name: '吴十', email: 'wushi@company.com', 
    department: 'marketing', progress: 75, avgScore: 80, 
    status: 'completed', lastActive: '6小时前', avatar: '吴'
  }
])

// 计算属性
const filteredStudents = computed(() => {
  let result = students.value

  if (filters.value.department) {
    result = result.filter(student => student.department === filters.value.department)
  }

  if (filters.value.status) {
    result = result.filter(student => student.status === filters.value.status)
  }

  if (filters.value.search) {
    const keyword = filters.value.search.toLowerCase()
    result = result.filter(student => 
      student.name.toLowerCase().includes(keyword) ||
      student.email.toLowerCase().includes(keyword)
    )
  }

  return result
})

const totalStudents = computed(() => filteredStudents.value.length)
const totalPages = computed(() => Math.ceil(totalStudents.value / pageSize.value))

const paginatedStudents = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredStudents.value.slice(start, end)
})

// 方法
const getDepartmentLabel = (department) => {
  const labels = {
    'product': '产品部',
    'technology': '技术部',
    'marketing': '市场部',
    'design': '设计部',
    'hr': '人事部'
  }
  return labels[department] || department
}

const getStatusLabel = (status) => {
  const labels = {
    'active': '活跃',
    'inactive': '不活跃',
    'completed': '已完成'
  }
  return labels[status] || status
}

const getStatusClass = (status) => {
  return `status-${status}`
}

const getScoreClass = (score) => {
  if (score >= 90) return 'score-excellent'
  if (score >= 80) return 'score-good'
  if (score >= 60) return 'score-pass'
  return 'score-fail'
}

const searchStudents = () => {
  currentPage.value = 1
}

const filterStudents = () => {
  currentPage.value = 1
}

const handlePageSizeChange = () => {
  currentPage.value = 1
}

const previousPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--
  }
}

const nextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++
  }
}

const exportData = () => {
  ElMessage.success('正在导出学员数据...')
}

const sendBatchNotification = () => {
  ElMessage.success('正在发送批量通知...')
}

const addNewStudent = () => {
  ElMessage.info('打开添加学员对话框')
}

const refreshData = () => {
  ElMessage.success('数据已刷新')
}

const viewStudent = (student) => {
  ElMessage.info(`查看学员：${student.name}`)
}

const editStudent = (student) => {
  ElMessage.info(`编辑学员：${student.name}`)
}

const sendMessage = (student) => {
  ElMessage.success(`已向 ${student.name} 发送消息`)
}
</script>

<style scoped>
.student-management-container {
  /* Layout已处理容器样式 */
}

/* 顶部统计概览 */
.stats-overview {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.stat-item {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 15px;
  padding: 30px 20px;
  text-align: center;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.stat-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.15);
}

.stat-item .stat-number {
  font-size: 36px;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 8px;
}

.stat-item .stat-label {
  color: #666;
  font-size: 16px;
  font-weight: 500;
}

/* 主卡片 - 包含除统计外的所有内容 */
.main-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 15px;
  padding: 25px;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  /* 所有内容都在这个背景下 */
}

/* 页面头部 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.page-header h2 {
  margin: 0;
  color: #333;
  font-size: 20px;
  font-weight: 600;
}

.header-actions {
  display: flex;
  gap: 10px;
}

/* 按钮样式 */
.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.btn-secondary {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
  border: 1px solid rgba(102, 126, 234, 0.2);
}

.btn-secondary:hover {
  background: rgba(102, 126, 234, 0.2);
}

.btn-secondary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 筛选区域 */
.student-filters {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
  align-items: center;
}

.student-filters input,
.student-filters select {
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
}

.student-filters input {
  flex: 1;
}

/* 表格样式 */
.student-table {
  width: 100%;
  border-collapse: collapse;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.student-table th,
.student-table td {
  padding: 15px;
  text-align: left;
  border-bottom: 1px solid #e0e0e0;
}

.student-table th {
  background: rgba(102, 126, 234, 0.1);
  font-weight: 600;
  color: #333;
}

.student-table tbody tr {
  background: white;
}

.student-table tbody tr:hover {
  background: rgba(102, 126, 234, 0.05);
}

/* 学员信息 */
.student-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.student-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: bold;
  font-size: 14px;
}

.student-name {
  font-weight: 600;
  color: #333;
  margin-bottom: 2px;
}

.student-email {
  color: #666;
  font-size: 12px;
}

/* 部门标签 */
.department-tag {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
}

/* 进度条 */
.progress-container {
  display: flex;
  align-items: center;
  gap: 8px;
}

.progress-bar {
  width: 100px;
  height: 8px;
  background: #e0e0e0;
  border-radius: 4px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #667eea, #764ba2);
  transition: width 0.3s ease;
}

.progress-text {
  font-size: 12px;
  color: #666;
  min-width: 35px;
}

/* 成绩样式 */
.score-excellent {
  color: #28a745;
  font-weight: bold;
}

.score-good {
  color: #007bff;
  font-weight: bold;
}

.score-pass {
  color: #ffc107;
}

.score-fail {
  color: #dc3545;
  font-weight: bold;
}

/* 状态标签 */
.status-badge {
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.status-active {
  background: #d4edda;
  color: #155724;
}

.status-inactive {
  background: #f8d7da;
  color: #721c24;
}

.status-completed {
  background: #cce7ff;
  color: #0066cc;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 5px;
}

.action-btn {
  padding: 6px 12px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.3s ease;
}

.view-btn {
  background: #007bff;
  color: white;
}

.edit-btn {
  background: #28a745;
  color: white;
}

.message-btn {
  background: #ffc107;
  color: #333;
}

.action-btn:hover {
  transform: translateY(-1px);
}

/* 分页区域 - 重要：在同一个卡片背景下 */
.pagination-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e0e0e0;
  /* 注意：这里没有单独的背景色，使用父级卡片的背景 */
}

.pagination-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.page-size-selector select {
  padding: 5px;
  border: 1px solid #ddd;
  border-radius: 4px;
  margin: 0 5px;
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 15px;
}

.page-info {
  color: #666;
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .stats-overview {
    grid-template-columns: repeat(2, 1fr);
  }
}
@media (max-width: 1200px) {
  .student-table {
    font-size: 12px;
  }
  
  .student-table th,
  .student-table td {
    padding: 10px;
  }
}

@media (max-width: 768px) {
  .stats-overview {
    grid-template-columns: repeat(2, 1fr);
    gap: 15px;
  }
  
  .stat-item {
    padding: 20px 15px;
  }
  
  .stat-item .stat-number {
    font-size: 28px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 15px;
    align-items: stretch;
  }
  
  .header-actions {
    justify-content: center;
  }
  
  .student-filters {
    flex-direction: column;
    gap: 10px;
  }
  
  .pagination-section {
    flex-direction: column;
    gap: 15px;
  }
  
  .student-table {
    display: block;
    overflow-x: auto;
    white-space: nowrap;
  }
}

@media (max-width: 480px) {
  .stats-overview {
    grid-template-columns: 1fr;
  }
}
</style>