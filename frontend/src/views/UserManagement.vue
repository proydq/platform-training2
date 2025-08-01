<template>
  <div class="user-management-page">
    <!-- 头部区域 -->
    <div class="header-section">
      <div class="header-content">
        <div class="header-info">
          <div class="header-icon">👥</div>
          <div class="header-text">
            <h1>用户管理</h1>
            <p>管理系统用户和权限设置</p>
          </div>
        </div>

        <div class="header-actions">
          <button class="add-btn" @click="openAddUser">
            ➕ 添加用户
          </button>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stats-card">
        <div class="stats-content">
          <div class="stats-info">
            <div class="stats-label">总用户数</div>
            <div class="stats-value">{{ stats.total }}</div>
          </div>
          <div class="stats-icon">👥</div>
        </div>
      </div>

      <div class="stats-card">
        <div class="stats-content">
          <div class="stats-info">
            <div class="stats-label">在线用户</div>
            <div class="stats-value online">{{ stats.online }}</div>
          </div>
          <div class="stats-icon">🟢</div>
        </div>
      </div>

      <div class="stats-card">
        <div class="stats-content">
          <div class="stats-info">
            <div class="stats-label">新增用户</div>
            <div class="stats-value new">{{ stats.new }}</div>
          </div>
          <div class="stats-icon">📈</div>
        </div>
      </div>

      <div class="stats-card">
        <div class="stats-content">
          <div class="stats-info">
            <div class="stats-label">待审核</div>
            <div class="stats-value pending">{{ stats.pending }}</div>
          </div>
          <div class="stats-icon">⏳</div>
        </div>
      </div>
    </div>

    <!-- 主内容区域 -->
    <div class="main-content">
      <!-- 标签页导航 -->
      <div class="tab-header">
        <div class="tab-item active" @click="switchTab('list')">
          📋 用户列表
        </div>
        <div class="tab-item" @click="switchTab('roles')">
          🔐 角色管理
        </div>
        <div class="tab-item" @click="switchTab('permissions')">
          ⚙️ 权限设置
        </div>
        <div class="tab-item" @click="switchTab('audit')">
          📝 操作日志
        </div>
      </div>

      <!-- 用户列表内容 -->
      <div v-show="activeTab === 'list'" class="tab-content">
        <!-- 搜索和筛选 -->
        <div class="search-section">
          <div class="search-left">
            <input
              type="text"
              class="search-input"
              placeholder="搜索用户名、姓名或邮箱..."
              v-model="query.keyword"
              @input="handleSearch"
            />
          </div>

          <div class="search-right">
            <select class="filter-select" v-model="query.role" @change="handleSearch">
              <option value="">全部角色</option>
              <option v-for="r in roleOptions" :key="r.value" :value="r.value">{{ r.label }}</option>
            </select>

            <button class="filter-btn">
              🔍 更多筛选
            </button>
          </div>
        </div>

        <!-- 用户表格 -->
        <table class="user-table">
          <thead>
          <tr>
            <th>用户</th>
            <th>角色</th>
            <th>状态</th>
            <th>最近登录</th>
            <th style="text-align: center;">操作</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="row in filteredUsers" :key="row.id">
            <td>
              <div class="user-info">
                <div class="user-avatar">{{ (row.name || row.username).charAt(0) }}</div>
                <div class="user-details">
                  <div class="user-name">{{ row.name }}</div>
                  <div class="user-email">{{ row.email }}</div>
                </div>
              </div>
            </td>
            <td>
              <span class="tag" :class="getRoleTagClass(row.role)">{{ getRoleText(row.role) }}</span>
            </td>
            <td>
              <span class="tag" :class="getStatusTagClass(row.status)">{{ getStatusText(row.status) }}</span>
            </td>
            <td>{{ row.lastLogin }}</td>
            <td style="text-align: center;">
              <div class="action-buttons">
                <button class="action-btn" @click="openEditUser(row)">✏️ 编辑</button>
                <button class="action-btn warning" @click="resetPassword(row)">🔒 重置密码</button>
                <button class="action-btn danger" @click="deleteUser(row)">🗑️ 删除</button>
              </div>
            </td>
          </tr>
          </tbody>
        </table>

        <!-- 分页 -->
        <div class="pagination">
          <span>显示 {{ displayStart }}-{{ displayEnd }}，共 {{ total }} 条记录</span>
          <button class="page-btn" :disabled="query.page === 1" @click="changePage(query.page - 1)">
            上一页
          </button>
          <button
            v-for="p in totalPages"
            :key="p"
            class="page-btn"
            :class="{ active: p === query.page }"
            @click="changePage(p)"
          >
            {{ p }}
          </button>
          <button class="page-btn" :disabled="query.page === totalPages" @click="changePage(query.page + 1)">
            下一页
          </button>
        </div>
      </div>

      <!-- 其他标签页内容 -->
      <div v-show="activeTab === 'roles'" class="tab-content">
        <div class="empty-state">
          <div class="empty-icon">🔐</div>
          <h3>角色管理</h3>
          <p>管理系统角色和权限分配</p>
        </div>
      </div>

      <div v-show="activeTab === 'permissions'" class="tab-content">
        <div class="empty-state">
          <div class="empty-icon">⚙️</div>
          <h3>权限设置</h3>
          <p>配置用户权限和访问控制</p>
        </div>
      </div>

      <div v-show="activeTab === 'audit'" class="tab-content">
        <div class="empty-state">
          <div class="empty-icon">📝</div>
          <h3>操作日志</h3>
          <p>查看用户操作记录和系统日志</p>
        </div>
      </div>
    </div>

    <!-- 添加/编辑用户弹窗 -->
    <div class="modal-overlay" :class="{ show: showModal }" @click.self="closeModal">
      <div class="modal">
        <div class="modal-header">
          <h3 class="modal-title">{{ isEdit ? '编辑用户信息' : '添加新用户' }}</h3>
          <button class="close-btn" @click="closeModal">&times;</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent>
            <div class="form-row">
              <div class="form-col">
                <label class="form-label">姓名 <span class="required">*</span></label>
                <input v-model="form.name" type="text" class="form-input" required />
              </div>
              <div class="form-col">
                <label class="form-label">用户名 <span class="required">*</span></label>
                <input v-model="form.username" :disabled="isEdit" type="text" class="form-input" required />
                <div class="form-note" v-show="isEdit">用户名创建后不可修改</div>
              </div>
            </div>
            <div class="form-group">
              <label class="form-label">邮箱地址 <span class="required">*</span></label>
              <input v-model="form.email" type="email" class="form-input" required />
            </div>
            <div class="form-group" v-show="!isEdit">
              <label class="form-label">密码 <span class="required">*</span></label>
              <input v-model="form.password" type="password" class="form-input" required />
            </div>
            <div class="password-note" v-show="isEdit">
              <span>🔒</span>
              <div>
                <strong>密码管理</strong><br />如需重置密码，请使用"重置密码"功能
              </div>
            </div>
            <div class="form-row">
              <div class="form-col">
                <label class="form-label">角色 <span class="required">*</span></label>
                <select v-model="form.role" class="form-select" required>
                  <option value="">请选择角色</option>
                  <option v-for="r in roleOptions" :key="'form-' + r.value" :value="r.value">{{ r.label }}</option>
                </select>
              </div>
              <div class="form-col">
                <label class="form-label">状态 <span class="required">*</span></label>
                <select v-model="form.status" class="form-select" required>
                  <option value="active">正常</option>
                  <option value="inactive">停用</option>
                  <option value="locked">锁定</option>
                </select>
              </div>
            </div>
            <div class="form-row">
              <div class="form-col">
                <label class="form-label">部门</label>
                <input v-model="form.department" type="text" class="form-input" />
              </div>
              <div class="form-col">
                <label class="form-label">手机号</label>
                <input v-model="form.phone" type="tel" class="form-input" />
              </div>
            </div>
            <div class="form-group">
              <label class="form-label">备注描述</label>
              <textarea v-model="form.description" class="form-textarea"></textarea>
            </div>
          </form>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-cancel" @click="closeModal">取消</button>
          <button type="button" class="btn btn-primary" @click="saveUser">{{ isEdit ? '更新用户' : '添加用户' }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getUsersAPI,
  createUserAPI,
  updateUserAPI,
  deleteUserAPI,
  resetUserPasswordAPI,
  getRoleOptionsAPI
} from '@/api/user'

const users = ref([])
const total = ref(0)
const stats = reactive({ total: 0, online: 0, new: 0, pending: 0 })
const query = reactive({ page: 1, pageSize: 10, role: '', keyword: '' })
const roleOptions = ref([])
const activeTab = ref('list')

const showModal = ref(false)
const isEdit = ref(false)
const form = reactive({
  id: null,
  name: '',
  username: '',
  email: '',
  password: '',
  role: '',
  status: 'active',
  department: '',
  phone: '',
  description: ''
})

const filteredUsers = computed(() => users.value)
const totalPages = computed(() => Math.ceil(total.value / query.pageSize))
const displayStart = computed(() => (total.value === 0 ? 0 : (query.page - 1) * query.pageSize + 1))
const displayEnd = computed(() => Math.min(query.page * query.pageSize, total.value))

async function fetchUsers() {
  try {
    const res = await getUsersAPI(query)
    users.value = res?.data?.data || []
    total.value = res?.data?.total || 0
    stats.total = total.value
    stats.online = users.value.filter(u => u.status === 'active').length
    stats.pending = users.value.filter(u => u.status === 'pending').length
    stats.new = users.value.filter(u => u.isNew).length
  } catch (e) {
    ElMessage.error('获取用户列表失败')
  }
}

async function fetchRoles() {
  try {
    const res = await getRoleOptionsAPI()
    roleOptions.value = res.data || res || []
  } catch (e) {
    roleOptions.value = [
      { value: 'STUDENT', label: '学员' },
      { value: 'TEACHER', label: '讲师' },
      { value: 'ADMIN', label: '管理员' }
    ]
  }
}

function handleSearch() {
  query.page = 1
  fetchUsers()
}

function changePage(p) {
  if (p !== query.page) {
    query.page = p
    fetchUsers()
  }
}

function switchTab(tabName) {
  activeTab.value = tabName
}

function openAddUser() {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    name: '',
    username: '',
    email: '',
    password: '',
    role: '',
    status: 'active',
    department: '',
    phone: '',
    description: ''
  })
  showModal.value = true
}

function openEditUser(item) {
  isEdit.value = true
  Object.assign(form, { ...item, password: '' })
  showModal.value = true
}

function closeModal() {
  showModal.value = false
}

async function saveUser() {
  try {
    const payload = {
      ...form,
      role: typeof form.role === 'object' ? form.role.value : form.role
    }
    if (isEdit.value && (!form.password || form.password.trim() === '')) {
      delete payload.password
    }
    if (isEdit.value) {
      await updateUserAPI(form.id, payload)
      ElMessage.success('用户更新成功')
    } else {
      await createUserAPI(payload)
      ElMessage.success('用户添加成功')
    }
    showModal.value = false
    fetchUsers()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

async function deleteUser(item) {
  try {
    await ElMessageBox.confirm(`确定要删除用户 "${item.name}" 吗？此操作不可恢复。`, '删除用户', { type: 'warning' })
    await deleteUserAPI(item.id)
    ElMessage.success('用户删除成功')
    fetchUsers()
  } catch {
    // cancel
  }
}

async function resetPassword(item) {
  try {
    await ElMessageBox.confirm(`确定要重置用户 "${item.name}" 的密码吗？`, '重置密码', { type: 'warning' })
    await resetUserPasswordAPI(item.id)
    ElMessage.success('密码重置成功')
  } catch {
    // cancel
  }
}

function getRoleText(role) {
  const map = { STUDENT: '学员', TEACHER: '讲师', ADMIN: '管理员' }
  return map[role] || role
}

function getRoleTagClass(role) {
  const map = { STUDENT: 'primary', TEACHER: 'success', ADMIN: 'danger' }
  return map[role] || 'primary'
}

function getStatusText(status) {
  const map = { active: '正常', inactive: '停用', locked: '锁定' }
  return map[status] || status
}

function getStatusTagClass(status) {
  const map = { active: 'success', inactive: 'info', locked: 'danger' }
  return map[status] || 'info'
}

onMounted(() => {
  fetchUsers()
  fetchRoles()
})
</script>

<style scoped>
/* 整体布局 */
.user-management-page {
  min-height: 100vh;
  padding: 20px;
}

/* 头部区域 - 模仿 HTML 设计 */
.header-section {
  margin-bottom: 30px;
}

.header-content {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 15px;
  padding: 20px 30px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.header-icon {
  font-size: 32px;
}

.header-text h1 {
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-size: 24px;
  font-weight: bold;
  margin: 0;
}

.header-text p {
  color: #666;
  margin: 5px 0 0 0;
  font-size: 14px;
}

.add-btn {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  border-radius: 8px;
  color: white;
  padding: 10px 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  transition: all 0.3s;
}

.add-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stats-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.stats-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stats-label {
  color: #666;
  font-size: 14px;
}

.stats-value {
  font-size: 24px;
  font-weight: bold;
  color: #667eea;
  margin-top: 5px;
}

.stats-value.online { color: #67c23a; }
.stats-value.new { color: #409eff; }
.stats-value.pending { color: #e6a23c; }

.stats-icon {
  font-size: 32px;
}

/* 主内容区域 */
.main-content {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 15px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

/* 标签页 */
.tab-header {
  display: flex;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 20px;
}

.tab-item {
  padding: 15px 20px;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  color: #606266;
  font-size: 14px;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 8px;
}

.tab-item.active {
  color: #409eff;
  border-bottom-color: #409eff;
}

.tab-item:hover {
  color: #409eff;
}

.tab-content {
  padding: 20px;
}

/* 搜索区域 */
.search-section {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  align-items: center;
  flex-wrap: wrap;
}

.search-left {
  flex: 1;
  min-width: 300px;
}

.search-input {
  width: 100%;
  padding: 10px 15px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.3s;
}

.search-input:focus {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.search-right {
  display: flex;
  gap: 10px;
  align-items: center;
}

.filter-select {
  padding: 10px 15px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  font-size: 14px;
  outline: none;
  background: white;
  min-width: 150px;
}

.filter-btn {
  padding: 10px 15px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  background: white;
  cursor: pointer;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 5px;
  transition: all 0.3s;
}

.filter-btn:hover {
  border-color: #409eff;
  color: #409eff;
}

/* 表格样式 */
.user-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 20px;
}

.user-table th {
  background: #f5f7fa;
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #ebeef5;
  font-weight: 500;
  color: #909399;
  font-size: 14px;
}

.user-table td {
  padding: 12px;
  border-bottom: 1px solid #ebeef5;
  font-size: 14px;
}

.user-table tbody tr:hover {
  background-color: #f5f7fa;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 16px;
}

.user-name {
  font-weight: 500;
  color: #333;
}

.user-email {
  font-size: 12px;
  color: #666;
  margin-top: 2px;
}

/* 标签 */
.tag {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.tag.primary { background: #ecf5ff; color: #409eff; }
.tag.success { background: #f0f9ff; color: #67c23a; }
.tag.danger { background: #fef0f0; color: #f56c6c; }
.tag.warning { background: #fdf6ec; color: #e6a23c; }
.tag.info { background: #f4f4f5; color: #909399; }

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 5px;
}

.action-btn {
  padding: 5px 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: white;
  cursor: pointer;
  font-size: 12px;
  margin-right: 5px;
  transition: all 0.3s;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.action-btn:hover { border-color: #409eff; color: #409eff; }
.action-btn.warning:hover { border-color: #e6a23c; color: #e6a23c; }
.action-btn.danger:hover { border-color: #f56c6c; color: #f56c6c; }

/* 分页 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.page-btn {
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: white;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.page-btn:hover, .page-btn.active {
  border-color: #409eff;
  color: #409eff;
}

.page-btn.active {
  background: #409eff;
  color: white;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 20px;
}

.empty-state h3 {
  font-size: 18px;
  color: #333;
  margin-bottom: 10px;
}

.empty-state p {
  color: #666;
  font-size: 14px;
}

/* 弹窗样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  opacity: 0;
  visibility: hidden;
  transition: all 0.3s;
}

.modal-overlay.show {
  opacity: 1;
  visibility: visible;
}

.modal {
  background: white;
  border-radius: 8px;
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
  transform: translateY(-50px);
  transition: transform 0.3s;
}

.modal-overlay.show .modal {
  transform: translateY(0);
}

.modal-header {
  padding: 20px;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-title {
  font-size: 18px;
  font-weight: 600;
}

.close-btn {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #909399;
}

.modal-body {
  padding: 20px;
}

.form-group {
  margin-bottom: 20px;
}

.form-row {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
}

.form-col {
  flex: 1;
}

.form-label {
  display: block;
  margin-bottom: 5px;
  font-weight: 500;
  color: #606266;
  font-size: 14px;
}

.form-input, .form-select {
  width: 100%;
  padding: 10px 15px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.3s;
}

.form-input:focus, .form-select:focus {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.form-textarea {
  width: 100%;
  padding: 10px 15px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  font-size: 14px;
  outline: none;
  resize: vertical;
  min-height: 80px;
  font-family: inherit;
}

.required {
  color: #f56c6c;
}

.form-note {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.password-note {
  background: #ecf5ff;
  border: 1px solid #b3d8ff;
  border-radius: 6px;
  padding: 15px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.modal-footer {
  padding: 20px;
  border-top: 1px solid #ebeef5;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.btn {
  padding: 10px 20px;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid transparent;
}

.btn-cancel {
  background: white;
  color: #606266;
  border-color: #dcdfe6;
}

.btn-cancel:hover {
  color: #409eff;
  border-color: #c6e2ff;
  background-color: #ecf5ff;
}

.btn-primary {
  background: #409eff;
  color: white;
}

.btn-primary:hover {
  background: #66b1ff;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .user-management-page {
    padding: 10px;
  }

  .header-content {
    flex-direction: column;
    gap: 15px;
    text-align: center;
  }

  .search-section {
    flex-direction: column;
  }

  .search-left,
  .search-right {
    width: 100%;
  }

  .form-row {
    flex-direction: column;
    gap: 0;
  }

  .tab-header {
    flex-wrap: wrap;
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 15px;
  }
}

@media (max-width: 480px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>
