<template>
  <main class="plan-dashboard">
    <PageHeader title="训练计划" />

    <!-- 顶部：当前计划总览 -->
    <CurrentPlanOverview :plan="activePlan" />

    <!-- 中间：新建表单 + 历史计划 -->
    <div class="middle-grid">
      <TrainingPlanForm
        :edit-data="editingPlan"
        :has-active-plan="!!activePlan"
        @submit="handleFormSubmit"
        @cancel="cancelEdit"
      />
      <TrainingPlanList
        :plans="plans"
        @edit="startEdit"
        @delete="handleDelete"
        @activate="handleActivate"
        @complete="handleComplete"
      />
    </div>
  </main>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { request } from '@/api/request.js'
import PageHeader from '@/components/PageHeader.vue'
import CurrentPlanOverview from '@/components/plan/CurrentPlanOverview.vue'
import TrainingPlanForm from '@/components/plan/TrainingPlanForm.vue'
import TrainingPlanList from '@/components/plan/TrainingPlanList.vue'

// 数据状态
const plans = ref([])
const editingPlan = ref(null)

// 当前进行中的计划
const activePlan = computed(() => {
  return plans.value.find(p => p.status === '进行中') || null
})

// 获取所有计划
async function fetchPlans() {
  try {
    const res = await request('/workout-plans')
    plans.value = res || []
  } catch (e) {
    console.error('获取计划失败:', e)
  }
}

// 表单提交
async function handleFormSubmit(formData, isEdit) {
  if (formData.status === '进行中' && activePlan.value && !isEdit) {
    ElMessage.warning('已有进行中的计划，请先结束当前计划或保存为草稿')
    return
  }

  await savePlan(formData, isEdit)
}

// 保存计划
async function savePlan(data, isEdit) {
  try {
    if (isEdit && editingPlan.value) {
      await request(`/workout-plans/${editingPlan.value.id}`, {
        method: 'PUT',
        body: JSON.stringify(data)
      })
      ElMessage.success('计划修改成功')
    } else {
      await request('/workout-plans', {
        method: 'POST',
        body: JSON.stringify(data)
      })
      ElMessage.success('计划创建成功')
    }
    editingPlan.value = null
    await fetchPlans()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

// 开始编辑
function startEdit(plan) {
  editingPlan.value = { ...plan }
}

// 取消编辑
function cancelEdit() {
  editingPlan.value = null
}

// 删除计划
async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定删除此计划？', '提示', { type: 'warning' })
    await request(`/workout-plans/${id}`, { method: 'DELETE' })
    ElMessage.success('删除成功')
    await fetchPlans()
  } catch {
    // 取消
  }
}

// 设为当前计划
async function handleActivate(plan) {
  if (activePlan.value) {
    try {
      await ElMessageBox.confirm(
        `当前有进行中的计划"${activePlan.value.planName}"，是否先结束它？`,
        '提示',
        { type: 'warning' }
      )
      // 先结束当前计划
      await request(`/workout-plans/${activePlan.value.id}`, {
        method: 'PUT',
        body: JSON.stringify({ status: '已完成' })
      })
    } catch {
      return
    }
  }

  try {
    await request(`/workout-plans/${plan.id}`, {
      method: 'PUT',
      body: JSON.stringify({ status: '进行中' })
    })
    ElMessage.success('已设为当前计划')
    await fetchPlans()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

// 结束计划
async function handleComplete(plan) {
  try {
    await ElMessageBox.confirm('确定结束此计划？', '提示', { type: 'warning' })
    await request(`/workout-plans/${plan.id}`, {
      method: 'PUT',
      body: JSON.stringify({ status: '已完成' })
    })
    ElMessage.success('计划已结束')
    await fetchPlans()
  } catch {
    // 取消
  }
}

onMounted(() => {
  fetchPlans()
})
</script>

<style scoped>
.plan-dashboard {
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

.middle-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  align-items: stretch;
}

.middle-grid > * {
  height: 100%;
}

/* 响应式 */
@media (max-width: 1200px) {
  .plan-dashboard {
    padding: 32px 28px 48px;
  }

  .middle-grid {
    grid-template-columns: 1fr;
  }
}
</style>
