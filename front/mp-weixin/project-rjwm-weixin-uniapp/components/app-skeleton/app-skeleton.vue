<template>
	<view class="app-skeleton" :style="{ padding: padding }">
		<!-- 菜品卡片骨架屏 -->
		<template v-if="type === 'dish-list'">
			<view class="dish-skeleton-item" v-for="i in count" :key="i">
				<view class="dish-skeleton-img skeleton-animate"></view>
				<view class="dish-skeleton-info">
					<view class="skeleton-line skeleton-animate" style="width: 70%; height: 32rpx;"></view>
					<view class="skeleton-line skeleton-animate" style="width: 90%; height: 24rpx; margin-top: 12rpx;"></view>
					<view class="skeleton-line skeleton-animate" style="width: 40%; height: 24rpx; margin-top: 12rpx;"></view>
				</view>
			</view>
		</template>

		<!-- 分类侧边栏骨架屏 -->
		<template v-if="type === 'category-bar'">
			<view class="category-skeleton-item" v-for="i in count" :key="i">
				<view class="skeleton-line skeleton-animate" style="width: 80%; height: 28rpx;"></view>
			</view>
		</template>

		<!-- 通用卡片骨架屏 -->
		<template v-if="type === 'card'">
			<view class="card-skeleton" v-for="i in count" :key="i">
				<view class="skeleton-line skeleton-animate" style="width: 60%; height: 36rpx;"></view>
				<view class="skeleton-line skeleton-animate" style="width: 90%; height: 24rpx; margin-top: 16rpx;"></view>
				<view class="skeleton-line skeleton-animate" style="width: 40%; height: 24rpx; margin-top: 12rpx;"></view>
			</view>
		</template>

		<!-- 文本骨架屏 -->
		<template v-if="type === 'text'">
			<view class="skeleton-line skeleton-animate" v-for="i in count" :key="i"
				style="height: 28rpx; margin-bottom: 16rpx;"
				:style="{ width: widths[(i - 1) % widths.length] }">
			</view>
		</template>
	</view>
</template>

<script>
export default {
	name: 'AppSkeleton',
	props: {
		type: {
			type: String,
			default: 'dish-list'  // dish-list | category-bar | card | text
		},
		count: {
			type: Number,
			default: 6
		},
		padding: {
			type: String,
			default: '20rpx'
		}
	},
	data() {
		return {
			widths: ['85%', '60%', '75%', '50%', '90%']
		}
	}
}
</script>

<style lang="scss" scoped>
.app-skeleton {
	width: 100%;
}

.skeleton-animate {
	background: linear-gradient(90deg, #f0f0f0 25%, #e8e8e8 50%, #f0f0f0 75%);
	background-size: 200% 100%;
	animation: skeleton-shimmer 1.5s ease-in-out infinite;
	border-radius: 8rpx;
}

@keyframes skeleton-shimmer {
	0% { background-position: 200% 0; }
	100% { background-position: -200% 0; }
}

.skeleton-line {
	height: 24rpx;
	border-radius: 8rpx;
}

/* 菜品骨架 */
.dish-skeleton-item {
	display: flex;
	padding: 20rpx 0;
	border-bottom: 1rpx solid $border-light;
}

.dish-skeleton-img {
	width: 180rpx;
	height: 180rpx;
	border-radius: $radius-md;
	flex-shrink: 0;
}

.dish-skeleton-info {
	flex: 1;
	padding-left: 20rpx;
	display: flex;
	flex-direction: column;
	justify-content: flex-start;
	padding-top: 8rpx;
}

/* 分类骨架 */
.category-skeleton-item {
	padding: 24rpx 20rpx;
}

/* 卡片骨架 */
.card-skeleton {
	padding: 24rpx;
	background: #fff;
	border-radius: $radius-lg;
	margin-bottom: 16rpx;
}
</style>
