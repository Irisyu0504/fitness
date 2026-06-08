<template>
  <div class="record-form-card">
    <div class="card-header">
      <h3>新增记录</h3>
      <span class="card-badge">每日打卡</span>
    </div>

    <el-form
      class="body-form"
      :model="form"
      label-position="top"
      @submit.prevent="handleSubmit"
    >
      <div class="form-grid">
        <el-form-item label="体重 (kg)">
          <el-input-number
            v-model="form.weight"
            :min="20"
            :max="300"
            :step="0.1"
            :precision="1"
            placeholder="58.5"
            controls-position="right"
          />
        </el-form-item>

        <el-form-item label="体脂率 (%)">
          <el-input-number
            v-model="form.bodyFatRate"
            :min="1"
            :max="60"
            :step="0.1"
            :precision="1"
            placeholder="21.5"
            controls-position="right"
          />
        </el-form-item>

        <el-form-item label="腰围 (cm)">
          <el-input-number
            v-model="form.waistline"
            :min="30"
            :max="200"
            :step="0.5"
            :precision="1"
            placeholder="68"
            controls-position="right"
          />
        </el-form-item>

        <el-form-item label="日期">
          <el-date-picker
            v-model="form.recordDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择日期"
          />
        </el-form-item>
      </div>

      <el-form-item label="备注" class="full-width">
        <el-input
          v-model="form.remark"
          type="textarea"
          :rows="2"
          placeholder="今天的感受..."
        />
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
  weight: null,
  bodyFatRate: null,
  waistline: null,
  recordDate: new Date().toISOString().split('T')[0],
  remark: ''
})

async function handleSubmit() {
  if (!form.weight || !form.recordDate) {
    ElMessage.warning('体重和日期为必填项')
    return
  }

  loading.value = true
  try {
    await emit('submit', { ...form })
    form.weight = null
    form.bodyFatRate = null
    form.waistline = null
    form.remark = ''
    form.recordDate = new Date().toISOString().split('T')[0]
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.record-form-card {
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

.card-badge {
  padding: 4px 12px;
  font-size: 12px;
  font-weight: 600;
  color: var(--text-secondary);
  background: #F8F6F3;
  border-radius: 999px;
}

.body-form :deep(.el-form-item__label) {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.form-grid :deep(.el-input-number) {
  width: 100%;
}

.form-grid :deep(.el-input-number .el-input__wrapper) {
  border-radius: 14px;
  background: #F8F6F3;
  border: 1px solid #E8ECF1;
  box-shadow: none;
  transition: all 0.2s ease;
}

.form-grid :deep(.el-input-number .el-input__wrapper:hover),
.form-grid :deep(.el-input-number .el-input__wrapper.is-focus) {
  border-color: #7EB8DA;
  box-shadow: 0 0 0 3px rgba(126, 184, 218, 0.12);
}

.form-grid :deep(.el-date-editor) {
  width: 100%;
}

.form-grid :deep(.el-date-editor .el-input__wrapper) {
  border-radius: 14px;
  background: #F8F6F3;
  border: 1px solid #E8ECF1;
  box-shadow: none;
}

.full-width :deep(.el-textarea__inner) {
  border-radius: 14px;
  background: #F8F6F3;
  border: 1px solid #E8ECF1;
  box-shadow: none;
  transition: all 0.2s ease;
}

.full-width :deep(.el-textarea__inner:focus) {
  border-color: #7EB8DA;
  box-shadow: 0 0 0 3px rgba(126, 184, 218, 0.12);
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
