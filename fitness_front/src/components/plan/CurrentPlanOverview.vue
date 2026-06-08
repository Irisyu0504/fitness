<template>
  <div class="overview-card">
    <div class="card-header">
      <h3>当前训练计划</h3>
      <PlanStatusTag v-if="plan" :status="plan.status" />
    </div>

    <div v-if="plan" class="plan-content">
      <div class="plan-info">
        <h2 class="plan-name">{{ plan.planName }}</h2>
        <p class="plan-goal">{{ plan.planGoal }}</p>
      </div>

      <div class="plan-stats">
        <div class="stat-item">
          <span class="stat-label">频率</span>
          <strong class="stat-value">{{ plan.frequency || '--' }}</strong>
        </div>
        <div class="stat-item">
          <span class="stat-label">开始日期</span>
          <strong class="stat-value">{{ plan.startDate || '--' }}</strong>
        </div>
        <div class="stat-item">
          <span class="stat-label">结束日期</span>
          <strong class="stat-value">{{ plan.endDate || '--' }}</strong>
        </div>
        <div class="stat-item">
          <span class="stat-label">已执行</span>
          <strong class="stat-value">{{ elapsedDays }} 天</strong>
        </div>
        <div class="stat-item">
          <span class="stat-label">剩余</span>
          <strong class="stat-value">{{ remainingDays }} 天</strong>
        </div>
      </div>

      <div class="progress-section">
        <div class="progress-header">
          <span>计划进度</span>
          <span class="progress-percent">{{ progressPercent }}%</span>
        </div>
        <div class="progress-bar">
          <div class="progress-fill" :style="{ width: progressPercent + '%' }"></div>
        </div>
      </div>
    </div>

    <div v-else class="empty-state">
      <p>当前暂无进行中的训练计划</p>
      <span>可以手动创建或使用 AI 生成训练计划</span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import PlanStatusTag from './PlanStatusTag.vue'

const props = defineProps({
  plan: {
    type: Object,
    default: null
  }
})

const elapsedDays = computed(() => {
  if (!props.plan?.startDate) return 0
  const start = new Date(props.plan.startDate)
  const now = new Date()
  return Math.max(0, Math.floor((now - start) / 86400000))
})

const remainingDays = computed(() => {
  if (!props.plan?.endDate) return 0
  const end = new Date(props.plan.endDate)
  const now = new Date()
  return Math.max(0, Math.ceil((end - now) / 86400000))
})

const progressPercent = computed(() => {
  if (!props.plan?.startDate || !props.plan?.endDate) return 0
  const start = new Date(props.plan.startDate)
  const end = new Date(props.plan.endDate)
  const now = new Date()
  const total = end - start
  const elapsed = now - start
  if (total <= 0) return 100
  return Math.min(100, Math.max(0, Math.round((elapsed / total) * 100)))
})
</script>

<style scoped>
.overview-card {
  background: #FFFFFF;
  border-radius: 24px;
  padding: 28px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.card-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
}

.plan-info {
  margin-bottom: 24px;
}

.plan-name {
  margin: 0 0 8px;
  font-size: 24px;
  font-weight: 800;
  color: var(--text-primary);
}

.plan-goal {
  margin: 0;
  font-size: 14px;
  color: var(--text-secondary);
}

.plan-stats {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
  margin-bottom: 24px;
  padding: 20px;
  background: #F8F6F3;
  border-radius: 16px;
}

.stat-item {
  text-align: center;
}

.stat-label {
  display: block;
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.stat-value {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
}

.progress-section {
  padding-top: 20px;
  border-top: 1px solid #E8ECF1;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  font-size: 13px;
  color: var(--text-secondary);
}

.progress-percent {
  font-weight: 700;
  color: var(--text-primary);
}

.progress-bar {
  height: 8px;
  background: #F0F2F5;
  border-radius: 999px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: #7EB8DA;
  border-radius: 999px;
  transition: width 0.5s ease;
}

.empty-state {
  text-align: center;
  padding: 40px 0;
  color: var(--text-secondary);
}

.empty-state p {
  margin: 0 0 8px;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.empty-state span {
  font-size: 14px;
}

@media (max-width: 768px) {
  .plan-stats {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>
