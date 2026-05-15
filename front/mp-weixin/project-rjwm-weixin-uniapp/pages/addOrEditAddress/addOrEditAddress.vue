<template>
  <view class="customer-box">
    <uni-nav-bar @clickLeft="goBack" left-icon="back" leftIcon="arrowleft" :title="delId?'编辑收获地址':'新增收获地址'" statusBar="true" fixed="true" color="#ffffff" backgroundColor="#E94E3C"></uni-nav-bar>
    <view class="add_edit" :style="{'height': `calc(100% - ${statusBarHeight} - 44px)`}">
      <form class="form_address">
        <view class="uni-form-item uni-column form_item">
          <view class="title">联系人:</view>
          <input class="uni-input" placeholder-class="uni-place" v-model="form.name" placeholder="请输入联系人" maxlength="5" />
          <view class="radio">
            <view class="radio-item" v-for="(item, index) in items" :key="item.value" @click="sexChangeHandle(item.value)">
              <image v-if="item.value!=form.sex" class="radio-img" src="../../static/icon-radio.png"></image>
              <image v-else class="radio-img" src="../../static/icon-radio-selected.png"></image>
              <text class="radio-label">{{item.name}}</text>
              <!-- <image src="../../static/icon-radio-selected.png"></image> -->
            </view>
          </view>
          <!-- <radio-group @change="radioChange" class="radio">
            <label class="uni-list-cell uni-list-cell-pd" v-for="(item, index) in items" :key="item.value">
              <radio class="radio_item" :value="item.value" :checked="index === Number(form.radio)">
              {{ item.name }}
              </radio>
            </label>
          </radio-group> -->
        </view>
        <view class="uni-form-item uni-column form_item">
          <view class="title">手机号:</view>
          <input class="uni-input" v-model="form.phone" type="number" placeholder-class="uni-place" placeholder="请输入手机号" maxlength="11" />
        </view>
        <!-- <view class="uni-form-item uni-column form_item">
          <view class="title">收货地址:</view>
          <input class="uni-input" v-model="form.address" placeholder="请输入收货地址" maxlength="40" />
        </view> -->
        <view class="uni-form-item uni-column form_item">
          <view class="title">所在地区:</view>
          <!-- 联动省市县 -->
          <view class="update-input">
            <input v-if="showInput" class="uni-input" placeholder-class="uni-place" @tap="openAddres" v-model="address"/>
          </view>
        </view>
        <!-- 详细地址 -->
        <textarea class="detail" :class="{'detail-ios':platform=='ios'}" placeholder-class="uni-place" v-model="form.detail" placeholder="详细地址:如道路,门牌号"></textarea>
        <simple-address ref="simpleAddress" :pickerValueDefault="cityPickerValueDefault" @onConfirm="onConfirm" themeColor='#007AFF'></simple-address>
        <view class="uni-form-item uni-column form_item tag-box">
          <view class="title">标签:</view>
          <text :class="{ 'active': form.type === item.type }" class="tag_text" v-for="(item) in options" :key="item.type" @click="getTextOption(item)">{{ item.name }}</text>
        </view>
      </form>
      
      <view class="add_address">
        <button class="add_btn" type="primary" plain="true" @click="addAddressFun()">
          保存地址
        </button>
        <button v-if="showDel" class="del_btn" type="default" plain="true" @click="deleteAddressFun()">
          删除地址
        </button>
      </view>
    </view>
  </view>
</template>

<script>
import simpleAddress from '../common/simple-address/simple-address.nvue'
import { addAddressBook, delAddressBook, queryAddressBookById, editAddressBook } from '../api/api.js'
import uniNavBar from '@/components/uni-nav-bar/uni-nav-bar.vue'

export default {
	components: {
		simpleAddress,
    uniNavBar
	},
	data () {
		return {
			platform: 'ios',
			showDel: false,
			showInput: true,
			valueMan: true,
			valueWoman: true,
			items: [
				{
					value: '0',
					name: '男士'
				},
				{
					value: '1',
					name: '女士'
				}
			],
			current: 0,
			options: [
				{
					name: '公司',
					type: 1
				},
				{
					name: '家',
					type: 2
				},
				{
					name: '学校',
					type: 3
				}
			],
			// type: 1,
			form: {
				name: '',
				phone: '',
				// address: '',
				type: 1,
				// radio: 0,
        sex: '0',
				provinceCode: '11',
				provinceName: '',
				cityCode: '1101',
				cityName: '',
				districtCode: '110102',
				districtName: '',
				detail: ''
			},
			// 联动省市县
			// 弹框的初始值
			cityPickerValueDefault: [0, 0, 1],
			pickerText:'',
			// 初始值
			address:'北京市-市辖区-西城区',
			// 保存将要删除的
			delId: ''
		}
	},
	onLoad (options) {
		this.init()
		if (options && options.type === '编辑') {
			this.delId = ''
			this.showDel = true
			uni.setNavigationBarTitle({
			    title: '编辑收获地址'
			})
			// 保存将要删除的id
			this.delId = options.id
			// 查询详情的接口
			this.queryAddressBookById(options.id)
		} else {
			this.showDel = false
		}
	},
	onUnload () {
		uni.removeStorage({
			key: 'edit'
		})
	},
	computed:{
	   statusBarHeight(){
	     return (uni.getWindowInfo ? uni.getWindowInfo().statusBarHeight : uni.getSystemInfoSync().statusBarHeight) + 'px'
	   }
	},
	methods: {
		init(){
			const res = uni.getDeviceInfo ? uni.getDeviceInfo() : uni.getSystemInfoSync();
			this.platform = res.platform
		},
    goBack(){
      uni.redirectTo({
        url: '/pages/address/address'
      })
    },
		// 查询地址详情接口
		queryAddressBookById (id) {
			queryAddressBookById({ id }).then(res =>{
				if (res.code === 1) {
					this.form = {
						provinceCode: res.data.provinceCode,
						cityCode: res.data.cityCode,
						districtCode: res.data.districtCode,
						phone: res.data.phone,
						name: res.data.consignee,
						sex: res.data.sex,
						type: Number(res.data.label),
						detail: res.data.detail,
						id: res.data.id
					}
					if (res.data.provinceName && res.data.cityName && res.data.districtName) {
						this.address = res.data.provinceName + '-' + res.data.cityName + '-' + res.data.districtName
					}
				}
			})
		},
		openAddres () {
			this.$refs.simpleAddress.open()
			// this.showInput = false
			uni.hideKeyboard()
		},
		onConfirm (e) {
			this.form.provinceCode = e.provinceCode
			this.form.cityCode = e.cityCode
			this.form.districtCode = e.areaCode
			// 把选择的地址回显到input框中
			this.address = e.label
		},
		bindTextAreaBlur: function (e) {
			console.log(e.detail.value)
		},
		radioChange (e) {
			if (e.detail.value === 'man') {
				this.form.radio = 0
			} else {
				this.form.radio = 1
			}
		},
    sexChangeHandle(val){
      this.form.sex = val
    },
		// 保存地址
		addAddressFun () {
			if (this.form.name === '') {
				return uni.showToast({
					title:'联系人不能为空',
					duration: 1000,
					icon: 'none'
				})
			} else if (this.form.phone === '') {
				return uni.showToast({
					title:'手机号不能为空',
					duration: 1000,
					icon: 'none'
				})
			} else if (this.form.type === '') {
				return uni.showToast({
					title:'所属标签不能为空',
					duration: 1000,
					icon: 'none'
				})
			} else if (this.address === '') {
				return uni.showToast({
					title:'所在地区不能为空',
					duration: 1000,
					icon: 'none'
				})
			}
			
			if (this.form.phone) {
				const reg = /^(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\d{8}$/
				if (!reg.test(this.form.phone)) {
					return uni.showToast({
						title:'手机号输入有误',
						duration: 1000,
						icon: 'none'
					})
				}
			}
			const params = {
				...this.form,
				// sex: this.form.radio,
				label: this.form.type,
				consignee: this.form.name,
				provinceName: this.address.split('-')[0],
				cityName: this.address.split('-')[1],
				districtName: this.address.split('-')[2]
			}
			// 编辑
			if (this.showDel) {
				editAddressBook(params).then(res => {
					if (res.code === 1) {
						uni.redirectTo({
							url: '/pages/address/address'
						})
					}
				})
			} else {
				delete params.id
				addAddressBook(params).then(res => {
					if (res.code === 1) {
						uni.redirectTo({
							url: '/pages/address/address'
						})
					}
				})
			}
			
		},
		// 删除地址
		deleteAddressFun () {
			delAddressBook(this.delId).then(res => {
				if (res.code === 1) {
					uni.redirectTo({
						url: '/pages/address/address'
					})
					uni.showToast({
						title:'地址删除成功',
						duration: 1000,
						icon: 'none'
					})
					this.form.name = ''
					this.form.phone = ''
					this.form.address = ''
					this.form.type = 1
					this.form.radio = 0
					this.form.provinceCode = '11'
					this.form.cityCode = '1101'
					this.form.districtCode = '110102'
				}
			})
		},
		// 标签的事件
		getTextOption (item) {
			this.form.type = item.type
		}
	}
}
</script>

<style lang="scss" scoped>
.add_edit {
	width: 750rpx;
	height: 100%;
	background-color: #FFFDF5;
	.form_address {
		.form_item {
			margin: 0 22rpx;
			height: 110rpx;
			line-height: 110rpx;
			border-bottom: 1px solid #efefef;
			display: flex;
			.title {
				width: 140rpx;
				opacity: 1;
				font-size: 28rpx;
				font-family: PingFangSC, PingFangSC-Medium;
				font-weight: 500;
				text-align: left;
				color: #333333;
				letter-spacing: 0px;
			}
			.uni-input {
				flex: 1;
				height: 110rpx;
				line-height: 110rpx;
			}
			::v-deep .uni-place{
				font-size: 26rpx;
				font-family: PingFangSC, PingFangSC-Regular;
				font-weight: 400;
				color: #999999;
			}
			.radio {
				opacity: 1;
				font-size: 26rpx;
				font-family: PingFangSC, PingFangSC-Regular;
				font-weight: 400;
				text-align: left;
				color: #333333;
				letter-spacing: 0px;
        display: flex;
        padding-right: 20rpx;
        .radio-item{
          display: flex;
          align-items: center;
          &:first-child{
            margin-right: 54rpx;
          }
        }
        .radio-img{
          width: 32rpx;
          height: 32rpx;
          margin-right: 10rpx;
        }
			}
			
			// 标签
			.tag_text {
				width: 68rpx;
				height: 44rpx;
				line-height: 40rpx;
				border: 1px solid #e5e4e4;
				display: inline-block;
				border-radius: 6rpx;
				text-align: center;
				margin-top: 34rpx;
        box-sizing: border-box;
        color: #333333;
        font-size: 24rpx;
				&:nth-child(3) {
					margin: 34rpx 20rpx;
          
				}
        &:nth-child(3){
          &.active{
            background: #fef8e7;
						border: 1px solid #fef8e7;
          }
        }
        &:nth-child(4){
          &.active{
            background: #e7fef8;
						border: 1px solid #e7fef8;
          }
        }
        
			}
			
			.active {
				background: #e1f1fe;
        border: 1px solid #e1f1fe;
			}
		
		}
		// 详细地址
		.detail {
			padding: 20rpx 22rpx;
			width: 100%;
			box-sizing: border-box;
			::v-deep .uni-place{
				font-size: 26rpx;
				font-family: PingFangSC, PingFangSC-Regular;
				font-weight: 400;
				color: #999999;
				text-align: left;
			}
			// color: #999;
		}
		.detail-ios{
			padding: 20rpx 14rpx;
		}
	}

	.tag-box{
		border-top: 1px solid #efefef;
	}
	
	
	.add_address {
		margin: 0 auto;
		.add_btn {
			margin-top: 40rpx;
			width: 668rpx;
			height: 72rpx;
			line-height: 72rpx;
			border-radius: 72rpx;
			background: #ffc200;
			border: 1px solid #ffc200;
			opacity: 1;
			font-size: 30rpx;
			font-family: PingFangSC, PingFangSC-Medium;
			font-weight: 500;
			text-align: center;
			color: #333333;
			letter-spacing: 0px;
			.img_btn {
				width: 44rpx;
				height: 44rpx;
				vertical-align: middle;
				margin-bottom: 8rpx;
			}
		}
		
		.del_btn {
			margin-top: 40rpx;
			opacity: 1;
			background: #f6f6f6;
			border: 1px solid #f6f6f6;
			width: 668rpx;
			height: 72rpx;
			line-height: 72rpx;
			border-radius: 72rpx;
			font-size: 30rpx;
			font-family: PingFangSC, PingFangSC-Medium;
			font-weight: 550;
			text-align: center;
			color: #333333;
			letter-spacing: 0px;
		}
	}
}

.customer-box{
  height: 100vh;
}
</style>