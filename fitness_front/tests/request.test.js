import assert from 'node:assert/strict'
import test from 'node:test'
import { request } from '../src/api/request.js'

test('request treats string success codes from the backend as successful responses', async () => {
  const calls = []

  globalThis.localStorage = {
    getItem(key) {
      assert.equal(key, 'token')
      return 'jwt-token'
    }
  }

  globalThis.fetch = async (url, options) => {
    calls.push({ url, options })
    return {
      ok: true,
      async json() {
        return {
          code: '200',
          msg: 'ok',
          data: { accepted: true }
        }
      }
    }
  }

  const data = await request('/user/profile', {
    headers: {
      'X-Test': '1'
    }
  })

  assert.deepEqual(data, { accepted: true })
  assert.equal(calls[0].url, '/api/user/profile')
  assert.equal(calls[0].options.headers.Authorization, 'Bearer jwt-token')
  assert.equal(calls[0].options.headers['X-Test'], '1')
})
