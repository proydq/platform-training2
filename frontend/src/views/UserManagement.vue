<template>
  <div class="user-management-page">
    <div class="header-card">
      <div class="header-info">
        <h2 class="header-title">用户管理</h2>
        <p class="header-desc">管理系统用户和权限设置</p>
      </div>
      <el-button type="primary" @click="openAddUser">添加用户</el-button>
    </div>

    <div class="stats-overview">
      <div class="stat-item">
        <div class="stat-number">{{ stats.total }}</div>
        <div class="stat-label">总用户数</div>
      </div>
      <div class="stat-item">
        <div class="stat-number">{{ stats.online }}</div>
        <div class="stat-label">在线用户</div>
      </div>
      <div class="stat-item">
        <div class="stat-number">{{ stats.new }}</div>
        <div class="stat-label">新增用户</div>
      </div>
      <div class="stat-item">
        <div class="stat-number">{{ stats.pending }}</div>
        <div class="stat-label">待审核</div>
      </div>
    </div>

    <el-form :inline="true" class="search-form">
      <el-form-item>
        <el-input
          v-model="query.keyword"
          placeholder="搜索用户名、姓名或邮箱"
          clearable
          @input="handleSearch"
        />
      </el-form-item>
      <el-form-item>
        <el-select v-model="query.role" placeholder="全部角色" clearable @change="handleSearch">
          <el-option label="全部角色" value="" />
          <el-option v-for="r in roleOptions" :key="r.value" :label="r.label" :value="r.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="openAddUser">添加用户</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="filteredUsers" stripe class="user-table">
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="name" label="姓名" width="120" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column label="角色" width="120">
        <template #default="{ row }">
          <el-tag :type="getRoleTagClass(row.role)">{{ getRoleText(row.role) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="getStatusTagClass(row.status)">{{ getStatusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="lastLogin" label="最近登录" width="180" />
      <el-table-column label="操作" width="220" align="center">
        <template #default="{ row }">
          <el-button size="small" link type="primary" @click="openEditUser(row)">编辑</el-button>
          <el-button size="small" link type="warning" @click="resetPassword(row)">重置密码</el-button>
          <el-button size="small" link type="danger" @click="deleteUser(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container">
      <el-pagination
        v-model:current-page="query.page"
        :page-size="query.pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="changePage"
      />
    </div>

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
                <div class="form-note" v-show="isEdit" id="usernameNote">用户名创建后不可修改</div>
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
    roleOptions.value = ['STUDENT', 'TEACHER', 'ADMIN']
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
.user-management-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}
.header-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.header-desc {
  margin: 4px 0 0;
  color: #666;
  font-size: 14px;
}

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
  padding: 20px;
  text-align: center;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #667eea;
  margin-bottom: 5px;
}

.stat-label {
  color: #666;
  font-size: 14px;
}
.search-form {
  margin-bottom: 20px;
}
.pagination-container {
  margin-top: 20px;
  text-align: center;
}
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
.form-input,
.form-select {
  width: 100%;
  padding: 10px 15px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.3s;
}
.form-input:focus,
.form-select:focus {
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

@media (max-width: 768px) {
  .stats-overview {
    grid-template-columns: repeat(2, 1fr);
    gap: 15px;
  }
}
</style>
