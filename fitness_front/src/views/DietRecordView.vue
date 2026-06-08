<template>
  <main class="diet-dashboard">
    <PageHeader title="饮食记录" />

    <!-- 上方两栏：新增表单 + 营养分析 -->
    <div class="top-grid">
      <DietRecordForm @submit="handleAddRecord" />
      <NutritionAnalysisPanel :data="todayStats" />
    </div>

    <!-- 下方：历史记录表格 -->
    <DietHistoryTable
      ref="historyTableRef"
      :records="records"
      :total="total"
      :page="page"
      :size="size"
      @edit="openEdit"
      @delete="handleDelete"
      @search="fetchRecords"
      @page-change="handlePageChange"
    />

    <!-- 编辑弹窗 -->
    <el-dialog v-model="editDialogVisible" title="编辑饮食记录" width="500px" :close-on-click-modal="false">
      <el-form label-position="top">
        <el-form-item label="食物名称">
          <el-input v-model="editForm.foodName" />
        </el-form-item>
        <div class="edit-form-row">
          <el-form-item label="餐次类型">
            <el-select v-model="editForm.mealType">
              <el-option label="早餐" value="早餐" />
              <el-option label="午餐" value="午餐" />
              <el-option label="晚餐" value="晚餐" />
              <el-option label="加餐" value="加餐" />
              <el-option label="夜宵" value="夜宵" />
            </el-select>
          </el-form-item>
          <el-form-item label="热量 (kcal)">
            <el-input-number v-model="editForm.calories" :min="0" :max="9999" />
          </el-form-item>
        </div>
        <div class="edit-form-row">
          <el-form-item label="蛋白质 (g)">
            <el-input-number v-model="editForm.protein" :min="0" :max="999" :precision="1" />
          </el-form-item>
          <el-form-item label="碳水 (g)">
            <el-input-number v-model="editForm.carbs" :min="0" :max="999" :precision="1" />
          </el-form-item>
          <el-form-item label="脂肪 (g)">
            <el-input-number v-model="editForm.fat" :min="0" :max="999" :precision="1" />
          </el-form-item>
        </div>
        <el-form-item label="摄入时间">
          <el-date-picker
            v-model="editForm.mealTime"
            type="datetime"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DDTHH:mm"
          />
        </el-form-item>
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { request } from '@/api/request.js'
import PageHeader from '@/components/PageHeader.vue'
import NutritionAnalysisPanel from '@/components/diet/NutritionAnalysisPanel.vue'
import DietRecordForm from '@/components/diet/DietRecordForm.vue'
import DietHistoryTable from '@/components/diet/DietHistoryTable.vue'

// 数据状态
const records = ref([])
const todayStats = ref({})
const page = ref(1)
const size = ref(10)
const total = ref(0)
const historyTableRef = ref(null)

// 编辑相关
const editDialogVisible = ref(false)
const editId = ref(null)
const editForm = reactive({
  foodName: '',
  mealType: '',
  calories: null,
  protein: null,
  carbs: null,
  fat: null,
  mealTime: '',
  remark: ''
})

// 获取今日统计
async function fetchTodayStats() {
  try {
    const res = await request('/diet-records/calorie-stat')
    todayStats.value = res || {}
  } catch (e) {
    console.error('获取统计失败:', e)
  }
}

// 获取历史记录
async function fetchRecords() {
  try {
    const filters = historyTableRef.value?.getFilters() || {}
    const params = new URLSearchParams()
    params.append('page', page.value)
    params.append('size', size.value)
    if (filters.foodName) params.append('foodName', filters.foodName)
    if (filters.mealType) params.append('mealType', filters.mealType)

    const res = await request(`/diet-records?${params}`)
    if (res && res.records) {
      records.value = res.records
      total.value = Number(res.total) || 0
    } else {
      records.value = res || []
      total.value = res?.length || 0
    }
  } catch (e) {
    console.error('获取记录失败:', e)
  }
}

// 刷新所有数据
async function refreshAll() {
  await Promise.all([
    fetchTodayStats(),
    fetchRecords()
  ])
}

// 新增记录
async function handleAddRecord(form) {
  try {
    await request('/diet-records', {
      method: 'POST',
      body: JSON.stringify(form)
    })
    ElMessage.success('记录添加成功')
    await refreshAll()
  } catch (e) {
    ElMessage.error(e.message || '添加失败')
  }
}

// 打开编辑
function openEdit(row) {
  editId.value = row.id
  editForm.foodName = row.foodName
  editForm.mealType = row.mealType
  editForm.calories = row.calories
  editForm.protein = row.protein
  editForm.carbs = row.carbs
  editForm.fat = row.fat
  editForm.mealTime = toMinuteDateTime(row.mealTime)
  editForm.remark = row.remark || ''
  editDialogVisible.value = true
}

// 更新记录
async function handleUpdate() {
  if (!editId.value) return

  try {
    await request(`/diet-records/${editId.value}`, {
      method: 'PUT',
      body: JSON.stringify(editForm)
    })
    ElMessage.success('修改成功')
    editDialogVisible.value = false
    editId.value = null
    await refreshAll()
  } catch (e) {
    ElMessage.error(e.message || '修改失败')
  }
}

// 删除记录
async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定删除这条记录？', '提示', { type: 'warning' })
    await request(`/diet-records/${id}`, { method: 'DELETE' })
    ElMessage.success('删除成功')
    await refreshAll()
  } catch {
    // 取消删除
  }
}

// 分页变化
function handlePageChange({ page: p, size: s }) {
  page.value = p
  size.value = s
  fetchRecords()
}

function toMinuteDateTime(value) {
  if (!value) return ''
  if (value instanceof Date) {
    const pad = item => String(item).padStart(2, '0')
    return [
      value.getFullYear(),
      pad(value.getMonth() + 1),
      pad(value.getDate())
    ].join('-') + `T${pad(value.getHours())}:${pad(value.getMinutes())}`
  }

  return String(value).replace(' ', 'T').slice(0, 16)
}

onMounted(() => {
  refreshAll()
})
</script>

<style scoped>
.diet-dashboard {
  position: relative;
  width: min(100%, 1560px);
  min-height: auto;
  box-sizing: border-box;
  margin: 0 auto;
  padding: 36px 56px 48px 128px;
  overflow: visible;
  color: var(--text-primary);
  background: transparent;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.top-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  align-items: stretch;
}

.top-grid > * {
  height: 100%;
}

/* 编辑弹窗 */
.edit-form-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 16px;
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
  .diet-dashboard {
    padding: 32px 28px 48px;
  }

  .top-grid {
    grid-template-columns: 1fr;
  }
}
</style>
