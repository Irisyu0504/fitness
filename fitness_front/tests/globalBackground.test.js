import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import test from 'node:test'

const appVue = readFileSync(new URL('../src/App.vue', import.meta.url), 'utf8')

test('App renders shell content with sidebar and routed page transition layer', () => {
  assert.match(appVue, /import\s+\{\s*computed\s*\}\s+from\s+['"]vue['"]/)
  assert.match(appVue, /import\s+\{\s*useRoute\s*\}\s+from\s+['"]vue-router['"]/)
  assert.match(appVue, /const\s+route\s*=\s*useRoute\(\)/)
  assert.match(appVue, /const\s+showSidebar\s*=\s*computed\(/)
  assert.match(appVue, /hideOnRoutes\s*=\s*\['\/',\s*'\/auth',\s*'\/login',\s*'\/register'\]/)
  assert.match(appVue, /<FloatingSidebar\s+v-if="showSidebar"\s*\/>/)
  assert.match(appVue, /<GlobalAiChat\s+v-if="showSidebar"\s*\/>/)
  assert.match(appVue, /<router-view\s+v-slot="\{\s*Component,\s*route\s*\}">/)
  assert.match(appVue, /<transition\s+:name="transitionName"\s+mode="out-in">/)
})

test('app shell keeps layered content container and route transition styles', () => {
  assert.match(appVue, /\.app-shell\s*\{/)
  assert.match(appVue, /min-height:\s*100vh/)
  assert.match(appVue, /isolation:\s*isolate/)
  assert.match(appVue, /\.app-content\s*\{/)
  assert.match(appVue, /position:\s*relative/)
  assert.match(appVue, /\.app-content\s*\{[\s\S]*z-index:\s*1/)
  assert.match(appVue, /\.fade-enter-active[\s\S]*transition:\s*opacity 250ms ease/)
  assert.match(appVue, /\.slide-up-enter-active[\s\S]*cubic-bezier\(0\.22,\s*1,\s*0\.36,\s*1\)/)
})
