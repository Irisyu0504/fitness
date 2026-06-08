<template>
  <div class="admin-users">
    <h1 class="page-title">用户管理</h1>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input v-model="searchUsername" placeholder="搜索用户名" clearable @keyup.enter="fetchUsers" style="max-width: 300px" />
      <button class="btn-primary" @click="fetchUsers">搜索</button>
    </div>

    <!-- 用户表格 -->
    <div class="table-card">
      <table class="data-table">
        <thead>
          <tr>
            <th>用户名</th>
            <th>昵称</th>
            <th>角色</th>
            <th>VIP 状态</th>
            <th>到期时间</th>
            <th>注册时间</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in users" :key="user.id">
            <td class="cell-bold">{{ user.username }}</td>
            <td>{{ user.nickname || '--' }}</td>
            <td><span class="role-tag" :class="user.role">{{ user.role === 'admin' ? '管理员' : '用户' }}</span></td>
            <td>
              <span class="vip-tag" :class="isVip(user) ? 'active' : 'inactive'">
                {{ isVip(user) ? '会员' : '普通' }}
              </span>
            </td>
            <td>{{ user.vipExpireTime ? formatTime(user.vipExpireTime) : '--' }}</td>
            <td>{{ formatTime(user.createTime) }}</td>
            <td>
              <span class="status-dot" :class="user.status ? 'on' : 'off'"></span>
              {{ user.status ? '正常' : '禁用' }}
            </td>
            <td class="cell-actions">
              <div class="activate-row">
                <el-input-number v-model="activateDays[user.id]" :min="1" :max="3650" size="small" style="width: 90px" />
                <button class="btn-sm btn-blue" @click="handleActivate(user.id)">激活</button>
              </div>
              <button
                v-if="user.role !== 'admin'"
                class="btn-sm"
                :class="user.status ? 'btn-red' : 'btn-green'"
                @click="handleToggleStatus(user)"
              >
                {{ user.status ? '封禁' : '解封' }}
              </button>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-if="users.length === 0" class="empty-text">暂无用户数据</p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { request } from '@/api/request.js'

const searchUsername = ref('')
const users = ref([])
const activateDays = reactive({})

function isVip(user) {
  return user.vipExpireTime && new Date(user.vipExpireTime) > new Date()
}

function formatTime(t) {
  if (!t) return '--'
  return String(t).replace('T', ' ').substring(0, 16)
}

async function fetchUsers() {
  try {
    const params = searchUsername.value ? `?username=${encodeURIComponent(searchUsername.value)}` : ''
    const data = await request(`/user/admin/users${params}`)
    users.value = data || []
    users.value.forEach(u => {
      if (!activateDays[u.id]) activateDays[u.id] = 30
    })
  } catch (e) {
    ElMessage.error(e.message || '获取用户失败')
  }
}

async function handleActivate(userId) {
  const days = activateDays[userId]
  if (!days || days <= 0) return ElMessage.warning('请输入有效天数')
  try {
    await request(`/admin/users/${userId}/activate-vip`, {
      method: 'PUT',
      body: JSON.stringify({ days })
    })
    ElMessage.success('激活成功')
    await fetchUsers()
  } catch (e) {
    ElMessage.error(e.message || '激活失败')
  }
}

async function handleToggleStatus(user) {
  const action = user.status ? '封禁' : '解封'
  try {
    await ElMessageBox.confirm(`确定${action}用户 ${user.username}？`, '提示', { type: 'warning' })
    await request(`/user/admin/users/${user.id}/status`, {
      method: 'PUT',
      body: JSON.stringify({ status: user.status ? 0 : 1 })
    })
    ElMessage.success(`${action}成功`)
    await fetchUsers()
  } catch { /* 取消 */ }
}

onMounted(fetchUsers)
</script>

<style scoped>
.admin-users {
  max-width: 1200px;
}

.page-title {
  margin: 0 0 24px;
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
}

.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.search-bar :deep(.el-input__wrapper) {
  border-radius: 12px;
  background: #FFFFFF;
  border: 1px solid #E8ECF1;
  box-shadow: none;
}

.btn-primary {
  padding: 0 20px;
  height: 36px;
  background: #7EB8DA;
  color: #FFFFFF;
  border: none;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
}

.btn-primary:hover { background: #6AABC8; }

/* ── 表格 ── */
.table-card {
  background: #FFFFFF;
  border: 1px solid #E8ECF1;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.data-table th {
  padding: 14px 16px;
  text-align: left;
  font-size: 12px;
  font-weight: 700;
  color: var(--text-secondary);
  background: #F8F9FA;
  border-bottom: 1px solid #E8ECF1;
  white-space: nowrap;
}

.data-table td {
  padding: 12px 16px;
  border-bottom: 1px solid #F0F2F5;
  color: var(--text-primary);
  vertical-align: middle;
}

.data-table tr:last-child td { border-bottom: none; }

.cell-bold { font-weight: 700; }

.cell-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.activate-row {
  display: flex;
  align-items: center;
  gap: 6px;
}

.role-tag {
  padding: 2px 8px;
  font-size: 11px;
  font-weight: 700;
  border-radius: 6px;
}

.role-tag.admin {
  background: rgba(184, 169, 201, 0.15);
  color: #8B7BA5;
}

.role-tag.user {
  background: #F0F2F5;
  color: var(--text-tertiary);
}

.vip-tag {
  padding: 2px 8px;
  font-size: 11px;
  font-weight: 700;
  border-radius: 6px;
}

.vip-tag.active {
  background: rgba(168, 216, 185, 0.15);
  color: #5a9e6f;
}

.vip-tag.inactive {
  background: #F0F2F5;
  color: var(--text-tertiary);
}

.status-dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  margin-right: 4px;
}

.status-dot.on { background: #A8D8B9; }
.status-dot.off { background: #F0A8A8; }

.btn-sm {
  padding: 4px 12px;
  border: none;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-blue { background: rgba(126, 184, 218, 0.15); color: #5a8ea8; }
.btn-blue:hover { background: rgba(126, 184, 218, 0.25); }
.btn-red { background: rgba(240, 168, 168, 0.15); color: #c07070; }
.btn-red:hover { background: rgba(240, 168, 168, 0.25); }
.btn-green { background: rgba(168, 216, 185, 0.15); color: #5a9e6f; }
.btn-green:hover { background: rgba(168, 216, 185, 0.25); }

.empty-text {
  padding: 40px;
  text-align: center;
  color: var(--text-tertiary);
  font-size: 14px;
}
</style>
