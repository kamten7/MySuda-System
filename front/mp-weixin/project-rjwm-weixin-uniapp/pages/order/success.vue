<template>
	<view class="success_content">
		<!-- <view class="navBar">
			<view class="leftNav" @click="goback()">
				<image class="back" src="../../static/btn_back.png" mode=""></image>
			</view>
			<view class="centerNav">
				{{tableInfo.storeName}}
			</view>
		</view> -->
		<view class="success_info">
			<image class="success_icon" src="../../static/success.png" mode=""></image>
			<view class="success_title">
				下单成功
			</view>
			<view class="word-box">
				<text class="word_bottom">预计<text class="word_date">{{ arrivalTime }}</text>送达</text>
			</view>
			<view class="success_desc">
				后厨已开始疯狂备餐中, 请耐心等待~
			</view>
			<!-- <view class="go_dish" @click="goIndex()">
				继续点餐
			</view> -->
			<!-- 新添加 -->
			<view class="btns">
				<view class="go_dish" @click="goIndex()">
					返回首页
				</view>
				<view class="go_dish" @click="goOrder()">
					查看订单
				</view>
			</view>
			
		</view>
	</view>	
</template>
<script>
	import {mapState, mapMutations, mapActions} from 'vuex'
	export default {
		data () {
			return {
				arrivalTime: ''
			}
		},
		computed: {
			tableInfo:function(){
				return this.shopInfo()
			}
		},
		onLoad () {
			// 获取一小时以后的时间
			this.getHarfAnOur()
		},
		methods: {
			...mapState(['shopInfo']),
			goIndex () {
				// console.log(23)
				uni.navigateTo({
					url: '/pages/index/index?status=' + '不验证'
				})
			},
			goOrder () {
				uni.navigateTo({
					url: '/pages/historyOrder/historyOrder'
				})
			},
			// 获取一小时以后的时间
			getHarfAnOur () {
				const date = new Date()
				date.setTime(date.getTime() + 3600000)
				let hours = date.getHours()
				let minutes = date.getMinutes()
				if (hours < 10) hours = '0' + hours
				if (minutes < 10) minutes = '0' + minutes
				this.arrivalTime = hours + ':' + minutes
			}
		}
	}
</script>
<style src="./../common/Navbar/navbar.scss" lang="scss" scoped></style>
<style lang="scss" scoped>
	.success_content{
		padding-top: 260rpx;
		.success_info{
			text-align: center;
			.success_icon{
				width: 180rpx;
				height: 172rpx;
				text-align: center;
			}
			.success_title{
				font-size: 48rpx;
				font-family: PingFangSC, PingFangSC-Medium;
				font-weight: 500;
				color: #333333;
				margin-top: 10rpx;
				height: 66rpx;
				line-height: 66rpx;
			}
			.success_desc{
				font-size: 32rpx;
				margin-bottom:40rpx;
				font-family: PingFangSC, PingFangSC-Regular;
				font-weight: 400;
				text-align: center;
				color: #666666;
				height: 44rpx;
				line-height: 44rp;
				margin-top: 14rpx;
			}
			.word-box{
				margin-top: 20rpx;
				height: 44rpx;
				line-height: 44rpx;
			}
			.word_bottom{
				font-family: PingFangSC, PingFangSC-Regular;
				font-weight: 400;
				text-align: center;
				font-size: 32rpx;
				color: #666666;
				.word_date{
					color: #333333;
				}
			}
			
			.btns {
				width: 750rpx;
				display: flex;
        justify-content: center;
				.go_dish {
					// flex: 1;
					position: relative;
					font-size: 30rpx;
					// margin: 0 auto;
					width: 200rpx;
					line-height: 72rpx;
					margin-top: 20rpx;
					background: #ffc200;
					border-radius: 36rpx;
          &+ .go_dish {
            margin-left: 40rpx;
          }
				}
			}
			
		}
	}
</style>
 