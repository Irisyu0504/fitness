function hasOwn(value, key) {
  return Object.prototype.hasOwnProperty.call(value, key)
}

function firstDefined(...values) {
  return values.find(value => value !== undefined && value !== null)
}

const workoutMetricKeys = new Set(['duration', 'caloriesBurned', 'setsCount', 'reps'])

function toOptionalNumber(value) {
  if (value === null || value === undefined || value === '') return value
  const number = Number(value)
  return Number.isFinite(number) ? number : value
}

export function normalizeWorkoutDetail(item = {}) {
  const record = item.record && typeof item.record === 'object' ? item.record : item
  const normalized = { ...record }

  for (const key of [
    'id',
    'recordDate',
    'duration',
    'caloriesBurned',
    'exerciseId',
    'planId',
    'setsCount',
    'reps',
    'remark',
    'exerciseName',
    'muscleGroup',
    'difficulty',
    'planName'
  ]) {
    const value = firstDefined(item[key], record[key])
    if (value !== undefined) {
      normalized[key] = workoutMetricKeys.has(key) ? toOptionalNumber(value) : value
    }
  }

  return normalized
}

export function normalizeWorkoutDetails(payload) {
  const list = Array.isArray(payload?.records)
    ? payload.records
    : Array.isArray(payload)
      ? payload
      : []

  return list.map(normalizeWorkoutDetail)
}

export function normalizeGoalInfo(goalInfo) {
  const info = goalInfo || {}
  const goal = info.goal && typeof info.goal === 'object' ? info.goal : info
  const hasGoal = hasOwn(info, 'hasGoal') ? Boolean(info.hasGoal) : Boolean(goal?.id || goal?.goalType)

  return {
    hasGoal,
    goalType: firstDefined(goal.goalType, info.goalType, ''),
    startWeight: Number(firstDefined(goal.currentWeight, info.startWeight, 0)),
    currentWeight: Number(firstDefined(info.latestWeight, info.currentWeight, goal.currentWeight, 0)),
    targetWeight: firstDefined(goal.targetWeight, info.targetWeight, null),
    progress: firstDefined(info.progressRate, info.progress, undefined),
    weeklyTarget: firstDefined(goal.weeklyTarget, info.weeklyTarget, null),
    startDate: firstDefined(goal.startDate, info.startDate, null),
    targetDate: firstDefined(goal.targetDate, info.targetDate, null)
  }
}
