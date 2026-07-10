<template>
	<view class="app-empty">
		<view class="empty-icon">
			<image v-if="image" :src="image" class="empty-image" mode="aspectFit" />
			<view v-else class="empty-icon-default">
				<text class="icon-emoji">{{ defaultIcon }}</text>
			</view>
		</view>
		<view class="empty-title" v-if="title">{{ title }}</view>
		<view class="empty-desc" v-if="desc">{{ desc }}</view>
		<view class="empty-action" v-if="actionText" @click="handleAction">
			<text class="action-btn">{{ actionText }}</text>
		</view>
	</view>
</template>

<script>
export default {
	name: 'AppEmpty',
	props: {
		image: {
			type: String,
			default: ''
		},
		title: {
			type: String,
			default: '暂无数据'
		},
		desc: {
			type: String,
			default: ''
		},
		actionText: {
			type: String,
			default: ''
		},
		iconType: {
			type: String,
			default: 'default'  // default | search | cart | order | network
		}
	},
	computed: {
		defaultIcon() {
			const icons = {
				search: '🔍',
				cart: '🛒',
				order: '📋',
				network: '📡',
				default: '📭'
			}
			return icons[this.iconType] || icons.default
		}
	},
	methods: {
		handleAction() {
			this.$emit('action')
		}
	}
}
</script>

<style lang="scss" scoped>
.app-empty {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 80rpx 40rpx;
}

.empty-icon {
	margin-bottom: 24rpx;
}

.empty-image {
	width: 240rpx;
	height: 240rpx;
}

.empty-icon-default {
	width: 160rpx;
	height: 160rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	background: $brand-primary-bg;
	border-radius: 50%;

	.icon-emoji {
		font-size: 72rpx;
	}
}

.empty-title {
	font-size: $font-lg;
	color: $text-primary;
	font-weight: 500;
	margin-bottom: 8rpx;
}

.empty-desc {
	font-size: $font-sm;
	color: $text-tertiary;
	margin-bottom: 32rpx;
}

.empty-action {
	.action-btn {
		display: inline-block;
		padding: 16rpx 48rpx;
		background: $brand-primary;
		color: #fff;
		font-size: $font-base;
		border-radius: $radius-round;
	}
}
</style>
