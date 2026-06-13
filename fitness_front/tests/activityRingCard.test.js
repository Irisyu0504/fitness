import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import test from 'node:test'

const dashboardView = readFileSync(new URL('../src/views/DashboardView.vue', import.meta.url), 'utf8')

const cssBlock = (selector) => {
  const match = dashboardView.match(new RegExp(`${selector.replace('.', '\\.')}\\s*\\{([^}]*)\\}`))
  return match?.[1] ?? ''
}

test('dashboard exposes separate workout burn and diet intake metric cards', () => {
  assert.match(dashboardView, /class="glass-card metric-card calories-card"/)
  assert.match(dashboardView, /class="glass-card metric-card intake-card"/)
  assert.match(dashboardView, /dashboard\.todayWorkoutCalories/)
  assert.match(dashboardView, /dashboard\.todayDietCalories/)
  assert.match(dashboardView, /dashboard\.calorieBalance/)
  assert.match(dashboardView, /Math\.abs\(dashboard\.calorieBalance \|\| 0\)/)
})

test('metric cards use shared glass shell with colored orbs and footer state dots', () => {
  assert.match(cssBlock('.glass-card'), /background:\s*#FFFFFF/)
  assert.match(cssBlock('.glass-card'), /border-radius:\s*24px/)
  assert.match(cssBlock('.metric-card'), /width:\s*200px/)
  assert.match(cssBlock('.metric-card'), /height:\s*130px/)
  assert.match(cssBlock('.metric-orb'), /position:\s*absolute/)
  assert.match(cssBlock('.status-dot.cyan'), /background:\s*#7EB8DA/)
  assert.match(cssBlock('.status-dot.rose'), /background:\s*#F0A8A8/)
})

test('metric cards keep large numeric emphasis and unit footer separation', () => {
  assert.match(cssBlock('.metric-main'), /display:\s*flex/)
  assert.match(cssBlock('.metric-main'), /align-items:\s*flex-end/)
  assert.match(cssBlock('.metric-value'), /font-size:\s*32px/)
  assert.match(cssBlock('.metric-value'), /font-weight:\s*820/)
  assert.match(cssBlock('.metric-footer'), /display:\s*inline-flex/)
  assert.match(cssBlock('.metric-footer'), /font-size:\s*12px/)
})
