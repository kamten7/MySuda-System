<template>
	<view class="history-page">
		<view class="history-list" v-if="recentOrdersList && recentOrdersList.length > 0">
			<view class="order-card" v-for="(item, index) in recentOrdersList" :key="index">
				<view class="order-card-head">
					<text class="order-time">{{ item.orderTime || item.checkoutTime }}</text>
					<text class="order-status" :class="'status-' + item.status">{{ statusWord(item.status) }}</text>
				</view>
				<view class="order-items">
					<view class="order-item" v-for="(num, y) in item.orderDetailList" :key="y">
						<text class="item-name">{{ num.name }}</text>
						<text class="item-qty">x{{ num.number }}</text>
					</view>
				</view>
				<view class="order-card-foot">
					<text class="order-total">共{{ numes(item.orderDetailList).count }}件，实付 <text class="total-price">¥{{ numes(item.orderDetailList).total }}</text></text>
					<view class="order-again-btn" v-if="item.status === 4" @click="oneMoreOrder(item.id)">
						<text>再来一单</text>
					</view>
				</view>
			</view>
			<view class="list-footer" v-if="finished">
				<text class="footer-text">— 没有更多了 —</text>
			</view>
			<view class="list-footer" v-else-if="loadingStatus === 'loading'">
				<text class="footer-text loading-text">加载中...</text>
			</view>
		</view>
		<view class="history-empty" v-else>
			<view class="empty-icon-wrap">
				<text class="empty-emoji">📋</text>
			</view>
			<text class="empty-text">暂无订单记录</text>
		</view>
	</view>
</template>

<script>
import { queryOrderUserPage, oneOrderAgain, delShoppingCart } from '../api/api.js'

export default {
	data () {
		return {
			recentOrdersList: [],
			pageInfo: { page: 1, pageSize: 10, total: 0 },
			finished: false,
			loadingStatus: 'complete'
		}
	},
	onLoad () {
		this.getList()
	},
	onPullDownRefresh () {
		this.pageInfo.page = 1
		this.recentOrdersList = []
		this.finished = false
		this.getList()
		uni.stopPullDownRefresh()
	},
	onReachBottom () {
		if (this.recentOrdersList.length < Number(this.pageInfo.total)) {
			this.pageInfo.page++
			this.loadingStatus = 'loading'
			this.getList()
		}
	},
	methods: {
		numes (list) {
			let count = 0, total = 0
			if (list && list.length > 0) {
				list.forEach(obj => {
					count += Number(obj.number)
					total += Number(obj.number) * Number(obj.amount)
				})
			}
			return { count, total: total.toFixed(2) }
		},
		statusWord (status) {
			const map = { 1: '待付款', 2: '待派送', 3: '已派送', 4: '已完成', 5: '已取消' }
			return map[status] || '未知'
		},
		getList () {
			queryOrderUserPage({ pageSize: 10, page: this.pageInfo.page }).then(res => {
				this.loadingStatus = 'complete'
				if (res.code === 1) {
					this.recentOrdersList = [...this.recentOrdersList, ...(res.data.records || [])]
					this.pageInfo.total = res.data.total
					this.finished = this.recentOrdersList.length >= Number(this.pageInfo.total)
				}
			}).catch(() => { this.loadingStatus = 'complete' })
		},
		async oneMoreOrder (id) {
			const pages = getCurrentPages()
			const routeIndex = pages.findIndex(item => item.route === 'pages/index/index')
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
.history-page {
	min-height: 100vh;
	background: $page-bg;
	padding: 16rpx 24rpx;
}

.order-card {
	background: #fff;
	border-radius: $radius-md;
	padding: 24rpx 28rpx;
	margin-bottom: 16rpx;
	box-shadow: $shadow-card;
}

.order-card-head {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding-bottom: 16rpx;
	border-bottom: 1rpx dashed $border-light;
}

.order-time {
	font-size: $font-sm;
	color: $text-secondary;
}

.order-status {
	font-size: $font-sm;
	font-weight: 600;

	&.status-1 { color: $brand-accent; }
	&.status-2 { color: $brand-primary; }
	&.status-3 { color: $brand-primary; }
	&.status-4 { color: $brand-success; }
	&.status-5 { color: $text-tertiary; }
}

.order-items {
	padding: 12rpx 0 16rpx;
}

.order-item {
	display: flex;
	justify-content: space-between;
	padding: 6rpx 0;
}

.item-name {
	font-size: $font-sm;
	color: $text-secondary;
}

.item-qty {
	font-size: $font-sm;
	color: $text-tertiary;
}

.order-card-foot {
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.order-total {
	font-size: $font-sm;
	color: $text-secondary;

	.total-price {
		font-weight: 600;
		color: $text-primary;
	}
}

.order-again-btn {
	padding: 8rpx 24rpx;
	border: 1rpx solid $brand-primary;
	border-radius: 32rpx;

	text {
		font-size: $font-sm;
		color: $brand-primary;
		font-weight: 500;
	}

	&:active { background: $brand-primary-bg; }
}

.list-footer {
	text-align: center;
	padding: 24rpx 0;
}

.footer-text {
	font-size: $font-sm;
	color: $text-tertiary;
}

.loading-text { color: $brand-primary; }

/* 空状态 */
.history-empty {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding-top: 200rpx;
}

.empty-icon-wrap {
	width: 160rpx;
	height: 160rpx;
	border-radius: 50%;
	background: #f3f4f7;
	display: flex;
	align-items: center;
	justify-content: center;
	margin-bottom: 24rpx;

	.empty-emoji { font-size: 72rpx; }
}

.empty-text {
	font-size: $font-md;
	color: $text-tertiary;
}
</style>
