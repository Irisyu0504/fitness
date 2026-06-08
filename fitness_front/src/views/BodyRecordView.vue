<template>
  <main class="body-dashboard">
    <PageHeader title="身体数据" />

    <!-- 历史记录全屏模式 -->
    <div v-if="showHistory" class="history-fullscreen">
      <div class="history-header">
        <h2>历史记录</h2>
        <div class="header-actions">
          <span class="record-count">共 {{ total }} 条</span>
          <button class="close-btn" @click="showHistory = false">返回</button>
        </div>
      </div>

      <div v-if="records.length > 0" class="table-wrapper">
        <el-table :data="records" stripe style="width: 100%">
          <el-table-column prop="recordDate" label="日期" width="120" />
          <el-table-column prop="weight" label="体重(kg)" width="100" />
          <el-table-column prop="bodyFatRate" label="体脂率(%)" width="100" />
          <el-table-column prop="waistline" label="腰围(cm)" width="100" />
          <el-table-column prop="bmi" label="BMI" width="80" />
          <el-table-column prop="remark" label="备注" />
          <el-table-column label="操作" width="140" fixed="right">
            <template #default="{ row }">
              <button class="action-btn edit" @click="openEdit(row)">编辑</button>
              <button class="action-btn delete" @click="handleDelete(row.id)">删除</button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="page"
            v-model:page-size="size"
            :page-sizes="[10, 20, 50]"
            :total="total"
            layout="total, sizes, prev, pager, next"
            @current-change="fetchRecords"
            @size-change="fetchRecords"
          />
        </div>
      </div>

      <div v-else class="empty-table">
        <p>暂无体测记录</p>
        <span>添加第一条记录后即可查看</span>
      </div>
    </div>

    <!-- 正常模式 -->
    <div v-else class="dashboard-layout">
      <!-- 左侧 -->
      <aside class="left-panel">
        <BodyRecordForm @submit="handleAddRecord" />

        <!-- 历史记录入口 -->
        <div class="history-entry" @click="showHistory = true">
          <div class="entry-info">
            <h3>历史记录</h3>
            <span class="record-count">共 {{ total }} 条</span>
          </div>
          <span class="entry-arrow">查看全部 ›</span>
        </div>
      </aside>

      <!-- 右侧 -->
      <section class="right-panel">
        <!-- 关键指标卡片组 -->
        <BodyMetricCards
          :metrics="metricCards"
          :active-metric="activeMetric"
          @select="activeMetric = $event"
        />

        <!-- 趋势图 -->
        <BodyTrendChart
          :chart-data="chartData"
          :active-metric="activeMetric"
          :active-time-range="activeTimeRange"
          @time-change="activeTimeRange = $event"
        />

        <!-- BMI 状态 -->
        <div class="bottom-grid">
          <BmiStatusCard :bmi="latestRecord?.bmi" />
        </div>
      </section>
    </div>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="editDialogVisible" title="编辑记录" width="480px" :close-on-click-modal="false">
      <el-form label-position="top">
        <div class="edit-form-grid">
          <el-form-item label="体重 (kg)">
            <el-input-number v-model="editForm.weight" :min="20" :max="300" :step="0.1" :precision="1" />
          </el-form-item>
          <el-form-item label="体脂率 (%)">
            <el-input-number v-model="editForm.bodyFatRate" :min="1" :max="60" :step="0.1" :precision="1" />
          </el-form-item>
          <el-form-item label="腰围 (cm)">
            <el-input-number v-model="editForm.waistline" :min="30" :max="200" :step="0.5" :precision="1" />
          </el-form-item>
          <el-form-item label="日期">
            <el-date-picker v-model="editForm.recordDate" type="date" value-format="YYYY-MM-DD" />
          </el-form-item>
        </div>
        <el-form-item label="备注">
          <el-input v-model="editForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <button class="dialog-btn cancel" @click="editDialogVisible = false">取消</button>
        <button class="dialog-btn confirm" @click="handleUpdate">保存</button>
      </template>
    </el-dialog>
  </main>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { request } from '@/api/request.js'
import PageHeader from '@/components/PageHeader.vue'
import BodyRecordForm from '@/components/body/BodyRecordForm.vue'
import BodyMetricCards from '@/components/body/BodyMetricCards.vue'
import BodyTrendChart from '@/components/body/BodyTrendChart.vue'
import BmiStatusCard from '@/components/body/BmiStatusCard.vue'

// 数据状态
const records = ref([])
const allRecords = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const activeMetric = ref('weight')
const activeTimeRange = ref('7days')
const showHistory = ref(false)

const trendMetrics = [
  { key: 'weight', label: '体重', unit: 'kg' },
  { key: 'waistline', label: '腰围', unit: 'cm' },
  { key: 'bmi', label: 'BMI', unit: '' },
  { key: 'bodyFatRate', label: '体脂率', unit: '%' }
]

// 编辑相关
const editDialogVisible = ref(false)
const editId = ref(null)
const editForm = reactive({
  weight: null,
  bodyFatRate: null,
  waistline: null,
  recordDate: null,
  remark: ''
})

// 计算属性
const latestRecord = computed(() => {
  return allRecords.value[0] || records.value[0] || null
})

const metricCards = computed(() => {
  const latest = latestRecord.value
  const prev = allRecords.value[1] || records.value[1]

  if (!latest) {
    return [
      { key: 'weight', label: '体重', value: null, unit: 'kg', change: null },
      { key: 'bodyFatRate', label: '体脂率', value: null, unit: '%', change: null },
      { key: 'waistline', label: '腰围', value: null, unit: 'cm', change: null },
      { key: 'bmi', label: 'BMI', value: null, unit: '', change: null }
    ]
  }

  return [
    {
      key: 'weight',
      label: '体重',
      value: latest.weight ?? null,
      unit: 'kg',
      change: prev?.weight != null && latest.weight != null
        ? Number((latest.weight - prev.weight).toFixed(1)) : null
    },
    {
      key: 'bodyFatRate',
      label: '体脂率',
      value: latest.bodyFatRate ?? null,
      unit: '%',
      change: prev?.bodyFatRate != null && latest.bodyFatRate != null
        ? Number((latest.bodyFatRate - prev.bodyFatRate).toFixed(1)) : null
    },
    {
      key: 'waistline',
      label: '腰围',
      value: latest.waistline ?? null,
      unit: 'cm',
      change: prev?.waistline != null && latest.waistline != null
        ? Number((latest.waistline - prev.waistline).toFixed(1)) : null
    },
    {
      key: 'bmi',
      label: 'BMI',
      value: latest.bmi ?? null,
      unit: '',
      change: prev?.bmi != null && latest.bmi != null
        ? Number((latest.bmi - prev.bmi).toFixed(1)) : null
    }
  ]
})

const chartData = computed(() => {
  let data = [...allRecords.value].reverse()

  if (activeTimeRange.value === '7days') {
    data = data.slice(-7)
  } else if (activeTimeRange.value === 'weekly') {
    data = aggregateByWeek(data)
  } else if (activeTimeRange.value === 'monthly') {
    data = aggregateByMonth(data)
  }

  return {
    dates: data.map(d => d.recordDate),
    series: trendMetrics.map(metric => ({
      ...metric,
      values: data.map(d => toNumberOrNull(d[metric.key]))
    }))
  }
})

// 数据聚合函数
function aggregateByWeek(data) {
  return aggregateMetricAverages(data, (item) => {
    const date = parseDateOnly(item.recordDate)
    const weekStart = new Date(date)
    const mondayOffset = (date.getDay() + 6) % 7
    weekStart.setDate(date.getDate() - mondayOffset)
    return formatDateOnly(weekStart)
  })
}

function aggregateByMonth(data) {
  return aggregateMetricAverages(data, item => item.recordDate.substring(0, 7))
}

function aggregateMetricAverages(data, getKey) {
  const groups = {}

  data.forEach(item => {
    const key = getKey(item)

    if (!groups[key]) {
      groups[key] = {}
      trendMetrics.forEach(metric => {
        groups[key][metric.key] = { sum: 0, count: 0 }
      })
    }

    trendMetrics.forEach(metric => {
      const value = toNumberOrNull(item[metric.key])
      if (value == null) return
      groups[key][metric.key].sum += value
      groups[key][metric.key].count += 1
    })
  })

  return Object.entries(groups).map(([key, metrics]) => {
    const row = { recordDate: key }
    trendMetrics.forEach(metric => {
      const stat = metrics[metric.key]
      row[metric.key] = stat.count ? Number((stat.sum / stat.count).toFixed(1)) : null
    })
    return row
  })
}

function toNumberOrNull(value) {
  if (value === null || value === undefined || value === '') return null
  const numberValue = Number(value)
  return Number.isFinite(numberValue) ? numberValue : null
}

function parseDateOnly(value) {
  const [year, month, day] = String(value).split('-').map(Number)
  return new Date(year, month - 1, day)
}

function formatDateOnly(date) {
  const pad = value => String(value).padStart(2, '0')
  return [
    date.getFullYear(),
    pad(date.getMonth() + 1),
    pad(date.getDate())
  ].join('-')
}

// API 调用
async function fetchRecords() {
  try {
    const res = await request(`/body-records?page=${page.value}&size=${size.value}`)
    if (res && res.records) {
      records.value = res.records
      total.value = res.total
    } else {
      records.value = res || []
      total.value = res?.length || 0
    }
  } catch (e) {
    console.error('获取记录失败:', e)
  }
}

async function fetchAllRecords() {
  try {
    const res = await request('/body-records')
    allRecords.value = Array.isArray(res) ? res : (res?.records || [])
  } catch (e) {
    console.error('获取全部记录失败:', e)
  }
}

async function refreshAll() {
  await Promise.all([fetchRecords(), fetchAllRecords()])
}

async function handleAddRecord(form) {
  try {
    await request('/body-records', {
      method: 'POST',
      body: JSON.stringify(form)
    })
    ElMessage.success('记录添加成功')
    await refreshAll()
  } catch (e) {
    ElMessage.error(e.message || '添加失败')
  }
}

function openEdit(row) {
  editId.value = row.id
  editForm.weight = row.weight
  editForm.bodyFatRate = row.bodyFatRate
  editForm.waistline = row.waistline
  editForm.recordDate = row.recordDate
  editForm.remark = row.remark || ''
  editDialogVisible.value = true
}

async function handleUpdate() {
  if (!editId.value) return

  try {
    await request(`/body-records/${editId.value}`, {
      method: 'PUT',
      body: JSON.stringify({
        weight: editForm.weight,
        bodyFatRate: editForm.bodyFatRate,
        waistline: editForm.waistline,
        recordDate: editForm.recordDate,
        remark: editForm.remark
      })
    })
    ElMessage.success('修改成功')
    editDialogVisible.value = false
    editId.value = null
    await refreshAll()
  } catch (e) {
    ElMessage.error(e.message || '修改失败')
  }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定删除这条记录？', '提示', { type: 'warning' })
    await request(`/body-records/${id}`, { method: 'DELETE' })
    ElMessage.success('删除成功')
    await refreshAll()
  } catch {
    // 取消删除
  }
}

onMounted(() => {
  refreshAll()
})
</script>

<style scoped>
.body-dashboard {
  position: relative;
  width: min(100%, 1560px);
  min-height: auto;
  box-sizing: border-box;
  margin: 0 auto;
  padding: 36px 56px 36px 128px;
  overflow: visible;
  color: var(--text-primary);
  background: transparent;
}

/* ===== 历史记录全屏模式 ===== */
.history-fullscreen {
  background: #FFFFFF;
  border-radius: 24px;
  padding: 32px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
}

.history-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.history-header h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.record-count {
  font-size: 14px;
  color: var(--text-secondary);
}

.close-btn {
  padding: 8px 20px;
  border: none;
  border-radius: 12px;
  background: #F8F6F3;
  color: var(--text-secondary);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.close-btn:hover {
  background: #E8ECF1;
  color: var(--text-primary);
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
  font-size: 13px;
  padding: 14px 0;
}

.table-wrapper :deep(.el-table td.el-table__cell) {
  padding: 14px 0;
  border-bottom: 1px solid #E8ECF1;
}

.action-btn {
  padding: 6px 14px;
  border: none;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  margin-right: 8px;
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
  margin-top: 24px;
}

.pagination-wrapper :deep(.el-pagination) {
  --el-pagination-bg-color: transparent;
  --el-pagination-button-bg-color: #F8F6F3;
  --el-pagination-button-color: var(--text-secondary);
  --el-pagination-hover-color: #7EB8DA;
}

.empty-table {
  text-align: center;
  padding: 80px 0;
  color: var(--text-secondary);
}

.empty-table p {
  margin: 0 0 8px;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.empty-table span {
  font-size: 14px;
}

/* ===== 正常模式布局 ===== */
.dashboard-layout {
  display: grid;
  grid-template-columns: 420px 1fr;
  gap: 24px;
  align-items: start;
}

/* 左侧面板 */
.left-panel {
  display: flex;
  flex-direction: column;
  gap: 24px;
  position: sticky;
  top: 24px;
}

/* 历史记录入口卡片 */
.history-entry {
  background: #FFFFFF;
  border-radius: 24px;
  padding: 24px 28px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
  transition: all 0.3s ease;
}

.history-entry:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
}

.entry-info h3 {
  margin: 0 0 4px;
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
}

.entry-info .record-count {
  font-size: 13px;
  color: var(--text-secondary);
}

.entry-arrow {
  font-size: 14px;
  font-weight: 600;
  color: #7EB8DA;
}

/* 右侧面板 */
.right-panel {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.bottom-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 24px;
}

/* 编辑弹窗 */
.edit-form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.edit-form-grid :deep(.el-input-number) {
  width: 100%;
}

.dialog-btn {
  padding: 10px 24px;
  border: none;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.dialog-btn.cancel {
  color: var(--text-secondary);
  background: #F8F6F3;
}

.dialog-btn.confirm {
  color: #FFFFFF;
  background: #7EB8DA;
}

.dialog-btn.confirm:hover {
  background: #6AABC8;
}

/* 响应式 */
@media (max-width: 1200px) {
  .dashboard-layout {
    grid-template-columns: 1fr;
  }

  .left-panel {
    position: static;
  }

  .bottom-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .body-dashboard {
    padding: 32px 28px 48px;
  }
}
</style>
