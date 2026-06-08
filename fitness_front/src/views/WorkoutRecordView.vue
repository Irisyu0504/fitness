<template>
  <main class="page-container">
    <PageHeader title="训练记录" />

    <!-- 中间：表单 + 趋势图 -->
    <div class="mid-row">
      <div class="mid-left">
        <TrainingRecordForm @submit="handleAdd" />
      </div>
      <div class="mid-right">
        <TrainingTrendChart
          :chart-data="trendData"
          :active-metric="trendMetric"
          :active-time-range="trendTimeRange"
          @metric-change="trendMetric = $event"
          @time-change="trendTimeRange = $event"
        />
      </div>
    </div>

    <!-- 底部：历史表格 -->
    <TrainingHistoryTable
      :records="records"
      :total="total"
      :page="page"
      :size="size"
      @edit="openEdit"
      @delete="handleDelete"
      @page-change="handlePageChange"
    />

    <!-- 编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      title="编辑训练记录"
      width="520px"
      :close-on-click-modal="false"
      class="edit-dialog"
    >
      <el-form label-position="top">
        <div class="dlg-grid">
          <el-form-item label="运动项目">
            <el-select v-model="editForm.exerciseId" filterable>
              <el-option v-for="ex in exercises" :key="ex.id" :label="ex.exerciseName" :value="ex.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="训练日期">
            <el-date-picker v-model="editForm.recordDate" type="date" value-format="YYYY-MM-DD" />
          </el-form-item>
        </div>
        <div class="dlg-grid">
          <el-form-item label="时长 (min)">
            <el-input-number v-model="editForm.duration" :min="1" :max="999" controls-position="right" />
          </el-form-item>
          <el-form-item label="消耗 (kcal)">
            <el-input-number v-model="editForm.caloriesBurned" :min="0" :max="99999" controls-position="right" />
          </el-form-item>
        </div>
        <div class="dlg-grid">
          <el-form-item label="组数">
            <el-input-number v-model="editForm.setsCount" :min="0" :max="999" controls-position="right" />
          </el-form-item>
          <el-form-item label="次数">
            <el-input-number v-model="editForm.reps" :min="0" :max="9999" controls-position="right" />
          </el-form-item>
        </div>
        <el-form-item label="备注">
          <el-input v-model="editForm.remark" />
        </el-form-item>
      </el-form>
      <template #footer>
        <button class="dlg-btn cancel" @click="dialogVisible = false">取消</button>
        <button class="dlg-btn confirm" @click="handleUpdate">保存</button>
      </template>
    </el-dialog>
  </main>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { request } from '@/api/request.js'
import { normalizeWorkoutDetails } from '@/api/normalizers.js'
import PageHeader from '@/components/PageHeader.vue'
import TrainingRecordForm from '@/components/workout/TrainingRecordForm.vue'
import TrainingTrendChart from '@/components/workout/TrainingTrendChart.vue'
import TrainingHistoryTable from '@/components/workout/TrainingHistoryTable.vue'

// ── State ──
const records = ref([])
const allRecords = ref([])   // un-paginated, for chart & overview
const exercises = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const trendMetric = ref('calories')
const trendTimeRange = ref('7days')

// ── Edit dialog ──
const dialogVisible = ref(false)
const editId = ref(null)
const editForm = reactive({
  exerciseId: null, recordDate: null, duration: null,
  caloriesBurned: null, setsCount: null, reps: null, remark: ''
})

// ── Trend chart (computed from backend records) ──
const trendData = computed(() => {
  const chronologicalRecords = [...allRecords.value]
    .filter(r => r.recordDate)
    .sort((a, b) => a.recordDate.localeCompare(b.recordDate))

  if (trendTimeRange.value === 'weekly') {
    return buildTrendSeries(aggregateTrainingByWeek(chronologicalRecords), item => item.recordDate.substring(5))
  }

  if (trendTimeRange.value === 'monthly') {
    return buildTrendSeries(aggregateTrainingByMonth(chronologicalRecords), item => item.recordDate)
  }

  const days = buildRecentDays(7)
  const dailyMap = aggregateTrainingByDate(chronologicalRecords)

  return {
    dates: days.map(d => d.substring(5)),
    values: days.map(d => {
      const entry = dailyMap[d]
      return entry ? entry[trendMetric.value] : 0
    })
  }
})

function buildRecentDays(length) {
  const days = []
  for (let i = length - 1; i >= 0; i--) {
    const d = new Date()
    d.setDate(d.getDate() - i)
    days.push(formatDate(d))
  }
  return days
}

function aggregateTrainingByDate(data) {
  const map = {}
  data.forEach(r => {
    const key = r.recordDate
    if (!key) return
    if (!map[key]) map[key] = { recordDate: key, calories: 0, duration: 0 }
    map[key].calories += Number(r.caloriesBurned || 0)
    map[key].duration += Number(r.duration || 0)
  })
  return map
}

function aggregateTrainingByWeek(data) {
  const weeks = {}
  data.forEach(r => {
    if (!r.recordDate) return
    const date = new Date(`${r.recordDate}T00:00:00`)
    const weekStart = new Date(date)
    const dayOfWeek = date.getDay() || 7
    weekStart.setDate(date.getDate() - dayOfWeek + 1)
    const key = formatDate(weekStart)

    if (!weeks[key]) weeks[key] = { recordDate: key, calories: 0, duration: 0 }
    weeks[key].calories += Number(r.caloriesBurned || 0)
    weeks[key].duration += Number(r.duration || 0)
  })

  return Object.values(weeks).sort((a, b) => a.recordDate.localeCompare(b.recordDate))
}

function aggregateTrainingByMonth(data) {
  const months = {}
  data.forEach(r => {
    if (!r.recordDate) return
    const key = r.recordDate.substring(0, 7)
    if (!months[key]) months[key] = { recordDate: key, calories: 0, duration: 0 }
    months[key].calories += Number(r.caloriesBurned || 0)
    months[key].duration += Number(r.duration || 0)
  })

  return Object.values(months).sort((a, b) => a.recordDate.localeCompare(b.recordDate))
}

function buildTrendSeries(data, labelFormatter) {
  return {
    dates: data.map(labelFormatter),
    values: data.map(item => Number(item[trendMetric.value] || 0))
  }
}

function formatDate(date) {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

// ── API calls ──
async function fetchExercises() {
  try {
    const res = await request('/exercises')
    exercises.value = res?.records || res || []
  } catch { /* ignore */ }
}

async function fetchRecords() {
  try {
    const params = new URLSearchParams({ page: page.value, size: size.value })
    const res = await request(`/workout-records/detail?${params}`)
    const rows = normalizeWorkoutDetails(res)
    records.value = rows
    total.value = res?.records ? Number(res.total || rows.length) : rows.length
  } catch { /* ignore */ }
}

async function fetchAllRecords() {
  try {
    const res = await request('/workout-records/detail')
    allRecords.value = normalizeWorkoutDetails(res)
  } catch { /* ignore */ }
}

async function refreshAll() {
  await Promise.all([fetchRecords(), fetchAllRecords()])
}

// ── CRUD handlers ──
async function handleAdd(form) {
  try {
    await request('/workout-records', { method: 'POST', body: JSON.stringify(form) })
    ElMessage.success('添加成功')
    await refreshAll()
  } catch (e) {
    ElMessage.error(e.message || '添加失败')
  }
}

function openEdit(row) {
  editId.value = row.id
  editForm.exerciseId = row.exerciseId
  editForm.recordDate = row.recordDate
  editForm.duration = row.duration
  editForm.caloriesBurned = row.caloriesBurned
  editForm.setsCount = row.setsCount
  editForm.reps = row.reps
  editForm.remark = row.remark || ''
  dialogVisible.value = true
}

async function handleUpdate() {
  if (!editId.value) return
  try {
    await request(`/workout-records/${editId.value}`, {
      method: 'PUT', body: JSON.stringify(editForm)
    })
    ElMessage.success('修改成功')
    dialogVisible.value = false
    editId.value = null
    await refreshAll()
  } catch (e) {
    ElMessage.error(e.message || '修改失败')
  }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定删除这条记录？', '提示', { type: 'warning' })
    await request(`/workout-records/${id}`, { method: 'DELETE' })
    ElMessage.success('删除成功')
    await refreshAll()
  } catch { /* cancelled */ }
}

function handlePageChange({ page: p, size: s }) {
  page.value = p
  size.value = s
  fetchRecords()
}

onMounted(() => {
  fetchExercises()
  refreshAll()
})
</script>

<style scoped>
.page-container {
  width: 100%;
  max-width: 1440px;
  margin: 0 auto;
  padding: 36px 48px 48px 120px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* ── Middle row ── */
.mid-row {
  display: grid;
  grid-template-columns: 380px 1fr;
  gap: 24px;
  align-items: stretch;
}

.mid-left,
.mid-right {
  min-width: 0;
}

/* ── Edit dialog ── */
.edit-dialog :deep(.el-dialog) {
  border-radius: 24px;
}

.edit-dialog :deep(.el-dialog__header) {
  padding: 24px 28px 12px;
  margin: 0;
}

.edit-dialog :deep(.el-dialog__title) {
  font-size: 17px;
  font-weight: 700;
  color: #2D3748;
}

.edit-dialog :deep(.el-dialog__body) {
  padding: 0 28px;
}

.edit-dialog :deep(.el-dialog__footer) {
  padding: 16px 28px 24px;
}

.dlg-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 20px;
}

.dlg-grid :deep(.el-input__wrapper),
.dlg-grid :deep(.el-select .el-input__wrapper) {
  border-radius: 12px;
  background: #F8F9FA;
  border: 1px solid #E8ECF1;
  box-shadow: none;
}

.dlg-grid :deep(.el-input__wrapper:hover),
.dlg-grid :deep(.el-input__wrapper.is-focus) {
  border-color: #7EB8DA;
}

.edit-dialog :deep(.el-form-item) {
  margin-bottom: 18px;
}

.edit-dialog :deep(.el-form-item__label) {
  font-size: 13px;
  font-weight: 600;
  color: #718096;
}

.dlg-btn {
  padding: 9px 24px;
  border: none;
  border-radius: 999px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.dlg-btn.cancel {
  color: #718096;
  background: #F5F7FA;
}

.dlg-btn.cancel:hover {
  background: #E8ECF1;
}

.dlg-btn.confirm {
  color: #fff;
  background: #7EB8DA;
}

.dlg-btn.confirm:hover {
  background: #6AABC8;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(126, 184, 218, 0.3);
}

/* ── Responsive ── */
@media (max-width: 1100px) {
  .page-container {
    padding: 28px 24px 40px;
  }

  .mid-row {
    grid-template-columns: 1fr;
  }
}
</style>
