<template>
  <div class="quick-wrap">
    <h3 class="quick-title">快捷入口</h3>

    <div class="actions-grid">
      <button v-for="a in actions" :key="a.label" class="action-item" @click="$router.push(a.to)">
        <div class="action-icon" :style="{ background: a.bg }">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" :stroke="a.color" stroke-width="2" stroke-linecap="round" v-html="a.svg"></svg>
        </div>
        <span class="action-label">{{ a.label }}</span>
      </button>
    </div>

    <div class="recent-section">
      <h4 class="recent-title">最近记录</h4>
      <div v-if="recent.length > 0" class="recent-list">
        <div v-for="r in recent" :key="r.id" class="recent-item">
          <span class="recent-name">{{ r.exerciseName || '训练' }}</span>
          <span class="recent-meta">{{ r.recordDate }} · {{ r.duration }}min</span>
        </div>
      </div>
      <div v-else class="recent-empty">暂无记录</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { request } from '@/api/request.js'
import { normalizeWorkoutDetails } from '@/api/normalizers.js'

const recent = ref([])

const actions = [
  { label: '训练记录', to: '/records', color: '#7EB8DA', bg: 'rgba(126,184,218,0.1)', svg: '<polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/>' },
  { label: '饮食记录', to: '/diets', color: '#A8D8B9', bg: 'rgba(168,216,185,0.1)', svg: '<path d="M18 8h1a4 4 0 0 1 0 8h-1"/><path d="M2 8h16v9a4 4 0 0 1-4 4H6a4 4 0 0 1-4-4V8z"/><line x1="6" y1="1" x2="6" y2="4"/><line x1="10" y1="1" x2="10" y2="4"/><line x1="14" y1="1" x2="14" y2="4"/>' },
  { label: '身体数据', to: '/body', color: '#B8A9C9', bg: 'rgba(184,169,201,0.1)', svg: '<path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>' },
  { label: '训练计划', to: '/plans', color: '#e8a18b', bg: 'rgba(232,161,139,0.1)', svg: '<rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/>' }
]

async function fetchRecent() {
  try {
    const res = await request('/workout-records/detail?page=1&size=4')
    recent.value = normalizeWorkoutDetails(res).slice(0, 4)
  } catch { /* ignore */ }
}

onMounted(fetchRecent)
</script>

<style scoped>
.quick-wrap {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.quick-title {
  margin: 0 0 16px;
  font-size: 16px;
  font-weight: 700;
  color: #2D3748;
}

.actions-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-bottom: 20px;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  background: #F8F9FA;
  border: 1px solid #E8ECF1;
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
  font: inherit;
  color: inherit;
}

.action-item:hover {
  border-color: #B8DDEF;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
}

.action-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: grid;
  place-items: center;
  flex-shrink: 0;
}

.action-label {
  font-size: 13px;
  font-weight: 600;
  color: #2D3748;
}

/* ── Recent ── */
.recent-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.recent-title {
  margin: 0 0 10px;
  font-size: 13px;
  font-weight: 600;
  color: #A0AEC0;
}

.recent-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
  overflow-y: auto;
}

.recent-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  background: #F8F9FA;
  border-radius: 10px;
}

.recent-name {
  font-size: 13px;
  font-weight: 600;
  color: #2D3748;
}

.recent-meta {
  font-size: 12px;
  color: #A0AEC0;
}

.recent-empty {
  flex: 1;
  display: grid;
  place-items: center;
  font-size: 13px;
  color: #A0AEC0;
}
</style>
