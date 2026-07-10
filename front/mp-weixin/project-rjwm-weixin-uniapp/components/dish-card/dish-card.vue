<template>
	<view class="dish-card" :class="{ 'dish-card--last': isLast }" @click="handleClick">
		<view class="dish-card__image">
			<image
				:src="image"
				class="dish-card__img"
				mode="aspectFill"
				@error="onImageError"
			/>
			<view class="dish-card__fav" v-if="isHot">
				<text class="fav-text">🔥 热销</text>
			</view>
		</view>
		<view class="dish-card__info">
			<view class="dish-card__name">{{ name }}</view>
			<view class="dish-card__desc" v-if="desc">{{ desc }}</view>
			<view class="dish-card__sales" v-if="sales > 0">月销量 {{ sales }}</view>
			<view class="dish-card__bottom">
				<view class="dish-card__price">
					<text class="price-symbol">¥</text>
					<text class="price-value">{{ priceText }}</text>
				</view>
				<view class="dish-card__actions">
					<image
						v-if="count > 0"
						class="action-btn action-btn--minus"
						src="/static/btn_red.png"
						@click.stop="$emit('minus', $event)"
					/>
					<text v-if="count > 0" class="action-count">{{ count }}</text>
					<image
						class="action-btn action-btn--add"
						src="/static/btn_add.png"
						@click.stop="$emit('add', $event)"
					/>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
export default {
	name: 'DishCard',
	props: {
		image: { type: String, default: '' },
		name: { type: String, default: '' },
		desc: { type: String, default: '' },
		price: { type: Number, default: 0 },       // 分为单位
		count: { type: Number, default: 0 },        // 已在购物车中的数量
		sales: { type: Number, default: 0 },
		isHot: { type: Boolean, default: false },
		hasFlavors: { type: Boolean, default: false },
		isLast: { type: Boolean, default: false }
	},
	computed: {
		priceText() {
			return (this.price / 100).toFixed(2)
		}
	},
	methods: {
		handleClick() {
			if (this.hasFlavors) {
				this.$emit('select-flavor')
			} else {
				this.$emit('click')
			}
		},
		onImageError() {
			// 图片加载失败使用默认占位图
		}
	}
}
</script>

<style lang="scss" scoped>
.dish-card {
	display: flex;
	padding: 20rpx 0;
	border-bottom: 1rpx solid $border-light;
	transition: background 0.15s ease;

	&:active {
		background: #fafafa;
	}

	&--last {
		border-bottom: none;
	}
}

/* 图片区 */
.dish-card__image {
	width: 180rpx;
	height: 180rpx;
	flex-shrink: 0;
	position: relative;
}

.dish-card__img {
	width: 100%;
	height: 100%;
	border-radius: $radius-md;
	background: #f5f5f5;
}

.dish-card__fav {
	position: absolute;
	top: 0;
	left: 0;
	padding: 4rpx 12rpx;
	background: linear-gradient(135deg, #ff6b6b, #e74c3c);
	border-radius: $radius-md 0 $radius-md 0;

	.fav-text {
		font-size: 18rpx;
		color: #fff;
		font-weight: 500;
	}
}

/* 信息区 */
.dish-card__info {
	flex: 1;
	padding-left: 20rpx;
	display: flex;
	flex-direction: column;
	justify-content: space-between;
	min-height: 180rpx;
}

.dish-card__name {
	font-size: $font-md;
	color: $text-primary;
	font-weight: 600;
	line-height: 1.3;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.dish-card__desc {
	font-size: $font-sm;
	color: $text-tertiary;
	line-height: 1.4;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.dish-card__sales {
	font-size: 20rpx;
	color: $text-tertiary;
}

.dish-card__bottom {
	display: flex;
	align-items: center;
	justify-content: space-between;
}

.dish-card__price {
	.price-symbol {
		font-size: $font-sm;
		color: $brand-accent;
		font-weight: 600;
	}
	.price-value {
		font-size: $font-lg;
		color: $brand-accent;
		font-weight: 700;
	}
}

.dish-card__actions {
	display: flex;
	align-items: center;
	gap: 8rpx;

	.action-btn {
		width: 44rpx;
		height: 44rpx;

		&:active {
			opacity: 0.7;
			transform: scale(0.9);
		}
	}

	.action-count {
		font-size: $font-base;
		color: $text-primary;
		font-weight: 500;
		min-width: 40rpx;
		text-align: center;
	}
}
</style>
