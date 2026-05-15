<template>
<view class="order_content">
	<scroll-view class="order_content_box" scroll-y="true" scroll-top="0rpx">
		<!-- <view class="navBar">
			<view class="leftNav" @click="goback()">
				<image class="back" src="../../static/btn_back.png" mode=""></image>
			</view>
			<view class="centerNav">
				提交订单
			</view>
		</view> -->
		<!-- <view class="restaurant_info_box">
			<view class="restaurant_info">
				<view class="left_info">
					<view class="title">
						{{tableInfo.storeName}}
					</view>
					<view class="position">
						{{tableInfo.tableName}}
					</view>
				</view>
				<view class="restaurant_logo">
					<image class="restaurant_logo_img" :src="tableInfo.brandLogo"></image>
				</view>
			</view>
		</view> -->
		<!-- 新写逻辑 -->
		<view class="new_address">
			<!-- 上部 -->
			<view class="top" @click="goAddress">
        <view v-if="!address" class="address_name_disabled">
          	请选择收货地址
        </view>
				<view v-if="address" class="address_name">
					<view class="address">
						<text class="word">{{ address }}</text>
					</view>
					<view class="name">
						<text class="name_1">{{ nickName }}</text>
						<text class="name_2">{{ phoneNumber }}</text>
					</view>
				</view>
				<view class="address_image">
					<image class="to_right" src="../../static/toRight.png" mode=""></image>
				</view>
			</view>
			<!-- 下部 -->
			<view class="bottom">
				<text class="word_bottom">预计{{ arrivalTime }}送达</text>
			</view>
		</view>
		<view class="order_list_cont">
			<view class="order_list">
			  <view class="word_text">
					<text class="word_style">订单明细</text>
				</view>
				<view class="order-type">
					<view class="type_item" v-for="(obj, index) in orderListDataes" :key="index">
						<view class="dish_img">
							<image mode="aspectFill" :src="getNewImage(obj.image)" class="dish_img_url"></image>
						</view>
						<view class="dish_info">
							<view class="dish_name"> {{ obj.name }} </view>
							<view class="dish_price">× <text v-if="obj.number && obj.number > 0" class="dish_number">{{obj.number}}</text> </view>
							<view class="dish_active">
								<text class="ico">￥</text> {{ obj.amount / 100 }} 
							</view>
						</view>
					</view>
				</view>
				<!-- <view class="seize_seat"></view> -->
			</view >
			<view class="order_list">
				<!-- 添加的备注 -->
				<view class="uni-textarea">
					<view class="word_text">
						<text class="word_style">备注</text>
					</view>
					<!-- <textarea class="beizhu_text" placeholder-style="font-size: 24rpx;font-family: PingFangSC, PingFangSC-Regular;font-weight: 400;color: #bdbdbd;" placeholder-class="textarea-placeholder" v-model="remark" maxlength="50" placeholder="请输入您需要备注的信息"/> -->
					<textarea class="beizhu_text" :class="{'beizhu_text_ios':platform==='ios'}" placeholder-class="textarea-placeholder" v-model="remark" maxlength="50" placeholder="请输入您需要备注的信息"/>
				</view>
			</view>
		</view>
	</scroll-view>
	<view class="footer_order_buttom order_form">
		<view class="order_number">
			<image src="../../static/btn_waiter_sel.png" class="order_number_icon" mode=""></image>
			<view class="order_dish_num"> {{orderDishNumber}} </view>
		</view>
		<view class="order_price">
				<text class="ico">￥ </text> {{orderDishPrice / 100+6}}
		</view>
		<view class="order_but">
			<!-- <view class="order_but_left" @click="goback()">
				继续加菜
			</view> -->
			<view class="order_but_rit" @click="payOrderHandle()">
				去支付
			</view>
		</view>
	</view>
	<view class="mask-box"></view>
	<!-- 已有人支付弹窗 - start -->
	<view class="pop_mask " v-show="openPayType">
		<view class="pop">
			<view class="open_table_cont">
				<view class="cont_icon">
					<image class="cont_icon_img" src="../../static/unable_pay.png" mode=""></image>
				</view>
				<view class="cont_tit">
					该餐桌在支付途中
				</view>
				<view class="cont_desc">
					请勿重复支付
				</view>
			</view>
			<view class="butList">
				<view class="define" @click="closeMask()"> 知道了 </view>
			</view>
		</view>
	</view>
	<!-- 已有人支付弹窗 - end -->
</view>
</template>
<script src="./index.js"></script>
<style src="./../common/Navbar/navbar.scss" lang="scss" scoped></style>
<style src="./style.scss" lang="scss" scoped></style>
