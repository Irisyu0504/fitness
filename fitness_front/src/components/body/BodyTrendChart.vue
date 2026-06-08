<template>
  <div class="trend-chart-card">
    <div class="card-header">
      <h3>身体数据联合趋势</h3>
      <div class="time-tabs">
        <button
          v-for="tab in timeTabs"
          :key="tab.value"
          class="time-tab"
          :class="{ active: activeTimeRange === tab.value }"
          @click="$emit('time-change', tab.value)"
        >
          {{ tab.label }}
        </button>
      </div>
    </div>

    <div v-if="hasData" ref="chartRef" class="chart-container"></div>
    <div v-else class="empty-state">
      <p>暂无趋势数据</p>
      <span>添加体重、腰围等身体记录后即可查看联合趋势</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  chartData: {
    type: Object,
    default: () => ({ dates: [], series: [] })
  },
  activeMetric: {
    type: String,
    default: 'weight'
  },
  activeTimeRange: {
    type: String,
    default: '7days'
  }
})

defineEmits(['time-change'])

const chartRef = ref(null)
let chart = null

const timeTabs = [
  { label: '近7天', value: '7days' },
  { label: '按周', value: 'weekly' },
  { label: '按月', value: 'monthly' }
]

const metricColors = {
  weight: '#7EB8DA',
  waistline: '#feda6a',
  bmi: '#A8D8B9',
  bodyFatRate: '#B8A9C9'
}

const rightAxisMetrics = new Set(['bmi', 'bodyFatRate'])

const normalizedSeries = computed(() => {
  return (props.chartData.series || [])
    .map(item => ({
      ...item,
      values: (item.values || []).map(value => value === null || value === undefined ? null : Number(value))
    }))
    .filter(item => item.values.some(value => Number.isFinite(value)))
})

const hasData = computed(() => {
  return props.chartData.dates?.length > 0 && normalizedSeries.value.length > 0
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

  const dates = props.chartData.dates || []

  chart.setOption({
    color: normalizedSeries.value.map(item => metricColors[item.key] || '#7EB8DA'),
    legend: {
      top: 8,
      right: 0,
      icon: 'roundRect',
      itemWidth: 12,
      itemHeight: 6,
      textStyle: {
        color: '#718096',
        fontSize: 12
      }
    },
    grid: {
      top: 64,
      right: 58,
      bottom: 32,
      left: 48
    },
    xAxis: {
      type: 'category',
      data: dates,
      boundaryGap: false,
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: {
        color: '#A0AEC0',
        fontSize: 11
      }
    },
    yAxis: [
      createYAxis('体重/腰围'),
      {
        ...createYAxis('BMI/体脂率'),
        splitLine: { show: false }
      }
    ],
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#FFFFFF',
      borderColor: '#E8ECF1',
      textStyle: { color: '#2D3748' },
      extraCssText: 'box-shadow: 0 8px 30px rgba(0, 0, 0, 0.08); border-radius: 14px;',
      formatter: formatTooltip
    },
    series: normalizedSeries.value.map(item => ({
      id: item.key,
      name: item.label,
      type: 'line',
      yAxisIndex: rightAxisMetrics.has(item.key) ? 1 : 0,
      data: item.values,
      smooth: true,
      connectNulls: true,
      showSymbol: false,
      symbolSize: 7,
      emphasis: {
        focus: 'series',
        itemStyle: {
          borderColor: '#FFFFFF',
          borderWidth: 3,
          shadowColor: 'rgba(126, 184, 218, 0.3)',
          shadowBlur: 10
        }
      },
      lineStyle: {
        width: item.key === props.activeMetric ? 4 : 2.5,
        color: metricColors[item.key] || '#7EB8DA'
      },
      itemStyle: {
        color: metricColors[item.key] || '#7EB8DA'
      }
    }))
  }, true)
}

function createYAxis(name) {
  return {
    type: 'value',
    name,
    scale: true,
    nameTextStyle: {
      color: '#A0AEC0',
      fontSize: 11,
      padding: [0, 0, 6, 0]
    },
    axisLine: { show: false },
    axisTick: { show: false },
    splitLine: {
      lineStyle: {
        color: '#F0F2F5',
        type: 'dashed'
      }
    },
    axisLabel: {
      color: '#A0AEC0',
      fontSize: 11
    }
  }
}

function formatTooltip(params) {
  const rows = params
    .filter(item => item.value !== null && item.value !== undefined && item.value !== '-')
    .map(item => {
      const meta = normalizedSeries.value.find(series => series.label === item.seriesName)
      const color = item.color || metricColors[meta?.key] || '#7EB8DA'
      const unit = meta?.unit || ''
      return `
        <div style="display:flex;align-items:center;justify-content:space-between;gap:18px;margin-top:6px;">
          <span style="display:flex;align-items:center;gap:6px;color:#718096;">
            <i style="width:8px;height:8px;border-radius:50%;background:${color};display:inline-block;"></i>
            ${item.seriesName}
          </span>
          <strong style="color:#2D3748;">${item.value}${unit}</strong>
        </div>
      `
    })
    .join('')

  return `
    <div style="min-width:150px;padding:4px 0;">
      <div style="font-size:12px;color:#A0AEC0;margin-bottom:8px;">${params[0]?.axisValue || ''}</div>
      ${rows}
    </div>
  `
}

function renderChartWhenReady() {
  nextTick(() => {
    if (!hasData.value) {
      chart?.dispose()
      chart = null
      return
    }

    if (!chart) {
      initChart()
    } else {
      updateChart()
    }
  })
}

onMounted(renderChartWhenReady)

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  chart?.dispose()
  chart = null
})

watch(() => props.chartData, renderChartWhenReady, { deep: true })
watch(() => props.activeMetric, renderChartWhenReady)
</script>

<style scoped>
.trend-chart-card {
  background: #FFFFFF;
  border-radius: 24px;
  padding: 28px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 24px;
}

.card-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
}

.time-tabs {
  display: flex;
  gap: 4px;
  background: #F8F6F3;
  border-radius: 12px;
  padding: 4px;
  flex-shrink: 0;
}

.time-tab {
  padding: 6px 14px;
  border: none;
  border-radius: 10px;
  background: transparent;
  color: var(--text-secondary);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.time-tab.active {
  background: #FFFFFF;
  color: var(--text-primary);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.chart-container {
  width: 100%;
  height: 340px;
}

.empty-state {
  height: 340px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary);
}

.empty-state p {
  margin: 0 0 8px;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.empty-state span {
  font-size: 13px;
}

@media (max-width: 720px) {
  .card-header {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
