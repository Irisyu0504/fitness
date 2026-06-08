<template>
  <div class="list-card">
    <div class="card-header">
      <h3>历史训练计划</h3>
      <span class="count">共 {{ plans.length }} 个</span>
    </div>

    <div v-if="plans.length > 0" class="plan-grid">
      <div v-for="plan in plans" :key="plan.id" class="plan-item">
        <div class="plan-header">
          <h4 class="plan-name">{{ plan.planName }}</h4>
          <PlanStatusTag :status="plan.status" />
        </div>

        <p class="plan-goal">{{ plan.planGoal }}</p>

        <div class="plan-meta">
          <span v-if="plan.frequency">{{ plan.frequency }}</span>
          <span v-if="plan.startDate && plan.endDate">{{ plan.startDate }} ~ {{ plan.endDate }}</span>
        </div>

        <div class="plan-actions">
          <button class="action-btn edit" @click="$emit('edit', plan)">编辑</button>
          <button
            v-if="plan.status !== '进行中'"
            class="action-btn activate"
            @click="$emit('activate', plan)"
          >
            设为当前
          </button>
          <button
            v-if="plan.status === '进行中'"
            class="action-btn complete"
            @click="$emit('complete', plan)"
          >
            结束计划
          </button>
          <button class="action-btn delete" @click="$emit('delete', plan.id)">删除</button>
        </div>
      </div>
    </div>

    <div v-else class="empty-state">
      <p>暂无历史训练计划</p>
      <span>创建第一个训练计划开始追踪</span>
    </div>
  </div>
</template>

<script setup>
import PlanStatusTag from './PlanStatusTag.vue'

defineProps({
  plans: {
    type: Array,
    default: () => []
  }
})

defineEmits(['edit', 'delete', 'activate', 'complete'])
</script>

<style scoped>
.list-card {
  background: #FFFFFF;
  border-radius: 24px;
  padding: 28px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.card-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
}

.count {
  font-size: 13px;
  color: var(--text-secondary);
}

.plan-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

.plan-item {
  padding: 20px;
  background: #F8F6F3;
  border-radius: 16px;
  transition: all 0.2s ease;
}

.plan-item:hover {
  background: #F5F7FA;
  transform: translateY(-2px);
}

.plan-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.plan-name {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
}

.plan-goal {
  margin: 0 0 12px;
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.5;
}

.plan-meta {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
  font-size: 12px;
  color: var(--text-secondary);
}

.plan-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.action-btn {
  padding: 6px 14px;
  border: none;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.action-btn.edit {
  color: #7EB8DA;
  background: rgba(126, 184, 218, 0.1);
}

.action-btn.edit:hover {
  background: rgba(126, 184, 218, 0.2);
}

.action-btn.activate {
  color: #7BC6A0;
  background: rgba(123, 198, 160, 0.1);
}

.action-btn.activate:hover {
  background: rgba(123, 198, 160, 0.2);
}

.action-btn.complete {
  color: #feda6a;
  background: rgba(254, 218, 106, 0.15);
}

.action-btn.complete:hover {
  background: rgba(254, 218, 106, 0.25);
}

.action-btn.delete {
  color: #e8a18b;
  background: rgba(232, 161, 139, 0.1);
}

.action-btn.delete:hover {
  background: rgba(232, 161, 139, 0.2);
}

.empty-state {
  text-align: center;
  padding: 60px 0;
  color: var(--text-secondary);
}

.empty-state p {
  margin: 0 0 8px;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.empty-state span {
  font-size: 14px;
}

@media (max-width: 768px) {
  .plan-grid {
    grid-template-columns: 1fr;
  }
}
</style>
