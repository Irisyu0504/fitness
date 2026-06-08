<template>
  <div class="gauge-card">
    <div class="card-header">
      <div>
        <h3>训练强度</h3>
        <p class="header-desc">今日 kcal/min 指数</p>
      </div>
      <span v-if="hasData" class="intensity-badge" :class="intensityClass">{{ intensityLabel }}</span>
    </div>

    <div v-if="hasData" ref="chartRef" class="chart-container"></div>
    <div v-else class="empty-state">
      <div class="empty-icon">
        <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="#A0AEC0" stroke-width="1.5">
          <circle cx="12" cy="12" r="10" />
          <path d="M12 6v6l4 2" />
        </svg>
      </div>
      <p>暂无数据</p>
      <span>添加训练记录后查看强度</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  intensity: {
    type: Number,
    default: 0
  }
})

const chartRef = ref(null)
let chart = null

const hasData = computed(() => props.intensity > 0)

const intensityLabel = computed(() => {
  if (props.intensity < 30) return '低强度'
  if (props.intensity < 70) return '中强度'
  return '高强度'
})

const intensityClass = computed(() => {
  if (props.intensity < 30) return 'low'
  if (props.intensity < 70) return 'medium'
  return 'high'
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
  if (!chart) return

  const value = Math.min(100, Math.max(0, props.intensity))

  chart.setOption({
    series: [{
      type: 'gauge',
      startAngle: 210,
      endAngle: -30,
      min: 0,
      max: 100,
      splitNumber: 5,
      radius: '90%',
      axisLine: {
        lineStyle: {
          width: 16,
          color: [
            [0.3, '#A8D8B9'],
            [0.7, '#7EB8DA'],
            [1, '#e8a18b']
          ]
        }
      },
      pointer: {
        icon: 'path://M12.8,0.7l12,40.1H0.7L12.8,0.7z',
        length: '55%',
        width: 7,
        offsetCenter: [0, '-40%'],
        itemStyle: {
          color: '#7EB8DA'
        }
      },
      axisTick: {
        length: 6,
        lineStyle: {
          color: 'auto',
          width: 1
        }
      },
      splitLine: {
        length: 14,
        lineStyle: {
          color: 'auto',
          width: 2
        }
      },
      axisLabel: {
        color: '#A0AEC0',
        fontSize: 10,
        distance: -36,
        formatter: function (value) {
          if (value === 0) return '低'
          if (value === 50) return '中'
          if (value === 100) return '高'
          return ''
        }
      },
      title: {
        show: false
      },
      detail: {
        fontSize: 26,
        fontWeight: 800,
        offsetCenter: [0, '15%'],
        valueAnimation: true,
        formatter: function (value) {
          return Math.round(value) + '%'
        },
        color: '#2D3748'
      },
      data: [{
        value: value
      }]
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

watch(() => props.intensity, () => {
  nextTick(() => {
    if (hasData.value) {
      if (!chart) initChart()
      else updateChart()
    }
  })
})
</script>

<style scoped>
.gauge-card {
  background: #FFFFFF;
  border-radius: 24px;
  padding: 28px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
}

.card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 8px;
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

.intensity-badge {
  padding: 4px 12px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 700;
}

.intensity-badge.low {
  color: #A8D8B9;
  background: rgba(168, 216, 185, 0.12);
}

.intensity-badge.medium {
  color: #7EB8DA;
  background: rgba(126, 184, 218, 0.12);
}

.intensity-badge.high {
  color: #e8a18b;
  background: rgba(232, 161, 139, 0.12);
}

.chart-container {
  width: 100%;
  height: 200px;
}

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
  margin: 0 0 8px;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.empty-state span {
  font-size: 12px;
}
</style>
