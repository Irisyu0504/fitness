<template>
  <div class="heatmap-card">
    <div class="card-header">
      <div>
        <h3>训练热力日历</h3>
        <p class="header-desc">近 4 周训练打卡记录</p>
      </div>
      <div class="header-tabs">
        <button
          v-for="tab in metricTabs"
          :key="tab.value"
          class="tab-btn"
          :class="{ active: activeMetric === tab.value }"
          @click="$emit('metric-change', tab.value)"
        >
          {{ tab.label }}
        </button>
      </div>
    </div>

    <div v-if="hasData" class="heatmap-body">
      <div class="weekday-labels">
        <span v-for="day in weekdayLabels" :key="day" class="weekday-label">{{ day }}</span>
      </div>
      <div class="heatmap-grid">
        <div v-for="(week, wi) in weeks" :key="wi" class="heatmap-week">
          <div
            v-for="(cell, ci) in week"
            :key="ci"
            class="heatmap-cell"
            :style="{ background: cell.color }"
            @mouseenter="hoveredCell = cell"
            @mouseleave="hoveredCell = null"
          />
        </div>
      </div>
      <div class="heatmap-dates">
        <span v-for="(week, wi) in weeks" :key="wi" class="date-label">
          {{ week[0]?.dateStr || '' }}
        </span>
      </div>
      <div class="heatmap-legend">
        <span class="legend-label">少</span>
        <div
          v-for="(color, i) in legendColors"
          :key="i"
          class="legend-cell"
          :style="{ background: color }"
        />
        <span class="legend-label">多</span>
      </div>
    </div>

    <div v-else class="empty-state">
      <div class="empty-icon">
        <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="#A0AEC0" stroke-width="1.5">
          <rect x="3" y="4" width="18" height="18" rx="2" />
          <line x1="16" y1="2" x2="16" y2="6" />
          <line x1="8" y1="2" x2="8" y2="6" />
          <line x1="3" y1="10" x2="21" y2="10" />
        </svg>
      </div>
      <p>暂无打卡数据</p>
      <span>开始训练后查看热力日历</span>
    </div>

    <!-- Hover tooltip -->
    <Transition name="tooltip-fade">
      <div v-if="hoveredCell && hoveredCell.count > 0" class="heatmap-tooltip" :style="tooltipStyle">
        <div class="tooltip-date">{{ hoveredCell.dateStr }}</div>
        <div class="tooltip-row">
          <span class="tooltip-dot" style="background: #7EB8DA"></span>
          <span>训练 {{ hoveredCell.count }} 次</span>
        </div>
        <div class="tooltip-row">
          <span class="tooltip-dot" style="background: #e8a18b"></span>
          <span>时长 {{ hoveredCell.duration }} 分钟</span>
        </div>
        <div class="tooltip-row">
          <span class="tooltip-dot" style="background: #A8D8B9"></span>
          <span>消耗 {{ hoveredCell.calories }} kcal</span>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  records: {
    type: Array,
    default: () => []
  },
  activeMetric: {
    type: String,
    default: 'duration'
  }
})

defineEmits(['metric-change'])

const hoveredCell = ref(null)
const tooltipStyle = ref({})

const metricTabs = [
  { label: '时长', value: 'duration' },
  { label: '消耗', value: 'calories' }
]

const weekdayLabels = ['一', '二', '三', '四', '五', '六', '日']

// Build color scale
const legendColors = [
  '#F0F2F5',
  'rgba(126, 184, 218, 0.2)',
  'rgba(126, 184, 218, 0.4)',
  'rgba(126, 184, 218, 0.65)',
  '#7EB8DA'
]

// Group records by date
const recordsByDate = computed(() => {
  const map = {}
  props.records.forEach(r => {
    if (!r.recordDate) return
    if (!map[r.recordDate]) {
      map[r.recordDate] = { count: 0, duration: 0, calories: 0 }
    }
    map[r.recordDate].count += 1
    map[r.recordDate].duration += r.duration || 0
    map[r.recordDate].calories += Number(r.caloriesBurned || 0)
  })
  return map
})

// Build 4-week grid (28 days)
const weeks = computed(() => {
  const today = new Date()
  const result = []
  // Find the Monday of the current week
  const dayOfWeek = today.getDay() || 7 // Sunday = 7
  const currentMonday = new Date(today)
  currentMonday.setDate(today.getDate() - dayOfWeek + 1)

  // Go back 3 weeks to get 4 weeks total
  const startDate = new Date(currentMonday)
  startDate.setDate(currentMonday.getDate() - 21)

  // Collect all values for normalization
  const allValues = []
  for (let d = 0; d < 28; d++) {
    const date = new Date(startDate)
    date.setDate(startDate.getDate() + d)
    const dateStr = formatDate(date)
    const rec = recordsByDate.value[dateStr]
    if (rec) {
      allValues.push(rec[props.activeMetric] || 0)
    }
  }
  const maxValue = Math.max(...allValues, 1)

  for (let w = 0; w < 4; w++) {
    const week = []
    for (let d = 0; d < 7; d++) {
      const date = new Date(startDate)
      date.setDate(startDate.getDate() + w * 7 + d)
      const dateStr = formatDate(date)
      const rec = recordsByDate.value[dateStr]
      const value = rec ? (rec[props.activeMetric] || 0) : 0
      const intensity = value > 0 ? Math.max(0.15, value / maxValue) : 0

      week.push({
        date,
        dateStr: formatDisplayDate(date),
        count: rec?.count || 0,
        duration: rec?.duration || 0,
        calories: rec?.calories || 0,
        value,
        color: getHeatColor(intensity),
        isToday: dateStr === formatDate(today)
      })
    }
    result.push(week)
  }

  return result
})

const hasData = computed(() => {
  return Object.keys(recordsByDate.value).length > 0
})

function getHeatColor(intensity) {
  if (intensity === 0) return '#F0F2F5'
  if (intensity < 0.25) return 'rgba(126, 184, 218, 0.2)'
  if (intensity < 0.5) return 'rgba(126, 184, 218, 0.4)'
  if (intensity < 0.75) return 'rgba(126, 184, 218, 0.65)'
  return '#7EB8DA'
}

function formatDate(date) {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

function formatDisplayDate(date) {
  return `${date.getMonth() + 1}/${date.getDate()}`
}
</script>

<style scoped>
.heatmap-card {
  background: #FFFFFF;
  border-radius: 24px;
  padding: 28px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
  position: relative;
}

.card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 24px;
}

.card-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
}

.header-desc {
  margin: 6px 0 0;
  font-size: 12px;
  color: var(--text-secondary);
}

.header-tabs {
  display: flex;
  gap: 4px;
  background: #F8F6F3;
  border-radius: 10px;
  padding: 3px;
}

.tab-btn {
  padding: 5px 12px;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: var(--text-secondary);
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.tab-btn.active {
  background: #FFFFFF;
  color: var(--text-primary);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.06);
}

.heatmap-body {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.weekday-labels {
  display: flex;
  flex-direction: column;
  gap: 4px;
  position: absolute;
  left: 28px;
  top: 96px;
}

.weekday-label {
  height: 20px;
  display: flex;
  align-items: center;
  font-size: 10px;
  color: var(--text-tertiary);
  line-height: 1;
}

.heatmap-grid {
  display: flex;
  gap: 4px;
  margin-left: 20px;
}

.heatmap-week {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.heatmap-cell {
  width: 20px;
  height: 20px;
  border-radius: 4px;
  transition: transform 0.15s ease;
  cursor: pointer;
}

.heatmap-cell:hover {
  transform: scale(1.2);
  outline: 2px solid rgba(126, 184, 218, 0.4);
  outline-offset: 1px;
}

.heatmap-dates {
  display: flex;
  gap: 4px;
  margin-left: 20px;
  margin-top: 4px;
}

.date-label {
  width: 20px;
  font-size: 9px;
  color: var(--text-tertiary);
  text-align: center;
}

.heatmap-legend {
  display: flex;
  align-items: center;
  gap: 4px;
  justify-content: flex-end;
  margin-top: 12px;
}

.legend-label {
  font-size: 10px;
  color: var(--text-tertiary);
  margin: 0 4px;
}

.legend-cell {
  width: 14px;
  height: 14px;
  border-radius: 3px;
}

/* Tooltip */
.heatmap-tooltip {
  position: absolute;
  background: #FFFFFF;
  border: 1px solid #E8ECF1;
  border-radius: 14px;
  padding: 14px 18px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
  z-index: 100;
  pointer-events: none;
  min-width: 160px;
}

.tooltip-date {
  font-size: 13px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 10px;
  padding-bottom: 8px;
  border-bottom: 1px solid #F0F2F5;
}

.tooltip-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 4px;
}

.tooltip-row:last-child {
  margin-bottom: 0;
}

.tooltip-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  flex-shrink: 0;
}

.tooltip-fade-enter-active,
.tooltip-fade-leave-active {
  transition: opacity 0.15s ease;
}

.tooltip-fade-enter-from,
.tooltip-fade-leave-to {
  opacity: 0;
}

/* Empty state */
.empty-state {
  height: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary);
}

.empty-icon {
  margin-bottom: 12px;
  opacity: 0.5;
}

.empty-state p {
  margin: 0 0 6px;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.empty-state span {
  font-size: 12px;
}
</style>
