<template>
  <div class="form-card">
    <h3 class="form-title">新增训练记录</h3>

    <el-form :model="form" label-position="top" @submit.prevent="handleSubmit">
      <div class="form-grid">
        <el-form-item label="训练计划">
          <el-select v-model="form.planId" clearable placeholder="选择计划（可选）">
            <el-option
              v-for="p in plans"
              :key="p.id"
              :label="p.planName"
              :value="p.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="运动项目" required>
          <el-select v-model="form.exerciseId" filterable placeholder="选择运动">
            <el-option
              v-for="ex in exercises"
              :key="ex.id"
              :label="ex.exerciseName"
              :value="ex.id"
            />
          </el-select>
        </el-form-item>
      </div>

      <div class="form-grid">
        <el-form-item label="训练日期" required>
          <el-date-picker
            v-model="form.recordDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择日期"
          />
        </el-form-item>

        <el-form-item label="训练时长 (min)" required>
          <el-input-number v-model="form.duration" :min="1" :max="999" controls-position="right" />
        </el-form-item>
      </div>

      <div class="form-grid">
        <el-form-item label="组数">
          <el-input-number v-model="form.setsCount" :min="0" :max="999" controls-position="right" />
        </el-form-item>

        <el-form-item label="次数">
          <el-input-number v-model="form.reps" :min="0" :max="9999" controls-position="right" />
        </el-form-item>
      </div>

      <div class="form-grid">
        <el-form-item label="消耗热量 (kcal)">
          <el-input-number v-model="form.caloriesBurned" :min="0" :max="99999" controls-position="right" />
        </el-form-item>

        <el-form-item label="备注">
          <el-input v-model="form.remark" placeholder="可选备注" />
        </el-form-item>
      </div>

      <button class="submit-btn" type="submit" :disabled="loading">
        {{ loading ? '提交中...' : '添加记录' }}
      </button>
    </el-form>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { request } from '@/api/request.js'

const emit = defineEmits(['submit'])
const loading = ref(false)
const exercises = ref([])
const plans = ref([])

const form = reactive({
  planId: null,
  exerciseId: null,
  recordDate: new Date().toISOString().split('T')[0],
  duration: null,
  setsCount: null,
  reps: null,
  caloriesBurned: null,
  remark: ''
})

async function fetchExercises() {
  try {
    const res = await request('/exercises')
    exercises.value = res?.records || res || []
  } catch { /* ignore */ }
}

async function fetchPlans() {
  try {
    const res = await request('/workout-plans')
    plans.value = Array.isArray(res) ? res : (res?.records || [])
    // Auto-select active plan
    const active = plans.value.find(p => p.status === '进行中' || p.status === 'ACTIVE')
    if (active) form.planId = active.id
  } catch { /* ignore */ }
}

async function handleSubmit() {
  if (!form.exerciseId || !form.recordDate || !form.duration) {
    ElMessage.warning('运动项目、日期和时长为必填')
    return
  }

  loading.value = true
  try {
    await emit('submit', { ...form })
    // Reset form
    form.exerciseId = null
    form.duration = null
    form.setsCount = null
    form.reps = null
    form.caloriesBurned = null
    form.remark = ''
    form.recordDate = new Date().toISOString().split('T')[0]
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchExercises()
  fetchPlans()
})
</script>

<style scoped>
.form-card {
  background: #FFFFFF;
  border: 1px solid #E8ECF1;
  border-radius: 24px;
  padding: 28px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06), 0 1px 3px rgba(0, 0, 0, 0.04);
}

.form-title {
  margin: 0 0 22px;
  font-size: 17px;
  font-weight: 700;
  color: #2D3748;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 20px;
}

.form-grid :deep(.el-input-number),
.form-grid :deep(.el-select),
.form-grid :deep(.el-date-editor) {
  width: 100%;
}

.form-grid :deep(.el-input__wrapper),
.form-grid :deep(.el-select .el-input__wrapper) {
  border-radius: 12px;
  background: #F8F9FA;
  border: 1px solid #E8ECF1;
  box-shadow: none;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.form-grid :deep(.el-input__wrapper:hover),
.form-grid :deep(.el-input__wrapper.is-focus) {
  border-color: #7EB8DA;
  box-shadow: 0 0 0 3px rgba(126, 184, 218, 0.1);
}

.form-grid :deep(.el-form-item) {
  margin-bottom: 18px;
}

.form-grid :deep(.el-form-item__label) {
  font-size: 13px;
  font-weight: 600;
  color: #718096;
  margin-bottom: 6px;
}

.submit-btn {
  width: 100%;
  height: 46px;
  border: none;
  border-radius: 999px;
  background: #7EB8DA;
  color: #FFFFFF;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  transition: transform 0.25s ease, box-shadow 0.25s ease, background 0.2s ease;
  margin-top: 4px;
}

.submit-btn:hover:not(:disabled) {
  background: #6AABC8;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(126, 184, 218, 0.35);
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
