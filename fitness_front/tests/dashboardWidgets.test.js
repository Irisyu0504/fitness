import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import test from 'node:test'

const dashboardView = readFileSync(new URL('../src/views/DashboardView.vue', import.meta.url), 'utf8')

test('Dashboard wires the activity ring, body focus, and data carousel widgets', () => {
  assert.match(dashboardView, /import\s+ActivityRingCard\s+from\s+['"]@\/components\/ActivityRingCard\.vue['"]/)
  assert.match(dashboardView, /import\s+RecentActivityList\s+from\s+['"]@\/components\/RecentActivityList\.vue['"]/)
  assert.match(dashboardView, /<ActivityRingCard\s*\/>/)
  assert.match(dashboardView, /<RecentActivityList\s*\/>/)
  assert.match(dashboardView, /<SweatGrid\s*\/>/)
  assert.match(dashboardView, /<BodyFocus\s*\/>/)
  assert.match(dashboardView, /<DataCarousel\s*\/>/)
})

test('Dashboard places widgets in the requested wide-left and narrow-right stacks', () => {
  assert.match(dashboardView, /class="left-dashboard-stack"/)
  assert.match(dashboardView, /class="left-top-grid"/)
  assert.match(dashboardView, /class="glass-card trend-card"/)
  assert.match(dashboardView, /class="right-dashboard-stack"/)
  assert.ok(dashboardView.indexOf('<ActivityRingCard />') < dashboardView.indexOf('<RecentActivityList />'))
  assert.ok(dashboardView.indexOf('<SweatGrid />') < dashboardView.indexOf('<BodyFocus />'))
  assert.ok(dashboardView.indexOf('<BodyFocus />') < dashboardView.indexOf('<DataCarousel />'))
})

test('Dashboard keeps 3D carousel overflow visible inside its card', () => {
  assert.match(dashboardView, /class="glass-card carousel-card"/)
  assert.match(dashboardView, /\.carousel-card\s*\{[\s\S]*overflow:\s*visible/)
})
