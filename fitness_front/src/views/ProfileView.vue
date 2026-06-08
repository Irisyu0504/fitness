<template>
  <main class="page-container">
    <PageHeader title="个人主页" />

    <div class="profile-card">
      <!-- 顶部：头像 + 基本信息 -->
      <div class="card-head">
        <div class="avatar-wrap" @click="triggerUpload" title="点击更换头像">
          <img v-if="avatarUrl" :src="avatarUrl" class="avatar-img" alt="头像" />
          <div v-else class="avatar-fallback">{{ initials }}</div>
          <div class="avatar-overlay">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2" stroke-linecap="round"><path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/><circle cx="12" cy="13" r="4"/></svg>
          </div>
          <input ref="fileInput" type="file" accept="image/*" hidden @change="onAvatarChange" />
        </div>

        <div class="head-info">
          <div class="nickname-row">
            <template v-if="!editingNickname">
              <h2 class="nickname-text">{{ profile.nickname || '未设置昵称' }}</h2>
              <button class="edit-icon" @click="startEditNickname" title="修改昵称">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
              </button>
            </template>
            <template v-else>
              <el-input
                v-model="nicknameInput"
                size="large"
                class="nickname-input"
                placeholder="输入昵称"
                @keyup.enter="saveNickname"
                @keyup.escape="editingNickname = false"
              />
              <button class="save-btn" @click="saveNickname">保存</button>
              <button class="cancel-btn" @click="editingNickname = false">取消</button>
            </template>
          </div>
          <p class="email-text">{{ profile.email || '未绑定邮箱' }}</p>
          <span class="username-badge">@{{ profile.username }}</span>
        </div>
      </div>

      <!-- 会员状态 -->
      <div class="divider"></div>
      <div class="vip-section">
        <div class="vip-info">
          <span class="vip-label">会员状态</span>
          <div class="vip-status-row">
            <span v-if="vipStatus.isVip" class="vip-badge active">生效中</span>
            <span v-else-if="vipStatus.expireTime" class="vip-badge expired">已过期</span>
            <span v-else class="vip-badge inactive">未开通</span>
            <span v-if="vipStatus.isVip" class="vip-remain">剩余 {{ vipStatus.remainingDays }} 天</span>
            <span v-else-if="vipStatus.expireTime" class="vip-expire-text">到期：{{ formatVipTime(vipStatus.expireTime) }}</span>
          </div>
        </div>
        <button class="vip-action-btn" @click="$router.push('/membership')">
          {{ vipStatus.isVip ? '续费' : '开通会员' }}
        </button>
      </div>

      <!-- 分隔线 -->
      <div class="divider"></div>

      <!-- 身体数据 -->
      <div class="fields-grid">
        <div class="field-item">
          <span class="field-label">性别</span>
          <div class="field-value-row">
            <template v-if="!editingGender">
              <strong class="field-value">{{ profile.gender || '未设置' }}</strong>
              <button class="edit-icon sm" @click="editingGender = true" title="修改">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
              </button>
            </template>
            <template v-else>
              <el-select v-model="genderInput" size="large" class="field-select">
                <el-option label="男" value="男" />
                <el-option label="女" value="女" />
              </el-select>
              <button class="save-btn sm" @click="saveGender">保存</button>
              <button class="cancel-btn sm" @click="editingGender = false">取消</button>
            </template>
          </div>
        </div>

        <div class="field-item">
          <span class="field-label">身高</span>
          <div class="field-value-row">
            <template v-if="!editingHeight">
              <strong class="field-value">{{ profile.height ? profile.height + ' cm' : '未设置' }}</strong>
              <button class="edit-icon sm" @click="startEditHeight" title="修改">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
              </button>
            </template>
            <template v-else>
              <el-input-number v-model="heightInput" :min="100" :max="250" size="large" class="field-number" />
              <span class="field-unit">cm</span>
              <button class="save-btn sm" @click="saveHeight">保存</button>
              <button class="cancel-btn sm" @click="editingHeight = false">取消</button>
            </template>
          </div>
        </div>
      </div>

      <!-- 分隔线 -->
      <div class="divider"></div>

      <!-- 底部操作 -->
      <div class="actions-row">
        <button class="action-btn pwd" @click="openPwdDialog">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
          修改密码
        </button>
        <button class="action-btn logout" @click="handleLogout">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
          退出登录
        </button>
      </div>
    </div>

    <!-- 修改密码弹窗 -->
    <el-dialog v-model="pwdVisible" title="修改密码" width="400px" :close-on-click-modal="false" class="pwd-dialog">
      <el-form label-position="top">
        <el-form-item label="当前密码">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="输入当前密码" />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="输入新密码" />
        </el-form-item>
        <el-form-item label="确认新密码">
          <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="再次输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <button class="dlg-btn cancel" @click="pwdVisible = false">取消</button>
        <button class="dlg-btn confirm" @click="submitPassword">确认修改</button>
      </template>
    </el-dialog>
  </main>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { request } from '@/api/request.js'

const router = useRouter()

const profile = reactive({
  username: '', nickname: '', email: '', gender: '', height: null
})

const avatarUrl = ref('')
const initials = computed(() => (profile.nickname || profile.username || 'U').slice(0, 2).toUpperCase())

const vipStatus = reactive({ isVip: false, expireTime: null, remainingDays: 0 })
function formatVipTime(t) { return t ? String(t).replace('T', ' ').substring(0, 16) : '--' }

// ── Avatar ──
const fileInput = ref(null)
function triggerUpload() { fileInput.value?.click() }

function onAvatarChange(e) {
  const file = e.target.files?.[0]
  if (!file) return
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.warning('图片不能超过 2MB')
    return
  }
  const reader = new FileReader()
  reader.onload = (ev) => {
    avatarUrl.value = ev.target.result
    localStorage.setItem('avatar', avatarUrl.value)
    ElMessage.success('头像已更新')
  }
  reader.readAsDataURL(file)
  e.target.value = ''
}

// ── Nickname ──
const editingNickname = ref(false)
const nicknameInput = ref('')

function startEditNickname() {
  nicknameInput.value = profile.nickname || ''
  editingNickname.value = true
}

async function saveNickname() {
  if (!nicknameInput.value.trim()) {
    ElMessage.warning('昵称不能为空')
    return
  }
  try {
    await request('/user/profile', {
      method: 'PUT',
      body: JSON.stringify({ nickname: nicknameInput.value.trim() })
    })
    profile.nickname = nicknameInput.value.trim()
    editingNickname.value = false
    ElMessage.success('昵称已更新')
    localStorage.setItem('user', JSON.stringify({ ...JSON.parse(localStorage.getItem('user') || '{}'), nickname: profile.nickname }))
  } catch (e) {
    ElMessage.error(e.message || '修改失败')
  }
}

// ── Gender ──
const editingGender = ref(false)
const genderInput = ref('')

async function saveGender() {
  try {
    await request('/user/profile', {
      method: 'PUT',
      body: JSON.stringify({ gender: genderInput.value })
    })
    profile.gender = genderInput.value
    editingGender.value = false
    ElMessage.success('性别已更新')
    localStorage.setItem('user', JSON.stringify({ ...JSON.parse(localStorage.getItem('user') || '{}'), gender: profile.gender }))
  } catch (e) {
    ElMessage.error(e.message || '修改失败')
  }
}

// ── Height ──
const editingHeight = ref(false)
const heightInput = ref(null)

function startEditHeight() {
  heightInput.value = profile.height || 170
  editingHeight.value = true
}

async function saveHeight() {
  try {
    await request('/user/profile', {
      method: 'PUT',
      body: JSON.stringify({ height: heightInput.value })
    })
    profile.height = heightInput.value
    editingHeight.value = false
    ElMessage.success('身高已更新')
    localStorage.setItem('user', JSON.stringify({ ...JSON.parse(localStorage.getItem('user') || '{}'), height: profile.height }))
  } catch (e) {
    ElMessage.error(e.message || '修改失败')
  }
}

// ── Password ──
const pwdVisible = ref(false)
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

function openPwdDialog() {
  pwdForm.oldPassword = ''
  pwdForm.newPassword = ''
  pwdForm.confirmPassword = ''
  pwdVisible.value = true
}

async function submitPassword() {
  if (!pwdForm.oldPassword || !pwdForm.newPassword) {
    ElMessage.warning('请填写完整')
    return
  }
  if (pwdForm.newPassword !== pwdForm.confirmPassword) {
    ElMessage.warning('两次密码不一致')
    return
  }
  try {
    await request('/user/profile', {
      method: 'PUT',
      body: JSON.stringify({ password: pwdForm.newPassword })
    })
    ElMessage.success('密码修改成功')
    pwdVisible.value = false
  } catch (e) {
    ElMessage.error(e.message || '修改失败')
  }
}

// ── Fetch profile ──
async function fetchProfile() {
  try {
    const user = await request('/user/profile')
    Object.assign(profile, {
      username: user.username || '',
      nickname: user.nickname || '',
      email: user.email || '',
      gender: user.gender || '',
      height: user.height || null
    })
    localStorage.setItem('user', JSON.stringify(user))
  } catch {
    const cached = JSON.parse(localStorage.getItem('user') || '{}')
    Object.assign(profile, {
      username: cached.username || '',
      nickname: cached.nickname || '',
      email: cached.email || '',
      gender: cached.gender || '',
      height: cached.height || null
    })
  }
  avatarUrl.value = localStorage.getItem('avatar') || ''

  try {
    const vip = await request('/user/vip-status')
    Object.assign(vipStatus, vip)
  } catch { /* ignore */ }
}

function handleLogout() {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  localStorage.removeItem('avatar')
  router.push('/')
}

onMounted(fetchProfile)
</script>

<style scoped>
.page-container {
  width: 100%;
  max-width: 1440px;
  margin: 0 auto;
  padding: 36px 48px 48px 120px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.page-container > :first-child {
  align-self: flex-start;
}

/* ── Card ── */
.profile-card {
  background: #FFFFFF;
  border: 1px solid #E8ECF1;
  border-radius: 24px;
  padding: 36px 40px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06), 0 1px 3px rgba(0, 0, 0, 0.04);
  width: 720px;
  box-sizing: border-box;
}

/* ── Header ── */
.card-head {
  display: flex;
  align-items: center;
  gap: 24px;
}

.avatar-wrap {
  position: relative;
  width: 80px;
  height: 80px;
  flex-shrink: 0;
  border-radius: 50%;
  cursor: pointer;
  overflow: hidden;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.avatar-fallback {
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  background: linear-gradient(145deg, #7EB8DA, #B8A9C9);
  color: #fff;
  font-size: 28px;
  font-weight: 780;
}

.avatar-overlay {
  position: absolute;
  inset: 0;
  display: grid;
  place-items: center;
  background: rgba(0, 0, 0, 0.35);
  opacity: 0;
  transition: opacity 0.2s ease;
}

.avatar-wrap:hover .avatar-overlay {
  opacity: 1;
}

.head-info {
  min-width: 0;
  flex: 1;
}

.nickname-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
  min-width: 280px;
}

.nickname-text {
  margin: 0;
  font-size: 22px;
  font-weight: 780;
  color: #2D3748;
  line-height: 1.3;
}

.nickname-input {
  max-width: 220px;
}

.nickname-input :deep(.el-input__wrapper) {
  border-radius: 10px;
  background: #F8F9FA;
  border: 1px solid #E8ECF1;
  box-shadow: none;
}

.nickname-input :deep(.el-input__wrapper.is-focus) {
  border-color: #7EB8DA;
}

.email-text {
  margin: 0 0 6px;
  font-size: 14px;
  color: #718096;
}

.username-badge {
  display: inline-block;
  padding: 3px 10px;
  font-size: 12px;
  font-weight: 600;
  color: #A0AEC0;
  background: #F5F7FA;
  border-radius: 999px;
}

/* ── Divider ── */
.divider {
  height: 1px;
  background: #F0F2F5;
  margin: 24px 0;
}

/* ── Fields ── */
.fields-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.field-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.field-label {
  font-size: 12px;
  font-weight: 600;
  color: #A0AEC0;
}

.field-value-row {
  display: flex;
  align-items: center;
  gap: 8px;
  min-height: 40px;
  min-width: 200px;
}

.field-value {
  font-size: 18px;
  font-weight: 700;
  color: #2D3748;
}

.field-select {
  width: 120px;
}

.field-select :deep(.el-input__wrapper) {
  border-radius: 10px;
  background: #F8F9FA;
  border: 1px solid #E8ECF1;
  box-shadow: none;
}

.field-number {
  width: 140px;
}

.field-number :deep(.el-input__wrapper) {
  border-radius: 10px;
  background: #F8F9FA;
  border: 1px solid #E8ECF1;
  box-shadow: none;
}

.field-unit {
  font-size: 13px;
  color: #A0AEC0;
  font-weight: 500;
}

/* ── VIP Section ── */
.vip-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.vip-label {
  font-size: 12px;
  font-weight: 600;
  color: #A0AEC0;
  display: block;
  margin-bottom: 6px;
}

.vip-status-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.vip-badge {
  padding: 3px 10px;
  font-size: 12px;
  font-weight: 700;
  border-radius: 8px;
}

.vip-badge.active {
  background: rgba(168, 216, 185, 0.15);
  color: #5a9e6f;
}

.vip-badge.expired {
  background: rgba(240, 168, 168, 0.15);
  color: #c07070;
}

.vip-badge.inactive {
  background: #F0F2F5;
  color: #A0AEC0;
}

.vip-remain {
  font-size: 13px;
  font-weight: 600;
  color: #5a9e6f;
}

.vip-expire-text {
  font-size: 12px;
  color: #A0AEC0;
}

.vip-action-btn {
  padding: 8px 22px;
  background: #7EB8DA;
  color: #FFFFFF;
  border: none;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
}

.vip-action-btn:hover {
  background: #6AABC8;
}

/* ── Edit icons ── */
.edit-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: #A0AEC0;
  cursor: pointer;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.edit-icon:hover {
  background: #F5F7FA;
  color: #7EB8DA;
}

.edit-icon.sm {
  width: 24px;
  height: 24px;
}

/* ── Save / Cancel buttons ── */
.save-btn {
  padding: 6px 14px;
  border: none;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  color: #fff;
  background: #7EB8DA;
  cursor: pointer;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.save-btn:hover {
  background: #6AABC8;
}

.save-btn.sm {
  padding: 4px 12px;
  font-size: 11px;
}

.cancel-btn {
  padding: 6px 14px;
  border: none;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  color: #718096;
  background: #F5F7FA;
  cursor: pointer;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.cancel-btn:hover {
  background: #E8ECF1;
}

.cancel-btn.sm {
  padding: 4px 12px;
  font-size: 11px;
}

/* ── Actions ── */
.actions-row {
  display: flex;
  justify-content: space-between;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 22px;
  border: none;
  border-radius: 999px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.action-btn.pwd {
  color: #7EB8DA;
  background: rgba(126, 184, 218, 0.1);
}

.action-btn.pwd:hover {
  background: rgba(126, 184, 218, 0.18);
}

.action-btn.logout {
  color: #F0A8A8;
  background: rgba(240, 168, 168, 0.1);
}

.action-btn.logout:hover {
  background: rgba(240, 168, 168, 0.18);
  transform: translateY(-1px);
}

/* ── Password dialog ── */
.pwd-dialog :deep(.el-dialog) {
  border-radius: 24px;
}

.pwd-dialog :deep(.el-dialog__header) {
  padding: 24px 28px 12px;
  margin: 0;
}

.pwd-dialog :deep(.el-dialog__title) {
  font-size: 17px;
  font-weight: 700;
  color: #2D3748;
}

.pwd-dialog :deep(.el-dialog__body) {
  padding: 0 28px;
}

.pwd-dialog :deep(.el-dialog__footer) {
  padding: 16px 28px 24px;
}

.pwd-dialog :deep(.el-input__wrapper) {
  border-radius: 12px;
  background: #F8F9FA;
  border: 1px solid #E8ECF1;
  box-shadow: none;
}

.pwd-dialog :deep(.el-input__wrapper:hover),
.pwd-dialog :deep(.el-input__wrapper.is-focus) {
  border-color: #7EB8DA;
}

.pwd-dialog :deep(.el-form-item) {
  margin-bottom: 18px;
}

.pwd-dialog :deep(.el-form-item__label) {
  font-size: 13px;
  font-weight: 600;
  color: #718096;
}

.dlg-btn {
  padding: 9px 24px;
  border: none;
  border-radius: 999px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.dlg-btn.cancel {
  color: #718096;
  background: #F5F7FA;
}

.dlg-btn.cancel:hover {
  background: #E8ECF1;
}

.dlg-btn.confirm {
  color: #fff;
  background: #7EB8DA;
}

.dlg-btn.confirm:hover {
  background: #6AABC8;
  transform: translateY(-1px);
}

/* ── Responsive ── */
@media (max-width: 900px) {
  .page-container {
    padding: 28px 24px 40px;
  }

  .profile-card {
    padding: 28px 24px;
    max-width: 100%;
  }

  .card-head {
    flex-direction: column;
    text-align: center;
  }

  .nickname-row {
    justify-content: center;
  }

  .fields-grid {
    grid-template-columns: 1fr;
  }

  .actions-row {
    flex-direction: column;
  }
}
</style>
