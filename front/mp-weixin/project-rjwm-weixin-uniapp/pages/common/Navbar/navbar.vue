<template>
	<view class="navBar" :style="{ paddingTop: statusBarHeight + 'px' }">
		<view class="navBar-inner">
			<!-- 左侧品牌 -->
			<view class="navBar-brand">
				<image class="brand-logo" src="/static/logo_ruiji.png" mode="aspectFit" />
				<text class="brand-name">速达外卖</text>
			</view>
			<!-- 右侧购物车角标 -->
			<view class="navBar-cart" v-if="cartCount > 0" @click="goCart">
				<text class="cart-icon">🛒</text>
				<view class="cart-badge">{{ cartCount > 99 ? '99+' : cartCount }}</view>
			</view>
		</view>
	</view>
</template>

<script>
export default {
	props: {
		cartCount: {
			type: Number,
			default: 0
		},
		statusBarHeight: {
			type: Number,
			default: 0
		}
	},
	data() {
		return {
			ht: 0
		}
	},
	created() {
		try {
			const res = uni.getMenuButtonBoundingClientRect()
			this.ht = res.top + 5
		} catch (e) {
			this.ht = 44
		}
	},
	methods: {
		goCart() {
			// 购物车在首页底部，通过事件通知首页打开购物车弹窗
			this.$emit('open-cart')
		}
	}
}
</script>

<style src="./navbar.scss" lang="scss"></style>
