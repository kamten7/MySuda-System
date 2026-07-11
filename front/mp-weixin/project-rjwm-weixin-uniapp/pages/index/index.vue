<template>
<view class="home-page">
	<!-- 加载骨架屏 -->
	<view class="home-loading" v-if="loading">
		<view class="home-skeleton">
			<view class="skeleton-sidebar">
				<view class="skeleton-cat-item" v-for="i in 8" :key="i">
					<view class="skeleton-line skeleton-animate" style="width:80%;height:28rpx;"></view>
				</view>
			</view>
			<view class="skeleton-dishes">
				<view class="skeleton-dish-item" v-for="i in 5" :key="i">
					<view class="skeleton-dish-img skeleton-animate"></view>
					<view class="skeleton-dish-info">
						<view class="skeleton-line skeleton-animate" style="width:70%;height:32rpx;"></view>
						<view class="skeleton-line skeleton-animate" style="width:90%;height:24rpx;margin-top:12rpx;"></view>
						<view class="skeleton-line skeleton-animate" style="width:40%;height:24rpx;margin-top:12rpx;"></view>
					</view>
				</view>
			</view>
		</view>
	</view>

	<!-- 主内容 -->
	<view class="home-content" v-else>
		<!-- 店铺信息卡片 -->
		<view class="shop-card stagger-item">
			<view class="shop-card-top">
				<image class="shop-logo" src="/static/logo_brand.svg" mode="aspectFit" />
				<view class="shop-info">
					<text class="shop-name">速达外卖</text>
					<view class="shop-meta">
						<text class="meta-item">⭐ 4.8</text>
						<text class="meta-sep">|</text>
						<text class="meta-item">🕐 约30分钟</text>
						<text class="meta-sep">|</text>
						<text class="meta-item">🚀 配送费¥6</text>
					</view>
				</view>
			</view>
			<view class="shop-card-desc">
				<text>速达外卖 — 专业外卖平台，品质美食，极速送达 🛵</text>
			</view>
		</view>

		<!-- 菜品区域：侧边分类 + 菜品列表 -->
		<view class="menu-area">
			<!-- 左侧分类栏 -->
			<scroll-view class="menu-categories" scroll-y="true">
				<view
					v-for="(item, index) in typeListData"
					:key="index"
					class="menu-cat-item"
					:class="{ 'menu-cat-item--active': typeIndex === index }"
					@click="getDishListDataes(item, index)"
				>
					<view class="cat-indicator" v-if="typeIndex === index"></view>
					<text class="cat-name">{{ item.name }}</text>
				</view>
				<view class="menu-cat-spacer"></view>
			</scroll-view>

			<!-- 右侧菜品列表 -->
			<scroll-view class="menu-dishes" scroll-y="true" v-if="dishListItems && dishListItems.length > 0">
				<view class="dish-section">
					<DishCard
						v-for="(item, index) in dishListItems"
						:key="index"
						:image="getNewImage(item.image)"
						:name="item.name"
						:desc="item.description || ''"
						:price="item.price"
						:count="item.dishNumber || 0"
						:sales="0"
						:isHot="false"
						:hasFlavors="item.flavors && item.flavors.length > 0"
						:isLast="index === dishListItems.length - 1"
						@add="addDishAction(item, '普通')"
						@minus="redDishAction(item, '普通')"
						@select-flavor="moreNormDataesHandle(item)"
						@click="openDetailHandle(item)"
					/>
				</view>
				<view class="menu-dish-spacer"></view>
			</scroll-view>

			<!-- 空菜品 -->
			<view class="menu-dishes-empty" v-else>
				<AppEmpty icon-type="search" title="暂无菜品" desc="该分类下还没有菜品哦~" />
			</view>
		</view>
	</view>

	<!-- 底部结算栏 -->
	<view class="footer-bar" :class="{ 'footer-bar--has-items': orderDishNumber > 0 }">
		<view class="footer-cart-icon" @click="openOrderCartList = !openOrderCartList">
			<view class="cart-icon-wrap" :class="{ 'cart-icon-wrap--filled': orderDishNumber > 0 }">
				<text class="cart-emoji">🛒</text>
			</view>
			<view class="cart-badge" v-if="orderDishNumber > 0">{{ orderDishNumber > 99 ? '99+' : orderDishNumber }}</view>
		</view>
		<view class="footer-price" :class="{ 'footer-price--has': orderDishNumber > 0 }">
			<text class="price-symbol">¥</text>
			<text class="price-value">{{ orderDishNumber > 0 ? ((orderDishPrice + 6).toFixed(2)) : '0.00' }}</text>
		</view>
		<view class="footer-btn" :class="{ 'footer-btn--active': orderDishNumber > 0 }" @click="goOrder()">
			去结算
		</view>
	</view>

	<!-- 口味选择弹窗 -->
	<view class="pop_mask popup-mask" v-show="openMoreNormPop" @click="closeMoreNorm(moreNormDishdata)">
		<view class="flavor-popup popup-slide-up" @click.stop>
			<view class="flavor-header">
				<text class="flavor-title">{{ moreNormDishdata.name }}</text>
			</view>
			<scroll-view class="flavor-body" scroll-y>
				<view class="flavor-group" v-for="(obj, index) in moreNormdata" :key="index">
					<view class="flavor-group-name">{{ obj.name }}</view>
					<view class="flavor-group-items">
						<view
							v-for="(item, ind) in obj.value"
							:key="ind"
							class="flavor-tag"
							:class="{ 'flavor-tag--active': flavorDataes.indexOf(item) !== -1 }"
							@click="checkMoreNormPop(obj.value, item)"
						>{{ item }}</view>
					</view>
				</view>
			</scroll-view>
			<view class="flavor-footer">
				<view class="flavor-footer-price">
					<text class="price-ico">¥ </text>{{ moreNormDishdata.price }}
				</view>
				<view class="flavor-footer-actions" v-if="moreNormDishdata.dishNumber && moreNormDishdata.dishNumber > 0">
					<image src="/static/btn_red.png" @click="redDishAction(moreNormDishdata, '普通')" class="action-icon" />
					<text class="action-num">{{ moreNormDishdata.dishNumber }}</text>
					<image src="/static/btn_add.png" @click="addDishAction(moreNormDishdata, '普通')" class="action-icon" />
				</view>
				<view class="flavor-footer-actions" v-else>
					<view class="flavor-add-btn" @click="addDishAction(moreNormDishdata, '普通')">加入购物车</view>
				</view>
			</view>
			<view class="flavor-close" @click="closeMoreNorm(moreNormDishdata)">
				<text class="close-icon">✕</text>
			</view>
		</view>
	</view>

	<!-- 菜品详情弹窗 -->
	<view class="pop_mask popup-mask" v-show="openDetailPop" @click="openDetailPop = false">
		<view class="detail-popup popup-slide-up" @click.stop v-if="dishDetailes.type == 1">
			<image mode="aspectFill" class="detail-image" :src="getNewImage(dishDetailes.image)" />
			<view class="detail-name">{{ dishDetailes.name }}</view>
			<view class="detail-desc">{{ dishDetailes.description }}</view>
			<view class="detail-footer">
				<view class="detail-price"><text class="price-ico">¥ </text>{{ dishDetailes.price }}</view>
				<view class="detail-actions" v-if="dishDetailes.dishNumber && dishDetailes.dishNumber > 0">
					<image src="/static/btn_red.png" @click="redDishAction(dishDetailes, '普通')" class="action-icon" />
					<text class="action-num">{{ dishDetailes.dishNumber }}</text>
					<image src="/static/btn_add.png" @click="addDishAction(dishDetailes, '普通')" class="action-icon" />
				</view>
				<view class="detail-actions" v-else>
					<view class="detail-add-btn" @click="addDishAction(dishDetailes, '普通')">加入购物车</view>
				</view>
			</view>
			<view class="detail-close" @click="openDetailPop = false"><text class="close-icon">✕</text></view>
		</view>
		<!-- 套餐详情 -->
		<view class="detail-popup popup-slide-up" @click.stop v-else>
			<scroll-view class="setmeal-items" scroll-y>
				<view class="setmeal-item" v-for="(item, index) in dishMealData" :key="index">
					<image class="setmeal-item-img" :src="getNewImage(item.image)" mode="aspectFill" />
					<view class="setmeal-item-info">
						<text class="setmeal-item-name">{{ item.name }}</text>
						<text class="setmeal-item-copies">x{{ item.copies }}</text>
					</view>
				</view>
			</scroll-view>
			<view class="detail-footer">
				<view class="detail-price"><text class="price-ico">¥ </text>{{ dishDetailes.price }}</view>
				<view class="detail-actions" v-if="dishDetailes.dishNumber && dishDetailes.dishNumber > 0">
					<image src="/static/btn_red.png" @click="redDishAction(dishDetailes, '普通')" class="action-icon" />
					<text class="action-num">{{ dishDetailes.dishNumber }}</text>
					<image src="/static/btn_add.png" @click="addDishAction(dishDetailes, '普通')" class="action-icon" />
				</view>
				<view class="detail-actions" v-else>
					<view class="detail-add-btn" @click="addDishAction(dishDetailes, '普通')">加入购物车</view>
				</view>
			</view>
			<view class="detail-close" @click="openDetailPop = false"><text class="close-icon">✕</text></view>
		</view>
	</view>

	<!-- 购物车弹窗 -->
	<view class="pop_mask popup-mask" v-show="openOrderCartList" @click="openOrderCartList = false">
		<view class="cart-popup popup-slide-up" @click.stop>
			<view class="cart-header">
				<text class="cart-header-title">购物车</text>
				<view class="cart-header-clear" @click="clearCardOrder()">
					<text class="clear-text">🗑 清空</text>
				</view>
			</view>
			<scroll-view class="cart-list" scroll-y>
				<view class="cart-item" v-for="(item, ind) in orderListDataes" :key="ind">
					<image mode="aspectFill" :src="getNewImage(item.image)" class="cart-item-img" />
					<view class="cart-item-info">
						<text class="cart-item-name">{{ item.name }}</text>
						<text class="cart-item-price">¥{{ item.amount }}</text>
					</view>
					<view class="cart-item-actions">
						<image v-if="item.number > 0" src="/static/btn_red.png" @click="redDishAction(item, '购物车')" class="action-icon" />
						<text v-if="item.number > 0" class="action-num">{{ item.number }}</text>
						<image src="/static/btn_add.png" @click="addDishAction(item, '购物车')" class="action-icon" />
					</view>
				</view>
				<view class="cart-spacer"></view>
			</scroll-view>
		</view>
	</view>

	<!-- 全局 Loading -->
	<AppLoading :visible="loaddingSt" text="加载中..." />
</view>
</template>

<script src="./index.js"></script>
<style src="./style.scss" lang="scss" scoped></style>
