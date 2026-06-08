<template>
  <main class="exercise-container">
    <PageHeader title="健身动作" />

    <!-- 搜索栏 -->
    <article class="solid-card filter-card">
      <div class="filter-bar">
        <div class="filter-left">
          <el-input v-model="filters.exerciseName" placeholder="搜索运动名称" clearable @clear="resetToFirstPageAndFetch" @keyup.enter="resetToFirstPageAndFetch" style="width: 200px" />
          <el-select v-model="filters.difficulty" placeholder="难度" clearable @change="resetToFirstPageAndFetch" style="width: 120px">
            <el-option label="初级" value="初级" />
            <el-option label="中级" value="中级" />
            <el-option label="高级" value="高级" />
          </el-select>
        </div>
        <div class="filter-right">
          <button class="plan-action" @click="resetToFirstPageAndFetch">查询</button>
          <button v-if="isAdmin" class="plan-action" @click="openDialog(null)">+ 新增运动</button>
        </div>
      </div>
    </article>

    <!-- 肌群快捷筛选 -->
    <div class="muscle-chips">
      <button
        v-for="group in ['全部', ...muscleGroups]"
        :key="group"
        class="chip"
        :class="{ 'chip--active': (group === '全部' && !filters.muscleGroup) || filters.muscleGroup === group }"
        @click="selectMuscleGroup(group)"
      >{{ group }}</button>
    </div>

    <!-- 运动卡片网格 -->
    <div class="exercise-grid">
      <article v-for="ex in exercises" :key="ex.id" class="solid-card exercise-card">
        <!-- 图片区域 -->
        <div class="card-visual">
          <img :src="getExerciseImage(ex.exerciseName, ex.muscleGroup)" :alt="ex.exerciseName" class="card-image" />
        </div>

        <!-- 信息区域 -->
        <div class="card-body">
          <h3 class="card-name">{{ ex.exerciseName }}</h3>
          <div class="card-tags">
            <span class="tag tag--muscle">{{ ex.muscleGroup }}</span>
            <span class="tag tag--difficulty" :class="'tag--' + getDifficultyLevel(ex.difficulty)">{{ ex.difficulty }}</span>
          </div>
          <p class="card-desc">{{ ex.description }}</p>
          <div class="card-footer">
            <span class="card-cal">⚡ {{ ex.caloriesPerMinute }} kcal/min</span>
            <div v-if="isAdmin" class="card-actions">
              <button class="link-btn" @click="openDialog(ex)">编辑</button>
              <button class="link-btn link-danger" @click="handleDelete(ex.id)">删除</button>
            </div>
          </div>
        </div>
      </article>
    </div>

    <!-- 分页 -->
    <div class="pagination-wrap">
      <el-pagination
        :current-page="page"
        :page-size="size"
        :page-sizes="[8, 12, 24]"
        :total="total"
        layout="total, sizes, prev, pager, next"
        @update:current-page="handleCurrentChange"
        @current-change="handleCurrentChange"
        @update:page-size="handleSizeChange"
        @size-change="handleSizeChange"
      />
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogForm.id ? '编辑运动' : '新增运动'" width="480px" :close-on-click-modal="false">
      <el-form label-position="top">
        <el-form-item label="运动名称 *">
          <el-input v-model="dialogForm.exerciseName" />
        </el-form-item>
        <el-form-item label="目标肌群 *">
          <el-select v-model="dialogForm.muscleGroup" style="width: 100%">
            <el-option v-for="g in muscleGroups" :key="g" :label="g" :value="g" />
          </el-select>
        </el-form-item>
        <el-form-item label="难度">
          <el-select v-model="dialogForm.difficulty" style="width: 100%">
            <el-option label="初级" value="初级" />
            <el-option label="中级" value="中级" />
            <el-option label="高级" value="高级" />
          </el-select>
        </el-form-item>
        <el-form-item label="每分钟消耗(kcal)">
          <el-input-number v-model="dialogForm.caloriesPerMinute" :min="0" :max="50" :step="0.5" :precision="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="dialogForm.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <button class="plan-action plan-action--ghost" @click="dialogVisible = false">取消</button>
        <button class="plan-action" @click="handleSave">保存</button>
      </template>
    </el-dialog>
  </main>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { request } from '@/api/request.js'

// 导入健身动作图片
import imgPushup from '@/assets/images/俯卧撑.jpg'
import imgLatPulldown from '@/assets/images/高位下拉.jpg'
import imgLateralRaise from '@/assets/images/侧平举.jpg'
import imgBicepCurl from '@/assets/images/杠铃弯举.jpg'
import imgTricepPushdown from '@/assets/images/三头绳索下压.jpg'
import imgPlank from '@/assets/images/平板支撑.jpg'
import imgRunning from '@/assets/images/跑步.jpg'
import imgCrunch from '@/assets/images/卷腹.jpg'

const muscleGroups = ['胸部', '背部', '腿部', '肩部', '手臂', '核心', '有氧']
const exercises = ref([])
const page = ref(1)
const size = ref(8)
const total = ref(0)
const isAdmin = ref(false)

const filters = reactive({ exerciseName: '', muscleGroup: '', difficulty: '' })

const dialogVisible = ref(false)
const dialogForm = reactive({ id: null, exerciseName: '', muscleGroup: '', difficulty: '初级', caloriesPerMinute: null, description: '' })

// 运动名称到图片的映射
const exerciseImageMap = {
  '俯卧撑': imgPushup,
  '高位下拉': imgLatPulldown,
  '侧平举': imgLateralRaise,
  '杠铃弯举': imgBicepCurl,
  '三头绳索下压': imgTricepPushdown,
  '平板支撑': imgPlank,
  '跑步': imgRunning,
  '卷腹': imgCrunch
}

// 肌群到默认图片的映射
const muscleImageMap = {
  '胸部': imgPushup,
  '背部': imgLatPulldown,
  '肩部': imgLateralRaise,
  '手臂': imgBicepCurl,
  '核心': imgPlank,
  '有氧': imgRunning,
  '腿部': imgRunning
}

// 肌群渐变色（作为图片加载失败的备用）
const muscleGradients = {
  '胸部': 'linear-gradient(135deg, #7EB8DA, #A8D4E6)',
  '背部': 'linear-gradient(135deg, #e8a18b, #F0BFB0)',
  '腿部': 'linear-gradient(135deg, #B8A9C9, #D4C5E2)',
  '肩部': 'linear-gradient(135deg, #ffdb69, #F5D5A0)',
  '手臂': 'linear-gradient(135deg, #A8D8B9, #C5E8D0)',
  '核心': 'linear-gradient(135deg, #F0A8A8, #F5C0C0)',
  '有氧': 'linear-gradient(135deg, #7EB8DA, #B8A9C9)'
}

function getExerciseImage(exerciseName, muscleGroup) {
  // 优先按运动名称匹配
  if (exerciseImageMap[exerciseName]) {
    return exerciseImageMap[exerciseName]
  }
  // 其次按肌群匹配
  return muscleImageMap[muscleGroup] || imgPushup
}

function getMuscleGradient(group) {
  return muscleGradients[group] || 'linear-gradient(135deg, #7EB8DA, #A8D4E6)'
}

function getDifficultyLevel(diff) {
  if (diff === '初级') return 'easy'
  if (diff === '中级') return 'medium'
  return 'hard'
}

function selectMuscleGroup(group) {
  filters.muscleGroup = group === '全部' ? '' : group
  page.value = 1
  fetchExercises()
}

function resetToFirstPageAndFetch() {
  page.value = 1
  fetchExercises()
}

async function fetchExercises() {
  try {
    const params = new URLSearchParams()
    params.append('page', page.value)
    params.append('size', size.value)
    if (filters.exerciseName) params.append('exerciseName', filters.exerciseName)
    if (filters.muscleGroup) params.append('muscleGroup', filters.muscleGroup)
    if (filters.difficulty) params.append('difficulty', filters.difficulty)
    const res = await request(`/exercises?${params}`)
    if (res && res.records) {
      const rows = Array.isArray(res.records) ? res.records : []
      exercises.value = rows
      total.value = Number(res.total || rows.length)
    } else {
      const rows = Array.isArray(res) ? res : []
      exercises.value = rows
      total.value = rows.length
    }
  } catch { /* ignore */ }
}

function handleCurrentChange(nextPage) {
  const normalizedPage = Number(nextPage) || 1
  if (normalizedPage === page.value) return
  page.value = normalizedPage
  fetchExercises()
}

function handleSizeChange(nextSize) {
  const normalizedSize = Number(nextSize) || size.value
  if (normalizedSize === size.value) return
  size.value = normalizedSize
  page.value = 1
  fetchExercises()
}

function openDialog(ex) {
  if (ex) {
    Object.assign(dialogForm, { id: ex.id, exerciseName: ex.exerciseName, muscleGroup: ex.muscleGroup, difficulty: ex.difficulty || '初级', caloriesPerMinute: ex.caloriesPerMinute, description: ex.description || '' })
  } else {
    Object.assign(dialogForm, { id: null, exerciseName: '', muscleGroup: '', difficulty: '初级', caloriesPerMinute: null, description: '' })
  }
  dialogVisible.value = true
}

async function handleSave() {
  if (!dialogForm.exerciseName || !dialogForm.muscleGroup) {
    ElMessage.warning('运动名称和肌群为必填')
    return
  }
  try {
    const body = JSON.stringify(dialogForm)
    if (dialogForm.id) {
      await request(`/exercises/${dialogForm.id}`, { method: 'PUT', body })
    } else {
      await request('/exercises', { method: 'POST', body })
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchExercises()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定删除此运动？', '提示', { type: 'warning' })
    await request(`/exercises/${id}`, { method: 'DELETE' })
    ElMessage.success('删除成功')
    fetchExercises()
  } catch { /* cancelled */ }
}

onMounted(() => {
  const user = JSON.parse(localStorage.getItem('user') || '{}')
  isAdmin.value = user.role === 'admin'
  fetchExercises()
})
</script>

<style scoped>
.exercise-container {
  position: relative;
  min-height: 100vh;
  padding: 36px 56px 36px 128px;
  color: var(--text-primary);
  overflow: hidden;
}

.solid-card {
  position: relative;
  background: #FFFFFF;
  border: 1px solid #E8ECF1;
  border-radius: 18px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06), 0 1px 3px rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

.filter-card {
  padding: 18px 22px;
  margin-bottom: 16px;
  position: relative;
  z-index: 1;
}

.filter-bar {
  position: relative;
  z-index: 1;
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.filter-left {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.filter-right {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.muscle-chips {
  position: relative;
  z-index: 1;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 20px;
}

.chip {
  padding: 6px 16px;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
  background: #F8F6F3;
  border: 1px solid #E8ECF1;
  border-radius: 999px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.chip:hover {
  color: var(--text-primary);
  background: #F5F7FA;
  border-color: #B8DDEF;
}

.chip--active {
  color: #FFFFFF;
  background: #7EB8DA;
  border-color: transparent;
  box-shadow: 0 2px 8px rgba(126, 184, 218, 0.3);
}

.exercise-grid {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 20px;
}

.exercise-card {
  display: flex;
  flex-direction: column;
  transition: transform 0.22s ease, box-shadow 0.22s ease;
}

.exercise-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.card-visual {
  position: relative;
  height: 180px;
  overflow: hidden;
  background: #F8F6F3;
}

.card-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.exercise-card:hover .card-image {
  transform: scale(1.05);
}

.card-body {
  padding: 18px 20px 20px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.card-name {
  margin: 0 0 10px;
  font-size: 17px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
}

.card-tags {
  display: flex;
  gap: 6px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

.tag {
  padding: 3px 10px;
  font-size: 11px;
  font-weight: 600;
  border-radius: 999px;
}

.tag--muscle {
  color: #7EB8DA;
  background: rgba(126, 184, 218, 0.1);
  border: 1px solid transparent;
}

.tag--easy {
  color: #7BC6A0;
  background: rgba(123, 198, 160, 0.1);
  border: 1px solid transparent;
}

.tag--medium {
  color: #E8C468;
  background: rgba(232, 196, 104, 0.1);
  border: 1px solid transparent;
}

.tag--hard {
  color: #E88B7A;
  background: rgba(232, 139, 122, 0.1);
  border: 1px solid transparent;
}

.card-desc {
  margin: 0 0 12px;
  color: var(--text-secondary);
  font-size: 13px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex: 1;
}

.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 12px;
  border-top: 1px solid #E8ECF1;
}

.card-cal {
  color: #7EB8DA;
  font-size: 13px;
  font-weight: 700;
}

.card-actions {
  display: flex;
  gap: 10px;
}

.plan-action {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 10px 22px;
  border: 0;
  cursor: pointer;
  background: #7EB8DA;
  color: #FFFFFF;
  font-size: 14px;
  font-weight: 700;
  border-radius: 999px;
  box-shadow: 0 2px 8px rgba(126, 184, 218, 0.3);
  transition: all 0.2s ease;
}

.plan-action:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 14px rgba(126, 184, 218, 0.4);
}

.plan-action--ghost {
  background: transparent;
  color: var(--text-secondary);
  box-shadow: none;
  border: 1px solid #E8ECF1;
}

.plan-action--ghost:hover {
  border-color: #7EB8DA;
  color: #7EB8DA;
}

.link-btn {
  background: none;
  border: 0;
  color: #7EB8DA;
  cursor: pointer;
  font-size: 13px;
  font-weight: 600;
  padding: 0;
}

.link-btn:hover {
  color: #5A9BC0;
}

.link-danger {
  color: #F0A8A8;
}

.link-danger:hover {
  color: #E88B7A;
}

.pagination-wrap {
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

@media (max-width: 1200px) {
  .exercise-container { padding-left: 100px; padding-right: 24px; }
}

@media (max-width: 992px) {
  .exercise-container { padding: 32px 20px; }
}

@media (max-width: 640px) {
  .exercise-container { padding: 24px 16px; }
  .exercise-grid { grid-template-columns: 1fr; }
}
</style>
