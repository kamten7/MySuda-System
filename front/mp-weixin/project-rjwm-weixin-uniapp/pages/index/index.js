import DishCard from '@/components/dish-card/dish-card.vue'
import AppEmpty from '@/components/app-empty/app-empty.vue'
import AppLoading from '@/components/app-loading/app-loading.vue'
import {
	userLogin,
	getCategoryList,
	dishListByCategoryId,
	querySetmeaList,
	getShoppingCartList,
	newAddShoppingCartAdd,
	newShoppingCartSub,
	delShoppingCart,
	querySetmealDishById
} from '../api/api.js'
import { mapState, mapMutations } from 'vuex'

export default {
	data() {
		return {
			loading: true,
			typeListData: [],
			dishListData: [],
			dishListItems: [],
			typeIndex: 0,
			openOrderCartList: false,
			openDetailPop: false,
			dishDetailes: {},
			dishMealData: null,
			openMoreNormPop: false,
			moreNormDishdata: null,
			moreNormdata: null,
			flavorDataes: [],
			orderDishNumber: 0,
			orderDishPrice: 0,
			rightIdAndType: {},
		}
	},
	computed: {
		...mapState(['orderListData', 'lodding', 'token']),
		orderListDataes() {
			return this.orderListData || []
		},
		loaddingSt() {
			return this.lodding
		}
	},
	components: {
		DishCard,
		AppEmpty,
		AppLoading
	},
	onLoad(options) {

		uni.onNetworkStatusChange(function (res) {
			if (res.isConnected == false) {
				uni.navigateTo({ url: '/pages/nonet/index' })
			}
		})
		if (options) {
			if (!options.status && !options.formOrder) {
				this.getData()
			}
		}
	},
	onShow() {
		if (this.token) {
			this.init()
		}
	},
	methods: {
		...mapMutations(['setShopInfo', 'initdishListMut', 'setStoreInfo', 'setBaseUserInfo', 'setLodding', 'setToken', 'setCartCount']),

		loginSync() {
			return new Promise((resolve, reject) => {
				uni.login({
					success: (loginRes) => {
						if (loginRes.errMsg === 'login:ok') {
							resolve(loginRes.code)
						}
					}
				})
			})
		},

		getData() {
			const _this = this
			uni.showModal({
				title: '温馨提示',
				content: '亲，授权微信登录后才能正常点餐！',
				showCancel: false,
				success(modalRes) {
					if (modalRes.confirm) {
						_this.loginSync().then(jsCode => {
							uni.getUserProfile({
								desc: '登录',
								success: function (userInfo) {
									_this.setBaseUserInfo(userInfo.rawData)
									userLogin({ code: jsCode }).then(success => {
										if (success.code === 1) {
											success.data && _this.setToken(success.data.token)
											_this.init()
										}
									}).catch(err => {})
								},
								fail: function (err) {}
							})
						})
					}
				}
			})
		},

		async init() {
			this.loading = true
			try {
				const res = await getCategoryList()
				if (res && res.code === 1) {
					this.typeListData = [...res.data]
					if (res.data.length > 0) {
						await this.getDishListDataes(res.data[this.typeIndex || 0])
					}
				}
				await this.getTableOrderDishListes()
			} catch (e) {
				// 静默处理
			}
			this.loading = false
		},

		async getDishListDataes(params, index) {
			this.rightIdAndType = {
				id: params.id,
				type: params.type
			}
			const param = { categoryId: params.id, type: params.type, page: 1, pageSize: 1000, status: 1 }
			try {
				if (params.type === 1) {
					const res = await dishListByCategoryId(param)
					if (res && res.code === 1) {
						this.dishListData = res.data && res.data.map(obj => ({ ...obj, type: 1, dishNumber: 0 }))
					}
				} else {
					const res = await querySetmeaList(param)
					if (res && res.code === 1) {
						this.dishListData = res.data && res.data.map(obj => ({ ...obj, type: 2, dishNumber: 0 }))
					}
				}
				this.typeIndex = index !== undefined ? index : this.typeIndex
				this.setOrderNum()
			} catch (err) {
				// 静默处理
			}
		},

		getNewImage(image) {
			return image
		},

		async getTableOrderDishListes() {
			try {
				const res = await getShoppingCartList({})
				if (res.code === 1) {
					this.initdishListMut(res.data)
					// 更新全局购物车数量
					const count = res.data ? res.data.reduce((sum, item) => sum + (item.number || 0), 0) : 0
					this.setCartCount(count)
					this.computOrderInfo()
				}
			} catch (err) {}
		},

		goOrder() {
			if (this.orderDishNumber === 0) return
			uni.navigateTo({ url: '/pages/order/index' })
		},

		async addDishAction(item, form) {
			if (this.openMoreNormPop && (!this.flavorDataes || this.flavorDataes.length <= 0)) {
				uni.showToast({ title: '请选择规格', icon: 'none' })
				return false
			}
			let params = {
				amount: item.price,
				dishFlavor: this.flavorDataes.length > 0 ? this.flavorDataes.join(',') : '',
				number: 1,
				name: item.name,
				image: item.image
			}
			if (item.type === 1 || item.dishId !== null) {
				params = { ...params, dishId: form === '购物车' ? item.dishId : item.id }
			} else {
				params = { ...params, setmealId: form === '购物车' ? item.setmealId : item.id }
			}
			try {
				const res = await newAddShoppingCartAdd(params)
				if (res.code === 1) {
					this.openDetailPop = false
					this.openMoreNormPop = false
					this.flavorDataes.splice(0)
					await this.getTableOrderDishListes()
					await this.getDishListDataes(this.rightIdAndType)
				}
			} catch (err) {}
		},

		async redDishAction(item, form) {
			let params = {}
			if (item.type === 1 || item.dishId !== null) {
				params = { dishId: form === '购物车' ? item.dishId : item.id }
			} else {
				params = { setmealId: form === '购物车' ? item.setmealId : item.id }
			}
			try {
				const res = await newShoppingCartSub(params)
				if (res.code === 1) {
					await this.getTableOrderDishListes()
					await this.getDishListDataes(this.rightIdAndType)
				}
			} catch (err) {}
		},

		clearCardOrder() {
			delShoppingCart().then(res => {
				this.openOrderCartList = false
				this.getTableOrderDishListes()
				this.getDishListDataes(this.rightIdAndType)
			}).catch(err => {})
		},

		openDetailHandle(item) {
			this.dishDetailes = item
			if (item.type === 2) {
				querySetmealDishById({ id: item.id }).then(res => {
					if (res.code === 1) {
						this.openDetailPop = true
						this.dishMealData = res.data
					}
				}).catch(err => {})
			} else {
				this.openDetailPop = true
			}
		},

		moreNormDataesHandle(item) {
			this.flavorDataes.splice(0)
			this.moreNormDishdata = item
			this.openMoreNormPop = true
			this.moreNormdata = item.flavors.map(obj => ({ ...obj, value: JSON.parse(obj.value) }))
			this.moreNormdata.forEach(item => {
				if (item.value && item.value.length > 0) {
					this.flavorDataes.push(item.value[0])
				}
			})
		},

		checkMoreNormPop(obj, item) {
			let ind
			let findst = obj.some(n => {
				ind = this.flavorDataes.findIndex(o => o == n)
				return ind != -1
			})
			const num = this.flavorDataes.findIndex(it => it == item)
			if (num == -1 && !findst) {
				this.flavorDataes.push(item)
			} else if (findst) {
				this.flavorDataes.splice(ind, 1)
				this.flavorDataes.push(item)
			} else {
				this.flavorDataes.splice(num, 1)
			}
		},

		closeMoreNorm(moreNormDishdata) {
			this.flavorDataes.splice(0, this.flavorDataes.length)
			this.openMoreNormPop = false
		},

		computOrderInfo() {
			const oriData = this.orderListDataes
			this.orderDishNumber = 0
			this.orderDishPrice = 0
			if (oriData && oriData.length) {
				oriData.forEach(n => {
					this.orderDishNumber += n.number
					this.orderDishPrice += n.number * n.amount
				})
			}
		},

		setOrderNum() {
			const ODate = this.dishListData
			const CData = this.orderListDataes
			if (ODate && ODate.length) {
				ODate.forEach(obj => {
					obj.dishNumber = 0
					if (CData && CData.length) {
						CData.forEach(tg => {
							if (obj.id === tg.dishId) {
								obj.dishNumber = tg.number
							}
						})
					}
				})
			}
			if (this.dishListItems.length === 0) {
				this.dishListItems = ODate
			} else {
				this.dishListItems.splice(0, this.dishListItems.length, ...(ODate || []))
			}
		}
	}
}
