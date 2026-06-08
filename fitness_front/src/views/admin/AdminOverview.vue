<template>
  <div class="admin-overview">
    <h1 class="page-title">数据概览</h1>

    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon blue">
          <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg>
        </div>
        <div class="stat-info">
          <span class="stat-label">总用户数</span>
          <strong class="stat-value">{{ stats.totalUsers }}</strong>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon green">
          <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg>
        </div>
        <div class="stat-info">
          <span class="stat-label">VIP 用户</span>
          <strong class="stat-value">{{ stats.vipUsers }}</strong>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon purple">
          <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M6.5 6.5l11 11 M3 14l7 7 M14 3l7 7"/></svg>
        </div>
        <div class="stat-info">
          <span class="stat-label">动作总数</span>
          <strong class="stat-value">{{ stats.totalExercises }}</strong>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon orange">
          <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="8.5" cy="7" r="4"/><line x1="20" y1="8" x2="20" y2="14"/><line x1="23" y1="11" x2="17" y2="11"/></svg>
        </div>
        <div class="stat-info">
          <span class="stat-label">今日新增</span>
          <strong class="stat-value">{{ stats.todayNewUsers }}</strong>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import { request } from '@/api/request.js'

const stats = reactive({
  totalUsers: 0,
  vipUsers: 0,
  totalExercises: 0,
  todayNewUsers: 0
})

onMounted(async () => {
  try {
    const data = await request('/admin/stats')
    Object.assign(stats, data)
  } catch { /* ignore */ }
})
</script>

<style scoped>
.admin-overview {
  max-width: 1200px;
}

.page-title {
  margin: 0 0 28px;
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 24px;
  background: #FFFFFF;
  border: 1px solid #E8ECF1;
  border-radius: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.stat-icon {
  width: 48px;
  height: 48px;
  display: grid;
  place-items: center;
  border-radius: 14px;
  flex-shrink: 0;
}

.stat-icon.blue {
  background: rgba(126, 184, 218, 0.12);
  color: #7EB8DA;
}

.stat-icon.green {
  background: rgba(168, 216, 185, 0.15);
  color: #5a9e6f;
}

.stat-icon.purple {
  background: rgba(184, 169, 201, 0.15);
  color: #8B7BA5;
}

.stat-icon.orange {
  background: rgba(232, 161, 139, 0.15);
  color: #c67a5a;
}

.stat-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-secondary);
}

.stat-value {
  font-size: 28px;
  font-weight: 820;
  color: var(--text-primary);
  line-height: 1;
}
</style>
