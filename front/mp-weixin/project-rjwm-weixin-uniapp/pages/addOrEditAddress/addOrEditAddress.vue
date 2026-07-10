<template>
	<view class="addredit-page">
		<uni-nav-bar @clickLeft="goBack" left-icon="back" leftIcon="arrowleft" :title="delId ? '编辑收货地址' : '新增收货地址'" statusBar="true" fixed="true" color="#ffffff" backgroundColor="#1a56db"></uni-nav-bar>
		<view class="addredit-content">
			<view class="form-card">
				<view class="form-row">
					<text class="form-label">联系人</text>
					<input class="form-input" placeholder-class="form-placeholder" v-model="form.name" placeholder="请输入联系人" maxlength="5" />
					<view class="form-radio">
						<view class="radio-item" v-for="item in items" :key="item.value" @click="sexChangeHandle(item.value)">
							<view class="radio-circle" :class="{ 'radio-circle--active': item.value == form.sex }">
								<view v-if="item.value == form.sex" class="radio-dot"></view>
							</view>
							<text class="radio-label">{{ item.name }}</text>
						</view>
					</view>
				</view>
				<view class="form-row">
					<text class="form-label">手机号</text>
					<input class="form-input" v-model="form.phone" type="number" placeholder-class="form-placeholder" placeholder="请输入手机号" maxlength="11" />
				</view>
				<view class="form-row" @click="openAddres">
					<text class="form-label">所在地区</text>
					<input class="form-input" disabled placeholder-class="form-placeholder" v-model="address" placeholder="请选择所在地区" />
					<text class="form-arrow">›</text>
				</view>
				<view class="form-row-textarea">
					<textarea class="form-textarea" placeholder-class="form-placeholder" v-model="form.detail" placeholder="详细地址：如道路、门牌号、小区等" :maxlength="100" />
				</view>
			</view>

			<view class="form-card">
				<view class="form-row">
					<text class="form-label">标签</text>
					<view class="tag-list">
						<text
							v-for="item in options"
							:key="item.type"
							class="tag-item"
							:class="{ 'tag-item--active': form.type === item.type }"
							@click="getTextOption(item)"
						>{{ item.name }}</text>
					</view>
				</view>
			</view>

			<simple-address ref="simpleAddress" :pickerValueDefault="cityPickerValueDefault" @onConfirm="onConfirm" themeColor="#1a56db"></simple-address>

			<view class="btn-area">
				<view class="save-btn" @click="addAddressFun">保存地址</view>
				<view class="del-btn" v-if="showDel" @click="deleteAddressFun">删除地址</view>
			</view>
		</view>
	</view>
</template>

<script>
import simpleAddress from '../common/simple-address/simple-address.nvue'
import { addAddressBook, delAddressBook, queryAddressBookById, editAddressBook } from '../api/api.js'
import uniNavBar from '@/components/uni-nav-bar/uni-nav-bar.vue'

export default {
	components: { simpleAddress, uniNavBar },
	data () {
		return {
			platform: 'ios',
			showDel: false,
			showInput: true,
			items: [
				{ value: '0', name: '男士' },
				{ value: '1', name: '女士' }
			],
			options: [
				{ name: '公司', type: 1 },
				{ name: '家', type: 2 },
				{ name: '学校', type: 3 }
			],
			form: {
				name: '', phone: '', type: 1, sex: '0',
				provinceCode: '11', provinceName: '',
				cityCode: '1101', cityName: '',
				districtCode: '110102', districtName: '',
				detail: ''
			},
			cityPickerValueDefault: [0, 0, 1],
			address: '北京市-市辖区-西城区',
			delId: ''
		}
	},
	onLoad (options) {
		this.init()
		if (options && options.type === '编辑') {
			this.showDel = true
			this.delId = options.id
			uni.setNavigationBarTitle({ title: '编辑收货地址' })
			this.queryAddressBookById(options.id)
		}
	},
	methods: {
		init () {
			const res = uni.getDeviceInfo ? uni.getDeviceInfo() : uni.getSystemInfoSync()
			this.platform = res.platform
		},
		goBack () {
			uni.navigateBack()
		},
		queryAddressBookById (id) {
			queryAddressBookById({ id }).then(res => {
				if (res.code === 1) {
					this.form = {
						provinceCode: res.data.provinceCode, cityCode: res.data.cityCode, districtCode: res.data.districtCode,
						phone: res.data.phone, name: res.data.consignee, sex: res.data.sex,
						type: Number(res.data.label), detail: res.data.detail, id: res.data.id
					}
					if (res.data.provinceName && res.data.cityName && res.data.districtName) {
						this.address = res.data.provinceName + '-' + res.data.cityName + '-' + res.data.districtName
					}
				}
			}).catch(() => {})
		},
		openAddres () {
			this.$refs.simpleAddress.open()
			uni.hideKeyboard()
		},
		onConfirm (e) {
			this.form.provinceCode = e.provinceCode
			this.form.cityCode = e.cityCode
			this.form.districtCode = e.areaCode
			this.address = e.label
		},
		sexChangeHandle (val) {
			this.form.sex = val
		},
		getTextOption (item) {
			this.form.type = item.type
		},
		addAddressFun () {
			if (!this.form.name) return uni.showToast({ title: '联系人不能为空', icon: 'none' })
			if (!this.form.phone) return uni.showToast({ title: '手机号不能为空', icon: 'none' })
			if (!this.address) return uni.showToast({ title: '所在地区不能为空', icon: 'none' })

			const reg = /^(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\d{8}$/
			if (!reg.test(this.form.phone)) return uni.showToast({ title: '手机号输入有误', icon: 'none' })

			const params = {
				...this.form, label: this.form.type, consignee: this.form.name,
				provinceName: this.address.split('-')[0],
				cityName: this.address.split('-')[1],
				districtName: this.address.split('-')[2]
			}
			const apiCall = this.showDel ? editAddressBook(params) : addAddressBook(params)
			apiCall.then(res => {
				if (res.code === 1) uni.navigateBack()
			}).catch(() => {})
		},
		deleteAddressFun () {
			delAddressBook(this.delId).then(res => {
				if (res.code === 1) {
					uni.navigateBack()
					uni.showToast({ title: '地址删除成功', icon: 'none' })
				}
			}).catch(() => {})
		}
	}
}
</script>

<style lang="scss" scoped>
.addredit-page {
	height: 100vh;
	background: $page-bg;
}

.addredit-content {
	padding: 16rpx 24rpx;
	padding-top: 100rpx;
}

.form-card {
	background: #fff;
	border-radius: $radius-md;
	margin-bottom: 16rpx;
	box-shadow: $shadow-card;
	overflow: hidden;
}

.form-row {
	display: flex;
	align-items: center;
	padding: 28rpx 24rpx;
	border-bottom: 1rpx solid $border-light;
	gap: 16rpx;
}

.form-label {
	font-size: $font-base;
	font-weight: 500;
	color: $text-primary;
	width: 140rpx;
	flex-shrink: 0;
}

.form-input {
	flex: 1;
	font-size: $font-base;
	color: $text-primary;
}

.form-placeholder {
	font-size: $font-sm;
	color: $text-placeholder;
}

.form-arrow {
	font-size: 36rpx;
	color: #ccc;
	flex-shrink: 0;
}

.form-radio {
	display: flex;
	gap: 36rpx;
	padding-right: 12rpx;
}

.radio-item {
	display: flex;
	align-items: center;
	gap: 8rpx;
}

.radio-circle {
	width: 32rpx;
	height: 32rpx;
	border-radius: 50%;
	border: 2rpx solid #ccc;
	display: flex;
	align-items: center;
	justify-content: center;

	&--active {
		border-color: $brand-primary;
	}
}

.radio-dot {
	width: 16rpx;
	height: 16rpx;
	border-radius: 50%;
	background: $brand-primary;
}

.radio-label {
	font-size: $font-sm;
	color: $text-secondary;
}

.form-row-textarea {
	padding: 24rpx;
}

.form-textarea {
	width: 100%;
	height: 120rpx;
	font-size: $font-base;
	color: $text-primary;
}

.tag-list {
	display: flex;
	gap: 16rpx;
}

.tag-item {
	padding: 10rpx 24rpx;
	background: #f3f4f7;
	border: 1rpx solid $border-light;
	border-radius: $radius-round;
	font-size: $font-sm;
	color: $text-secondary;

	&--active {
		background: $brand-primary-bg;
		border-color: $brand-primary;
		color: $brand-primary;
		font-weight: 500;
	}
}

.btn-area {
	padding: 32rpx 0;
}

.save-btn {
	padding: 24rpx;
	text-align: center;
	background: linear-gradient(135deg, $brand-primary, $brand-primary-light);
	color: #fff;
	font-size: $font-md;
	font-weight: 600;
	border-radius: $radius-round;
	box-shadow: 0 4rpx 16rpx rgba($brand-primary, 0.3);

	&:active { transform: scale(0.98); }
}

.del-btn {
	margin-top: 24rpx;
	padding: 24rpx;
	text-align: center;
	background: #fff;
	color: $text-secondary;
	font-size: $font-md;
	font-weight: 500;
	border-radius: $radius-round;
	border: 1rpx solid $border-light;

	&:active { background: #fee2e2; color: $brand-danger; }
}
</style>
