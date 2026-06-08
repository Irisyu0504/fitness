import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import test from 'node:test'

const source = readFileSync(new URL('../src/components/workout/TrainingHistoryTable.vue', import.meta.url), 'utf8')

test('training history pagination emits Element Plus page payload instead of stale props', () => {
  assert.match(source, /:current-page="page"/)
  assert.match(source, /:page-size="size"/)
  assert.match(source, /@current-change="handleCurrentChange"/)
  assert.match(source, /@size-change="handleSizeChange"/)
  assert.match(source, /function handleCurrentChange\(nextPage\)/)
  assert.match(source, /page: Number\(nextPage\) \|\| 1/)
  assert.match(source, /function handleSizeChange\(nextSize\)/)
  assert.doesNotMatch(source, /v-model:current-page/)
  assert.doesNotMatch(source, /v-model:page-size/)
})
