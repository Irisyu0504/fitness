<template>
  <div class="weight-goal-card">
    <div class="card-header">
      <h3>目标体重计划</h3>
      <span class="card-badge">减重</span>
    </div>

    <div v-if="hasGoal" class="goal-content">
      <div class="goal-stats">
        <div class="stat-item">
          <span class="stat-label">当前</span>
          <strong class="stat-value">{{ currentWeight }} kg</strong>
        </div>
        <div class="stat-item target">
          <span class="stat-label">目标</span>
          <strong class="stat-value">{{ targetWeight }} kg</strong>
        </div>
      </div>

      <div class="progress-section">
        <div class="progress-bar-container">
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: progressPercent + '%' }">
              <div class="progress-thumb"></div>
            </div>
          </div>
          <div class="progress-labels">
            <span>{{ startWeight }} kg</span>
            <span>{{ targetWeight }} kg</span>
          </div>
        </div>

        <div class="progress-info">
          <div class="progress-percent">
            <strong>{{ progressPercent }}%</strong>
            <span>已完成</span>
          </div>
          <div class="remaining">
            <strong>{{ remainingWeight }} kg</strong>
            <span>距离目标</span>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="empty-state">
      <p>暂未设置目标体重</p>
      <span>在目标页面设置后即可追踪进度</span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  currentWeight: { type: Number, default: null },
  targetWeight: { type: Number, default: null },
  startWeight: { type: Number, default: null }
})

const hasGoal = computed(() => {
  return props.currentWeight && props.targetWeight && props.startWeight
})

const progressPercent = computed(() => {
  if (!hasGoal.value) return 0
  const total = Math.abs(props.startWeight - props.targetWeight)
  const completed = Math.abs(props.startWeight - props.currentWeight)
  if (!total) return 100
  return Math.min(100, Math.max(0, Math.round((completed / total) * 100)))
})

const remainingWeight = computed(() => {
  if (!hasGoal.value) return '--'
  return Math.abs(props.currentWeight - props.targetWeight).toFixed(1)
})
</script>

<style scoped>
.weight-goal-card {
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

.card-badge {
  padding: 4px 12px;
  font-size: 12px;
  font-weight: 600;
  color: var(--text-secondary);
  background: #F8F6F3;
  border-radius: 999px;
}

.goal-stats {
  display: flex;
  justify-content: space-between;
  margin-bottom: 24px;
}

.stat-item {
  text-align: center;
}

.stat-label {
  display: block;
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 4px;
}

.stat-value {
  font-size: 24px;
  font-weight: 800;
  color: var(--text-primary);
}

.stat-item.target .stat-value {
  color: #7EB8DA;
}

.progress-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.progress-bar-container {
  flex: 1;
}

.progress-bar {
  height: 12px;
  background: #F8F6F3;
  border-radius: 999px;
  overflow: hidden;
  position: relative;
}

.progress-fill {
  height: 100%;
  background: #7EB8DA;
  border-radius: 999px;
  transition: width 0.8s ease;
  position: relative;
}

.progress-thumb {
  position: absolute;
  right: -6px;
  top: 50%;
  transform: translateY(-50%);
  width: 20px;
  height: 20px;
  background: #FFFFFF;
  border: 3px solid #7EB8DA;
  border-radius: 50%;
  box-shadow: 0 2px 8px rgba(126, 184, 218, 0.3);
}

.progress-labels {
  display: flex;
  justify-content: space-between;
  margin-top: 8px;
  font-size: 11px;
  color: var(--text-secondary);
}

.progress-info {
  display: flex;
  justify-content: space-around;
  padding-top: 16px;
  border-top: 1px solid #E8ECF1;
}

.progress-percent,
.remaining {
  text-align: center;
}

.progress-percent strong,
.remaining strong {
  display: block;
  font-size: 20px;
  font-weight: 800;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.progress-percent span,
.remaining span {
  font-size: 12px;
  color: var(--text-secondary);
}

.empty-state {
  text-align: center;
  padding: 20px 0;
  color: var(--text-secondary);
}

.empty-state p {
  margin: 0 0 8px;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.empty-state span {
  font-size: 12px;
}
</style>
