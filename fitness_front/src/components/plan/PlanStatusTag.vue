<template>
  <span class="status-tag" :class="statusClass">{{ statusText }}</span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  status: {
    type: String,
    default: ''
  }
})

const statusClass = computed(() => {
  const map = {
    '进行中': 'active',
    '已完成': 'completed',
    '草稿': 'draft',
    '已过期': 'expired'
  }
  return map[props.status] || 'draft'
})

const statusText = computed(() => {
  return props.status || '未知'
})
</script>

<style scoped>
.status-tag {
  display: inline-flex;
  align-items: center;
  padding: 4px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}

.status-tag.active {
  color: #7EB8DA;
  background: rgba(126, 184, 218, 0.1);
}

.status-tag.completed {
  color: #7BC6A0;
  background: rgba(123, 198, 160, 0.1);
}

.status-tag.draft {
  color: var(--text-secondary);
  background: #F8F6F3;
}

.status-tag.expired {
  color: #e8a18b;
  background: rgba(232, 161, 139, 0.1);
}
</style>
