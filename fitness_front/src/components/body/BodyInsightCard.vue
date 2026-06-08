<template>
  <div class="insight-card">
    <div class="card-header">
      <h3>数据洞察</h3>
      <span class="insight-badge">AI</span>
    </div>

    <div v-if="insight" class="insight-content">
      <p>{{ insight }}</p>
    </div>

    <div v-else class="empty-state">
      <p>暂无洞察</p>
      <span>连续记录数据后生成分析</span>
    </div>
  </div>
</template>

<script setup>
defineProps({
  insight: {
    type: String,
    default: ''
  }
})
</script>

<style scoped>
.insight-card {
  background: #FFFFFF;
  border-radius: 24px;
  padding: 28px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.card-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
}

.insight-badge {
  padding: 4px 10px;
  font-size: 11px;
  font-weight: 700;
  color: var(--text-secondary);
  background: #F8F6F3;
  border-radius: 8px;
}

.insight-content p {
  margin: 0;
  font-size: 14px;
  line-height: 1.8;
  color: var(--text-primary);
}

.empty-state {
  text-align: center;
  padding: 12px 0;
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
</style>
