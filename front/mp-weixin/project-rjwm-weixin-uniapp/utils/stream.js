/**
 * SSE 流式请求工具
 * 使用微信小程序原生 wx.request 的 enableChunked 能力实现流式读取
 * 需要微信基础库 >= 2.20.1
 *
 * 改进点：
 * - 使用持久化 TextDecoder（stream: true）处理跨 chunk 的多字节 UTF-8 字符
 * - 更健壮的 SSE 行解析
 * - 防止 [DONE] / [ERROR] 后继续处理残留数据
 * - 支持 [CART_UPDATED:N] 事件，实时同步购物车数量
 */

import { baseUrl } from './env'
import store from '../store'

/**
 * 发起 SSE 流式请求
 *
 * @param {Object} options
 * @param {string} options.url          - API 路径（会自动拼接 baseUrl）
 * @param {Object} options.data         - 请求体
 * @param {Function} options.onMessage  - 每收到一个 chunk 的回调 (chunkText: string)
 * @param {Function} options.onDone     - 流结束回调
 * @param {Function} options.onError    - 错误回调 (errorMsg: string)
 * @param {Function} options.onCartUpdated - 购物车更新回调 (cartCount: number)
 * @returns {Object} requestTask        - 可调用 .abort() 中断请求
 */
export function streamRequest({ url, data = {}, onMessage, onDone, onError, onCartUpdated }) {
	const token = store.state.token

	// 流结束标记，防止结束后继续处理 chunk
	let finished = false

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
			// 流正常结束但没收到 [DONE]（可能是服务器提前关闭连接）
			if (!finished && res.statusCode === 200) {
				finished = true
				onDone && onDone()
			}
		},
		fail: (err) => {
			if (!finished) {
				finished = true
				onError && onError(err.errMsg || '网络请求失败')
			}
		}
	})

	// 监听分块数据（微信基础库 >= 2.20.1）
	if (requestTask && requestTask.onChunkReceived) {
		// 使用持久化 TextDecoder，stream: true 处理跨 chunk 的多字节字符
		let decoder = null
		try {
			decoder = new TextDecoder('utf-8', { stream: true })
		} catch (e) {
			decoder = new TextDecoder('utf-8') // 旧版本降级
		}

		// SSE 行缓冲区
		let buffer = ''

		requestTask.onChunkReceived((res) => {
			if (finished) return

			try {
				// res.data 是 ArrayBuffer，使用持久化解码器处理多字节字符
				const text = decoder.decode(new Uint8Array(res.data), { stream: true })
				buffer += text

				// SSE 格式：每行以 "data: " 开头，以 "\n\n" 结尾
				// 按 "\n\n" 分割完整的消息
				const parts = buffer.split('\n\n')
				// 最后一个可能是不完整的，保留到下一次
				buffer = parts.pop() || ''

				for (const part of parts) {
					if (finished) return
					processSSEPart(part)
				}
			} catch (e) {
				console.error('SSE chunk 解析错误:', e)
			}
		})
	} else {
		// 降级：不支持 enableChunked
		if (!finished) {
			finished = true
			onError && onError('当前微信版本不支持流式对话，请升级微信')
		}
	}

	/**
	 * 处理一条完整的 SSE 消息（可能包含多行 data:）
	 */
	function processSSEPart(part) {
		const lines = part.split('\n')
		for (const line of lines) {
			const trimmed = line.trim()
			if (!trimmed.startsWith('data:')) continue

			const content = trimmed.slice(5).trim() // 去掉 "data:" 前缀

			if (content === '[DONE]') {
				finished = true
				// 冲刷 TextDecoder 中剩余的字节
				try { decoder.decode() } catch (e) { /* ignore */ }
				onDone && onDone()
				return
			}

			if (content.startsWith('[ERROR]')) {
				finished = true
				const errorMsg = content.slice(7).trim() || '服务暂时不可用'
				// 冲刷 TextDecoder
				try { decoder.decode() } catch (e) { /* ignore */ }
				onError && onError(errorMsg)
				return
			}

			// 购物车更新事件：[CART_UPDATED:数量]
			if (content.startsWith('[CART_UPDATED:')) {
				const match = content.match(/\[CART_UPDATED:(\d+)\]/)
				if (match && onCartUpdated) {
					onCartUpdated(parseInt(match[1], 10))
				}
				continue
			}

			// 正常内容块：处理转义字符
			const decoded = content
				.replace(/\\n/g, '\n')
				.replace(/\\t/g, '\t')
				.replace(/\\\\/g, '\\')
			onMessage && onMessage(decoded)
		}
	}

	return requestTask
}
