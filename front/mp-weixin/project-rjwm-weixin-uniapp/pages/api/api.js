import {request} from "../../utils/request.js"

// ===== 用户相关 =====
// 微信登录
export const userLogin = (params) => {
	return request({
		url: '/user/user/login',
		method: 'POST',
		params
	})
}

// ===== 分类相关 =====
// 获取分类列表（type: 1=菜品 2=套餐）
export const getCategoryList = (params) => {
	return request({
		url: '/user/category/list',
		method: 'GET',
		params
	})
}

// ===== 菜品相关 =====
// 根据分类ID查询菜品列表
export const dishListByCategoryId = (params) => {
	return request({
		url: '/user/dish/list',
		method: 'GET',
		params
	})
}

// ===== 套餐相关 =====
// 查询套餐列表
export const querySetmeaList = (params) => {
	return request({
		url: '/user/setmeal/list',
		method: 'GET',
		params
	})
}

// 根据套餐id查询包含的菜品
export const querySetmealDishById = (params) => {
	return request({
		url: `/user/setmeal/dish/${params.id}`,
		method: 'GET'
	})
}

// ===== 购物车相关 =====
// 添加购物车
export const newAddShoppingCartAdd = (params) => {
	return request({
		url: '/user/shoppingCart/add',
		method: 'POST',
		params
	})
}

// 购物车减菜
export const newShoppingCartSub = (params) => {
	return request({
		url: '/user/shoppingCart/sub',
		method: 'POST',
		params
	})
}

// 获取购物车集合
export const getShoppingCartList = (params) => {
	return request({
		url: '/user/shoppingCart/list',
		method: 'GET',
		params
	})
}

// 清空购物车
export const delShoppingCart = () => {
	return request({
		url: '/user/shoppingCart/clean',
		method: 'DELETE'
	})
}

// ===== 地址相关 =====
// 查询地址列表
export const queryAddressBookList = () => {
	return request({
		url: '/user/addressBook/list',
		method: 'GET'
	})
}

// 新增地址
export const addAddressBook = (params) => {
	return request({
		url: '/user/addressBook',
		method: 'POST',
		params
	})
}

// 修改地址
export const editAddressBook = (params) => {
	return request({
		url: '/user/addressBook',
		method: 'PUT',
		params
	})
}

// 查询地址通过id
export const queryAddressBookById = (params) => {
	return request({
		url: `/user/addressBook/${params.id}`,
		method: 'GET'
	})
}

// 设置默认地址
export const putAddressBookDefault = (params) => {
	return request({
		url: '/user/addressBook/default',
		method: 'PUT',
		params
	})
}

// 查询默认地址
export const getAddressBookDefault = () => {
	return request({
		url: '/user/addressBook/default',
		method: 'GET'
	})
}

// 删除地址
export const delAddressBook = (id) => {
	return request({
		url: '/user/addressBook',
		method: 'DELETE',
		params: { id }
	})
}

// ===== 订单相关 =====
// 用户下单
export const submitOrderSubmit = (params) => {
	return request({
		url: '/user/order/submit',
		method: 'POST',
		params
	})
}

// 订单支付（模拟微信支付）
export const orderPayment = (params) => {
	return request({
		url: '/user/order/payment',
		method: 'PUT',
		params
	})
}

// 历史/最近订单查询
export const queryOrderUserPage = (params) => {
	return request({
		url: '/user/order/historyOrders',
		method: 'GET',
		params
	})
}

// 再来一单
export const oneOrderAgain = (params) => {
	return request({
		url: `/user/order/repetition/${params.id}`,
		method: 'POST'
	})
}

// ===== AI 助手相关 =====
// AI 同步聊天
export const aiChat = (params) => {
	return request({
		url: '/user/ai/chat',
		method: 'POST',
		params
	})
}

// AI 获取购物车状态
export const aiGetCartStatus = () => {
	return request({
		url: '/user/ai/cart/status',
		method: 'GET'
	})
}

// AI 清空对话历史
export const aiClearHistory = (sessionId) => {
	return request({
		url: `/user/ai/history/${sessionId}`,
		method: 'DELETE'
	})
}

// ===== 订单详情 =====
// 根据订单ID查询订单详情
export const queryOrderDetailById = (id) => {
	return request({
		url: `/user/order/orderDetail/${id}`,
		method: 'GET'
	})
}

// ===== 订单取消 =====
export const cancelOrder = (id) => {
	return request({
		url: `/user/order/cancel/${id}`,
		method: 'PUT'
	})
}
