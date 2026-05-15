import store from './../store'
import { baseUrl } from './env'

export function request({url='', params={}, method='GET'}) {
	const storeInfo = store.state
	let header = {
		'Accept': 'application/json',
		'Content-Type': 'application/json',
	}
	// JWT 认证：使用 authentication 头部传递 token
	if (storeInfo.token) {
		header['authentication'] = storeInfo.token
	}

	const requestRes = new Promise((resolve, reject) => {
		uni.request({
			url: baseUrl + url,
			data: params,
			header: header,
			method: method,
			success: (res) => {
				const { data } = res
				if (data.code == 200 || data.code === 1) {
					resolve(res.data)
				} else {
					reject(res.data)
				}
			},
			fail: (err) => {
				const error = {data:{msg: err.errMsg || '网络请求失败'}}
				reject(error)
			}
		});
	})
	return requestRes
}
