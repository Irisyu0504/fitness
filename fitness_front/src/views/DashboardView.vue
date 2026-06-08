<template>
  <main class="dashboard-container">
    <PageHeader title="健身首页" />

    <el-row :gutter="24" class="bento-grid">
      <el-col :xs="24" :md="24" :lg="15" :xl="15">
        <section class="left-bento">
          <div class="top-metrics-group">
            <article class="glass-card metric-card calories-card">
              <div class="metric-orb cyan"></div>
              <span class="metric-label">今日消耗</span>
              <div class="metric-main">
                <strong class="metric-value">{{ dashboard.todayWorkoutCalories || 0 }}</strong>
                <span class="metric-unit">kcal</span>
              </div>
              <div class="metric-footer">
                <span class="status-dot cyan"></span>
                <span>训练 {{ dashboard.todayWorkoutDuration || 0 }} 分钟</span>
              </div>
            </article>

            <article class="glass-card metric-card intake-card">
              <div class="metric-orb rose"></div>
              <span class="metric-label">今日摄入</span>
              <div class="metric-main">
                <strong class="metric-value">{{ dashboard.todayDietCalories || 0 }}</strong>
                <span class="metric-unit">kcal</span>
              </div>
              <div class="metric-footer">
                <span class="status-dot rose"></span>
                <span>{{ dashboard.calorieBalance >= 0 ? '盈余' : '缺口' }} {{ Math.abs(dashboard.calorieBalance || 0) }} kcal</span>
              </div>
            </article>
          </div>

          <article class="glass-card plan-card">
            <div class="plan-top">
              <div class="plan-title-row">
                <span class="section-eyebrow">当前目标</span>
                <span v-if="goalInfo.goalType" class="plan-goal-type">{{ goalInfo.goalType }}{{ goalInfo.targetWeight ? ' → ' + goalInfo.targetWeight + 'kg' : '' }}</span>
                <span v-else class="plan-goal-type dim">暂无目标</span>
              </div>

              <div v-if="goalInfo.progress !== undefined" class="plan-progress-bar">
                <div class="progress-track">
                  <div class="progress-fill" :style="{ width: goalInfo.progress + '%' }"></div>
                </div>
                <span class="progress-label">{{ goalInfo.progress }}%</span>
              </div>
              <p v-else class="plan-hint">设置一个健身目标开始追踪进度</p>
            </div>

            <div class="plan-chips">
              <div class="plan-chip">
                <span class="chip-label">周目标</span>
                <strong class="chip-value">{{ weeklyTargetText || '--' }}</strong>
              </div>
              <div class="plan-chip">
                <span class="chip-label">剩余天数</span>
                <strong class="chip-value">{{ daysRemaining !== null ? daysRemaining + ' 天' : '--' }}</strong>
              </div>
              <div class="plan-chip">
                <span class="chip-label">今日摄入</span>
                <strong class="chip-value">{{ dashboard.todayDietCalories || 0 }} kcal</strong>
              </div>
              <button class="plan-action" type="button" @click="$router.push('/goals')">查看</button>
            </div>
          </article>

          <article class="glass-card sweat-card">
            <div class="card-header">
              <div>
                <h3>训练出勤率</h3>
                <p>{{ completionRuleText }}</p>
              </div>
            </div>

            <SweatGrid :done-dates="doneDates" />
          </article>
        </section>
      </el-col>

      <el-col :xs="24" :md="24" :lg="9" :xl="9">
        <aside class="right-bento">
          <article class="glass-card body-card">
            <div class="card-header compact">
              <div>
                <h3>肌肉疲劳分析</h3>
              </div>
            </div>

            <BodyFocus :records="allRecords" />
          </article>

          <article class="glass-card carousel-card">
            <div class="card-header compact">
              <div>
                <h3>深度健康洞察</h3>
                <p>训练负荷、心率与恢复信号</p>
              </div>
            </div>

            <DataCarousel
              :today-calories="dashboard.todayWorkoutCalories || 0"
              :today-duration="dashboard.todayWorkoutDuration || 0"
              :weekly-count="weeklyCount"
              :weekly-calories="weeklyCalories"
              :calorie-balance="dashboard.calorieBalance || 0"
            />
          </article>
        </aside>
      </el-col>
    </el-row>
  </main>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import BodyFocus from '@/components/BodyFocus.vue'
import DataCarousel from '@/components/DataCarousel.vue'
import SweatGrid from '@/components/SweatGrid.vue'
import PageHeader from '@/components/PageHeader.vue'
import { request } from '@/api/request.js'
import { normalizeGoalInfo, normalizeWorkoutDetails } from '@/api/normalizers.js'

const dashboard = reactive({
  todayWorkoutCalories: 0,
  todayWorkoutDuration: 0,
  todayDietCalories: 0,
  calorieBalance: 0
})

const goalInfo = reactive({
  goalType: '',
  targetWeight: null,
  progress: undefined,
  weeklyTarget: null,
  startDate: null,
  targetDate: null
})

const allRecords = ref([])

// 根据目标类型判断每天是否"已完成"
// 减脂 → 当日总消耗 >= 300 kcal
// 增肌/维持体重 → 当日有训练记录即可
// 提高体能 → 当日训练时长 >= 30 分钟
const CALORIE_THRESHOLD = 300
const DURATION_THRESHOLD = 30

const doneDates = computed(() => {
  const type = goalInfo.goalType
  const set = new Set()

  if (type === '减脂') {
    // 按日期聚合卡路里
    const calMap = {}
    allRecords.value.forEach(r => {
      if (!r.recordDate) return
      calMap[r.recordDate] = (calMap[r.recordDate] || 0) + Number(r.caloriesBurned || 0)
    })
    for (const [date, cal] of Object.entries(calMap)) {
      if (cal >= CALORIE_THRESHOLD) set.add(date)
    }
  } else if (type === '提高体能') {
    // 按日期聚合训练时长
    const durMap = {}
    allRecords.value.forEach(r => {
      if (!r.recordDate) return
      durMap[r.recordDate] = (durMap[r.recordDate] || 0) + Number(r.duration || 0)
    })
    for (const [date, dur] of Object.entries(durMap)) {
      if (dur >= DURATION_THRESHOLD) set.add(date)
    }
  } else {
    // 增肌 / 维持体重 / 暂无目标 → 有训练记录即算完成
    allRecords.value.forEach(r => {
      if (r.recordDate) set.add(r.recordDate)
    })
  }

  return set
})

const completionRuleText = computed(() => {
  const map = {
    '减脂': `每日消耗 ≥ ${CALORIE_THRESHOLD} kcal 即打卡`,
    '增肌': '每日完成训练即打卡',
    '维持体重': '每日完成训练即打卡',
    '提高体能': `每日训练 ≥ ${DURATION_THRESHOLD} 分钟即打卡`
  }
  return map[goalInfo.goalType] || '每日完成训练即打卡'
})

const daysRemaining = computed(() => {
  if (!goalInfo.targetDate) return null
  const target = new Date(goalInfo.targetDate)
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  target.setHours(0, 0, 0, 0)
  const diff = Math.ceil((target - today) / 86400000)
  return diff > 0 ? diff : 0
})

const weeklyTargetText = computed(() => {
  if (!goalInfo.weeklyTarget) return null
  const wt = Number(goalInfo.weeklyTarget)
  if (!wt) return null
  if (goalInfo.goalType === '提高体能') return `${wt} 次/周`
  return `${wt} kg/周`
})

const weeklyCount = computed(() => {
  const now = new Date()
  const dow = now.getDay() || 7
  const ws = new Date(now)
  ws.setDate(now.getDate() - dow + 1)
  const wsStr = ws.toISOString().split('T')[0]
  return allRecords.value.filter(r => r.recordDate >= wsStr).length
})

const weeklyCalories = computed(() => {
  const now = new Date()
  const dow = now.getDay() || 7
  const ws = new Date(now)
  ws.setDate(now.getDate() - dow + 1)
  const wsStr = ws.toISOString().split('T')[0]
  return allRecords.value
    .filter(r => r.recordDate >= wsStr)
    .reduce((sum, r) => sum + Number(r.caloriesBurned || 0), 0)
})

async function fetchDashboard() {
  try {
    const data = await request('/stat/dashboard')
    Object.assign(dashboard, {
      todayWorkoutCalories: data.todayWorkoutCalories || 0,
      todayWorkoutDuration: data.todayWorkoutDuration || 0,
      todayDietCalories: data.todayDietCalories || 0,
      calorieBalance: data.calorieBalance || 0
    })
    if (data.goalInfo) {
      const normalizedGoal = normalizeGoalInfo(data.goalInfo)
      Object.assign(goalInfo, {
        goalType: normalizedGoal.hasGoal ? normalizedGoal.goalType : '',
        targetWeight: normalizedGoal.hasGoal ? normalizedGoal.targetWeight : null,
        progress: normalizedGoal.hasGoal ? normalizedGoal.progress : undefined,
        weeklyTarget: normalizedGoal.hasGoal ? normalizedGoal.weeklyTarget : null,
        startDate: normalizedGoal.hasGoal ? normalizedGoal.startDate : null,
        targetDate: normalizedGoal.hasGoal ? normalizedGoal.targetDate : null
      })
    }
  } catch { /* use defaults */ }
}

async function fetchRecords() {
  try {
    const res = await request('/workout-records/detail')
    allRecords.value = normalizeWorkoutDetails(res)
  } catch { /* ignore */ }
}

onMounted(() => {
  fetchDashboard()
  fetchRecords()
})
</script>

<style scoped>
.dashboard-container {
  --dashboard-gap: 24px;

  position: relative;
  width: min(100%, 1560px);
  min-height: auto;
  box-sizing: border-box;
  margin: 0 auto;
  padding: 36px 56px 36px 128px;
  overflow: visible;
  color: var(--text-primary);
  background: transparent;
}

.bento-grid {
  position: relative;
  z-index: 1;
  align-items: stretch;
}

.left-bento,
.right-bento {
  display: flex;
  height: 100%;
  flex-direction: column;
  gap: var(--dashboard-gap);
}

.top-metrics-group {
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
  justify-content: flex-start;
  align-items: flex-start;
}

.glass-card {
  position: relative;
  box-sizing: border-box;
  padding: 18px;
  overflow: hidden;
  color: var(--text-primary);
  background: #FFFFFF;
  border: 1px solid #E8ECF1;
  border-radius: 24px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06), 0 1px 3px rgba(0, 0, 0, 0.04);
}

.metric-card {
  display: flex;
  width: 200px;
  max-width: 200px;
  height: 130px;
  flex: 0 0 200px;
  flex-direction: column;
  justify-content: flex-end;
}

.metric-orb {
  position: absolute;
  top: 18px;
  right: 18px;
  width: 38px;
  height: 38px;
  border-radius: 999px;
  filter: blur(1px);
  opacity: 0.88;
}

.metric-orb.cyan {
  background: radial-gradient(circle, rgba(126, 184, 218, 0.6), rgba(126, 184, 218, 0.08) 68%);
  box-shadow: 0 0 24px rgba(126, 184, 218, 0.2);
}

.metric-orb.rose {
  background: radial-gradient(circle, rgba(240, 168, 168, 0.6), rgba(240, 168, 168, 0.08) 68%);
  box-shadow: 0 0 24px rgba(240, 168, 168, 0.2);
}

.metric-label,
.section-eyebrow {
  color: var(--text-secondary);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.metric-main {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  margin-top: 10px;
}

.metric-value {
  color: var(--text-primary);
  font-size: 32px;
  font-weight: 820;
  line-height: 0.9;
}

.metric-unit {
  margin-bottom: 4px;
  color: var(--text-secondary);
  font-size: 14px;
  font-weight: 700;
}

.metric-footer {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin-top: 16px;
  color: var(--text-secondary);
  font-size: 12px;
  font-weight: 650;
}

.status-dot {
  width: 7px;
  height: 7px;
  border-radius: 999px;
}

.status-dot.cyan {
  background: #7EB8DA;
  box-shadow: 0 0 10px rgba(126, 184, 218, 0.4);
}

.status-dot.rose {
  background: #F0A8A8;
  box-shadow: 0 0 10px rgba(240, 168, 168, 0.4);
}

.plan-card {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 18px 22px;
}

.plan-top {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.plan-title-row {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.plan-goal-type {
  color: var(--text-primary);
  font-size: clamp(15px, 1.3vw, 18px);
  font-weight: 760;
  line-height: 1.15;
}

.plan-goal-type.dim {
  color: var(--text-tertiary);
  font-weight: 600;
}

.plan-progress-bar {
  display: flex;
  align-items: center;
  gap: 10px;
}

.progress-track {
  flex: 1;
  height: 6px;
  background: #F0F2F5;
  border-radius: 999px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #B8DDEF, #7EB8DA);
  border-radius: 999px;
  transition: width 0.6s ease;
}

.progress-label {
  flex: 0 0 auto;
  color: #7EB8DA;
  font-size: 13px;
  font-weight: 800;
}

.plan-hint {
  margin: 0;
  color: var(--text-tertiary);
  font-size: 13px;
  line-height: 1.5;
}

.plan-chips {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.plan-chip {
  display: flex;
  flex-direction: column;
  gap: 3px;
  padding: 8px 14px;
  background: #F8F9FA;
  border-radius: 12px;
}

.chip-label {
  color: var(--text-tertiary);
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.04em;
}

.chip-value {
  color: var(--text-primary);
  font-size: 13px;
  font-weight: 800;
  line-height: 1;
}

.plan-action {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-left: auto;
  padding: 8px 18px;
  color: #FFFFFF;
  font: inherit;
  font-size: 12px;
  font-weight: 800;
  background: #7EB8DA;
  border: 0;
  border-radius: 999px;
  box-shadow: 0 2px 8px rgba(126, 184, 218, 0.3);
  cursor: pointer;
  transition: transform 0.22s ease, box-shadow 0.22s ease;
}

.plan-action:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 14px rgba(126, 184, 218, 0.4);
}

.plan-action:focus-visible {
  outline: 2px solid rgba(126, 184, 218, 0.6);
  outline-offset: 3px;
}

.sweat-card {
  flex: 1 1 220px;
  min-height: 220px;
}

.card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.card-header.compact {
  margin-bottom: 14px;
}

.card-header h3 {
  margin: 0;
  color: var(--text-primary);
  font-size: 16px;
  font-weight: 740;
  line-height: 1.15;
}

.card-header p {
  margin: 6px 0 0;
  color: var(--text-secondary);
  font-size: 12px;
  line-height: 1.35;
}

.card-pill {
  flex: 0 0 auto;
  padding: 6px 11px;
  color: #7EB8DA;
  font-size: 12px;
  font-weight: 700;
  line-height: 1;
  background: rgba(126, 184, 218, 0.1);
  border: 1px solid transparent;
  border-radius: 999px;
}

/* ── 右侧卡片 ── */
.body-card {
  flex: 1 1 0;
  min-height: 380px;
}

.carousel-card {
  flex: 1 1 0;
  min-height: 300px;
  overflow: visible;
}

.body-card :deep(.body-focus-container) {
  height: calc(100% - 48px);
}

.carousel-card :deep(.carousel-wrapper) {
  min-height: 0;
}

.sweat-card :deep(.sweat-grid-wrapper) {
  padding: 0;
  background: transparent;
  border: 0;
  border-radius: 0;
  box-shadow: none;
}

.sweat-card :deep(.calendar-header) {
  margin-bottom: 14px;
}

.sweat-card :deep(.calendar-grid) {
  row-gap: 6px;
}

.sweat-card :deep(.calendar-cell) {
  min-height: 26px;
}

.sweat-card :deep(.day-status) {
  width: 26px;
  height: 26px;
  font-size: 11px;
}

.sweat-card :deep(.calendar-legend) {
  margin-top: 14px;
}

@media (max-width: 1200px) {
  .dashboard-container {
    padding-right: 32px;
  }
}

@media (max-width: 992px) {
  .dashboard-container {
    width: 100%;
    min-height: 100vh;
    padding: 40px 28px 48px;
    overflow: visible;
  }

  .bento-grid {
    row-gap: 24px;
  }

  .right-bento {
    height: auto;
  }

  .body-card,
  .carousel-card {
    height: auto;
    min-height: 320px;
  }

  .carousel-card {
    min-height: 280px;
  }
}

@media (max-width: 640px) {
  .dashboard-container {
    padding: 32px 18px 40px;
  }

  .metric-card {
    width: min(100%, 200px);
    flex-basis: min(100%, 200px);
  }

  .plan-card {
    min-height: 0;
    align-items: flex-start;
    flex-direction: column;
  }

  .plan-action {
    width: 100%;
    flex-basis: 44px;
  }

  .glass-card {
    padding: 20px;
    border-radius: 24px;
  }
}
</style>
