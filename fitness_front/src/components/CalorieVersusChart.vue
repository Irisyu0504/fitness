<template>
  <div class="versus-wrap">
    <div ref="chartRef" class="versus-chart"></div>
    <div class="net-badge" :class="netClass">
      <span class="net-label">净热量</span>
      <strong class="net-value">{{ Math.abs(net) }}</strong>
      <span class="net-unit">kcal {{ net >= 0 ? '盈余' : '缺口' }}</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  workout: { type: Number, default: 0 },
  intake: { type: Number, default: 0 }
})

const chartRef = ref(null)
let chart = null

const net = computed(() => props.intake - props.workout)
const netClass = computed(() => net.value >= 0 ? 'surplus' : 'deficit')

function initChart() {
  if (!chartRef.value) return
  chart = echarts.init(chartRef.value)
  renderChart()
  window.addEventListener('resize', handleResize)
}

function handleResize() { chart?.resize() }

function renderChart() {
  if (!chart) return
  const maxVal = Math.max(props.workout, props.intake, 100)

  chart.setOption({
    grid: { top: 8, right: 70, bottom: 8, left: 60, containLabel: false },
    xAxis: { type: 'value', max: maxVal * 1.2, show: false },
    yAxis: {
      type: 'category',
      data: ['摄入', '消耗'],
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: '#718096', fontSize: 13, fontWeight: 600, margin: 12 }
    },
    series: [{
      type: 'bar',
      data: [
        {
          value: props.intake,
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
              { offset: 0, color: 'rgba(240,168,168,0.2)' },
              { offset: 1, color: '#F0A8A8' }
            ]),
            borderRadius: [0, 6, 6, 0]
          }
        },
        {
          value: props.workout,
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
              { offset: 0, color: 'rgba(126,184,218,0.2)' },
              { offset: 1, color: '#7EB8DA' }
            ]),
            borderRadius: [0, 6, 6, 0]
          }
        }
      ],
      barWidth: 16,
      label: {
        show: true, position: 'right',
        color: '#718096', fontSize: 13, fontWeight: 700,
        formatter: '{c} kcal'
      }
    }]
  }, true)
}

onMounted(() => { nextTick(initChart) })
onBeforeUnmount(() => { window.removeEventListener('resize', handleResize); chart?.dispose() })
watch(() => [props.workout, props.intake], () => { nextTick(renderChart) })
</script>

<style scoped>
.versus-wrap {
  display: flex;
  align-items: center;
  gap: 20px;
  height: 100%;
}

.versus-chart {
  flex: 1;
  min-width: 0;
  height: 100%;
  min-height: 80px;
}

.net-badge {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  padding: 12px 18px;
  border-radius: 16px;
  min-width: 90px;
}

.net-badge.surplus {
  background: rgba(240, 168, 168, 0.08);
}

.net-badge.deficit {
  background: rgba(126, 184, 218, 0.08);
}

.net-label {
  font-size: 11px;
  font-weight: 600;
  color: #A0AEC0;
}

.net-value {
  font-size: 26px;
  font-weight: 800;
  line-height: 1;
}

.net-badge.surplus .net-value { color: #F0A8A8; }
.net-badge.deficit .net-value { color: #7EB8DA; }

.net-unit {
  font-size: 11px;
  color: #A0AEC0;
  font-weight: 500;
}
</style>
