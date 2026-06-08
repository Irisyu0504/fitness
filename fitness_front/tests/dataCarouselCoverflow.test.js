import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import test from 'node:test'

const dataCarouselVue = readFileSync(
  new URL('../src/components/DataCarousel.vue', import.meta.url),
  'utf8'
)

const cssBlock = (selector) => {
  const match = dataCarouselVue.match(new RegExp(`${selector.replace('.', '\\.')}\\s*\\{([^}]*)\\}`))
  return match?.[1] ?? ''
}

test('DataCarousel uses a fixed 3D viewport that does not clip coverflow cards', () => {
  assert.match(dataCarouselVue, /\.carousel-viewport\s*\{[\s\S]*height:\s*180px/)
  assert.match(dataCarouselVue, /\.carousel-viewport\s*\{[\s\S]*perspective:\s*1000px/)
  assert.match(dataCarouselVue, /\.carousel-viewport\s*\{[\s\S]*overflow:\s*visible/)
  assert.doesNotMatch(cssBlock('.carousel-track'), /display:\s*flex/)
})

test('DataCarousel cards are absolutely centered and animated in 3D depth', () => {
  assert.match(dataCarouselVue, /\.carousel-item\s*\{[\s\S]*position:\s*absolute/)
  assert.match(dataCarouselVue, /\.carousel-item\s*\{[\s\S]*left:\s*0/)
  assert.match(dataCarouselVue, /\.carousel-item\s*\{[\s\S]*right:\s*0/)
  assert.match(dataCarouselVue, /\.carousel-item\s*\{[\s\S]*margin:\s*auto/)
  assert.match(dataCarouselVue, /transition:\s*all\s+0\.6s\s+cubic-bezier/)
  assert.match(dataCarouselVue, /translateZ\(0\)/)
  assert.match(dataCarouselVue, /translateZ\(-100px\)/)
})

test('DataCarousel calculates circular offsets and pauses autoplay on hover', () => {
  assert.match(dataCarouselVue, /let\s+offset\s*=\s*index\s*-\s*currentIndex\.value/)
  assert.match(dataCarouselVue, /if\s*\(\s*offset\s*>\s*total\s*\/\s*2\s*\)\s*offset\s*-=\s*total/)
  assert.match(dataCarouselVue, /if\s*\(\s*offset\s*<\s*-total\s*\/\s*2\s*\)\s*offset\s*\+=\s*total/)
  assert.match(dataCarouselVue, /@mouseenter="pauseAutoPlay"/)
  assert.match(dataCarouselVue, /@mouseleave="startAutoPlay"/)
  assert.match(dataCarouselVue, /class="glass-card"/)
})
