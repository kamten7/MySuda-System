<template>
	<view class="my-page">
		<!-- 功能内容区（直接从顶部开始） -->
		<view class="my-content">
			<!-- 用户信息卡片 -->
			<view class="my-user-card">
				<image class="my-avatar" :src="psersonUrl" mode="aspectFill" />
				<view class="my-info">
					<view class="my-name-row">
						<text class="my-name">{{ nickName }}</text>
						<image v-if="String(gender) === '2'" class="my-gender-icon" src="/static/girl.png" />
						<image v-else class="my-gender-icon" src="/static/boy.png" />
					</view>
					<text class="my-phone">{{ phoneNumber | getPhoneNum }}</text>
				</view>
			</view>

			<!-- 地址 + 历史订单 -->
			<view class="my-card">
				<view class="my-card-item" @click="goAddress">
					<view class="my-card-left">
						<view class="my-card-icon my-card-icon--address">
							<text>📍</text>
						</view>
						<text class="my-card-label">地址管理</text>
					</view>
					<text class="my-card-arrow">›</text>
				</view>
				<view class="my-card-divider"></view>
				<view class="my-card-item" @click="goOrder">
					<view class="my-card-left">
						<view class="my-card-icon my-card-icon--order">
							<text>📋</text>
						</view>
						<text class="my-card-label">历史订单</text>
					</view>
					<text class="my-card-arrow">›</text>
				</view>
			</view>

			<!-- 最近订单 -->
			<view class="my-recent" v-if="recentOrdersList && recentOrdersList.length > 0">
				<view class="my-section-title">最近订单</view>
				<view class="my-order-card" v-for="(item, index) in recentOrdersList" :key="index" @click="goDetail(item.id)">
					<view class="order-card-header">
						<text class="order-card-time">{{ item.checkoutTime }}</text>
						<text class="order-card-status" :class="'status-' + item.status">{{ statusWord(item.status) }}</text>
					</view>
					<view class="order-card-items">
						<view class="order-card-item" v-for="(num, y) in item.orderDetails" :key="y">
							<text class="order-item-name">{{ num.name }}</text>
							<text class="order-item-num">x{{ num.number }}</text>
						</view>
					</view>
					<view class="order-card-footer">
						<text class="order-card-total">共{{ sumOrder.number }}件，实付 <text class="total-price">¥{{ sumOrder.amount }}</text></text>
						<view class="order-card-again" v-if="item.status === 4" @click="oneOrderFun(item.id)">
							<text>再来一单</text>
						</view>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
import { queryOrderUserPage, oneOrderAgain, delShoppingCart } from '../api/api.js'
import { mapMutations } from 'vuex'

export default {
	data () {
		return {
			psersonUrl: '/static/logo_brand.svg',
			nickName: '速达用户',
			gender: '0',
			phoneNumber: '18500557668',
			recentOrdersList: [],
			sumOrder: {
				amount: 0,
				number: 0
			}
		}
	},
	filters: {
		getPhoneNum (str) {
			const reg = /^(\d{3})\d*(\d{4})$/
			return str.replace(/(\d{3})\d*(\d{4})/, '$1****$2')
		}
	},
	onLoad () {
		const info = this.$store.state.baseUserInfo
		if (info) {
			this.psersonUrl = info.avatarUrl || this.psersonUrl
			this.nickName = info.nickName || '速达用户'
			this.gender = info.gender || '0'
		}
		this.getList()
	},
	methods: {
		...mapMutations(['setAddressBackUrl']),
		statusWord (status) {
			const map = { 1: '待付款', 2: '待派送', 3: '已派送', 4: '已完成', 5: '已取消' }
			return map[status] || '未知'
		},
		getList () {
			queryOrderUserPage({ pageSize: 1, page: 1 }).then(res => {
				if (res.code === 1) {
					const data = res.data
					let number = 0, amount = 0
					if (data.records && data.records.length > 0) {
						data.records[0].orderDetails && data.records[0].orderDetails.forEach(item => {
							number += item.number
							amount += Number(item.amount)
						})
						this.sumOrder = { amount: amount.toFixed(2), number }
					}
					this.recentOrdersList = data.records || []
				}
			}).catch(() => {})
		},
		goAddress () {
			this.setAddressBackUrl('/pages/my/my')
			uni.navigateTo({ url: '/pages/address/address?form=my' })
		},
		goOrder () {
			uni.navigateTo({ url: '/pages/historyOrder/historyOrder' })
		},
			goDetail (id) {
				uni.navigateTo({ url: '/pages/orderDetail/orderDetail?id=' + id })
			},
		async oneOrderFun (id) {
			let pages = getCurrentPages()
			let routeIndex = pages.findIndex(item => item.route === 'pages/index/index')
			await delShoppingCart()
			oneOrderAgain({ id }).then(res => {
				if (res.code === 1) {
					uni.switchTab({ url: '/pages/index/index' })
				}
			}).catch(() => {})
		}
	}
}
</script>

<style lang="scss" scoped>
.my-page {
	min-height: 100vh;
	background: $page-bg;
}

/* 内容区（从顶部开始，无头部渐变） */
.my-content {
	padding: 24rpx;
}

/* 用户信息卡片 */
.my-user-card {
	background: linear-gradient(135deg, $brand-primary-bg, #dbeafe);
	border-radius: $card-radius;
	padding: 32rpx 28rpx;
	margin-bottom: 20rpx;
	box-shadow: $shadow-card;
	display: flex;
	align-items: center;
	gap: 24rpx;
}

.my-avatar {
	width: 100rpx;
	height: 100rpx;
	border-radius: 50%;
	border: 3rpx solid rgba(26, 86, 219, 0.2);
	background: rgba(255, 255, 255, 0.8);
	flex-shrink: 0;
}

.my-info {
	flex: 1;
}

.my-name-row {
	display: flex;
	align-items: center;
	gap: 8rpx;
}

.my-name {
	font-size: 34rpx;
	font-weight: 700;
	color: $brand-primary-dark;
}

.my-gender-icon {
	width: 28rpx;
	height: 28rpx;
}

.my-phone {
	font-size: $font-sm;
	color: rgba(30, 58, 138, 0.6);
	margin-top: 6rpx;
}

/* 功能区卡片 */
.my-card {
	background: #fff;
	border-radius: $card-radius;
	box-shadow: $shadow-card;
	overflow: visible;
	margin-bottom: 16rpx;
}

.my-card-item {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 32rpx 28rpx;
	transition: background 0.15s;

	&:active {
		background: #fafbfc;
	}
}

.my-card-left {
	display: flex;
	align-items: center;
	gap: 20rpx;
}

.my-card-icon {
	width: 56rpx;
	height: 56rpx;
	border-radius: $radius-md;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 30rpx;
	box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);

	&--address {
		background: linear-gradient(135deg, $brand-primary-bg, #dbeafe);
	}
	&--order {
		background: linear-gradient(135deg, #fef3c7, #fde68a);
	}
}

.my-card-label {
	font-size: $font-md;
	color: $text-primary;
	font-weight: 500;
}

.my-card-arrow {
	font-size: 32rpx;
	color: #d1d5db;
	font-weight: 300;
}

.my-card-divider {
	height: 1rpx;
	background: $border-light;
	margin-left: 96rpx;
}

/* 最近订单 */
.my-section-title {
	font-size: $font-lg;
	font-weight: 700;
	color: $text-primary;
	margin: 28rpx 8rpx 20rpx;
}

.my-order-card {
	background: #fff;
	border-radius: $radius-md;
	padding: 28rpx 28rpx;
	margin-bottom: 16rpx;
	box-shadow: $shadow-card;
	transition: box-shadow 0.2s;

	&:active {
		box-shadow: $shadow-card-hover;
	}
}

.order-card-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 16rpx;
	padding-bottom: 16rpx;
	border-bottom: 1rpx dashed #e5e7eb;
}

.order-card-time {
	font-size: $font-sm;
	color: $text-secondary;
}

.order-card-status {
	font-size: 22rpx;
	font-weight: 600;
	padding: 4rpx 16rpx;
	border-radius: $radius-sm;

	&.status-1 {
		color: $brand-accent;
		background: #fef3c7;
	}
	&.status-2 {
		color: $brand-primary;
		background: $brand-primary-bg;
	}
	&.status-3 {
		color: $brand-primary;
		background: $brand-primary-bg;
	}
	&.status-4 {
		color: $brand-success;
		background: $brand-success-bg;
	}
	&.status-5 {
		color: $text-tertiary;
		background: #f3f4f6;
	}
}

.order-card-items {
	margin-bottom: 16rpx;
}

.order-card-item {
	display: flex;
	justify-content: space-between;
	padding: 10rpx 0;
}

.order-item-name {
	font-size: $font-sm;
	color: $text-primary;
	font-weight: 400;
}

.order-item-num {
	font-size: $font-sm;
	color: $text-tertiary;
}

.order-card-footer {
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.order-card-total {
	font-size: 24rpx;
	color: $text-secondary;

	.total-price {
		font-weight: 700;
		color: $brand-accent;
		font-size: 26rpx;
	}
}

.order-card-again {
	padding: 12rpx 32rpx;
	background: linear-gradient(135deg, $brand-primary, #3b82f6);
	border: none;
	border-radius: $radius-round;
	box-shadow: 0 4rpx 16rpx rgba($brand-primary, 0.25);
	transition: all 0.2s;

	text {
		font-size: 24rpx;
		color: #fff;
		font-weight: 600;
	}

	&:active {
		transform: scale(0.95);
		opacity: 0.9;
	}
}
</style>
