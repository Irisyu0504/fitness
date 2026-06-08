<template>
  <div class="overview-row">
    <div v-for="(card, i) in cards" :key="i" class="overview-card">
      <div class="card-orb" :class="card.orb"></div>
      <span class="card-label">{{ card.label }}</span>
      <div class="card-value-row">
        <strong class="card-value">{{ card.value }}</strong>
        <span class="card-unit">{{ card.unit }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  data: { type: Object, default: () => ({}) }
})

const cards = computed(() => [
  {
    label: '今日训练时长',
    value: props.data.todayDuration || 0,
    unit: 'min',
    orb: 'cyan'
  },
  {
    label: '今日消耗热量',
    value: props.data.todayCalories || 0,
    unit: 'kcal',
    orb: 'rose'
  },
  {
    label: '本周训练次数',
    value: props.data.weeklyCount || 0,
    unit: '次',
    orb: 'green'
  },
  {
    label: '本周总消耗',
    value: props.data.weeklyCalories || 0,
    unit: 'kcal',
    orb: 'yellow'
  }
])
</script>

<style scoped>
.overview-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.overview-card {
  position: relative;
  background: #FFFFFF;
  border: 1px solid #E8ECF1;
  border-radius: 20px;
  padding: 22px 24px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06), 0 1px 3px rgba(0, 0, 0, 0.04);
  overflow: hidden;
  transition: transform 0.25s cubic-bezier(0.22, 1, 0.36, 1), box-shadow 0.25s ease;
}

.overview-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
}

.card-orb {
  position: absolute;
  top: -10px;
  right: -10px;
  width: 52px;
  height: 52px;
  border-radius: 50%;
  filter: blur(14px);
  opacity: 0.45;
  pointer-events: none;
}

.card-orb.cyan  { background: #7EB8DA; }
.card-orb.rose  { background: #F0A8A8; }
.card-orb.green { background: #A8D8B9; }
.card-orb.yellow{ background: #F5D5A0; }

.card-label {
  display: block;
  font-size: 12px;
  color: #718096;
  font-weight: 600;
  margin-bottom: 10px;
  letter-spacing: 0.02em;
}

.card-value-row {
  display: flex;
  align-items: baseline;
  gap: 6px;
}

.card-value {
  font-size: 28px;
  font-weight: 800;
  color: #2D3748;
  line-height: 1;
}

.card-unit {
  font-size: 13px;
  color: #A0AEC0;
  font-weight: 500;
}

@media (max-width: 900px) {
  .overview-row {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 480px) {
  .overview-row {
    grid-template-columns: 1fr;
  }
}
</style>
