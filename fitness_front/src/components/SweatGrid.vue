<template>
  <section class="sweat-grid-wrapper" aria-label="月度训练日历">
    <header class="calendar-header">
      <h4>您的训练日历</h4>
      <div class="month-nav">
        <button class="month-arrow" type="button" aria-label="上一月" @click="prevMonth">
          <svg viewBox="0 0 12 12" aria-hidden="true">
            <path d="M8 3L4 6l4 3" />
          </svg>
        </button>
        <span class="month-label">{{ monthLabel }}</span>
        <button class="month-arrow" type="button" aria-label="下一月" @click="nextMonth">
          <svg viewBox="0 0 12 12" aria-hidden="true">
            <path d="M4 3l4 3-4 3" />
          </svg>
        </button>
      </div>
    </header>

    <div class="week-row" aria-hidden="true">
      <span v-for="(day, index) in weekDays" :key="`${day}-${index}`">{{ day }}</span>
    </div>

    <div class="calendar-grid">
      <div
        v-for="day in calendarDays"
        :key="day.key"
        class="calendar-cell"
        :class="{ 'is-empty': day.isEmpty }"
      >
        <span
          v-if="!day.isEmpty"
          class="day-status"
          :class="[day.status, { current: day.isCurrentDay }]"
          :title="day.label"
        >
          {{ day.date }}
        </span>
      </div>
    </div>

    <footer class="calendar-legend" aria-label="日历图例">
      <span class="legend-item">
        <i class="legend-dot current"></i>
        今日
      </span>
      <span class="legend-item">
        <i class="legend-dot done"></i>
        已完成
      </span>
    </footer>
  </section>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  doneDates: { type: Set, default: () => new Set() }
})

const weekDays = ['一', '二', '三', '四', '五', '六', '日']

const now = new Date()
const viewYear = ref(now.getFullYear())
const viewMonth = ref(now.getMonth()) // 0-indexed

const monthLabel = computed(() => {
  const d = new Date(viewYear.value, viewMonth.value, 1)
  return d.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long' })
})

const isCurrentMonth = computed(() => {
  return viewYear.value === now.getFullYear() && viewMonth.value === now.getMonth()
})

function prevMonth() {
  if (viewMonth.value === 0) {
    viewMonth.value = 11
    viewYear.value--
  } else {
    viewMonth.value--
  }
}

function nextMonth() {
  if (viewMonth.value === 11) {
    viewMonth.value = 0
    viewYear.value++
  } else {
    viewMonth.value++
  }
}

function pad(n) {
  return String(n).padStart(2, '0')
}

const calendarDays = computed(() => {
  const firstDay = new Date(viewYear.value, viewMonth.value, 1)
  const monthLength = new Date(viewYear.value, viewMonth.value + 1, 0).getDate()
  const leadingEmpty = (firstDay.getDay() + 6) % 7 // Monday-first
  const done = props.doneDates
  const days = []

  for (let i = 0; i < leadingEmpty; i++) {
    days.push({ key: `empty-${i}`, isEmpty: true })
  }

  for (let d = 1; d <= monthLength; d++) {
    const dateStr = `${viewYear.value}-${pad(viewMonth.value + 1)}-${pad(d)}`
    const isToday = isCurrentMonth.value && d === now.getDate()
    const status = done.has(dateStr) ? 'done' : 'idle'

    days.push({
      key: dateStr,
      date: d,
      status,
      isCurrentDay: isToday,
      isEmpty: false,
      label: `${monthLabel.value} ${d}日`
    })
  }

  return days
})
</script>

<style scoped>
.sweat-grid-wrapper {
  width: 100%;
  box-sizing: border-box;
  padding: 16px;
  color: var(--text-primary);
  background: transparent;
  border: none;
  border-radius: 24px;
}

.calendar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  margin-bottom: 12px;
}

.calendar-header h4 {
  margin: 0;
  color: var(--text-primary);
  font-size: 15px;
  font-weight: 800;
  line-height: 1.15;
}

.month-nav {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.month-arrow {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  padding: 0;
  color: var(--text-secondary);
  background: transparent;
  border: 1px solid #E8ECF1;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.15s ease, color 0.15s ease;
}

.month-arrow:hover {
  background: #F5F7FA;
  color: var(--text-primary);
}

.month-arrow svg {
  width: 10px;
  height: 10px;
  fill: none;
  stroke: currentColor;
  stroke-width: 1.8;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.month-label {
  min-width: 80px;
  text-align: center;
  color: var(--text-secondary);
  font-size: 12px;
  font-weight: 700;
  white-space: nowrap;
}

.week-row,
.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
}

.week-row {
  margin-bottom: 8px;
}

.week-row span {
  color: var(--text-secondary);
  font-size: 10px;
  font-weight: 700;
  line-height: 1;
  text-align: center;
}

.calendar-grid {
  row-gap: 5px;
  column-gap: 4px;
}

.calendar-cell {
  display: grid;
  min-height: 22px;
  place-items: center;
}

.calendar-cell.is-empty {
  pointer-events: none;
}

.day-status {
  display: grid;
  width: 22px;
  height: 22px;
  box-sizing: border-box;
  place-items: center;
  color: #718096;
  font-size: 10px;
  font-weight: 700;
  line-height: 1;
  border: 1px solid #E8ECF1;
  border-radius: 50%;
  background: #FAFBFC;
  transition:
    transform 0.22s ease,
    background 0.22s ease,
    border-color 0.22s ease,
    color 0.22s ease;
}

.day-status:hover {
  transform: translateY(-1px);
}

.day-status.done {
  color: #FFFFFF;
  background: #7EB8DA;
  border-radius: 50%;
  box-shadow: 0 2px 8px rgba(126, 184, 218, 0.3);
}

.day-status.current {
  border: 2px solid #7EB8DA;
  box-shadow: 0 0 0 3px rgba(126, 184, 218, 0.12);
}

.calendar-legend {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  margin-top: 12px;
  color: var(--text-secondary);
  font-size: 9px;
  font-weight: 700;
  line-height: 1;
}

.legend-item {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  white-space: nowrap;
}

.legend-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  box-sizing: border-box;
  border-radius: 50%;
}

.legend-dot.current {
  background: transparent;
  border: 2px solid #7EB8DA;
}

.legend-dot.done {
  background: #7EB8DA;
}

@media (max-width: 420px) {
  .sweat-grid-wrapper {
    padding: 20px;
  }

  .calendar-header h4 {
    font-size: 14px;
  }

  .day-status {
    width: 26px;
    height: 26px;
  }
}
</style>
