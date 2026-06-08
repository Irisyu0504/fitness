<template>
  <div class="distribution-card">
    <div class="card-header">
      <div>
        <h3>肌群分布</h3>
        <p class="header-desc">各肌群训练占比</p>
      </div>
      <div class="header-tabs">
        <button
          v-for="tab in statTabs"
          :key="tab.value"
          class="tab-btn"
          :class="{ active: activeStat === tab.value }"
          @click="$emit('stat-change', tab.value)"
        >
          {{ tab.label }}
        </button>
      </div>
    </div>

    <div v-if="hasData" class="chart-section">
      <div ref="chartRef" class="pie-chart"></div>
      <div class="legend-list">
        <div v-for="(item, idx) in chartData" :key="item.name" class="legend-item" :style="{ animationDelay: idx * 60 + 'ms' }">
          <span class="legend-dot" :style="{ background: item.color }"></span>
          <span class="legend-name">{{ item.name }}</span>
          <span class="legend-value">{{ item.value }}</span>
          <span class="legend-percent">{{ item.percent }}%</span>
        </div>
      </div>
    </div>

    <div v-else class="empty-state">
      <div class="empty-icon">
        <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="#A0AEC0" stroke-width="1.5">
          <circle cx="12" cy="12" r="10" />
          <path d="M12 2 A10 10 0 0 1 22 12 L12 12 Z" fill="rgba(126,184,218,0.2)" stroke="none" />
        </svg>
      </div>
      <p>暂无肌群数据</p>
      <span>添加训练记录后查看分布</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  data: {
    type: Array,
    default: () => []
  },
  activeStat: {
    type: String,
    default: 'duration'
  }
})

defineEmits(['stat-change'])

const chartRef = ref(null)
let chart = null

const statTabs = [
  { label: '时长', value: 'duration' },
  { label: '消耗', value: 'calories' },
  { label: '次数', value: 'count' }
]

const colors = [
  '#7EB8DA', '#e8a18b', '#F5D5A0', '#B8A9C9',
  '#A8D8B9', '#F0A8A8', '#D4C5E2'
]

const chartData = computed(() => {
  if (!props.data || props.data.length === 0) return []

  const total = props.data.reduce((sum, item) => sum + (item.value || 0), 0)

  return props.data.map((item, index) => ({
    name: item.name,
    value: item.value || 0,
    percent: total ? Math.round((item.value / total) * 100) : 0,
    color: colors[index % colors.length]
  }))
})

const hasData = computed(() => {
  return chartData.value.length > 0 && chartData.value.some(item => item.value > 0)
})

function initChart() {
  if (!chartRef.value) return
  chart = echarts.init(chartRef.value)
  updateChart()
  window.addEventListener('resize', handleResize)
}

function handleResize() {
  chart?.resize()
}

function updateChart() {
  if (!chart || !hasData.value) return

  chart.setOption({
    tooltip: {
      trigger: 'item',
      backgroundColor: '#FFFFFF',
      borderColor: '#E8ECF1',
      textStyle: { color: '#2D3748', fontSize: 13 },
      extraCssText: 'box-shadow: 0 8px 30px rgba(0, 0, 0, 0.08); border-radius: 14px; padding: 14px 18px;',
      formatter: function (params) {
        return `<div style="font-weight:700;margin-bottom:4px">${params.name}</div>
          <div style="color:#718096">${params.value} (${params.percent}%)</div>`
      }
    },
    series: [{
      type: 'pie',
      radius: ['52%', '78%'],
      center: ['50%', '50%'],
      avoidLabelOverlap: false,
      label: { show: false },
      labelLine: { show: false },
      emphasis: {
        scaleSize: 8,
        itemStyle: {
          shadowBlur: 16,
          shadowColor: 'rgba(0, 0, 0, 0.08)'
        }
      },
      data: chartData.value.map((item, index) => ({
        value: item.value,
        name: item.name,
        itemStyle: { color: colors[index % colors.length] }
      }))
    }]
  }, true)
}

onMounted(() => {
  nextTick(() => {
    if (hasData.value) initChart()
  })
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  chart?.dispose()
})

watch(() => props.data, () => {
  nextTick(() => {
    if (hasData.value) {
      if (!chart) initChart()
      else updateChart()
    }
  })
}, { deep: true })

watch(() => props.activeStat, () => {
  nextTick(() => {
    if (hasData.value) {
      if (!chart) initChart()
      else updateChart()
    }
  })
})
</script>

<style scoped>
.distribution-card {
  background: #FFFFFF;
  border-radius: 24px;
  padding: 28px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
  transition: transform 0.3s cubic-bezier(0.22, 1, 0.36, 1), box-shadow 0.3s ease;
  position: relative;
  overflow: hidden;
}

.distribution-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #A8D8B9, #7EB8DA, #B8A9C9, #A8D8B9);
  background-size: 200% 100%;
  animation: shimmerLine 4s ease infinite;
  border-radius: 24px 24px 0 0;
}

@keyframes shimmerLine {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}

.distribution-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 36px rgba(0, 0, 0, 0.08);
}

.card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 20px;
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
  transition: all 0.25s cubic-bezier(0.22, 1, 0.36, 1);
}

.tab-btn:hover:not(.active) {
  color: var(--text-primary);
  background: rgba(126, 184, 218, 0.06);
}

.tab-btn.active {
  background: #FFFFFF;
  color: var(--text-primary);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.06);
}

.chart-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.pie-chart {
  width: 100%;
  height: 180px;
  animation: chartReveal 0.6s cubic-bezier(0.22, 1, 0.36, 1) forwards;
}

@keyframes chartReveal {
  from {
    opacity: 0;
    transform: scale(0.94);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.legend-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
  padding: 6px 10px;
  border-radius: 10px;
  transition: background 0.2s ease;
  animation: legendFadeIn 0.4s ease forwards;
  opacity: 0;
}

@keyframes legendFadeIn {
  from {
    opacity: 0;
    transform: translateX(-8px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.legend-item:hover {
  background: #F8F6F3;
}

.legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}

.legend-name {
  flex: 1;
  color: var(--text-primary);
  font-weight: 500;
}

.legend-value {
  color: var(--text-secondary);
  min-width: 40px;
  text-align: right;
}

.legend-percent {
  color: var(--text-tertiary);
  min-width: 40px;
  text-align: right;
  font-size: 12px;
}

.empty-state {
  height: 250px;
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
  margin: 0 0 8px;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.empty-state span {
  font-size: 12px;
}
</style>
