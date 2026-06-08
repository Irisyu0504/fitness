<template>
  <div class="bmi-card">
    <div class="card-header">
      <h3>BMI 状态</h3>
    </div>

    <div v-if="hasBmi" class="bmi-content">
      <div class="bmi-summary">
        <div class="bmi-display">
          <div class="bmi-value-wrapper">
            <strong class="bmi-value">{{ bmiDisplay }}</strong>
            <span class="bmi-label">BMI</span>
          </div>
          <div class="bmi-status-badge" :class="statusClass">
            {{ statusText }}
          </div>
        </div>
      </div>

      <div class="bmi-detail">
        <div class="bmi-scale">
          <div class="scale-bar">
            <div class="scale-segment underweight" :class="{ active: statusClass === 'underweight' }"></div>
            <div class="scale-segment normal" :class="{ active: statusClass === 'normal' }"></div>
            <div class="scale-segment overweight" :class="{ active: statusClass === 'overweight' }"></div>
            <div class="scale-segment obese" :class="{ active: statusClass === 'obese' }"></div>
          </div>
        </div>

        <div class="bmi-boundary-grid">
          <div
            v-for="item in bmiCategories"
            :key="item.className"
            class="boundary-item"
            :class="[item.className, { active: statusClass === item.className }]"
          >
            <span>{{ item.label }}</span>
            <strong>{{ item.range }}</strong>
          </div>
        </div>

        <p class="bmi-advice">{{ advice }}</p>
      </div>
    </div>

    <div v-else class="empty-state">
      <p>暂无 BMI 数据</p>
      <span>添加体重记录后自动计算</span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  bmi: { type: [Number, String], default: null }
})

const bmiCategories = [
  { className: 'underweight', label: '偏瘦', range: '<18.5' },
  { className: 'normal', label: '正常', range: '18.5-23.9' },
  { className: 'overweight', label: '超重', range: '24.0-27.9' },
  { className: 'obese', label: '肥胖', range: '≥28.0' }
]

const bmiValue = computed(() => {
  const value = Number(props.bmi)
  return Number.isFinite(value) ? value : null
})

const hasBmi = computed(() => {
  return bmiValue.value !== null
})

const bmiDisplay = computed(() => {
  if (!hasBmi.value) return ''
  return bmiValue.value.toFixed(1)
})

const statusClass = computed(() => {
  if (!hasBmi.value) return ''
  if (bmiValue.value < 18.5) return 'underweight'
  if (bmiValue.value < 24) return 'normal'
  if (bmiValue.value < 28) return 'overweight'
  return 'obese'
})

const statusText = computed(() => {
  if (!hasBmi.value) return ''
  if (bmiValue.value < 18.5) return '偏瘦'
  if (bmiValue.value < 24) return '正常'
  if (bmiValue.value < 28) return '超重'
  return '肥胖'
})

const advice = computed(() => {
  if (!hasBmi.value) return ''
  if (bmiValue.value < 18.5) return '体重偏低，建议适当增加营养摄入和力量训练。'
  if (bmiValue.value < 24) return '保持当前训练和饮食节奏，继续稳定记录身体变化。'
  if (bmiValue.value < 28) return '体重略高，建议增加有氧运动频率，控制碳水摄入。'
  return '体重超标较多，建议咨询专业教练制定减脂计划。'
})
</script>

<style scoped>
.bmi-card {
  background: #FFFFFF;
  border-radius: 24px;
  padding: 28px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
}

.bmi-content {
  display: grid;
  grid-template-columns: minmax(180px, 0.8fr) minmax(0, 1.6fr);
  gap: 28px;
  align-items: center;
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

.bmi-display {
  display: flex;
  align-items: flex-start;
  flex-direction: column;
  gap: 18px;
}

.bmi-value-wrapper {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.bmi-value {
  font-size: 48px;
  font-weight: 800;
  color: var(--text-primary);
  line-height: 1;
}

.bmi-label {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-secondary);
}

.bmi-status-badge {
  padding: 8px 16px;
  border-radius: 999px;
  font-size: 14px;
  font-weight: 700;
}

.bmi-status-badge.underweight {
  color: #3B82F6;
  background: rgba(59, 130, 246, 0.1);
}

.bmi-status-badge.normal {
  color: #7BC6A0;
  background: rgba(123, 198, 160, 0.1);
}

.bmi-status-badge.overweight {
  color: #feda6a;
  background: rgba(254, 218, 106, 0.15);
}

.bmi-status-badge.obese {
  color: #e8a18b;
  background: rgba(232, 161, 139, 0.1);
}

.bmi-scale {
  margin-bottom: 14px;
}

.scale-bar {
  display: flex;
  height: 8px;
  border-radius: 999px;
  overflow: hidden;
  gap: 2px;
}

.scale-segment {
  flex: 1;
  border-radius: 999px;
  opacity: 0.3;
  transition: opacity 0.3s ease;
}

.scale-segment.active {
  opacity: 1;
}

.scale-segment.underweight {
  background: #3B82F6;
}

.scale-segment.normal {
  background: #7BC6A0;
}

.scale-segment.overweight {
  background: #feda6a;
}

.scale-segment.obese {
  background: #e8a18b;
}

.bmi-boundary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 16px;
}

.boundary-item {
  padding: 12px 14px;
  border: 1px solid #E8ECF1;
  border-radius: 14px;
  background: #FAFBFC;
  transition: border-color 0.2s ease, background 0.2s ease, transform 0.2s ease;
}

.boundary-item.active {
  transform: translateY(-1px);
  border-color: currentColor;
  background: #FFFFFF;
}

.boundary-item span {
  display: block;
  margin-bottom: 6px;
  font-size: 12px;
  font-weight: 700;
  color: var(--text-secondary);
}

.boundary-item strong {
  display: block;
  color: var(--text-primary);
  font-size: 15px;
  line-height: 1;
}

.boundary-item.underweight.active {
  color: #3B82F6;
}

.boundary-item.normal.active {
  color: #7BC6A0;
}

.boundary-item.overweight.active {
  color: #C9961A;
}

.boundary-item.obese.active {
  color: #e8a18b;
}

.bmi-advice {
  margin: 0;
  font-size: 13px;
  line-height: 1.6;
  color: var(--text-secondary);
  padding: 16px;
  background: #F8F6F3;
  border-radius: 16px;
}

.empty-state {
  text-align: center;
  padding: 20px 0;
  color: var(--text-secondary);
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

@media (max-width: 900px) {
  .bmi-content {
    grid-template-columns: 1fr;
  }

  .bmi-boundary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 520px) {
  .bmi-boundary-grid {
    grid-template-columns: 1fr;
  }
}
</style>
