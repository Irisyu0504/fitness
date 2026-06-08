<template>
  <div class="history-card">
    <div class="card-header">
      <h3>历史饮食记录</h3>
      <div class="header-actions">
        <el-input
          v-model="searchFood"
          placeholder="搜索食物"
          clearable
          style="width: 150px"
          @clear="$emit('search')"
          @keyup.enter="$emit('search')"
        />
        <el-select v-model="filterMealType" placeholder="餐次" clearable style="width: 100px" @change="$emit('search')">
          <el-option label="早餐" value="早餐" />
          <el-option label="午餐" value="午餐" />
          <el-option label="晚餐" value="晚餐" />
          <el-option label="加餐" value="加餐" />
          <el-option label="夜宵" value="夜宵" />
        </el-select>
        <button class="search-btn" @click="$emit('search')">查询</button>
      </div>
    </div>

    <div v-if="records.length > 0" class="table-wrapper">
      <el-table :data="records" stripe style="width: 100%">
        <el-table-column label="摄入时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.mealTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="foodName" label="食物名称" width="140" />
        <el-table-column label="餐次" width="80">
          <template #default="{ row }">
            <span class="meal-tag" :class="getMealClass(row.mealType)">{{ row.mealType }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="calories" label="热量" width="80" />
        <el-table-column prop="protein" label="蛋白质" width="80" />
        <el-table-column prop="carbs" label="碳水" width="70" />
        <el-table-column prop="fat" label="脂肪" width="70" />
        <el-table-column prop="remark" label="备注" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <button class="action-btn edit" @click="$emit('edit', row)">编辑</button>
            <button class="action-btn delete" @click="$emit('delete', row.id)">删除</button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="currentSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next"
          @current-change="$emit('page-change', { page: currentPage, size: currentSize })"
          @size-change="$emit('page-change', { page: currentPage, size: currentSize })"
        />
      </div>
    </div>

    <div v-else class="empty-state">
      <p>暂无饮食记录</p>
      <span>添加第一条记录开始追踪</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  records: { type: Array, default: () => [] },
  total: { type: Number, default: 0 },
  page: { type: Number, default: 1 },
  size: { type: Number, default: 10 }
})

const emit = defineEmits(['edit', 'delete', 'search', 'page-change'])

const searchFood = ref('')
const filterMealType = ref('')
const currentPage = computed({
  get: () => props.page,
  set: (val) => emit('page-change', { page: val, size: currentSize.value })
})
const currentSize = computed({
  get: () => props.size,
  set: (val) => emit('page-change', { page: currentPage.value, size: val })
})

// 暴露搜索条件给父组件
defineExpose({
  getFilters: () => ({
    foodName: searchFood.value,
    mealType: filterMealType.value
  })
})

function formatTime(mealTime) {
  if (!mealTime) return ''
  const date = new Date(mealTime)
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

function getMealClass(type) {
  const map = {
    '早餐': 'breakfast',
    '午餐': 'lunch',
    '晚餐': 'dinner',
    '加餐': 'snack',
    '夜宵': 'late'
  }
  return map[type] || ''
}
</script>

<style scoped>
.history-card {
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
  flex-wrap: wrap;
  gap: 16px;
}

.card-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.header-actions :deep(.el-input__wrapper) {
  border-radius: 12px;
  background: #F8F6F3;
  border: 1px solid #E8ECF1;
  box-shadow: none;
}

.header-actions :deep(.el-select .el-input__wrapper) {
  border-radius: 12px;
  background: #F8F6F3;
  border: 1px solid #E8ECF1;
  box-shadow: none;
}

.search-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 12px;
  background: #7EB8DA;
  color: #FFFFFF;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.search-btn:hover {
  background: #6AABC8;
}

.table-wrapper {
  overflow: hidden;
  border-radius: 16px;
}

.table-wrapper :deep(.el-table) {
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: #F8F6F3;
  --el-table-row-hover-bg-color: #F5F7FA;
  --el-table-border-color: #E8ECF1;
  --el-table-text-color: var(--text-primary);
  --el-table-header-text-color: var(--text-secondary);
  font-size: 13px;
}

.table-wrapper :deep(.el-table th.el-table__cell) {
  font-weight: 600;
  font-size: 12px;
}

.table-wrapper :deep(.el-table td.el-table__cell) {
  padding: 12px 0;
  border-bottom: 1px solid #E8ECF1;
}

.meal-tag {
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 600;
}

.meal-tag.breakfast {
  color: #feda6a;
  background: rgba(254, 218, 106, 0.15);
}

.meal-tag.lunch {
  color: #7EB8DA;
  background: rgba(126, 184, 218, 0.1);
}

.meal-tag.dinner {
  color: #B8A9C9;
  background: rgba(184, 169, 201, 0.15);
}

.meal-tag.snack {
  color: #7BC6A0;
  background: rgba(123, 198, 160, 0.1);
}

.meal-tag.late {
  color: #e8a18b;
  background: rgba(232, 161, 139, 0.1);
}

.action-btn {
  padding: 5px 12px;
  border: none;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  margin-right: 6px;
}

.action-btn.edit {
  color: #7EB8DA;
  background: rgba(126, 184, 218, 0.1);
}

.action-btn.edit:hover {
  background: rgba(126, 184, 218, 0.2);
}

.action-btn.delete {
  color: #e8a18b;
  background: rgba(232, 161, 139, 0.1);
}

.action-btn.delete:hover {
  background: rgba(232, 161, 139, 0.2);
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.pagination-wrapper :deep(.el-pagination) {
  --el-pagination-bg-color: transparent;
  --el-pagination-button-bg-color: #F8F6F3;
  --el-pagination-button-color: var(--text-secondary);
  --el-pagination-hover-color: #7EB8DA;
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
</style>
