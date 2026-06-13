import assert from 'node:assert/strict'
import test from 'node:test'

import {
  normalizeGoalInfo,
  normalizeWorkoutDetails
} from '../src/api/normalizers.js'

test('normalizes paged workout detail records without changing flat rows', () => {
  const rows = normalizeWorkoutDetails({
    records: [
      {
        id: 1,
        recordDate: '2026-05-27',
        duration: 40,
        caloriesBurned: 440,
        exerciseName: 'Run',
        muscleGroup: 'Cardio',
        planName: 'Plan A'
      }
    ],
    total: 1
  })

  assert.deepEqual(rows, [
    {
      id: 1,
      recordDate: '2026-05-27',
      duration: 40,
      caloriesBurned: 440,
      exerciseName: 'Run',
      muscleGroup: 'Cardio',
      planName: 'Plan A'
    }
  ])
})

test('normalizes unpaged workout detail records returned with nested record objects', () => {
  const rows = normalizeWorkoutDetails([
    {
      record: {
        id: 2,
        recordDate: '2026-05-26',
        duration: 30,
        caloriesBurned: 240,
        exerciseId: 20001,
        planId: 9,
        setsCount: 4,
        reps: 12,
        remark: 'good'
      },
      exerciseName: 'Bench Press',
      muscleGroup: 'Chest',
      difficulty: 'Medium',
      planName: 'Plan B'
    }
  ])

  assert.deepEqual(rows, [
    {
      id: 2,
      recordDate: '2026-05-26',
      duration: 30,
      caloriesBurned: 240,
      exerciseId: 20001,
      planId: 9,
      setsCount: 4,
      reps: 12,
      remark: 'good',
      exerciseName: 'Bench Press',
      muscleGroup: 'Chest',
      difficulty: 'Medium',
      planName: 'Plan B'
    }
  ])
})

test('coerces workout metric fields returned as JSON strings', () => {
  const rows = normalizeWorkoutDetails([
    {
      record: {
        id: '2099000000000000301',
        recordDate: '2026-04-12',
        duration: '45',
        caloriesBurned: '382.5',
        setsCount: '4',
        reps: '12',
        exerciseId: '20003',
        planId: '2099000000000000200'
      },
      exerciseName: 'push-up'
    }
  ])

  assert.equal(rows[0].duration, 45)
  assert.equal(rows[0].caloriesBurned, 382.5)
  assert.equal(rows[0].setsCount, 4)
  assert.equal(rows[0].reps, 12)
  assert.equal(rows[0].id, '2099000000000000301')
  assert.equal(rows[0].planId, '2099000000000000200')
})

test('normalizes dashboard goal progress from backend goalInfo shape', () => {
  const goal = normalizeGoalInfo({
    hasGoal: true,
    goal: {
      goalType: 'fat-loss',
      currentWeight: 82.5,
      targetWeight: 75
    },
    latestWeight: 79.8,
    progressRate: 36
  })

  assert.equal(goal.hasGoal, true)
  assert.equal(goal.goalType, 'fat-loss')
  assert.equal(goal.startWeight, 82.5)
  assert.equal(goal.currentWeight, 79.8)
  assert.equal(goal.targetWeight, 75)
  assert.equal(goal.progress, 36)
  assert.equal(goal.weeklyTarget, undefined)
  assert.equal(goal.startDate, undefined)
  assert.equal(goal.targetDate, undefined)
})
