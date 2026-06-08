<template>
  <div class="table-card">
    <div class="table-header">
      <h3 class="table-title">训练记录</h3>
      <span class="table-count">共 {{ total }} 条</span>
    </div>

    <div v-if="records.length > 0" class="table-wrap">
      <el-table :data="records" stripe style="width: 100%">
        <el-table-column prop="recordDate" label="日期" width="110" />
        <el-table-column label="计划" min-width="100">
          <template #default="{ row }">
            <span class="text-cell">{{ row.planName || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="运动" min-width="110">
          <template #default="{ row }">
            <span class="text-cell bold">{{ row.exerciseName || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="时长" width="80">
          <template #default="{ row }">
            <span class="num-cell">{{ row.duration || '—' }} <small>min</small></span>
          </template>
        </el-table-column>
        <el-table-column label="组数" width="65">
          <template #default="{ row }">
            <span class="num-cell">{{ row.setsCount ?? '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="次数" width="65">
          <template #default="{ row }">
            <span class="num-cell">{{ row.reps ?? '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="消耗" width="90">
          <template #default="{ row }">
            <span class="cal-cell">{{ row.caloriesBurned ?? '—' }} <small>kcal</small></span>
          </template>
        </el-table-column>
        <el-table-column label="备注" min-width="100">
          <template #default="{ row }">
            <span class="text-cell dim">{{ row.remark || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <button class="act-btn edit" @click="$emit('edit', row)">编辑</button>
            <button class="act-btn del" @click="$emit('delete', row.id)">删除</button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager-wrap">
        <el-pagination
          :current-page="page"
          :page-size="size"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next"
          @current-change="handleCurrentChange"
          @size-change="handleSizeChange"
        />
      </div>
    </div>

    <div v-else class="empty-state">
      <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="#A0AEC0" stroke-width="1.5">
        <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
        <polyline points="14 2 14 8 20 8" />
      </svg>
      <p>暂无训练记录</p>
      <span>添加第一条记录开始追踪</span>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  records: { type: Array, default: () => [] },
  total: { type: Number, default: 0 },
  page: { type: Number, default: 1 },
  size: { type: Number, default: 10 }
})

const emit = defineEmits(['edit', 'delete', 'page-change'])

function handleCurrentChange(nextPage) {
  emit('page-change', { page: Number(nextPage) || 1, size: props.size })
}

function handleSizeChange(nextSize) {
  emit('page-change', { page: 1, size: Number(nextSize) || props.size })
}
</script>

<style scoped>
.table-card {
  background: #FFFFFF;
  border: 1px solid #E8ECF1;
  border-radius: 24px;
  padding: 28px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06), 0 1px 3px rgba(0, 0, 0, 0.04);
}

.table-header {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 20px;
}

.table-title {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
  color: #2D3748;
}

.table-count {
  font-size: 12px;
  color: #A0AEC0;
  font-weight: 500;
}

/* ── Table deep styling ── */
.table-wrap {
  overflow: hidden;
  border-radius: 16px;
}

.table-wrap :deep(.el-table) {
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: #F8F9FA;
  --el-table-row-hover-bg-color: #F5F7FA;
  --el-table-border-color: #F0F2F5;
  --el-table-text-color: #2D3748;
  --el-table-header-text-color: #718096;
  font-size: 13px;
}

.table-wrap :deep(.el-table th.el-table__cell) {
  font-weight: 600;
  font-size: 12px;
}

.table-wrap :deep(.el-table td.el-table__cell) {
  padding: 14px 0;
  border-bottom: 1px solid #F0F2F5;
}

.table-wrap :deep(.el-table--striped .el-table__body tr.el-table__row--striped td.el-table__cell) {
  background: #FAFBFC;
}

.text-cell { font-size: 13px; color: #718096; }
.text-cell.bold { font-weight: 600; color: #2D3748; }
.text-cell.dim { color: #A0AEC0; }

.num-cell {
  font-size: 13px;
  font-weight: 600;
  color: #2D3748;
}

.num-cell small {
  font-size: 11px;
  color: #A0AEC0;
  font-weight: 400;
}

.cal-cell {
  font-size: 13px;
  font-weight: 600;
  color: #e8a18b;
}

.cal-cell small {
  font-size: 11px;
  color: #A0AEC0;
  font-weight: 400;
}

/* ── Action buttons ── */
.act-btn {
  padding: 4px 14px;
  border: none;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  margin-right: 6px;
}

.act-btn.edit {
  color: #7EB8DA;
  background: rgba(126, 184, 218, 0.1);
}

.act-btn.edit:hover {
  background: rgba(126, 184, 218, 0.2);
  transform: translateY(-1px);
}

.act-btn.del {
  color: #F0A8A8;
  background: rgba(240, 168, 168, 0.1);
}

.act-btn.del:hover {
  background: rgba(240, 168, 168, 0.2);
  transform: translateY(-1px);
}

/* ── Pagination ── */
.pager-wrap {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.pager-wrap :deep(.el-pagination) {
  --el-pagination-bg-color: transparent;
  --el-pagination-button-bg-color: #F5F7FA;
  --el-pagination-button-color: #718096;
  --el-pagination-hover-color: #7EB8DA;
}

.pager-wrap :deep(.el-pagination button),
.pager-wrap :deep(.el-pager li) {
  border-radius: 8px;
}

/* ── Empty state ── */
.empty-state {
  text-align: center;
  padding: 56px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.empty-state p {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: #2D3748;
}

.empty-state span {
  font-size: 13px;
  color: #A0AEC0;
}
</style>
