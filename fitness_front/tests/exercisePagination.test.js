import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import test from 'node:test'

const source = readFileSync(new URL('../src/views/ExerciseListView.vue', import.meta.url), 'utf8')

test('exercise list uses numeric backend total and explicit pagination handlers', () => {
  assert.match(source, /:current-page="page"/)
  assert.match(source, /:page-size="size"/)
  assert.match(source, /@current-change="handleCurrentChange"/)
  assert.match(source, /@update:current-page="handleCurrentChange"/)
  assert.match(source, /@size-change="handleSizeChange"/)
  assert.match(source, /@update:page-size="handleSizeChange"/)
  assert.match(source, /total\.value = Number\(res\.total \|\| rows\.length\)/)
  assert.match(source, /function handleCurrentChange\(nextPage\)/)
  assert.match(source, /const normalizedPage = Number\(nextPage\) \|\| 1/)
  assert.match(source, /if \(normalizedPage === page\.value\) return/)
  assert.match(source, /function handleSizeChange\(nextSize\)/)
  assert.doesNotMatch(source, /v-model:current-page/)
  assert.doesNotMatch(source, /v-model:page-size/)
})

test('exercise filters reset to the first page before fetching', () => {
  assert.match(source, /function resetToFirstPageAndFetch\(\)/)
  assert.match(source, /page\.value = 1\s+fetchExercises\(\)/)
  assert.match(source, /@clear="resetToFirstPageAndFetch"/)
  assert.match(source, /@keyup\.enter="resetToFirstPageAndFetch"/)
  assert.match(source, /@change="resetToFirstPageAndFetch"/)
  assert.match(source, /@click="resetToFirstPageAndFetch"/)
  assert.match(source, /selectMuscleGroup\(group\)[\s\S]*page\.value = 1[\s\S]*fetchExercises\(\)/)
})
