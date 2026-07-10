<template>
	<view class="app-loading" v-if="visible">
		<view class="loading-mask">
			<view class="loading-spinner">
				<view class="spinner-ring"></view>
			</view>
			<view class="loading-text" v-if="text">{{ text }}</view>
		</view>
	</view>
</template>

<script>
export default {
	name: 'AppLoading',
	props: {
		visible: {
			type: Boolean,
			default: false
		},
		text: {
			type: String,
			default: '加载中...'
		}
	}
}
</script>

<style lang="scss" scoped>
.app-loading {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	z-index: 9999;
}

.loading-mask {
	width: 100%;
	height: 100%;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	background: rgba(255, 255, 255, 0.85);
	backdrop-filter: blur(4rpx);
}

.loading-spinner {
	width: 80rpx;
	height: 80rpx;
	position: relative;
}

.spinner-ring {
	width: 64rpx;
	height: 64rpx;
	margin: 8rpx;
	border-radius: 50%;
	border: 4rpx solid #e5e7eb;
	border-top-color: $brand-primary;
	animation: spin 0.8s linear infinite;
}

@keyframes spin {
	to { transform: rotate(360deg); }
}

.loading-text {
	margin-top: 24rpx;
	font-size: $font-base;
	color: $text-secondary;
}
</style>
