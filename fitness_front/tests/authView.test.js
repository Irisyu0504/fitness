import assert from 'node:assert/strict'
import { existsSync, readFileSync } from 'node:fs'
import test from 'node:test'

const authViewUrl = new URL('../src/views/AuthView.vue', import.meta.url)
const routerSource = readFileSync(new URL('../src/router/index.js', import.meta.url), 'utf8')
const mainSource = readFileSync(new URL('../src/main.js', import.meta.url), 'utf8')
const landingConfigSource = readFileSync(
  new URL('../src/config/authLanding.config.js', import.meta.url),
  'utf8'
)
const packageJson = JSON.parse(
  readFileSync(new URL('../package.json', import.meta.url), 'utf8')
)

test('AuthView exists and uses LiquidGlass to wrap a 3D login/register flip card', () => {
  assert.equal(existsSync(authViewUrl), true)

  const authViewSource = readFileSync(authViewUrl, 'utf8')

  assert.match(authViewSource, /<template>/)
  assert.match(authViewSource, /<script setup>/)
  assert.match(authViewSource, /<style scoped>/)
  assert.match(authViewSource, /<main\s+class="auth-view relative z-10">/)
  assert.doesNotMatch(authViewSource, /<main[^>]*global-app-background/)
  assert.match(authViewSource, /import\s+LiquidGlass\s+from\s+['"]@\/components\/LiquidGlass\.vue['"]/)
  assert.match(authViewSource, /<LiquidGlass\s+[^>]*mode="shader"[^>]*:displacementScale="40"[^>]*:cornerRadius="24"[^>]*padding="0"/s)
  assert.match(authViewSource, /const\s+isLogin\s*=\s*ref\(/)
  assert.match(authViewSource, /route\.query\.type\s*===\s*['"]register['"]/)
  assert.match(authViewSource, /rotateY\(180deg\)/)
  assert.match(authViewSource, /perspective:/)
  assert.match(authViewSource, /backface-visibility:\s*hidden/)
})

test('AuthView renders Element Plus login and register forms with glass-dark input overrides', () => {
  assert.equal(existsSync(authViewUrl), true)

  const authViewSource = readFileSync(authViewUrl, 'utf8')

  for (const token of [
    '<el-form',
    '<el-form-item',
    '<el-input',
    '<el-button',
    'Welcome',
    'Join Us',
    '还没加入我们？',
    '立即注册',
    '已有账号？',
    '返回登录',
    '邮箱地址',
    '不少于6位'
  ]) {
    assert.match(authViewSource, new RegExp(token.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')))
  }

  assert.match(authViewSource, /:deep\(\.el-input__wrapper\)/)
  assert.match(authViewSource, /background:\s*rgba\(255,\s*255,\s*255,\s*0\.05\)/)
  assert.match(authViewSource, /color:\s*#fff/)
})

test('router and landing navigation use the unified auth route with query mode', () => {
  assert.match(routerSource, /path:\s*['"]\/auth['"]/)
  assert.match(routerSource, /import\(['"]@\/views\/AuthView\.vue['"]\)/)
  assert.match(routerSource, /redirect:\s*\{\s*path:\s*['"]\/auth['"],\s*query:\s*\{\s*type:\s*['"]login['"]/s)
  assert.match(routerSource, /redirect:\s*\{\s*path:\s*['"]\/auth['"],\s*query:\s*\{\s*type:\s*['"]register['"]/s)
  assert.match(landingConfigSource, /href:\s*['"]\/auth\?type=login['"]/)
  assert.match(landingConfigSource, /href:\s*['"]\/auth\?type=register['"]/)
})

test('Element Plus is installed and registered globally', () => {
  assert.equal(Boolean(packageJson.dependencies?.['element-plus']), true)
  assert.match(mainSource, /import\s+ElementPlus\s+from\s+['"]element-plus['"]/)
  assert.match(mainSource, /import\s+['"]element-plus\/dist\/index\.css['"]/)
  assert.match(mainSource, /\.use\(ElementPlus\)/)
})
