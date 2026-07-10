<template>
	<view class="address-page">
		<uni-nav-bar @clickLeft="goBack" left-icon="back" leftIcon="arrowleft" title="地址管理" statusBar="true" fixed="true" color="#ffffff" backgroundColor="#1a56db"></uni-nav-bar>
		<view class="address-content">
			<view class="address-list" v-if="addressList && addressList.length > 0">
				<view class="address-card" v-for="(item, index) in addressList" :key="index" @click="choseAddress(index, item)">
					<view class="address-card-top">
						<view class="address-card-info">
							<view class="address-detail-line">
								<text class="address-tag" :class="'tag-' + item.label">{{ getLableVal(item.label) }}</text>
								<text class="address-text">{{ item.provinceName }}{{ item.cityName }}{{ item.districtName }}{{ item.detail }}</text>
							</view>
							<view class="address-contact-line">
								<text class="contact-name">{{ item.sex === '0' ? item.consignee + ' 男士' : item.consignee + ' 女士' }}</text>
								<text class="contact-phone">{{ item.phone }}</text>
							</view>
						</view>
						<view class="address-edit" @click.stop="addOrEdit('编辑', item)">
							<text class="edit-icon">✎</text>
						</view>
					</view>
					<view class="address-card-bottom" @click.stop="getRadio(index, item)">
						<label class="default-label">
							<radio class="default-radio" color="#1a56db" :value="item.id" :checked="item.isDefault === 1" />
							<text>设为默认地址</text>
						</label>
					</view>
				</view>
			</view>
			<view class="address-empty" v-else>
				<view class="empty-icon-circle">
					<text class="empty-emoji">📍</text>
				</view>
				<text class="empty-text">暂无地址</text>
			</view>
		</view>
		<view class="address-bottom-btn">
			<view class="add-btn" @click="addOrEdit('新增')">
				<text class="add-icon">+</text>
				<text>添加收货地址</text>
			</view>
		</view>
	</view>
</template>

<script>
import { queryAddressBookList, putAddressBookDefault } from '../api/api.js'
import { mapState } from 'vuex'
import uniNavBar from '@/components/uni-nav-bar/uni-nav-bar.vue'

export default {
	components: { uniNavBar },
	data () {
		return {
			testValue: true,
			addressList: [],
			formRouter: ''
		}
	},
	onShow () {
		this.getAddressList()
	},
	computed: {
		...mapState(['addressBackUrl'])
	},
	methods: {
		goBack () {
			const pages = getCurrentPages()
				if (pages.length > 1) {
					uni.navigateBack()
				} else {
					uni.switchTab({ url: '/pages/my/my' })
				}
		},
		getLableVal (item) {
			const map = { '1': '公司', '2': '家', '3': '学校' }
			return map[item] || '其他'
		},
		getAddressList () {
			this.testValue = false
			queryAddressBookList().then(res => {
				if (res.code === 1) {
					this.testValue = true
					this.addressList = res.data
				}
			}).catch(() => {})
		},
		addOrEdit (type, item) {
			if (type === '新增') {
				uni.redirectTo({ url: '/pages/addOrEditAddress/addOrEditAddress' })
			} else {
				uni.redirectTo({ url: '/pages/addOrEditAddress/addOrEditAddress?type=编辑&id=' + item.id })
			}
		},
		choseAddress (e, item) {
			this.getRadio(e, item)
		},
		getRadio (e, item) {
			putAddressBookDefault({ id: item.id }).then(res => {
				if (res.code === 1) {
					uni.showToast({ title: '默认地址设置成功', duration: 2000, icon: 'none' })
					this.getAddressList()
					if (this.addressBackUrl !== '/pages/order/index') return
					uni.redirectTo({ url: '/pages/order/index?address=' + JSON.stringify(item) })
				}
			}).catch(() => {})
		}
	}
}
</script>

<style lang="scss" scoped>
.address-page {
	height: 100vh;
	background: $page-bg;
	display: flex;
	flex-direction: column;
}

.address-content {
	flex: 1;
	padding: 16rpx 24rpx;
	overflow-y: auto;
	padding-bottom: 140rpx;
}

.address-card {
	background: #fff;
	border-radius: $radius-md;
	margin-bottom: 16rpx;
	box-shadow: $shadow-card;
	overflow: hidden;
}

.address-card-top {
	display: flex;
	padding: 28rpx 24rpx 16rpx;
	gap: 16rpx;
}

.address-card-info {
	flex: 1;
	overflow: hidden;
}

.address-detail-line {
	display: flex;
	align-items: flex-start;
	gap: 8rpx;
	margin-bottom: 12rpx;
}

.address-tag {
	padding: 2rpx 12rpx;
	border-radius: 4rpx;
	font-size: 22rpx;
	font-weight: 500;
	color: #333;
	flex-shrink: 0;

	&.tag-1 { background: #e1f1fe; }
	&.tag-2 { background: #fef8e7; }
	&.tag-3 { background: #e7fef8; }
}

.address-text {
	font-size: $font-base;
	color: $text-primary;
	line-height: 1.4;
}

.address-contact-line {
	.contact-name, .contact-phone {
		font-size: $font-sm;
		color: $text-tertiary;
	}
	.contact-phone { margin-left: 20rpx; }
}

.address-edit {
	flex-shrink: 0;
	width: 56rpx;
	height: 56rpx;
	display: flex;
	align-items: center;
	justify-content: center;

	.edit-icon { font-size: 32rpx; color: $text-tertiary; }
}

.address-card-bottom {
	padding: 12rpx 24rpx;
	border-top: 1rpx solid $border-light;
}

.default-label {
	display: flex;
	align-items: center;
	font-size: $font-sm;
	color: $text-secondary;
}

.default-radio {
	transform: scale(0.7);
}

/* 空状态 */
.address-empty {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding-top: 200rpx;
}

.empty-icon-circle {
	width: 140rpx;
	height: 140rpx;
	border-radius: 50%;
	background: #f3f4f7;
	display: flex;
	align-items: center;
	justify-content: center;
	margin-bottom: 24rpx;

	.empty-emoji { font-size: 64rpx; }
}

.empty-text {
	font-size: $font-md;
	color: $text-tertiary;
}

/* 底部按钮 */
.address-bottom-btn {
	padding: 20rpx 32rpx;
	background: #fff;
	box-shadow: 0 -2rpx 16rpx rgba(0,0,0,0.06);
	padding-bottom: constant(safe-area-inset-bottom);
	padding-bottom: env(safe-area-inset-bottom);
}

.add-btn {
	display: flex;
	align-items: center;
	justify-content: center;
	gap: 8rpx;
	padding: 22rpx;
	background: linear-gradient(135deg, $brand-primary, $brand-primary-light);
	color: #fff;
	font-size: $font-md;
	font-weight: 600;
	border-radius: $radius-round;
	box-shadow: 0 4rpx 16rpx rgba($brand-primary, 0.3);

	.add-icon { font-size: 36rpx; }

	&:active { transform: scale(0.98); opacity: 0.9; }
}
</style>
