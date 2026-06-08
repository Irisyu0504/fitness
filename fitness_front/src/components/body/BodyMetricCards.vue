<template>
  <div class="metric-cards-grid">
    <div
      v-for="metric in metrics"
      :key="metric.key"
      class="metric-card"
      :class="{ 'is-active': activeMetric === metric.key }"
      @click="$emit('select', metric.key)"
    >
      <div class="metric-info">
        <span class="metric-label">{{ metric.label }}</span>
        <div class="metric-value-row">
          <strong class="metric-value">{{ metric.value ?? '--' }}</strong>
          <span class="metric-unit">{{ metric.unit }}</span>
        </div>
        <span
          v-if="metric.change !== null && metric.change !== undefined"
          class="metric-change"
          :class="getChangeClass(metric.key, metric.change)"
        >
          {{ formatChange(metric.key, metric.change) }}
        </span>
        <span v-else class="metric-change muted">暂无对比数据</span>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  metrics: {
    type: Array,
    default: () => []
  },
  activeMetric: {
    type: String,
    default: 'weight'
  }
})

defineEmits(['select'])

function getChangeClass(key, change) {
  if (change === 0) return 'neutral'
  if (['weight', 'bodyFatRate', 'waistline'].includes(key)) {
    return change < 0 ? 'positive' : 'negative'
  }
  return 'neutral'
}

function formatChange(key, change) {
  if (change === 0) return '无变化'
  const prefix = change > 0 ? '+' : ''
  const units = {
    weight: 'kg',
    bodyFatRate: '%',
    waistline: 'cm',
    bmi: ''
  }
  return `较上次 ${prefix}${change}${units[key] || ''}`
}
</script>

<style scoped>
.metric-cards-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.metric-card {
  background: #FFFFFF;
  border-radius: 20px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.metric-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.08);
}

.metric-card.is-active {
  border-color: #7EB8DA;
  background: rgba(126, 184, 218, 0.04);
}

.metric-label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: 12px;
}

.metric-value-row {
  display: flex;
  align-items: baseline;
  gap: 4px;
  margin-bottom: 12px;
}

.metric-value {
  font-size: 28px;
  font-weight: 800;
  color: var(--text-primary);
  line-height: 1;
}

.metric-unit {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-secondary);
}

.metric-change {
  font-size: 12px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 999px;
  display: inline-block;
}

.metric-change.positive {
  color: #7BC6A0;
  background: rgba(123, 198, 160, 0.1);
}

.metric-change.negative {
  color: #e8a18b;
  background: rgba(232, 161, 139, 0.1);
}

.metric-change.neutral {
  color: var(--text-secondary);
  background: #F8F6F3;
}

.metric-change.muted {
  color: #A0AEC0;
  background: transparent;
  padding: 0;
}

@media (max-width: 1200px) {
  .metric-cards-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
