<template>
	<view class="my-page">
		<!-- 头部渐变区 -->
		<view class="my-header">
			<view class="my-header-bg"></view>
			<view class="my-header-content">
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
		</view>

		<!-- 功能区 -->
		<view class="my-content">
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
				<view class="my-order-card" v-for="(item, index) in recentOrdersList" :key="index">
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
			psersonUrl: '/static/btn_waiter_sel.png',
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
							amount += item.amount / 100
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

/* 头部 */
.my-header {
	position: relative;
	padding-bottom: 40rpx;
}

.my-header-bg {
	position: absolute;
	top: 0;
	left: 0;
	right: 0;
	height: 280rpx;
	background: linear-gradient(135deg, #1e3a8a 0%, #1a56db 100%);
	border-radius: 0 0 40rpx 40rpx;
}

.my-header-content {
	position: relative;
	display: flex;
	align-items: center;
	gap: 24rpx;
	padding: 40rpx 32rpx;
	padding-top: 80rpx;
}

.my-avatar {
	width: 120rpx;
	height: 120rpx;
	border-radius: 50%;
	border: 4rpx solid rgba(255,255,255,0.4);
	background: rgba(255,255,255,0.2);
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
	font-size: 36rpx;
	font-weight: 700;
	color: #fff;
}

.my-gender-icon {
	width: 32rpx;
	height: 32rpx;
}

.my-phone {
	font-size: $font-sm;
	color: rgba(255,255,255,0.75);
	margin-top: 6rpx;
}

/* 内容区 */
.my-content {
	padding: 0 24rpx;
	margin-top: -20rpx;
}

/* 功能区卡片 */
.my-card {
	background: #fff;
	border-radius: $radius-lg;
	box-shadow: $shadow-card;
	overflow: hidden;
}

.my-card-item {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 32rpx 28rpx;

	&:active {
		background: #fafafa;
	}
}

.my-card-left {
	display: flex;
	align-items: center;
	gap: 16rpx;
}

.my-card-icon {
	width: 48rpx;
	height: 48rpx;
	border-radius: 12rpx;
	display: flex;
	align-items: center;
	justify-content: center;

	&--address {
		background: $brand-primary-bg;
	}
	&--order {
		background: #fef3c7;
	}
}

.my-card-label {
	font-size: $font-md;
	color: $text-primary;
	font-weight: 500;
}

.my-card-arrow {
	font-size: 36rpx;
	color: #ccc;
	font-weight: 300;
}

.my-card-divider {
	height: 1rpx;
	background: $border-light;
	margin-left: 88rpx;
}

/* 最近订单 */
.my-section-title {
	font-size: $font-lg;
	font-weight: 700;
	color: $text-primary;
	margin: 32rpx 8rpx 16rpx;
}

.my-order-card {
	background: #fff;
	border-radius: $radius-md;
	padding: 24rpx 28rpx;
	margin-bottom: 16rpx;
	box-shadow: $shadow-card;
}

.order-card-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 16rpx;
	padding-bottom: 16rpx;
	border-bottom: 1rpx dashed $border-light;
}

.order-card-time {
	font-size: $font-sm;
	color: $text-secondary;
}

.order-card-status {
	font-size: $font-sm;
	font-weight: 600;

	&.status-1 { color: $brand-accent; }
	&.status-2 { color: $brand-primary; }
	&.status-3 { color: $brand-primary; }
	&.status-4 { color: $brand-success; }
	&.status-5 { color: $text-tertiary; }
}

.order-card-items {
	margin-bottom: 16rpx;
}

.order-card-item {
	display: flex;
	justify-content: space-between;
	padding: 8rpx 0;
}

.order-item-name {
	font-size: $font-sm;
	color: $text-secondary;
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
	font-size: $font-sm;
	color: $text-secondary;

	.total-price {
		font-weight: 600;
		color: $text-primary;
	}
}

.order-card-again {
	padding: 10rpx 28rpx;
	border: 1rpx solid $brand-primary;
	border-radius: 32rpx;

	text {
		font-size: $font-sm;
		color: $brand-primary;
		font-weight: 500;
	}

	&:active {
		background: $brand-primary-bg;
	}
}
</style>
