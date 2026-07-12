<template>
<view class="order-detail-page">
	<!-- 骨架加载 -->
	<view class="detail-loading" v-if="loading">
		<view class="loading-card" v-for="i in 3" :key="i">
			<view class="skeleton-line skeleton-animate" style="width:60%;height:32rpx;"></view>
			<view class="skeleton-line skeleton-animate" style="width:90%;height:24rpx;margin-top:16rpx;"></view>
			<view class="skeleton-line skeleton-animate" style="width:40%;height:24rpx;margin-top:12rpx;"></view>
		</view>
	</view>

	<!-- 主体内容 -->
	<view class="detail-content" v-else>
		<!-- 订单状态卡片 -->
		<view class="status-card">
			<view class="status-icon-wrap">
				<text class="status-icon">{{ statusIcon }}</text>
			</view>
			<view class="status-info">
				<text class="status-text">{{ statusText }}</text>
				<text class="status-hint">{{ statusHint }}</text>
			</view>
		</view>

		<!-- 收货地址 -->
		<view class="section-card">
			<view class="section-head">
				<text class="section-icon">📍</text>
				<text class="section-title">收货信息</text>
			</view>
			<view class="address-body">
				<view class="address-contact">
					<text class="contact-name">{{ order.consignee || '--' }}</text>
					<text class="contact-phone">{{ phoneMask }}</text>
				</view>
				<text class="address-text">{{ order.address || '--' }}</text>
			</view>
		</view>

		<!-- 预计送达 -->
		<view class="section-card" v-if="order.estimatedDeliveryTime">
			<view class="section-head">
				<text class="section-icon">🕐</text>
				<text class="section-title">预计送达</text>
			</view>
			<text class="eta-text">{{ formatTime(order.estimatedDeliveryTime) }}</text>
		</view>

		<!-- 订单商品 -->
		<view class="section-card">
			<view class="section-head">
				<text class="section-icon">📋</text>
				<text class="section-title">订单商品</text>
			</view>
			<view class="goods-list">
				<view class="goods-item" v-for="(item, i) in order.orderDetailList" :key="i">
					<image
						class="goods-img"
						:src="item.image || '/static/logo_brand.svg'"
						mode="aspectFill"
					/>
					<view class="goods-info">
						<text class="goods-name">{{ item.name }}</text>
						<text class="goods-flavor" v-if="item.dishFlavor">{{ item.dishFlavor }}</text>
					</view>
					<view class="goods-right">
						<text class="goods-price">¥{{ formatAmount(item.amount) }}</text>
						<text class="goods-qty">x{{ item.number }}</text>
					</view>
				</view>
			</view>
		</view>

		<!-- 订单信息 -->
		<view class="section-card">
			<view class="section-head">
				<text class="section-icon">📝</text>
				<text class="section-title">订单信息</text>
			</view>
			<view class="info-rows">
				<view class="info-row">
					<text class="info-label">订单编号</text>
					<view class="info-value-wrap">
						<text class="info-value info-mono">{{ order.number || '--' }}</text>
						<text class="info-copy" @click="copyNumber">复制</text>
					</view>
				</view>
				<view class="info-row">
					<text class="info-label">下单时间</text>
					<text class="info-value">{{ formatTime(order.orderTime) }}</text>
				</view>
				<view class="info-row" v-if="order.checkoutTime">
					<text class="info-label">支付时间</text>
					<text class="info-value">{{ formatTime(order.checkoutTime) }}</text>
				</view>
				<view class="info-row" v-if="order.deliveryTime">
					<text class="info-label">送达时间</text>
					<text class="info-value">{{ formatTime(order.deliveryTime) }}</text>
				</view>
				<view class="info-row" v-if="order.remark">
					<text class="info-label">订单备注</text>
					<text class="info-value remark-value">{{ order.remark }}</text>
				</view>
				<view class="info-row" v-if="order.cancelReason">
					<text class="info-label">取消原因</text>
					<text class="info-value cancel-reason">{{ order.cancelReason }}</text>
				</view>
			</view>
		</view>

		<!-- 金额明细 -->
		<view class="section-card">
			<view class="section-head">
				<text class="section-icon">💰</text>
				<text class="section-title">金额明细</text>
			</view>
			<view class="info-rows">
				<view class="info-row">
					<text class="info-label">商品总额</text>
					<text class="info-value price-value">¥{{ goodsTotal }}</text>
				</view>
				<view class="info-row">
					<text class="info-label">配送费</text>
					<text class="info-value">¥6.00</text>
				</view>
				<view class="info-row" v-if="order.packAmount">
					<text class="info-label">包装费</text>
					<text class="info-value">¥{{ (order.packAmount / 100).toFixed(2) }}</text>
				</view>
				<view class="info-row info-row--total">
					<text class="info-label-total">实付金额</text>
					<text class="info-value-total">¥{{ formatAmount(order.amount) }}</text>
				</view>
			</view>
		</view>

		<view class="detail-spacer"></view>
	</view>

	<!-- 底部操作栏 -->
	<view class="detail-footer" v-if="!loading">
		<view class="footer-left">
			<text class="footer-total">实付 <text class="total-price">¥{{ formatAmount(order.amount) }}</text></text>
		</view>
		<view class="footer-actions">
			<view class="btn-again" v-if="order.status === 5" @click="oneMoreOrder">
				<text>再来一单</text>
			</view>
			<view class="btn-remind" v-if="order.status >= 2 && order.status <= 4" @click="remindOrder">
				<text>催单</text>
			</view>
			<view class="btn-cancel" v-if="order.status === 1" @click="cancelOrderHandle">
				<text>取消订单</text>
			</view>
			<view class="btn-pay" v-if="order.status === 1" @click="goPay">
				<text>去支付</text>
			</view>
		</view>
	</view>
</view>
</template>

<script>
import { queryOrderDetailById, oneOrderAgain, cancelOrder, delShoppingCart, remindOrder } from '../api/api.js'

export default {
	data() {
		return {
			loading: true,
			order: {},
			id: ''
		}
	},
	computed: {
		// 订单金额（元）
		goodsTotal() {
			if (!this.order.orderDetailList) return '0.00'
			const total = this.order.orderDetailList.reduce((s, item) => s + (item.amount || 0), 0)
			return total.toFixed(2)
		},
		// 脱敏手机号
		phoneMask() {
			const p = this.order.phone || ''
			return p.replace(/(\d{3})\d*(\d{4})/, '$1****$2')
		},
		// 状态图标
		statusIcon() {
			const map = { 1: '📋', 2: '⏳', 3: '✅', 4: '🛵', 5: '🎉', 6: '❌' }
			return map[this.order.status] || '📋'
		},
		// 状态文字
		statusText() {
			const map = { 1: '待付款', 2: '待接单', 3: '已接单', 4: '派送中', 5: '已完成', 6: '已取消' }
			return map[this.order.status] || '未知状态'
		},
		// 状态提示语
		statusHint() {
			const map = {
				1: '请尽快支付，超时将自动取消',
				2: '商家正在确认您的订单',
				3: '商家正在备餐中',
				4: '骑手正在赶往目的地',
				5: '感谢您的惠顾，欢迎再次下单',
				6: '该订单已被取消'
			}
			return map[this.order.status] || ''
		}
	},
	onLoad(options) {
		if (!options || !options.id) {
			uni.showToast({ title: '订单不存在', icon: 'none' })
			setTimeout(() => uni.navigateBack(), 1500)
			return
		}
		this.id = options.id
		this.fetchDetail()
	},
	methods: {
		async fetchDetail() {
			this.loading = true
			try {
				const res = await queryOrderDetailById(this.id)
				if (res.code === 1) {
					this.order = res.data || {}
				} else {
					uni.showToast({ title: '订单加载失败', icon: 'none' })
				}
			} catch (e) {
				uni.showToast({ title: '网络异常，请重试', icon: 'none' })
			}
			this.loading = false
		},
		// 格式化时间
		formatTime(t) {
			if (!t) return '--'
			const d = new Date(t)
			const pad = n => String(n).padStart(2, '0')
			return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
		},
		// 格式化金额（分→元）
		formatAmount(v) {
			if (v == null) return '0.00'
			return Number(v).toFixed(2)
		},
		// 复制订单号
		copyNumber() {
			if (!this.order.number) return
			uni.setClipboardData({
				data: this.order.number,
				success: () => uni.showToast({ title: '已复制', icon: 'success', duration: 1200 })
			})
		},
		// 再来一单
		async oneMoreOrder() {
			const pages = getCurrentPages()
			const hasIndex = pages.findIndex(item => item.route === 'pages/index/index')
			await delShoppingCart()
			try {
				const res = await oneOrderAgain({ id: this.order.id })
				if (res.code === 1) {
					uni.switchTab({ url: '/pages/index/index' })
				}
			} catch (e) {
				uni.showToast({ title: '操作失败', icon: 'none' })
			}
		},
		// 取消订单
		cancelOrderHandle() {
			uni.showModal({
				title: '取消订单',
				content: '确定要取消该订单吗？',
				confirmColor: '#ef4444',
				success: async (r) => {
					if (!r.confirm) return
					try {
						const res = await cancelOrder(this.order.id)
						if (res.code === 1) {
							uni.showToast({ title: '已取消', icon: 'success' })
							this.fetchDetail()
						}
					} catch (e) {
						uni.showToast({ title: '取消失败', icon: 'none' })
					}
				}
			})
		},
		// 催单
		async remindOrder() {
			try {
				const res = await remindOrder(this.order.id)
				if (res.code === 1) {
					uni.showToast({ title: '已催单，请耐心等待', icon: 'success', duration: 2000 })
				} else {
					uni.showToast({ title: '催单失败，请重试', icon: 'none' })
				}
			} catch (e) {
				uni.showToast({ title: '网络异常，请重试', icon: 'none' })
			}
		},
		// 去支付
		goPay() {
			uni.redirectTo({ url: '/pages/order/index?id=' + this.order.id })
		}
	}
}
</script>

<style lang="scss" scoped>
$b: #1a56db;

.order-detail-page {
	min-height: 100vh;
	background: $page-bg;
}

/* ===== 骨架加载 ===== */
.detail-loading {
	padding: 24rpx;
}

.loading-card {
	background: #fff;
	border-radius: $radius-md;
	padding: 32rpx 28rpx;
	margin-bottom: 16rpx;
	box-shadow: $shadow-card;
}

.skeleton-line {
	height: 24rpx;
	border-radius: 8rpx;
}

.skeleton-animate {
	background: linear-gradient(90deg, #f0f0f0 25%, #e8e8e8 50%, #f0f0f0 75%);
	background-size: 200% 100%;
	animation: shimmer 1.5s ease-in-out infinite;
}

@keyframes shimmer {
	0% { background-position: 200% 0; }
	100% { background-position: -200% 0; }
}

/* ===== 主体内容 ===== */
.detail-content {
	padding: 24rpx;
	padding-bottom: 160rpx;
}

/* ===== 订单状态卡片 ===== */
.status-card {
	background: linear-gradient(135deg, $brand-primary-bg, #dbeafe);
	border-radius: $card-radius;
	padding: 36rpx 28rpx;
	margin-bottom: 20rpx;
	box-shadow: $shadow-card;
	display: flex;
	align-items: center;
	gap: 24rpx;
}

.status-icon-wrap {
	width: 88rpx;
	height: 88rpx;
	border-radius: 50%;
	background: #fff;
	display: flex;
	align-items: center;
	justify-content: center;
	box-shadow: 0 4rpx 16rpx rgba($b, 0.1);
}

.status-icon {
	font-size: 44rpx;
}

.status-info {
	flex: 1;
}

.status-text {
	font-size: 36rpx;
	font-weight: 700;
	color: $brand-primary-dark;
	display: block;
	margin-bottom: 6rpx;
}

.status-hint {
	font-size: 24rpx;
	color: rgba(30, 58, 138, 0.6);
	line-height: 1.4;
}

/* ===== 通用区块卡片 ===== */
.section-card {
	background: #fff;
	border-radius: $card-radius;
	padding: 28rpx;
	margin-bottom: 16rpx;
	box-shadow: $shadow-card;
}

.section-head {
	display: flex;
	align-items: center;
	gap: 12rpx;
	margin-bottom: 20rpx;
}

.section-icon {
	font-size: 28rpx;
}

.section-title {
	font-size: $font-md;
	font-weight: 700;
	color: $text-primary;
}

/* 地址 */
.address-body {
	margin-left: 4rpx;
}

.address-contact {
	display: flex;
	align-items: center;
	gap: 24rpx;
	margin-bottom: 10rpx;
}

.contact-name {
	font-size: $font-md;
	color: $text-primary;
	font-weight: 600;
}

.contact-phone {
	font-size: $font-base;
	color: $text-secondary;
}

.address-text {
	font-size: $font-sm;
	color: $text-tertiary;
	line-height: 1.5;
}

/* 预计送达 */
.eta-text {
	font-size: $font-md;
	color: $brand-primary;
	font-weight: 600;
	margin-left: 4rpx;
}

/* 商品列表 */
.goods-list {
	margin-left: 4rpx;
}

.goods-item {
	display: flex;
	align-items: center;
	gap: 16rpx;
	padding: 16rpx 0;
	border-bottom: 1rpx solid $border-light;

	&:last-child {
		border-bottom: none;
	}
}

.goods-img {
	width: 100rpx;
	height: 100rpx;
	border-radius: $radius-sm;
	background: #f5f5f5;
	flex-shrink: 0;
}

.goods-info {
	flex: 1;
	min-width: 0;
}

.goods-name {
	font-size: $font-base;
	color: $text-primary;
	font-weight: 500;
	display: block;
	line-height: 1.4;
}

.goods-flavor {
	font-size: 22rpx;
	color: $text-tertiary;
	margin-top: 4rpx;
	display: block;
}

.goods-right {
	display: flex;
	flex-direction: column;
	align-items: flex-end;
	gap: 6rpx;
	flex-shrink: 0;
}

.goods-price {
	font-size: $font-base;
	color: $text-primary;
	font-weight: 500;
}

.goods-qty {
	font-size: $font-sm;
	color: $text-tertiary;
}

/* 订单信息行 */
.info-rows {
	margin-left: 4rpx;
}

.info-row {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 14rpx 0;
	border-bottom: 1rpx solid $border-light;

	&:last-child {
		border-bottom: none;
	}

	&--total {
		border-bottom: none;
		padding-top: 20rpx;
		margin-top: 4rpx;
		border-top: 2rpx dashed $border-light;
	}
}

.info-label {
	font-size: $font-sm;
	color: $text-secondary;
	flex-shrink: 0;
}

.info-label-total {
	font-size: $font-md;
	color: $text-primary;
	font-weight: 700;
}

.info-value {
	font-size: $font-sm;
	color: $text-primary;
	text-align: right;
}

.info-value-total {
	font-size: $font-xl;
	color: $brand-accent;
	font-weight: 700;
}

.info-mono {
	font-family: monospace;
	letter-spacing: 1rpx;
}

.info-value-wrap {
	display: flex;
	align-items: center;
	gap: 12rpx;
}

.info-copy {
	font-size: 22rpx;
	color: $b;
	padding: 4rpx 16rpx;
	border: 1rpx solid $b;
	border-radius: 20rpx;

	&:active {
		background: $brand-primary-bg;
	}
}

.remark-value {
	color: $text-secondary;
	font-size: 24rpx;
	max-width: 380rpx;
	word-break: break-all;
}

.cancel-reason {
	color: $brand-danger;
}

.price-value {
	color: $brand-accent;
	font-weight: 600;
}

/* ===== 底部操作栏 ===== */
.detail-footer {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	background: #fff;
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 20rpx 28rpx;
	padding-bottom: max(20rpx, env(safe-area-inset-bottom));
	box-shadow: 0 -2rpx 16rpx rgba(0, 0, 0, 0.06);
	z-index: 99;
	box-sizing: border-box;
}

.footer-left {
	flex: 1;
}

.footer-total {
	font-size: $font-sm;
	color: $text-secondary;

	.total-price {
		font-size: $font-xl;
		font-weight: 700;
		color: $brand-accent;
	}
}

.footer-actions {
	display: flex;
	gap: 16rpx;
	flex-shrink: 0;
}

.btn-again, .btn-remind, .btn-cancel {
	padding: 16rpx 32rpx;
	border-radius: $radius-round;
	border: 1rpx solid $b;

	text {
		font-size: $font-base;
		font-weight: 500;
		color: $b;
	}

	&:active {
		background: $brand-primary-bg;
	}
}

.btn-pay {
	padding: 16rpx 48rpx;
	border-radius: $radius-round;
	background: linear-gradient(135deg, $b, #3b82f6);
	box-shadow: 0 4rpx 16rpx rgba($b, 0.3);

	text {
		font-size: $font-base;
		font-weight: 600;
		color: #fff;
	}

	&:active {
		transform: scale(0.96);
		opacity: 0.92;
	}
}

.btn-remind {
	border-color: $brand-accent;
	text { color: $brand-accent; }
	&:active { background: $brand-accent-light; }
}

.btn-cancel {
	border-color: $brand-danger;
	text { color: $brand-danger; }
	&:active { background: $brand-danger-bg; }
}

.detail-spacer {
	height: 40rpx;
}
</style>
