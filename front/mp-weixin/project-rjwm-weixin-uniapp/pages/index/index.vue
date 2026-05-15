<template>
	<view class="home_content" :style="{paddingTop:ht +'px'}">
		<navBar></navBar>
		<view class="restaurant_info_box">
			<view class="restaurant_info">
				<!-- 上部 -->
				<view class="info_top">
					<view class="info_top_left">
						<image class="logo_ruiji" src="../../static/logo_ruiji.png"></image>
					</view>
					<view class="info_top_right">
						<view class="right_title">
							<text>速达外卖</text>
						</view>
						<view class="right_details">
							<!-- 左 -->
							<view class="details_flex">
								<image class="top_icon" src="../../static/length.png"></image>
								<text class="icon_text">距离1.5km</text>
							</view>
							<!-- 此乃竖线 -->
							<!-- <text class="vertical-line"></text> -->
							<!-- 中 -->
							<view class="details_flex">
								<image class="top_icon" src="../../static/money.png"></image>
								<text class="icon_text">配送费6元</text>
							</view>
							<!-- 此乃竖线 -->
							<!-- <text class="vertical-line"></text> -->
							<!-- 右 -->
							<view class="details_flex test">
								<image class="top_icon" src="../../static/time.png"></image>
								<text class="icon_text">预计时长12min</text>
							</view>
						</view>
					</view>
				</view>
				<!-- 下部---信息简介 -->
				<view class="info_bottom">
					<text class="word">
						简介: 速达外卖是综合外送平台，定位”大众化的外送平台”旨在为顾客打造专业的外送平台。
					</text>
				</view>
			</view>
		</view>
		
		<view class="restaurant_menu_list">
			<scroll-view class="type_list" scroll-y="true" scroll-top="0rpx" v-if="typeListData.length > 0 ">
				<view class="type_item" :class="{active: typeIndex == index}" v-for="(item, index) in typeListData" :key="index" @click="getDishListDataes(item, index)">
					{{ item.name }} 
				</view>
				<view class="seize_seat"></view>
			</scroll-view>
			<scroll-view class="vegetable_order_list" scroll-y="true" scroll-top="0rpx" v-if="dishListItems && dishListItems.length >0">
				<view class="type_item"  v-for="(item, index) in dishListItems" :key="index" >
					<!-- 点击查看详情 -->
					<view class="dish_img" @click="openDetailHandle(item)">
						<image mode="aspectFill" :src="getNewImage(item.image)" class="dish_img_url"></image>
					</view>
					<view class="dish_info">
						<!-- <view class="dish_name" @click="openDetailHandle(item)"> {{ item.dishName }} </view> -->
						<view class="dish_name" @click="openDetailHandle(item)">
							{{ item.name }}
						</view>
						<view class="dish_label" @click="openDetailHandle(item)"> {{ item.description || item.name }} </view>
						<view class="dish_label" @click="openDetailHandle(item)"> 月销量0</view>
						<!-- <view class="dish_num"> {{ item.dishName }} </view> -->
						<view class="dish_price"> <text class="ico">￥</text> {{ item.price / 100 }} </view>
						<!-- item.flavors && item.flavors.length === 0 || item.dishNumber > 0 -->
						<view class="dish_active" v-if="!item.flavors || item.flavors.length === 0 || item.dishNumber > 0">
							<!-- 减菜 -->
							<!-- <image v-if="item.dishNumber > 0" src="../../static/btn_red.png"  @click="redDishAction(item, '普通')" class="dish_red"></image> -->
							<image v-if="item.dishNumber >= 1" src="../../static/btn_red.png"  @click="redDishAction(item, '普通')" class="dish_red"></image>
							<!-- <image v-if="item.newCardNumber > 0" src="../../static/btn_red.png"  @click="redDishAction(item, '普通')" class="dish_red"></image> -->
							<text v-if="item.dishNumber > 0" class="dish_number">{{item.dishNumber}}</text>
							<!-- <text v-if="item.newCardNumber > 0" class="dish_number">{{item.newCardNumber}}</text> -->
							<!-- 加菜 -->
							<image src="../../static/btn_add.png" class="dish_add" @click="addDishAction(item, '普通')"></image>
						</view>
						<view class="dish_active_btn" v-else>
							<view class="check_but" @click="moreNormDataesHandle(item)"> 选择规格 </view>
						</view>
					</view>
				</view>
				<view class="seize_seat"></view>
			</scroll-view >
			<view class="no_dish" v-else>
				<view v-if="typeListData.length > 0 ">该分类下暂无菜品</view>
			</view>	
		</view>
		<view class="mask-box"></view>
		<!-- orderListData().dishList.length === 0 -->
		<view class="footer_order_buttom" v-if="orderListData().length === 0">
			<view class="order_number">
				<image src="../../static/btn_waiter_nor.png" class="order_number_icon" mode=""></image>
			</view>
			<view class="order_price">
				 <text class="ico">￥</text> 0
			</view>
			<view class="order_but">
				去结算
			</view>
		</view>
		<!-- 真结算 -->
		<view class="footer_order_buttom order_form" v-else>
			<view class="order_number" @click="() => openOrderCartList = !openOrderCartList">
				<image src="../../static/btn_waiter_sel.png" class="order_number_icon" mode=""></image>
				<view class="order_dish_num"> {{orderDishNumber}} </view>
			</view>
			<view class="order_price">
				 <text class="ico">￥ </text> {{orderDishPrice / 100+6}}
			</view>
			<view class="order_but" @click="goOrder()">
				去结算
			</view>
		</view>
		<!-- 开桌弹框 - start -->
		<!-- <view class="pop_mask " v-show="openTablePop">
			<view class="pop">
				<view class="open_table_cont">
					<view class="cont_tit">
						就餐人数
					</view>
					<view class="people_num_act">
						<image src="../../static/btn_red.png" class="red" @click="setOpenTableNumber('red')" mode=""></image>
						<text class="people_num"> {{ openTablePeoPleNumber }} </text>
						<image src="../../static/btn_add.png" class="add" @click="setOpenTableNumber('add')" mode=""></image>
					</view>
				</view>
				<view class="butList">
					<view class="define" @click="openTableHandle()"> 确定 </view>
				</view>
			</view>
		</view> -->
		<!-- 开桌弹框 - end -->
		<!-- 多规格 - start -->
		<view class="pop_mask " v-show="openMoreNormPop">
			<view class="more_norm_pop">
				<view class="title">
					{{moreNormDishdata.name}}
				</view>
        <scroll-view class="items_cont" scroll-y="true" scroll-top="0rpx">
          <!-- <view class="items_cont"> -->
					<view class="item_row" v-for="(obj, index) in moreNormdata" :key="index">
						<view class="flavor_name">{{obj.name}}</view>
						<view class="flavor_item">
							<view :class="{item: true, act: flavorDataes.findIndex(it => item === it) !== -1}" v-for="(item, ind) in obj.value" :key="ind" @click="checkMoreNormPop(obj.value, item)">
								{{item}}
							</view>
						</view>
					</view>
				<!-- </view> -->
        </scroll-view>
        <view class="but_item">
					<view class="price">
						 <text class="ico"> ￥ </text> {{moreNormDishdata.price / 100}}
					</view>
					<view class="active" v-if="moreNormDishdata.dishNumber && moreNormDishdata.dishNumber > 0">
						<image src="../../static/btn_red.png"  @click="redDishAction(moreNormDishdata, '普通')" class="dish_red" mode=""></image>
						<text class="dish_number">{{moreNormDishdata.dishNumber}}</text>
						<!-- <text class="dish_number">{{item.newCardNumber}}</text> -->
						<image src="../../static/btn_add.png" class="dish_add" @click="addDishAction(moreNormDishdata, '普通')" mode=""></image>
					</view>
					<view class="active" v-else-if="moreNormDishdata.dishNumber == 0">
						<view class="dish_card_add" @click="addDishAction(moreNormDishdata, '普通')"> 加入购物车 </view>
					</view>
				</view>
				<view class="close" @click="closeMoreNorm(moreNormDishdata)">
					<image class="close_img" src="../../static/but_close.png" mode=""></image>
				</view>
			</view>
		</view>
		<!-- 多规格 - end -->
		<!-- 菜品详情 - start -->
		<!-- openDetailHandle 这个函数触发的菜品详情 -->
		<view class="pop_mask " v-show="openDetailPop" style="z-index: 9999;" >
			<view class="dish_detail_pop" v-if="dishDetailes.type == 1">
				<image mode="aspectFill" class="div_big_image" :src="getNewImage(dishDetailes.image)"></image>
				<view class="title">
					{{dishDetailes.name}}
				</view>
				<view class="desc">
					{{dishDetailes.description}}
				</view>
				<view class="but_item">
					<view class="price">
						 <text class="ico"> ￥ </text> {{dishDetailes.price / 100}}
					</view>
					<view class="active" v-if="dishDetailes.dishNumber && dishDetailes.dishNumber > 0">
						<image src="../../static/btn_red.png"  @click="redDishAction(dishDetailes, '普通')" class="dish_red" mode=""></image>
						<text class="dish_number">{{dishDetailes.dishNumber}}</text>
						<!-- <text class="dish_number">{{item.newCardNumber}}</text> -->
						<image src="../../static/btn_add.png" class="dish_add" @click="addDishAction(dishDetailes, '普通')" mode=""></image>
					</view>
					<view class="active" v-else-if="dishDetailes.dishNumber == 0">
						<view class="dish_card_add" @click="addDishAction(dishDetailes, '普通')"> 加入购物车 </view>
					</view>
				</view>
				<view class="close" @click="() => openDetailPop = false">
					<image class="close_img" src="../../static/but_close.png" mode=""></image>
				</view>
			</view>
			<view class="dish_detail_pop" v-else>
				<scroll-view class="dish_items" scroll-y="true" scroll-top="0rpx">
					<view class="dish_item" v-for="(item, index) in dishMealData" :key="index">
						<image class="div_big_image" :src="getNewImage(item.image)" mode=""></image>
						<view class="title">
							{{item.name}}
							<text style="">X{{ item.copies }}</text>
						</view>
						<view class="desc">
							{{item.description}}
						</view>
					</view>
				</scroll-view>
				<view class="but_item">
					<view class="price">
						 <text class="ico"> ￥ </text> {{dishDetailes.price / 100}}
					</view>
					<view class="active" v-if="dishDetailes.dishNumber && dishDetailes.dishNumber > 0">
						<image src="../../static/btn_red.png"  @click="redDishAction(dishDetailes, '普通')" class="dish_red" mode=""></image>
						<text class="dish_number">{{dishDetailes.dishNumber}}</text>
						<!-- <text class="dish_number">{{item.newCardNumber}}</text> -->
						<image src="../../static/btn_add.png" class="dish_add" @click="addDishAction(dishDetailes, '普通')" mode=""></image>
					</view>
					<view class="active" v-else-if="dishDetailes.dishNumber == 0">
						<view class="dish_card_add" @click="addDishAction(dishDetailes, '普通')"> 加入购物车 </view>
					</view>
				</view>
				<view class="close" @click="() => openDetailPop = false">
					<image class="close_img" src="../../static/but_close.png" mode=""></image>
				</view>
			</view>
		</view>
		<!-- 菜品详情 - end -->
		<!-- 购物车弹框 - start -->
		<view class="pop_mask " v-show="openOrderCartList"  @click="openOrderCartList = !openOrderCartList">
			<view class="cart_pop" @click.stop="openOrderCartList = openOrderCartList">
				<view class="top_title">
					<view class="tit"> 购物车 </view>
					<view class="clear" @click.stop="clearCardOrder()"> 
					<image class="clear_icon" src="../../static/clear.png" mode=""></image> 
						<text class="clear-des">清空 </text>	
					</view>
				</view>
				<scroll-view class="card_order_list" scroll-y="true" scroll-top="40rpx">
					<view class="type_item_cont"  v-for="(item, ind) in orderAndUserInfo" :key="ind">
						<view class="type_item"  v-for="(obj, index) in item.dishList" :key="index">
							<view class="dish_img">
								<image mode="aspectFill" :src="getNewImage(obj.image)" class="dish_img_url"></image>
							</view>
							<view class="dish_info">
								<view class="dish_name"> {{ obj.name }} </view>
								<view class="dish_price"> <text class="ico">￥</text> {{ obj.amount / 100 }} </view>
								<view class="dish_active">
									<image v-if="obj.number && obj.number > 0" src="../../static/btn_red.png"  @click.stop="redDishAction(obj, '购物车')" class="dish_red" mode=""></image>
									<text v-if="obj.number && obj.number > 0" class="dish_number">{{obj.number}}</text>
									<image src="../../static/btn_add.png" class="dish_add" @click.stop="addDishAction(obj, '购物车')" mode=""></image>
								</view>
							</view>
						</view>
					</view>
					<view class="seize_seat"></view>
				</scroll-view >
			</view>
		</view>
		<!-- 购物车弹框 - end -->
		<view class="pop_mask" v-show="loaddingSt">
			<view class="lodding">
				<image class="lodding_ico" src="../../static/lodding.gif" mode=""></image>
			</view>
		</view>
	</view>
</template>
<script src="./index.js"></script>
<style src="./style.scss" lang="scss" scoped></style>
