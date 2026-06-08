import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import test from 'node:test'

const appVue = readFileSync(new URL('../src/App.vue', import.meta.url), 'utf8')

test('App renders the global background as an independent layer above RouterView', () => {
  assert.match(appVue, /import\s+\{\s*computed\s*\}\s+from\s+['"]vue['"]/)
  assert.match(appVue, /import\s+\{\s*useRoute\s*\}\s+from\s+['"]vue-router['"]/)
  assert.match(appVue, /const\s+route\s*=\s*useRoute\(\)/)
  assert.match(appVue, /route\.path\s*===\s*['"]\/['"]/)
  assert.match(
    appVue,
    /<div\s+v-if="!isHomeRoute"\s+class="global-app-background"\s+aria-hidden="true"><\/div>/
  )
  assert.match(appVue, /<RouterView\s*\/>/)
  assert.ok(appVue.indexOf('class="global-app-background"') < appVue.indexOf('<RouterView />'))
  assert.doesNotMatch(appVue, /import\s+BackgroundOverlay/)
})

test('global-app-background renders only the raw image with no visual effects', () => {
  assert.match(appVue, /\.global-app-background\s*\{/)
  assert.match(appVue, /position:\s*fixed/)
  assert.match(appVue, /inset:\s*0/)
  assert.match(appVue, /z-index:\s*0/)
  assert.match(appVue, /background-image:\s*url\(['"]@\/assets\/images\/bg-gym-mountain\.jpg['"]\)/)
  assert.match(appVue, /background-size:\s*cover/)
  assert.match(appVue, /background-position:\s*center/)
  assert.doesNotMatch(appVue, /filter:\s*blur/)
  assert.doesNotMatch(appVue, /saturate\(/)
  assert.doesNotMatch(appVue, /transform:\s*scale/)
  assert.doesNotMatch(appVue, /\.global-app-background::before/)
  assert.doesNotMatch(appVue, /\.global-app-background::after/)
  assert.match(appVue, /\.app-content\s*\{[\s\S]*z-index:\s*1/)
})
