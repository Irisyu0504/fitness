import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import test from 'node:test'

const sweatGridVue = readFileSync(
  new URL('../src/components/SweatGrid.vue', import.meta.url),
  'utf8'
)

const cssBlock = (selector) => {
  const match = sweatGridVue.match(new RegExp(`${selector.replace('.', '\\.')}\\s*\\{([^}]*)\\}`))
  return match?.[1] ?? ''
}

test('SweatGrid renders a training calendar header and localized month label', () => {
  assert.match(sweatGridVue, /monthLabel/)
  assert.match(sweatGridVue, /weekDays/)
  assert.match(sweatGridVue, /toLocaleDateString\('zh-CN',\s*\{\s*year:\s*'numeric',\s*month:\s*'long'\s*\}\)/)
  assert.match(sweatGridVue, /class="week-row"/)
})

test('SweatGrid generates current month calendar days with done and idle states', () => {
  assert.match(sweatGridVue, /const\s+calendarDays\s*=\s*computed/)
  assert.match(sweatGridVue, /const\s+firstDay\s*=\s*new\s+Date\(viewYear\.value,\s*viewMonth\.value,\s*1\)/)
  assert.match(sweatGridVue, /done\.has\(dateStr\)\s*\?\s*'done'\s*:\s*'idle'/)
  assert.match(sweatGridVue, /leadingEmpty\s*=\s*\(firstDay\.getDay\(\)\s*\+\s*6\)\s*%\s*7/)
  assert.match(sweatGridVue, /isCurrentDay/)
})

test('SweatGrid uses a transparent shell and centered circular day states', () => {
  assert.match(cssBlock('.sweat-grid-wrapper'), /background:\s*transparent/)
  assert.match(cssBlock('.sweat-grid-wrapper'), /border-radius:\s*24px/)
  assert.match(cssBlock('.sweat-grid-wrapper'), /padding:\s*16px/)
  assert.match(cssBlock('.calendar-grid'), /grid-template-columns:\s*repeat\(7,\s*1fr\)/)
  assert.match(cssBlock('.day-status'), /display:\s*grid/)
  assert.match(cssBlock('.day-status'), /place-items:\s*center/)
  assert.match(cssBlock('.day-status.done'), /border-radius:\s*50%/)
  assert.match(cssBlock('.day-status.done'), /background:\s*#7EB8DA/)
  assert.match(cssBlock('.day-status.current'), /border:\s*2px\s+solid\s+#7EB8DA/)
})
