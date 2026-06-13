import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import test from 'node:test'

const dashboardView = readFileSync(new URL('../src/views/DashboardView.vue', import.meta.url), 'utf8')

test('Dashboard wires plan, calendar, body focus, and data carousel widgets', () => {
  assert.match(dashboardView, /import\s+BodyFocus\s+from\s+['"]@\/components\/BodyFocus\.vue['"]/)
  assert.match(dashboardView, /import\s+DataCarousel\s+from\s+['"]@\/components\/DataCarousel\.vue['"]/)
  assert.match(dashboardView, /import\s+SweatGrid\s+from\s+['"]@\/components\/SweatGrid\.vue['"]/)
  assert.match(dashboardView, /class="glass-card metric-card calories-card"/)
  assert.match(dashboardView, /class="glass-card metric-card intake-card"/)
  assert.match(dashboardView, /class="glass-card plan-card"/)
  assert.match(dashboardView, /<SweatGrid\s+:done-dates="doneDates"\s*\/>/)
  assert.match(dashboardView, /<BodyFocus\s+:records="allRecords"\s*\/>/)
  assert.match(dashboardView, /<DataCarousel/)
})

test('Dashboard places widgets in left and right bento stacks', () => {
  assert.match(dashboardView, /class="left-bento"/)
  assert.match(dashboardView, /class="top-metrics-group"/)
  assert.match(dashboardView, /class="glass-card sweat-card"/)
  assert.match(dashboardView, /class="right-bento"/)
  assert.ok(dashboardView.indexOf('class="glass-card plan-card"') < dashboardView.indexOf('class="glass-card sweat-card"'))
  assert.ok(dashboardView.indexOf('<BodyFocus') < dashboardView.indexOf('<DataCarousel'))
})

test('Dashboard keeps 3D carousel overflow visible inside its card', () => {
  assert.match(dashboardView, /class="glass-card carousel-card"/)
  assert.match(dashboardView, /\.carousel-card\s*\{[\s\S]*overflow:\s*visible/)
})
