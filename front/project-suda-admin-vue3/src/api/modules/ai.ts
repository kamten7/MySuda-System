import { getToken } from '@/utils/cookies'

const baseURL = import.meta.env.VITE_API_BASE_URL || '/api'

/**
 * AI 流式查询
 */
export function streamQuery(
  question: string,
  onToken: (token: string) => void,
  onDone: () => void,
  onError: (err: string) => void,
): () => void {
  return doFetch(`${baseURL}/ai/query/stream`, JSON.stringify({ question }), onToken, onDone, onError)
}

/**
 * AI 一键经营诊断
 */
export function streamDiagnose(
  onToken: (token: string) => void,
  onDone: () => void,
  onError: (err: string) => void,
): () => void {
  return doFetch(`${baseURL}/ai/diagnose`, '{}', onToken, onDone, onError)
}

// ==================== 通用 SSE fetch ====================

function doFetch(
  url: string,
  body: string,
  onToken: (token: string) => void,
  onDone: () => void,
  onError: (err: string) => void,
): () => void {
  const controller = new AbortController()

  fetch(url, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      token: getToken() || '',
    },
    body,
    signal: controller.signal,
  })
    .then(async (response) => {
      if (!response.ok) {
        onError(`请求失败 (HTTP ${response.status})`)
        return
      }
      if (!response.body) {
        onError('当前浏览器不支持流式读取')
        return
      }

      const reader = response.body.getReader()
      const decoder = new TextDecoder()
      let buffer = ''

      while (true) {
        const { done, value } = await reader.read()
        if (done) break

        buffer += decoder.decode(value, { stream: true })

        let idx: number
        while ((idx = buffer.indexOf('\n\n')) !== -1) {
          const chunk = buffer.slice(0, idx)
          buffer = buffer.slice(idx + 2)

          if (chunk.startsWith('data:')) {
            const payload = chunk.slice(5)
            if (payload === '[DONE]') {
              onDone()
              return
            } else if (payload.startsWith('ERROR:')) {
              onError(payload.slice(6))
              return
            } else {
              onToken(payload)
            }
          }
        }
      }

      onDone()
    })
    .catch((err) => {
      if (err.name === 'AbortError') return
      onError(err.message || '网络连接失败')
    })

  return () => controller.abort()
}
