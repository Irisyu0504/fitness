<template>
  <div class="form-card">
    <div class="card-header">
      <h3>{{ isEdit ? '编辑计划' : '新建计划' }}</h3>
    </div>

    <el-form class="plan-form" :model="form" label-position="top" @submit.prevent="handleSubmit">
      <el-form-item label="计划名称">
        <el-input v-model="form.planName" placeholder="如：减脂塑形计划" />
      </el-form-item>

      <el-form-item label="计划目标">
        <el-input v-model="form.planGoal" placeholder="如：减少体脂率至18%" />
      </el-form-item>

      <div class="form-row">
        <el-form-item label="训练频率">
          <el-input v-model="form.frequency" placeholder="如：每周5次" />
        </el-form-item>

        <el-form-item label="状态">
          <el-select v-model="form.status">
            <el-option label="草稿" value="草稿" />
            <el-option label="进行中" value="进行中" :disabled="hasActivePlan" />
            <el-option label="已完成" value="已完成" />
          </el-select>
        </el-form-item>
      </div>

      <div class="form-row">
        <el-form-item label="开始日期">
          <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>

        <el-form-item label="结束日期">
          <el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
      </div>

      <el-form-item label="计划描述">
        <el-input v-model="form.description" type="textarea" :rows="3" placeholder="计划详细描述" />
      </el-form-item>

      <button class="submit-btn" type="submit" :disabled="loading">
        <span v-if="loading">提交中...</span>
        <span v-else>{{ isEdit ? '保存修改' : '创建计划' }}</span>
      </button>

      <button v-if="isEdit" class="cancel-btn" type="button" @click="$emit('cancel')">取消编辑</button>
    </el-form>
  </div>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  editData: { type: Object, default: null },
  hasActivePlan: { type: Boolean, default: false }
})

const emit = defineEmits(['submit', 'cancel'])

const loading = ref(false)
const isEdit = ref(false)

const form = reactive({
  planName: '',
  planGoal: '',
  frequency: '',
  startDate: null,
  endDate: null,
  description: '',
  status: '草稿'
})

watch(() => props.editData, (data) => {
  if (data) {
    isEdit.value = true
    form.planName = data.planName || ''
    form.planGoal = data.planGoal || ''
    form.frequency = data.frequency || ''
    form.startDate = data.startDate
    form.endDate = data.endDate
    form.description = data.description || ''
    form.status = data.status || '草稿'
  } else {
    isEdit.value = false
    resetForm()
  }
}, { immediate: true })

function resetForm() {
  form.planName = ''
  form.planGoal = ''
  form.frequency = ''
  form.startDate = null
  form.endDate = null
  form.description = ''
  form.status = '草稿'
}

async function handleSubmit() {
  if (!form.planName || !form.planGoal) {
    ElMessage.warning('计划名称和目标为必填')
    return
  }

  // 检查是否有进行中计划且用户选择"进行中"
  if (form.status === '进行中' && props.hasActivePlan && !isEdit.value) {
    ElMessage.warning('已有进行中的计划，请先结束当前计划或保存为草稿')
    return
  }

  loading.value = true
  try {
    await emit('submit', { ...form }, isEdit.value)
    if (!isEdit.value) {
      resetForm()
    }
  } finally {
    loading.value = false
  }
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

.plan-form {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.plan-form :deep(.el-form-item__label) {
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

.form-row :deep(.el-input-number),
.form-row :deep(.el-select),
.form-row :deep(.el-date-editor) {
  width: 100%;
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
  margin-top: auto;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(126, 184, 218, 0.35);
}

.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.cancel-btn {
  width: 100%;
  height: 40px;
  border: none;
  border-radius: 12px;
  background: #F8F6F3;
  color: var(--text-secondary);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  margin-top: 12px;
}

.cancel-btn:hover {
  background: #E8ECF1;
}
</style>
