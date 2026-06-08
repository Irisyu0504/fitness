import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import test from 'node:test'

const panelSource = readFileSync(new URL('../src/components/diet/NutritionAnalysisPanel.vue', import.meta.url), 'utf8')
const formSource = readFileSync(new URL('../src/components/diet/DietRecordForm.vue', import.meta.url), 'utf8')
const viewSource = readFileSync(new URL('../src/views/DietRecordView.vue', import.meta.url), 'utf8')

test('diet record time pickers keep minute precision only', () => {
  assert.match(formSource, /value-format="YYYY-MM-DDTHH:mm"/)
  assert.match(formSource, /format="YYYY-MM-DD HH:mm"/)
  assert.match(formSource, /function formatMinuteDateTime/)
  assert.doesNotMatch(formSource, /YYYY-MM-DDTHH:mm:ss/)

  assert.match(viewSource, /value-format="YYYY-MM-DDTHH:mm"/)
  assert.match(viewSource, /format="YYYY-MM-DD HH:mm"/)
  assert.match(viewSource, /function toMinuteDateTime/)
  assert.doesNotMatch(viewSource, /YYYY-MM-DDTHH:mm:ss/)
})

test('nutrition panel is driven by current day macro analysis without extra insight sections', () => {
  assert.match(panelSource, /macroPercentages/)
  assert.match(panelSource, /macroReminder/)
  assert.match(panelSource, /今天还未记录，快来添加饮食记录吧/)
  assert.doesNotMatch(panelSource, /progress-section/)
  assert.doesNotMatch(panelSource, /meal-dist/)
  assert.doesNotMatch(panelSource, /insight-section/)

  assert.doesNotMatch(viewSource, /:calorie-goal/)
  assert.doesNotMatch(viewSource, /fetchCalorieGoal/)
})
