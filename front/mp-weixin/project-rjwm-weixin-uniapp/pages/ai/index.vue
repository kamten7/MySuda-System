<template>
<view class="ai-page">
	<!-- ====== 顶部导航栏 ====== -->
	<view class="navbar" :style="{ paddingTop: statusBarH + 'px' }">
		<view class="navbar-inner">
			<view class="nav-left" @click="goBack">
				<text class="nav-back-icon">&#8249;</text>
			</view>
			<text class="nav-title">{{ navTitle }}</text>
			<view class="nav-right" @click="showMenu = true">
				<text class="nav-menu-icon">···</text>
			</view>
		</view>
	</view>

	<!-- ====== 消息滚动区 ====== -->
	<scroll-view
		class="msg-scroll"
		scroll-y
		:scroll-into-view="scrollToId"
		:scroll-with-animation="true"
		:style="scrollStyle"
		@touchstart="onTouchMsgScroll"
	>
		<!-- 欢迎页 -->
		<view v-if="!hasMessages" class="welcome">
			<view class="w-brand">
				<view class="w-avatar-glow">
					<view class="w-avatar-ring">
						<text class="w-avatar-text">AI</text>
					</view>
				</view>
				<text class="w-title">你好，我是小速 👋</text>
				<text class="w-sub">你的智能点餐助手，让我帮你找到最合适的美食</text>
			</view>

			<view class="w-cards">
				<view class="w-card" v-for="(c, i) in featCards" :key="c.title">
					<text class="wc-icon">{{ c.icon }}</text>
					<text class="wc-title">{{ c.title }}</text>
					<text class="wc-desc">{{ c.desc }}</text>
				</view>
			</view>

			<text class="w-section-label">试试这样问我</text>
			<view class="w-prompts">
				<view class="wp-item" v-for="(p, idx) in prompts" :key="idx" :data-idx="idx" @click="onPromptTap(idx)">
					<text class="wp-emoji">{{ p.icon }}</text>
					<text class="wp-text">{{ p.text }}</text>
				</view>
			</view>

			<text class="w-disclaimer">内容由 AI 生成，仅供参考</text>
		</view>

		<!-- 对话区 -->
		<view v-else class="chat-area">
			<view v-for="(group, gi) in groupedMessages" :key="gi">
				<view class="time-divider" v-if="group.timeLabel">
					<text class="time-divider-text">{{ group.timeLabel }}</text>
				</view>

				<view class="msg-group" :class="group.role === 'user' ? 'msg-group--right' : ''">
					<view v-if="group.role === 'ai'" class="msg-avatar msg-avatar--ai">
						<text class="avatar-label">AI</text>
					</view>

					<view class="msg-group-body" :class="group.role === 'user' ? 'msg-group-body--right' : ''">
						<view
							v-for="(m, mi) in group.messages"
							:key="mi"
							class="msg-bubble"
							:class="[m.role === 'user' ? 'msg-bubble--user' : 'msg-bubble--ai']"
						>
							<!-- 打字动画 -->
							<view v-if="m.loading && !m.isStreaming" class="typing-dots">
								<view class="typing-dot"></view>
								<view class="typing-dot"></view>
								<view class="typing-dot"></view>
							</view>

							<!-- AI 消息：文本 + 加粗 + 列表 -->
							<view v-else-if="m.role === 'ai' && !m.loading" class="bubble-md">
								<block v-for="(line, li) in m._lines" :key="li">
									<!-- 无序列表 -->
									<view v-if="line._ul" class="md-li">
										<view class="md-li-dot"></view>
										<text class="md-li-text">
											<text v-for="(seg, si) in line._segs" :key="si" :class="seg.b ? 'md-bold' : ''">{{ seg.t }}</text>
										</text>
									</view>
									<!-- 有序列表 -->
									<view v-else-if="line._ol" class="md-oli">
										<text class="md-oli-num">{{ line._ol }}.</text>
										<text class="md-oli-text">
											<text v-for="(seg, si) in line._segs" :key="si" :class="seg.b ? 'md-bold' : ''">{{ seg.t }}</text>
										</text>
									</view>
									<!-- 空行 -->
									<view v-else-if="!line._segs || line._segs.length === 0" class="md-gap"></view>
									<!-- 普通文本 -->
									<text v-else class="md-line">
										<text v-for="(seg, si) in line._segs" :key="si" :class="seg.b ? 'md-bold' : ''">{{ seg.t }}</text>
									</text>
								</block>
								<text v-if="m.isStreaming" class="stream-cursor">▌</text>
							</view>

							<!-- 用户消息 -->
							<text v-else-if="m.role === 'user'" class="bubble-txt">{{ m.content }}</text>

							<!-- 错误重试 -->
							<view v-if="m.error" class="msg-error" @click.stop="retry">
								<text class="msg-error-text">⚠️ {{ m.error }}</text>
								<text class="msg-error-btn">重试</text>
							</view>
						</view>

						<!-- 消息操作按钮 -->
						<view
							v-if="group.role === 'ai' && group.messages.length && gi === groupedMessages.length - 1"
							class="msg-actions"
						>
							<view
								v-if="!group.messages[group.messages.length - 1].isStreaming"
								class="msg-action"
								:data-gi="gi"
								@click="copyLastMsg(gi)"
							>
								<text class="msg-action-icon">📋</text>
							</view>
							<view
								v-if="gi === groupedMessages.length - 1 && !group.messages[group.messages.length - 1].isStreaming"
								class="msg-action"
								@click="retry"
							>
								<text class="msg-action-icon">🔄</text>
							</view>
						</view>
					</view>

					<view v-if="group.role === 'user'" class="msg-avatar msg-avatar--user">
						<image v-if="userAvatar" class="msg-avatar-img" :src="userAvatar" mode="aspectFill"/>
						<text v-else class="avatar-label">👤</text>
					</view>
				</view>
			</view>

			<view id="msg-bottom" class="bottom-anchor"/>
		</view>
	</scroll-view>

	<!-- ====== 底部输入栏 ====== -->
	<view class="input-bar" :style="{ paddingBottom: (safeBottom + 12) + 'px' }">
		<view class="voice-btn" @click="onVoiceTap">
			<text class="voice-icon">🎤</text>
		</view>
		<view class="input-wrap">
			<textarea
				class="input-textarea"
				v-model="inputText"
				placeholder="告诉小速你想吃什么..."
				placeholder-style="color:#bdbdbd"
				:maxlength="500"
				:cursor-spacing="12"
				:show-confirm-bar="false"
				:adjust-position="true"
				:auto-height="true"
				:disabled="busy"
				@confirm="sendCurrent"
			/>
		</view>
		<view v-if="!busy" class="send-btn" :class="{ 'send-btn--on': hasText }" @click="sendCurrent">
			<text class="send-icon">➤</text>
		</view>
		<view v-else class="stop-btn" @click="stopStream">
			<text class="stop-icon">■</text>
		</view>
	</view>

	<!-- ====== 菜单浮层 ====== -->
	<view v-if="showMenu" class="menu-mask" @click="showMenu = false">
		<view class="menu-pop" @click.stop>
			<view class="menu-item" @click="onNewChat">
				<text class="menu-item-icon">💬</text>
				<text class="menu-item-text">新建对话</text>
			</view>
			<view class="menu-item" @click="onClearChat">
				<text class="menu-item-icon">🗑️</text>
				<text class="menu-item-text">清空对话</text>
			</view>
			<view class="menu-divider"/>
			<view class="menu-item" @click="showMenu = false">
				<text class="menu-item-text menu-item-text--cancel">取消</text>
			</view>
		</view>
	</view>
</view>
</template>

<script>
import { mapState, mapMutations } from 'vuex'
import { aiClearHistory } from '@/pages/api/api.js'
import { streamRequest } from '@/utils/stream.js'

export default {
	data() {
		return {
			inputText: '',
			busy: false,
			scrollToId: '',
			statusBarH: 0,
			safeBottom: 0,
			inputBarH: 64,
			lastUserMsg: '',
			streamTask: null,
			showMenu: false,
			nearBottom: true,
			_streamBuffer: '',
			_streamTimer: null,
			_lastStreamUpdate: 0,
			STREAM_THROTTLE_MS: 80,
			prompts: [
				{ icon: '🍜', text: '今天吃什么好？' },
				{ icon: '🔥', text: '推荐热门菜品' },
				{ icon: '💰', text: '性价比高的套餐' },
				{ icon: '🌶️', text: '想吃辣的' },
				{ icon: '🥗', text: '低卡减脂推荐' },
				{ icon: '🍚', text: '有什么主食推荐？' }
			],
			featCards: [
				{ icon: '🍽️', title: '推荐美食', desc: '根据口味智能推荐菜品' },
				{ icon: '🛒', title: '快速点餐', desc: '对话即可完成下单' },
				{ icon: '💡', title: '解答疑问', desc: '菜品的食材口味信息' }
			]
		}
	},
	computed: {
		...mapState(['aiMessages', 'baseUserInfo', 'sessionId']),
		msgs() { return this.aiMessages || [] },
		hasMessages() { return this.msgs.length > 0 },
		hasText() { return !!this.inputText.trim() },
		userAvatar() {
			try { return JSON.parse(this.baseUserInfo || '{}').avatarUrl || '' } catch { return '' }
		},
		navTitle() {
			if (!this.hasMessages) return 'AI 小速'
			const f = this.msgs.find(m => m.role === 'user')
			if (f) { const t = f.content || ''; return t.length > 12 ? t.slice(0, 12) + '…' : t }
			return 'AI 小速'
		},
		scrollStyle() {
			const top = this.statusBarH + 44
			const bottom = this.inputBarH
			return 'top:' + top + 'px;bottom:' + bottom + 'px'
		},
		groupedMessages() {
			const msgs = this.msgs
			if (!msgs || msgs.length === 0) return []
			const groups = []
			let cur = null
			for (let i = 0; i < msgs.length; i++) {
				const m = this._enrichMsg(msgs[i])
				if (!cur || cur.role !== m.role) {
					let timeLabel = ''
					if (m.time) { timeLabel = this.fmtTimeGroup(m.time, m._timestamp) }
					cur = { role: m.role, messages: [m], timeLabel: timeLabel }
					groups.push(cur)
				} else {
					cur.messages.push(m)
				}
			}
			return groups
		}
	},
	mounted() {
		this.calcLayout()
	},
	methods: {
		...mapMutations([
			'setAIMessages', 'setCartCount', 'clearAIMessages', 'setSessionId',
			'addAIMessage', 'updateLastAIMessage', 'finishLastAIMessage', 'setAIStreamingTask'
		]),

		// ===== 预处理消息（解析 Markdown 行），内容变化时自动重新解析 =====
		_enrichMsg(m) {
			const contentLen = (m.content && m.content.length) || 0
			if (m._lines && m._linesLen === contentLen) return m
			if (m.role !== 'ai' || !m.content) {
				m._lines = []
				m._linesLen = 0
				return m
			}
			const lines = []
			const rawLines = m.content.split('\n')
			for (let i = 0; i < rawLines.length; i++) {
				const raw = rawLines[i]
				if (!raw.trim()) { lines.push({ _segs: [] }); continue }
				const ulMatch = raw.match(/^[\s]*[-*•]\s+(.+)/)
				if (ulMatch) {
					lines.push({ _ul: true, _segs: this._parseBold(ulMatch[1]) })
					continue
				}
				const olMatch = raw.match(/^[\s]*(\d+)\.\s+(.+)/)
				if (olMatch) {
					lines.push({ _ol: parseInt(olMatch[1]), _segs: this._parseBold(olMatch[2]) })
					continue
				}
				lines.push({ _segs: this._parseBold(raw) })
			}
			this.$set(m, '_lines', lines)
			this.$set(m, '_linesLen', contentLen)
			return m
		},

		// 行内加粗解析 → [{ t:'text', b:false }, { t:'bold', b:true }]
		_parseBold(text) {
			const segs = []
			let remain = text || ''
			let safety = 0
			while (remain.length > 0 && safety < 20) {
				const m = remain.match(/\*\*(.+?)\*\*/)
				if (m && m.index !== undefined) {
					if (m.index > 0) segs.push({ t: remain.slice(0, m.index), b: false })
					segs.push({ t: m[1], b: true })
					remain = remain.slice(m.index + m[0].length)
				} else {
					segs.push({ t: remain, b: false })
					break
				}
				safety++
			}
			return segs
		},

		// ===== 去除 [ID:数字]/[D:数字]/（ID：数字）等内部标记（前端安全网） =====
		_stripIds(text) {
			if (!text) return ''
			return text.replace(/\[ID:\d+\]/g, '')
				.replace(/\[D:\d+\]/g, '')
				.replace(/（ID：\d+）/g, '')
				.replace(/\(ID:\d+\)/g, '')
				.replace(/【ID[：:]\d+】/g, '')
		},

		// ===== 布局 =====
		calcLayout() {
			let h = 667, b = 0, s = 20
			try {
				if (typeof wx !== 'undefined' && wx.getWindowInfo) {
					const w = wx.getWindowInfo()
					h = w.screenHeight || 667
					if (w.safeArea) b = h - w.safeArea.bottom
					if (w.statusBarHeight) s = w.statusBarHeight
				} else {
					const info = uni.getSystemInfoSync()
					h = info.screenHeight || 667
					s = info.statusBarHeight || 20
					if (info.safeAreaInsets) b = h - info.safeAreaInsets.bottom
				}
			} catch (e) {}
			this.statusBarH = s
			this.safeBottom = Math.max(0, b)
			this.inputBarH = 56 + this.safeBottom
		},

		// ===== 快捷提问 =====
		onPromptTap(idx) {
			const p = this.prompts[idx]
			if (p) this.send(p.text)
		},

		// ===== 发送当前输入 =====
		sendCurrent() {
			this.send(this.inputText)
		},

		// ===== 导航 =====
		goBack() {
			uni.navigateBack({ delta: 1 }).catch(() => {
				uni.switchTab({ url: '/pages/index/index' })
			})
		},
		onNewChat() { this.showMenu = false; this.handleClear() },
		onClearChat() { this.showMenu = false; this.handleClear() },

		// ===== 发送消息（SSE 流式） =====
		send(t) {
			const msg = (t || '').trim()
			if (!msg || this.busy) return

			this.inputText = ''
			this.lastUserMsg = msg
			this.busy = true

			const now = this.fmtTime()
			const ts = Date.now()
			const msgs = [...this.msgs]
			msgs.push({ role: 'user', content: msg, time: now, _timestamp: ts })
			msgs.push({ role: 'ai', content: '', loading: true, isStreaming: false, time: '', _timestamp: ts + 1 })
			this.setAIMessages(msgs)
			this.nearBottom = true
			this.$nextTick(() => this.scrollToBottom())

			const self = this
			this._streamBuffer = ''
			this._lastStreamUpdate = Date.now()

			const task = streamRequest({
				url: '/user/ai/chat/stream',
				data: { message: msg, sessionId: this.sessionId || '' },
				onMessage(chunk) {
					self._streamBuffer += self._stripIds(chunk)
					const n = Date.now()
					if (n - self._lastStreamUpdate >= self.STREAM_THROTTLE_MS) {
						self._flushStreamBuffer()
						self._lastStreamUpdate = n
					}
				},
				onDone() {
					self._flushStreamBuffer()
					const arr = [...self.msgs]
					const last = arr.length - 1
					if (last >= 0 && arr[last].role === 'ai') {
						arr[last].isStreaming = false
						arr[last].loading = false
						if (!arr[last].content) arr[last].content = '收到回复，但内容为空，请重试'
						if (!arr[last].time) arr[last].time = self.fmtTime()
					}
					self.setAIMessages(arr)
					self._endStream()
				},
				onError(err) {
					self._flushStreamBuffer()
					const arr = [...self.msgs]
					const last = arr.length - 1
					if (last >= 0 && arr[last].role === 'ai') {
						arr[last].loading = false
						arr[last].isStreaming = false
						arr[last].error = err || '服务暂不可用'
						if (!arr[last].content) arr[last].content = '抱歉，服务暂不可用，请稍后重试 😅'
						if (!arr[last].time) arr[last].time = self.fmtTime()
					}
					self.setAIMessages(arr)
					self._endStream()
				},
				onCartUpdated(count) {
					self.setCartCount(count)
				}
			})

			this.streamTask = task
			this.setAIStreamingTask(task)

			this._streamTimer = setInterval(() => {
				if (!self.busy) { clearInterval(self._streamTimer); self._streamTimer = null; return }
				if (self._streamBuffer) { self._flushStreamBuffer(); self._lastStreamUpdate = Date.now() }
			}, 120)
		},

		_endStream() {
			this.busy = false
			this.streamTask = null
			this.setAIStreamingTask(null)
			this.$nextTick(() => this.scrollToBottom())
			clearInterval(this._streamTimer); this._streamTimer = null
			this.refreshCart()
		},

		_flushStreamBuffer() {
			if (!this._streamBuffer) return
			const chunk = this._streamBuffer
			this._streamBuffer = ''

			const arr = [...this.msgs]
			const last = arr.length - 1
			if (last < 0 || arr[last].role !== 'ai') return

			if (arr[last].loading) {
				arr[last].loading = false
				arr[last].isStreaming = true
				arr[last].time = this.fmtTime()
			}
			arr[last].content += chunk
			this.setAIMessages(arr)
			if (this.nearBottom) this.scrollToBottom()
		},

		stopStream() {
			if (this.streamTask && this.streamTask.abort) this.streamTask.abort()
			clearInterval(this._streamTimer); this._streamTimer = null
			this._flushStreamBuffer()
			const arr = [...this.msgs]
			const last = arr.length - 1
			if (last >= 0 && arr[last].role === 'ai') {
				arr[last].isStreaming = false; arr[last].loading = false
				if (!arr[last].content) arr[last].content = '已停止生成'
				if (!arr[last].time) arr[last].time = this.fmtTime()
			}
			this.setAIMessages(arr)
			this._endStream()
		},

		retry() {
			if (this.lastUserMsg) {
				const arr = [...this.msgs]
				if (arr.length > 0 && arr[arr.length - 1].role === 'ai') arr.pop()
				this.setAIMessages(arr)
				this.send(this.lastUserMsg)
			}
		},

		copyLastMsg(gi) {
			const group = this.groupedMessages[gi]
			if (!group) return
			const msg = group.messages[group.messages.length - 1]
			if (!msg || !msg.content) return
			uni.setClipboardData({
				data: msg.content,
				success() {
					try { wx.vibrateShort({ type: 'light' }) } catch (e) {}
					uni.showToast({ title: '已复制', icon: 'success', duration: 1500 })
				}
			})
		},

		scrollToBottom() {
			this.scrollToId = ''
			this.$nextTick(() => { this.scrollToId = 'msg-bottom' })
		},

		onTouchMsgScroll() { this.nearBottom = true },

		handleClear() {
			const self = this
			uni.showModal({
				title: '清空对话',
				content: '确定要清空当前对话吗？',
				confirmColor: '#1a56db',
				success(r) {
					if (r.confirm) {
						aiClearHistory(self.sessionId || '').catch(() => {})
						self.clearAIMessages()
						self.setSessionId('')
						self.lastUserMsg = ''
						self.scrollToId = ''
						uni.showToast({ title: '已清空', icon: 'success', duration: 1200 })
					}
				}
			})
		},

		onVoiceTap() {
			uni.showToast({ title: '语音输入即将上线', icon: 'none', duration: 1500 })
		},

		async refreshCart() {
			try {
				const api = await import('@/pages/api/api.js')
				const res = await api.getShoppingCartList()
				if (res && res.code === 1 && res.data) {
					const total = res.data.reduce((s, x) => s + (x.number || 0), 0)
					this.setCartCount(total)
				}
			} catch (e) {}
		},

		// ===== 时间 =====
		fmtTime() {
			const d = new Date()
			return String(d.getHours()).padStart(2, '0') + ':' + String(d.getMinutes()).padStart(2, '0')
		},
		fmtTimeGroup(timeStr, ts) {
			if (!timeStr) return ''
			const then = ts ? new Date(ts) : new Date()
			const now = new Date()
			const today = new Date(now.getFullYear(), now.getMonth(), now.getDate()).getTime()
			const thatDay = new Date(then.getFullYear(), then.getMonth(), then.getDate()).getTime()
			const diffDays = Math.floor((today - thatDay) / 86400000)
			if (diffDays === 0) return '今天 ' + timeStr
			if (diffDays === 1) return '昨天 ' + timeStr
			const mo = String(then.getMonth() + 1).padStart(2, '0')
			const dd = String(then.getDate()).padStart(2, '0')
			return mo + '-' + dd + ' ' + timeStr
		}
	}
}
</script>

<style> page { height:100vh; overflow:hidden; background:#f3f4f7 } </style>
<style scoped lang="scss">
$brand: #1a56db;
$brand-light: #3b82f6;
$brand-bg: #eff6ff;
$page-bg: #f3f4f7;
$text-dark: #1f2937;
$text-mid: #6b7280;
$text-light: #9ca3af;

// ===== 导航栏 =====
.navbar {
	position: fixed; top: 0; left: 0; right: 0; z-index: 100;
	background: rgba(255,255,255,.92);
	backdrop-filter: blur(20rpx);
	-webkit-backdrop-filter: blur(20rpx);
	border-bottom: 1rpx solid #f0f0f0;
}
.navbar-inner {
	height: 44px;
	display: flex; align-items: center; justify-content: space-between;
	padding: 0 16rpx;
}
.nav-left { width: 80rpx; height: 44px; display: flex; align-items: center; }
.nav-back-icon { font-size: 52rpx; color: $brand; font-weight: 300; line-height: 1; margin-top: -4rpx; }
.nav-title { flex: 1; text-align: center; font-size: 32rpx; font-weight: 600; color: $text-dark; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.nav-right { width: 80rpx; height: 44px; display: flex; align-items: center; justify-content: flex-end; }
.nav-menu-icon { font-size: 40rpx; color: $text-mid; font-weight: 700; letter-spacing: 2rpx; padding: 8rpx; }

// ===== 消息滚动区 =====
.msg-scroll {
	position: fixed; left: 0; right: 0;
	overflow-y: auto; box-sizing: border-box;
	-webkit-overflow-scrolling: touch; background: $page-bg;
}

// ===== 欢迎页 =====
.welcome { padding: 60rpx 32rpx 80rpx; display: flex; flex-direction: column; align-items: center; }
.w-brand { display: flex; flex-direction: column; align-items: center; margin-bottom: 48rpx; }
.w-avatar-glow {
	width: 140rpx; height: 140rpx; border-radius: 50%;
	background: radial-gradient(circle at 50% 40%, rgba($brand,.15) 0%, transparent 70%);
	display: flex; align-items: center; justify-content: center; margin-bottom: 32rpx;
}
.w-avatar-ring {
	width: 108rpx; height: 108rpx; border-radius: 50%;
	background: linear-gradient(135deg, $brand, $brand-light);
	display: flex; align-items: center; justify-content: center;
	box-shadow: 0 12rpx 48rpx rgba($brand, .28);
}
.w-avatar-text { font-size: 38rpx; font-weight: 900; color: #fff; letter-spacing: 2rpx; }
.w-title { font-size: 40rpx; font-weight: 700; color: $text-dark; margin-bottom: 12rpx; }
.w-sub { font-size: 26rpx; color: $text-mid; text-align: center; line-height: 1.6; }

.w-cards { display: flex; gap: 16rpx; width: 100%; margin-bottom: 48rpx; }
.w-card {
	flex: 1; background: #fff; border-radius: 24rpx; padding: 24rpx 20rpx;
	display: flex; flex-direction: column; align-items: center;
	box-shadow: 0 2rpx 16rpx rgba(0,0,0,.04);
}
.wc-icon { font-size: 44rpx; margin-bottom: 10rpx; }
.wc-title { font-size: 26rpx; font-weight: 600; color: $text-dark; margin-bottom: 4rpx; }
.wc-desc { font-size: 20rpx; color: $text-light; text-align: center; line-height: 1.4; }

.w-section-label { font-size: 24rpx; color: $text-light; margin-bottom: 20rpx; align-self: flex-start; padding-left: 4rpx; }
.w-prompts { display: flex; flex-wrap: wrap; gap: 16rpx; width: 100%; }
.wp-item {
	width: calc(50% - 8rpx); display: flex; align-items: center; gap: 12rpx;
	padding: 22rpx 20rpx; background: #fff; border: 1rpx solid #e5e7eb;
	border-radius: 20rpx; box-shadow: 0 2rpx 12rpx rgba(0,0,0,.025); box-sizing: border-box;
}
.wp-item:active { background: $brand-bg; border-color: $brand; transform: scale(.97); }
.wp-emoji { font-size: 32rpx; flex-shrink: 0; }
.wp-text { font-size: 26rpx; color: $text-dark; font-weight: 500; line-height: 1.4; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.w-disclaimer { font-size: 22rpx; color: #c0c4cc; margin-top: 56rpx; }

// ===== 对话区 =====
.chat-area { padding: 16rpx 0 40rpx; }
.time-divider { display: flex; align-items: center; justify-content: center; padding: 20rpx 0 10rpx; }
.time-divider-text { font-size: 22rpx; color: #b0b7c3; background: $page-bg; padding: 4rpx 24rpx; border-radius: 20rpx; }

// ===== 消息组 =====
.msg-group { display: flex; padding: 4rpx 20rpx; align-items: flex-start; }
.msg-group--right { flex-direction: row-reverse; }

.msg-avatar { flex-shrink: 0; width: 68rpx; height: 68rpx; border-radius: 50%; overflow: hidden; display: flex; align-items: center; justify-content: center; }
.msg-avatar--ai { background: linear-gradient(135deg, $brand-bg, #dbeafe); margin-right: 12rpx; box-shadow: 0 2rpx 8rpx rgba($brand,.08); }
.msg-avatar--user { background: linear-gradient(135deg, $brand, $brand-light); margin-left: 12rpx; box-shadow: 0 2rpx 12rpx rgba($brand,.2); }
.msg-avatar-img { width: 68rpx; height: 68rpx; border-radius: 50%; }
.avatar-label { font-size: 22rpx; font-weight: 800; color: $brand; }
.msg-avatar--user .avatar-label { font-size: 32rpx; color: #fff; }

.msg-group-body { max-width: 73%; display: flex; flex-direction: column; gap: 6rpx; }
.msg-group-body--right { align-items: flex-end; }

// ===== 气泡 =====
.msg-bubble { padding: 18rpx 22rpx; line-height: 1.65; word-break: break-all; }
.msg-bubble--ai { background: #fff; border-radius: 4rpx 20rpx 20rpx 20rpx; box-shadow: 0 2rpx 12rpx rgba(0,0,0,.035); }
.msg-bubble--user { background: linear-gradient(135deg, $brand, $brand-light); border-radius: 20rpx 4rpx 20rpx 20rpx; box-shadow: 0 2rpx 12rpx rgba($brand,.18); }
.bubble-txt { font-size: 28rpx; color: #fff; white-space: pre-wrap; line-height: 1.65; }

// Markdown 行
.bubble-md { font-size: 28rpx; color: $text-dark; line-height: 1.7; }
.md-line { display: block; min-height: 2rpx; }
.md-gap { height: 12rpx; }
.md-bold { font-weight: 700; }
.md-li { display: flex; align-items: flex-start; margin: 4rpx 0; padding-left: 4rpx; }
.md-li-dot { width: 8rpx; height: 8rpx; border-radius: 50%; background: $brand; margin-top: 14rpx; margin-right: 10rpx; flex-shrink: 0; }
.md-li-text { flex: 1; line-height: 1.65; }
.md-oli { display: flex; align-items: flex-start; margin: 4rpx 0; padding-left: 4rpx; }
.md-oli-num { min-width: 28rpx; font-weight: 600; color: $brand; margin-right: 8rpx; flex-shrink: 0; }
.md-oli-text { flex: 1; line-height: 1.65; }

// 打字
.typing-dots { display: flex; gap: 8rpx; padding: 6rpx 0; align-items: center; min-width: 60rpx; }
.typing-dot { width: 14rpx; height: 14rpx; border-radius: 50%; background: #c4c9d4; animation: dotBounce 1.4s infinite; }
.typing-dot:nth-child(1) { animation-delay: 0s; }
.typing-dot:nth-child(2) { animation-delay: .16s; }
.typing-dot:nth-child(3) { animation-delay: .32s; }
@keyframes dotBounce { 0%,80%,100% { transform:translateY(0); opacity:.35; } 40% { transform:translateY(-10rpx); opacity:1; } }
.stream-cursor { color: $brand; font-weight: 300; animation: blink 1s step-end infinite; margin-left: 2rpx; }
@keyframes blink { 0%,100% { opacity:1; } 50% { opacity:0; } }

// 消息操作
.msg-actions { display: flex; gap: 20rpx; margin-top: 10rpx; padding-left: 4rpx; }
.msg-action { width: 48rpx; height: 48rpx; display: flex; align-items: center; justify-content: center; border-radius: 50%; background: #f5f6f8; }
.msg-action:active { background: $brand-bg; transform: scale(.9); }
.msg-action-icon { font-size: 24rpx; }

// 错误
.msg-error { display: flex; align-items: center; gap: 12rpx; margin-top: 8rpx; padding: 6rpx 12rpx; }
.msg-error-text { font-size: 22rpx; color: #ef4444; }
.msg-error-btn { font-size: 22rpx; color: $brand; font-weight: 600; padding: 4rpx 16rpx; border: 1rpx solid $brand; border-radius: 8rpx; }
.msg-error-btn:active { background: $brand-bg; }

.bottom-anchor { height: 24rpx; }

// ===== 输入栏 =====
.input-bar {
	position: fixed; bottom: 0; left: 0; right: 0; z-index: 99;
	display: flex; align-items: flex-end; gap: 12rpx;
	padding: 12rpx 16rpx 0; background: #fff;
	border-top: 1rpx solid #f0f0f0; box-shadow: 0 -4rpx 20rpx rgba(0,0,0,.03);
	box-sizing: border-box;
}
.voice-btn { flex-shrink: 0; width: 72rpx; height: 72rpx; border-radius: 50%; background: #f5f6f8; display: flex; align-items: center; justify-content: center; }
.voice-btn:active { background: $brand-bg; }
.voice-icon { font-size: 36rpx; }
.input-wrap { flex: 1; background: #f5f6f8; border-radius: 28rpx; padding: 0 24rpx; }
.input-textarea { width: 100%; min-height: 44px; max-height: 120px; padding: 10rpx 0; font-size: 28rpx; color: $text-dark; line-height: 1.5; box-sizing: border-box; }

.send-btn { flex-shrink: 0; width: 72rpx; height: 72rpx; border-radius: 50%; background: #e5e7eb; display: flex; align-items: center; justify-content: center; transition: .2s; }
.send-btn--on { background: linear-gradient(135deg, $brand, $brand-light); box-shadow: 0 4rpx 16rpx rgba($brand, .3); }
.send-btn--on:active { transform: scale(.9); }
.send-icon { font-size: 28rpx; color: #fff; font-weight: 700; }
.stop-btn { flex-shrink: 0; width: 72rpx; height: 72rpx; border-radius: 50%; background: #ef4444; box-shadow: 0 4rpx 16rpx rgba(#ef4444, .3); display: flex; align-items: center; justify-content: center; }
.stop-btn:active { transform: scale(.9); }
.stop-icon { font-size: 24rpx; color: #fff; }

// ===== 菜单 =====
.menu-mask { position: fixed; top:0; left:0; right:0; bottom:0; z-index:200; background: rgba(0,0,0,.35); display: flex; align-items: flex-end; justify-content: center; padding-bottom: 120rpx; }
.menu-pop { width: 640rpx; background: #fff; border-radius: 28rpx; padding: 8rpx 0 24rpx; box-shadow: 0 8rpx 48rpx rgba(0,0,0,.12); }
.menu-item { display: flex; align-items: center; padding: 28rpx 32rpx; gap: 20rpx; }
.menu-item:active { background: #f9fafb; }
.menu-item-icon { font-size: 36rpx; }
.menu-item-text { font-size: 30rpx; color: $text-dark; }
.menu-item-text--cancel { color: $text-light; font-weight: 500; }
.menu-divider { height: 8rpx; background: #f3f4f7; margin: 8rpx 0; }
</style>
