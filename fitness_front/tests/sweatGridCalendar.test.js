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

test('SweatGrid renders a training calendar header and weekday row', () => {
  assert.match(sweatGridVue, /Your Training Days/)
  assert.match(sweatGridVue, /currentMonthLabel/)
  assert.match(sweatGridVue, /weekDays/)
  assert.match(sweatGridVue, /M',\s*'T',\s*'W',\s*'T',\s*'F',\s*'S',\s*'S/)
})

test('SweatGrid generates current-month calendar days with done and scheduled states', () => {
  assert.match(sweatGridVue, /const\s+calendarDays\s*=\s*computed/)
  assert.match(sweatGridVue, /new\s+Date\(today\.getFullYear\(\),\s*today\.getMonth\(\),\s*1\)/)
  assert.match(sweatGridVue, /doneDays\.has\(date\)\s*\?\s*'done'/)
  assert.match(sweatGridVue, /scheduledDays\.has\(date\)\s*\?\s*'scheduled'/)
  assert.match(sweatGridVue, /isCurrentDay/)
})

test('SweatGrid uses a soft dark shell and centered circular day states', () => {
  assert.match(cssBlock('.sweat-grid-wrapper'), /background:\s*rgba\(0,\s*0,\s*0,\s*0\.15\)/)
  assert.match(cssBlock('.sweat-grid-wrapper'), /border-radius:\s*24px/)
  assert.match(cssBlock('.sweat-grid-wrapper'), /padding:\s*24px/)
  assert.match(cssBlock('.calendar-grid'), /grid-template-columns:\s*repeat\(7,\s*1fr\)/)
  assert.match(cssBlock('.day-status'), /display:\s*grid/)
  assert.match(cssBlock('.day-status'), /place-items:\s*center/)
  assert.match(cssBlock('.day-status.done'), /border-radius:\s*50%/)
  assert.match(cssBlock('.day-status.done'), /background:\s*#67e8f9/)
  assert.match(cssBlock('.day-status.current'), /border:\s*1px\s+solid\s+rgba\(103,\s*232,\s*249/)
})
