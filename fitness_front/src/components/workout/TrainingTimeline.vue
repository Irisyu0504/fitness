<template>
  <div class="timeline-card">
    <div class="card-header">
      <div>
        <h3>今日训练时间线</h3>
        <p class="header-desc">{{ records.length > 0 ? `共 ${records.length} 条记录` : '记录今日训练' }}</p>
      </div>
    </div>

    <div v-if="records.length > 0" class="timeline-list">
      <div v-for="(record, index) in records" :key="record.id" class="timeline-item" :style="{ animationDelay: index * 60 + 'ms' }">
        <div class="timeline-line">
          <div class="timeline-dot"></div>
          <div v-if="index < records.length - 1" class="timeline-connector"></div>
        </div>
        <div class="record-card">
          <div class="record-header">
            <span class="record-time">{{ formatTime(record.createTime) }}</span>
            <span class="record-exercise">{{ record.exerciseName || '训练' }}</span>
          </div>
          <div class="record-info">
            <span class="record-duration">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
              {{ record.duration }} 分钟
            </span>
            <span v-if="record.caloriesBurned" class="record-calories">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2C6.5 2 2 6.5 2 12s4.5 10 10 10 10-4.5 10-10S17.5 2 12 2z"/><path d="M12 6v6l4 2"/></svg>
              {{ record.caloriesBurned }} kcal
            </span>
          </div>
          <div v-if="record.setsCount || record.reps" class="record-sets">
            <span v-if="record.setsCount">{{ record.setsCount }} 组</span>
            <span v-if="record.reps">× {{ record.reps }} 次</span>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="empty-state">
      <div class="empty-icon">
        <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="#A0AEC0" stroke-width="1.5">
          <circle cx="12" cy="12" r="10" />
          <polyline points="12 6 12 12 16 14" />
        </svg>
      </div>
      <p>今日还没有训练记录</p>
      <span>添加第一条记录开始追踪</span>
    </div>
  </div>
</template>

<script setup>
defineProps({
  records: {
    type: Array,
    default: () => []
  }
})

function formatTime(createTime) {
  if (!createTime) return ''
  const date = new Date(createTime)
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}
</script>

<style scoped>
.timeline-card {
  background: #FFFFFF;
  border-radius: 24px;
  padding: 28px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
  max-height: 420px;
  overflow-y: auto;
}

.timeline-card::-webkit-scrollbar {
  width: 4px;
}

.timeline-card::-webkit-scrollbar-track {
  background: transparent;
}

.timeline-card::-webkit-scrollbar-thumb {
  background: #E8ECF1;
  border-radius: 999px;
}

.card-header {
  margin-bottom: 20px;
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

.timeline-list {
  display: flex;
  flex-direction: column;
}

.timeline-item {
  display: flex;
  gap: 14px;
  align-items: flex-start;
  animation: fadeInUp 0.3s ease forwards;
  opacity: 0;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.timeline-line {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex-shrink: 0;
  width: 14px;
}

.timeline-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #7EB8DA;
  box-shadow: 0 0 0 3px rgba(126, 184, 218, 0.15);
  flex-shrink: 0;
  margin-top: 16px;
}

.timeline-connector {
  width: 2px;
  flex: 1;
  min-height: 20px;
  background: #E8ECF1;
  margin-top: 4px;
}

.record-card {
  flex: 1;
  padding: 14px 18px;
  background: #F8F9FA;
  border-radius: 16px;
  margin-bottom: 12px;
  transition: all 0.2s ease;
  border: 1px solid transparent;
}

.record-card:hover {
  transform: translateY(-2px);
  background: #FFFFFF;
  border-color: #E8ECF1;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
}

.record-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.record-time {
  font-size: 12px;
  color: var(--text-tertiary);
  font-weight: 500;
}

.record-exercise {
  font-size: 14px;
  font-weight: 700;
  color: var(--text-primary);
}

.record-info {
  display: flex;
  gap: 16px;
  margin-bottom: 4px;
}

.record-duration,
.record-calories {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  color: var(--text-secondary);
}

.record-sets {
  display: flex;
  gap: 8px;
  font-size: 12px;
  color: #7EB8DA;
  font-weight: 600;
  margin-top: 4px;
}

.empty-state {
  text-align: center;
  padding: 40px 0;
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
