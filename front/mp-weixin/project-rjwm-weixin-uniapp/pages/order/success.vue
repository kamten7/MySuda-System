<template>
	<view class="success-page">
		<view class="success-content">
			<view class="success-icon-wrap">
				<text class="success-emoji">✅</text>
			</view>
			<view class="success-title">下单成功</view>
			<view class="success-eta">预计 <text class="eta-time">{{ arrivalTime }}</text> 送达</view>
			<view class="success-desc">后厨已开始疯狂备餐中，请耐心等待~ 🍳</view>
			<view class="success-btns">
				<view class="btn btn--primary" @click="goIndex">返回首页</view>
				<view class="btn btn--outline" @click="goOrder">查看订单</view>
			</view>
		</view>
	</view>
</template>

<script>
import { mapState } from 'vuex'

export default {
	data () {
		return { arrivalTime: '' }
	},
	onLoad () {
		this.getHarfAnOur()
	},
	methods: {
		...mapState(['shopInfo']),
		getHarfAnOur () {
			const date = new Date()
			date.setTime(date.getTime() + 3600000)
			let hours = date.getHours()
			let minutes = date.getMinutes()
			if (hours < 10) hours = '0' + hours
			if (minutes < 10) minutes = '0' + minutes
			this.arrivalTime = hours + ':' + minutes
		},
		goIndex () {
			uni.switchTab({ url: '/pages/index/index' })
		},
		goOrder () {
			uni.navigateTo({ url: '/pages/historyOrder/historyOrder' })
		}
	}
}
</script>

<style lang="scss" scoped>
.success-page {
	min-height: 100vh;
	background: $page-bg;
	display: flex;
	align-items: center;
	justify-content: center;
}

.success-content {
	text-align: center;
	padding: 40rpx;
}

.success-icon-wrap {
	width: 160rpx;
	height: 160rpx;
	border-radius: 50%;
	background: #e8f5e9;
	display: flex;
	align-items: center;
	justify-content: center;
	margin: 0 auto 24rpx;
	box-shadow: 0 8rpx 32rpx rgba($brand-success, 0.2);

	.success-emoji { font-size: 72rpx; }
}

.success-title {
	font-size: 44rpx;
	font-weight: 700;
	color: $text-primary;
	margin-bottom: 12rpx;
}

.success-eta {
	font-size: $font-md;
	color: $text-secondary;
	margin-bottom: 8rpx;

	.eta-time { color: $brand-primary; font-weight: 600; }
}

.success-desc {
	font-size: $font-sm;
	color: $text-tertiary;
	margin-bottom: 48rpx;
}

.success-btns {
	display: flex;
	gap: 24rpx;
	justify-content: center;
}

.btn {
	padding: 20rpx 48rpx;
	font-size: $font-md;
	font-weight: 600;
	border-radius: $radius-round;

	&--primary {
		background: linear-gradient(135deg, $brand-primary, $brand-primary-light);
		color: #fff;
		box-shadow: 0 4rpx 16rpx rgba($brand-primary, 0.3);

		&:active { transform: scale(0.95); }
	}

	&--outline {
		background: #fff;
		color: $brand-primary;
		border: 1rpx solid $brand-primary;

		&:active { background: $brand-primary-bg; }
	}
}
</style>
