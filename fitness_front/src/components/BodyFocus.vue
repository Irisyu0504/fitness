<template>
  <section
    class="body-focus-container"
    aria-label="肌肉恢复状态轮播"
    @mouseenter="stop"
    @mouseleave="start"
  >
    <header class="focus-toolbar">
      <div class="focus-title-block">
        <span class="focus-tag">{{ currentGroup.tag }}</span>
        <h4>{{ currentGroup.title }}</h4>
      </div>

      <div class="focus-indicators" aria-label="肌肉分析分组">
        <button
          v-for="(group, index) in focusGroups"
          :key="group.id"
          type="button"
          class="focus-dot"
          :class="{ active: activeIndex === index }"
          :aria-label="`查看${group.title}`"
          :aria-pressed="activeIndex === index"
          @click="goTo(index)"
        ></button>
      </div>
    </header>

    <Transition name="focus-slide" mode="out-in">
      <div :key="currentGroup.id" class="focus-content">
        <div class="figure-wrapper">
          <svg viewBox="0 0 100 200" class="abstract-figure" aria-hidden="true">
            <circle cx="50" cy="25" r="14" class="body-part head" />
            <rect x="32" y="46" width="36" height="56" rx="12" class="body-part torso" />
            <rect x="74" y="46" width="14" height="48" rx="7" class="body-part arm" />
            <rect x="12" y="46" width="14" height="48" rx="7" class="body-part arm" />
            <rect x="52" y="108" width="16" height="56" rx="8" class="body-part leg" />
            <rect x="32" y="108" width="16" height="56" rx="8" class="body-part leg" />
          </svg>

          <div
            v-for="item in bodyStats"
            :key="`${currentGroup.id}-${item.part}`"
            class="glowing-dot"
            :style="{ top: item.y, left: item.x, '--part-color': item.color }"
          >
            <div class="dot-core"></div>
            <div class="dot-pulse"></div>
          </div>
        </div>

        <div class="data-list">
          <article
            v-for="item in bodyStats"
            :key="`${currentGroup.id}-data-${item.part}`"
            class="data-item"
          >
            <div class="item-header">
              <span class="mini-dot" :style="{ background: item.color }"></span>
              <span class="part-name">{{ item.part }}</span>
            </div>
            <div class="item-details">
              <span class="status-text" :class="`${item.status}-text`">
                {{ getStatusLabel(item.status) }}
              </span>
              <span class="time-text">{{ item.lastTrained }}</span>
            </div>
          </article>
        </div>
      </div>
    </Transition>
  </section>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'

const props = defineProps({
  records: { type: Array, default: () => [] }
})

const layoutConfig = {
  upper: [
    { part: '胸肩区域', muscles: ['胸部', '肩部'], x: '50%', y: '36%', color: '#6C8EBF' },
    { part: '右臂链路', muscles: ['手臂'], x: '81%', y: '38%', color: '#82B366' },
    { part: '左臂链路', muscles: ['手臂'], x: '19%', y: '38%', color: '#D6A756' },
    { part: '上背部', muscles: ['背部'], x: '50%', y: '28%', color: '#B07BAC' }
  ],
  core: [
    { part: '深层核心', muscles: ['核心'], x: '50%', y: '49%', color: '#6C8EBF' },
    { part: '腹斜肌', muscles: ['核心'], x: '64%', y: '48%', color: '#82B366' },
    { part: '下背部', muscles: ['核心', '背部'], x: '42%', y: '55%', color: '#D6A756' },
    { part: '呼吸节奏', muscles: ['核心'], x: '50%', y: '40%', color: '#B07BAC' }
  ],
  lower: [
    { part: '臀部肌群', muscles: ['腿部'], x: '50%', y: '62%', color: '#6C8EBF' },
    { part: '右侧股四头肌', muscles: ['腿部'], x: '61%', y: '72%', color: '#82B366' },
    { part: '左侧股四头肌', muscles: ['腿部'], x: '39%', y: '72%', color: '#D6A756' },
    { part: '小腿肌群', muscles: ['腿部', '有氧'], x: '55%', y: '84%', color: '#B07BAC' }
  ]
}

const muscleLastTrained = computed(() => {
  const map = {}
  const today = new Date().toISOString().split('T')[0]
  props.records.forEach(r => {
    if (!r.muscleGroup || !r.recordDate) return
    if (!map[r.muscleGroup] || r.recordDate > map[r.muscleGroup]) {
      map[r.muscleGroup] = r.recordDate
    }
  })
  const result = {}
  for (const [muscle, dateStr] of Object.entries(map)) {
    if (dateStr === today) {
      result[muscle] = { days: 0, label: '今日' }
    } else {
      const diff = Math.floor((new Date(today) - new Date(dateStr)) / 86400000)
      result[muscle] = { days: diff, label: diff === 1 ? '昨日' : `${diff} 天前` }
    }
  }
  return result
})

function getStatus(days) {
  if (days <= 0) return 'exhausted'
  if (days <= 2) return 'recovering'
  return 'fresh'
}

function getPartStatus(muscles) {
  let minDays = 999
  let bestLabel = '未训练'
  for (const m of muscles) {
    const info = muscleLastTrained.value[m]
    if (info && info.days < minDays) {
      minDays = info.days
      bestLabel = info.label
    }
  }
  return { status: minDays === 999 ? 'fresh' : getStatus(minDays), lastTrained: bestLabel }
}

const focusGroups = computed(() => {
  const titleMap = { upper: '上肢恢复', core: '核心状态', lower: '下肢状态' }
  const tagMap = { upper: '恢复负荷', core: '控制评分', lower: '力量储备' }
  return ['upper', 'core', 'lower'].map(groupId => ({
    id: groupId,
    title: titleMap[groupId],
    tag: tagMap[groupId],
    stats: layoutConfig[groupId].map(item => {
      const { status, lastTrained } = getPartStatus(item.muscles)
      return { part: item.part, status, lastTrained, x: item.x, y: item.y, color: item.color }
    })
  }))
})

const activeIndex = ref(0)
let timer = null

const currentGroup = computed(() => {
  return focusGroups.value[activeIndex.value] || focusGroups.value[0]
})

const bodyStats = computed(() => currentGroup.value.stats)

function getStatusLabel(status) {
  return { exhausted: '疲劳', recovering: '恢复中', fresh: '可训练' }[status] || status
}

function goTo(index) {
  activeIndex.value = index
  restart()
}

function next() {
  activeIndex.value = (activeIndex.value + 1) % focusGroups.value.length
}

function start() {
  stop()
  timer = setInterval(next, 4000)
}

function stop() {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
}

function restart() {
  stop()
  start()
}

onMounted(start)
onBeforeUnmount(stop)
</script>

<style scoped>
.body-focus-container {
  position: relative;
  width: 100%;
  min-height: 0;
  overflow: hidden;
}

.focus-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.focus-title-block {
  min-width: 0;
}

.focus-tag {
  display: inline-block;
  margin-bottom: 4px;
  color: var(--text-secondary);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.focus-title-block h4 {
  margin: 0;
  color: var(--text-primary);
  font-size: 15px;
  font-weight: 800;
  line-height: 1.15;
}

.focus-indicators {
  display: flex;
  gap: 6px;
}

.focus-dot {
  width: 8px;
  height: 8px;
  padding: 0;
  background: #E8ECF1;
  border: 0;
  border-radius: 999px;
  cursor: pointer;
  transition: width 0.25s ease, background 0.25s ease;
}

.focus-dot.active {
  width: 22px;
  background: #7EB8DA;
}

.focus-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  align-items: start;
}

.figure-wrapper {
  position: relative;
  width: 100%;
  max-width: 110px;
  margin: 0 auto;
}

.abstract-figure {
  width: 100%;
  height: auto;
}

.body-part {
  fill: #F0F2F5;
  stroke: #E8ECF1;
  stroke-width: 0.5;
}

.glowing-dot {
  position: absolute;
  width: 10px;
  height: 10px;
  transform: translate(-50%, -50%);
  pointer-events: none;
}

.dot-core {
  position: absolute;
  inset: 2px;
  border-radius: 50%;
}

.dot-pulse {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  animation: pulse 2s ease-in-out infinite;
}

.glowing-dot .dot-core {
  background: var(--part-color, #7EB8DA);
}

.glowing-dot .dot-pulse {
  background: color-mix(in srgb, var(--part-color, #7EB8DA) 30%, transparent);
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(2.2);
    opacity: 0;
  }
}

.data-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.data-item {
  padding: 8px 10px;
  background: #F8F9FA;
  border-radius: 10px;
}

.item-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 4px;
}

.mini-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  flex-shrink: 0;
}

.mini-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  flex-shrink: 0;
}

.part-name {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-primary);
}

.item-details {
  display: flex;
  justify-content: space-between;
  gap: 8px;
}

.status-text {
  font-size: 11px;
  font-weight: 600;
}

.status-text.exhausted-text {
  color: #e8a18b;
}

.status-text.recovering-text {
  color: #d4a017;
}

.status-text.fresh-text {
  color: #A8D8B9;
}

.time-text {
  font-size: 11px;
  color: var(--text-tertiary);
}

.focus-slide-enter-active,
.focus-slide-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.focus-slide-enter-from {
  opacity: 0;
  transform: translateX(16px);
}

.focus-slide-leave-to {
  opacity: 0;
  transform: translateX(-16px);
}

@media (max-width: 480px) {
  .focus-content {
    grid-template-columns: 1fr;
  }
}
</style>
