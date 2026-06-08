<template>
  <div class="trend-wrap">
    <div class="trend-header">
      <h3 class="trend-title">本周训练趋势</h3>
      <div class="trend-tabs">
        <button
          v-for="tab in tabs"
          :key="tab.value"
          class="tab-btn"
          :class="{ active: metric === tab.value }"
          @click="$emit('metric-change', tab.value)"
        >{{ tab.label }}</button>
      </div>
    </div>
    <div v-if="hasData" ref="chartRef" class="chart-area"></div>
    <div v-else class="empty">
      <p>暂无数据</p>
      <span>添加训练记录后查看趋势</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  chartData: { type: Object, default: () => ({ dates: [], values: [] }) },
  metric: { type: String, default: 'calories' }
})

defineEmits(['metric-change'])

const chartRef = ref(null)
let chart = null

const tabs = [
  { label: '消耗热量', value: 'calories' },
  { label: '训练时长', value: 'duration' },
  { label: '训练次数', value: 'count' }
]

const hasData = computed(() =>
  props.chartData.dates?.length > 0 && props.chartData.values?.some(v => v > 0)
)

function initChart() {
  if (!chartRef.value) return
  chart = echarts.init(chartRef.value)
  render()
  window.addEventListener('resize', handleResize)
}

function handleResize() { chart?.resize() }

function render() {
  if (!chart || !hasData.value) return
  const { dates, values } = props.chartData

  chart.setOption({
    grid: { top: 20, right: 16, bottom: 28, left: 44 },
    xAxis: {
      type: 'category', data: dates,
      axisLine: { show: false }, axisTick: { show: false },
      axisLabel: { color: '#A0AEC0', fontSize: 11 }
    },
    yAxis: {
      type: 'value', scale: true,
      axisLine: { show: false }, axisTick: { show: false },
      splitLine: { lineStyle: { color: '#F0F2F5', type: 'dashed' } },
      axisLabel: { color: '#A0AEC0', fontSize: 11 }
    },
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#FFFFFF', borderColor: '#E8ECF1',
      textStyle: { color: '#2D3748', fontSize: 13 },
      extraCssText: 'box-shadow: 0 8px 24px rgba(0,0,0,0.08); border-radius: 12px; padding: 10px 14px;'
    },
    series: [{
      type: 'line', data: values, smooth: true,
      showSymbol: true, symbolSize: 6, symbol: 'circle',
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
  nextTick(() => { if (hasData.value) { if (!chart) initChart(); else render() } })
}, { deep: true })

watch(() => props.metric, () => { nextTick(render) })
</script>

<style scoped>
.trend-wrap {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.trend-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  flex-shrink: 0;
}

.trend-title {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #2D3748;
}

.trend-tabs {
  display: flex;
  gap: 4px;
  background: #F5F7FA;
  border-radius: 10px;
  padding: 3px;
}

.tab-btn {
  padding: 5px 12px;
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
  min-height: 200px;
}

.empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.empty p { margin: 0; font-size: 14px; font-weight: 600; color: #2D3748; }
.empty span { font-size: 12px; color: #A0AEC0; }
</style>
