<template>
  <div class="form-card">
    <div class="card-header">
      <h3>新增饮食记录</h3>
    </div>

    <el-form class="diet-form" :model="form" label-position="top" @submit.prevent="handleSubmit">
      <el-form-item label="食物名称">
        <el-input v-model="form.foodName" placeholder="如：鸡胸肉沙拉" />
      </el-form-item>

      <div class="form-row">
        <el-form-item label="餐次类型">
          <el-select v-model="form.mealType" placeholder="选择餐次">
            <el-option label="早餐" value="早餐" />
            <el-option label="午餐" value="午餐" />
            <el-option label="晚餐" value="晚餐" />
            <el-option label="加餐" value="加餐" />
            <el-option label="夜宵" value="夜宵" />
          </el-select>
        </el-form-item>

        <el-form-item label="热量 (kcal)">
          <el-input-number v-model="form.calories" :min="0" :max="9999" :step="10" controls-position="right" />
        </el-form-item>
      </div>

      <div class="form-row">
        <el-form-item label="蛋白质 (g)">
          <el-input-number v-model="form.protein" :min="0" :max="999" :step="1" :precision="1" controls-position="right" />
        </el-form-item>

        <el-form-item label="碳水 (g)">
          <el-input-number v-model="form.carbs" :min="0" :max="999" :step="1" :precision="1" controls-position="right" />
        </el-form-item>
      </div>

      <div class="form-row">
        <el-form-item label="脂肪 (g)">
          <el-input-number v-model="form.fat" :min="0" :max="999" :step="1" :precision="1" controls-position="right" />
        </el-form-item>

        <el-form-item label="摄入时间">
          <el-date-picker
            v-model="form.mealTime"
            type="datetime"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DDTHH:mm"
            placeholder="选择时间"
          />
        </el-form-item>
      </div>

      <el-form-item label="备注">
        <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="可选备注" />
      </el-form-item>

      <button class="submit-btn" type="submit" :disabled="loading">
        <span v-if="loading">提交中...</span>
        <span v-else>添加记录</span>
      </button>
    </el-form>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'

const emit = defineEmits(['submit'])

const loading = ref(false)
const form = reactive({
  foodName: '',
  mealType: '午餐',
  calories: null,
  protein: null,
  carbs: null,
  fat: null,
  mealTime: formatMinuteDateTime(),
  remark: ''
})

async function handleSubmit() {
  if (!form.foodName || !form.calories) {
    ElMessage.warning('食物名称和热量为必填')
    return
  }

  loading.value = true
  try {
    await emit('submit', { ...form })
    // 重置表单
    form.foodName = ''
    form.calories = null
    form.protein = null
    form.carbs = null
    form.fat = null
    form.remark = ''
    form.mealTime = formatMinuteDateTime()
  } finally {
    loading.value = false
  }
}

function formatMinuteDateTime(date = new Date()) {
  const pad = value => String(value).padStart(2, '0')
  return [
    date.getFullYear(),
    pad(date.getMonth() + 1),
    pad(date.getDate())
  ].join('-') + `T${pad(date.getHours())}:${pad(date.getMinutes())}`
}
</script>

<style scoped>
.form-card {
  background: #FFFFFF;
  border-radius: 24px;
  padding: 28px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
  height: 100%;
  display: flex;
  flex-direction: column;
}

.card-header {
  margin-bottom: 24px;
}

.card-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
}

.diet-form :deep(.el-form-item__label) {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.form-row :deep(.el-input-number),
.form-row :deep(.el-select) {
  width: 100%;
}

.form-row :deep(.el-input__wrapper),
.form-row :deep(.el-select .el-input__wrapper) {
  border-radius: 14px;
  background: #F8F6F3;
  border: 1px solid #E8ECF1;
  box-shadow: none;
}

.form-row :deep(.el-input__wrapper:hover),
.form-row :deep(.el-input__wrapper.is-focus) {
  border-color: #7EB8DA;
}

:deep(.el-textarea__inner) {
  border-radius: 14px;
  background: #F8F6F3;
  border: 1px solid #E8ECF1;
  box-shadow: none;
}

:deep(.el-textarea__inner:focus) {
  border-color: #7EB8DA;
}

.submit-btn {
  width: 100%;
  height: 48px;
  border: none;
  border-radius: 999px;
  background: #7EB8DA;
  color: #FFFFFF;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 8px;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(126, 184, 218, 0.35);
}

.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}
</style>
