import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

const store = new Vuex.Store({
	state: {
		storeInfo: {},        // 店铺请求的id信息
		shopInfo: '',          // 店铺详细信息
		orderListData: [],     // 购物车列表信息
		baseUserInfo: '',      // 存储获取的用户微信的信息（用户名、头像）
		lodding: false,
		token: '',
		addressBackUrl: '',
		dishTypeIndex: 0,
		// 新增 AI 相关
		sessionId: '',         // AI 会话ID
		cartCount: 0,          // 购物车实时数量（用于 TabBar 角标和首页底部栏）
		aiMessages: [],        // AI 聊天消息缓存（页面切换时保持）
		aiStreamingTask: null  // AI 流式请求 task（用于中断）
	},
	mutations: {
		setStoreInfo(state, provider) {
			state.storeInfo = provider
		},
		setShopInfo(state, provider) {
			state.shopInfo = provider
		},
		initdishListMut(state, provider) {
			state.orderListData = provider
		},
		setBaseUserInfo(state, provider) {
			state.baseUserInfo = JSON.parse(provider)
		},
		setLodding(state, provider) {
			state.lodding = provider
		},
		setToken(state, provider) {
			state.token = provider
		},
		setAddressBackUrl(state, provider) {
			state.addressBackUrl = provider
		},
		setDishTypeIndex(state, provider) {
			state.dishTypeIndex = provider
		},
		// AI 相关
		setSessionId(state, provider) {
			state.sessionId = provider
		},
		setCartCount(state, provider) {
			state.cartCount = provider
		},
		setAIMessages(state, provider) {
			state.aiMessages = provider
		},
		addAIMessage(state, message) {
			state.aiMessages.push(message)
		},
		updateLastAIMessage(state, content) {
			const msgs = state.aiMessages
			if (msgs.length > 0 && msgs[msgs.length - 1].role === 'ai') {
				msgs[msgs.length - 1].content = content
				msgs[msgs.length - 1].isStreaming = true
			}
		},
		finishLastAIMessage(state) {
			const msgs = state.aiMessages
			if (msgs.length > 0 && msgs[msgs.length - 1].role === 'ai') {
				msgs[msgs.length - 1].isStreaming = false
			}
		},
		clearAIMessages(state) {
			state.aiMessages = []
		},
		setAIStreamingTask(state, task) {
			state.aiStreamingTask = task
		}
	},
	actions: {}
})

export default store
