import assert from 'node:assert/strict'
import test from 'node:test'

import { isExternalLink, isRouterLink } from '../src/utils/routeLink.js'

test('PillNav treats app paths as vue-router links', () => {
  assert.equal(isRouterLink('/login'), true)
  assert.equal(isRouterLink('/register'), true)
  assert.equal(isRouterLink('dashboard'), true)
})

test('PillNav treats external and hash links as regular anchors', () => {
  for (const href of ['https://example.com', 'http://example.com', '//example.com', 'mailto:a@b.com', 'tel:123', '#top']) {
    assert.equal(isExternalLink(href), true)
    assert.equal(isRouterLink(href), false)
  }
})
