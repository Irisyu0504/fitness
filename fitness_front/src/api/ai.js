const API_BASE = '/api'

/**
 * 流式调用 AI 聊天接口
 * @param {Array} messages - 对话历史 [{ role: 'user'|'assistant', content: '...' }]
 * @param {Object} context - 用户上下文（目标、频率等）
 * @param {Function} onToken - 每收到一个 token 的回调
 * @param {Function} onDone - 流结束回调
 * @param {Function} onError - 错误回调
 */
export async function streamChat(messages, context, { onToken, onDone, onError }) {
  const token = localStorage.getItem('token')

  try {
    const response = await fetch(`${API_BASE}/ai/chat`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {})
      },
      body: JSON.stringify({ messages, context })
    })

    if (!response.ok) {
      throw new Error(`请求失败: ${response.status}`)
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''

      for (const line of lines) {
        const trimmed = line.trim()
        if (!trimmed || !trimmed.startsWith('data:')) continue

        const data = trimmed.slice(5).trim()
        if (data === '[DONE]') continue
        if (data) {
          onToken(data)
        }
      }
    }

    onDone?.()
  } catch (err) {
    onError?.(err)
  }
}
