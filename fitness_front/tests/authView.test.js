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

test('AuthView exists and uses a centered 3D login register flip card', () => {
  assert.equal(existsSync(authViewUrl), true)

  const authViewSource = readFileSync(authViewUrl, 'utf8')

  assert.match(authViewSource, /<template>/)
  assert.match(authViewSource, /<script setup>/)
  assert.match(authViewSource, /<style scoped>/)
  assert.match(authViewSource, /<main\s+class="auth-view">/)
  assert.match(authViewSource, /class="auth-card-wrapper"/)
  assert.match(authViewSource, /const\s+cardPositionStyle\s*=\s*\{/)
  assert.match(authViewSource, /const\s+isLogin\s*=\s*ref\(/)
  assert.match(authViewSource, /route\.query\.type\s*!==\s*['"]register['"]/)
  assert.match(authViewSource, /class="flip-scene"/)
  assert.match(authViewSource, /class="flip-card"/)
  assert.match(authViewSource, /rotateY\(180deg\)/)
  assert.match(authViewSource, /perspective:/)
  assert.match(authViewSource, /backface-visibility:\s*hidden/)
})

test('AuthView renders Element Plus login and register forms with light card input overrides', () => {
  assert.equal(existsSync(authViewUrl), true)

  const authViewSource = readFileSync(authViewUrl, 'utf8')

  for (const token of [
    '<el-form',
    '<el-form-item',
    '<el-input',
    '<el-button',
    'Welcome',
    'Join Us',
    'handleLogin',
    'handleRegister',
    'switchMode(false)',
    'switchMode(true)',
    'autocomplete="username"',
    'autocomplete="email"',
    'autocomplete="new-password"'
  ]) {
    assert.match(authViewSource, new RegExp(token.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')))
  }

  assert.match(authViewSource, /:deep\(\.el-input__wrapper\)/)
  assert.match(authViewSource, /background:\s*#F8F6F3/)
  assert.match(authViewSource, /border-color:\s*#7EB8DA/)
  assert.match(authViewSource, /color:\s*var\(--text-primary\)/)
})

test('router and landing navigation use the unified auth route with query mode', () => {
  assert.match(routerSource, /path:\s*['"]\/auth['"]/)
  assert.match(routerSource, /import\s+AuthView\s+from\s+['"]\.\.\/views\/AuthView\.vue['"]/)
  assert.match(routerSource, /name:\s*['"]auth['"]/)
  assert.doesNotMatch(landingConfigSource, /\/login/)
  assert.doesNotMatch(landingConfigSource, /\/register/)
})

test('Element Plus is installed and registered globally', () => {
  assert.equal(Boolean(packageJson.dependencies?.['element-plus']), true)
  assert.match(mainSource, /import\s+ElementPlus\s+from\s+['"]element-plus['"]/)
  assert.match(mainSource, /import\s+['"]element-plus\/dist\/index\.css['"]/)
  assert.match(mainSource, /\.use\(ElementPlus\)/)
})
