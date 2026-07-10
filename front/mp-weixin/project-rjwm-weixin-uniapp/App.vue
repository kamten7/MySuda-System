<script>
	export default {
		onLaunch: function () {
			console.log('App Launch')
		},
		onShow: function () {
			console.log('App Show')
		},
		onHide: function () {
			console.log('App Hide')
		}
	}
</script>

<style>
	/* ===== 全局基础样式 ===== */
	page {
		background-color: #f3f4f7;
		height: 100%;
		font-size: 28rpx;
		line-height: 1.6;
		font-family: -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Microsoft YaHei', sans-serif;
		color: #333333;
	}

	uni-page-body {
		background-color: #f3f4f7 !important;
		min-height: 100% !important;
		height: auto !important;
	}

	/* ===== 全局过渡动画 ===== */

	/* 页面进入动画 */
	.page-fade-in {
		animation: pageFadeIn 0.3s cubic-bezier(0.4, 0, 0.2, 1) both;
	}

	@keyframes pageFadeIn {
		from {
			opacity: 0;
			transform: translateY(20rpx);
		}
		to {
			opacity: 1;
			transform: translateY(0);
		}
	}

	/* 弹窗动画 */
	.popup-slide-up {
		animation: popupSlideUp 0.3s cubic-bezier(0.4, 0, 0.2, 1) both;
	}

	@keyframes popupSlideUp {
		from {
			opacity: 0;
			transform: translateY(100%);
		}
		to {
			opacity: 1;
			transform: translateY(0);
		}
	}

	/* 遮罩淡入 */
	.mask-fade-in {
		animation: maskFadeIn 0.25s ease both;
	}

	@keyframes maskFadeIn {
		from { opacity: 0; }
		to { opacity: 1; }
	}

	/* 列表错落入场 */
	@keyframes staggerItemIn {
		from {
			opacity: 0;
			transform: translateY(30rpx);
		}
		to {
			opacity: 1;
			transform: translateY(0);
		}
	}

	.stagger-item {
		animation: staggerItemIn 0.35s cubic-bezier(0.22, 0.61, 0.36, 1) both;
	}

	/* 骨架屏闪烁 */
	@keyframes skeletonShimmer {
		0% { background-position: 200% 0; }
		100% { background-position: -200% 0; }
	}

	/* 脉冲动画 */
	@keyframes pulse {
		0%, 100% { opacity: 1; }
		50% { opacity: 0.5; }
	}

	/* ===== 通用工具类 ===== */

	/* 通用遮罩 */
	.pop-mask {
		position: fixed;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		background: rgba(0, 0, 0, 0.45);
		z-index: 9998;
	}

	/* 全局 loading 动画 */
	.loading-gif {
		width: 80rpx;
		height: 80rpx;
	}

	/* placeholder 全局样式 */
	.placeholder-style {
		color: #bdbdbd;
		font-size: 28rpx;
	}

	/* 安全区域底部 */
	.safe-bottom {
		padding-bottom: constant(safe-area-inset-bottom);
		padding-bottom: env(safe-area-inset-bottom);
	}

	/* ===== 全局按钮按下反馈 ===== */
	.touch-scale:active {
		transform: scale(0.95);
		transition: transform 0.15s ease;
	}

	.touch-opacity:active {
		opacity: 0.7;
		transition: opacity 0.15s ease;
	}

	/* ===== 兼容旧代码 — 保留 .pop_mask 类名 ===== */
	.pop_mask {
		position: fixed;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		background: rgba(0, 0, 0, 0.45);
		z-index: 9998;
	}

	/* ===== PC 兼容 ===== */
	/* #ifdef H5 */
	@media screen and (min-width: 768px) {
		body {
			overflow-y: scroll;
		}
	}
	/* #endif */
</style>
