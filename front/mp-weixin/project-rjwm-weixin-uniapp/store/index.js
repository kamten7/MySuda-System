import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

const store = new Vuex.Store({
	state: {
		storeInfo: {}, // 店铺请求的id信息
		shopInfo:'',  // 店铺详细信息
		orderListData:[] ,// 购物车列表信息
		baseUserInfo: '', // 存储获取的用户微信的信息（用户名、头像）
		lodding: false,
		token: '',
	    addressBackUrl: '',
	    dishTypeIndex: 0
	},
	mutations: {
		setStoreInfo(state, provider){
			state.storeInfo = provider;
		},
		setShopInfo(state, provider){
			state.shopInfo = provider;
		},
		initdishListMut(state, provider){
			state.orderListData = provider;
		},
		setBaseUserInfo(state, provider){
			state.baseUserInfo = JSON.parse(provider);
		},
		setLodding(state, provider){
			console.log(5656, provider)
			state.lodding = provider;
		},
		setToken(state, provider) {
			state.token = provider
		},
		setAddressBackUrl(state, provider) {
			state.addressBackUrl = provider
		},
	    setDishTypeIndex(state, provider) {
	    	state.dishTypeIndex = provider
	    }
	},
	actions: {

	}
})

export default store
