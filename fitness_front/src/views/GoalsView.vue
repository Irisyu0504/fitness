<template>
  <main class="goals-view">
    <div class="page-header-row">
      <PageHeader title="身体目标" />
      <div class="header-actions">
        <button v-if="!hasGoal" class="action-btn primary" @click="openGoalDialog('create')">+ 创建目标</button>
        <template v-else>
          <button class="action-btn primary" @click="openGoalDialog('edit')">编辑</button>
          <button class="action-btn danger" @click="handleDeleteGoal">删除</button>
        </template>
      </div>
    </div>

    <!-- Hero 进度卡 -->
    <section class="goal-hero glass-card">
      <div class="hero-progress-section">
        <div class="hero-bar-wrapper">
          <div class="hero-bar-track">
            <div class="hero-bar-fill" :style="{ width: progressPercent + '%' }"></div>
          </div>
          <span class="hero-bar-label">进度 {{ progressPercent }}%</span>
        </div>
        <div class="hero-title">
          <span class="hero-goal-type">{{ goalData.goalType || '暂无目标' }}{{ goalData.targetWeight ? ' → ' + goalData.targetWeight + 'kg' : '' }}</span>
          <span class="hero-current-weight">当前 {{ goalData.currentWeight || '--' }} kg</span>
        </div>
      </div>
      <div class="hero-stats">
        <div class="hero-stat">
          <span class="stat-label">今日摄入</span>
          <strong class="stat-value">{{ calorieBalance.intake }}<small>kcal</small></strong>
        </div>
        <div class="hero-stat">
          <span class="stat-label">今日消耗</span>
          <strong class="stat-value">{{ calorieBalance.burned }}<small>kcal</small></strong>
        </div>
        <div class="hero-stat">
          <span class="stat-label">今日训练</span>
          <strong class="stat-value">{{ todayDuration }}<small>分钟</small></strong>
        </div>
      </div>
    </section>

    <!-- 体重旅程 + 目标时间线 -->
    <el-row :gutter="24" class="info-row">
      <el-col :xs="24" :lg="12">
        <section class="glass-card weight-journey">
          <p class="card-kicker">体重旅程</p>
          <div class="journey-endpoints">
            <div class="endpoint">
              <span class="endpoint-label">起始</span>
              <strong class="endpoint-value">{{ goalData.startWeight || '--' }} kg</strong>
            </div>
            <div class="endpoint">
              <span class="endpoint-label">当前</span>
              <strong class="endpoint-value accent">{{ goalData.currentWeight || '--' }} kg</strong>
            </div>
            <div class="endpoint">
              <span class="endpoint-label">目标</span>
              <strong class="endpoint-value">{{ goalData.targetWeight || '--' }} kg</strong>
            </div>
          </div>
          <div class="journey-bar">
            <div class="journey-bar-track">
              <div class="journey-bar-fill" :style="{ width: weightProgressPercent + '%' }"></div>
              <div class="journey-marker" :style="{ left: weightProgressPercent + '%' }"></div>
            </div>
          </div>
          <p class="journey-remaining">还差 <strong>{{ remainingWeight }} kg</strong></p>
        </section>
      </el-col>

      <el-col :xs="24" :lg="12">
        <section class="glass-card goal-timeline">
          <p class="card-kicker">目标时间线</p>
          <div class="timeline-items">
            <div class="timeline-item">
              <span class="timeline-label">起始日期</span>
              <strong class="timeline-value">{{ goalData.startDate || '--' }}</strong>
            </div>
            <div class="timeline-item">
              <span class="timeline-label">周目标</span>
              <strong class="timeline-value">{{ weeklyTargetDisplay }}</strong>
            </div>
            <div class="timeline-item">
              <span class="timeline-label">目标日期</span>
              <strong class="timeline-value">{{ goalData.targetDate || '--' }}</strong>
            </div>
          </div>
          <div class="countdown">
            <span class="countdown-label">剩余</span>
            <strong class="countdown-value">{{ daysRemainingDisplay }}</strong>
          </div>
        </section>
      </el-col>
    </el-row>

    <!-- 热量收支 + 阶段成就 -->
    <el-row :gutter="24" class="info-row">
      <el-col :xs="24" :lg="12">
        <section class="glass-card calories-section">
          <p class="card-kicker">热量收支</p>
          <div class="calorie-rows">
            <div class="calorie-row">
              <span class="calorie-label">摄入</span>
              <strong class="calorie-value intake">{{ calorieBalance.intake }} kcal</strong>
            </div>
            <div class="calorie-row">
              <span class="calorie-label">消耗</span>
              <strong class="calorie-value burned">{{ calorieBalance.burned }} kcal</strong>
            </div>
            <div class="calorie-divider"></div>
            <div class="calorie-row">
              <span class="calorie-label">净值</span>
              <strong class="calorie-value" :class="calorieBalance.net >= 0 ? 'surplus' : 'deficit'">
                {{ calorieBalance.net >= 0 ? '+' : '' }}{{ calorieBalance.net }} kcal
              </strong>
            </div>
          </div>
        </section>
      </el-col>

      <el-col :xs="24" :lg="12">
        <section class="glass-card milestones-section">
          <p class="card-kicker">阶段成就</p>
          <div class="milestone-track">
            <div class="milestone-line">
              <div class="milestone-line-fill" :style="{ width: progressPercent + '%' }"></div>
            </div>
            <div
              v-for="m in milestones"
              :key="m.percent"
              class="milestone-node"
              :class="{ achieved: m.achieved }"
              :style="{ left: m.percent + '%' }"
            >
              <span class="node-dot"></span>
              <span class="node-label">{{ m.percent }}%</span>
            </div>
          </div>
        </section>
      </el-col>
    </el-row>

    <!-- 目标编辑弹窗 -->
    <el-dialog v-model="goalDialogVisible" :title="dialogMode === 'create' ? '创建健身目标' : '编辑健身目标'" width="500px" :close-on-click-modal="false">
      <el-form label-position="top" :model="goalForm">
        <el-form-item label="目标类型">
          <el-select v-model="goalForm.goalType" placeholder="选择目标类型">
            <el-option label="减脂" value="减脂" />
            <el-option label="增肌" value="增肌" />
            <el-option label="维持体重" value="维持体重" />
            <el-option label="提高体能" value="提高体能" />
          </el-select>
        </el-form-item>
        <div class="form-row">
          <el-form-item label="当前体重 (kg)">
            <el-input-number v-model="goalForm.currentWeight" :min="20" :max="300" :precision="1" />
          </el-form-item>
          <el-form-item label="目标体重 (kg)">
            <el-input-number v-model="goalForm.targetWeight" :min="20" :max="300" :precision="1" />
          </el-form-item>
        </div>
        <div class="form-row">
          <el-form-item label="开始日期">
            <el-date-picker v-model="goalForm.startDate" type="date" value-format="YYYY-MM-DD" />
          </el-form-item>
          <el-form-item label="目标日期">
            <el-date-picker v-model="goalForm.targetDate" type="date" value-format="YYYY-MM-DD" />
          </el-form-item>
        </div>
        <el-form-item label="每周目标 (kg)">
          <el-input-number v-model="goalForm.weeklyTarget" :min="0" :max="5" :precision="2" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="goalForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <button class="dialog-btn cancel" @click="goalDialogVisible = false">取消</button>
        <button class="dialog-btn confirm" @click="handleSaveGoal">保存</button>
      </template>
    </el-dialog>
  </main>
</template>

<script setup>
import { computed, onMounted, reactive, ref, nextTick, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { request } from '@/api/request.js'
import { normalizeGoalInfo } from '@/api/normalizers.js'

const goalData = reactive({
  id: null,
  goalType: '',
  startWeight: 0,
  currentWeight: 0,
  targetWeight: 0,
  startDate: null,
  targetDate: null,
  weeklyTarget: null
})

const fixId = (id) => {
  if (id === null || id === undefined) return null
  return String(id)
}

const hasGoal = ref(false)
const serverProgress = ref(null)
const calorieBalance = reactive({ intake: 0, burned: 0, net: 0 })
const todayDuration = ref(0)
const isSyncingGoalSchedule = ref(false)
const userHeight = ref(null)

// 目标弹窗相关
const goalDialogVisible = ref(false)
const dialogMode = ref('create')
const goalForm = reactive({
  goalType: '减脂',
  currentWeight: null,
  targetWeight: null,
  startDate: '',
  targetDate: '',
  weeklyTarget: 0.5,
  remark: ''
})

const progressPercent = computed(() => {
  if (!hasGoal.value) return 0
  if (serverProgress.value !== null) return serverProgress.value
  const total = Math.abs(goalData.startWeight - goalData.targetWeight)
  const completed = Math.abs(goalData.startWeight - goalData.currentWeight)
  if (!total) return 100
  return Math.min(100, Math.max(0, Math.round((completed / total) * 100)))
})

const remainingWeight = computed(() => {
  return Math.abs(goalData.currentWeight - goalData.targetWeight).toFixed(1)
})

const weightProgressPercent = computed(() => {
  const total = Math.abs(goalData.startWeight - goalData.targetWeight)
  if (!total) return 0
  const done = Math.abs(goalData.startWeight - goalData.currentWeight)
  return Math.min(100, Math.max(0, Math.round((done / total) * 100)))
})

const daysRemainingDisplay = computed(() => {
  if (!goalData.targetDate) return '--'
  const target = new Date(goalData.targetDate)
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  target.setHours(0, 0, 0, 0)
  const diff = Math.ceil((target - today) / 86400000)
  return diff > 0 ? diff + ' 天' : '已到期'
})

const weeklyTargetDisplay = computed(() => {
  if (!goalData.weeklyTarget) return '--'
  const wt = Number(goalData.weeklyTarget)
  if (!wt) return '--'
  if (goalData.goalType === '提高体能') return wt + ' 次/周'
  return wt + ' kg/周'
})

const milestones = computed(() => [
  { percent: 25, achieved: progressPercent.value >= 25 },
  { percent: 50, achieved: progressPercent.value >= 50 },
  { percent: 75, achieved: progressPercent.value >= 75 },
  { percent: 100, achieved: progressPercent.value >= 100 }
])

function resetGoalState() {
  hasGoal.value = false
  goalData.id = null
  goalData.goalType = ''
  goalData.startWeight = 0
  goalData.currentWeight = 0
  goalData.targetWeight = 0
  goalData.startDate = null
  goalData.targetDate = null
  goalData.weeklyTarget = null
  serverProgress.value = null
}

function toNumber(value) {
  const n = Number(value)
  return Number.isFinite(n) ? n : 0
}

function formatDate(date) {
  const pad = v => String(v).padStart(2, '0')
  return [date.getFullYear(), pad(date.getMonth() + 1), pad(date.getDate())].join('-')
}

function parseDate(value) {
  if (!value) return null
  const [y, m, d] = String(value).split('-').map(Number)
  if (!y || !m || !d) return null
  return new Date(y, m - 1, d)
}

function calculateWeightDelta() {
  return Math.abs(toNumber(goalForm.currentWeight) - toNumber(goalForm.targetWeight))
}

function calculateGoalWeeks(start, target) {
  const s = parseDate(start), t = parseDate(target)
  if (!s || !t || t <= s) return 0
  return Math.max(1, Math.ceil((t - s) / (7 * 86400000)))
}

function syncWeeklyTargetFromTargetDate() {
  if (isSyncingGoalSchedule.value) return
  const delta = calculateWeightDelta()
  const weeks = calculateGoalWeeks(goalForm.startDate, goalForm.targetDate)
  if (!delta || !weeks) return
  isSyncingGoalSchedule.value = true
  goalForm.weeklyTarget = Number((delta / weeks).toFixed(2))
  nextTick(() => { isSyncingGoalSchedule.value = false })
}

function syncTargetDateFromWeeklyTarget() {
  if (isSyncingGoalSchedule.value) return
  const delta = calculateWeightDelta()
  const wt = toNumber(goalForm.weeklyTarget)
  if (!delta || !wt) return
  const start = parseDate(goalForm.startDate) || new Date()
  const days = Math.max(7, Math.ceil((delta / wt) * 7))
  const target = new Date(start)
  target.setDate(start.getDate() + days)
  isSyncingGoalSchedule.value = true
  goalForm.startDate = goalForm.startDate || formatDate(start)
  goalForm.targetDate = formatDate(target)
  nextTick(() => { isSyncingGoalSchedule.value = false })
}

const fetchGoalData = async () => {
  // 获取用户身高
  try {
    const profile = await request('/user/profile')
    if (profile && profile.height) {
      userHeight.value = Number(profile.height)
    }
  } catch { /* ignore */ }

  try {
    const goal = await request('/fitnessGoals/current')
    if (goal && goal.id) {
      hasGoal.value = true
      goalData.id = fixId(goal.id)
      const ng = normalizeGoalInfo(goal)
      goalData.goalType = ng.goalType
      goalData.startWeight = ng.startWeight
      goalData.currentWeight = ng.currentWeight
      goalData.targetWeight = Number(ng.targetWeight || 0)
      goalData.startDate = ng.startDate
      goalData.targetDate = ng.targetDate
      goalData.weeklyTarget = ng.weeklyTarget

      const progressData = await request(`/fitnessGoals/${goal.id}/progress`)
      const np = normalizeGoalInfo(progressData)
      if (np.progress !== undefined) {
        serverProgress.value = np.progress
        goalData.currentWeight = np.currentWeight
      }
    } else {
      resetGoalState()
    }
  } catch {
    resetGoalState()
  }

  try {
    const stat = await request('/stat/dashboard')
    if (stat) {
      calorieBalance.intake = Number(stat.todayDietCalories || 0)
      calorieBalance.burned = Number(stat.todayWorkoutCalories || 0)
      calorieBalance.net = Number(stat.calorieBalance || 0)
      todayDuration.value = Number(stat.todayWorkoutDuration || 0)
    }
  } catch { /* no stat data */ }
}

const openGoalDialog = async (mode) => {
  dialogMode.value = mode
  if (mode === 'edit' && goalData.id) {
    try {
      const goal = await request('/fitnessGoals/current')
      if (goal) {
        goalForm.goalType = goal.goalType || '减脂'
        goalForm.currentWeight = Number(goal.currentWeight || 0)
        goalForm.targetWeight = Number(goal.targetWeight || 0)
        goalForm.startDate = goal.startDate || ''
        goalForm.targetDate = goal.targetDate || ''
        goalForm.weeklyTarget = Number(goal.weeklyTarget || 0.5)
        goalForm.remark = goal.remark || ''
        if (goalForm.targetDate) syncWeeklyTargetFromTargetDate()
        else syncTargetDateFromWeeklyTarget()
      }
    } catch (e) {
      ElMessage.error('获取目标详情失败')
      return
    }
  } else {
    goalForm.goalType = '减脂'
    goalForm.currentWeight = goalData.currentWeight || null
    goalForm.targetWeight = null
    goalForm.startDate = ''
    goalForm.targetDate = ''
    goalForm.weeklyTarget = 0.5
    goalForm.remark = ''
    syncTargetDateFromWeeklyTarget()
  }
  goalDialogVisible.value = true
}

const handleSaveGoal = async () => {
  if (!goalForm.currentWeight || !goalForm.targetWeight) {
    ElMessage.warning('请填写当前体重和目标体重')
    return
  }
  try {
    const payload = { ...goalForm }
    if (dialogMode.value === 'create') {
      payload.status = '进行中'
      await request('/fitnessGoals', { method: 'POST', body: JSON.stringify(payload) })
      ElMessage.success('目标创建成功')
    } else {
      await request(`/fitnessGoals/${goalData.id}`, { method: 'PUT', body: JSON.stringify(payload) })
      ElMessage.success('目标更新成功')
    }
    goalDialogVisible.value = false
    await fetchGoalData()
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  }
}

const handleDeleteGoal = async () => {
  if (!goalData.id) return
  try {
    await ElMessageBox.confirm('确定删除当前健身目标？', '提示', { type: 'warning' })
    await request(`/fitnessGoals/${goalData.id}`, { method: 'DELETE' })
    ElMessage.success('目标已删除')
    resetGoalState()
    await fetchGoalData()
  } catch { /* 取消 */ }
}

onMounted(fetchGoalData)

watch([() => goalForm.currentWeight, () => goalForm.targetWeight, () => goalForm.startDate, () => goalForm.targetDate], () => {
  if (goalForm.targetDate) syncWeeklyTargetFromTargetDate()
  else syncTargetDateFromWeeklyTarget()
})
watch(() => goalForm.weeklyTarget, syncTargetDateFromWeeklyTarget)
</script>

<style scoped>
.goals-view {
  --goal-gap: 24px;

  position: relative;
  z-index: 1;
  display: flex;
  width: min(100%, 1560px);
  box-sizing: border-box;
  flex-direction: column;
  margin: 0 auto;
  padding: 36px 56px 48px 128px;
  color: var(--text-primary);
  background: transparent;
}

/* ── 页头 ── */
.page-header-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 4px;
}

.header-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
  padding-top: 8px;
}

/* ── 通用 ── */
.glass-card {
  position: relative;
  box-sizing: border-box;
  padding: 24px;
  overflow: hidden;
  color: var(--text-primary);
  background: #FFFFFF;
  border: 1px solid #E8ECF1;
  border-radius: 24px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06), 0 1px 3px rgba(0, 0, 0, 0.04);
}

.card-kicker {
  margin: 0 0 16px;
  color: #7EB8DA;
  font-size: 12px;
  font-weight: 760;
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

.info-row {
  margin-top: var(--goal-gap);
}

.info-row .glass-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

/* ── 操作按钮 ── */
.action-btn {
  padding: 8px 18px;
  border: 1px solid #E8ECF1;
  border-radius: 12px;
  background: #FFFFFF;
  color: var(--text-primary);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.action-btn:hover {
  background: #F8F6F3;
  border-color: #D0D5DD;
}

.action-btn.primary {
  background: #7EB8DA;
  color: #FFFFFF;
  border-color: #7EB8DA;
}

.action-btn.primary:hover {
  background: #6AABC8;
}

.action-btn.danger {
  color: #E53E3E;
  border-color: #FED7D7;
}

.action-btn.danger:hover {
  background: #FFF5F5;
  border-color: #FEB2B2;
}

/* ── Hero 卡片 ── */
.goal-hero {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 28px;
}

.hero-progress-section {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.hero-bar-wrapper {
  display: flex;
  align-items: center;
  gap: 14px;
}

.hero-bar-track {
  flex: 1;
  height: 10px;
  background: #F0F2F5;
  border-radius: 999px;
  overflow: hidden;
}

.hero-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #B8DDEF, #7EB8DA);
  border-radius: 999px;
  transition: width 0.6s ease;
}

.hero-bar-label {
  flex: 0 0 auto;
  color: #7EB8DA;
  font-size: 14px;
  font-weight: 800;
}

.hero-title {
  display: flex;
  align-items: baseline;
  gap: 16px;
  flex-wrap: wrap;
}

.hero-goal-type {
  font-size: clamp(18px, 1.6vw, 22px);
  font-weight: 780;
  line-height: 1.2;
}

.hero-current-weight {
  color: var(--text-secondary);
  font-size: 14px;
  font-weight: 600;
}

.hero-stats {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.hero-stat {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 10px 18px;
  background: #F8F9FA;
  border-radius: 14px;
  min-width: 100px;
}

.stat-label {
  color: var(--text-tertiary);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.04em;
}

.stat-value {
  color: var(--text-primary);
  font-size: 20px;
  font-weight: 820;
  line-height: 1;
}

.stat-value small {
  margin-left: 3px;
  font-size: 12px;
  font-weight: 600;
  color: var(--text-secondary);
}

/* ── 体重旅程 ── */
.weight-journey {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.journey-endpoints {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}

.endpoint {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.endpoint-label {
  color: var(--text-tertiary);
  font-size: 11px;
  font-weight: 700;
}

.endpoint-value {
  font-size: 18px;
  font-weight: 800;
  line-height: 1;
}

.endpoint-value.accent {
  color: #7EB8DA;
}

.journey-bar {
  padding: 4px 0;
}

.journey-bar-track {
  position: relative;
  height: 8px;
  background: #F0F2F5;
  border-radius: 999px;
  overflow: visible;
}

.journey-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #B8DDEF, #7EB8DA);
  border-radius: 999px;
  transition: width 0.6s ease;
}

.journey-marker {
  position: absolute;
  top: 50%;
  width: 14px;
  height: 14px;
  background: #7EB8DA;
  border: 3px solid #FFFFFF;
  border-radius: 50%;
  box-shadow: 0 2px 8px rgba(126, 184, 218, 0.4);
  transform: translate(-50%, -50%);
  transition: left 0.6s ease;
}

.journey-remaining {
  margin: 0;
  color: var(--text-secondary);
  font-size: 13px;
  text-align: center;
}

.journey-remaining strong {
  color: #7EB8DA;
  font-weight: 800;
}

/* ── 目标时间线 ── */
.goal-timeline {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.timeline-items {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.timeline-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex: 1;
  padding: 12px;
  background: #F8F9FA;
  border-radius: 14px;
  text-align: center;
}

.timeline-label {
  color: var(--text-tertiary);
  font-size: 11px;
  font-weight: 700;
}

.timeline-value {
  color: var(--text-primary);
  font-size: 14px;
  font-weight: 800;
}

.countdown {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 8px;
  padding: 14px;
  background: linear-gradient(135deg, rgba(126, 184, 218, 0.08), rgba(126, 184, 218, 0.03));
  border: 1px solid rgba(126, 184, 218, 0.15);
  border-radius: 14px;
}

.countdown-label {
  color: var(--text-secondary);
  font-size: 13px;
  font-weight: 600;
}

.countdown-value {
  color: #7EB8DA;
  font-size: 22px;
  font-weight: 840;
}

/* ── 热量收支 ── */
.calories-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.calorie-rows {
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex: 1;
  justify-content: center;
}

.calorie-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.calorie-label {
  color: var(--text-secondary);
  font-size: 14px;
  font-weight: 600;
}

.calorie-value {
  font-size: 16px;
  font-weight: 800;
}

.calorie-value.intake {
  color: #F0A8A8;
}

.calorie-value.burned {
  color: #7EB8DA;
}

.calorie-value.surplus {
  color: #E8A18B;
}

.calorie-value.deficit {
  color: #A8D8B9;
}

.calorie-divider {
  height: 1px;
  background: #E8ECF1;
}

/* ── 阶段成就 ── */
.milestones-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.milestone-track {
  position: relative;
  flex: 1;
  display: flex;
  align-items: center;
  padding: 0 12px;
}

.milestone-line {
  position: absolute;
  left: 12px;
  right: 12px;
  height: 4px;
  background: #F0F2F5;
  border-radius: 999px;
}

.milestone-line-fill {
  height: 100%;
  background: linear-gradient(90deg, #B8DDEF, #7EB8DA);
  border-radius: 999px;
  transition: width 0.6s ease;
}

.milestone-node {
  position: absolute;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  transform: translateX(-50%);
}

.node-dot {
  width: 16px;
  height: 16px;
  background: #F0F2F5;
  border: 3px solid #E8ECF1;
  border-radius: 50%;
  transition: all 0.3s ease;
}

.milestone-node.achieved .node-dot {
  background: #7EB8DA;
  border-color: #7EB8DA;
  box-shadow: 0 2px 8px rgba(126, 184, 218, 0.35);
}

.node-label {
  color: var(--text-tertiary);
  font-size: 11px;
  font-weight: 700;
  white-space: nowrap;
}

.milestone-node.achieved .node-label {
  color: #7EB8DA;
}

/* ── 弹窗 ── */
.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.dialog-btn {
  padding: 10px 24px;
  border: none;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.dialog-btn.cancel {
  color: var(--text-secondary);
  background: #F8F6F3;
}

.dialog-btn.confirm {
  color: #FFFFFF;
  background: #7EB8DA;
}

.dialog-btn.confirm:hover {
  background: #6AABC8;
}

/* ── 响应式 ── */
@media (max-width: 992px) {
  .goals-view {
    padding: 32px 24px 48px;
  }

  .info-row {
    row-gap: 16px;
  }

  .info-row .el-col {
    margin-bottom: 0;
  }
}

@media (max-width: 640px) {
  .goals-view {
    padding: 24px 16px 40px;
  }

  .glass-card {
    padding: 18px;
  }

  .hero-stats {
    flex-direction: column;
  }

  .hero-stat {
    flex-direction: row;
    justify-content: space-between;
    align-items: center;
  }

  .timeline-items {
    flex-direction: column;
  }

  .form-row {
    grid-template-columns: 1fr;
  }
}
</style>
