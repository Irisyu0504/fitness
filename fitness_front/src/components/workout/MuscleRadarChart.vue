<template>
  <div class="radar-card">
    <div class="card-header">
      <div>
        <h3>肌群覆盖雷达</h3>
        <p class="header-desc">训练均衡度分析</p>
      </div>
    </div>

    <div v-if="hasData" ref="chartRef" class="chart-container"></div>
    <div v-else class="empty-state">
      <div class="empty-icon">
        <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="#A0AEC0" stroke-width="1.5">
          <polygon points="12 2 22 8.5 22 15.5 12 22 2 15.5 2 8.5" />
          <line x1="12" y1="2" x2="12" y2="22" />
          <line x1="22" y1="8.5" x2="2" y2="15.5" />
          <line x1="2" y1="8.5" x2="22" y2="15.5" />
        </svg>
      </div>
      <p>暂无数据</p>
      <span>添加训练记录后查看覆盖情况</span>
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
  }
})

const chartRef = ref(null)
let chart = null

const muscles = ['胸部', '背部', '腿部', '核心', '有氧', '手臂', '肩部']

const hasData = computed(() => {
  return props.data && props.data.length > 0 && props.data.some(item => item.value > 0)
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

  const values = muscles.map(muscle => {
    const item = props.data.find(d => d.name === muscle)
    return item?.value || 0
  })

  const maxValue = Math.max(...values, 1)

  chart.setOption({
    radar: {
      indicator: muscles.map(name => ({
        name,
        max: maxValue * 1.2
      })),
      shape: 'polygon',
      splitNumber: 4,
      radius: '65%',
      axisName: {
        color: '#718096',
        fontSize: 12,
        fontWeight: 500
      },
      splitLine: {
        lineStyle: {
          color: '#F0F2F5'
        }
      },
      splitArea: {
        show: true,
        areaStyle: {
          color: ['#FFFFFF', '#F8F9FA']
        }
      },
      axisLine: {
        lineStyle: {
          color: '#E8ECF1'
        }
      }
    },
    tooltip: {
      trigger: 'item',
      backgroundColor: '#FFFFFF',
      borderColor: '#E8ECF1',
      textStyle: { color: '#2D3748', fontSize: 13 },
      extraCssText: 'box-shadow: 0 8px 30px rgba(0, 0, 0, 0.08); border-radius: 14px; padding: 14px 18px;',
      formatter: function (params) {
        const vals = params.value
        let html = `<div style="font-weight:700;margin-bottom:8px">肌群覆盖</div>`
        muscles.forEach((m, i) => {
          html += `<div style="display:flex;align-items:center;gap:8px;margin-bottom:4px">
            <span style="display:inline-block;width:6px;height:6px;border-radius:50%;background:#7EB8DA"></span>
            <span style="color:#718096;flex:1">${m}</span>
            <span style="font-weight:600;color:#2D3748">${vals[i]}</span>
          </div>`
        })
        return html
      }
    },
    series: [{
      type: 'radar',
      data: [{
        value: values,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(126, 184, 218, 0.3)' },
            { offset: 1, color: 'rgba(126, 184, 218, 0.05)' }
          ])
        },
        lineStyle: {
          color: '#7EB8DA',
          width: 2
        },
        itemStyle: {
          color: '#7EB8DA',
          borderColor: '#FFFFFF',
          borderWidth: 2
        },
        symbol: 'circle',
        symbolSize: 6
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

watch(() => props.data, () => {
  nextTick(() => {
    if (hasData.value) {
      if (!chart) initChart()
      else updateChart()
    }
  })
}, { deep: true })
</script>

<style scoped>
.radar-card {
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

.chart-container {
  width: 100%;
  height: 240px;
}

.empty-state {
  height: 240px;
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
