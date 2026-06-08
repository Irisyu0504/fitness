import assert from 'node:assert/strict'
import test from 'node:test'

import { railData } from '../src/config/authLanding.config.js'

test('railData defines three bottom interaction rails', () => {
  assert.deepEqual(
    railData.map(({ id, text }) => ({ id, text })),
    [
      { id: 1, text: 'Have you workout today?' },
      { id: 2, text: 'Consistency is key.' },
      { id: 3, text: 'Great job, keep going!' }
    ]
  )
})
