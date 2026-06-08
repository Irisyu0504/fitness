<template>
  <div class="admin-exercises">
    <div class="page-header-row">
      <h1 class="page-title">动作库管理</h1>
      <button class="btn-primary" @click="openDialog('create')">+ 新增动作</button>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-input v-model="filters.name" placeholder="搜索动作名称" clearable @keyup.enter="fetchExercises" style="max-width: 200px" />
      <el-select v-model="filters.muscleGroup" placeholder="训练部位" clearable style="max-width: 140px">
        <el-option label="胸部" value="胸部" /><el-option label="背部" value="背部" />
        <el-option label="肩部" value="肩部" /><el-option label="手臂" value="手臂" />
        <el-option label="腿部" value="腿部" /><el-option label="核心" value="核心" />
        <el-option label="有氧" value="有氧" />
      </el-select>
      <el-select v-model="filters.difficulty" placeholder="难度" clearable style="max-width: 120px">
        <el-option label="初级" value="初级" /><el-option label="中级" value="中级" /><el-option label="高级" value="高级" />
      </el-select>
      <button class="btn-outline" @click="fetchExercises">筛选</button>
    </div>

    <!-- 表格 -->
    <div class="table-card">
      <table class="data-table">
        <thead>
          <tr>
            <th>动作名称</th>
            <th>训练部位</th>
            <th>难度</th>
            <th>卡路里/分钟</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="ex in exercises" :key="ex.id">
            <td class="cell-bold">{{ ex.exerciseName }}</td>
            <td><span class="tag">{{ ex.muscleGroup }}</span></td>
            <td><span class="difficulty-tag" :class="ex.difficulty">{{ ex.difficulty }}</span></td>
            <td>{{ ex.caloriesPerMinute || '--' }}</td>
            <td>
              <span class="status-dot" :class="ex.status == 1 ? 'on' : 'off'"></span>
              {{ ex.status == 1 ? '启用' : '禁用' }}
            </td>
            <td class="cell-actions">
              <button class="btn-sm btn-blue" @click="openDialog('edit', ex)">编辑</button>
              <button class="btn-sm btn-red" @click="handleDelete(ex)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-if="exercises.length === 0" class="empty-text">暂无动作数据</p>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogMode === 'create' ? '新增动作' : '编辑动作'" width="480px" :close-on-click-modal="false">
      <el-form label-position="top" :model="form">
        <el-form-item label="动作名称" required>
          <el-input v-model="form.exerciseName" placeholder="如：卧推" />
        </el-form-item>
        <div class="form-row">
          <el-form-item label="训练部位" required>
            <el-select v-model="form.muscleGroup" placeholder="选择部位">
              <el-option label="胸部" value="胸部" /><el-option label="背部" value="背部" />
              <el-option label="肩部" value="肩部" /><el-option label="手臂" value="手臂" />
              <el-option label="腿部" value="腿部" /><el-option label="核心" value="核心" />
              <el-option label="有氧" value="有氧" />
            </el-select>
          </el-form-item>
          <el-form-item label="难度">
            <el-select v-model="form.difficulty" placeholder="选择难度">
              <el-option label="初级" value="初级" /><el-option label="中级" value="中级" /><el-option label="高级" value="高级" />
            </el-select>
          </el-form-item>
        </div>
        <el-form-item label="每分钟消耗 (kcal)">
          <el-input-number v-model="form.caloriesPerMinute" :min="0" :max="50" :precision="1" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="动作描述或注意事项" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status">
            <el-option label="启用" :value="1" /><el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <button class="btn-cancel" @click="dialogVisible = false">取消</button>
        <button class="btn-confirm" @click="handleSave">保存</button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { request } from '@/api/request.js'

const exercises = ref([])
const filters = reactive({ name: '', muscleGroup: '', difficulty: '' })

const dialogVisible = ref(false)
const dialogMode = ref('create')
const editingId = ref(null)
const form = reactive({
  exerciseName: '',
  muscleGroup: '',
  difficulty: '初级',
  caloriesPerMinute: null,
  description: '',
  status: 1
})

async function fetchExercises() {
  try {
    const params = new URLSearchParams()
    if (filters.name) params.set('exerciseName', filters.name)
    if (filters.muscleGroup) params.set('muscleGroup', filters.muscleGroup)
    if (filters.difficulty) params.set('difficulty', filters.difficulty)
    const query = params.toString() ? `?${params}` : ''
    const data = await request(`/exercises${query}`)
    exercises.value = Array.isArray(data) ? data : (data?.records || [])
  } catch (e) {
    ElMessage.error(e.message || '获取动作失败')
  }
}

function openDialog(mode, ex = null) {
  dialogMode.value = mode
  if (mode === 'edit' && ex) {
    editingId.value = ex.id
    Object.assign(form, {
      exerciseName: ex.exerciseName,
      muscleGroup: ex.muscleGroup,
      difficulty: ex.difficulty || '初级',
      caloriesPerMinute: ex.caloriesPerMinute,
      description: ex.description || '',
      status: ex.status ?? 1
    })
  } else {
    editingId.value = null
    Object.assign(form, {
      exerciseName: '', muscleGroup: '', difficulty: '初级',
      caloriesPerMinute: null, description: '', status: 1
    })
  }
  dialogVisible.value = true
}

async function handleSave() {
  if (!form.exerciseName || !form.muscleGroup) {
    return ElMessage.warning('请填写动作名称和训练部位')
  }
  try {
    if (dialogMode.value === 'create') {
      await request('/exercises', { method: 'POST', body: JSON.stringify(form) })
      ElMessage.success('新增成功')
    } else {
      await request(`/exercises/${editingId.value}`, { method: 'PUT', body: JSON.stringify(form) })
      ElMessage.success('更新成功')
    }
    dialogVisible.value = false
    await fetchExercises()
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  }
}

async function handleDelete(ex) {
  try {
    await ElMessageBox.confirm(`确定删除动作「${ex.exerciseName}」？`, '提示', { type: 'warning' })
    await request(`/exercises/${ex.id}`, { method: 'DELETE' })
    ElMessage.success('删除成功')
    await fetchExercises()
  } catch { /* 取消 */ }
}

onMounted(fetchExercises)
</script>

<style scoped>
.admin-exercises { max-width: 1200px; }

.page-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.page-title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
}

.btn-primary {
  padding: 8px 20px;
  background: #7EB8DA;
  color: #FFFFFF;
  border: none;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
}

.btn-primary:hover { background: #6AABC8; }

.filter-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.filter-bar :deep(.el-input__wrapper),
.filter-bar :deep(.el-select .el-input__wrapper) {
  border-radius: 10px;
  background: #FFFFFF;
  border: 1px solid #E8ECF1;
  box-shadow: none;
}

.btn-outline {
  padding: 0 16px;
  height: 36px;
  background: #FFFFFF;
  border: 1px solid #E8ECF1;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  color: var(--text-secondary);
}

.btn-outline:hover { border-color: #7EB8DA; color: #7EB8DA; }

/* ── 表格 ── */
.table-card {
  background: #FFFFFF;
  border: 1px solid #E8ECF1;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.data-table th {
  padding: 14px 16px;
  text-align: left;
  font-size: 12px;
  font-weight: 700;
  color: var(--text-secondary);
  background: #F8F9FA;
  border-bottom: 1px solid #E8ECF1;
  white-space: nowrap;
}

.data-table td {
  padding: 12px 16px;
  border-bottom: 1px solid #F0F2F5;
  color: var(--text-primary);
}

.data-table tr:last-child td { border-bottom: none; }

.cell-bold { font-weight: 700; }

.cell-actions {
  display: flex;
  gap: 8px;
}

.tag {
  padding: 2px 8px;
  font-size: 11px;
  font-weight: 700;
  background: #F0F2F5;
  border-radius: 6px;
  color: var(--text-secondary);
}

.difficulty-tag {
  padding: 2px 8px;
  font-size: 11px;
  font-weight: 700;
  border-radius: 6px;
}

.difficulty-tag.初级 { background: rgba(168, 216, 185, 0.15); color: #5a9e6f; }
.difficulty-tag.中级 { background: rgba(255, 219, 105, 0.2); color: #b8860b; }
.difficulty-tag.高级 { background: rgba(240, 168, 168, 0.15); color: #c07070; }

.status-dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  margin-right: 4px;
}

.status-dot.on { background: #A8D8B9; }
.status-dot.off { background: #F0A8A8; }

.btn-sm {
  padding: 4px 12px;
  border: none;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
}

.btn-blue { background: rgba(126, 184, 218, 0.15); color: #5a8ea8; }
.btn-blue:hover { background: rgba(126, 184, 218, 0.25); }
.btn-red { background: rgba(240, 168, 168, 0.15); color: #c07070; }
.btn-red:hover { background: rgba(240, 168, 168, 0.25); }

.empty-text {
  padding: 40px;
  text-align: center;
  color: var(--text-tertiary);
}

/* ── 弹窗 ── */
.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.btn-cancel {
  padding: 8px 20px;
  background: #F8F6F3;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  cursor: pointer;
  color: var(--text-secondary);
}

.btn-confirm {
  padding: 8px 20px;
  background: #7EB8DA;
  color: #FFFFFF;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
}

.btn-confirm:hover { background: #6AABC8; }
</style>
