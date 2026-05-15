import navBar from '../common/Navbar/navbar.vue'
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
import {mapState, mapMutations, mapActions} from 'vuex'

export default {
	data () {
		return {
			title: 'Hello',
			openOrderCartList: false,
			typeListData: [],
			dishListData: [],
			dishListItems: [],
			dishDetailes: {},
			openDetailPop: false,
			openMoreNormPop: false,
			moreNormDataes: null,
			tableInfo:null,
			moreNormDishdata:null,
			moreNormdata:null,
			dishMealData:null,
			openTablePeoPleNumber: 1,
			orderData: 0,
			typeIndex: 0,
			openTablePop: false,
			flavorDataes: [],
			orderDishNumber: 0,
			orderDishPrice: 0,
			params: {
				shopId: 'f3deb',
				storeId: '1282344676983062530',
				tableId: '1282346960773238786'
			 },
			rightIdAndType: {}
		}
	},
	computed: {
		orderListDataes: function () {
			return this.orderListData()
		},
		loaddingSt: function () {
			return this.lodding()
		},
		orderAndUserInfo: function () {
			let orderData = []
			Array.isArray(this.orderListDataes) && this.orderListDataes.forEach((n,i) => {
				let userData = {}
				userData.nickName = n.name ?? ''
				userData.avatarUrl = n.image ?? ''
				userData.dishList = [n]
				const num = orderData.findIndex(o => o.nickName == userData.nickName)
				if (num != -1) {
					orderData[num].dishList.push(n)
				} else {
					orderData.push(userData)
				}
			})
			return orderData
		},
		ht: function () {
			return uni.getMenuButtonBoundingClientRect().top + uni.getMenuButtonBoundingClientRect().height + 7
		}
	},
	components: { navBar },
	onLoad (options) {
		uni.onNetworkStatusChange(function(res) {
			if (res.isConnected == false) {
				uni.navigateTo({url: '/pages/nonet/index'})
			}
		})
		if (options) {
			if (!options.status && !options.formOrder) {
				this.getData()
			}
		}
	},
	onShow () {
		this.token() && this.init()
	},
	methods: {
		...mapMutations(['setShopInfo', 'initdishListMut', 'setStoreInfo', 'setBaseUserInfo', 'setLodding', 'setToken']),
		...mapState(['shopInfo', 'orderListData', 'baseUserInfo', 'lodding', 'token']),
		loginSync () {
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
		getData () {
			let res = wx.getMenuButtonBoundingClientRect()
			let _this = this
			this.selectHeight = res.height
			uni.showModal({
				title: '温馨提示',
				content: '亲，授权微信登录后才能正点餐！',
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

		async init () {
			getCategoryList().then(res => {
				if (res && res.code === 1) {
					this.typeListData = [ ...res.data ]
					if (res.data.length > 0){
						this.getDishListDataes(res.data[this.typeIndex || 0])
					}
				}
			})
			this.getTableOrderDishListes()
		},
		async getDishListDataes (params, index) {
			console.log('=-=-=-=-=-=-=getDishListDataes-=-params=-',params)
			this.rightIdAndType = {}
			this.rightIdAndType = {
				id: params.id,
				type: params.type
			}
			const param = {categoryId: params.id,type: params.type, page: 1, pageSize: 1000,status:1}
			if (params.type === 1) {
				await dishListByCategoryId(param).then(res => {
					if (res && res.code === 1) {
						this.dishListData = res.data && res.data.map((obj) => ({ ...obj, type: 1, newCardNumber: 0 }))
					}
				}).catch(err => {
				})
			} else {
				await querySetmeaList(param).then(success => {
					if (success && success.code === 1) {
						this.dishListData = success.data && success.data.map((obj) => ({ ...obj, type: 2, newCardNumber: 0 }))
					}
				}).catch(err => {
				})
			}
			this.typeIndex = index
			this.setOrderNum()
		},
		getNewImage (image) {
			return image
		},
		async getTableOrderDishListes () {
			await getShoppingCartList({}).then(res => {
				if (res.code === 1) {
					this.initdishListMut(res.data)
					this.computOrderInfo()
				}
			}).catch(err => {
			})
		},
		goOrder () {
			uni.navigateTo({url: '/pages/order/index'})
		},
		async addDishAction (item, form) {
			console.log('this.flavorDataes',this.flavorDataes)
			if(this.openMoreNormPop && (!this.flavorDataes || this.flavorDataes.length<=0) ){
				uni.showToast({
					title: '请选择规格',
					icon: 'none',
				})
				return false
			}
			console.log('-=-=-=addDishAction-=-=-')
			if(this.orderListDataes && !this.orderListDataes.some(n => n.id == item.dishId) && this.flavorDataes.length > 0) {
				item.flavorRemark = JSON.stringify(this.flavorDataes)
			}
			let params = {
				amount: item.price,
				dishFlavor: this.flavorDataes.join(','),
				number: 1 || item.dishNumber,
				name: item.name,
				image: item.image
			}
			if (item.type === 1 || item.dishId !== null) {
				params = {
					...params,
					dishId: form === '购物车' ? item.dishId : item.id
				}
			} else {
				params = {
					...params,
					setmealId: form === '购物车' ? item.setmealId : item.id
				}
			}
			newAddShoppingCartAdd(params).then(res => {
				if (res.code === 1) {
					this.openDetailPop = false
					this.openMoreNormPop = false
					this.getTableOrderDishListes()
					this.getDishListDataes(this.rightIdAndType)
				}
			}).catch(err => {
			})
		},
		async redDishAction (item, form) {
			let params = {}
			if (item.type === 1 || item.dishId !== null) {
				params = {
					...params,
					dishId: form === '购物车' ? item.dishId : item.id
				}
			} else {
				params = {
					...params,
					setmealId: form === '购物车' ? item.setmealId : item.id
				}
			}
			await newShoppingCartSub(params).then(res => {
				if (res.code === 1) {
					this.getTableOrderDishListes()
					this.getDishListDataes(this.rightIdAndType)
				}
			}).catch(err => {
			})
		},
		clearCardOrder () {
			delShoppingCart().then(res => {
				this.openOrderCartList = false
				this.getTableOrderDishListes()
				this.getDishListDataes(this.rightIdAndType)
			}).catch(err => {
			})
		},
		openDetailHandle (item) {
			this.dishDetailes = item
			if (item.type === 2) {
				querySetmealDishById({ id: item.id }).then(res => {
					console.log(res)
					if (res.code === 1) {
						this.openDetailPop = true
						this.dishMealData = res.data
					}
				}).catch(err => {
				})
			} else {
				this.openDetailPop = true
			}
		},
		moreNormDataesHandle (item) {
			this.flavorDataes.splice(0)
			this.moreNormDishdata = item
			this.openMoreNormPop = true
			this.moreNormdata = item.flavors.map(obj => ({ ...obj, value: JSON.parse(obj.value) }))
			this.moreNormdata.forEach((item)=>{
				if(item.value && item.value.length>0){
					this.flavorDataes.push(item.value[0])
				}
			})
		},
		checkMoreNormPop (obj, item) {
			let ind
			let findst = obj.some(n => {
				ind = this.flavorDataes.findIndex(o => o == n)
				return ind != -1
			})
			const num = this.flavorDataes.findIndex(it => it == item)
			if (num == -1 && !findst){
				this.flavorDataes.push(item)
			} else if(findst) {
				this.flavorDataes.splice(ind, 1)
				this.flavorDataes.push(item)
			} else {
				this.flavorDataes.splice(num, 1)
			}
		},
		closeMoreNorm (moreNormDishdata) {
			this.flavorDataes.splice(0, this.flavorDataes.length)
			this.openMoreNormPop = false
		},
		computOrderInfo () {
			let oriData = this.orderListDataes
			this.orderDishNumber = this.orderDishPrice = 0
			oriData.map((n, i) => {
				this.orderDishNumber += n.number
				this.orderDishPrice += n.number * n.amount
			})
		},
		setOrderNum () {
			let ODate = this.dishListData
			let CData = this.orderListDataes
			ODate && ODate.map((obj, index) => {
				obj.dishNumber = 0
				CData && CData.forEach((tg, ind) => {
					if (obj.id === tg.dishId) {
						obj.dishNumber = tg.number
					}
				})
			})
			if (this.dishListItems.length == 0) {
				this.dishListItems = ODate
			} else {
				this.dishListItems.splice(0, this.dishListItems.length, ...ODate)
			}
			console.log('-=-=-=-setOrderNum-=-=',this.dishListItems)
		},
	}
}
