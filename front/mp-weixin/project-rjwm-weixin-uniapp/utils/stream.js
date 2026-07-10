/**
 * SSE 流式请求工具
 * 使用微信小程序原生 wx.request 的 enableChunked 能力实现流式读取
 * 需要微信基础库 >= 2.20.1
 */

import { baseUrl } from './env'
import store from '../store'

/**
 * 发起 SSE 流式请求
 *
 * @param {Object} options
 * @param {string} options.url       - API 路径（会自动拼接 baseUrl）
 * @param {Object} options.data      - 请求体
 * @param {Function} options.onMessage - 每收到一个 chunk 的回调 (chunkText: string)
 * @param {Function} options.onDone    - 流结束回调
 * @param {Function} options.onError   - 错误回调 (errorMsg: string)
 * @returns {Object} requestTask      - 可调用 .abort() 中断请求
 */
export function streamRequest({ url, data = {}, onMessage, onDone, onError }) {
	const token = store.state.token

	const requestTask = wx.request({
		url: baseUrl + url,
		data: data,
		method: 'POST',
		enableChunked: true,
		header: {
			'Content-Type': 'application/json',
			'authentication': token || ''
		},
		success: (res) => {
			// 流结束后 success 回调，不做特殊处理
			if (res.statusCode !== 200) {
				onError && onError('请求失败: ' + res.statusCode)
			}
		},
		fail: (err) => {
			onError && onError(err.errMsg || '网络请求失败')
		}
	})

	// 监听分块数据（微信基础库 >= 2.20.1）
	if (requestTask && requestTask.onChunkReceived) {
		// 用于拼接不完整的 SSE 行
		let buffer = ''

		requestTask.onChunkReceived((res) => {
			try {
				// res.data 是 ArrayBuffer
				const text = arrayBufferToString(res.data)
				buffer += text

				// SSE 格式：每行以 "data: " 开头，以 "\n\n" 结尾
				// 按 "\n\n" 分割完整的消息
				const parts = buffer.split('\n\n')
				// 最后一个可能是不完整的，保留到下一次
				buffer = parts.pop() || ''

				for (const part of parts) {
					const lines = part.split('\n')
					for (const line of lines) {
						const trimmed = line.trim()
						if (trimmed.startsWith('data: ')) {
							const content = trimmed.slice(6) // 去掉 "data: " 前缀

							if (content === '[DONE]') {
								onDone && onDone()
								return
							}

							if (content.startsWith('[ERROR]')) {
								const errorMsg = content.slice(7).trim() || '服务暂时不可用'
								onError && onError(errorMsg)
								return
							}

							// 正常内容块：可能包含转义的换行符
							const decoded = content
								.replace(/\\n/g, '\n')
								.replace(/\\t/g, '\t')
							onMessage && onMessage(decoded)
						}
					}
				}
			} catch (e) {
				console.error('SSE chunk 解析错误:', e)
			}
		})
	} else {
		// 降级：不支持 enableChunked，回调错误
		onError && onError('当前微信版本不支持流式对话，请升级微信')
	}

	return requestTask
}

/**
 * ArrayBuffer 转 UTF-8 字符串
 */
function arrayBufferToString(buffer) {
	// 微信小程序中可以使用 TextDecoder（基础库 >= 2.16.0）
	if (typeof TextDecoder !== 'undefined') {
		return new TextDecoder('utf-8').decode(new Uint8Array(buffer))
	}

	// 降级方案
	let str = ''
	const bytes = new Uint8Array(buffer)
	for (let i = 0; i < bytes.length; i++) {
		str += String.fromCharCode(bytes[i])
	}
	try {
		return decodeURIComponent(escape(str))
	} catch (e) {
		return str
	}
}
