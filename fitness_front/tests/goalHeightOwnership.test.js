import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import test from 'node:test'

const goalsView = readFileSync(new URL('../src/views/GoalsView.vue', import.meta.url), 'utf8')
const bodyRecordController = readFileSync(
  new URL('../../fitness_backend/src/main/java/org/example/fitness_backend/controller/BodyRecordController.java', import.meta.url),
  'utf8'
)
const statController = readFileSync(
  new URL('../../fitness_backend/src/main/java/org/example/fitness_backend/controller/StatController.java', import.meta.url),
  'utf8'
)

test('fitness goal dialog does not own or submit profile height', () => {
  assert.doesNotMatch(goalsView, /goalForm\.height/)
  assert.doesNotMatch(goalsView, /label="身高 \(cm\)"/)
})

test('body record read endpoints refresh BMI from the current profile height', () => {
  assert.match(
    bodyRecordController,
    /pageResult\.getRecords\(\)\.forEach\(bodyRecordService::calculateAndSetBmi\)/
  )
  assert.match(
    bodyRecordController,
    /List<BodyRecord>\s+list\s*=\s*bodyRecordService\.list\(queryWrapper\);\s*list\.forEach\(bodyRecordService::calculateAndSetBmi\);/s
  )
  assert.match(
    bodyRecordController,
    /List<BodyRecord>\s+list\s*=\s*bodyRecordService\.list\(queryWrapper\);\s*list\.forEach\(bodyRecordService::calculateAndSetBmi\);/s
  )
  assert.match(
    statController,
    /List<BodyRecord>\s+records\s*=\s*bodyRecordService\.list\(queryWrapper\);\s*records\.forEach\(bodyRecordService::calculateAndSetBmi\);\s*return Result\.success/s
  )
})
