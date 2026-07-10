<template>
	<view class="nonet-page">
		<view class="nonet-content">
			<view class="nonet-icon-wrap">
				<text class="nonet-emoji">📡</text>
			</view>
			<view class="nonet-title">网络无法连接</view>
			<view class="nonet-desc">请检查网络设置后重试</view>
			<view class="nonet-btn" @click="reload">点击刷新</view>
		</view>
	</view>
</template>

<script>
export default {
	methods: {
		reload () {
			uni.switchTab({ url: '/pages/index/index' })
		}
	}
}
</script>

<style lang="scss" scoped>
.nonet-page {
	min-height: 100vh;
	background: $page-bg;
	display: flex;
	align-items: center;
	justify-content: center;
}

.nonet-content { text-align: center; padding: 40rpx; }

.nonet-icon-wrap {
	width: 160rpx;
	height: 160rpx;
	border-radius: 50%;
	background: #f3f4f7;
	display: flex;
	align-items: center;
	justify-content: center;
	margin: 0 auto 24rpx;

	.nonet-emoji { font-size: 72rpx; }
}

.nonet-title {
	font-size: 36rpx;
	font-weight: 600;
	color: $text-primary;
	margin-bottom: 8rpx;
}

.nonet-desc {
	font-size: $font-sm;
	color: $text-tertiary;
	margin-bottom: 40rpx;
}

.nonet-btn {
	display: inline-block;
	padding: 20rpx 56rpx;
	background: linear-gradient(135deg, $brand-primary, $brand-primary-light);
	color: #fff;
	font-size: $font-md;
	font-weight: 600;
	border-radius: $radius-round;
	box-shadow: 0 4rpx 16rpx rgba($brand-primary, 0.3);

	&:active { transform: scale(0.95); }
}
</style>
