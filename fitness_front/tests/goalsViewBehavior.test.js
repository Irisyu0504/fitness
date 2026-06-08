import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import test from 'node:test'

const source = readFileSync(new URL('../src/views/GoalsView.vue', import.meta.url), 'utf8')

test('goals page no longer renders the weight trend card', () => {
  assert.doesNotMatch(source, /体重趋势/)
  assert.doesNotMatch(source, /trendChartRef/)
  assert.doesNotMatch(source, /renderTrendChart/)
  assert.doesNotMatch(source, /\/stat\/weight-trend/)
  assert.doesNotMatch(source, /goalData\.trend/)
})

test('goal dialog keeps target date and weekly target matched automatically', () => {
  assert.match(source, /function calculateGoalWeeks/)
  assert.match(source, /function syncWeeklyTargetFromTargetDate/)
  assert.match(source, /function syncTargetDateFromWeeklyTarget/)
  assert.match(source, /const isSyncingGoalSchedule = ref\(false\)/)
  assert.match(source, /watch\(\[\(\) => goalForm\.currentWeight/)
  assert.match(source, /watch\(\(\) => goalForm\.weeklyTarget/)
})

test('deleting a goal resets page state by refetching the current goal', () => {
  assert.match(source, /await request\(`\/fitnessGoals\/\$\{goalData\.id\}`/)
  assert.match(source, /await fetchGoalData\(\)/)
  assert.match(source, /resetGoalState\(\)/)
})
