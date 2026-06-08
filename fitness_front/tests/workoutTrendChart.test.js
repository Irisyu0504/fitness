import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import test from 'node:test'

const workoutView = readFileSync(new URL('../src/views/WorkoutRecordView.vue', import.meta.url), 'utf8')
const trendChart = readFileSync(new URL('../src/components/workout/TrainingTrendChart.vue', import.meta.url), 'utf8')

test('workout trend chart is fed by backend records across daily weekly and monthly ranges', () => {
  assert.match(workoutView, /request\('\/workout-records\/detail'\)/)
  assert.match(workoutView, /const trendTimeRange = ref\('7days'\)/)
  assert.match(workoutView, /:active-time-range="trendTimeRange"/)
  assert.match(workoutView, /@time-change="trendTimeRange = \$event"/)
  assert.match(workoutView, /aggregateTrainingByWeek/)
  assert.match(workoutView, /aggregateTrainingByMonth/)
  assert.doesNotMatch(workoutView, /const\s+(?:mock|sample|static)\w*\s*=/i)
})

test('workout trend aggregation and duration axis stay numeric and tightly bounded', () => {
  assert.match(workoutView, /duration \+= Number\(r\.duration \|\| 0\)/)
  assert.match(trendChart, /const numericValues = values\.map\(value => Number\(value\) \|\| 0\)/)
  assert.match(trendChart, /const yAxisMax = computed/)
  assert.match(trendChart, /max: yAxisMax\.value/)
  assert.doesNotMatch(trendChart, /scale:\s*true/)
})
