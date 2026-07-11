<template>
<view class="order-page">
	<scroll-view class="order-scroll" scroll-y>
		<!-- 收货地址 -->
		<view class="order-address-card" @click="goAddress">
			<view class="address-pin">
				<text class="pin-icon">📍</text>
			</view>
			<view class="address-info" v-if="address">
				<view class="address-detail">
					<text class="address-text">{{ address }}</text>
				</view>
				<view class="address-contact">
					<text class="contact-name">{{ nickName }}</text>
					<text class="contact-phone">{{ phoneNumber }}</text>
				</view>
			</view>
			<view class="address-info address-info--empty" v-else>
				<text class="address-empty-text">请选择收货地址</text>
			</view>
			<text class="address-arrow">›</text>
		</view>

		<!-- 预计送达 -->
		<view class="order-eta">
			<text class="eta-icon">🕐</text>
			<text class="eta-text">预计 {{ arrivalTime }} 送达</text>
		</view>

		<!-- 订单明细 -->
		<view class="order-section-card">
			<view class="section-header">
				<text class="section-title">订单明细</text>
			</view>
			<view class="order-item" v-for="(obj, index) in orderListDataes" :key="index">
				<image mode="aspectFill" :src="getNewImage(obj.image)" class="order-item-img" />
				<view class="order-item-info">
					<text class="order-item-name">{{ obj.name }}</text>
					<text class="order-item-qty">x{{ obj.number }}</text>
				</view>
				<text class="order-item-price">¥{{ obj.amount }}</text>
			</view>
		</view>

		<!-- 备注 -->
		<view class="order-section-card">
			<view class="section-header">
				<text class="section-title">备注</text>
			</view>
			<view class="remark-area">
				<textarea
					class="remark-input"
					v-model="remark"
					maxlength="50"
					placeholder="请输入您需要备注的信息"
					placeholder-class="remark-placeholder"
				/>
			</view>
		</view>

		<view class="order-bottom-spacer"></view>
	</scroll-view>

	<!-- 底部结算栏 -->
	<view class="order-footer">
		<view class="order-footer-info">
			<view class="footer-count">
				<text class="count-icon">🛒</text>
				<text class="count-badge">{{ orderDishNumber }}</text>
			</view>
			<text class="footer-total">
				<text class="total-label">合计 </text>
				<text class="total-symbol">¥</text>
				<text class="total-value">{{ (orderDishPrice + 6).toFixed(2) }}</text>
			</text>
		</view>
		<view class="order-footer-btn" @click="payOrderHandle">
			去支付
		</view>
	</view>
</view>
</template>

<script src="./index.js"></script>
<style src="./style.scss" lang="scss" scoped></style>
