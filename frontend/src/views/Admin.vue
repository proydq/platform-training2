<template>
  <div class="admin-container">
    <!-- 系统概览 -->
    <div class="overview-section">
      <h2>⚙️ 系统管理</h2>
      <div class="overview-grid">
        <div class="overview-card">
          <div class="card-icon">👥</div>
          <div class="card-content">
            <div class="card-title">用户管理</div>
            <div class="card-desc">管理系统用户和权限</div>
            <div class="card-stats">总用户：{{ systemStats.totalUsers }}</div>
          </div>
          <el-button type="primary" @click="goToUserManagement">
            管理用户
          </el-button>
        </div>
        
        <div class="overview-card">
          <div class="card-icon">📚</div>
          <div class="card-content">
            <div class="card-title">课程管理</div>
            <div class="card-desc">管理培训课程内容</div>
            <div class="card-stats">总课程：{{ systemStats.totalCourses }}</div>
          </div>
          <el-button type="primary" @click="goToCourseManagement">
            管理课程
          </el-button>
        </div>
        
        <div class="overview-card">
          <div class="card-icon">📝</div>
          <div class="card-content">
            <div class="card-title">考试管理</div>
            <div class="card-desc">管理考试和题库</div>
            <div class="card-stats">总题目：{{ systemStats.totalQuestions }}</div>
          </div>
          <el-button type="primary" @click="goToExamManagement">
            管理考试
          </el-button>
        </div>
        
        <div class="overview-card">
          <div class="card-icon">📊</div>
          <div class="card-content">
            <div class="card-title">数据统计</div>
            <div class="card-desc">查看系统使用情况</div>
            <div class="card-stats">活跃用户：{{ systemStats.activeUsers }}</div>
          </div>
          <el-button type="primary" @click="goToStatistics">
            查看统计
          </el-button>
        </div>
      </div>
    </div>

    <!-- 系统配置 -->
    <div class="config-section">
      <el-card class="config-card">
        <template #header>
          <div class="card-header">
            <span>🔧 系统配置</span>
          </div>
        </template>
        
        <el-tabs v-model="activeTab" type="border-card">
          <el-tab-pane label="基础设置" name="basic">
            <el-form :model="basicConfig" label-width="120px">
              <el-form-item label="系统名称">
                <el-input v-model="basicConfig.systemName" />
              </el-form-item>
              <el-form-item label="系统描述">
                <el-input v-model="basicConfig.systemDesc" type="textarea" />
              </el-form-item>
              <el-form-item label="默认语言">
                <el-select v-model="basicConfig.defaultLanguage">
                  <el-option label="中文" value="zh-CN" />
                  <el-option label="英文" value="en-US" />
                </el-select>
              </el-form-item>
              <el-form-item label="维护模式">
                <el-switch v-model="basicConfig.maintenanceMode" />
              </el-form-item>
            </el-form>
          </el-tab-pane>
          
          <el-tab-pane label="邮件配置" name="email">
            <el-form :model="emailConfig" label-width="120px">
              <el-form-item label="SMTP服务器">
                <el-input v-model="emailConfig.smtpHost" />
              </el-form-item>
              <el-form-item label="端口">
                <el-input v-model="emailConfig.smtpPort" />
              </el-form-item>
              <el-form-item label="用户名">
                <el-input v-model="emailConfig.username" />
              </el-form-item>
              <el-form-item label="密码">
                <el-input v-model="emailConfig.password" type="password" />
              </el-form-item>
              <el-form-item label="启用SSL">
                <el-switch v-model="emailConfig.sslEnabled" />
              </el-form-item>
            </el-form>
          </el-tab-pane>
          
          <el-tab-pane label="安全设置" name="security">
            <el-form :model="securityConfig" label-width="140px">
              <el-form-item label="密码最小长度">
                <el-input-number v-model="securityConfig.minPasswordLength" :min="6" :max="20" />
              </el-form-item>
              <el-form-item label="会话超时时间(分钟)">
                <el-input-number v-model="securityConfig.sessionTimeout" :min="10" :max="1440" />
              </el-form-item>
              <el-form-item label="登录失败锁定次数">
                <el-input-number v-model="securityConfig.maxLoginAttempts" :min="3" :max="10" />
              </el-form-item>
              <el-form-item label="强制定期修改密码">
                <el-switch v-model="securityConfig.forcePasswordChange" />
              </el-form-item>
            </el-form>
          </el-tab-pane>
        </el-tabs>
        
        <div class="config-actions">
          <el-button type="primary" @click="saveConfig">
            保存配置
          </el-button>
          <el-button @click="resetConfig">
            重置配置
          </el-button>
        </div>
      </el-card>
    </div>

    <!-- 系统监控 -->
    <div class="monitor-section">
      <div class="monitor-grid">
        <el-card class="monitor-card">
          <template #header>
            <div class="card-header">
              <span>💾 系统资源</span>
              <el-button type="text" @click="refreshSystemInfo">
                刷新
              </el-button>
            </div>
          </template>
          
          <div class="resource-info">
            <div class="resource-item">
              <div class="resource-label">CPU使用率</div>
              <el-progress :percentage="systemInfo.cpuUsage" :color="getUsageColor(systemInfo.cpuUsage)" />
            </div>
            <div class="resource-item">
              <div class="resource-label">内存使用率</div>
              <el-progress :percentage="systemInfo.memoryUsage" :color="getUsageColor(systemInfo.memoryUsage)" />
            </div>
            <div class="resource-item">
              <div class="resource-label">磁盘使用率</div>
              <el-progress :percentage="systemInfo.diskUsage" :color="getUsageColor(systemInfo.diskUsage)" />
            </div>
          </div>
        </el-card>
        
        <el-card class="monitor-card">
          <template #header>
            <div class="card-header">
              <span>📈 使用统计</span>
            </div>
          </template>
          
          <div class="usage-stats">
            <div class="stat-item">
              <div class="stat-value">{{ systemInfo.onlineUsers }}</div>
              <div class="stat-label">在线用户</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ systemInfo.todayLogins }}</div>
              <div class="stat-label">今日登录</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ systemInfo.activeConnections }}</div>
              <div class="stat-label">活跃连接</div>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 操作日志 -->
    <div class="logs-section">
      <el-card class="logs-card">
        <template #header>
          <div class="card-header">
            <span>📋 操作日志</span>
            <div class="header-actions">
              <el-button size="small" @click="exportLogs">
                导出日志
              </el-button>
              <el-button size="small" @click="clearLogs">
                清空日志
              </el-button>
            </div>
          </div>
        </template>
        
        <el-table :data="operationLogs" stripe>
          <el-table-column prop="time" label="时间" width="180" />
          <el-table-column prop="user" label="操作用户" width="120" />
          <el-table-column prop="action" label="操作类型" width="120">
            <template #default="{ row }">
              <el-tag :type="getActionTagType(row.action)">
                {{ row.action }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="操作描述" />
          <el-table-column prop="ip" label="IP地址" width="120" />
          <el-table-column prop="result" label="结果" width="100">
            <template #default="{ row }">
              <el-tag :type="row.result === '成功' ? 'success' : 'danger'">
                {{ row.result }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
        
        <div class="logs-pagination">
          <el-pagination
            v-model:current-page="logsCurrentPage"
            :page-size="10"
            :total="100"
            layout="prev, pager, next"
          />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

// 响应式数据
const activeTab = ref('basic')
const logsCurrentPage = ref(1)

// 系统统计
const systemStats = ref({
  totalUsers: 1248,
  totalCourses: 156,
  totalQuestions: 2580,
  activeUsers: 189
})

// 基础配置
const basicConfig = ref({
  systemName: '智能培训系统',
  systemDesc: '公司内部产品培训平台',
  defaultLanguage: 'zh-CN',
  maintenanceMode: false
})

// 邮件配置
const emailConfig = ref({
  smtpHost: 'smtp.company.com',
  smtpPort: '587',
  username: 'noreply@company.com',
  password: '',
  sslEnabled: true
})

// 安全配置
const securityConfig = ref({
  minPasswordLength: 8,
  sessionTimeout: 120,
  maxLoginAttempts: 5,
  forcePasswordChange: false
})

// 系统信息
const systemInfo = ref({
  cpuUsage: 45,
  memoryUsage: 68,
  diskUsage: 35,
  onlineUsers: 24,
  todayLogins: 156,
  activeConnections: 89
})

// 操作日志
const operationLogs = ref([
  {
    time: '2025-01-18 14:30:25',
    user: '管理员',
    action: '用户管理',
    description: '添加新用户：张三',
    ip: '192.168.1.100',
    result: '成功'
  },
  {
    time: '2025-01-18 14:25:18',
    user: '管理员',
    action: '课程管理',
    description: '修改课程：产品基础培训',
    ip: '192.168.1.100',
    result: '成功'
  },
  {
    time: '2025-01-18 14:20:32',
    user: '讲师01',
    action: '考试管理',
    description: '创建新考试：产品知识测试',
    ip: '192.168.1.105',
    result: '成功'
  },
  {
    time: '2025-01-18 14:15:44',
    user: '管理员',
    action: '系统配置',
    description: '修改邮件配置',
    ip: '192.168.1.100',
    result: '失败'
  }
])

// 方法
const goToUserManagement = () => {
  ElMessage.info('跳转到用户管理页面')
}

const goToCourseManagement = () => {
  ElMessage.info('跳转到课程管理页面')
}

const goToExamManagement = () => {
  ElMessage.info('跳转到考试管理页面')
}

const goToStatistics = () => {
  ElMessage.info('跳转到数据统计页面')
}

const saveConfig = () => {
  ElMessage.success('配置保存成功')
}

const resetConfig = () => {
  ElMessage.info('配置已重置')
}

const refreshSystemInfo = () => {
  // 模拟刷新系统信息
  systemInfo.value.cpuUsage = Math.floor(Math.random() * 100)
  systemInfo.value.memoryUsage = Math.floor(Math.random() * 100)
  systemInfo.value.diskUsage = Math.floor(Math.random() * 100)
  ElMessage.success('系统信息已刷新')
}

const getUsageColor = (usage) => {
  if (usage > 80) return '#f56c6c'
  if (usage > 60) return '#e6a23c'
  return '#67c23a'
}

const getActionTagType = (action) => {
  const typeMap = {
    '用户管理': 'primary',
    '课程管理': 'success',
    '考试管理': 'warning',
    '系统配置': 'info'
  }
  return typeMap[action] || ''
}

const exportLogs = () => {
  ElMessage.success('操作日志导出中...')
}

const clearLogs = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要清空所有操作日志吗？此操作不可恢复。',
      '清空日志',
      { type: 'warning' }
    )
    ElMessage.success('操作日志已清空')
  } catch {
    // 用户取消
  }
}

onMounted(() => {
  // 初始化数据
})
</script>

<style scoped>
.admin-container {
  max-width: 1400px;
  margin: 0 auto;
}

.overview-section {
  margin-bottom: 30px;
}

.overview-section h2 {
  margin-bottom: 20px;
  color: #333;
  font-size: 24px;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.overview-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 15px;
  padding: 25px;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  display: flex;
  gap: 20px;
  align-items: center;
  transition: all 0.3s ease;
}

.overview-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.15);
}

.card-icon {
  font-size: 48px;
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 15px;
}

.card-content {
  flex: 1;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 5px;
}

.card-desc {
  color: #666;
  font-size: 14px;
  margin-bottom: 8px;
}

.card-stats {
  color: #667eea;
  font-size: 14px;
  font-weight: 500;
}

.config-section,
.monitor-section,
.logs-section {
  margin-bottom: 30px;
}

.config-card,
.monitor-card,
.logs-card {
  border-radius: 15px;
  border: none;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  font-size: 16px;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.config-actions {
  margin-top: 20px;
  text-align: center;
}

.monitor-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 20px;
}

.resource-info {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.resource-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.resource-label {
  font-size: 14px;
  color: #666;
}

.usage-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.stat-item {
  text-align: center;
  padding: 15px;
  background: rgba(102, 126, 234, 0.05);
  border-radius: 10px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #667eea;
  margin-bottom: 5px;
}

.stat-label {
  color: #666;
  font-size: 14px;
}

.logs-pagination {
  margin-top: 15px;
  display: flex;
  justify-content: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .overview-grid {
    grid-template-columns: 1fr;
  }
  
  .overview-card {
    flex-direction: column;
    text-align: center;
  }
  
  .monitor-grid {
    grid-template-columns: 1fr;
  }
  
  .usage-stats {
    grid-template-columns: 1fr;
  }
  
  .card-header {
    flex-direction: column;
    gap: 10px;
  }
}
</style>