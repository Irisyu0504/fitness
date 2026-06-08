import assert from 'node:assert/strict'
import { existsSync, readFileSync } from 'node:fs'
import test from 'node:test'

const componentUrl = new URL('../src/components/ActivityRingCard.vue', import.meta.url)
const activityRingCardVue = existsSync(componentUrl) ? readFileSync(componentUrl, 'utf8') : ''

const cssBlock = (selector) => {
  const match = activityRingCardVue.match(new RegExp(`${selector.replace('.', '\\.')}\\s*\\{([^}]*)\\}`))
  return match?.[1] ?? ''
}

test('ActivityRingCard component exists with calorie deficit content', () => {
  assert.ok(existsSync(componentUrl))
  assert.match(activityRingCardVue, /今日热量缺口/)
  assert.match(activityRingCardVue, /intake:\s*1800/)
  assert.match(activityRingCardVue, /burned:\s*2120/)
  assert.match(activityRingCardVue, /calorieStats\.value\.burned\s*-\s*calorieStats\.value\.intake/)
  assert.match(activityRingCardVue, /摄入/)
  assert.match(activityRingCardVue, /消耗/)
})

test('ActivityRingCard renders a glass card with two ambient glow layers', () => {
  assert.match(cssBlock('.activity-ring-card'), /height:\s*280px/)
  assert.match(cssBlock('.activity-ring-card'), /background:\s*rgba\(255,\s*255,\s*255,\s*0\.02\)/)
  assert.match(cssBlock('.activity-ring-card'), /border-radius:\s*24px/)
  assert.match(cssBlock('.activity-ring-card'), /border:\s*1px\s+solid\s+rgba\(255,\s*255,\s*255/)
  assert.match(activityRingCardVue, /class="ambient-glow glow-primary"/)
  assert.match(activityRingCardVue, /class="ambient-glow glow-secondary"/)
  assert.match(cssBlock('.ambient-glow'), /filter:\s*blur\(60px\)/)
})

test('ActivityRingCard uses data-driven double SVG rings and foreground flex layout', () => {
  assert.match(activityRingCardVue, /const\s+calorieStats\s*=\s*ref/)
  assert.match(activityRingCardVue, /strokeDasharray/)
  assert.match(activityRingCardVue, /intakeDasharray/)
  assert.match(activityRingCardVue, /burnedDasharray/)
  assert.match(activityRingCardVue, /class="ring-progress intake-ring"/)
  assert.match(activityRingCardVue, /class="ring-progress burned-ring"/)
  assert.match(cssBlock('.card-content'), /display:\s*flex/)
  assert.match(cssBlock('.card-content'), /z-index:\s*2/)
})
