<template>
  <div class="trend-card">
    <div class="trend-header">
      <h3 class="trend-title">训练趋势</h3>
      <div class="trend-controls">
        <div class="time-tabs">
          <button
            v-for="tab in timeTabs"
            :key="tab.value"
            class="tab-btn"
            :class="{ active: activeTimeRange === tab.value }"
            @click="$emit('time-change', tab.value)"
          >{{ tab.label }}</button>
        </div>
        <div class="trend-tabs">
          <button
            v-for="tab in tabs"
            :key="tab.value"
            class="tab-btn"
            :class="{ active: activeMetric === tab.value }"
            @click="$emit('metric-change', tab.value)"
          >{{ tab.label }}</button>
        </div>
      </div>
    </div>

    <div v-if="hasData" ref="chartRef" class="chart-area"></div>

    <div v-else class="empty-state">
      <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="#A0AEC0" stroke-width="1.5">
        <polyline points="22 12 18 12 15 21 9 3 6 12 2 12" />
      </svg>
      <p>暂无趋势数据</p>
      <span>添加训练记录后查看趋势</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  chartData: { type: Object, default: () => ({ dates: [], values: [] }) },
  activeMetric: { type: String, default: 'calories' },
  activeTimeRange: { type: String, default: '7days' }
})

defineEmits(['metric-change', 'time-change'])

const chartRef = ref(null)
let chart = null

const tabs = [
  { label: '消耗热量', value: 'calories' },
  { label: '训练时长', value: 'duration' }
]

const timeTabs = [
  { label: '近7天', value: '7days' },
  { label: '按周', value: 'weekly' },
  { label: '按月', value: 'monthly' }
]

const metricUnits = {
  calories: 'kcal',
  duration: 'min'
}

const hasData = computed(() =>
  props.chartData.dates?.length > 0 && props.chartData.values?.some(v => Number(v) > 0)
)

const yAxisMax = computed(() => {
  const values = props.chartData.values || []
  const maxValue = Math.max(...values.map(value => Number(value) || 0), 0)
  const step = props.activeMetric === 'duration' ? 10 : 50
  const floor = props.activeMetric === 'duration' ? 30 : 100
  return Math.max(floor, Math.ceil((maxValue * 1.15) / step) * step)
})

function initChart() {
  if (!chartRef.value) return
  chart = echarts.init(chartRef.value)
  renderChart()
  window.addEventListener('resize', handleResize)
}

function handleResize() {
  chart?.resize()
}

function renderChart() {
  if (!chart || !hasData.value) return
  const { dates, values } = props.chartData
  const numericValues = values.map(value => Number(value) || 0)
  const unit = metricUnits[props.activeMetric] || ''

  chart.setOption({
    grid: { top: 24, right: 16, bottom: 28, left: 44 },
    xAxis: {
      type: 'category',
      data: dates,
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: '#A0AEC0', fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      min: 0,
      max: yAxisMax.value,
      splitNumber: 4,
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: { lineStyle: { color: '#F0F2F5', type: 'dashed' } },
      axisLabel: { color: '#A0AEC0', fontSize: 11 }
    },
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#FFFFFF',
      borderColor: '#E8ECF1',
      textStyle: { color: '#2D3748', fontSize: 13 },
      extraCssText: 'box-shadow: 0 8px 24px rgba(0,0,0,0.08); border-radius: 12px; padding: 12px 16px;',
      formatter: (params) => {
        const p = params[0]
        return `${p.axisValue}<br/><strong>${p.value} ${unit}</strong>`
      }
    },
    series: [{
      type: 'line',
      data: numericValues,
      smooth: true,
      showSymbol: true,
      symbolSize: 6,
      symbol: 'circle',
      lineStyle: { width: 3, color: '#7EB8DA' },
      itemStyle: { color: '#7EB8DA', borderColor: '#fff', borderWidth: 2 },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(126,184,218,0.25)' },
          { offset: 1, color: 'rgba(126,184,218,0.02)' }
        ])
      }
    }]
  }, true)
}

onMounted(() => { nextTick(() => { if (hasData.value) initChart() }) })
onBeforeUnmount(() => { window.removeEventListener('resize', handleResize); chart?.dispose() })

watch(() => props.chartData, () => {
  nextTick(() => {
    if (hasData.value) {
      if (!chart) initChart()
      else renderChart()
    }
  })
}, { deep: true })

watch(() => props.activeMetric, () => { nextTick(renderChart) })
watch(() => props.activeTimeRange, () => { nextTick(renderChart) })
</script>

<style scoped>
.trend-card {
  background: #FFFFFF;
  border: 1px solid #E8ECF1;
  border-radius: 24px;
  padding: 28px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06), 0 1px 3px rgba(0, 0, 0, 0.04);
  height: 100%;
  display: flex;
  flex-direction: column;
}

.trend-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
}

.trend-title {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
  color: #2D3748;
}

.trend-controls {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  flex-wrap: wrap;
  gap: 8px;
}

.time-tabs,
.trend-tabs {
  display: flex;
  gap: 4px;
  background: #F5F7FA;
  border-radius: 10px;
  padding: 3px;
}

.tab-btn {
  padding: 6px 14px;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: #718096;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.tab-btn.active {
  background: #FFFFFF;
  color: #2D3748;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.06);
}

.chart-area {
  flex: 1;
  min-height: 260px;
}

.empty-state {
  flex: 1;
  min-height: 260px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.empty-state p {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: #2D3748;
}

.empty-state span {
  font-size: 12px;
  color: #A0AEC0;
}
</style>
