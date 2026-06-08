<template>
  <div class="ranking-card">
    <div class="card-header">
      <div>
        <h3>运动排行</h3>
        <p class="header-desc">近 7 天热门运动</p>
      </div>
      <div class="header-tabs">
        <button
          v-for="tab in rankTabs"
          :key="tab.value"
          class="tab-btn"
          :class="{ active: activeRank === tab.value }"
          @click="$emit('rank-change', tab.value)"
        >
          {{ tab.label }}
        </button>
      </div>
    </div>

    <div v-if="hasData" ref="chartRef" class="chart-container"></div>
    <div v-else class="empty-state">
      <div class="empty-icon">
        <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="#A0AEC0" stroke-width="1.5">
          <rect x="3" y="12" width="4" height="9" rx="1" />
          <rect x="10" y="7" width="4" height="14" rx="1" />
          <rect x="17" y="3" width="4" height="18" rx="1" />
        </svg>
      </div>
      <p>暂无数据</p>
      <span>添加训练记录后查看排行</span>
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
  activeRank: {
    type: String,
    default: 'duration'
  }
})

defineEmits(['rank-change'])

const chartRef = ref(null)
let chart = null

const rankTabs = [
  { label: '时长', value: 'duration' },
  { label: '次数', value: 'count' },
  { label: '消耗', value: 'calories' }
]

const hasData = computed(() => {
  return props.data && props.data.length > 0
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

  const sortedData = [...props.data].sort((a, b) => a.value - b.value)
  const names = sortedData.map(item => item.name)
  const values = sortedData.map(item => item.value)

  chart.setOption({
    grid: {
      top: 10,
      right: 50,
      bottom: 10,
      left: 10,
      containLabel: true
    },
    xAxis: {
      type: 'value',
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { show: false },
      splitLine: { show: false }
    },
    yAxis: {
      type: 'category',
      data: names,
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: {
        color: '#718096',
        fontSize: 12,
        fontWeight: 500
      }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      backgroundColor: '#FFFFFF',
      borderColor: '#E8ECF1',
      textStyle: { color: '#2D3748', fontSize: 13 },
      extraCssText: 'box-shadow: 0 8px 30px rgba(0, 0, 0, 0.08); border-radius: 14px; padding: 14px 18px;'
    },
    series: [{
      type: 'bar',
      data: values,
      barWidth: 14,
      itemStyle: {
        borderRadius: [0, 8, 8, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: 'rgba(126, 184, 218, 0.2)' },
          { offset: 1, color: '#7EB8DA' }
        ])
      },
      emphasis: {
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: 'rgba(126, 184, 218, 0.4)' },
            { offset: 1, color: '#5A9BC0' }
          ])
        }
      },
      label: {
        show: true,
        position: 'right',
        color: '#718096',
        fontSize: 12,
        fontWeight: 600,
        formatter: '{c}'
      }
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

watch(() => props.activeRank, () => {
  nextTick(updateChart)
})
</script>

<style scoped>
.ranking-card {
  background: #FFFFFF;
  border-radius: 24px;
  padding: 28px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
  transition: transform 0.3s cubic-bezier(0.22, 1, 0.36, 1), box-shadow 0.3s ease;
  position: relative;
  overflow: hidden;
}

.ranking-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #e8a18b, #F5D5A0, #e8a18b);
  background-size: 200% 100%;
  animation: shimmerLine 3.5s ease infinite;
  border-radius: 24px 24px 0 0;
}

@keyframes shimmerLine {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}

.ranking-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 36px rgba(0, 0, 0, 0.08);
}

.card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 16px;
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

.chart-container {
  width: 100%;
  height: 250px;
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
