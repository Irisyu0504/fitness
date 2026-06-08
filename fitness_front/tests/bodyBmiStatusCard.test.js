import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import test from 'node:test'

const viewSource = readFileSync(new URL('../src/views/BodyRecordView.vue', import.meta.url), 'utf8')
const bmiCardSource = readFileSync(new URL('../src/components/body/BmiStatusCard.vue', import.meta.url), 'utf8')

test('body page removes data insight and lets BMI status card replace that space', () => {
  assert.doesNotMatch(viewSource, /BodyInsightCard/)
  assert.doesNotMatch(viewSource, /insightText/)
  assert.match(viewSource, /<BmiStatusCard :bmi="latestRecord\?\.bmi" \/>/)
  assert.match(viewSource, /grid-template-columns:\s*1fr;/)
})

test('BMI status card shows Chinese adult BMI category boundary values', () => {
  assert.match(bmiCardSource, /<18\.5/)
  assert.match(bmiCardSource, /18\.5-23\.9/)
  assert.match(bmiCardSource, /24\.0-27\.9/)
  assert.match(bmiCardSource, /≥28\.0/)
  assert.match(bmiCardSource, /bmi-boundary-grid/)
  assert.match(bmiCardSource, /bmi-content/)
  assert.match(bmiCardSource, /grid-template-columns:\s*minmax\(180px,\s*0\.8fr\)\s*minmax\(0,\s*1\.6fr\)/)
})
