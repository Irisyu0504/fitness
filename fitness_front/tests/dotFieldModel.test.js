import assert from 'node:assert/strict'
import test from 'node:test'

import { buildDotGrid } from '../src/utils/dotGrid.js'

test('buildDotGrid creates dots inside the lower field bounds', () => {
  const dots = buildDotGrid({
    width: 100,
    height: 50,
    dotRadius: 1.5,
    dotSpacing: 14
  })

  assert.equal(dots.length, 18)

  for (const dot of dots) {
    assert.ok(dot.ax >= 0 && dot.ax <= 100)
    assert.ok(dot.ay >= 0 && dot.ay <= 50)
    assert.equal(dot.x, dot.ax)
    assert.equal(dot.y, dot.ay)
  }
})
