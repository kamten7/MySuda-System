<template>
	<view class="chat-bubble" :class="'chat-bubble--' + role">
		<!-- AI 消息头像 -->
		<view class="chat-bubble__avatar" v-if="role === 'ai'">
			<view class="avatar-ai">
				<text class="avatar-emoji">🤖</text>
			</view>
		</view>

		<!-- 消息内容 -->
		<view class="chat-bubble__content">
			<view class="chat-bubble__body" :class="'chat-bubble__body--' + role">
				<!-- 文本内容 -->
				<view class="bubble-text" v-if="!hasCards">
					<text class="bubble-text-inner">{{ content }}</text>
					<text v-if="isStreaming" class="typing-cursor">▌</text>
				</view>

				<!-- 带菜品卡片的混合内容 -->
				<view class="bubble-mixed" v-else>
					<view class="bubble-text" v-if="textContent">
						<text class="bubble-text-inner">{{ textContent }}</text>
					</view>
					<view class="bubble-cards" v-if="cards && cards.length">
						<view class="mini-dish-card" v-for="(card, i) in cards" :key="i" @click="$emit('card-click', card)">
							<image :src="card.image" class="mini-dish-img" mode="aspectFill" />
							<view class="mini-dish-info">
								<text class="mini-dish-name">{{ card.name }}</text>
								<text class="mini-dish-price">¥{{ card.price }}</text>
							</view>
						</view>
					</view>
					<text v-if="isStreaming" class="typing-cursor">▌</text>
				</view>
			</view>

			<!-- 时间（可选） -->
			<view class="chat-bubble__time" v-if="time">{{ time }}</view>
		</view>

		<!-- 用户头像 -->
		<view class="chat-bubble__avatar" v-if="role === 'user'">
			<view class="avatar-user">
				<image v-if="userAvatar" :src="userAvatar" class="avatar-img" />
				<text v-else class="avatar-emoji">😋</text>
			</view>
		</view>
	</view>
</template>

<script>
export default {
	name: 'ChatBubble',
	props: {
		role: {
			type: String,
			default: 'ai',         // 'ai' | 'user'
			validator: v => ['ai', 'user'].includes(v)
		},
		content: {
			type: String,
			default: ''
		},
		textContent: {
			type: String,
			default: ''
		},
		cards: {
			type: Array,
			default: () => []
		},
		isStreaming: {
			type: Boolean,
			default: false
		},
		time: {
			type: String,
			default: ''
		},
		userAvatar: {
			type: String,
			default: ''
		}
	},
	computed: {
		hasCards() {
			return this.cards && this.cards.length > 0
		}
	}
}
</script>

<style lang="scss" scoped>
.chat-bubble {
	display: flex;
	padding: 16rpx 24rpx;
	align-items: flex-start;

	&--user {
		flex-direction: row-reverse;
	}
}

/* 头像 */
.chat-bubble__avatar {
	flex-shrink: 0;
}

.avatar-ai,
.avatar-user {
	width: 72rpx;
	height: 72rpx;
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
}

.avatar-ai {
	background: linear-gradient(135deg, $brand-primary-bg, $brand-primary-border);
	.avatar-emoji { font-size: 36rpx; }
}

.avatar-user {
	background: linear-gradient(135deg, #fef3c7, #fde68a);
	.avatar-emoji { font-size: 36rpx; }
}

.avatar-img {
	width: 72rpx;
	height: 72rpx;
	border-radius: 50%;
}

/* 消息内容 */
.chat-bubble__content {
	max-width: calc(100% - 120rpx);
	min-width: 0;
}

.chat-bubble__body {
	padding: 16rpx 20rpx;
	font-size: $font-base;
	line-height: 1.7;
	word-break: break-all;
	position: relative;

	&--ai {
		background: #fff;
		border: 1rpx solid $border-normal;
		border-radius: 4rpx 16rpx 16rpx 16rpx;
		color: $text-primary;
		margin-left: 12rpx;
		box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04);
	}

	&--user {
		background: $brand-primary;
		border-radius: 16rpx 4rpx 16rpx 16rpx;
		color: #fff;
		margin-right: 12rpx;
	}
}

.bubble-text-inner {
	white-space: pre-wrap;
}

.typing-cursor {
	animation: blink-cursor 1s step-end infinite;
	color: $brand-primary;
}

@keyframes blink-cursor {
	0%, 100% { opacity: 1; }
	50% { opacity: 0; }
}

.chat-bubble--user .typing-cursor {
	color: rgba(255,255,255,0.8);
}

/* 菜品小卡片 */
.bubble-cards {
	display: flex;
	flex-wrap: wrap;
	gap: 12rpx;
	margin-top: 12rpx;
}

.mini-dish-card {
	display: flex;
	align-items: center;
	background: #f9fafb;
	border: 1rpx solid $border-light;
	border-radius: $radius-md;
	padding: 8rpx;
	padding-right: 16rpx;
	gap: 12rpx;
	max-width: 100%;

	&:active {
		background: $brand-primary-bg;
		border-color: $brand-primary-border;
	}
}

.mini-dish-img {
	width: 72rpx;
	height: 72rpx;
	border-radius: 8rpx;
	background: #f0f0f0;
}

.mini-dish-info {
	display: flex;
	flex-direction: column;
	gap: 4rpx;
}

.mini-dish-name {
	font-size: $font-sm;
	color: $text-primary;
	font-weight: 500;
}

.mini-dish-price {
	font-size: $font-sm;
	color: $brand-accent;
	font-weight: 600;
}

/* 时间 */
.chat-bubble__time {
	font-size: 20rpx;
	color: $text-tertiary;
	margin-top: 8rpx;
	padding: 0 16rpx;
}
</style>
