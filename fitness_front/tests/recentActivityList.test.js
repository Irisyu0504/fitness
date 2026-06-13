import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import test from 'node:test'

const dashboardView = readFileSync(new URL('../src/views/DashboardView.vue', import.meta.url), 'utf8')

const cssBlock = (selector) => {
  const match = dashboardView.match(new RegExp(`${selector.replace('.', '\\.')}\\s*\\{([^}]*)\\}`))
  return match?.[1] ?? ''
}

test('dashboard replaces recent activity list with a goal progress plan card', () => {
  assert.doesNotMatch(dashboardView, /RecentActivityList/)
  assert.match(dashboardView, /class="glass-card plan-card"/)
  assert.match(dashboardView, /goalInfo\.progress !== undefined/)
  assert.match(dashboardView, /class="progress-track"/)
  assert.match(dashboardView, /class="progress-fill"/)
  assert.match(dashboardView, /weeklyTargetText/)
  assert.match(dashboardView, /daysRemaining/)
})

test('plan card uses compact chips and a pill action instead of list rows', () => {
  assert.match(cssBlock('.plan-card'), /display:\s*flex/)
  assert.match(cssBlock('.plan-card'), /flex-direction:\s*column/)
  assert.match(cssBlock('.plan-chip'), /background:\s*#F8F9FA/)
  assert.match(cssBlock('.plan-chip'), /border-radius:\s*12px/)
  assert.match(cssBlock('.plan-action'), /border-radius:\s*999px/)
  assert.match(cssBlock('.plan-action'), /background:\s*#7EB8DA/)
})

test('plan card keeps progress bar and textual goal summary styling', () => {
  assert.match(cssBlock('.plan-progress-bar'), /display:\s*flex/)
  assert.match(cssBlock('.progress-track'), /height:\s*6px/)
  assert.match(cssBlock('.progress-fill'), /linear-gradient\(90deg,\s*#B8DDEF,\s*#7EB8DA\)/)
  assert.match(cssBlock('.plan-goal-type'), /font-weight:\s*760/)
  assert.match(cssBlock('.progress-label'), /color:\s*#7EB8DA/)
})
