<template>
  <div class="analysis-card">
    <div class="card-header">
      <h3>营养结构分析</h3>
    </div>

    <div class="chart-section">
      <div ref="pieChartRef" class="pie-chart"></div>

      <div class="macro-summary">
        <div class="macro-legend">
          <div class="legend-item">
            <span class="legend-dot protein"></span>
            <span>蛋白质 {{ proteinPercent }}%</span>
          </div>
          <div class="legend-item">
            <span class="legend-dot carbs"></span>
            <span>碳水 {{ carbsPercent }}%</span>
          </div>
          <div class="legend-item">
            <span class="legend-dot fat"></span>
            <span>脂肪 {{ fatPercent }}%</span>
          </div>
        </div>

        <p class="macro-message" :class="messageClass">{{ macroMessage }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import * as echarts from 'echarts'

const EMPTY_MESSAGE = '今天还未记录，快来添加饮食记录吧'

const props = defineProps({
  data: {
    type: Object,
    default: () => ({})
  }
})

const pieChartRef = ref(null)
let chart = null

const macroPercentages = computed(() => props.data.macroPercentages || {})
const proteinCalories = computed(() => toNumber(props.data.totalProtein) * 4)
const carbsCalories = computed(() => toNumber(props.data.totalCarbs) * 4)
const fatCalories = computed(() => toNumber(props.data.totalFat) * 9)
const totalMacroCalories = computed(() => proteinCalories.value + carbsCalories.value + fatCalories.value)

const hasTodayRecords = computed(() => {
  return toNumber(props.data.recordCount) > 0 || toNumber(props.data.totalCalories) > 0 || totalMacroCalories.value > 0
})

const proteinPercent = computed(() => readPercent('proteinPercent', proteinCalories.value))
const carbsPercent = computed(() => readPercent('carbsPercent', carbsCalories.value))
const fatPercent = computed(() => readPercent('fatPercent', fatCalories.value))

const hasMacroData = computed(() => {
  return hasTodayRecords.value && (proteinPercent.value + carbsPercent.value + fatPercent.value) > 0
})

const macroMessage = computed(() => {
  if (!hasTodayRecords.value) return EMPTY_MESSAGE
  return props.data.macroReminder || buildFallbackReminder()
})

const messageClass = computed(() => {
  if (!hasTodayRecords.value) return 'is-empty'
  if (macroMessage.value.includes('偏高') || macroMessage.value.includes('偏低') || macroMessage.value.includes('不完整')) {
    return 'is-warning'
  }
  return 'is-balanced'
})

function toNumber(value) {
  const numberValue = Number(value)
  return Number.isFinite(numberValue) ? numberValue : 0
}

function readPercent(key, calories) {
  const backendValue = macroPercentages.value[key] ?? props.data[key]
  if (backendValue !== undefined && backendValue !== null) {
    return Math.max(0, Math.round(toNumber(backendValue)))
  }

  if (!totalMacroCalories.value) return 0
  return Math.round((calories / totalMacroCalories.value) * 100)
}

function buildFallbackReminder() {
  if (!totalMacroCalories.value) {
    return '今天的三大营养素数据还不完整，补充蛋白质、脂肪和碳水后再看占比。'
  }

  if (carbsPercent.value > 65) return `碳水占比偏高（${carbsPercent.value}%），高于推荐上限 65%，建议减少精制主食和含糖食物。`
  if (fatPercent.value > 35) return `脂肪占比偏高（${fatPercent.value}%），高于推荐上限 35%，建议减少油炸和高脂食物。`
  if (proteinPercent.value > 35) return `蛋白质占比偏高（${proteinPercent.value}%），高于推荐上限 35%，建议搭配足量蔬菜和主食。`
  if (carbsPercent.value < 45) return `碳水占比偏低（${carbsPercent.value}%），低于推荐下限 45%，可适量补充全谷物或薯类。`
  if (fatPercent.value < 20) return `脂肪占比偏低（${fatPercent.value}%），低于推荐下限 20%，可适量增加坚果或植物油。`
  if (proteinPercent.value < 10) return `蛋白质占比偏低（${proteinPercent.value}%），低于推荐下限 10%，可增加鸡蛋、鱼肉或豆制品。`

  return '今日三大营养素占比在推荐范围内，继续保持。'
}

function initChart() {
  if (!pieChartRef.value) return

  chart = echarts.init(pieChartRef.value)
  updateChart()
}

function updateChart() {
  if (!chart) return

  const data = hasMacroData.value
    ? [
        { name: '蛋白质', value: proteinPercent.value, itemStyle: { color: '#7EB8DA' } },
        { name: '碳水', value: carbsPercent.value, itemStyle: { color: '#feda6a' } },
        { name: '脂肪', value: fatPercent.value, itemStyle: { color: '#e8a18b' } }
      ]
    : [
        { name: '暂无记录', value: 1, itemStyle: { color: '#E8ECF1' } }
      ]

  chart.setOption({
    tooltip: {
      trigger: 'item',
      formatter: params => hasMacroData.value ? `${params.name}: ${params.value}%` : EMPTY_MESSAGE
    },
    series: [{
      type: 'pie',
      radius: ['60%', '85%'],
      center: ['50%', '50%'],
      silent: !hasMacroData.value,
      label: { show: false },
      labelLine: { show: false },
      data
    }]
  })
}

function handleResize() {
  chart?.resize()
}

onMounted(() => {
  nextTick(initChart)
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  chart?.dispose()
  chart = null
})

watch(() => props.data, () => {
  nextTick(updateChart)
}, { deep: true })
</script>

<style scoped>
.analysis-card {
  background: #FFFFFF;
  border-radius: 24px;
  padding: 28px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
  height: 100%;
  display: flex;
  flex-direction: column;
}

.card-header {
  margin-bottom: 24px;
}

.card-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
}

.chart-section {
  display: flex;
  align-items: center;
  gap: 24px;
  min-height: 160px;
}

.pie-chart {
  width: 140px;
  height: 140px;
  flex-shrink: 0;
}

.macro-summary {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.macro-legend {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--text-secondary);
}

.legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}

.legend-dot.protein {
  background: #7EB8DA;
}

.legend-dot.carbs {
  background: #feda6a;
}

.legend-dot.fat {
  background: #e8a18b;
}

.macro-message {
  margin: 0;
  max-width: 320px;
  font-size: 13px;
  line-height: 1.6;
  color: var(--text-secondary);
}

.macro-message.is-warning {
  color: #A75D3B;
}

.macro-message.is-balanced {
  color: #3F7D5A;
}

.macro-message.is-empty {
  color: var(--text-secondary);
}

@media (max-width: 640px) {
  .chart-section {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
