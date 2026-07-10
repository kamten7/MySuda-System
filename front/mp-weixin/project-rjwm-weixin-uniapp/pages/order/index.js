import { submitOrderSubmit, getAddressBookDefault, orderPayment } from '../api/api.js'
import { mapState, mapMutations } from 'vuex'

export default {
	data () {
		return {
			platform: 'ios',
			orderDishPrice: 0,
			openPayType: false,
			psersonUrl: '/static/btn_waiter_sel.png',
			nickName: '',
			gender: '0',
			phoneNumber: '',
			address: '',
			remark: '',
			arrivalTime: '',
			addressBookId: '',
			orderDishNumber: 0
		}
	},
	computed: {
		...mapState(['orderListData']),
		orderListDataes() {
			return this.orderListData || []
		}
	},
	onLoad (options) {
		this.initPlatform()
		const info = this.$store.state.baseUserInfo
		if (info) {
			this.psersonUrl = info.avatarUrl || this.psersonUrl
			this.nickName = info.nickName || ''
		}
		this.init()
		this.getHarfAnOur()

		if (options && options.address) {
			this.addressBookId = ''
			const newAddress = JSON.parse(options.address)
			this.address = newAddress.provinceName + newAddress.cityName + newAddress.districtName + newAddress.detail
			this.phoneNumber = newAddress.phone
			this.nickName = newAddress.consignee
			this.gender = newAddress.sex
			this.addressBookId = newAddress.id
		}
		this.getAddressBookDefault()
	},
	methods: {
		...mapMutations(['setAddressBackUrl']),
		init () {
			this.computOrderInfo()
		},
		initPlatform () {
			const res = uni.getDeviceInfo ? uni.getDeviceInfo() : uni.getSystemInfoSync()
			this.platform = res.platform
		},
		getHarfAnOur () {
			const date = new Date()
			date.setTime(date.getTime() + 3600000)
			let hours = date.getHours()
			let minutes = date.getMinutes()
			if (hours < 10) hours = '0' + hours
			if (minutes < 10) minutes = '0' + minutes
			this.arrivalTime = hours + ':' + minutes
		},
		getAddressBookDefault () {
			getAddressBookDefault().then(res => {
				if (res.code === 1) {
					this.addressBookId = ''
					const data = res.data
					this.address = data.provinceName + data.cityName + data.districtName + data.detail
					this.phoneNumber = data.phone
					this.nickName = data.consignee
					this.gender = data.sex
					this.addressBookId = data.id
				}
			}).catch(() => {})
		},
		goAddress () {
			this.setAddressBackUrl('/pages/order/index')
			uni.navigateTo({ url: '/pages/address/address' })
		},
		getNewImage (image) {
			return image
		},
		computOrderInfo () {
			const oriData = this.orderListDataes
			this.orderDishNumber = 0
			this.orderDishPrice = 0
			if (oriData && oriData.length) {
				oriData.forEach(n => {
					this.orderDishPrice += n.number * n.amount
					this.orderDishNumber += n.number
				})
			}
		},
		closeMask () {
			this.openPayType = false
		},
		payOrderHandle () {
			if (!this.address) {
				uni.showToast({ title: '请选择收货地址', icon: 'none' })
				return false
			}
			const params = {
				payMethod: 1,
				addressBookId: this.addressBookId,
				remark: this.remark
			}
			submitOrderSubmit(params).then(res => {
				if (res.code === 1) {
					// 下单成功后自动调用模拟支付
					const orderData = res.data
					orderPayment({
						orderNumber: orderData.orderNumber,
						payMethod: 1
					}).then(() => {
						uni.redirectTo({ url: '/pages/order/success' })
					}).catch(() => {
						uni.redirectTo({ url: '/pages/order/success' })
					})
				} else {
					uni.showToast({ title: res.msg || '操作失败', icon: 'none' })
				}
			}).catch(() => {
				uni.showToast({ title: '提交失败，请重试', icon: 'none' })
			})
		}
	}
}
