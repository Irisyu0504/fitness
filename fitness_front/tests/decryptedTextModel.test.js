import assert from 'node:assert/strict'
import test from 'node:test'

import { DEFAULT_DECRYPTED_TEXT_OPTIONS, createScrambledText } from '../src/utils/scrambleText.js'

test('decrypted text defaults match the login title design', () => {
  assert.deepEqual(DEFAULT_DECRYPTED_TEXT_OPTIONS, {
    text: 'CoreFitness',
    fontSize: 64,
    textColor: '#FFFFFF',
    revealColor: '#FFFFFF',
    speed: 50
  })
})

test('createScrambledText preserves text length and spaces', () => {
  const scrambled = createScrambledText('Core Fitness')

  assert.equal(scrambled.length, 'Core Fitness'.length)
  assert.equal(scrambled[4], ' ')
  assert.notEqual(scrambled, 'Core Fitness')
})
