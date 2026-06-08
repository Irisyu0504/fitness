import assert from 'node:assert/strict'
import { existsSync, readFileSync } from 'node:fs'
import test from 'node:test'

const componentUrl = new URL('../src/components/RecentActivityList.vue', import.meta.url)
const source = existsSync(componentUrl) ? readFileSync(componentUrl, 'utf8') : ''

const cssBlock = (selector) => {
  const match = source.match(new RegExp(`${selector.replace('.', '\\.')}\\s*\\{([^}]*)\\}`))
  return match?.[1] ?? ''
}

test('RecentActivityList exists with workout header and four mock records', () => {
  assert.ok(existsSync(componentUrl))
  assert.match(source, /Recent Workouts/)
  assert.match(source, /View All/)
  assert.match(source, /const\s+activities\s*=\s*ref\(\[/)
  assert.match(source, /胸部与三头肌训练/)
  assert.match(source, /间歇跑训练/)
  assert.match(source, /力量循环训练/)
  assert.match(source, /瑜伽拉伸恢复/)
})

test('RecentActivityList uses restrained glass card and spaced flex list items', () => {
  assert.match(cssBlock('.recent-activity-card'), /background:\s*rgba\(255,\s*255,\s*255,\s*0\.025\)/)
  assert.match(cssBlock('.recent-activity-card'), /border-radius:\s*24px/)
  assert.match(cssBlock('.activity-list'), /display:\s*flex/)
  assert.match(cssBlock('.activity-list'), /flex-direction:\s*column/)
  assert.match(cssBlock('.activity-list'), /gap:\s*16px/)
  assert.match(cssBlock('.activity-item:hover'), /background:\s*rgba\(255,\s*255,\s*255,\s*0\.04\)/)
})

test('RecentActivityList aligns icon, text, and right calorie metric', () => {
  assert.match(cssBlock('.activity-icon'), /width:\s*40px/)
  assert.match(cssBlock('.activity-icon'), /height:\s*40px/)
  assert.match(cssBlock('.activity-icon'), /border-radius:\s*50%/)
  assert.match(cssBlock('.activity-title'), /font-size:\s*15px/)
  assert.match(cssBlock('.activity-time'), /font-size:\s*12px/)
  assert.match(cssBlock('.activity-calories'), /text-align:\s*right/)
  assert.match(cssBlock('.activity-calories'), /font-family:\s*'Oswald'/)
})
